package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import mp.agencja.apsik.kidotv.R;

public class YoutubeOverLayScene extends Activity {
    private Button btnPlay, btnPause;
    private ImageButton btnFx;
    private ImageButton btnVolume;
    private ImageButton btnBack;
    private boolean lock;
    private static boolean sceneButttonsVisible = true;
    public static final Handler handler = new Handler();
    public static final Handler handlerButtonsVisibility = new Handler();
    public static SeekBar seekBar;
    private boolean allowShowing = true;
    private static RelativeLayout mainLayout;
    private static TextView currentTimeTv, endTime;
    private RelativeLayout optionsLayout;
    private boolean expanded = true;
    private ImageButton btnLock;
    private ImageView randomAnimal;
    private Button btnPrevious;
    private Button btnNext;
    private final int[] drawable = new int[]{
            R.drawable.bear,
            R.drawable.cat
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_overlay);

        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        mainLayout.setOnTouchListener(onMainLayoutTouchListener);

        btnBack = (ImageButton) findViewById(R.id.backButton);
        ImageButton btnSettings = (ImageButton) findViewById(R.id.btnSettings);
        btnVolume = (ImageButton) findViewById(R.id.btnVolume);
        btnFx = (ImageButton) findViewById(R.id.btnFx);

        btnLock = (ImageButton) findViewById(R.id.btnLock);

        btnSettings.setOnTouchListener(onSettingsTouchListener);
        btnLock.setOnTouchListener(onLockTouchListener);
        btnFx.setOnTouchListener(onFxTouchListener);
        btnVolume.setOnTouchListener(onVolumeTouchListener);
        btnBack.setOnTouchListener(onBackTouchListener);

        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnTouchListener(onPlayTouchListener);

        btnPause = (Button) findViewById(R.id.btnPause);
        btnPause.setOnTouchListener(onPauseTouchListener);

        btnPrevious = (Button) findViewById(R.id.previous);
        btnNext = (Button) findViewById(R.id.next);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        currentTimeTv = (TextView) findViewById(R.id.currentTime);
        currentTimeTv.setText("00:00 /");
        endTime = (TextView) findViewById(R.id.endTime);

        optionsLayout = (RelativeLayout) findViewById(R.id.optionsLayout);

        randomAnimal = (ImageView) findViewById(R.id.randomanimal);
    }

    private final ImageButton.OnTouchListener onPauseTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            btnPause.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.INVISIBLE);
            btnPrevious.setVisibility(View.INVISIBLE);
            YouTubePlayerScene.youtubePlayer.pause();
            btnPlay.setVisibility(View.VISIBLE);
            return false;
        }
    };

    private Animation createAnimation() {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimationIn = new TranslateAnimation(0, 0,
                randomAnimal.getHeight(), randomAnimal.getHeight() * 0.15F);
        translateAnimationIn.setDuration(1000);
        translateAnimationIn.setInterpolator(new DecelerateInterpolator());
        translateAnimationIn.setFillAfter(true);

        TranslateAnimation translateAnimationOut = new TranslateAnimation(0, 0,
                randomAnimal.getHeight() * 0.15F, randomAnimal.getHeight());
        translateAnimationOut.setStartOffset(4000);
        translateAnimationOut.setDuration(1000);
        translateAnimationOut.setInterpolator(new AccelerateInterpolator());
        translateAnimationIn.setFillAfter(false);

        animationSet.addAnimation(translateAnimationIn);
        animationSet.addAnimation(translateAnimationOut);
        animationSet.setFillAfter(false);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                allowShowing = true;
                randomAnimal.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return animationSet;
    }

    private final ImageButton.OnTouchListener onMainLayoutTouchListener = new ImageButton.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (YouTubePlayerScene.youtubePlayer != null) {
                if (YouTubePlayerScene.youtubePlayer.isPlaying()) {
                    int childcount = mainLayout.getChildCount();
                    if (!isLock()) {
                        if (allowShowing) {
                            Random random = new Random();
                            randomAnimal.setImageDrawable(getResources().getDrawable(drawable[random.nextInt(2)]));
                            randomAnimal.setX(random.nextInt(mainLayout.getWidth() - randomAnimal.getWidth()));
                            randomAnimal.setVisibility(View.VISIBLE);
                            randomAnimal.startAnimation(createAnimation());
                            allowShowing = false;
                        }
                        findViewById(R.id.rightMenuLayout).setVisibility(View.VISIBLE);
                        handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
                        handlerButtonsVisibility.postDelayed(runnableVisibilityButtons, 5000);
                    } else {
                        if (sceneButttonsVisible) {
                            sceneButttonsVisible = false;
                            for (int i = 0; i < childcount; i++) {
                                View v = mainLayout.getChildAt(i);
                                assert v != null;
                                if (v.getId() != R.id.btnPlay) {
                                    if (v.getId() != R.id.randomanimal) {
                                        if (v.getId() == R.id.previous && YouTubePlayerScene.youtubePlayer.hasPrevious()) {
                                            v.setVisibility(View.VISIBLE);
                                        } else if (v.getId() == R.id.next && YouTubePlayerScene.youtubePlayer.hasNext()) {
                                            v.setVisibility(View.VISIBLE);
                                        } else if (v.getId() != R.id.previous && v.getId() != R.id.next) {
                                            v.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                            handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
                            handlerButtonsVisibility.postDelayed(runnableVisibilityButtons, 5000);
                        } else {
                            handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
                            handlerButtonsVisibility.post(runnableVisibilityButtons);
                            sceneButttonsVisible = true;
                        }
                    }
                }
            }
            return false;
        }
    };

    private final ImageButton.OnTouchListener onSettingsTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
                handlerButtonsVisibility.postDelayed(runnableVisibilityButtons, 5000);
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
                handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
                handlerButtonsVisibility.postDelayed(runnableVisibilityButtons, 5000);
                scaleAnimation(1f, 0.75f, view, "none");
                if (isLock()) {
                    optionsLayout.setVisibility(View.GONE);
                    expanded = true;
                    btnLock.setImageDrawable(getResources().getDrawable(R.drawable.unlock_button));
                    btnBack.setVisibility(View.INVISIBLE);
                    btnPause.setVisibility(View.INVISIBLE);
                    btnNext.setVisibility(View.INVISIBLE);
                    btnPrevious.setVisibility(View.INVISIBLE);
                    btnPlay.setEnabled(false);
                    seekBar.setVisibility(View.INVISIBLE);
                    btnFx.setEnabled(false);
                    btnVolume.setEnabled(false);
                    setLock(true);
                } else {
                    btnLock.setImageDrawable(getResources().getDrawable(R.drawable.start_lock_button));
                    btnBack.setVisibility(View.VISIBLE);
                    btnPlay.setEnabled(true);
                    btnPause.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.VISIBLE);
                    btnNext.setVisibility(View.VISIBLE);
                    btnPrevious.setVisibility(View.VISIBLE);
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
                handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
                handlerButtonsVisibility.postDelayed(runnableVisibilityButtons, 5000);
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
                handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
                handlerButtonsVisibility.postDelayed(runnableVisibilityButtons, 5000);
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
            handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
            handlerButtonsVisibility.postDelayed(runnableVisibilityButtons, 5000);
            YouTubePlayerScene.youtubePlayer.play();
            btnPlay.setVisibility(View.INVISIBLE);
            allowShowing = true;
            return false;
        }
    };

    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            seekBar.setProgress(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
            handler.removeCallbacks(runnable);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            YouTubePlayerScene.youtubePlayer.seekToMillis(seekBar.getProgress() * 1000);
            handlerButtonsVisibility.postDelayed(runnableVisibilityButtons, 5000);
            handler.postDelayed(runnable, 1000);
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

    public static final Runnable runnableVisibilityButtons = new Runnable() {
        public void run() {
            if (YouTubePlayerScene.youtubePlayer != null) {
                if (YouTubePlayerScene.youtubePlayer.isPlaying()) {
                    sceneButttonsVisible = true;
                    int childcount = mainLayout.getChildCount();
                    for (int i = 0; i < childcount; i++) {
                        View view = mainLayout.getChildAt(i);
                        assert view != null;
                        if (view.getId() != R.id.btnPlay) {
                            view.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
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
    public void onBackPressed() {
        if (isLock()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        YouTubePlayerScene.savelastVideo();
        finish();
        YouTubePlayerScene.activity.finish();
        handler.removeCallbacks(runnable);
        handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (!isLock()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    public void onPreviusClickListenr(View view) {
        if (YouTubePlayerScene.youtubePlayer.hasPrevious()) {
            YouTubePlayerScene.youtubePlayer.previous();
            handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
            handlerButtonsVisibility.post(runnableVisibilityButtons);
        }
    }

    public void onNextClickListenr(View view) {
        if (YouTubePlayerScene.youtubePlayer.hasNext()) {
            YouTubePlayerScene.youtubePlayer.next();
            handlerButtonsVisibility.removeCallbacks(runnableVisibilityButtons);
            handlerButtonsVisibility.post(runnableVisibilityButtons);
        }
    }
}
