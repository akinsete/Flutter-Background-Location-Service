package com.payload.flutterbackgroundresources.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.flutter.view.FlutterMain;

/**
 * Created by sundayakinsete on 2019-03-28.
 */
public class GeofencingBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        FlutterMain.ensureInitializationComplete(context, null);
        GeofencingService.enqueueWork(context, intent);
    }
}
