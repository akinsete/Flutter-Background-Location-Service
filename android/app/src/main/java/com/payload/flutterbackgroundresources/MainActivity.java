package com.payload.flutterbackgroundresources;


import android.os.Bundle;

import com.payload.flutterbackgroundresources.geofence.GeofencingPlugin;
import com.payload.flutterbackgroundresources.locationupdates.LocationUpdateClient;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {

  private LocationUpdateClient locationUpdateClient;
  private static final String LOCATION_UPDATE_CHANNEL = "com.payload.flutterbackgroundresources/background_location_methods";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);

    new MethodChannel(getFlutterView(), "com.payload.flutterbackgroundresources/geofencing_plugin")
            .setMethodCallHandler(new GeofencingPlugin(this, this));

    locationUpdateClient = new LocationUpdateClient(this, getFlutterView());

    MethodChannel methodChannel = new MethodChannel(getFlutterView(), LOCATION_UPDATE_CHANNEL);
    methodChannel.setMethodCallHandler(locationUpdateClient);
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (locationUpdateClient != null) {
      locationUpdateClient.onStart();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (locationUpdateClient != null) {
      locationUpdateClient.onPause();
    }
  }


  @Override
  protected void onResume() {
    super.onResume();
    if (locationUpdateClient != null) {
      locationUpdateClient.onResume();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();

    if (locationUpdateClient != null) {
      locationUpdateClient.onStop();
    }
  }
}

