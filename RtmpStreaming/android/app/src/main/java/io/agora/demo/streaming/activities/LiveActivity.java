package io.agora.demo.streaming.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.util.HashSet;
import java.util.Set;

import io.agora.base.SnapshotFrame;
import io.agora.demo.streaming.R;
import io.agora.demo.streaming.StreamingMode;
import io.agora.demo.streaming.beauty.BeautyVideoFilter;
import io.agora.demo.streaming.presenter.LiveStreamingPresenter;
import io.agora.demo.streaming.ui.OperationMenu;
import io.agora.demo.streaming.ui.VideoGridContainer;
import io.agora.demo.streaming.utils.MediaStoreUtil;
import io.agora.demo.streaming.utils.PrefManager;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.streaming.SnapshotCallback;
import io.agora.streaming.StreamingEventHandler;
import io.agora.streaming.VideoDeviceError;
import io.agora.streaming.VideoDeviceEventHandler;

public class LiveActivity extends BaseActivity {
    private static final String TAG = LiveActivity.class.getSimpleName();
    public static final String KEY_ROOM_NAME = "key_room_name";

    private int mCurrentStreamingMode = StreamingMode.NONE;

    private LiveStreamingPresenter mPresenter;
    private IRtcEngineEventHandler mRtcEngineEventHandler = new MyRtcEngineEventHandler();
    private StreamingEventHandler mStreamingEventHandler = new MyStreamingEventHandler();
    private VideoDeviceEventHandler mVideoDeviceEventHandler = new MyVideoDeviceEventHandler();
    private BeautyVideoFilter mBeautyVideoFilter;

    private boolean mLocalAudioMuted = false;
    private boolean mLocalVideoMuted = false;
    private boolean mVideoCapturerEnabled = false;
    private volatile boolean mPendingVideoCapturerFailure = false;

    private String mRoomName;
    private VideoGridContainer mVideoGridContainer;
    private Button mStartStreamingBtn;
    private SurfaceView mLocalView;
    private Toast mToast;
    private Set<Integer> mRemoteUidSet = new HashSet<>();

    private ViewGroup mBeautyLayout;
    private View beautyView = null;

    // for simultaneous streaming test
    private ImageView mSimulStreamingBtn;

    //screen touch
    private ScreenTouchImpl screenTouch;
    private ScreenWindow screenWindow;

    //operation menu
    private PopupMenu popupMenu;
    private OperationMenu operationMenu;

    //mute video and audio btn
    ImageView muteVideoBtn;
    ImageView muteAudioBtn;
    boolean muteVideoForever = false;
    boolean muteAudioForever = false;

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
        setCameraOperation();
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
        muteVideoBtn = findViewById(R.id.live_btn_mute_video);
        muteVideoBtn.setActivated(true);
        muteAudioBtn = findViewById(R.id.live_btn_mute_audio);
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
        mPresenter.registerVideoDeviceEventHandler(mVideoDeviceEventHandler);

        // beauty video filter
        mBeautyVideoFilter = new BeautyVideoFilter();
        mBeautyVideoFilter.init(this);
        View beautyActionView = mBeautyVideoFilter.getActionView();
        if(beautyActionView != null){
            mBeautyLayout.addView(beautyActionView);
        }
        mLocalView.getHolder().addCallback(mBeautyVideoFilter);


        // set local video view
        mPresenter.setPreview(mLocalView);
        mVideoGridContainer.addUserVideo(0, mLocalView, true);

        mPresenter.enableAudioRecording(true);
        mPresenter.enableVideoCapturing(true);
        streamingTypeSelect();
        mVideoCapturerEnabled = true;
    }

    private void deinitLiveStreaming() {
        Log.i(TAG, "deinitLiveStreaming");
        mPresenter.enableAudioRecording(false);
        mPresenter.enableVideoCapturing(false);
        mVideoCapturerEnabled = false;

        // unset local video view
        mVideoGridContainer.removeUserVideo(0, true);
        mPresenter.setPreview(null);

        mPresenter.unregisterVideoDeviceEventHandler(mVideoDeviceEventHandler);
        mPresenter.setRtcEngineEventHandler(null);
        mPresenter.setStreamingEventHandler(null);
        LiveStreamingPresenter.destroyEngineAndKit();

        mLocalView.getHolder().removeCallback(mBeautyVideoFilter);
        mBeautyLayout.removeAllViews();
        mBeautyVideoFilter.deinit();
    }

    /**
     * add by nianji tang 2020-10-15
     */
    private void setCameraOperation() {
        //Camera
        if (screenTouch == null) {
            screenTouch = new ScreenTouchImpl(mPresenter);
        }
        if (screenWindow == null) {
            screenWindow = new ScreenWindow(this);
        }
        screenTouch.setScreenWindow(screenWindow);
        screenWindow.setAutoFocusState(true);
        screenTouch.startDrawThread();
        mLocalView.setOnTouchListener(screenTouch);
    }

    private void deinitCameraOperation() {
        try {
            screenTouch.destroyDraw();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        screenWindow.setAutoFocusState(false);
        mLocalView.setOnTouchListener(null);
    }

    private void streamingTypeSelect() {
        int streamingType = PrefManager.getStreamTypeIndex(this);
        switch (streamingType) {
            case PrefManager.StreamType
                    .TYPE_AUDIO_AND_VIDEO:
                mPresenter.muteLocalAudioStream(false);
                mPresenter.muteLocalVideoStream(false);
                muteVideoForever = false;
                muteAudioForever = false;
                break;
            case PrefManager.StreamType.TYPE_AUDIO_ONLY:
                mPresenter.muteLocalAudioStream(false);
                mPresenter.muteLocalVideoStream(true);
                muteVideoBtn.setActivated(false);
                muteVideoForever = true;
                break;
            case PrefManager.StreamType.TYPE_VIDEO_ONLY:
                mPresenter.muteLocalVideoStream(false);
                mPresenter.muteLocalAudioStream(true);
                muteAudioBtn.setActivated(false);
                muteAudioForever = true;
                break;
            default:
                break;
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
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mCurrentStreamingMode == StreamingMode.CLIENT_STREAMING) {
            mPresenter.stopClientStreaming();
        } else if (mCurrentStreamingMode == StreamingMode.SERVER_STREAMING) {
            mPresenter.stopServerStreamingAndLeaveAgoraChannel();
        } else if (mCurrentStreamingMode == StreamingMode.SIMUL_STREAMING) {
            mPresenter.stopClientStreaming();
            mPresenter.stopServerStreamingAndLeaveAgoraChannel();
        }
        mCurrentStreamingMode = StreamingMode.NONE;

        // reset to initial state
        if (mLocalVideoMuted) {
            mPresenter.muteLocalVideoStream(false);
            mLocalVideoMuted = false;
        }
        if (mLocalAudioMuted) {
            mPresenter.muteLocalAudioStream(false);
            mLocalAudioMuted = false;
        }
        deinitCameraOperation();
        onBeautyClose();
        deinitLiveStreaming();
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
        if (mCurrentStreamingMode == StreamingMode.NONE) {
            Log.i(TAG, "start client streaming directly");
            mPresenter.startClientStreaming();
            mCurrentStreamingMode = StreamingMode.CLIENT_STREAMING;
        } else if (mCurrentStreamingMode == StreamingMode.CLIENT_STREAMING) {
            Log.i(TAG, "join agora channel and start server streaming");
            mPresenter.stopClientStreaming();
            mPresenter.joinAgoraChannelAndStartServerStreaming(mRoomName);
            mCurrentStreamingMode = StreamingMode.SERVER_STREAMING;
        } else if (mCurrentStreamingMode == StreamingMode.SERVER_STREAMING) {
            Log.i(TAG, "leave agora channel and start client streaming");
            mPresenter.stopServerStreamingAndLeaveAgoraChannel();
            for (int uid : mRemoteUidSet) {
                removeRemoteUser(uid);
            }
            mPresenter.startClientStreaming();
            mCurrentStreamingMode = StreamingMode.CLIENT_STREAMING;
        }
        screenTouch.clearDraw();
        if (mCurrentStreamingMode == StreamingMode.CLIENT_STREAMING) {
            mStartStreamingBtn.setText(getString(R.string.switch_to_agora_channel));
        } else if (mCurrentStreamingMode == StreamingMode.SERVER_STREAMING) {
            mStartStreamingBtn.setText(getString(R.string.switch_to_rtmp_streaming));
        }
    }

    public void onLeaveClicked(View view) {
        Log.i(TAG, "onLeaveClicked");
        try {
            screenTouch.destroyDraw();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finish();
    }

    public void showPopMenu(View view) {
        if (popupMenu == null) {
            popupMenu = new PopupMenu(this, view);
            MenuInflater inflater = null;
            inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.operation_menu, popupMenu.getMenu());
            operationMenu = new OperationMenu(this);
            popupMenu.setOnMenuItemClickListener(operationMenu);
            popupMenu.setOnDismissListener(operationMenu);
        }
        popupMenu.show();
    }

    public void onSwitchResolution(View view, MenuItem item) {
        // switchCamera is deprecated, use switchCameraSource instead
        int ratio = PrefManager.getVideoRatioIndex(this);
        int new_ratio = ratio + 1;
        if (new_ratio > 2) {
            new_ratio = 0;
        }
        PrefManager.setVideoRatioIndex(this, new_ratio);

        int ret = mPresenter.switchResolution(PrefManager.getResolutionWidth(this), PrefManager.getResolutionHeight(this));
        // Notify FaceUnity SDK of camera change
        if (ret == 0) {
            screenTouch.clearDraw(); //改变摄像头前置和后置，则清除聚焦框
            showSwitchResolutionBtnText(item, new_ratio);
        } else {
            PrefManager.setVideoRatioIndex(this, ratio);
        }
    }


    public void onSnapshot(View view, MenuItem item) {
        int ret = mPresenter.snapshot(new SnapshotCallback() {
            @Override
            public void onSnapshot(SnapshotFrame snapshotFrame) {
                Bitmap bmp = snapshotFrame.getBitmap();
                String filename = "RSK_" + System.currentTimeMillis() + ".jpg";
                new Thread(() -> {
                    boolean ret = MediaStoreUtil.saveBmp2Gallery(LiveActivity.this, bmp, filename);
                    if (ret) {
                        showToast(getString(R.string.save_pic_success));
                    } else {
                        showToast(getString(R.string.save_pic_failed));
                    }
                    bmp.recycle();
                }).start();

            }

        });

        if (ret != 0) {
            showToast( "snapshot error " + ret);
        }
    }

    public void showSwitchResolutionBtnText(MenuItem item, int resolutionRatioId) {
        switch (resolutionRatioId) {
            case 0:
                item.setTitle("1:1");
                break;
            case 1:
                item.setTitle("16:9");
                break;
            case 2:
                item.setTitle("4:3");
        }
    }

    public void onSwitchCameraClicked(View view) {
        // switchCamera is deprecated, use switchCameraSource instead
        int ret = mPresenter.switchCameraSource();

        // Notify FaceUnity SDK of camera change
        if (ret == 0) {
            boolean isCameraFacingFront = mPresenter.isCameraFacingFront();
            int currentCameraType = isCameraFacingFront ? Camera.CameraInfo.CAMERA_FACING_FRONT
                    : Camera.CameraInfo.CAMERA_FACING_BACK;
            int inputImageOrientation = isCameraFacingFront ? 270 : 90;
            mBeautyVideoFilter.onCameraChange(currentCameraType, inputImageOrientation);
            screenTouch.clearDraw();                //改变摄像头前置和后置，则清除聚焦框
        }
    }

    public void onBeautyClicked(View view) {
        if(!BeautyVideoFilter.enableBeauty){
            return;
        }
        view.setActivated(!view.isActivated());
        if (view.isActivated()) {
            mBeautyLayout.setVisibility(View.VISIBLE);
            mPresenter.addVideoFilter(mBeautyVideoFilter);
            showToast(getString(R.string.fu_face_tracking_time_tips));
            beautyView = view;
        } else {
            mBeautyLayout.setVisibility(View.GONE);
            mPresenter.removeVideoFilter(mBeautyVideoFilter);
            beautyView = null;
        }
        screenTouch.clearDraw();
        Log.d(TAG, "onBeautyClicked");
    }

    private void onBeautyClose() {
        if(!BeautyVideoFilter.enableBeauty){
            return;
        }
        if (beautyView != null) {
            beautyView.setActivated(false);
            mBeautyLayout.setVisibility(View.GONE);
            mPresenter.removeVideoFilter(mBeautyVideoFilter);
            beautyView = null;
        }
        Log.d(TAG, "BeautyClose");
    }

    public void onMuteAudioClicked(View view) {
        if (muteAudioForever) {
            return;
        }
        view.setActivated(!view.isActivated());
        mLocalAudioMuted = !view.isActivated();
        mPresenter.muteLocalAudioStream(mLocalAudioMuted);
        screenTouch.clearDraw();
        showToast((mLocalAudioMuted ? "mute" : "un-mute") + " audio");
    }

    public void onMuteVideoClicked(View view) {
        if (muteVideoForever) {
            return;
        }
        view.setActivated(!view.isActivated());
        mLocalVideoMuted = !view.isActivated();
        mPresenter.muteLocalVideoStream(mLocalVideoMuted);
        screenTouch.clearDraw();
        showToast((mLocalVideoMuted ? "mute" : "un-mute") + " video");
    }

    public void onServerStreamingClicked(View view) {
        view.setActivated(!view.isActivated());
        boolean publish = view.isActivated();
        if (publish) {
            mPresenter.startClientStreaming();
            mPresenter.joinAgoraChannelAndStartServerStreaming(mRoomName);
            mCurrentStreamingMode = StreamingMode.SIMUL_STREAMING;
        } else {
            mPresenter.stopServerStreamingAndLeaveAgoraChannel();
            mPresenter.stopClientStreaming();
            mCurrentStreamingMode = StreamingMode.NONE;
        }
        screenTouch.clearDraw();
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
//        mBeautyVideoFilter.onActivityResume();

        // restart video capturing if failure occurred in background
        if (mVideoCapturerEnabled && mPendingVideoCapturerFailure) {
            mPresenter.enableVideoCapturing(false);
            mPresenter.enableVideoCapturing(true);
            mPendingVideoCapturerFailure = false;
        }
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mBeautyVideoFilter.onActivityPause();
        Log.d(TAG, "onPause");
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

    private class MyVideoDeviceEventHandler extends VideoDeviceEventHandler {

        @Override
        public void onVideoDeviceError(int error) {
            showToast("video device error: " + error);
            if (error != VideoDeviceError.VIDEO_DEVICE_ERROR_OK) {
                mPendingVideoCapturerFailure = true;
            }
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

    public LiveStreamingPresenter getLiveStreamingInstance() {
        return mPresenter;
    }

    public int getCurrentStreamingMode() {
        return mCurrentStreamingMode;
    }

}
