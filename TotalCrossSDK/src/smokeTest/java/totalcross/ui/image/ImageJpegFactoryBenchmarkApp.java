// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Focused benchmark for eager/reference and lazy explicit JPEG factory barriers. */
public class ImageJpegFactoryBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;
  private static final int REPEATED_DRAWS = 16;

  @Override
  public void initUI() {
    int samples = integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    boolean factoryOnlyZeroDecode = true;
    boolean firstDrawOneDecode = true;
    boolean repeatedDrawOneDecode = true;
    boolean overallPass = false;
    String error = "";

    try {
      require(samples > 0 && samples <= 200, "samples must be between 1 and 200");
      byte[] referenceBytes = Vm.getFile("image-abi/back3.jpg");
      require(referenceBytes != null && referenceBytes.length > 0, "JPEG fixture");
      Image target = new Image(240, 140);
      Graphics surface = target.getGraphics();
      require(surface != null, "benchmark target graphics");

      for (int warmup = 0; warmup < 3; warmup++) {
        runScenario("eager_reference", referenceBytes, surface, 0);
        runScenario("lazy_factory_only", referenceBytes, surface, 0);
        runScenario("lazy_first_draw", referenceBytes, surface, 1);
        runScenario("lazy_repeated_draws", referenceBytes, surface, REPEATED_DRAWS);
      }

      for (int sample = 1; sample <= samples; sample++) {
        ScenarioResult eager = runScenario("eager_reference", referenceBytes, surface, 0);
        ScenarioResult factoryOnly = runScenario("lazy_factory_only", referenceBytes, surface, 0);
        ScenarioResult firstDraw = runScenario("lazy_first_draw", referenceBytes, surface, 1);
        ScenarioResult repeated = runScenario("lazy_repeated_draws", referenceBytes, surface, REPEATED_DRAWS);
        factoryOnlyZeroDecode &= factoryOnly.decodeCount == 0 && factoryOnly.materializationCount == 0;
        firstDrawOneDecode &= firstDraw.decodeCount == 1 && firstDraw.materializationCount == 1;
        repeatedDrawOneDecode &= repeated.decodeCount == 1 && repeated.materializationCount == 1;
        printSample(sample, eager, factoryOnly, firstDraw, repeated);
      }
      overallPass = factoryOnlyZeroDecode && firstDrawOneDecode && repeatedDrawOneDecode;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    System.out.println("fixture=ImageJpegFactoryBenchmarkApp,samples=" + samples
        + ",factory_only_zero_decode=" + factoryOnlyZeroDecode
        + ",first_draw_one_decode=" + firstDrawOneDecode
        + ",repeated_draw_one_decode=" + repeatedDrawOneDecode
        + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private static ScenarioResult runScenario(String scenario, byte[] referenceBytes, Graphics surface,
      int drawCount) throws Exception {
    Image.resetImageOperationAccountingForTest();
    long start = Vm.getTimeStamp();
    if ("eager_reference".equals(scenario)) {
      Image eager = new Image(referenceBytes);
      eager.getPixels();
    } else {
      Image lazy = Image.getJpegBestFit("image-abi/back3.jpg", 200, 113);
      if (drawCount > 0) {
        for (int i = 0; i < drawCount; i++) {
          surface.drawImage(lazy, 0, 0, true);
        }
      }
    }
    long elapsed = Vm.getTimeStamp() - start;
    int decodeCount = Image.fullDecodeInvocationCountForTest() + Image.targetedDecodeInvocationCountForTest();
    return new ScenarioResult(elapsed, decodeCount, Image.materializationCountForTest());
  }

  private static void printSample(int sample, ScenarioResult eager, ScenarioResult factoryOnly,
      ScenarioResult firstDraw, ScenarioResult repeated) {
    System.out.println("sample=" + sample + ",eager_reference_ms=" + eager.elapsedMs
        + ",eager_reference_decode=" + eager.decodeCount
        + ",eager_reference_materialization=" + eager.materializationCount
        + ",lazy_factory_only_ms=" + factoryOnly.elapsedMs
        + ",lazy_factory_only_decode=" + factoryOnly.decodeCount
        + ",lazy_factory_only_materialization=" + factoryOnly.materializationCount
        + ",lazy_first_draw_ms=" + firstDraw.elapsedMs
        + ",lazy_first_draw_decode=" + firstDraw.decodeCount
        + ",lazy_first_draw_materialization=" + firstDraw.materializationCount
        + ",lazy_repeated_draws_ms=" + repeated.elapsedMs
        + ",lazy_repeated_draws_decode=" + repeated.decodeCount
        + ",lazy_repeated_draws_materialization=" + repeated.materializationCount);
    System.out.flush();
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static int integerArgument(String commandLine, String name, int fallback) {
    String prefix = "--" + name + "=";
    if (commandLine != null) {
      for (String part : commandLine.trim().split("\\s+")) {
        if (part.startsWith(prefix)) {
          return Integer.parseInt(part.substring(prefix.length()));
        }
      }
    }
    return fallback;
  }

  private static final class ScenarioResult {
    final long elapsedMs;
    final int decodeCount;
    final int materializationCount;

    ScenarioResult(long elapsedMs, int decodeCount, int materializationCount) {
      this.elapsedMs = elapsedMs;
      this.decodeCount = decodeCount;
      this.materializationCount = materializationCount;
    }
  }
}
