package totalcross.android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RecordProgressDrawable extends Drawable {

    private final Paint trackPaint;      // Track
    private final Paint progressPaint;   // Progress

    private float progress = 0f; // 0.0 → 1.0
    private final RectF arcRect = new RectF();

    public RecordProgressDrawable() {
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(10f);
        trackPaint.setColor(Color.parseColor("#50FFFFFF")); // White 50%

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(10f);
        progressPaint.setColor(Color.WHITE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        RectF bounds = new RectF(getBounds());
        float padding = 12f;

        arcRect.set(
                bounds.left + padding,
                bounds.top + padding,
                bounds.right - padding,
                bounds.bottom - padding
        );

        // Track (complete circle)
        canvas.drawArc(arcRect, -90, 360, false, trackPaint);

        // Progress (partial arc)
        canvas.drawArc(arcRect, -90, progress * 360, false, progressPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        trackPaint.setAlpha(alpha);
        progressPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable android.graphics.ColorFilter colorFilter) {
        trackPaint.setColorFilter(colorFilter);
        progressPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }

    /** Updates progress between 0f and 1f */
    public void setProgress(float value) {
        progress = Math.max(0f, Math.min(1f, value));
        invalidateSelf();
    }

    /** Changes progress color */
    public void setProgressColor(int color) {
        progressPaint.setColor(color);
        invalidateSelf();
    }
}
