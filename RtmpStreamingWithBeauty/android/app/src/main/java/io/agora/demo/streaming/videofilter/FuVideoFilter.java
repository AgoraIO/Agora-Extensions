package io.agora.demo.streaming.videofilter;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import com.faceunity.FURenderer;
import com.faceunity.fulivedemo.utils.CameraUtils;

import java.util.concurrent.Callable;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.rtcwithfu.view.FuBeautyContainer;

class FuVideoFilter extends VideoFilterWrapper
    implements SurfaceHolder.Callback, SensorEventListener {
    private static final String TAG = FuVideoFilter.class.getSimpleName();

    private SurfaceView mSurfaceView;
    private FuBeautyContainer mBeautyContainer;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private FURenderer mFURenderer;
    private final Object mRenderLock = new Object();
    private boolean mFUResourceCreated;
    private TextureBufferHelper mTextureBufferHelper;
    private int lastInputTextureId = 0;

    public void init(Context context, SurfaceView surfaceView) {
        mBeautyContainer = new FuBeautyContainer(context);
        mSurfaceView = surfaceView;
        mSurfaceView.getHolder().addCallback(this);

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        FURenderer.initFURenderer(context);
        mFURenderer = new FURenderer
            .Builder(context)
            .inputImageOrientation(CameraUtils.getFrontCameraOrientation())
            .setOnFUDebugListener(new FURenderer.OnFUDebugListener() {
                @Override
                public void onFpsChange(double fps, double renderTime) {
                    Log.d(TAG, "FURenderer.onFpsChange, fps: " + fps + ", renderTime: " + renderTime);
                }
            })
            .setOnTrackingStatusChangedListener(mBeautyContainer)
            .inputTextureType(FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE)
            .build();

        mBeautyContainer.bind(mFURenderer);
    }

    public void deinit() {
        mSurfaceView.getHolder().removeCallback(this);
        mSurfaceView = null;

        synchronized (mRenderLock) {
            if (mFUResourceCreated) {
                mFURenderer.onSurfaceDestroyed();
                mFUResourceCreated = false;
            }
            if (mTextureBufferHelper != null) {
                mTextureBufferHelper.dispose();
                mTextureBufferHelper = null;
            }
        }
    }

    // VideoFilter callback
    @Override
    public synchronized VideoFrame process(final VideoFrame videoFrame) {
        if (!(videoFrame.getBuffer() instanceof VideoFrame.TextureBuffer)) {
            Log.e(TAG, "Receives a non-texture buffer, which should not happen!");
            return null;
        }
        final VideoFrame.TextureBuffer texBuffer = (VideoFrame.TextureBuffer) videoFrame.getBuffer();

        synchronized (mRenderLock) {
            if (!mFUResourceCreated) {
                return null;
            }

            if (mTextureBufferHelper == null) {
                mTextureBufferHelper = TextureBufferHelper.create("FuRenderThread",
                    texBuffer.getEglBaseContext());
                if (mTextureBufferHelper == null) {
                    Log.e(TAG, "Failed to create texture buffer helper!");
                    return null;
                }
            }

            return mTextureBufferHelper.invoke(new Callable<VideoFrame>() {
                @Override
                public VideoFrame call() throws Exception {
                    // Drop incoming frame if output texture buffer is still in use.
                    if (mTextureBufferHelper.isTextureInUse()) {
                        return null;
                    }

                    // Process frame with FaceUnity SDK.
                    int fuTex = mFURenderer.onDrawFrame(texBuffer.getTextureId(),
                        texBuffer.getWidth(), texBuffer.getHeight());

                    // Drop the frame if the incoming texture id changes, which occurs for the
                    // first frame on start or after camera switching.
                    // This avoids rendering a black frame (the first output frame on start)
                    // or a staled frame (the first output frame after camera switching),
                    // since the FURender output delays by one frame.
                    if (lastInputTextureId != texBuffer.getTextureId()) {
                        lastInputTextureId = texBuffer.getTextureId();
                        Log.i(TAG, "Dropping frame since the source of input is changing");
                        return null;
                    }

                    // Return processed frame to Agora SDK.
                    VideoFrame.TextureBuffer processedBuffer = mTextureBufferHelper.wrapTextureBuffer(
                        texBuffer.getWidth(), texBuffer.getHeight(), VideoFrame.TextureBuffer.Type.RGB,
                        fuTex, texBuffer.getTransformMatrix());
                    return new VideoFrame(processedBuffer, videoFrame.getRotation(),
                        videoFrame.getTimestampNs());
                }
            });
        }
    }

    public void onCameraChange(int currentCameraType, int inputImageOrientation) {
        mFURenderer.onCameraChange(currentCameraType, inputImageOrientation);

    }

    public void onActivityResume() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onActivityPause() {
        mSensorManager.unregisterListener(this);
    }

    public RelativeLayout getBeautyContainer() {
        return mBeautyContainer;
    }

    // SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated: " + holder);
        synchronized (mRenderLock) {
            if (!mFUResourceCreated) {
                mFURenderer.onSurfaceCreated();
                mFUResourceCreated = true;
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged: " + holder + " format: " + format + " width: " + width +
            " height:" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed: " + holder);
        synchronized (mRenderLock) {
            if (mFUResourceCreated) {
                mFURenderer.onSurfaceDestroyed();
                mFUResourceCreated = false;
            }
        }
    }

    // SensorEventListener
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if (Math.abs(x) > 3 || Math.abs(y) > 3) {
                if (Math.abs(x) > Math.abs(y)) {
                    mFURenderer.setTrackOrientation(x > 0 ? 0 : 180);
                } else {
                    mFURenderer.setTrackOrientation(y > 0 ? 90 : 270);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
}
