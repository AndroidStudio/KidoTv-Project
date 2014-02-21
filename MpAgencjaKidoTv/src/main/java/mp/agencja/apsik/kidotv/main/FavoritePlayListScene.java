package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import mp.agencja.apsik.kidotv.R;

public class FavoritePlayListScene extends Activity implements AdapterView.OnItemClickListener {
    private ListViewAdapter listViewAdapter;
    private EditText headerView;
    private InputMethodManager inputMethodManager;
    private final StringBuilder builder = new StringBuilder();
    private Handler handler = new Handler();
    private ImageView buyPremiumScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favoriteplaylist_scene);
        if (!PlayListScene.database.isOpen()) {
            PlayListScene.database.openToWrite();
        }

        buyPremiumScene = (ImageView) findViewById(R.id.image_buy_premium_full_scene);
        final int width = buyPremiumScene.getDrawable().getIntrinsicWidth();
        buyPremiumScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(FavoritePlayListScene.this, "Buy premium", Toast.LENGTH_SHORT).show();
            }
        });


        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnTouchListener(onBackTouchListener);

        ImageButton showAll = (ImageButton) findViewById(R.id.showAll);
        showAll.setOnTouchListener(onShowAllTouchListener);

        ImageButton displayFavorites = (ImageButton) findViewById(R.id.favorites);
        displayFavorites.setOnTouchListener(onDisplayFavoritesTouchListener);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "font.TTF");
        headerView = (EditText) findViewById(R.id.headerview);
        headerView.setCursorVisible(false);
        headerView.setTypeface(typeface);
        headerView.setMaxWidth(headerView.getWidth());
        headerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, width / 21);
        headerView.addTextChangedListener(textWatcher);
        headerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    headerView.clearFocus();
                    headerView.requestFocus();
                    return true;
                }
                return false;
            }
        });
        ListView listView = (ListView) findViewById(R.id.listView);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, buyPremiumScene.getDrawable().getIntrinsicHeight());
        params.addRule(RelativeLayout.BELOW, headerView.getId());
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        listView.setLayoutParams(params);
        listView.setItemsCanFocus(true);

        listViewAdapter = new ListViewAdapter(this);
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(this);
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            listViewAdapter.searchItems(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent KEvent) {
        int keyaction = KEvent.getAction();
        if (keyaction == KeyEvent.ACTION_DOWN) {
            int keycode = KEvent.getKeyCode();
            if (keycode == KeyEvent.KEYCODE_ENTER) {
                headerView.removeTextChangedListener(textWatcher);
                headerView.setText("");
                headerView.addTextChangedListener(textWatcher);
                return false;
            }

        }
        return super.dispatchKeyEvent(KEvent);
    }

    private final ImageButton.OnTouchListener onShowAllTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleAnimation(1f, 0.75f, view, "none");
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                scaleAnimation(0.75f, 1f, view, "showAll");
            }
            return false;
        }
    };

    private final ImageButton.OnTouchListener onDisplayFavoritesTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleAnimation(1f, 0.75f, view, "none");
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                scaleAnimation(0.75f, 1f, view, "favorites");
            }
            return false;
        }
    };

    private final ImageButton.OnTouchListener onBackTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleAnimation(1f, 0.75f, view, "none");
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                scaleAnimation(0.75f, 1f, view, "back");
            }
            return false;
        }
    };


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final Cursor cursor = PlayListScene.database.getAllFavoritesItems();
        final String playListId = view.getTag().toString();
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBoxFavorites);
        if (checkBox.isChecked()) {
            PlayListScene.database.updateFavoriteItem(playListId, "false");
            PlayListScene.database.updateContainerByPlayListId(playListId);
        } else {
            if (cursor.getCount() < 3) {
                String title = ((TextView) view.findViewById(R.id.title)).getText().toString();
                PlayListScene.database.updateFavoriteItem(playListId, "true");
                Bundle bundle = getIntent().getExtras();
                if (bundle != null && bundle.containsKey("container")) {
                    String id = bundle.getString("container");
                    PlayListScene.database.updateContainer(id, title, playListId);
                    finish();
                } else {
                    PlayListScene.database.updateContainerByNullPlayList(title, playListId);
                }
            } else {
                handler.postDelayed(runnable, 5000);
                buyPremiumScene.setVisibility(View.VISIBLE);
                return;
            }
        }
        checkBox.toggle();
        listViewAdapter.getItems();
        listViewAdapter.notifyDataSetChanged();
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            buyPremiumScene.setVisibility(View.INVISIBLE);
        }
    };

    private void scaleAnimation(float from, float to, View view, final String action) {
        final ScaleAnimation scaleAnimation = new ScaleAnimation(
                from, to, from, to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(300);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation anim) {
                if (action.equals("showAll")) {
                    listViewAdapter.getItems();
                    listViewAdapter.notifyDataSetChanged();
                } else if (action.equals("favorites")) {
                    listViewAdapter.getAllFavorites();
                    listViewAdapter.notifyDataSetChanged();
                }
            }

            public void onAnimationRepeat(Animation anim) {
            }

            public void onAnimationEnd(Animation anim) {
                if (action.equals("back")) {
                    finish();
                }
            }
        });
        view.startAnimation(scaleAnimation);
    }
}
