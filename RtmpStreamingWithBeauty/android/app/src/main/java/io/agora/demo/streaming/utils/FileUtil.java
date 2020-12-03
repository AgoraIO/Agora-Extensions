package io.agora.demo.streaming.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class FileUtil {
    private static final String LOG_FOLDER_NAME = "log";
    private static final String MEDIA_FOLDER_NAME = "media";

    /**
     * Get the log file path
     * @param context Context to find the accessible file folder
     * @param fileName The name of the log file
     * @return The absolute path of the log file
     */
    public static String getLogFilePath(Context context, String fileName) {
        File folder;
        if (Build.VERSION.SDK_INT >= 29) {
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

    public static String geMediaFilePath(Context context, String fileName) {
        File folder;
        if (Build.VERSION.SDK_INT >= 29) {
            folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    MEDIA_FOLDER_NAME);
        } else {
            folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + context.getPackageName() + File.separator + MEDIA_FOLDER_NAME);
        }

        if (!folder.exists() && !folder.mkdir()) {
            return "";
        } else {
            return new File(folder, fileName).getAbsolutePath();
        }
    }
}
