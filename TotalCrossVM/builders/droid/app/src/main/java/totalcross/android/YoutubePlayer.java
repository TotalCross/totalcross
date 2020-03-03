package totalcross.android;

import android.os.Handler;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import totalcross.Launcher4A;

public class YoutubePlayer extends YouTubeBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String id = getIntent().getStringExtra("id");
        boolean autoPlay = getIntent().getBooleanExtra("autoPlay", true);
        int start = getIntent().getIntExtra("start", 0);
        int end = getIntent().getIntExtra("end", -1);
        playVideo(id, autoPlay, start, end);
    }

    public void playVideo(String id, boolean autoPlay, int start, int end) {
        YouTubePlayerView youTubePlayerView = new YouTubePlayerView(this);
        addContentView(youTubePlayerView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        final String idForYoutube = id;
        YouTubePlayer.OnInitializedListener onInitializedListener =  new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                //This handles the autoPlay and start parameter.
                if(autoPlay) {
                    youTubePlayer.loadVideo(idForYoutube, start * 1000);
                } else {
                    youTubePlayer.cueVideo(idForYoutube, start * 1000);
                }
                //This handles the end parameter.
                if(end * 1000 >= 1000) {
                    final Handler timeHandler = new Handler();
                    timeHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(youTubePlayer.getCurrentTimeMillis() >= end * 1000) {
                                timeHandler.removeCallbacks(this);
                                youTubePlayer.release();
                                Launcher4A.nativeYoutubeCallback(1); //Callback for the end state
                                YoutubePlayer.this.finish();
                            } else {
                                timeHandler.postDelayed(this, 1000);
                            }
                        }
                    }, 1000);
                }
                youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                    @Override
                    public void onPlaying() {
                        Launcher4A.nativeYoutubeCallback(2);
                    }

                    @Override
                    public void onPaused() {
                        Launcher4A.nativeYoutubeCallback(3);
                    }

                    @Override
                    public void onStopped() {

                    }

                    @Override
                    public void onBuffering(boolean b) {
                        if(b) {
                            Launcher4A.nativeYoutubeCallback(4);
                        }
                    }

                    @Override
                    public void onSeekTo(int i) {

                    }
                });
                youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
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

                    }

                    @Override
                    public void onVideoEnded() {
                        Launcher4A.nativeYoutubeCallback(1);
                    }

                    @Override
                    public void onError(YouTubePlayer.ErrorReason errorReason) {
                        if (errorReason == YouTubePlayer.ErrorReason.UNKNOWN) {
                            Launcher4A.nativeYoutubeCallback(8);
                        } else if (errorReason == YouTubePlayer.ErrorReason.EMPTY_PLAYLIST) {
                            Launcher4A.nativeYoutubeCallback(7);
                        }
                    }
                });
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };
        youTubePlayerView.initialize("AIzaSyDH4b5bnVKQcQ6prRL83EbOLVU10xpN5x8", onInitializedListener);

    }

}
