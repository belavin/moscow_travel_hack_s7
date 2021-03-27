
package com.example.android.mediarecorder;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.common.media.CameraHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int MEDIA_RECORDER_REQUEST = 0;

    private Camera mCamera;
    private TextureView mPreview;
    private MediaRecorder mMediaRecorder;
    private File mOutputFile;

    private boolean isRecording = false;
    private static final String TAG = "Recorder";
    private Button captureButton;

    private final String[] requiredPermissions = {
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);

        mPreview = (TextureView) findViewById(R.id.surface_view);
        captureButton = (Button) findViewById(R.id.button_capture);
    }


    public void onCaptureClick(View view) {

        if (areCameraPermissionGranted()){
            startCapture();
        } else {
            requestCameraPermissions();
        }
    }

    private void startCapture(){

        if (isRecording) {
            try {
                mMediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                Log.d(TAG, "после старта");
                mOutputFile.delete();
            }
            releaseMediaRecorder();
            mCamera.lock();

            setCaptureButtonText("Capture");
            isRecording = false;
            releaseCamera();


        } else {

            // BEGIN_INCLUDE(prepare_start_media_recorder)

            new MediaPrepareTask().execute(null, null, null);

            // END_INCLUDE(prepare_start_media_recorder)

        }
    }

    private void setCaptureButtonText(String title) {
        captureButton.setText(title);
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseMediaRecorder();

        releaseCamera();
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {

            mMediaRecorder.reset();

            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean prepareVideoRecorder(){

        mCamera = CameraHelper.getDefaultCameraInstance();

        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mPreview.getWidth(), mPreview.getHeight());

        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);
        try {
                mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
        } catch (IOException e) {
            Log.e(TAG, "Текстура поверхности недоступна или неподходящая" + e.getMessage());
            return false;
        }

        mMediaRecorder = new MediaRecorder();


        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);


        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT );
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


        mMediaRecorder.setProfile(profile);

        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
        }
        mMediaRecorder.setOutputFile(mOutputFile.getPath());

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "Подготовливаю медиарекордер " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "ошибка подготовки медирекордера " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private boolean areCameraPermissionGranted() {

        for (String permission : requiredPermissions){
            if (!(ActivityCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }

    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(
                this,
                requiredPermissions,
                MEDIA_RECORDER_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (MEDIA_RECORDER_REQUEST != requestCode) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        boolean areAllPermissionsGranted = true;
        for (int result : grantResults){
            if (result != PackageManager.PERMISSION_GRANTED){
                areAllPermissionsGranted = false;
                break;
            }
        }

        if (areAllPermissionsGranted){
            startCapture();
        } else {

            Toast.makeText(getApplicationContext(),
                    getString(R.string.need_camera_permissions),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            if (prepareVideoRecorder()) {

                mMediaRecorder.start();

                isRecording = true;
            } else {

                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                MainActivity.this.finish();
            }

            setCaptureButtonText("Стоп");

        }
    }

}
