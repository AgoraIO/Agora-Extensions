package io.agora.demo.streaming.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import io.agora.base.SnapshotFrame;
import io.agora.demo.streaming.R;
import io.agora.demo.streaming.StreamingMode;
import io.agora.demo.streaming.activities.MainActivity;
import io.agora.demo.streaming.presenter.LiveStreamingPresenter;
import io.agora.demo.streaming.utils.MediaStoreUtil;
import io.agora.demo.streaming.utils.StatusBarUtil;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.streaming.SnapshotCallback;
import io.agora.streaming.StreamingEventHandler;

public class ScreenCaptureService extends Service {
    private static final String TAG = ScreenCaptureService.class.getSimpleName();
    private final static int NOTIFICATION_ID = 1234;
    public static boolean serviceIsLive = false;
    private static final int REQUEST_CODE = 100;
    private static final String ACTION_START = "start";
    private static final String ACTION_STOP = "stop";
    private static final String ACTION_SNAPSHOT = "snapshot";
    // notify channel id.
    private final String notificationChannelId = "notification_channel_rsk_01";

    private Intent mScreenIntent;

    private Handler mHandler;
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private int mCurrentStreamingMode = StreamingMode.NONE;
    private String mRoomName;

    private LiveStreamingPresenter mPresenter;
    private final IRtcEngineEventHandler mRtcEngineEventHandler = new MyRtcEngineEventHandler();
    private final StreamingEventHandler mStreamingEventHandler = new MyStreamingEventHandler();

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

        }

        @Override
        public void onUserOffline(final int uid, int reason) {

        }

        @Override
        public void onRtmpStreamingStateChanged(String url, int state, int errCode) {
            showToast("server streaming state: " + state + " err: " + errCode);
        }
    }

    public ScreenCaptureService() {
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action:" + action);

            switch (action) {
                case ACTION_START:
                    doActionStart();
                    break;
                case ACTION_STOP:
                    Log.w(TAG, "ACTION_STOP");

                    doActionStop();
                    break;
                case ACTION_SNAPSHOT:
                    Log.w(TAG, "ACTION_SNAPSHOT");
                    snapshot();
                    break;
            }
            StatusBarUtil.collapse(ScreenCaptureService.this);

        }
    };

    private void doActionStop() {
        stopLiveStreaming();
        ScreenCaptureService.this.stopSelf();
    }

    private void doActionStart() {
        Log.w(TAG, "ACTION_START");
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
            mPresenter.startClientStreaming();
            mCurrentStreamingMode = StreamingMode.CLIENT_STREAMING;
        }

        String needChangBtnText = null;
        if (mCurrentStreamingMode == StreamingMode.CLIENT_STREAMING) {
            needChangBtnText = getString(R.string.btn_screen_capture_start_rtc);
        } else if (mCurrentStreamingMode == StreamingMode.SERVER_STREAMING) {
            needChangBtnText = getString(R.string.btn_screen_capture_start_rtmp);
        }
        if (needChangBtnText != null) {
            String finalNeedChangBtnText = needChangBtnText;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNotification.contentView.setTextViewText(R.id.screen_capture_start_text, finalNeedChangBtnText);
                    mNotificationManager.notify(NOTIFICATION_ID, mNotification);
                }
            }, 500);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotification = createForegroundNotification();
        startForeground(NOTIFICATION_ID, mNotification);

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ACTION_START);
        mFilter.addAction(ACTION_STOP);
        mFilter.addAction(ACTION_SNAPSHOT);
        registerReceiver(broadcastReceiver, mFilter);

        mHandler = new Handler(getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");

        ScreenCaptureService.serviceIsLive = true;

        mScreenIntent = intent.getParcelableExtra("screen_intent");
        mRoomName = intent.getStringExtra("key_room_name");

        initLiveStreaming();
        doActionStart();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Create Service Notification
     */
    private Notification createForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Foreground Service Notification";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.setDescription("Channel description");
            // LED
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            // Vibration
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }

        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.screen_capture_notify_bar);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        builder.setContent(remoteViews);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("@string/app_name");
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.ic_launcher);

        Intent intentStart = new Intent(ACTION_START);
        PendingIntent pIntentPlay = PendingIntent.getBroadcast(this.getApplicationContext(),
                REQUEST_CODE, intentStart, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.screen_capture_start, pIntentPlay);

        Intent intentStop = new Intent(ACTION_STOP);
        PendingIntent pIntentStop = PendingIntent.getBroadcast(this.getApplicationContext(),
                REQUEST_CODE, intentStop, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.screen_capture_stop, pIntentStop);

        Intent intentSnapshot = new Intent(ACTION_SNAPSHOT);
        PendingIntent pIntentSnapshot = PendingIntent.getBroadcast(this.getApplicationContext(),
                REQUEST_CODE, intentSnapshot, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.screen_capture_snapshot, pIntentSnapshot);

        return builder.build();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }

        ScreenCaptureService.serviceIsLive = false;
        stopForeground(true);
        mNotification = null;

        Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //need Intent.FLAG_ACTIVITY_NEW_TASK

        mainIntent.putExtra("from", "ScreenCaptureService");
        mainIntent.putExtra("why", "StopSelf");
        getApplication().startActivity(mainIntent);

        super.onDestroy();
    }

    private Toast mToast = null;

    private void showToast(final String msg) {
        Log.i(TAG, msg);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(ScreenCaptureService.this, msg, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }

    private void initLiveStreaming() {
        Log.i(TAG, "initLiveStreaming");

        if (mPresenter == null) {
            mPresenter = LiveStreamingPresenter.getInstance();
        }

        mPresenter.setRtcEngineEventHandler(mRtcEngineEventHandler);
        mPresenter.setStreamingEventHandler(mStreamingEventHandler);
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        mPresenter.enableAudioRecording(true);
        mPresenter.startScreenCapture(mScreenIntent, dm.widthPixels, dm.heightPixels);
    }

    private void snapshot() {
        Log.i(TAG, "initLiveStreaming");

        if (mCurrentStreamingMode == StreamingMode.NONE) {
            return;
        }
        if (mPresenter == null) {
            mPresenter = LiveStreamingPresenter.getInstance();
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    // wait for collapse status bar
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mPresenter.snapshot(new SnapshotCallback() {
                    @Override
                    public void onSnapshot(SnapshotFrame snapshotFrame) {
                        Bitmap bmp = snapshotFrame.getBitmap();
                        String filename = "RSK_" + System.currentTimeMillis() + ".jpg";
                        new Thread(() -> {
                            boolean ret = MediaStoreUtil.saveBmp2Gallery(ScreenCaptureService.this, bmp, filename);
                            if (ret) {
                                showToast(getString(R.string.save_pic_success));
                            } else {
                                showToast(getString(R.string.save_pic_failed));
                            }
                            bmp.recycle();
                        }).start();
                    }
                });
            }
        }.start();
    }

    private void stopLiveStreaming() {
        Log.i(TAG, "stopLiveStreaming");

        if (mPresenter == null) {
            return;
        }

        if (mCurrentStreamingMode == StreamingMode.CLIENT_STREAMING) {
            mPresenter.stopClientStreaming();
        } else if (mCurrentStreamingMode == StreamingMode.SERVER_STREAMING) {
            mPresenter.stopServerStreamingAndLeaveAgoraChannel();
        } else if (mCurrentStreamingMode == StreamingMode.SIMUL_STREAMING) {
            mPresenter.stopClientStreaming();
            mPresenter.stopServerStreamingAndLeaveAgoraChannel();
        }
        mCurrentStreamingMode = StreamingMode.NONE;

        mPresenter.enableAudioRecording(false);
        mPresenter.stopScreenCapture();

        mPresenter.setRtcEngineEventHandler(null);
        mPresenter.setStreamingEventHandler(null);
        LiveStreamingPresenter.destroyEngineAndKit();
    }
}