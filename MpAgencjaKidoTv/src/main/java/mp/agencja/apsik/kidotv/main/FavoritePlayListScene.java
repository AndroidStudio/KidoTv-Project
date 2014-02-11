package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import mp.agencja.apsik.kidotv.R;

public class FavoritePlayListScene extends Activity implements AdapterView.OnItemClickListener {
    private ListViewAdapter listViewAdapter;
    private ImageView headerView;
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
        listView.setItemsCanFocus(true);

        ArrayList<HashMap<String, String>> mainList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("list");
        listViewAdapter = new ListViewAdapter(this);
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //do poprrawy
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
        Cursor cursor = PlayListScene.database.getAllFavoritesItems();

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
                }else{
                    PlayListScene.database.updateContainerByNullPlayList(title, playListId);
                }
            } else {
                Toast.makeText(FavoritePlayListScene.this, "Buy pro version", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        checkBox.toggle();
        listViewAdapter.getItems();
        listViewAdapter.notifyDataSetChanged();

//        final Intent intent = new Intent(FavoritePlayListScene.this, YouTubePlayerScene.class);
//        intent.putExtra("playListId", playListId);
//        startActivity(intent);
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

                } else if (action.equals("favorites")) {

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
                //listViewAdapter.search(builder.toString());
                builder.delete(0, builder.length());
                inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
                return false;
            }
            builder.append(character);
        }
        return super.dispatchKeyEvent(KEvent);
    }
}
