package io.agora.demo.streaming;

import android.app.Application;
import android.os.StrictMode;

import com.tencent.bugly.crashreport.CrashReport;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Bugly crash report
        CrashReport.initCrashReport(getApplicationContext(), "009978ddb3", true);

        // Workaround for sharing log files
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }
}
