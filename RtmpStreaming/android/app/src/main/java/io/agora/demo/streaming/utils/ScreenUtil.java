package io.agora.demo.streaming.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtil {
    private static DisplayMetrics getMetrics(Context context){
        return context.getResources().getDisplayMetrics();
    }

    /**
     * get screen width
     * @param context
     * @return screen width
     */
    public static int getScreenWidth(Context context){
        return getMetrics(context).widthPixels;
    }

    /**
     * get screen height
     * @param context
     * @return screen height
     */
    public static int getScreenHeight(Context context){
        return getMetrics(context).heightPixels;
    }
}
