package io.agora.mediaplayer;

import android.app.Fragment;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

import io.agora.RtcChannelPublishHelper;

import io.agora.mediaplayer.data.AudioFrame;
import io.agora.mediaplayer.data.MediaStreamInfo;
import io.agora.mediaplayer.data.VideoFrame;

import io.agora.mediaplayer.Constants.*;
import io.agora.mediaplayer.utils.ToolUtil;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.mediaio.AgoraDefaultSource;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.utils.LogUtil;


public class PlayerFragment extends Fragment implements SurfaceHolder.Callback, TextureView.SurfaceTextureListener {
    private AgoraMediaPlayerKit agoraMediaPlayerKit1;
    private Button video1Open;
    private Button video1Play;
    private Button video1Pause;
    private Button video1Stop;
    private Button video1Connect;
    private Button video1PubVideo;
    private Button video1PuAudio;
    private Button video1Disconnect;
    private Button video1mute;
    private Button video1Duration;
    private Button video1GetStream;
    private Button video1GetStreamInfo;

    private EditText video1Path;
    private TextView video1State;
    private TextView video1Info;
    private SeekBar video1Bar;
    private SeekBar video1VoiceBar;
    private SeekBar video1PushVoiceBar;
    private FrameLayout video1Container;
    private SurfaceView videoView1;
    private SurfaceView videoView3;

    private AgoraMediaPlayerKit agoraMediaPlayerKit2;
    private Button video2Open;
    private Button video2Play;
    private Button video2Pause;
    private Button video2Stop;
    private Button video2Connect;
    private Button video2PubVideo;
    private Button video2PuAudio;
    private Button video2Disconnect;
    private Button video2mute;
    private Button video2Duration;
    private Button video2GetStream;
    private Button video2GetStreamInfo;

    private EditText video2Path;
    private TextView video2State;
    private TextView video2Info;
    private SeekBar video2Bar;
    private SeekBar video2VoiceBar;
    private SeekBar video2PushVoiceBar;
    private FrameLayout video2Container;
    private SurfaceView videoView2;
    private ScrollView scrollView;

    private long player1Duration = 0;
    private long player2Duration = 0;

    public PlayerFragment() {
        // need default constructor
    }

    private RtcChannelPublishHelper rtcChannelPublishHelper = null;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        try {
            mRtcEngine = RtcEngine.create(getActivity().getApplicationContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        rtcChannelPublishHelper = RtcChannelPublishHelper.getInstance();
        setupAgoraEngineAndJoinChannel();
        // For Debug
        // mRtcEngine.setParameters("{\"rtc.log_filter\": 65535}");
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        initPlayer();
        initUI(rootView);
        return rootView;
    }

    public void initPlayer() {
        agoraMediaPlayerKit1 = new AgoraMediaPlayerKit(this.getActivity());
        agoraMediaPlayerKit2 = new AgoraMediaPlayerKit(this.getActivity());
        agoraMediaPlayerKit1.registerPlayerObserver(new MediaPlayerObserver() {
            @Override
            public void onPlayerStateChanged(MediaPlayerState state, MediaPlayerError error) {
                LogUtil.i("agoraMediaPlayerKit1 onPlayerStateChanged:" + state + " " + error);
                if(getActivity() == null || getActivity().isFinishing()){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        video1State.setText("state:" + MediaPlayerState.getValue(state) + " error:" + MediaPlayerError.getValue(error));
                    }
                });
            }

            @Override
            public void onPositionChanged(final long position) {
                LogUtil.i("agoraMediaPlayerKit1 seekPosition:" + position + " duration:" + player1Duration);
                if (player1Duration > 0) {
                    if(getActivity() == null || getActivity().isFinishing()){
                        return;
                    }
                    final int result = (int) ((float) position / (float) player1Duration * 100);
                    LogUtil.i("agoraMediaPlayerKit1 seek:" + result);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            video1Bar.setProgress(result);
                        }
                    });
                }
            }

            @Override
            public void onPlayerEvent(MediaPlayerEvent eventCode) {
                LogUtil.i("agoraMediaPlayerKit1 onEvent:" + eventCode);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setInfo1Text("eventCode:" + MediaPlayerEvent.getValue(eventCode));
                        if (MediaPlayerEvent.getValue(eventCode) == 1) {
                            LogUtil.i( "seekPosition 4:" + agoraMediaPlayerKit1.getPlayPosition());
                        }
                    }
                });
            }

            @Override
            public void onMetaData(MediaPlayerMetadataType type, byte[] data) {
                LogUtil.i("agoraMediaPlayerKit1 onMetaData " + new String(data) + " type:" + type);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setInfo1Text("metaData:" + new String(data));
                    }
                });
            }
        });

        agoraMediaPlayerKit1.registerVideoFrameObserver(new VideoFrameObserver() {
            @Override
            public void onFrame(VideoFrame videoFrame) {
                LogUtil.i("agoraMediaPlayerKit1 video onFrame :" + videoFrame);

            }
        });

        agoraMediaPlayerKit1.registerAudioFrameObserver(new AudioFrameObserver() {
            @Override
            public void onFrame(AudioFrame audioFrame) {
                LogUtil.i("agoraMediaPlayerKit1 audio onFrame :" + audioFrame);
            }
        });

        if (agoraMediaPlayerKit2 != null) {
            agoraMediaPlayerKit2.registerPlayerObserver(new MediaPlayerObserver() {
                @Override
                public void onPlayerStateChanged(MediaPlayerState state, MediaPlayerError error) {
                    LogUtil.i("agoraMediaPlayerKit2 onPlayerStateChanged:" + state + " " + error);
                    if(getActivity() == null || getActivity().isFinishing()){
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            video2State.setText("state:" + MediaPlayerState.getValue(state) + " error:" + MediaPlayerError.getValue(error));
                        }
                    });
                }

                @Override
                public void onPositionChanged(final long position) {
                    LogUtil.i("agoraMediaPlayerKit2 onPositionChanged:" + position);
                    if (player2Duration > 0) {
                        final int result = (int) ((float) position / (float) player2Duration * 100);
                        LogUtil.i("agoraMediaPlayerKit2 position:" + result);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                video2Bar.setProgress(result);
                            }
                        });
                    }
                }

                @Override
                public void onPlayerEvent(MediaPlayerEvent eventCode) {
                    LogUtil.i(" agoraMediaPlayerKit2 onEvent:" + eventCode);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setInfo2Text("eventCode:" + MediaPlayerEvent.getValue(eventCode));
                        }
                    });
                }

                @Override
                public void onMetaData(MediaPlayerMetadataType type, byte[] data) {
                    LogUtil.i("agoraMediaPlayerKit1 onMetaData " + new String(data) + " type:" + type);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setInfo2Text("metaData:" + new String(data));
                        }
                    });
                }
            });

            agoraMediaPlayerKit2.registerVideoFrameObserver(new VideoFrameObserver() {
                @Override
                public void onFrame(VideoFrame videoFrame) {
                }
            });

            agoraMediaPlayerKit2.registerAudioFrameObserver(new AudioFrameObserver() {
                @Override
                public void onFrame(AudioFrame audioFrame) {
                }
            });
        }


    }

    public void initUI(View rootView) {
        scrollView = (ScrollView) rootView.findViewById(R.id.scrollview);
        video1Open = (Button) rootView.findViewById(R.id.bt_load1);
        video1Open.setOnClickListener(mOnClickListener);
        video1Play = (Button) rootView.findViewById(R.id.bt_play1);
        video1Play.setOnClickListener(mOnClickListener);
        video1Pause = (Button) rootView.findViewById(R.id.bt_pause1);
        video1Pause.setOnClickListener(mOnClickListener);
        video1Stop = (Button) rootView.findViewById(R.id.bt_stop1);
        video1Stop.setOnClickListener(mOnClickListener);
        video1Path = (EditText) rootView.findViewById(R.id.video1_path);
        video1State = (TextView) rootView.findViewById(R.id.tv_video1_state);
        video1Info = (TextView) rootView.findViewById(R.id.tv_video1_info);
        video1Info.setMovementMethod(ScrollingMovementMethod.getInstance());
        video1Container = (FrameLayout) rootView.findViewById(R.id.player_view_1);


        video1mute = (Button) rootView.findViewById(R.id.bt_mute1);
        video1mute.setOnClickListener(mOnClickListener);
        video1Duration = (Button) rootView.findViewById(R.id.bt_get_duration1);
        video1Duration.setOnClickListener(mOnClickListener);
        video1GetStream = (Button) rootView.findViewById(R.id.bt_get_streams1);
        video1GetStream.setOnClickListener(mOnClickListener);
        video1GetStreamInfo = (Button) rootView.findViewById(R.id.bt_get_stream_info1);
        video1GetStreamInfo.setOnClickListener(mOnClickListener);


        video1Connect = (Button) rootView.findViewById(R.id.bt_connect_rtc1);
        video1Connect.setOnClickListener(mOnClickListener);
        video1PubVideo = (Button) rootView.findViewById(R.id.bt_publish_video1);
        video1PubVideo.setOnClickListener(mOnClickListener);
        video1PuAudio = (Button) rootView.findViewById(R.id.bt_publish_audio1);
        video1PuAudio.setOnClickListener(mOnClickListener);
        video1Disconnect = (Button) rootView.findViewById(R.id.bt_disconnet_rtc1);
        video1Disconnect.setOnClickListener(mOnClickListener);


        video1Bar = (SeekBar) rootView.findViewById(R.id.sb_1);
        video1Bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (agoraMediaPlayerKit1 != null && player1Duration > 0) {

                    long durationTemp = (long) (player1Duration * ((float) seekBar.getProgress() / 100));
                    LogUtil.i("onStopTrackingTouch1:" + seekBar.getProgress() + " seek duration:" + durationTemp);
                    agoraMediaPlayerKit1.seek(durationTemp);
                }
            }
        });

        video1VoiceBar = (SeekBar) rootView.findViewById(R.id.sb_volume1);
        video1VoiceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                agoraMediaPlayerKit1.adjustPlayoutVolume(seekBar.getProgress());
            }
        });

        video1PushVoiceBar = (SeekBar) rootView.findViewById(R.id.sb_rtc_volume1);
        video1PushVoiceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (rtcChannelPublishHelper != null) {
                    rtcChannelPublishHelper.adjustPublishSignalVolume(seekBar.getProgress(), 400);
                }
            }
        });

        videoView1 = new SurfaceView(this.getActivity());
        videoView1.getHolder().addCallback(this);
        videoView1.setZOrderMediaOverlay(false);
        //video1Container.addView(videoView1);
        //rtc.setupLocalVideo(new VideoCanvas(videoView1, VideoCanvas.RENDER_MODE_FIT, 0));
        //rtc.startPreview();
        agoraMediaPlayerKit1.setView(videoView1);
        video1Container.addView(videoView1);


        video2Open = (Button) rootView.findViewById(R.id.bt_load2);
        video2Open.setOnClickListener(mOnClickListener);
        video2Play = (Button) rootView.findViewById(R.id.bt_play2);
        video2Play.setOnClickListener(mOnClickListener);
        video2Pause = (Button) rootView.findViewById(R.id.bt_pause2);
        video2Pause.setOnClickListener(mOnClickListener);
        video2Stop = (Button) rootView.findViewById(R.id.bt_stop2);
        video2Stop.setOnClickListener(mOnClickListener);
        video2Path = (EditText) rootView.findViewById(R.id.video2_path);
        video2State = (TextView) rootView.findViewById(R.id.tv_video2_state);
        video2Info = (TextView) rootView.findViewById(R.id.tv_video2_info);
        video2Info.setMovementMethod(ScrollingMovementMethod.getInstance());
        video2Container = (FrameLayout) rootView.findViewById(R.id.player_view_2);

        video2Connect = (Button) rootView.findViewById(R.id.bt_connect_rtc2);
        video2Connect.setOnClickListener(mOnClickListener);
        video2PubVideo = (Button) rootView.findViewById(R.id.bt_publish_video2);
        video2PubVideo.setOnClickListener(mOnClickListener);
        video2PuAudio = (Button) rootView.findViewById(R.id.bt_publish_audio2);
        video2PuAudio.setOnClickListener(mOnClickListener);
        video2Disconnect = (Button) rootView.findViewById(R.id.bt_disconnet_rtc2);
        video2Disconnect.setOnClickListener(mOnClickListener);
        video2mute = (Button) rootView.findViewById(R.id.bt_mute2);
        video2mute.setOnClickListener(mOnClickListener);
        video2Duration = (Button) rootView.findViewById(R.id.bt_get_duration2);
        video2Duration.setOnClickListener(mOnClickListener);
        video2GetStream = (Button) rootView.findViewById(R.id.bt_get_streams2);
        video2GetStream.setOnClickListener(mOnClickListener);
        video2GetStreamInfo = (Button) rootView.findViewById(R.id.bt_get_stream_info2);
        video2GetStreamInfo.setOnClickListener(mOnClickListener);

        video2Bar = (SeekBar) rootView.findViewById(R.id.sb_2);
        video2Bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (agoraMediaPlayerKit2 != null && player2Duration > 0) {

                    long durationTemp = (long) (player2Duration * ((float) seekBar.getProgress() / 100));
                    LogUtil.i("onStopTrackingTouch2:" + seekBar.getProgress() + " seek duration:" + durationTemp);
                    agoraMediaPlayerKit2.seek(durationTemp);
                }
            }
        });

        video2VoiceBar = (SeekBar) rootView.findViewById(R.id.sb_volume2);
        video2VoiceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                agoraMediaPlayerKit2.adjustPlayoutVolume(seekBar.getProgress());
            }
        });

        video2PushVoiceBar = (SeekBar) rootView.findViewById(R.id.sb_rtc_volume2);
        video2PushVoiceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (rtcChannelPublishHelper != null) {
                    rtcChannelPublishHelper.adjustPublishSignalVolume(seekBar.getProgress(), 400);
                }
            }
        });

        videoView2 = new SurfaceView(getActivity());
        videoView3 = new SurfaceView(getActivity());


        videoView2.setZOrderMediaOverlay(true);
        videoView2.getHolder().addCallback(this);
        if (agoraMediaPlayerKit2 != null) {
            agoraMediaPlayerKit2.setView(videoView2);
        }
        videoView2.getHolder().setFormat(PixelFormat.TRANSPARENT);
    }


    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i("onResume:");
    }

    @Override
    public void onStop() {
        super.onStop();
        leaveChannel();
        agoraMediaPlayerKit1.stop();
        agoraMediaPlayerKit2.stop();
        LogUtil.i("onStop:");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i("onDestroy:");

    }

    @Override
    public void onPause() {
        LogUtil.i("onPause:");
        super.onPause();
    }

    private boolean isAgoraStarted = false;
    private int voiceCount1 = 0;
    private int voiceCount2 = 0;
    /**
     * method when touch record button
     */
    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.bt_load1:
                    if (voiceCount1 == 0) {
                        voiceCount1 = 1;
                    }
                    video1Load();
                    break;
                case R.id.bt_play1:
                    video1Play();
                    break;
                case R.id.bt_pause1:
                    video1Pause();
                    break;
                case R.id.bt_stop1:
                    video1Stop();
                    break;
                case R.id.bt_load2:
                    video2Load();
                    break;
                case R.id.bt_play2:
                    video2Play();
                    break;
                case R.id.bt_pause2:
                    video2Pause();
                    break;
                case R.id.bt_stop2:
                    video2Stop();
                    break;
                case R.id.bt_connect_rtc1:
                    video1Connect();
                    break;
                case R.id.bt_publish_audio1:
                    video1PubAudio();
                    break;
                case R.id.bt_publish_video1:
                    video1PubVideo();
                    break;
                case R.id.bt_disconnet_rtc1:
                    break;
                case R.id.bt_connect_rtc2:
                    video2Connect();
                    break;
                case R.id.bt_publish_audio2:
                    video2PubAudio();
                    break;
                case R.id.bt_publish_video2:
                    video2PubVideo();
                    break;
                case R.id.bt_disconnet_rtc2:
                    break;
                case R.id.bt_mute1:
                    video1Mute();
                    break;
                case R.id.bt_get_duration1:
                    video1Duration();
                    break;
                case R.id.bt_get_streams1:
                    video1GetStream();
                    break;
                case R.id.bt_get_stream_info1:
                    video1GetStreamInfo();
                    break;
                case R.id.bt_mute2:
                    video2Mute();
                    break;
                case R.id.bt_get_duration2:
                    video2Duration();
                    break;
                case R.id.bt_get_streams2:
                    video2GetStream();
                    break;
                case R.id.bt_get_stream_info2:
                    video2GetStreamInfo();
                    break;
            }
        }
    };

    private void video1Mute() {
        if(video1mute.getTag() == null || (boolean) (video1mute.getTag()) == false) {
            video1mute.setTag(true);
            video1mute.setText("unMute");
            agoraMediaPlayerKit1.mute(true);
        } else {
            video1mute.setTag(false);
            video1mute.setText("Mute");
            agoraMediaPlayerKit1.mute(false);
        }
        boolean muteState = agoraMediaPlayerKit1.isMuted();
        setInfo1Text("Mute:"+muteState);
    }

    private void video1Duration() {
        long duration = agoraMediaPlayerKit1.getDuration();
        setInfo1Text("duration:" + duration);
    }

    private void video1GetStream() {
        int streamCount = agoraMediaPlayerKit1.getStreamCount();
        setInfo1Text("streamCount:" + streamCount);

    }

    private void video1GetStreamInfo() {
        int streamCount = agoraMediaPlayerKit1.getStreamCount();
        String streamInfos = "";
        for (int i = 0; i < streamCount; i++) {
            String streamInfo = agoraMediaPlayerKit1.getStreamInfo(i).toString();
            streamInfos = streamInfos + streamInfo + "\n";
            LogUtil.i( "streamInfo:" + streamInfo);
        }
        agoraMediaPlayerKit1.getStreamInfo(streamCount);
        setInfo1Text("streamInfos:" + streamInfos);
    }

    private void video1Stop() {
        agoraMediaPlayerKit1.stop();
    }

    private void video1Pause() {
        agoraMediaPlayerKit1.pause();
    }

    private void video1Play() {
        //
        agoraMediaPlayerKit1.play();
        player1Duration = agoraMediaPlayerKit1.getDuration();
        LogUtil.i("player1Duration:" + player1Duration);
        int count = agoraMediaPlayerKit1.getStreamCount();
        LogUtil.i("video1Play count:" + count);
        for (int i = 0; i < count; i++) {
            MediaStreamInfo mediaStreamInfo = agoraMediaPlayerKit1.getStreamInfo(i);
            LogUtil.i("video1Play mediaStreamInfo:" + mediaStreamInfo);
        }


    }

    private int test = 0;

    private void video1Load() {
        agoraMediaPlayerKit1.open(video1Path.getText().toString(), 0);
        LogUtil.i("player1Duration:" + player1Duration);
    }

    private void video1Connect() {
        if (video1Connect.getTag() == null || (boolean) (video1Connect.getTag()) == false) {
            video1Connect.setTag(true);
            video1Connect.setText("断开");
            rtcChannelPublishHelper.attachPlayerToRtc(agoraMediaPlayerKit1, this.mRtcEngine);

        } else {
            video1Connect.setTag(false);
            video1Connect.setText("链接");
            rtcChannelPublishHelper.detachPlayerFromRtc();
            //switch to camera view
            mRtcEngine.setVideoSource(new AgoraDefaultSource());
        }

    }


    private void video1PubVideo() {
        if (video1PubVideo.getTag() == null || (boolean) (video1PubVideo.getTag()) == false) {
            video1PubVideo.setTag(true);
            video1PubVideo.setText("停止推");
            rtcChannelPublishHelper.publishVideo();
        } else {
            video1PubVideo.setTag(false);
            video1PubVideo.setText("推视频");
            rtcChannelPublishHelper.unpublishVideo();
        }
    }

    private void video1PubAudio() {
        if (video1PuAudio.getTag() == null || (boolean) (video1PuAudio.getTag()) == false) {
            video1PuAudio.setTag(true);
            video1PuAudio.setText("停止推");
            // For Debug
            // mRtcEngine.setParameters("{\"che.audio.headset.monitoring\":true}");
            // mRtcEngine.setParameters("{\"che.audio.enable.androidlowlatencymode\":true}");
            // mRtcEngine.setParameters(String.format(Locale.US, "{\"che.audio.profile\":{\"scenario\":%d}}", 1));
            // mRtcEngine.adjustRecordingSignalVolume(100);
            rtcChannelPublishHelper.adjustPublishSignalVolume(400, 400);
            rtcChannelPublishHelper.publishAudio();
        } else {
            video1PuAudio.setTag(false);
            video1PuAudio.setText("推音频");
            rtcChannelPublishHelper.unpublishAudio();

        }
    }


    private void video2Stop() {
        agoraMediaPlayerKit2.stop();
    }

    private void video2Pause() {
        agoraMediaPlayerKit2.pause();
    }

    private void video2Play() {
        agoraMediaPlayerKit2.play();
        player2Duration = agoraMediaPlayerKit2.getDuration();
    }

    private boolean isView2add = false;

    private void video2Load() {
        if (!isView2add) {
            isView2add = true;
            video2Container.addView(videoView2);
        }
        agoraMediaPlayerKit2.open(video2Path.getText().toString(), 0);
        player2Duration = agoraMediaPlayerKit2.getDuration();
    }

    private void video2Connect() {
        if (video2Connect.getTag() == null || (boolean) (video2Connect.getTag()) == false) {
            video2Connect.setTag(true);
            video2Connect.setText("断开");
            rtcChannelPublishHelper.attachPlayerToRtc(agoraMediaPlayerKit2, this.mRtcEngine);

        } else {
            video2Connect.setTag(false);
            video2Connect.setText("链接");
            rtcChannelPublishHelper.detachPlayerFromRtc();
            //switch to camera view
            mRtcEngine.setVideoSource(new AgoraDefaultSource());
        }
    }


    private void video2PubVideo() {
        if (video2PubVideo.getTag() == null || (boolean) (video2PubVideo.getTag()) == false) {
            video2PubVideo.setTag(true);
            video2PubVideo.setText("停止推");
            rtcChannelPublishHelper.publishVideo();
        } else {
            video2PubVideo.setTag(false);
            video2PubVideo.setText("推视频");
            rtcChannelPublishHelper.unpublishVideo();
        }
    }

    private void video2PubAudio() {
        if (video2PuAudio.getTag() == null || (boolean) (video2PuAudio.getTag()) == false) {
            video2PuAudio.setTag(true);
            video2PuAudio.setText("停止推");
            agoraMediaPlayerKit2.adjustPlayoutVolume(0);
            rtcChannelPublishHelper.adjustPublishSignalVolume(400, 400);
            rtcChannelPublishHelper.publishAudio();
        } else {
            video2PuAudio.setTag(false);
            video2PuAudio.setText("推音频");
            rtcChannelPublishHelper.unpublishAudio();
        }
    }


    private void video2Mute() {
        if (video2mute.getTag() == null || (boolean) (video2mute.getTag()) == false) {
            video2mute.setTag(true);
            video2mute.setText("unMute");
            agoraMediaPlayerKit2.mute(true);
        } else {
            video2mute.setTag(false);
            video2mute.setText("Mute");
            agoraMediaPlayerKit2.mute(false);
        }
        boolean muteState = agoraMediaPlayerKit2.isMuted();
        setInfo2Text("Mute:" + muteState);
    }

    private void video2Duration() {
        long duration = agoraMediaPlayerKit2.getDuration();
        setInfo2Text("duration:" + duration);
    }

    private void video2GetStream() {
        int value = agoraMediaPlayerKit2.getPlayoutVolume();
        setInfo2Text("value:" + value);
    }

    private void video2GetStreamInfo() {
        int streamCount = agoraMediaPlayerKit2.getStreamCount();
        String streamInfos = null;
        for (int i = 0; i < streamCount; i++) {
            String streamInfo = agoraMediaPlayerKit2.getStreamInfo(i).toString();
            streamInfos = streamInfos + streamInfo + "\n";
        }
        setInfo2Text("streamInfos:" + streamInfos);
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void setInfo1Text(String msg) {
        video1Info.setText("info:" + msg);
    }

    private void setInfo2Text(String msg) {
        video2Info.setText("info:" + msg);
    }

    /**
     * ******************************************** view callback deal with******************************************
     */

    //surfaceview deal with
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        LogUtil.i("surfaceCreated :" + surfaceHolder);
        scrollView.requestLayout();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        LogUtil.i("surfaceChanged :" + surfaceHolder.getSurface() + " format:" + format);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        LogUtil.i("surfaceDestroyed :" + surfaceHolder);
    }

    //textureView deal with
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        LogUtil.i("onSurfaceTextureAvailable");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        LogUtil.i("onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        LogUtil.i("onSurfaceTextureDestroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        LogUtil.i("onSurfaceTextureUpdated");
    }


    /**
     * ******************************************** Agora*********************************************
     */

    private RtcEngine mRtcEngine;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            LogUtil.i("onFirstRemoteVideoDecoded");
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isAgoraStarted = true;
            LogUtil.i("onJoinChannelSuccess " + channel + " " + (uid & 0xFFFFFFFFL) + " " + isAgoraStarted);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            LogUtil.i("onUserOffline");
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) {
            LogUtil.i("onUserMuteVideo");
        }


        public void onVideoSizeChanged(int uid, int width, int height, int rotation) {
            LogUtil.i("onVideoSizeChanged");
        }


        public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
            isAgoraStarted = false;
            LogUtil.i("onLeaveChannel " + isAgoraStarted);
        }
    };

    private void setupAgoraEngineAndJoinChannel() {
        setupVideoProfile();
        joinChannel();

    }


    private void setupVideoProfile() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        mRtcEngine.enableVideo();
        //mRtcEngine.muteLocalAudioStream(true);
        //mRtcEngine.setExternalVideoSource(true,false,true);
//      mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false); // Earlier than 2.3.0
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_1280x720, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE));
    }

    private void joinChannel() {
        //mRtcEngine.startPreview();

        //mRtcEngine.setParameters("{\"rtc.log_filter\": 65535}");
        mRtcEngine.joinChannel(null, "yong135", "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you

    }

    private void leaveChannel() {
        //mRtcEngine.startPreview();
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel(); // if you do not specify the uid, we will generate the uid for you
        }

    }
}
