package moe.feng.android.io.file;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class JavaApiFilesProvider implements IAutoFilesProvider<JavaApiFile> {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;

    private final int mId;
    private final String mRootPath;

    JavaApiFilesProvider(int id, @NonNull String rootPath) {
        mId = id;
        mRootPath = rootPath;
    }

    private JavaApiFilesProvider(Parcel src) {
        mId = src.readInt();
        mRootPath = src.readString();
    }

    @Override
    public int getId() {
        return mId;
    }

    @NonNull
    public String getRootPath() {
        return mRootPath;
    }

    @Override
    public boolean isAttached(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // TODO Check App Ops?
            return true;
        }

        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null) {
            externalCacheDir = new File(Environment.getExternalStorageDirectory(),
                    "Android/data/" + context.getPackageName() + "/cache");
        }
        if (mRootPath.startsWith(externalCacheDir.getPath())) {
            return true;
        }
        if (mRootPath.startsWith(externalCacheDir.getParent() + "/data")) {
            return true;
        }

        return context.checkSelfPermission(READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED &&
                context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    @Override
    public void onAttach(final ProviderAttachSession session) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            session.setAttached();
            session.close();
        } else {
            session.requestPermission(
                    new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION,
                    new ProviderAttachSession.PermissionResultHandler() {
                        @Override
                        public void onPermissionResult(@NonNull String[] permissions,
                                                       @NonNull int[] grantResults) {
                            if (grantResults[0] == PERMISSION_GRANTED &&
                                    grantResults[1] == PERMISSION_GRANTED) {
                                session.setAttached();
                            }
                            session.close();
                        }
                    });
        }
    }

    @NonNull
    @Override
    public JavaApiFile getFile(@NonNull Context context, @NonNull String filePath) {
        return new JavaApiFile(mId, mRootPath, filePath);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof JavaApiFilesProvider) {
            JavaApiFilesProvider other = (JavaApiFilesProvider) obj;
            return other.getId() == this.getId() && other.getRootPath().equals(this.getRootPath());
        }
        return false;
    }

}
