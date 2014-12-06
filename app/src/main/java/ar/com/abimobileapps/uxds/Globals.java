package ar.com.abimobileapps.uxds;

import android.content.Context;
import android.content.res.AssetManager;
import android.telephony.TelephonyManager;

import java.io.File;

final class Globals {
    private static volatile File appDir = null;
    private static volatile String appId = null;
    private static volatile String deviceId = null;
    private static volatile AssetManager assetManager = null;

    static public String getSentryExtension() {
        return "new";
    }

    static public String getLocalBroadcastName() {
        return "refreshActivity";
    }

    public static File appDir(Context context) {
        if (appDir == null) {
            synchronized (Globals.class) {
                if (appDir == null) {
                    // appDir
                    appDir = new File(context.getFilesDir() + "");

                    // appId
                    appId = context.getResources().getString(R.string.app_id);

                    // assetManager
                    assetManager = context.getResources().getAssets();

                    // deviceId
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    deviceId = telephonyManager.getDeviceId();
                }
            }
        }
        return appDir;
    }

    public static File tmpDir(Context context) {
        return new File(appDir(context) + "/tmp");
    }

    static public String appId(Context context) {
        appDir(context);
        return appId;
    }

    static public String deviceId(Context context) {
        appDir(context);
        return deviceId;
    }

    static public AssetManager assetManager(Context context) {
        appDir(context);
        return assetManager;
    }
}


