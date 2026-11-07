package pe.asterionsystem.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.util.Locale;

/**
 * Approved Canva design DAHaL0a4BxM: functional native overlays over
 * a packaged 941 x 1672 illustration. No downloads occur at app runtime.
 * All profile information is captured by the original validated widgets.
 * Existing stages 2-4 and registration storage remain unchanged.
 */
final class CanvaProfileStepOneView extends FrameLayout {
    private static final float ART_W = 941f;
    private static final float ART_H = 1672f;
    private final FrameLayout design;
    private final RadioGroup originalGender;
    private final ImageView chosenPhoto;
    private final TextView[] choices = new TextView[3];

    CanvaProfileStepOneView(Activity activity,
                           EditText name, EditText age, EditText height,
                           EditText weight, RadioGroup gender,
                           Runnable onNext, Runnable onPhoto) {
        super(activity);
        setBackgroundColor(Color.rgb(6, 15, 36));
        originalGender = gender;
        design = new FrameLayout(activity);
        addView(design, new FrameLayout.LayoutParams((int) ART_W, (int) ART_H));

        ImageView artwork = new ImageView(activity);
        int artId = activity.getResources().getIdentifier(
                "asterion_canva_paso1", "drawable", activity.getPackageName());
        artwork.setImageResource(artId == 0 ? R.drawable.asterion_reg_background : artId);
        artwork.setScaleType(ImageView.ScaleType.FIT_XY);
        artwork.setContentDescription(null);
        design.addView(artwork, rect(0, 0, ART_W, ART_H));

        input(activity, name, 407, 514, 468, 52, "Ingresa tu nombre",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, 40, false);
        input(activity, age, 407, 628, 468, 49, "Ingresa tu edad",
                InputType.TYPE_CLASS_NUMBER, 3, false);
        input(activity, height, 407, 737, 468, 52, "Ingresa tu estatura (ej. 1.75 m)",
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, 7, true);
        input(activity, weight, 407, 849, 468, 51, "Ingresa tu peso (ej. 70 kg)",
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, 7, false);

        for (int i = 0; i < 3; i++) {
            final int selected = i;
            TextView option = new TextView(activity);
            option.setText(i == 0 ? "♂\nMasculino" : i == 1
                    ? "♀\nFemenino" : "⊖\nNo especificado");
            option.setTextColor(i == 0 ? Color.rgb(157, 222, 250) :
                    i == 1 ? Color.rgb(250, 176, 211) : Color.WHITE);
            option.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
            option.setTextSize(TypedValue.COMPLEX_UNIT_PX, i == 2 ? 22 : 26);
            option.setGravity(Gravity.CENTER);
            option.setContentDescription(i == 0 ? "Masculino" :
                    i == 1 ? "Femenino" : "No especificado");
            option.setClickable(true);
            option.setFocusable(true);
            choices[i] = option;
            int x = i == 0 ? 394 : i == 1 ? 557 : 723;
            int w = i == 0 ? 152 : 151;
            design.addView(option, rect(x, 954, w, 105));
            option.setOnClickListener(v -> {
                originalGender.check(selected == 0 ? R.id.profile_gender_male
                        : selected == 1 ? R.id.profile_gender_female
                        : R.id.profile_gender_unspecified);
                updateSelection();
            });
        }
        originalGender.setOnCheckedChangeListener((g, id) -> updateSelection());
        updateSelection();

        chosenPhoto = new ImageView(activity);
        chosenPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        chosenPhoto.setClipToOutline(true);
        chosenPhoto.setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        chosenPhoto.setVisibility(View.GONE);
        design.addView(chosenPhoto, rect(303, 1101, 155, 155));

        View choosePhoto = new View(activity);
        choosePhoto.setContentDescription("Elegir fotografía de perfil");
        choosePhoto.setClickable(true);
        choosePhoto.setFocusable(true);
        design.addView(choosePhoto, rect(506, 1122, 365, 117));
        choosePhoto.setOnClickListener(v -> onPhoto.run());

        View continueButton = new View(activity);
        continueButton.setContentDescription("Siguiente paso de registro");
        continueButton.setClickable(true);
        continueButton.setFocusable(true);
        design.addView(continueButton, rect(270, 1366, 608, 120));
        continueButton.setOnClickListener(v -> onNext.run());
    }

    void showSelectedAvatar(Uri uri) {
        if (uri == null) return;
        try {
            chosenPhoto.setImageURI(uri);
            chosenPhoto.setVisibility(VISIBLE);
        } catch (RuntimeException invalid) {
            chosenPhoto.setVisibility(GONE);
        }
    }

    private void updateSelection() {
        int current = originalGender.getCheckedRadioButtonId();
        int selected = current == R.id.profile_gender_male ? 0
                : current == R.id.profile_gender_female ? 1 : 2;
        for (int i = 0; i < choices.length; i++) {
            GradientDrawable background = new GradientDrawable();
            background.setCornerRadius(14f);
            background.setColor(i == selected
                    ? Color.rgb(9, 63, 93) : Color.rgb(16, 30, 48));
            background.setStroke(i == selected ? 3 : 2,
                    i == selected ? Color.rgb(90, 206, 247) : Color.rgb(91, 109, 134));
            choices[i].setBackground(background);
            choices[i].setSelected(i == selected);
        }
    }

    private void input(Activity activity, EditText original, int x, int y, int w, int h,
                       String hint, int inputType, int maxLength, boolean meters) {
        EditText edit = new EditText(activity);
        edit.setSingleLine(true);
        edit.setInputType(inputType);
        edit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 27);
        edit.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        edit.setTextColor(Color.rgb(244, 247, 255));
        edit.setHintTextColor(Color.rgb(177, 194, 213));
        edit.setHint(hint);
        edit.setPadding(13, 0, 8, 0);
        edit.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        edit.setSelectAllOnFocus(false);
        edit.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(maxLength)});
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(13, 27, 46));
        background.setCornerRadius(9f);
        background.setStroke(2, Color.rgb(105, 126, 156));
        edit.setBackground(background);
        edit.setContentDescription(hint);
        String current = original.getText().toString();
        if (meters && !current.trim().isEmpty()) {
            try {
                double cm = Double.parseDouble(current);
                if (cm >= 30d) current = String.format(Locale.US, "%.2f", cm / 100d);
            } catch (NumberFormatException ignored) { }
        }
        edit.setText(current);
        edit.setSelection(edit.getText().length());
        edit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = s.toString().trim().replace(',', '.');
                if (meters && !value.isEmpty()) {
                    try {
                        double number = Double.parseDouble(value);
                        if (number > 0 && number < 3d)
                            value = String.format(Locale.US, "%.2f", number * 100d);
                    } catch (NumberFormatException ignored) { }
                }
                if (!value.equals(original.getText().toString())) original.setText(value);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        design.addView(edit, rect(x, y, w, h));
    }

    private static FrameLayout.LayoutParams rect(float x, float y, float w, float h) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) w, (int) h);
        params.leftMargin = (int) x;
        params.topMargin = (int) y;
        return params;
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float scale = Math.min(w / ART_W, h / ART_H);
        design.setPivotX(0f);
        design.setPivotY(0f);
        design.setScaleX(scale);
        design.setScaleY(scale);
        design.setX((w - ART_W * scale) / 2f);
        design.setY((h - ART_H * scale) / 2f);
    }
}
