package pe.asterionsystem.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Vídeo MP4 H.264 local por ID; no usa conexión ni traslada archivos al servidor.
 * Reutiliza solo cache interna del mismo ID, no los vídeos de otras variantes.
 */
public final class OfflineExerciseMp4Player extends FrameLayout {
    private final VideoView video;
    private boolean prepared;
    private boolean userPaused;
    private boolean screenPaused;
    private final boolean motionReduced;

    public OfflineExerciseMp4Player(Activity activity, String id, String assetPath)
            throws IOException {
        super(activity);
        if(!id.matches("[a-z0-9_-]+")
                || !assetPath.equals("exercise_mp4/v1/"+id+".mp4"))
            throw new IOException("La ruta MP4 no corresponde a esta variante");
        setBackgroundColor(0xFF191C26);
        motionReduced=Settings.Global.getFloat(activity.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE,1f)==0f;
        File source=cacheMp4(activity,id,assetPath);
        video=new VideoView(activity);
        video.setBackgroundColor(Color.TRANSPARENT);
        addView(video,new LayoutParams(-1,-1,Gravity.CENTER));
        video.setContentDescription("Demostración animada específica de "+id);
        video.setOnPreparedListener(player->{
            prepared=true;
            player.setVolume(0f,0f);
            player.setLooping(true);
            video.seekTo(0);
            if(!motionReduced&&!userPaused&&!screenPaused)video.start();
            else {
                // Renderiza el primer fotograma sin movimiento.
                video.start();
                video.pause();
                video.seekTo(1);
            }
        });
        video.setOnErrorListener((media,what,extra)->{
            prepared=false;
            removeView(video);
            TextView error=new TextView(activity);
            error.setGravity(Gravity.CENTER);
            error.setTextColor(Color.WHITE);
            error.setText("No se puede reproducir este MP4. "
                    +"La ficha técnica sigue disponible.");
            addView(error,new LayoutParams(-1,-1));
            return true;
        });
        video.setVideoPath(source.getAbsolutePath());
    }

    private static File cacheMp4(Context context,String id,String asset)
            throws IOException {
        File folder=new File(context.getCacheDir(),"asterion_mp4_v1");
        if(!folder.exists()&&!folder.mkdirs())
            throw new IOException("No se pudo crear la caché de MP4");
        File target=new File(folder,id+".mp4");
        // Siempre reemplazar este ID para no reproducir una versión obsoleta.
        File temp=new File(folder,id+".download");
        int total=0;
        try(InputStream in=context.getAssets().open(asset);
            FileOutputStream out=new FileOutputStream(temp)) {
            byte[] buffer=new byte[32768];
            for(int length;(length=in.read(buffer))!=-1;) {
                total+=length;
                if(total>800000)throw new IOException("MP4 supera límite local");
                out.write(buffer,0,length);
            }
        } catch(IOException failure) {
            temp.delete();
            throw failure;
        }
        if(total==0||(!temp.renameTo(target)&&!target.delete()))
            throw new IOException("No se pudo preparar el MP4 de esta variante");
        if(temp.exists()&&!temp.renameTo(target))
            throw new IOException("No se pudo actualizar vídeo en caché");
        return target;
    }

    public void toggle() {
        if(!prepared || motionReduced)return;
        if(video.isPlaying()){
            video.pause();
            userPaused=true;
        }else{
            userPaused=false;
            if(!screenPaused)video.start();
        }
    }

    public boolean isPausedByUser(){return userPaused || motionReduced;}

    public void pauseForLifecycle(){
        screenPaused=true;
        if(prepared && video.isPlaying())video.pause();
    }

    public void resumeForLifecycle(){
        screenPaused=false;
        if(prepared&&!userPaused&&!motionReduced)video.start();
    }

    public void release(){
        video.stopPlayback();
        prepared=false;
    }
}
