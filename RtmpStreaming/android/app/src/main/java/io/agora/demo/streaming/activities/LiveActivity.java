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
import android.os.Handler;
import android.os.HandlerThread;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.demo.streaming.R;
import io.agora.demo.streaming.RtcEngineWrapper;
import io.agora.demo.streaming.StreamingKitWrapper;
import io.agora.demo.streaming.stats.LocalStatsData;
import io.agora.demo.streaming.stats.RemoteStatsData;
import io.agora.demo.streaming.stats.StatsData;
import io.agora.demo.streaming.stats.StatsManager;
import io.agora.demo.streaming.ui.VideoGridContainer;
import io.agora.demo.streaming.utils.PrefManager;
import io.agora.rtcwithfu.view.EffectPanel;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
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

    private String mRoomName;
    private VideoGridContainer mVideoGridContainer;
    private Button mStartStreamingBtn;
    private SurfaceView mLocalView;
    private Toast mToast;

    private HandlerThread mWorkerThread;
    private Handler mWorkHandler;

    // engine/kit and event handler
    private RtcEngineWrapper mRtcEngineWrapper;
    private IRtcEngineEventHandler mRtcEventHandler = new MyRtcEventHandler();
    private StreamingKitWrapper mStreamingKitWrapper;
    private StreamingEventHandler mStreamingEventHandler = new MyStreamingEventHandler();

    private StatsManager mStatsManager;
    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    private Set<Integer> mRemoteUidSet = new HashSet<>();

    private ViewGroup mBeautyLayout;
    private MyFuVideoFilter mFUVideoFilter;

    // for simultaneous streaming test
    private ImageView mSimulStreamingBtn;
    private List<Integer> mUidList = new ArrayList<>();
    private boolean mPendingServerStreaming = false;
    private boolean mIsServerStreaming = false;

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

        // data
        mVideoDimension = PrefManager.VIDEO_DIMENSIONS[PrefManager.getVideoDimensionsIndex(this)];
        mStatsManager = new StatsManager();
        mStatsManager.enableStats(PrefManager.isStatsEnabled(this));
        mVideoGridContainer.setStatsManager(mStatsManager);
    }

    private void initLiveStreaming() {
        Log.i(TAG, "initLiveStreaming");
        mWorkerThread = new HandlerThread("worker");
        mWorkerThread.start();
        mWorkHandler = new Handler(mWorkerThread.getLooper());

        mFUVideoFilter = new MyFuVideoFilter();
        mFUVideoFilter.init(this);
        mStreamingKitWrapper = new StreamingKitWrapper(getApplicationContext(), mWorkHandler);
        mStreamingKitWrapper.init(mStreamingEventHandler);
        mRtcEngineWrapper = new RtcEngineWrapper(getApplicationContext(), mWorkHandler);
        mRtcEngineWrapper.init(mRtcEventHandler);

        // set local video view
        mStreamingKitWrapper.setPreviewRenderer(mLocalView);
        mVideoGridContainer.addUserVideo(0, mLocalView, true);

        // enable audio/video capture
        mStreamingKitWrapper.enableAudioRecording(true);
        mStreamingKitWrapper.enableVideoCapturing(true);
    }

    private void deinitLiveStreaming() {
        Log.i(TAG, "deinitLiveStreaming");
        // disable audio/video capture
        mStreamingKitWrapper.enableVideoCapturing(false);
        mStreamingKitWrapper.enableAudioRecording(false);

        // unset local video view
        mVideoGridContainer.removeUserVideo(0, true);
        mStreamingKitWrapper.setPreviewRenderer(null);

        mRtcEngineWrapper.deinit();
        mStreamingKitWrapper.deinit();
        mFUVideoFilter.deinit();

        if (mWorkerThread != null) {
            mWorkerThread.quitSafely();
            mWorkerThread = null;
        }
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
            mStreamingKitWrapper.stopStreaming();
        } else if (mCurrentStreamingMode == STREAMING_MODE.SERVER_STREAMING) {
            stopServerStreamingAndLeaveAgoraChannel();
        } else if (mCurrentStreamingMode == STREAMING_MODE.SIMUL_STREAMING) {
            mStreamingKitWrapper.stopStreaming();
            stopServerStreamingAndLeaveAgoraChannel();
        }
        mCurrentStreamingMode = STREAMING_MODE.NONE;

        mStatsManager.clearAllData();
        deinitLiveStreaming();
        super.onDestroy();
    }

    private SurfaceView prepareRemoteVideo(int uid) {
        SurfaceView surface = RtcEngine.CreateRendererView(getApplicationContext());
        mRtcEngineWrapper.setupRemoteVideo(surface, VideoCanvas.RENDER_MODE_HIDDEN, uid,
            PrefManager.VIDEO_MIRROR_MODES[PrefManager.getMirrorRemoteIndex(this)]);
        return surface;
    }

    private void removeRemoteVideo(int uid) {
        mRtcEngineWrapper.setupRemoteVideo(null, VideoCanvas.RENDER_MODE_HIDDEN, uid);
    }

    private void renderRemoteUser(int uid) {
        SurfaceView surface = prepareRemoteVideo(uid);
        mVideoGridContainer.addUserVideo(uid, surface, false);
    }

    private void removeRemoteUser(int uid) {
        mVideoGridContainer.removeUserVideo(uid, false);
        removeRemoteVideo(uid);
    }

    private void joinAgoraChannelAndStartServerStreaming() {
        mRtcEngineWrapper.setExternalAudioSource(true);
        mStreamingKitWrapper.registerAudioFrameObserver(mRtcEngineWrapper);
        mRtcEngineWrapper.setVideoSource(true);
        mStreamingKitWrapper.registerVideoFrameObserver(mRtcEngineWrapper);
        mRtcEngineWrapper.joinChannel(mRoomName);
        mPendingServerStreaming = true;
    }

    private void stopServerStreamingAndLeaveAgoraChannel() {
        mPendingServerStreaming = false;
        if (mIsServerStreaming) {
            mRtcEngineWrapper.removePublishStreamUrl();
            mIsServerStreaming = false;
        }
        mRtcEngineWrapper.leaveChannel();
        mStreamingKitWrapper.unregisterVideoFrameObserver(mRtcEngineWrapper);
        mStreamingKitWrapper.unregisterAudioFrameObserver(mRtcEngineWrapper);
    }

    public void onStreamingBtnClicked(View view) {
        Log.i(TAG, "onStreamingBtnClicked");

        if (mCurrentStreamingMode == STREAMING_MODE.NONE) {
            Log.i(TAG, "start client streaming directly");
            mStreamingKitWrapper.startStreaming();
            mCurrentStreamingMode = STREAMING_MODE.CLIENT_STREAMING;

        } else if (mCurrentStreamingMode == STREAMING_MODE.CLIENT_STREAMING) {
            Log.i(TAG, "join agora channel and start server streaming");
            mStreamingKitWrapper.stopStreaming();
            joinAgoraChannelAndStartServerStreaming();
            mCurrentStreamingMode = STREAMING_MODE.SERVER_STREAMING;

        } else if (mCurrentStreamingMode == STREAMING_MODE.SERVER_STREAMING) {
            Log.i(TAG, "leave agora channel and start client streaming");
            stopServerStreamingAndLeaveAgoraChannel();
            mStreamingKitWrapper.startStreaming();
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
        mStreamingKitWrapper.setPreviewRenderer(null);
        int ret = mStreamingKitWrapper.switchCamera();

        // Notify FaceUnity SDK of camera change
        if (ret == 0) {
            boolean isCameraFacingFront = mStreamingKitWrapper.isCameraFacingFront();
            int currentCameraType = isCameraFacingFront ? Camera.CameraInfo.CAMERA_FACING_FRONT
                : Camera.CameraInfo.CAMERA_FACING_BACK;
            int inputImageOrientation = isCameraFacingFront ? 270 : 90;
            mFUVideoFilter.onCameraChange(currentCameraType, inputImageOrientation);
        }

        // Re-enable renderer
        mStreamingKitWrapper.setPreviewRenderer(mLocalView);
    }

    public void onBeautyClicked(View view) {
        view.setActivated(!view.isActivated());
        if (view.isActivated()) {
            mBeautyLayout.setVisibility(View.VISIBLE);
            mStreamingKitWrapper.addVideoFilter(mFUVideoFilter);
            showToast(getString(R.string.fu_face_tracking_time_tips));
        } else {
            mBeautyLayout.setVisibility(View.GONE);
            mStreamingKitWrapper.removeVideoFilter(mFUVideoFilter);
        }
    }

    public void onMuteAudioClicked(View view) {
        view.setActivated(!view.isActivated());
        boolean muted = !view.isActivated();
        mRtcEngineWrapper.muteLocalAudioStream(muted);
        mStreamingKitWrapper.muteAudioStream(muted);
        showToast((muted ? "mute" : "un-mute") + " audio");
    }

    public void onMuteVideoClicked(View view) {
        view.setActivated(!view.isActivated());
        boolean muted = !view.isActivated();
        mRtcEngineWrapper.muteLocalVideoStream(muted);
        mStreamingKitWrapper.muteVideoStream(muted);
        showToast((muted ? "mute" : "un-mute") + " video");
    }

    public void onServerStreamingClicked(View view) {
        view.setActivated(!view.isActivated());
        boolean publish = view.isActivated();
        if (publish) {
            mStreamingKitWrapper.startStreaming();
            joinAgoraChannelAndStartServerStreaming();
            mCurrentStreamingMode = STREAMING_MODE.SIMUL_STREAMING;
        } else {
            stopServerStreamingAndLeaveAgoraChannel();
            mStreamingKitWrapper.stopStreaming();
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

    private class MyRtcEventHandler extends IRtcEngineEventHandler {

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            showToast("join channel success");
            mUidList.add(uid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mPendingServerStreaming) {
                        mPendingServerStreaming = false;
                        VideoEncoderConfiguration.VideoDimensions videoDimension =
                            PrefManager.VIDEO_DIMENSIONS[PrefManager.getVideoDimensionsIndex(LiveActivity.this)];
                        int videoBitrate =
                            PrefManager.VIDEO_BITRATES[PrefManager.getVideoBitrateIndex(LiveActivity.this)];
                        int videoFramerate =
                            PrefManager.VIDEO_FRAMERATES[PrefManager.getVideoFramerateIndex(LiveActivity.this)].getValue();
                        mRtcEngineWrapper.setLiveTranscoding(mUidList, videoDimension.width,
                            videoDimension.height, videoBitrate, videoFramerate);
                        int ret = mRtcEngineWrapper.addPublishStreamUrl();
                        if (ret != 0) {
                            showToast("addPublishStreamUrl failed: " + ret);
                        } else {
                            mIsServerStreaming = true;
                        }
                    }
                }
            });
        }

        @Override
        public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
            // Do nothing at the moment
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            mUidList.add(uid);
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
            mUidList.remove(Integer.valueOf(uid));
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

        @Override
        public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
            if (!mStatsManager.isEnabled()) return;

            LocalStatsData data = (LocalStatsData) mStatsManager.getStatsData(0);
            if (data == null) return;

            data.setWidth(mVideoDimension.width);
            data.setHeight(mVideoDimension.height);
            data.setFramerate(stats.sentFrameRate);
        }

        @Override
        public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
            if (!mStatsManager.isEnabled()) return;

            LocalStatsData data = (LocalStatsData) mStatsManager.getStatsData(0);
            if (data == null) return;

            data.setLastMileDelay(stats.lastmileDelay);
            data.setVideoSendBitrate(stats.txVideoKBitRate);
            data.setVideoRecvBitrate(stats.rxVideoKBitRate);
            data.setAudioSendBitrate(stats.txAudioKBitRate);
            data.setAudioRecvBitrate(stats.rxAudioKBitRate);
            data.setCpuApp(stats.cpuAppUsage);
            data.setCpuTotal(stats.cpuAppUsage);
            data.setSendLoss(stats.txPacketLossRate);
            data.setRecvLoss(stats.rxPacketLossRate);
        }

        @Override
        public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
            if (!mStatsManager.isEnabled()) return;

            StatsData data = mStatsManager.getStatsData(uid);
            if (data == null) return;

            data.setSendQuality(mStatsManager.qualityToString(txQuality));
            data.setRecvQuality(mStatsManager.qualityToString(rxQuality));
        }

        @Override
        public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
            if (!mStatsManager.isEnabled()) return;

            RemoteStatsData data = (RemoteStatsData) mStatsManager.getStatsData(stats.uid);
            if (data == null) return;

            data.setWidth(stats.width);
            data.setHeight(stats.height);
            data.setFramerate(stats.rendererOutputFrameRate);
            data.setVideoDelay(stats.delay);
        }

        @Override
        public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
            if (!mStatsManager.isEnabled()) return;

            RemoteStatsData data = (RemoteStatsData) mStatsManager.getStatsData(stats.uid);
            if (data == null) return;

            data.setAudioNetDelay(stats.networkTransportDelay);
            data.setAudioNetJitter(stats.jitterBufferDelay);
            data.setAudioLoss(stats.audioLossRate);
            data.setAudioQuality(mStatsManager.qualityToString(stats.quality));
        }
    }

    private class MyFuVideoFilter extends VideoFilter implements SurfaceHolder.Callback,
        SensorEventListener {

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
