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
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
  private Camera camera;
  private boolean isFlashOn;
  private boolean hasFlash;
  private Handler mHandler = new Handler();
  private boolean mSwap = true;
  private int strobeIntensity = 10;
  ImageButton btnSwitch;
  SeekBar strobeIntensitySeek;
  Parameters params;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);
    strobeIntensitySeek = (SeekBar) findViewById(R.id.seekBar);

    // First check if device is supporting flashlight or not.
    hasFlash = getApplicationContext().getPackageManager()
        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

    if (!hasFlash) {
      // Device doesn't support flash.
      // Show alert message and close the application.
      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
      builder.setTitle("Error")
      .setMessage("Sorry, your device doesn't support flash light!")
      .setNegativeButton("Close", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          // Closing the application.
          finish();
        }
      });
      AlertDialog alert = builder.create();
      alert.show();
      return;
    }

    // Switch button click event to toggle flash on/off.
    btnSwitch.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        controlFlash(isFlashOn);
        startStrobe();
      }
    });

    strobeIntensitySeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      int progressValue = 0;

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progressValue = progress;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        strobeIntensity = (progressValue + 1) * 10;
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

  /**
   * Toggle switch button images.
   * Changing image states to on/off.
   */
  private void toggleButtonImage() {
    if (isFlashOn) {
      btnSwitch.setImageResource(R.drawable.button_on);
    }
    else {
      btnSwitch.setImageResource(R.drawable.button_off);
    }
  }

  private final Runnable mRunnable = new Runnable() {
    public void run() {
      if (isFlashOn) {
        if (mSwap) {
          params.setFlashMode(Parameters.FLASH_MODE_TORCH);
          camera.setParameters(params);
        }
        else {
          params.setFlashMode(Parameters.FLASH_MODE_OFF);
          camera.setParameters(params);
        }

        mSwap = !mSwap;
        mHandler.postDelayed(mRunnable, strobeIntensity);
      }
    }
  };

  /**
   * Start flashlight strobe.
   */
  private void startStrobe() {
    mHandler.post(mRunnable);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    super.onPause();
    controlFlash(true);
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

    // Get the camera params.
    getCamera();
    controlFlash(false);
  }

  @Override
  protected void onStop() {
    super.onStop();

    // Release the camera.
    if (camera != null) {
      camera.release();
      camera = null;
    }
  }
}
