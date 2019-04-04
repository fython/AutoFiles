package moe.feng.android.io.file;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static android.app.Activity.RESULT_OK;

public class DocumentsApiFilesProvider extends SAFApiFilesProvider {

    @SuppressLint("ObsoleteSdkInt")
    public static boolean isSupported(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 1;

    private static final String KEY_ROOT_URI = "AutoFiles:root_uri";

    private Uri mInitialUri = null;

    DocumentsApiFilesProvider(int id, @NonNull Context context, @Nullable String path) {
        super(id, context, null, null);

        SharedPreferences prefs = AutoFiles.getProviderPreferences(context, id);
        String uriString = prefs.getString(KEY_ROOT_URI, null);
        if (uriString != null) {
            setRootUri(Uri.parse(uriString));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setInitialUri(@Nullable Uri initialUri) {
        mInitialUri = initialUri;
    }

    @Override
    public void onAttach(final ProviderAttachSession session) {
        final Context context = session.getContext();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mInitialUri != null) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, mInitialUri);
        }
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            session.setException(new ActivityNotFoundException("Cannot resolve DocumentsUi app."));
            session.close();
            return;
        }
        session.addActivityResultHandler(REQUEST_CODE_OPEN_DOCUMENT_TREE,
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
        session.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof DocumentsApiFilesProvider) {
            DocumentsApiFilesProvider other = (DocumentsApiFilesProvider) obj;
            return Objects.equals(other.getPath(), this.getPath()) &&
                    other.getId() == this.getId();
        }
        return false;
    }

}
