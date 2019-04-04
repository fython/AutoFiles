package moe.feng.android.io.file.demo;

import android.app.Application;
import android.os.Environment;

import moe.feng.android.io.file.AutoFiles;

public class DemoApplication extends Application {

    public static final int PROVIDER_ID_PICTURES = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        AutoFiles.createPublicDirFilesProvider(
                PROVIDER_ID_PICTURES, this, Environment.DIRECTORY_PICTURES, "AutoFilesDemo");
    }

}
