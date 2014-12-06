package ar.com.abimobileapps;


import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ar.com.abimobileapps.uxds.ItemListActivity;

public class Utils {

    static String error_rmdir = "No se pudo remover directorio en internal storage";
    static String error_mkdir = "No se pudo crear directorio en internal storage";

    static public void rmDir(File file) throws IOException {
        if (file.isDirectory()) {
            for (String child : file.list()) {
                rmDir(new File(file, child));
            }
        }
        boolean success = file.delete();  // delete child file or empty directory
        if (!success)
            throw new IOException(error_rmdir);
    }

    static public void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    static public void copyFile(File src, File dest) throws IOException {
        FileInputStream fis = new FileInputStream(src);

        if (dest.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dest.delete();
        }

        FileOutputStream fos = new FileOutputStream(dest);

        Utils.copyFile(fis, fos);

        fis.close();
        fos.flush();
        fos.close();
    }

    static public void moveFile(File src, File dest) throws IOException {
        copyFile(src, dest);
        //noinspection ResultOfMethodCallIgnored
        src.delete();
    }

    static public void unzip(File destDir, File zipFile) throws IOException {
        FileInputStream fis = new FileInputStream(zipFile);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

        ZipEntry entry;

        while((entry = zis.getNextEntry()) != null) {
            // write the files to the disk

            if (entry.isDirectory()) {
                String dirName = entry.getName().replaceAll("/+$", "");
                File dir = new File(destDir + "/" + dirName);
                if(dir.exists()) {
                    Utils.rmDir(dir);
                }
                if (!dir.mkdirs()) throw new IOException(error_mkdir);
            }
            else {
                File outputFile = new File(destDir + "/" + entry.getName());
                FileOutputStream fos = new FileOutputStream(outputFile.getPath());

                Utils.copyFile(zis, fos);

                fos.flush();
                fos.close();
            }
        }

        zis.close();
    }

    static public String getExtension(String pathname)
    {
        String filename = new File(pathname).getName();
        String extension = "";
        int pos = filename.lastIndexOf(".");
        if (pos > 0) {
            extension = filename.substring(pos + 1, filename.length());
        }
        return extension;
    }

    static public String getFilename(String pathname)
    {
        String filename = new File(pathname).getName();
        int pos = filename.lastIndexOf(".");
        if (pos > 0) {
            filename = filename.substring(0, pos);
        }
        return filename;
    }

    static public Boolean isActivityRunning(Class activityClass, Context context)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //noinspection deprecation
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }

    static public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return (networkInfo != null && networkInfo.isConnected());
    }

    static public void doNotify(Context context, String title, String mssg, String mssg_short, int icon_id)
    {
        Intent notificationIntent = new Intent(context, ItemListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_ONE_SHOT);

        //noinspection deprecation
        Notification notification = new Notification(icon_id, mssg_short, System.currentTimeMillis());
        //noinspection deprecation
        notification.setLatestEventInfo(context, title, mssg, pendingIntent);
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
