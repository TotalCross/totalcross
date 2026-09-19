// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.Arrays;

import totalcross.io.File;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Container;
import totalcross.ui.Flick;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.ScrollContainer;
import totalcross.ui.Window;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;

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

  private ScrollContainer mainContainer;
  private ScrollContainer scroll;
  private int tileWidth;
  private int rowCount;
  private int imageControlCount;
  private long uiBuildElapsedNs;
  private String imageDir;
  private String maskArgument;
  private long runNumber;
  private String outputDir;
  private String runOutputDir;
  private String datasetHashArgument;
  private String prefetchProfile;
  private String accountingProfile;
  private long benchmarkTargetWidth = -1;
  private long benchmarkTargetHeight = -1;
  private long benchmarkTargetRowBytes = -1;
  private long benchmarkTargetAlphaType = -1;
  private long benchmarkTargetColorType = -1;
  private long benchmarkTargetColorClass = -1;
  private long benchmarkN32ColorType = -1;
  private long benchmarkRendererBackend = -1;
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
      ImageRasterBenchmarkSupport.require(imageDir != null && imageDir.length() > 0,
          "missing --corpus=<dir>");
      ImageRasterBenchmarkSupport.require(maskArgument != null && maskArgument.length() > 0,
          "missing --image-optimization=<mask>");
      ImageRasterBenchmarkSupport.require(scrollDurationNs > 0,
          "duration must be positive");
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
      captureTargetMetrics();
      Image.resetImageOperationAccountingForBenchmarkTest(accountingEnabled());
      ImagePreparation.resetAccountingForTest();

      long buildStartNs = System.nanoTime();
      String[] imagePaths = sortedCorpusPaths(imageDir);
      ImageRasterBenchmarkSupport.require(imagePaths.length == IMAGE_COUNT,
          "expected exactly " + IMAGE_COUNT + " corpus files but found " + imagePaths.length);
      buildUi(imagePaths);
      uiBuildElapsedNs = System.nanoTime() - buildStartNs;
      ImageRasterBenchmarkSupport.require(rowCount == IMAGE_COUNT / COLUMN_COUNT,
          "unexpected row count " + rowCount);
      ImageRasterBenchmarkSupport.require(imageControlCount == IMAGE_COUNT,
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
      Image.resetImageOperationAccountingForBenchmarkTest(accountingEnabled());
      int minimum = scroll.sbV.getMinimum();
      int maximum = validMaximum();
      PassResult cold = runPass("cold", true, minimum, maximum);
      printPass(cold);
      writeRunFrames(cold);
      writeRunSummary(cold);
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
        + ",run=" + runNumber
        + ",output_dir=" + String.valueOf(runOutputDir)
        + ",image_dir=" + String.valueOf(imageDir)
        + ",image_count=" + IMAGE_COUNT + ",rows=" + rowCount
        + ",image_controls=" + imageControlCount + ",tile_logical=" + tileWidth
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
    try {
      ImageOptimizationSettings.setMask(Long.parseLong(maskArgument));
    } catch (NumberFormatException error) {
      throw new IllegalArgumentException("image-optimization must be decimal: " + maskArgument);
    }
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
    return "mask-" + (maskArgument == null ? "default" : maskArgument)
        + "-prefetch-" + prefetchProfile + "-accounting-" + accountingProfile
        + "-run-" + runNumber;
  }

  private boolean prefetchEnabled() {
    return "on".equals(prefetchProfile);
  }

  private boolean accountingEnabled() {
    return "on".equals(accountingProfile);
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
        row = new Container();
        row.setBackColor(totalcross.ui.gfx.Color.darker(totalcross.ui.gfx.Color.GREEN));
        scroll.add(row, LEFT, AFTER + 2, width, tileWidth);
        rowCount++;
        controlsInRow = 0;
      }
      Image image = new Image(imagePaths[i]).getSmoothScaledInstance(tileWidth, tileWidth);
      row.add(new ImageControl(image), AFTER + 1, TOP, tileWidth, tileWidth);
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
      int target = elapsedNs >= scrollDurationNs
          ? endpoint
          : minimum + (int) ((long) (maximum - minimum) * elapsedNs / scrollDurationNs);
      long jpegDecodeCountBefore = Image.jpegNativeDecodeCountForTest;
      long jpegDecodeNsBefore = Image.jpegNativeDecodeNsForTest;
      long jpegFullCountBefore = Image.jpegNativeDecodeFullCountForTest;
      long jpegFullNsBefore = Image.jpegNativeDecodeFullNsForTest;
      long jpegHalfCountBefore = Image.jpegNativeDecodeHalfCountForTest;
      long jpegHalfNsBefore = Image.jpegNativeDecodeHalfNsForTest;
      long jpegQuarterCountBefore = Image.jpegNativeDecodeQuarterCountForTest;
      long jpegQuarterNsBefore = Image.jpegNativeDecodeQuarterNsForTest;
      long jpegEighthCountBefore = Image.jpegNativeDecodeEighthCountForTest;
      long jpegEighthNsBefore = Image.jpegNativeDecodeEighthNsForTest;
      long jpegOtherCountBefore = Image.jpegNativeDecodeOtherCountForTest;
      long jpegOtherNsBefore = Image.jpegNativeDecodeOtherNsForTest;
      int before = scroll.sbV.getValue();
      long workStartNs = System.nanoTime();
      long scrollWorkNs = 0;
      if (target != before) {
        long scrollWorkStartNs = System.nanoTime();
        ImageRasterBenchmarkSupport.require(scroll.scrollContent(0, target - before, true),
            name + " stopped before reaching time-based target");
        scrollWorkNs = Math.max(0, System.nanoTime() - scrollWorkStartNs);
      }
      long paintWorkStartNs = System.nanoTime();
      scroll.repaintNow();
      long paintWorkNs = Math.max(0, System.nanoTime() - paintWorkStartNs);
      long workTimeNs = Math.max(0, System.nanoTime() - workStartNs);
      long jpegDecodeCountAfter = Image.jpegNativeDecodeCountForTest;
      long jpegDecodeNsAfter = Image.jpegNativeDecodeNsForTest;
      long jpegFullCountAfter = Image.jpegNativeDecodeFullCountForTest;
      long jpegFullNsAfter = Image.jpegNativeDecodeFullNsForTest;
      long jpegHalfCountAfter = Image.jpegNativeDecodeHalfCountForTest;
      long jpegHalfNsAfter = Image.jpegNativeDecodeHalfNsForTest;
      long jpegQuarterCountAfter = Image.jpegNativeDecodeQuarterCountForTest;
      long jpegQuarterNsAfter = Image.jpegNativeDecodeQuarterNsForTest;
      long jpegEighthCountAfter = Image.jpegNativeDecodeEighthCountForTest;
      long jpegEighthNsAfter = Image.jpegNativeDecodeEighthNsForTest;
      long jpegOtherCountAfter = Image.jpegNativeDecodeOtherCountForTest;
      long jpegOtherNsAfter = Image.jpegNativeDecodeOtherNsForTest;
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
      }
      frameTimesNs[frames] = frameTimeNs;
      frameElapsedNs[frames] = elapsedNs;
      framePositions[frames] = scroll.sbV.getValue();
      frameScrollWorkNs[frames] = scrollWorkNs;
      framePaintWorkNs[frames] = paintWorkNs;
      frameWorkTimeNs[frames] = workTimeNs;
      frameJpegDecodeCounts[frames] = jpegDecodeCountAfter - jpegDecodeCountBefore;
      frameJpegDecodeNs[frames] = jpegDecodeNsAfter - jpegDecodeNsBefore;
      frameJpegFullCounts[frames] = jpegFullCountAfter - jpegFullCountBefore;
      frameJpegFullNs[frames] = jpegFullNsAfter - jpegFullNsBefore;
      frameJpegHalfCounts[frames] = jpegHalfCountAfter - jpegHalfCountBefore;
      frameJpegHalfNs[frames] = jpegHalfNsAfter - jpegHalfNsBefore;
      frameJpegQuarterCounts[frames] = jpegQuarterCountAfter - jpegQuarterCountBefore;
      frameJpegQuarterNs[frames] = jpegQuarterNsAfter - jpegQuarterNsBefore;
      frameJpegEighthCounts[frames] = jpegEighthCountAfter - jpegEighthCountBefore;
      frameJpegEighthNs[frames] = jpegEighthNsAfter - jpegEighthNsBefore;
      frameJpegOtherCounts[frames] = jpegOtherCountAfter - jpegOtherCountBefore;
      frameJpegOtherNs[frames] = jpegOtherNsAfter - jpegOtherNsBefore;
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
        actualFrameJpegEighthNs, actualFrameJpegOtherCounts, actualFrameJpegOtherNs, counters);
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

  private void printPass(PassResult result) {
    System.out.println("fixture=ImageScrollRealWorkloadBenchmarkApp,record=pass"
        + ",resolution=" + width + "x" + height
        + ",prefetch_profile=" + prefetchProfile
        + ",accounting=" + accountingProfile
        + ",requested_mask=" + (maskArgument == null ? "default" : maskArgument)
        + ",effective_mask=" + ImageOptimizationSettings.getEffectiveMask()
        + ",run=" + runNumber
        + ",image_count=" + IMAGE_COUNT + ",rows=" + rowCount
        + ",image_controls=" + imageControlCount + ",tile_logical=" + tileWidth
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
    StringBuilder frames = new StringBuilder(4096);
    frames.append("frame_index,elapsed_ns,frame_time_ns,scroll_value,scroll_work_ns,paint_work_ns,work_time_ns,")
        .append("jpeg_decode_count,jpeg_decode_ns,")
        .append("jpeg_full_count,jpeg_full_ns,jpeg_half_count,jpeg_half_ns,jpeg_quarter_count,")
        .append("jpeg_quarter_ns,jpeg_eighth_count,jpeg_eighth_ns,jpeg_other_count,jpeg_other_ns\n");
    for (int i = 0; i < result.frames; i++) {
      frames.append(i).append(',').append(result.frameElapsedNs[i]).append(',')
          .append(result.frameTimesNs[i]).append(',').append(result.framePositions[i]).append(',')
          .append(result.frameScrollWorkNs[i]).append(',').append(result.framePaintWorkNs[i]).append(',')
          .append(result.frameWorkTimeNs[i]).append(',')
          .append(result.frameJpegDecodeCounts[i]).append(',').append(result.frameJpegDecodeNs[i]).append(',')
          .append(result.frameJpegFullCounts[i]).append(',').append(result.frameJpegFullNs[i]).append(',')
          .append(result.frameJpegHalfCounts[i]).append(',').append(result.frameJpegHalfNs[i]).append(',')
          .append(result.frameJpegQuarterCounts[i]).append(',').append(result.frameJpegQuarterNs[i]).append(',')
          .append(result.frameJpegEighthCounts[i]).append(',').append(result.frameJpegEighthNs[i]).append(',')
          .append(result.frameJpegOtherCounts[i]).append(',').append(result.frameJpegOtherNs[i]).append('\n');
    }
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "frames.csv"), frames.toString());
  }

  private void writeEnvironment() throws Exception {
    String json = "{\n"
        + "  \"os\":\"" + escapeJson(Settings.platform) + "\",\n"
        + "  \"osVersion\":\"unavailable\",\n"
        + "  \"architecture\":\"unavailable\",\n"
        + "  \"endianness\":\"unavailable\",\n"
        + "  \"cpu\":\"unavailable\",\n"
        + "  \"gpu\":\"unavailable\",\n"
        + "  \"ramTotalBytes\":null,\n"
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
        + "  \"refreshRate\":null,\n"
        + "  \"sdlDrawableWidth\":" + jsonMetric(benchmarkTargetWidth) + ",\n"
        + "  \"sdlDrawableHeight\":" + jsonMetric(benchmarkTargetHeight) + ",\n"
        + "  \"skiaSurfaceWidth\":" + jsonMetric(benchmarkTargetWidth) + ",\n"
        + "  \"skiaSurfaceHeight\":" + jsonMetric(benchmarkTargetHeight) + ",\n"
        + "  \"rendererBackend\":\"" + rendererBackendName(benchmarkRendererBackend) + "\",\n"
        + "  \"accounting\":\"" + accountingProfile + "\",\n"
        + "  \"kN32SkColorType\":" + jsonMetric(benchmarkN32ColorType) + ",\n"
        + "  \"skiaSurfaceColorType\":" + jsonMetric(benchmarkTargetColorType) + ",\n"
        + "  \"skiaSurfaceColorClassification\":\""
        + targetColorClassification(benchmarkTargetColorClass) + "\",\n"
        + "  \"skiaSurfaceAlphaType\":" + jsonMetric(benchmarkTargetAlphaType) + ",\n"
        + "  \"skiaSurfaceRowBytes\":" + jsonMetric(benchmarkTargetRowBytes) + ",\n"
        + "  \"totalCrossVersion\":\"" + escapeJson(Settings.versionStr) + "\",\n"
        + "  \"sdkVersion\":\"" + escapeJson(Settings.versionStr) + "\",\n"
        + "  \"benchmarkVersion\":\"1\",\n"
        + "  \"datasetFileCount\":" + IMAGE_COUNT + ",\n"
        + "  \"datasetHash\":\"" + escapeJson(datasetHashArgument) + "\",\n"
        + "  \"columns\":" + COLUMN_COUNT + "\n"
        + "}\n";
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(outputDir, "environment.json"), json);
  }

  private void writeMemory() throws Exception {
    StringBuilder csv = new StringBuilder(2048);
    csv.append("checkpoint,elapsed_ns,current_resident_bytes,peak_resident_bytes,private_bytes,phys_footprint_bytes\n");
    csv.append("memory_unavailable,0,unavailable,unavailable,unavailable,unavailable\n");
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "memory.csv"), csv.toString());
  }

  private void writeTimeline() throws Exception {
    StringBuilder timeline = new StringBuilder(1024);
    timeline.append("event,elapsed_ns,value\n");
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "timeline.csv"), timeline.toString());
  }

  private static String reportFailure(Throwable failure) {
    failure.printStackTrace();
    return failure.toString();
  }

  private static String jsonMetric(long value) {
    return value < 0 ? "null" : String.valueOf(value);
  }

  private static String rendererBackendName(long value) {
    return value == 1 ? "software" : value == 2 ? "gpu" : "unavailable";
  }

  private static String targetColorClassification(long value) {
    return value == 1 ? "BGRA8888" : value == 2 ? "RGB565" : value == 0 ? "OTHER" : "unavailable";
  }

  private void writeRunSummary(PassResult result) throws Exception {
    long requestedMask = ImageOptimizationSettings.getMask();
    long effectiveMask = ImageOptimizationSettings.getEffectiveMask();
    String json = "{\n"
        + "  \"fixture\":\"ImageScrollRealWorkloadBenchmarkApp\",\n"
        + "  \"status\":\"" + (requestedMask == effectiveMask ? "PASS" : "INVALID_CONFIGURATION") + "\",\n"
        + "  \"run\":" + runNumber + ",\n"
        + "  \"corpus\":\"" + escapeJson(imageDir) + "\",\n"
        + "  \"imageCount\":" + imageControlCount + ",\n"
        + "  \"columns\":" + COLUMN_COUNT + ",\n"
        + "  \"prefetch\":\"" + prefetchProfile + "\",\n"
        + "  \"accounting\":\"" + accountingProfile + "\",\n"
        + "  \"requestedMask\":" + requestedMask + ",\n"
        + "  \"effectiveMask\":" + effectiveMask + ",\n"
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
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "summary.json"), json);
    writeCounters(result.counters);
  }

  private void writeCounters(Counters counters) throws Exception {
    StringBuilder json = new StringBuilder(4096);
    json.append("{\n");
    if (!counters.accountingAvailable) {
      json.append("  \"accountingEnabled\":false,\n")
          .append("  \"diagnosticsAvailable\":false\n}\n");
      ImageRasterBenchmarkSupport.writeUtf8(
          ImageRasterBenchmarkSupport.joinPath(runOutputDir, "counters.json"), json.toString());
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
    appendCounter(json, "physicalVariantLookups", counters.physicalVariantLookups, true);
    appendCounter(json, "physicalVariantHits", counters.physicalVariantHits, true);
    appendCounter(json, "physicalVariantMisses", counters.physicalVariantMisses, true);
    appendCounter(json, "physicalVariantStores", counters.physicalVariantStores, true);
    appendCounter(json, "physicalVariantBytes", counters.physicalVariantBytes, true);
    appendCounter(json, "backingLiveBytes", counters.backingLiveBytes, true);
    appendCounter(json, "backingPeakBytes", counters.backingPeakBytes, true);
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
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "counters.json"), json.toString());
  }

  private static void appendCounter(StringBuilder json, String name, long value, boolean comma) {
    json.append("  \"").append(name).append("\":").append(value)
        .append(comma ? ",\n" : "\n");
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
    final Counters counters;

    PassResult(String name, boolean forward, int minimum, int end, int maximum, long elapsedNs,
        int frames, long[] frameTimesNs, long[] frameElapsedNs, int[] framePositions,
        long[] frameScrollWorkNs, long[] framePaintWorkNs, long[] frameWorkTimeNs,
        long[] sortedFrameTimesNs, long[] sortedWorkTimeNs, long[] sortedPaintWorkNs,
        long[] frameJpegDecodeCounts, long[] frameJpegDecodeNs,
        long[] frameJpegFullCounts, long[] frameJpegFullNs, long[] frameJpegHalfCounts,
        long[] frameJpegHalfNs, long[] frameJpegQuarterCounts, long[] frameJpegQuarterNs,
        long[] frameJpegEighthCounts, long[] frameJpegEighthNs,
        long[] frameJpegOtherCounts, long[] frameJpegOtherNs, Counters counters) {
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
    final long physicalIdentityRejectPartial = NativeImageBacking.physicalIdentityRejectionCountForTest(3);
    final long physicalIdentityRejectMapping = NativeImageBacking.physicalIdentityRejectionCountForTest(4);
    final long physicalIdentityRejectBacking = NativeImageBacking.physicalIdentityRejectionCountForTest(5);
    final long physicalIdentityRejectExecution = NativeImageBacking.physicalIdentityRejectionCountForTest(6);
    final long targetColorAttempts = NativeImageBacking.targetColorAttemptsForTest();
    final long targetColorHits = NativeImageBacking.targetColorHitsForTest();
    final long targetColorMaterializations = NativeImageBacking.targetColorMaterializationsForTest();
    final long targetColorConvertedBytes = NativeImageBacking.targetColorConvertedBytesForTest();
    final long physicalVariantLookups = NativeImageBacking.physicalVariantLookupsForTest();
    final long physicalVariantHits = NativeImageBacking.physicalVariantHitsForTest();
    final long physicalVariantMisses = NativeImageBacking.physicalVariantMissesForTest();
    final long physicalVariantStores = NativeImageBacking.physicalVariantMaterializationsForTest();
    final long physicalVariantBytes = NativeImageBacking.physicalVariantBytesForTest();
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
      return ",targeted_jpeg_decodes=" + targetedJpegDecodes
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
          + ",physical_identity_reject_partial=" + physicalIdentityRejectPartial
          + ",physical_identity_reject_mapping=" + physicalIdentityRejectMapping
          + ",physical_identity_reject_backing=" + physicalIdentityRejectBacking
          + ",physical_identity_reject_execution=" + physicalIdentityRejectExecution
          + ",target_color_attempts=" + targetColorAttempts
          + ",target_color_hits=" + targetColorHits
          + ",target_color_materializations=" + targetColorMaterializations
          + ",target_color_converted_bytes=" + targetColorConvertedBytes
          + ",physical_variant_lookups=" + physicalVariantLookups
          + ",physical_variant_hits=" + physicalVariantHits
          + ",physical_variant_misses=" + physicalVariantMisses
          + ",physical_variant_stores=" + physicalVariantStores
          + ",physical_variant_bytes=" + physicalVariantBytes
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
