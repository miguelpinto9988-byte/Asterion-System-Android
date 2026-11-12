package pe.asterionsystem.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Shared continuous organic silhouette for every 2D camera.
 *
 * The endpoints are *only* skeleton joints. Curved bone caps remain inside
 * each joint neighborhood and never extend to an unrelated screen pixel.
 * This prevents the former protruding elbow/knee spheres and hard cylinder
 * seams. It is intentionally a lightweight 2D rendering constraint, not
 * a physically accurate skin-deformation solver.
 */
public final class AvatarSoftContour {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();

    /** width is full rendered diameter, not radius. */
    public void segment(Canvas canvas,AvatarKinematics.Point start,
                        AvatarKinematics.Point end,float startWidth,
                        float endWidth,int argb) {
        validPoint(start);validPoint(end);
        validWidth(startWidth);validWidth(endWidth);
        if(canvas==null)throw new IllegalArgumentException("Canvas required");
        float dx=end.x-start.x,dy=end.y-start.y;
        float length=(float)Math.hypot(dx,dy);
        if(!Float.isFinite(length)||length<.25f)
            throw new IllegalArgumentException("Collapsed rendered bone");
        float ux=dx/length,uy=dy/length;
        float nx=-uy,ny=ux;

        // Earlier caps extended 40% of the bone width beyond each joint.
        // Bound their bulge to <= 16% of width and <= 9% of bone length.
        float rear=Math.min(startWidth*.16f,length*.09f);
        float tip=Math.min(endWidth*.16f,length*.09f);
        float ax=start.x+nx*startWidth*.5f;
        float ay=start.y+ny*startWidth*.5f;
        float bx=end.x+nx*endWidth*.5f;
        float by=end.y+ny*endWidth*.5f;
        float cx=end.x-nx*endWidth*.5f;
        float cy=end.y-ny*endWidth*.5f;
        float ex=start.x-nx*startWidth*.5f;
        float ey=start.y-ny*startWidth*.5f;

        path.rewind();
        path.moveTo(ax,ay);
        path.cubicTo(start.x+dx*.30f+nx*startWidth*.50f,
                start.y+dy*.30f+ny*startWidth*.50f,
                end.x-dx*.28f+nx*endWidth*.50f,
                end.y-dy*.28f+ny*endWidth*.50f,bx,by);
        path.cubicTo(bx+ux*tip*.90f,by+uy*tip*.90f,
                cx+ux*tip*.90f,cy+uy*tip*.90f,cx,cy);
        path.cubicTo(end.x-dx*.28f-nx*endWidth*.50f,
                end.y-dy*.28f-ny*endWidth*.50f,
                start.x+dx*.30f-nx*startWidth*.50f,
                start.y+dy*.30f-ny*startWidth*.50f,ex,ey);
        path.cubicTo(ex-ux*rear*.90f,ey-uy*rear*.90f,
                ax-ux*rear*.90f,ay-uy*rear*.90f,ax,ay);
        path.close();
        prepare(argb);
        canvas.drawPath(path,paint);
    }

    /**
     * Inboard joint bridge: a small matched-color oval, smaller than both
     * adjacent bone widths. It fills the bend without painting a large
     * independent ball at the elbow or knee. No stroke/outline separator.
     */
    public void hinge(Canvas canvas,AvatarKinematics.Point center,
                      float proximalWidth,float distalWidth,int argb) {
        validPoint(center);
        validWidth(proximalWidth);validWidth(distalWidth);
        if(canvas==null)throw new IllegalArgumentException("Canvas required");
        float r=Math.min(proximalWidth,distalWidth)*.32f;
        prepare(argb);
        canvas.drawOval(center.x-r,center.y-r*.89f,
                center.x+r,center.y+r*.89f,paint);
    }

    private void prepare(int argb){
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        paint.setColor(argb);
    }
    private static void validWidth(float w) {
        if(!Float.isFinite(w)||w<=0f||w>150f)
            throw new IllegalArgumentException("Invalid bone silhouette width");
    }
    private static void validPoint(AvatarKinematics.Point p){
        if(p==null||!Float.isFinite(p.x)||!Float.isFinite(p.y)||
                Math.abs(p.x)>4096f||Math.abs(p.y)>4096f)
            throw new IllegalArgumentException("Invalid bone anchor");
    }
}
