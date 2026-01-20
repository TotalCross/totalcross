package totalcross.android;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.journeyapps.barcodescanner.CaptureActivity;

public class MyCaptureActivity extends CaptureActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View root = findViewById(R.id.zxing_barcode_scanner);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            ImageButton btnBack = new ImageButton(this);
            btnBack.setImageResource(R.drawable.ic_close_white);
            btnBack.setId(R.id.zxing_back_button);
            btnBack.setBackgroundColor(Color.TRANSPARENT);
            btnBack.setOnClickListener(l -> {
                setResult(Activity.RESULT_CANCELED);
                finish();
            });

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    dp(48), dp(48)
            );
            lp.gravity = Gravity.TOP | Gravity.START;
            lp.topMargin = sys.top + dp(16);
            lp.leftMargin = dp(16);

            ((ViewGroup) v).addView(btnBack, lp);

            return insets;
        });
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }
}
