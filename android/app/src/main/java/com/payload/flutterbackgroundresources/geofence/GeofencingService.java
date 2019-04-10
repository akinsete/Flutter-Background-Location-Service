package com.payload.flutterbackgroundresources.geofence;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterCallbackInformation;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterNativeView;
import io.flutter.view.FlutterRunArguments;

/**
 * Created by sundayakinsete on 2019-03-28.
 */
public class GeofencingService extends JobIntentService implements MethodChannel.MethodCallHandler {

    private MethodChannel mBackgroundChannel;
    private Context mContext;

    private static String TAG = "GeofencingService";
    private static int JOB_ID = (int) UUID.randomUUID().getMostSignificantBits();
    private static FlutterNativeView mBackgroundFlutterView;
    private static final AtomicBoolean sServiceStarted = new AtomicBoolean(false);
    private static PluginRegistry.PluginRegistrantCallback mPluginRegistrantCallback;

    private ArrayDeque queue = new ArrayDeque <List<Object>>();

    @Override
    public void onCreate() {
        super.onCreate();

        startGeofencingService(this);
    }


    public static void enqueueWork(Context context,Intent work) {
        enqueueWork(context, GeofencingService.class, JOB_ID, work);
    }

    public static void setPluginRegistrant(PluginRegistry.PluginRegistrantCallback pluginRegistrantCallback) {
        mPluginRegistrantCallback = pluginRegistrantCallback;
    }

    private void startGeofencingService(Context context) {
        synchronized (sServiceStarted) {
            mContext = context;
            if (mBackgroundFlutterView == null) {
                long callbackHandle = context.getSharedPreferences(
                        GeofencingPlugin.SHARED_PREFERENCES_KEY,
                        Context.MODE_PRIVATE)
                        .getLong(GeofencingPlugin.CALLBACK_DISPATCHER_HANDLE_KEY, 0);

                FlutterCallbackInformation callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle);
                if (callbackInfo == null) {
                    Log.e(TAG, "Fatal: failed to find callback");
                    return;
                }

                Log.i(TAG, "Starting Geofencing Service...");
                mBackgroundFlutterView = new FlutterNativeView(context, true);

                PluginRegistry pluginRegistry = mBackgroundFlutterView.getPluginRegistry();
                mPluginRegistrantCallback.registerWith(pluginRegistry);

                FlutterRunArguments flutterRunArguments = new FlutterRunArguments();
                flutterRunArguments.bundlePath = FlutterMain.findAppBundlePath(context);
                flutterRunArguments.entrypoint = callbackInfo.callbackName;
                flutterRunArguments.libraryPath = callbackInfo.callbackLibraryPath;

                mBackgroundFlutterView.runFromBundle(flutterRunArguments);
                IsolateHolderService.setBackgroundFlutterView(mBackgroundFlutterView);
            }
        }

        mBackgroundChannel = new MethodChannel(mBackgroundFlutterView, "com.payload.flutterbackgroundresources/geofencing_plugin_background");
        mBackgroundChannel.setMethodCallHandler(this);
    }



    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "GeofencingService.initialized":
                synchronized(sServiceStarted) {
                    while (!queue.isEmpty()) {
                       // mBackgroundChannel.invokeMethod("", queue.remove());
                    }
                    sServiceStarted.set(true);
                }
                break;
            case "GeofencingService.promoteToForeground":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(new Intent(mContext, IsolateHolderService.class));
                }
                break;
            case "GeofencingService.demoteToBackground":
                Intent intent = new Intent(mContext, IsolateHolderService.class);
                intent.setAction(IsolateHolderService.ACTION_SHUTDOWN);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(intent);
                }
                break;
             default:
                result.notImplemented();
                break;
        }
        result.success(null);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        long callbackHandle = intent.getLongExtra(GeofencingPlugin.CALLBACK_HANDLE_KEY, 0);
        GeofencingEvent mGeofencingEvent = GeofencingEvent.fromIntent(intent);

        if (mGeofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error:" + mGeofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = mGeofencingEvent.getGeofenceTransition();

        List<Geofence> triggeredGeofences = mGeofencingEvent.getTriggeringGeofences();
        if(triggeredGeofences == null){
            return;
        }


        List<Object> geofenceUpdateList = new ArrayList<>();

        ArrayList<Double> locationList = new ArrayList<>();

        Location location = mGeofencingEvent.getTriggeringLocation();

        locationList.add(location.getLatitude());
        locationList.add(location.getLongitude());

        geofenceUpdateList.add(callbackHandle);
        geofenceUpdateList.add(triggeredGeofences);
        geofenceUpdateList.add(locationList);
        geofenceUpdateList.add(geofenceTransition);


        synchronized(sServiceStarted) {
            if (!sServiceStarted.get()) {
                // Queue up geofencing events while background isolate is starting
                queue.add(geofenceUpdateList);
            } else {
                // Callback method name is intentionally left blank.
                mBackgroundChannel.invokeMethod("", geofenceUpdateList);
            }
        }
    }

}
