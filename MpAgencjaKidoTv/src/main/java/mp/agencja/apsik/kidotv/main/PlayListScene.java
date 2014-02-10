package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mp.agencja.apsik.kidotv.R;

public class PlayListScene extends Activity {
    private final static String TAG_LOG = "PlayListScene";
    private final static String kido_play_list_url = "http://androidstudio.pl/kidotv/kido_play_list.json";
    private static boolean needUpdate = true;
    private ViewPagerParallax viewPager;
    private DownloadPlayListTask downloadPlayListTask;
    private ProgressBar progressBar;
    private ImageButton btnSettings;
    private ImageButton btnFx;
    private ImageButton btnVolume;
    private ImageButton btnBack;
    private ImageButton btnLeft;
    private ImageButton btnRight;
    private ImageButton btndisplayAllPlayLists;
    private boolean lock;
    private boolean accesToFavorites = false;
    private List<List<HashMap<String, String>>> mainKidoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_scene);
        btnSettings = (ImageButton) findViewById(R.id.btnSettings);
        final ImageButton btnLock = (ImageButton) findViewById(R.id.btnLock);
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
        viewPager.setMaxPages(0);
        viewPager.setBackgroundAsset();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needUpdate) {
            if (downloadPlayListTask != null) {
                AsyncTask.Status diStatus = downloadPlayListTask.getStatus();
                if (diStatus != AsyncTask.Status.FINISHED) {
                    return;
                }
            }
            downloadPlayListTask = new DownloadPlayListTask();
            downloadPlayListTask.execute(kido_play_list_url);
        }
    }

    public static void needUpdate(boolean update) {
        needUpdate = update;
    }

    private class DownloadPlayListTask extends AsyncTask<String, Integer, Boolean> {

        private final HttpClient httpClient = CustomHttpClient.getHttpClient();
        private List<HashMap<String, String>> kidoPlayList;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            mainKidoList = new ArrayList<List<HashMap<String, String>>>();
            kidoPlayList = new ArrayList<HashMap<String, String>>(4);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            InputStream inputStream;
            try {
                final HttpGet request = new HttpGet(urls[0]);
                final HttpResponse response = httpClient.execute(request);
                final StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    final HttpEntity httpEntity = response.getEntity();
                    inputStream = httpEntity.getContent();
                    try {
                        final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        final StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        final JSONObject jsonObject = new JSONObject(sb.toString());
                        final JSONArray jsonArray = jsonObject.getJSONArray("KidoTvPlayList");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            if (!isCancelled()) {
                                final JSONObject jsonRow = jsonArray.getJSONObject(i);

                                HashMap<String, String> map;
                                map = getPlayListThumb(jsonRow.getString("PlayList"));
                                if (map == null) return false;
                                map.put("play_list_id", jsonRow.getString("PlayList"));
                                publishProgress(i, jsonArray.length() - 1);
                                kidoPlayList.add(map);
                                if ((i + 1) % 4 == 0 || i == jsonArray.length() - 1) {
                                    mainKidoList.add(kidoPlayList);
                                    kidoPlayList = new ArrayList<HashMap<String, String>>(4);
                                }
                            }
                        }
                        br.close();
                    } catch (Exception e) {
                        Log.e(TAG_LOG, e.getMessage());
                        return false;
                    } finally {
                        inputStream.close();
                    }
                } else {
                    return false;
                }
                viewPager.setMaxPages(mainKidoList.size());
            } catch (IOException e) {
                Log.e(TAG_LOG, e.getMessage());
                return false;
            }
            return true;
        }

        private HashMap<String, String> getPlayListThumb(String playListid) {
            HashMap<String, String> hasMap = null;
            InputStream inputStream;
            final String youtubeData = "http://gdata.youtube.com/feeds/api/playlists/" + playListid + "?v=2&prettyprint=true&alt=json";
            try {
                final HttpGet request = new HttpGet(youtubeData);
                final HttpResponse response = httpClient.execute(request);
                final StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    final HttpEntity httpEntity = response.getEntity();
                    inputStream = httpEntity.getContent();
                    try {
                        final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        final StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        final JSONObject jsonObject = new JSONObject(sb.toString());
                        final JSONObject feed = jsonObject.getJSONObject("feed");

                        final JSONObject mediaAndGroup = feed.getJSONObject("media$group");
                        final JSONObject title = feed.getJSONObject("title");

                        final JSONArray mediaThumbial = mediaAndGroup.getJSONArray("media$thumbnail");

                        // final float desiny = PlayListScene.this.getResources().getDisplayMetrics().density;
                        final JSONObject jsonRow;
                        //  if (desiny > 1.0F) {
                        //    jsonRow = mediaThumbial.getJSONObject(1);
                        //  } else {
                        jsonRow = mediaThumbial.getJSONObject(2);
                        //   }

                        hasMap = new HashMap<String, String>();
                        hasMap.put("title", title.getString("$t"));
                        hasMap.put("url", jsonRow.getString("url"));

                        br.close();
                    } catch (Exception e) {
                        Log.e(TAG_LOG, e.getMessage());
                        return null;
                    } finally {
                        inputStream.close();
                    }
                } else {
                    return null;
                }
            } catch (IOException e) {
                Log.e(TAG_LOG, e.getMessage());
                return null;
            }

            return hasMap;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            double p1 = progress[0];
            double p2 = progress[1];
            double x = p1 * 100 / p2;
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');
            decimalFormatSymbols.setGroupingSeparator(',');
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", decimalFormatSymbols);
            ((TextView) findViewById(R.id.progresTextView)).setText(decimalFormat.format(x) + "%");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                accesToFavorites = true;
                progressBar.setVisibility(View.INVISIBLE);
                viewPager.setVisibility(View.VISIBLE);
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(PlayListScene.this, mainKidoList);
                viewPager.setAdapter(viewPagerAdapter);
                viewPager.setOnPageChangeListener(onPageChangeListener);
            } else {
                Toast.makeText(PlayListScene.this, "Connection error...", Toast.LENGTH_LONG).show();
                PlayListScene.this.finish();
            }
        }
    }

    private final ViewPagerParallax.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }

        @Override
        public void onPageSelected(int i) {
            Log.e(TAG_LOG, "selected page :" + i);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        needUpdate(true);
        if (downloadPlayListTask != null) {
            downloadPlayListTask.cancel(true);
            downloadPlayListTask = null;
        }

    }

    private final ImageButton.OnTouchListener onSettingsTouchListener = new ImageButton.OnTouchListener() {
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
    private final ImageButton.OnTouchListener onLockTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleAnimation(1f, 0.75f, view, "none");
                if (isLock()) {
                    btnBack.setEnabled(false);
                    btnLeft.setEnabled(false);
                    btnRight.setEnabled(false);
                    btndisplayAllPlayLists.setEnabled(false);
                    btnSettings.setEnabled(false);
                    btnFx.setEnabled(false);
                    btnVolume.setEnabled(false);
                    setLock(true);
                } else {
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
                    if (accesToFavorites) {
                        final ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
                        for (List<HashMap<String, String>> arraylist : mainKidoList) {
                            for (HashMap<String, String> map : arraylist) {
                                list.add(map);
                            }
                        }
                        Intent intent = new Intent(PlayListScene.this, FavoritePlayListScene.class);
                        intent.putExtra("list", list);
                        startActivity(intent);
                    }
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
}
