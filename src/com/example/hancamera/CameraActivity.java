package com.example.hancamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {

	private Camera mCamera;
	private Preview mPreview;
	private MediaRecorder mMediaRecorder;
	private Button captureButton;
	private SurfaceView surfaceView;

	private final String TAG = "CameraActivity";
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	public static final int K_STATE_PREVIEW = 1;
	public static final int K_STATE_BUSY = 0;
	public static final int K_STATE_FROZEN = -1;

	private int mPreviewState = K_STATE_PREVIEW;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new Preview(this, surfaceView);

		((FrameLayout) findViewById(R.id.camera_preview)).addView(mPreview);

		while (!safeCameraOpen()) {

		}
		mPreview.setCamera(mCamera);

		captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.bringToFront();
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCamera.takePicture(null, null, mPicture);
			}
		});
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.e(TAG,
						"Error creating media file, check storage permissions: pictureFile== null");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				Log.e("onPictureTaken", "save success, path: " + pictureFile.getPath());
				mCamera.startPreview();
			} catch (FileNotFoundException e) {
				Log.e(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, "Error accessing file: " + e.getMessage());
			}
		}
	};

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.e("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.TAIWAN)
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaRecorder(); // if you are using MediaRecorder, release it first
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}


	private boolean safeCameraOpen() {
		boolean qOpened = false;

		try {
			releaseCameraAndPreview();
			mCamera = Camera.open();
			qOpened = (mCamera != null);
			Log.e("safeCameraOpen", "true");
		} catch (Exception e) {
			Log.e(getString(R.string.app_name), "failed to open Camera");
			e.printStackTrace();
		}

		return qOpened;
	}

	private void releaseCameraAndPreview() {
		mPreview.setCamera(null);
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}
}
