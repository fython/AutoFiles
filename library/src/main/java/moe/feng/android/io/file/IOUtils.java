package moe.feng.android.io.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;

public final class IOUtils {

    private IOUtils() {
        throw new UnsupportedOperationException("Use static method only");
    }

    private static final int COPY_BUFFER_SIZE = 1024;

    public static void copy(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        byte[] buf = new byte[COPY_BUFFER_SIZE];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

}
