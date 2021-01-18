package io.agora.demo.streaming.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Method;


public class StatusBarUtil {
    /**
     * collapse Notification bar
     *
     */
    public static void collapse(Context context) {
        @SuppressLint("WrongConstant") Object service = context.getSystemService("statusbar");
        if (null == service)
            return;
        try {
            Class<?> clazz = Class.forName("android.app.StatusBarManager");
            int sdkVersion = android.os.Build.VERSION.SDK_INT;
            Method collapse = null;
            if (sdkVersion <= 16) {
                collapse = clazz.getMethod("collapse");
            } else {
                collapse = clazz.getMethod("collapsePanels");
            }
            collapse.setAccessible(true);
            collapse.invoke(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * expand Notification bar
     *
     */
    private static void _expandNotification(Context context, String level) {
        @SuppressLint("WrongConstant") Object service = context.getSystemService("statusbar");
        if (null == service)
            return;
        try {
            Class<?> clazz = Class.forName("android.app.StatusBarManager");
            int sdkVersion = android.os.Build.VERSION.SDK_INT;
            Method expand = null;
            if (sdkVersion <= 16) {
                expand = clazz.getDeclaredMethod("expand");
            } else {
                expand = clazz.getDeclaredMethod(level);
            }
            expand.setAccessible(true);
            expand.invoke(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Normally expand Notification bar
     *
     * @param context
     */
    public static void expandNotification(Context context) {
        _expandNotification(context, "expandNotificationsPanel");
    }

    /**
     * Fully expand Notification bar
     *
     * @param context
     */
    public static void expandNotificationFull(Context context) {
        _expandNotification(context, "expandSettingsPanel");

        @SuppressLint("WrongConstant") Object service = context.getSystemService("statusbar");
    }
}
