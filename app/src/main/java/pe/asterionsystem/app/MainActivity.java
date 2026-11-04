package pe.asterionsystem.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * Fase 01: entrada mínima para comprobar la estructura nativa Android.
 * La navegación, el estado y el motor de dominio pertenecen a fases posteriores.
 */
public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
