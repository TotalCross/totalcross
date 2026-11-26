// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.ui.MainWindow;
import totalcross.ui.Window;

/** Measures bounded repaint maintenance without imposing a timing threshold. */
class ImageOwnershipMaintenanceBenchmarkTest {
  private static final int SAMPLE_COUNT = 200;
  private static final int WARMUP_PAIRS = 20;
  private static final int REPAINTS_PER_SAMPLE = 64;
  private static final int POPULATED_OWNERS = 1510;

  @BeforeAll
  static void initializeUi() {
    new tc.simulator.Launcher();
    if (MainWindow.getMainWindow() == null) {
      new MainWindow();
    }
  }

  @Test
  void repaintMaintenanceSkipsNestedEntries() throws Exception {
    Image image = new Image(png());
    Object owner = new Object();
    ImageDrawingBridge.retainPersistentOwner(owner, image);
    Field repainting = Window.class.getDeclaredField("repaintActiveWindowsRunning");
    repainting.setAccessible(true);
    boolean previous = repainting.getBoolean(null);
    ImageDrawingBridge.startRegistryInspectionCountForTest();
    try {
      repainting.setBoolean(null, true);
      repaintCycles(1);
    } finally {
      repainting.setBoolean(null, previous);
    }
    long inspections = ImageDrawingBridge.stopRegistryInspectionCountForTest();
    ImageDrawingBridge.releasePersistentOwner(owner, image);
    assertEquals(0, inspections, "nested repaint entries must skip ownership maintenance");
  }

  @Test
  void repaintMaintenanceBenchmarkReportsAllFourConfigurations() throws Exception {
    ImageDrawingBridge.completePersistentOwnerSweepForTest();
    assertEquals(0, ImageDrawingBridge.persistentOwnerRelationCountForTest(),
        "benchmark starts with an empty ownership registry");

    ConfigurationResult empty = measureConfiguration();

    Image image = new Image(png());
    Object[] owners = new Object[POPULATED_OWNERS];
    for (int i = 0; i < owners.length; i++) {
      owners[i] = new Object();
      ImageDrawingBridge.retainPersistentOwner(owners[i], image);
    }
    ConfigurationResult populated;
    try {
      populated = measureConfiguration();
    } finally {
      ImageOwnershipMaintenanceBridge.setDisabledForTest(false);
      for (int i = owners.length - 1; i >= 0; i--) {
        ImageDrawingBridge.releasePersistentOwner(owners[i], image);
      }
    }

    assertEquals(0, empty.disabled.inspections);
    assertEquals(0, empty.enabled.inspections);
    assertEquals(0, populated.disabled.inspections);
    assertTrue(populated.enabled.inspections > 0,
        "the enabled repaint hook should inspect the populated registry");
    assertTrue(populated.enabled.inspections <= (long) SAMPLE_COUNT
        * PersistentImageOwnershipRegistry.MAX_SWEEP_RELATIONSHIPS,
        "each maintenance invocation must inspect no more than 32 relationships");

    String rawSamples = "{\"emptyDisabledNanos\":" + empty.disabled.rawSamplesJson()
        + ",\"emptyEnabledNanos\":" + empty.enabled.rawSamplesJson()
        + ",\"populatedDisabledNanos\":" + populated.disabled.rawSamplesJson()
        + ",\"populatedEnabledNanos\":" + populated.enabled.rawSamplesJson() + "}";
    String report = "{\"samplesPerConfiguration\":" + SAMPLE_COUNT + ",\"warmupPairs\":" + WARMUP_PAIRS
        + ",\"repaintsPerSample\":" + REPAINTS_PER_SAMPLE + ",\"populatedOwners\":" + POPULATED_OWNERS
        + ",\"empty\":{\"disabled\":" + empty.disabled.json() + ",\"enabled\":"
        + empty.enabled.json() + "},\"populated\":{\"disabled\":" + populated.disabled.json()
        + ",\"enabled\":" + populated.enabled.json() + "}}";
    Path reportPath = Path.of("build/reports/image-ownership-repaint-benchmark.json");
    Path rawPath = Path.of("build/reports/image-ownership-repaint-raw.json");
    Files.createDirectories(reportPath.getParent());
    Files.writeString(reportPath, report + System.lineSeparator(), StandardCharsets.UTF_8);
    Files.writeString(rawPath, rawSamples + System.lineSeparator(), StandardCharsets.UTF_8);
    System.out.println("imageOwnershipRepaintBenchmark=" + report);
    System.out.println("imageOwnershipRepaintBenchmarkFile=" + reportPath.toAbsolutePath());
    System.out.println("imageOwnershipRepaintRawFile=" + rawPath.toAbsolutePath());
  }

  private static ConfigurationResult measureConfiguration() {
    for (int i = 0; i < WARMUP_PAIRS; i++) {
      measureOne(i % 2 != 0);
      measureOne(i % 2 == 0);
    }
    long[] disabledSamples = new long[SAMPLE_COUNT];
    long[] enabledSamples = new long[SAMPLE_COUNT];
    long disabledInspections = 0;
    long enabledInspections = 0;
    for (int i = 0; i < SAMPLE_COUNT; i++) {
      Measurement first = measureOne(i % 2 != 0);
      Measurement second = measureOne(i % 2 == 0);
      Measurement disabled = i % 2 == 0 ? first : second;
      Measurement enabled = i % 2 == 0 ? second : first;
      disabledSamples[i] = disabled.nanos;
      enabledSamples[i] = enabled.nanos;
      disabledInspections += disabled.inspections;
      enabledInspections += enabled.inspections;
    }
    return new ConfigurationResult(new BenchmarkResult(disabledSamples, disabledInspections),
        new BenchmarkResult(enabledSamples, enabledInspections));
  }

  private static Measurement measureOne(boolean enabled) {
    ImageOwnershipMaintenanceBridge.setDisabledForTest(!enabled);
    ImageDrawingBridge.startRegistryInspectionCountForTest();
    long start = System.nanoTime();
    repaintCycles(1);
    long nanos = System.nanoTime() - start;
    long inspections = ImageDrawingBridge.stopRegistryInspectionCountForTest();
    return new Measurement(nanos, inspections);
  }

  private static void repaintCycles(int cycles) {
    for (int cycle = 0; cycle < cycles; cycle++) {
      for (int repaint = 0; repaint < REPAINTS_PER_SAMPLE; repaint++) {
        Window.repaintActiveWindows();
      }
    }
  }

  private static byte[] png() throws Exception {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, 0xFF204060);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(image, "png", output);
    return output.toByteArray();
  }

  private static final class BenchmarkResult {
    final long p50Nanos;
    final long p95Nanos;
    final long inspections;
    final long[] samples;

    BenchmarkResult(long[] samples, long inspections) {
      this.samples = samples;
      long[] sorted = samples.clone();
      Arrays.sort(sorted);
      p50Nanos = percentile(sorted, 0.50);
      p95Nanos = percentile(sorted, 0.95);
      this.inspections = inspections;
    }

    String json() {
      return "{\"p50Nanos\":" + p50Nanos + ",\"p95Nanos\":" + p95Nanos + ",\"inspections\":"
          + inspections + "}";
    }

    String rawSamplesJson() {
      StringBuilder result = new StringBuilder("[");
      for (int i = 0; i < samples.length; i++) {
        if (i > 0) {
          result.append(',');
        }
        result.append(samples[i]);
      }
      return result.append(']').toString();
    }

    private static long percentile(long[] sorted, double percentile) {
      int index = (int) Math.ceil(percentile * sorted.length) - 1;
      return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
    }
  }

  private static final class ConfigurationResult {
    final BenchmarkResult disabled;
    final BenchmarkResult enabled;

    ConfigurationResult(BenchmarkResult disabled, BenchmarkResult enabled) {
      this.disabled = disabled;
      this.enabled = enabled;
    }
  }

  private static final class Measurement {
    final long nanos;
    final long inspections;

    Measurement(long nanos, long inspections) {
      this.nanos = nanos;
      this.inspections = inspections;
    }
  }
}
