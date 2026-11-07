package pe.asterionsystem.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import org.json.JSONException;
import org.json.JSONObject;
import pe.asterionsystem.app.presentation.NeonFrameDrawable;

/**
 * Native four-stage profile registration. The approved Step 1 contains separate
 * compiled scenery, hunter, icons and scalable neon frame. Steps 2–4 use native
 * controls and remain visually extensible without changing the draft contract.
 */
public final class ProfileCreateActivity extends ComponentActivity {
    private JSONObject draft;
    private CanvaProfileStepOneView canvaStepOne;
    private CanvaProfileStepTwoView canvaStepTwo;
    private int step = 1;
    private boolean confirmed = false;
    private EditText name;
    private EditText age;
    private EditText height;
    private EditText weight;
    private ImageView avatar;
    private RadioGroup gender;
    private LinearLayout panel;
    private View firstStep;
    private LinearLayout dynamic;
    private View forward;
    private View back;
    private TextView forwardText;
    private TextView heading;
    private TextView indicator;
    private ScrollView scroll;
    private float density;

    private final ActivityResultLauncher<String[]> pickImage =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    put("avatar_uri", uri.toString());
                    avatar.setImageURI(uri);
                    if (canvaStepOne != null) canvaStepOne.showSelectedAvatar(uri);
                } catch (SecurityException invalidGrant) {
                    Toast.makeText(this, "No se pudo conservar el acceso a la imagen.",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.screen_profile_create);
        density = getResources().getDisplayMetrics().density;
        draft = ProfileRegistrationDraft.load(this);

        panel = findViewById(R.id.profile_create_panel);
        firstStep = findViewById(R.id.profile_first_step);
        dynamic = findViewById(R.id.profile_dynamic_step);
        scroll = findViewById(R.id.profile_wizard_scroll);
        heading = findViewById(R.id.profile_step_heading);
        indicator = findViewById(R.id.profile_step_indicator);
        forward = findViewById(R.id.profile_create_submit);
        back = findViewById(R.id.profile_back);
        forwardText = findViewById(R.id.profile_forward_text);
        name = findViewById(R.id.profile_name_input);
        age = findViewById(R.id.profile_age_input);
        height = findViewById(R.id.profile_height_input);
        weight = findViewById(R.id.profile_weight_input);
        avatar = findViewById(R.id.profile_avatar_preview);
        gender = findViewById(R.id.profile_gender_group);

        float d = density;
        panel.setBackground(new NeonFrameDrawable(Color.rgb(80,196,255),d));
        findViewById(R.id.profile_choose_image).setBackground(
                new NeonFrameDrawable(Color.rgb(80,196,255),d));
        forward.setBackground(new NeonFrameDrawable(Color.rgb(177,116,255),d));

        findViewById(R.id.profile_choose_image)
                .setOnClickListener(v -> pickImage.launch(new String[]{"image/*"}));
        forward.setOnClickListener(v -> next());
        back.setOnClickListener(v -> {
            if (step > 1 && step <= 4) showStep(step - 1);
        });

        loadStepOne();
        canvaStepOne = new CanvaProfileStepOneView(this,
                name, age, height, weight, gender,
                this::next,
                () -> pickImage.launch(new String[]{"image/*"}));
        ((FrameLayout) findViewById(R.id.profile_wizard_root)).addView(
                canvaStepOne, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        canvaStepTwo = new CanvaProfileStepTwoView(this, draft,
                () -> ProfileRegistrationDraft.save(this,draft),
                () -> showStep(1), this::next);
        ((FrameLayout) findViewById(R.id.profile_wizard_root)).addView(
                canvaStepTwo, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        canvaStepTwo.setVisibility(View.GONE);
        String savedPhoto = draft.optString("avatar_uri", "");
        if (!savedPhoto.isEmpty()) canvaStepOne.showSelectedAvatar(Uri.parse(savedPhoto));
        showStep(state == null ? 1 : Math.max(1,Math.min(4,state.getInt("stage",1))));
    }

    private void loadStepOne() {
        name.setText(draft.optString("name",""));
        age.setText(draft.optString("age",""));
        height.setText(draft.optString("height",""));
        weight.setText(draft.optString("weight",""));
        String g=draft.optString("gender","unspecified");
        gender.check("masculino".equals(g)?R.id.profile_gender_male
                :"femenino".equals(g)?R.id.profile_gender_female
                :R.id.profile_gender_unspecified);
        String uri=draft.optString("avatar_uri","");
        if(!uri.isEmpty())try{avatar.setImageURI(Uri.parse(uri));}
        catch(RuntimeException ignored){
            avatar.setImageResource(AsterionRegistrationIcons.drawable(
                    AsterionRegistrationIcons.Icon.PROFILE));
        }
    }

    private void put(String key, Object value) {
        try {
            draft.put(key,value);
            ProfileRegistrationDraft.save(this,draft);
        } catch (JSONException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private boolean saveStepOne(boolean validate) {
        String requestedName=name.getText().toString().trim();
        String rawAge=age.getText().toString().trim();
        if(validate && (requestedName.isEmpty() || requestedName.length()>40)){
            name.setError("Introduce un nombre de cazador válido.");
            name.requestFocus(); return false;
        }
        int years=0;
        try { years=Integer.parseInt(rawAge); }
        catch(NumberFormatException invalid) {
            if(validate){age.setError("Introduce una edad válida.");age.requestFocus();return false;}
        }
        if(validate && (years<1 || years>120)){
            age.setError("Comprueba la edad introducida.");age.requestFocus();return false;
        }
        if(validate && !validOptionalNumber(height,30d,270d,"estatura")
                || validate && !validOptionalNumber(weight,1d,350d,"peso"))return false;
        put("name",requestedName);
        put("age",rawAge);
        put("height",height.getText().toString().trim());
        put("weight",weight.getText().toString().trim());
        int checked=gender.getCheckedRadioButtonId();
        put("gender", checked==R.id.profile_gender_male?"masculino"
                :checked==R.id.profile_gender_female?"femenino":"unspecified");
        return true;
    }

    private boolean validOptionalNumber(EditText input,double min,double max,String label) {
        String raw=input.getText().toString().trim();
        if(raw.isEmpty())return true;
        try {
            double amount=Double.parseDouble(raw);
            if(Double.isFinite(amount) && amount>=min && amount<=max)return true;
        }catch(NumberFormatException ignored){ }
        input.setError("Comprueba el valor de "+label+".");
        input.requestFocus();
        return false;
    }

    private void showStep(int requestedStep) {
        if(step==1 && requestedStep!=1)saveStepOne(false);
        step=requestedStep;
        if (canvaStepOne != null) canvaStepOne.setVisibility(
                step == 1 ? View.VISIBLE : View.GONE);
        if (canvaStepTwo != null) canvaStepTwo.setVisibility(
                step == 2 ? View.VISIBLE : View.GONE);
        confirmed=false;
        applyRegisteredScenery(step);
        firstStep.setVisibility(step==1 && canvaStepOne==null?View.VISIBLE:View.GONE);
        dynamic.setVisibility(step==1?View.GONE:View.VISIBLE);
        dynamic.removeAllViews();
        back.setVisibility(step==1?View.GONE:View.VISIBLE);
        String[] titles={"","PASO 1 DE 4 · IDENTIDAD DEL CAZADOR",
                "PASO 2 DE 4 · ENTRENAMIENTO",
                "PASO 3 DE 4 · PERSONALIZACIÓN",
                "PASO 4 DE 4 · ACTIVACIÓN DEL SISTEMA"};
        heading.setText(titles[step]);
        // Step 3 uses the approved gold-and-navy visual language. Preserve
        // the independent artwork and behavior of steps 1, 2 and 4.
        if (step == 3) {
            panel.setBackground(new NeonFrameDrawable(Color.rgb(244, 186, 93),density));
            heading.setTextColor(Color.rgb(255, 211, 145));
            heading.setTypeface(Typeface.SERIF,Typeface.BOLD);
            forward.setBackground(new NeonFrameDrawable(Color.rgb(255, 196, 102),density));
            back.setBackground(new NeonFrameDrawable(Color.rgb(100, 170, 255),density));
        } else {
            panel.setBackground(new NeonFrameDrawable(Color.rgb(80,196,255),density));
            heading.setTextColor(Color.WHITE);
            heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            forward.setBackground(new NeonFrameDrawable(Color.rgb(177,116,255),density));
        }

        indicator.setText(step==1?"①  —  ②  —  ③  —  ④"
                :step==2?"✓  —  ②  —  ③  —  ④"
                :step==3?"✓  —  ✓  —  ③  —  ④"
                :"✓  —  ✓  —  ✓  —  ④");
        forwardText.setText(step==4?"ACTIVAR PERFIL":"SIGUIENTE");
        forward.setEnabled(step!=4);
        forward.setAlpha(step==4?0.5f:1.0f);

        if(step>1) {
            ProfileRegistrationSteps factory=new ProfileRegistrationSteps(
                    this,draft,()->ProfileRegistrationDraft.save(this,draft),
                    this::onConfirmationChanged);
            if(step==2 && canvaStepTwo==null)dynamic.addView(factory.step2());
            if(step==3)dynamic.addView(factory.step3());
            if(step==4)dynamic.addView(factory.step4());
        }
        scroll.post(()->scroll.smoothScrollTo(0,0));
    }

    private void onConfirmationChanged(boolean checked) {
        confirmed=checked;
        if(step==4){
            forward.setEnabled(checked);
            forward.setAlpha(checked?1.0f:0.5f);
        }
    }

    private void next() {
        if(step==1) {
            if(!saveStepOne(true))return;
            showStep(2);
            return;
        }
        if(step==2){showStep(3);return;}
        if(step==3){showStep(4);return;}
        if(step==4 && confirmed){
            if(!ProfileStore.saveRegistration(this,draft)){
                Toast.makeText(this,"No se pudo guardar el perfil. Comprueba los campos.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            ProfileRegistrationDraft.clear(this);
            showActivated();
        } else if(step==5) {
            Intent home=new Intent(this,MainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(home);
            finish();
        }
    }

    private void showActivated() {
        step=5;
        applyRegisteredScenery(5);
        firstStep.setVisibility(View.GONE);
        dynamic.setVisibility(View.VISIBLE);
        dynamic.removeAllViews();
        back.setVisibility(View.GONE);
        heading.setText("SISTEMA ACTIVADO");
        indicator.setText("✓  —  ✓  —  ✓  —  ✓");
        TextView banner=new TextView(this);
        banner.setText("✦ REGISTRO COMPLETADO ✦\n\nBienvenido a Asterion System\n\n"
                +"Tu identidad de cazador ha sido establecida.\n\n"
                +"La configuración se guardó correctamente en este dispositivo.");
        banner.setTextColor(Color.WHITE);
        banner.setTextSize(19);
        banner.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        banner.setGravity(android.view.Gravity.CENTER);
        banner.setPadding(dp(12),dp(35),dp(12),dp(35));
        dynamic.addView(banner,new LinearLayout.LayoutParams(-1,-2));
        forwardText.setText("ENTRAR AL SISTEMA");
        forward.setEnabled(true);
        forward.setAlpha(1f);
        scroll.post(()->scroll.smoothScrollTo(0,0));
    }

    /**
     * Drawable resources are packaged at build time. On builds that predate
     * the approved PNG/WebP uploads, the existing local graphics are used.
     * This lookup never generates, downloads, tints or alters image files.
     */
    private int registeredDrawable(String name, int fallback) {
        int id = getResources().getIdentifier(name, "drawable", getPackageName());
        return id == 0 ? fallback : id;
    }

    private void applyRegisteredScenery(int shownStep) {
        ImageView scenery = findViewById(R.id.profile_scenery);
        int background = R.drawable.asterion_reg_background;
        if (shownStep >= 2) {
            String asset = shownStep == 2 ? "asterion_step2_background"
                    : shownStep == 3 ? "asterion_step3_background"
                    : "asterion_step4_background";
            background = registeredDrawable(asset, background);
        }
        scenery.setImageResource(background);

        ImageView hunter = findViewById(R.id.profile_hunter_art);
        int hunterArt=shownStep == 3
                ? registeredDrawable("asterion_step3_cha_haein",
                        registeredDrawable("asterion_hunter_lateral",
                                R.drawable.asterion_guardian))
                : registeredDrawable("asterion_hunter_lateral",R.drawable.asterion_guardian);
        hunter.setImageResource(shownStep == 1
                ? R.drawable.asterion_guardian : hunterArt);
        hunter.setVisibility(shownStep == 4 || shownStep == 5
                ? View.GONE : View.VISIBLE);
    }

    private int dp(int value){return Math.round(value*density);}

    @Override protected void onSaveInstanceState(Bundle saved) {
        if(step==1)saveStepOne(false);
        saved.putInt("stage",Math.min(4,step));
        super.onSaveInstanceState(saved);
    }
}
