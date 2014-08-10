package com.paravibe.flashlight;

import android.app.Activity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends Activity {

  ImageButton btnSwitch;

  private Camera camera;
  private boolean isFlashOn;
  private boolean hasFlash;
  Parameters params;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Flash switch button
    btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);

    // First check if device is supporting flashlight or not
    hasFlash = getApplicationContext().getPackageManager()
        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

    if (!hasFlash) {
      // Device doesn't support flash.
      // Show alert message and close the application.
      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
      builder.setTitle("Error")
      .setMessage("Sorry, your device doesn't support flash light!")
      .setNegativeButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          // Closing the application.
          finish();
        }
      });
      AlertDialog alert = builder.create();
      alert.show();
      return;
    }

    // Get the camera.
    getCamera();

    // Displaying button image.
    toggleButtonImage();

    // Switch button click event to toggle flash on/off.
    btnSwitch.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        controlFlash(isFlashOn);
      }
    });
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

  // Turning on/off flash.
  private void controlFlash(boolean flashOff) {
    String mode = Parameters.FLASH_MODE_TORCH;
    if (flashOff) {
      mode = Parameters.FLASH_MODE_OFF;
    }

    if (camera == null || params == null) {
      return;
    }
    params = camera.getParameters();
    params.setFlashMode(mode);
    camera.setParameters(params);
    camera.stopPreview();

    isFlashOn = !flashOff;

    // Changing button/switch image.
    toggleButtonImage();
  }

  /*
   * Toggle switch button images
   * changing image states to on / off
   * */
  private void toggleButtonImage() {
    if (isFlashOn) {
      btnSwitch.setImageResource(R.drawable.button_off);
    }
    else {
      btnSwitch.setImageResource(R.drawable.button_on);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onRestart() {
    super.onRestart();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();

    // On starting the app get the camera params.
    getCamera();
  }

  @Override
  protected void onStop() {
    super.onStop();

    // on stop release the camera
    if (camera != null) {
      camera.release();
      camera = null;
    }
  }
}
