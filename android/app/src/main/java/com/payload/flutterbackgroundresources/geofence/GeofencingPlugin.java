package com.payload.flutterbackgroundresources.geofence;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * Created by sundayakinsete on 2019-03-28.
 */
public class GeofencingPlugin implements MethodChannel.MethodCallHandler {

    private static Context mContext;
    private Activity mActivity;
    private GeofencingClient mGeofencingClient;

    private static String TAG = "GeofencingPlugin";

    public static String SHARED_PREFERENCES_KEY = "geofencing_plugin_cache";
    public static String CALLBACK_HANDLE_KEY = "callback_handle";
    public static String CALLBACK_DISPATCHER_HANDLE_KEY = "callback_dispatch_handler";
    public static String PERSISTENT_GEOFENCES_KEY = "persistent_geofences";
    public static String PERSISTENT_GEOFENCES_IDS = "persistent_geofences_ids";


    public GeofencingPlugin(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;
        mGeofencingClient = LocationServices.getGeofencingClient(mContext);
    }

    public static void reRegisterAfterReboot(Context context) {
        mContext = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

    }

//    public static void registerWith(PluginRegistry.Registrar registrar) {
//        GeofencingPlugin plugin = new GeofencingPlugin(registrar.activeContext(), registrar.activity());
//        MethodChannel channel = new MethodChannel(registrar.messenger(), "com.payload.flutterbackgroundresources/geofencing_plugin");
//        channel.setMethodCallHandler(plugin);
//    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        ArrayList<Object> arguments = (ArrayList<Object>) methodCall.arguments;
        switch (methodCall.method) {
            case "GeofencingPlugin.initializeService":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 12312);
                }
                initializeService(mContext, arguments);
                result.success(true);
                break;
            case "GeofencingPlugin.registerGeofence":
                registerGeofence(mContext, mGeofencingClient, arguments, result, true);
                break;
            case "GeofencingPlugin.removeGeofence":
                removeGeofence(mContext, mGeofencingClient, arguments, result);
                break;
                default:result.notImplemented();
        }
    }

    private void initializeService(Context context,ArrayList<Object> args) {
        Log.d(TAG, "Initializing GeofencingService");
        long callbackHandle = (long) args.get(0);
        context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                .edit()
                .putLong(CALLBACK_DISPATCHER_HANDLE_KEY, callbackHandle)
                .apply();
    }

    public static void registerGeofence(final Context context, GeofencingClient geofencingClient,
                                        final ArrayList<Object> args, final MethodChannel.Result result, final boolean cache) {

        Log.e("args",String.valueOf(args));
        Log.e("args",String.valueOf(cache));

        Long callbackHandle = (Long) args.get(0);
        final String id = String.valueOf(args.get(1));
        Double lat = (Double) args.get(2);
        Double lng = (Double) args.get(3);
        long radius = Math.round((double)args.get(4));
        int fenceTriggers = (int) args.get(5);
        int initialTriggers = (int) args.get(6);
        int expirationDuration = (int) args.get(7);
        int loiteringDelay = (int) args.get(8);
        int notificationResponsiveness = (int) args.get(9);


        Geofence geofence = new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(lat, lng, radius)
                    .setTransitionTypes(fenceTriggers)
                .setLoiteringDelay(loiteringDelay)
                .setNotificationResponsiveness(notificationResponsiveness)
                .setExpirationDuration(expirationDuration)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_DENIED)) {
            String msg = "'registerGeofence' requires the ACCESS_FINE_LOCATION permission.";
            Log.w(TAG, msg);
            result.error(msg, null, null);
        }


        geofencingClient.addGeofences(getGeofencingRequest(geofence, initialTriggers), getGeofencePendingIndent(context, callbackHandle))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (cache) {
                            Log.i(TAG, "Successfully added geofence");
                            addGeofenceToCache(context, id, args);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private static void addGeofenceToCache(Context context, String id, ArrayList<Object> args) {

    }


    private static GeofencingRequest getGeofencingRequest(Geofence geofence, int initialTrigger) {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(initialTrigger)
                .build();
    }

    private static PendingIntent getGeofencePendingIndent(Context context, Long callbackHandle) {
        Intent intent = new Intent(context, GeofencingBroadcastReceiver.class)
                    .putExtra(CALLBACK_HANDLE_KEY, callbackHandle);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void removeGeofence(final Context context, GeofencingClient geofencingClient, ArrayList<Object> args,
                                final MethodChannel.Result result) {
        List<String> geoFenceIds = new ArrayList<>();
        final String id = String.valueOf(args.get(0));
        geoFenceIds.add(id);
        geofencingClient.removeGeofences(geoFenceIds)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        removeGeofenceFromCache(context, id);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void removeGeofenceFromCache(Context context, String id) {

    }
}
