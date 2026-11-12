package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

public final class AvatarVariantSceneTest {
    private static final AvatarBodySpec BODY=AvatarBodySpec.DEFAULT;
    private static AvatarKinematics.Point[] joints(AvatarPose p){
        return new AvatarKinematics.Point[]{p.hip,p.farHip,p.shoulder,p.farShoulder,
                p.knee,p.farKnee,p.ankle,p.farAnkle,p.elbow,p.farElbow,p.wrist,p.farWrist,p.neck,p.head};
    }
    @Test public void everyAuthoredCyclePreservesRigContactsAndContinuity(){
        assertEquals(51,AvatarVariantScene.all().size());
        for(AvatarVariantScene.Recipe r:AvatarVariantScene.all().values()){
            AvatarVariantScene.Frame start=AvatarVariantScene.at(r,0,BODY),last=start;
            float travelled=0f;
            for(int i=0;i<=600;i++){
                AvatarVariantScene.Frame frame=AvatarVariantScene.at(r,i/600f,BODY);
                AvatarPose p=frame.pose;
                AvatarMotionEngine.assertSkeleton(p,BODY,.02f);
                AvatarPoseGuard.requireRig(AvatarSkeletonFrame.fromSide(p,BODY));
                AvatarKinematics.Point[] now=joints(p),before=joints(last.pose);
                for(int j=0;j<now.length;j++){
                    assertTrue(r.id+" finite",Float.isFinite(now[j].x)&&Float.isFinite(now[j].y));
                    assertTrue(r.id+" continuity",now[j].distance(before[j])<4f);
                    travelled+=now[j].distance(before[j]);
                    assertTrue(r.id+" viewport x="+now[j].x,now[j].x>-15&&now[j].x<430);
                    assertTrue(r.id+" viewport y="+now[j].y,now[j].y>-30&&now[j].y<420);
                }
                if(r.posture==AvatarVariantScene.Posture.STANDING){
                    assertEquals(r.id,0f,p.ankle.distance(start.pose.ankle),.001f);
                    assertEquals(r.id,0f,p.farAnkle.distance(start.pose.farAnkle),.001f);
                }else{
                    assertEquals(r.id,0f,p.hip.distance(start.pose.hip),.001f);
                    assertEquals(r.id,0f,p.farHip.distance(start.pose.farHip),.001f);
                    if(r.movement==AvatarVariantScene.Movement.HEEL_SLIDE){
                        assertEquals(start.pose.ankle.y,p.ankle.y,.001f);
                        assertEquals(start.pose.farAnkle.y,p.farAnkle.y,.001f);
                    }
                }
                last=frame;
            }
            assertTrue(r.id+" actually moves",travelled>10f);
            AvatarKinematics.Point[] a=joints(start.pose),z=joints(last.pose);
            for(int j=0;j<a.length;j++)assertEquals(r.id+" closed loop",0,a[j].distance(z[j]),.001f);
        }
    }
    @Test public void alternatingSidesNeverExecuteSimultaneously(){
        for(AvatarVariantScene.Recipe r:AvatarVariantScene.all().values()){
            if(r.pattern!=AvatarVariantScene.Pattern.ALTERNATING)continue;
            for(int i=0;i<=600;i++){
                float phase=i/600f;
                assertEquals(r.id,0f,AvatarVariantScene.sideWeight(r,phase,false)*
                        AvatarVariantScene.sideWeight(r,phase,true),.00001f);
            }
            assertEquals(1f,AvatarVariantScene.sideWeight(r,.25f,false),.001f);
            assertEquals(1f,AvatarVariantScene.sideWeight(r,.75f,true),.001f);
        }
    }
    @Test public void distinctRangesAndPausedVariantsHaveDistinctExecutions(){
        AvatarPose small=AvatarVariantScene.at(AvatarVariantScene.find("extension_rodilla_sentado_corta_v01"),.5f,BODY).pose;
        AvatarPose large=AvatarVariantScene.at(AvatarVariantScene.find("extension_rodilla_sentado_corta_v02"),.5f,BODY).pose;
        assertTrue(large.ankle.distance(small.ankle)>35f);
        AvatarVariantScene.Recipe paused=AvatarVariantScene.find("zancada_estatica_v04");
        assertEquals(0,AvatarVariantScene.at(paused,.30f,BODY).pose.hip.distance(
                AvatarVariantScene.at(paused,.70f,BODY).pose.hip),.001f);
        assertNull(AvatarVariantScene.find("not_a_real_variant"));
        assertNull(AvatarVariantScene.find("curl_biceps_v05")); // Inclined bench is not a chair.
    }
    @Test public void unsupportedPhasesAreRejected(){
        AvatarVariantScene.Recipe recipe=AvatarVariantScene.all().values().iterator().next();
        for(float phase:new float[]{-1f,1.01f,Float.NaN,Float.POSITIVE_INFINITY}){
            assertThrows(IllegalArgumentException.class,()->AvatarVariantScene.at(recipe,phase,BODY));
        }
    }
}
