package ar.com.abimobileapps.uxds;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.io.File;

/**
 * Created by AMB on 29/11/2014.
 */
public class Globals {

    public Globals (){
    }

    static public String getSentryExtension() {
        return ".new";
    }

    static public File getApplicationDir(Context c) {
        return new File(c.getFilesDir() + "");
    }

    static public File getTmpDir(Context c) {
        return new File(c.getFilesDir() + "/tmp");
    }

    static public String getAppId(Context c) {
        return c.getResources().getString(R.string.app_id);
    }

    static public String getIMEI(Context c) {
        TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

}
