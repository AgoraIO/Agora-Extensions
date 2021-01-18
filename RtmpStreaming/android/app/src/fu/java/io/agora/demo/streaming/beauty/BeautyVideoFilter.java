package io.agora.demo.streaming.beauty;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.faceunity.FURenderer;
import com.faceunity.fulivedemo.ui.adapter.EffectRecyclerAdapter;
import com.faceunity.fulivedemo.utils.CameraUtils;

import java.util.concurrent.Callable;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.demo.streaming.R;

public class BeautyVideoFilter extends BaseBeautyVideoFilter {
    private static final String TAG = BeautyVideoFilter.class.getSimpleName() + "_fu";

    public static final boolean enableBeauty = true;

    private View mActionView;
    private EffectPanel mEffectPanel;
    private RecyclerView mTypeListView;
    private LinearLayout mEffectLayout;
    private TextView mDescriptionText;
    private TextView mTrackingText;
    private Runnable mEffectDescriptionHideRunnable;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private FURenderer mFURenderer;
    private final Object mRenderLock = new Object();
    private boolean mFUResourceCreated;
    private TextureBufferHelper mTextureBufferHelper;
    private int lastInputTextureId = 0;
    private int skipCount = 0;  // skip frame count

    public View getActionView() {
        return mActionView;
    }

    public void init(Activity activity) {
        if (mActionView == null) {
            mActionView = View.inflate(activity, R.layout.fu_action_view, null);
        }
        mDescriptionText = mActionView.findViewById(R.id.effect_desc_text);
        mTrackingText = mActionView.findViewById(R.id.iv_face_detect);
        mTypeListView = mActionView.findViewById(R.id.effect_type_list);
        mEffectLayout = mActionView.findViewById(R.id.effect_panel_container);

        mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        FURenderer.initFURenderer(activity.getBaseContext());
        mFURenderer = new FURenderer
                .Builder(activity.getBaseContext())
                .inputImageOrientation(CameraUtils.getFrontCameraOrientation())
                .setOnFUDebugListener(new FURenderer.OnFUDebugListener() {
                    @Override
                    public void onFpsChange(double fps, double renderTime) {
                        Log.d(TAG, "FURenderer.onFpsChange, fps: " + fps + ", renderTime: " + renderTime);
                    }
                })
                .setOnTrackingStatusChangedListener(new FURenderer.OnTrackingStatusChangedListener() {
                    @Override
                    public void onTrackingStatusChanged(final int status) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "FURenderer.onTrackingStatusChanged, status: " + status);
                                mTrackingText.setVisibility(status > 0 ? View.GONE : View.VISIBLE);
                            }
                        });
                    }
                })
                .inputTextureType(FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE)
                .build();

        mEffectDescriptionHideRunnable = () -> {
            mDescriptionText.setVisibility(View.INVISIBLE);
            mDescriptionText.setText("");
        };

        mEffectPanel = new EffectPanel(mTypeListView, mEffectLayout, mFURenderer,
                new EffectRecyclerAdapter.OnDescriptionChangeListener() {

                    @Override
                    public void onDescriptionChangeListener(int description) {
                        if (description == 0) {
                            return;
                        }
                        mDescriptionText.removeCallbacks(mEffectDescriptionHideRunnable);
                        mDescriptionText.setText(description);
                        mDescriptionText.setVisibility(View.VISIBLE);
                        mDescriptionText.postDelayed(mEffectDescriptionHideRunnable, 1500);
                    }
                });
    }

    public void deinit() {
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
                    // skip some frames on switch camera to avoid image stand upside down
                    synchronized (this) {
                        int _skip = skipCount;
                        skipCount--;
                        if (_skip > 0) {
                            return videoFrame;
                        }
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

    public void onCameraChange(int currentCameraType, int inputImageOrientation) {
        synchronized (this) {
            // skip some frames on switch camera to avoid image stand upside down
            skipCount = 2;
        }
        mFURenderer.onCameraChange(currentCameraType, inputImageOrientation);

    }

    public void onActivityResume() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onActivityPause() {
        mSensorManager.unregisterListener(this);
    }
}
