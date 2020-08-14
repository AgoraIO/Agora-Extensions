package io.agora.demo.streaming;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

public class MyApplication extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;

        // Workaround for sharing log files
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public static Context getAppContext() {
        return appContext;
    }
}
