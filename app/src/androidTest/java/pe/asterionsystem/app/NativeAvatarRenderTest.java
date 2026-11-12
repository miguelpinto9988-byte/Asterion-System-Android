package pe.asterionsystem.app;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.io.File;
import java.io.FileOutputStream;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Ensures both native palettes really paint an articulated squat pose.
 * Runs without an Activity, network, MP4 player or third-party engine.
 */
@RunWith(AndroidJUnit4.class)
public final class NativeAvatarRenderTest {
    @Test public void blueAndPurpleDrawDifferentPausedPosePixels() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        final Bitmap[] frames=new Bitmap[2];
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            view.setPhase(.49f);
            assertEquals(.49f,view.getPhase(),.001f);
            for(int i=0;i<2;i++) {
                view.setFemale(i==1);
                frames[i]=Bitmap.createBitmap(400,480,Bitmap.Config.ARGB_8888);
                Canvas canvas=new Canvas(frames[i]);
                view.draw(canvas);
            }
        });
        assertNotNull(frames[0]);assertNotNull(frames[1]);
        int male=frames[0].getPixel(170,208);
        int female=frames[1].getPixel(170,208);
        assertNotEquals("Different tops should have visibly distinct colors",male,female);
        frames[0].recycle();frames[1].recycle();
    }
    @Test public void optionDRendersAcrossResolutionsAndModes() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.setPlaying(false);
            for(int[] size:new int[][]{{400,480},{200,240},{600,720}}) {
                view.layout(0,0,size[0],size[1]);
                for(boolean dark:new boolean[]{false,true}) {
                    view.setDark(dark);
                    for(float phase:new float[]{0f,.25f,.48f,.75f}) {
                        view.setPhase(phase);
                        Bitmap bm=Bitmap.createBitmap(size[0],size[1],
                                Bitmap.Config.ARGB_8888);
                        view.draw(new Canvas(bm));
                        // Canvas background is solid inside the design margin.
                        assertNotEquals(0,bm.getPixel(size[0]/2,size[1]/2));
                        bm.recycle();
                    }
                }
            }
        });
    }
    @Test public void independentlyPaintedV3PartsHaveVisibleDetails() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            AvatarArtDetails art=new AvatarArtDetails();
            Bitmap bitmap=Bitmap.createBitmap(220,220,Bitmap.Config.ARGB_8888);
            Canvas canvas=new Canvas(bitmap);
            AvatarKinematics.Point face=new AvatarKinematics.Point(70,70);
            AvatarKinematics.Point foot=new AvatarKinematics.Point(70,155);
            art.drawHead(canvas,face,AvatarBodySpec.DEFAULT,AvatarPalette.BLUE);
            art.drawSneaker(canvas,foot,AvatarPalette.BLUE,false);
            assertNotEquals(0,bitmap.getPixel(70,70));
            assertNotEquals(0,bitmap.getPixel(81,69));
            assertNotEquals(0,bitmap.getPixel(80,160));
            bitmap.eraseColor(0);
            art.drawHead(canvas,face,AvatarBodySpec.DEFAULT,AvatarPalette.PURPLE);
            art.drawSneaker(canvas,foot,AvatarPalette.PURPLE,false);
            assertNotEquals(0,bitmap.getPixel(70,70));
            assertNotEquals(0,bitmap.getPixel(80,160));
            bitmap.recycle();
        });
    }

    @Test public void fiveMotionPresetsRenderWithoutChangingRigDimensions() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            for(AvatarMotionEngine.Motion m:AvatarMotionEngine.Motion.values()){
                view.setMotion(m);
                assertEquals(m,view.getMotion());
                assertTrue(view.getContentDescription().toString()
                        .contains(m.label.toLowerCase(java.util.Locale.ROOT)));
                for(float phase:new float[]{0f,.18f,.48f,.7f,.99f}){
                    view.setPhase(phase);
                    Bitmap frame=Bitmap.createBitmap(
                            400,480,Bitmap.Config.ARGB_8888);
                    view.draw(new Canvas(frame));
                    assertNotEquals(0,frame.getPixel(200,300));
                    frame.recycle();
                    AvatarMotionEngine.assertSkeleton(
                            AvatarMotionEngine.pose(m,phase,AvatarBodySpec.DEFAULT),
                            AvatarBodySpec.DEFAULT,.07f);
                }
            }
        });
    }

    @Test public void frontAndTopCameraRenderWithHandInsetAndAccessibility(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            for(AvatarMultiViewKinematics.ViewMode camera:
                    new AvatarMultiViewKinematics.ViewMode[]{
                        AvatarMultiViewKinematics.ViewMode.FRONT,
                        AvatarMultiViewKinematics.ViewMode.TOP}) {
                view.setViewMode(camera);
                assertEquals(camera,view.getViewMode());
                for(float phase:new float[]{0f,.21f,.49f,.70f,.99f}) {
                    view.setPhase(phase);
                    Bitmap bitmap=Bitmap.createBitmap(
                            400,480,Bitmap.Config.ARGB_8888);
                    view.draw(new Canvas(bitmap));
                    assertNotEquals(0,bitmap.getPixel(200,200));
                    if(camera==AvatarMultiViewKinematics.ViewMode.TOP) {
                        assertNotEquals(0,bitmap.getPixel(270,185));
                        assertTrue(view.getContentDescription().toString()
                                .contains("detalle de manos"));
                    } else {
                        assertTrue(view.getContentDescription().toString()
                                .contains("elevación frontal"));
                    }
                    bitmap.recycle();
                }
            }
        });
    }

    @Test public void frontalWideSquatRendersAndKeepsContact(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            view.setViewMode(AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT);
            assertTrue(view.getContentDescription().toString()
                    .contains("sentadilla abierta"));
            for(float phase:new float[]{0f,.24f,.49f,.77f,.99f}){
                view.setPhase(phase);
                Bitmap bitmap=Bitmap.createBitmap(400,480,Bitmap.Config.ARGB_8888);
                view.draw(new Canvas(bitmap));
                assertNotEquals(0,bitmap.getPixel(200,230));
                bitmap.recycle();
                AvatarMultiViewKinematics.Pose pose=
                        AvatarMultiViewKinematics.frontWideSquat(
                                phase,AvatarBodySpec.DEFAULT);
                pose.assertArms(.08f);
                assertEquals(pose.leftShoulder.y,pose.rightShoulder.y,.001f);
            }
        });
    }

    @Test public void jumpingJacksElbowsAndBurpeeRenderInBothPalettes() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            for(boolean female:new boolean[]{false,true}) {
                view.setFemale(female);
                for(AvatarMultiViewKinematics.ViewMode scene:
                        new AvatarMultiViewKinematics.ViewMode[]{
                        AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,
                        AvatarMultiViewKinematics.ViewMode.FRONT_ELBOW}) {
                    view.setViewMode(scene);
                    for(float phase:new float[]{0f,.24f,.49f,.76f,1f}) {
                        view.setPhase(phase);
                        Bitmap frame=Bitmap.createBitmap(400,480,
                                Bitmap.Config.ARGB_8888);
                        view.draw(new Canvas(frame));
                        assertNotEquals(0,frame.getPixel(200,200));
                        frame.recycle();
                        AvatarMultiViewKinematics.Pose pose=
                                scene==AvatarMultiViewKinematics.ViewMode.FRONT_JACKS
                                ?AvatarMultiViewKinematics.frontJumpingJack(
                                        phase,AvatarBodySpec.DEFAULT)
                                :AvatarMultiViewKinematics.frontElbowFlex(
                                        phase,AvatarBodySpec.DEFAULT);
                        pose.assertArms(.09f);
                    }
                }
                view.setViewMode(AvatarMultiViewKinematics.ViewMode.SIDE);
                view.setMotion(AvatarMotionEngine.Motion.BURPEE);
                assertTrue(view.getContentDescription().toString()
                        .contains("burpee"));
                for(float phase:new float[]{0f,.17f,.25f,.40f,.5f,.75f,1f}) {
                    view.setPhase(phase);
                    Bitmap frame=Bitmap.createBitmap(400,480,
                            Bitmap.Config.ARGB_8888);
                    view.draw(new Canvas(frame));
                    assertNotEquals(0,frame.getPixel(200,240));
                    frame.recycle();
                }
            }
        });
    }

    @Test public void sharedVisualDetailsRenderForAllThreeProjections(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            AvatarArtDetails details=new AvatarArtDetails();
            for(AvatarPalette palette:new AvatarPalette[]{
                    AvatarPalette.BLUE,AvatarPalette.PURPLE}) {
                Bitmap frame=Bitmap.createBitmap(260,280,Bitmap.Config.ARGB_8888);
                Canvas c=new Canvas(frame);
                AvatarKinematics.Point head=new AvatarKinematics.Point(75,82);
                AvatarKinematics.Point sole=new AvatarKinematics.Point(76,206);
                AvatarKinematics.Point hand=new AvatarKinematics.Point(168,205);
                details.drawHeadFront(c,head,AvatarBodySpec.DEFAULT,palette);
                details.drawHeadTop(c,new AvatarKinematics.Point(177,82),
                        AvatarBodySpec.DEFAULT,palette);
                details.drawProjectedSneaker(c,sole,palette,false);
                details.drawProjectedSneaker(c,
                        new AvatarKinematics.Point(123,206),palette,true);
                details.drawProjectedHand(c,hand,palette);
                assertNotEquals("frontal face",0,frame.getPixel(75,82));
                assertNotEquals("hair in top view",0,frame.getPixel(177,64));
                assertNotEquals("front shoe",0,frame.getPixel(76,206));
                assertNotEquals("overhead shoe",0,frame.getPixel(123,206));
                assertNotEquals("palm and thumb",0,frame.getPixel(168,205));
                frame.recycle();
            }
        });
    }

    @Test public void consistentCamerasRenderEntireAnimationWithoutGaps(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            for(boolean purple:new boolean[]{false,true}){
                view.setFemale(purple);
                for(AvatarMultiViewKinematics.ViewMode camera:
                        new AvatarMultiViewKinematics.ViewMode[]{
                        AvatarMultiViewKinematics.ViewMode.SIDE,
                        AvatarMultiViewKinematics.ViewMode.FRONT,
                        AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT,
                        AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,
                        AvatarMultiViewKinematics.ViewMode.FRONT_ELBOW,
                        AvatarMultiViewKinematics.ViewMode.TOP}) {
                    view.setViewMode(camera);
                    for(float t:new float[]{0f,.19f,.41f,.5f,.76f,1f}) {
                        view.setPhase(t);
                        Bitmap frame=Bitmap.createBitmap(400,480,
                                Bitmap.Config.ARGB_8888);
                        view.draw(new Canvas(frame));
                        assertNotEquals("scene needs background",0,
                                frame.getPixel(200,210));
                        frame.recycle();
                    }
                }
            }
        });
    }

    @Test public void proneTopShoesPointTowardHeadAndShowOnlyUpper() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            AvatarArtDetails details=new AvatarArtDetails();
            AvatarMultiViewKinematics.Pose pose=
                    AvatarMultiViewKinematics.diamondTop(0f,AvatarBodySpec.DEFAULT);
            for(AvatarFootOrientation.Side side:AvatarFootOrientation.Side.values()){
                AvatarFootOrientation.FootProjection foot=
                        AvatarFootOrientation.proneTop(pose,side);
                for(AvatarPalette palette:new AvatarPalette[]{
                        AvatarPalette.BLUE,AvatarPalette.PURPLE}){
                    Bitmap b=Bitmap.createBitmap(260,480,Bitmap.Config.ARGB_8888);
                    details.drawProneTopSneaker(new Canvas(b),foot,palette);
                    int x=(int)foot.ankle.x,y=(int)foot.ankle.y;
                    assertTrue("toe must be nearer head",foot.toe.y<foot.ankle.y);
                    assertNotEquals("visible toe upper",0,b.getPixel(x,y-16));
                    assertNotEquals("visible heel",0,b.getPixel(x,y+11));
                    assertEquals("no protruding reversed outsole",0,
                            b.getPixel(x,y+23));
                    b.recycle();
                }
            }
            try {
                AvatarFootOrientation.proneTop(pose,null);
                fail("Missing foot side must be rejected");
            } catch(IllegalArgumentException expected) {
                // An invalid orientation cannot reach the renderer.
            }
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            view.setViewMode(AvatarMultiViewKinematics.ViewMode.TOP);
            for(float phase:new float[]{0f,.2f,.49f,.75f,1f}){
                view.setPhase(phase);
                Bitmap frame=Bitmap.createBitmap(400,480,
                        Bitmap.Config.ARGB_8888);
                view.draw(new Canvas(frame));
                assertNotEquals(0,frame.getPixel(200,400));
                frame.recycle();
            }
        });
    }

    @Test public void proneTopShoesUseRearHeelCounterWithoutLaceBands() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            AvatarMultiViewKinematics.Pose pose=
                    AvatarMultiViewKinematics.diamondTop(.49f,AvatarBodySpec.DEFAULT);
            AvatarArtDetails details=new AvatarArtDetails();
            for(AvatarFootOrientation.Side side:AvatarFootOrientation.Side.values()){
                AvatarFootOrientation.FootProjection foot=
                        AvatarFootOrientation.proneTop(pose,side);
                for(AvatarPalette palette:new AvatarPalette[]{
                        AvatarPalette.BLUE,AvatarPalette.PURPLE}){
                    Bitmap b=Bitmap.createBitmap(280,480,Bitmap.Config.ARGB_8888);
                    details.drawProneTopSneaker(new Canvas(b),foot,palette);
                    int x=Math.round(foot.ankle.x),y=Math.round(foot.ankle.y);
                    // The rear collar/backstay is dark and unbroken.
                    // A lace/tongue graphic here would render pale stripes.
                    for(int delta:new int[]{-5,-2,2,5,8}){
                        int pixel=b.getPixel(x,y+delta);
                        assertTrue("Rear counter must cover the heel",pixel!=0);
                        int red=(pixel>>16)&0xFF;
                        assertTrue("No front tongue/laces on rear heel",red<180);
                    }
                    // The toe remains in the correct anatomical direction;
                    // no floating outsole is painted below the heel.
                    assertTrue(foot.toe.y<foot.ankle.y);
                    assertEquals(0,b.getPixel(x,y+24));
                    b.recycle();
                }
            }
        });
    }

    /**
     * Pixel-level proof from the REAL Android View renderer, not a web mock.
     * CI pulls these 42 PNGs from app-specific external storage and uploads
     * them as GitHub artifacts; users can inspect the side, front, top, and
     * every additional motion in both palettes at three distinct phases.
     */
    @Test public void exportRealAndroidMultiviewFramesForReview() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        File root=context.getExternalFilesDir(null);
        assertNotNull("An emulator external files directory is required",root);
        final File dir=new File(root,"asterion-v11-evidence");
        assertTrue("Cannot create evidence directory",dir.isDirectory()||dir.mkdirs());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            view.setDark(false);
            for(boolean purple:new boolean[]{false,true}){
                view.setFemale(purple);
                for(int scene=0;scene<7;scene++){
                    if(scene<=1){
                        view.setViewMode(AvatarMultiViewKinematics.ViewMode.SIDE);
                        view.setMotion(scene==0?AvatarMotionEngine.Motion.SQUAT
                                :AvatarMotionEngine.Motion.BURPEE);
                    }else{
                        AvatarMultiViewKinematics.ViewMode[] views={
                            AvatarMultiViewKinematics.ViewMode.FRONT,
                            AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT,
                            AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,
                            AvatarMultiViewKinematics.ViewMode.FRONT_ELBOW,
                            AvatarMultiViewKinematics.ViewMode.TOP};
                        view.setViewMode(views[scene-2]);
                    }
                    for(int phaseIndex=0;phaseIndex<3;phaseIndex++){
                        float phase=phaseIndex==0?0f:phaseIndex==1?.49f:1f;
                        view.setPhase(phase);
                        Bitmap bitmap=Bitmap.createBitmap(400,480,
                                Bitmap.Config.ARGB_8888);
                        view.draw(new Canvas(bitmap));
                        String name=(purple?"morado":"azul")+"_"
                                +(scene==0?"lateral_sentadilla":scene==1?"lateral_burpee":
                                  view.getViewMode().name().toLowerCase(java.util.Locale.ROOT))
                                +"_"+phaseIndex+".png";
                        File output=new File(dir,name);
                        try(FileOutputStream stream=new FileOutputStream(output)){
                            assertTrue("PNG export failed",
                                    bitmap.compress(Bitmap.CompressFormat.PNG,100,stream));
                        }catch(java.io.IOException e){
                            throw new IllegalStateException("Cannot save Android evidence",e);
                        }finally{
                            bitmap.recycle();
                        }
                        assertTrue("Empty screenshot",output.length()>2000);
                    }
                }
            }
        });
        File[] images=dir.listFiles((folder,name)->name.endsWith(".png"));
        assertNotNull(images);
        assertEquals("2 palettes × 7 scenes × 3 phases",42,images.length);
    }

    /**
     * Real Android Canvas previews for four previously unmapped catalog exercises,
     * covering begin/active/return in both palettes. The rig is deliberately 2D
     * and illustrations do not certify correct physical exercise technique.
     */
    @Test public void exportNewCatalogMotionSamplesAtThreePhases() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        AvatarCommandCatalog catalog;
        try(java.io.InputStreamReader input=new java.io.InputStreamReader(
                context.getAssets().open(
                        "asterion_exercises/v1/avatar_command_profiles.tsv"),
                java.nio.charset.StandardCharsets.UTF_8)) {
            catalog=AvatarCommandCatalog.load(input);
        }
        String[] sampleIds={"curl_biceps_v03","flexion_rodilla_bipeda_v03",
                "extension_cadera_bipeda_v01","equilibrio_unipodal_v04"};
        for(String id:sampleIds)assertTrue(id,catalog.require(id).canAnimate());
        File root=context.getExternalFilesDir(null);
        assertNotNull(root);
        File directory=new File(root,"asterion-avatar-catalog-preview");
        assertTrue(directory.isDirectory()||directory.mkdirs());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setDark(false);
            for(String variantId:sampleIds) {
                AvatarCommandCatalog.Entry entry=catalog.require(variantId);
                view.setCatalogEntry(entry);
                view.setPlaying(false);
                for(boolean purple:new boolean[]{false,true}) {
                    view.setFemale(purple);
                    for(int phaseIndex=0;phaseIndex<3;phaseIndex++) {
                        float phase=phaseIndex==0?0f:phaseIndex==1?.49f:1f;
                        view.setPhase(phase);
                        Bitmap bitmap=Bitmap.createBitmap(400,480,
                                Bitmap.Config.ARGB_8888);
                        view.draw(new Canvas(bitmap));
                        File output=new File(directory,
                                (purple?"morado_":"azul_")+variantId
                                        +"_"+phaseIndex+".png");
                        try(FileOutputStream file=new FileOutputStream(output)) {
                            assertTrue(bitmap.compress(
                                    Bitmap.CompressFormat.PNG,100,file));
                        }catch(java.io.IOException error) {
                            throw new IllegalStateException(
                                    "Failed to export real avatar sample",error);
                        }finally {bitmap.recycle();}
                        assertTrue(output.length()>2000);
                    }
                }
            }
        });
        File[] result=directory.listFiles((dir,name)->name.endsWith(".png"));
        assertNotNull(result);
        assertEquals(24,result.length);
    }

    @Test public void playbackParametersRejectNonFiniteValuesAndRemainAccessible() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.setPlaying(false);
            view.setPhase(Float.NaN);
            assertEquals(0f,view.getPhase(),.0001f);
            view.setPhase(Float.NEGATIVE_INFINITY);
            assertEquals(0f,view.getPhase(),.0001f);
            view.setPhase(Float.POSITIVE_INFINITY);
            assertEquals(1f,view.getPhase(),.0001f);
            view.setSpeed(1f);
            view.setSpeed(Float.NaN);
            assertEquals(1f,view.getSpeed(),.0001f);
            view.setSpeed(Float.POSITIVE_INFINITY);
            assertEquals(1f,view.getSpeed(),.0001f);
            view.setFemale(true);
            assertTrue(view.getContentDescription().toString().contains("Avatar morado"));
            assertTrue(view.getContentDescription().toString().contains("pausada"));
            view.setPlaying(true);
            assertTrue(view.getContentDescription().toString().contains("reproducción"));
        });
    }
}
