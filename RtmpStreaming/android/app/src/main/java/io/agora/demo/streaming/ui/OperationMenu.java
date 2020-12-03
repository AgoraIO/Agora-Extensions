package io.agora.demo.streaming.ui;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.PopupMenu;

import java.util.HashMap;
import java.util.Map;

import io.agora.demo.streaming.presenter.LiveStreamingPresenter;
import io.agora.demo.streaming.R;
import io.agora.demo.streaming.StreamingMode;
import io.agora.demo.streaming.activities.LiveActivity;

public class OperationMenu implements PopupMenu.OnMenuItemClickListener , PopupMenu.OnDismissListener{
    private static final String TAG = OperationMenu.class.getSimpleName();
    //screen share
    private MediaProjectionManager mpm;
    private Intent mediaProjectionIntent;
    private DisplayMetrics dm;
    private LiveActivity activity;
    private LiveStreamingPresenter mPresent;
    private Map<String, Boolean> clickMap = new HashMap<>();
    private boolean isStopCapture = true;

    public static int PROJECTION_REQ_CODE = 1;

    private static final String SCREEN_SHARE = "SCREEN_SAHARE";

    public OperationMenu(LiveActivity activity){
        this.activity = activity;
        clickMap.put(SCREEN_SHARE, false);
    }

    private void initScreenCapture(){
        if(mpm == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mpm = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            }
        }
        if(mediaProjectionIntent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaProjectionIntent = mpm.createScreenCaptureIntent();
            }
        }
        boolean screenShareState = clickMap.get(SCREEN_SHARE);
        if(screenShareState) {
            activity.startActivityForResult(mediaProjectionIntent, PROJECTION_REQ_CODE);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.screen_share:
                if(activity.getCurrentStreamingMode() != StreamingMode.NONE){
                    break;
                }
                getLivePresent();
                changeScreenShareState();
                initScreenCapture();
                displayScreenShareTitle(item);
                stopScreenCapture();
                break;
            case R.id.screen_ration:
                if(activity.getCurrentStreamingMode() != StreamingMode.NONE){
//                    item.setEnabled(false);
                    break;
                }
                activity.onSwitchResolution(activity.findViewById(R.id.live_room_top_layout), item);
                break;
            case R.id.screen_snapshot:
                activity.onSnapshot(activity.findViewById(R.id.live_room_top_layout), item);
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    public void onDismiss(PopupMenu menu) {

    }

    public void startScreenCapture(Intent intent){
        boolean screenShareState = clickMap.get(SCREEN_SHARE);

        if(!screenShareState){
            return;
        }
        if(dm == null) {
            dm = new DisplayMetrics();
        }
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mPresent.muteLocalVideoStream(true);
        mPresent.enableVideoCapturing(false);
        isStopCapture = true;
        switchBetwwenClientStreamingAndAgoraChnnel(intent);
        Log.d(TAG, "startScreenCapture");
    }

    private void stopScreenCapture(){
        boolean screenShareState = clickMap.get(SCREEN_SHARE);
        if(!screenShareState) {
            mpm = null;
            mediaProjectionIntent = null;
            isStopCapture = false;
            mPresent.muteLocalVideoStream(true);
            mPresent.stopScreenCapture();
            switchBetwwenClientStreamingAndAgoraChnnel(null);
            Log.d(TAG, "stopScreenCapture");
        }
    }

    private void getLivePresent(){
        if(mPresent == null){
            mPresent = activity.getLiveStreamingInstance();
        }
    }

    private void changeScreenShareState(){
        boolean screenShareState = clickMap.get(SCREEN_SHARE);
        screenShareState = !screenShareState;
        clickMap.put(SCREEN_SHARE, screenShareState);
    }

    private void displayScreenShareTitle(MenuItem item){
        boolean screenShareState = clickMap.get(SCREEN_SHARE);
        if(screenShareState){
            item.setTitle(R.string.stop_screen_share_title);
        }else{
            item.setTitle(R.string.switch_screen_share_title);
        }
    }

    private void switchBetwwenClientStreamingAndAgoraChnnel(Intent intent){
        switch (activity.getCurrentStreamingMode()){
            case StreamingMode.NONE:
                if(isStopCapture) {
                    mPresent.startScreenCapture(intent, dm.widthPixels, dm.heightPixels);
                    mPresent.muteLocalVideoStream(false);
                }else{
                    mPresent.enableVideoCapturing(true);
                    mPresent.muteLocalVideoStream(false);
                }
                break;
            default:
                break;
        }
    }
}
