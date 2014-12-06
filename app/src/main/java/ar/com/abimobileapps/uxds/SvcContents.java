package ar.com.abimobileapps.uxds;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import ar.com.abimobileapps.Utils;

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

        Contents contents = new Contents(this);

        // Borra archivos de más de un mes en tmp
        contents.cleanupTmp();

        if (Utils.isNetworkAvailable(this)) {
            String url = getResources().getString(R.string.contents_url);
            contents.getContentsFromServer(url);
        }

        return Service.START_NOT_STICKY;
    }

}

