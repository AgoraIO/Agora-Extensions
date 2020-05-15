package io.agora.capture.framework.modules.consumers;

import android.opengl.GLES20;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import io.agora.capture.framework.modules.channels.VideoChannel;
import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.capture.video.camera.VideoModule;

public class SurfaceViewConsumer extends BaseWindowConsumer implements SurfaceHolder.Callback {
    private static final String TAG = SurfaceViewConsumer.class.getSimpleName();

    private SurfaceView mSurfaceView;

    public SurfaceViewConsumer(SurfaceView surfaceView) {
        super(VideoModule.instance());
        mSurfaceView = surfaceView;
    }

    @Override
    public void onConsumeFrame(VideoCaptureFrame frame, VideoChannel.ChannelContext context) {
        if (mSurfaceView == null) {
            return;
        }

        super.onConsumeFrame(frame, context);
    }

    @Override
    public Object onGetDrawingTarget() {
        return mSurfaceView != null ? mSurfaceView.getHolder().getSurface() : null;
    }

    @Override
    public int onMeasuredWidth() {
        return mSurfaceView.getMeasuredWidth();
    }

    @Override
    public int onMeasuredHeight() {
        return mSurfaceView.getMeasuredHeight();
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        surfaceDestroyed = false;
        needResetSurface = true;
        connectChannel(CHANNEL_ID);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged:" + width + "x" + height);
        GLES20.glViewport(0, 0, width, height);
        needResetSurface = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        disconnectChannel(CHANNEL_ID);
        surfaceDestroyed = true;
    }
}
