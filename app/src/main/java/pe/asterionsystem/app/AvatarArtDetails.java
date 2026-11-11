package pe.asterionsystem.app;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

/**
 * Asterion V3: the skin, face, jersey, trousers and shoes are separate
 * painted parts. Their highlights and seams follow existing skeleton bones.
 * Paints, shaders and Path are reused rather than allocated every frame.
 */
public final class AvatarArtDetails {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final LinearGradient jeans=new LinearGradient(0f,-24f,0f,24f,
            new int[]{0xFF1B2B43,0xFF4E5B78,0xFF26364E},null,Shader.TileMode.CLAMP);

    private void fill(int color) {
        paint.setShader(null);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }
    private void line(Canvas c,float x1,float y1,float x2,float y2,int color,float width){
        fill(color);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x1,y1,x2,y2,paint);
    }
    /** Align a garment layer to an animated bone. Caller balances save. */
    private float bone(Canvas c,AvatarKinematics.Point start,AvatarKinematics.Point end){
        c.save();
        c.translate(start.x,start.y);
        float dx=end.x-start.x,dy=end.y-start.y;
        c.rotate((float)Math.toDegrees(Math.atan2(dy,dx)));
        return (float)Math.hypot(dx,dy);
    }

    public void drawDenimLeg(Canvas c,AvatarKinematics.Point hip,
                             AvatarKinematics.Point knee,AvatarKinematics.Point ankle,
                             boolean far){
        seamSegment(c,hip,knee,far?28f:33f,false,far);
        seamSegment(c,knee,ankle,far?22f:24f,true,far);
        c.save();
        c.translate(knee.x,knee.y);
        c.rotate((float)Math.toDegrees(Math.atan2(
                ankle.y-knee.y,ankle.x-knee.x)));
        fill(far?0xFF26374F:0xFF43536C);
        c.drawRoundRect(-9f,-11f,12f,11f,6f,6f,paint);
        line(c,-6f,-7f,6f,-7f,0x998CA1C0,1.1f);
        line(c,-6f,7f,6f,7f,0x998CA1C0,1.1f);
        line(c,1f,-2f,6f,-2f,0x99D5E0EE,.9f);
        c.restore();
    }

    private void seamSegment(Canvas c,AvatarKinematics.Point a,
                             AvatarKinematics.Point b,float width,
                             boolean calf,boolean far){
        float length=bone(c,a,b);
        // Subtle textile shading under the stitched seam.
        fill(0x22344762);
        path.rewind();
        path.moveTo(length*.10f,-width*.35f);
        path.cubicTo(length*.36f,-width*.49f,length*.73f,-width*.39f,
                length*.91f,-width*.25f);
        path.lineTo(length*.91f,-width*.12f);
        path.cubicTo(length*.64f,-width*.24f,length*.32f,-width*.25f,
                length*.10f,-width*.17f);
        path.close();
        paint.setShader(jeans);
        paint.setAlpha(far?85:125);
        c.drawPath(path,paint);
        paint.setShader(null);
        paint.setAlpha(255);
        line(c,length*.10f,width*.23f,length*.92f,width*.17f,
                far?0x55899AB4:0xAA8DA3C2,1.3f);
        line(c,length*.20f,width*.27f,length*.58f,width*.21f,
                0x66869AB7,.6f);
        if(calf) {
            fill(0xFF172840);
            c.drawRoundRect(length*.84f,-width*.32f,length*.99f,
                    width*.31f,3f,3f,paint);
            line(c,length*.88f,width*.19f,length*.96f,width*.19f,
                    0xCC92A8C8,1.1f);
        } else {
            line(c,length*.33f,-width*.09f,length*.49f,-width*.09f,
                    0x8899A9BF,1f);
        }
        c.restore();
    }

    public void drawPelvis(Canvas c,AvatarKinematics.Point hip,
                           AvatarKinematics.Point shoulder){
        bone(c,hip,shoulder);
        fill(0xFF273A57);
        path.rewind();
        path.moveTo(-16f,-20f);
        path.cubicTo(-12f,-24f,12f,-23f,17f,-18f);
        path.lineTo(17f,19f);
        path.cubicTo(8f,23f,-10f,22f,-16f,18f);
        path.close();c.drawPath(path,paint);
        fill(0xFF1B2A43);
        c.drawRoundRect(5f,-21f,14f,21f,3f,3f,paint);
        line(c,9f,-16f,9f,17f,0xAA9DAFC7,1f);
        line(c,-11f,-12f,0f,-16f,0x8899BCD8,1.25f);
        line(c,-11f,12f,0f,16f,0x8899BCD8,1.25f);
        fill(0xFFA7B6C8);
        c.drawRoundRect(6f,-3f,11f,5f,1.5f,1.5f,paint);
        line(c,-9f,-13f,-5f,-7f,0xBB8EA6C4,1.25f);
        line(c,-9f,13f,-5f,7f,0xBB8EA6C4,1.25f);
        c.restore();
    }

    /** Minimal technical fabric grain and seams inside the organic torso. */
    public void drawJersey(Canvas c,AvatarKinematics.Point hip,
                           AvatarKinematics.Point shoulder,
                           AvatarBodySpec body,AvatarPalette palette){
        float length=bone(c,hip,shoulder);
        fill(0x55375B91);
        path.rewind();
        path.moveTo(length*.09f,-body.waistHalfWidth*.55f);
        path.cubicTo(length*.4f,-body.waistHalfWidth*.61f,
                length*.72f,-body.chestHalfWidth*.58f,
                length*.94f,-body.shoulderHalfWidth*.58f);
        path.lineTo(length*.90f,-body.shoulderHalfWidth*.39f);
        path.cubicTo(length*.68f,-body.chestHalfWidth*.39f,
                length*.4f,-body.waistHalfWidth*.32f,
                length*.09f,-body.waistHalfWidth*.26f);
        path.close();c.drawPath(path,paint);
        line(c,length*.12f,body.waistHalfWidth*.63f,
                length*.88f,body.chestHalfWidth*.63f,
                palette.female?0x997C50B9:0xAA93C5ED,1.3f);
        line(c,length*.19f,-body.waistHalfWidth*.43f,
                length*.75f,-body.chestHalfWidth*.49f,
                0x77599BDE,1.5f);
        fill(0x5587C6F5);
        for(int i=0;i<6;i++){
            float x=length*(.29f+i*.09f);
            c.drawCircle(x,4f,.7f,paint);
            c.drawCircle(x+2f,-3f,.55f,paint);
        }
        // A small geometric jersey motif, not the official Asterion mark.
        fill(palette.female?0xFFBDB0EC:0xFFB9DDF7);
        path.rewind();
        path.moveTo(length*.68f,-2f);
        path.lineTo(length*.77f,-8f);
        path.lineTo(length*.86f,-2f);
        path.lineTo(length*.77f,5f);
        path.close();c.drawPath(path,paint);
        fill(palette.top);
        c.drawCircle(length*.77f,-1f,2.5f,paint);
        // Collar fabric, sleeve-bound seam and two restrained sport panels.
        fill(0xCC123666);
        c.drawArc(length-17f,-12f,length+4f,12f,5f,170f,false,paint);
        line(c,length*.12f,-body.waistHalfWidth*.54f,
                length*.23f,-body.waistHalfWidth*.64f,
                0x9099C9F2,1.1f);
        line(c,length*.22f,body.waistHalfWidth*.55f,
                length*.29f,body.waistHalfWidth*.61f,
                0x9074B9EC,.8f);
        c.restore();
    }

    public void drawSleeve(Canvas c,AvatarKinematics.Point shoulder,
                           AvatarKinematics.Point elbow,AvatarPalette palette){
        float length=bone(c,shoulder,elbow);
        float x=length*.29f;
        line(c,x,-10f,x,10f,
                palette.female?0xFFBFA5E7:0xFF9CCBF1,2.2f);
        line(c,length*.14f,-10f,length*.22f,-8f,0x99C4E0F9,1.3f);
        c.restore();
    }

    public void drawForearm(Canvas c,AvatarKinematics.Point elbow,
                            AvatarKinematics.Point wrist){
        float len=bone(c,elbow,wrist);
        line(c,len*.18f,-4f,len*.63f,-3f,0x88FFE1BC,2.1f);
        fill(0xFF28598D);
        c.drawRoundRect(len*.85f,-7f,len*.93f,7f,1f,1f,paint);
        line(c,len*.88f,-5f,len*.88f,5f,0xFF83BDE8,1.1f);
        line(c,len*.89f,-5f,len*.89f,5f,0xFFB6DDF7,.75f);
        c.restore();
    }

    /** Independently designed upper, tongue, toe, three laces and outsole. */
    public void drawSneaker(Canvas c,AvatarKinematics.Point ankle,
                            AvatarPalette palette,boolean far){
        c.save();c.translate(ankle.x,ankle.y);
        path.rewind();
        path.moveTo(-12f,1f);path.lineTo(-9f,-12f);
        path.quadTo(-2f,-15f,8f,-12f);
        path.lineTo(18f,-4f);
        path.quadTo(40f,-2f,42f,6f);
        path.quadTo(44f,11f,38f,12f);
        path.lineTo(-12f,12f);path.close();
        fill(far?0xFFB5C5D8:palette.shoe);
        c.drawPath(path,paint);
        path.rewind();
        path.moveTo(-9f,-9f);path.lineTo(11f,-10f);
        path.lineTo(29f,3f);path.lineTo(-5f,4f);path.close();
        fill(palette.female?0xFF8466B2:0xFF346BA8);
        c.drawPath(path,paint);
        line(c,-6f,-5f,23f,-2f,0xCCDAEEFF,1.2f);
        for(int i=0;i<3;i++){
            float x=-1f+7f*i;
            line(c,x,-5f,x+5f,-2f,0xFFF3F8FF,1.35f);
        }
        // Forefoot overlay, heel counter, lace eyelets and outsole layering.
        fill(0xFFD7E5F1);
        path.rewind();
        path.moveTo(27f,0f);
        path.quadTo(40f,1f,41f,7f);
        path.quadTo(33f,7f,27f,5f);
        path.close();c.drawPath(path,paint);
        fill(0xFF7796B4);
        path.rewind();path.moveTo(-12f,1f);
        path.lineTo(-9f,-10f);path.lineTo(-3f,-11f);
        path.lineTo(-4f,5f);path.close();c.drawPath(path,paint);
        fill(0xFFE6F5FF);
        for(int i=0;i<3;i++){
            c.drawCircle(0f+i*6f,-5f+i*.7f,1f,paint);
        }
        line(c,13f,-7f,24f,0f,0xFFB7D3EA,.9f);
        fill(0xFFA6B8D0);
        c.drawRoundRect(30f,1f,40f,6f,2f,2f,paint);
        fill(palette.shoeDark);
        c.drawRoundRect(-13f,6f,42f,14f,4f,4f,paint);
        line(c,-9f,10f,36f,10f,0xFFECF4FF,2f);
        for(int i=0;i<4;i++)line(c,1f+i*9f,12f,5f+i*9f,12f,0xAA91A4BC,.75f);
        c.restore();
    }

    /** A modest, visible side neck tucked under the jaw and shirt collar.
     * Head geometry and shoulder pivots stay unchanged by this visual layer. */
    public void drawShortNeck(Canvas c,AvatarKinematics.Point shoulder,
                              AvatarKinematics.Point head,AvatarPalette palette){
        if(c==null||shoulder==null||head==null||palette==null)
            throw new IllegalArgumentException("Neck data required");
        float midX=head.x-3f;
        float top=head.y+17f;
        float bottom=shoulder.y+1f;
        if(bottom<=top)return;
        fill(palette.skinShade);
        path.rewind();
        path.moveTo(midX-8f,top);
        path.quadTo(midX-10f,top+8f,midX-9f,bottom);
        path.lineTo(midX+8f,bottom);
        path.quadTo(midX+7f,top+8f,midX+7f,top);
        path.close();c.drawPath(path,paint);
        fill(palette.skin);
        c.drawRoundRect(midX-6f,top,midX+7f,bottom-3f,4f,4f,paint);
        // Fine collar line, not another thick rounded shoulder.
        fill(palette.top);
        c.drawRoundRect(midX-11f,bottom-9f,midX+10f,bottom+1f,5f,5f,paint);
        line(c,midX-8f,bottom-8f,midX+7f,bottom-8f,
                palette.female?0xFFCEB7ED:0xFF9BC7E6,1.2f);
    }

    /**
     * Readable, one-eyed side profile with a distinct nose, jaw, ear,
     * forehead and hairline. The eye count deliberately reflects the camera.
     * These are artistic features, not a biological or biometric model.
     */
    public void drawHead(Canvas c,AvatarKinematics.Point center,
                         AvatarBodySpec body,AvatarPalette palette){
        c.save();c.translate(center.x,center.y);
        c.scale(body.headScale,body.headScale);
        // Hair behind the cheek and neck; remains separate from facial skin.
        fill(palette.hair);
        path.rewind();
        path.moveTo(-16f,-20f);
        path.cubicTo(-23f,-26f,-26f,-8f,-21f,10f);
        path.lineTo(-17f,15f);
        path.quadTo(-23f,-1f,-16f,-20f);
        path.close();c.drawPath(path,paint);

        // Profile instead of a broad oval: brow -> nose -> lips -> jaw.
        path.rewind();
        path.moveTo(-18f,-18f);
        path.cubicTo(-13f,-27f,5f,-29f,15f,-18f);
        path.cubicTo(18f,-14f,18f,-5f,17f,-2f);
        path.quadTo(24f,3f,23f,6f);
        path.quadTo(22f,8f,18f,9f);
        path.quadTo(21f,12f,18f,15f);
        path.quadTo(17f,22f,7f,23f);
        path.cubicTo(-3f,23f,-14f,18f,-17f,9f);
        path.cubicTo(-21f,2f,-21f,-11f,-18f,-18f);
        path.close();
        fill(palette.skin);
        c.drawPath(path,paint);

        // Ear on the near side and subtle jaw modeling.
        fill(palette.skinShade);
        c.drawOval(-19f,-1.5f,-10f,9f,paint);
        fill(palette.skin);
        c.drawOval(-17.1f,.2f,-12.2f,6.4f,paint);
        line(c,-15f,2.7f,-13f,4.6f,0x778F5844,.8f);
        fill(0x25FFE1BC);
        c.drawOval(-3f,5f,14f,16f,paint);
        line(c,-7f,15f,4f,21f,0x45B47759,.95f);

        // Natural, asymmetrical cap of short hair and a swept fringe.
        path.rewind();
        path.moveTo(-20f,-11f);
        path.cubicTo(-24f,-23f,-12f,-32f,1f,-31f);
        path.cubicTo(14f,-33f,24f,-24f,18f,-14f);
        path.quadTo(12f,-17f,8f,-16f);
        path.quadTo(4f,-13f,-2f,-14f);
        path.quadTo(-9f,-11f,-14f,-13f);
        path.close();
        fill(palette.hair);c.drawPath(path,paint);
        line(c,-9f,-25f,3f,-28f,0x665F4942,1.6f);
        line(c,3f,-28f,13f,-23f,0x554A3A35,1.3f);
        if(palette.female){
            fill(palette.hair);
            path.rewind();
            path.moveTo(-19f,-20f);
            path.cubicTo(-32f,-17f,-31f,1f,-26f,14f);
            path.lineTo(-20f,15f);
            path.quadTo(-24f,0f,-19f,-20f);
            path.close();c.drawPath(path,paint);
        }
        // Only the eye facing the camera should be visible in side view.
        line(c,7f,-6f,14f,-7f,0xFF60483F,1.6f);
        fill(0xFF2D353E);
        c.drawOval(10f,-3f,13.4f,1.3f,paint);
        fill(0xEEFFFFFF);
        c.drawCircle(11.6f,-2.1f,.55f,paint);
        // Nose bridge, nostril and restrained expression.
        line(c,18f,0f,21f,4f,0xAAAF765D,.85f);
        fill(0xFF966D59);c.drawCircle(20.2f,6.4f,.7f,paint);
        line(c,10f,14f,16f,14.8f,0xFF975B5D,1.25f);
        line(c,16f,14.8f,18f,12.9f,0xAA975B5D,.85f);
        c.restore();
    }

    public void drawHand(Canvas c,AvatarKinematics.Point wrist,int skin){
        c.save();c.translate(wrist.x,wrist.y);
        path.rewind();path.moveTo(-6f,-5f);
        path.quadTo(1f,-9f,7f,-4f);
        path.lineTo(12f,1f);path.quadTo(13f,5f,6f,8f);
        path.quadTo(-2f,11f,-8f,4f);path.close();
        fill(skin);c.drawPath(path,paint);
        line(c,0f,3f,5f,5f,0x99CA8D6F,.85f);
        fill(skin);c.drawOval(5f,-8f,14f,0f,paint);
        c.restore();
    }
}
