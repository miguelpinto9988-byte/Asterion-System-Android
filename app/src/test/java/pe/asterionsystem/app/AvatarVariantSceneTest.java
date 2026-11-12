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
        assertEquals(111,AvatarVariantScene.all().size());
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
                    boolean movingFoot=r.movement==AvatarVariantScene.Movement.CALF_RAISE||
                            r.movement==AvatarVariantScene.Movement.CALF_HOLD||
                            r.movement==AvatarVariantScene.Movement.STEP_GROUND||
                            r.movement==AvatarVariantScene.Movement.STEP_PLATFORM||
                            r.movement==AvatarVariantScene.Movement.CLAP_STEP||
                            r.movement==AvatarVariantScene.Movement.WALK_STEPS||
                            r.movement==AvatarVariantScene.Movement.WALK_ARMS||
                            r.movement==AvatarVariantScene.Movement.STANDING_LEG_CURL||
                            r.movement==AvatarVariantScene.Movement.MARCH_STABLE||
                            r.movement==AvatarVariantScene.Movement.MARCH_KNEES||
                            r.movement==AvatarVariantScene.Movement.MARCH_ARMS||
                            r.movement==AvatarVariantScene.Movement.HEEL_TAP||
                            r.movement==AvatarVariantScene.Movement.HEEL_TAP_ARMS||
                            r.movement==AvatarVariantScene.Movement.BACK_STEP;
                    if(movingFoot){
                        assertTrue(r.id+" near foot never passes through ground",
                                p.ankle.y<=start.pose.ankle.y+.001f);
                        assertTrue(r.id+" far foot never passes through ground",
                                p.farAnkle.y<=start.pose.farAnkle.y+.001f);
                        if(r.pattern==AvatarVariantScene.Pattern.NEAR)
                            assertEquals(r.id,0f,p.farAnkle.distance(start.pose.farAnkle),.001f);
                    }else{
                        assertEquals(r.id,0f,p.ankle.distance(start.pose.ankle),.001f);
                        assertEquals(r.id,0f,p.farAnkle.distance(start.pose.farAnkle),.001f);
                    }
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
    @Test public void firstBatchContainsExactlyThirtyNewIDsAndDifferentContactTypes(){
        String[] ids={"curl_martillo_v01","curl_martillo_v02","curl_martillo_v03",
                "buenos_dias_v03","peso_muerto_v03","peso_muerto_v04","peso_muerto_v06",
                "extension_cuadriceps_v02","flexion_rodilla_bipeda_v01","flexion_rodilla_bipeda_v02",
                "alcance_pared_brazo_v01","alcance_pared_brazo_v02",
                "deslizamiento_manos_vertical_v01","deslizamiento_manos_vertical_v02",
                "apoyo_manos_pared_v01","apoyo_manos_pared_v02",
                "marcha_estable_tronco_v01","marcha_estable_tronco_v02",
                "alternancia_hombros_pie_v01","alternancia_hombros_pie_v02",
                "elevaciones_rodillas_cardio_v01","elevaciones_rodillas_cardio_v03",
                "talones_alternos_v01","talones_alternos_v03",
                "brazos_ritmicos_caminando_v01","brazos_ritmicos_caminando_v02",
                "paso_atras_bajoimpacto_v01","paso_atras_bajoimpacto_v02",
                "remo_banda_pecho_v01","remo_banda_pecho_v02"};
        assertEquals(30,ids.length);
        for(String id:ids)assertNotNull(id,AvatarVariantScene.find(id));
        AvatarVariantScene.Recipe wall=AvatarVariantScene.find("deslizamiento_manos_vertical_v02");
        for(float phase:new float[]{0f,.25f,.5f,.75f,1f}){
            AvatarPose p=AvatarVariantScene.at(wall,phase,BODY).pose;
            assertEquals(AvatarVariantScene.WALL_X,p.wrist.x,.001f);
            assertEquals(AvatarVariantScene.WALL_X,p.farWrist.x,.001f);
        }
    }
    @Test public void secondBatchContainsThirtySpecificRecipesAndContacts(){
        String[] ids={"elevacion_gemelos_v02","elevacion_gemelos_v05",
                "dorsiflexion_tibial_v02","talones_estaticos_v03",
                "dorsiflexion_tobillo_v02","dorsiflexion_tobillo_v03",
                "dorsiflexion_tobillo_v04","flexion_rodilla_sin_carga_pie_v01",
                "flexion_rodilla_sin_carga_pie_v02","step_cardio_v01",
                "step_cardio_v02","step_cardio_v04","talones_alternos_v02",
                "elevaciones_rodillas_cardio_v02","marcha_pasitos_sitio_v01",
                "marcha_pasitos_sitio_v02","marcha_lenta_brazos_v01",
                "marcha_lenta_brazos_v02","marcha_v01",
                "palmada_paso_controlado_v01","palmada_paso_controlado_v02",
                "press_hombros_v01","press_hombros_v02","press_hombros_v03",
                "press_pecho_v01","press_banda_pie_v01","press_banda_pie_v02",
                "press_banda_pie_v03","press_neutro_mancuernas_v01","remo_v05"};
        assertEquals(30,ids.length);
        java.util.Set<String> unique=new java.util.HashSet<>();
        for(String id:ids){assertTrue(id,unique.add(id));assertNotNull(AvatarVariantScene.find(id));}
        AvatarVariantScene.Recipe platform=AvatarVariantScene.find("step_cardio_v02");
        AvatarPose peak=AvatarVariantScene.at(platform,.25f,BODY).pose;
        AvatarPose start=AvatarVariantScene.at(platform,0f,BODY).pose;
        assertTrue("Foot reaches raised step",peak.ankle.y<start.ankle.y);
        AvatarVariantScene.Recipe chair=AvatarVariantScene.find("marcha_v01");
        assertEquals(246f,AvatarVariantScene.at(chair,.25f,BODY).pose.wrist.x,.01f);
        AvatarVariantScene.Recipe clap=AvatarVariantScene.find("palmada_paso_controlado_v02");
        AvatarPose contact=AvatarVariantScene.at(clap,.25f,BODY).pose;
        assertEquals(0f,contact.wrist.distance(contact.farWrist),.01f);
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
