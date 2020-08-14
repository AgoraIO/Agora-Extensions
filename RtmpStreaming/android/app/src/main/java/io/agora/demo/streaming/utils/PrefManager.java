package io.agora.demo.streaming.utils;

import android.content.Context;
import android.content.SharedPreferences;

import io.agora.demo.streaming.MyApplication;
import io.agora.demo.streaming.R;
import io.agora.rtc.Constants;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.streaming.AudioStreamConfiguration;
import io.agora.streaming.StreamingKit;
import io.agora.streaming.VideoStreamConfiguration;


public class PrefManager {
    public static final String PREF_FILE_NAME = "io.agora.demo.streaming";

    public static final String PREF_RTMP_URL = "pref_rtmp_url";
    public static final String PREF_VIDEO_DIMENSIONS_INDEX = "pref_video_dimensions_index";
    public static final String PREF_VIDEO_FRAMERATE_INDEX = "pref_video_framerate_index";
    public static final String PREF_VIDEO_BITRATE_INDEX = "pref_video_bitrate_index";
    public static final String PREF_VIDEO_ORIENTATION_MODE_INDEX = "pref_video_orientation_mode_index";
    public static final String PREF_AUDIO_SAMPLE_RATE_INDEX = "pref_audio_sample_rate_index";
    public static final String PREF_AUDIO_TYPE_INDEX = "pref_audio_type_index";
    public static final String PREF_AUDIO_BITRATE_INDEX = "pref_audio_bitrate_index";
    public static final String PREF_LOG_PATH = "pref_log_path";
    public static final String PREF_LOG_FILTER_INDEX = "pref_log_filter_index";
    public static final String PREF_LOG_FILE_SIZE = "pref_log_file_size";
    public static final String PREF_MIRROR_LOCAL = "pref_mirror_local";
    public static final String PREF_MIRROR_REMOTE = "pref_mirror_remote";

    /************************************** video settings **************************************/
    // video dimensions
    public static final VideoEncoderConfiguration.VideoDimensions[] VIDEO_DIMENSIONS =
        new VideoEncoderConfiguration.VideoDimensions[]{
            VideoEncoderConfiguration.VD_320x240,
            VideoEncoderConfiguration.VD_480x360,
            VideoEncoderConfiguration.VD_640x360,
            VideoEncoderConfiguration.VD_640x480,
            new VideoEncoderConfiguration.VideoDimensions(960, 540),
            VideoEncoderConfiguration.VD_1280x720
    };
    private static final int DEFAULT_VIDEO_DIMENSIONS_INDEX = 2;

    // video frame rates
    public static final VideoEncoderConfiguration.FRAME_RATE[] VIDEO_FRAMERATES =
        new VideoEncoderConfiguration.FRAME_RATE[] {
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_7,
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
    };
    private static final int DEFAULT_VIDEO_FRAMERATE_INDEX = 1;

    // video bitrates
    public static final int[] VIDEO_BITRATES = new int[] {
        VideoEncoderConfiguration.STANDARD_BITRATE, 400, 640, 800, 1000, 2260, 3420,
    };
    private static final int DEFAULT_VIDEO_BITRATE_INDEX = 0;

    // video orientation modes
    public static final VideoStreamConfiguration.ORIENTATION_MODE[] VIDEO_ORIENTATION_MODES =
        new VideoStreamConfiguration.ORIENTATION_MODE[] {
            VideoStreamConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT,
            VideoStreamConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE,
        };
    public static final String[] VIDEO_ORIENTATION_MODE_STRINGS = new String[] {
        "Portrait", "Landscape"
    };
    private static final int DEFAULT_VIDEO_ORIENTATION_MODE_INDEX = 0;

    // video mirror modes
    public static final int[] VIDEO_MIRROR_MODES = new int[] {
        Constants.VIDEO_MIRROR_MODE_AUTO,
        Constants.VIDEO_MIRROR_MODE_ENABLED,
        Constants.VIDEO_MIRROR_MODE_DISABLED,
    };
    public static final String[] VIDEO_MIRROR_MODE_STRINGS = new String[] {
        "Auto", "Enabled", "Disabled"
    };
    private static final int DEFAULT_MIRROR_MODE_INDEX_LOCAL = 0;
    private static final int DEFAULT_MIRROR_MODE_INDEX_REMOTE = 0;

    /************************************** audio settings **************************************/
    // audio sample rates
    @AudioStreamConfiguration.SoundRate
    public static final int[] AUDIO_SAMPLE_RATES = new int[] {
        AudioStreamConfiguration.SoundRate.SAMPLE_RATE_11000,
        AudioStreamConfiguration.SoundRate.SAMPLE_RATE_22000,
        AudioStreamConfiguration.SoundRate.SAMPLE_RATE_44100,
    };
    public static final String[] AUDIO_SAMPLE_RATE_STRINGS = new String[] {
        "11KHz", "22KHz", "44KHz"
    };
    private static final int DEFAULT_AUDIO_SAMPLE_RATE_INDEX = 2;

    // audio types
    @AudioStreamConfiguration.SoundType
    public static final int[] AUDIO_TYPES = new int[] {
        AudioStreamConfiguration.SoundType.TYPE_MONO,
        AudioStreamConfiguration.SoundType.TYPE_STEREO,
    };
    public static final String[] AUDIO_TYPE_STRINGS = new String[] {
        "Mono", "Stereo"
    };
    private static final int DEFAULT_AUDIO_TYPE_INDEX = 0;

    // audio bitrates
    public static final int[] AUDIO_BITRATES = new int[] {
        0, 12, 18, 36, 48, 56, 92, 112, 192
    };
    private static final int DEFAULT_AUDIO_BITRATE_INDEX = 4;

    /************************************** log settings **************************************/
    private static final int DEFAULT_LOG_FILE_SIZE = 512; // KB

    // log filter
    public static final int[] LOG_FILTERS = new int[] {
        StreamingKit.LogFilter.LOG_FILTER_OFF,
        StreamingKit.LogFilter.LOG_FILTER_DEBUG,
        StreamingKit.LogFilter.LOG_FILTER_INFO,
        StreamingKit.LogFilter.LOG_FILTER_WARN,
        StreamingKit.LogFilter.LOG_FILTER_ERROR,
        StreamingKit.LogFilter.LOG_FILTER_CRITICAL,
    };
    public static final String[] LOG_FILTER_STRINGS = new String[] {
        "OFF", "DEBUG", "INFO", "WARN", "ERROR", "CRITICAL"
    };
    private static final int DEFAULT_LOG_FILTER_INDEX = 1;

    private static SharedPreferences mPref;

    public static synchronized SharedPreferences getPreferences() {
        if (mPref == null) {
            mPref = MyApplication.getAppContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
        return mPref;
    }

    public static String getAppID() {
        return MyApplication.getAppContext().getString(R.string.private_app_id);
    }

    public static String getRtmpUrl() {
        return getPreferences().getString(PREF_RTMP_URL, MyApplication.getAppContext().getString(R.string.test_rtmp_url));
    }

    public static int getVideoDimensionsIndex() {
        return getPreferences().getInt(PREF_VIDEO_DIMENSIONS_INDEX, DEFAULT_VIDEO_DIMENSIONS_INDEX);
    }

    public static VideoEncoderConfiguration.VideoDimensions getVideoDimensions() {
     return VIDEO_DIMENSIONS[getVideoDimensionsIndex()];
    }

    public static int getVideoFramerateIndex() {
        return getPreferences().getInt(PREF_VIDEO_FRAMERATE_INDEX, DEFAULT_VIDEO_FRAMERATE_INDEX);
    }

    public static int getVideoFramerate() {
        return VIDEO_FRAMERATES[getVideoFramerateIndex()].getValue();
    }

    public static int getVideoBitrateIndex() {
        return getPreferences().getInt(PREF_VIDEO_BITRATE_INDEX, DEFAULT_VIDEO_BITRATE_INDEX);
    }

    public static int getVideoBitrate() {
        return VIDEO_BITRATES[getVideoBitrateIndex()];
    }

    public static int getVideoOrientationModeIndex() {
        return getPreferences().getInt(PREF_VIDEO_ORIENTATION_MODE_INDEX, DEFAULT_VIDEO_ORIENTATION_MODE_INDEX);
    }

    public static VideoStreamConfiguration.ORIENTATION_MODE getVideoOrientationMode() {
        return VIDEO_ORIENTATION_MODES[getVideoOrientationModeIndex()];
    }

    public static int getMirrorModeIndexLocal() {
        return getPreferences().getInt(PREF_MIRROR_LOCAL, DEFAULT_MIRROR_MODE_INDEX_LOCAL);
    }

    public static int getMirrorModeLocal() {
        return VIDEO_MIRROR_MODES[getMirrorModeIndexLocal()];
    }

    public static int getMirrorModeIndexRemote() {
        return getPreferences().getInt(PREF_MIRROR_REMOTE, DEFAULT_MIRROR_MODE_INDEX_REMOTE);
    }

    public static int getMirrorMoteRemote() {
        return VIDEO_MIRROR_MODES[getMirrorModeIndexRemote()];
    }

    public static int getAudioSampleRateIndex() {
        return getPreferences().getInt(PREF_AUDIO_SAMPLE_RATE_INDEX, DEFAULT_AUDIO_SAMPLE_RATE_INDEX);
    }

    public static int getAudioSampleRate() {
        return AUDIO_SAMPLE_RATES[getAudioSampleRateIndex()];
    }

    public static int getAudioTypeIndex() {
        return getPreferences().getInt(PREF_AUDIO_TYPE_INDEX, DEFAULT_AUDIO_TYPE_INDEX);
    }

    public static int getAudioType() {
        return AUDIO_TYPES[getAudioTypeIndex()];
    }

    public static int getAudioBitrateIndex() {
        return getPreferences().getInt(PREF_AUDIO_BITRATE_INDEX, DEFAULT_AUDIO_BITRATE_INDEX);
    }

    public static int getAudioBitrate() {
        return AUDIO_BITRATES[getAudioBitrateIndex()];
    }

    public static String getDefaultLogPath() {
        return FileUtil.getLogFilePath(MyApplication.getAppContext(), "streaming-kit.log");
    }

    public static String getLogPath() {
        return getPreferences().getString(PREF_LOG_PATH, getDefaultLogPath());
    }

    public static int getLogFilterIndex() {
        return getPreferences().getInt(PREF_LOG_FILTER_INDEX, DEFAULT_LOG_FILTER_INDEX);
    }

    public static int getLogFilter() {
        return LOG_FILTERS[getLogFilterIndex()];
    }

    public static int getLogFileSize() {
        return getPreferences().getInt(PREF_LOG_FILE_SIZE, DEFAULT_LOG_FILE_SIZE);
    }
}
