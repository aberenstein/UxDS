package ar.com.abimobileapps.uxds;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;

public class SvcContents extends Service {

    public SvcContents() {
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
        String mssg_short = getResources().getString(R.string.mssg_status_short);

        Intent notificationIntent = new Intent(this, ItemListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification(R.drawable.status_icon, mssg_short, System.currentTimeMillis());
        notification.setLatestEventInfo(getApplicationContext(), title, mssg, pendingIntent);
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private boolean doWork()
    {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();


        doNotify();
        return true;
    }
/// http://www.vogella.com/tutorials/AndroidServices/article.html#scehdulingservices

}


/*
PARA HACER:

3.- Algoritmo de actualización

4.- Obtención de contenidos por http

5.- Obtención inmediata por interacción con GUI

6.- Señalización de 'Nuevo'



 */