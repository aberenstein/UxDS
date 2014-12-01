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
        return ".new";
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
        File tmpDir = new File(appDir(context) + "/tmp");
        return tmpDir;
    }

    public static File appDir() {
        if (appDir == null) {
            synchronized (Globals.class) {
                if (appDir == null) {
                    throw new RuntimeException("Necesita inicializarse antes de usar");
                }
            }
        }
        return appDir;
    }

    public static File tmpDir() {
        File tmpDir = new File(appDir() + "/tmp");
        return tmpDir;
    }

    static public String appId() {
        if (appDir == null) {
            synchronized (Globals.class) {
                if (appDir == null) {
                    throw new RuntimeException("Necesita inicializarse antes de usar");
                }
            }
        }
        return appId;
    }

    static public String deviceId() {
        if (appDir == null) {
            synchronized (Globals.class) {
                if (appDir == null) {
                    throw new RuntimeException("Necesita inicializarse antes de usar");
                }
            }
        }
        return deviceId;
    }

    static public AssetManager assetManager() {
        if (appDir == null) {
            synchronized (Globals.class) {
                if (appDir == null) {
                    throw new RuntimeException("Necesita inicializarse antes de usar");
                }
            }
        }
        return assetManager;
    }
}


