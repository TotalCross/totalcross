// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.MainWindow;
import totalcross.ui.ScrollContainer;
import totalcross.ui.Window;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;
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
    boolean clip = "clipped".equals(testCase);
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
    String screenScale = "unknown";
    boolean overallPass = false;
    try {
      ImageRasterBenchmarkSupport.require("clipped".equals(testCase) || "unclipped".equals(testCase),
          "case must be clipped or unclipped");
      ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
      ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
          ImageOptimizationSettings.ENABLED);
      Window.resetRepaintDiagnosticsForTest();
      byte[] encoded = ImageRasterBenchmarkSupport.resource("image-abi/lena512.jpg");
      Image[] images = lazyTiles(encoded);
      ScrollContainer scroll = buildScroll(images, clip);
      Graphics screenGraphics = scroll.getGraphics();
      ImageRasterBenchmarkSupport.require(screenGraphics != null, "scroll graphics");
      screenScale = String.valueOf(screenGraphics.getContentScale());
      pixelHash = verifyPixelParity(encoded);
      cold = runScrollPass(scroll, true);
      coldFrames = lastScrollPassFramesForTest;
      warmReverse = runScrollPass(scroll, false);
      warmReverseFrames = lastScrollPassFramesForTest;
      warmForward = runScrollPass(scroll, true);
      warmForwardFrames = lastScrollPassFramesForTest;
      runTimerUpdateProbe(scroll);
      variant = runVariantScenario(encoded);
      ImageRasterBenchmarkSupport.require(coldFrames > 0 && warmReverseFrames > 0
          && warmForwardFrames > 0,
          "scroll passes did not paint");
      completed = 1;
      overallPass = true;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    String details = "case=" + testCase + ",image_count=" + IMAGE_COUNT
        + ",columns=" + COLUMN_COUNT + ",visible_rows=" + VISIBLE_ROWS
        + ",tile_logical=" + TILE_SIZE + ",screen_scale=" + screenScale
        + ",validation_scale=2,pixel_hash=" + String.valueOf(pixelHash)
        + ",cold_forward=" + (cold == null ? "missing" : cold)
        + ",warm_reverse=" + (warmReverse == null ? "missing" : warmReverse)
        + ",warm_forward=" + (warmForward == null ? "missing" : warmForward)
        + ",repaint_frame=" + Window.repaintDiagnosticsForTest()
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
    if (forward) {
      scroll.scrollToOrigin();
    } else {
      scroll.scrollContent(0, Integer.MAX_VALUE, true);
    }
    Image.resetImageOperationAccountingForTest();
    long start = Vm.getTimeStamp();
    int frames = 0;
    paintFrame(scroll);
    frames++;
    while (true) {
      int before = scroll.sbV == null ? 0 : scroll.sbV.getValue();
      scroll.scrollContent(0, forward ? SCROLL_STEP : -SCROLL_STEP, true);
      int after = scroll.sbV == null ? before : scroll.sbV.getValue();
      if (before == after) {
        break;
      }
      paintFrame(scroll);
      frames++;
    }
    lastScrollPassFramesForTest = frames;
    return "elapsed_ms=" + (Vm.getTimeStamp() - start) + ",frames=" + frames + counterDetails();
  }

  private static void paintFrame(ScrollContainer scroll) {
    Graphics graphics = scroll.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "scroll frame graphics");
    Window.setRepaintDiagnosticSourceForTest(Window.REPAINT_DIAGNOSTIC_SOURCE_EVENT_FOR_TEST);
    try {
      Control.repaint();
      Window.repaintActiveWindows();
    } finally {
      Window.setRepaintDiagnosticSourceForTest(Window.REPAINT_DIAGNOSTIC_SOURCE_UNKNOWN_FOR_TEST);
    }
  }

  private void runTimerUpdateProbe(final ScrollContainer scroll) {
    final int[] frames = { 0 };
    TimerListener listener = new TimerListener() {
      @Override
      public void timerTriggered(TimerEvent event) {
        if (frames[0] < 4) {
          scroll.scrollContent(0, SCROLL_STEP, true);
          Control.repaint();
          frames[0]++;
        }
      }
    };
    scroll.addTimerListener(listener);
    TimerEvent timer = scroll.addTimer(1);
    try {
      for (int i = 0; i < 4; i++) {
        timer.lastTick = Vm.getTimeStamp() - timer.millis;
        Vm.sleep(2);
        _onTimerTick(true);
      }
    } finally {
      scroll.removeTimer(timer);
      scroll.removeTimerListener(listener);
    }
    ImageRasterBenchmarkSupport.require(frames[0] == 4, "timer probe did not trigger");
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

  private static String runVariantScenario(byte[] encoded) throws Exception {
    ImageOptimizationSettings.resetForTest();
    for (int feature = 0; feature < ImageOptimizationSettings.FEATURE_COUNT; feature++) {
      ImageOptimizationSettings.setState(feature, ImageOptimizationSettings.DISABLED);
    }
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DECODE_ZERO_COPY,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_OPACITY_METADATA,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE,
        ImageOptimizationSettings.ENABLED);
    Image image = new Image(encoded, encoded.length).getSmoothScaledInstance(TILE_SIZE - 8, TILE_SIZE - 8);
    Image target = Image.createLogical(TILE_SIZE, TILE_SIZE, 2);
    Graphics graphics = target.getGraphics();
    for (int warmup = 0; warmup < 2; warmup++) {
      graphics.drawImage(image, 0, 0, false);
    }
    long start = Vm.getTimeStamp();
    for (int draw = 0; draw < 12; draw++) {
      graphics.drawImage(image, 0, 0, false);
    }
    long elapsed = Vm.getTimeStamp() - start;
    return "elapsed_ms=" + elapsed + counterDetails();
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
        + ",target_color_attempts=" + NativeImageBacking.targetColorAttemptsForTest()
        + ",target_color_hits=" + NativeImageBacking.targetColorHitsForTest()
        + ",target_color_fallbacks=" + NativeImageBacking.targetColorFallbacksForTest()
        + ",write_pixels_attempts=" + NativeImageBacking.writePixelsAttemptsForTest()
        + ",write_pixels_hits=" + NativeImageBacking.writePixelsHitsForTest()
        + ",generic_geometry_draws=" + ImageRasterBenchmarkSupport.genericGeometryDrawsForTest()
        + ",smooth_resample_draws=" + ImageRasterBenchmarkSupport.smoothResampleDrawsForTest();
  }
}
