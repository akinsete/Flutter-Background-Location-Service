import 'dart:isolate';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_background_resources/src/geofence/geofence_export.dart';

class HomeScreen extends StatefulWidget {
  HomeScreen({Key key, this.title}) : super(key: key);
  final String title;

  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return HomeScreenState();
  }

}

class HomeScreenState extends State<HomeScreen> {

  List<String> titles = new List();
  ScrollController _controller = new ScrollController();

  @override
  void initState() {
    super.initState();
    locationEvents.receiveBroadcastStream().listen(_onEvent, onError: _onError);
  }

  void _onEvent(Object event) {
    setState(() {
      titles.add(event);
      _controller.jumpTo(_controller.position.maxScrollExtent);
    });
  }

  void _onError(Object error) {
    print(error);
  }


  String btnStart = 'Start Backgound Location Monitoring';
  String btnStop = 'Location montitoring not running';
  bool monitoring = false;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Flutter Background location Update'),
          ),
          body: Container(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                  mainAxisAlignment: MainAxisAlignment.start,
                  children: <Widget>[
                    Expanded(
                      flex: 2,
                      child: Column(
                        children: <Widget>[
                          Center(
                            child: RaisedButton(
                                child: Text(monitoring ? "Location Monitoring Started" : "Start Backgound Location Monitoring"),
                                onPressed: () => monitoring ? null : locationUpdateService("startLocationService")

                            ),
                          ),
                          Center(
                            child: RaisedButton(
                                child: Text(monitoring ? "Stop Background Location Service" : "Location montitoring not running"),
                                onPressed: () => monitoring ? locationUpdateService("stopLocationService") : null

//                              GeofencingManager.removeGeofenceById('mtv')
                            ),
                          ),
                        ],
                      ),
                    ),
                    Expanded(
                      flex: 1,
                      child: Text(titles.length.toString(),style: TextStyle(color: Colors.black, fontSize: 33,fontWeight: FontWeight.w500)),
                    ),
                    Expanded(
                      flex: 7,
                      child: _myListView(),
                    )
                  ]
              )
          )
      ),
    );
  }

  Widget _myListView() {
    return ListView.builder(
      controller: _controller,
      itemCount: titles.length,
      itemBuilder: (context, index) {
        final item = titles[index];
        return Card(
          child: ListTile(
            title: Text(item),

            onTap: () { //                                  <-- onTap
              setState(() {
                titles.insert(index, 'Planet');
              });
            },

            onLongPress: () { //                            <-- onLongPress

            },

          ),
        );
      },
    );
  }

  static const platform = const MethodChannel('com.payload.flutterbackgroundresources/background_location_methods');
  static const locationEvents = const EventChannel("com.payload.flutterbackgroundresources/background_location_events");

  Future<void> locationUpdateService(String method) async {
    platform.setMethodCallHandler((MethodCall call) async {
      print(call.arguments);
    });


    
    await platform.invokeMethod(method).then((data){
       switch (data.toString()) {
         case "started":
           setState(() {
             monitoring = true;
           });
           break;
         case "stopped":
           setState(() {
             monitoring = false;
           });
           break;
       }
    });
  }

//  Future<void> initPlatformState() async {
//    print('Initializing...');
//    await GeofencingManager.initialize();
//    print('Initialization done');
//  }

}