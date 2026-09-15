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
      ImageRasterBenchmarkSupport.require(imageDir != null && imageDir.length() > 0,
          "missing --corpus=<dir>");
      ImageRasterBenchmarkSupport.require(maskArgument != null && maskArgument.length() > 0,
          "missing --image-optimization=<mask>");
      ImageRasterBenchmarkSupport.require(scrollDurationNs > 0,
          "duration must be positive");
      ImageRasterBenchmarkSupport.require("off".equals(prefetchProfile)
          || "on".equals(prefetchProfile),
          "prefetch must be off or on");
      ImageRasterBenchmarkSupport.ensureDirectory(outputDir);
      ImageRasterBenchmarkSupport.ensureDirectory(
          ImageRasterBenchmarkSupport.joinPath(outputDir, "runs"));
      runOutputDir = ImageRasterBenchmarkSupport.joinPath(
          ImageRasterBenchmarkSupport.joinPath(outputDir, "runs"), runName());
      ImageRasterBenchmarkSupport.ensureDirectory(runOutputDir);
      requireBenchmarkResolution();
      configureMask();

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
        Image.resetImageOperationAccountingForBenchmarkTest();
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
      Image.resetImageOperationAccountingForBenchmarkTest();
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
        + "-prefetch-" + prefetchProfile + "-run-" + runNumber;
  }

  private boolean prefetchEnabled() {
    return "on".equals(prefetchProfile);
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
    Image.resetImageOperationAccountingForBenchmarkTest();
    long startNs = System.nanoTime();
    long[] frameTimesNs = new long[256];
    long[] frameElapsedNs = new long[256];
    int[] framePositions = new int[256];
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
      int before = scroll.sbV.getValue();
      if (target != before) {
        ImageRasterBenchmarkSupport.require(scroll.scrollContent(0, target - before, true),
            name + " stopped before reaching time-based target");
      }
      long paintTimeNs = paintFrameNs();
      long frameTimeNs = frames == 0
          ? paintTimeNs : Math.max(0, frameStartNs - previousFrameStartNs);
      if (frames == frameTimesNs.length) {
        int newLength = frameTimesNs.length * 2;
        frameTimesNs = Arrays.copyOf(frameTimesNs, newLength);
        frameElapsedNs = Arrays.copyOf(frameElapsedNs, newLength);
        framePositions = Arrays.copyOf(framePositions, newLength);
      }
      frameTimesNs[frames] = frameTimeNs;
      frameElapsedNs[frames] = elapsedNs;
      framePositions[frames] = scroll.sbV.getValue();
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
    long[] sortedFrameTimesNs = Arrays.copyOf(actualFrameTimesNs, frames);
    Arrays.sort(sortedFrameTimesNs);
    return new PassResult(name, forward, minimum, endpoint, maximum, elapsedNs, frames,
        actualFrameTimesNs, actualFrameElapsedNs, actualFramePositions, sortedFrameTimesNs,
        counters);
  }

  private long paintFrameNs() {
    long startNs = System.nanoTime();
    scroll.repaintNow();
    return Math.max(0, System.nanoTime() - startNs);
  }

  private void printPass(PassResult result) {
    System.out.println("fixture=ImageScrollRealWorkloadBenchmarkApp,record=pass"
        + ",resolution=" + width + "x" + height
        + ",prefetch_profile=" + prefetchProfile
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
    frames.append("frame_index,elapsed_ns,frame_time_ns,scroll_value\n");
    for (int i = 0; i < result.frames; i++) {
      frames.append(i).append(',').append(result.frameElapsedNs[i]).append(',')
          .append(result.frameTimesNs[i]).append(',').append(result.framePositions[i]).append('\n');
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
        + "  \"sdlDrawableWidth\":null,\n"
        + "  \"sdlDrawableHeight\":null,\n"
        + "  \"skiaSurfaceWidth\":null,\n"
        + "  \"skiaSurfaceHeight\":null,\n"
        + "  \"rendererBackend\":\"unavailable\",\n"
        + "  \"kN32SkColorType\":null,\n"
        + "  \"skiaSurfaceColorType\":null,\n"
        + "  \"skiaSurfaceAlphaType\":null,\n"
        + "  \"skiaSurfaceRowBytes\":null,\n"
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
      String status = (effectiveMask & (1L << feature)) == 0
          ? "DISABLED" : hits == 0 ? "NOT_EXERCISED" : "EXERCISED";
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
    final long[] sortedFrameTimesNs;
    final Counters counters;

    PassResult(String name, boolean forward, int minimum, int end, int maximum, long elapsedNs,
        int frames, long[] frameTimesNs, long[] frameElapsedNs, int[] framePositions,
        long[] sortedFrameTimesNs, Counters counters) {
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
      this.sortedFrameTimesNs = sortedFrameTimesNs;
      this.counters = counters;
    }

    long percentileNs(int percent) {
      if (percent <= 0) {
        return sortedFrameTimesNs[0];
      }
      if (percent >= 100) {
        return sortedFrameTimesNs[sortedFrameTimesNs.length - 1];
      }
      int index = (int) Math.ceil(sortedFrameTimesNs.length * percent / 100.0) - 1;
      return sortedFrameTimesNs[Math.max(0, Math.min(sortedFrameTimesNs.length - 1, index))];
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
    final long imageCreated = Image.imageCreatedCountForTest();
    final long targetedJpegDecodes = Image.targetedDecodeInvocationCountForTest();
    final long fullJpegDecodes = Image.fullDecodeInvocationCountForTest();
    final long decodeFinalBufferBytes = Image.decodeFinalBufferBytesForTest();
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

    String featureDetails() {
      StringBuilder details = new StringBuilder(1024);
      long enabledMask = ImageOptimizationSettings.getEffectiveMask();
      for (int feature = 0; feature < FEATURE_NAMES.length; feature++) {
        long hits = featureHits(feature);
        String status = (enabledMask & (1L << feature)) == 0
            ? "DISABLED" : hits == 0 ? "NOT_EXERCISED" : "EXERCISED";
        details.append(",feature_").append(FEATURE_NAMES[feature]).append("_hits=").append(hits)
            .append(",feature_").append(FEATURE_NAMES[feature]).append("_status=").append(status);
      }
      return details.toString();
    }
  }
}
