package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import mp.agencja.apsik.kidotv.R;
import mp.agencja.apsik.kidotv.main.inappbilling.util.IabHelper;
import mp.agencja.apsik.kidotv.main.inappbilling.util.IabResult;
import mp.agencja.apsik.kidotv.main.inappbilling.util.Inventory;
import mp.agencja.apsik.kidotv.main.inappbilling.util.Purchase;

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
    private MediaPlayer mMediaPlayer;
    private Uri button_sound_uri;
    private IabHelper mHelper;
    private static final String ITEM_SKU = "turn.off.adds";
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_scene);
        final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkRx8UKdPNnivALvyGyGmiBnXuY6zzTJRq+tN5J2DUWcG670iyA3+8zu7DGhJDCjbGm83CbcwQUK/U2R+VnMqGPSpu3vx0+WGgIgXAtFcpSWL7jKlldwg5HShTtT66Mv5mY6k+/QBAcJr9kAYqiquqbGM+GpkUOW1jogWe2nUf77373FbGwpbfix1vmX0EVIfMVuJ8LPOpcOODzAUdbjls7qv3NQ/wWntK2g929cwXzJz2aLoSMEg7ZyN6mA9wZd1ikmy079Kd8vTAmdOCzvkTLdGI7/5Zu8nfD4zpJfpHPlmtVGvfyshDef3sySaMWkyuBmOtvNLToEz4bGwHdBOrQIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG_LOG, "In-app Billing setup failed: " + result);
                } else {
                    Log.d(TAG_LOG, "In-app Billing is set up OK");
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            }
        });

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
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
        button_sound_uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.button_click);
    }

    private void playSound(Uri uri) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.w("mMediaPlayer", "error");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG_LOG, "onActivityResult handled by IABUtil.");
        }
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
            try {
                Serializable listIntent = getIntent().getSerializableExtra("list");
                @SuppressWarnings("unchecked")
                ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) listIntent;
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
            } catch (Exception ignored) {

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

                viewPagerAdapter = new ViewPagerAdapter(PlayListScene.this, mainKidoList);
                viewPager.setAdapter(viewPagerAdapter);

                if (sharedPreferences.getInt("show_tip", 0) == 0) {
                    TipDialog tipDialog = new TipDialog(this, R.style.CustomDialog, viewPagerAdapter.width, sharedPreferences);
                    tipDialog.show();
                }
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
                playSound(button_sound_uri);
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
                playSound(button_sound_uri);
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
                playSound(button_sound_uri);
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
                playSound(button_sound_uri);
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
                playSound(button_sound_uri);
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
                playSound(button_sound_uri);
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
                playSound(button_sound_uri);
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

    private final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null)
                return;
            if (result.isFailure()) {
                return;
            }

            Purchase adsPurchase = inventory.getPurchase(ITEM_SKU);
            if (adsPurchase != null) {
                Log.d(TAG_LOG, "Premium Account");
                //premiumAccount = true;
            } else {
                Log.d(TAG_LOG, "Free Account");
            }
        }
    };

    public void buyClick() {
        try {
            mHelper.flagEndAsync();
            mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener);
        } catch (Exception e) {

        }
    }

    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.i("onIabPurchaseFinished", "error");
                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                Log.i("onIabPurchaseFinished", "succes");
//                consumeItem();
//                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
//                preferencesEditor.putInt(ADDS_PREFERENCES, 1);
//                preferencesEditor.commit();
            }
        }
    };

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    private final IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                Log.i("onQueryInventoryFinished", "error");
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU), mConsumeFinishedListener);
                Log.i("onQueryInventoryFinished", "succes");
            }
        }
    };

    private final IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                Log.i("onConsumeFinished", "succes");
            } else {
                Log.i("onConsumeFinished", "error");
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        if (mHelper != null)
            mHelper.dispose();
        mHelper = null;
    }
}
