package ar.com.abimobileapps.uxds;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class for providing contents for user interface
 */
public class Contents {

    /**
     * An array of items.
     */
    public List<ContentsItem> ITEMS = new ArrayList<ContentsItem>();

    /**
     * A map of items, by ID.
     */
    public Map<String, ContentsItem> ITEM_MAP = new HashMap<String, ContentsItem>();

    private Activity callingActivity;

    public Contents(Activity caller)
    {
        callingActivity = caller;

        XmlPullParser parser = Xml.newPullParser();

        try {
            InputStream is =  callingActivity.openFileInput("items.xml");
            parser.setInput(is, "UTF-8");

            for (int eventType = parser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = parser.next()) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equalsIgnoreCase("item")) {
                            ContentsItem item = new ContentsItem(parser.getAttributeValue(0), parser.getAttributeValue(1));
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

        public ContentsItem(String id, String contents) {
            this.id = id;
            this.contents = contents;
        }

        @Override
        public String toString() {
            return contents;
        }
    }

    public void initialize() throws IOException {
        // Verifico si existe fs y si no lo creo
        fsSetup();

        // Setup alarmas
        RcvrBootComplete rcvr = new RcvrBootComplete();
        if (!rcvr.isAlarmSet(callingActivity.getApplicationContext())) {
            rcvr.startAlarmSvc(callingActivity.getApplicationContext());
        }

    }

    private void fsSetup() throws IOException {
        File applicationDir = new File(callingActivity.getFilesDir() + "");

        try {
            // Verifico si hay fs en internal storage
            File itemsFile = new File(applicationDir + "/items.xml");
            FileInputStream is = callingActivity.openFileInput(itemsFile.getName());
            is.close();
            return;
        } catch (FileNotFoundException e) {
            // No hay fs: procedo a grearlo
        } catch (IOException e) {
            return;
        }

        // 1.- Creo el subdirectorio tmp
        File tmpDir = new File(callingActivity.getFilesDir() + "/tmp");
        if(tmpDir.exists()) {
            rmDir(tmpDir);
        }
        if (!tmpDir.mkdir()) throw new IOException("No se pudo crear directorio en internal storage");

        // 2.- Copio el directorio assets en tmpDir
        copyAssets(tmpDir);

        // 3.- Expando el directorio tmpDir en applicationDir
        fsUpdate(applicationDir, tmpDir);
    }

    private void copyAssets(File destDir) throws IOException {
        AssetManager am = callingActivity.getResources().getAssets();
        String[] fileList = am.list("");

        for(String filename : fileList) {
            try {
                InputStream in = am.open(filename);
                OutputStream out = new FileOutputStream(new File(destDir, filename));

                copyFile(in, out);

                in.close();
                out.flush();
                out.close();
            }
            catch (FileNotFoundException e) {
                // Es directorio. No hago nada
            }
        }

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private void fsUpdate(File applicationDir, File tmpDir) throws IOException {
        String[] fileList = tmpDir.list();

        for(String filename : fileList) {

            String extension = getExtension(filename);

            if (extension.equalsIgnoreCase("zip")) {
                // es archivo comprimido, lo expando
                unzip(applicationDir, tmpDir + "/" + filename);
            }
            else {
                // no es archivo comprimido, simplemente lo copio
                FileInputStream fis = new FileInputStream(tmpDir + "/" + filename);
                FileOutputStream fos = new FileOutputStream(applicationDir + "/" + filename);

                byte[] bites = new byte[1024];

                for (int count = fis.read(bites); count != -1; count = fis.read(bites)) {
                    fos.write(bites, 0, count);
                }
            }
        }
    }

    private void unzip(File applicationDir, String zipPathname) throws IOException {
        FileInputStream fis = new FileInputStream(zipPathname);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

        ZipEntry entry;

        while((entry = zis.getNextEntry()) != null) {
            // write the files to the disk

            if (entry.isDirectory()) {
                String dirName = entry.getName().replace("/", "");
                File dir = new File(applicationDir + "/" + dirName);
                if(dir.exists()) {
                    rmDir(dir);
                }
                if (!dir.mkdirs()) throw new IOException("No se pudo crear directorio en internal storage");
            }
            else {
                final int BUFFER = 2048;
                byte data[] = new byte[BUFFER];

                File outputFile = new File(applicationDir + "/" + entry.getName());
                FileOutputStream fos = new FileOutputStream(outputFile.getPath());
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                int count;
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }

                dest.flush();
                dest.close();
            }
        }

        zis.close();
    }

    private String getExtension(String pathname)
    {
        String[] pathElements = pathname.split("/");
        String pathLastElement = pathElements[pathElements.length-1];
        String[] filenameElements = pathLastElement.split("\\.");
        return (filenameElements.length > 1? filenameElements[filenameElements.length-1]: "");
    }

    private void rmDir(File file) throws IOException {
        if (file.isDirectory()) {
            for (String child : file.list()) {
                rmDir(new File(file, child));
            }
        }
        boolean success = file.delete();  // delete child file or empty directory
        if (!success) throw new IOException("No se pudo remover directorio en internal storage");
    }

}
