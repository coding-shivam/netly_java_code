package com.example.check_netly_java_code;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.plugin.common.MethodChannel;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.os.PowerManager;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FlutterActivity implements MFS100Event {
  private static final String CHANNEL = "mychannel";
  MFS100 mfs100;
  TextView lblMessage;
  EditText txtEventLog;
  private FlutterEngine flutterEngine;

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    this.flutterEngine = flutterEngine;
    GeneratedPluginRegistrant.registerWith(flutterEngine);

    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
        .setMethodCallHandler((call, result) -> {
          if (call.method.equals("takePhoto")) {
            readyCamera();

          } else if (call.method.equals("powerManage")) {
            boolean deviceStatus = getDeviceStatus();
            String myMessage = Boolean.toString(deviceStatus);
            result.success(myMessage);
          } else if (call.method.equals("initScanner")) {
            System.out.println("Init success");
            String deviceStatus = InitScanner();
            String myMessage = deviceStatus;
            result.success(myMessage);
          }

        });
  }

  private boolean getDeviceStatus() {
    boolean deviceStatus = false;
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
      deviceStatus = powerManager.isDeviceIdleMode();
    }
    return deviceStatus;
  }

  public void readyCamera() {
    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    startActivity(intent);

  }

  private void SetTextOnUIThread(final String str) {

    lblMessage.post(new Runnable() {
      public void run() {
        try {
          lblMessage.setText(str);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void SetLogOnUIThread(final String str) {

    txtEventLog.post(new Runnable() {
      public void run() {
        try {
          txtEventLog.append("\n" + str);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private String InitScanner() {

    String myexecption = null;
    // try {
    // String info = "enter in try bolck";
    if (mfs100 == null) {
      // String info = "enter in try bolck mfs100 = null";
      // myexecption = info;

      mfs100 = new MFS100(this);
      String info = "enter in try bolck mfs100 = null";
      myexecption = info;

      // mfs100.SetApplicationContext(context);
    } else {
      int ret = mfs100.Init();

      String info = "working every thing good";
      myexecption = info;
      //
      //
      //
      //
      // try {
      // int ret = mfs100.Init();
      // System.out.println("HELLO WORLD");
      // System.out.println(ret);
      // System.out.println(ret);
      // System.out.println(ret);
      // System.out.println(ret);
      // System.out.println("HELLO INIT");
      // if (ret != 0) {
      // SetTextOnUIThread(mfs100.GetErrorMsg(ret));
      // myexecption = "Init failed, ********* On ret != 0 *********";

      // } else {
      // SetTextOnUIThread("Init success");
      // info = "Serial: " + mfs100.GetDeviceInfo().SerialNo() + " Make: " +
      // mfs100.GetDeviceInfo().Make() + " Model: "
      // + mfs100.GetDeviceInfo().Model() + "\nCertificate: " +
      // mfs100.GetCertification();
      // SetTextOnUIThread(info);
      // myexecption = info;

      // }
      // } catch (Exception e) {
      // System.out.println("HELLO WORLD");

      // StringWriter sw = new StringWriter();
      // e.printStackTrace(new PrintWriter(sw));
      // String exceptionAsString = sw.toString();
      // System.out.println(exceptionAsString);
      // myexecption = "Init failed, unhandled exception";
      // // Toast.makeText(context, "Init failed, unhandled exception",
      // // Toast.LENGTH_LONG).show();
      // // SetTextOnUIThread("Init failed, unhandled exception");
      // }

    }
    // } catch (Exception ex) {
    // String info = "enter in catch bolck";
    // myexecption = info;
    // }
    //
    //
    //
    //
    //
    //

    return myexecption;

  }

  @Override
  public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {

    int ret;
    if (!hasPermission) {
      SetTextOnUIThread("Permission denied");
      System.out.println("Permission denied");

      return;
    }
    if (vid == 1204 || vid == 11279) {
      if (pid == 34323) {
        ret = mfs100.LoadFirmware();
        if (ret != 0) {
          System.out.println("OnDeviceAttached  ret != 0 ");

          SetTextOnUIThread(mfs100.GetErrorMsg(ret));
          System.out.println(mfs100.GetErrorMsg(ret));

        } else {
          SetTextOnUIThread("Load firmware success");
          System.out.println("Load firmware success");

        }
      } else if (pid == 4101) {
        String key = "Without Key";
        ret = mfs100.Init();
        if (ret == 0) {
          showSuccessLog(key);
        } else {
          System.out.println("OnDeviceAttached pid == 4101");

          SetTextOnUIThread(mfs100.GetErrorMsg(ret));
          System.out.println(mfs100.GetErrorMsg(ret));

        }

      }
    }
  }

  private void showSuccessLog(String key) {
    SetTextOnUIThread("Init success");
    System.out.println("Init success");

    String info = "\nKey: " + key + "\nSerial: " + mfs100.GetDeviceInfo().SerialNo() + " Make: "
        + mfs100.GetDeviceInfo().Make() + " Model: " + mfs100.GetDeviceInfo().Model() + "\nCertificate: "
        + mfs100.GetCertification();
    SetTextOnUIThread(info);
    System.out.println(info);

  }

  @Override
  public void OnDeviceDetached() {
    SetTextOnUIThread("Device removed");
    System.out.println("Device removed");

  }

  @Override
  public void OnHostCheckFailed(String s) {

    try {
      SetTextOnUIThread(s);
      System.out.println(s);

    } catch (Exception ignored) {
    }
  }

  // private String InitScanner() {
  // // mfs100 = new MFS100(this);

  // String myexecption = null;
  // try {
  // if (mfs100 == null) {
  // mfs100 = new MFS100(this);
  // mfs100.SetApplicationContext(context);

  // // mfs100.SetApplicationContext(MFS100Test.this);
  // } else {
  // try {
  // int ret = mfs100.Init();
  // System.out.println("HELLO WORLD");
  // System.out.println(ret);
  // System.out.println(ret);
  // System.out.println(ret);
  // System.out.println(ret);
  // System.out.println("HELLO SHIVAM");

  // if (ret != 0) {
  // SetTextOnUIThread(mfs100.GetErrorMsg(ret));
  // } else {
  // SetTextOnUIThread("Init success");
  // String info = "Serial: " + mfs100.GetDeviceInfo().SerialNo() + " Make: " +
  // mfs100.GetDeviceInfo().Make()
  // + " Model: " + mfs100.GetDeviceInfo().Model() + "\nCertificate: " +
  // mfs100.GetCertification();
  // SetLogOnUIThread(info);
  // myexecption = info;
  // // print(myexecption);
  // }
  // } catch (Exception e) {
  // System.out.println("HELLO WORLD");

  // StringWriter sw = new StringWriter();
  // e.printStackTrace(new PrintWriter(sw));
  // String exceptionAsString = sw.toString();
  // System.out.println(exceptionAsString);
  // myexecption = exceptionAsString;
  // // print(myexecption);
  // // Toast.makeText(getApplicationContext(), "Init failed, unhandled
  // exception",
  // // Toast.LENGTH_LONG).show();
  // // SetTextOnUIThread("Init failed, unhandled exception");
  // }
  // }
  // } catch (Exception e) {
  // System.out.println("HELLO TEST");
  // StringWriter sw = new StringWriter();
  // e.printStackTrace(new PrintWriter(sw));
  // String exceptionAsString = sw.toString();
  // System.out.println(exceptionAsString);
  // myexecption = exceptionAsString;
  // }
  // return myexecption;
  // }

  // @Override
  // public void OnDeviceAttached(int i, int i1, boolean b) {

  // }

  // @Override
  // public void OnDeviceDetached() {

  // }

  // @Override
  // public void OnHostCheckFailed(String s) {

  // }
}