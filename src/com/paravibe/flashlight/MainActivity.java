package com.paravibe.flashlight;

import android.app.Activity;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.view.WindowManager;
import android.widget.ImageView;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
  private final int BATTERY_LOW_LEVEL = 10;
  private final int LED_ON_INTERVAL = 60 * 1000 * 15;
  private Camera camera;
  private Camera.Parameters params;
  private Intent batteryStatus;
  private Timer mTimer;
  private boolean isLedOn;
  private boolean hasLed;
  private static ImageView imageView;
  private static ImageView toggleButton;
  private static Drawable flashOnImage;
  private static Drawable flashOffImage;
  private int batLevel;

  private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context c, Intent i) {
      batLevel = i.getIntExtra("level", 0);
      boolean isCharging = isCharging();
      if (batLevel <= BATTERY_LOW_LEVEL && !isCharging) {
        // Turn off led, show alert message and close the application.
        setFlashStatus(false);
        new AlertDialog.Builder(MainActivity.this)
            .setMessage("Sorry, battery level is too low.")
            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                finish();
              }
            })
            .show();
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // First check if device is supporting flashlight or not.
    hasLed = getApplicationContext().getPackageManager()
        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

    if (!hasLed) {
      // Show alert message and close the application.
      new AlertDialog.Builder(MainActivity.this)
          .setTitle("Error")
          .setMessage("Sorry, your device doesn't support flash light!")
          .setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              finish();
            }
          })
          .show();

      return;
    }

    batteryStatus = registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    setContentView(R.layout.main);

    // Keep screen always on.
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    imageView = (ImageView) findViewById(R.id.background);
    toggleButton = (ImageView) findViewById(R.id.powerButton);
    flashOffImage = getResources().getDrawable(R.drawable.flashlight_off);
    flashOnImage = getResources().getDrawable(R.drawable.flashlight_on);

    isLedOn = true;
  }

  public boolean onKeyDown(int i, KeyEvent keyevent) {
    switch (i) {
      // key Vol+;
      case KeyEvent.KEYCODE_VOLUME_UP:
        setFlashStatus(!isLedOn);
        break;

      // key Vol-;
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        setFlashStatus(true);
        break;

      default:
        return super.onKeyDown(i, keyevent);
    }

    return true;
  }

  public boolean onKeyUp(int i, KeyEvent keyevent) {
    switch (i) {
      // key Vol+;
      case KeyEvent.KEYCODE_VOLUME_UP:
        break;

      // key Vol-;
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        setFlashStatus(false);
        break;

      default:
        return super.onKeyUp(i, keyevent);
    }

    return true;
  }

  private boolean isCharging() {
    int batStatus = batteryStatus.getIntExtra("status", 0);
    boolean isCharging = batStatus == 2 || batStatus == 5;

    return isCharging;
  }

  @Override
  protected void onStart() {
    super.onStart();
    batLevel = batteryStatus.getIntExtra("level", 0);

    // Check if device has led and if battery level is not very low.
    if (hasLed && (batLevel > BATTERY_LOW_LEVEL || isCharging())) {
      camera = null;
      getCamera();
      setFlashStatus(isLedOn);

      // Switch button click event to toggle flash on/off.
      toggleButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          setFlashStatus(!isLedOn);
        }
      });
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    unregisterReceiver(mBatInfoReceiver);
  }

  @Override
  protected void onResume() {
    super.onResume();
    batteryStatus = registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (camera != null) {
      camera.release();
      camera = null;
    }
  }

  // Get the camera
  private void getCamera() {
    if (camera == null) {
      try {
        camera = Camera.open();
        params = camera.getParameters();
      }
      catch (RuntimeException e) {
        Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
      }
    }
  }

  public void setFlashStatus(Boolean status) {
    String mode;

    if (camera == null || params == null) {
      return;
    }

    if (status) {
      mode = Camera.Parameters.FLASH_MODE_TORCH;
      isLedOn = true;

      // Set new timer.
      if (mTimer != null) {
        mTimer.cancel();
        mTimer = null;
      }
      mTimer = new Timer();

      // Turn off led if it uses long time.
      mTimer.schedule(new TimerTask() {
        public void run() {
          isLedOn = false;
          controlCamera(Camera.Parameters.FLASH_MODE_OFF);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              toggleButton(isLedOn);
            }
          });
        }
      }, LED_ON_INTERVAL);
    }
    else {
      mode = Camera.Parameters.FLASH_MODE_OFF;
      isLedOn = false;
      if (mTimer != null) {
        mTimer.cancel();
        mTimer = null;
      }
    }

    toggleButton(isLedOn);
    controlCamera(mode);
  }

  private void controlCamera(String mode) {
    params = camera.getParameters();
    params.setFlashMode(mode);
    camera.setParameters(params);
    camera.stopPreview();
  }

  private void toggleButton(boolean status) {
    if (status) {
      imageView.setImageDrawable(flashOnImage);
    }
    else {
      imageView.setImageDrawable(flashOffImage);
    }
  }
}
