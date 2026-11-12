package pe.asterionsystem.app;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Contract checks over the complete real catalog. This does not certify
 * individual exercise biomechanics; text-only commands remain unavailable
 * for rendering until reviewed 2D rigs exist.
 */
public final class AvatarCommandCatalogTest {
    private AvatarCommandCatalog commands;

    @Before public void loadRealAssets() throws Exception {
        try(InputStreamReader stream=new InputStreamReader(
                getClass().getResourceAsStream(
                        "/asterion_exercises/v1/avatar_command_profiles.tsv"),
                StandardCharsets.UTF_8)) {
            commands=AvatarCommandCatalog.load(stream);
        }
    }

    @Test public void everyOneOf1257VariantsHasAUniqueExplicitCommand() throws Exception {
        Set<String> actual=new HashSet<>();
        try(java.io.BufferedReader reader=new java.io.BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(
                        "/asterion_exercises/v1/variants.tsv"),StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;
            while((line=reader.readLine())!=null) {
                if(line.isBlank())continue;
                String id=line.split("\t",-1)[0];
                assertNotNull("Missing visual command for "+id,commands.find(id));
                actual.add(id);
            }
        }
        assertEquals(1257,commands.size());
        assertEquals(1257,actual.size());
        assertThrows(IllegalArgumentException.class,()->commands.require("undefined"));
    }

    @Test public void allJointTracesHaveClosedThreePhaseCurves() {
        int schematic=0,textOnly=0;
        for(AvatarCommandCatalog.Entry entry:commands.all()) {
            AvatarCommandCatalog.JointCurve[] joints={
                entry.neck,entry.shoulder,entry.elbow,entry.wrist,
                entry.trunk,entry.hip,entry.knee,entry.ankle};
            for(AvatarCommandCatalog.JointCurve joint:joints) {
                assertEquals(entry.variantId,joint.start,joint.end);
                assertTrue(Math.abs(joint.active)<=180);
            }
            assertTrue(entry.tempoMs>=2000&&entry.tempoMs<=12000);
            assertTrue(entry.startPose.length()>8);
            assertTrue(entry.activePose.length()>8);
            assertTrue(entry.returnPose.length()>8);
            if(entry.canAnimate())schematic++;
            else textOnly++;
        }
        assertEquals(1257,schematic+textOnly);
        assertTrue(schematic>600);
        assertTrue(textOnly>150);
    }

    @Test public void everyEnabledSideCommandIsContinuousAndUsesRigidSegments() {
        int rendered=0;
        for(AvatarCommandCatalog.Entry entry:commands.all()) {
            if(!entry.canAnimate()
                    ||entry.camera!=AvatarMultiViewKinematics.ViewMode.SIDE)
                continue;
            AvatarKinematics.Pose start=AvatarCommandKinematics.pose(
                    entry,0f,AvatarBodySpec.DEFAULT);
            AvatarKinematics.Pose last=start;
            int samples=entry.command==AvatarCommandCatalog.Command.BURPEE
                    ?300:60;
            for(int i=1;i<=samples;i++) {
                AvatarKinematics.Pose p=AvatarCommandKinematics.pose(
                        entry,i/(float)samples,AvatarBodySpec.DEFAULT);
                AvatarMotionEngine.assertSkeleton(p,AvatarBodySpec.DEFAULT,.09f);
                assertTrue("Hip discontinuity "+entry.variantId,
                        p.hip.distance(last.hip)<22f);
                assertTrue("Knee discontinuity "+entry.variantId,
                        p.knee.distance(last.knee)<25f);
                assertTrue("Wrist discontinuity "+entry.variantId,
                        p.wrist.distance(last.wrist)<40f);
                last=p;
            }
            assertTrue("Cycle does not close "+entry.variantId,
                    start.hip.distance(last.hip)<.02f);
            assertTrue("Wrist cycle does not close "+entry.variantId,
                    start.wrist.distance(last.wrist)<.02f);
            assertTrue("Head cycle does not close "+entry.variantId,
                    start.head.distance(last.head)<.02f);
            String jointTrace=AvatarCommandKinematics.projectedJointTrace(
                    entry,AvatarBodySpec.DEFAULT);
            assertTrue(jointTrace.contains("Rodilla opuesta"));
            rendered++;
        }
        assertTrue("Need at least 500 new and preserved drawable profiles",rendered>500);
    }

    @Test public void specialEquipmentAndProneCasesDoNotShowWrongExercises() {
        assertFalse(commands.require("superman_v01").canAnimate());
        assertFalse(commands.require("ciclo_estatico_v01").canAnimate());
        assertFalse(commands.require("equilibrio_unipodal_v01").canAnimate());
        assertTrue(commands.require("sentadilla_bilateral_v03").canAnimate());
        assertEquals(AvatarCommandCatalog.Command.SQUAT,
                commands.require("sentadilla_bilateral_v03").command);
    }

    @Test public void originalSixCommandsRemainAvailable() {
        assertEquals(6,AvatarMotionEngine.Motion.values().length);
        for(AvatarMotionEngine.Motion preserved:AvatarMotionEngine.Motion.values())
            assertEquals(preserved,AvatarCommandKinematics.legacy(
                    AvatarCommandCatalog.Command.valueOf(preserved.name())));
    }

    @Test public void parserRejectsBrokenCountsAndInvalidJointAngles() {
        String header="variant_id\tname\ttype_id\tcommand\tcamera\tvisual_status"
                +"\ttempo_ms\tamplitude\tneck_deg\tshoulder_deg\telbow_deg"
                +"\twrist_deg\ttrunk_deg\thip_deg\tknee_deg\tankle_deg"
                +"\tstart_pose\tactive_pose\treturn_pose\tnotice\n";
        assertThrows(IllegalArgumentException.class,
                ()->AvatarCommandCatalog.load(new java.io.StringReader(header)));
        assertThrows(IllegalArgumentException.class,
                ()->AvatarCommandCatalog.JointCurve.read("0>10>5"));
        assertThrows(IllegalArgumentException.class,
                ()->AvatarCommandCatalog.JointCurve.read("0>199>0"));
    }
}
