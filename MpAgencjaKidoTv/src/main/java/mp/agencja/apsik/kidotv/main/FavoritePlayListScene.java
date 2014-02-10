package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

import mp.agencja.apsik.kidotv.R;

public class FavoritePlayListScene extends Activity implements AdapterView.OnItemClickListener {
    private ListViewAdapter listViewAdapter;
    private ImageView headerView;
    private ArrayList<HashMap<String, String>> mainList;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favoriteplaylist_scene);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnTouchListener(onBackTouchListener);

        ImageButton showAll = (ImageButton) findViewById(R.id.showAll);
        showAll.setOnTouchListener(onShowAllTouchListener);

        ImageButton displayFavorites = (ImageButton) findViewById(R.id.favorites);
        displayFavorites.setOnTouchListener(onDisplayFavoritesTouchListener);


        headerView = (ImageView) findViewById(R.id.headerview);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputMethodManager.toggleSoftInputFromWindow(headerView.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            }
        });


        ListView listView = (ListView) findViewById(R.id.listView);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (0.9 * headerView.getDrawable().getIntrinsicWidth()), (int) (3.5 * headerView.getDrawable().getIntrinsicHeight()));
        params.addRule(RelativeLayout.BELOW, headerView.getId());
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        listView.setLayoutParams(params);


        mainList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("list");
        listViewAdapter = new ListViewAdapter(this, mainList);
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PlayListScene.needUpdate(false);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
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
        final String playListId = view.getTag().toString();
        final Intent intent = new Intent(FavoritePlayListScene.this, YouTubePlayerScene.class);
        intent.putExtra("playListId", playListId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listViewAdapter.database.close();
    }

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
                    listViewAdapter.createItems(mainList);
                    listViewAdapter.notifyDataSetChanged();
                } else if (action.equals("favorites")) {
                    listViewAdapter.createItems(mainList);
                    listViewAdapter.showFavorites();
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

    private final StringBuilder builder = new StringBuilder();


    @Override
    public boolean dispatchKeyEvent(KeyEvent KEvent) {
        int keyaction = KEvent.getAction();

        if (keyaction == KeyEvent.ACTION_DOWN) {
            int keycode = KEvent.getKeyCode();

            int keyunicode = KEvent.getUnicodeChar(KEvent.getMetaState());
            char character = (char) keyunicode;
            if (keycode == KeyEvent.KEYCODE_ENTER) {
                listViewAdapter.search(builder.toString());
                builder.delete(0, builder.length());
                inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
                return false;
            }
            builder.append(character);
        }
        return super.dispatchKeyEvent(KEvent);
    }
}
