package io.agora.demo.streaming.videofilter;

import android.content.Context;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import io.agora.base.VideoFrame;
import io.agora.streaming.VideoFilter;

public abstract class VideoFilterWrapper extends VideoFilter {

    public static VideoFilterWrapper createFuVideoFilter(Context context) {
        return new FuVideoFilter(context);
    }

    public void init(Context context, SurfaceView surfaceView) {

    }

    public void deinit() {

    }

    @Override
    public VideoFrame process(VideoFrame videoFrame) {
        return null;
    }

    /**
     * @param currentCameraType
     *      android.hardware.Camera.CameraInfo#CAMERA_FACING_BACK or
     *      android.hardware.Camera.CameraInfo#CAMERA_FACING_FRONT
     * @param inputImageOrientation
     */
    public void onCameraChange(int currentCameraType, int inputImageOrientation) {

    }

    public void onActivityResume() {

    }

    public void onActivityPause() {

    }

    public abstract RelativeLayout getBeautyContainer();
}
