package com.example.check_netly_java_code;

import androidx.annotation.NonNull;

import io.flutter.Log;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.plugin.common.MethodChannel;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
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
import java.util.Iterator;
import java.util.Map;

import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FlutterActivity implements MFS100Event {
  private static final String CHANNEL = "mychannel";
  MFS100 mfs100 = null;
  TextView lblMessage;
  EditText txtEventLog;
  private FlutterEngine flutterEngine;
  private static final String TAG = "MainActivity";

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    this.flutterEngine = flutterEngine;
    GeneratedPluginRegistrant.registerWith(flutterEngine);

    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
        .setMethodCallHandler((call, result) -> {
          if (call.method.equals("takePhoto")) {
            readyCamera();

          } else if (call.method.equals("capture")) {
            String deviceStatus = StartSyncCapture();
            String myMessage = deviceStatus;
            result.success(myMessage);
          } else if (call.method.equals("initScanner")) {
            System.out.println("hitting Init function on java file");
            // mfs100 = null;
            String deviceStatus = InitScanner();
            String myMessage = deviceStatus;
            result.success(myMessage);
          }

        });
  }

  private static long mLastClkTime = 0;
  private static long Threshold = 1500;

  private enum ScannerAction {
    Capture, Verify
  }

  byte[] Enroll_Template;
  byte[] Verify_Template;
  private FingerData lastCapFingerData = null;
  ScannerAction scannerAction = ScannerAction.Capture;

  int timeout = 10000;

  private boolean isCaptureRunning = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Log.e("Error" );
    // setContentView(R.layout.activity_mfs100_sample);

    // FindFormControls();
    try {
      this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    } catch (Exception e) {
      Log.e("Error", e.toString());
    }

    try {
      mfs100 = new MFS100(this);
      mfs100.SetApplicationContext(MainActivity.this);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onStart() {
    try {
      if (mfs100 == null) {
        mfs100 = new MFS100(this);
        mfs100.SetApplicationContext(MainActivity.this);
      } else {
        InitScanner();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    super.onStart();
  }

  protected void onStop() {
    try {
      if (isCaptureRunning) {
        int ret = mfs100.StopAutoCapture();
      }
      Thread.sleep(500);
      // UnInitScanner();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    try {
      if (mfs100 != null) {
        mfs100.Dispose();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    super.onDestroy();
  }

  // public void FindFormControls() {
  // try {
  // btnInit = (Button) findViewById(R.id.btnInit);
  // btnUninit = (Button) findViewById(R.id.btnUninit);
  // btnMatchISOTemplate = (Button) findViewById(R.id.btnMatchISOTemplate);
  // btnExtractISOImage = (Button) findViewById(R.id.btnExtractISOImage);
  // btnExtractAnsi = (Button) findViewById(R.id.btnExtractAnsi);
  // btnExtractWSQImage = (Button) findViewById(R.id.btnExtractWSQImage);
  // btnClearLog = (Button) findViewById(R.id.btnClearLog);
  // lblMessage = (TextView) findViewById(R.id.lblMessage);
  // txtEventLog = (EditText) findViewById(R.id.txtEventLog);
  // imgFinger = (ImageView) findViewById(R.id.imgFinger);
  // btnSyncCapture = (Button) findViewById(R.id.btnSyncCapture);
  // btnStopCapture = (Button) findViewById(R.id.btnStopCapture);
  // cbFastDetection = (CheckBox) findViewById(R.id.cbFastDetection);
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
  // }

  // public void onControlClicked(View v) {
  // if (SystemClock.elapsedRealtime() - mLastClkTime < Threshold) {
  // return;
  // }
  // mLastClkTime = SystemClock.elapsedRealtime();
  // try {
  // switch (v.getId()) {
  // case R.id.btnInit:
  // InitScanner();
  // break;
  // case R.id.btnUninit:
  // UnInitScanner();
  // break;
  // case R.id.btnSyncCapture:
  // scannerAction = ScannerAction.Capture;
  // if (!isCaptureRunning) {
  // StartSyncCapture();
  // }
  // break;
  // case R.id.btnStopCapture:
  // StopCapture();
  // break;
  // case R.id.btnMatchISOTemplate:
  // scannerAction = ScannerAction.Verify;
  // if (!isCaptureRunning) {
  // StartSyncCapture();
  // }
  // break;
  // case R.id.btnExtractISOImage:
  // ExtractISOImage();
  // break;
  // case R.id.btnExtractAnsi:
  // ExtractANSITemplate();
  // break;
  // case R.id.btnExtractWSQImage:
  // ExtractWSQImage();
  // break;
  // case R.id.btnClearLog:
  // ClearLog();
  // break;
  // default:
  // break;
  // }
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
  // }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // setContentView(R.layout.activity_mfs100_sample);
    // FindFormControls();
    try {
      if (mfs100 == null) {
        mfs100 = new MFS100(this);
        mfs100.SetApplicationContext(this);
      } /*
         * else { InitScanner(); }
         */
      if (isCaptureRunning) {
        if (mfs100 != null) {
          mfs100.StopAutoCapture();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  CheckBox cbFastDetection;
  String data = " ";

  private String StartSyncCapture() {
    System.out.println("i am hitting sync capture");

    data = " ";
    new Thread(new Runnable() {

      @Override
      public void run() {
        System.out.println("i am in before  try sync capture");

        // SetTextOnUIThread("");
        isCaptureRunning = true;
        try {

          FingerData fingerData = new FingerData();
          System.out.println("i am in try of sync capture");

          int ret = mfs100.AutoCapture(fingerData, timeout, false);
          Log.e("StartSyncCapture.RET", "" + ret);
          System.out.println(ret);

          Log.e("StartSyncCapture.RET", "" + ret);
          if (ret != 0) {
            System.out.println("i am in if of sync capture");
            System.out.println(mfs100.GetErrorMsg(ret));

            // SetTextOnUIThread(mfs100.GetErrorMsg(ret));
          } else {
            System.out.println("i am in else of sync capture");

            lastCapFingerData = fingerData;

            final Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0,
                fingerData.FingerImage().length);

            // MainActivity.this.runOnUiThread(new Runnable() {
            // @Override
            // public void run() {
            // imgFinger.setImageBitmap(bitmap);
            // }
            // });

            Log.e("RawImage", Base64.encodeToString(fingerData.RawData(), Base64.DEFAULT));
            Log.e("FingerISOTemplate", Base64.encodeToString(fingerData.ISOTemplate(), Base64.DEFAULT));
            System.out.println("Capture Success");

            String log = "\nQuality: " + fingerData.Quality() + "\nNFIQ: " + fingerData.Nfiq()
                + "\nWSQ Compress Ratio: " + fingerData.WSQCompressRatio() + "\nImage Dimensions (inch): "
                + fingerData.InWidth() + "\" X " + fingerData.InHeight() + "\"" + "\nImage Area (inch): "
                + fingerData.InArea() + "\"" + "\nResolution (dpi/ppi): " + fingerData.Resolution() + "\nGray Scale: "
                + fingerData.GrayScale() + "\nBits Per Pixal: " + fingerData.Bpp() + "\nWSQ Info: "
                + fingerData.WSQInfo() + "\nFingerImage Info: " + fingerData.FingerImage() + "\nRawData Info: "
                + fingerData.RawData();
            data = log;
            Log.e("fingerData.RawData ", "" + fingerData.RawData());

            System.out.println(log);
            SetData2(fingerData);
          }
        } catch (Exception ex) {
          System.out.println("i am in catch of sync capture");
          System.out.println(ex);

          System.out.println("Error");
          data = "Error from catch block";
        } finally {
          isCaptureRunning = false;
        }
      }
    }).start();
    System.out.println("return data from here");
    return data;
  }

  private void StopCapture() {
    try {
      mfs100.StopAutoCapture();
    } catch (Exception e) {
      System.out.println("Error");
    }
  }

  private void ExtractANSITemplate() {
    try {
      if (lastCapFingerData == null) {
        System.out.println("Finger not capture");
        return;
      }
      byte[] tempData = new byte[2000]; // length 2000 is mandatory
      byte[] ansiTemplate;
      int dataLen = mfs100.ExtractANSITemplate(lastCapFingerData.RawData(), tempData);
      if (dataLen <= 0) {
        if (dataLen == 0) {
          System.out.println("Failed to extract ANSI Template");
        } else {
          System.out.println(mfs100.GetErrorMsg(dataLen));
        }
      } else {
        ansiTemplate = new byte[dataLen];
        System.arraycopy(tempData, 0, ansiTemplate, 0, dataLen);
        WriteFile("ANSITemplate.ansi", ansiTemplate);
        System.out.println("Extract ANSI Template Success");
      }
    } catch (Exception e) {
      Log.e("Error", "Extract ANSI Template Error", e);
    }
  }

  private void ExtractISOImage() {
    try {
      if (lastCapFingerData == null) {
        System.out.println("Finger not capture");
        return;
      }
      byte[] tempData = new byte[(mfs100.GetDeviceInfo().Width() * mfs100.GetDeviceInfo().Height()) + 1078];
      byte[] isoImage;

      // ISOType 1 == Regular ISO Image
      // 2 == WSQ Compression ISO Image
      int dataLen = mfs100.ExtractISOImage(lastCapFingerData.RawData(), tempData, 2);
      if (dataLen <= 0) {
        if (dataLen == 0) {
          System.out.println("Failed to extract ISO Image");
        } else {
          System.out.println(mfs100.GetErrorMsg(dataLen));
        }
      } else {
        isoImage = new byte[dataLen];
        System.arraycopy(tempData, 0, isoImage, 0, dataLen);
        WriteFile("ISOImage.iso", isoImage);
        System.out.println("Extract ISO Image Success");
      }
    } catch (Exception e) {
      Log.e("Error", "Extract ISO Image Error", e);
    }
  }

  private void ExtractWSQImage() {
    try {
      if (lastCapFingerData == null) {
        System.out.println("Finger not capture");
        return;
      }
      byte[] tempData = new byte[(mfs100.GetDeviceInfo().Width() * mfs100.GetDeviceInfo().Height()) + 1078];
      byte[] wsqImage;
      int dataLen = mfs100.ExtractWSQImage(lastCapFingerData.RawData(), tempData);
      if (dataLen <= 0) {
        if (dataLen == 0) {
          System.out.println("Failed to extract WSQ Image");
        } else {
          System.out.println(mfs100.GetErrorMsg(dataLen));
        }
      } else {
        wsqImage = new byte[dataLen];
        System.arraycopy(tempData, 0, wsqImage, 0, dataLen);
        WriteFile("WSQ.wsq", wsqImage);
        System.out.println("Extract WSQ Image Success");
      }
    } catch (Exception e) {
      Log.e("Error", "Extract WSQ Image Error", e);
    }
  }

  private void UnInitScanner() {
    try {
      int ret = mfs100.UnInit();
      if (ret != 0) {
        System.out.println(mfs100.GetErrorMsg(ret));
      } else {
        SetLogOnUIThread("Uninit Success");
        System.out.println("Uninit Success");
        lastCapFingerData = null;
      }
    } catch (Exception e) {
      Log.e("UnInitScanner.EX", e.toString());
    }
  }

  private void WriteFile(String filename, byte[] bytes) {
    try {
      String path = Environment.getExternalStorageDirectory() + "//FingerData";
      File file = new File(path);
      if (!file.exists()) {
        file.mkdirs();
      }
      path = path + "//" + filename;
      file = new File(path);
      if (!file.exists()) {
        file.createNewFile();
      }
      FileOutputStream stream = new FileOutputStream(path);
      stream.write(bytes);
      stream.close();
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  private void WriteFileString(String filename, String data) {
    try {
      String path = Environment.getExternalStorageDirectory() + "//FingerData";
      File file = new File(path);
      if (!file.exists()) {
        file.mkdirs();
      }
      path = path + "//" + filename;
      file = new File(path);
      if (!file.exists()) {
        file.createNewFile();
      }
      FileOutputStream stream = new FileOutputStream(path);
      OutputStreamWriter writer = new OutputStreamWriter(stream);
      writer.write(data);
      writer.flush();
      writer.close();
      stream.close();
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  private void ClearLog() {
    txtEventLog.post(new Runnable() {
      public void run() {
        try {
          txtEventLog.setText("", TextView.BufferType.EDITABLE);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
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

  public void SetData2(FingerData fingerData) {
    try {
      if (scannerAction.equals(ScannerAction.Capture)) {
        Enroll_Template = new byte[fingerData.ISOTemplate().length];
        System.arraycopy(fingerData.ISOTemplate(), 0, Enroll_Template, 0, fingerData.ISOTemplate().length);
      } else if (scannerAction.equals(ScannerAction.Verify)) {
        if (Enroll_Template == null) {
          return;
        }
        Verify_Template = new byte[fingerData.ISOTemplate().length];
        System.arraycopy(fingerData.ISOTemplate(), 0, Verify_Template, 0, fingerData.ISOTemplate().length);
        int ret = mfs100.MatchISO(Enroll_Template, Verify_Template);
        if (ret < 0) {
          System.out.println("Error: " + ret + "(" + mfs100.GetErrorMsg(ret) + ")");
        } else {
          if (ret >= 96) {
            System.out.println("Finger matched with score: " + ret);
          } else {
            System.out.println("Finger not matched, score: " + ret);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      WriteFile("Raw.raw", fingerData.RawData());
      WriteFile("Bitmap.bmp", fingerData.FingerImage());
      WriteFile("ISOTemplate.iso", fingerData.ISOTemplate());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private long mLastAttTime = 0l;

  @Override
  public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {

    if (SystemClock.elapsedRealtime() - mLastAttTime < Threshold) {
      return;
    }
    mLastAttTime = SystemClock.elapsedRealtime();
    int ret;
    if (!hasPermission) {
      System.out.println("Permission denied");
      return;
    }
    try {
      if (vid == 1204 || vid == 11279) {
        if (pid == 34323) {
          ret = mfs100.LoadFirmware();
          if (ret != 0) {
            System.out.println(mfs100.GetErrorMsg(ret));
          } else {
            System.out.println("Load firmware success");
          }
        } else if (pid == 4101) {
          String key = "Without Key";
          ret = mfs100.Init();
          if (ret == 0) {
            showSuccessLog(key);
          } else {
            System.out.println(mfs100.GetErrorMsg(ret));
          }

        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showSuccessLog(String key) {
    try {
      System.out.println("Init success");
      String info = "\nKey: " + key + "\nSerial: " + mfs100.GetDeviceInfo().SerialNo() + " Make: "
          + mfs100.GetDeviceInfo().Make() + " Model: " + mfs100.GetDeviceInfo().Model() + "\nCertificate: "
          + mfs100.GetCertification();
      SetLogOnUIThread(info);
    } catch (Exception e) {
    }
  }

  long mLastDttTime = 0l;

  @Override
  public void OnDeviceDetached() {
    try {

      if (SystemClock.elapsedRealtime() - mLastDttTime < Threshold) {
        return;
      }
      mLastDttTime = SystemClock.elapsedRealtime();
      UnInitScanner();

      System.out.println("Device removed");
    } catch (Exception e) {
    }
  }

  @Override
  public void OnHostCheckFailed(String err) {
    try {
      SetLogOnUIThread(err);
      Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
    } catch (Exception ignored) {
    }
  }

  public void readyCamera() {
    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    startActivity(intent);

  }

  private static final String ACTION_USB_PERMISSION = "com.example.check_netly_java_code";
  private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            if (device != null) {
              // call method to set up device communication
            }
          } else {
            Log.d(TAG, "permission denied for device " + device);
          }
        }
      }
    }
  };

  // private void SetTextOnUIThread(final String str) {

  // lblMessage.post(new Runnable() {
  // public void run() {
  // try {
  // lblMessage.setText(str);
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
  // }
  // });
  // }

  // private void SetLogOnUIThread(final String str) {
  //
  // txtEventLog.post(new Runnable() {
  // public void run() {
  // try {
  // txtEventLog.append("\n" + str);
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
  // }
  // });
  // }

  private String InitScanner() {

    String myexecption = "null";
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
      // int ret = mfs100.Init();

      // String info = "working every thing good";
      // myexecption = info;
      //
      //
      //
      //
      try {
        int ret = mfs100.Init();
        System.out.println("hitting Init() in try block");
        System.out.println(ret);
        System.out.println(ret);
        System.out.println(ret);
        System.out.println(ret);
        System.out.println("print ret done.....");
        if (ret != 0) {
          System.out.println(" ******* ret != 0 in IF condition******** ");
          System.out.println(" *******  ");
          //
          //
          //
          //
          //
          //
          //

          UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
          Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
          intent.addCategory("android.hardware.usb.action.USB_DEVICE_DETACHED");
          Map<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
          System.out.println("Deivce List Size: " + usbDeviceList.size());
          Log.w("USB DEVICES = ", usbDeviceList.toString());
          Iterator<UsbDevice> deviceIterator = usbDeviceList.values().iterator();
          Log.w("USB DEVICES = ", String.valueOf(usbDeviceList.size()));

          if (usbDeviceList.size() > 0) {
            while (deviceIterator.hasNext()) {
              UsbDevice device = deviceIterator.next();
              System.out.println("device pid: " + device.getProductId() + " Device vid: " + device.getVendorId()
                  + " Device ManufacturerName: ");

              // vid= vendor id .... pid= product id
            }

          }
          //
          //
          //
          //
          //
          //
          //

          // SetTextOnUIThread(mfs100.GetErrorMsg(ret));
          System.out.println(mfs100.GetErrorMsg(ret));

          System.out.println("Init failed, ********* On ret != 0 *********");

        } else {
          System.out.println(" ******* in ELSE condition ********* ");

          System.out.println("Init success");
          String info = "Serial: " + mfs100.GetDeviceInfo().SerialNo() + " Make: " + mfs100.GetDeviceInfo().Make()
              + " Model: " + mfs100.GetDeviceInfo().Model() + "\nCertificate: " + mfs100.GetCertification();
          // SetTextOnUIThread(info);
          myexecption = info;
          System.out.println(info);

        }
      } catch (Exception e) {
        System.out.println("in catch bolck");

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        System.out.println(exceptionAsString);
        myexecption = "Init failed, unhandled exception";
        System.out.println(" Init failed, unhandled exception ");

      }

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

  // @Override
  // public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
  //
  // int ret;
  // if (!hasPermission) {
  // // SetTextOnUIThread("Permission denied");
  // System.out.println("Permission denied");
  //
  // return;
  // }
  // if (vid == 1204 || vid == 11279) {
  // if (pid == 34323) {
  // ret = mfs100.LoadFirmware();
  // if (ret != 0) {
  // System.out.println("OnDeviceAttached ret != 0 ");
  //
  // // SetTextOnUIThread(mfs100.GetErrorMsg(ret));
  // System.out.println(mfs100.GetErrorMsg(ret));
  //
  // } else {
  // // SetTextOnUIThread("Load firmware success");
  // System.out.println("Load firmware success");
  //
  // }
  // } else if (pid == 4101) {
  // String key = "Without Key";
  // ret = mfs100.Init();
  // if (ret == 0) {
  // showSuccessLog(key);
  // } else {
  // System.out.println("OnDeviceAttached pid == 4101");
  //
  // // SetTextOnUIThread(mfs100.GetErrorMsg(ret));
  // System.out.println(mfs100.GetErrorMsg(ret));
  //
  // }
  //
  // }
  // }
  // }

  // private void showSuccessLog(String key) {
  // // SetTextOnUIThread("Init success");
  // System.out.println("Init success");
  //
  // String info = "\nKey: " + key + "\nSerial: " +
  // mfs100.GetDeviceInfo().SerialNo() + " Make: "
  // + mfs100.GetDeviceInfo().Make() + " Model: " + mfs100.GetDeviceInfo().Model()
  // + "\nCertificate: "
  // + mfs100.GetCertification();
  // // SetTextOnUIThread(info);
  // System.out.println(info);
  //
  // }

  // @Override
  // public void OnDeviceDetached() {
  // // SetTextOnUIThread("Device removed");
  // System.out.println("Device removed");
  //
  // }
  //
  // @Override
  // public void OnHostCheckFailed(String s) {
  //
  // try {
  // // SetTextOnUIThread(s);
  // System.out.println(s);
  //
  // } catch (Exception ignored) {
  // }
  // }

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