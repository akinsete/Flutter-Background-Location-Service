package com.payload.flutterbackgroundresources.locationupdates;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.FlutterView;

interface BasicLifeCircle {
    void onStop();
    void onPause();
    void onResume();
    void onStart();
}

public class LocationUpdateClient implements MethodChannel.MethodCallHandler, BasicLifeCircle, EventChannel.StreamHandler {

    private final String TAG = LocationUpdateClient.class.getSimpleName();
    private static final String EVENT_CHANNEL_NAME = "com.payload.flutterbackgroundresources/background_location_events";

    private Context mContext;
    private BackgroundLocationService mService = null;
    private boolean mBound = false;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    public LocationUpdateClient(Context context, FlutterView flutterView){
        mContext = context;
//
        if (Utils.requestingLocationUpdates(context)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        EventChannel eventChannel = new EventChannel(flutterView, EVENT_CHANNEL_NAME);
        eventChannel.setStreamHandler(this);

    }

    private void startLocationServiceUpdate() {
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            mService.requestLocationUpdates();
        }
    }

    private void stopLocationService() {
        mService.removeLocationUpdates();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundLocationService.LocalBinder binder = (BackgroundLocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };



    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "startLocationService":
                startLocationServiceUpdate();
                Log.i("LocationUpdateClient","Location monitoring started");
                result.success("started");
                break;
            case "stopLocationService":
                stopLocationService();
                Log.e("LocationUpdateClient","Location monitoring stopped");
                result.success("stopped");
                break;
            default:result.notImplemented();
        }
    }



    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            mContext.unbindService(mServiceConnection);
            mBound = false;
        }
    }

    @Override
    public void onPause() {
        // LocalBroadcastManager.getInstance(mContext).unregisterReceiver(locationServiceReceiver);
    }

    @Override
    public void onResume() {
//        LocalBroadcastManager.getInstance(mContext).registerReceiver(locationServiceReceiver,
//                new IntentFilter(BackgroundLocationService.ACTION_BROADCAST));
    }

    @Override
    public void onStart() {
        mContext.bindService(new Intent(mContext, BackgroundLocationService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(new LocationBroadcastReceiverListener(eventSink),
                new IntentFilter(BackgroundLocationService.ACTION_BROADCAST));
    }

    @Override
    public void onCancel(Object o) {

    }

    private class LocationBroadcastReceiverListener extends BroadcastReceiver {
        private EventChannel.EventSink mEventSink;
        public LocationBroadcastReceiverListener(EventChannel.EventSink eventSink) {
            mEventSink = eventSink;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(BackgroundLocationService.EXTRA_LOCATION);

            //Log.e("onReceive", String.valueOf(location));
            if (location != null) {
                mEventSink.success(Utils.getLocationText(location));
                //Log.i("LocationReceiver: ", Utils.getLocationText(location));
            } else {
                mEventSink.error("Location is not available","","");
            }
        }
    }


    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}

