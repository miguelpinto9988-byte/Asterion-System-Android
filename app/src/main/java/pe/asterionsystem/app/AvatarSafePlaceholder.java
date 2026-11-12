package pe.asterionsystem.app;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Lightweight safe-state artwork if an internal rig is mathematically
 * unreachable. Keeps the View free of drawing primitives/allocations and
 * clearly indicates that the animation was not rendered.
 */
public final class AvatarSafePlaceholder {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);

    public void draw(Canvas canvas,boolean dark){
        if(canvas==null)throw new IllegalArgumentException("Canvas required");
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dark?0xFF1C2A40:0xFFF1F5FA);
        canvas.drawRoundRect(24f,30f,376f,446f,17f,17f,paint);
        paint.setColor(dark?0xFFDFE9F7:0xFF344766);
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        canvas.drawText("Vista temporalmente no disponible",52f,231f,paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(12f);
        canvas.drawText("Postura fuera del rango permitido",63f,254f,paint);
    }
}
