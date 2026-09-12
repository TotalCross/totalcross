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
import totalcross.ui.gfx.Graphics;

/** Real-corpus scrolling workload based on the customer-provided Tcsort layout. */
public class ImageScrollRealWorkloadBenchmarkApp extends MainWindow implements TimerListener {
  private static final int IMAGE_COUNT = 663;
  private static final int COLUMN_COUNT = 3;
  private static final int SCROLL_STEP = 120;
  private static final int SCROLL_DURATION_MILLIS = 3000;
  private static final int FRAME_INTERVAL_MILLIS = 16;
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
  private long uiBuildElapsedMillis;
  private String imageDir;
  private String maskArgument;
  private long runNumber;
  private String outputDir;
  private String runOutputDir;
  private String targetColorProfile;
  private String variantCacheProfile;
  private String prefetchProfile;
  private long prefetchElapsedMillis;
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
  private final MemorySampler memory = new MemorySampler();

  public ImageScrollRealWorkloadBenchmarkApp() {
    super("", Window.NO_BORDER);
    Flick.defaultShortestFlick = 200;
    Flick.defaultLongestFlick = 2500;
    Flick.defaultFlickAcceleration = 1.8;
    Settings.scrollDistanceOnMouseWheelMove = SCROLL_STEP;
    setDeviceTitle("image-scroll-real-workload");
    setUIStyle(Settings.ANDROID_UI);
  }

  @Override
  public void initUI() {
    super.initUI();
    try {
      imageDir = ImageRasterBenchmarkSupport.argument(getCommandLine(), "corpus", null);
      if (imageDir == null) {
        imageDir = ImageRasterBenchmarkSupport.argument(getCommandLine(), "image-dir", null);
      }
      maskArgument = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "image-optimization", null);
      runNumber = parseRunNumber(ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "run", "0"));
      outputDir = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "output", "benchmark-output");
      targetColorProfile = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "target-color", "disabled");
      variantCacheProfile = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "variant-cache", "disabled");
      prefetchProfile = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "prefetch", "off");
      ImageRasterBenchmarkSupport.require(imageDir != null && imageDir.length() > 0,
          "missing --corpus=<dir>");
      ImageRasterBenchmarkSupport.ensureDirectory(outputDir);
      runOutputDir = ImageRasterBenchmarkSupport.joinPath(outputDir, runName());
      ImageRasterBenchmarkSupport.ensureDirectory(runOutputDir);
      configureMask();
      memory.record("process_start", Vm.getTimeStamp());
      ImageRasterBenchmarkSupport.require("disabled".equals(targetColorProfile)
          || "enabled".equals(targetColorProfile),
          "target-color must be disabled or enabled");
      ImageRasterBenchmarkSupport.require("disabled".equals(variantCacheProfile)
          || "enabled".equals(variantCacheProfile),
          "variant-cache must be disabled or enabled");
      ImageRasterBenchmarkSupport.require("off".equals(prefetchProfile)
          || "on".equals(prefetchProfile)
          || "disabled".equals(prefetchProfile)
          || "all".equals(prefetchProfile),
          "prefetch must be off or on");

      long buildStart = Vm.getTimeStamp();
      String[] imagePaths = sortedCorpusPaths(imageDir);
      ImageRasterBenchmarkSupport.require(imagePaths.length == IMAGE_COUNT,
          "expected exactly " + IMAGE_COUNT + " corpus files but found " + imagePaths.length);
      buildUi(imagePaths);
      uiBuildElapsedMillis = Vm.getTimeStamp() - buildStart;
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
      memory.record("before_prefetch", Vm.getTimeStamp());

      if (prefetchEnabled()) {
        Image.resetImageOperationAccountingForBenchmarkTest();
        ImagePreparation.resetAccountingForTest();
        final long prefetchStart = Vm.getTimeStamp();
        addTimerListener(this);
        prefetchTimer = addTimer(10);
        scroll.prepareForDisplay(new Runnable() {
          @Override
          public void run() {
            prefetchElapsedMillis = Vm.getTimeStamp() - prefetchStart;
            capturePrefetchAccounting();
            memory.record("after_prefetch", Vm.getTimeStamp());
            prefetchComplete = true;
          }
        });
        return;
      }
      benchmarkReady = true;
      prefetchTimer = addTimer(50);
    } catch (Throwable failure) {
      String error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_').replace(',', '_');
      finishBenchmark(false, error);
      return;
    }
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
      memory.record("after_scroll", Vm.getTimeStamp());
      writeRunSummary(cold);
      finishBenchmark(true, "");
    } catch (Throwable failure) {
      String error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_').replace(',', '_');
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
    memory.record("process_end", Vm.getTimeStamp());
    String summary = "fixture=ImageScrollRealWorkloadBenchmarkApp,record=summary"
        + ",resolution=" + width + "x" + height
        + ",target_color_profile=" + String.valueOf(targetColorProfile)
        + ",variant_cache_profile=" + String.valueOf(variantCacheProfile)
        + ",prefetch_profile=" + String.valueOf(prefetchProfile)
        + ",requested_mask=" + (maskArgument == null ? "default" : maskArgument)
        + ",effective_mask=" + ImageOptimizationSettings.getEffectiveMask()
        + ",run=" + runNumber
        + ",output_dir=" + String.valueOf(runOutputDir)
        + ",image_dir=" + String.valueOf(imageDir)
        + ",image_count=" + IMAGE_COUNT + ",rows=" + rowCount
        + ",image_controls=" + imageControlCount + ",tile_logical=" + tileWidth
        + ",ui_build_elapsed_ms=" + uiBuildElapsedMillis
        + ",prefetch_elapsed_ms=" + prefetchElapsedMillis
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
      System.out.println("benchmark_output_error=" + outputFailure.getClass().getName());
    }
    exit(overallPass ? 0 : 1);
  }

  @Override
  public void timerTriggered(TimerEvent event) {
    if (prefetchComplete || benchmarkReady) {
      executeBenchmark();
    }
  }

  private void configureProfile() {
    if (maskArgument == null) {
      ImageRasterBenchmarkSupport.configureApplicationRasterFeatures("post-enabled",
          "enabled".equals(targetColorProfile), "enabled".equals(variantCacheProfile));
    }
  }

  private void configureMask() {
    ImageOptimizationSettings.resetForTest();
    if (maskArgument != null) {
      try {
        ImageOptimizationSettings.setMask(Long.parseLong(maskArgument));
      } catch (NumberFormatException error) {
        throw new IllegalArgumentException("image-optimization must be decimal: " + maskArgument);
      }
    }
    configureProfile();
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
    return "on".equals(prefetchProfile) || "all".equals(prefetchProfile);
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
    long elapsedStart = Vm.getTimeStamp();
    memory.record("before_scroll", elapsedStart);
    int[] frameTimes = new int[256];
    long[] frameElapsed = new long[256];
    int[] framePositions = new int[256];
    int frames = 0;
    long previousFrameStart = elapsedStart;
    while (true) {
      long frameStart = Vm.getTimeStamp();
      long sinceStart = frameStart - elapsedStart;
      if (frames > 0 && sinceStart < (long) frames * FRAME_INTERVAL_MILLIS) {
        Vm.sleep((int) Math.min(4, (long) frames * FRAME_INTERVAL_MILLIS - sinceStart));
        continue;
      }
      int target = sinceStart >= SCROLL_DURATION_MILLIS
          ? endpoint
          : minimum + (int) ((long) (maximum - minimum) * sinceStart / SCROLL_DURATION_MILLIS);
      int before = scroll.sbV.getValue();
      if (target != before) {
        ImageRasterBenchmarkSupport.require(scroll.scrollContent(0, target - before, true),
            name + " stopped before reaching time-based target");
      }
      int paintMillis = paintFrame();
      int frameMillis = frames == 0
          ? paintMillis : (int) Math.max(0, frameStart - previousFrameStart);
      if (frames == frameTimes.length) {
        int newLength = frameTimes.length * 2;
        frameTimes = Arrays.copyOf(frameTimes, newLength);
        frameElapsed = Arrays.copyOf(frameElapsed, newLength);
        framePositions = Arrays.copyOf(framePositions, newLength);
      }
      frameTimes[frames] = frameMillis;
      frameElapsed[frames] = sinceStart;
      framePositions[frames] = scroll.sbV.getValue();
      frames++;
      memory.sampleIfDue(frameStart);
      previousFrameStart = frameStart;
      if (scroll.sbV.getValue() == endpoint && sinceStart >= SCROLL_DURATION_MILLIS) {
        break;
      }
    }
    ImageRasterBenchmarkSupport.require(scroll.sbV.getValue() == endpoint,
        name + " did not reach the full scrollbar extent");
    ImageRasterBenchmarkSupport.require(frames > 1,
        name + " did not traverse multiple frames");
    long elapsed = Vm.getTimeStamp() - elapsedStart;
    Counters counters = Counters.capture();
    int[] actualFrameTimes = Arrays.copyOf(frameTimes, frames);
    long[] actualFrameElapsed = Arrays.copyOf(frameElapsed, frames);
    int[] actualFramePositions = Arrays.copyOf(framePositions, frames);
    int[] sortedFrameTimes = Arrays.copyOf(actualFrameTimes, frames);
    Arrays.sort(sortedFrameTimes);
    return new PassResult(name, forward, minimum, endpoint, maximum, elapsed, frames,
        actualFrameTimes, actualFrameElapsed, actualFramePositions, sortedFrameTimes, counters);
  }

  private int paintFrame() {
    long start = Vm.getTimeStamp();
    scroll.repaintNow();
    return (int) Math.max(0, Vm.getTimeStamp() - start);
  }

  private void printPass(PassResult result) {
    System.out.println("fixture=ImageScrollRealWorkloadBenchmarkApp,record=pass"
        + ",resolution=" + width + "x" + height
        + ",target_color_profile=" + targetColorProfile
        + ",variant_cache_profile=" + variantCacheProfile
        + ",prefetch_profile=" + prefetchProfile
        + ",requested_mask=" + (maskArgument == null ? "default" : maskArgument)
        + ",effective_mask=" + ImageOptimizationSettings.getEffectiveMask()
        + ",run=" + runNumber
        + ",image_count=" + IMAGE_COUNT + ",rows=" + rowCount
        + ",image_controls=" + imageControlCount + ",tile_logical=" + tileWidth
        + ",ui_build_elapsed_ms=" + uiBuildElapsedMillis
        + ",prefetch_elapsed_ms=" + prefetchElapsedMillis
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
        + ",elapsed_total_ms=" + result.elapsed + ",frames=" + result.frames
        + ",frame_time_min_ms=" + result.percentile(0)
        + ",frame_time_p90_ms=" + result.percentile(90)
        + ",frame_time_p50_ms=" + result.percentile(50)
        + ",frame_time_p95_ms=" + result.percentile(95)
        + ",frame_time_p99_ms=" + result.percentile(99)
        + ",frame_time_max_ms=" + result.percentile(100)
        + ",frames_over_16_67_ms=" + result.countAtLeast(17)
        + ",frames_over_33_3_ms=" + result.countAtLeast(34)
        + ",frames_over_50_ms=" + result.countAtLeast(51)
        + ",frames_over_100_ms=" + result.countAtLeast(101)
        + ",largest_stall_ms=" + result.percentile(100)
        + ",largest_consecutive_over_33_3=" + result.maxConsecutiveAtLeast(34)
        + result.counters.details() + result.counters.featureDetails());
    System.out.flush();
  }

  private void writeRunFrames(PassResult result) throws Exception {
    StringBuilder frames = new StringBuilder(4096);
    frames.append("frame_index,elapsed_ms,frame_time_ms,scroll_value\n");
    for (int i = 0; i < result.frames; i++) {
      frames.append(i).append(',').append(result.frameElapsed[i]).append(',')
          .append(result.frameTimes[i]).append(',').append(result.framePositions[i]).append('\n');
    }
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "frames.csv"), frames.toString());
  }

  private void writeEnvironment() throws Exception {
    long surfaceColorType = Image.nativeMetricForBenchmarkTest(6);
    long surfaceAlphaType = Image.nativeMetricForBenchmarkTest(7);
    long surfaceRowBytes = Image.nativeMetricForBenchmarkTest(8);
    long surfaceWidth = Image.nativeMetricForBenchmarkTest(9);
    long surfaceHeight = Image.nativeMetricForBenchmarkTest(10);
    long n32ColorType = Image.nativeMetricForBenchmarkTest(11);
    String json = "{\n"
        + "  \"os\":\"" + escapeJson(property("os.name")) + "\",\n"
        + "  \"osVersion\":\"" + escapeJson(property("os.version")) + "\",\n"
        + "  \"architecture\":\"" + escapeJson(property("os.arch")) + "\",\n"
        + "  \"endianness\":\"" + endianness(Image.nativeMetricForBenchmarkTest(4)) + "\",\n"
        + "  \"cpu\":\"unavailable\",\n"
        + "  \"gpu\":\"unavailable\",\n"
        + "  \"ramTotalBytes\":null,\n"
        + "  \"windowLogicalWidth\":" + width + ",\n"
        + "  \"windowLogicalHeight\":" + height + ",\n"
        + "  \"windowPhysicalWidth\":" + jsonMetric(surfaceWidth) + ",\n"
        + "  \"windowPhysicalHeight\":" + jsonMetric(surfaceHeight) + ",\n"
        + "  \"totalCrossScreenWidth\":" + Settings.screenWidth + ",\n"
        + "  \"totalCrossScreenHeight\":" + Settings.screenHeight + ",\n"
        + "  \"density\":" + Settings.screenDensity + ",\n"
        + "  \"systemDisplayScale\":" + Graphics.getMainWindowContentScale() + ",\n"
        + "  \"refreshRate\":null,\n"
        + "  \"sdlDrawableWidth\":" + jsonMetric(surfaceWidth) + ",\n"
        + "  \"sdlDrawableHeight\":" + jsonMetric(surfaceHeight) + ",\n"
        + "  \"skiaSurfaceWidth\":" + jsonMetric(surfaceWidth) + ",\n"
        + "  \"skiaSurfaceHeight\":" + jsonMetric(surfaceHeight) + ",\n"
        + "  \"rendererBackend\":\"" + rendererBackend() + "\",\n"
        + "  \"kN32SkColorType\":" + jsonMetric(n32ColorType) + ",\n"
        + "  \"skiaSurfaceColorType\":" + jsonMetric(surfaceColorType) + ",\n"
        + "  \"skiaSurfaceAlphaType\":" + jsonMetric(surfaceAlphaType) + ",\n"
        + "  \"skiaSurfaceRowBytes\":" + jsonMetric(surfaceRowBytes) + ",\n"
        + "  \"totalCrossVersion\":\"" + escapeJson(Settings.versionStr) + "\",\n"
        + "  \"sdkVersion\":\"" + escapeJson(Settings.versionStr) + "\",\n"
        + "  \"benchmarkVersion\":\"1\",\n"
        + "  \"datasetFileCount\":" + IMAGE_COUNT + ",\n"
        + "  \"datasetHash\":\"unavailable\",\n"
        + "  \"columns\":" + COLUMN_COUNT + "\n"
        + "}\n";
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "environment.json"), json);
  }

  private void writeMemory() throws Exception {
    StringBuilder csv = new StringBuilder(2048);
    csv.append("checkpoint,elapsed_ms,current_resident_bytes,peak_resident_bytes,private_bytes,phys_footprint_bytes\n");
    for (int i = 0; i < memory.count; i++) {
      csv.append(memory.names[i]).append(',').append(memory.elapsed[i]).append(',')
          .append(csvMetric(memory.current[i])).append(',').append(csvMetric(memory.peak[i])).append(',')
          .append(csvMetric(memory.privateBytes[i])).append(',').append(csvMetric(memory.physFootprint[i]))
          .append('\n');
    }
    csv.append("global_peak,0,").append(csvMetric(memory.globalPeak)).append("\n");
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "memory.csv"), csv.toString());
  }

  private void writeTimeline() throws Exception {
    StringBuilder timeline = new StringBuilder(1024);
    timeline.append("event,elapsed_ms,value\n");
    for (int i = 0; i < memory.count; i++) {
      timeline.append(memory.names[i]).append(',').append(memory.elapsed[i]).append(',')
          .append(csvMetric(memory.current[i])).append('\n');
    }
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "timeline.csv"), timeline.toString());
  }

  private static String property(String name) {
    try {
      String value = System.getProperty(name);
      return value == null || value.length() == 0 ? "unavailable" : value;
    } catch (Throwable ignored) {
      return "unavailable";
    }
  }

  private static String endianness(long value) {
    return value == 1 ? "little" : value == 2 ? "big" : "unavailable";
  }

  private static String rendererBackend() {
    return Image.nativeMetricForBenchmarkTest(5) == 1 ? "skia" : "unavailable";
  }

  private static String jsonMetric(long value) {
    return value < 0 ? "null" : String.valueOf(value);
  }

  private static String csvMetric(long value) {
    return value < 0 ? "unavailable" : String.valueOf(value);
  }

  private void writeRunSummary(PassResult result) throws Exception {
    String json = "{\n"
        + "  \"fixture\":\"ImageScrollRealWorkloadBenchmarkApp\",\n"
        + "  \"run\":" + runNumber + ",\n"
        + "  \"corpus\":\"" + escapeJson(imageDir) + "\",\n"
        + "  \"imageCount\":" + imageControlCount + ",\n"
        + "  \"columns\":" + COLUMN_COUNT + ",\n"
        + "  \"prefetch\":\"" + prefetchProfile + "\",\n"
        + "  \"requestedMask\":" + (maskArgument == null
            ? ImageOptimizationSettings.getMask() : maskArgument) + ",\n"
        + "  \"effectiveMask\":" + ImageOptimizationSettings.getEffectiveMask() + ",\n"
        + "  \"durationMs\":" + result.elapsed + ",\n"
        + "  \"frameCount\":" + result.frames + "\n"
        + "}\n";
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "summary.json"), json);
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
    final long elapsed;
    final int frames;
    final int[] frameTimes;
    final long[] frameElapsed;
    final int[] framePositions;
    final int[] sortedFrameTimes;
    final Counters counters;

    PassResult(String name, boolean forward, int minimum, int end, int maximum, long elapsed,
        int frames, int[] frameTimes, long[] frameElapsed, int[] framePositions,
        int[] sortedFrameTimes, Counters counters) {
      this.name = name;
      this.forward = forward;
      this.minimum = minimum;
      this.start = forward ? minimum : maximum;
      this.end = end;
      this.maximum = maximum;
      this.elapsed = elapsed;
      this.frames = frames;
      this.frameTimes = frameTimes;
      this.frameElapsed = frameElapsed;
      this.framePositions = framePositions;
      this.sortedFrameTimes = sortedFrameTimes;
      this.counters = counters;
    }

    int percentile(int percent) {
      if (percent <= 0) {
        return sortedFrameTimes[0];
      }
      if (percent >= 100) {
        return sortedFrameTimes[sortedFrameTimes.length - 1];
      }
      int index = (int) Math.ceil(sortedFrameTimes.length * percent / 100.0) - 1;
      return sortedFrameTimes[Math.max(0, Math.min(sortedFrameTimes.length - 1, index))];
    }

    int countAtLeast(int threshold) {
      int count = 0;
      for (int time : sortedFrameTimes) {
        if (time >= threshold) {
          count++;
        }
      }
      return count;
    }

    int maxConsecutiveAtLeast(int threshold) {
      int maximum = 0;
      int current = 0;
      for (int time : frameTimes) {
        current = time >= threshold ? current + 1 : 0;
        maximum = Math.max(maximum, current);
      }
      return maximum;
    }
  }

  private static final class MemorySampler {
    private static final int MAX_CHECKPOINTS = 64;
    final String[] names = new String[MAX_CHECKPOINTS];
    final long[] elapsed = new long[MAX_CHECKPOINTS];
    final long[] current = new long[MAX_CHECKPOINTS];
    final long[] peak = new long[MAX_CHECKPOINTS];
    final long[] privateBytes = new long[MAX_CHECKPOINTS];
    final long[] physFootprint = new long[MAX_CHECKPOINTS];
    int count;
    long globalPeak = -1;
    long nextSampleMillis;
    long originMillis = -1;

    void record(String name, long timestamp) {
      long currentBytes = Image.nativeMetricForBenchmarkTest(0);
      long peakBytes = Image.nativeMetricForBenchmarkTest(1);
      if (originMillis < 0) {
        originMillis = timestamp;
      }
      if (count < MAX_CHECKPOINTS) {
        names[count] = name;
        elapsed[count] = timestamp - originMillis;
        current[count] = currentBytes;
        peak[count] = peakBytes;
        privateBytes[count] = Image.nativeMetricForBenchmarkTest(2);
        physFootprint[count] = Image.nativeMetricForBenchmarkTest(3);
        count++;
      }
      if (currentBytes > globalPeak) {
        globalPeak = currentBytes;
      }
      if (nextSampleMillis == 0) {
        nextSampleMillis = timestamp + 250;
      }
    }

    void sampleIfDue(long timestamp) {
      if (timestamp >= nextSampleMillis) {
        record("during_scroll", timestamp);
        nextSampleMillis = timestamp + 250;
      }
    }
  }

  private static final class Counters {
    final long targetedJpegDecodes = Image.targetedDecodeInvocationCountForTest();
    final long fullJpegDecodes = Image.fullDecodeInvocationCountForTest();
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

    static Counters capture() {
      return new Counters();
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
