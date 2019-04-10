package com.payload.flutterbackgroundresources.locationupdates;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Created by sundayakinsete on 2019-04-07.
 */
public class BackgroundLocationServiceCopy extends Service {

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private NotificationManager mNotificationManager;
    private boolean mGooglePlayServiceAvailable = false;
    private Handler mServiceHandler;
    private final IBinder mBinder = new LocalBinder();
    static boolean mInProgress = false;
    private final String TAG = "BackgroundService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mGooglePlayServiceAvailable = servicesConnected();

        HandlerThread handlerThread = new HandlerThread("TAG");
        handlerThread.start();

        createLocationRequest();

        mServiceHandler = new Handler(handlerThread.getLooper());


        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "BackgroundTest";
            NotificationChannel mChannel = new NotificationChannel("default", name, NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mChannel);
        }

    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "Location:" + location.getLatitude() + " : " + location.getLongitude());
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setInterval(1000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    protected boolean servicesConnected() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);
        return ConnectionResult.SUCCESS == resultCode;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Started requesting location update");

        requestLocationUpdates();

        return START_NOT_STICKY;
    }


    public void requestLocationUpdates() {

        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not request updates. (createLocalCoordinate)" + unlikely);
        }

//            if (mGooglePlayServiceAvailable) {
//                if (!mInProgress) {
//                    mInProgress = true;
//                    if (Build.VERSION.SDK_INT >= 26) {
//                        //startServiceInForeground();
//                        ContextCompat.startForegroundService(getApplicationContext(), new Intent(getApplicationContext(), BackgroundLocationService.class));
//                        Log.i(TAG, "Requesting location updates resumed (createLocalCoordinate) API 26");
//                    } else {
//                        Log.i(TAG, "Requesting location updates resumed (createLocalCoordinate) API < 26");
//                        startService(new Intent(getApplicationContext(), BackgroundLocationService.class));
//                    }
//
//
//                    try {
//                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//                    } catch (SecurityException unlikely) {
//                        Log.e(TAG, "Lost location permission. Could not request updates. (createLocalCoordinate)" + unlikely);
//                    }
//                } else {
//                    Log.i(TAG, "Can't request location, mInProgress is set to true (createLocalCoordinate)");
//                }
//            }
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public BackgroundLocationServiceCopy getService() {
            return BackgroundLocationServiceCopy.this;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("BackgroundService", "Service destroyed");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        stopSelf();
    }
}
