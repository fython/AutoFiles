package moe.feng.android.io.file;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static java.util.Objects.requireNonNull;
import static moe.feng.android.io.file.AutoFiles.EXTRA_PROVIDER_ID;
import static moe.feng.android.io.file.PrivateConstants.LOG_TAG;

public class ProviderAttachActivity extends Activity {

    private IAutoFilesProvider mProvider;
    private ProviderAttachSession mSession;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(EXTRA_PROVIDER_ID)) {
            Log.e(LOG_TAG, "ProviderAttachActivity requires EXTRA_PROVIDER argument.",
                    new IllegalArgumentException());
            return;
        }
        mProvider = AutoFiles.getProvider(intent.getIntExtra(EXTRA_PROVIDER_ID, -1));
        requireNonNull(mProvider);

        Log.d(LOG_TAG, "Now starting to attach provider (id=" + mProvider.getId() + ", " +
                "class=" + mProvider.getClass().getName() + ").");

        mSession = new ProviderAttachSession(this, mProvider.getId());

        try {
            mProvider.onAttach(mSession);
        } catch (Exception e) {
            mSession.setException(e);
            mSession.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSession.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mSession.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mSession.onPermissionResult(requestCode, permissions, grantResults);
    }

}
