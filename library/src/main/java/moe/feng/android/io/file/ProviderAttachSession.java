package moe.feng.android.io.file;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static moe.feng.android.io.file.PrivateConstants.LOG_TAG;

public class ProviderAttachSession {

    private ProviderAttachActivity mActivity;
    private int mProviderId;

    private boolean isDestroyed = false;
    private SparseArray<ActivityResultHandler> mActivityResultHandlers;
    private SparseArray<PermissionResultHandler> mPermissionResultHandlers;

    private int mResultCode = Activity.RESULT_CANCELED;
    private Intent mResultIntent = new Intent();

    ProviderAttachSession(@NonNull ProviderAttachActivity activity, int providerId) {
        mActivity = activity;
        mProviderId = providerId;
        mActivityResultHandlers = new SparseArray<>();
        mPermissionResultHandlers = new SparseArray<>();
    }

    public int getProviderId() {
        return mProviderId;
    }

    @NonNull
    public Context getContext() {
        return mActivity;
    }

    @NonNull
    public SharedPreferences getProviderPreferences() {
        return AutoFiles.getProviderPreferences(mActivity, mProviderId);
    }

    public void startActivityForResult(@NonNull Intent intent, int requestCode) {
        mActivity.startActivityForResult(intent, requestCode);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermission(@NonNull String[] permissions, int requestCode) {
        mActivity.requestPermissions(permissions, requestCode);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermission(@NonNull String[] permissions,
                                  int requestCode,
                                  @NonNull PermissionResultHandler handler) {
        addPermissionResultHandler(requestCode, handler);
        mActivity.requestPermissions(permissions, requestCode);
    }

    public void addActivityResultHandler(int requestCode, @NonNull ActivityResultHandler handler) {
        mActivityResultHandlers.put(requestCode, handler);
    }

    void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        ActivityResultHandler handler = mActivityResultHandlers.get(requestCode);
        if (handler != null) {
            handler.onActivityResult(resultCode, data);
        } else {
            Log.e(LOG_TAG, "No handler for requestCode=" + requestCode);
            close();
        }
    }

    public void addPermissionResultHandler(int requestCode,
                                           @NonNull PermissionResultHandler handler) {
        mPermissionResultHandlers.put(requestCode, handler);
    }

    void onPermissionResult(int requestCode,
                            @NonNull String[] permissions,
                            @NonNull int[] grantResults) {
        PermissionResultHandler handler = mPermissionResultHandlers.get(requestCode);
        if (handler != null) {
            handler.onPermissionResult(permissions, grantResults);
        } else {
            Log.e(LOG_TAG, "No permission handler for requestCode=" + requestCode);
            close();
        }
    }

    public void setAttached() {
        if (mActivity != null && !mActivity.isFinishing() && !isDestroyed) {
            mResultCode = Activity.RESULT_OK;
            mActivity.setResult(mResultCode, mResultIntent);
        }
    }

    public void setException(@Nullable Exception exception) {
        if (mActivity != null && !mActivity.isFinishing() && !isDestroyed) {
            mResultIntent.putExtra(AutoFiles.EXTRA_EXCEPTION, exception);
            mActivity.setResult(mResultCode, mResultIntent);
        }
    }

    public void close() {
        mActivityResultHandlers.clear();
        mPermissionResultHandlers.clear();
        if (mActivity != null && !mActivity.isFinishing() && !isDestroyed) {
            mActivity.setResult(mResultCode, mResultIntent);
            mActivity.finish();
        }
    }

    void onDestroy() {
        isDestroyed = true;
        mActivityResultHandlers.clear();
        mPermissionResultHandlers.clear();
    }

    public interface ActivityResultHandler {

        void onActivityResult(int resultCode, @Nullable Intent data);

    }

    public interface PermissionResultHandler {

        void onPermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults);

    }

}
