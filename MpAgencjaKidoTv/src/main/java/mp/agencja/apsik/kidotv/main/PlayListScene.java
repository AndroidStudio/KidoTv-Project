package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mp.agencja.apsik.kidotv.R;

public class PlayListScene extends Activity {
    private final static String TAG_LOG = "PlayListScene";
    private ViewPagerParallax viewPager;
    private ImageButton btnSettings;
    private ImageButton btnFx;
    private ImageButton btnVolume;
    private ImageButton btnBack;
    private ImageButton btnLeft;
    private ImageButton btnRight;
    private ImageButton btndisplayAllPlayLists;
    private boolean lock;
    private RelativeLayout optionsLayout;
    private boolean expanded = true;
    public static Database database;
    private ArrayList<List<HashMap<String, String>>> mainKidoList;
    private ImageButton btnLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_scene);
        database = new Database(this);
        database.openToWrite();

        btnSettings = (ImageButton) findViewById(R.id.btnSettings);

        btnLock = (ImageButton) findViewById(R.id.btnLock);
        btnFx = (ImageButton) findViewById(R.id.btnFx);
        btnVolume = (ImageButton) findViewById(R.id.btnVolume);

        btnBack = (ImageButton) findViewById(R.id.backButton);
        btnLeft = (ImageButton) findViewById(R.id.btnLeft);
        btnRight = (ImageButton) findViewById(R.id.btnRight);
        btndisplayAllPlayLists = (ImageButton) findViewById(R.id.displayAllPlayLists);

        btnSettings.setOnTouchListener(onSettingsTouchListener);
        btnLock.setOnTouchListener(onLockTouchListener);
        btnFx.setOnTouchListener(onFxTouchListener);
        btnVolume.setOnTouchListener(onVolumeTouchListener);

        btnBack.setOnTouchListener(onBackTouchListener);
        btnLeft.setOnTouchListener(onLeftTouchListener);
        btnRight.setOnTouchListener(onRightTouchListener);
        btndisplayAllPlayLists.setOnTouchListener(onDisplayAllPlayLists);

        viewPager = (ViewPagerParallax) findViewById(R.id.viewpager);
        viewPager.setMaxPages(3);
        viewPager.setBackgroundAsset();

        optionsLayout = (RelativeLayout) findViewById(R.id.optionsLayout);
    }

    private void setContainers() {
        final Cursor cursor = database.getAllContainers();
        try {
            if (cursor.getCount() > 0) {
                mainKidoList = new ArrayList<List<HashMap<String, String>>>(3);
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
            } else {
                database.insertContainer("Tap to add playlist", "false");
                database.insertContainer("Tap to add playlist", "false");
                database.insertContainer("Tap to add playlist", "false");
                database.insertContainer("Tap to unlock", "true");
                database.insertContainer("Tap to unlock", "true");
                database.insertContainer("Tap to unlock", "true");
                database.insertContainer("Tap to unlock", "true");
                database.insertContainer("Tap to unlock", "true");
                database.insertContainer("Tap to unlock", "true");
                database.insertContainer("Tap to unlock", "true");
                database.insertContainer("Tap to unlock", "true");
                database.insertContainer("Tap to unlock", "true");
                setContainers();
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "setContainers error...");
        } finally {
            cursor.close();
        }
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(PlayListScene.this, mainKidoList);
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContainers();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
    private final ImageButton.OnTouchListener onLockTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleAnimation(1f, 0.75f, view, "none");
                if (isLock()) {
                    btnLock.setImageDrawable(getResources().getDrawable(R.drawable.unlock_button));
                    btnBack.setEnabled(false);
                    btnLeft.setEnabled(false);
                    btnRight.setEnabled(false);
                    btndisplayAllPlayLists.setEnabled(false);
                    btnSettings.setEnabled(false);
                    btnFx.setEnabled(false);
                    btnVolume.setEnabled(false);
                    setLock(true);
                } else {
                    btnLock.setImageDrawable(getResources().getDrawable(R.drawable.start_lock_button));
                    btnBack.setEnabled(true);
                    btnLeft.setEnabled(true);
                    btnRight.setEnabled(true);
                    btndisplayAllPlayLists.setEnabled(true);
                    btnSettings.setEnabled(true);
                    btnFx.setEnabled(true);
                    btnVolume.setEnabled(true);
                    setLock(false);
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
                scaleAnimation(1f, 0.75f, view, "none");
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
                scaleAnimation(1f, 0.75f, view, "none");
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

    private void scaleAnimation(float from, float to, View view, final String action) {
        final ScaleAnimation scaleAnimation = new ScaleAnimation(
                from, to, from, to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(300);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation anim) {
                if (action.equals("left")) {
                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem > 0) {
                        viewPager.setCurrentItem(currentItem - 1, true);
                    }
                    Log.i("", "currentItem: " + viewPager.getCurrentItem());
                } else if (action.equals("right")) {
                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem != viewPager.getMaxPages() - 1) {
                        viewPager.setCurrentItem(currentItem + 1, true);
                    }
                    Log.i("", "currentItem: " + viewPager.getCurrentItem());
                }
            }

            public void onAnimationRepeat(Animation anim) {
            }

            public void onAnimationEnd(Animation anim) {
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

    private boolean isLock() {
        return !lock;
    }

    private void setLock(boolean lock) {
        this.lock = lock;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }
}
