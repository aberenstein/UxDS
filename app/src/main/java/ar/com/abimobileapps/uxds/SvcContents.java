package ar.com.abimobileapps.uxds;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

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

    private boolean doWork()
    {
        Toast.makeText(this, "SvcContents.doWork", Toast.LENGTH_SHORT).show();
        return true;
    }

/// http://www.vogella.com/tutorials/AndroidServices/article.html#scehdulingservices

}
