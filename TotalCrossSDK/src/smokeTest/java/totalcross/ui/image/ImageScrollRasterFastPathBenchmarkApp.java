// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.MainWindow;
import totalcross.ui.ScrollContainer;
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

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "post-enabled");
    String testCase = ImageRasterBenchmarkSupport.argument(getCommandLine(), "case", "clipped");
    boolean clip = "clipped".equals(testCase);
    int completed = 0;
    String error = "";
    ScrollRun cold = null;
    ScrollRun warmReverse = null;
    ScrollRun warmForward = null;
    VariantRun variant = null;
    String pixelHash = null;
    String screenScale = "unknown";
    boolean overallPass = false;
    try {
      Vm.debug("raster-stage=init");
      ImageRasterBenchmarkSupport.require("clipped".equals(testCase) || "unclipped".equals(testCase),
          "case must be clipped or unclipped");
      ImageRasterBenchmarkSupport.configureApplicationRasterFeatures(scenario);
      ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
          ImageOptimizationSettings.ENABLED);
      byte[] encoded = ImageRasterBenchmarkSupport.resource("image-abi/lena512.jpg");
      Image[] images = lazyTiles(encoded);
      Vm.debug("raster-stage=tiles");
      ScrollContainer scroll = buildScroll(images, clip);
      Vm.debug("raster-stage=scroll");
      Graphics screenGraphics = scroll.getGraphics();
      ImageRasterBenchmarkSupport.require(screenGraphics != null, "scroll graphics");
      screenScale = String.valueOf(screenGraphics.getContentScale());
      pixelHash = verifyPixelParity(encoded);
      Vm.debug("raster-stage=pixels");
      cold = runScrollPass(scroll, true);
      Vm.debug("raster-stage=cold");
      warmReverse = runScrollPass(scroll, false);
      Vm.debug("raster-stage=reverse");
      warmForward = runScrollPass(scroll, true);
      Vm.debug("raster-stage=forward");
      variant = runVariantScenario(encoded);
      Vm.debug("raster-stage=variant");
      ImageRasterBenchmarkSupport.require(cold.frames > 0 && warmReverse.frames > 0
          && warmForward.frames > 0, "scroll passes did not paint");
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
        + passDetails("cold_forward", cold)
        + passDetails("warm_reverse", warmReverse)
        + passDetails("warm_forward", warmForward)
        + variantDetails(variant);
    boolean pass = ImageRasterBenchmarkSupport.finish(
        "ImageScrollRasterFastPathBenchmarkApp", scenario, 1, completed, details,
        overallPass && error.length() == 0 ? "" : error.length() == 0 ? "assertion_failed" : error);
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

  private static ScrollRun runScrollPass(ScrollContainer scroll, boolean forward) {
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
    return new ScrollRun(Vm.getTimeStamp() - start, frames, Counters.capture());
  }

  private static void paintFrame(ScrollContainer scroll) {
    Graphics graphics = scroll.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "scroll frame graphics");
    scroll.onPaint(graphics);
    scroll.paintChildren();
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

  private static VariantRun runVariantScenario(byte[] encoded) throws Exception {
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
    return new VariantRun(elapsed, Counters.capture());
  }

  private static String passDetails(String name, ScrollRun run) {
    return "," + name + "=" + (run == null ? "missing" : run.details());
  }

  private static String variantDetails(VariantRun run) {
    return ",variant_cache=" + (run == null ? "missing" : run.details());
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

  private static final class ScrollRun {
    final long elapsed;
    final int frames;
    final Counters counters;

    ScrollRun(long elapsed, int frames, Counters counters) {
      this.elapsed = elapsed;
      this.frames = frames;
      this.counters = counters;
    }

    String details() {
      return "elapsed_ms=" + elapsed + ",frames=" + frames + counters.details();
    }
  }

  private static final class VariantRun {
    final long elapsed;
    final Counters counters;

    VariantRun(long elapsed, Counters counters) {
      this.elapsed = elapsed;
      this.counters = counters;
    }

    String details() {
      return "elapsed_ms=" + elapsed + counters.details();
    }
  }

  private static final class Counters {
    final long identityAttempts = NativeImageBacking.physicalIdentityAttemptsForTest();
    final long identityHits = NativeImageBacking.physicalIdentityHitsForTest();
    final long identityFallbacks = NativeImageBacking.physicalIdentityFallbacksForTest();
    final long variantLookups = NativeImageBacking.physicalVariantLookupsForTest();
    final long variantHits = NativeImageBacking.physicalVariantHitsForTest();
    final long variantStores = NativeImageBacking.physicalVariantMaterializationsForTest();
    final long targetColorAttempts = NativeImageBacking.targetColorAttemptsForTest();
    final long targetColorHits = NativeImageBacking.targetColorHitsForTest();
    final long targetColorFallbacks = NativeImageBacking.targetColorFallbacksForTest();
    final long writePixelsAttempts = NativeImageBacking.writePixelsAttemptsForTest();
    final long writePixelsHits = NativeImageBacking.writePixelsHitsForTest();
    final long genericGeometryDraws = ImageRasterBenchmarkSupport.genericGeometryDrawsForTest();
    final long smoothResampleDraws = ImageRasterBenchmarkSupport.smoothResampleDrawsForTest();

    static Counters capture() {
      return new Counters();
    }

    String details() {
      return ",physical_identity_attempts=" + identityAttempts
          + ",physical_identity_hits=" + identityHits
          + ",physical_identity_fallbacks=" + identityFallbacks
          + ",physical_variant_lookups=" + variantLookups
          + ",physical_variant_hits=" + variantHits
          + ",physical_variant_stores=" + variantStores
          + ",target_color_attempts=" + targetColorAttempts
          + ",target_color_hits=" + targetColorHits
          + ",target_color_fallbacks=" + targetColorFallbacks
          + ",write_pixels_attempts=" + writePixelsAttempts
          + ",write_pixels_hits=" + writePixelsHits
          + ",generic_geometry_draws=" + genericGeometryDraws
          + ",smooth_resample_draws=" + smoothResampleDraws;
    }
  }
}
