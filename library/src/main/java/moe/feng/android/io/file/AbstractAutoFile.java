package moe.feng.android.io.file;

import android.webkit.MimeTypeMap;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public abstract class AbstractAutoFile<T extends IAutoFile<T>, P extends IAutoFilesProvider<T>>
        implements IAutoFile<T> {

    private final int mProviderId;

    public AbstractAutoFile(int providerId) {
        mProviderId = providerId;
    }

    @Override
    public int getProviderId() {
        return mProviderId;
    }

    @Nullable
    public P getProvider() {
        return AutoFiles.getProvider(getProviderId());
    }

    @NonNull
    public P requireProvider() {
        return requireNonNull(getProvider());
    }

    @Nullable
    @Override
    public String getNameSuffix() {
        final String name = getName();
        if (!name.contains(".")) {
            return null;
        }
        return name.substring(name.lastIndexOf(".") + 1);
    }

    @Nullable
    @Override
    public String getMimeType() {
        final String nameSuffix = getNameSuffix();
        if (nameSuffix == null) {
            return null;
        } else {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(nameSuffix);
        }
    }

    @Override
    public boolean renameOnly(@NonNull String name) {
        if (getName().isEmpty()) {
            throw new UnsupportedOperationException("Rename root path is unsupported.");
        }
        return renameTo(getParent().getChildFile(name));
    }

    @NonNull
    @Override
    public List<T> listFiles() {
        final List<T> list = new ArrayList<>();
        for (String childName : list()) {
            list.add(getChildFile(childName));
        }
        return list;
    }

    @Override
    public boolean ensureParentDirectories() {
        return getParent().mkdirs();
    }

}
