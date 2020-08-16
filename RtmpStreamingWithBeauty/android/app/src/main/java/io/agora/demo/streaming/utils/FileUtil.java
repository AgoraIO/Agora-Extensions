package io.agora.demo.streaming.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class FileUtil {
    private static final String LOG_FOLDER_NAME = "log";

    /**
     * Get the log file path
     * @param context Context to find the accessible file folder
     * @param fileName The name of the log file
     * @return The absolute path of the log file
     */
    public static String getLogFilePath(Context context, String fileName) {
        File folder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                LOG_FOLDER_NAME);
        } else {
            folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + context.getPackageName() + File.separator + LOG_FOLDER_NAME);
        }

        if (!folder.exists() && !folder.mkdir()) {
            return "";
        } else {
            return new File(folder, fileName).getAbsolutePath();
        }
    }
}
