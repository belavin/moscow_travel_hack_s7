package com.pin.video.recorder.state;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.pin.video.recorder.CameraInterface;
import com.pin.video.recorder.CameraView;

class PreviewState implements State {

    private static final String TAG = PreviewState.class.getSimpleName();

    private CameraMachine machine;

    PreviewState(CameraMachine machine) {
        this.machine = machine;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().doStartPreview(holder, screenProp);
    }

    @Override
    public void stop() {
        CameraInterface.getInstance().doStopPreview();
    }


    @Override
    public void focus(float x, float y, CameraInterface.FocusCallback callback) {
        Log.d(TAG, "preview state focus");
        if (machine.getView().handlerFocus(x, y)) {
            CameraInterface.getInstance().handleFocus(machine.getContext(), x, y, callback);
        }
    }

    @Override
    public void switchz(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().switchCamera(holder, screenProp);
    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {
        CameraInterface.getInstance().takePicture(new CameraInterface.TakePictureCallback() {
            @Override
            public void captureResult(Bitmap bitmap, boolean isVertical) {
                machine.getView().showPicture(bitmap, isVertical);
                machine.setState(machine.getBorrowPictureState());
                Log.d(TAG, "capture");
            }
        });
    }

    @Override
    public void record(Surface surface, float screenProp) {
        CameraInterface.getInstance().startRecord(surface, screenProp, null);
    }

    @Override
    public void stopRecord(final boolean isShort, long time) {
        CameraInterface.getInstance().stopRecord(isShort, new CameraInterface.StopRecordCallback() {
            @Override
            public void recordResult(String url, Bitmap firstFrame) {
                if (isShort) {
                    machine.getView().resetState(CameraView.TYPE_SHORT);
                } else {
                    machine.getView().playVideo(firstFrame, url);
                    machine.setState(machine.getBorrowVideoState());
                }
            }
        });
    }

    @Override
    public void cancel(SurfaceHolder holder, float screenProp) {
        Log.d(TAG, "???????????????,?????? cancel ??????");
    }

    @Override
    public void confirm() {
        Log.d(TAG, "???????????????,?????? confirm ??????");
    }

    @Override
    public void zoom(float zoom, int type) {
        Log.d(TAG, "zoom");
        CameraInterface.getInstance().setZoom(zoom, type);
    }

    @Override
    public void flash(String mode) {
        CameraInterface.getInstance().setFlashMode(mode);
    }
}
