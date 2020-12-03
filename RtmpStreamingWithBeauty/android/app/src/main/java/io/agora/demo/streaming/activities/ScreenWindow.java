package io.agora.demo.streaming.activities;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

class ScreenWindow{
    private static String TAG = "ScreenWindow";

    private LiveActivity activity;
    private WindowManager windowManager;
    private DisplayMetrics dm = new DisplayMetrics();
    private int screenWidth;
    private int screenHeight;
    private boolean autoFocus;

    public ScreenWindow(LiveActivity activity){
        this.activity = activity;
    }

    private void getScreenWidthAndHeight(){
        windowManager = activity.getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        Log.d(TAG, "screen width: " + String.valueOf(screenWidth));
        Log.d(TAG, "screen height: " + String.valueOf(screenHeight));
    }

    public int getScreenWidth(){
        getScreenWidthAndHeight();
        return screenWidth;
    }

    public int getScreenHeight(){
        getScreenWidthAndHeight();
        return screenHeight;
    }

    public Context getLiveActivityContext(){
        if(activity == null){
            return null;
        }

        return activity.getApplicationContext();
    }

    public void setAutoFocusState(boolean enable){
        autoFocus = enable;
        activity.getLiveStreamingInstance().autoFaceFocus(enable);
    }

    public boolean getAutoFocusState(){
        return autoFocus;
    }

    public LiveActivity getActivity(){
        return activity;
    }
}
