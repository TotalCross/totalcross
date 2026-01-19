package totalcross.android;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

public class VideoPlayerActivity extends AdjustedInsetsActivity {

    private VideoView videoView;
    private ImageButton btnPlayPause, btnAvancar, btnRetroceder, btnSalvar, btnBackPlayer;
    private SeekBar seekBar;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.videoView);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnAvancar = findViewById(R.id.btnForward);
        btnRetroceder = findViewById(R.id.btnRewind);
        seekBar = findViewById(R.id.seekBar);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnBackPlayer = findViewById(R.id.btnBackPlayer);

        // caminho do vídeo recebido pela intent
        String videoPath = getIntent().getStringExtra("video_path");
        if (videoPath == null) {
            Toast.makeText(this, "File not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        boolean playbackOnly = getIntent().getBooleanExtra("playback_only", true);
        if (playbackOnly) {
            btnSalvar.setVisibility(View.GONE);
        }

        // Toast.makeText(this, "File1 " + videoPath, Toast.LENGTH_SHORT).show();
        File videoFile = new File(videoPath);

        // Toast.makeText(this, "File2 " + videoFile, Toast.LENGTH_SHORT).show();
        Uri videoUri = Uri.fromFile(videoFile);

        // Toast.makeText(this, "File3 " + videoUri, Toast.LENGTH_SHORT).show();
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(mp -> {
            seekBar.setMax(videoView.getDuration());
            videoView.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause_new);
            updateSeekBar();
        });

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnAvancar.setOnClickListener(v -> seekBy(1000));
        btnRetroceder.setOnClickListener(v -> seekBy(-1000));

        /**
         *
         * */
        btnSalvar.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();

            // Toast.makeText(this, "Vídeo salvo em: " + videoPath, Toast.LENGTH_LONG).show();
            // MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            // try {
            //     mediaMetadataRetriever.setDataSource(videoPath);
            //     File file = new File("/sdcard/DCIM/", videoFile.getName());
            //     if (file.exists()) {
            //         Toast.makeText(this, "Vídeo já existe na galeria.", Toast.LENGTH_SHORT).show();
            //     } else {
            //         FileInputStream in = new FileInputStream(videoFile);
            //         FileOutputStream out = new FileOutputStream(file);
            //         byte[] buf = new byte[1024];
            //         int len;
            //         while ((len = in.read(buf)) > 0) {
            //             out.write(buf, 0, len);
            //         }
            //         in.close();
            //         out.close();
            //         Toast.makeText(this, "Vídeo salvo na galeria: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            //     }
            // } catch (Exception ignored) {
            // } finally {
            //     try { mediaMetadataRetriever.release(); } catch (Exception ignored) {}
            // }
        });

        btnBackPlayer.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && videoView != null) videoView.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                isSeeking = false;
            }
        });

        videoView.setOnCompletionListener(mp -> btnPlayPause.setImageResource(R.drawable.ic_play_new));
    }

    private void togglePlayPause() {
        if (videoView.isPlaying()) {
            videoView.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play_new);
        } else {
            videoView.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause_new);
        }
    }

    private void seekBy(int ms) {
        int pos = videoView.getCurrentPosition() + ms;
        pos = Math.max(0, Math.min(pos, videoView.getDuration()));
        videoView.seekTo(pos);
    }

    private void updateSeekBar() {
        if (videoView != null && videoView.isPlaying() && !isSeeking) {
            seekBar.setProgress(videoView.getCurrentPosition());
        }
        handler.postDelayed(this::updateSeekBar, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        if (videoView.isPlaying()) videoView.pause();
    }
}