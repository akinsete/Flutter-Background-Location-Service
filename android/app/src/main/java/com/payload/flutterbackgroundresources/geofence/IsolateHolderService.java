package com.payload.flutterbackgroundresources.geofence;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import io.flutter.view.FlutterNativeView;

/**
 * Created by sundayakinsete on 2019-03-28.
 */
public class IsolateHolderService extends Service {

    final static String ACTION_SHUTDOWN = "SHUTDOWN";
    private String WAKELOCK_TAG = "IsolateHolderService::WAKE_LOCK";
    private String TAG = "IsolateHolderService";
    private static FlutterNativeView sBackgroundFlutterView = null;
    String CHANNEL_ID = "geofencing_plugin_channel";

    public static void setBackgroundFlutterView(FlutterNativeView view) {
        sBackgroundFlutterView = view;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        int imageId = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());
//
////        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
////            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Flutter Geofencing Plugin", NotificationManager.IMPORTANCE_LOW);
////        }
//
//        Notification.Builder notification = new Notification.Builder(this);
//        notification.setContentTitle("Almost home!")
//                .setContentText("Within 1KM of home. Fine location tracking enabled.")
//                .setSmallIcon(imageId)
//                .setPriority(Notification.PRIORITY_HIGH);
//
//        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
//        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,WAKELOCK_TAG);
//        wakeLock.setReferenceCounted(false);
//        wakeLock.acquire();
//
//        startForeground(1, notification.build());

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_SHUTDOWN)) {
            PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,WAKELOCK_TAG);

            if(wakeLock.isHeld()){
                wakeLock.release();
            }

            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }
}
