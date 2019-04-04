package moe.feng.android.io.file;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

public abstract class SAFApiFilesProvider implements IAutoFilesProvider<SAFApiFile> {

    private int mId;
    private Context mContext;
    private Uri mRootUri;
    private String mPath;

    SAFApiFilesProvider(int id,
                        @NonNull Context context,
                        @Nullable Uri rootUri,
                        @Nullable String path) {
        mId = id;
        if (context instanceof Application) {
            mContext = context;
        } else {
            mContext = context.getApplicationContext();
        }
        mRootUri = rootUri;
        mPath = path;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public boolean isAttached(@NonNull Context context) {
        if (getRootUri() == null) {
            return false;
        }
        DocumentFile docFile = DocumentFile.fromTreeUri(context, getRootUri());
        return docFile != null && docFile.canRead() && docFile.canWrite();
    }

    @Nullable
    public Uri getRootUri() {
        return mRootUri;
    }

    protected void setRootUri(@Nullable Uri rootUri) {
        mRootUri = rootUri;
    }

    @Nullable
    public String getPath() {
        return mPath;
    }

    public void setPath(@Nullable String path) {
        mPath = path;
    }

    @NonNull
    @Override
    public SAFApiFile getFile(@NonNull Context context, @NonNull String filePath) {
        Uri rootUri = getRootUri();
        if (rootUri == null) {
            throw new ProviderNotAttachedException(getId());
        }
        return new SAFApiFile(
                mId,
                rootUri,
                getPath() == null ? filePath : getPath() + "/" + filePath
        );
    }

}
