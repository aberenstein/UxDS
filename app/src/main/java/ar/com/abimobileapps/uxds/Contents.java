package ar.com.abimobileapps.uxds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.abimobileapps.Utils;

/**
 * Helper class for providing contents for user interface
 */
public class Contents {

    Context context;

    /**
     * An array of items.
     */
    public List<ContentsItem> ITEMS = new ArrayList<ContentsItem>();

    /**
     * A map of items, by ID.
     */
    public Map<String, ContentsItem> ITEM_MAP = new HashMap<String, ContentsItem>();

    public Contents(Context context)
    {
        this.context = context;

        XmlPullParser parser = Xml.newPullParser();

        try {
            File applicationDir = Globals.appDir(context);
            File items = new File(applicationDir, "items.xml");
            InputStream is = new FileInputStream(items.getAbsoluteFile());

            parser.setInput(is, "UTF-8");

            for (int eventType = parser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = parser.next()) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equalsIgnoreCase("item")) {
                            String id = parser.getAttributeValue(0);
                            String contents = parser.getAttributeValue(1);
                            boolean isItemNew = isItemNew(id);
                            ContentsItem item = new ContentsItem(id, contents, isItemNew);
                            addItem(item);
                        }
                        break;

                    default:
                        break;
                }
            }
        }
        catch (IOException e) {
            clear();
        }
        catch (XmlPullParserException e) {
            clear();
        }

    }

    private void addItem(ContentsItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private void clear() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    /**
     * An item representing a piece of contents.
     */
    public class ContentsItem {
        public String id;
        public String contents;
        public boolean flag;

        public ContentsItem(String id, String contents, boolean flag) {
            this.id = id;
            this.contents = contents;
            this.flag = flag;
        }

        @Override
        public String toString() {
            return contents;
        }
    }

    public static class ContentsMeta {
        private enum ContentsType { ITEM, SECTION, RESOURCE }

        public String id;
        public String item;
        public int size;
        public ContentsType type;
        public boolean isNew;

        // Item
        public ContentsMeta(String id, int size, boolean isNew) {
            this.id = id;
            this.item = null;
            this.size = size;
            this.type = ContentsType.ITEM;
            this.isNew = isNew;
        }

        // Section
        public ContentsMeta(String id, String item, int size) {
            this.id = id;
            this.item = item;
            this.size = size;
            this.type = ContentsType.SECTION;
            this.isNew = false;
        }

        // Resource
        public ContentsMeta(String id, int size) {
            this.id = id;
            this.item = null;
            this.size = size;
            this.type = ContentsType.RESOURCE;
            this.isNew = false;
        }

    }

    public void initialize(Activity callingActivity) throws IOException {
        // Verifico si existe fs y si no lo creo
        fsSetup();

        // Setup alarmas
        RcvrBootComplete rcvr = new RcvrBootComplete();
        if (!rcvr.isAlarmSet(callingActivity)) {
            rcvr.startAlarmSvc(callingActivity);
        }

    }

    private synchronized void fsSetup() throws IOException {
        File applicationDir = Globals.appDir(context);

        try {
            // Verifico si hay fs en internal storage
            File items = new File(applicationDir, "items.xml");
            InputStream is = new FileInputStream(items.getAbsoluteFile());
            is.close();
            return;
        } catch (FileNotFoundException e) {
            // No hay fs: procedo a grearlo
        } catch (IOException e) {
            return;
        }

        // 1.- Creo el subdirectorio tmp
        File tmpDir = Globals.tmpDir(context);
        if(tmpDir.exists()) {
            Utils.rmDir(tmpDir);
        }
        if (!tmpDir.mkdir()) throw new IOException("No se pudo crear directorio en internal storage");

        // 2.- Copio el directorio assets en tmpDir
        copyAssets();

        // 3.- Expando el directorio tmpDir en applicationDir
        fsUpdate();

        // 4.- Elimino los archivos .new en applicationDir
        removeSentries();

    }

    private void copyAssets() throws IOException {
        File destDir = Globals.tmpDir(context);
        AssetManager am = Globals.assetManager(context);
        String[] fileList = am.list("");

        for(String filename : fileList) {
            try {
                InputStream in = am.open(filename);
                OutputStream out = new FileOutputStream(new File(destDir, filename));

                Utils.copyFile(in, out);

                in.close();
                out.flush();
                out.close();
            }
            catch (FileNotFoundException e) {
                // Es directorio. No hago nada
            }
        }

    }

    private void setSentry(File sentry)
    {
        try {
            if (!sentry.exists()) {
                //noinspection ResultOfMethodCallIgnored
                sentry.createNewFile();
            }
        }
        catch (IOException ignored) {
        }
    }

    static public synchronized void removeSentry(File sentry)
    {
        if (sentry.exists()) {
            //noinspection ResultOfMethodCallIgnored
            sentry.delete();
        }
    }

    private void removeSentries()
    {
        File appDir = Globals.appDir(context);
        File[] files = appDir.listFiles();
        for (File file: files) {
            if (!file.isDirectory() && Utils.getExtension(file.getName()).equalsIgnoreCase("new")) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

    public synchronized void cleanupTmp()
    {
        File tmpDir = Globals.tmpDir(context);
        File[] files = tmpDir.listFiles();
        for (File file: files) {
            if (!file.isDirectory()) {
                long lastModified = file.lastModified();
                long now = System.currentTimeMillis();
                long monthMillis = 1000L * 3600L * 24L * 30L;
                if ((now - lastModified) > monthMillis) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }
    }

    /**
     * A partir item id devuelve si está señalado como nuevo o no
     */
    private boolean isItemNew(String id) {
        File applicationDir = Globals.appDir(context);

        File[] fileList = applicationDir.listFiles();

        for (File file: fileList) {
            if (file.isDirectory()) continue;

            String name = Utils.getFilename(file.getAbsolutePath());
            String ext = Utils.getExtension(file.getAbsolutePath());
            if (name.equalsIgnoreCase(id) && ext.equalsIgnoreCase(Globals.getSentryExtension())) {
                return true;
            }
        }

        return false;
    }

    /**
     * A partir del nombre del archivo se obtiene el id de sección y de ahí, mediante items.xml, se
     * obtiene el id del item que lo invoca.
     */
    private String getItemId(File dir, String filename) throws IOException, XmlPullParserException {
        String sectionId = Utils.getFilename(filename);

        File items = new File(dir, "items.xml");
        Map<String,ContentsMeta> contents = parseItems(items);

        for (String id: contents.keySet()) {
            ContentsMeta meta = contents.get(id);
            if (id.equalsIgnoreCase(sectionId) && meta.type == ContentsMeta.ContentsType.SECTION) {
                return meta.item;
            }
        }
        throw new InvalidParameterException();
    }

    public synchronized void fsUpdate() throws IOException {
        File applicationDir = Globals.appDir(context);
        File tmpDir = Globals.tmpDir(context);

        String[] fileList = tmpDir.list();

        for(String filename : fileList) {

            String extension = Utils.getExtension(filename);

            if (extension.equalsIgnoreCase("zip")) {
                // es archivo comprimido, lo expando
                File src = new File(tmpDir, filename);
                Log.d("UxDS", "unzipping " + src.getName());
                Utils.unzip(applicationDir, src);
                //noinspection ResultOfMethodCallIgnored
                src.delete();
                try {
                    String itemId = getItemId(tmpDir, filename);
                    File sentry = new File(applicationDir, itemId + "." + Globals.getSentryExtension());
                    setSentry(sentry);
                }
                catch (Exception ignore) {
                }
            }
            else if (!filename.equalsIgnoreCase("items.xml")) {
                // no es archivo comprimido, simplemente lo muevo
                File src = new File(tmpDir, filename);
                File dest = new File(applicationDir, filename);
                Utils.moveFile(src, dest);
            }
        }

        String filename = "items.xml";
        File src = new File(tmpDir, filename);
        File dest = new File(applicationDir, filename);
        Utils.moveFile(src, dest);
    }

    private Map<String,ContentsMeta> parseItems(File items) throws IOException, XmlPullParserException {

        HashMap<String,ContentsMeta> contentsMeta = new HashMap<String,ContentsMeta>();

        if (items.exists()) {

            XmlPullParser parser = Xml.newPullParser();

            InputStream is =  new FileInputStream(items);
            parser.setInput(is, "UTF-8");

            for (int eventType = parser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = parser.next()) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equalsIgnoreCase("item")) {
                            String id = parser.getAttributeValue(0);
                            int size = Integer.parseInt(parser.getAttributeValue(2));
                            ContentsMeta itemMeta = new ContentsMeta(id, size, isItemNew(id));
                            contentsMeta.put(id, itemMeta);
                        }
                        else if (parser.getName().equalsIgnoreCase("section")) {
                            String id = parser.getAttributeValue(0);
                            String item = parser.getAttributeValue(1);
                            int size = Integer.parseInt(parser.getAttributeValue(2));
                            ContentsMeta itemMeta = new ContentsMeta(id, item, size);
                            contentsMeta.put(id, itemMeta);
                        }
                        else if (parser.getName().equalsIgnoreCase("resource")) {
                            String id = parser.getAttributeValue(0);
                            int size = Integer.parseInt(parser.getAttributeValue(1));
                            ContentsMeta itemMeta = new ContentsMeta(id, size);
                            contentsMeta.put(id, itemMeta);
                        }
                        break;

                    default:
                        break;
                }
            }

        }

        return contentsMeta;
    }

    public void getContentsFromServer(String url) {
        DownloadTask downloadTask = new DownloadTask(context);
        downloadTask.execute(Globals.appDir(context).getAbsolutePath(),
                             Globals.tmpDir(context).getAbsolutePath(),
                             url,
                             Globals.appId(context),
                             Globals.deviceId(context));
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadTask extends AsyncTask<String, Void, Boolean> {
        Context context;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPostExecute(Boolean newData) {
            if (newData) {
                Resources res = context.getResources();
                String title = res.getString(R.string.app_name_status);
                String mssg = res.getString(R.string.mssg_status);
                String mssg_short = res.getString(R.string.mssg_status_short);
                int status_icon_id = R.drawable.status_icon;

                Utils.doNotify(context,
                        title,
                        mssg,
                        mssg_short,
                        status_icon_id);

                refreshActivity();
            }
        }

        private void refreshActivity() {
            if (Utils.isActivityRunning(ItemListActivity.class, context)) {
                Intent intent = new Intent(Globals.getLocalBroadcastName());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                Log.d("UxDS", "sendBroadcast");
            }
        }

        @Override
        protected Boolean doInBackground(String... param) {
            /*
            Actualización de contenido
            1.- Obtengo items.xml del servidor
            2.- Leo items.xml de applicationDir y comparo. Obtengo filesToDownload y filesToDelete. Considerar el caso de que no exista items.xml en applicationDir.
            3.- Descargo el contenido de filesToDownload sobre tmpDir, si todavía no existe en tmpDir.
            4.- Muevo los archivos descargados en tmpDir sobre applicationDir. Si son comprimidos los expando y elimino el archivo comprimido de tmpDir.
            5.- Elimino de applicationDir los archivos y subdirectorios contenidos en filesToDelete.
            */

            String applicationDir = param[0];
            String tmpDir = param[1];
            String url = param[2];
            String appid = param[3];
            String imei = param[4];

            try {
                // items.xml
                Log.d("UxDS", "Traigo items.xml");
                downloadToTmp(null, tmpDir, url, appid, imei);

                Log.d("UxDS", "Obtengo el Map filesToDownload");
                Map<String,ContentsMeta> filesToDownload = getFilesToDownload(applicationDir, tmpDir);

                Log.d("UxDS", "Obtengo el Map filesToDelete");
                Map<String,ContentsMeta> filesToDelete = getFilesToDelete(applicationDir, tmpDir);

                if (!filesToDownload.isEmpty()) {
                    Log.d("UxDS", "Itero sobre filesToDownload");
                    for (String id : filesToDownload.keySet()) {
                        String file;
                        if (filesToDownload.get(id).type == ContentsMeta.ContentsType.ITEM) {
                            file = id + ".html";
                        }
                        else if (filesToDownload.get(id).type == ContentsMeta.ContentsType.SECTION) {
                            file = id + ".zip";
                        }
                        else {
                            file = id;
                        }
                        Log.d("UxDS", "Archivo a descargar:"+file);

                        File pathname = new File(tmpDir, file);
                        if (pathname.exists() && pathname.length() != filesToDownload.get(id).size) {
                            Log.d("UxDS", "No lo tengo. Lo descargo.");
                            downloadToTmp(file, tmpDir, url, appid, imei);
                        }
                        else {
                            Log.d("UxDS", "Ya lo tengo. No lo descargo.");
                        }
                    }

                    Log.d("UxDS", "Pongo los archivos descargados online.");
                    Contents contents = new Contents(context);
                    contents.fsUpdate();
                }
                else {
                    Log.d("UxDS", "filesToDownload es vacío");
                }

                if (!filesToDelete.isEmpty()) {
                    Log.d("UxDS", "Itero sobre filesToDelete");
                    for (String id : filesToDelete.keySet()) {

                        if (filesToDownload.get(id).type == ContentsMeta.ContentsType.ITEM) {
                            File file = new File(id + ".html");
                            Log.d("UxDS", "Elimino archivo:"+file);
                            //noinspection ResultOfMethodCallIgnored
                            file.delete();
                        }
                        else if (filesToDownload.get(id).type == ContentsMeta.ContentsType.SECTION) {
                            File file = new File(id);
                            Log.d("UxDS", "Elimino directorio:"+file);
                            Utils.rmDir(file);
                        }
                        else {
                            File file = new File(id);
                            Log.d("UxDS", "Elimino archivo:"+file);
                            //noinspection ResultOfMethodCallIgnored
                            file.delete();
                        }
                    }
                }
                else {
                    Log.d("UxDS", "filesToDelete es vacío");
                }

                return !filesToDownload.isEmpty();
            }
            catch (Exception e) {
                Log.d("UxDS", Log.getStackTraceString(e));
                return false;
            }
        }

        private Map<String,ContentsMeta> getFilesToDownload(String applicationDir, String tmpDir) throws IOException, XmlPullParserException {

            File itemsDownloaded = new File (applicationDir + "/items.xml");
            File itemsToDownload = new File (tmpDir + "/items.xml");

            Map<String,ContentsMeta> downloadedMeta = parseItems(itemsDownloaded);
            Map<String,ContentsMeta> toDownloadMeta = parseItems(itemsToDownload);

            Map<String,ContentsMeta> toRemove = new HashMap<String,ContentsMeta>();

            for (String id: toDownloadMeta.keySet()) {
                if (downloadedMeta.containsKey(id) &&
                        downloadedMeta.get(id).type == toDownloadMeta.get(id).type &&
                        downloadedMeta.get(id).size == toDownloadMeta.get(id).size) {
                    // el mismo item en ambas colecciones
                    // significa que tal item/section no debe actualizarse
                    // lo eliminamos de toDownloadMeta
                    toRemove.put(id, toDownloadMeta.get(id));
                }
            }

            for (String id: toRemove.keySet()) {
                toDownloadMeta.remove(id);
            }

            // lo que ha quedado en toDownloadMeta es lo que hay que descargar

            return toDownloadMeta;
        }

        private Map<String,ContentsMeta> getFilesToDelete(String applicationDir, String tmpDir) throws IOException, XmlPullParserException {

            File itemsDownloaded = new File (applicationDir + "/items.xml");
            File itemsToDownload = new File (tmpDir + "/items.xml");

            Map<String,ContentsMeta>  downloadedMeta = parseItems(itemsDownloaded);
            Map<String,ContentsMeta>  toDownloadMeta = parseItems(itemsToDownload);

            Map<String,ContentsMeta> toRemove = new HashMap<String,ContentsMeta>();

            for (String id: toDownloadMeta.keySet()) {
                if (downloadedMeta.containsKey(id)) {
                    // downloadedMeta contiene una version de itemToDownload en toDownloadMeta
                    // significa que tal item/section permanece (así o actualizado)
                    // lo eliminamos de downloadedMeta
                    toRemove.put(id, downloadedMeta.get(id));
                }
            }

            for (String id: toRemove.keySet()) {
                downloadedMeta.remove(id);
            }

            // lo que ha quedado en downloadedMeta es lo que hay que eliminar

            return downloadedMeta;
        }

        private void downloadToTmp(String target, String tmpDir, String urlPath, String appid, String imei) throws IOException {

            File output;

            urlPath += "?";
            urlPath += "imei=" + imei;
            urlPath += "&appid=" + appid;

            if (target != null) {
                File file = new File(target);
                urlPath += "&file=" + file.getName();
                output = new File(tmpDir, file.getName());
            }
            else
            {
                output = new File(tmpDir, "items.xml");
            }

            Log.d("UxDS", "downloadToTmp:"+output.getName());

            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            InputStream inputStream = connection.getInputStream();
            OutputStream outputStream = new FileOutputStream(output.getAbsolutePath());

            byte data[] = new byte[1024];
            int total = 0;
            int count;
            while ((count = inputStream.read(data)) != -1) {
                total += count;
                outputStream.write(data, 0, count);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Log.d("UxDS", "Downloaded. Count:"+String.valueOf(total));
        }
    }
}
