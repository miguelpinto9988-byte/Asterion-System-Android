package pe.asterionsystem.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * A continuous two-bone skin envelope, shared by lateral/front/top renderers.
 *
 * The original renderer drew two separate tapered solids plus a circular
 * elbow/knee plug. That plug could protrude outside a flexed joint. This
 * envelope joins both segments at exactly the SAME mathematical hinge, with
 * a limited-width Bézier transition and no independent joint disk.
 *
 * It is a rendering skin, not a new solver and not a clinically validated
 * anatomical model. AvatarRigConstraints still owns the underlying lengths.
 */
public final class AvatarOrganicVolume {
    private static final float EPS=.0001f;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path contour=new Path();

    public static final class JointMetrics {
        public final float upperLength,lowerLength,bendDegrees,bridgeRadius;
        JointMetrics(float upperLength,float lowerLength,
                     float bendDegrees,float bridgeRadius){
            this.upperLength=upperLength;
            this.lowerLength=lowerLength;
            this.bendDegrees=bendDegrees;
            this.bridgeRadius=bridgeRadius;
        }
    }

    public static JointMetrics measure(AvatarKinematics.Point root,
                                      AvatarKinematics.Point joint,
                                      AvatarKinematics.Point end,
                                      float proximalWidth,float jointWidth,
                                      float distalWidth) {
        requirePoint(root);requirePoint(joint);requirePoint(end);
        float upper=root.distance(joint),lower=joint.distance(end);
        if(!Float.isFinite(upper)||!Float.isFinite(lower)||
                upper<2f||lower<2f)
            throw new IllegalArgumentException("A bone cannot collapse");
        width(proximalWidth,upper);
        width(jointWidth,Math.min(upper,lower));
        width(distalWidth,lower);
        float u1x=(joint.x-root.x)/upper,u1y=(joint.y-root.y)/upper;
        float u2x=(end.x-joint.x)/lower,u2y=(end.y-joint.y)/lower;
        float dot=Math.max(-1f,Math.min(1f,u1x*u2x+u1y*u2y));
        float bend=(float)Math.toDegrees(Math.acos(dot));
        // Compact the contour on tight bends. A clamped local radius
        // prevents an elbow 'ball' sticking out of the limb envelope.
        float radius=Math.min(jointWidth*.5f,Math.min(upper,lower)*.24f);
        radius*=1f-.22f*(1f-dot)*.5f;
        return new JointMetrics(upper,lower,bend,radius);
    }

    public void drawChain(Canvas canvas,
                          AvatarKinematics.Point root,
                          AvatarKinematics.Point joint,
                          AvatarKinematics.Point end,
                          float proximalWidth,float jointWidth,
                          float distalWidth,int fill) {
        if(canvas==null)throw new IllegalArgumentException("Canvas required");
        JointMetrics m=measure(root,joint,end,
                proximalWidth,jointWidth,distalWidth);
        float u1x=(joint.x-root.x)/m.upperLength;
        float u1y=(joint.y-root.y)/m.upperLength;
        float u2x=(end.x-joint.x)/m.lowerLength;
        float u2y=(end.y-joint.y)/m.lowerLength;
        float n1x=-u1y,n1y=u1x,n2x=-u2y,n2y=u2x;
        float mx=n1x+n2x,my=n1y+n2y;
        float ml=(float)Math.hypot(mx,my);
        // At a folded limb the bisector is undefined. Use the incoming
        // normal; do not extrapolate any sharp miter or detached sphere.
        if(ml<.18f){mx=n1x;my=n1y;}
        else {mx/=ml;my/=ml;}
        float ra=proximalWidth*.5f,rb=distalWidth*.5f;
        float r=m.bridgeRadius;
        // Round the bend over a *finite arc* rather than placing one
        // Bézier endpoint at the outside of the elbow. A single endpoint
        // there made a visible triangular tip in highly flexed poses.
        float fillet1=Math.min(m.upperLength*.31f,jointWidth*.98f);
        float fillet2=Math.min(m.lowerLength*.31f,jointWidth*.98f);
        float preLx=joint.x-u1x*fillet1+n1x*r;
        float preLy=joint.y-u1y*fillet1+n1y*r;
        float postLx=joint.x+u2x*fillet2+n2x*r;
        float postLy=joint.y+u2y*fillet2+n2y*r;
        float preRx=joint.x-u1x*fillet1-n1x*r;
        float preRy=joint.y-u1y*fillet1-n1y*r;
        float postRx=joint.x+u2x*fillet2-n2x*r;
        float postRy=joint.y+u2y*fillet2-n2y*r;

        contour.rewind();
        contour.moveTo(root.x+n1x*ra,root.y+n1y*ra);
        contour.cubicTo(
                root.x+u1x*m.upperLength*.29f+n1x*ra,
                root.y+u1y*m.upperLength*.29f+n1y*ra,
                preLx-u1x*fillet1*.42f,
                preLy-u1y*fillet1*.42f,
                preLx,preLy);
        contour.quadTo(joint.x+mx*r*.94f,joint.y+my*r*.94f,
                postLx,postLy);
        contour.cubicTo(
                postLx+u2x*fillet2*.44f,
                postLy+u2y*fillet2*.44f,
                end.x-u2x*m.lowerLength*.25f+n2x*rb,
                end.y-u2y*m.lowerLength*.25f+n2y*rb,
                end.x+n2x*rb,end.y+n2y*rb);
        contour.cubicTo(
                end.x+u2x*rb*.55f+n2x*rb*.65f,
                end.y+u2y*rb*.55f+n2y*rb*.65f,
                end.x+u2x*rb*.55f-n2x*rb*.65f,
                end.y+u2y*rb*.55f-n2y*rb*.65f,
                end.x-n2x*rb,end.y-n2y*rb);
        contour.cubicTo(
                end.x-u2x*m.lowerLength*.25f-n2x*rb,
                end.y-u2y*m.lowerLength*.25f-n2y*rb,
                postRx+u2x*fillet2*.44f,
                postRy+u2y*fillet2*.44f,
                postRx,postRy);
        contour.quadTo(joint.x-mx*r*.94f,joint.y-my*r*.94f,
                preRx,preRy);
        contour.cubicTo(
                preRx-u1x*fillet1*.42f,
                preRy-u1y*fillet1*.42f,
                root.x+u1x*m.upperLength*.29f-n1x*ra,
                root.y+u1y*m.upperLength*.29f-n1y*ra,
                root.x-n1x*ra,root.y-n1y*ra);
        contour.cubicTo(
                root.x-u1x*ra*.50f-n1x*ra*.65f,
                root.y-u1y*ra*.50f-n1y*ra*.65f,
                root.x-u1x*ra*.50f+n1x*ra*.65f,
                root.y-u1y*ra*.50f+n1y*ra*.65f,
                root.x+n1x*ra,root.y+n1y*ra);
        contour.close();
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fill);
        paint.setAlpha(255);
        canvas.drawPath(contour,paint);
        // A deeply folded joint can cause the inner contour to overlap
        // itself, briefly leaving a pixel-sized gap at the mathematical
        // hinge. Seal only that INTERIOR seam with a short rounded ribbon,
        // smaller than the skin radius. This is not a separate elbow disk.
        if(m.bendDegrees>88f){
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(Math.min(m.bridgeRadius*1.32f,10.5f));
            canvas.drawLine(joint.x-u1x*2.2f,joint.y-u1y*2.2f,
                    joint.x+u2x*2.2f,joint.y+u2y*2.2f,paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private static void requirePoint(AvatarKinematics.Point p){
        if(p==null||!Float.isFinite(p.x)||!Float.isFinite(p.y))
            throw new IllegalArgumentException("Joint coordinates must be finite");
    }
    private static void width(float value,float segmentLength){
        if(!Float.isFinite(value)||value<=0f||
                value>Math.max(24f,segmentLength*1.25f))
            throw new IllegalArgumentException("Unsupported limb envelope width");
    }
}
