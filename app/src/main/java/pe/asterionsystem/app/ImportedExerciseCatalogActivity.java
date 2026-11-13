package pe.asterionsystem.app;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import androidx.activity.ComponentActivity;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Spanish educational catalogue from MIT metadata. Media rights are NOT
 * inherited from the source dataset: do not load GIF or image paths here.
 * Imported entries are NOT currently eligible for automatic workout planning.
 */
public final class ImportedExerciseCatalogActivity extends ComponentActivity {
    private static final String ASSET="external_exercises/v1/ejercicios_es.json";
    private static final String[] CATEGORIES={
            "Todas las categorías","Tren superior","Tren central",
            "Tren inferior","Movilidad","Cardio"};
    private static final String[] CODES={
            "", "tren_superior", "tren_central",
            "tren_inferior", "movilidad", "cardio"};
    private static final int PAGE_SIZE=18;
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private final List<JSONObject> items=new ArrayList<>();
    private final List<JSONObject> matches=new ArrayList<>();
    private LinearLayout list;
    private TextView summary;
    private EditText search;
    private Spinner category;
    private int page,variant;
    private boolean dark,closed;
    private float density;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        density=getResources().getDisplayMetrics().density;
        dark=!"light".equals(ProfileStore.getRegistration(this)
                .optString("theme","light"));
        LinearLayout root=column();
        root.setPadding(dp(16),dp(12),dp(16),dp(12));
        root.setBackgroundColor(background());
        TextView heading=text("CATÁLOGO DE EJERCICIOS · ESPAÑOL",21);
        root.addView(heading);
        root.addView(text("Fichas informativas con instrucciones en español. "
                +"El nivel técnico aún requiere revisión; estos ejercicios "
                +"no se añaden automáticamente a rutinas.",13));
        root.addView(text("GIFs y miniaturas pendientes de licencia propia "
                +"de Gym visual. No se descargan ni muestran archivos protegidos.",12));
        search=new EditText(this);
        search.setSingleLine(true);
        search.setHint("Buscar un ejercicio en español");
        search.setTextColor(foreground());
        search.setHintTextColor(dark?0xFFC0C0C0:0xFF666666);
        root.addView(search,new LinearLayout.LayoutParams(-1,dp(54)));
        category=new Spinner(this);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,CATEGORIES);
        category.setAdapter(adapter);
        root.addView(category,new LinearLayout.LayoutParams(-1,dp(54)));
        Button random=button("Consultar un ejercicio aleatorio");
        root.addView(random,new LinearLayout.LayoutParams(-1,dp(55)));
        random.setOnClickListener(v->pickRandom());
        summary=text("Cargando catálogo...",13);
        root.addView(summary);
        ScrollView scroll=new ScrollView(this);
        list=column();
        scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button back=button("Volver a Rutinas");
        root.addView(back,new LinearLayout.LayoutParams(-1,dp(50)));
        back.setOnClickListener(v->finish());
        setContentView(root);

        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                refresh();
            }
            @Override public void afterTextChanged(Editable e){}
        });
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> p,View view,int pos,long id){
                refresh();
            }
            @Override public void onNothingSelected(AdapterView<?> p){}
        });
        executor.execute(()->{
            try {
                List<JSONObject> parsed=load();
                runOnUiThread(()->{
                    if(closed||isFinishing())return;
                    items.clear();
                    items.addAll(parsed);
                    refresh();
                });
            } catch(Exception error) {
                runOnUiThread(()->{
                    if(closed||isFinishing())return;
                    summary.setText("No se pudo cargar el catálogo importado. "
                            +"Comprueba que el importador generó el JSON español.");
                });
            }
        });
    }

    private List<JSONObject> load() throws Exception {
        byte[] bytes;
        try(InputStream stream=getAssets().open(ASSET);
            ByteArrayOutputStream buffer=new ByteArrayOutputStream()) {
            byte[] chunk=new byte[16384];
            int n;
            while((n=stream.read(chunk))!=-1)buffer.write(chunk,0,n);
            bytes=buffer.toByteArray();
        }
        JSONObject root=new JSONObject(new String(bytes,StandardCharsets.UTF_8));
        if(!"es".equals(root.optString("idioma"))
                ||root.optBoolean("multimedia_incluida",true)
                ||root.optInt("total")!=1324)
            throw new IllegalArgumentException("Contrato español/licencia no válido");
        JSONArray array=root.getJSONArray("ejercicios");
        if(array.length()!=1324)throw new IllegalArgumentException("Cantidad incompleta");
        ArrayList<JSONObject> result=new ArrayList<>(1324);
        for(int i=0;i<array.length();i++){
            JSONObject entry=array.getJSONObject(i);
            if(entry.optString("nombre_es").isEmpty()
                    ||entry.optJSONArray("instrucciones_es")==null
                    ||entry.optBoolean("gif_incluido",true)
                    ||entry.optBoolean("apto_para_rutinas_automaticas",true))
                throw new IllegalArgumentException("Ficha de importación inválida");
            result.add(entry);
        }
        result.sort(Comparator.comparing(j->j.optString("id")));
        return result;
    }

    private void refresh() {
        if(list==null||summary==null)return;
        matches.clear();
        String chosen=CODES[Math.max(0,Math.min(5,category.getSelectedItemPosition()))];
        String query=search.getText().toString().trim().toLowerCase(Locale.ROOT);
        for(JSONObject x:items) {
            if(!chosen.isEmpty()&&!chosen.equals(x.optString("categoria")))continue;
            String title=x.optString("nombre_es").toLowerCase(Locale.ROOT);
            String region=x.optString("region_es").toLowerCase(Locale.ROOT);
            String material=x.optString("material_es").toLowerCase(Locale.ROOT);
            if(!query.isEmpty()&&!title.contains(query)
                    &&!region.contains(query)&&!material.contains(query))continue;
            matches.add(x);
        }
        page=0;
        renderPage();
    }

    private void renderPage() {
        list.removeAllViews();
        if(items.isEmpty()){
            summary.setText("Cargando 1.324 fichas españolas...");
            return;
        }
        summary.setText(matches.size()+" ejercicios disponibles para consultar. "
                +"Títulos descriptivos provisionales y niveles por revisar.");
        if(matches.isEmpty()){
            list.addView(text("No hay coincidencias. Prueba otra categoría.",14));
            return;
        }
        int limit=Math.min(matches.size(),(page+1)*PAGE_SIZE);
        for(int i=0;i<limit;i++) {
            final JSONObject x=matches.get(i);
            Button b=button(x.optString("nombre_es"));
            b.setContentDescription("Consultar "+x.optString("nombre_es"));
            list.addView(b,new LinearLayout.LayoutParams(-1,dp(58)));
            b.setOnClickListener(v->details(x));
        }
        if(limit<matches.size()){
            Button more=button("Mostrar más ("+(matches.size()-limit)+" pendientes)");
            list.addView(more,new LinearLayout.LayoutParams(-1,dp(56)));
            more.setOnClickListener(v->{page++;renderPage();});
        }
    }

    private void pickRandom() {
        if(matches.isEmpty())return;
        long seed=LocalDate.now().toEpochDay()*1000003L
                +category.getSelectedItemPosition()*997L
                +search.getText().toString().hashCode()+variant++;
        ArrayList<JSONObject> sorted=new ArrayList<>(matches);
        sorted.sort(Comparator.comparing(x->x.optString("id")));
        details(sorted.get(new Random(seed).nextInt(sorted.size())));
    }

    private void details(JSONObject x) {
        StringBuilder out=new StringBuilder();
        out.append("Categoría: ").append(spanishCategory(x.optString("categoria")))
                .append("\nRegión: ").append(x.optString("region_es"))
                .append("\nMaterial: ").append(x.optString("material_es"))
                .append("\nNivel técnico: pendiente de revisión")
                .append("\n\nINSTRUCCIONES\n");
        JSONArray steps=x.optJSONArray("instrucciones_es");
        if(steps!=null)for(int i=0;i<steps.length();i++)
            out.append("\n").append(i+1).append(". ")
                    .append(steps.optString(i));
        out.append("\n\nDemostración GIF: pendiente de licencia de Gym visual.")
                .append("\n© Gym visual — https://gymvisual.com/")
                .append("\n\nConsulta informativa; no es una instrucción obligatoria.");
        new AlertDialog.Builder(this)
                .setTitle(x.optString("nombre_es"))
                .setMessage(out.toString())
                .setPositiveButton("Cerrar",null)
                .show();
    }

    private String spanishCategory(String id) {
        for(int i=1;i<CODES.length;i++)if(CODES[i].equals(id))return CATEGORIES[i];
        return "Por clasificar";
    }
    private LinearLayout column() {
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }
    private TextView text(String s,int size) {
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);
        t.setTextColor(foreground());t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setPadding(dp(3),dp(9),dp(3),dp(9));
        return t;
    }
    private Button button(String s) {
        Button b=new Button(this);b.setText(s);b.setAllCaps(false);
        b.setTextSize(13);b.setTextColor(foreground());
        return b;
    }
    private int background(){return dark?0xFF080808:Color.WHITE;}
    private int foreground(){return dark?Color.WHITE:0xFF111111;}
    private int dp(int px){return Math.max(1,Math.round(px*density));}
    @Override protected void onDestroy() {
        closed=true;executor.shutdownNow();super.onDestroy();
    }
}
