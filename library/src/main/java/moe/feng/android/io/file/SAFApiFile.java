package moe.feng.android.io.file;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

public class SAFApiFile extends AbstractAutoFile<SAFApiFile, SAFApiFilesProvider> {

    private Uri mRootUri;
    private String mPath;

    SAFApiFile(int providerId, @NonNull Uri rootUri, @NonNull String path) {
        super(providerId);
        mRootUri = rootUri;
        mPath = path;
    }

    private SAFApiFile(Parcel src) {
        super(src.readInt());
        mRootUri = src.readParcelable(Uri.class.getClassLoader());
        mPath = src.readString();
    }

    @NonNull
    private Context getContext() {
        return requireProvider().getContext();
    }

    @Nullable
    public DocumentFile getDocumentFile() {
        return getDocumentFileFromRoot(mPath);
    }

    @Nullable
    public DocumentFile getParentDocumentFile() {
        String newPath = mPath;
        if (newPath.isEmpty()) {
            return null;
        }
        if (newPath.contains("/")) {
            newPath = newPath.substring(0, newPath.lastIndexOf("/"));
        } else {
            newPath = "";
        }
        return getDocumentFileFromRoot(newPath);
    }

    @Nullable
    public DocumentFile getDocumentFileFromRoot(@Nullable String path) {
        DocumentFile docFile = DocumentFile.fromTreeUri(getContext(), mRootUri);
        if (docFile == null || path == null || path.isEmpty()) {
            return docFile;
        } else {
            String[] pathSegments = path.split("/");
            for (String pathSeg : pathSegments) {
                docFile = docFile.findFile(pathSeg);
                if (docFile == null) {
                    return null;
                }
            }
            return docFile;
        }
    }

    @NonNull
    @Override
    public InputStream openInputStream() throws IOException {
        DocumentFile docFile = getDocumentFile();
        if (docFile == null) {
            throw new FileNotFoundException();
        }
        InputStream inputStream = getContext().getContentResolver()
                .openInputStream(docFile.getUri());
        if (inputStream == null) {
            throw new IOException("Cannot open input stream for " + docFile);
        }
        return inputStream;
    }

    @NonNull
    @Override
    public OutputStream openOutputStream() throws IOException {
        DocumentFile docFile = getDocumentFile();
        if (docFile == null) {
            throw new FileNotFoundException();
        }
        OutputStream outputStream = getContext().getContentResolver()
                .openOutputStream(docFile.getUri());
        if (outputStream == null) {
            throw new IOException("Cannot open output stream for " + docFile);
        }
        return outputStream;
    }

    @NonNull
    @Override
    public ParcelFileDescriptor openParcelFileDescriptor(int mode) throws IOException {
        DocumentFile docFile = getDocumentFile();
        if (docFile == null) {
            throw new FileNotFoundException();
        }
        String modeStr = "";
        if ((mode & ParcelFileDescriptor.MODE_READ_ONLY) == ParcelFileDescriptor.MODE_READ_ONLY) {
            modeStr += "r";
        }
        if ((mode & ParcelFileDescriptor.MODE_WRITE_ONLY) == ParcelFileDescriptor.MODE_WRITE_ONLY) {
            modeStr += "w";
        }
        if ((mode & ParcelFileDescriptor.MODE_APPEND) == ParcelFileDescriptor.MODE_APPEND) {
            modeStr += "a";
        }
        if ((mode & ParcelFileDescriptor.MODE_TRUNCATE) == ParcelFileDescriptor.MODE_TRUNCATE) {
            modeStr += "t";
        }
        ParcelFileDescriptor pfd = getContext().getContentResolver()
                .openFileDescriptor(docFile.getUri(), modeStr);
        if (pfd == null) {
            throw new FileNotFoundException();
        }
        return pfd;
    }

    @Override
    public boolean exists() {
        DocumentFile docFile = getDocumentFile();
        return docFile != null && docFile.exists();
    }

    @Override
    public boolean isFile() {
        DocumentFile docFile = getDocumentFile();
        return docFile != null && docFile.isFile();
    }

    @Override
    public boolean isDirectory() {
        DocumentFile docFile = getDocumentFile();
        return docFile != null && docFile.isDirectory();
    }

    @NonNull
    @Override
    public String getPath() {
        return mPath;
    }

    @NonNull
    @Override
    public SAFApiFile getParent() {
        if (mPath.isEmpty()) {
            throw new IllegalStateException("Cannot access the parent of root path.");
        }
        String newPath = mPath;
        if (newPath.contains("/")) {
            newPath = newPath.substring(0, newPath.lastIndexOf("/"));
        }
        return new SAFApiFile(getProviderId(), mRootUri, newPath);
    }

    @NonNull
    @Override
    public String getName() {
        if (mPath.isEmpty()) {
            return "";
        }
        if (!mPath.contains("/")) {
            return mPath;
        }
        return mPath.substring(mPath.lastIndexOf("/") + 1);
    }

    @Override
    public boolean renameTo(@NonNull IAutoFile destination) {
        DocumentFile docFile = getDocumentFile();
        if (docFile == null) {
            return false;
        }
        return copyTo(destination) && delete();
    }

    @Override
    public boolean copyTo(@NonNull IAutoFile destination) {
        DocumentFile docFile = getDocumentFile();
        if (docFile == null) {
            return false;
        }
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
        DocumentFile docFile = getDocumentFile();
        if (docFile == null) {
            return false;
        }
        return docFile.delete();
    }

    @Override
    public boolean ensureParentDirectories() {
        return super.ensureParentDirectories();
    }

    @Override
    public boolean createNewFile() {
        if (getDocumentFile() != null && getDocumentFile().isFile()) {
            return false;
        }

        DocumentFile parentDocFile = getParentDocumentFile();
        if (parentDocFile == null) {
            return false;
        }
        String mimeType = getMimeType();
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        return parentDocFile.createFile(mimeType, getName()) != null;
    }

    @Override
    public long length() {
        DocumentFile docFile = getDocumentFile();
        if (docFile == null) {
            return 0;
        }
        return docFile.length();
    }

    @Override
    public long lastModified() {
        DocumentFile docFile = getDocumentFile();
        if (docFile == null) {
            return 0;
        }
        return docFile.lastModified();
    }

    @NonNull
    @Override
    public SAFApiFile getChildFile(@NonNull String childPath) {
        String newPath = mPath;
        if (newPath.isEmpty()) {
            newPath = childPath;
        } else {
            newPath += "/" + childPath;
        }
        return new SAFApiFile(getProviderId(), mRootUri, newPath);
    }

    @NonNull
    @Override
    public List<String> list() {
        DocumentFile docFile = getDocumentFile();
        if (docFile == null) {
            return Collections.emptyList();
        }
        DocumentFile[] children = docFile.listFiles();
        List<String> childrenList = new ArrayList<>();
        for (DocumentFile child : children) {
            if (child != null) {
                childrenList.add(child.getName());
            }
        }
        return childrenList;
    }

    @Override
    public boolean mkdirs() {
        final DocumentFile rootFile = getDocumentFileFromRoot(null);
        if (rootFile == null) {
            return false;
        }
        String[] pathSegs = mPath.split("/");
        DocumentFile currentFile = rootFile;
        for (String pathSeg : pathSegs) {
            DocumentFile nextFile = currentFile.findFile(pathSeg);
            if (nextFile == null || !nextFile.isDirectory()) {
                currentFile = currentFile.createDirectory(pathSeg);
            } else {
                currentFile = nextFile;
            }
            if (currentFile == null) {
                return false;
            }
        }

        DocumentFile parentDocFile = getParentDocumentFile();
        if (parentDocFile == null) {
            return false;
        }
        return parentDocFile.findFile(getName()) != null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof SAFApiFile) {
            SAFApiFile other = (SAFApiFile) obj;
            return other.mRootUri.equals(this.mRootUri) && other.mPath.equals(this.mPath);
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
        dest.writeParcelable(mRootUri, flags);
        dest.writeString(mPath);
    }

    public static final Creator<SAFApiFile> CREATOR = new Creator<SAFApiFile>() {
        @Override
        public SAFApiFile createFromParcel(Parcel source) {
            return new SAFApiFile(source);
        }

        @Override
        public SAFApiFile[] newArray(int size) {
            return new SAFApiFile[size];
        }
    };

}
