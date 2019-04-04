package moe.feng.android.io.file.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import java.io.OutputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import moe.feng.android.io.file.AutoFiles;
import moe.feng.android.io.file.IAutoFile;
import moe.feng.android.io.file.IAutoFilesProvider;

public class SampleActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ATTACH_FILES = 10;

    private IAutoFilesProvider<?> mFilesProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        mFilesProvider = AutoFiles.requireProvider(DemoApplication.PROVIDER_ID_PICTURES);

        findViewById(R.id.request_attach_button).setOnClickListener(v -> {
            if (!mFilesProvider.isAttached(this)) {
                AutoFiles.requestAttach(this, REQUEST_CODE_ATTACH_FILES, mFilesProvider);
            }
        });

        findViewById(R.id.write_file_button).setOnClickListener(v -> {
            if (!mFilesProvider.isAttached(this)) {
                Toast.makeText(
                        this,
                        "FilesProvider hasn't been attached.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
            IAutoFile file = mFilesProvider.getFile(this, "1.jpg");
            try {
                Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565);
                bitmap.eraseColor(Color.CYAN);
                file.ensureParentDirectories();
                file.createNewFile();
                OutputStream out = file.openOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_ATTACH_FILES) {
            if (resultCode == RESULT_OK) {

            } else {
                if (data == null) {
                    return;
                }
                Exception err = (Exception) data.getSerializableExtra(AutoFiles.EXTRA_EXCEPTION);
                if (err != null) {
                    throw new RuntimeException(err);
                }
            }
        }
    }
}
