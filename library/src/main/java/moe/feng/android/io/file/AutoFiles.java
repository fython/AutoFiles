package moe.feng.android.io.file;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static android.content.Context.MODE_PRIVATE;
import static moe.feng.android.io.file.PrivateConstants.LOG_TAG;

public final class AutoFiles {

    public static final String EXTRA_PROVIDER_ID = "AutoFiles:provider_id";
    public static final String EXTRA_EXCEPTION = "AutoFiles:exception";
    public static final String PREFS_NAME_FORMAT = "AutoFiles_provider_pref_%d";

    private static final int PROVIDER_ID_EXTERNAL_ANDROID_CACHE = -1;
    private static final int PROVIDER_ID_EXTERNAL_ANDROID_DATA = -2;

    private AutoFiles() {
        throw new UnsupportedOperationException("Use static method only.");
    }

    private static final SparseArray<IAutoFilesProvider> mProviders = new SparseArray<>();

    @Nullable
    public static <T extends IAutoFilesProvider> T getProvider(int providerId) {
        return (T) mProviders.get(providerId);
    }

    @NonNull
    public static <T extends IAutoFilesProvider> T requireProvider(int providerId) {
        return (T) Objects.requireNonNull(mProviders.get(providerId));
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    public static SharedPreferences getProviderPreferences(@NonNull Context context, int id) {
        return context.getSharedPreferences(String.format(PREFS_NAME_FORMAT, id), MODE_PRIVATE);
    }

    @NonNull
    public static JavaApiFilesProvider createJavaFilesProvider(int id, @NonNull String rootPath) {
        if (Build.VERSION.SDK_INT >= 29 ||
                (Build.VERSION.SDK_INT == 28 && Build.VERSION.PREVIEW_SDK_INT >= 1)) {
            try {
                File extStorageFile = Environment.getExternalStorageDirectory();
                if (extStorageFile != null) {
                    String extPath = extStorageFile.getPath();
                    if (rootPath.startsWith(extPath + "/")) {
                        String relativePath = extPath.replace(extPath + "/", "");
                        if (relativePath.contains("/")) {
                            relativePath = relativePath.substring(0, relativePath.indexOf("/"));
                        }
                        if (EnvironmentUtils.isStandardDirectory(relativePath)) {
                            Log.w(LOG_TAG, "Accessing media files in public directories by " +
                                    "Java File API is not recommended since Android Q. " +
                                    "Please use MediaStore or Storage Access Framework " +
                                    "(including Scoped Directory Access) instead.");
                        }
                    }
                }
            } catch (Exception ignored) {

            }
        }

        if (mProviders.indexOfKey(id) != -1) {
            IAutoFilesProvider savedProvider = mProviders.get(id);
            if (savedProvider instanceof JavaApiFilesProvider) {
                JavaApiFilesProvider savedJavaProvider = (JavaApiFilesProvider) savedProvider;
                if (savedJavaProvider.getId() == id &&
                        savedJavaProvider.getRootPath().equals(rootPath)) {
                    return savedJavaProvider;
                }
            }
        }
        JavaApiFilesProvider newInstance = new JavaApiFilesProvider(id, rootPath);
        mProviders.put(id, newInstance);
        return newInstance;
    }

    public static JavaApiFilesProvider createExternalCacheFilesProvider(@NonNull Context context) {
        return createJavaFilesProvider(PROVIDER_ID_EXTERNAL_ANDROID_CACHE,
                EnvironmentUtils.getExternalCacheDirSafe(context).getPath());
    }

    public static JavaApiFilesProvider createExternalDataFilesProvider(@NonNull Context context) {
        return createJavaFilesProvider(PROVIDER_ID_EXTERNAL_ANDROID_DATA,
                EnvironmentUtils.getExternalDataDirSafe(context).getPath());
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ScopedDirectoryAccessApiFilesProvider createScopedDirectoryAccessFilesProvider(
            int id,
            @NonNull Context context,
            @NonNull StorageVolume storageVolume,
            @NonNull String directoryName,
            @Nullable String childPath
    ) {
        if (mProviders.indexOfKey(id) != -1) {
            IAutoFilesProvider savedProvider = mProviders.get(id);
            if (savedProvider instanceof ScopedDirectoryAccessApiFilesProvider) {
                ScopedDirectoryAccessApiFilesProvider savedSDAProvider =
                        (ScopedDirectoryAccessApiFilesProvider) savedProvider;
                if (savedSDAProvider.getId() == id &&
                        savedSDAProvider.getStorageVolume().equals(storageVolume) &&
                        savedSDAProvider.getDirectoryName().equals(directoryName)) {
                    return savedSDAProvider;
                }
            }
        }
        ScopedDirectoryAccessApiFilesProvider newInstance =
                new ScopedDirectoryAccessApiFilesProvider(
                        id, context, storageVolume, directoryName, childPath
                );
        mProviders.put(id, newInstance);
        return newInstance;
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ScopedDirectoryAccessApiFilesProvider createScopedDirectoryAccessFilesProvider(
            int id,
            @NonNull Context context,
            @NonNull String directoryName,
            @Nullable String childPath
    ) {
        StorageManager sm = Objects.requireNonNull(context.getSystemService(StorageManager.class));
        return createScopedDirectoryAccessFilesProvider(
                id, context, sm.getPrimaryStorageVolume(), directoryName, childPath);
    }

    @NonNull
    public static DocumentsApiFilesProvider createDocumentsFilesProvider(
            int id, @NonNull Context context
    ) {

        if (mProviders.indexOfKey(id) != -1) {
            IAutoFilesProvider savedProvider = mProviders.get(id);
            if (savedProvider instanceof DocumentsApiFilesProvider) {
                return (DocumentsApiFilesProvider) savedProvider;
            }
        }
        DocumentsApiFilesProvider newInstance = new DocumentsApiFilesProvider(id, context, null);
        mProviders.put(id, newInstance);
        return newInstance;
    }

    @SuppressLint("NewApi")
    @NonNull
    public static IAutoFilesProvider createPublicDirFilesProvider(
            int id,
            @NonNull Context context,
            @NonNull String publicDirName,
            @Nullable String path
    ) {
        if (!EnvironmentUtils.isStandardDirectory(publicDirName)) {
            throw new IllegalArgumentException(publicDirName + " is not a standard directory.");
        }
        if (ScopedDirectoryAccessApiFilesProvider.isSupported(context)) {
            return createScopedDirectoryAccessFilesProvider(id, context, publicDirName, path);
        } else {
            return createJavaFilesProvider(id,
                    Environment.getExternalStoragePublicDirectory(publicDirName).getPath());
        }
    }

    @NonNull
    public static <T extends IAutoFile<T>> Intent getRequestAttachIntent(
            @NonNull Context context,
            @NonNull IAutoFilesProvider<T> provider
    ) {
        Intent intent = new Intent(context, ProviderAttachActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra(EXTRA_PROVIDER_ID, provider.getId());
        return intent;
    }

    public static <T extends IAutoFile<T>> void requestAttach(
            @NonNull Activity activity,
            int requestCode,
            @NonNull IAutoFilesProvider<T> provider
    ) {
        if (provider.isAttached(activity)) {
            Log.w(LOG_TAG, "This provider has been attached.");
            return;
        }
        activity.startActivityForResult(getRequestAttachIntent(activity, provider), requestCode);
    }

    public static <T extends IAutoFile<T>> void requestAttach(
            @NonNull Fragment fragment,
            int requestCode,
            @NonNull IAutoFilesProvider<T> provider
    ) {
        if (provider.isAttached(fragment.getActivity())) {
            Log.w(LOG_TAG, "This provider has been attached.");
            return;
        }
        fragment.startActivityForResult(
                getRequestAttachIntent(fragment.getActivity(), provider),
                requestCode
        );
    }

    public static <T extends IAutoFile<T>> void requestAttach(
            @NonNull androidx.fragment.app.Fragment fragment,
            int requestCode,
            @NonNull IAutoFilesProvider<T> provider
    ) {
        if (provider.isAttached(fragment.requireContext())) {
            Log.w(LOG_TAG, "This provider has been attached.");
            return;
        }
        fragment.startActivityForResult(
                getRequestAttachIntent(fragment.requireContext(), provider),
                requestCode
        );
    }

}
