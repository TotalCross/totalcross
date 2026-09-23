// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.Arrays;

import totalcross.io.File;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Flick;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.RenderingOptimizations;
import totalcross.ui.ScrollContainer;
import totalcross.ui.Window;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;
import totalcross.ui.gfx.Graphics;

/** Real-corpus scrolling workload based on the customer-provided Tcsort layout. */
public class ImageScrollRealWorkloadBenchmarkApp extends MainWindow implements TimerListener {
  private static final int IMAGE_COUNT = 663;
  private static final int COLUMN_COUNT = 3;
  private static final int SCROLL_STEP = 120;
  private static final int SCROLL_DURATION_MS = 3000;
  private static final int FRAME_INTERVAL_MS = 16;
  private static final long NANOS_PER_MILLISECOND = 1000000L;
  private static final long FRAME_INTERVAL_NS = FRAME_INTERVAL_MS * NANOS_PER_MILLISECOND;
  private static final long FRAME_THRESHOLD_16_67_NS = 16_670_000L;
  private static final long FRAME_THRESHOLD_33_3_NS = 33_300_000L;
  private static final long FRAME_THRESHOLD_50_NS = 50_000_000L;
  private static final long FRAME_THRESHOLD_100_NS = 100_000_000L;
  private static final int POC_IMAGE_COUNT = 120;
  private static final long RELEASE_CANDIDATE_MASK = 32795L;
  private static final int POC_SEGMENT_COUNT = 4;
  private static final int[] POC_SEGMENT_DURATIONS_MS = {500, 250, 200, 150};
  private static final long POC_FRAME_INTERVAL_NS = 16L * NANOS_PER_MILLISECOND;
  private static final int EXPECTED_LOGICAL_WIDTH = 540;
  private static final int EXPECTED_LOGICAL_HEIGHT = 960;
  private static final String MODE_BENCHMARK = "benchmark";
  private static final String[] FEATURE_NAMES = {
      "DECODE_ZERO_COPY", "RASTER_OPACITY_METADATA", "RASTER_OPAQUE_WRITE_PIXELS",
      "RASTER_ROW_READBACK", "RASTER_DIRECT_COLOR_MATERIALIZATION", "STORAGE_RGB565",
      "STORAGE_GRAY8", "STORAGE_ARGB4444", "CACHE_BYTE_BUDGET",
      "CACHE_MEMORY_PRESSURE_EVICTION", "GPU_DISCARD_CPU_BACKING", "STORAGE_MMAP_LARGE_BACKINGS",
      "DIAGNOSTIC_ACCOUNTING", "RASTER_TARGET_COLORTYPE_CONVERSION",
      "RASTER_PHYSICAL_VARIANT_CACHE", "RASTER_PHYSICAL_IDENTITY_FOLDING"
  };
  private static final String[] SAVE_COUNT_BUCKET_NAMES = {
      "0", "1", "2", "3", "4", "5OrMore"
  };
  private static final String[] MAPPING_SUBREASON_NAMES = {
      "CompileGeometry", "MatrixInversion", "RootToDevice", "DestinationAxis",
      "DestinationFractional", "SourceMapping", "ValidRoot", "UnsupportedTransform",
      "ExplicitClip", "VisibleMapping"
  };

  private ScrollContainer mainContainer;
  private ScrollContainer scroll;
  private String[] benchmarkImagePaths;
  private int tileWidth;
  private int rowCount;
  private int imageControlCount;
  private long corpusFileEnumerationElapsedNs;
  private long imageLoadElapsedNs;
  private long imageScaleElapsedNs;
  private long imageControlAttachElapsedNs;
  private long uiBuildElapsedNs;
  private String imageDir;
  private String maskArgument;
  private long runNumber;
  private int benchmarkPassCount = 1;
  private String outputDir;
  private String runOutputDir;
  private String datasetHashArgument;
  private String prefetchProfile;
  private String accountingProfile;
  private String benchmarkProfile;
  private boolean scrollRasterReuseProfile;
  private boolean releaseDefaultScrollProfile;
  private boolean releaseCandidateScrollProfile;
  private boolean rasterReuseBenchmarkProfile;
  private String renderingReuseProfile;
  private int workloadImageCount = IMAGE_COUNT;
  private long nativeDrawMask = -1;
  private long nativeDecodeMask = -1;
  private long observedDrawMask = -1;
  private long observedDecodeMask = -1;
  private int measuredRowPaints;
  private int measuredImagePaints;
  private long benchmarkTargetWidth = -1;
  private long benchmarkTargetHeight = -1;
  private long benchmarkTargetRowBytes = -1;
  private long benchmarkTargetAlphaType = -1;
  private long benchmarkTargetColorType = -1;
  private long benchmarkTargetColorClass = -1;
  private long benchmarkTargetPixelBytes = -1;
  private long benchmarkN32ColorType = -1;
  private long benchmarkRendererBackend = -1;
  private long benchmarkDrawableWidth = -1;
  private long benchmarkDrawableHeight = -1;
  private long benchmarkRefreshRate = -1;
  private double benchmarkSurfaceScaleX = -1;
  private double benchmarkSurfaceScaleY = -1;
  private long prefetchElapsedNs;
  private long prefetchRequestCount;
  private long prefetchReadyCount;
  private long prefetchFailedCount;
  private long prefetchNotPrefetchableCount;
  private long prefetchBackingLiveBytes;
  private long prefetchBackingPeakBytes;
  private long prefetchTargetedJpegDecodes;
  private long prefetchFullJpegDecodes;
  private long prefetchImageMaterializations;
  private long prefetchNativeGeometryMaterializations;
  private Counters prefetchCounters;
  private boolean benchmarkStarted;
  private boolean prefetchComplete;
  private TimerEvent prefetchTimer;
  private boolean benchmarkReady;
  private long scrollDurationNs = SCROLL_DURATION_MS * NANOS_PER_MILLISECOND;

  public ImageScrollRealWorkloadBenchmarkApp() {
    super("", Window.NO_BORDER);
    Flick.defaultShortestFlick = 200;
    Flick.defaultLongestFlick = 2500;
    Flick.defaultFlickAcceleration = 1.8;
    Settings.scrollDistanceOnMouseWheelMove = SCROLL_STEP;
    setDeviceTitle("image-scroll-real-workload");
    String applicationRoot = ImageRasterBenchmarkSupport.argument(
        getCommandLine(), "app-root", null);
    if (applicationRoot != null && applicationRoot.length() > 0) {
      Settings.appPath = applicationRoot;
    }
    setUIStyle(Settings.ANDROID_UI);
  }

  @Override
  public void initUI() {
    super.initUI();
    try {
      String mode = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "mode", MODE_BENCHMARK);
      ImageRasterBenchmarkSupport.require(MODE_BENCHMARK.equals(mode),
          "unsupported mode: " + mode);
      imageDir = ImageRasterBenchmarkSupport.argument(getCommandLine(), "corpus", null);
      maskArgument = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "image-optimization", null);
      runNumber = parseRunNumber(ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "run", "0"));
      benchmarkPassCount = ImageRasterBenchmarkSupport.integerArgument(
          getCommandLine(), "passes", 1);
      outputDir = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "output", "benchmark-output");
      datasetHashArgument = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "dataset-hash", "unavailable");
      int scrollDurationMs = ImageRasterBenchmarkSupport.integerArgument(
          getCommandLine(), "duration", SCROLL_DURATION_MS);
      scrollDurationNs = (long) scrollDurationMs * NANOS_PER_MILLISECOND;
      prefetchProfile = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "prefetch", "off");
      accountingProfile = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "accounting", "on");
      benchmarkProfile = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "profile", "standard");
      scrollRasterReuseProfile = "scroll-raster-reuse-poc".equals(benchmarkProfile);
      releaseDefaultScrollProfile = "release-default-scroll".equals(benchmarkProfile);
      releaseCandidateScrollProfile = "release-candidate-scroll".equals(benchmarkProfile);
      rasterReuseBenchmarkProfile = scrollRasterReuseProfile || releaseDefaultScrollProfile
          || releaseCandidateScrollProfile;
      workloadImageCount = scrollRasterReuseProfile ? POC_IMAGE_COUNT : IMAGE_COUNT;
      configureScrollRasterReuse();
      ImageRasterBenchmarkSupport.require(imageDir != null && imageDir.length() > 0,
          "missing --corpus=<dir>");
      ImageRasterBenchmarkSupport.require(
          (maskArgument != null && maskArgument.length() > 0)
              || "release-default-scroll".equals(benchmarkProfile),
          "missing --image-optimization=<mask> outside the release-default-scroll profile");
      ImageRasterBenchmarkSupport.require(scrollDurationNs > 0,
          "duration must be positive");
      ImageRasterBenchmarkSupport.require(rasterReuseBenchmarkProfile
          ? benchmarkPassCount == 2 : benchmarkPassCount == 1 || benchmarkPassCount == 3,
          rasterReuseBenchmarkProfile ? "scroll raster reuse profile requires passes=2"
              : "passes must be 1 or 3");
      ImageRasterBenchmarkSupport.require("off".equals(prefetchProfile)
          || "on".equals(prefetchProfile),
          "prefetch must be off or on");
      ImageRasterBenchmarkSupport.require("off".equals(accountingProfile)
          || "on".equals(accountingProfile),
          "accounting must be off or on");
      ImageRasterBenchmarkSupport.ensureDirectory(outputDir);
      ImageRasterBenchmarkSupport.ensureDirectory(
          ImageRasterBenchmarkSupport.joinPath(outputDir, "runs"));
      runOutputDir = ImageRasterBenchmarkSupport.joinPath(
          ImageRasterBenchmarkSupport.joinPath(outputDir, "runs"), runName());
      ImageRasterBenchmarkSupport.ensureDirectory(runOutputDir);
      requireBenchmarkResolution();
      configureMask();
      validateDefaultMaskTransport();
      captureTargetMetrics();
      Image.resetImageOperationAccountingForBenchmarkTest(accountingEnabled());
      ImagePreparation.resetAccountingForTest();

      long buildStartNs = System.nanoTime();
      long corpusScanStartNs = System.nanoTime();
      String[] imagePaths = sortedCorpusPaths(imageDir);
      corpusFileEnumerationElapsedNs = System.nanoTime() - corpusScanStartNs;
      ImageRasterBenchmarkSupport.require(imagePaths.length >= workloadImageCount,
          "expected at least " + workloadImageCount + " corpus files but found " + imagePaths.length);
      if (imagePaths.length != workloadImageCount) {
        imagePaths = Arrays.copyOf(imagePaths, workloadImageCount);
      }
      buildUi(imagePaths);
      benchmarkImagePaths = imagePaths;
      uiBuildElapsedNs = System.nanoTime() - buildStartNs;
      ImageRasterBenchmarkSupport.require(rowCount == workloadImageCount / COLUMN_COUNT,
          "unexpected row count " + rowCount);
      ImageRasterBenchmarkSupport.require(imageControlCount == workloadImageCount,
          "unexpected ImageControl count " + imageControlCount);
      ImageRasterBenchmarkSupport.require(scroll.sbV != null, "real workload vertical scrollbar");
      int minimum = scroll.sbV.getMinimum();
      int maximum = validMaximum();
      ImageRasterBenchmarkSupport.require(maximum > minimum,
          "real workload content did not extend beyond the viewport");
      scroll.sbV.setValue(minimum);

      addTimerListener(this);
      if (prefetchEnabled()) {
        Image.resetImageOperationAccountingForBenchmarkTest(accountingEnabled());
        ImagePreparation.resetAccountingForTest();
        final long prefetchStartNs = System.nanoTime();
        prefetchTimer = addTimer(10);
        scroll.prepareForDisplay(new Runnable() {
          @Override
          public void run() {
            prefetchElapsedNs = System.nanoTime() - prefetchStartNs;
            capturePrefetchAccounting();
            prefetchComplete = true;
          }
        });
        return;
      }
      benchmarkReady = true;
      prefetchTimer = addTimer(50);
    } catch (Throwable failure) {
      String error = reportFailure(failure);
      finishBenchmark(false, error);
    }
  }

  private static void requireBenchmarkResolution() {
    ImageRasterBenchmarkSupport.require(
        Settings.screenWidth == EXPECTED_LOGICAL_WIDTH
            && Settings.screenHeight == EXPECTED_LOGICAL_HEIGHT,
        "benchmark requires logical resolution " + EXPECTED_LOGICAL_WIDTH + "x"
            + EXPECTED_LOGICAL_HEIGHT + " but got " + Settings.screenWidth + "x"
            + Settings.screenHeight);
  }

  private void executeBenchmark() {
    if (benchmarkStarted || !benchmarkReady && !prefetchComplete) {
      return;
    }
    benchmarkStarted = true;
    if (prefetchTimer != null) {
      removeTimer(prefetchTimer);
      prefetchTimer = null;
    }
    try {
      if (rasterReuseBenchmarkProfile) {
        executeScrollRasterReuseProfile();
        finishBenchmark(true, "");
        return;
      }
      int minimum = scroll.sbV.getMinimum();
      int maximum = validMaximum();
      for (int passIndex = 0; passIndex < benchmarkPassCount; passIndex++) {
        boolean forward = passIndex != 1;
        String passName = benchmarkPassCount == 1 ? "cold"
            : passIndex == 0 ? "cold-forward"
            : passIndex == 1 ? "warm-reverse" : "warm-forward";
        PassResult result = runPass(passName, forward,
            forward ? minimum : maximum, maximum);
        printPass(result, passIndex + 1, benchmarkPassCount);
        if (benchmarkPassCount == 1) {
          writeRunFrames(result);
          writeRunSummary(result);
        } else {
          String passOutputDir = ImageRasterBenchmarkSupport.joinPath(
              ImageRasterBenchmarkSupport.joinPath(runOutputDir, "passes"), passName);
          ImageRasterBenchmarkSupport.ensureDirectory(passOutputDir);
          writeRunFrames(result, passOutputDir);
          writeRunSummary(result, passOutputDir);
          writeRunMemory(passOutputDir);
          writeRunTimeline(passOutputDir);
          if (passIndex == benchmarkPassCount - 1) {
            writeRunFrames(result);
            writeRunSummary(result);
          }
        }
      }
      finishBenchmark(true, "");
    } catch (Throwable failure) {
      String error = reportFailure(failure);
      finishBenchmark(false, error);
    }
  }

  private void capturePrefetchAccounting() {
    prefetchRequestCount = ImagePreparation.requestCountForTest();
    prefetchReadyCount = ImagePreparation.readyCountForTest();
    prefetchFailedCount = ImagePreparation.failedCountForTest();
    prefetchNotPrefetchableCount = ImagePreparation.notPrefetchableCountForTest();
    prefetchBackingLiveBytes = NativeImageBacking.backingBytesLiveForTest();
    prefetchBackingPeakBytes = NativeImageBacking.backingBytesPeakLiveForTest();
    prefetchTargetedJpegDecodes = Image.targetedDecodeInvocationCountForTest();
    prefetchFullJpegDecodes = Image.fullDecodeInvocationCountForTest();
    prefetchImageMaterializations = Image.materializationCountForTest();
    prefetchNativeGeometryMaterializations = Image.nativeGeometryMaterializationCountForTest();
    prefetchCounters = Counters.capture();
    validateJpegDiagnostics(prefetchCounters, "prefetch");
  }

  private void finishBenchmark(boolean overallPass, String error) {
    Control.setBenchmarkScreenTimingForTest(false);
    long requestedMask = ImageOptimizationSettings.getMask();
    long effectiveMask = ImageOptimizationSettings.getEffectiveMask();
    if (requestedMask != effectiveMask) {
      overallPass = false;
      error = error.length() == 0 ? "INVALID_CONFIGURATION" : error + "_INVALID_CONFIGURATION";
    }
    String summary = "fixture=ImageScrollRealWorkloadBenchmarkApp,record=summary"
        + ",resolution=" + width + "x" + height
        + ",prefetch_profile=" + String.valueOf(prefetchProfile)
        + ",accounting=" + accountingProfile
        + ",requested_mask=" + requestedMask
        + ",effective_mask=" + effectiveMask
        + ",native_draw_mask=" + nativeDrawMask
        + ",native_decode_mask=" + nativeDecodeMask
        + ",observed_draw_mask=" + observedDrawMask
        + ",observed_decode_mask=" + observedDecodeMask
        + ",run=" + runNumber
        + ",passes=" + benchmarkPassCount
        + ",output_dir=" + String.valueOf(runOutputDir)
        + ",image_dir=" + String.valueOf(imageDir)
        + ",profile=" + benchmarkProfile
        + ",image_count=" + workloadImageCount + ",rows=" + rowCount
        + ",image_controls=" + imageControlCount + ",tile_logical=" + tileWidth
        + ",corpus_file_enumeration_elapsed_ns=" + corpusFileEnumerationElapsedNs
        + ",image_load_elapsed_ns=" + imageLoadElapsedNs
        + ",image_scale_elapsed_ns=" + imageScaleElapsedNs
        + ",image_control_attach_elapsed_ns=" + imageControlAttachElapsedNs
        + ",ui_build_elapsed_ns=" + uiBuildElapsedNs
        + ",prefetch_elapsed_ns=" + prefetchElapsedNs
        + ",prefetch_request_count=" + prefetchRequestCount
        + ",prefetch_ready_count=" + prefetchReadyCount
        + ",prefetch_failed_count=" + prefetchFailedCount
        + ",prefetch_not_prefetchable_count=" + prefetchNotPrefetchableCount
        + ",prefetch_backing_live_bytes=" + prefetchBackingLiveBytes
        + ",prefetch_backing_peak_bytes=" + prefetchBackingPeakBytes
        + ",prefetch_targeted_jpeg_decodes=" + prefetchTargetedJpegDecodes
        + ",prefetch_full_jpeg_decodes=" + prefetchFullJpegDecodes
        + ",prefetch_image_materializations=" + prefetchImageMaterializations
        + ",prefetch_native_geometry_materializations=" + prefetchNativeGeometryMaterializations
        + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error);
    System.out.println(summary);
    System.out.flush();
    ImageRasterBenchmarkSupport.writeReport("ImageScrollRealWorkloadBenchmarkApp.log", summary);
    try {
      writeEnvironment();
      writeMemory();
      writeTimeline();
    } catch (Throwable outputFailure) {
      overallPass = false;
      String message = reportFailure(outputFailure);
      System.out.println("benchmark_output_error=" + message);
    }
    exit(overallPass ? 0 : 1);
  }

  @Override
  public void timerTriggered(TimerEvent event) {
    if (event == prefetchTimer && (prefetchComplete || benchmarkReady)) {
      executeBenchmark();
    }
  }

  private void configureMask() {
    ImageOptimizationSettings.resetForTest();
    if (maskArgument == null || maskArgument.length() == 0) {
      return;
    }
    try {
      ImageOptimizationSettings.setMask(Long.parseLong(maskArgument));
    } catch (NumberFormatException error) {
      throw new IllegalArgumentException("image-optimization must be decimal: " + maskArgument);
    }
  }

  private void validateDefaultMaskTransport() throws Exception {
    if (!releaseDefaultScrollProfile && !releaseCandidateScrollProfile) {
      return;
    }
    long expected = releaseDefaultScrollProfile
        ? ImageOptimizationSettings.DEFAULT_EFFECTIVE_MASK : RELEASE_CANDIDATE_MASK;
    String profileLabel = releaseDefaultScrollProfile ? "release default" : "release candidate";
    long effectiveMask = ImageOptimizationSettings.getEffectiveMask();
    nativeDrawMask = Image.nativeOptimizationMaskForDrawForTest();
    nativeDecodeMask = Image.nativeOptimizationMaskForDecodeForTest();
    Image probe = new Image(1, 1);
    observedDrawMask = Image.nativeOptimizationMaskObservedForTest(probe, true) & 0xFFFFFFFFL;
    observedDecodeMask = Image.nativeOptimizationMaskObservedForTest(probe, false) & 0xFFFFFFFFL;
    ImageRasterBenchmarkSupport.require(effectiveMask == expected,
        profileLabel + " Java effective mask must be " + expected);
    ImageRasterBenchmarkSupport.require(nativeDrawMask == expected,
        profileLabel + " native draw mask must be " + expected);
    ImageRasterBenchmarkSupport.require(nativeDecodeMask == expected,
        profileLabel + " native decode mask must be " + expected);
    ImageRasterBenchmarkSupport.require(observedDrawMask == expected,
        profileLabel + " observed draw mask must be " + expected);
    ImageRasterBenchmarkSupport.require(observedDecodeMask == expected,
        profileLabel + " observed decode mask must be " + expected);
  }

  private void configureScrollRasterReuse() {
    renderingReuseProfile = ImageRasterBenchmarkSupport.argument(
        getCommandLine(), "rendering-reuse", "off");
    ImageRasterBenchmarkSupport.require("off".equals(renderingReuseProfile)
        || "on".equals(renderingReuseProfile),
        "rendering-reuse must be off or on");
    RenderingOptimizations.setMask("on".equals(renderingReuseProfile)
        ? RenderingOptimizations.SCROLL_RASTER_REUSE : 0);
    RenderingOptimizations.setDiagnosticsEnabledForTest(
        rasterReuseBenchmarkProfile && accountingEnabled());
    RenderingOptimizations.resetDiagnosticsForTest();
    Control.setBenchmarkScreenTimingForTest(rasterReuseBenchmarkProfile);
    Control.resetBenchmarkScreenTimingForTest();
  }

  private static long parseRunNumber(String value) {
    try {
      long run = Long.parseLong(value);
      if (run < 0) {
        throw new NumberFormatException();
      }
      return run;
    } catch (NumberFormatException error) {
      throw new IllegalArgumentException("run must be a non-negative integer: " + value);
    }
  }

  private String runName() {
    return (rasterReuseBenchmarkProfile ? benchmarkProfile + "-" : "")
        + "mask-" + (maskArgument == null ? "default" : maskArgument)
        + "-prefetch-" + prefetchProfile + "-accounting-" + accountingProfile
        + (rasterReuseBenchmarkProfile ? "-reuse-" + renderingReuseProfile : "")
        + "-run-" + runNumber;
  }

  private boolean prefetchEnabled() {
    return "on".equals(prefetchProfile);
  }

  private boolean accountingEnabled() {
    return "on".equals(accountingProfile);
  }

  private static long diagnosticMetric(int kind) {
    return NativeImageBacking.benchmarkMetricForTest(100 + kind);
  }

  private static long[] diagnosticMetrics(int firstKind, int count) {
    long[] values = new long[count];
    for (int i = 0; i < count; i++) {
      values[i] = diagnosticMetric(firstKind + i);
    }
    return values;
  }

  private void captureTargetMetrics() {
    benchmarkTargetColorType = NativeImageBacking.benchmarkMetricForTest(0);
    benchmarkTargetAlphaType = NativeImageBacking.benchmarkMetricForTest(1);
    benchmarkTargetRowBytes = NativeImageBacking.benchmarkMetricForTest(2);
    benchmarkTargetWidth = NativeImageBacking.benchmarkMetricForTest(3);
    benchmarkTargetHeight = NativeImageBacking.benchmarkMetricForTest(4);
    benchmarkN32ColorType = NativeImageBacking.benchmarkMetricForTest(5);
    benchmarkTargetColorClass = NativeImageBacking.benchmarkMetricForTest(6);
    benchmarkRendererBackend = NativeImageBacking.benchmarkMetricForTest(7);
    benchmarkDrawableWidth = NativeImageBacking.benchmarkMetricForTest(8);
    benchmarkDrawableHeight = NativeImageBacking.benchmarkMetricForTest(9);
    benchmarkRefreshRate = NativeImageBacking.benchmarkMetricForTest(10);
    benchmarkTargetPixelBytes = Graphics.getMainWindowPixelBytes();
    if (benchmarkTargetWidth > 0 && benchmarkTargetHeight > 0) {
      benchmarkSurfaceScaleX = (double) benchmarkTargetWidth / EXPECTED_LOGICAL_WIDTH;
      benchmarkSurfaceScaleY = (double) benchmarkTargetHeight / EXPECTED_LOGICAL_HEIGHT;
    }
    validateTargetMetrics();
  }

  private void validateTargetMetrics() {
    ImageRasterBenchmarkSupport.require(
        benchmarkTargetWidth > 0 && benchmarkTargetHeight > 0
            && benchmarkTargetRowBytes > 0 && benchmarkTargetColorType >= 0
            && benchmarkTargetAlphaType >= 0 && benchmarkN32ColorType >= 0
            && benchmarkTargetColorClass >= 0 && benchmarkTargetColorClass <= 2
            && (benchmarkRendererBackend == 1 || benchmarkRendererBackend == 2),
        "native target metrics are incomplete");
    long minimumRowBytes = benchmarkTargetColorClass == 1
        ? benchmarkTargetWidth * 4
        : benchmarkTargetColorClass == 2 ? benchmarkTargetWidth * 2 : benchmarkTargetWidth;
    ImageRasterBenchmarkSupport.require(benchmarkTargetRowBytes >= minimumRowBytes,
        "native target rowBytes is smaller than the target color format requires");
    long expectedPixelBytes = benchmarkTargetColorClass == 1 ? 4
        : benchmarkTargetColorClass == 2 ? 2 : -1;
    ImageRasterBenchmarkSupport.require(expectedPixelBytes < 0
        ? benchmarkTargetPixelBytes >= 0 : benchmarkTargetPixelBytes == expectedPixelBytes,
        "native target pixel-byte width does not match its color format");
  }

  private static String[] sortedCorpusPaths(String directory) throws Exception {
    String[] entries = File.listFiles(directory, true);
    String[] paths = new String[entries.length];
    int count = 0;
    for (String entry : entries) {
      if (entry.endsWith("/") || !isCorpusImage(entry)) {
        continue;
      }
      paths[count++] = entry;
    }
    paths = Arrays.copyOf(paths, count);
    Arrays.sort(paths);
    return paths;
  }

  private static boolean isCorpusImage(String path) {
    String lower = path.toLowerCase();
    return lower.endsWith(".jpg") || lower.endsWith(".jpeg");
  }

  private void buildUi(String[] imagePaths) throws Exception {
    mainContainer = new ScrollContainer();
    scroll = new ScrollContainer();
    add(mainContainer);
    mainContainer.setBackColor(totalcross.ui.gfx.Color.brighter(totalcross.ui.gfx.Color.BLUE));
    mainContainer.setRect(LEFT, TOP, FILL, FILL);
    scroll.setBackColor(totalcross.ui.gfx.Color.brighter(totalcross.ui.gfx.Color.BLUE));
    mainContainer.add(scroll, LEFT, TOP + 40, FILL, FILL - 60);

    tileWidth = (width - 3) / 3;
    ImageRasterBenchmarkSupport.require(tileWidth > 0, "screen is too narrow for three tiles");
    Container row = null;
    int controlsInRow = 0;
    for (int i = 0; i < imagePaths.length; i++) {
      if (i % COLUMN_COUNT == 0) {
        if (i > 0) {
          ImageRasterBenchmarkSupport.require(controlsInRow == COLUMN_COUNT,
              "previous row did not contain exactly three ImageControls");
        }
        row = rasterReuseBenchmarkProfile ? new MeasuredRow() : new Container();
        row.setBackColor(totalcross.ui.gfx.Color.darker(totalcross.ui.gfx.Color.GREEN));
        scroll.add(row, LEFT, AFTER + 2, width, tileWidth);
        rowCount++;
        controlsInRow = 0;
      }
      long imageLoadStartNs = System.nanoTime();
      Image loadedImage = new Image(imagePaths[i]);
      imageLoadElapsedNs += System.nanoTime() - imageLoadStartNs;
      long imageScaleStartNs = System.nanoTime();
      Image image = loadedImage.getSmoothScaledInstance(tileWidth, tileWidth);
      imageScaleElapsedNs += System.nanoTime() - imageScaleStartNs;
      long imageControlAttachStartNs = System.nanoTime();
      row.add(rasterReuseBenchmarkProfile ? new MeasuredImageControl(image)
          : new ImageControl(image), AFTER + 1, TOP, tileWidth, tileWidth);
      imageControlAttachElapsedNs += System.nanoTime() - imageControlAttachStartNs;
      controlsInRow++;
      imageControlCount++;
    }
    ImageRasterBenchmarkSupport.require(controlsInRow == COLUMN_COUNT,
        "last row does not contain exactly three ImageControls");
    mainContainer.resize();
    scroll.resize();
  }

  private int validMaximum() {
    return Math.max(scroll.sbV.getMinimum(),
        scroll.sbV.getMaximum() - scroll.sbV.getVisibleItems());
  }

  private long[] visibleControlIdentity(int scrollValue) {
    int rowHeight = Math.max(1, tileWidth + 2);
    int firstRow = Math.max(0, scrollValue / rowHeight);
    int visibleRows = Math.max(1, Settings.screenHeight / rowHeight + 2);
    int lastRow = Math.min(rowCount - 1, firstRow + visibleRows);
    int first = Math.min(workloadImageCount - 1, firstRow * COLUMN_COUNT);
    int last = Math.min(workloadImageCount - 1, (lastRow + 1) * COLUMN_COUNT - 1);
    long hash = 0xcbf29ce484222325L;
    for (int index = first; index <= last; index++) {
      hash ^= index;
      hash *= 0x100000001b3L;
      hash ^= benchmarkImagePaths[index].hashCode();
      hash *= 0x100000001b3L;
    }
    return new long[] {first, last, last - first + 1, hash};
  }

  private PassResult runPass(String name, boolean forward, int expectedStart, int maximum) {
    int minimum = scroll.sbV.getMinimum();
    int endpoint = forward ? maximum : minimum;
    ImageRasterBenchmarkSupport.require(scroll.sbV.getValue() == expectedStart,
        name + " did not start at the expected scrollbar endpoint");
    Image.resetImageOperationAccountingForBenchmarkTest(accountingEnabled());
    long startNs = System.nanoTime();
    long[] frameTimesNs = new long[256];
    long[] frameElapsedNs = new long[256];
    int[] framePositions = new int[256];
    long[] frameScrollWorkNs = new long[256];
    long[] framePaintWorkNs = new long[256];
    long[] frameWorkTimeNs = new long[256];
    long[] frameJpegDecodeCounts = new long[256];
    long[] frameJpegDecodeNs = new long[256];
    long[] frameJpegFullCounts = new long[256];
    long[] frameJpegFullNs = new long[256];
    long[] frameJpegHalfCounts = new long[256];
    long[] frameJpegHalfNs = new long[256];
    long[] frameJpegQuarterCounts = new long[256];
    long[] frameJpegQuarterNs = new long[256];
    long[] frameJpegEighthCounts = new long[256];
    long[] frameJpegEighthNs = new long[256];
    long[] frameJpegOtherCounts = new long[256];
    long[] frameJpegOtherNs = new long[256];
    FrameMetrics[] frameMetrics = new FrameMetrics[256];
    FrameMetrics[] frameScrollMetrics = new FrameMetrics[256];
    FrameMetrics[] framePaintMetrics = new FrameMetrics[256];
    int frames = 0;
    long previousFrameStartNs = startNs;
    while (true) {
      long frameStartNs = System.nanoTime();
      long elapsedNs = frameStartNs - startNs;
      long nextFrameNs = (long) frames * FRAME_INTERVAL_NS;
      if (frames > 0 && elapsedNs < nextFrameNs) {
        long remainingNs = nextFrameNs - elapsedNs;
        long sleepMs = Math.max(1L,
            (remainingNs + NANOS_PER_MILLISECOND - 1) / NANOS_PER_MILLISECOND);
        Vm.sleep((int) Math.min(4L, sleepMs));
        continue;
      }
      int target;
      if (frames == 0) {
        target = expectedStart;
      } else if (elapsedNs >= scrollDurationNs) {
        target = endpoint;
      } else {
        int progress = (int) ((long) (maximum - minimum) * elapsedNs / scrollDurationNs);
        target = forward ? minimum + progress : maximum - progress;
      }
      int before = scroll.sbV.getValue();
      long workStartNs = System.nanoTime();
      long scrollWorkNs = 0;
      NativeImageBacking.resetWritePixelsFrameMetricsForTest();
      FrameMetrics scrollBefore = FrameMetrics.capture();
      if (target != before) {
        long scrollWorkStartNs = System.nanoTime();
        ImageRasterBenchmarkSupport.require(scroll.scrollContent(0, target - before, true),
            name + " stopped before reaching time-based target");
        scrollWorkNs = Math.max(0, System.nanoTime() - scrollWorkStartNs);
      }
      FrameMetrics scrollAfter = FrameMetrics.capture();
      FrameMetrics scrollMetrics = FrameMetrics.delta(scrollBefore, scrollAfter);
      NativeImageBacking.resetWritePixelsFrameMetricsForTest();
      FrameMetrics paintBefore = FrameMetrics.capture();
      long paintWorkStartNs = System.nanoTime();
      scroll.repaintNow();
      long paintWorkNs = Math.max(0, System.nanoTime() - paintWorkStartNs);
      FrameMetrics paintAfter = FrameMetrics.capture();
      FrameMetrics paintMetrics = FrameMetrics.delta(paintBefore, paintAfter);
      FrameMetrics frameAttribution = FrameMetrics.merge(scrollMetrics, paintMetrics);
      long workTimeNs = Math.max(0, System.nanoTime() - workStartNs);
      long frameTimeNs = frames == 0
          ? paintWorkNs : Math.max(0, frameStartNs - previousFrameStartNs);
      if (frames == frameTimesNs.length) {
        int newLength = frameTimesNs.length * 2;
        frameTimesNs = Arrays.copyOf(frameTimesNs, newLength);
        frameElapsedNs = Arrays.copyOf(frameElapsedNs, newLength);
        framePositions = Arrays.copyOf(framePositions, newLength);
        frameScrollWorkNs = Arrays.copyOf(frameScrollWorkNs, newLength);
        framePaintWorkNs = Arrays.copyOf(framePaintWorkNs, newLength);
        frameWorkTimeNs = Arrays.copyOf(frameWorkTimeNs, newLength);
        frameJpegDecodeCounts = Arrays.copyOf(frameJpegDecodeCounts, newLength);
        frameJpegDecodeNs = Arrays.copyOf(frameJpegDecodeNs, newLength);
        frameJpegFullCounts = Arrays.copyOf(frameJpegFullCounts, newLength);
        frameJpegFullNs = Arrays.copyOf(frameJpegFullNs, newLength);
        frameJpegHalfCounts = Arrays.copyOf(frameJpegHalfCounts, newLength);
        frameJpegHalfNs = Arrays.copyOf(frameJpegHalfNs, newLength);
        frameJpegQuarterCounts = Arrays.copyOf(frameJpegQuarterCounts, newLength);
        frameJpegQuarterNs = Arrays.copyOf(frameJpegQuarterNs, newLength);
        frameJpegEighthCounts = Arrays.copyOf(frameJpegEighthCounts, newLength);
        frameJpegEighthNs = Arrays.copyOf(frameJpegEighthNs, newLength);
        frameJpegOtherCounts = Arrays.copyOf(frameJpegOtherCounts, newLength);
        frameJpegOtherNs = Arrays.copyOf(frameJpegOtherNs, newLength);
        frameMetrics = Arrays.copyOf(frameMetrics, newLength);
        frameScrollMetrics = Arrays.copyOf(frameScrollMetrics, newLength);
        framePaintMetrics = Arrays.copyOf(framePaintMetrics, newLength);
      }
      frameTimesNs[frames] = frameTimeNs;
      frameElapsedNs[frames] = elapsedNs;
      framePositions[frames] = scroll.sbV.getValue();
      frameScrollWorkNs[frames] = scrollWorkNs;
      framePaintWorkNs[frames] = paintWorkNs;
      frameWorkTimeNs[frames] = workTimeNs;
      frameMetrics[frames] = frameAttribution;
      frameScrollMetrics[frames] = scrollMetrics;
      framePaintMetrics[frames] = paintMetrics;
      frameJpegDecodeCounts[frames] = frameAttribution.jpegDecodeCount;
      frameJpegDecodeNs[frames] = frameAttribution.jpegDecodeNs;
      frameJpegFullCounts[frames] = frameAttribution.jpegFullCount;
      frameJpegFullNs[frames] = frameAttribution.jpegFullNs;
      frameJpegHalfCounts[frames] = frameAttribution.jpegHalfCount;
      frameJpegHalfNs[frames] = frameAttribution.jpegHalfNs;
      frameJpegQuarterCounts[frames] = frameAttribution.jpegQuarterCount;
      frameJpegQuarterNs[frames] = frameAttribution.jpegQuarterNs;
      frameJpegEighthCounts[frames] = frameAttribution.jpegEighthCount;
      frameJpegEighthNs[frames] = frameAttribution.jpegEighthNs;
      frameJpegOtherCounts[frames] = frameAttribution.jpegOtherCount;
      frameJpegOtherNs[frames] = frameAttribution.jpegOtherNs;
      frames++;
      previousFrameStartNs = frameStartNs;
      if (scroll.sbV.getValue() == endpoint && elapsedNs >= scrollDurationNs) {
        break;
      }
    }
    ImageRasterBenchmarkSupport.require(scroll.sbV.getValue() == endpoint,
        name + " did not reach the full scrollbar extent");
    ImageRasterBenchmarkSupport.require(frames > 1,
        name + " did not traverse multiple frames");
    long elapsedNs = System.nanoTime() - startNs;
    Counters counters = Counters.capture();
    long[] actualFrameTimesNs = Arrays.copyOf(frameTimesNs, frames);
    long[] actualFrameElapsedNs = Arrays.copyOf(frameElapsedNs, frames);
    int[] actualFramePositions = Arrays.copyOf(framePositions, frames);
    long[] actualFrameScrollWorkNs = Arrays.copyOf(frameScrollWorkNs, frames);
    long[] actualFramePaintWorkNs = Arrays.copyOf(framePaintWorkNs, frames);
    long[] actualFrameWorkTimeNs = Arrays.copyOf(frameWorkTimeNs, frames);
    long[] actualFrameJpegDecodeCounts = Arrays.copyOf(frameJpegDecodeCounts, frames);
    long[] actualFrameJpegDecodeNs = Arrays.copyOf(frameJpegDecodeNs, frames);
    long[] actualFrameJpegFullCounts = Arrays.copyOf(frameJpegFullCounts, frames);
    long[] actualFrameJpegFullNs = Arrays.copyOf(frameJpegFullNs, frames);
    long[] actualFrameJpegHalfCounts = Arrays.copyOf(frameJpegHalfCounts, frames);
    long[] actualFrameJpegHalfNs = Arrays.copyOf(frameJpegHalfNs, frames);
    long[] actualFrameJpegQuarterCounts = Arrays.copyOf(frameJpegQuarterCounts, frames);
    long[] actualFrameJpegQuarterNs = Arrays.copyOf(frameJpegQuarterNs, frames);
    long[] actualFrameJpegEighthCounts = Arrays.copyOf(frameJpegEighthCounts, frames);
    long[] actualFrameJpegEighthNs = Arrays.copyOf(frameJpegEighthNs, frames);
    long[] actualFrameJpegOtherCounts = Arrays.copyOf(frameJpegOtherCounts, frames);
    long[] actualFrameJpegOtherNs = Arrays.copyOf(frameJpegOtherNs, frames);
    long[] sortedFrameTimesNs = Arrays.copyOf(actualFrameTimesNs, frames);
    Arrays.sort(sortedFrameTimesNs);
    long[] sortedWorkTimeNs = Arrays.copyOf(actualFrameWorkTimeNs, frames);
    Arrays.sort(sortedWorkTimeNs);
    long[] sortedPaintWorkNs = Arrays.copyOf(actualFramePaintWorkNs, frames);
    Arrays.sort(sortedPaintWorkNs);
    for (int i = 0; i < frames; i++) {
      ImageRasterBenchmarkSupport.require(actualFrameTimesNs[i] >= 0,
          name + " has a negative frame time");
      ImageRasterBenchmarkSupport.require(actualFrameElapsedNs[i] >= 0,
          name + " has a negative frame elapsed time");
    }
    validateActiveWorkDiagnostics(name, actualFrameScrollWorkNs, actualFramePaintWorkNs,
        actualFrameWorkTimeNs);
    validateFrameDiagnostics(counters, actualFrameJpegDecodeCounts, actualFrameJpegDecodeNs,
        actualFrameJpegFullCounts, actualFrameJpegFullNs, actualFrameJpegHalfCounts,
        actualFrameJpegHalfNs, actualFrameJpegQuarterCounts, actualFrameJpegQuarterNs,
        actualFrameJpegEighthCounts, actualFrameJpegEighthNs, actualFrameJpegOtherCounts,
        actualFrameJpegOtherNs);
    return new PassResult(name, forward, minimum, endpoint, maximum, elapsedNs, frames,
        actualFrameTimesNs, actualFrameElapsedNs, actualFramePositions, actualFrameScrollWorkNs,
        actualFramePaintWorkNs, actualFrameWorkTimeNs, sortedFrameTimesNs, sortedWorkTimeNs,
        sortedPaintWorkNs,
        actualFrameJpegDecodeCounts, actualFrameJpegDecodeNs, actualFrameJpegFullCounts,
        actualFrameJpegFullNs, actualFrameJpegHalfCounts, actualFrameJpegHalfNs,
        actualFrameJpegQuarterCounts, actualFrameJpegQuarterNs, actualFrameJpegEighthCounts,
        actualFrameJpegEighthNs, actualFrameJpegOtherCounts, actualFrameJpegOtherNs,
        Arrays.copyOf(frameMetrics, frames), Arrays.copyOf(frameScrollMetrics, frames),
        Arrays.copyOf(framePaintMetrics, frames), counters);
  }

  private void executeScrollRasterReuseProfile() throws Exception {
    int minimum = scroll.sbV.getMinimum();
    int maximum = validMaximum();
    int visibleExtent = Math.max(1, scroll.getRect().height);
    int[] waypoints = {
        minimum,
        clampScrollValue(minimum + 7L * visibleExtent, minimum, maximum),
        clampScrollValue(minimum + 4L * visibleExtent, minimum, maximum),
        clampScrollValue(minimum + 5L * visibleExtent, minimum, maximum),
        clampScrollValue(minimum + (9L * visibleExtent) / 2L, minimum, maximum)
    };
    StringBuilder frames = new StringBuilder(16384);
    StringBuilder waypointRows = new StringBuilder(4096);
    frames.append("pass,frame_index,segment_index,elapsed_ns,segment_elapsed_ns,")
        .append("target_scroll,scroll_value,requested_delta,actual_delta,hit,fallback_reason,")
        .append("decision_ns,move_ns,dirty_paint_ns,screen_update_ns,scroll_work_ns,")
        .append("paint_work_ns,work_time_ns,row_paints,image_paints,movement,measured,needs_paint_after\n");
    waypointRows.append("pass,waypoint_index,target_scroll,actual_scroll,hash,top_hash,bottom_hash,elapsed_ns,hit,")
        .append("attempts,hits,fallbacks,post_move_recoveries\n");

    for (int passIndex = 0; passIndex < benchmarkPassCount; passIndex++) {
      resetToScrollStart(minimum);
      normalRepaint();
      RenderingOptimizations.resetDiagnosticsForTest();
      resetMeasuredPaintCounters();
      waypointRows.append(passIndex == 0 ? "cold" : "warm").append(",0,")
          .append(minimum).append(',').append(scroll.sbV.getValue()).append(',')
          .append(ImageRasterBenchmarkSupport.hashString(scroll.rasterReuseViewportHashForTest())).append(',')
          .append(ImageRasterBenchmarkSupport.hashString(scroll.rasterReuseViewportHashForTest(0, 455)))
          .append(',').append(ImageRasterBenchmarkSupport.hashString(scroll.rasterReuseViewportHashForTest(455, 455)))
          .append(",0,0,0,0,0,0\n");
      PocPassResult result = runScrollRasterReusePass(passIndex == 0 ? "cold" : "warm",
          waypoints, frames, waypointRows);
      validateScrollRasterReusePass(result);
      printScrollRasterReusePass(result, passIndex + 1);
    }
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "scroll_raster_reuse_frames.csv"),
        frames.toString());
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "scroll_raster_reuse_waypoints.csv"),
        waypointRows.toString());
    RenderingOptimizations.resetDiagnosticsForTest();
    Control.resetBenchmarkScreenTimingForTest();
  }

  private PocPassResult runScrollRasterReusePass(String name, int[] waypoints,
      StringBuilder frames, StringBuilder waypointRows) {
    long passStartNs = System.nanoTime();
    int frameIndex = 0;
    int movementFrameCount = 0;
    long attempts = 0;
    long hits = 0;
    long fallbacks = 0;
    long viewportPixels = 0;
    long reusedPixels = 0;
    long dirtyPixels = 0;
    long movedBytes = 0;
    long screenUpdateNs = 0;
    long postMoveRecoveries = 0;
    for (int segmentIndex = 0; segmentIndex < POC_SEGMENT_COUNT; segmentIndex++) {
      int segmentStart = waypoints[segmentIndex];
      int segmentEnd = waypoints[segmentIndex + 1];
      long segmentStartNs = System.nanoTime();
      long segmentDurationNs = POC_SEGMENT_DURATIONS_MS[segmentIndex] * NANOS_PER_MILLISECOND;
      long nextFrameNs = 0;
      while (true) {
        long frameStartNs = System.nanoTime();
        long segmentElapsedNs = frameStartNs - segmentStartNs;
        if (frameIndex > 0 && segmentElapsedNs < nextFrameNs) {
          long remainingNs = nextFrameNs - segmentElapsedNs;
          Vm.sleep((int) Math.min(4L, Math.max(1L,
              (remainingNs + NANOS_PER_MILLISECOND - 1) / NANOS_PER_MILLISECOND)));
          continue;
        }
        long boundedElapsedNs = Math.min(segmentElapsedNs, segmentDurationNs);
        int target = boundedElapsedNs >= segmentDurationNs ? segmentEnd
            : interpolateScroll(segmentStart, segmentEnd, boundedElapsedNs, segmentDurationNs);
        int before = scroll.sbV.getValue();
        long decisionBefore = RenderingOptimizations.diagnosticMetricForTest(8);
        long moveBefore = RenderingOptimizations.diagnosticMetricForTest(9);
        long dirtyBefore = RenderingOptimizations.diagnosticMetricForTest(10);
        resetMeasuredPaintCounters();
        Control.resetBenchmarkScreenTimingForTest();
        long scrollWorkNs = 0;
        long paintWorkNs = 0;
        long workTimeNs = 0;
        long actualDelta = 0;
        boolean requestedMovement = target != before;
        boolean hit = false;
        long[] metrics = new long[] {0, 0, -1, 0, 0, 0, 0};
        if (requestedMovement) {
          long workStartNs = System.nanoTime();
          long scrollStartNs = System.nanoTime();
          ImageRasterBenchmarkSupport.require(scroll.scrollContent(0, target - before, true),
              name + " stopped before reaching target");
          scrollWorkNs = System.nanoTime() - scrollStartNs;
          actualDelta = scroll.sbV.getValue() - before;
          metrics = scroll.rasterReuseLastMetricsForTest();
          hit = actualDelta != 0 && metrics[0] == 1;
          if (actualDelta != 0) {
            movementFrameCount++;
            if (RenderingOptimizations.getMask() != 0) {
              attempts++;
              if (hit) {
                hits++;
                viewportPixels += metrics[3];
                reusedPixels += metrics[4];
                dirtyPixels += metrics[5];
                movedBytes += metrics[6];
              } else {
                fallbacks++;
              }
            }
          }
          if (metrics[1] != 0) {
            postMoveRecoveries++;
          }
          if (!hit) {
            paintWorkNs = normalRepaint();
          }
          workTimeNs = System.nanoTime() - workStartNs;
        }
        long frameScreenUpdateNs = Control.consumeBenchmarkScreenUpdateNsForTest();
        screenUpdateNs += frameScreenUpdateNs;
        boolean measured = actualDelta != 0;
        boolean needsPaintAfter = Window.needsPaint;
        if (measured) {
          ImageRasterBenchmarkSupport.require(!needsPaintAfter,
              name + " left repaint pending after scroll frame");
        }
        String fallbackReason = metrics[2] >= 0
            ? RenderingOptimizations.fallbackReasonNameForTest((int) metrics[2]) : "";
        frames.append(name).append(',').append(frameIndex).append(',').append(segmentIndex).append(',')
            .append(frameStartNs - passStartNs).append(',').append(segmentElapsedNs).append(',')
            .append(target).append(',').append(scroll.sbV.getValue()).append(',')
            .append(target - before).append(',').append(actualDelta).append(',').append(hit ? 1 : 0)
            .append(',').append(fallbackReason).append(',')
            .append(RenderingOptimizations.diagnosticMetricForTest(8) - decisionBefore).append(',')
            .append(RenderingOptimizations.diagnosticMetricForTest(9) - moveBefore).append(',')
            .append(RenderingOptimizations.diagnosticMetricForTest(10) - dirtyBefore).append(',')
            .append(frameScreenUpdateNs).append(',')
            .append(scrollWorkNs).append(',').append(paintWorkNs).append(',').append(workTimeNs)
            .append(',').append(measuredRowPaints).append(',').append(measuredImagePaints)
            .append(',').append(requestedMovement ? 1 : 0).append(',').append(measured ? 1 : 0)
            .append(',').append(needsPaintAfter ? 1 : 0).append('\n');
        frameIndex++;
        if (boundedElapsedNs >= segmentDurationNs && scroll.sbV.getValue() == segmentEnd) {
          long hash = scroll.rasterReuseViewportHashForTest();
          waypointRows.append(name).append(',').append(segmentIndex + 1).append(',')
              .append(segmentEnd).append(',').append(scroll.sbV.getValue()).append(',')
              .append(ImageRasterBenchmarkSupport.hashString(hash)).append(',')
              .append(ImageRasterBenchmarkSupport.hashString(scroll.rasterReuseViewportHashForTest(0, 455)))
              .append(',').append(ImageRasterBenchmarkSupport.hashString(
                  scroll.rasterReuseViewportHashForTest(455, 455))).append(',')
              .append(System.nanoTime() - passStartNs).append(',')
              .append(hit ? 1 : 0).append(',').append(attempts).append(',').append(hits).append(',')
              .append(fallbacks).append(',').append(postMoveRecoveries).append('\n');
          break;
        }
        nextFrameNs += POC_FRAME_INTERVAL_NS;
      }
    }
    return new PocPassResult(name, System.nanoTime() - passStartNs, frameIndex,
        movementFrameCount, attempts, hits, fallbacks, viewportPixels, reusedPixels,
        dirtyPixels, movedBytes, screenUpdateNs, postMoveRecoveries);
  }

  private void validateScrollRasterReusePass(PocPassResult result) {
    ImageRasterBenchmarkSupport.require(result.attempts == result.hits + result.fallbacks,
        result.name + " raster reuse accounting mismatch");
    ImageRasterBenchmarkSupport.require(result.postMoveRecoveries == 0,
        result.name + " had post-move recovery");
    if (RenderingOptimizations.getMask() != 0) {
      ImageRasterBenchmarkSupport.require(result.hits > 0,
          result.name + " did not record raster reuse hits: attempts=" + result.attempts
              + ",fallbacks=" + result.fallbacks + ",reasons=" + rasterReuseFallbackDetails()
              + ",geometry=" + scroll.rasterReuseViewportGeometryForTest());
    } else {
      ImageRasterBenchmarkSupport.require(result.hits == 0,
          result.name + " recorded hits with raster reuse disabled");
    }
    if (accountingEnabled()) {
      ImageRasterBenchmarkSupport.require(
          result.attempts == RenderingOptimizations.diagnosticMetricForTest(0)
              && result.hits == RenderingOptimizations.diagnosticMetricForTest(1)
              && result.fallbacks == RenderingOptimizations.diagnosticMetricForTest(2),
          result.name + " local reuse outcome disagrees with accounting");
    } else {
      for (int kind = 0; kind < 20; kind++) {
        ImageRasterBenchmarkSupport.require(RenderingOptimizations.diagnosticMetricForTest(kind) == 0,
            result.name + " unexpectedly enabled rendering diagnostics");
      }
      ImageRasterBenchmarkSupport.require(RenderingOptimizations.diagnosticMetricForTest(20) == -1,
          result.name + " unexpectedly recorded a rendering fallback reason");
      for (int reason = 0; reason < 12; reason++) {
        ImageRasterBenchmarkSupport.require(
            RenderingOptimizations.diagnosticMetricForTest(100 + reason) == 0,
            result.name + " unexpectedly recorded a rendering fallback diagnostic");
      }
    }
    ImageRasterBenchmarkSupport.require(
        result.movedBytes == result.reusedPixels * benchmarkTargetPixelBytes,
        result.name + " moved-byte accounting mismatch");
  }

  private static String rasterReuseFallbackDetails() {
    StringBuilder details = new StringBuilder();
    for (int reason = 0; reason < 12; reason++) {
      long count = RenderingOptimizations.diagnosticMetricForTest(100 + reason);
      if (count != 0) {
        if (details.length() != 0) {
          details.append(';');
        }
        details.append(RenderingOptimizations.fallbackReasonNameForTest(reason))
            .append('=').append(count);
      }
    }
    return details.toString();
  }

  private void printScrollRasterReusePass(PocPassResult result, int passIndex) {
    System.out.println("fixture=ImageScrollRealWorkloadBenchmarkApp,record=scroll-raster-reuse"
        + ",profile=" + benchmarkProfile + ",pass=" + result.name
        + ",pass_index=" + passIndex + ",rendering_reuse="
        + (RenderingOptimizations.getMask() == 0 ? "off" : "on")
        + ",image_count=" + workloadImageCount + ",frame_count=" + result.frameCount
        + ",movement_frames=" + result.movementFrameCount
        + ",elapsed_ns=" + result.elapsedNs + ",attempts=" + result.attempts
        + ",hits=" + result.hits + ",fallbacks=" + result.fallbacks
        + ",viewport_pixels=" + result.viewportPixels + ",reused_pixels="
        + result.reusedPixels + ",dirty_pixels=" + result.dirtyPixels
        + ",moved_bytes=" + result.movedBytes + ",screen_update_ns="
        + result.screenUpdateNs + ",post_move_recoveries=" + result.postMoveRecoveries
        + ",segment_durations_ms=500,250,200,150");
    System.out.flush();
  }

  private void resetToScrollStart(int minimum) {
    long mask = RenderingOptimizations.getMask();
    RenderingOptimizations.setMask(0);
    int delta = minimum - scroll.sbV.getValue();
    if (delta != 0) {
      ImageRasterBenchmarkSupport.require(scroll.scrollContent(0, delta, true),
          "could not reset scroll raster profile position");
    }
    normalRepaint();
    RenderingOptimizations.setMask(mask);
  }

  private long normalRepaint() {
    Window.needsPaint = true;
    long startNs = System.nanoTime();
    Window.repaintActiveWindows();
    return System.nanoTime() - startNs;
  }

  private void resetMeasuredPaintCounters() {
    measuredRowPaints = 0;
    measuredImagePaints = 0;
  }

  private static int interpolateScroll(int start, int end, long elapsedNs, long durationNs) {
    long distance = (long) end - start;
    return (int) (start + distance * elapsedNs / durationNs);
  }

  private static int clampScrollValue(long value, int minimum, int maximum) {
    return (int) Math.max(minimum, Math.min(maximum, value));
  }

  private static void validateJpegDiagnostics(Counters counters, String phase) {
    ImageRasterBenchmarkSupport.require(counters != null, phase + " counters are missing");
    if (!counters.accountingAvailable) {
      return;
    }
    requireNonNegative(counters.jpegDecodeCount, phase + " jpeg decode count");
    requireNonNegative(counters.jpegDecodeNs, phase + " jpeg decode ns");
    requireNonNegative(counters.jpegFullCount, phase + " full jpeg count");
    requireNonNegative(counters.jpegFullNs, phase + " full jpeg ns");
    requireNonNegative(counters.jpegHalfCount, phase + " half jpeg count");
    requireNonNegative(counters.jpegHalfNs, phase + " half jpeg ns");
    requireNonNegative(counters.jpegQuarterCount, phase + " quarter jpeg count");
    requireNonNegative(counters.jpegQuarterNs, phase + " quarter jpeg ns");
    requireNonNegative(counters.jpegEighthCount, phase + " eighth jpeg count");
    requireNonNegative(counters.jpegEighthNs, phase + " eighth jpeg ns");
    requireNonNegative(counters.jpegOtherCount, phase + " other jpeg count");
    requireNonNegative(counters.jpegOtherNs, phase + " other jpeg ns");
    requireNonNegative(counters.jpegRequestedFullCount, phase + " requested full jpeg count");
    requireNonNegative(counters.jpegRequestedTargetCount, phase + " requested target count");
    requireNonNegative(counters.jpegRequestedExplicitRatioCount,
        phase + " requested explicit ratio count");
    requireNonNegative(counters.jpegRequestedBestFitCount, phase + " requested best-fit count");
    requireNonNegative(counters.jpegDecodeFailureCount, phase + " jpeg decode failures");
    ImageRasterBenchmarkSupport.require(
        counters.jpegDecodeCount == counters.jpegFullCount + counters.jpegHalfCount
            + counters.jpegQuarterCount + counters.jpegEighthCount + counters.jpegOtherCount,
        phase + " jpeg denominator count mismatch");
    ImageRasterBenchmarkSupport.require(
        counters.jpegDecodeNs == counters.jpegFullNs + counters.jpegHalfNs
            + counters.jpegQuarterNs + counters.jpegEighthNs + counters.jpegOtherNs,
        phase + " jpeg denominator ns mismatch");
    ImageRasterBenchmarkSupport.require(
        counters.jpegDecodeCount == counters.jpegRequestedFullCount
            + counters.jpegRequestedTargetCount + counters.jpegRequestedExplicitRatioCount
            + counters.jpegRequestedBestFitCount,
        phase + " jpeg requested mode count mismatch");
    ImageRasterBenchmarkSupport.require(
        ImageOptimizationSettings.getMask() == ImageOptimizationSettings.getEffectiveMask(),
        "requested mask != effective mask");
  }

  private static void validateFrameDiagnostics(Counters counters, long[] decodeCounts,
      long[] decodeNs, long[] fullCounts, long[] fullNs, long[] halfCounts, long[] halfNs,
      long[] quarterCounts, long[] quarterNs, long[] eighthCounts, long[] eighthNs,
      long[] otherCounts, long[] otherNs) {
    validateJpegDiagnostics(counters, "scroll");
    validateWritePixelsDiagnostics(counters);
    long decodeCount = 0;
    long decodeNsTotal = 0;
    long fullCount = 0;
    long fullNsTotal = 0;
    long halfCount = 0;
    long halfNsTotal = 0;
    long quarterCount = 0;
    long quarterNsTotal = 0;
    long eighthCount = 0;
    long eighthNsTotal = 0;
    long otherCount = 0;
    long otherNsTotal = 0;
    for (int i = 0; i < decodeCounts.length; i++) {
      requireNonNegative(decodeCounts[i], "frame jpeg decode count");
      requireNonNegative(decodeNs[i], "frame jpeg decode ns");
      requireNonNegative(fullCounts[i], "frame full jpeg count");
      requireNonNegative(fullNs[i], "frame full jpeg ns");
      requireNonNegative(halfCounts[i], "frame half jpeg count");
      requireNonNegative(halfNs[i], "frame half jpeg ns");
      requireNonNegative(quarterCounts[i], "frame quarter jpeg count");
      requireNonNegative(quarterNs[i], "frame quarter jpeg ns");
      requireNonNegative(eighthCounts[i], "frame eighth jpeg count");
      requireNonNegative(eighthNs[i], "frame eighth jpeg ns");
      requireNonNegative(otherCounts[i], "frame other jpeg count");
      requireNonNegative(otherNs[i], "frame other jpeg ns");
      decodeCount += decodeCounts[i];
      decodeNsTotal += decodeNs[i];
      fullCount += fullCounts[i];
      fullNsTotal += fullNs[i];
      halfCount += halfCounts[i];
      halfNsTotal += halfNs[i];
      quarterCount += quarterCounts[i];
      quarterNsTotal += quarterNs[i];
      eighthCount += eighthCounts[i];
      eighthNsTotal += eighthNs[i];
      otherCount += otherCounts[i];
      otherNsTotal += otherNs[i];
    }
    ImageRasterBenchmarkSupport.require(decodeCount == counters.jpegDecodeCount,
        "frame jpeg decode counts do not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(decodeNsTotal == counters.jpegDecodeNs,
        "frame jpeg decode ns does not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(fullCount == counters.jpegFullCount,
        "frame full jpeg counts do not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(fullNsTotal == counters.jpegFullNs,
        "frame full jpeg ns does not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(halfCount == counters.jpegHalfCount,
        "frame half jpeg counts do not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(halfNsTotal == counters.jpegHalfNs,
        "frame half jpeg ns does not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(quarterCount == counters.jpegQuarterCount,
        "frame quarter jpeg counts do not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(quarterNsTotal == counters.jpegQuarterNs,
        "frame quarter jpeg ns does not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(eighthCount == counters.jpegEighthCount,
        "frame eighth jpeg counts do not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(eighthNsTotal == counters.jpegEighthNs,
        "frame eighth jpeg ns does not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(otherCount == counters.jpegOtherCount,
        "frame other jpeg counts do not match scroll aggregate");
    ImageRasterBenchmarkSupport.require(otherNsTotal == counters.jpegOtherNs,
        "frame other jpeg ns does not match scroll aggregate");
  }

  private static void validateWritePixelsDiagnostics(Counters counters) {
    if (!counters.accountingAvailable) {
      return;
    }
    requireNonNegative(counters.writePixelsAttempts, "writePixels attempts");
    requireNonNegative(counters.writePixelsHits, "writePixels hits");
    requireNonNegative(counters.writePixelsFallbacks, "writePixels fallbacks");
    requireNonNegative(counters.writePixelsRegularAttempts, "regular writePixels attempts");
    requireNonNegative(counters.writePixelsRegularHits, "regular writePixels hits");
    requireNonNegative(counters.writePixelsRegularFallbacks, "regular writePixels fallbacks");
    requireNonNegative(counters.writePixelsRegularCopiedBytes,
        "regular writePixels copied bytes");
    requireNonNegative(counters.writePixelsRegularClippedHits,
        "regular writePixels clipped hits");
    requireNonNegative(counters.writePixelsDeviceOneToOneCandidates,
        "writePixels one-to-one candidates");
    requireNonNegative(counters.writePixelsDeviceOneToOneKnownOpaqueCandidates,
        "writePixels known-opaque candidates");
    ImageRasterBenchmarkSupport.require(
        counters.writePixelsAttempts == counters.writePixelsHits + counters.writePixelsFallbacks,
        "writePixels attempts do not equal hits plus fallbacks");
    ImageRasterBenchmarkSupport.require(
        counters.writePixelsRegularAttempts
            == counters.writePixelsRegularHits + counters.writePixelsRegularFallbacks,
        "regular writePixels attempts do not equal hits plus fallbacks");
    ImageRasterBenchmarkSupport.require(
        counters.writePixelsRegularClippedHits <= counters.writePixelsRegularHits,
        "regular writePixels clipped hits exceed hits");
    ImageRasterBenchmarkSupport.require(
        counters.writePixelsDeviceOneToOneCandidates <= counters.writePixelsAttempts,
        "writePixels candidates exceed attempts");
    ImageRasterBenchmarkSupport.require(
        counters.writePixelsDeviceOneToOneKnownOpaqueCandidates
            <= counters.writePixelsDeviceOneToOneCandidates,
        "writePixels known-opaque candidates exceed candidates");
  }

  private static void validateActiveWorkDiagnostics(String phase, long[] scrollWorkNs,
      long[] paintWorkNs, long[] workTimeNs) {
    ImageRasterBenchmarkSupport.require(scrollWorkNs.length == paintWorkNs.length
        && scrollWorkNs.length == workTimeNs.length,
        phase + " active work arrays have different lengths");
    for (int i = 0; i < workTimeNs.length; i++) {
      requireNonNegative(scrollWorkNs[i], phase + " frame scroll work ns");
      requireNonNegative(paintWorkNs[i], phase + " frame paint work ns");
      requireNonNegative(workTimeNs[i], phase + " frame work ns");
      ImageRasterBenchmarkSupport.require(workTimeNs[i] >= scrollWorkNs[i],
          phase + " frame work ns is less than scroll work ns");
      ImageRasterBenchmarkSupport.require(workTimeNs[i] >= paintWorkNs[i],
          phase + " frame work ns is less than paint work ns");
    }
  }

  private static void requireNonNegative(long value, String description) {
    ImageRasterBenchmarkSupport.require(value >= 0, "negative " + description);
  }

  private void printPass(PassResult result, int passIndex, int passCount) {
    System.out.println("fixture=ImageScrollRealWorkloadBenchmarkApp,record=pass"
        + ",resolution=" + width + "x" + height
        + ",prefetch_profile=" + prefetchProfile
        + ",accounting=" + accountingProfile
        + ",requested_mask=" + (maskArgument == null ? "default" : maskArgument)
        + ",effective_mask=" + ImageOptimizationSettings.getEffectiveMask()
        + ",run=" + runNumber
        + ",pass_index=" + passIndex + ",pass_count=" + passCount
        + ",profile=" + benchmarkProfile
        + ",image_count=" + workloadImageCount + ",rows=" + rowCount
        + ",image_controls=" + imageControlCount + ",tile_logical=" + tileWidth
        + ",corpus_file_enumeration_elapsed_ns=" + corpusFileEnumerationElapsedNs
        + ",image_load_elapsed_ns=" + imageLoadElapsedNs
        + ",image_scale_elapsed_ns=" + imageScaleElapsedNs
        + ",image_control_attach_elapsed_ns=" + imageControlAttachElapsedNs
        + ",ui_build_elapsed_ns=" + uiBuildElapsedNs
        + ",prefetch_elapsed_ns=" + prefetchElapsedNs
        + ",prefetch_request_count=" + prefetchRequestCount
        + ",prefetch_ready_count=" + prefetchReadyCount
        + ",prefetch_failed_count=" + prefetchFailedCount
        + ",prefetch_not_prefetchable_count=" + prefetchNotPrefetchableCount
        + ",prefetch_backing_live_bytes=" + prefetchBackingLiveBytes
        + ",prefetch_backing_peak_bytes=" + prefetchBackingPeakBytes
        + ",prefetch_targeted_jpeg_decodes=" + prefetchTargetedJpegDecodes
        + ",prefetch_full_jpeg_decodes=" + prefetchFullJpegDecodes
        + ",prefetch_image_materializations=" + prefetchImageMaterializations
        + ",prefetch_native_geometry_materializations=" + prefetchNativeGeometryMaterializations
        + ",pass=" + result.name + ",direction=" + (result.forward ? "top-to-bottom" : "bottom-to-top")
        + ",scroll_start=" + result.start + ",scroll_end=" + result.end
        + ",scroll_max=" + result.maximum + ",scroll_distance=" + (result.maximum - result.minimum)
        + ",elapsed_total_ns=" + result.elapsedNs + ",frames=" + result.frames
        + ",frame_time_min_ns=" + result.percentileNs(0)
        + ",frame_time_p90_ns=" + result.percentileNs(90)
        + ",frame_time_p50_ns=" + result.percentileNs(50)
        + ",frame_time_p95_ns=" + result.percentileNs(95)
        + ",frame_time_p99_ns=" + result.percentileNs(99)
        + ",frame_time_max_ns=" + result.percentileNs(100)
        + ",work_time_p50_ns=" + result.workTimePercentileNs(50)
        + ",work_time_p95_ns=" + result.workTimePercentileNs(95)
        + ",work_time_p99_ns=" + result.workTimePercentileNs(99)
        + ",work_time_max_ns=" + result.workTimePercentileNs(100)
        + ",paint_time_p50_ns=" + result.paintTimePercentileNs(50)
        + ",paint_time_p95_ns=" + result.paintTimePercentileNs(95)
        + ",paint_time_p99_ns=" + result.paintTimePercentileNs(99)
        + ",paint_time_max_ns=" + result.paintTimePercentileNs(100)
        + ",frames_over_16_67_count=" + result.countOverNs(FRAME_THRESHOLD_16_67_NS)
        + ",frames_over_33_3_count=" + result.countOverNs(FRAME_THRESHOLD_33_3_NS)
        + ",frames_over_50_count=" + result.countOverNs(FRAME_THRESHOLD_50_NS)
        + ",frames_over_100_count=" + result.countOverNs(FRAME_THRESHOLD_100_NS)
        + ",largest_stall_ns=" + result.percentileNs(100)
        + ",largest_consecutive_over_33_3="
        + result.maxConsecutiveOverNs(FRAME_THRESHOLD_33_3_NS)
        + result.counters.details() + result.counters.featureDetails());
    System.out.flush();
  }

  private void writeRunFrames(PassResult result) throws Exception {
    writeRunFrames(result, runOutputDir);
  }

  private void writeRunFrames(PassResult result, String targetDir) throws Exception {
    StringBuilder frames = new StringBuilder(4096);
    frames.append("frame_index,elapsed_ns,frame_time_ns,scroll_value,scroll_work_ns,paint_work_ns,work_time_ns,")
        .append("jpeg_decode_count,jpeg_decode_ns,")
        .append("jpeg_full_count,jpeg_full_ns,jpeg_half_count,jpeg_half_ns,jpeg_quarter_count,")
        .append("jpeg_quarter_ns,jpeg_eighth_count,jpeg_eighth_ns,jpeg_other_count,jpeg_other_ns,")
        .append("diagnostics_available,visible_control_first,visible_control_last,")
        .append("visible_control_count,visible_control_path_hash,")
        .append("write_pixels_attempts,write_pixels_hits,write_pixels_fallbacks,write_pixels_copied_bytes,")
        .append("write_pixels_regular_attempts,write_pixels_regular_hits,")
        .append("write_pixels_regular_fallbacks,write_pixels_regular_copied_bytes,")
        .append("write_pixels_full_hits,write_pixels_clipped_hits,")
        .append("write_pixels_full_copied_bytes,write_pixels_clipped_copied_bytes,")
        .append("write_pixels_last_width,write_pixels_last_height,write_pixels_last_format,")
        .append("write_pixels_total_ns,write_pixels_preparation_ns,write_pixels_copy_ns,")
        .append("write_pixels_rgb565_conversion_ns,")
        .append("image_materializations,native_geometry_materializations,")
        .append("target_color_attempts,target_color_hits,target_color_materializations,")
        .append("target_color_fallbacks,target_color_converted_bytes,")
        .append("physical_variant_lookups,physical_variant_hits,physical_variant_misses,")
        .append("physical_variant_materializations,physical_variant_evictions,physical_variant_bytes,")
        .append("backing_live_bytes,backing_peak_bytes,rgba8888_bytes,rgb565_bytes,gray8_bytes,"
            + "argb4444_bytes,")
        .append("scroll_jpeg_decode_count,scroll_jpeg_decode_ns,scroll_image_materializations,")
        .append("scroll_native_geometry_materializations,scroll_write_pixels_attempts,")
        .append("scroll_write_pixels_hits,scroll_write_pixels_fallbacks,scroll_write_pixels_copied_bytes,")
        .append("scroll_write_pixels_total_ns,scroll_write_pixels_preparation_ns,")
        .append("scroll_write_pixels_copy_ns,scroll_write_pixels_rgb565_conversion_ns,")
        .append("scroll_target_color_attempts,scroll_target_color_hits,")
        .append("scroll_physical_variant_lookups,scroll_physical_variant_hits,")
        .append("scroll_physical_variant_misses,")
        .append("paint_jpeg_decode_count,paint_jpeg_decode_ns,paint_image_materializations,")
        .append("paint_native_geometry_materializations,paint_write_pixels_attempts,")
        .append("paint_write_pixels_hits,paint_write_pixels_fallbacks,paint_write_pixels_copied_bytes,")
        .append("paint_write_pixels_total_ns,paint_write_pixels_preparation_ns,")
        .append("paint_write_pixels_copy_ns,paint_write_pixels_rgb565_conversion_ns,")
        .append("paint_target_color_attempts,paint_target_color_hits,")
        .append("paint_physical_variant_lookups,paint_physical_variant_hits,")
        .append("paint_physical_variant_misses\n");
    for (int i = 0; i < result.frames; i++) {
      FrameMetrics metrics = result.frameMetrics[i];
      long[] identity = visibleControlIdentity(result.framePositions[i]);
      frames.append(i).append(',').append(result.frameElapsedNs[i]).append(',')
          .append(result.frameTimesNs[i]).append(',').append(result.framePositions[i]).append(',')
          .append(result.frameScrollWorkNs[i]).append(',').append(result.framePaintWorkNs[i]).append(',')
          .append(result.frameWorkTimeNs[i]).append(',')
          .append(result.frameJpegDecodeCounts[i]).append(',').append(result.frameJpegDecodeNs[i]).append(',')
          .append(result.frameJpegFullCounts[i]).append(',').append(result.frameJpegFullNs[i]).append(',')
          .append(result.frameJpegHalfCounts[i]).append(',').append(result.frameJpegHalfNs[i]).append(',')
          .append(result.frameJpegQuarterCounts[i]).append(',').append(result.frameJpegQuarterNs[i]).append(',')
          .append(result.frameJpegEighthCounts[i]).append(',').append(result.frameJpegEighthNs[i]).append(',')
          .append(result.frameJpegOtherCounts[i]).append(',').append(result.frameJpegOtherNs[i]).append(',')
          .append(metrics.diagnosticsAvailable ? 1 : 0).append(',')
          .append(identity[0]).append(',').append(identity[1]).append(',').append(identity[2]).append(',')
          .append(identity[3]).append(',');
      appendFullMetrics(frames, metrics);
      appendSegmentMetrics(frames, "", result.frameScrollMetrics[i]);
      frames.append(',');
      appendSegmentMetrics(frames, "", result.framePaintMetrics[i]);
      frames.append('\n');
    }
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(targetDir, "frames.csv"), frames.toString());
  }

  private static void appendFullMetrics(StringBuilder output, FrameMetrics metrics) {
    output.append(metrics.writePixelsAttempts).append(',').append(metrics.writePixelsHits).append(',')
        .append(metrics.writePixelsFallbacks).append(',').append(metrics.writePixelsCopiedBytes).append(',')
        .append(metrics.writePixelsRegularAttempts).append(',').append(metrics.writePixelsRegularHits).append(',')
        .append(metrics.writePixelsRegularFallbacks).append(',')
        .append(metrics.writePixelsRegularCopiedBytes).append(',')
        .append(metrics.writePixelsFullHits).append(',').append(metrics.writePixelsClippedHits).append(',')
        .append(metrics.writePixelsFullCopiedBytes).append(',')
        .append(metrics.writePixelsClippedCopiedBytes).append(',')
        .append(metrics.writePixelsLastWidth).append(',').append(metrics.writePixelsLastHeight).append(',')
        .append(metrics.writePixelsLastFormat).append(',')
        .append(metrics.writePixelsTotalNs).append(',').append(metrics.writePixelsPreparationNs).append(',')
        .append(metrics.writePixelsCopyNs).append(',').append(metrics.writePixelsRgb565ConversionNs).append(',')
        .append(metrics.imageMaterializations).append(',').append(metrics.nativeGeometryMaterializations).append(',')
        .append(metrics.targetColorAttempts).append(',').append(metrics.targetColorHits).append(',')
        .append(metrics.targetColorMaterializations).append(',').append(metrics.targetColorFallbacks).append(',')
        .append(metrics.targetColorConvertedBytes).append(',')
        .append(metrics.physicalVariantLookups).append(',').append(metrics.physicalVariantHits).append(',')
        .append(metrics.physicalVariantMisses).append(',')
        .append(metrics.physicalVariantMaterializations).append(',')
        .append(metrics.physicalVariantEvictions).append(',').append(metrics.physicalVariantBytes).append(',')
        .append(metrics.backingLiveBytes).append(',').append(metrics.backingPeakBytes).append(',')
        .append(metrics.rgba8888Bytes).append(',').append(metrics.rgb565Bytes).append(',')
        .append(metrics.gray8Bytes).append(',').append(metrics.argb4444Bytes).append(',');
  }

  private static void appendSegmentMetrics(StringBuilder output, String unusedPrefix,
      FrameMetrics metrics) {
    output.append(metrics.jpegDecodeCount).append(',').append(metrics.jpegDecodeNs).append(',')
        .append(metrics.imageMaterializations).append(',').append(metrics.nativeGeometryMaterializations).append(',')
        .append(metrics.writePixelsAttempts).append(',').append(metrics.writePixelsHits).append(',')
        .append(metrics.writePixelsFallbacks).append(',').append(metrics.writePixelsCopiedBytes).append(',')
        .append(metrics.writePixelsTotalNs).append(',').append(metrics.writePixelsPreparationNs).append(',')
        .append(metrics.writePixelsCopyNs).append(',').append(metrics.writePixelsRgb565ConversionNs).append(',')
        .append(metrics.targetColorAttempts).append(',').append(metrics.targetColorHits).append(',')
        .append(metrics.physicalVariantLookups).append(',').append(metrics.physicalVariantHits).append(',')
        .append(metrics.physicalVariantMisses);
  }

  private void writeEnvironment() throws Exception {
    String json = "{\n"
        + "  \"packageTarget\":null,\n"
        + "  \"hostOs\":null,\n"
        + "  \"hostOsVersion\":null,\n"
        + "  \"hostArchitecture\":null,\n"
        + "  \"endianness\":null,\n"
        + "  \"cpuModel\":null,\n"
        + "  \"gpu\":null,\n"
        + "  \"ramTotalBytes\":null,\n"
        + "  \"totalCrossPlatform\":\"" + escapeJson(Settings.platform) + "\",\n"
        + "  \"expectedLogicalWidth\":" + EXPECTED_LOGICAL_WIDTH + ",\n"
        + "  \"expectedLogicalHeight\":" + EXPECTED_LOGICAL_HEIGHT + ",\n"
        + "  \"effectiveLogicalWidth\":" + Settings.screenWidth + ",\n"
        + "  \"effectiveLogicalHeight\":" + Settings.screenHeight + ",\n"
        + "  \"windowLogicalWidth\":" + Settings.screenWidth + ",\n"
        + "  \"windowLogicalHeight\":" + Settings.screenHeight + ",\n"
        + "  \"windowPhysicalWidth\":null,\n"
        + "  \"windowPhysicalHeight\":null,\n"
        + "  \"totalCrossScreenWidth\":" + Settings.screenWidth + ",\n"
        + "  \"totalCrossScreenHeight\":" + Settings.screenHeight + ",\n"
        + "  \"density\":" + Settings.screenDensity + ",\n"
        + "  \"systemDisplayScale\":null,\n"
        + "  \"refreshRate\":" + jsonMetric(benchmarkRefreshRate) + ",\n"
        + "  \"sdlDrawableWidth\":" + jsonMetric(benchmarkDrawableWidth) + ",\n"
        + "  \"sdlDrawableHeight\":" + jsonMetric(benchmarkDrawableHeight) + ",\n"
        + "  \"skiaSurfaceWidth\":" + jsonMetric(benchmarkTargetWidth) + ",\n"
        + "  \"skiaSurfaceHeight\":" + jsonMetric(benchmarkTargetHeight) + ",\n"
        + "  \"surfaceScaleX\":" + jsonDouble(benchmarkSurfaceScaleX) + ",\n"
        + "  \"surfaceScaleY\":" + jsonDouble(benchmarkSurfaceScaleY) + ",\n"
        + "  \"rendererBackend\":\"" + rendererBackendName(benchmarkRendererBackend) + "\",\n"
        + "  \"accounting\":\"" + accountingProfile + "\",\n"
        + "  \"kN32SkColorType\":" + jsonMetric(benchmarkN32ColorType) + ",\n"
        + "  \"skiaSurfaceColorType\":" + jsonMetric(benchmarkTargetColorType) + ",\n"
        + "  \"skiaSurfaceColorClassification\":\""
        + targetColorClassification(benchmarkTargetColorClass) + "\",\n"
        + "  \"skiaSurfaceAlphaType\":" + jsonMetric(benchmarkTargetAlphaType) + ",\n"
        + "  \"skiaSurfaceRowBytes\":" + jsonMetric(benchmarkTargetRowBytes) + ",\n"
        + "  \"skiaSurfacePixelBytes\":" + jsonMetric(benchmarkTargetPixelBytes) + ",\n"
        + "  \"totalCrossVersion\":\"" + escapeJson(Settings.versionStr) + "\",\n"
        + "  \"sdkVersion\":\"" + escapeJson(Settings.versionStr) + "\",\n"
        + "  \"benchmarkVersion\":\"1\",\n"
        + "  \"datasetFileCount\":" + workloadImageCount + ",\n"
        + "  \"datasetHash\":\"" + escapeJson(datasetHashArgument) + "\",\n"
        + "  \"columns\":" + COLUMN_COUNT + ",\n"
        + "  \"corpusFileEnumerationElapsedNs\":" + corpusFileEnumerationElapsedNs + ",\n"
        + "  \"imageLoadElapsedNs\":" + imageLoadElapsedNs + ",\n"
        + "  \"imageScaleElapsedNs\":" + imageScaleElapsedNs + ",\n"
        + "  \"imageControlAttachElapsedNs\":" + imageControlAttachElapsedNs + "\n"
        + "}\n";
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(outputDir, "environment.json"), json);
  }

  private void writeMemory() throws Exception {
    writeRunMemory(runOutputDir);
  }

  private void writeRunMemory(String targetDir) throws Exception {
    StringBuilder csv = new StringBuilder(2048);
    csv.append("checkpoint,elapsed_ns,current_resident_bytes,peak_resident_bytes,private_bytes,phys_footprint_bytes\n");
    csv.append("memory_unavailable,0,unavailable,unavailable,unavailable,unavailable\n");
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(targetDir, "memory.csv"), csv.toString());
  }

  private void writeTimeline() throws Exception {
    writeRunTimeline(runOutputDir);
  }

  private void writeRunTimeline(String targetDir) throws Exception {
    StringBuilder timeline = new StringBuilder(1024);
    timeline.append("event,elapsed_ns,value\n");
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(targetDir, "timeline.csv"), timeline.toString());
  }

  private static String reportFailure(Throwable failure) {
    failure.printStackTrace();
    return failure.toString();
  }

  private static String jsonMetric(long value) {
    return value < 0 ? "null" : String.valueOf(value);
  }

  private static String jsonDouble(double value) {
    return value > 0 && !Double.isInfinite(value) && !Double.isNaN(value)
        ? String.valueOf(value) : "null";
  }

  private static String rendererBackendName(long value) {
    return value == 1 ? "software" : value == 2 ? "gpu" : "unavailable";
  }

  private static String targetColorClassification(long value) {
    return value == 1 ? "BGRA8888" : value == 2 ? "RGB565" : value == 0 ? "OTHER" : "unavailable";
  }

  private void writeRunSummary(PassResult result) throws Exception {
    writeRunSummary(result, runOutputDir);
  }

  private void writeRunSummary(PassResult result, String targetDir) throws Exception {
    long requestedMask = ImageOptimizationSettings.getMask();
    long effectiveMask = ImageOptimizationSettings.getEffectiveMask();
    String json = "{\n"
        + "  \"fixture\":\"ImageScrollRealWorkloadBenchmarkApp\",\n"
        + "  \"status\":\"" + (requestedMask == effectiveMask ? "PASS" : "INVALID_CONFIGURATION") + "\",\n"
        + "  \"run\":" + runNumber + ",\n"
        + "  \"pass\":\"" + result.name + "\",\n"
        + "  \"corpus\":\"" + escapeJson(imageDir) + "\",\n"
        + "  \"imageCount\":" + imageControlCount + ",\n"
        + "  \"columns\":" + COLUMN_COUNT + ",\n"
        + "  \"prefetch\":\"" + prefetchProfile + "\",\n"
        + "  \"accounting\":\"" + accountingProfile + "\",\n"
        + "  \"requestedMask\":" + requestedMask + ",\n"
        + "  \"effectiveMask\":" + effectiveMask + ",\n"
        + "  \"corpusFileEnumerationElapsedNs\":" + corpusFileEnumerationElapsedNs + ",\n"
        + "  \"imageLoadElapsedNs\":" + imageLoadElapsedNs + ",\n"
        + "  \"imageScaleElapsedNs\":" + imageScaleElapsedNs + ",\n"
        + "  \"imageControlAttachElapsedNs\":" + imageControlAttachElapsedNs + ",\n"
        + "  \"uiBuildElapsedNs\":" + uiBuildElapsedNs + ",\n"
        + "  \"prefetchElapsedNs\":" + prefetchElapsedNs + ",\n"
        + "  \"memoryPeakResidentBytes\":-1,\n"
        + "  \"durationNs\":" + result.elapsedNs + ",\n"
        + "  \"frameCount\":" + result.frames + ",\n"
        + "  \"frameTimeP50Ns\":" + result.percentileNs(50) + ",\n"
        + "  \"frameTimeP90Ns\":" + result.percentileNs(90) + ",\n"
        + "  \"frameTimeP95Ns\":" + result.percentileNs(95) + ",\n"
        + "  \"frameTimeP99Ns\":" + result.percentileNs(99) + ",\n"
        + "  \"frameTimeMaxNs\":" + result.percentileNs(100) + ",\n"
        + "  \"workTimeP50Ns\":" + result.workTimePercentileNs(50) + ",\n"
        + "  \"workTimeP95Ns\":" + result.workTimePercentileNs(95) + ",\n"
        + "  \"workTimeP99Ns\":" + result.workTimePercentileNs(99) + ",\n"
        + "  \"workTimeMaxNs\":" + result.workTimePercentileNs(100) + ",\n"
        + "  \"paintTimeP50Ns\":" + result.paintTimePercentileNs(50) + ",\n"
        + "  \"paintTimeP95Ns\":" + result.paintTimePercentileNs(95) + ",\n"
        + "  \"paintTimeP99Ns\":" + result.paintTimePercentileNs(99) + ",\n"
        + "  \"paintTimeMaxNs\":" + result.paintTimePercentileNs(100) + ",\n"
        + "  \"framesOver16_67Count\":" + result.countOverNs(FRAME_THRESHOLD_16_67_NS) + ",\n"
        + "  \"framesOver33_3Count\":" + result.countOverNs(FRAME_THRESHOLD_33_3_NS) + ",\n"
        + "  \"framesOver50Count\":" + result.countOverNs(FRAME_THRESHOLD_50_NS) + ",\n"
        + "  \"framesOver100Count\":" + result.countOverNs(FRAME_THRESHOLD_100_NS) + ",\n"
        + "  \"largestStallNs\":" + result.percentileNs(100) + ",\n"
        + "  \"largestConsecutiveOver33_3\":"
        + result.maxConsecutiveOverNs(FRAME_THRESHOLD_33_3_NS) + "\n"
        + "}\n";
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(targetDir, "summary.json"), json);
    writeCounters(result.counters, targetDir);
  }

  private void writeCounters(Counters counters) throws Exception {
    writeCounters(counters, runOutputDir);
  }

  private void writeCounters(Counters counters, String targetDir) throws Exception {
    StringBuilder json = new StringBuilder(4096);
    json.append("{\n");
    if (!counters.accountingAvailable) {
      json.append("  \"accountingEnabled\":false,\n")
          .append("  \"diagnosticsAvailable\":false\n}\n");
      ImageRasterBenchmarkSupport.writeUtf8(
          ImageRasterBenchmarkSupport.joinPath(targetDir, "counters.json"), json.toString());
      return;
    }
    json.append("  \"accountingEnabled\":true,\n")
        .append("  \"diagnosticsAvailable\":true,\n");
    appendCounter(json, "targetedJpegDecodes", counters.targetedJpegDecodes, true);
    appendCounter(json, "fullJpegDecodes", counters.fullJpegDecodes, true);
    appendCounter(json, "imageMaterializations", counters.imageMaterializations, true);
    appendCounter(json, "imagePipelines", counters.imagePipelines, true);
    appendCounter(json, "drawPlansCreated", counters.drawPlansCreated, true);
    appendCounter(json, "drawPlanCacheHits", counters.drawPlanCacheHits, true);
    appendCounter(json, "directDrawPlanExecutions", counters.directDrawPlanExecutions, true);
    appendCounter(json, "nativeGeometryMaterializations", counters.nativeGeometryMaterializations, true);
    appendCounter(json, "writePixelsAttempts", counters.writePixelsAttempts, true);
    appendCounter(json, "writePixelsHits", counters.writePixelsHits, true);
    appendCounter(json, "writePixelsFallbacks", counters.writePixelsFallbacks, true);
    appendCounter(json, "writePixelsCopiedBytes", counters.writePixelsCopiedBytes, true);
    appendCounter(json, "writePixelsRegularAttempts", counters.writePixelsRegularAttempts, true);
    appendCounter(json, "writePixelsRegularHits", counters.writePixelsRegularHits, true);
    appendCounter(json, "writePixelsRegularFallbacks", counters.writePixelsRegularFallbacks, true);
    appendCounter(json, "writePixelsRegularCopiedBytes",
        counters.writePixelsRegularCopiedBytes, true);
    appendCounter(json, "writePixelsRegularClippedHits",
        counters.writePixelsRegularClippedHits, true);
    appendCounter(json, "writePixelsRejectInvalidTargetOrSource",
        counters.writePixelsRejectInvalidTargetOrSource, true);
    appendCounter(json, "writePixelsRejectAlphaMask", counters.writePixelsRejectAlphaMask, true);
    appendCounter(json, "writePixelsRejectMatrix", counters.writePixelsRejectMatrix, true);
    appendCounter(json, "writePixelsRejectSaveCount", counters.writePixelsRejectSaveCount, true);
    appendCounter(json, "writePixelsRejectSourceRect", counters.writePixelsRejectSourceRect, true);
    appendCounter(json, "writePixelsRejectSizeMismatch", counters.writePixelsRejectSizeMismatch, true);
    appendCounter(json, "writePixelsRejectFractionalDestination",
        counters.writePixelsRejectFractionalDestination, true);
    appendCounter(json, "writePixelsRejectDestinationBounds",
        counters.writePixelsRejectDestinationBounds, true);
    appendCounter(json, "writePixelsRejectOpacity", counters.writePixelsRejectOpacity, true);
    appendCounter(json, "writePixelsRejectSourcePixels", counters.writePixelsRejectSourcePixels, true);
    appendCounter(json, "writePixelsRejectWriteFailure",
        counters.writePixelsRejectWriteFailure, true);
    appendCounter(json, "writePixelsDeviceOneToOneCandidates",
        counters.writePixelsDeviceOneToOneCandidates, true);
    appendCounter(json, "writePixelsDeviceOneToOneKnownOpaqueCandidates",
        counters.writePixelsDeviceOneToOneKnownOpaqueCandidates, true);
    json.append("  \"jpegDecode\":{\n");
    appendJpegDecodeSection(json, "prefetch", prefetchCounters, true);
    appendJpegDecodeSection(json, "scroll", counters, false);
    json.append("  },\n");
    appendCounter(json, "opacityKnownIntrinsic", counters.opacityKnownIntrinsic, true);
    appendCounter(json, "opacityKnownFromSource", counters.opacityKnownFromSource, true);
    appendCounter(json, "opacityDeterminedDuringDecode",
        counters.opacityDeterminedDuringDecode, true);
    appendCounter(json, "opacityFallbackScans", counters.opacityFallbackScans, true);
    appendCounter(json, "physicalIdentityAttempts", counters.physicalIdentityAttempts, true);
    appendCounter(json, "physicalIdentityHits", counters.physicalIdentityHits, true);
    appendCounter(json, "physicalIdentityFallbacks", counters.physicalIdentityFallbacks, true);
    appendCounter(json, "targetColorAttempts", counters.targetColorAttempts, true);
    appendCounter(json, "targetColorHits", counters.targetColorHits, true);
    appendCounter(json, "targetColorMaterializations", counters.targetColorMaterializations, true);
    appendCounter(json, "targetColorFallbacks", counters.targetColorFallbacks, true);
    appendCounter(json, "targetColorConvertedBytes", counters.targetColorConvertedBytes, true);
    appendCounter(json, "physicalVariantLookups", counters.physicalVariantLookups, true);
    appendCounter(json, "physicalVariantHits", counters.physicalVariantHits, true);
    appendCounter(json, "physicalVariantMisses", counters.physicalVariantMisses, true);
    appendCounter(json, "physicalVariantStores", counters.physicalVariantStores, true);
    appendCounter(json, "physicalVariantEvictions", counters.physicalVariantEvictions, true);
    appendCounter(json, "physicalVariantBytes", counters.physicalVariantBytes, true);
    appendCounter(json, "physicalIdentityRejectCanvas", counters.physicalIdentityRejectCanvas, true);
    appendCounter(json, "physicalIdentityRejectSurface", counters.physicalIdentityRejectSurface, true);
    appendCounter(json, "physicalIdentityRejectClip", counters.physicalIdentityRejectClip, true);
    appendCounter(json, "physicalIdentityRejectMapping", counters.physicalIdentityRejectMapping, true);
    appendCounter(json, "physicalIdentityRejectBacking", counters.physicalIdentityRejectBacking, true);
    appendCounter(json, "physicalIdentityRejectExecution", counters.physicalIdentityRejectExecution, true);
    appendCounter(json, "targetColorUniqueSources", counters.targetColorUniqueSources, true);
    appendCounter(json, "targetColorUniqueFullKeys", counters.targetColorUniqueFullKeys, true);
    appendCounter(json, "targetColorUniqueNoDestinationKeys",
        counters.targetColorUniqueNoDestinationKeys, true);
    appendCounter(json, "targetColorUniqueIntrinsicKeys",
        counters.targetColorUniqueIntrinsicKeys, true);
    appendCounter(json, "targetColorAcquisitionSources",
        counters.targetColorAcquisitionSources, true);
    appendCounter(json, "targetColorPendingReplacements",
        counters.targetColorPendingReplacements, true);
    appendCounter(json, "targetColorRejectCanvas", counters.targetColorRejectCanvas, true);
    appendCounter(json, "targetColorRejectSurface", counters.targetColorRejectSurface, true);
    appendCounter(json, "targetColorRejectClip", counters.targetColorRejectClip, true);
    appendCounter(json, "targetColorRejectMapping", counters.targetColorRejectMapping, true);
    appendCounter(json, "targetColorRejectBacking", counters.targetColorRejectBacking, true);
    appendCounter(json, "targetColorRejectExecution", counters.targetColorRejectExecution, true);
    appendCounter(json, "physicalVariantUniqueFullKeys", counters.physicalVariantUniqueFullKeys, true);
    appendCounter(json, "physicalVariantUniqueNoSurfaceSizeKeys",
        counters.physicalVariantUniqueNoSurfaceSizeKeys, true);
    appendCounter(json, "physicalVariantUniqueSources",
        counters.physicalVariantUniqueSources, true);
    appendCounter(json, "physicalVariantPendingReplacements",
        counters.physicalVariantPendingReplacements, true);
    appendCounter(json, "physicalVariantRejectCanvas", counters.physicalVariantRejectCanvas, true);
    appendCounter(json, "physicalVariantRejectSurface", counters.physicalVariantRejectSurface, true);
    appendCounter(json, "physicalVariantRejectClip", counters.physicalVariantRejectClip, true);
    appendCounter(json, "physicalVariantRejectMapping", counters.physicalVariantRejectMapping, true);
    appendCounter(json, "physicalVariantRejectBacking", counters.physicalVariantRejectBacking, true);
    appendCounter(json, "physicalVariantRejectExecution",
        counters.physicalVariantRejectExecution, true);
    appendCounter(json, "sharedSlotTargetToPhysical", counters.sharedSlotTargetToPhysical, true);
    appendCounter(json, "sharedSlotPhysicalToTarget", counters.sharedSlotPhysicalToTarget, true);
    appendCounter(json, "sharedPendingTargetToPhysical",
        counters.sharedPendingTargetToPhysical, true);
    appendCounter(json, "sharedPendingPhysicalToTarget",
        counters.sharedPendingPhysicalToTarget, true);
    appendDiagnosticMetrics(json, "targetColorSaveCount", SAVE_COUNT_BUCKET_NAMES,
        counters.targetColorSaveCount);
    appendDiagnosticMetrics(json, "physicalVariantSaveCount", SAVE_COUNT_BUCKET_NAMES,
        counters.physicalVariantSaveCount);
    appendDiagnosticMetrics(json, "physicalIdentitySaveCount", SAVE_COUNT_BUCKET_NAMES,
        counters.physicalIdentitySaveCount);
    appendDiagnosticMetrics(json, "targetColorMapping", MAPPING_SUBREASON_NAMES,
        counters.targetColorMappingSubreasons);
    appendDiagnosticMetrics(json, "physicalVariantMapping", MAPPING_SUBREASON_NAMES,
        counters.physicalVariantMappingSubreasons);
    appendDiagnosticMetrics(json, "physicalIdentityMapping", MAPPING_SUBREASON_NAMES,
        counters.physicalIdentityMappingSubreasons);
    appendCounter(json, "backingLiveBytes", counters.backingLiveBytes, true);
    appendCounter(json, "backingPeakBytes", counters.backingPeakBytes, true);
    json.append("  \"prefetchPhases\":{\n")
        .append("    \"imageMaterializations\":")
        .append(prefetchCounters == null ? 0 : prefetchCounters.imageMaterializations).append(",\n")
        .append("    \"nativeGeometryMaterializations\":")
        .append(prefetchCounters == null ? 0 : prefetchCounters.nativeGeometryMaterializations)
        .append(",\n")
        .append("    \"targetColorMaterializations\":")
        .append(prefetchCounters == null ? 0 : prefetchCounters.targetColorMaterializations)
        .append(",\n")
        .append("    \"targetColorConvertedBytes\":")
        .append(prefetchCounters == null ? 0 : prefetchCounters.targetColorConvertedBytes)
        .append(",\n")
        .append("    \"physicalVariantStores\":")
        .append(prefetchCounters == null ? 0 : prefetchCounters.physicalVariantStores)
        .append(",\n")
        .append("    \"physicalVariantBytes\":")
        .append(prefetchCounters == null ? 0 : prefetchCounters.physicalVariantBytes).append("\n")
        .append("  },\n");
    json.append("  \"features\":{\n");
    long effectiveMask = ImageOptimizationSettings.getEffectiveMask();
    for (int feature = 0; feature < FEATURE_NAMES.length; feature++) {
      long hits = counters.featureHits(feature);
      String status = counters.featureStatus(feature, effectiveMask, hits);
      json.append("    \"").append(FEATURE_NAMES[feature]).append("\":{\"bit\":")
          .append(feature).append(",\"enabled\":")
          .append((effectiveMask & (1L << feature)) != 0)
          .append(",\"hits\":").append(hits).append(",\"status\":\"")
          .append(status).append("\"}")
          .append(feature == FEATURE_NAMES.length - 1 ? "\n" : ",\n");
    }
    json.append("  },\n  \"accounting\":{\n")
        .append("    \"encoded\":{\"count\":").append(imageControlCount)
        .append(",\"bytes\":null},\n")
        .append("    \"decoded\":{\"count\":").append(counters.fullJpegDecodes + counters.targetedJpegDecodes)
        .append(",\"bytes\":").append(jsonMetric(counters.decodeFinalBufferBytes)).append("},\n")
        .append("    \"raster\":{\"count\":").append(counters.imageMaterializations)
        .append(",\"bytes\":").append(jsonMetric(counters.rasterBytes())).append("},\n")
        .append("    \"physicalVariant\":{\"count\":").append(counters.physicalVariantStores)
        .append(",\"bytes\":").append(jsonMetric(counters.physicalVariantBytes)).append("},\n")
        .append("    \"prefetch\":{\"count\":").append(prefetchRequestCount)
        .append(",\"bytes\":").append(jsonMetric(prefetchBackingPeakBytes)).append("},\n")
        .append("    \"trackedTexture\":{\"count\":null,\"bytes\":null},\n")
        .append("    \"image\":{\"count\":").append(counters.imageCreated).append(",\"bytes\":null},\n")
        .append("    \"backing\":{\"count\":null,\"bytes\":").append(jsonMetric(counters.backingPeakBytes)).append("},\n")
        .append("    \"colorType\":{\"rgba8888Bytes\":").append(jsonMetric(counters.rgba8888Bytes))
        .append(",\"rgb565Bytes\":").append(jsonMetric(counters.rgb565Bytes))
        .append(",\"gray8Bytes\":").append(jsonMetric(counters.gray8Bytes))
        .append(",\"argb4444Bytes\":").append(jsonMetric(counters.argb4444Bytes)).append("}\n")
        .append("  }\n}\n");
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(targetDir, "counters.json"), json.toString());
  }

  private static void appendCounter(StringBuilder json, String name, long value, boolean comma) {
    json.append("  \"").append(name).append("\":").append(value)
        .append(comma ? ",\n" : "\n");
  }

  private static void appendDiagnosticMetrics(StringBuilder json, String prefix,
      String[] names, long[] values) {
    for (int i = 0; i < names.length; i++) {
      appendCounter(json, prefix + names[i], values[i], true);
    }
  }

  private static void appendJpegDecodeSection(StringBuilder json, String name, Counters counters,
      boolean comma) {
    long count = counters == null ? 0 : counters.jpegDecodeCount;
    long ns = counters == null ? 0 : counters.jpegDecodeNs;
    long fullCount = counters == null ? 0 : counters.jpegFullCount;
    long fullNs = counters == null ? 0 : counters.jpegFullNs;
    long halfCount = counters == null ? 0 : counters.jpegHalfCount;
    long halfNs = counters == null ? 0 : counters.jpegHalfNs;
    long quarterCount = counters == null ? 0 : counters.jpegQuarterCount;
    long quarterNs = counters == null ? 0 : counters.jpegQuarterNs;
    long eighthCount = counters == null ? 0 : counters.jpegEighthCount;
    long eighthNs = counters == null ? 0 : counters.jpegEighthNs;
    long otherCount = counters == null ? 0 : counters.jpegOtherCount;
    long otherNs = counters == null ? 0 : counters.jpegOtherNs;
    long requestedFull = counters == null ? 0 : counters.jpegRequestedFullCount;
    long requestedTarget = counters == null ? 0 : counters.jpegRequestedTargetCount;
    long requestedExplicitRatio = counters == null ? 0 : counters.jpegRequestedExplicitRatioCount;
    long requestedBestFit = counters == null ? 0 : counters.jpegRequestedBestFitCount;
    long failures = counters == null ? 0 : counters.jpegDecodeFailureCount;
    json.append("    \"").append(name).append("\":{\"count\":").append(count)
        .append(",\"ns\":").append(ns)
        .append(",\"full\":{\"count\":").append(fullCount)
        .append(",\"ns\":").append(fullNs).append("}")
        .append(",\"half\":{\"count\":").append(halfCount)
        .append(",\"ns\":").append(halfNs).append("}")
        .append(",\"quarter\":{\"count\":").append(quarterCount)
        .append(",\"ns\":").append(quarterNs).append("}")
        .append(",\"eighth\":{\"count\":").append(eighthCount)
        .append(",\"ns\":").append(eighthNs).append("}")
        .append(",\"other\":{\"count\":").append(otherCount)
        .append(",\"ns\":").append(otherNs).append("}")
        .append(",\"requested\":{\"full\":").append(requestedFull)
        .append(",\"target\":").append(requestedTarget)
        .append(",\"explicitRatio\":").append(requestedExplicitRatio)
        .append(",\"bestFit\":").append(requestedBestFit).append("}")
        .append(",\"failures\":").append(failures).append("}")
        .append(comma ? ",\n" : "\n");
  }

  private static String escapeJson(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private final class MeasuredRow extends Container {
    @Override
    public void onPaint(Graphics g) {
      measuredRowPaints++;
      super.onPaint(g);
    }
  }

  private final class MeasuredImageControl extends ImageControl {
    MeasuredImageControl(Image image) {
      super(image);
    }

    @Override
    public void onPaint(Graphics g) {
      measuredImagePaints++;
      super.onPaint(g);
    }
  }

  private static final class PocPassResult {
    final String name;
    final long elapsedNs;
    final int frameCount;
    final int movementFrameCount;
    final long attempts;
    final long hits;
    final long fallbacks;
    final long viewportPixels;
    final long reusedPixels;
    final long dirtyPixels;
    final long movedBytes;
    final long screenUpdateNs;
    final long postMoveRecoveries;

    PocPassResult(String name, long elapsedNs, int frameCount, int movementFrameCount,
        long attempts, long hits,
        long fallbacks, long viewportPixels, long reusedPixels, long dirtyPixels, long movedBytes,
        long screenUpdateNs, long postMoveRecoveries) {
      this.name = name;
      this.elapsedNs = elapsedNs;
      this.frameCount = frameCount;
      this.movementFrameCount = movementFrameCount;
      this.attempts = attempts;
      this.hits = hits;
      this.fallbacks = fallbacks;
      this.viewportPixels = viewportPixels;
      this.reusedPixels = reusedPixels;
      this.dirtyPixels = dirtyPixels;
      this.movedBytes = movedBytes;
      this.screenUpdateNs = screenUpdateNs;
      this.postMoveRecoveries = postMoveRecoveries;
    }
  }

  private static final class FrameMetrics {
    final boolean diagnosticsAvailable;
    final long jpegDecodeCount;
    final long jpegDecodeNs;
    final long jpegFullCount;
    final long jpegFullNs;
    final long jpegHalfCount;
    final long jpegHalfNs;
    final long jpegQuarterCount;
    final long jpegQuarterNs;
    final long jpegEighthCount;
    final long jpegEighthNs;
    final long jpegOtherCount;
    final long jpegOtherNs;
    final long imageMaterializations;
    final long nativeGeometryMaterializations;
    final long writePixelsAttempts;
    final long writePixelsHits;
    final long writePixelsFallbacks;
    final long writePixelsCopiedBytes;
    final long writePixelsRegularAttempts;
    final long writePixelsRegularHits;
    final long writePixelsRegularFallbacks;
    final long writePixelsRegularCopiedBytes;
    final long writePixelsFullHits;
    final long writePixelsClippedHits;
    final long writePixelsFullCopiedBytes;
    final long writePixelsClippedCopiedBytes;
    final long writePixelsLastWidth;
    final long writePixelsLastHeight;
    final long writePixelsLastFormat;
    final long writePixelsTotalNs;
    final long writePixelsPreparationNs;
    final long writePixelsCopyNs;
    final long writePixelsRgb565ConversionNs;
    final long targetColorAttempts;
    final long targetColorHits;
    final long targetColorMaterializations;
    final long targetColorFallbacks;
    final long targetColorConvertedBytes;
    final long physicalVariantLookups;
    final long physicalVariantHits;
    final long physicalVariantMisses;
    final long physicalVariantMaterializations;
    final long physicalVariantEvictions;
    final long physicalVariantBytes;
    final long backingLiveBytes;
    final long backingPeakBytes;
    final long rgba8888Bytes;
    final long rgb565Bytes;
    final long gray8Bytes;
    final long argb4444Bytes;

    private FrameMetrics(boolean diagnosticsAvailable, long jpegDecodeCount, long jpegDecodeNs,
        long jpegFullCount, long jpegFullNs, long jpegHalfCount, long jpegHalfNs,
        long jpegQuarterCount, long jpegQuarterNs, long jpegEighthCount, long jpegEighthNs,
        long jpegOtherCount, long jpegOtherNs, long imageMaterializations,
        long nativeGeometryMaterializations, long writePixelsAttempts, long writePixelsHits,
        long writePixelsFallbacks, long writePixelsCopiedBytes, long writePixelsRegularAttempts,
        long writePixelsRegularHits, long writePixelsRegularFallbacks,
        long writePixelsRegularCopiedBytes, long writePixelsFullHits, long writePixelsClippedHits,
        long writePixelsFullCopiedBytes, long writePixelsClippedCopiedBytes,
        long writePixelsLastWidth, long writePixelsLastHeight, long writePixelsLastFormat,
        long writePixelsTotalNs, long writePixelsPreparationNs, long writePixelsCopyNs,
        long writePixelsRgb565ConversionNs,
        long targetColorAttempts, long targetColorHits, long targetColorMaterializations,
        long targetColorFallbacks, long targetColorConvertedBytes, long physicalVariantLookups,
        long physicalVariantHits, long physicalVariantMisses, long physicalVariantMaterializations,
        long physicalVariantEvictions, long physicalVariantBytes, long backingLiveBytes,
        long backingPeakBytes, long rgba8888Bytes, long rgb565Bytes, long gray8Bytes,
        long argb4444Bytes) {
      this.diagnosticsAvailable = diagnosticsAvailable;
      this.jpegDecodeCount = jpegDecodeCount;
      this.jpegDecodeNs = jpegDecodeNs;
      this.jpegFullCount = jpegFullCount;
      this.jpegFullNs = jpegFullNs;
      this.jpegHalfCount = jpegHalfCount;
      this.jpegHalfNs = jpegHalfNs;
      this.jpegQuarterCount = jpegQuarterCount;
      this.jpegQuarterNs = jpegQuarterNs;
      this.jpegEighthCount = jpegEighthCount;
      this.jpegEighthNs = jpegEighthNs;
      this.jpegOtherCount = jpegOtherCount;
      this.jpegOtherNs = jpegOtherNs;
      this.imageMaterializations = imageMaterializations;
      this.nativeGeometryMaterializations = nativeGeometryMaterializations;
      this.writePixelsAttempts = writePixelsAttempts;
      this.writePixelsHits = writePixelsHits;
      this.writePixelsFallbacks = writePixelsFallbacks;
      this.writePixelsCopiedBytes = writePixelsCopiedBytes;
      this.writePixelsRegularAttempts = writePixelsRegularAttempts;
      this.writePixelsRegularHits = writePixelsRegularHits;
      this.writePixelsRegularFallbacks = writePixelsRegularFallbacks;
      this.writePixelsRegularCopiedBytes = writePixelsRegularCopiedBytes;
      this.writePixelsFullHits = writePixelsFullHits;
      this.writePixelsClippedHits = writePixelsClippedHits;
      this.writePixelsFullCopiedBytes = writePixelsFullCopiedBytes;
      this.writePixelsClippedCopiedBytes = writePixelsClippedCopiedBytes;
      this.writePixelsLastWidth = writePixelsLastWidth;
      this.writePixelsLastHeight = writePixelsLastHeight;
      this.writePixelsLastFormat = writePixelsLastFormat;
      this.writePixelsTotalNs = writePixelsTotalNs;
      this.writePixelsPreparationNs = writePixelsPreparationNs;
      this.writePixelsCopyNs = writePixelsCopyNs;
      this.writePixelsRgb565ConversionNs = writePixelsRgb565ConversionNs;
      this.targetColorAttempts = targetColorAttempts;
      this.targetColorHits = targetColorHits;
      this.targetColorMaterializations = targetColorMaterializations;
      this.targetColorFallbacks = targetColorFallbacks;
      this.targetColorConvertedBytes = targetColorConvertedBytes;
      this.physicalVariantLookups = physicalVariantLookups;
      this.physicalVariantHits = physicalVariantHits;
      this.physicalVariantMisses = physicalVariantMisses;
      this.physicalVariantMaterializations = physicalVariantMaterializations;
      this.physicalVariantEvictions = physicalVariantEvictions;
      this.physicalVariantBytes = physicalVariantBytes;
      this.backingLiveBytes = backingLiveBytes;
      this.backingPeakBytes = backingPeakBytes;
      this.rgba8888Bytes = rgba8888Bytes;
      this.rgb565Bytes = rgb565Bytes;
      this.gray8Bytes = gray8Bytes;
      this.argb4444Bytes = argb4444Bytes;
    }

    static FrameMetrics capture() {
      boolean available = Image.diagnosticAccountingEnabledForTest();
      if (!available) {
        return new FrameMetrics(false, Image.jpegNativeDecodeCountForTest,
            Image.jpegNativeDecodeNsForTest, Image.jpegNativeDecodeFullCountForTest,
            Image.jpegNativeDecodeFullNsForTest, Image.jpegNativeDecodeHalfCountForTest,
            Image.jpegNativeDecodeHalfNsForTest, Image.jpegNativeDecodeQuarterCountForTest,
            Image.jpegNativeDecodeQuarterNsForTest, Image.jpegNativeDecodeEighthCountForTest,
            Image.jpegNativeDecodeEighthNsForTest, Image.jpegNativeDecodeOtherCountForTest,
            Image.jpegNativeDecodeOtherNsForTest, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1);
      }
      return new FrameMetrics(true, Image.jpegNativeDecodeCountForTest,
          Image.jpegNativeDecodeNsForTest, Image.jpegNativeDecodeFullCountForTest,
          Image.jpegNativeDecodeFullNsForTest, Image.jpegNativeDecodeHalfCountForTest,
          Image.jpegNativeDecodeHalfNsForTest, Image.jpegNativeDecodeQuarterCountForTest,
          Image.jpegNativeDecodeQuarterNsForTest, Image.jpegNativeDecodeEighthCountForTest,
          Image.jpegNativeDecodeEighthNsForTest, Image.jpegNativeDecodeOtherCountForTest,
          Image.jpegNativeDecodeOtherNsForTest, Image.materializationCountForTest(),
          Image.nativeGeometryMaterializationCountForTest(), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_ATTEMPTS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_HITS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_FALLBACKS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_COPIED_BYTES), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_REGULAR_ATTEMPTS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_REGULAR_HITS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_REGULAR_FALLBACKS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_REGULAR_COPIED_BYTES), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_FULL_HITS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_CLIPPED_HITS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_FULL_COPIED_BYTES), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_CLIPPED_COPIED_BYTES), frameMetric(
          NativeImageBacking.WRITE_PIXELS_FRAME_LAST_WIDTH), frameMetric(
          NativeImageBacking.WRITE_PIXELS_FRAME_LAST_HEIGHT), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_LAST_FORMAT), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_TOTAL_NS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_PREPARATION_NS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_COPY_NS), frameMetric(
              NativeImageBacking.WRITE_PIXELS_FRAME_RGB565_CONVERSION_NS),
          NativeImageBacking.targetColorAttemptsForTest(), NativeImageBacking.targetColorHitsForTest(),
          NativeImageBacking.targetColorMaterializationsForTest(),
          NativeImageBacking.targetColorFallbacksForTest(),
          NativeImageBacking.targetColorConvertedBytesForTest(),
          NativeImageBacking.physicalVariantLookupsForTest(),
          NativeImageBacking.physicalVariantHitsForTest(),
          NativeImageBacking.physicalVariantMissesForTest(),
          NativeImageBacking.physicalVariantMaterializationsForTest(),
          NativeImageBacking.physicalVariantEvictionsForTest(),
          NativeImageBacking.physicalVariantBytesForTest(),
          NativeImageBacking.backingBytesLiveForTest(),
          NativeImageBacking.backingBytesPeakLiveForTest(),
          NativeImageBacking.rgba8888BackingBytesForTest(),
          NativeImageBacking.rgb565BackingBytesForTest(),
          NativeImageBacking.gray8BackingBytesForTest(),
          NativeImageBacking.argb4444BackingBytesForTest());
    }

    private static long frameMetric(int kind) {
      return NativeImageBacking.writePixelsFrameMetricForTest(kind);
    }

    static FrameMetrics delta(FrameMetrics before, FrameMetrics after) {
      boolean available = after.diagnosticsAvailable;
      return new FrameMetrics(available, after.jpegDecodeCount - before.jpegDecodeCount,
          after.jpegDecodeNs - before.jpegDecodeNs, after.jpegFullCount - before.jpegFullCount,
          after.jpegFullNs - before.jpegFullNs, after.jpegHalfCount - before.jpegHalfCount,
          after.jpegHalfNs - before.jpegHalfNs, after.jpegQuarterCount - before.jpegQuarterCount,
          after.jpegQuarterNs - before.jpegQuarterNs, after.jpegEighthCount - before.jpegEighthCount,
          after.jpegEighthNs - before.jpegEighthNs, after.jpegOtherCount - before.jpegOtherCount,
          after.jpegOtherNs - before.jpegOtherNs, available ? after.imageMaterializations
              - before.imageMaterializations : -1, available ? after.nativeGeometryMaterializations
              - before.nativeGeometryMaterializations : -1, after.writePixelsAttempts,
          after.writePixelsHits, after.writePixelsFallbacks, after.writePixelsCopiedBytes,
          after.writePixelsRegularAttempts, after.writePixelsRegularHits,
          after.writePixelsRegularFallbacks, after.writePixelsRegularCopiedBytes,
          after.writePixelsFullHits, after.writePixelsClippedHits,
          after.writePixelsFullCopiedBytes, after.writePixelsClippedCopiedBytes,
          after.writePixelsLastWidth, after.writePixelsLastHeight, after.writePixelsLastFormat,
          after.writePixelsTotalNs, after.writePixelsPreparationNs, after.writePixelsCopyNs,
          after.writePixelsRgb565ConversionNs,
          available ? after.targetColorAttempts - before.targetColorAttempts : -1,
          available ? after.targetColorHits - before.targetColorHits : -1,
          available ? after.targetColorMaterializations - before.targetColorMaterializations : -1,
          available ? after.targetColorFallbacks - before.targetColorFallbacks : -1,
          available ? after.targetColorConvertedBytes - before.targetColorConvertedBytes : -1,
          available ? after.physicalVariantLookups - before.physicalVariantLookups : -1,
          available ? after.physicalVariantHits - before.physicalVariantHits : -1,
          available ? after.physicalVariantMisses - before.physicalVariantMisses : -1,
          available ? after.physicalVariantMaterializations
              - before.physicalVariantMaterializations : -1,
          available ? after.physicalVariantEvictions - before.physicalVariantEvictions : -1,
          available ? after.physicalVariantBytes - before.physicalVariantBytes : -1,
          available ? after.backingLiveBytes : -1, available ? after.backingPeakBytes : -1,
          available ? after.rgba8888Bytes : -1, available ? after.rgb565Bytes : -1,
          available ? after.gray8Bytes : -1, available ? after.argb4444Bytes : -1);
    }

    static FrameMetrics merge(FrameMetrics first, FrameMetrics second) {
      boolean available = first.diagnosticsAvailable && second.diagnosticsAvailable;
      return new FrameMetrics(available, first.jpegDecodeCount + second.jpegDecodeCount,
          first.jpegDecodeNs + second.jpegDecodeNs, first.jpegFullCount + second.jpegFullCount,
          first.jpegFullNs + second.jpegFullNs, first.jpegHalfCount + second.jpegHalfCount,
          first.jpegHalfNs + second.jpegHalfNs, first.jpegQuarterCount + second.jpegQuarterCount,
          first.jpegQuarterNs + second.jpegQuarterNs, first.jpegEighthCount + second.jpegEighthCount,
          first.jpegEighthNs + second.jpegEighthNs, first.jpegOtherCount + second.jpegOtherCount,
          first.jpegOtherNs + second.jpegOtherNs, available ? first.imageMaterializations
              + second.imageMaterializations : -1, available ? first.nativeGeometryMaterializations
              + second.nativeGeometryMaterializations : -1, add(first.writePixelsAttempts,
              second.writePixelsAttempts), add(first.writePixelsHits, second.writePixelsHits),
          add(first.writePixelsFallbacks, second.writePixelsFallbacks),
          add(first.writePixelsCopiedBytes, second.writePixelsCopiedBytes),
          add(first.writePixelsRegularAttempts, second.writePixelsRegularAttempts),
          add(first.writePixelsRegularHits, second.writePixelsRegularHits),
          add(first.writePixelsRegularFallbacks, second.writePixelsRegularFallbacks),
          add(first.writePixelsRegularCopiedBytes, second.writePixelsRegularCopiedBytes),
          add(first.writePixelsFullHits, second.writePixelsFullHits),
          add(first.writePixelsClippedHits, second.writePixelsClippedHits),
          add(first.writePixelsFullCopiedBytes, second.writePixelsFullCopiedBytes),
          add(first.writePixelsClippedCopiedBytes, second.writePixelsClippedCopiedBytes),
          second.writePixelsLastWidth >= 0 ? second.writePixelsLastWidth : first.writePixelsLastWidth,
          second.writePixelsLastHeight >= 0 ? second.writePixelsLastHeight : first.writePixelsLastHeight,
          second.writePixelsLastFormat >= 0 ? second.writePixelsLastFormat : first.writePixelsLastFormat,
          add(first.writePixelsTotalNs, second.writePixelsTotalNs),
          add(first.writePixelsPreparationNs, second.writePixelsPreparationNs),
          add(first.writePixelsCopyNs, second.writePixelsCopyNs),
          add(first.writePixelsRgb565ConversionNs, second.writePixelsRgb565ConversionNs),
          available ? first.targetColorAttempts + second.targetColorAttempts : -1,
          available ? first.targetColorHits + second.targetColorHits : -1,
          available ? first.targetColorMaterializations + second.targetColorMaterializations : -1,
          available ? first.targetColorFallbacks + second.targetColorFallbacks : -1,
          available ? first.targetColorConvertedBytes + second.targetColorConvertedBytes : -1,
          available ? first.physicalVariantLookups + second.physicalVariantLookups : -1,
          available ? first.physicalVariantHits + second.physicalVariantHits : -1,
          available ? first.physicalVariantMisses + second.physicalVariantMisses : -1,
          available ? first.physicalVariantMaterializations + second.physicalVariantMaterializations : -1,
          available ? first.physicalVariantEvictions + second.physicalVariantEvictions : -1,
          available ? first.physicalVariantBytes + second.physicalVariantBytes : -1,
          available ? second.backingLiveBytes : -1, available ? second.backingPeakBytes : -1,
          available ? second.rgba8888Bytes : -1, available ? second.rgb565Bytes : -1,
          available ? second.gray8Bytes : -1, available ? second.argb4444Bytes : -1);
    }

    private static long add(long first, long second) {
      return first < 0 || second < 0 ? -1 : first + second;
    }
  }

  private static final class PassResult {
    final String name;
    final boolean forward;
    final int minimum;
    final int start;
    final int end;
    final int maximum;
    final long elapsedNs;
    final int frames;
    final long[] frameTimesNs;
    final long[] frameElapsedNs;
    final int[] framePositions;
    final long[] frameScrollWorkNs;
    final long[] framePaintWorkNs;
    final long[] frameWorkTimeNs;
    final long[] sortedFrameTimesNs;
    final long[] sortedWorkTimeNs;
    final long[] sortedPaintWorkNs;
    final long[] frameJpegDecodeCounts;
    final long[] frameJpegDecodeNs;
    final long[] frameJpegFullCounts;
    final long[] frameJpegFullNs;
    final long[] frameJpegHalfCounts;
    final long[] frameJpegHalfNs;
    final long[] frameJpegQuarterCounts;
    final long[] frameJpegQuarterNs;
    final long[] frameJpegEighthCounts;
    final long[] frameJpegEighthNs;
    final long[] frameJpegOtherCounts;
    final long[] frameJpegOtherNs;
    final FrameMetrics[] frameMetrics;
    final FrameMetrics[] frameScrollMetrics;
    final FrameMetrics[] framePaintMetrics;
    final Counters counters;

    PassResult(String name, boolean forward, int minimum, int end, int maximum, long elapsedNs,
        int frames, long[] frameTimesNs, long[] frameElapsedNs, int[] framePositions,
        long[] frameScrollWorkNs, long[] framePaintWorkNs, long[] frameWorkTimeNs,
        long[] sortedFrameTimesNs, long[] sortedWorkTimeNs, long[] sortedPaintWorkNs,
        long[] frameJpegDecodeCounts, long[] frameJpegDecodeNs,
        long[] frameJpegFullCounts, long[] frameJpegFullNs, long[] frameJpegHalfCounts,
        long[] frameJpegHalfNs, long[] frameJpegQuarterCounts, long[] frameJpegQuarterNs,
        long[] frameJpegEighthCounts, long[] frameJpegEighthNs,
        long[] frameJpegOtherCounts, long[] frameJpegOtherNs, FrameMetrics[] frameMetrics,
        FrameMetrics[] frameScrollMetrics, FrameMetrics[] framePaintMetrics, Counters counters) {
      this.name = name;
      this.forward = forward;
      this.minimum = minimum;
      this.start = forward ? minimum : maximum;
      this.end = end;
      this.maximum = maximum;
      this.elapsedNs = elapsedNs;
      this.frames = frames;
      this.frameTimesNs = frameTimesNs;
      this.frameElapsedNs = frameElapsedNs;
      this.framePositions = framePositions;
      this.frameScrollWorkNs = frameScrollWorkNs;
      this.framePaintWorkNs = framePaintWorkNs;
      this.frameWorkTimeNs = frameWorkTimeNs;
      this.sortedFrameTimesNs = sortedFrameTimesNs;
      this.sortedWorkTimeNs = sortedWorkTimeNs;
      this.sortedPaintWorkNs = sortedPaintWorkNs;
      this.frameJpegDecodeCounts = frameJpegDecodeCounts;
      this.frameJpegDecodeNs = frameJpegDecodeNs;
      this.frameJpegFullCounts = frameJpegFullCounts;
      this.frameJpegFullNs = frameJpegFullNs;
      this.frameJpegHalfCounts = frameJpegHalfCounts;
      this.frameJpegHalfNs = frameJpegHalfNs;
      this.frameJpegQuarterCounts = frameJpegQuarterCounts;
      this.frameJpegQuarterNs = frameJpegQuarterNs;
      this.frameJpegEighthCounts = frameJpegEighthCounts;
      this.frameJpegEighthNs = frameJpegEighthNs;
      this.frameJpegOtherCounts = frameJpegOtherCounts;
      this.frameJpegOtherNs = frameJpegOtherNs;
      this.frameMetrics = frameMetrics;
      this.frameScrollMetrics = frameScrollMetrics;
      this.framePaintMetrics = framePaintMetrics;
      this.counters = counters;
    }

    long percentileNs(int percent) {
      return percentileNs(sortedFrameTimesNs, percent);
    }

    long workTimePercentileNs(int percent) {
      return percentileNs(sortedWorkTimeNs, percent);
    }

    long paintTimePercentileNs(int percent) {
      return percentileNs(sortedPaintWorkNs, percent);
    }

    private static long percentileNs(long[] sortedValues, int percent) {
      if (percent <= 0) {
        return sortedValues[0];
      }
      if (percent >= 100) {
        return sortedValues[sortedValues.length - 1];
      }
      int index = (int) Math.ceil(sortedValues.length * percent / 100.0) - 1;
      return sortedValues[Math.max(0, Math.min(sortedValues.length - 1, index))];
    }

    int countOverNs(long thresholdNs) {
      int count = 0;
      for (long frameTimeNs : sortedFrameTimesNs) {
        if (frameTimeNs > thresholdNs) {
          count++;
        }
      }
      return count;
    }

    int maxConsecutiveOverNs(long thresholdNs) {
      int maximum = 0;
      int current = 0;
      for (long frameTimeNs : frameTimesNs) {
        current = frameTimeNs > thresholdNs ? current + 1 : 0;
        maximum = Math.max(maximum, current);
      }
      return maximum;
    }
  }

  private static void appendPolicyDiagnosticDetails(StringBuilder details, Counters counters) {
    details.append(",target_color_unique_sources=").append(counters.targetColorUniqueSources)
        .append(",target_color_unique_full_keys=").append(counters.targetColorUniqueFullKeys)
        .append(",target_color_unique_no_destination_keys=")
        .append(counters.targetColorUniqueNoDestinationKeys)
        .append(",target_color_unique_intrinsic_keys=")
        .append(counters.targetColorUniqueIntrinsicKeys)
        .append(",target_color_acquisition_sources=")
        .append(counters.targetColorAcquisitionSources)
        .append(",target_color_pending_replacements=")
        .append(counters.targetColorPendingReplacements)
        .append(",target_color_reject_canvas=").append(counters.targetColorRejectCanvas)
        .append(",target_color_reject_surface=").append(counters.targetColorRejectSurface)
        .append(",target_color_reject_clip=").append(counters.targetColorRejectClip)
        .append(",target_color_reject_mapping=").append(counters.targetColorRejectMapping)
        .append(",target_color_reject_backing=").append(counters.targetColorRejectBacking)
        .append(",target_color_reject_execution=").append(counters.targetColorRejectExecution)
        .append(",physical_variant_unique_full_keys=")
        .append(counters.physicalVariantUniqueFullKeys)
        .append(",physical_variant_unique_no_surface_size_keys=")
        .append(counters.physicalVariantUniqueNoSurfaceSizeKeys)
        .append(",physical_variant_unique_sources=")
        .append(counters.physicalVariantUniqueSources)
        .append(",physical_variant_pending_replacements=")
        .append(counters.physicalVariantPendingReplacements)
        .append(",physical_variant_reject_canvas=").append(counters.physicalVariantRejectCanvas)
        .append(",physical_variant_reject_surface=").append(counters.physicalVariantRejectSurface)
        .append(",physical_variant_reject_clip=").append(counters.physicalVariantRejectClip)
        .append(",physical_variant_reject_mapping=").append(counters.physicalVariantRejectMapping)
        .append(",physical_variant_reject_backing=").append(counters.physicalVariantRejectBacking)
        .append(",physical_variant_reject_execution=")
        .append(counters.physicalVariantRejectExecution)
        .append(",shared_slot_target_to_physical=").append(counters.sharedSlotTargetToPhysical)
        .append(",shared_slot_physical_to_target=").append(counters.sharedSlotPhysicalToTarget)
        .append(",shared_pending_target_to_physical=")
        .append(counters.sharedPendingTargetToPhysical)
        .append(",shared_pending_physical_to_target=")
        .append(counters.sharedPendingPhysicalToTarget)
        .append(",physical_identity_save_count_diagnostics=separate_by_feature");
    appendDiagnosticDetails(details, "target_color_save_count_", SAVE_COUNT_BUCKET_NAMES,
        counters.targetColorSaveCount);
    appendDiagnosticDetails(details, "physical_variant_save_count_", SAVE_COUNT_BUCKET_NAMES,
        counters.physicalVariantSaveCount);
    appendDiagnosticDetails(details, "physical_identity_save_count_", SAVE_COUNT_BUCKET_NAMES,
        counters.physicalIdentitySaveCount);
    appendDiagnosticDetails(details, "target_color_mapping_", MAPPING_SUBREASON_NAMES,
        counters.targetColorMappingSubreasons);
    appendDiagnosticDetails(details, "physical_variant_mapping_", MAPPING_SUBREASON_NAMES,
        counters.physicalVariantMappingSubreasons);
    appendDiagnosticDetails(details, "physical_identity_mapping_", MAPPING_SUBREASON_NAMES,
        counters.physicalIdentityMappingSubreasons);
  }

  private static void appendDiagnosticDetails(StringBuilder details, String prefix,
      String[] names, long[] values) {
    for (int i = 0; i < names.length; i++) {
      details.append(",").append(prefix).append(names[i].toLowerCase()).append("=")
          .append(values[i]);
    }
  }

  private static final class Counters {
    final boolean accountingAvailable = Image.diagnosticAccountingEnabledForTest();
    final long imageCreated = Image.imageCreatedCountForTest();
    final long targetedJpegDecodes = Image.targetedDecodeInvocationCountForTest();
    final long fullJpegDecodes = Image.fullDecodeInvocationCountForTest();
    final long jpegDecodeCount = Image.jpegNativeDecodeCountForTest;
    final long jpegDecodeNs = Image.jpegNativeDecodeNsForTest;
    final long jpegFullCount = Image.jpegNativeDecodeFullCountForTest;
    final long jpegFullNs = Image.jpegNativeDecodeFullNsForTest;
    final long jpegHalfCount = Image.jpegNativeDecodeHalfCountForTest;
    final long jpegHalfNs = Image.jpegNativeDecodeHalfNsForTest;
    final long jpegQuarterCount = Image.jpegNativeDecodeQuarterCountForTest;
    final long jpegQuarterNs = Image.jpegNativeDecodeQuarterNsForTest;
    final long jpegEighthCount = Image.jpegNativeDecodeEighthCountForTest;
    final long jpegEighthNs = Image.jpegNativeDecodeEighthNsForTest;
    final long jpegOtherCount = Image.jpegNativeDecodeOtherCountForTest;
    final long jpegOtherNs = Image.jpegNativeDecodeOtherNsForTest;
    final long jpegRequestedFullCount = Image.jpegNativeDecodeRequestedFullCountForTest;
    final long jpegRequestedTargetCount = Image.jpegNativeDecodeRequestedTargetCountForTest;
    final long jpegRequestedExplicitRatioCount = Image.jpegNativeDecodeRequestedExplicitRatioCountForTest;
    final long jpegRequestedBestFitCount = Image.jpegNativeDecodeRequestedBestFitCountForTest;
    final long jpegDecodeFailureCount = Image.jpegNativeDecodeFailureCountForTest;
    final long decodeFinalBufferBytes = Image.decodeFinalBufferBytesForTest();
    final long opacityKnownIntrinsic = Image.opacityKnownIntrinsicForTest();
    final long opacityKnownFromSource = Image.opacityKnownFromSourceForTest();
    final long opacityDeterminedDuringDecode = Image.opacityDeterminedDuringDecodeForTest();
    final long opacityFallbackScans = Image.opacityFallbackScansForTest();
    final long imageMaterializations = Image.materializationCountForTest();
    final long imagePipelines = Image.imagePipelineCreatedCountForTest();
    final long drawPlansCreated = Image.imageDrawPlanCreatedCountForTest();
    final long drawPlanCacheHits = Image.imageDrawPlanCacheHitCountForTest();
    final long directDrawPlanExecutions = Image.directDrawPlanExecutionCountForTest();
    final long nativeGeometryMaterializations = Image.nativeGeometryMaterializationCountForTest();
    final long writePixelsAttempts = NativeImageBacking.writePixelsAttemptsForTest();
    final long writePixelsHits = NativeImageBacking.writePixelsHitsForTest();
    final long writePixelsFallbacks = NativeImageBacking.writePixelsFallbacksForTest();
    final long writePixelsCopiedBytes = NativeImageBacking.writePixelsCopiedBytesForTest();
    final long writePixelsRegularAttempts = NativeImageBacking.writePixelsRegAttempts();
    final long writePixelsRegularHits = NativeImageBacking.writePixelsRegHits();
    final long writePixelsRegularFallbacks = NativeImageBacking.writePixelsRegFallbacks();
    final long writePixelsRegularCopiedBytes = NativeImageBacking.writePixelsRegBytes();
    final long writePixelsRegularClippedHits = NativeImageBacking.writePixelsRegClipped();
    final long writePixelsRejectInvalidTargetOrSource = NativeImageBacking.writePixelsRejectInvalidTargetOrSourceForTest();
    final long writePixelsRejectAlphaMask = NativeImageBacking.writePixelsRejectAlphaMaskForTest();
    final long writePixelsRejectMatrix = NativeImageBacking.writePixelsRejectMatrixForTest();
    final long writePixelsRejectSaveCount = NativeImageBacking.writePixelsRejectSaveCountForTest();
    final long writePixelsRejectSourceRect = NativeImageBacking.writePixelsRejectSourceRectForTest();
    final long writePixelsRejectSizeMismatch = NativeImageBacking.writePixelsRejectSizeMismatchForTest();
    final long writePixelsRejectFractionalDestination = NativeImageBacking.writePixelsRejectFractionalDestinationForTest();
    final long writePixelsRejectDestinationBounds = NativeImageBacking.writePixelsRejectDestinationBoundsForTest();
    final long writePixelsRejectOpacity = NativeImageBacking.writePixelsRejectOpacityForTest();
    final long writePixelsRejectSourcePixels = NativeImageBacking.writePixelsRejectSourcePixelsForTest();
    final long writePixelsRejectWriteFailure = NativeImageBacking.writePixelsRejectWriteFailureForTest();
    final long writePixelsDeviceOneToOneCandidates = NativeImageBacking.writePixelsDeviceOneToOneCandidatesForTest();
    final long writePixelsDeviceOneToOneKnownOpaqueCandidates = NativeImageBacking.writePixelsDeviceOneToOneKnownOpaqueCandidatesForTest();
    final long physicalIdentityAttempts = NativeImageBacking.physicalIdentityAttemptsForTest();
    final long physicalIdentityHits = NativeImageBacking.physicalIdentityHitsForTest();
    final long physicalIdentityFallbacks = NativeImageBacking.physicalIdentityFallbacksForTest();
    final long physicalIdentityRejectCanvas = NativeImageBacking.physicalIdentityRejectionCountForTest(0);
    final long physicalIdentityRejectSurface = NativeImageBacking.physicalIdentityRejectionCountForTest(1);
    final long physicalIdentityRejectClip = NativeImageBacking.physicalIdentityRejectionCountForTest(2);
    final long physicalIdentityRejectMapping = NativeImageBacking.physicalIdentityRejectionCountForTest(4);
    final long physicalIdentityRejectBacking = NativeImageBacking.physicalIdentityRejectionCountForTest(5);
    final long physicalIdentityRejectExecution = NativeImageBacking.physicalIdentityRejectionCountForTest(6);
    final long targetColorAttempts = NativeImageBacking.targetColorAttemptsForTest();
    final long targetColorHits = NativeImageBacking.targetColorHitsForTest();
    final long targetColorMaterializations = NativeImageBacking.targetColorMaterializationsForTest();
    final long targetColorFallbacks = NativeImageBacking.targetColorFallbacksForTest();
    final long targetColorConvertedBytes = NativeImageBacking.targetColorConvertedBytesForTest();
    final long physicalVariantLookups = NativeImageBacking.physicalVariantLookupsForTest();
    final long physicalVariantHits = NativeImageBacking.physicalVariantHitsForTest();
    final long physicalVariantMisses = NativeImageBacking.physicalVariantMissesForTest();
    final long physicalVariantStores = NativeImageBacking.physicalVariantMaterializationsForTest();
    final long physicalVariantEvictions = NativeImageBacking.physicalVariantEvictionsForTest();
    final long physicalVariantBytes = NativeImageBacking.physicalVariantBytesForTest();
    final long targetColorUniqueSources = diagnosticMetric(0);
    final long targetColorUniqueFullKeys = diagnosticMetric(1);
    final long targetColorUniqueNoDestinationKeys = diagnosticMetric(2);
    final long targetColorUniqueIntrinsicKeys = diagnosticMetric(3);
    final long targetColorPendingReplacements = diagnosticMetric(4);
    final long targetColorAcquisitionSources = diagnosticMetric(5);
    final long targetColorRejectCanvas = diagnosticMetric(8);
    final long targetColorRejectSurface = diagnosticMetric(9);
    final long targetColorRejectClip = diagnosticMetric(10);
    final long targetColorRejectMapping = diagnosticMetric(12);
    final long targetColorRejectBacking = diagnosticMetric(13);
    final long targetColorRejectExecution = diagnosticMetric(14);
    final long physicalVariantUniqueFullKeys = diagnosticMetric(16);
    final long physicalVariantUniqueNoSurfaceSizeKeys = diagnosticMetric(17);
    final long physicalVariantPendingReplacements = diagnosticMetric(18);
    final long physicalVariantUniqueSources = diagnosticMetric(19);
    final long physicalVariantRejectCanvas = diagnosticMetric(24);
    final long physicalVariantRejectSurface = diagnosticMetric(25);
    final long physicalVariantRejectClip = diagnosticMetric(26);
    final long physicalVariantRejectMapping = diagnosticMetric(28);
    final long physicalVariantRejectBacking = diagnosticMetric(29);
    final long physicalVariantRejectExecution = diagnosticMetric(30);
    final long sharedSlotTargetToPhysical = diagnosticMetric(32);
    final long sharedSlotPhysicalToTarget = diagnosticMetric(33);
    final long sharedPendingTargetToPhysical = diagnosticMetric(34);
    final long sharedPendingPhysicalToTarget = diagnosticMetric(35);
    final long[] targetColorSaveCount = diagnosticMetrics(40, 6);
    final long[] physicalVariantSaveCount = diagnosticMetrics(48, 6);
    final long[] physicalIdentitySaveCount = diagnosticMetrics(56, 6);
    final long[] targetColorMappingSubreasons = diagnosticMetrics(64, 10);
    final long[] physicalVariantMappingSubreasons = diagnosticMetrics(80, 10);
    final long[] physicalIdentityMappingSubreasons = diagnosticMetrics(96, 10);
    final long genericGeometryDraws = NativeImageBacking.genericGeometryDrawsForTest();
    final long smoothResampleDraws = NativeImageBacking.smoothResampleDrawsForTest();
    final long backingLiveBytes = NativeImageBacking.backingBytesLiveForTest();
    final long backingPeakBytes = NativeImageBacking.backingBytesPeakLiveForTest();
    final long rgba8888Bytes = NativeImageBacking.rgba8888BackingBytesForTest();
    final long rgb565Bytes = NativeImageBacking.rgb565BackingBytesForTest();
    final long gray8Bytes = NativeImageBacking.gray8BackingBytesForTest();
    final long argb4444Bytes = NativeImageBacking.argb4444BackingBytesForTest();

    static Counters capture() {
      return new Counters();
    }

    long rasterBytes() {
      if (rgba8888Bytes < 0 || rgb565Bytes < 0 || gray8Bytes < 0 || argb4444Bytes < 0) {
        return -1;
      }
      return rgba8888Bytes + rgb565Bytes + gray8Bytes + argb4444Bytes;
    }

    String details() {
      StringBuilder details = new StringBuilder(",targeted_jpeg_decodes=" + targetedJpegDecodes
          + ",full_jpeg_decodes=" + fullJpegDecodes
          + ",jpeg_decode_count=" + jpegDecodeCount
          + ",jpeg_decode_ns=" + jpegDecodeNs
          + ",jpeg_full_count=" + jpegFullCount
          + ",jpeg_half_count=" + jpegHalfCount
          + ",jpeg_quarter_count=" + jpegQuarterCount
          + ",jpeg_eighth_count=" + jpegEighthCount
          + ",jpeg_other_count=" + jpegOtherCount
          + ",opacity_known_intrinsic=" + opacityKnownIntrinsic
          + ",opacity_known_from_source=" + opacityKnownFromSource
          + ",opacity_determined_decode=" + opacityDeterminedDuringDecode
          + ",opacity_fallback_scans=" + opacityFallbackScans
          + ",image_materializations=" + imageMaterializations
          + ",image_pipelines=" + imagePipelines
          + ",draw_plans_created=" + drawPlansCreated
          + ",draw_plan_cache_hits=" + drawPlanCacheHits
          + ",direct_draw_plan_executions=" + directDrawPlanExecutions
          + ",native_geometry_materializations=" + nativeGeometryMaterializations
          + ",write_pixels_attempts=" + writePixelsAttempts
          + ",write_pixels_hits=" + writePixelsHits
          + ",write_pixels_fallbacks=" + writePixelsFallbacks
          + ",write_pixels_copied_bytes=" + writePixelsCopiedBytes
          + ",write_pixels_regular_attempts=" + writePixelsRegularAttempts
          + ",write_pixels_regular_hits=" + writePixelsRegularHits
          + ",write_pixels_regular_fallbacks=" + writePixelsRegularFallbacks
          + ",write_pixels_regular_copied_bytes=" + writePixelsRegularCopiedBytes
          + ",write_pixels_regular_clipped_hits=" + writePixelsRegularClippedHits
          + ",write_pixels_device_1to1_candidates=" + writePixelsDeviceOneToOneCandidates
          + ",write_pixels_device_1to1_known_opaque_candidates="
          + writePixelsDeviceOneToOneKnownOpaqueCandidates
          + ",write_pixels_reject_matrix=" + writePixelsRejectMatrix
          + ",write_pixels_reject_save_count=" + writePixelsRejectSaveCount
          + ",write_pixels_reject_size_mismatch=" + writePixelsRejectSizeMismatch
          + ",physical_identity_attempts=" + physicalIdentityAttempts
          + ",physical_identity_hits=" + physicalIdentityHits
          + ",physical_identity_fallbacks=" + physicalIdentityFallbacks
          + ",physical_identity_reject_canvas=" + physicalIdentityRejectCanvas
          + ",physical_identity_reject_surface=" + physicalIdentityRejectSurface
          + ",physical_identity_reject_clip=" + physicalIdentityRejectClip
          + ",physical_identity_reject_mapping=" + physicalIdentityRejectMapping
          + ",physical_identity_reject_backing=" + physicalIdentityRejectBacking
          + ",physical_identity_reject_execution=" + physicalIdentityRejectExecution
          + ",target_color_attempts=" + targetColorAttempts
          + ",target_color_hits=" + targetColorHits
          + ",target_color_materializations=" + targetColorMaterializations
          + ",target_color_fallbacks=" + targetColorFallbacks
          + ",target_color_converted_bytes=" + targetColorConvertedBytes
          + ",physical_variant_lookups=" + physicalVariantLookups
          + ",physical_variant_hits=" + physicalVariantHits
          + ",physical_variant_misses=" + physicalVariantMisses
          + ",physical_variant_stores=" + physicalVariantStores
          + ",physical_variant_evictions=" + physicalVariantEvictions
          + ",physical_variant_bytes=" + physicalVariantBytes);
      appendPolicyDiagnosticDetails(details, this);
      return details
          + ",generic_geometry_draws=" + genericGeometryDraws
          + ",smooth_resample_draws=" + smoothResampleDraws
          + ",backing_live_bytes=" + backingLiveBytes
          + ",backing_peak_bytes=" + backingPeakBytes;
    }

    long featureHits(int feature) {
      switch (feature) {
      case ImageOptimizationSettings.DECODE_ZERO_COPY:
        return Image.zeroCopyDecodeCountForTest();
      case ImageOptimizationSettings.RASTER_OPACITY_METADATA:
        return Image.opacityKnownFromSourceForTest() + Image.opacityDeterminedDuringDecodeForTest();
      case ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS:
        return writePixelsHits;
      case ImageOptimizationSettings.RASTER_ROW_READBACK:
        return Image.rowReadbackCountForTest();
      case ImageOptimizationSettings.RASTER_DIRECT_COLOR_MATERIALIZATION:
        return Image.directColorMaterializationCountForTest();
      case ImageOptimizationSettings.STORAGE_RGB565:
        return NativeImageBacking.rgb565BackingBytesForTest() > 0 ? 1 : 0;
      case ImageOptimizationSettings.STORAGE_GRAY8:
        return NativeImageBacking.gray8BackingBytesForTest() > 0 ? 1 : 0;
      case ImageOptimizationSettings.STORAGE_ARGB4444:
        return NativeImageBacking.argb4444BackingBytesForTest() > 0 ? 1 : 0;
      case ImageOptimizationSettings.CACHE_MEMORY_PRESSURE_EVICTION:
        return NativeImageBacking.physicalVariantEvictionsForTest();
      case ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING:
        return 1;
      case ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION:
        return targetColorHits;
      case ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE:
        return physicalVariantHits;
      case ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING:
        return physicalIdentityHits;
      default:
        return 0;
      }
    }

    private static boolean isAttemptFeature(int feature) {
      return feature == ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS
          || feature == ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION
          || feature == ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE
          || feature == ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING;
    }

    long featureActivity(int feature) {
      switch (feature) {
      case ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS:
        return writePixelsAttempts;
      case ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION:
        return targetColorAttempts;
      case ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE:
        return physicalVariantLookups;
      case ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING:
        return physicalIdentityAttempts;
      default:
        return featureHits(feature);
      }
    }

    String featureStatus(int feature, long enabledMask, long hits) {
      if ((enabledMask & (1L << feature)) == 0) {
        return "DISABLED";
      }
      if (isAttemptFeature(feature)) {
        long activity = featureActivity(feature);
        return activity == 0 ? "NOT_REACHED" : hits == 0 ? "ATTEMPTED_NO_HIT" : "EXERCISED";
      }
      return hits == 0 ? "NOT_EXERCISED" : "EXERCISED";
    }

    String featureDetails() {
      StringBuilder details = new StringBuilder(1024);
      long enabledMask = ImageOptimizationSettings.getEffectiveMask();
      for (int feature = 0; feature < FEATURE_NAMES.length; feature++) {
        long hits = featureHits(feature);
        String status = featureStatus(feature, enabledMask, hits);
        details.append(",feature_").append(FEATURE_NAMES[feature]).append("_hits=").append(hits)
            .append(",feature_").append(FEATURE_NAMES[feature]).append("_status=").append(status);
      }
      return details.toString();
    }
  }
}
