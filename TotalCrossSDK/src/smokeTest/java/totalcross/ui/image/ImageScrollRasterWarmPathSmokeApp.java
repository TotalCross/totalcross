// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.sys.runtime.ImageRasterSmokeTestSupport;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Warm-path comparison for materialized and deferred Image copies. */
public final class ImageScrollRasterWarmPathSmokeApp extends MainWindow {
  private static final int IMAGE_SIZE = 16;
  private static final int SOURCE_SIZE = IMAGE_SIZE + 2;
  private static final int DESTINATION_SIZE = 36;
  private static final int DESTINATION_X = 5;
  private static final int DESTINATION_Y = 5;
  private static final int SAMPLE_COUNT = 5;
  private static final int ITERATIONS_PER_SAMPLE = 1200;
  private static final int WARMUP_COUNT = 3;
  private static final int BACKGROUND = 0xFF203040;

  @Override
  public void initUI() {
    boolean fullParity = false;
    boolean partialParity = false;
    boolean deferredCopyUsesDirectPath = false;
    String error = "";
    try {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
      Image.resetImageOperationAccountingForTest();
      ImageRasterFeatureBridge.resetDrawAccountingForTest();
      LaneResult[] full = measureSet(false);
      LaneResult[] partial = measureSet(true);
      fullParity = full[0].pixelHash == full[1].pixelHash
          && full[1].pixelHash == full[2].pixelHash;
      partialParity = partial[0].pixelHash == partial[1].pixelHash
          && partial[1].pixelHash == partial[2].pixelHash;
      deferredCopyUsesDirectPath = full[2].directCopyHits > 0
          && partial[2].directCopyHits > 0
          && full[2].genericGeometryDraws == 0 && partial[2].genericGeometryDraws == 0;
      require(fullParity && partialParity, "warm path pixel parity");
      require(deferredCopyUsesDirectPath, "deferred copy missed direct identity path");
      for (LaneResult result : full) {
        printResult(result);
      }
      for (LaneResult result : partial) {
        printResult(result);
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean overallPass = fullParity && partialParity && deferredCopyUsesDirectPath;
    System.out.println("fixture=ImageScrollRasterWarmPathSmokeApp,fullVisibleParity=" + fullParity
        + ",partialClipParity=" + partialParity + ",deferredCopyUsesDirectPath="
        + deferredCopyUsesDirectPath + ",overallPass=" + overallPass + ",error=" + error);
    exit(overallPass ? 0 : 1);
  }

  private static LaneResult[] measureSet(boolean partialClip) throws Exception {
    LaneContext[] lanes = {
        new LaneContext("materialized-copy", partialClip, materializedImage(), false),
        new LaneContext("deferred-drawImage", partialClip, deferredImage(), true),
        new LaneContext("deferred-copyRect", partialClip, deferredImage(), false)
    };
    for (LaneContext lane : lanes) {
      for (int i = 0; i < WARMUP_COUNT; i++) {
        lane.drawOnce();
      }
    }
    LaneResult[] results = new LaneResult[lanes.length];
    for (int i = 0; i < lanes.length; i++) {
      results[i] = measure(lanes[i]);
    }
    return results;
  }

  private static LaneResult measure(LaneContext lane) {
    int planCacheHitsBefore = Image.imageDrawPlanCacheHitCountForTest();
    int materializationsBefore = Image.nativeGeometryMaterializationCountForTest();
    int decodesBefore = Image.fullDecodeInvocationCountForTest()
        + Image.targetedDecodeInvocationCountForTest();
    int identityHitsBefore = Image.physicalIdentityHitCountForTest();
    int identityAttemptsBefore = ImageRasterFeatureBridge.identityAttemptsForTest;
    int directCopyHitsBefore = ImageRasterFeatureBridge.physicalCopyHitsForTest;
    int genericDrawsBefore = ImageRasterFeatureBridge.genericGeometryDrawsForTest;
    int smoothDrawsBefore = ImageRasterFeatureBridge.smoothResampleDrawsForTest;
    int planCopyAttemptsBefore = ImageRasterFeatureBridge.copyRectPlanAttemptsForTest;
    int[] elapsedMillis = new int[SAMPLE_COUNT];
    for (int sample = 0; sample < SAMPLE_COUNT; sample++) {
      int start = Vm.getTimeStamp();
      for (int iteration = 0; iteration < ITERATIONS_PER_SAMPLE; iteration++) {
        lane.drawOnce();
      }
      elapsedMillis[sample] = Vm.getTimeStamp() - start;
    }
    return new LaneResult(lane.name, lane.partialClip, pixelHash(lane.destination),
        Image.nativeGeometryMaterializationCountForTest() - materializationsBefore,
        Image.fullDecodeInvocationCountForTest() + Image.targetedDecodeInvocationCountForTest() - decodesBefore,
        Image.imageDrawPlanCacheHitCountForTest() - planCacheHitsBefore,
        ImageRasterFeatureBridge.identityAttemptsForTest - identityAttemptsBefore,
        Image.physicalIdentityHitCountForTest() - identityHitsBefore,
        ImageRasterFeatureBridge.physicalCopyHitsForTest - directCopyHitsBefore,
        ImageRasterFeatureBridge.genericGeometryDrawsForTest - genericDrawsBefore,
        ImageRasterFeatureBridge.smoothResampleDrawsForTest - smoothDrawsBefore,
        ImageRasterFeatureBridge.copyRectPlanAttemptsForTest - planCopyAttemptsBefore,
        elapsedMillis);
  }

  private static Image materializedImage() throws ImageException {
    Image image = new Image(IMAGE_SIZE, IMAGE_SIZE);
    require(image.getGraphics().setRGB(pixels(IMAGE_SIZE, 1, 1), 0, 0, 0, IMAGE_SIZE, IMAGE_SIZE)
        == IMAGE_SIZE * IMAGE_SIZE, "materialized source initialization");
    return image;
  }

  private static Image deferredImage() throws ImageException {
    Image image = new Image(SOURCE_SIZE, SOURCE_SIZE);
    require(image.getGraphics().setRGB(pixels(SOURCE_SIZE), 0, 0, 0, SOURCE_SIZE, SOURCE_SIZE)
        == SOURCE_SIZE * SOURCE_SIZE, "deferred source initialization");
    return image.getClippedInstance(1, 1, IMAGE_SIZE, IMAGE_SIZE);
  }

  private static int[] pixels(int size) {
    return pixels(size, 0, 0);
  }

  private static int[] pixels(int size, int originX, int originY) {
    int[] pixels = new int[size * size];
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        int sourceX = x + originX;
        int sourceY = y + originY;
        pixels[y * size + x] = 0xFF000000 | (sourceX * 13 << 16)
            | (sourceY * 11 << 8) | (sourceX * 3 + sourceY * 5);
      }
    }
    return pixels;
  }

  private static int pixelHash(Image image) {
    int[] pixels = image.getPixels();
    int hash = 1;
    for (int pixel : pixels) {
      hash = 31 * hash + pixel;
    }
    return hash;
  }

  private static void printResult(LaneResult result) {
    System.out.println("fixture=ImageScrollRasterWarmPathSmokeApp,lane=" + result.lane
        + ",clip=" + (result.partialClip ? "partial" : "full") + ",pixelHash=" + result.pixelHash
        + ",materializationCount=" + result.materializationCount + ",decodeCount=" + result.decodeCount
        + ",drawPlanCacheHits=" + result.drawPlanCacheHits + ",identityAttempts=" + result.identityAttempts
        + ",identityHits=" + result.identityHits + ",directCopyHits=" + result.directCopyHits
        + ",genericGeometryDraws=" + result.genericGeometryDraws + ",smoothResampleDraws="
        + result.smoothResampleDraws + ",planCopyAttempts=" + result.planCopyAttempts
        + ",elapsedMs=" + join(result.elapsedMillis));
  }

  private static String join(int[] values) {
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        output.append('|');
      }
      output.append(values[i]);
    }
    return output.toString();
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static final class LaneContext {
    final String name;
    final boolean partialClip;
    final Image source;
    final Image destination;
    final Graphics graphics;
    final boolean deferredDraw;

    LaneContext(String name, boolean partialClip, Image source, boolean deferredDraw) throws ImageException {
      this.name = name;
      this.partialClip = partialClip;
      this.source = source;
      this.deferredDraw = deferredDraw;
      destination = new Image(DESTINATION_SIZE, DESTINATION_SIZE);
      graphics = destination.getGraphics();
      graphics.backColor = BACKGROUND;
      graphics.fillRect(0, 0, DESTINATION_SIZE, DESTINATION_SIZE);
      if (partialClip) {
        graphics.setClip(9, 9, 8, 8);
      }
    }

    void drawOnce() {
      if (deferredDraw) {
        graphics.drawImage(source, DESTINATION_X, DESTINATION_Y);
      } else {
        graphics.copyRect(source, 0, 0, IMAGE_SIZE, IMAGE_SIZE, DESTINATION_X, DESTINATION_Y);
      }
    }
  }

  private static final class LaneResult {
    final String lane;
    final boolean partialClip;
    final int pixelHash;
    final int materializationCount;
    final int decodeCount;
    final int drawPlanCacheHits;
    final int identityAttempts;
    final int identityHits;
    final int directCopyHits;
    final int genericGeometryDraws;
    final int smoothResampleDraws;
    final int planCopyAttempts;
    final int[] elapsedMillis;

    LaneResult(String lane, boolean partialClip, int pixelHash, int materializationCount,
        int decodeCount, int drawPlanCacheHits, int identityAttempts, int identityHits,
        int directCopyHits, int genericGeometryDraws, int smoothResampleDraws,
        int planCopyAttempts, int[] elapsedMillis) {
      this.lane = lane;
      this.partialClip = partialClip;
      this.pixelHash = pixelHash;
      this.materializationCount = materializationCount;
      this.decodeCount = decodeCount;
      this.drawPlanCacheHits = drawPlanCacheHits;
      this.identityAttempts = identityAttempts;
      this.identityHits = identityHits;
      this.directCopyHits = directCopyHits;
      this.genericGeometryDraws = genericGeometryDraws;
      this.smoothResampleDraws = smoothResampleDraws;
      this.planCopyAttempts = planCopyAttempts;
      this.elapsedMillis = elapsedMillis;
    }
  }
}
