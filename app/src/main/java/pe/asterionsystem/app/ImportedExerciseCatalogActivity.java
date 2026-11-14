package pe.asterionsystem.app;

import android.app.AlertDialog;
import android.content.Intent;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Spanish educational catalogue. The verified GIF bundle is packaged separately
 * from its metadata and loaded only by exact exercise ID. Imported entries are
 * not currently eligible for automatic workout planning.
 */
public final class ImportedExerciseCatalogActivity extends ComponentActivity {
    private static final String ASSET="unified_exercises/v2/catalogo_es.json";
    private static final String CALISTHENICS_ASSET="unified_exercises/v2/calistenia_casa_es.json";
    private static final String MOTION_ASSET="exercise_gifs/v10/specs_es.json";
    private static final int BASE_EXERCISES=2104;
    private static final int CALISTHENICS_EXERCISES=19;
    private static final int EXERCISE_COUNT=2123;
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
    private final Map<String,JSONObject> motionSpecs=new HashMap<>();
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
        TextView heading=text("CATÁLOGO INDEPENDIENTE · ESPAÑOL",21);
        root.addView(heading);
        root.addView(text("Se conservan todos los ejercicios históricos. "
                +"Las variantes nuevas proceden de fuentes documentadas y "
                +"tienen nombre descriptivo en español. Su nivel debe revisarse "
                +"antes de incluirlas en propuestas automáticas.",13));
        root.addView(text("La app incluye un GIF local por variante. "
                +"Cada ficha mantiene su ID exacto; la correspondencia y la técnica "
                +"siguen pendientes de revisión individual.",12));
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
                Map<String,JSONObject> parsedSpecs=loadMotionSpecs(parsed);
                runOnUiThread(()->{
                    if(closed||isFinishing())return;
                    items.clear();
                    items.addAll(parsed);
                    motionSpecs.clear();
                    motionSpecs.putAll(parsedSpecs);
                    refresh();
                });
            } catch(Exception error) {
                runOnUiThread(()->{
                    if(closed||isFinishing())return;
                    summary.setText("No se pudo cargar el catálogo de 2123 ejercicios y sus GIFs. "
                            +"Comprueba que la compilación haya preparado los recursos.");
                });
            }
        });
    }

    private List<JSONObject> load() throws Exception {
        JSONObject base=readCatalog(ASSET);
        JSONObject calisthenics=readCatalog(CALISTHENICS_ASSET);
        ArrayList<JSONObject> result=new ArrayList<>(EXERCISE_COUNT);
        Map<String,Boolean> ids=new HashMap<>(EXERCISE_COUNT);
        appendCatalog(base,BASE_EXERCISES,1257,result,ids);
        appendCatalog(calisthenics,CALISTHENICS_EXERCISES,0,result,ids);
        if(result.size()!=EXERCISE_COUNT)
            throw new IllegalArgumentException("Catálogo combinado incompleto");
        result.sort(Comparator.comparing(j->
                ("asterion".equals(j.optString("origen"))?"0":"1")
                        +j.optString("id")));
        return result;
    }

    private JSONObject readCatalog(String asset) throws Exception {
        byte[] bytes;
        try(InputStream stream=getAssets().open(asset);
            ByteArrayOutputStream buffer=new ByteArrayOutputStream()) {
            byte[] chunk=new byte[16384];
            int n;
            while((n=stream.read(chunk))!=-1)buffer.write(chunk,0,n);
            bytes=buffer.toByteArray();
        }
        return new JSONObject(new String(bytes,StandardCharsets.UTF_8));
    }

    private void appendCatalog(JSONObject catalog,int expected,int originals,
            List<JSONObject> result,Map<String,Boolean> ids) throws Exception {
        if(!"asterion.public-domain-unified.es.v2".equals(catalog.optString("schema"))
                ||!"es".equals(catalog.optString("idioma"))
                ||catalog.optBoolean("multimedia_incluida",true)
                ||catalog.optInt("originales",-1)!=originals
                ||catalog.optInt("nuevos",-1)!=expected-originals)
            throw new IllegalArgumentException("Contrato del catálogo no válido");
        JSONArray array=catalog.getJSONArray("ejercicios");
        if(array.length()!=expected)throw new IllegalArgumentException("Cantidad incompleta");
        for(int i=0;i<array.length();i++){
            JSONObject entry=array.getJSONObject(i);
            String id=entry.optString("id");
            if(entry.optString("nombre_es").isEmpty()
                    ||entry.optJSONArray("instrucciones_es")==null
                    ||entry.optBoolean("gif_incluido",true)
                    ||entry.optBoolean("imagen_incluida",true)
                    ||entry.optBoolean("apto_para_rutinas_automaticas",true)
                    ||!id.matches("[a-z0-9_-]+")
                    ||ids.put(id,Boolean.TRUE)!=null)
                throw new IllegalArgumentException("Ficha de importación inválida");
            result.add(entry);
        }
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
            summary.setText("Cargando catálogo unificado...");
            return;
        }
        long originalCount=items.stream().filter(x->
                "asterion".equals(x.optString("origen"))).count();
        summary.setText(matches.size()+" coincidencias. Catálogo: "
                +originalCount+" originales y "
                +(items.size()-originalCount)+" incorporados sin coincidencia probable. "
                +"Cada variante dispone de una ficha de especificaciones.");
        if(matches.isEmpty()){
            list.addView(text("No hay coincidencias. Prueba otra categoría.",14));
            return;
        }
        int limit=Math.min(matches.size(),(page+1)*PAGE_SIZE);
        for(int i=0;i<limit;i++) {
            final JSONObject x=matches.get(i);
            Button b=button(("asterion".equals(x.optString("origen"))
                    ?"Asterion · ":"Nuevo · ")+x.optString("nombre_es"));
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

    private Map<String,JSONObject> loadMotionSpecs(List<JSONObject> exercises) throws Exception {
        byte[] bytes;
        try(InputStream stream=getAssets().open(MOTION_ASSET);
            ByteArrayOutputStream buffer=new ByteArrayOutputStream()) {
            byte[] part=new byte[16384];
            int read;
            while((read=stream.read(part))!=-1) buffer.write(part,0,read);
            bytes=buffer.toByteArray();
        }
        JSONObject manifest=new JSONObject(new String(bytes,StandardCharsets.UTF_8));
        if(!"asterion.exercise-motion.es.v2".equals(manifest.optString("schema"))
                ||manifest.optInt("total")!=EXERCISE_COUNT
                ||manifest.optInt("gifs_fisicos")!=EXERCISE_COUNT
                ||manifest.optInt("gifs_pendientes")!=0)
            throw new IllegalArgumentException("El paquete de GIF no está completo");
        JSONArray specs=manifest.getJSONArray("ejercicios");
        if(specs.length()!=EXERCISE_COUNT)
            throw new IllegalArgumentException("Rutas GIF incompletas");
        Map<String,JSONObject> records=new HashMap<>(EXERCISE_COUNT);
        for(int i=0;i<specs.length();i++) {
            JSONObject entry=specs.getJSONObject(i);
            String id=entry.getString("id");
            if(!id.matches("[a-z0-9_-]+")
                    ||!entry.optString("gif_ruta").equals("exercise_gifs/v10/"+id+".gif")
                    ||entry.optBoolean("gif_validado",true)
                    ||records.put(id,entry)!=null)
                throw new IllegalArgumentException("GIF de variante repetido o inválido: "+id);
        }
        if(records.size()!=exercises.size())
            throw new IllegalArgumentException("Catálogo y GIFs no tienen el mismo tamaño");
        for(JSONObject exercise:exercises) {
            if(!records.containsKey(exercise.optString("id")))
                throw new IllegalArgumentException("Falta GIF para "+exercise.optString("id"));
        }
        return records;
    }

    private void details(JSONObject x) {
        JSONObject spec=motionSpecs.get(x.optString("id"));
        if(spec==null) {
            new AlertDialog.Builder(this)
                    .setTitle("Ficha no disponible")
                    .setMessage("El cuadro técnico de esta variante no se ha cargado. "
                            +"No se mostrará una demostración de otro ejercicio.")
                    .setPositiveButton("Cerrar",null)
                    .show();
            return;
        }
        Intent details=new Intent(this,ExerciseMotionDetailActivity.class);
        details.putExtra(ExerciseMotionDetailActivity.EXTRA_EXERCISE,x.toString());
        details.putExtra(ExerciseMotionDetailActivity.EXTRA_MOTION_SPEC,spec.toString());
        startActivity(details);
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
