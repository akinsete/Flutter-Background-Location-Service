package com.payload.flutterbackgroundresources;


import com.payload.flutterbackgroundresources.geofence.GeofencingPlugin;
import com.payload.flutterbackgroundresources.geofence.GeofencingService;

import io.flutter.app.FlutterApplication;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.view.FlutterMain;

/**
 * Created by sundayakinsete on 2019-04-02.
 */
public class Application extends FlutterApplication implements PluginRegistry.PluginRegistrantCallback {

    @Override
    public void onCreate() {
        super.onCreate();
        FlutterMain.startInitialization(this);
        GeofencingService.setPluginRegistrant(this);

    }

    @Override
    public void registerWith(PluginRegistry pluginRegistry) {
        GeneratedPluginRegistrant.registerWith(pluginRegistry);
        //xGeofencingPlugin.registerWith(pluginRegistry.registrarFor("com.payload.flutterbackgroundresources/geofencing_plugin"));
    }
}