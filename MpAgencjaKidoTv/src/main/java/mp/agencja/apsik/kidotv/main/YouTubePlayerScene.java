package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

public class YouTubePlayerScene extends YouTubeFailureRecoveryActivity {
    private YouTubePlayerView playerView;
    public static YouTubePlayer youtubePlayer = null;
    public static Activity activity;
    private static String play_list_id;
    private static int videoIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle bundle = getIntent().getExtras();
        videoIndex = 0;
        if (!PlayListScene.database.isOpen()) {
            PlayListScene.database.openToWrite();
        }
        if (bundle != null && bundle.containsKey("playListId")) {
            play_list_id = bundle.getString("playListId");
        }
        activity = YouTubePlayerScene.this;
        Intent intent = new Intent(YouTubePlayerScene.this, YoutubeOverLayScene.class);
        startActivity(intent);
        playerView = new YouTubePlayerView(this);
        playerView.initialize(DeveloperKey.DEVELOPER_KEY, this);
        setContentView(playerView);
    }

    @Override
    protected Provider getYouTubePlayerProvider() {
        return playerView;
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            youtubePlayer = player;
            youtubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
            youtubePlayer.setFullscreen(true);
            youtubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
            youtubePlayer.setPlayerStateChangeListener(playerStateChangeListener);
            youtubePlayer.setPlaybackEventListener(playbackEventListener);
            youtubePlayer.setPlaylistEventListener(playlistEventListener);
            final Cursor cursor = PlayListScene.database.getLastVideos(play_list_id);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                youtubePlayer.loadPlaylist(play_list_id, cursor.getInt(0), cursor.getInt(1) * 1000);
            } else {
                youtubePlayer.loadPlaylist(play_list_id);
            }
        }
    }

    private final YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String lastWatchedVideoId) {
        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {
            YoutubeOverLayScene.handler.postDelayed(YoutubeOverLayScene.runnable, 1000);
            YoutubeOverLayScene.handlerButtonsVisibility.postDelayed(YoutubeOverLayScene.runnableVisibilityButtons, 5000);
        }

        @Override
        public void onVideoEnded() {
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            if (errorReason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                youtubePlayer = null;
            }
        }
    };

    private final YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };

    private final YouTubePlayer.PlaylistEventListener playlistEventListener = new YouTubePlayer.PlaylistEventListener() {
        @Override
        public void onPrevious() {
            videoIndex--;
        }

        @Override
        public void onNext() {
            videoIndex++;
        }

        @Override
        public void onPlaylistEnded() {
            youtubePlayer.loadPlaylist(play_list_id);
            videoIndex = 0;
        }
    };

    public static void savelastVideo() {
        if (youtubePlayer != null) {
            Cursor cursor = PlayListScene.database.getLastVideos(play_list_id);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                PlayListScene.database.updateLastVideo(play_list_id, cursor.getInt(0) + videoIndex, YoutubeOverLayScene.seekBar.getProgress());
            } else {
                PlayListScene.database.insertNewVideo(play_list_id, videoIndex, YoutubeOverLayScene.seekBar.getProgress());
            }
        }
    }
}
