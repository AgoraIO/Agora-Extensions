package io.agora.demo.streaming.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.faceunity.FURenderer;
import com.faceunity.fulivedemo.ui.adapter.EffectRecyclerAdapter;
import com.faceunity.fulivedemo.utils.CameraUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.demo.streaming.LiveStreamingPresenter;
import io.agora.demo.streaming.R;
import io.agora.demo.streaming.ui.VideoGridContainer;
import io.agora.demo.streaming.utils.PrefManager;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtcwithfu.view.EffectPanel;
import io.agora.streaming.StreamingEventHandler;
import io.agora.streaming.VideoFilter;

public class LiveActivity extends BaseActivity {
    private static final String TAG = LiveActivity.class.getSimpleName();
    public static final String KEY_ROOM_NAME = "key_room_name";

    @interface STREAMING_MODE {
        int NONE = 0;
        int CLIENT_STREAMING = 1;
        int SERVER_STREAMING = 2;
        int SIMUL_STREAMING = 3; // test only
    }
    @STREAMING_MODE
    private int mCurrentStreamingMode = STREAMING_MODE.NONE;

    private LiveStreamingPresenter mPresenter;
    private IRtcEngineEventHandler mRtcEngineEventHandler = new MyRtcEngineEventHandler();
    private StreamingEventHandler mStreamingEventHandler = new MyStreamingEventHandler();
    private MyFuVideoFilter mFUVideoFilter;

    private boolean mLocalAudioMuted = false;
    private boolean mLocalVideoMuted = false;

    private String mRoomName;
    private VideoGridContainer mVideoGridContainer;
    private Button mStartStreamingBtn;
    private SurfaceView mLocalView;
    private Toast mToast;

    private Set<Integer> mRemoteUidSet = new HashSet<>();

    private ViewGroup mBeautyLayout;

    // for simultaneous streaming test
    private ImageView mSimulStreamingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 0.7f;
        getWindow().setAttributes(params);

        mRoomName = getIntent().getStringExtra(KEY_ROOM_NAME);

        initUIAndData();
        initLiveStreaming();
    }

    private void initUIAndData() {
        // room name
        TextView roomName = findViewById(R.id.live_room_name);
        roomName.setText(mRoomName);
        roomName.setSelected(true);

        // user icon
        Bitmap origin = BitmapFactory.decodeResource(getResources(), R.drawable.fake_user_icon);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), origin);
        drawable.setCircular(true);
        ImageView iconView = findViewById(R.id.live_name_board_icon);
        iconView.setImageDrawable(drawable);

        // buttons
        mStartStreamingBtn = findViewById(R.id.btn_rtmp_streaming);
        ImageView muteVideoBtn = findViewById(R.id.live_btn_mute_video);
        muteVideoBtn.setActivated(true);
        ImageView muteAudioBtn = findViewById(R.id.live_btn_mute_audio);
        muteAudioBtn.setActivated(true);
        ImageView beautyBtn = findViewById(R.id.live_btn_beautification);
        beautyBtn.setActivated(false);
        ImageView serverStreamingBtn = findViewById(R.id.live_btn_server_streaming);
        serverStreamingBtn.setActivated(false);
        mSimulStreamingBtn = findViewById(R.id.live_btn_server_streaming);
        if (PrefManager.IS_SIMUL_TEST) {
            mStartStreamingBtn.setVisibility(View.GONE);
            mSimulStreamingBtn.setVisibility(View.VISIBLE);
        }

        // views
        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mLocalView = new SurfaceView(this);
        mBeautyLayout = findViewById(R.id.beauty_overlay);
        mBeautyLayout.setVisibility(beautyBtn.isActivated() ? View.VISIBLE : View.GONE);
    }

    private void initLiveStreaming() {
        Log.i(TAG, "initLiveStreaming");

        mPresenter = LiveStreamingPresenter.getInstance();
        mPresenter.setRtcEngineEventHandler(mRtcEngineEventHandler);
        mPresenter.setStreamingEventHandler(mStreamingEventHandler);

        // video filter
        mFUVideoFilter = new MyFuVideoFilter();
        mFUVideoFilter.init(this);

        // set local video view
        mPresenter.setPreview(mLocalView);
        mVideoGridContainer.addUserVideo(0, mLocalView, true);

        mPresenter.enableAudioRecordingAndVideoCapturing(true);
    }

    private void deinitLiveStreaming() {
        Log.i(TAG, "deinitLiveStreaming");
        mPresenter.enableAudioRecordingAndVideoCapturing(false);

        // unset local video view
        mVideoGridContainer.removeUserVideo(0, true);
        mPresenter.setPreview(null);

        mPresenter.setRtcEngineEventHandler(null);
        mPresenter.setStreamingEventHandler(null);
        mFUVideoFilter.deinit();
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout topLayout = findViewById(R.id.live_room_top_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.height = mStatusBarHeight + topLayout.getMeasuredHeight();
        topLayout.setLayoutParams(params);
        topLayout.setPadding(0, mStatusBarHeight, 0, 0);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (mCurrentStreamingMode == STREAMING_MODE.CLIENT_STREAMING) {
            mPresenter.stopClientStreaming();
        } else if (mCurrentStreamingMode == STREAMING_MODE.SERVER_STREAMING) {
            mPresenter.stopServerStreamingAndLeaveAgoraChannel();
        } else if (mCurrentStreamingMode == STREAMING_MODE.SIMUL_STREAMING) {
            mPresenter.stopClientStreaming();
            mPresenter.stopServerStreamingAndLeaveAgoraChannel();
        }
        mCurrentStreamingMode = STREAMING_MODE.NONE;

        // reset to initial state
        if (mLocalVideoMuted) {
            mPresenter.muteLocalVideoStream(false);
            mLocalVideoMuted = false;
        }
        if (mLocalAudioMuted) {
            mPresenter.muteLocalAudioStream(false);
            mLocalAudioMuted = false;
        }

        deinitLiveStreaming();
        super.onDestroy();
    }

    private SurfaceView prepareRemoteVideo(int uid) {
        SurfaceView surface = RtcEngine.CreateRendererView(getApplicationContext());
        mPresenter.setupRemoteVideo(new VideoCanvas(surface, VideoCanvas.RENDER_MODE_HIDDEN, uid,
            PrefManager.getMirrorRemoteMode(this)));
        return surface;
    }

    private void removeRemoteVideo(int uid) {
        mPresenter.removeRemoteVideo(uid);
    }

    private void renderRemoteUser(int uid) {
        SurfaceView surface = prepareRemoteVideo(uid);
        mVideoGridContainer.addUserVideo(uid, surface, false);
    }

    private void removeRemoteUser(int uid) {
        mVideoGridContainer.removeUserVideo(uid, false);
        removeRemoteVideo(uid);
    }

    public void onStreamingBtnClicked(View view) {
        Log.i(TAG, "onStreamingBtnClicked");

        if (mCurrentStreamingMode == STREAMING_MODE.NONE) {
            Log.i(TAG, "start client streaming directly");
            mPresenter.startClientStreaming();
            mCurrentStreamingMode = STREAMING_MODE.CLIENT_STREAMING;

        } else if (mCurrentStreamingMode == STREAMING_MODE.CLIENT_STREAMING) {
            Log.i(TAG, "join agora channel and start server streaming");
            mPresenter.stopClientStreaming();
            mPresenter.joinAgoraChannelAndStartServerStreaming(mRoomName);
            mCurrentStreamingMode = STREAMING_MODE.SERVER_STREAMING;

        } else if (mCurrentStreamingMode == STREAMING_MODE.SERVER_STREAMING) {
            Log.i(TAG, "leave agora channel and start client streaming");
            mPresenter.stopServerStreamingAndLeaveAgoraChannel();
            mPresenter.startClientStreaming();
            for(int uid : mRemoteUidSet) {
                removeRemoteUser(uid);
            }
            mCurrentStreamingMode = STREAMING_MODE.CLIENT_STREAMING;
        }
        mStartStreamingBtn.setText(getString(mCurrentStreamingMode == STREAMING_MODE.CLIENT_STREAMING ?
            R.string.switch_to_agora_channel : R.string.switch_to_rtmp_streaming));
    }

    public void onLeaveClicked(View view) {
        Log.i(TAG, "onLeaveClicked");
        finish();
    }

    public void onSwitchCameraClicked(View view) {
        // Disable renderer to avoid unexpected render effect while switching camera
        mPresenter.setPreview(null);
        int ret = mPresenter.switchCamera();

        // Notify FaceUnity SDK of camera change
        if (ret == 0) {
            boolean isCameraFacingFront = mPresenter.isCameraFacingFront();
            int currentCameraType = isCameraFacingFront ? Camera.CameraInfo.CAMERA_FACING_FRONT
                : Camera.CameraInfo.CAMERA_FACING_BACK;
            int inputImageOrientation = isCameraFacingFront ? 270 : 90;
            mFUVideoFilter.onCameraChange(currentCameraType, inputImageOrientation);
        }

        // Re-enable renderer
        mPresenter.setPreview(mLocalView);
    }

    public void onBeautyClicked(View view) {
        view.setActivated(!view.isActivated());
        if (view.isActivated()) {
            mBeautyLayout.setVisibility(View.VISIBLE);
            mPresenter.addVideoFilter(mFUVideoFilter);
            showToast(getString(R.string.fu_face_tracking_time_tips));
        } else {
            mBeautyLayout.setVisibility(View.GONE);
            mPresenter.removeVideoFilter(mFUVideoFilter);
        }
    }

    public void onMuteAudioClicked(View view) {
        view.setActivated(!view.isActivated());
        mLocalAudioMuted = !view.isActivated();
        mPresenter.muteLocalAudioStream(mLocalAudioMuted);
        showToast((mLocalAudioMuted ? "mute" : "un-mute") + " audio");
    }

    public void onMuteVideoClicked(View view) {
        view.setActivated(!view.isActivated());
        mLocalVideoMuted = !view.isActivated();
        mPresenter.muteLocalVideoStream(mLocalVideoMuted);
        showToast((mLocalVideoMuted ? "mute" : "un-mute") + " video");
    }

    public void onServerStreamingClicked(View view) {
        view.setActivated(!view.isActivated());
        boolean publish = view.isActivated();
        if (publish) {
            mPresenter.startClientStreaming();
            mPresenter.joinAgoraChannelAndStartServerStreaming(mRoomName);
            mCurrentStreamingMode = STREAMING_MODE.SIMUL_STREAMING;
        } else {
            mPresenter.stopServerStreamingAndLeaveAgoraChannel();
            mPresenter.stopClientStreaming();
            mCurrentStreamingMode = STREAMING_MODE.NONE;
        }
        showToast((publish ? "start" : "stop") + " simultaneous CDN streaming");
    }

    private void showToast(final String msg) {
        Log.i(TAG, msg);
        runOnUiThread(new Runnable() {
            @SuppressLint("ShowToast")
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(LiveActivity.this, msg, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFUVideoFilter.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFUVideoFilter.onActivityPause();
    }

    private class MyStreamingEventHandler extends StreamingEventHandler {

        @Override
        public void onStartStreamingSuccess() {
            showToast("start streaming success");
        }

        @Override
        public void onStartStreamingFailure(int err, String msg) {
            showToast("start streaming failed: " + err + ", " + msg);
        }

        @Override
        public void onMediaStreamingError(int err, String msg) {
            showToast("media streaming error: " + err + ", " + msg);
        }

        @Override
        public void onStreamingConnectionStateChanged(int state) {
            // showToast("streaming connection state changes to: " + state);
        }
    }

    private class MyRtcEngineEventHandler extends IRtcEngineEventHandler {

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            showToast("join channel success");
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRemoteUidSet.add(uid);
                    renderRemoteUser(uid);
                }
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRemoteUidSet.remove(uid);
                    removeRemoteUser(uid);
                }
            });
        }

        @Override
        public void onRtmpStreamingStateChanged(String url, int state, int errCode) {
            showToast("server streaming state: " + state + " err: " + errCode);
        }
    }

    private class MyFuVideoFilter extends VideoFilter implements SurfaceHolder.Callback, SensorEventListener {

        private EffectPanel mEffectPanel;
        private RecyclerView mTypeListView;
        private LinearLayout mEffectLayout;
        private TextView mDescriptionText;
        private TextView mTrackingText;
        private Runnable mEffectDescriptionHide;

        private SensorManager mSensorManager;
        private Sensor mSensor;

        private FURenderer mFURenderer;
        private final Object mRenderLock = new Object();
        private boolean mFUResourceCreated;
        private TextureBufferHelper mTextureBufferHelper;
        private int lastInputTextureId = 0;

        public void init(Context context) {
            mDescriptionText = findViewById(R.id.effect_desc_text);
            mTrackingText = findViewById(R.id.iv_face_detect);
            mTypeListView = findViewById(R.id.effect_type_list);
            mEffectLayout = findViewById(R.id.effect_panel_container);

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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
                .setOnTrackingStatusChangedListener(new FURenderer.OnTrackingStatusChangedListener() {
                    @Override
                    public void onTrackingStatusChanged(final int status) {
                        runOnUiThread(new Runnable() {
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
            mLocalView.getHolder().addCallback(this);

            mEffectDescriptionHide = new Runnable() {
                @Override
                public void run() {
                    mDescriptionText.setVisibility(View.INVISIBLE);
                    mDescriptionText.setText("");
                }
            };

            mEffectPanel = new EffectPanel(mTypeListView, mEffectLayout, mFURenderer,
                new EffectRecyclerAdapter.OnDescriptionChangeListener() {

                    @Override
                    public void onDescriptionChangeListener(int description) {
                        if (description == 0) return;
                        mDescriptionText.removeCallbacks(mEffectDescriptionHide);
                        mDescriptionText.setText(description);
                        mDescriptionText.setVisibility(View.VISIBLE);
                        mDescriptionText.postDelayed(mEffectDescriptionHide, 1500);
                    }
                });
        }

        public void deinit() {
            mLocalView.getHolder().removeCallback(this);
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
            mFURenderer.onCameraChange(currentCameraType, inputImageOrientation);

        }

        public void onActivityResume() {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        public void onActivityPause() {
            mSensorManager.unregisterListener(this);
        }
    }
}
