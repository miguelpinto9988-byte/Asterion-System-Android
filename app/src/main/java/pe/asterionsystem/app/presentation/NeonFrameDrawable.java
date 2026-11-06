package pe.asterionsystem.app.presentation;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Resolution-independent neon octagonal panel. Border and glow are actual
 * native drawing commands responding to focused/pressed/selected states.
 */
public final class NeonFrameDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int accent;
    private final float density;
    private boolean emphasized;
    private int drawableAlpha = 255;

    public NeonFrameDrawable(int accent, float density) {
        this.accent = accent;
        this.density = density;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        float inset = 5f * density;
        float left = b.left + inset;
        float top = b.top + inset;
        float right = b.right - inset;
        float bottom = b.bottom - inset;
        if (right <= left || bottom <= top) return;
        float cut = Math.min(17f * density, Math.min(right-left, bottom-top)*0.14f);
        Path contour = new Path();
        contour.moveTo(left + cut, top);
        contour.lineTo(right - cut, top);
        contour.lineTo(right, top + cut);
        contour.lineTo(right, bottom - cut);
        contour.lineTo(right - cut, bottom);
        contour.lineTo(left + cut, bottom);
        contour.lineTo(left, bottom - cut);
        contour.lineTo(left, top + cut);
        contour.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(emphasized ? 238 : 220, 5, 12, 35));
        canvas.drawPath(contour, paint);
        for (int layer = 3; layer >= 0; layer--) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth((layer == 0 ? 1.7f : (layer*2.7f+1.5f)) * density);
            int alpha = (layer == 0 ? 255 : (emphasized ? 48 : 23));
            alpha = alpha * drawableAlpha / 255;
            paint.setColor((accent & 0x00FFFFFF) | (alpha << 24));
            canvas.drawPath(contour, paint);
        }
        // Subtle luminous upper rail: independent of bitmap resolution.
        paint.setStrokeWidth(1f * density);
        paint.setColor((accent & 0x00FFFFFF) | (Math.min(220, drawableAlpha) << 24));
        canvas.drawLine(left + cut + 3*density, top + 2*density,
                right - cut - 3*density, top + 2*density, paint);
    }

    @Override public boolean isStateful() { return true; }
    @Override protected boolean onStateChange(int[] states) {
        boolean next = false;
        for (int state : states) {
            if (state == android.R.attr.state_pressed
                    || state == android.R.attr.state_focused
                    || state == android.R.attr.state_selected) {
                next = true;
                break;
            }
        }
        if (emphasized == next) return false;
        emphasized = next;
        invalidateSelf();
        return true;
    }
    @Override public void setAlpha(int alpha) {
        drawableAlpha = Math.max(0, Math.min(255, alpha));
        invalidateSelf();
    }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
