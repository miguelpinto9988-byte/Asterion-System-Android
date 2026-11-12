package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Mat/chair scenes have independent support constraints and rigid bones. */
public final class AvatarDedicatedSceneTest {
    private static final AvatarBodySpec BODY=AvatarBodySpec.DEFAULT;
    private static final float EPS=.16f;
    @Test public void threeScenesMaintainEveryBoneAcross301FramesAndLoop(){
        for(AvatarDedicatedScene.Kind kind:AvatarDedicatedScene.Kind.values()){
            AvatarDedicatedScene start=AvatarDedicatedScene.at(kind,0f,BODY);
            AvatarDedicatedScene previous=null;
            for(int n=0;n<=300;n++){
                AvatarDedicatedScene scene=AvatarDedicatedScene.at(kind,n/300f,BODY);
                AvatarMotionEngine.assertSkeleton(scene.pose,BODY,EPS);
                AvatarPoseGuard.requireRig(AvatarSkeletonFrame.fromSide(scene.pose,BODY));
                if(previous!=null){
                    assertTrue(kind+" torso jump",
                            scene.pose.hip.distance(previous.pose.hip)<4f);
                    assertTrue(kind+" knee jump",
                            scene.pose.knee.distance(previous.pose.knee)<4f);
                    assertTrue(kind+" ankle jump",
                            scene.pose.ankle.distance(previous.pose.ankle)<4f);
                }
                previous=scene;
            }
            assertNotNull(previous);
            assertEquals(0f,start.pose.hip.distance(previous.pose.hip),EPS);
            assertEquals(0f,start.pose.knee.distance(previous.pose.knee),EPS);
            assertEquals(0f,start.pose.ankle.distance(previous.pose.ankle),EPS);
            assertEquals(0f,start.pose.shoulder.distance(previous.pose.shoulder),EPS);
        }
    }
    @Test public void shortAndFullBridgeShareFixedShouldersHeadAndTwoSoles(){
        AvatarDedicatedScene shortPeak=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_SHORT,.49f,BODY);
        AvatarDedicatedScene fullPeak=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_BASIC,.49f,BODY);
        AvatarDedicatedScene base=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_BASIC,0f,BODY);
        assertTrue(shortPeak.floorBackContact);
        assertFalse(shortPeak.chairSeatContact);
        assertTrue(base.pose.hip.y-shortPeak.pose.hip.y>20f);
        assertTrue(base.pose.hip.y-fullPeak.pose.hip.y>40f);
        assertTrue(fullPeak.pose.hip.y<shortPeak.pose.hip.y-18f);
        for(AvatarDedicatedScene.Kind kind:new AvatarDedicatedScene.Kind[]{
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_SHORT,
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_BASIC}){
            for(int n=0;n<=60;n++){
                AvatarDedicatedScene p=AvatarDedicatedScene.at(kind,n/60f,BODY);
                assertEquals(0f,base.pose.head.distance(p.pose.head),EPS);
                assertEquals(0f,base.pose.shoulder.distance(p.pose.shoulder),EPS);
                assertEquals(0f,base.pose.ankle.distance(p.pose.ankle),EPS);
                assertEquals(0f,base.pose.farAnkle.distance(p.pose.farAnkle),EPS);
            }
        }
    }
    @Test public void chairKneeExtensionPinsSeatKneeAndSupportFoot(){
        AvatarDedicatedScene start=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION,0f,BODY);
        AvatarDedicatedScene peak=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION,.49f,BODY);
        assertTrue(start.chairSeatContact);
        assertFalse(start.floorBackContact);
        assertEquals(0f,start.pose.hip.distance(peak.pose.hip),EPS);
        assertEquals(0f,start.pose.knee.distance(peak.pose.knee),EPS);
        assertEquals(0f,start.pose.farAnkle.distance(peak.pose.farAnkle),EPS);
        assertTrue(peak.pose.ankle.x>start.pose.ankle.x+70f);
        assertTrue(peak.pose.ankle.y<start.pose.ankle.y-40f);
        assertEquals(0f,peak.supportFoot.distance(start.supportFoot),EPS);
    }
    @Test public void toeLiftOnlyRotatesTheFeetAndKeepsSeatedJointsStable(){
        AvatarDedicatedScene start=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_TOE_RAISE,0f,BODY);
        AvatarDedicatedScene max=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_TOE_RAISE,.49f,BODY);
        AvatarDedicatedScene end=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_TOE_RAISE,1f,BODY);
        assertTrue(max.chairSeatContact);
        assertFalse(max.floorBackContact);
        assertEquals(22f,max.toeRaiseDegrees,.15f);
        assertEquals(0f,start.toeRaiseDegrees,EPS);
        assertEquals(0f,end.toeRaiseDegrees,EPS);
        assertEquals(0f,start.pose.hip.distance(max.pose.hip),EPS);
        assertEquals(0f,start.pose.shoulder.distance(max.pose.shoulder),EPS);
        assertEquals(0f,start.pose.knee.distance(max.pose.knee),EPS);
        assertEquals(0f,start.pose.ankle.distance(max.pose.ankle),EPS);
        assertEquals(0f,start.pose.farAnkle.distance(max.pose.farAnkle),EPS);
    }
    @Test public void seatedForwardReachRotatesTrunkAroundFixedHipAndHoldsBothFeet(){
        AvatarDedicatedScene start=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_FORWARD_REACH,0f,BODY);
        AvatarDedicatedScene max=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_FORWARD_REACH,.49f,BODY);
        assertTrue(max.chairSeatContact);
        assertEquals(0f,start.pose.hip.distance(max.pose.hip),EPS);
        assertEquals(0f,start.pose.farHip.distance(max.pose.farHip),EPS);
        assertEquals(0f,start.pose.ankle.distance(max.pose.ankle),EPS);
        assertEquals(0f,start.pose.farAnkle.distance(max.pose.farAnkle),EPS);
        assertEquals(0f,start.pose.knee.distance(max.pose.knee),EPS);
        assertTrue(max.pose.shoulder.x>start.pose.shoulder.x+32f);
        assertTrue(max.pose.wrist.x>start.pose.wrist.x+100f);
        assertEquals(BODY.torso,
                max.pose.hip.distance(max.pose.shoulder),EPS);
        assertEquals(0f,max.toeRaiseDegrees,EPS);
    }
    @Test public void heldBridgeRetainsMatContactsAndPersistsAtItsApex(){
        AvatarDedicatedScene initial=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_PAUSE,0f,BODY);
        AvatarDedicatedScene firstHold=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_PAUSE,.35f,BODY);
        AvatarDedicatedScene lastHold=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_PAUSE,.65f,BODY);
        AvatarDedicatedScene end=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_PAUSE,1f,BODY);
        assertTrue(firstHold.floorBackContact);
        assertEquals(0f,firstHold.pose.hip.distance(lastHold.pose.hip),EPS);
        assertTrue(initial.pose.hip.y-firstHold.pose.hip.y>40f);
        assertEquals(0f,initial.pose.head.distance(firstHold.pose.head),EPS);
        assertEquals(0f,initial.pose.ankle.distance(firstHold.pose.ankle),EPS);
        assertEquals(0f,initial.pose.hip.distance(end.pose.hip),EPS);
    }
    @Test public void pausedSeatedExtensionUsesLongerHoldWithStableSupportFoot(){
        AvatarDedicatedScene start=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_PAUSE,0f,BODY);
        AvatarDedicatedScene held1=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_PAUSE,.35f,BODY);
        AvatarDedicatedScene held2=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_PAUSE,.65f,BODY);
        assertEquals(0f,held1.pose.ankle.distance(held2.pose.ankle),EPS);
        assertTrue(held1.pose.ankle.x>start.pose.ankle.x+70f);
        assertEquals(0f,start.pose.knee.distance(held1.pose.knee),EPS);
        assertEquals(0f,start.pose.farAnkle.distance(held1.pose.farAnkle),EPS);
        assertTrue(held1.chairSeatContact);
    }
    @Test public void alternatingKneeExtensionNeverLiftsBothLegsAtOnce(){
        AvatarDedicatedScene start=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_ALTERNATING,0f,BODY);
        AvatarDedicatedScene first=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_ALTERNATING,.25f,BODY);
        AvatarDedicatedScene middle=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_ALTERNATING,.5f,BODY);
        AvatarDedicatedScene second=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_ALTERNATING,.75f,BODY);
        AvatarDedicatedScene end=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_ALTERNATING,1f,BODY);
        assertTrue(first.chairSeatContact);
        assertTrue(first.pose.ankle.x>start.pose.ankle.x+70f);
        assertEquals(0f,start.pose.farAnkle.distance(first.pose.farAnkle),EPS);
        assertEquals(0f,start.pose.ankle.distance(middle.pose.ankle),EPS);
        assertEquals(0f,start.pose.farAnkle.distance(middle.pose.farAnkle),EPS);
        assertTrue(second.pose.farAnkle.x>start.pose.farAnkle.x+70f);
        assertEquals(0f,start.pose.ankle.distance(second.pose.ankle),EPS);
        assertEquals(0f,start.pose.farAnkle.distance(end.pose.farAnkle),EPS);
    }
    @Test public void chairHeelRaiseKeepsForefootContactAndRigidBones(){
        for(AvatarDedicatedScene.Kind kind:new AvatarDedicatedScene.Kind[]{
                AvatarDedicatedScene.Kind.SEATED_HEEL_RAISE,
                AvatarDedicatedScene.Kind.SEATED_HEEL_RAISE_GENTLE}){
            AvatarDedicatedScene start=AvatarDedicatedScene.at(kind,0f,BODY);
            AvatarDedicatedScene peak=AvatarDedicatedScene.at(kind,.49f,BODY);
            AvatarDedicatedScene end=AvatarDedicatedScene.at(kind,1f,BODY);
            assertTrue(peak.chairSeatContact);
            assertEquals(0f,start.pose.hip.distance(peak.pose.hip),EPS);
            assertEquals(0f,start.nearToeContact.distance(peak.nearToeContact),EPS);
            assertEquals(0f,start.farToeContact.distance(peak.farToeContact),EPS);
            assertTrue(peak.pose.ankle.y<start.pose.ankle.y-7f);
            assertTrue(peak.pose.farAnkle.y<start.pose.farAnkle.y-7f);
            assertTrue(peak.heelRaiseDegrees>10f);
            assertEquals(0f,start.pose.ankle.distance(end.pose.ankle),EPS);
            assertEquals(0f,start.pose.farAnkle.distance(end.pose.farAnkle),EPS);
            assertEquals(0f,end.heelRaiseDegrees,EPS);
        }
        AvatarDedicatedScene gentle=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_HEEL_RAISE_GENTLE,.49f,BODY);
        AvatarDedicatedScene normal=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_HEEL_RAISE,.49f,BODY);
        assertTrue(normal.heelRaiseDegrees>gentle.heelRaiseDegrees+9f);
    }
    @Test public void unilateralToeRaiseKeepsFarShoeAndChairUnchanged(){
        AvatarDedicatedScene start=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_TOE_RAISE_SINGLE,0f,BODY);
        AvatarDedicatedScene peak=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.SEATED_TOE_RAISE_SINGLE,.49f,BODY);
        assertEquals(22f,peak.toeRaiseDegrees,.1f);
        assertEquals(0f,start.pose.farAnkle.distance(peak.pose.farAnkle),EPS);
        assertEquals(0f,start.pose.ankle.distance(peak.pose.ankle),EPS);
        assertEquals(0f,start.pose.knee.distance(peak.pose.knee),EPS);
        assertTrue(peak.chairSeatContact);
    }
    @Test public void bridgeBandHasMatAndCorrectTwoFootContacts(){
        AvatarDedicatedScene normal=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_BASIC,.49f,BODY);
        AvatarDedicatedScene band=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.GLUTE_BRIDGE_BAND,.49f,BODY);
        assertTrue(band.floorBackContact);
        assertEquals(0f,normal.pose.hip.distance(band.pose.hip),EPS);
        assertEquals(0f,normal.pose.ankle.distance(band.pose.ankle),EPS);
        assertEquals(0f,normal.pose.farAnkle.distance(band.pose.farAnkle),EPS);
    }
    @Test public void seatedHeelToeAlternationRetainsActualPivotsAndReturnsToNeutral(){
        AvatarDedicatedScene.Kind kind=
                AvatarDedicatedScene.Kind.SEATED_HEEL_TOE_ALTERNATION;
        AvatarDedicatedScene neutral=AvatarDedicatedScene.at(kind,0f,BODY);
        AvatarDedicatedScene heel=AvatarDedicatedScene.at(kind,.25f,BODY);
        AvatarDedicatedScene middle=AvatarDedicatedScene.at(kind,.5f,BODY);
        AvatarDedicatedScene toes=AvatarDedicatedScene.at(kind,.75f,BODY);
        AvatarDedicatedScene finish=AvatarDedicatedScene.at(kind,1f,BODY);
        assertTrue(heel.chairSeatContact);
        assertFalse(heel.floorBackContact);
        assertEquals(16f,heel.heelRaiseDegrees,.01f);
        assertEquals(0f,heel.toeRaiseDegrees,0f);
        assertEquals(0f,toes.heelRaiseDegrees,0f);
        assertEquals(16f,toes.toeRaiseDegrees,.01f);
        assertEquals(0f,neutral.pose.ankle.distance(middle.pose.ankle),EPS);
        assertEquals(0f,neutral.pose.farAnkle.distance(middle.pose.farAnkle),EPS);
        assertEquals(0f,neutral.pose.ankle.distance(toes.pose.ankle),EPS);
        assertEquals(0f,neutral.pose.farAnkle.distance(toes.pose.farAnkle),EPS);
        assertEquals(0f,neutral.pose.hip.distance(heel.pose.hip),EPS);
        assertEquals(0f,neutral.pose.hip.distance(toes.pose.hip),EPS);
        assertEquals(0f,neutral.nearToeContact.distance(
                heel.nearToeContact),EPS);
        assertEquals(0f,neutral.farToeContact.distance(
                heel.farToeContact),EPS);
        assertTrue(heel.pose.ankle.y<neutral.pose.ankle.y-6f);
        assertEquals(0f,neutral.pose.ankle.distance(finish.pose.ankle),EPS);
        assertEquals(0f,neutral.pose.farAnkle.distance(finish.pose.farAnkle),EPS);
    }

    @Test public void standingHeelAndToeRaisesHaveDistinctFixedSupportContacts(){
        AvatarDedicatedScene heelStart=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.STANDING_HEEL_RAISE,0f,BODY);
        AvatarDedicatedScene heelPeak=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.STANDING_HEEL_RAISE,.49f,BODY);
        AvatarDedicatedScene heelEnd=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.STANDING_HEEL_RAISE,1f,BODY);
        assertTrue(heelPeak.standingFloorContact);
        assertFalse(heelPeak.chairSeatContact);
        assertFalse(heelPeak.floorBackContact);
        assertEquals(18f,heelPeak.heelRaiseDegrees,.01f);
        assertEquals(0f,heelPeak.toeRaiseDegrees,0f);
        assertEquals(0f,heelStart.supportFoot.distance(heelPeak.supportFoot),EPS);
        assertEquals(0f,heelStart.nearToeContact.distance(heelPeak.nearToeContact),EPS);
        assertEquals(0f,heelStart.farToeContact.distance(heelPeak.farToeContact),EPS);
        assertTrue(heelPeak.pose.ankle.y<heelStart.pose.ankle.y-8f);
        assertTrue(heelPeak.pose.hip.y<heelStart.pose.hip.y-8f);
        assertTrue(heelPeak.pose.head.y<heelStart.pose.head.y-8f);
        assertEquals(0f,heelStart.pose.ankle.distance(heelEnd.pose.ankle),EPS);
        assertEquals(0f,heelStart.pose.head.distance(heelEnd.pose.head),EPS);
        AvatarDedicatedScene toeStart=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.STANDING_TOE_RAISE,0f,BODY);
        AvatarDedicatedScene toePeak=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.STANDING_TOE_RAISE,.49f,BODY);
        AvatarDedicatedScene toeEnd=AvatarDedicatedScene.at(
                AvatarDedicatedScene.Kind.STANDING_TOE_RAISE,1f,BODY);
        assertTrue(toePeak.standingFloorContact);
        assertFalse(toePeak.chairSeatContact);
        assertEquals(16f,toePeak.toeRaiseDegrees,.01f);
        assertEquals(0f,toePeak.heelRaiseDegrees,0f);
        assertEquals(0f,toeStart.pose.ankle.distance(toePeak.pose.ankle),EPS);
        assertEquals(0f,toeStart.pose.farAnkle.distance(toePeak.pose.farAnkle),EPS);
        assertEquals(0f,toeStart.pose.hip.distance(toePeak.pose.hip),EPS);
        assertEquals(0f,toeStart.supportFoot.distance(toePeak.supportFoot),EPS);
        assertEquals(0f,toeStart.pose.ankle.distance(toeEnd.pose.ankle),EPS);
        assertEquals(0f,toeStart.pose.head.distance(toeEnd.pose.head),EPS);
    }

    @Test public void invalidSpecsAndPhasesCannotProduceFakeScenes(){
        assertThrows(IllegalArgumentException.class,()->
                AvatarDedicatedScene.at(null,.5f,BODY));
        assertThrows(IllegalArgumentException.class,()->
                AvatarDedicatedScene.at(AvatarDedicatedScene.Kind.GLUTE_BRIDGE_BASIC,.5f,null));
    }
}
