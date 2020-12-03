package io.agora.demo.streaming.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.util.HashSet;
import java.util.Set;

import io.agora.demo.streaming.R;
import io.agora.demo.streaming.LiveStreamingPresenter;
import io.agora.demo.streaming.ui.VideoGridContainer;
import io.agora.demo.streaming.utils.PrefManager;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.streaming.StreamingEventHandler;

public class LiveActivity extends BaseActivity {
    private static final String TAG = LiveActivity.class.getSimpleName();
    public static final String KEY_ROOM_NAME = "key_room_name";

    @interface STREAMING_MODE {
        int NONE = 0;
        int CLIENT_STREAMING = 1;
        int SERVER_STREAMING = 2;
    }
    @STREAMING_MODE
    private int mCurrentStreamingMode = STREAMING_MODE.NONE;

    private LiveStreamingPresenter mPresenter;
    private IRtcEngineEventHandler mRtcEngineEventHandler = new MyRtcEngineEventHandler();
    private StreamingEventHandler mStreamingEventHandler = new MyStreamingEventHandler();

    private boolean mLocalAudioMuted = false;
    private boolean mLocalVideoMuted = false;

    private String mRoomName;
    private VideoGridContainer mVideoGridContainer;
    private Button mStartStreamingBtn;
    private SurfaceView mLocalView;
    private Toast mToast;

    private Set<Integer> mRemoteUidSet = new HashSet<>();

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

        // views
        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mLocalView = new SurfaceView(this);
    }

    private void initLiveStreaming() {
        Log.i(TAG, "initLiveStreaming");

        mPresenter = LiveStreamingPresenter.getInstance();
        mPresenter.setRtcEngineEventHandler(mRtcEngineEventHandler);
        mPresenter.setStreamingEventHandler(mStreamingEventHandler);

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
            PrefManager.getMirrorMoteRemote()));
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
        // disable renderer before switching camera to avoid unexpected render effect
        mPresenter.setPreview(null);
        mPresenter.switchCamera();
        mPresenter.setPreview(mLocalView);
    }

    public void onBeautyClicked(View view) {
        // do nothing for now
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
    }

    @Override
    protected void onPause() {
        super.onPause();
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
}
