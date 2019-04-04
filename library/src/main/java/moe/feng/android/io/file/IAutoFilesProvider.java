package moe.feng.android.io.file;

import android.content.Context;

import androidx.annotation.NonNull;

public interface IAutoFilesProvider<T extends IAutoFile<T>> {

    int getId();

    boolean isAttached(@NonNull Context context);

    void onAttach(ProviderAttachSession session);

    @NonNull
    T getFile(@NonNull Context context, @NonNull String filePath);

}
