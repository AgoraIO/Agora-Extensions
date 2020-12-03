package io.agora.demo.streaming;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.tencent.bugly.crashreport.CrashReport;

public class DemoApplication extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;

        // Bugly crash report
        CrashReport.initCrashReport(getApplicationContext(), "009978ddb3", true);

        // Workaround for sharing log files
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public static Context getAppContext() {
        return appContext;
    }
}
