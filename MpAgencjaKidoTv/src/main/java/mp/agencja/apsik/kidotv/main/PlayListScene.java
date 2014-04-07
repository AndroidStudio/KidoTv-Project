package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import mp.agencja.apsik.kidotv.R;

public class PlayListScene extends Activity {
    private final static String TAG_LOG = "PlayListScene";
    private final static String kido_play_list_url = "http://androidstudio.pl/kidotv/kido_play_list.json";
    private DownloadPlayListTask downloadPlayListTask;
    private ViewPagerParallax viewPager;
    private ImageButton btnLeft;
    private ImageButton btnRight;
    private RelativeLayout optionsLayout;
    private boolean expanded = true;
    public static Database database;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_scene);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        database = new Database(this);
        try {
            database.openToWrite();
        } catch (Exception e) {
            Log.w("Exception: ", "SQLITE EXCEPTION");
        }


        checkForUpdatePlayList();

        final ImageButton btnSettings = (ImageButton) findViewById(R.id.btnSettings);
        final ImageButton btnFx = (ImageButton) findViewById(R.id.btnFx);
        final ImageButton btnVolume = (ImageButton) findViewById(R.id.btnVolume);
        final ImageButton btnBack = (ImageButton) findViewById(R.id.backButton);

        btnLeft = (ImageButton) findViewById(R.id.btnLeft);
        btnLeft.setVisibility(View.INVISIBLE);
        btnRight = (ImageButton) findViewById(R.id.btnRight);
        final ImageButton btndisplayAllPlayLists = (ImageButton) findViewById(R.id.displayAllPlayLists);

        btnSettings.setOnTouchListener(onSettingsTouchListener);
        btnFx.setOnTouchListener(onFxTouchListener);
        btnVolume.setOnTouchListener(onVolumeTouchListener);

        btnBack.setOnTouchListener(onBackTouchListener);
        btnLeft.setOnTouchListener(onLeftTouchListener);
        btnRight.setOnTouchListener(onRightTouchListener);
        btndisplayAllPlayLists.setOnTouchListener(onDisplayAllPlayLists);

        viewPager = (ViewPagerParallax) findViewById(R.id.viewpager);
        viewPager.setMaxPages(3);
        viewPager.setBackgroundAsset();
        viewPager.setOnPageChangeListener(onPageChangeListener);

        optionsLayout = (RelativeLayout) findViewById(R.id.optionsLayout);
    }

    private void checkForUpdatePlayList() {
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("version")) {
            Cursor cursor = database.getAllContainers();
            List<String> used_containers = new ArrayList<String>();
            while (cursor.moveToNext()) {
                String play_list_id = cursor.getString(1);
                if (play_list_id != null) {
                    if (!play_list_id.equals("null")) {
                        used_containers.add(cursor.getString(1));
                    }
                }
            }

            cursor = database.getAllFavoritesItems();
            List<String> used_favorites = new ArrayList<String>();
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    used_favorites.add(cursor.getString(1));
                }
            }

            database.deleteFavoriteItems();

            ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("list");
            if (list != null) {
                for (HashMap<String, String> map : list) {
                    if (used_containers.size() > 0) {
                        Iterator<String> iterator = used_containers.iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().equals(map.get("play_list_id"))) {
                                iterator.remove();
                            }
                        }
                    }
                    database.addItem(map.get("title"), map.get("play_list_id"), map.get("is_favorite"), map.get("duration"));
                    if (used_favorites.size() > 0) {
                        Iterator<String> iterator = used_favorites.iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().equals(map.get("play_list_id"))) {
                                database.updateFavoriteItem(map.get("play_list_id"), "true");
                                iterator.remove();
                            }
                        }
                    }
                }
            }

            if (used_containers.size() > 0) {
                for (String id : used_containers) {
                    database.updateContainerByPlayListId(id);
                }
            }

            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("version", bundle.getInt("version"));
            editor.commit();
            Toast.makeText(this, "Play list updated", Toast.LENGTH_LONG).show();

        }
    }

    private final ViewPagerParallax.OnPageChangeListener onPageChangeListener = new ViewPagerParallax.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }

        @Override
        public void onPageSelected(int i) {
            if (i == 0) {
                btnLeft.setVisibility(View.INVISIBLE);
            } else if (i == 2) {
                btnRight.setVisibility(View.INVISIBLE);
            } else {
                btnLeft.setVisibility(View.VISIBLE);
                btnRight.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    private void setContainers() {
        final Cursor cursor = database.getAllContainers();
        try {
            if (cursor.getCount() > 0) {
                ArrayList<List<HashMap<String, String>>> mainKidoList = new ArrayList<List<HashMap<String, String>>>(3);
                ArrayList<HashMap<String, String>> kidoPlayList = new ArrayList<HashMap<String, String>>(4);
                while (cursor.moveToNext()) {
                    String title = cursor.getString(0);
                    String play_list_id = cursor.getString(1);
                    String is_locked = cursor.getString(2);

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("title", title);
                    map.put("play_list_id", play_list_id);
                    map.put("is_locked", is_locked);
                    kidoPlayList.add(map);

                    if (kidoPlayList.size() % 4 == 0) {
                        mainKidoList.add(kidoPlayList);
                        kidoPlayList = new ArrayList<HashMap<String, String>>(4);
                    }
                }
                final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(PlayListScene.this, mainKidoList);
                viewPager.setAdapter(viewPagerAdapter);

                if (sharedPreferences.getInt("show_tip", 0) == 0) {
                    TipDialog tipDialog = new TipDialog(this, R.style.CustomDialog, viewPagerAdapter.width, sharedPreferences);
                    tipDialog.show();
                }

//                if (sharedPreferences.getInt("show_tip", 0) == 0) {
//                    viewPager.paint.setColorFilter(new PorterDuffColorFilter(Color.rgb(4, 84, 114), PorterDuff.Mode.MULTIPLY));
//                    final ImageView yellow_arrow = (ImageView) findViewById(R.id.yellow_arrow);
//                    yellow_arrow.setY(viewPagerAdapter.width / 2);
//                    yellow_arrow.setX(yellow_arrow.getX() - viewPagerAdapter.width - yellow_arrow.getWidth());
//                    yellow_arrow.setVisibility(View.VISIBLE);
//                    final RelativeLayout checkBoxContainer = (RelativeLayout) findViewById(R.id.checkBoxContainer);
//                    checkBoxContainer.setVisibility(View.VISIBLE);
//                    final ImageView tip_bear = (ImageView) findViewById(R.id.tip_bear);
//                    tip_bear.setVisibility(View.VISIBLE);
//                    final CheckBox checkBox = (CheckBox) findViewById(R.id.tip_check_box);
//                    final Typeface typeface = Typeface.createFromAsset(getAssets(), "font.TTF");
//                    checkBox.setTypeface(typeface);
//                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                        @Override
//                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                            if (b) {
//                                final SharedPreferences.Editor editor = sharedPreferences.edit();
//                                editor.putInt("show_tip", 1);
//                                editor.commit();
//                                tip_bear.setVisibility(View.INVISIBLE);
//                                checkBoxContainer.setVisibility(View.INVISIBLE);
//                                yellow_arrow.setVisibility(View.INVISIBLE);
//                                viewPager.paint.setColorFilter(null);
//                                viewPager.invalidate();
//                            }
//                        }
//                    });
//                }
            } else {
                database.insertContainer("Tap to add playlist", "false");
                database.insertContainer("Tap to add playlist", "false");
                database.insertContainer("Tap to add playlist", "false");
                database.insertContainer("Buy premium to get more 9 screens", "true");
                database.insertContainer("Buy premium to get more 9 screens", "true");
                database.insertContainer("Buy premium to get more 9 screens", "true");
                database.insertContainer("Buy premium to get more 9 screens", "true");
                database.insertContainer("Buy premium to get more 9 screens", "true");
                database.insertContainer("Buy premium to get more 9 screens", "true");
                database.insertContainer("Buy premium to get more 9 screens", "true");
                database.insertContainer("Buy premium to get more 9 screens", "true");
                database.insertContainer("Buy premium to get more 9 screens", "true");

                final SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("version", 0);
                editor.commit();

                setContainers();
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "setContainers error...");
        } finally {
            cursor.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContainers();
        if (downloadPlayListTask != null) {
            AsyncTask.Status diStatus = downloadPlayListTask.getStatus();
            if (diStatus != AsyncTask.Status.FINISHED) {
                return;
            }
        }
        downloadPlayListTask = new DownloadPlayListTask(sharedPreferences, this);
        downloadPlayListTask.execute(kido_play_list_url);


    }

    @Override
    protected void onPause() {
        super.onPause();
        downloadPlayListTask.cancel(true);
    }

    private final ImageButton.OnTouchListener onSettingsTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleAnimation(1f, 0.75f, view, "none");
                if (!expanded) {
                    optionsLayout.setVisibility(View.GONE);
                    expanded = true;
                } else {
                    optionsLayout.setVisibility(View.VISIBLE);
                    expanded = false;
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                scaleAnimation(0.75f, 1f, view, "none");
            }
            return false;
        }
    };

    private final ImageButton.OnTouchListener onFxTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleAnimation(1f, 0.75f, view, "none");
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.adjustStreamVolume(AudioManager.STREAM_SYSTEM,
                        AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                scaleAnimation(0.75f, 1f, view, "none");
            }
            return false;
        }
    };
    private final ImageButton.OnTouchListener onVolumeTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleAnimation(1f, 0.75f, view, "none");
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                scaleAnimation(0.75f, 1f, view, "none");
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

    private final ImageButton.OnTouchListener onLeftTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleAnimation(1f, 0.75f, view, "left1");
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                scaleAnimation(0.75f, 1f, view, "left");
            }
            return false;
        }
    };

    private final ImageButton.OnTouchListener onRightTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                scaleAnimation(1f, 0.75f, view, "right1");
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                scaleAnimation(0.75f, 1f, view, "right");
            }
            return false;
        }
    };

    private final ImageButton.OnTouchListener onDisplayAllPlayLists = new ImageButton.OnTouchListener() {
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

    private void scaleAnimation(float from, float to, final View view, final String action) {
        final ScaleAnimation scaleAnimation = new ScaleAnimation(
                from, to, from, to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(300);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation anim) {
                if (action.equals("left1")) {
                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem > 0) {
                        viewPager.setCurrentItem(currentItem - 1, true);
                    }
                } else if (action.equals("right1")) {
                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem != viewPager.getMaxPages() - 1) {
                        viewPager.setCurrentItem(currentItem + 1, true);
                    }
                }
            }

            public void onAnimationRepeat(Animation anim) {
            }

            public void onAnimationEnd(Animation anim) {
                if (action.equals("left")) {
                    scaleAnimation.setFillAfter(false);
                } else if (action.equals("right")) {
                    scaleAnimation.setFillAfter(false);
                }

                if (action.equals("back")) {
                    finish();
                } else if (action.equals("favorites")) {
                    Intent intent = new Intent(PlayListScene.this, FavoritePlayListScene.class);
                    startActivity(intent);
                }
            }
        });
        view.startAnimation(scaleAnimation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }
}
