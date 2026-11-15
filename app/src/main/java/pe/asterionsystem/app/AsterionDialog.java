package pe.asterionsystem.app;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

/** Native dialogs inherit the same accessible colors and global bold typography. */
public final class AsterionDialog extends AlertDialog.Builder {
    private final Context owner;
    public AsterionDialog(Context context){super(context);owner=context;}
    @Override public AlertDialog show(){
        AlertDialog dialog=super.show();
        View decor=dialog.getWindow().getDecorView();
        boolean light=AppTheme.isLight(owner);
        decor.getViewTreeObserver().addOnGlobalLayoutListener(()->AppTheme.apply(decor,light));
        AppTheme.apply(decor,light);
        return dialog;
    }
}
