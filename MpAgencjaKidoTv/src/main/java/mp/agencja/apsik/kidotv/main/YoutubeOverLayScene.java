package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import mp.agencja.apsik.kidotv.R;

public class YoutubeOverLayScene extends Activity {
    private Button btnPlay, btnPause;
    private ImageButton btnSettings;
    private ImageButton btnFx;
    private ImageButton btnVolume;
    private ImageButton btnBack;
    private static SeekBar seekBar;
    private boolean lock;
    public static final Handler handler = new Handler();
    private boolean allowShowing = false;
    private RelativeLayout mainLayout;
    private static TextView currentTimeTv, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_overlay);

        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        mainLayout.setOnTouchListener(onMainLayoutTouchListener);

        btnBack = (ImageButton) findViewById(R.id.backButton);
        btnSettings = (ImageButton) findViewById(R.id.btnSettings);
        btnVolume = (ImageButton) findViewById(R.id.btnVolume);
        btnFx = (ImageButton) findViewById(R.id.btnFx);
        final ImageButton btnLock = (ImageButton) findViewById(R.id.btnLock);

        btnSettings.setOnTouchListener(onSettingsTouchListener);
        btnLock.setOnTouchListener(onLockTouchListener);
        btnFx.setOnTouchListener(onFxTouchListener);
        btnVolume.setOnTouchListener(onVolumeTouchListener);
        btnBack.setOnTouchListener(onBackTouchListener);

        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnTouchListener(onPlayTouchListener);

        btnPause = (Button) findViewById(R.id.btnPause);
        btnPause.setOnTouchListener(onPauseTouchListener);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        currentTimeTv = (TextView) findViewById(R.id.currentTime);
        currentTimeTv.setText("00:00 /");
        endTime = (TextView) findViewById(R.id.endTime);

    }

    private final ImageButton.OnTouchListener onPauseTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            btnPause.setVisibility(View.INVISIBLE);
            YouTubePlayerScene.youtubePlayer.pause();
            btnPlay.setVisibility(View.VISIBLE);
            return false;
        }
    };
    private final ImageButton.OnTouchListener onMainLayoutTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (YouTubePlayerScene.youtubePlayer != null) {
                if (allowShowing && YouTubePlayerScene.youtubePlayer.isPlaying()) {
                    btnPause.setVisibility(View.VISIBLE);
                    allowShowing = false;
                } else {
                    btnPause.setVisibility(View.INVISIBLE);
                    allowShowing = true;
                }
            }
            return false;
        }
    };
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
                    mainLayout.setOnTouchListener(null);
                    btnBack.setEnabled(false);
                    btnPause.setEnabled(false);
                    btnPlay.setEnabled(false);
                    seekBar.setEnabled(false);
                    btnSettings.setEnabled(false);
                    btnFx.setEnabled(false);
                    btnVolume.setEnabled(false);
                    setLock(true);
                } else {
                    mainLayout.setOnTouchListener(onMainLayoutTouchListener);
                    btnBack.setEnabled(true);
                    btnPlay.setEnabled(true);
                    btnPause.setEnabled(true);
                    seekBar.setEnabled(true);
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

    private final Button.OnTouchListener onPlayTouchListener = new Button.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            YouTubePlayerScene.youtubePlayer.play();
            btnPlay.setVisibility(View.INVISIBLE);
            allowShowingPause();
            return false;
        }
    };

    private void allowShowingPause() {
        allowShowing = true;
    }


    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            seekBar.setProgress(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            YouTubePlayerScene.youtubePlayer.seekToMillis(seekBar.getProgress() * 1000);
        }
    };


    public static final Runnable runnable = new Runnable() {
        public void run() {
            int currentTime = YouTubePlayerScene.youtubePlayer.getDurationMillis();
            int duration = YouTubePlayerScene.youtubePlayer.getCurrentTimeMillis();
            currentTimeTv.setText(getDate(duration) + " /");
            endTime.setText(getDate(currentTime));
            seekBar.setMax(currentTime / 1000);
            seekBar.setProgress(duration / 1000);
            handler.postDelayed(runnable, 1000);
        }
    };

    private static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
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

    private boolean isLock() {
        return !lock;
    }

    private void setLock(boolean lock) {
        this.lock = lock;
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
        YouTubePlayerScene.activity.finish();
        handler.removeCallbacks(runnable);
    }

}
