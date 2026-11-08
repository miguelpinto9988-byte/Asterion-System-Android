package pe.asterionsystem.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.SystemClock;
import android.view.View;

/** Local offline GIF playback for the isolated preview APK, without cropping. */
public final class AnimatedSplashView extends View {
    private static final long FRAME_INTERVAL_MILLIS = 50L;
    private static final int FALLBACK_DURATION_MILLIS = 1000;

    private final Movie gif;
    private long firstFrameAtMillis;
    private boolean playing;

    public AnimatedSplashView(Context context, Movie gif) {
        super(context);
        if (gif == null || gif.width() <= 0 || gif.height() <= 0) {
            throw new IllegalArgumentException("A valid GIF is required");
        }
        this.gif = gif;
        setBackgroundColor(Color.rgb(9, 14, 29));
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    public void start() {
        firstFrameAtMillis = 0L;
        playing = true;
        invalidate();
    }

    public void stop() {
        playing = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        if (playing) {
            long now = SystemClock.uptimeMillis();
            if (firstFrameAtMillis == 0L) {
                firstFrameAtMillis = now;
            }
            int duration = gif.duration() > 0
                    ? gif.duration() : FALLBACK_DURATION_MILLIS;
            gif.setTime((int) ((now - firstFrameAtMillis) % duration));
        }
        float scale = Math.min(
                getWidth() / (float) gif.width(),
                getHeight() / (float) gif.height());
        float dx = (getWidth() - gif.width() * scale) * .5f;
        float dy = (getHeight() - gif.height() * scale) * .5f;
        int saved = canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale, scale);
        gif.draw(canvas, 0f, 0f);
        canvas.restoreToCount(saved);
        if (playing) {
            postInvalidateDelayed(FRAME_INTERVAL_MILLIS);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }
}
