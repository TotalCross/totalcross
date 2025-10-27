package totalcross.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class VideoCaptureActivity extends AppCompatActivity {

    public enum Qualidade {ALTO, MEDIO, BAIXO}

    public static final String EXTRA_MAX_SECONDS = "max_seconds";
    public static final String EXTRA_TARGET_FPS = "target_fps";
    public static final String EXTRA_QUALITY = "quality";

    private String lastSavedVideoPath = null;

    // public static Intent createIntent(Context ctx, int maxSeconds, int fps, Qualidade qualidade) {
    //     Intent i = new Intent(ctx, VideoCaptureActivity.class);
    //     i.putExtra(EXTRA_MAX_SECONDS, maxSeconds);
    //     i.putExtra(EXTRA_TARGET_FPS, fps);
    //     i.putExtra(EXTRA_QUALITY, qualidade.name());
    //     return i;
    // }

    public String getLastSavedVideoPath() {
        return lastSavedVideoPath;
    }

    private static final String TAG = "VideoCapture";
    private static final int REQ_PERMS = 101;
    private static final int PLAYBACK_REQ = 201; // novo request code para o player

    private TextureView textureView;
    private ImageButton btnRecord;
    private ImageButton btnBackCapture;

    // Progress UI
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private long recordingStartTime = 0;
    private final int PROGRESS_UPDATE_INTERVAL = 50; // ms
    private boolean progressActive = false;

    // Camera2
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private Size previewSize, videoSize;
    private CaptureRequest.Builder previewBuilder;
    private CameraCharacteristics cameraCharacteristics;

    // Background thread
    private HandlerThread bgThread;
    private Handler bgHandler;

    // Recorder
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private int maxSeconds = 30;
    private int targetFps = 30;
    private Qualidade qualidade = Qualidade.MEDIO;

    // Drawable de progresso (mesma instância)
    private RecordProgressDrawable progressDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);

        textureView = findViewById(R.id.textureView);
        btnRecord = findViewById(R.id.btnRecord);
        btnBackCapture = findViewById(R.id.btnBackCapture);

        Intent in = getIntent();
        int ems = in.getIntExtra(EXTRA_MAX_SECONDS, 60*5);
        maxSeconds = ems <= 0 ? 60*5 : Math.max(1, ems);

        targetFps = Math.max(5, in.getIntExtra(EXTRA_TARGET_FPS, 60));
        try {
            int q = in.getIntExtra(EXTRA_QUALITY, 2);
            if (q == 1) {
                qualidade = Qualidade.BAIXO;
            } else if (q == 3) {
                qualidade = Qualidade.ALTO;
            } else {
                qualidade = Qualidade.MEDIO;
            }
            // Toast.makeText(this, "QUALIDADE 1 " + qualidade, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            qualidade = Qualidade.MEDIO;
        }
        // Toast.makeText(this, "QUALIDADE 2 " + qualidade, Toast.LENGTH_LONG).show();

        lastSavedVideoPath = in.getStringExtra("file");
        if (lastSavedVideoPath == null) {
            lastSavedVideoPath = buildOutputFilePath();
        }

        // cria a instância única do drawable
        progressDrawable = new RecordProgressDrawable();

        // Touch: press & hold to record, release to stop
        btnRecord.setOnTouchListener((v, ev) -> {
            if (!permissionsGranted()) {
                requestPermissions();
                return true;
            }
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // coloca o drawable (mesma instância) e inicia o fluxo
                    btnRecord.setImageDrawable(progressDrawable);
                    progressDrawable.setProgress(0f);
                    startRecordingFlow();            // prepara e inicia gravação async
                    startRecordButtonProgress();     // inicia a animação/local progress
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Se já está gravando -> pára a gravação; se não, cancela o que estava pendente
                    if (isRecording) {
                        stopRecordingAndPreview();
                    } else {
                        cancelPendingRecording();
                    }
                    stopRecordButtonProgress();
                    return true;
            }
            return false;
        });

        btnBackCapture.setOnClickListener(v -> {
            // Intent data = new Intent();
            // if (lastSavedVideoPath != null) {
            //     data.setData(Uri.fromFile(new File(lastSavedVideoPath)));
            //     data.putExtra("video_path", lastSavedVideoPath);
            // }
            // setResult(RESULT_OK, data);
            setResult(RESULT_CANCELED);
            finish();
        });

        textureView.setSurfaceTextureListener(surfaceListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBgThread();
        if (textureView.isAvailable()) openCamera();
        else textureView.setSurfaceTextureListener(surfaceListener);
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBgThread();
        super.onPause();
    }

    // Permissões
    private boolean permissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQ_PERMS);
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == REQ_PERMS && permissionsGranted()) {
            if (textureView.isAvailable()) openCamera();
        } else {
            Toast.makeText(this, "Missing permissions", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // TextureView listener
    private final TextureView.SurfaceTextureListener surfaceListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture st, int w, int h) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture st, int w, int h) {
            configureTransform(w, h);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture st) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture st) {
        }
    };

    // Camera open / preview
    private void openCamera() {
        if (!permissionsGranted()) return;
        try {
            CameraManager cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = chooseBackCamera(cm);
            if (cameraId == null) {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_LONG).show();
                return;
            }

            cameraCharacteristics = cm.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                Toast.makeText(this, "Camera configuration unavailable", Toast.LENGTH_LONG).show();
                return;
            }

            videoSize = chooseVideoSize(map, qualidade);
            // previewSize agora igual ao videoSize para evitar distorção
            previewSize = videoSize;

            configureTransform(textureView.getWidth(), textureView.getHeight());

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cm.openCamera(cameraId, stateCallback, bgHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "openCamera", e);
            Toast.makeText(this, "Cannot open camera", Toast.LENGTH_LONG).show();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
            Toast.makeText(VideoCaptureActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
        }
    };

    private void startPreviewSession() {
        if (cameraDevice == null || !textureView.isAvailable() || videoSize == null) return;
        try {
            closeSession();

            SurfaceTexture st = textureView.getSurfaceTexture();
            // Use videoSize para preview e gravação
            st.setDefaultBufferSize(videoSize.getWidth(), videoSize.getHeight());
            Surface previewSurface = new Surface(st);

            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(previewSurface);
            setFpsRange(previewBuilder, targetFps);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) return;
                            captureSession = session;
                            try {
                                captureSession.setRepeatingRequest(previewBuilder.build(), null, bgHandler);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "startPreviewSession setRepeatingRequest", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(VideoCaptureActivity.this, "Failed to start preview", Toast.LENGTH_LONG).show();
                        }
                    }, bgHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "startPreviewSession", e);
        }
    }

    private void startRecordingFlow() {
        try {
            if (cameraDevice == null) {
                openCamera();
            }

            // lastSavedVideoPath = buildOutputFilePath();
            prepareMediaRecorder(lastSavedVideoPath);

            SurfaceTexture st = textureView.getSurfaceTexture();
            // Use videoSize para gravação
            st.setDefaultBufferSize(videoSize.getWidth(), videoSize.getHeight());
            Surface previewSurface = new Surface(st);
            Surface recordSurface = mediaRecorder.getSurface();

            closeSession();

            final CaptureRequest.Builder recordBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            recordBuilder.addTarget(previewSurface);
            recordBuilder.addTarget(recordSurface);
            setFpsRange(recordBuilder, targetFps);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                captureSession.setRepeatingRequest(recordBuilder.build(), null, bgHandler);
                                runOnUiThread(() -> {
                                    try {
                                        mediaRecorder.start();
                                        isRecording = true;
                                    } catch (Exception e) {
                                        Log.e(TAG, "mediaRecorder.start", e);
                                        Toast.makeText(VideoCaptureActivity.this, "Failed to start recording", Toast.LENGTH_LONG).show();
                                        cleanupRecorder();
                                    }
                                });
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "onConfigured record", e);
                                cleanupRecorder();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(VideoCaptureActivity.this, "Failure on record configuration", Toast.LENGTH_LONG).show();
                            cleanupRecorder();
                        }
                    }, bgHandler);

        } catch (Exception e) {
            Log.e(TAG, "startRecordingFlow", e);
            cleanupRecorder();
            throw new RuntimeException(e);
        }
    }

    private void stopRecordingAndPreview() {
        // Se não estava gravando, apenas limpa e retorna
        if (!isRecording) {
            cleanupRecorder();
            // garante que a interface volte ao preview
            showCameraPreviewUI();
            reopenCameraForPreview();
            isRecording = false;
            return;
        }

        isRecording = false;

        try {
            if (captureSession != null) captureSession.stopRepeating();
        } catch (Exception ignored) {
        }

        try {
            if (mediaRecorder != null) mediaRecorder.setOnInfoListener(null);
            if (mediaRecorder != null)
                mediaRecorder.stop(); // pode lançar RuntimeException se muito curto
        } catch (RuntimeException e) {
            Log.w(TAG, "early stop(); removing corrupted file.", e);
            deleteLastVideoIfExists();
            lastSavedVideoPath = null;
        } finally {
            cleanupRecorder();
        }

        // Mostra vídeo para o usuário, se existir
        if (lastSavedVideoPath != null) {
            showPlaybackUI(lastSavedVideoPath);
        } else {
            // Volta ao preview se falhou
            showCameraPreviewUI();
            reopenCameraForPreview();
        }
    }

    // Caso o usuário soltar antes da gravação começar: limpa pendências
    private void cancelPendingRecording() {
        progressActive = false;
        try {
            cleanupRecorder();
            closeSession();
        } catch (Exception ignored) {
        }
        lastSavedVideoPath = null;
        showCameraPreviewUI();
        reopenCameraForPreview();
    }

    private void prepareMediaRecorder(String outputPath) throws IOException, CameraAccessException {
        if (mediaRecorder == null) mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        VideoParams params = chooseVideoParams(qualidade, targetFps, videoSize);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(outputPath);

        mediaRecorder.setVideoEncodingBitRate(params.videoBitrate);
        mediaRecorder.setVideoFrameRate(params.fps);
        // grava sempre na resolução exata de videoSize
        mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mediaRecorder.setAudioEncodingBitRate(128_000);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        mediaRecorder.setMaxDuration(maxSeconds * 1000);
        mediaRecorder.setOnInfoListener((mr, what, extra) -> {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED && isRecording) {
                runOnUiThread(this::stopRecordingAndPreview);
            }
        });

        int hint = getOrientationHint();
        mediaRecorder.setOrientationHint(hint);

        mediaRecorder.prepare();
    }

    // UI States
    private void showPlaybackUI(String path) {
        try {
            closeSession();
        } catch (Exception ignored) {
        }

        // ocultamos a preview local enquanto o player externo está em primeiro plano
        textureView.setVisibility(View.GONE);
        btnRecord.setVisibility(View.GONE);

        Intent videoIntent = new Intent(this, VideoPlayerActivity.class);
        videoIntent.putExtra("video_path", path);
        videoIntent.putExtra("playback_only", false);
        // abrir como Activity para resultado - quando voltar restauramos a captura
        startActivityForResult(videoIntent, PLAYBACK_REQ);
        // Não ativamos previewControls aqui (evita a "tela branca" ao voltar)
    }

    private void showCameraPreviewUI() {
        textureView.setVisibility(View.VISIBLE);
        btnRecord.setVisibility(View.VISIBLE);
        btnRecord.setImageResource(R.drawable.bg_record_button_small); // garantia de ícone padrão
        progressDrawable.setProgress(0f);
    }

    private void reopenCameraForPreview() {
        if (cameraDevice == null) openCamera();
        else startPreviewSession();
    }

    private void deleteLastVideoIfExists() {
        if (lastSavedVideoPath != null) {
            try {
                new File(lastSavedVideoPath).delete();
            } catch (Exception ignored) {
            }
            lastSavedVideoPath = null;
        }
    }

    // --- Progresso visual na borda ---
    private void startRecordButtonProgress() {
        recordingStartTime = System.currentTimeMillis();
        progressActive = true;
        progressDrawable.setProgress(0f);
        progressHandler.post(recordButtonProgressRunnable);
    }

    private void stopRecordButtonProgress() {
        progressActive = false;
        progressHandler.removeCallbacks(recordButtonProgressRunnable);
        progressDrawable.setProgress(0f);
        btnRecord.setImageResource(R.drawable.bg_record_button_small);
    }

    private final Runnable recordButtonProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (!progressActive) return;
            long elapsed = System.currentTimeMillis() - recordingStartTime;
            float progress = Math.min(1f, (float) elapsed / (maxSeconds * 1000));
            progressDrawable.setProgress(progress);
            btnRecord.invalidate(); // força redraw
            if (progress < 1f) {
                progressHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            } else {
                // atingiu o máximo
                progressActive = false;
                if (isRecording) {
                    stopRecordingAndPreview();
                } else {
                    // se não tinha começado a gravar, apenas cancela
                    cancelPendingRecording();
                }
                stopRecordButtonProgress();
            }
        }
    };

    // --- Util: escolha de câmera/tamanhos/parametrização ---
    private String chooseBackCamera(CameraManager cm) throws CameraAccessException {
        for (String id : cm.getCameraIdList()) {
            CameraCharacteristics c = cm.getCameraCharacteristics(id);
            Integer facing = c.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) return id;
        }
        String[] all = cm.getCameraIdList();
        return all.length > 0 ? all[0] : null;
    }

    private static Size chooseVideoSize(StreamConfigurationMap map, Qualidade q) {
        Size[] choices = map.getOutputSizes(MediaRecorder.class);
        List<Size> preferred = new ArrayList<>();
        if (q == Qualidade.ALTO) {
            preferred.add(new Size(1920, 1080));
            preferred.add(new Size(1280, 720));
        } else if (q == Qualidade.MEDIO) {
            preferred.add(new Size(1280, 720));
            preferred.add(new Size(960, 540));
            preferred.add(new Size(854, 480));
        } else {
            preferred.add(new Size(854, 480));
            preferred.add(new Size(720, 480));
            preferred.add(new Size(640, 480));
        }
        for (Size p : preferred)
            for (Size c : choices) {
                if (c.getWidth() == p.getWidth() && c.getHeight() == p.getHeight()) return c;
            }
        Size best = choices[0];
        for (Size c : choices) {
            if (c.getWidth() * c.getHeight() < best.getWidth() * best.getHeight()) best = c;
        }
        return best;
    }

    private static Size chooseOptimalPreviewSize(Size[] choices, Size target) {
        List<Size> bigEnough = new ArrayList<>();
        for (Size option : choices) {
            if (option.getWidth() >= target.getWidth() && option.getHeight() >= target.getHeight())
                bigEnough.add(option);
        }
        if (bigEnough.size() > 0) {
            Size min = bigEnough.get(0);
            for (Size s : bigEnough) {
                if (s.getWidth() * s.getHeight() < min.getWidth() * min.getHeight()) min = s;
            }
            return min;
        } else {
            return choices[0];
        }
    }

    private static class VideoParams {
        final Size size;
        final int fps;
        final int videoBitrate;

        VideoParams(Size size, int fps, int videoBitrate) {
            this.size = size;
            this.fps = fps;
            this.videoBitrate = videoBitrate;
        }
    }

    private static VideoParams chooseVideoParams(Qualidade q, int fps, Size chosenSize) {
        int br;
        switch (q) {
            case ALTO:
                br = 10_000_000;
                break;
            case MEDIO:
                br = 5_000_000;
                break;
            default:
                br = 2_000_000;
                break;
        }
        int targetFps = Math.max(10, Math.min(60, fps));
        return new VideoParams(chosenSize, targetFps, br);
    }

    private void setFpsRange(CaptureRequest.Builder b, int target) {
        try {
            Range<Integer>[] ranges = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            if (ranges == null || ranges.length == 0) return;
            Range<Integer> best = ranges[0];
            for (Range<Integer> r : ranges) {
                if (r.contains(target)) {
                    if ((best.getUpper() - best.getLower()) > (r.getUpper() - r.getLower()))
                        best = r;
                }
            }
            b.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, best);
        } catch (Exception ignored) {
        }
    }

    private String buildOutputFilePath() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (dir != null && !dir.exists()) dir.mkdirs();
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return new File(dir, "VID_" + ts + ".mp4").getAbsolutePath();
    }

    private int getOrientationHint() throws CameraAccessException {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
        int degrees;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
            default: degrees = 0;
        }
        if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            // Frontal: compensação padrão Android
            return (sensorOrientation + degrees) % 360;
        } else {
            // Traseira: padrão Android
            return (sensorOrientation - degrees + 360) % 360;
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == textureView || null == previewSize) return;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        // bufferRect agora usa videoSize para manter proporção correta
        RectF bufferRect = new RectF(0, 0, videoSize.getHeight(), videoSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / videoSize.getHeight(),
                    (float) viewWidth / videoSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (rotation == Surface.ROTATION_180) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    // Threads & limpeza
    private void startBgThread() {
        // evita criar múltiplas threads se já estiver ativa
        if (bgThread != null && bgThread.isAlive()) return;
        bgThread = new HandlerThread("CameraBackground");
        bgThread.start();
        bgHandler = new Handler(bgThread.getLooper());
    }

    private void stopBgThread() {
        if (bgThread != null) {
            bgThread.quitSafely();
            try {
                bgThread.join();
            } catch (InterruptedException ignored) {
            }
            bgThread = null;
            bgHandler = null;
        }
    }

    private void closeSession() {
        if (captureSession != null) {
            try {
                captureSession.close();
            } catch (Exception ignored) {
            }
            captureSession = null;
        }
    }

    private void closeCamera() {
        closeSession();
        if (cameraDevice != null) {
            try {
                cameraDevice.close();
            } catch (Exception ignored) {
            }
            cameraDevice = null;
        }
        cleanupRecorder();
    }

    private void cleanupRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception ignored) {
            }
            mediaRecorder = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAYBACK_REQ) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            } else {
                // Ao voltar do player, garantir que a UI de captura volte (sem mostrar os controles)
                showCameraPreviewUI();
                // garante que a thread de background exista antes de reabrir a câmera
                startBgThread();
                reopenCameraForPreview();
            }
        }
    }

    /**
     * Drawable customizado que mostra a borda se preenchendo conforme o progresso
     */
    class RecordProgressDrawable extends android.graphics.drawable.Drawable {
        private float progress = 0f; // 0.0 a 1.0
        private final Paint paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint paintProgress = new Paint(Paint.ANTI_ALIAS_FLAG);

        public RecordProgressDrawable() {
            paintCircle.setStyle(Paint.Style.FILL);
            paintCircle.setColor(0xFFE53935); // vermelho do centro

            paintBorder.setStyle(Paint.Style.STROKE);
            paintBorder.setColor(Color.WHITE);
            paintBorder.setStrokeWidth(12f);
            paintBorder.setAlpha(230);

            paintProgress.setStyle(Paint.Style.STROKE);
            paintProgress.setColor(0xFF000000); // arco de progresso (preto)
            paintProgress.setStrokeWidth(12f);
            paintProgress.setStrokeCap(Paint.Cap.ROUND);
        }

        public void setProgress(float p) {
            progress = Math.max(0f, Math.min(1f, p));
            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            int w = getBounds().width();
            int h = getBounds().height();
            float cx = w / 2f;
            float cy = h / 2f;
            float radius = Math.min(w, h) / 2.5f;

            // círculo central
            canvas.drawCircle(cx, cy, radius, paintCircle);

            // borda base branca
            canvas.drawCircle(cx, cy, radius + 10f, paintBorder);

            // arco de progresso acima da borda
            if (progress > 0f) {
                RectF oval = new RectF(cx - (radius + 10f), cy - (radius + 10f),
                        cx + (radius + 10f), cy + (radius + 10f));
                canvas.drawArc(oval, -90, 360 * progress, false, paintProgress);
            }
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
