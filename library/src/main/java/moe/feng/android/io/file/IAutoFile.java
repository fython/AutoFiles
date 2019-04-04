package moe.feng.android.io.file;

import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IAutoFile<T extends IAutoFile> extends Parcelable {

    int getProviderId();

    @NonNull
    InputStream openInputStream() throws IOException;

    @NonNull
    OutputStream openOutputStream() throws IOException;

    @NonNull
    ParcelFileDescriptor openParcelFileDescriptor(int mode) throws IOException;

    boolean exists();

    boolean isFile();

    boolean isDirectory();

    @NonNull
    String getPath();

    @NonNull
    T getParent();

    @NonNull
    String getName();

    @Nullable
    String getNameSuffix();

    @Nullable
    String getMimeType();

    boolean renameTo(@NonNull IAutoFile destination);

    boolean copyTo(@NonNull IAutoFile destination);

    boolean renameOnly(@NonNull String name);

    boolean delete();

    boolean createNewFile();

    long length();

    long lastModified();

    @NonNull
    T getChildFile(@NonNull String childPath);

    @NonNull
    List<String> list();

    @NonNull
    List<T> listFiles();

    boolean mkdirs();

    boolean ensureParentDirectories();

}
