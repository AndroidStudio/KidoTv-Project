package mp.agencja.apsik.kidotv.main;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import mp.agencja.apsik.kidotv.R;

public class MusicService extends Service implements OnErrorListener {
    private final String TAG = "MusicService";
    private final IBinder mBinder = new ServiceBinder();
    public MediaPlayer mPlayer;
    private int length = 0;
    private Handler handler = new Handler();
    private ActivityManager activityManager;
    private NotificationManager notificationManager;

    public MusicService() {
    }

    public class ServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        Intent targetIntent = new Intent(this, MainManuScene.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Kido TV")
                .setContentText("http://www.mpagencja.pl")
                        //.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(12345, notificationBuilder.build());

        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        mPlayer = MediaPlayer.create(this, R.raw.game_music);
        mPlayer.setOnErrorListener(this);
        if (mPlayer != null) {
            mPlayer.setLooping(true);
            mPlayer.setVolume(100, 100);
        }

        mPlayer.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                onError(mPlayer, what, extra);
                return true;
            }
        });
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 1000);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if (intent != null) {

        }
        return START_STICKY;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            final List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
            if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
                ComponentName componentName = runningTaskInfos.get(0).topActivity;
                if (componentName != null) {
                    String className = componentName.getClassName();
                    Log.i(TAG, "Class name: " + className);
                    if (className != null) {
                        if (className.equals("mp.agencja.apsik.kidotv.main.MainManuScene") || className.equals("mp.agencja.apsik.kidotv.main.FavoritePlayListScene")
                                || className.equals("mp.agencja.apsik.kidotv.main.PlayListScene")) {
                            resumeMusic();
                        } else {
                            pauseMusic();
                        }
                    }
                }
            }
            handler.postDelayed(runnable, 1000);
        }
    };

    public void pauseMusic() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            length = mPlayer.getCurrentPosition();
        }
    }

    public void resumeMusic() {
        if (!mPlayer.isPlaying()) {
            mPlayer.seekTo(length);
            mPlayer.start();
        }
    }

    public void stopMusic() {
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        notificationManager.cancelAll();
        handler.removeCallbacks(runnable);
        stopMusic();
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, "MediaPlayer error...", Toast.LENGTH_SHORT).show();
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
        return false;
    }
}
