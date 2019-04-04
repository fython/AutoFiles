package moe.feng.android.io.file;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class JavaApiFile extends AbstractAutoFile<JavaApiFile, JavaApiFilesProvider> {

    private String mRootPath;
    private String mPath;
    private File mJavaFile;

    JavaApiFile(int providerId, @NonNull String rootPath, @NonNull String path) {
        super(providerId);
        mRootPath = rootPath;
        mPath = path;
        mJavaFile = new File(rootPath + "/" + path);
    }

    private JavaApiFile(@NonNull Parcel src) {
        super(src.readInt());
        mRootPath = src.readString();
        mPath = src.readString();
        mJavaFile = (File) src.readSerializable();
    }

    @NonNull
    @Override
    public InputStream openInputStream() throws IOException {
        return new FileInputStream(mJavaFile);
    }

    @NonNull
    @Override
    public OutputStream openOutputStream() throws IOException {
        return new FileOutputStream(mJavaFile);
    }

    @NonNull
    @Override
    public ParcelFileDescriptor openParcelFileDescriptor(int mode) throws IOException {
        return ParcelFileDescriptor.open(mJavaFile, mode);
    }

    @Override
    public boolean exists() {
        return mJavaFile.exists();
    }

    @Override
    public boolean isFile() {
        return mJavaFile.isFile();
    }

    @Override
    public boolean isDirectory() {
        return mJavaFile.isDirectory();
    }

    @NonNull
    @Override
    public String getPath() {
        return mJavaFile.getPath();
    }

    @NonNull
    @Override
    public JavaApiFile getParent() {
        if (mPath.isEmpty()) {
            throw new IllegalStateException("Cannot access the parent of root path.");
        }
        String newPath = mPath;
        if (newPath.contains("/")) {
            newPath = newPath.substring(0, newPath.lastIndexOf("/"));
        }
        return new JavaApiFile(getProviderId(), mRootPath, newPath);
    }

    @NonNull
    @Override
    public String getName() {
        if (mPath.isEmpty()) {
            return "";
        }
        return mJavaFile.getName();
    }

    @Override
    public boolean renameTo(@NonNull IAutoFile destination) {
        if (destination instanceof JavaApiFile) {
            return mJavaFile.renameTo(((JavaApiFile) destination).mJavaFile);
        } else {
            // Unknown IAutoFile type
            return copyTo(destination) && delete();
        }
    }

    @Override
    public boolean copyTo(@NonNull IAutoFile destination) {
        try {
            InputStream srcStream = openInputStream();
            OutputStream dstStream = destination.openOutputStream();
            IOUtils.copy(srcStream, dstStream);
            srcStream.close();
            dstStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete() {
        return mJavaFile.delete();
    }

    @Override
    public boolean createNewFile() {
        try {
            return mJavaFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long length() {
        return mJavaFile.length();
    }

    @Override
    public long lastModified() {
        return mJavaFile.lastModified();
    }

    @NonNull
    @Override
    public JavaApiFile getChildFile(@NonNull String childPath) {
        String newPath = mPath;
        if (newPath.isEmpty()) {
            newPath = childPath;
        } else {
            newPath += "/" + childPath;
        }
        return new JavaApiFile(getProviderId(), mRootPath, newPath);
    }

    @NonNull
    @Override
    public List<String> list() {
        return Arrays.asList(mJavaFile.list());
    }

    @Override
    public boolean mkdirs() {
        return mJavaFile.mkdirs();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof JavaApiFile) {
            final JavaApiFile other = (JavaApiFile) obj;
            return other.mRootPath.equals(this.mRootPath) && other.mPath.equals(this.mPath);
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getProviderId());
        dest.writeString(mRootPath);
        dest.writeString(mPath);
        dest.writeSerializable(mJavaFile);
    }

    public static final Creator<JavaApiFile> CREATOR = new Creator<JavaApiFile>() {
        @Override
        public JavaApiFile createFromParcel(Parcel source) {
            return new JavaApiFile(source);
        }

        @Override
        public JavaApiFile[] newArray(int size) {
            return new JavaApiFile[size];
        }
    };

}
