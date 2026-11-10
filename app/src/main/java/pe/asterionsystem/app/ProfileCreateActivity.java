package pe.asterionsystem.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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

/**
 * Four existing registration stages in two simple native color themes.
 * The original option keys, validation, draft and profile storage remain
 * unchanged. Screenshots are never used as form backgrounds or touch targets.
 */
public final class ProfileCreateActivity extends ComponentActivity {
    private JSONObject draft;
    private int step=1;
    private boolean confirmed=false;
    private float density;

    private FrameLayout root;
    private LinearLayout panel;
    private LinearLayout firstStep;
    private LinearLayout dynamic;
    private ScrollView scroll;
    private View forward;
    private Button back;
    private TextView forwardText;
    private TextView heading;
    private TextView indicator;
    private EditText name;
    private EditText age;
    private EditText height;
    private EditText weight;
    private ImageView avatar;
    private RadioGroup gender;

    private final ActivityResultLauncher<String[]> pickImage =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if(uri==null)return;
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    put("avatar_uri",uri.toString());
                    avatar.setImageURI(uri);
                }catch(SecurityException error){
                    Toast.makeText(this,"No se pudo conservar el acceso a la imagen.",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.screen_profile_create);
        density=getResources().getDisplayMetrics().density;
        draft=ProfileRegistrationDraft.load(this);
        if (!draft.has("theme")) put("theme","light");

        root=findViewById(R.id.profile_wizard_root);
        panel=findViewById(R.id.profile_create_panel);
        firstStep=findViewById(R.id.profile_first_step);
        dynamic=findViewById(R.id.profile_dynamic_step);
        scroll=findViewById(R.id.profile_wizard_scroll);
        heading=findViewById(R.id.profile_step_heading);
        indicator=findViewById(R.id.profile_step_indicator);
        forward=findViewById(R.id.profile_create_submit);
        back=findViewById(R.id.profile_back);
        forwardText=findViewById(R.id.profile_forward_text);
        name=findViewById(R.id.profile_name_input);
        age=findViewById(R.id.profile_age_input);
        height=findViewById(R.id.profile_height_input);
        weight=findViewById(R.id.profile_weight_input);
        avatar=findViewById(R.id.profile_avatar_preview);
        gender=findViewById(R.id.profile_gender_group);

        findViewById(R.id.profile_choose_image).setOnClickListener(v ->
                pickImage.launch(new String[]{"image/*"}));
        forward.setOnClickListener(v -> next());
        back.setOnClickListener(v -> {
            if(step>1 && step<=4)showStep(step-1);
        });

        loadStepOne();
        int stage=state==null?1:Math.max(1,Math.min(5,state.getInt("stage",1)));
        // After activation, restore the success screen instead of reopening
        // an unconfirmed final form if Android recreates this Activity.
        if(stage==5 && ProfileStore.hasProfile(this))showActivated();
        else showStep(Math.min(4,stage));
    }

    private void applyTheme() {
        boolean light="light".equals(draft.optString("theme","light"));
        ProfileWizardTheme.applyWizard(root,panel,forward,forwardText,
                light,density);
    }

    private void loadStepOne() {
        name.setText(draft.optString("name",""));
        age.setText(draft.optString("age",""));
        height.setText(draft.optString("height",""));
        weight.setText(draft.optString("weight",""));
        String selected=draft.optString("gender","unspecified");
        gender.check("masculino".equals(selected)?R.id.profile_gender_male:
                "femenino".equals(selected)?R.id.profile_gender_female:
                        R.id.profile_gender_unspecified);
        String photo=draft.optString("avatar_uri","");
        if(!photo.isEmpty()) {
            try{avatar.setImageURI(Uri.parse(photo));}
            catch(RuntimeException invalid){avatar.setImageResource(R.drawable.asterion_reg_profile);}
        }
    }

    private void put(String key,Object value) {
        try {
            draft.put(key,value);
            ProfileRegistrationDraft.save(this,draft);
        }catch(JSONException failure){throw new IllegalStateException(failure);}
    }

    private boolean saveStepOne(boolean validate) {
        String hunterName=name.getText().toString().trim();
        String rawAge=age.getText().toString().trim();
        if(validate && (hunterName.isEmpty() || hunterName.length()>40)) {
            name.setError("Introduce un nombre de cazador válido.");
            name.requestFocus();
            return false;
        }
        int years=0;
        try{years=Integer.parseInt(rawAge);}
        catch(NumberFormatException failure){
            if(validate){age.setError("Introduce una edad válida.");age.requestFocus();return false;}
        }
        if(validate && (years<1 || years>120)){
            age.setError("Comprueba la edad introducida.");
            age.requestFocus();
            return false;
        }
        if(validate && !validOptionalNumber(height,30d,270d,"estatura")
                || validate && !validOptionalNumber(weight,1d,350d,"peso"))return false;

        put("name",hunterName);
        put("age",rawAge);
        put("height",height.getText().toString().trim());
        put("weight",weight.getText().toString().trim());
        int checked=gender.getCheckedRadioButtonId();
        put("gender", checked==R.id.profile_gender_male?"masculino":
                checked==R.id.profile_gender_female?"femenino":"unspecified");
        return true;
    }

    private boolean validOptionalNumber(EditText input,double min,double max,String label) {
        String raw=input.getText().toString().trim();
        if(raw.isEmpty())return true;
        try {
            double value=Double.parseDouble(raw);
            if(Double.isFinite(value) && value>=min && value<=max)return true;
        }catch(NumberFormatException ignored){}
        input.setError("Comprueba el valor de "+label+".");
        input.requestFocus();
        return false;
    }

    private void showStep(int target) {
        if(step==1 && target!=1)saveStepOne(false);
        step=target;
        confirmed=false;
        firstStep.setVisibility(step==1?View.VISIBLE:View.GONE);
        dynamic.setVisibility(step==1?View.GONE:View.VISIBLE);
        dynamic.removeAllViews();
        back.setVisibility(step==1?View.GONE:View.VISIBLE);
        String[] titles={"","Identidad del cazador","Entrenamiento",
                "Personalización","Activación del sistema"};
        heading.setText(titles[step]);
        indicator.setText(step+" / 4");
        forwardText.setText(step==4?"ACTIVAR PERFIL":"SIGUIENTE");
        forward.setEnabled(step!=4);
        forward.setAlpha(step==4?0.5f:1f);

        if(step>1) {
            ProfileRegistrationSteps forms=new ProfileRegistrationSteps(
                    this,draft,()->ProfileRegistrationDraft.save(this,draft),
                    this::onConfirmationChanged);
            if(step==2)dynamic.addView(forms.step2());
            if(step==3)dynamic.addView(new SimpleProfileStepThreeView(
                    this,draft,()->ProfileRegistrationDraft.save(this,draft),
                    this::applyTheme));
            if(step==4)dynamic.addView(forms.step4());
        }
        applyTheme();
        scroll.post(()->scroll.smoothScrollTo(0,0));
    }

    private void onConfirmationChanged(boolean checked) {
        confirmed=checked;
        if(step==4) {
            forward.setEnabled(checked);
            forward.setAlpha(checked?1f:0.5f);
        }
    }

    private void next() {
        if(step==1){
            if(!saveStepOne(true))return;
            showStep(2);
            return;
        }
        if(step==2){showStep(3);return;}
        if(step==3){showStep(4);return;}
        if(step==4 && confirmed){
            if(!ProfileStore.saveRegistration(this,draft)) {
                Toast.makeText(this,"No se pudo guardar el perfil. Comprueba los campos.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            ProfileRegistrationDraft.clear(this);
            showActivated();
        }else if(step==5){
            Intent home=new Intent(this,MainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(home);
            finish();
        }
    }

    private void showActivated() {
        step=5;
        firstStep.setVisibility(View.GONE);
        dynamic.setVisibility(View.VISIBLE);
        dynamic.removeAllViews();
        back.setVisibility(View.GONE);
        heading.setText("Sistema activado");
        indicator.setText("4 / 4");
        TextView banner=new TextView(this);
        banner.setText("Registro completado.\n\n"
                +"Tu perfil se guardó correctamente en este dispositivo.");
        banner.setTextSize(17);
        banner.setGravity(Gravity.CENTER);
        banner.setPadding(dp(12),dp(24),dp(12),dp(24));
        dynamic.addView(banner,new LinearLayout.LayoutParams(-1,-2));
        forwardText.setText("ENTRAR AL SISTEMA");
        forward.setEnabled(true);
        forward.setAlpha(1f);
        applyTheme();
        scroll.post(()->scroll.smoothScrollTo(0,0));
    }

    private int dp(int n){return Math.round(n*density);}

    @Override protected void onSaveInstanceState(Bundle saved) {
        if(step==1)saveStepOne(false);
        saved.putInt("stage",step);
        super.onSaveInstanceState(saved);
    }
}
