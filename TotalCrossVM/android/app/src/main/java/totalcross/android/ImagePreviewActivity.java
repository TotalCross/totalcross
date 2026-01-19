package totalcross.android;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

public class ImagePreviewActivity extends AdjustedInsetsActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_image_preview);

        ImageView img = findViewById(R.id.imageView);

        String path = getIntent().getStringExtra("image_path");
        if (path == null) {
            finish();
            return;
        }

        img.setImageURI(Uri.fromFile(new File(path)));

        ImageButton btnBackPlayer = findViewById(R.id.btnBackPlayer);
        btnBackPlayer.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        ImageButton btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(view -> {
            setResult(RESULT_OK);
            finish();
        });
    }
}
