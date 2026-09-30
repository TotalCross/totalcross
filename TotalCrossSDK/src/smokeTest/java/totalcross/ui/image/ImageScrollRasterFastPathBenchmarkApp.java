// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.MainWindow;
import totalcross.ui.ScrollContainer;
import totalcross.ui.Window;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Graphics;

/** Native-deployed clipped scrolling workload for lazy JPEG raster accounting. */
public class ImageScrollRasterFastPathBenchmarkApp extends MainWindow {
  private static final int IMAGE_COUNT = 120;
  private static final int COLUMN_COUNT = 3;
  private static final int TILE_SIZE = 64;
  private static final int TILE_GAP = 8;
  private static final int TILE_PITCH = TILE_SIZE + TILE_GAP;
  private static final int VISIBLE_ROWS = 4;
  private static final int VIEWPORT_WIDTH = COLUMN_COUNT * TILE_PITCH;
  private static final int VIEWPORT_HEIGHT = VISIBLE_ROWS * TILE_PITCH;
  private static final int SCROLL_STEP = TILE_PITCH;
  private static int lastScrollPassFramesForTest;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "post-enabled");
    String testCase = ImageRasterBenchmarkSupport.argument(getCommandLine(), "case", "clipped");
    String framePath = ImageRasterBenchmarkSupport.argument(getCommandLine(), "frame-path", "manual");
    String variantCacheProfile = ImageRasterBenchmarkSupport.argument(
        getCommandLine(), "variant-cache", "disabled");
    boolean clip = "clipped".equals(testCase);
    boolean naturalFramePath = "natural".equals(framePath);
    boolean variantCacheEnabled = "enabled".equals(variantCacheProfile);
    int completed = 0;
    String error = "";
    String cold = null;
    String warmReverse = null;
    String warmForward = null;
    String variant = null;
    int coldFrames = 0;
    int warmReverseFrames = 0;
    int warmForwardFrames = 0;
    String pixelHash = null;
    String partialClipHash = null;
    String transformedFallbackHash = null;
    String noIntersection = "skipped";
    String screenScale = "unknown";
    boolean overallPass = false;
    boolean previousFingerTouch = Settings.fingerTouch;
    try {
      ImageRasterBenchmarkSupport.require("clipped".equals(testCase) || "unclipped".equals(testCase),
          "case must be clipped or unclipped");
      ImageRasterBenchmarkSupport.require("manual".equals(framePath) || naturalFramePath,
          "frame-path must be manual or natural");
      ImageRasterBenchmarkSupport.require("disabled".equals(variantCacheProfile)
          || variantCacheEnabled, "variant-cache must be disabled or enabled");
      ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
      ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
          ImageOptimizationSettings.ENABLED);
      byte[] encoded = ImageRasterBenchmarkSupport.resource("image-abi/lena512.jpg");
      Image[] images = lazyTiles(encoded);
      ScrollContainer scroll = buildScroll(images, clip);
      if (naturalFramePath) {
        // Keep the existing scrollbar construction; the normal pen-event path
        // consults this flag when dispatching drag events.
        Settings.fingerTouch = true;
      }
      Graphics screenGraphics = scroll.getGraphics();
      ImageRasterBenchmarkSupport.require(screenGraphics != null, "scroll graphics");
      screenScale = String.valueOf(screenGraphics.getContentScale());
      pixelHash = verifyPixelParity(encoded);
      partialClipHash = verifyPartialClipParity(scenario);
      if ("post-enabled".equals(scenario)) {
        noIntersection = verifyNoIntersectionInvariants(scenario);
      }
      transformedFallbackHash = verifyTransformedFallbackParity(scenario);
      ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
      if (variantCacheEnabled) {
        ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE,
            ImageOptimizationSettings.ENABLED);
      }
      ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
          ImageOptimizationSettings.ENABLED);
      if (naturalFramePath) {
        cold = runNaturalScrollScenario(scroll);
        coldFrames = lastScrollPassFramesForTest;
      } else {
        cold = runScrollPass(scroll, true);
        coldFrames = lastScrollPassFramesForTest;
        if ("post-enabled".equals(scenario) && clip) {
          ImageRasterBenchmarkSupport.require(NativeImageBacking.physicalIdentityHitsForTest() > 0,
              "fully visible clipped physical identity path was not used");
          if (!variantCacheEnabled) {
            ImageRasterBenchmarkSupport.require(
                NativeImageBacking.physicalVariantLookupsForTest() == 0,
                "application profile unexpectedly used physical variants");
          }
        }
        warmReverse = runScrollPass(scroll, false);
        warmReverseFrames = lastScrollPassFramesForTest;
        warmForward = runScrollPass(scroll, true);
        warmForwardFrames = lastScrollPassFramesForTest;
        if ("post-enabled".equals(scenario) && clip && variantCacheEnabled) {
          long variantLookups = NativeImageBacking.physicalVariantLookupsForTest();
          long variantHits = NativeImageBacking.physicalVariantHitsForTest();
          long variantMisses = NativeImageBacking.physicalVariantMissesForTest();
          long variantStores = NativeImageBacking.physicalVariantMaterializationsForTest();
          ImageRasterBenchmarkSupport.require(variantLookups == variantHits + variantMisses,
              "warm scroll physical variant accounting mismatch");
          ImageRasterBenchmarkSupport.require(
              variantStores <= variantMisses && variantStores <= IMAGE_COUNT,
              "warm scroll created position-specific physical variants");
        }
      }
      variant = runVariantScenario(scenario);
      ImageRasterBenchmarkSupport.require(coldFrames > 0
          && (naturalFramePath || (warmReverseFrames > 0 && warmForwardFrames > 0)),
          "scroll passes did not paint");
      completed = 1;
      overallPass = true;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    } finally {
      Settings.fingerTouch = previousFingerTouch;
    }

    String details = "case=" + testCase + ",image_count=" + IMAGE_COUNT
        + ",columns=" + COLUMN_COUNT + ",visible_rows=" + VISIBLE_ROWS
        + ",frame_path=" + framePath
        + ",variant_cache_profile=" + variantCacheProfile
        + ",tile_logical=" + TILE_SIZE + ",screen_scale=" + screenScale
        + ",validation_scale=2,pixel_hash=" + String.valueOf(pixelHash)
        + ",partial_clip_hash=" + String.valueOf(partialClipHash)
        + ",no_intersection=" + noIntersection
        + ",transformed_fallback_hash=" + String.valueOf(transformedFallbackHash)
        + ",cold_forward=" + (cold == null ? "missing" : cold)
        + ",warm_reverse=" + (warmReverse == null ? "missing" : warmReverse)
        + ",warm_forward=" + (warmForward == null ? "missing" : warmForward)
        + ",native_update_screen_calls=" + NativeImageBacking.screenUpdateCallsForTest()
        + ",native_present_calls=" + NativeImageBacking.screenPresentCallsForTest()
        + ",variant_cache=" + (variant == null ? "missing" : variant);
    boolean pass = ImageRasterBenchmarkSupport.finish(
        "ImageScrollRasterFastPathBenchmarkApp", scenario, 1, completed, details,
        overallPass && error.length() == 0 ? "" : error.length() == 0 ? "assertion_failed" : error);
    ImageRasterBenchmarkSupport.writeReport(
        "ImageScrollRasterFastPathBenchmarkApp.log",
        "fixture=ImageScrollRasterFastPathBenchmarkApp,scenario=" + scenario
            + ",completed_samples=" + completed + "," + details
            + ",overallPass=" + pass
            + (error.length() == 0 ? "" : ",error=" + error));
    exit(pass ? 0 : 1);
  }

  private static Image[] lazyTiles(byte[] encoded) throws Exception {
    Image[] images = new Image[IMAGE_COUNT];
    for (int i = 0; i < images.length; i++) {
      images[i] = new Image(encoded, encoded.length).getSmoothScaledInstance(TILE_SIZE, TILE_SIZE);
    }
    return images;
  }

  private ScrollContainer buildScroll(Image[] images, boolean clip) {
    ScrollContainer scroll = new ScrollContainer(false, true);
    add(scroll);
    scroll.setRect(0, 0, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    for (int i = 0; i < images.length; i++) {
      ScrollImageTile tile = new ScrollImageTile(images[i], clip);
      int column = i % COLUMN_COUNT;
      int row = i / COLUMN_COUNT;
      tile.setRect(column * TILE_PITCH, row * TILE_PITCH, TILE_SIZE, TILE_SIZE);
      scroll.add(tile);
    }
    scroll.resize();
    return scroll;
  }

  private static String runScrollPass(ScrollContainer scroll, boolean forward) {
    ImageRasterBenchmarkSupport.require(scroll.sbV != null, "vertical scrollbar");
    int minimum = scroll.sbV.getMinimum();
    int maximum = validMaximum(scroll);
    int startValue = moveToEndpoint(scroll, forward ? minimum : maximum);
    ImageRasterBenchmarkSupport.require(startValue == (forward ? minimum : maximum),
        "scroll pass did not reach its explicit endpoint");
    Image.resetImageOperationAccountingForTest();
    long start = Vm.getTimeStamp();
    int frames = 0;
    paintFrame(scroll);
    frames++;
    while (true) {
      int before = scroll.sbV.getValue();
      int distance = forward ? maximum - before : before - minimum;
      int delta = forward ? Math.min(SCROLL_STEP, distance) : -Math.min(SCROLL_STEP, distance);
      if (delta == 0) {
        break;
      }
      scroll.scrollContent(0, delta, true);
      int after = scroll.sbV.getValue();
      if (before == after) {
        break;
      }
      paintFrame(scroll);
      frames++;
    }
    int endValue = scroll.sbV.getValue();
    ImageRasterBenchmarkSupport.require(endValue == (forward ? maximum : minimum),
        "scroll pass did not traverse the full scrollbar range");
    ImageRasterBenchmarkSupport.require(frames > 1, "scroll pass needs multiple equivalent frames");
    lastScrollPassFramesForTest = frames;
    return "elapsed_ms=" + (Vm.getTimeStamp() - start) + ",scroll_start=" + startValue
        + ",scroll_end=" + endValue + ",scroll_max=" + maximum + ",frames=" + frames
        + counterDetails();
  }

  private static int validMaximum(ScrollContainer scroll) {
    int maximum = scroll.sbV.getMaximum() - scroll.sbV.getVisibleItems();
    return Math.max(scroll.sbV.getMinimum(), maximum);
  }

  private static int moveToEndpoint(ScrollContainer scroll, int endpoint) {
    int current = scroll.sbV.getValue();
    while (current != endpoint) {
      int distance = endpoint > current ? endpoint - current : current - endpoint;
      int step = Math.min(SCROLL_STEP, distance);
      scroll.scrollContent(0, endpoint > current ? step : -step, true);
      int next = scroll.sbV.getValue();
      ImageRasterBenchmarkSupport.require(next != current || step == 0,
          "scroll endpoint did not advance");
      current = next;
    }
    return current;
  }

  private static void paintFrame(ScrollContainer scroll) {
    Graphics graphics = scroll.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "scroll frame graphics");
    Control.repaint();
    Window.repaintActiveWindows();
  }

  private String runNaturalScrollScenario(ScrollContainer scroll) {
    Image.resetImageOperationAccountingForTest();
    Window.needsPaint = false;
    int startValue = scroll.sbV == null ? 0 : scroll.sbV.getValue();
    int x = Math.max(1, scroll.getWidth() / 2);
    int y = Math.max(2, scroll.getHeight() - 12);
    long start = Vm.getTimeStamp();
    int events = 0;
    int timerTicks = 0;
    this._postEvent(PenEvent.PEN_DOWN, 0, x, y, 0, (int) Vm.getTimeStamp());
    events++;
    int currentY = y;
    for (int i = 0; i < 36; i++) {
      currentY -= 4;
      Vm.sleep(17);
      this._postEvent(PenEvent.PEN_DRAG, 0, x, currentY, 0, (int) Vm.getTimeStamp());
      events++;
      Vm.sleep(17);
      _onTimerTick(true);
      timerTicks++;
    }
    this._postEvent(PenEvent.PEN_UP, 0, x, currentY, 0, (int) Vm.getTimeStamp());
    events++;
    int endValue = scroll.sbV == null ? startValue : scroll.sbV.getValue();
    ImageRasterBenchmarkSupport.require(endValue > startValue, "natural scroll did not move");
    lastScrollPassFramesForTest = timerTicks;
    return "elapsed_ms=" + (Vm.getTimeStamp() - start) + ",events=" + events
        + ",timer_ticks=" + timerTicks + ",scroll_start=" + startValue
        + ",scroll_end=" + endValue + ",frames=" + lastScrollPassFramesForTest + counterDetails();
  }

  private static String verifyPixelParity(byte[] encoded) throws Exception {
    Image source = new Image(encoded, encoded.length).getSmoothScaledInstance(TILE_SIZE, TILE_SIZE);
    Image clippedTarget = Image.createLogical(TILE_SIZE, TILE_SIZE, 2);
    Image unclippedTarget = Image.createLogical(TILE_SIZE, TILE_SIZE, 2);
    clippedTarget.getGraphics().drawImage(source, 0, 0, true);
    long clippedHash = ImageRasterBenchmarkSupport.fullPixelHash(clippedTarget);
    unclippedTarget.getGraphics().drawImage(source, 0, 0, false);
    long unclippedHash = ImageRasterBenchmarkSupport.fullPixelHash(unclippedTarget);
    ImageRasterBenchmarkSupport.require(clippedHash == unclippedHash, "clipped pixel hash mismatch");
    ImageRasterBenchmarkSupport.require(clippedTarget.getPixelWidth() >= TILE_SIZE * 2
        && clippedTarget.getPixelHeight() >= TILE_SIZE * 2, "validation target scale");
    return ImageRasterBenchmarkSupport.hashString(clippedHash);
  }

  private static String verifyPartialClipParity(String scenario) throws Exception {
    final int[][] clips = {
        {16, 8, 56, 64},  // left
        {8, 16, 64, 56},  // top
        {8, 8, 56, 64},   // right
        {8, 8, 64, 56},   // bottom
        {16, 16, 56, 56}, // corner
        {0, 0, 4, 4}      // no intersection
    };
    Image.resetImageOperationAccountingForTest();
    long firstHash = 0;
    long optimizedIdentityHits = 0;
    try {
      for (int scale = 1; scale <= 2; scale++) {
        Image source = Image.createLogical(TILE_SIZE, TILE_SIZE, scale);
        Graphics sourceGraphics = source.getGraphics();
        ImageRasterBenchmarkSupport.require(sourceGraphics != null, "partial clip source graphics");
        sourceGraphics.backColor = 0x102030;
        sourceGraphics.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        sourceGraphics.backColor = 0x405060;
        sourceGraphics.fillRect(0, 0, TILE_SIZE / 2, TILE_SIZE / 2);
        sourceGraphics.backColor = 0x708090;
        sourceGraphics.fillRect(TILE_SIZE / 2, TILE_SIZE / 2, TILE_SIZE / 2, TILE_SIZE / 2);
        if (scale == 2) {
          source = source.getSmoothScaledInstance(TILE_SIZE, TILE_SIZE);
        }
        for (int[] clip : clips) {
          long identityHitsBefore = NativeImageBacking.physicalIdentityHitsForTest();
          Image optimized = renderClipped(source, scale, clip);
          long optimizedHash = ImageRasterBenchmarkSupport.fullPixelHash(optimized);
          if (firstHash == 0) {
            firstHash = optimizedHash;
          }
          optimizedIdentityHits += NativeImageBacking.physicalIdentityHitsForTest()
              - identityHitsBefore;

          ImageOptimizationSettings.resetForTest();
          ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
              ImageOptimizationSettings.ENABLED);
          Image reference = renderClipped(source, scale, clip);
          long referenceHash = ImageRasterBenchmarkSupport.fullPixelHash(reference);
          ImageRasterBenchmarkSupport.require(optimizedHash == referenceHash,
              "partial clip pixel mismatch scale=" + scale + ",clip=" + clip[0] + ":"
                  + clip[1] + ":" + clip[2] + ":" + clip[3] + ",optimized="
                  + ImageRasterBenchmarkSupport.hashString(optimizedHash) + ",reference="
                  + ImageRasterBenchmarkSupport.hashString(referenceHash) + ",first="
                  + firstPixelMismatch(optimized.getPixels(), reference.getPixels()));
          ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
        }
      }
    } finally {
      ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
    }
    return ImageRasterBenchmarkSupport.hashString(firstHash) + ":identity_hits="
        + optimizedIdentityHits;
  }

  private static String verifyNoIntersectionInvariants(String scenario) throws Exception {
    ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE,
        ImageOptimizationSettings.ENABLED);
    Image source = Image.createLogical(TILE_SIZE, TILE_SIZE, 2);
    Image target = Image.createTestRaster(TILE_SIZE, TILE_SIZE, 2,
        NativeImageBacking.TEST_COLOR_RGB565);
    ImageRasterBenchmarkSupport.require(target.backing instanceof NativeImageBacking,
        "no-intersection native target");
    NativeImageBacking targetBacking = (NativeImageBacking) target.backing;
    Image scaledTarget = target.getSmoothScaledInstance(TILE_SIZE - 8, TILE_SIZE - 8);
    Image variantProbe = Image.createLogical(TILE_SIZE, TILE_SIZE, 2);
    Graphics probeGraphics = variantProbe.getGraphics();
    ImageRasterBenchmarkSupport.require(probeGraphics != null, "no-intersection variant probe");
    probeGraphics.setClip(0, 0, TILE_SIZE, TILE_SIZE);
    for (int i = 0; i < 3; i++) {
      probeGraphics.drawImage(scaledTarget, 0, 0, true);
    }
    long variantLookupsBefore = NativeImageBacking.physicalVariantLookupsForTest();
    long variantHitsBefore = NativeImageBacking.physicalVariantHitsForTest();
    long variantMissesBefore = NativeImageBacking.physicalVariantMissesForTest();
    long variantStoresBefore = NativeImageBacking.physicalVariantMaterializationsForTest();
    long generationBefore = targetBacking.generationForTest();
    int opacityBefore = targetBacking.opacityForTest();
    long targetMaterializationsBefore = NativeImageBacking.targetColorMaterializationsForTest();
    long variantEvictionsBefore = NativeImageBacking.physicalVariantEvictionsForTest();
    long variantBytesBefore = NativeImageBacking.physicalVariantBytesForTest();
    int[] pixelsBefore = target.getPixels();
    Graphics graphics = target.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "no-intersection target graphics");
    graphics.setClip(0, 0, 4, 4);
    graphics.drawImage(source, 8, 8, true);
    int[] pixelsAfter = target.getPixels();
    ImageRasterBenchmarkSupport.require(generationBefore == targetBacking.generationForTest(),
        "no-intersection draw changed target generation");
    ImageRasterBenchmarkSupport.require(opacityBefore == targetBacking.opacityForTest(),
        "no-intersection draw invalidated target opacity");
    ImageRasterBenchmarkSupport.require(targetMaterializationsBefore
        == NativeImageBacking.targetColorMaterializationsForTest()
        && variantEvictionsBefore == NativeImageBacking.physicalVariantEvictionsForTest()
        && variantBytesBefore == NativeImageBacking.physicalVariantBytesForTest()
        && variantLookupsBefore == NativeImageBacking.physicalVariantLookupsForTest()
        && variantHitsBefore == NativeImageBacking.physicalVariantHitsForTest()
        && variantMissesBefore == NativeImageBacking.physicalVariantMissesForTest()
        && variantStoresBefore == NativeImageBacking.physicalVariantMaterializationsForTest(),
        "no-intersection draw changed raster variant accounting");
    ImageRasterBenchmarkSupport.require(firstPixelMismatch(pixelsAfter, pixelsBefore)
        .equals(pixelsBefore.length + "/" + pixelsAfter.length),
        "no-intersection draw changed pixels");
    probeGraphics.drawImage(scaledTarget, 0, 0, true);
    ImageRasterBenchmarkSupport.require(NativeImageBacking.physicalVariantHitsForTest()
        == variantHitsBefore + 1
        && NativeImageBacking.physicalVariantMissesForTest() == variantMissesBefore
        && NativeImageBacking.physicalVariantMaterializationsForTest() == variantStoresBefore,
        "no-intersection draw cleared the source raster variant");
    ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
    return "generation=" + generationBefore + ",opacity=" + opacityBefore
        + ",target_color_materializations=" + targetMaterializationsBefore
        + ",physical_variant_evictions=" + variantEvictionsBefore
        + ",physical_variant_bytes=" + variantBytesBefore;
  }

  private static Image renderClipped(Image source, int scale, int[] clip) throws Exception {
    Image target = Image.createLogical(TILE_SIZE + 16, TILE_SIZE + 16, scale);
    Graphics graphics = target.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "partial clip target graphics");
    graphics.backColor = 0x102030;
    graphics.fillRect(0, 0, target.getWidth(), target.getHeight());
    graphics.setClip(clip[0], clip[1], clip[2], clip[3]);
    graphics.drawImage(source, 8, 8, true);
    return target;
  }

  private static String firstPixelMismatch(int[] actual, int[] expected) {
    int length = Math.min(actual.length, expected.length);
    for (int i = 0; i < length; i++) {
      if (actual[i] != expected[i]) {
        return i + ":" + Integer.toHexString(actual[i]) + "/" + Integer.toHexString(expected[i]);
      }
    }
    return actual.length + "/" + expected.length;
  }

  private static String verifyTransformedFallbackParity(String scenario) throws Exception {
    Image source = Image.createLogical(TILE_SIZE, TILE_SIZE, 2);
    Graphics sourceGraphics = source.getGraphics();
    ImageRasterBenchmarkSupport.require(sourceGraphics != null, "transformed source graphics");
    sourceGraphics.backColor = 0x204060;
    sourceGraphics.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
    sourceGraphics.backColor = 0xA0C0E0;
    sourceGraphics.fillRect(8, 8, TILE_SIZE - 16, TILE_SIZE - 16);
    Image transformed = source.getRotatedScaledInstance(100, 17, 0);
    Image.resetImageOperationAccountingForTest();
    ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.ENABLED);
    Image optimized = renderTransformed(transformed);
    long optimizedHash = ImageRasterBenchmarkSupport.fullPixelHash(optimized);
    long optimizedGenericDraws = ImageRasterBenchmarkSupport.genericGeometryDrawsForTest();
    ImageOptimizationSettings.resetForTest();
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.ENABLED);
    Image reference = renderTransformed(transformed);
    long referenceHash = ImageRasterBenchmarkSupport.fullPixelHash(reference);
    ImageRasterBenchmarkSupport.require(optimizedHash == referenceHash,
        "transformed pixel mismatch optimized=" + ImageRasterBenchmarkSupport.hashString(optimizedHash)
            + ",reference=" + ImageRasterBenchmarkSupport.hashString(referenceHash));
    ImageRasterBenchmarkSupport.require(optimizedGenericDraws > 0,
        "transformed draw did not use generic fallback");
    ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
    return ImageRasterBenchmarkSupport.hashString(optimizedHash) + ":generic=" + optimizedGenericDraws;
  }

  private static Image renderTransformed(Image source) throws Exception {
    Image target = Image.createLogical(TILE_SIZE + 16, TILE_SIZE + 16, 2);
    Graphics graphics = target.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "transformed target graphics");
    graphics.backColor = 0x102030;
    graphics.fillRect(0, 0, target.getWidth(), target.getHeight());
    graphics.setClip(8, 8, TILE_SIZE, TILE_SIZE);
    graphics.drawImage(source, 8, 8, true);
    return target;
  }

  private static String runVariantScenario(String scenario) throws Exception {
    Image image = variantSource().getSmoothScaledInstance(TILE_SIZE - 8, TILE_SIZE - 8);
    ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
    long referenceHash = renderVariantSequence(image, 14);

    Image.resetImageOperationAccountingForTest();
    ImageOptimizationSettings.resetForTest();
    for (int feature = 0; feature < ImageOptimizationSettings.FEATURE_COUNT; feature++) {
      ImageOptimizationSettings.setState(feature, ImageOptimizationSettings.DISABLED);
    }
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DECODE_ZERO_COPY,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_OPACITY_METADATA,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE,
        ImageOptimizationSettings.ENABLED);
    Image target = Image.createLogical(TILE_SIZE, TILE_SIZE, 2);
    Graphics graphics = target.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "variant target graphics");
    graphics.setClip(4, 4, TILE_SIZE - 8, TILE_SIZE - 8);
    long start = Vm.getTimeStamp();
    drawVariantSequence(graphics, image, 0, 2);
    long warmupLookups = NativeImageBacking.physicalVariantLookupsForTest();
    long warmupHits = NativeImageBacking.physicalVariantHitsForTest();
    long warmupMisses = NativeImageBacking.physicalVariantMissesForTest();
    long warmupStores = NativeImageBacking.physicalVariantMaterializationsForTest();
    long warmupSmoothResamples = ImageRasterBenchmarkSupport.smoothResampleDrawsForTest();
    ImageRasterBenchmarkSupport.require(warmupLookups == 2 && warmupHits == 0
        && warmupMisses == 2 && warmupStores == 1 && warmupSmoothResamples == 1,
        "unexpected physical variant population counters" + counterDetails());
    for (int draw = 2; draw < 14; draw++) {
      drawVariantSequence(graphics, image, draw, 1);
    }
    long elapsed = Vm.getTimeStamp() - start;
    long measuredLookups = NativeImageBacking.physicalVariantLookupsForTest() - warmupLookups;
    long measuredHits = NativeImageBacking.physicalVariantHitsForTest() - warmupHits;
    long measuredMisses = NativeImageBacking.physicalVariantMissesForTest() - warmupMisses;
    long measuredStores = NativeImageBacking.physicalVariantMaterializationsForTest() - warmupStores;
    long measuredSmoothResamples = ImageRasterBenchmarkSupport.smoothResampleDrawsForTest()
        - warmupSmoothResamples;
    ImageRasterBenchmarkSupport.require(measuredLookups == 12 && measuredHits == 12
        && measuredHits * 100 >= measuredLookups * 95 && measuredMisses == 0
        && measuredStores == 0 && measuredSmoothResamples == 0,
        "warm physical variant was not reused" + counterDetails());
    long optimizedHash = ImageRasterBenchmarkSupport.fullPixelHash(target);
    ImageRasterBenchmarkSupport.require(optimizedHash == referenceHash,
        "cached physical variant pixel mismatch optimized="
            + ImageRasterBenchmarkSupport.hashString(optimizedHash) + ",reference="
            + ImageRasterBenchmarkSupport.hashString(referenceHash));
    return "elapsed_ms=" + elapsed + ",optimized_hash="
        + ImageRasterBenchmarkSupport.hashString(optimizedHash) + ",reference_hash="
        + ImageRasterBenchmarkSupport.hashString(referenceHash)
        + ",warmup_lookups=" + warmupLookups + ",warmup_hits=" + warmupHits
        + ",warmup_misses=" + warmupMisses + ",warmup_stores=" + warmupStores
        + ",warmup_smooth_resamples=" + warmupSmoothResamples
        + ",measured_lookups=" + measuredLookups + ",measured_hits=" + measuredHits
        + ",measured_misses=" + measuredMisses + ",measured_stores=" + measuredStores
        + ",measured_smooth_resamples=" + measuredSmoothResamples + counterDetails();
  }

  private static long renderVariantSequence(Image image, int draws) throws Exception {
    Image target = Image.createLogical(TILE_SIZE, TILE_SIZE, 2);
    Graphics graphics = target.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "variant reference graphics");
    graphics.setClip(4, 4, TILE_SIZE - 8, TILE_SIZE - 8);
    drawVariantSequence(graphics, image, 0, draws);
    return ImageRasterBenchmarkSupport.fullPixelHash(target);
  }

  private static void drawVariantSequence(Graphics graphics, Image image, int firstDraw,
      int draws) {
    for (int draw = firstDraw; draw < firstDraw + draws; draw++) {
      graphics.drawImage(image, 0, drawVariantY(draw), true);
    }
  }

  private static int drawVariantY(int draw) {
    return draw < 2 ? draw : 2 + ((draw - 2) % 3);
  }

  private static Image variantSource() throws Exception {
    Image source = Image.createLogical(200, 200, 2);
    Graphics graphics = source.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "variant source graphics");
    for (int y = 0; y < 200; y += 16) {
      for (int x = 0; x < 200; x += 16) {
        graphics.foreColor = 0xFF000000 | ((x * 11) & 0xFF) << 16
            | ((y * 13) & 0xFF) << 8 | ((x + y * 3) & 0xFF);
        graphics.fillRect(x, y, Math.min(16, 200 - x), Math.min(16, 200 - y));
      }
    }
    return source;
  }

  private static final class ScrollImageTile extends Control {
    private final Image image;
    private boolean clip;

    ScrollImageTile(Image image, boolean clip) {
      this.image = image;
      this.clip = clip;
    }

    @Override
    public void onPaint(Graphics graphics) {
      graphics.drawImage(image, 0, 0, clip);
    }
  }

  private static String counterDetails() {
    long attemptsChannel = NativeImageBacking.physicalIdentityRejectionAttemptsChannelForTest();
    long hitsChannel = NativeImageBacking.physicalIdentityRejectionHitsChannelForTest();
    long fallbacksChannel = NativeImageBacking.physicalIdentityRejectionFallbacksChannelForTest();
    long resamplesChannel = NativeImageBacking.physicalIdentityRejectionResamplesChannelForTest();
    long rejectionCanvasState = (attemptsChannel >>> 16) & 0xffffL;
    long rejectionSurfaceDestination = (attemptsChannel >>> 32) & 0xffffL;
    long rejectionDeviceClip = (hitsChannel >>> 16) & 0xffffL;
    long rejectionPartialIntersection = (hitsChannel >>> 32) & 0xffffL;
    long rejectionMappingGeometry = (fallbacksChannel >>> 16) & 0xffffL;
    long rejectionBackingIncompatible = (fallbacksChannel >>> 32) & 0xffffL;
    long rejectionExecutionFailure = (resamplesChannel >>> 16) & 0xffffL;
    long identityAttempts = attemptsChannel & 0xffffL;
    long identityHits = hitsChannel & 0xffffL;
    long identityFallbacks = fallbacksChannel & 0xffffL;
    long rejectionTotal = rejectionCanvasState + rejectionSurfaceDestination + rejectionDeviceClip
        + rejectionPartialIntersection + rejectionMappingGeometry + rejectionBackingIncompatible
        + rejectionExecutionFailure;
    ImageRasterBenchmarkSupport.require(identityAttempts == identityHits + identityFallbacks
        && rejectionTotal == identityFallbacks, "physical identity accounting mismatch");
    return ",physical_identity_attempts=" + identityAttempts
        + ",physical_identity_hits=" + identityHits
        + ",physical_identity_fallbacks=" + identityFallbacks
        + ",physical_identity_rejections_canvas_state=" + rejectionCanvasState
        + ",physical_identity_rejections_surface_destination=" + rejectionSurfaceDestination
        + ",physical_identity_rejections_device_clip=" + rejectionDeviceClip
        + ",physical_identity_rejections_partial_intersection=" + rejectionPartialIntersection
        + ",physical_identity_rejections_mapping_geometry=" + rejectionMappingGeometry
        + ",physical_identity_rejections_backing_incompatible=" + rejectionBackingIncompatible
        + ",physical_identity_rejections_execution_failure=" + rejectionExecutionFailure
        + ",physical_variant_lookups=" + NativeImageBacking.physicalVariantLookupsForTest()
        + ",physical_variant_hits=" + NativeImageBacking.physicalVariantHitsForTest()
        + ",physical_variant_stores=" + NativeImageBacking.physicalVariantMaterializationsForTest()
        + ",physical_variant_evictions=" + NativeImageBacking.physicalVariantEvictionsForTest()
        + ",physical_variant_bytes=" + NativeImageBacking.physicalVariantBytesForTest()
        + ",target_color_attempts=" + NativeImageBacking.targetColorAttemptsForTest()
        + ",target_color_materializations="
        + NativeImageBacking.targetColorMaterializationsForTest()
        + ",target_color_converted_bytes="
        + NativeImageBacking.targetColorConvertedBytesForTest()
        + ",target_color_hits=" + NativeImageBacking.targetColorHitsForTest()
        + ",target_color_fallbacks=" + NativeImageBacking.targetColorFallbacksForTest()
        + ",write_pixels_attempts=" + NativeImageBacking.writePixelsAttemptsForTest()
        + ",write_pixels_hits=" + NativeImageBacking.writePixelsHitsForTest()
        + ",generic_geometry_draws=" + ImageRasterBenchmarkSupport.genericGeometryDrawsForTest()
        + ",smooth_resample_draws=" + ImageRasterBenchmarkSupport.smoothResampleDrawsForTest();
  }
}
