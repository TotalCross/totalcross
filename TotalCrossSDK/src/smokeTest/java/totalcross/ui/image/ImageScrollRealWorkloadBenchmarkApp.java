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
import totalcross.util.zip.CompressedStream;
import totalcross.util.zip.ZipEntry;
import totalcross.util.zip.ZipStream;

/** Real-corpus scrolling workload based on the customer-provided Tcsort layout. */
public class ImageScrollRealWorkloadBenchmarkApp extends MainWindow implements TimerListener {
  private static final int IMAGE_COUNT = 663;
  private static final int COLUMN_COUNT = 3;
  private static final int SCROLL_STEP = 120;
  private static final int SCROLL_DURATION_MILLIS = 3000;
  private static final int FRAME_INTERVAL_MILLIS = 16;
  private static final String MODE_BENCHMARK = "benchmark";
  private static final String MODE_RUN_ALL = "run-all";
  private static final String MODE_SELF_TEST = "self-test";
  private static final String MODE_AGGREGATE = "aggregate";
  private static final long SUITE_SEED = 73001L;
  private static final int SUITE_ROUNDS = 5;
  private static final int EXPECTED_SUITE_PROCESSES = 210;
  private static final long[] SUITE_MASKS = {
      0L, 1L, 2L, 4L, 8L, 16L, 32L, 64L, 128L, 256L, 512L, 1024L, 2048L,
      4096L, 8192L, 16384L, 32768L, 32799L, 40991L, 49183L, 57375L
  };
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
  private String datasetHashArgument;
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
  private int scrollDurationMillis = SCROLL_DURATION_MILLIS;
  private MemorySampler memory;

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
    memory = new MemorySampler();
    try {
      String mode = ImageRasterBenchmarkSupport.argument(getCommandLine(), "mode", MODE_BENCHMARK);
      if (MODE_RUN_ALL.equals(mode) || MODE_SELF_TEST.equals(mode) || MODE_AGGREGATE.equals(mode)) {
        runController(mode);
        return;
      }
      ImageRasterBenchmarkSupport.require(MODE_BENCHMARK.equals(mode),
          "unsupported mode: " + mode);
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
      datasetHashArgument = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "dataset-hash", "unavailable");
      scrollDurationMillis = ImageRasterBenchmarkSupport.integerArgument(
          getCommandLine(), "duration", SCROLL_DURATION_MILLIS);
      ImageRasterBenchmarkSupport.require(scrollDurationMillis > 0,
          "duration must be positive");
      targetColorProfile = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "target-color", "disabled");
      variantCacheProfile = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "variant-cache", "disabled");
      prefetchProfile = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "prefetch", "off");
      ImageRasterBenchmarkSupport.require(imageDir != null && imageDir.length() > 0,
          "missing --corpus=<dir>");
      ImageRasterBenchmarkSupport.ensureDirectory(outputDir);
      ImageRasterBenchmarkSupport.ensureDirectory(
          ImageRasterBenchmarkSupport.joinPath(outputDir, "runs"));
      runOutputDir = ImageRasterBenchmarkSupport.joinPath(
          ImageRasterBenchmarkSupport.joinPath(outputDir, "runs"), runName());
      ImageRasterBenchmarkSupport.ensureDirectory(runOutputDir);
      configureMask();
      recordMemory("process_start", Vm.getTimeStamp());
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
      recordMemory("before_prefetch", Vm.getTimeStamp());

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
            recordMemory("after_prefetch", Vm.getTimeStamp());
            prefetchComplete = true;
          }
        });
        return;
      }
      benchmarkReady = true;
      prefetchTimer = addTimer(50);
    } catch (Throwable failure) {
      String error = failure.toString();
      finishBenchmark(false, error);
      return;
    }
  }

  private void runController(String mode) {
    boolean pass = false;
    try {
      String corpus = ImageRasterBenchmarkSupport.argument(getCommandLine(), "corpus", null);
      String output = ImageRasterBenchmarkSupport.argument(getCommandLine(), "output", "results");
      ImageRasterBenchmarkSupport.require(corpus != null && corpus.length() > 0,
          "missing --corpus=<dir>");
      ImageRasterBenchmarkSupport.ensureDirectory(output);
      String datasetHash = validateCorpusAndHash(corpus);
      if (MODE_SELF_TEST.equals(mode)) {
        selfTest(corpus, output, datasetHash);
      } else if (MODE_AGGREGATE.equals(mode)) {
        aggregateResults(output);
        writeResultsZip(output);
      } else {
        selfTest(corpus, output + "/self-test", datasetHash);
        writeManifest(output, corpus, datasetHash);
        writeSuitePlan(output);
        runSuite(corpus, output, datasetHash);
        aggregateResults(output);
        writeResultsZip(output);
      }
      System.out.println("image-scroll benchmark complete,output=" + output);
      System.out.flush();
      pass = true;
    } catch (Throwable failure) {
      String message = failure.toString();
      System.out.println("image-scroll benchmark failed,error=" + message);
      System.out.flush();
    }
    exit(pass ? 0 : 1);
  }

  private void selfTest(String corpus, String output, String datasetHash) throws Exception {
    ImageRasterBenchmarkSupport.ensureDirectory(output);
    ImageOptimizationSettings.resetForTest();
    ImageOptimizationSettings.setMask(0);
    ImageRasterBenchmarkSupport.require(ImageOptimizationSettings.getMask() == 0
        && ImageOptimizationSettings.getEffectiveMask() == 0, "setMask(0) did not disable features");
    ImageOptimizationSettings.setMask(32799);
    ImageRasterBenchmarkSupport.require(ImageOptimizationSettings.getEffectiveMask() == 32799,
        "default mask did not resolve to 32799");
    ImageOptimizationSettings.setMask(57375);
    ImageRasterBenchmarkSupport.require(ImageOptimizationSettings.getEffectiveMask() == 57375,
        "matrix mask did not resolve to 57375");
    ImageRasterBenchmarkSupport.require(ImageOptimizationSettings.FEATURE_COUNT == 16,
        "expected 16 image optimization flags");
    int[] expectedFeatureIds = {
        ImageOptimizationSettings.DECODE_ZERO_COPY,
        ImageOptimizationSettings.RASTER_OPACITY_METADATA,
        ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS,
        ImageOptimizationSettings.RASTER_ROW_READBACK,
        ImageOptimizationSettings.RASTER_DIRECT_COLOR_MATERIALIZATION,
        ImageOptimizationSettings.STORAGE_RGB565,
        ImageOptimizationSettings.STORAGE_GRAY8,
        ImageOptimizationSettings.STORAGE_ARGB4444,
        ImageOptimizationSettings.CACHE_BYTE_BUDGET,
        ImageOptimizationSettings.CACHE_MEMORY_PRESSURE_EVICTION,
        ImageOptimizationSettings.GPU_DISCARD_CPU_BACKING,
        ImageOptimizationSettings.STORAGE_MMAP_LARGE_BACKINGS,
        ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
        ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE,
        ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING
    };
    for (int feature = 0; feature < expectedFeatureIds.length; feature++) {
      ImageRasterBenchmarkSupport.require(expectedFeatureIds[feature] == feature,
          "image optimization feature id changed at bit " + feature);
    }
    for (int i = 0; i < SUITE_MASKS.length; i++) {
      ImageRasterBenchmarkSupport.require(SUITE_MASKS[i] >= 0 && SUITE_MASKS[i] < (1L << 16),
          "invalid suite mask " + SUITE_MASKS[i]);
      for (int j = 0; j < i; j++) {
        ImageRasterBenchmarkSupport.require(SUITE_MASKS[i] != SUITE_MASKS[j],
            "duplicate suite mask " + SUITE_MASKS[i]);
      }
    }
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(output, "self-test-marker.txt"), "ok\n");
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(output, "dataset-manifest.json"),
        "{\"fileCount\":" + IMAGE_COUNT + ",\"datasetHash\":\"" + datasetHash + "\"}\n");
    String datasetManifest = readText(
        ImageRasterBenchmarkSupport.joinPath(output, "dataset-manifest.json"));
    ImageRasterBenchmarkSupport.require(datasetManifest.indexOf("\"fileCount\":663") >= 0
        && datasetManifest.indexOf("\"datasetHash\":\"" + datasetHash + "\"") >= 0,
        "self-test dataset manifest mismatch");

    String runOutput = ImageRasterBenchmarkSupport.joinPath(output, "smoke");
    String args = "--mode=" + MODE_BENCHMARK
        + " --image-optimization=0 --prefetch=off --run=0"
        + " --duration=100 --output=" + runOutput
        + " --corpus=" + corpus + " --dataset-hash=" + datasetHash;
    int childStatus = Vm.exec(benchmarkExecutable(), args, 0, true);
    ImageRasterBenchmarkSupport.require(childStatus == 0,
        "self-test benchmark exited with " + childStatus);
    String runDir = ImageRasterBenchmarkSupport.joinPath(
        ImageRasterBenchmarkSupport.joinPath(runOutput, "runs"),
        "mask-0-prefetch-off-run-0");
    ImageRasterBenchmarkSupport.require(new File(
        ImageRasterBenchmarkSupport.joinPath(runDir, "summary.json")).exists(),
        "self-test summary was not written");
    ImageRasterBenchmarkSupport.require(new File(
        ImageRasterBenchmarkSupport.joinPath(runDir, "frames.csv")).exists(),
        "self-test frames were not written");
    String environment = readText(ImageRasterBenchmarkSupport.joinPath(runOutput, "environment.json"));
    ImageRasterBenchmarkSupport.require(environment.indexOf("\"datasetFileCount\":663") >= 0,
        "self-test environment did not record 663 files");
    ImageRasterBenchmarkSupport.require(environment.indexOf("\"datasetHash\":\"" + datasetHash + "\"") >= 0,
        "self-test dataset hash mismatch");
    ImageRasterBenchmarkSupport.require(environment.indexOf("\"rendererBackend\":") >= 0,
        "self-test renderer metadata was not written");
    ImageRasterBenchmarkSupport.require(environment.indexOf("\"skiaSurfaceColorType\":") >= 0
        && environment.indexOf("\"endianness\":") >= 0,
        "self-test surface metadata was not written");
    writeTestZip(output);
  }

  private void writeTestZip(String output) throws Exception {
    String zipPath = ImageRasterBenchmarkSupport.joinPath(output, "self-test.zip");
    File archive = new File(zipPath, File.CREATE_EMPTY);
    ZipStream zip = new ZipStream(archive, CompressedStream.DEFLATE);
    byte[] expected = "image-scroll-self-test\n".getBytes("UTF-8");
    ZipEntry entry = new ZipEntry("self-test.txt");
    zip.putNextEntry(entry);
    zip.writeBytes(expected, 0, expected.length);
    zip.closeEntry();
    zip.close();
    archive.close();

    File inputFile = new File(zipPath, File.READ_ONLY);
    ZipStream input = new ZipStream(inputFile, CompressedStream.INFLATE);
    ZipEntry readEntry = input.getNextEntry();
    ImageRasterBenchmarkSupport.require(readEntry != null
        && "self-test.txt".equals(readEntry.getName()), "test ZIP entry missing");
    byte[] actual = new byte[expected.length];
    ImageRasterBenchmarkSupport.require(input.readBytes(actual, 0, actual.length) == actual.length,
        "test ZIP entry length mismatch");
    for (int i = 0; i < actual.length; i++) {
      ImageRasterBenchmarkSupport.require(actual[i] == expected[i], "test ZIP entry mismatch");
    }
    input.closeEntry();
    input.close();
    inputFile.close();
  }

  private String validateCorpusAndHash(String corpus) throws Exception {
    String[] paths = sortedCorpusPaths(corpus);
    ImageRasterBenchmarkSupport.require(paths.length == IMAGE_COUNT,
        "expected exactly " + IMAGE_COUNT + " corpus files but found " + paths.length);
    long hash = 0xcbf29ce484222325L;
    for (String path : paths) {
      String relative = path.startsWith(corpus) ? path.substring(corpus.length()) : path;
      while (relative.startsWith("/")) {
        relative = relative.substring(1);
      }
      byte[] name = relative.getBytes("UTF-8");
      hash = updateHash(hash, name, 0, name.length);
      hash = updateHash(hash, new byte[] { 0 }, 0, 1);
      File input = new File(path, File.READ_ONLY);
      byte[] buffer = new byte[16384];
      try {
        int count;
        while ((count = input.readBytes(buffer, 0, buffer.length)) > 0) {
          hash = updateHash(hash, buffer, 0, count);
        }
      } finally {
        input.close();
      }
    }
    return hexHash(hash);
  }

  private static long updateHash(long hash, byte[] bytes, int offset, int length) {
    for (int i = offset; i < offset + length; i++) {
      hash ^= bytes[i] & 0xffL;
      hash *= 0x100000001b3L;
    }
    return hash;
  }

  private static String hexHash(long hash) {
    StringBuilder result = new StringBuilder(16);
    for (int shift = 60; shift >= 0; shift -= 4) {
      int nibble = (int) ((hash >>> shift) & 0x0f);
      result.append((char) (nibble < 10 ? '0' + nibble : 'a' + nibble - 10));
    }
    return result.toString();
  }

  private void writeManifest(String output, String corpus, String datasetHash) throws Exception {
    StringBuilder json = new StringBuilder(2048);
    json.append("{\n")
        .append("  \"schemaVersion\":1,\n")
        .append("  \"benchmarkVersion\":\"1\",\n")
        .append("  \"fixture\":\"ImageScrollRealWorkloadBenchmarkApp\",\n")
        .append("  \"seed\":").append(SUITE_SEED).append(",\n")
        .append("  \"rounds\":").append(SUITE_ROUNDS).append(",\n")
        .append("  \"expectedProcessCount\":").append(EXPECTED_SUITE_PROCESSES).append(",\n")
        .append("  \"datasetFileCount\":").append(IMAGE_COUNT).append(",\n")
        .append("  \"columns\":").append(COLUMN_COUNT).append(",\n")
        .append("  \"corpus\":\"").append(escapeJson(corpus)).append("\",\n")
        .append("  \"datasetHash\":\"").append(datasetHash).append("\",\n")
        .append("  \"masks\":[");
    for (int i = 0; i < SUITE_MASKS.length; i++) {
      json.append(SUITE_MASKS[i]).append(i == SUITE_MASKS.length - 1 ? "]\n" : ",");
    }
    json.append("}\n");
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(output, "manifest.json"), json.toString());
  }

  private void writeSuitePlan(String output) throws Exception {
    StringBuilder plan = new StringBuilder(8192);
    plan.append("round\torder\trun\tmask\tprefetch\n");
    int order = 0;
    for (int round = 0; round < SUITE_ROUNDS; round++) {
      int[] combinations = shuffledCombinations(round);
      for (int combination : combinations) {
        long mask = SUITE_MASKS[combination / 2];
        String prefetch = combination % 2 == 0 ? "off" : "on";
        plan.append(round + 1).append('\t').append(order++).append('\t').append(round + 1)
            .append('\t').append(mask).append('\t').append(prefetch).append('\n');
      }
    }
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(output, "suite-plan.tsv"), plan.toString());
  }

  private static int[] shuffledCombinations(int round) {
    int[] combinations = new int[SUITE_MASKS.length * 2];
    for (int i = 0; i < combinations.length; i++) {
      combinations[i] = i;
    }
    long state = SUITE_SEED + 0x9e3779b97f4a7c15L * (round + 1);
    for (int i = combinations.length - 1; i > 0; i--) {
      state = state * 6364136223846793005L + 1442695040888963407L;
      int j = (int) ((state >>> 1) % (i + 1));
      int swap = combinations[i];
      combinations[i] = combinations[j];
      combinations[j] = swap;
    }
    return combinations;
  }

  private void runSuite(String corpus, String output, String datasetHash) throws Exception {
    int completed = 0;
    for (int round = 0; round < SUITE_ROUNDS; round++) {
      int[] combinations = shuffledCombinations(round);
      for (int combination : combinations) {
        long mask = SUITE_MASKS[combination / 2];
        String prefetch = combination % 2 == 0 ? "off" : "on";
        String args = "--mode=" + MODE_BENCHMARK
            + " --image-optimization=" + mask
            + " --prefetch=" + prefetch
            + " --run=" + (round + 1)
            + " --output=" + output
            + " --corpus=" + corpus
            + " --dataset-hash=" + datasetHash;
        int status = Vm.exec(benchmarkExecutable(), args, 0, true);
        ImageRasterBenchmarkSupport.require(status == 0,
            "benchmark process failed for mask=" + mask + ",prefetch=" + prefetch
                + ",run=" + (round + 1) + ",status=" + status);
        completed++;
        System.out.println("image-scroll benchmark progress=" + completed + "/"
            + EXPECTED_SUITE_PROCESSES);
      }
    }
    ImageRasterBenchmarkSupport.require(completed == EXPECTED_SUITE_PROCESSES,
        "suite completed " + completed + " processes");
  }

  private String benchmarkExecutable() throws Exception {
    String base = "ImageScrollRealWorkloadBenchmarkApp";
    if (Settings.WIN32.equals(Settings.platform)) {
      String withExtension = ImageRasterBenchmarkSupport.joinPath(Settings.appPath, base + ".exe");
      if (new File(withExtension).exists()) {
        return withExtension;
      }
    }
    String executable = ImageRasterBenchmarkSupport.joinPath(Settings.appPath, base);
    return Settings.WIN32.equals(Settings.platform) ? executable : "./" + base;
  }

  private void aggregateResults(String output) throws Exception {
    String runsDir = ImageRasterBenchmarkSupport.joinPath(output, "runs");
    String[] paths = File.listFiles(runsDir, true);
    RunRecord[] records = new RunRecord[EXPECTED_SUITE_PROCESSES];
    int count = 0;
    for (String path : paths) {
      if (!path.endsWith("/summary.json")) {
        continue;
      }
      ImageRasterBenchmarkSupport.require(count < records.length, "too many benchmark summaries");
      RunRecord record = RunRecord.read(path);
      ImageRasterBenchmarkSupport.require(record.status.equals("PASS"),
          "invalid run status for " + path + ": " + record.status);
      ImageRasterBenchmarkSupport.require(record.requestedMask == record.effectiveMask,
          "requested/effective mask mismatch for " + path);
      ImageRasterBenchmarkSupport.require(record.imageCount == IMAGE_COUNT && record.frameCount > 1,
          "incomplete benchmark summary for " + path);
      records[count++] = record;
    }
    ImageRasterBenchmarkSupport.require(count == EXPECTED_SUITE_PROCESSES,
        "expected " + EXPECTED_SUITE_PROCESSES + " summaries but found " + count);
    StringBuilder csv = new StringBuilder(32768);
    csv.append("run,prefetch,mask,status,frame_count,frame_p50_ms,frame_p90_ms,frame_p95_ms,frame_p99_ms,frame_max_ms,frames_over_16_67_ms,frames_over_33_3_ms,frames_over_50_ms,frames_over_100_ms,largest_stall_ms,largest_consecutive_over_33_3,prefetch_elapsed_ms,memory_peak_resident_bytes,baseline_scope,baseline_mask0_p50_ms,delta_p50_ms,baseline_mask0_p95_ms,delta_p95_ms\n");
    for (String prefetch : new String[] { "off", "on" }) {
      long baselineP50Sum = 0;
      long baselineP95Sum = 0;
      int baselineCount = 0;
      for (RunRecord record : records) {
        if (prefetch.equals(record.prefetch) && record.requestedMask == 0) {
          baselineP50Sum += record.p50;
          baselineP95Sum += record.p95;
          baselineCount++;
        }
      }
      ImageRasterBenchmarkSupport.require(baselineCount == SUITE_ROUNDS,
          "missing mask=0 baseline for prefetch=" + prefetch);
      long baselineP50 = baselineP50Sum / baselineCount;
      long baselineP95 = baselineP95Sum / baselineCount;
      for (RunRecord record : records) {
        if (!prefetch.equals(record.prefetch)) {
          continue;
        }
        csv.append(record.run).append(',').append(record.prefetch).append(',')
            .append(record.requestedMask).append(',').append(record.status).append(',')
            .append(record.frameCount).append(',').append(record.p50).append(',')
            .append(record.p90).append(',').append(record.p95).append(',').append(record.p99).append(',')
            .append(record.max).append(',').append(record.over16).append(',').append(record.over33).append(',')
            .append(record.over50).append(',').append(record.over100).append(',').append(record.stall).append(',')
            .append(record.consecutive).append(',').append(record.prefetchElapsed).append(',')
            .append(record.memoryPeak).append(",same-machine-same-prefetch-mask0,")
            .append(baselineP50).append(',').append(record.p50 - baselineP50).append(',')
            .append(baselineP95).append(',').append(record.p95 - baselineP95).append('\n');
      }
    }
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(output, "summary.csv"), csv.toString());
  }

  private void writeResultsZip(String output) throws Exception {
    String filename = "totalcross-image-benchmark-results-" + Vm.getTimeStamp() + ".zip";
    String zipPath = ImageRasterBenchmarkSupport.joinPath(output, filename);
    File archive = new File(zipPath, File.CREATE_EMPTY);
    ZipStream zip = new ZipStream(archive, CompressedStream.DEFLATE);
    String[] files = File.listFiles(output, true);
    for (String path : files) {
      if (path.endsWith("/") || path.equals(zipPath) || path.indexOf("/self-test/") >= 0
          || path.endsWith("/self-test.zip")) {
        continue;
      }
      String relative = path.startsWith(output) ? path.substring(output.length()) : path;
      while (relative.startsWith("/")) {
        relative = relative.substring(1);
      }
      addZipFile(zip, path, "results/" + relative);
    }
    zip.close();
    archive.close();
    System.out.println("image-scroll benchmark results ZIP=" + zipPath);
  }

  private static void addZipFile(ZipStream zip, String path, String entryName) throws Exception {
    File input = new File(path, File.READ_ONLY);
    ZipEntry entry = new ZipEntry(entryName);
    zip.putNextEntry(entry);
    byte[] buffer = new byte[16384];
    try {
      int count;
      while ((count = input.readBytes(buffer, 0, buffer.length)) > 0) {
        zip.writeBytes(buffer, 0, count);
      }
    } finally {
      input.close();
    }
    zip.closeEntry();
  }

  private static String readText(String path) throws Exception {
    File input = new File(path, File.READ_ONLY);
    try {
      int size = input.getSize();
      byte[] bytes = new byte[size];
      int offset = 0;
      while (offset < bytes.length) {
        int count = input.readBytes(bytes, offset, bytes.length - offset);
        if (count <= 0) {
          break;
        }
        offset += count;
      }
      return new String(bytes, 0, offset, "UTF-8");
    } finally {
      input.close();
    }
  }

  private static final class RunRecord {
    String status;
    String prefetch;
    long requestedMask;
    long effectiveMask;
    long run;
    long imageCount;
    long frameCount;
    long p50;
    long p90;
    long p95;
    long p99;
    long max;
    long over16;
    long over33;
    long over50;
    long over100;
    long stall;
    long consecutive;
    long prefetchElapsed;
    long memoryPeak;

    static RunRecord read(String path) throws Exception {
      String json = readText(path);
      RunRecord record = new RunRecord();
      record.status = jsonString(json, "status");
      record.prefetch = jsonString(json, "prefetch");
      record.requestedMask = jsonLong(json, "requestedMask");
      record.effectiveMask = jsonLong(json, "effectiveMask");
      record.run = jsonLong(json, "run");
      record.imageCount = jsonLong(json, "imageCount");
      record.frameCount = jsonLong(json, "frameCount");
      record.p50 = jsonLong(json, "frameTimeP50Ms");
      record.p90 = jsonLong(json, "frameTimeP90Ms");
      record.p95 = jsonLong(json, "frameTimeP95Ms");
      record.p99 = jsonLong(json, "frameTimeP99Ms");
      record.max = jsonLong(json, "frameTimeMaxMs");
      record.over16 = jsonLong(json, "framesOver16_67Ms");
      record.over33 = jsonLong(json, "framesOver33_3Ms");
      record.over50 = jsonLong(json, "framesOver50Ms");
      record.over100 = jsonLong(json, "framesOver100Ms");
      record.stall = jsonLong(json, "largestStallMs");
      record.consecutive = jsonLong(json, "largestConsecutiveOver33_3");
      record.prefetchElapsed = jsonLong(json, "prefetchElapsedMs");
      record.memoryPeak = jsonLong(json, "memoryPeakResidentBytes");
      return record;
    }

    private static String jsonString(String json, String key) {
      String token = "\"" + key + "\":\"";
      int start = json.indexOf(token);
      if (start < 0) {
        throw new IllegalStateException("missing JSON field " + key);
      }
      start += token.length();
      int end = json.indexOf('"', start);
      if (end < 0) {
        throw new IllegalStateException("unterminated JSON field " + key);
      }
      return json.substring(start, end);
    }

    private static long jsonLong(String json, String key) {
      String token = "\"" + key + "\":";
      int start = json.indexOf(token);
      if (start < 0) {
        throw new IllegalStateException("missing JSON field " + key);
      }
      start += token.length();
      int end = start;
      while (end < json.length() && "-0123456789".indexOf(json.charAt(end)) >= 0) {
        end++;
      }
      return Long.parseLong(json.substring(start, end));
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
      recordMemory("after_scroll", Vm.getTimeStamp());
      writeRunSummary(cold);
      finishBenchmark(true, "");
    } catch (Throwable failure) {
      String error = failure.toString();
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
    if (overallPass) {
      recordMemory("process_end", Vm.getTimeStamp());
    }
    long requestedMask = ImageOptimizationSettings.getMask();
    long effectiveMask = ImageOptimizationSettings.getEffectiveMask();
    if (requestedMask != effectiveMask) {
      overallPass = false;
      error = error.length() == 0 ? "INVALID_CONFIGURATION" : error + "_INVALID_CONFIGURATION";
    }
    String summary = "fixture=ImageScrollRealWorkloadBenchmarkApp,record=summary"
        + ",resolution=" + width + "x" + height
        + ",target_color_profile=" + String.valueOf(targetColorProfile)
        + ",variant_cache_profile=" + String.valueOf(variantCacheProfile)
        + ",prefetch_profile=" + String.valueOf(prefetchProfile)
        + ",requested_mask=" + requestedMask
        + ",effective_mask=" + effectiveMask
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
    recordMemory("before_scroll", elapsedStart);
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
      int target = sinceStart >= scrollDurationMillis
          ? endpoint
          : minimum + (int) ((long) (maximum - minimum) * sinceStart / scrollDurationMillis);
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
      if (memory != null) {
        memory.sampleIfDue(frameStart);
      }
      previousFrameStart = frameStart;
      if (scroll.sbV.getValue() == endpoint && sinceStart >= scrollDurationMillis) {
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
        + "  \"os\":\"" + escapeJson(Settings.platform) + "\",\n"
        + "  \"osVersion\":\"unavailable\",\n"
        + "  \"architecture\":\"unavailable\",\n"
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
        + "  \"datasetHash\":\"" + escapeJson(datasetHashArgument) + "\",\n"
        + "  \"columns\":" + COLUMN_COUNT + "\n"
        + "}\n";
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(outputDir, "environment.json"), json);
  }

  private void recordMemory(String name, long timestamp) {
    if (memory == null) {
      memory = new MemorySampler();
    }
    try {
      memory.record(name, timestamp);
    } catch (Throwable ignored) {
      memory = null;
    }
  }

  private void writeMemory() throws Exception {
    StringBuilder csv = new StringBuilder(2048);
    csv.append("checkpoint,elapsed_ms,current_resident_bytes,peak_resident_bytes,private_bytes,phys_footprint_bytes\n");
    int memoryCount = memory == null ? 0 : memory.count;
    for (int i = 0; i < memoryCount; i++) {
      csv.append(memory.names[i]).append(',').append(memory.elapsed[i]).append(',')
          .append(csvMetric(memory.current[i])).append(',').append(csvMetric(memory.peak[i])).append(',')
          .append(csvMetric(memory.privateBytes[i])).append(',').append(csvMetric(memory.physFootprint[i]))
          .append('\n');
    }
    csv.append("global_peak,0,").append(csvMetric(memory.globalPeak))
        .append(",unavailable,unavailable,unavailable\n");
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "memory.csv"), csv.toString());
  }

  private void writeTimeline() throws Exception {
    StringBuilder timeline = new StringBuilder(1024);
    timeline.append("event,elapsed_ms,value\n");
    int memoryCount = memory == null ? 0 : memory.count;
    for (int i = 0; i < memoryCount; i++) {
      timeline.append(memory.names[i]).append(',').append(memory.elapsed[i]).append(',')
          .append(csvMetric(memory.current[i])).append('\n');
    }
    ImageRasterBenchmarkSupport.writeUtf8(
        ImageRasterBenchmarkSupport.joinPath(runOutputDir, "timeline.csv"), timeline.toString());
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
        + "  \"uiBuildElapsedMs\":" + uiBuildElapsedMillis + ",\n"
        + "  \"prefetchElapsedMs\":" + prefetchElapsedMillis + ",\n"
        + "  \"memoryPeakResidentBytes\":" + (memory == null ? -1 : memory.globalPeak) + ",\n"
        + "  \"durationMs\":" + result.elapsed + ",\n"
        + "  \"frameCount\":" + result.frames + ",\n"
        + "  \"frameTimeP50Ms\":" + result.percentile(50) + ",\n"
        + "  \"frameTimeP90Ms\":" + result.percentile(90) + ",\n"
        + "  \"frameTimeP95Ms\":" + result.percentile(95) + ",\n"
        + "  \"frameTimeP99Ms\":" + result.percentile(99) + ",\n"
        + "  \"frameTimeMaxMs\":" + result.percentile(100) + ",\n"
        + "  \"framesOver16_67Ms\":" + result.countAtLeast(17) + ",\n"
        + "  \"framesOver33_3Ms\":" + result.countAtLeast(34) + ",\n"
        + "  \"framesOver50Ms\":" + result.countAtLeast(51) + ",\n"
        + "  \"framesOver100Ms\":" + result.countAtLeast(101) + ",\n"
        + "  \"largestStallMs\":" + result.percentile(100) + ",\n"
        + "  \"largestConsecutiveOver33_3\":" + result.maxConsecutiveAtLeast(34) + "\n"
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
