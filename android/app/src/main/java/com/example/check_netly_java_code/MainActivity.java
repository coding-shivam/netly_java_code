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
  MFS100 mfs100 = null;
  TextView lblMessage;
  EditText txtEventLog;

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
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
    try {
      int ret = mfs100.Init();
      if (ret != 0) {
        SetTextOnUIThread(mfs100.GetErrorMsg(ret));
      } else {
        SetTextOnUIThread("Init success");
        String info = "Serial: " + mfs100.GetDeviceInfo().SerialNo() + " Make: " + mfs100.GetDeviceInfo().Make()
            + " Model: " + mfs100.GetDeviceInfo().Model() + "\nCertificate: " + mfs100.GetCertification();
        SetLogOnUIThread(info);
        myexecption = info;
        // print(myexecption);
      }
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();
      System.out.println(exceptionAsString);
        myexecption = exceptionAsString;
      // print(myexecption);
      // Toast.makeText(getApplicationContext(), "Init failed, unhandled exception",
      // Toast.LENGTH_LONG).show();
      // SetTextOnUIThread("Init failed, unhandled exception");
    }
    return myexecption;
  }

  @Override
  public void OnDeviceAttached(int i, int i1, boolean b) {
    
  }


  @Override
  public void OnDeviceDetached() {

  }

  @Override
  public void OnHostCheckFailed(String s) {

  }
}