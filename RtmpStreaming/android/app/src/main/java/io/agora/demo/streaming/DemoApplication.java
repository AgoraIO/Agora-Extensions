package io.agora.demo.streaming;

import android.app.Application;
import android.os.StrictMode;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Workaround for sharing log files
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }
}
