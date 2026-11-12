package pe.asterionsystem.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

/** Shares the existing clothed avatar renderer; props track authored hand/foot anchors. */
public final class AvatarVariantSceneRenderer {
    private final AvatarRenderer avatar=new AvatarRenderer();
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private void color(int color){paint.setColor(color);paint.setStyle(Paint.Style.FILL);}
    private void line(Canvas c,float x,float y,float x2,float y2,float width,int color){
        color(color);paint.setStrokeWidth(width);paint.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x,y,x2,y2,paint);
    }
    private void rect(Canvas c,float x,float y,float x2,float y2,int color){
        color(color);c.drawRoundRect(x,y,x2,y2,4,4,paint);
    }
    private void dumbbell(Canvas c,AvatarKinematics.Point p,int color){
        line(c,p.x-14,p.y,p.x+14,p.y,5,color);
        rect(c,p.x-18,p.y-10,p.x-11,p.y+10,color);
        rect(c,p.x+11,p.y-10,p.x+18,p.y+10,color);
    }
    private void band(Canvas c,AvatarKinematics.Point hand,AvatarKinematics.Point anchor){
        line(c,anchor.x,anchor.y,hand.x,hand.y,3,0xFFB57ADB);
    }
    public void draw(Canvas c,AvatarVariantScene.Frame frame,AvatarBodySpec b,
            AvatarPalette palette,boolean dark){
        AvatarVariantScene.Recipe r=frame.recipe;AvatarPose p=frame.pose;
        rect(c,10,8,390,468,dark?0xFF151923:0xFFF8F9FD);
        int metal=dark?0xFFBCCBDD:0xFF52667D;
        // Fixed framing across all phases, around the grounded outsole.
        c.save();c.translate(40,42);c.scale(.80f,.80f);
        line(c,5,frame.floorY,405,frame.floorY,3,dark?0xFF607083:0xFFC5CEDB);
        if(r.prop==AvatarVariantScene.Prop.WALL){
            line(c,AvatarVariantScene.WALL_X,20,AvatarVariantScene.WALL_X,frame.floorY,
                    7,metal);
        }
        if(r.prop==AvatarVariantScene.Prop.STEP){
            rect(c,207,frame.floorY-18,278,frame.floorY+8,metal);
            rect(c,216,frame.floorY+8,226,frame.floorY+18,metal);
        }
        if(r.prop==AvatarVariantScene.Prop.CHAIR_SUPPORT){
            rect(c,232,128,267,139,metal);
            rect(c,235,138,241,frame.floorY,metal);
            rect(c,258,138,264,frame.floorY,metal);
        }
        if(r.posture==AvatarVariantScene.Posture.SEATED){
            rect(c,112,222,235,234,metal);
            rect(c,111,124,123,232,metal);
            rect(c,115,230,123,frame.floorY,metal);
            rect(c,221,230,229,frame.floorY,metal);
        }
        if(r.prop==AvatarVariantScene.Prop.BAND_ANCHOR){
            line(c,390,90,390,frame.floorY,8,metal);
            band(c,p.farWrist,new AvatarKinematics.Point(390,frame.pose.farShoulder.y+20));
            band(c,p.wrist,new AvatarKinematics.Point(390,frame.pose.shoulder.y+20));
        }else if(r.prop==AvatarVariantScene.Prop.BAND_FEET){
            band(c,p.farWrist,new AvatarKinematics.Point(frame.farRestFoot.x+10,frame.farRestFoot.y+14));
            band(c,p.wrist,new AvatarKinematics.Point(frame.nearRestFoot.x+10,frame.nearRestFoot.y+14));
        }else if(r.prop==AvatarVariantScene.Prop.BAND_SHOULDERS){
            band(c,p.shoulder,new AvatarKinematics.Point(frame.nearRestFoot.x,
                    frame.nearRestFoot.y+10f));
            band(c,p.farShoulder,new AvatarKinematics.Point(frame.farRestFoot.x,
                    frame.farRestFoot.y+10f));
        }else if(r.prop==AvatarVariantScene.Prop.BAND_BACK){
            AvatarKinematics.Point anchor=new AvatarKinematics.Point(
                    p.shoulder.x-55,p.shoulder.y+24);
            band(c,p.wrist,anchor);band(c,p.farWrist,anchor);
        }else if(r.prop==AvatarVariantScene.Prop.BAND_KNEE){
            line(c,272,p.knee.y-12,272,frame.floorY,5,metal);
            band(c,p.knee,new AvatarKinematics.Point(272,p.knee.y));
        }else if(r.prop==AvatarVariantScene.Prop.TOWEL){
            rect(c,p.ankle.x-16,frame.floorY-2,p.ankle.x+44,frame.floorY+3,0xFFA0C7CA);
            rect(c,p.farAnkle.x-16,frame.floorY-3,p.farAnkle.x+44,frame.floorY+2,0xFFA0C7CA);
        }
        avatar.drawFigure(c,p,b,palette);
        // Foot cues accompany, but do not replace, the dressed articulated rig.
        if(r.movement==AvatarVariantScene.Movement.CALF_RAISE||
                r.movement==AvatarVariantScene.Movement.CALF_HOLD){
            line(c,p.ankle.x,p.ankle.y+9,p.ankle.x+25,frame.floorY,4,metal);
            line(c,p.farAnkle.x,p.farAnkle.y+9,p.farAnkle.x+25,frame.floorY,4,metal);
        }
        if(r.movement==AvatarVariantScene.Movement.TOE_LIFT){
            float n=AvatarVariantScene.sideWeight(r,frame.phase,false);
            float f=AvatarVariantScene.sideWeight(r,frame.phase,true);
            line(c,p.ankle.x,p.ankle.y+12,p.ankle.x+27,
                    p.ankle.y+12-r.amplitude*n,4,metal);
            line(c,p.farAnkle.x,p.farAnkle.y+12,p.farAnkle.x+27,
                    p.farAnkle.y+12-r.amplitude*f,4,metal);
        }
        switch(r.prop){
            case HAMMER_DUMBBELLS:
                c.save();c.rotate(90,p.farWrist.x,p.farWrist.y);
                dumbbell(c,p.farWrist,metal);c.restore();
                c.save();c.rotate(90,p.wrist.x,p.wrist.y);
                dumbbell(c,p.wrist,metal);c.restore();break;
            case KETTLEBELL:
                color(metal);
                c.drawOval(p.wrist.x-16,p.wrist.y+8,p.wrist.x+16,p.wrist.y+36,paint);
                paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(4);
                c.drawRoundRect(p.wrist.x-9,p.wrist.y+1,p.wrist.x+9,p.wrist.y+17,6,6,paint);
                paint.setStyle(Paint.Style.FILL);break;
            case DUMBBELLS:case FRONT_DUMBBELLS:
                dumbbell(c,p.farWrist,metal);dumbbell(c,p.wrist,metal);break;
            case GOBLET:
                // One vertical dumbbell held close to the chest with both hands.
                c.save();c.rotate(90,p.wrist.x,p.wrist.y);dumbbell(c,p.wrist,metal);c.restore();break;
            case BAR:case BAR_FRONT:case BAR_BACK:
                line(c,p.farWrist.x-22,p.farWrist.y,p.wrist.x+22,p.wrist.y,5,metal);
                rect(c,p.farWrist.x-28,p.farWrist.y-20,p.farWrist.x-20,p.farWrist.y+20,metal);
                rect(c,p.wrist.x+20,p.wrist.y-20,p.wrist.x+28,p.wrist.y+20,metal);break;
            default:break;
        }
        c.restore();
        color(dark?0xFFCBD9E7:0xFF4F6B89);paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(11);
        String label=r.label;
        // Split instead of silently truncating long exercise names.
        int split=label.length()>47?label.lastIndexOf(' ',47):-1;
        if(split>0){c.drawText(label.substring(0,split),25,415,paint);
            c.drawText(label.substring(split+1),25,430,paint);}
        else c.drawText(label,25,430,paint);
        paint.setTextSize(10);c.drawText(r.phaseLabel(frame.phase),25,446,paint);
        if(r.supervision)c.drawText("Orientación supervisada recomendada",25,460,paint);
    }
}
