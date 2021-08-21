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

import android.os.PowerManager;

public class MainActivity extends FlutterActivity {
  private static final String CHANNEL = "mychannel";

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
}