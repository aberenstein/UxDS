package ar.com.abimobileapps.uxds;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SvcNotification extends Service {

    public SvcNotification() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doWork();
        return Service.START_NOT_STICKY;
    }

    private void doNotify()
    {
        String title = getResources().getString(R.string.app_name_status);
        String mssg = getResources().getString(R.string.mssg_status);

        Intent notificationIntent = new Intent(this, ItemListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,notificationIntent, 0);

        Notification notification = new Notification(R.drawable.status_icon, "New Message", System.currentTimeMillis());
        notification.setLatestEventInfo(getApplicationContext(), title, mssg, pendingIntent);
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private boolean doWork()
    {
        doNotify();
        return true;
    }

}
