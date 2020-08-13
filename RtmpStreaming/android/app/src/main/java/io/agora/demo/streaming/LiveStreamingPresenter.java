package io.agora.demo.streaming;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import io.agora.demo.streaming.utils.PrefManager;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.streaming.StreamingEventHandler;
import io.agora.streaming.VideoFilter;

public class LiveStreamingPresenter {
    private static final String TAG = LiveStreamingPresenter.class.getSimpleName();
    private static final LiveStreamingPresenter mPresenter = new LiveStreamingPresenter();

    public static LiveStreamingPresenter getInstance() {
        mPresenter.createIfNeeded(DemoApplication.getAppContext());
        return mPresenter;
    }

    private Context mContext;
    private Handler mUiHandler;
    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    private HandlerThread mEventThread;
    private Handler mEventHandler;

    private RtcEngineWrapper mRtcEngineWrapper;
    private final IRtcEngineEventHandler mRtcEngineEventHandlerWrapper =
        new RtcEngineEventHandlerWrapper();
    private IRtcEngineEventHandler mRtcEngineEventHandlerReal;

    private StreamingKitWrapper mStreamingKitWrapper;
    private final StreamingEventHandler mStreamingEventHandlerWrapper =
        new StreamingEventHandlerWrapper();
    private StreamingEventHandler mStreamingEventHandlerReal;

    // TODO(Haonong Yu): 2020/8/10 thread safety for belows?
    private List<Integer> mUidList = new ArrayList<>();
    private boolean mPendingServerStreaming = false;
    private boolean mIsServerStreaming = false;

    private LiveStreamingPresenter() {
        mContext = DemoApplication.getAppContext();
        mUiHandler = new Handler(Looper.getMainLooper());

        mWorkThread = new HandlerThread("StreamingWorker");
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper());

        mEventThread = new HandlerThread("StreamingEventHandler");
        mEventThread.start();
        mEventHandler = new Handler(mEventThread.getLooper());
    }

    private void createIfNeeded(Context appContext) {
        Log.i(TAG, "create");
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mStreamingKitWrapper == null) {
                    mStreamingKitWrapper = new StreamingKitWrapper(mContext);
                    mStreamingKitWrapper.init(mStreamingEventHandlerWrapper);
                }
                if (mRtcEngineWrapper == null) {
                    mRtcEngineWrapper = new RtcEngineWrapper(mContext);
                    mRtcEngineWrapper.create(mRtcEngineEventHandlerWrapper);
                }
            }
        });
    }

    private void destroy() {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRtcEngineWrapper != null) {
                    mRtcEngineWrapper.destroy();
                    mRtcEngineWrapper = null;
                }
                if (mStreamingKitWrapper != null) {
                    mStreamingKitWrapper.destroy();
                    mStreamingKitWrapper = null;
                }
            }
        });
    }

    public static void destroyEngineAndKit() {
        mPresenter.destroy();
    }

    public void setStreamingEventHandler(final StreamingEventHandler streamingEventHandler) {
        mEventHandler.post(new Runnable() {
            @Override
            public void run() {
                mStreamingEventHandlerReal = streamingEventHandler;
            }
        });
    }

    public void setRtcEngineEventHandler(final IRtcEngineEventHandler rtcEngineEventHandler) {
        mEventHandler.post(new Runnable() {
            @Override
            public void run() {
                mRtcEngineEventHandlerReal = rtcEngineEventHandler;
            }
        });
    }

    public void setPreview(final SurfaceView view) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mStreamingKitWrapper == null) {
                    return;
                }
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // this has to be called in UI thread
                        mStreamingKitWrapper.setPreview(view);
                    }
                });
            }
        });
    }

    public void enableAudioRecordingAndVideoCapturing(final boolean enable) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                mStreamingKitWrapper.enableAudioRecording(enable);
                mStreamingKitWrapper.enableVideoCapturing(enable);
            }
        });
    }

    public void startClientStreaming() {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                mStreamingKitWrapper.startStreaming();
            }
        });
    }

    public void stopClientStreaming() {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                mStreamingKitWrapper.stopStreaming();
            }
        });
    }

    public int switchCamera() {
        return invokeOnWorkerThread(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return mStreamingKitWrapper.switchCamera();
            }
        });
    }

    public boolean isCameraFacingFront() {
        return invokeOnWorkerThread(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return mStreamingKitWrapper.isCameraFacingFront() ? 1 : 0;
            }
        }) == 1;
    }

    public boolean addVideoFilter(final VideoFilter videoFilter) {
        return invokeOnWorkerThread(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return mStreamingKitWrapper.addVideoFilter(videoFilter) ? 1 : 0;
            }
        }) == 1;
    }

    public boolean removeVideoFilter(final VideoFilter videoFilter) {
        return invokeOnWorkerThread(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return mStreamingKitWrapper.removeVideoFilter(videoFilter) ? 1 : 0;
            }
        }) == 1;
    }

    public void setupRemoteVideo(final VideoCanvas videoCanvas) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRtcEngineWrapper == null) {
                    return;
                }
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // this has to be called in UI thread
                        mRtcEngineWrapper.setupRemoteVideo(videoCanvas);
                    }
                });
            }
        });
    }

    public void removeRemoteVideo(final int uid) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRtcEngineWrapper == null) {
                    return;
                }
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // this has to be called in UI thread
                        mRtcEngineWrapper.setupRemoteVideo(
                            new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid,
                            PrefManager.getMirrorRemoteMode(mContext)));
                    }
                });
            }
        });
    }

    public int setRtcLiveTranscoding(final List<Integer> uidList, final int width, final int height,
        final int videoBitrate, final int videoFramerate) {
        return invokeOnWorkerThread(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return mRtcEngineWrapper.setLiveTranscoding(uidList, width, height, videoBitrate,
                    videoFramerate);
            }
        });
    }

    public int addRtcPublishStreamUrl() {
        return invokeOnWorkerThread(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return mRtcEngineWrapper.addPublishStreamUrl();
            }
        });
    }

    public void muteLocalAudioStream(final boolean muted) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                mRtcEngineWrapper.muteLocalAudioStream(muted);
                mStreamingKitWrapper.muteAudioStream(muted);
            }
        });
    }

    public void muteLocalVideoStream(final boolean muted) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                mRtcEngineWrapper.muteLocalVideoStream(muted);
                mStreamingKitWrapper.muteVideoStream(muted);
            }
        });
    }

    public void joinAgoraChannelAndStartServerStreaming(final String channelName) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                mPendingServerStreaming = true;
                mRtcEngineWrapper.setExternalAudioSource(true);
                mStreamingKitWrapper.registerAudioFrameObserver(mRtcEngineWrapper);
                mRtcEngineWrapper.setExternalVideoSource(true);
                mStreamingKitWrapper.registerVideoFrameObserver(mRtcEngineWrapper);
                mRtcEngineWrapper.joinChannel(channelName);
            }
        });
    }

    public void stopServerStreamingAndLeaveAgoraChannel() {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                mPendingServerStreaming = false;
                if (mIsServerStreaming) {
                    mRtcEngineWrapper.removePublishStreamUrl();
                    mIsServerStreaming = false;
                }
                mRtcEngineWrapper.leaveChannel(/**/);
                mStreamingKitWrapper.unregisterVideoFrameObserver(mRtcEngineWrapper);
                mStreamingKitWrapper.unregisterAudioFrameObserver(mRtcEngineWrapper);
            }
        });
    }

    private int invokeOnWorkerThread(Callable<Integer> callable) {
        return invoke(callable, mWorkHandler);
    }

    private int invoke(final Callable<Integer> callable, Handler handler) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger ret = new AtomicInteger(-1);
        handler.post(new Runnable() {
            @Override
            public void run() {
                int v = -1;
                try {
                    v = callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ret.set(v);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret.get();
    }

    private class RtcEngineEventHandlerWrapper extends IRtcEngineEventHandler {

        @Override
        public void onJoinChannelSuccess(final String channel, final int uid, final int elapsed) {
            Log.i(TAG, "on join channel success, channel: " + channel + " uid: " + uid);
            mEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    mUidList.add(uid);

                    if (mPendingServerStreaming) {
                        mPendingServerStreaming = false;
                        VideoEncoderConfiguration.VideoDimensions videoDimension =
                            PrefManager.VIDEO_DIMENSIONS[PrefManager.getVideoDimensionsIndex(mContext)];
                        int videoBitrate =
                            PrefManager.VIDEO_BITRATES[PrefManager.getVideoBitrateIndex(mContext)];
                        int videoFramerate =
                            PrefManager.VIDEO_FRAMERATES[PrefManager.getVideoFramerateIndex(mContext)].getValue();
                        mPresenter.setRtcLiveTranscoding(mUidList, videoDimension.width,
                            videoDimension.height, videoBitrate, videoFramerate);
                        int ret = mPresenter.addRtcPublishStreamUrl();
                        if (ret != 0) {
                            // showToast("addPublishStreamUrl failed: " + ret);
                        } else {
                            mIsServerStreaming = true;
                        }
                    }

                    if (mRtcEngineEventHandlerReal != null) {
                        mRtcEngineEventHandlerReal.onJoinChannelSuccess(channel, uid, elapsed);
                    }
                }
            });
        }

        @Override
        public void onLeaveChannel(final IRtcEngineEventHandler.RtcStats stats) {
            Log.i(TAG, "on leave channel");
            mEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRtcEngineEventHandlerReal != null) {
                        mRtcEngineEventHandlerReal.onLeaveChannel(stats);
                    }
                }
            });
        }

        @Override
        public void onUserJoined(final int uid, final int elapsed) {
            Log.i(TAG, "on user joined, uid: " + uid);
            mEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    mUidList.add(uid);
                    if (mRtcEngineEventHandlerReal != null) {
                        mRtcEngineEventHandlerReal.onUserJoined(uid, elapsed);
                    }
                }
            });
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, final int width, final int height,
            final int elapsed) {
            Log.i(TAG, "on first remote video decoded, uid: " + uid);
            mEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRtcEngineEventHandlerReal != null) {
                        mRtcEngineEventHandlerReal.onFirstRemoteVideoDecoded(uid, width, height,
                            elapsed);
                    }
                }
            });
        }

        @Override
        public void onUserOffline(final int uid, final int reason) {
            Log.i(TAG, "on user offline, uid: " + uid);
            mEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    mUidList.remove(Integer.valueOf(uid));
                    if (mRtcEngineEventHandlerReal != null) {
                        mRtcEngineEventHandlerReal.onUserOffline(uid, reason);
                    }
                }
            });
        }

        @Override
        public void onRtmpStreamingStateChanged(final String url, final int state, final int errCode) {
            Log.i(TAG, "on rtmp streaming state changed, state: " + state);
            mEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRtcEngineEventHandlerReal != null) {
                        mRtcEngineEventHandlerReal.onRtmpStreamingStateChanged(url, state, errCode);
                    }
                }
            });
        }
    }

    private class StreamingEventHandlerWrapper extends StreamingEventHandler {

        @Override
        public void onStartStreamingSuccess() {
            Log.i(TAG, "on start streaming success");
            mEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mStreamingEventHandlerReal != null) {
                        mStreamingEventHandlerReal.onStartStreamingSuccess();
                    }
                }
            });
        }

        @Override
        public void onStartStreamingFailure(final int err, final String msg) {
            Log.i(TAG, "on start streaming failure: " + err + ", " + msg);
            mEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mStreamingEventHandlerReal != null) {
                        mStreamingEventHandlerReal.onStartStreamingFailure(err, msg);
                    }
                }
            });
        }

        @Override
        public void onMediaStreamingError(final int err, final String msg) {
            Log.i(TAG, "on media streaming error: " + err + ", " + msg);
            mEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mStreamingEventHandlerReal != null) {
                        mStreamingEventHandlerReal.onMediaStreamingError(err, msg);
                    }
                }
            });
        }

        @Override
        public void onStreamingConnectionStateChanged(final int state) {
            Log.i(TAG, "on streaming connection state changes to: " + state);
            mEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mStreamingEventHandlerReal != null) {
                        mStreamingEventHandlerReal.onStreamingConnectionStateChanged(state);
                    }
                }
            });
        }
    }
}
