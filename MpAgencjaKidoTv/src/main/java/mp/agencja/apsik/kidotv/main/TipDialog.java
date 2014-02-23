package mp.agencja.apsik.kidotv.main;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import mp.agencja.apsik.kidotv.R;

public class TipDialog extends Dialog {

    public TipDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tip_dialog);
        RelativeLayout tip_bear = (RelativeLayout)findViewById(R.id.tip_bear);
        tip_bear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TipDialog.this.dismiss();
            }
        });
        WindowManager.LayoutParams params = getWindow().getAttributes();

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;

        getWindow().setAttributes(params);

    }
}
