package com.example;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pin.video.compression.SiliCompressor;
import com.pin.video.recorder.CameraView;
import com.pin.video.recorder.listener.CameraListener;
import com.pin.video.recorder.listener.ErrorListener;
import com.pin.video.recorder.listener.RecordStateListener;
import com.pin.video.recorder.listener.SimpleClickListener;
import com.pin.video.recorder.util.ExtractVideoInfoUtil;
import com.pin.video.recorder.util.FileUtil;

import java.io.File;

public class VideoCameraActivity extends AppCompatActivity {

    private static final String TAG = VideoCameraActivity.class.getSimpleName();

    private CameraView mCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_camera);

        initView();
    }

    private void initView() {
        mCameraView = findViewById(R.id.jcameraview);

        mCameraView.setSaveVideoPath(getExternalFilesDir("record-video").getAbsolutePath());
        mCameraView.setMinDuration(3000);
        mCameraView.setDuration(10000);
        mCameraView.setFeatures(CameraView.BUTTON_STATE_ONLY_RECORDER);
        mCameraView.setTip("");
        mCameraView.setRecordShortTip("");
        mCameraView.setMediaQuality(CameraView.MEDIA_QUALITY_MIDDLE);
        mCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
                Log.e(TAG, "camera error");
                Intent intent = new Intent();
                setResult(103, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(VideoCameraActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        });
        //
        mCameraView.setCameraLisenter(new CameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                Log.d(TAG, "bitmap = " + bitmap.getWidth());
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                Log.d(TAG, "url:" + url + ", firstFrame.width: " + firstFrame.getWidth() + ", firstFrame.height: " + firstFrame.getHeight());
                compressVideo(url);
            }
        });
        mCameraView.setLeftClickListener(new SimpleClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
        mCameraView.setRightClickListener(new SimpleClickListener() {
            @Override
            public void onClick() {
                Toast.makeText(VideoCameraActivity.this, "Right", Toast.LENGTH_SHORT).show();
            }
        });
        mCameraView.setRecordStateListener(new RecordStateListener() {
            @Override
            public void recordStart() {

            }

            @Override
            public void recordEnd(long time) {
                Log.d(TAG, "" + time);
            }

            @Override
            public void recordCancel() {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraView.onPause();
    }


    private void compressVideo(final String srcPath) {
        showCompressLoading();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    File destDir = new File(getExternalFilesDir("record-video") + File.separator + "compressed-video");
                    if (!destDir.exists() || !destDir.isDirectory()) {
                        destDir.mkdirs();
                    }
                    String destDirPath = destDir.getAbsolutePath();
                    String compressedFilePath = SiliCompressor.with(VideoCameraActivity.this).compressVideo(srcPath, destDirPath, 720, 480, 900000);
                    Log.d(TAG, " " + compressedFilePath);


                    ExtractVideoInfoUtil extractVideoInfoUtil = new ExtractVideoInfoUtil(compressedFilePath);
                    Bitmap bitmap = extractVideoInfoUtil.extractFrame();
                    String firstFrameFilePath = FileUtil.saveBitmap(destDirPath, bitmap);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                    Log.d(TAG, "" + firstFrameFilePath);

                    dismissCompressLoading();

                    VideoPreviewActivity.startActivity(VideoCameraActivity.this, compressedFilePath, firstFrameFilePath);
                    finish();
                } catch (Exception e) {
                    dismissCompressLoading();
                    Log.e(TAG, " " + e.getMessage());
                }
            }
        });
    }

    private void showCompressLoading() {
        NormalProgressDialog.showLoading(this, ".", false);
    }

    private void dismissCompressLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NormalProgressDialog.stopLoading();
            }
        });
    }
}
