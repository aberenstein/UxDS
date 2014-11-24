package ar.com.abimobileapps.uxds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class RcvWakeup extends BroadcastReceiver {

    public RcvWakeup() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, SvcContents.class));
    }
}
