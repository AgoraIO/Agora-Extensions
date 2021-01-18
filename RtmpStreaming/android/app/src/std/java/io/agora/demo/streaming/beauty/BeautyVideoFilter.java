package io.agora.demo.streaming.beauty;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.SurfaceHolder;
import android.view.View;

import io.agora.base.VideoFrame;

public class BeautyVideoFilter extends BaseBeautyVideoFilter {
    public static final boolean enableBeauty = false;
    public void init(Activity activity) {

    }

    public void deinit() {

    }

    public View getActionView(){
        return null;
    }

    // SensorEventListener start
    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    // SensorEventListener  end

    // SurfaceHolder.Callback start
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    // SurfaceHolder.Callback end

    // VideoFilter start
    @Override
    public VideoFrame process(VideoFrame inputFrame) {
        return inputFrame;
    }
    // VideoFilter end

    public void onCameraChange(int currentCameraType, int inputImageOrientation) {

    }

    public void onActivityResume() {
    }

    public void onActivityPause() {

    }
}
