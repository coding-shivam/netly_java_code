import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      home: NativeStuff(),
    );
  }
}

class NativeStuff extends StatefulWidget {
  @override
  NativeStuffState createState() {
    return NativeStuffState();
  }
}

class NativeStuffState extends State<NativeStuff> {
  static const platformMethodChannel = const MethodChannel('mychannel');
  String nativeMessage = '';

  Future<Null> _managePower() async {
    String _message;
    try {
      final String result =
          await platformMethodChannel.invokeMethod('powerManage');
      _message = result;
    } on PlatformException catch (e) {
      _message = "Can't do native stuff ${e.message}.";
    }
    setState(() {
      nativeMessage = _message;
    });
  }

  Future<void> initFunction() async {
    String _message;
    try {
      final String result =
          await platformMethodChannel.invokeMethod('initScanner');
      _message = result;
    } on PlatformException catch (e) {
      _message = "Can't do intiScanner native stuff ${e.message}.";
    }
    setState(() {
      exectionget = _message;
    });
    print("#########################");
    print(exectionget);
    print("#########################");
  }

  String exectionget = ' ';
  Future<Null> _launchCamera() async {
    print("lunch camera called");
    String _message;
    try {
      final String result =
          await platformMethodChannel.invokeMethod('takePhoto');

      _message = result;
      print("{}{}{}{}{}{}{}{}{}{}{}{}");
      print(_message);
      print("{}{}{}{}{}{}{}{}{}{}{}{}");
    } on PlatformException catch (e) {
      _message = "Can't do native stuff ${e.message}.";
    }
    print("{}{}{}{}{}{}   {ERROR}   {}{}{}{}{}");
    print(_message);
    print("{}{}{}{}{}{}{}{}{}{}{}{}");
    setState(() {
      nativeMessage = _message;
    });
    print("lunch camera end");
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Netly Java Code Run"),
      ),
      body: Container(
        color: Colors.redAccent,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            // Padding(
            //   padding: const EdgeInsets.only(left: 8.0, right: 8.0, top: 0.0),
            //   child: Center(
            //     child: FlatButton.icon(
            //       icon: Icon(
            //         Icons.power_settings_new,
            //         size: 100,
            //       ),
            //       label: Text(''),
            //       textColor: Colors.white,
            //       onPressed: _managePower,
            //     ),
            //   ),
            // ),
            Padding(
              padding: const EdgeInsets.only(left: 8.0, right: 8.0, top: 0.0),
              child: Center(
                child: FlatButton.icon(
                  icon: Icon(
                    Icons.power_settings_new,
                    size: 100,
                  ),
                  label: Text('init Function'),
                  textColor: Colors.white,
                  onPressed: initFunction,
                ),
              ),
            ),
            // ignore: unnecessary_null_comparison
            // Text(exectionget == null ? '' : exectionget),
            Divider(),
            Padding(
              padding: const EdgeInsets.only(left: 8.0, right: 8.0, top: 50.0),
              child: Center(
                child: Text(
                  nativeMessage,
                  style: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w500,
                      fontSize: 23.0),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.only(left: 8.0, right: 8.0, top: 102.0),
              child: Center(
                child: FlatButton.icon(
                  icon: Icon(
                    Icons.photo_camera,
                    size: 100,
                  ),
                  label: Text('camera'),
                  textColor: Colors.white,
                  onPressed: _launchCamera,
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.only(left: 8.0, right: 8.0, top: 102.0),
              child: Center(
                child: Text(
                  nativeMessage,
                  style: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w500,
                      fontSize: 23.0),
                ),
              ),
            )
          ],
        ),
      ),
    );
  }
}
