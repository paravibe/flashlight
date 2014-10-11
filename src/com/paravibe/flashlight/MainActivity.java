package com.paravibe.flashlight;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.ImageView;

public class MainActivity extends Activity {
  private Camera camera;
  private Camera.Parameters params;
  private boolean isLedOn;
  private boolean hasLed;
  private static ImageView imageView;
  private static ImageView toggleButton;
  private static Drawable flashOnImage;
  private static Drawable flashOffImage;

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

    setContentView(R.layout.main);

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

  @Override
  protected void onStart() {
    super.onStart();
    if (hasLed) {
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
    String mode = Camera.Parameters.FLASH_MODE_TORCH;
    isLedOn = true;

    if (!status) {
      mode = Camera.Parameters.FLASH_MODE_OFF;
      isLedOn = false;
    }
    toggleButton(isLedOn);

    if (camera == null || params == null) {
      return;
    }
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
