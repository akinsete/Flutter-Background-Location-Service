package com.payload.flutterbackgroundresources.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by sundayakinsete on 2019-03-28.
 */
public class GeofencingRebootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.e("GEOFENCING REBOOT", "Reregistering geofences!");
            GeofencingPlugin.reRegisterAfterReboot(context);
        }
    }
}
