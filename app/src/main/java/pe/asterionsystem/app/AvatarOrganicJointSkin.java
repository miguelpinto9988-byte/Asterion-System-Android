package pe.asterionsystem.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * One soft silhouette shared by the side, front and overhead cameras.
 * A joint is a continuous curved bridge between two articulated segments,
 * never a detached circular bead or a rectangular plate.
 *
 * Widths are full rendered widths (Canvas units), not radii.
 * The bridge is bounded to the bone centerlines and cannot drift.
 */
public final class AvatarOrganicJointSkin {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();

    public void draw(Canvas canvas,AvatarKinematics.Point root,
                     AvatarKinematics.Point hinge,AvatarKinematics.Point tip,
                     float incomingWidth,float outgoingWidth,int fill){
        if(canvas==null)throw new IllegalArgumentException("Canvas required");
        AvatarPoseGuard.requireLink(root,hinge);
        AvatarPoseGuard.requireLink(hinge,tip);
        AvatarPoseGuard.requireWidth(incomingWidth);
        AvatarPoseGuard.requireWidth(outgoingWidth);

        float ux=(hinge.x-root.x)/root.distance(hinge);
        float uy=(hinge.y-root.y)/root.distance(hinge);
        float vx=(tip.x-hinge.x)/hinge.distance(tip);
        float vy=(tip.y-hinge.y)/hinge.distance(tip);
        float in=Math.min(incomingWidth*.27f,root.distance(hinge)*.24f);
        float out=Math.min(outgoingWidth*.27f,hinge.distance(tip)*.24f);
        // Smooth, small overlap. Endpoints remain inside the existing limb
        // silhouettes; the joint cannot float outside its actual hinge.
        float nx=-uy,ny=ux,mx=-vy,my=vx;
        float aX=hinge.x-ux*in,aY=hinge.y-uy*in;
        float bX=hinge.x+vx*out,bY=hinge.y+vy*out;
        float wa=incomingWidth*.43f,wb=outgoingWidth*.43f;
        path.rewind();
        path.moveTo(aX+nx*wa,aY+ny*wa);
        path.cubicTo(hinge.x+nx*wa*.72f,hinge.y+ny*wa*.72f,
                hinge.x+mx*wb*.72f,hinge.y+my*wb*.72f,
                bX+mx*wb,bY+my*wb);
        path.quadTo(bX+vx*out*.12f,bY+vy*out*.12f,
                bX-mx*wb,bY-my*wb);
        path.cubicTo(hinge.x-mx*wb*.72f,hinge.y-my*wb*.72f,
                hinge.x-nx*wa*.72f,hinge.y-ny*wa*.72f,
                aX-nx*wa,aY-ny*wa);
        path.quadTo(aX-ux*in*.12f,aY-uy*in*.12f,
                aX+nx*wa,aY+ny*wa);
        path.close();
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fill);
        canvas.drawPath(path,paint);
    }
}
