package ar.com.abimobileapps.uxds;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

public class RcvrBootComplete extends BroadcastReceiver {

    private static final long REPEAT_TIME = 1000 * 30;

    public RcvrBootComplete() {
    }

    public boolean isAlarmSet(Context context)
    {
        Intent intent = new Intent(context, RcvWakeup.class);

        boolean alarmSet = (PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE) != null);

        return alarmSet;
    }

    public void startAlarmSvc(Context context) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 30);
        long startTimeMillis = cal.getTimeInMillis();

        Intent intent = new Intent(context, RcvWakeup.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTimeMillis, REPEAT_TIME, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        startAlarmSvc(context);
    }
}
