package mp.agencja.apsik.kidotv.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

public class YouTubePlayerScene extends YouTubeFailureRecoveryActivity {
    private YouTubePlayerView playerView;
    public static YouTubePlayer youtubePlayer;
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("playListId")) {
            if (!wasRestored) {
                youtubePlayer = player;
                youtubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                youtubePlayer.setFullscreen(true);
                youtubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                youtubePlayer.setPlayerStateChangeListener(playerStateChangeListener);
                youtubePlayer.setPlaybackEventListener(playbackEventListener);
                youtubePlayer.setPlaylistEventListener(playlistEventListener);
                youtubePlayer.loadPlaylist(bundle.getString("playListId"));
            }
        }
    }

    private final YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {
            YoutubeOverLayScene.handler.postDelayed(YoutubeOverLayScene.runnable, 1000);
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

        }

        @Override
        public void onNext() {

        }

        @Override
        public void onPlaylistEnded() {

        }
    };
}
