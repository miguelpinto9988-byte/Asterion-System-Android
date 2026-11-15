package pe.asterionsystem.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Profile stays in the upper-right corner; appearance settings live inside profile. */
public final class AppHeader {
    private AppHeader() {}
    public static LinearLayout create(Activity a,String title,String subtitle) {
        LinearLayout row=new LinearLayout(a);row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,AppTheme.dp(a,4),0,AppTheme.dp(a,12));
        LinearLayout copy=new LinearLayout(a);copy.setOrientation(LinearLayout.VERTICAL);
        TextView brand=new TextView(a);brand.setText("ASTERION / SYSTEM");brand.setTextSize(11);brand.setTag("overline");
        copy.addView(brand);
        TextView name=new TextView(a);name.setText(title);name.setTextSize(25);
        copy.addView(name);
        if(subtitle!=null&&!subtitle.isEmpty()) {
            TextView caption=new TextView(a);caption.setText(subtitle);caption.setTextSize(13);caption.setTag("muted");
            caption.setPadding(0,AppTheme.dp(a,4),0,0);copy.addView(caption);
        }
        row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        ImageButton avatar=new ImageButton(a);avatar.setTag("avatar");
        avatar.setPadding(AppTheme.dp(a,12),AppTheme.dp(a,12),AppTheme.dp(a,12),AppTheme.dp(a,12));
        avatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        bindAvatar(a,avatar);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(AppTheme.dp(a,52),AppTheme.dp(a,52));
        lp.leftMargin=AppTheme.dp(a,12);row.addView(avatar,lp);
        AppTheme.apply(row,AppTheme.isLight(a));return row;
    }
    public static void bindAvatar(Activity a,ImageButton avatar) {
        if(avatar.getId()==View.NO_ID)avatar.setId(R.id.asterion_profile_entry);
        avatar.setTag("avatar");avatar.setImageResource(R.drawable.ic_as_profile);
        String uri=ProfileStore.getRegistration(a).optString("avatar_uri","");
        if(!uri.isEmpty())try {
            avatar.setImageURI(Uri.parse(uri));
            if(avatar.getDrawable()==null)avatar.setImageResource(R.drawable.ic_as_profile);
            else avatar.setPadding(0,0,0,0);
        }catch(RuntimeException ignored){avatar.setImageResource(R.drawable.ic_as_profile);}
        avatar.setContentDescription("Abrir perfil y configuración");
        avatar.setOnClickListener(v->a.startActivity(new Intent(a,ProfileDetailsActivity.class)));
    }
}
