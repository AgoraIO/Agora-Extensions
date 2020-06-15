package io.agora.mediaplayer;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import io.agora.mediaplayer.data.MediaStreamInfo;
import io.agora.mediaplayer.internal.AgoraMediaPlayer;
import io.agora.utils.LogUtil;

/**
 * Created by lixiaochen on 2020/5/25.
 */

public class PlayerCppFragment extends Fragment {
    private Button videoCppInit;
    private Button videoCppOpen;
    private Button videoCppPlay;
    private Button videoCppPause;
    private Button videoCppStop;
    private Button videoCppMute;
    private Button videoCppDuration;
    private Button videoCppGetStream;
    private Button videoCppGetStreamInfo;
    private FrameLayout videoCppContainer;
    private SeekBar videoCppSeek;
    private SeekBar videoCppVolume;

    static {
        System.loadLibrary("AgoraMediaPlayer");
        System.loadLibrary("media_player_test");
        //
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        LogUtil.i("TJY onCreateView");
        final View rootView = inflater.inflate(R.layout.fragment_main_cpp, container, false);
        initUI(rootView);
        return rootView;
    }


    private void initUI(View rootView) {
        videoCppInit = (Button) rootView.findViewById(R.id.bt_init_cpp);
        videoCppOpen = (Button) rootView.findViewById(R.id.bt_load_cpp);
        videoCppPlay = (Button) rootView.findViewById(R.id.bt_play_cpp);
        videoCppPause = (Button) rootView.findViewById(R.id.bt_pause_cpp);
        videoCppStop = (Button) rootView.findViewById(R.id.bt_stop_cpp);
        videoCppMute = (Button) rootView.findViewById(R.id.bt_mute_cpp);
        videoCppDuration = (Button) rootView.findViewById(R.id.bt_duration_cpp);
        videoCppGetStream = (Button) rootView.findViewById(R.id.bt_streams_cpp);
        videoCppGetStreamInfo = (Button) rootView.findViewById(R.id.bt_info_cpp);
        videoCppContainer =(FrameLayout) rootView.findViewById(R.id.player_view_cpp);
        videoCppSeek = (SeekBar) rootView.findViewById(R.id.sb_seek_cpp);
        videoCppVolume = (SeekBar) rootView.findViewById(R.id.sb_volume_cpp);


        videoCppInit.setOnClickListener(mOnClickListener);
        videoCppOpen.setOnClickListener(mOnClickListener);
        videoCppPlay.setOnClickListener(mOnClickListener);
        videoCppPause.setOnClickListener(mOnClickListener);
        videoCppStop.setOnClickListener(mOnClickListener);
        videoCppMute.setOnClickListener(mOnClickListener);
        videoCppDuration.setOnClickListener(mOnClickListener);
        videoCppGetStream.setOnClickListener(mOnClickListener);
        videoCppGetStreamInfo.setOnClickListener(mOnClickListener);


        videoCppSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekMediaPlayerCpp(seekBar.getProgress());
            }
        });


        videoCppVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                adjustPlayoutVolumeMediaPlayerCpp(seekBar.getProgress());
            }
        });
    }
    AgoraMediaPlayerKit mediaplayer = null;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.bt_init_cpp:
                    LogUtil.i("TJY bt_init_cpp 0");
                    //mediaplayer = new AgoraMediaPlayerKit(getActivity().getApplication());
                    int ret = AgoraMediaPlayer.nativeSetupAvJniEnv();
                    LogUtil.i("TJY nativeSetupJavaVm "+ret);
                    initMediaPlayerCpp(getActivity().getApplication());
                    SurfaceView videoView1 = new SurfaceView(getActivity());
                    setViewMediaPlayerCpp(videoView1);
                    videoCppContainer.addView(videoView1);
                    LogUtil.i("TJY bt_init_cpp 1");
                    break;
                case R.id.bt_load_cpp:
                    LogUtil.i("TJY bt_load_cpp ");
                    //mediaplayer.open("http://ting6.yymp3.net:82/new27/sunlu7/4.mp3",0);
                    openMediaPlayerCpp("http://114.236.93.153:8080/download/video/wudao1.flv",0);
                    break;
                case R.id.bt_play_cpp:
                    LogUtil.i("TJY bt_play_cpp");
                    playMediaPlayerCpp();
                    //mediaplayer.play();
                    break;
                case R.id.bt_pause_cpp:
                    LogUtil.i("TJY bt_pause_cpp");
                    pauseMediaPlayerCpp();
                    break;
                case R.id.bt_stop_cpp:
                    LogUtil.i("TJY bt_play_cpp");
                    stopMediaPlayerCpp();
                    break;
                case R.id.bt_mute_cpp:
                    LogUtil.i("TJY bt_mute_cpp");
                    if(view.getTag() == null || (boolean) (view.getTag()) == false) {
                        view.setTag(true);
                        ((Button)view).setText("不静音");
                        muteMediaPlayerCpp(true);
                    } else {
                        view.setTag(false);
                        ((Button)view).setText("静音");
                        muteMediaPlayerCpp(false);
                    }
                    break;
                case R.id.bt_duration_cpp:
                    LogUtil.i("TJY bt_play_cpp");
                    durationMediaPlayerCpp();
                    break;
                case R.id.bt_streams_cpp:
                    LogUtil.i("TJY bt_streams_cpp");
                    streamsMediaPlayerCpp();
                    break;
                case R.id.bt_info_cpp:
                    LogUtil.i("TJY bt_info_cpp");
                    infosMediaPlayerCpp();
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i("onResume:");
    }

    @Override
    public void onStop() {
        super.onStop();
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

    private native void initMediaPlayerCpp(Object context);

    private native void openMediaPlayerCpp(String src, long startPos);

    private native void setViewMediaPlayerCpp(SurfaceView view);

    private native void playMediaPlayerCpp();

    private native void pauseMediaPlayerCpp();

    private native void stopMediaPlayerCpp();

    private native void muteMediaPlayerCpp(boolean muted);

    private native void durationMediaPlayerCpp();

    private native void streamsMediaPlayerCpp();

    private native void infosMediaPlayerCpp();

    private native void setViewMediaPlayerCpp();

    private native void adjustPlayoutVolumeMediaPlayerCpp(int volume);

    private native void seekMediaPlayerCpp(long position);

}
