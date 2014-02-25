package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import mp.agencja.apsik.kidotv.R;

public class MainManuScene extends Activity {

    private ImageButton btnStart;
    private RelativeLayout optionsLayout;
    private boolean expanded = true;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu_scene);

        ImageView balon = (ImageView) findViewById(R.id.balon);
        balon.startAnimation(createBalonAnimation(balon));

        btnStart = (ImageButton) findViewById(R.id.btnStart);
        ImageButton btnGoogle = (ImageButton) findViewById(R.id.btnGoogle);
        ImageButton btnRate = (ImageButton) findViewById(R.id.btnRate);
        ImageButton btnFacebook = (ImageButton) findViewById(R.id.btnFacebook);
        ImageButton btnSettings = (ImageButton) findViewById(R.id.btnSettings);

        ImageButton btnFx = (ImageButton) findViewById(R.id.btnFx);
        ImageButton btnVolume = (ImageButton) findViewById(R.id.btnVolume);

        btnStart.setOnTouchListener(onStartTouchListener);
        btnGoogle.setOnTouchListener(onGoogleTouchListener);
        btnRate.setOnTouchListener(onRateTouchListener);
        btnFacebook.setOnTouchListener(onFacebookTouchListener);

        btnSettings.setOnTouchListener(onSettingsTouchListener);
        btnFx.setOnTouchListener(onFxTouchListener);
        btnVolume.setOnTouchListener(onVolumeTouchListener);

        AnimationSet animationSetLeft = new AnimationSet(false);

        Animation alfaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alfaAnimation.setDuration(500);
        animationSetLeft.addAnimation(alfaAnimation);

        Animation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.5f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        translateAnimation.setDuration(1000);
        translateAnimation.setInterpolator(new DecelerateInterpolator());
        animationSetLeft.addAnimation(translateAnimation);
        animationSetLeft.setStartOffset(400);
        animationSetLeft.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation anim) {
            }

            public void onAnimationRepeat(Animation anim) {
            }

            public void onAnimationEnd(Animation anim) {
                btnStart.setVisibility(View.VISIBLE);
                findViewById(R.id.btnGoogle).setVisibility(View.VISIBLE);
                findViewById(R.id.btnRate).setVisibility(View.VISIBLE);
                findViewById(R.id.btnFacebook).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.tv_image).startAnimation(animationSetLeft);
        optionsLayout = (RelativeLayout) findViewById(R.id.optionsLayout);

    }

    private Animation createBalonAnimation(ImageView balon) {
        Animation translateAnimation = new TranslateAnimation(-balon.getDrawable().getIntrinsicWidth() * 2, balon.getDrawable().getIntrinsicWidth() * 2, 0, 0);
        translateAnimation.setDuration(30000);
        translateAnimation.setRepeatMode(Animation.REVERSE);
        translateAnimation.setRepeatCount(Animation.INFINITE);
        return translateAnimation;
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

    private final ImageButton.OnTouchListener onStartTouchListener = new ImageButton.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleAnimation(1f, 0.75f, view, "none");
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                scaleAnimation(0.75f, 1f, view, "start");

            }
            return false;
        }
    };
    private final ImageButton.OnTouchListener onGoogleTouchListener = new ImageButton.OnTouchListener() {
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

    private final ImageButton.OnTouchListener onRateTouchListener = new ImageButton.OnTouchListener() {
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

    private final ImageButton.OnTouchListener onFacebookTouchListener = new ImageButton.OnTouchListener() {
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
                if (action.equals("start")) {
                    final Intent intent = new Intent(MainManuScene.this, PlayListScene.class);
                    MainManuScene.this.startActivity(intent);
                }
            }
        });
        view.startAnimation(scaleAnimation);
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.adver).setVisibility(View.VISIBLE);
        handler.postDelayed(runnable, 5000);
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };
}
