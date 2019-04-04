package moe.feng.android.io.file;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import androidx.annotation.NonNull;

import static android.os.Environment.*;

public final class EnvironmentUtils {

    public static final String[] STANDARD_DIRECTORIES = {
            DIRECTORY_MUSIC,
            DIRECTORY_PODCASTS,
            DIRECTORY_RINGTONES,
            DIRECTORY_ALARMS,
            DIRECTORY_NOTIFICATIONS,
            DIRECTORY_PICTURES,
            DIRECTORY_MOVIES,
            DIRECTORY_DOWNLOADS,
            DIRECTORY_DCIM,
            DIRECTORY_DOCUMENTS
    };

    private EnvironmentUtils() {
        throw new UnsupportedOperationException("Use static methods only");
    }

    @NonNull
    public static File getExternalCacheDirSafe(@NonNull Context context) {
        File file = context.getExternalCacheDir();
        if (file == null) {
            file = new File(Environment.getExternalStorageDirectory(),
                    "Android/data/" + context.getPackageName() + "/cache");
        }
        return file;
    }

    @NonNull
    public static File getExternalDataDirSafe(@NonNull Context context) {
        File file = context.getExternalCacheDir();
        if (file == null) {
            file = new File(Environment.getExternalStorageDirectory(),
                    "Android/data/" + context.getPackageName() + "/data");
        } else {
            file = new File(file.getParent() + "/data");
        }
        return file;
    }

    public static boolean isStandardDirectory(String dir) {
        for (String valid : STANDARD_DIRECTORIES) {
            if (valid.equals(dir)) {
                return true;
            }
        }
        return false;
    }

}
