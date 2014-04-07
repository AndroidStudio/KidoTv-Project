package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.plus.PlusShare;

import mp.agencja.apsik.kidotv.R;

public class MainManuScene extends Activity {
    private ImageButton btnStart;
    private RelativeLayout optionsLayout;
    private boolean expanded = true;
    private final Handler handler = new Handler();
    private boolean canPresentShareDialog;
    private UiLifecycleHelper uiHelper;
    private int REQUEST_CODE_INTERACTIVE_POST = 2;
    private String LOG_TAG = "MainManuScene";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu_scene);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        canPresentShareDialog = FacebookDialog.canPresentShareDialog(this, FacebookDialog.ShareDialogFeature.SHARE_DIALOG);

        ImageView sun = (ImageView) findViewById(R.id.sun);
        sun.startAnimation(createSunAnimation(sun));

        ImageView balon = (ImageView) findViewById(R.id.balon);
        balon.startAnimation(createBalonAnimation(balon));

        ImageView balon_small = (ImageView) findViewById(R.id.balon_small);
        balon_small.startAnimation(createBalonAnimation(balon_small));

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

    private Animation createSunAnimation(ImageView sun) {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360f,
                sun.getDrawable().getIntrinsicWidth() / 2, sun.getDrawable().getIntrinsicWidth() / 2);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setDuration(3000);
        rotateAnimation.setRepeatMode(Animation.REVERSE);
        return rotateAnimation;
    }

    private Animation createBalonAnimation(ImageView balon) {
        RotateAnimation rotateAnimation = new RotateAnimation(-20f, 20f,
                balon.getDrawable().getIntrinsicWidth() / 2,
                (balon.getDrawable().getIntrinsicWidth() + balon.getPaddingTop()) / 2);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setDuration(14000);
        rotateAnimation.setRepeatMode(Animation.REVERSE);
        return rotateAnimation;
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
                scaleAnimation(0.75f, 1f, view, "google");
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
                scaleAnimation(0.75f, 1f, view, "facebook");
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
                }else if (action.equals("facebook")) {
                    setMessage();
                } else if (action.equals("google")) {
                    int available = GooglePlayServicesUtil.isGooglePlayServicesAvailable(MainManuScene.this);
                    if (available != ConnectionResult.SUCCESS) {
                        Toast.makeText(MainManuScene.this, "Please install GooglePlayService", Toast.LENGTH_LONG).show();
                    } else {
                        Intent shareIntent = new PlusShare.Builder(MainManuScene.this).setType("text/plain").setText("Kido Tv")
                                .setContentUrl(Uri.parse("http://www.androidstudio.pl/animal_adventure.png")).getIntent();

                        startActivityForResult(shareIntent, REQUEST_CODE_INTERACTIVE_POST);
                    }
                }
            }
        });
        view.startAnimation(scaleAnimation);
    }

    private void setMessage() {
        if (canPresentShareDialog) {
            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this).setName("Kido Tv").setDescription("Gry dla dzieci").setLink("http://www.androidstudio.pl")
                    .setPicture("http://www.androidstudio.pl/animal_adventure.png").build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else {
            Toast.makeText(this, "Please install facebook aplication", Toast.LENGTH_LONG).show();
        }
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_INTERACTIVE_POST) {
            if (resultCode == RESULT_OK) {
                Log.e("succes", "Succes create google+ post");
            } else {
                Log.e("error", "canceled");
            }
        }

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
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
