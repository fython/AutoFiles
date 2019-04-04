package moe.feng.android.io.file;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static android.app.Activity.RESULT_OK;

public class ScopedDirectoryAccessApiFilesProvider extends SAFApiFilesProvider {

    public static boolean isSupported(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false;
        } else {
            StorageManager sm = context.getSystemService(StorageManager.class);
            PackageManager pm = context.getPackageManager();
            if (sm == null) {
                return false;
            }
            Intent accessIntent = sm.getPrimaryStorageVolume()
                    .createAccessIntent(Environment.DIRECTORY_DOWNLOADS);
            return accessIntent != null && accessIntent.resolveActivity(pm) != null;
        }
    }

    private static final int REQUEST_CODE_SCOPED_DIR_ACCESS = 1;

    private static final String KEY_ROOT_URI = "AutoFiles:root_uri";

    private StorageVolume mStorageVolume;
    private String mDirectoryName;

    @RequiresApi(api = Build.VERSION_CODES.N)
    ScopedDirectoryAccessApiFilesProvider(int id,
                                          @NonNull Context context,
                                          @NonNull StorageVolume targetVolume,
                                          @NonNull String directoryName,
                                          @Nullable String path) {
        super(id, context, null, path);
        mStorageVolume = targetVolume;
        mDirectoryName = directoryName;

        SharedPreferences prefs = AutoFiles.getProviderPreferences(context, id);
        String uriString = prefs.getString(KEY_ROOT_URI, null);
        if (uriString != null) {
            setRootUri(Uri.parse(uriString));
        }
    }

    @NonNull
    public StorageVolume getStorageVolume() {
        return mStorageVolume;
    }

    @NonNull
    public String getDirectoryName() {
        return mDirectoryName;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onAttach(final ProviderAttachSession session) {
        final Context context = session.getContext();
        Intent requestIntent = mStorageVolume.createAccessIntent(mDirectoryName);
        if (requestIntent == null) {
            session.close();
            return;
        }
        session.addActivityResultHandler(REQUEST_CODE_SCOPED_DIR_ACCESS,
                new ProviderAttachSession.ActivityResultHandler() {
                    @Override
                    public void onActivityResult(int resultCode, @Nullable Intent data) {
                        if (RESULT_OK == resultCode && data != null) {
                            Uri resultUri = data.getData();
                            if (resultUri != null) {
                                context.getContentResolver().takePersistableUriPermission(resultUri,
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                setRootUri(resultUri);
                                session.getProviderPreferences().edit()
                                        .putString(KEY_ROOT_URI, resultUri.toString())
                                        .apply();
                            }
                        }
                        session.close();
                    }
                });
        session.startActivityForResult(requestIntent, REQUEST_CODE_SCOPED_DIR_ACCESS);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ScopedDirectoryAccessApiFilesProvider) {
            ScopedDirectoryAccessApiFilesProvider other =
                    (ScopedDirectoryAccessApiFilesProvider) obj;
            return other.mStorageVolume.equals(this.mStorageVolume) &&
                    other.mDirectoryName.equals(this.mDirectoryName) &&
                    Objects.equals(other.getPath(), this.getPath()) &&
                    other.getId() == this.getId();
        }
        return false;
    }

}
