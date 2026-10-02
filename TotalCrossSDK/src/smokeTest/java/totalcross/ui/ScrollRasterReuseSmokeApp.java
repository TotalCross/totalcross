// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import java.util.Arrays;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.Settings;
import totalcross.sys.runtime.RuntimeCondition;
import totalcross.sys.runtime.RuntimeConfiguration;
import totalcross.sys.runtime.RuntimeFeatureState;
import totalcross.sys.runtime.ScrollRasterReuseTestSupport;
import totalcross.sys.runtime.RuntimeWhen;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;
import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageRuntimeRule;

/** Native macOS correctness smoke for Image content inside a scrolling raster viewport. */
@RuntimeConfiguration
@ImageRuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.RASTER)
}), scrollRasterReuse = RuntimeFeatureState.ENABLED)
public final class ScrollRasterReuseSmokeApp extends MainWindow {
  private ScrollContainer scroll;
  private boolean lastStepReused;
  private boolean smokeStarted;
  private String comparisonDiagnostics = "";
  private String preservedMismatch = "";

  @Override
  public void initUI() {
    try {
      int width = Math.max(160, Settings.screenWidth);
      int height = Math.max(160, Settings.screenHeight);
      scroll = new ScrollContainer(false, true);
      add(scroll);
      scroll.setRect(0, 0, width, height);
      int imageWidth = Math.max(80, scroll.width - 24);
      int imageHeight = Math.max(480, scroll.height * 4);
      Image image = patternedImage(imageWidth, imageHeight);
      scroll.add(new ImageControl(image), 0, 0, imageWidth, imageHeight);
      scroll.resize();
      addTimerListener(new TimerListener() {
        @Override
        public void timerTriggered(TimerEvent event) {
          if (!smokeStarted) {
            smokeStarted = true;
            runCorrectnessSmoke();
          }
        }
      });
      addTimer(500);
    } catch (Throwable failure) {
      finish(false, "setup=" + failure);
    }
  }

  private void runCorrectnessSmoke() {
    boolean productionPolicyEnabled = false;
    boolean productionScrollApplied = false;
    boolean productionScrollReused = false;
    boolean disabledPath = false;
    boolean enabledDown = false;
    boolean enabledUp = false;
    boolean nearViewport = false;
    boolean enabledDownReused = false;
    boolean enabledUpReused = false;
    boolean nearViewportReused = false;
    boolean nativePrimitive = false;
    boolean recoveredFailure = false;
    boolean conservativeScaleFallback = false;
    String failure = "";
    try {
      productionPolicyEnabled = ScrollRasterReuse.isEnabled();
      require(productionPolicyEnabled, "runtime configuration enables raster reuse");
      ScrollRasterReuse.resetTestHooks();
      repaintFull();
      Window.needsPaint = false;
      int productionBefore = scroll.sbV.getValue();
      scroll.scrollContent(0, 3, true);
      productionScrollApplied = scroll.sbV.getValue() != productionBefore;
      productionScrollReused = ScrollRasterReuse.lastFallbackReasonForTest() == null;
      require(productionScrollApplied, "configured vertical scroll is applied");
      require(productionScrollReused, "configured eligible vertical scroll reuses raster rows; reason="
          + ScrollRasterReuse.lastFallbackReasonForTest() + ",visible=" + scroll.visible + ",displayed="
          + scroll.isDisplayed() + ",changed=" + scroll.changed + ",offscreen=" + (scroll.bag.offscreen != null)
          + ",repainting=" + Window.isRepaintingActiveWindows() + ",vbar=" + scroll.sbV.isVisible()
          + ",openGl=" + Settings.isOpenGL + ",parentTop=" + (scroll.getParentWindow() == Window.topMost)
          + ",zStack=" + Window.zStack.size());
      repaintFull();
      scroll.scrollContent(0, -3, true);
      repaintFull();

      ScrollRasterReuse.recordFallback(ScrollRasterReuse.FallbackReason.NON_RASTER_BACKEND);
      ScrollRasterReuseTestSupport.setEnabled(false);
      repaintFull();
      Window.needsPaint = false;
      scroll.scrollContent(0, 3, true);
      require(ScrollRasterReuse.lastFallbackReasonForTest() == ScrollRasterReuse.FallbackReason.NON_RASTER_BACKEND,
          "disabled policy leaves raster reuse state untouched");
      require(Window.needsPaint, "disabled path schedules full repaint");
      disabledPath = matchesForcedFullPaint();
      require(disabledPath, "disabled path matches full repaint");
      ScrollRasterReuse.resetTestHooks();

      ScrollRasterReuseTestSupport.setEnabled(true);
      scroll.sbV.setVisible(false);
      if (scroll.sbH != null) {
        scroll.sbH.setVisible(false);
      }
      repaintFull();
      Window.needsPaint = false;
      nativePrimitive = exerciseNativeMovePrimitive();
      repaintFull();
      Window.needsPaint = false;
      enabledDown = compareStepWithFullPaint(4);
      enabledDownReused = lastStepReused;
      enabledUp = compareStepWithFullPaint(-2);
      enabledUpReused = lastStepReused;
      nearViewport = compareStepWithFullPaint(scroll.bag0.height - 1);
      nearViewportReused = lastStepReused;

      repaintFull();
      Window.needsPaint = false;
      ScrollRasterReuse.failNextMoveForTest();
      int failureDelta = scroll.sbV.getValue() > scroll.sbV.getMinimum() ? -1 : 1;
      double nativeScale = scroll.bag0.getGraphics().getContentScale();
      if (ScrollRasterReuse.isUnsupportedNativeScale(nativeScale)) {
        boolean scaleFallbackScrollApplied = scroll.scrollContent(0, failureDelta, true);
        ScrollRasterReuse.FallbackReason scaleFallbackReason = ScrollRasterReuse.lastFallbackReasonForTest();
        require(scaleFallbackScrollApplied, "unsupported scale scroll applied; value=" + scroll.sbV.getValue());
        require(scaleFallbackReason == ScrollRasterReuse.FallbackReason.UNSUPPORTED_TRANSFORM,
            "native scale above one remains ineligible; reason=" + scaleFallbackReason + ",scale=" + nativeScale);
        require(Window.needsPaint, "unsupported native scale schedules full repaint");
        conservativeScaleFallback = matchesForcedFullPaint();
        require(conservativeScaleFallback, "unsupported native scale fallback matches full repaint");
      } else {
        boolean failureScrollApplied = scroll.scrollContent(0, failureDelta, true);
        ScrollRasterReuse.FallbackReason failureReason = ScrollRasterReuse.lastFallbackReasonForTest();
        require(failureScrollApplied, "forced move failure scroll applied; value=" + scroll.sbV.getValue());
        require(failureReason == ScrollRasterReuse.FallbackReason.NATIVE_MOVE_FAILED,
            "forced move failure is reported; reason=" + failureReason + ",visible=" + scroll.visible
                + ",displayed=" + scroll.isDisplayed() + ",changed=" + scroll.changed + ",offscreen="
                + (scroll.bag.offscreen != null) + ",repainting=" + Window.isRepaintingActiveWindows()
                + ",vbar=" + scroll.sbV.isVisible() + ",openGl=" + Settings.isOpenGL + ",parentTop="
                + (scroll.getParentWindow() == Window.topMost) + ",zStack=" + Window.zStack.size());
        require(!Window.needsPaint, "failed move is immediately repainted");
        recoveredFailure = matchesForcedFullPaint();
        require(recoveredFailure, "failed move recovery matches full repaint");
      }
    } catch (Throwable smokeFailure) {
      failure = smokeFailure.toString();
    } finally {
      ScrollRasterReuse.resetTestHooks();
      ScrollRasterReuseTestSupport.setEnabled(false);
    }
    boolean fallbackSafe = recoveredFailure || conservativeScaleFallback;
    boolean pass = productionPolicyEnabled && productionScrollApplied && productionScrollReused
        && disabledPath && enabledDown && enabledUp && nearViewport && fallbackSafe
        && failure.length() == 0;
    boolean nativeReuse = enabledDownReused && enabledUpReused && nearViewportReused;
    System.out.println("fixture=ScrollRasterReuseSmokeApp,productionPolicyEnabled=" + productionPolicyEnabled
        + ",productionScrollApplied=" + productionScrollApplied + ",productionScrollReused="
        + productionScrollReused + ",disabledPath="
        + disabledPath + ",enabledDown=" + enabledDown + ",enabledUp=" + enabledUp + ",nearViewport="
        + nearViewport + ",nativeReuse=" + nativeReuse + ",nativePrimitive=" + nativePrimitive
        + ",recoveredFailure=" + recoveredFailure + ",conservativeScaleFallback=" + conservativeScaleFallback
        + ",fallbackSafe=" + fallbackSafe
        + ",overallPass=" + pass
        + (failure.length() == 0 ? "" : ",failure=" + failure.replace(' ', '_'))
        + (pass || comparisonDiagnostics.length() == 0 ? ""
            : ",comparison=" + comparisonDiagnostics.replace(' ', '_')));
    exit(pass ? 0 : 1);
  }

  private boolean exerciseNativeMovePrimitive() {
    Graphics graphics = scroll.bag0.getGraphics();
    int[] pixels = Graphics.mainWindowPixels;
    if (graphics == null || pixels == null) {
      return false;
    }
    int width = graphics.getSurfacePixelWidth();
    int height = graphics.getSurfacePixelHeight();
    int pitch = graphics.getSurfacePixelPitch();
    if (width <= 96 || height <= 96 || pitch < width || (long) pitch * height > pixels.length) {
      return false;
    }
    int[] original = pixels.clone();
    ScrollRasterReuse.Surface surface = new ScrollRasterReuse.Surface(width, height, pitch, Integer.BYTES,
        true, pitch == width, true);
    int x = 32;
    int y = 32;
    int rectWidth = 48;
    int rectHeight = 32;
    boolean passed = true;
    try {
      for (int delta : new int[] { 3, -3 }) {
        System.arraycopy(original, 0, pixels, 0, original.length);
        for (int row = 0; row < rectHeight; row++) {
          for (int column = 0; column < rectWidth; column++) {
            pixels[(y + row) * pitch + x + column] = 0xFF000000 | ((row * 977 + column * 131) & 0xFFFFFF);
          }
        }
        int[] before = pixels.clone();
        ScrollRasterReuse.Plan plan = ScrollRasterReuse.plan(true, true, 0, delta,
            new ScrollRasterReuse.Rect(x, y, rectWidth, rectHeight), 1.0, surface, false, false, false);
        if (!plan.eligible() || ScrollRasterReuse.move(plan, surface) != ScrollRasterReuse.MOVE_SUCCEEDED) {
          passed = false;
          break;
        }
        for (int row = 0; row < plan.source.height; row++) {
          int source = (plan.source.y + row) * pitch + plan.source.x;
          int destination = (plan.destination.y + row) * pitch + plan.destination.x;
          for (int column = 0; column < plan.source.width; column++) {
            if (pixels[destination + column] != before[source + column]) {
              passed = false;
              break;
            }
          }
          if (!passed) {
            break;
          }
        }
        System.arraycopy(original, 0, pixels, 0, original.length);
        Window.needsPaint = false;
        Window.repaintActiveWindows();
        if (!passed) {
          break;
        }
      }
    } finally {
      System.arraycopy(original, 0, pixels, 0, original.length);
      Window.needsPaint = false;
      Window.repaintActiveWindows();
    }
    return passed;
  }

  private boolean compareStepWithFullPaint(int delta) {
    repaintFull();
    Window.needsPaint = false;
    int[] before = Graphics.mainWindowPixels.clone();
    int oldValue = scroll.sbV.getValue();
    scroll.scrollContent(0, delta, true);
    require(scroll.sbV.getValue() != oldValue, "scroll delta is applied");
    boolean reused = ScrollRasterReuse.lastFallbackReasonForTest() == null;
    lastStepReused = reused;
    if (Window.needsPaint) {
      repaintFull();
    }
    int[] candidate = Graphics.mainWindowPixels.clone();
    repaintFull();
    int[] reference = Graphics.mainWindowPixels;
    boolean frameMatches = Arrays.equals(candidate, reference);
    boolean preservedMatches = true;
    preservedMismatch = "";
    if (reused) {
      preservedMatches = preservedAreaMatches(before, candidate, delta);
    }
    boolean matches = frameMatches && preservedMatches;
    if (!matches) {
      int first = 0;
      while (first < candidate.length && candidate[first] == reference[first]) {
        first++;
      }
      Graphics graphics = scroll.bag0.getGraphics();
      Rect viewport = graphics.getClip(new Rect());
      Coord translation = graphics.getTranslation();
      int pitch = graphics.getSurfacePixelPitch();
      int diffCount = 0;
      int minX = graphics.getSurfacePixelWidth();
      int minY = graphics.getSurfacePixelHeight();
      int maxX = -1;
      int maxY = -1;
      for (int index = 0; index < candidate.length; index++) {
        if (candidate[index] != reference[index]) {
          int x = index % pitch;
          int y = index / pitch;
          diffCount++;
          minX = Math.min(minX, x);
          minY = Math.min(minY, y);
          maxX = Math.max(maxX, x);
          maxY = Math.max(maxY, y);
        }
      }
      comparisonDiagnostics += "delta=" + delta + ":reused=" + reused + ":frame=" + frameMatches
          + ":preserved=" + preservedMatches + ":first=" + first + ":candidate="
          + (first < candidate.length ? candidate[first] : -1) + ":reference="
          + (first < reference.length ? reference[first] : -1) + ":before="
          + (first < before.length ? before[first] : -1) + ":pitch=" + graphics.getSurfacePixelPitch()
          + ":surface=" + graphics.getSurfacePixelWidth() + "/" + graphics.getSurfacePixelHeight()
          + ":screen=" + Settings.screenWidth + "/" + Settings.screenHeight + ":pixels=" + candidate.length
          + ":scale=" + graphics.getContentScale() + ":clip=" + viewport.x + "/" + viewport.y + "/"
          + viewport.width + "/" + viewport.height + ":translation=" + translation.x + "/" + translation.y
          + ":diff=" + diffCount + ":bounds=" + minX + "/" + minY + "/" + maxX + "/" + maxY
          + ":scroll=" + oldValue + "/" + scroll.sbV.getValue() + ":preservedAt=" + preservedMismatch + ";";
    }
    return matches;
  }

  private boolean matchesForcedFullPaint() {
    if (Window.needsPaint) {
      repaintFull();
    }
    int[] candidate = Graphics.mainWindowPixels.clone();
    repaintFull();
    return Arrays.equals(candidate, Graphics.mainWindowPixels);
  }

  private static void repaintFull() {
    Window.needsPaint = true;
    Window.repaintActiveWindows();
  }

  private boolean preservedAreaMatches(int[] before, int[] after, int delta) {
    Graphics graphics = scroll.bag0.getGraphics();
    if (graphics == null) {
      return false;
    }
    Rect clip = graphics.getClip(new Rect());
    Coord translation = graphics.getTranslation();
    double scale = graphics.getContentScale();
    if (!Double.isFinite(scale) || scale <= 0 || scale != Math.rint(scale)) {
      return false;
    }
    int x = (int) Math.round((translation.x + clip.x) * scale);
    int y = (int) Math.round((translation.y + clip.y) * scale);
    int width = (int) Math.round((translation.x + clip.x + clip.width) * scale) - x;
    int height = (int) Math.round((translation.y + clip.y + clip.height) * scale) - y;
    int physicalDelta = (int) Math.round(delta * scale);
    int magnitude = Math.abs(physicalDelta);
    int pitch = graphics.getSurfacePixelPitch();
    if (width <= 0 || height <= magnitude || pitch <= 0) {
      return false;
    }
    int firstDestination = physicalDelta > 0 ? 0 : magnitude;
    int firstSource = physicalDelta > 0 ? magnitude : 0;
    for (int row = 0; row < height - magnitude; row++) {
      int destinationY = y + firstDestination + row;
      int sourceY = y + firstSource + row;
      for (int column = 0; column < width; column++) {
        if (before[sourceY * pitch + x + column] != after[destinationY * pitch + x + column]) {
          preservedMismatch = "clip=" + clip.x + "/" + clip.y + "/" + clip.width + "/" + clip.height
              + ",translation=" + translation.x + "/" + translation.y + ",scale=" + scale + ",x=" + x + ",y="
              + y + ",sourceY=" + sourceY + ",destinationY=" + destinationY
              + ",beforeSource=" + before[sourceY * pitch + x + column] + ",afterDestination="
              + after[destinationY * pitch + x + column] + ",beforeDestination="
              + before[destinationY * pitch + x + column];
          return false;
        }
      }
    }
    return true;
  }

  private static Image patternedImage(int width, int height) throws Exception {
    Image image = new Image(width, height);
    Graphics graphics = image.getGraphics();
    for (int row = 0; row < height; row++) {
      graphics.backColor = 0xFF000000 | ((row * 977) & 0xFFFFFF);
      graphics.fillRect(0, row, width, 1);
    }
    return image;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private void finish(boolean pass, String detail) {
    System.out.println("fixture=ScrollRasterReuseSmokeApp,overallPass=" + pass + ",failure="
        + detail.replace(' ', '_'));
    ScrollRasterReuse.resetTestHooks();
    ScrollRasterReuseTestSupport.setEnabled(false);
    exit(pass ? 0 : 1);
  }
}
