package totalcross.ui.media;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import java.io.IOException;

public class YoutubePlayer {

	private boolean autoPlay;
	private int end;
	private int start;

	public YoutubePlayer() {
	}

	/**
	 * When the video is not started yet.
	 */
	public static final int STATE_UNSTARTED = 0;
	/**
	 * When the video ends.
	 */
	public static final int STATE_ENDED = 1;
	/**
	 * When the video starts playing.
	 */
	public static final int STATE_PLAYING = 2;
	/**
	 * When the video is paused.
	 */
	public static final int STATE_PAUSED = 3;
	/**
	 * When the video is buffering
	 */
	public static final int STATE_BUFFERING = 4;
	/**
	 * When the video is cued.
	 */
	public static final int STATE_CUED = 5;
	/**
	 * Unknown state of the player.
	 */
	public static final int STATE_UNKNOWN = 6;
	/**
	 * When the video couldn't be found.
	 */
	public static final int ERROR_VIDEO_NOT_FOUND = 7;
	/**
	 * Unknown error happened.
	 */
	public static final int ERROR_UNKNOWN = 8;

	/**
	 * Sets the video to play automatically when it's loaded.
	 */
	public YoutubePlayer autoPlay(boolean autoPlay) {
		this.autoPlay = autoPlay;
		return this;
	}

	/**
	 * Sets the end point of the video.
	 * 
	 * @param end the end point of the video in seconds.
	 */
	public YoutubePlayer end(int end) {
		this.end = end;
		return this;
	}

	/**
	 * Sets the start point of the video.
	 * 
	 * @param start the start point of the video in seconds.
	 */
	public YoutubePlayer start(int start) {
		this.start = start;
		return this;
	}

	public interface Callback {
		/**
		 * Listener to the state of the player.
		 * 
		 * @see totalcross.ui.media.YoutubePlayer#ERROR_UNKNOWN
		 * @see totalcross.ui.media.YoutubePlayer#ERROR_VIDEO_NOT_FOUND
		 * @see totalcross.ui.media.YoutubePlayer#STATE_BUFFERING
		 * @see totalcross.ui.media.YoutubePlayer#STATE_CUED
		 * @see totalcross.ui.media.YoutubePlayer#STATE_ENDED
		 * @see totalcross.ui.media.YoutubePlayer#STATE_PAUSED
		 * @see totalcross.ui.media.YoutubePlayer#STATE_PLAYING
		 * @see totalcross.ui.media.YoutubePlayer#STATE_UNKNOWN
		 * @see totalcross.ui.media.YoutubePlayer#STATE_UNSTARTED
		 */
		public void onStateChange(int state);
	}

	/**
	 * Plays a video with the video id passed (value from v query parameter).
	 * 
	 * @param url the video id eg.:
	 *            https://www.youtube.com/watch?v=<b>xBM4luqmCKs</b>.
	 */
	public void play(String id) throws IOException {
		play(id	, null);
	}

	/**
	 * Plays a video with the video id passed and has a callback function that
	 * returns the state of the the player.
	 * 
	 * @param url      the video id eg.:
	 *                 https://www.youtube.com/watch?v=<b>xBM4luqmCKs</b>.
	 * @param callback the YoutubePlayer.Callback
	 * @see {@link Callback#onStateChange(int)}
	 */
	public void play(String id, Callback callback) throws IOException {
	    if(!totalcross.net.ConnectionManager.isInternetAccessible()) {
	        throw new IOException("Could not reach https://youtube.com");
        }
		play(id, callback, autoPlay, end, start);
	}

	@ReplacedByNativeOnDeploy
	private void play(String id, Callback callback, boolean autoPlay, int end, int start) {

	}

}
