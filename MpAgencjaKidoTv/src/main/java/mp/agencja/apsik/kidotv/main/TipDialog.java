package mp.agencja.apsik.kidotv.main;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import mp.agencja.apsik.kidotv.R;

public class TipDialog extends Dialog {

    private final Context context;
    private final int width;
    private final SharedPreferences sharedPreferences;

    public TipDialog(Context context, int theme, int width, SharedPreferences sharedPreferences) {
        super(context, theme);
        this.context = context;
        this.width = width;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tip_dialog);
        RelativeLayout tip_bear = (RelativeLayout) findViewById(R.id.tip_bear);
        tip_bear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TipDialog.this.dismiss();
            }
        });

        ImageView yellow_arrow = (ImageView) findViewById(R.id.yellow_arrow);
        yellow_arrow.setY(width / 2);
        yellow_arrow.setX(yellow_arrow.getX() - width);

        final CheckBox checkBox = (CheckBox) findViewById(R.id.tip_check_box);
        final Typeface typeface = Typeface.createFromAsset(context.getAssets(), "font.TTF");
        checkBox.setTypeface(typeface);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("show_tip", 1);
                    editor.commit();
                }else{
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("show_tip", 0);
                    editor.commit();
                }
            }
        });

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

    }
}