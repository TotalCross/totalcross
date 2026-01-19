package totalcross.android;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdjustedInsetsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustToSafeArea();
    }

    private void adjustToSafeArea() {
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );
        View rootView = getWindow().getDecorView();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, insets) -> {

            WindowInsetsCompat rootInsets =
                    ViewCompat.getRootWindowInsets(view);

            if (rootInsets == null) {
                return insets;
            }

            Insets safeInsets = rootInsets.getInsetsIgnoringVisibility(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );

            Insets imeInsets = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            int bottomInset = Math.max(
                    safeInsets.bottom,
                    imeInsets.bottom
            );

            view.setPadding(
                    safeInsets.left,
                    safeInsets.top,
                    safeInsets.right,
                    bottomInset
            );

            return insets;
        });
    }
}
