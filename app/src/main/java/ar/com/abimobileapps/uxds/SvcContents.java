package ar.com.abimobileapps.uxds;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

public class SvcContents extends Service {

    public SvcContents() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("UxDS", "SvcContents.onStartCommand");

        // Borra archivos de más de un mes en tmp
        Contents.cleanupTmp();

        if (isNetworkAvailable()) {
            String url = getResources().getString(R.string.contents_url);

            Contents contents = new Contents();
            contents.getContentsFromServer(url, this);
        }

        return Service.START_NOT_STICKY;
    }

    public void doNotify()
    {
        String title = getResources().getString(R.string.app_name_status);
        String mssg = getResources().getString(R.string.mssg_status);
        String mssg_short = getResources().getString(R.string.mssg_status_short);

        Intent notificationIntent = new Intent(this, ItemListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_ONE_SHOT);

        //noinspection deprecation
        Notification notification = new Notification(R.drawable.status_icon, mssg_short, System.currentTimeMillis());
        //noinspection deprecation
        notification.setLatestEventInfo(getApplicationContext(), title, mssg, pendingIntent);
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return (networkInfo != null && networkInfo.isConnected());
    }

}

