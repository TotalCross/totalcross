// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.RuntimeDiagnosticSnapshot;
import totalcross.sys.RuntimeDiagnostics;
import totalcross.sys.Settings;
import totalcross.sys.runtime.RuntimeConfigurationStartup;
import totalcross.sys.runtime.ScrollRasterReuseTestSupport;
import totalcross.ui.gfx.Graphics;

class ScrollContainerRasterReuseTest {
  private static MainWindow mainWindow;
  private Fixture currentFixture;
  private boolean oldFingerTouch;
  private boolean oldPolicy;
  private int oldScreenWidth;
  private int oldScreenHeight;
  private int oldMainWindowWidth;
  private int oldMainWindowHeight;
  private int[] oldPixels;
  private boolean oldNeedsPaint;
  private Control temporaryOverlay;

  @BeforeAll
  static void initializeUi() {
    new Launcher();
    if (MainWindow.mainWindowInstance == null) {
      new MainWindow();
    }
    mainWindow = MainWindow.mainWindowInstance;
    selectBackend(GraphicsBackend.RASTER);
  }

  @BeforeEach
  void enableFeature() {
    oldScreenWidth = Settings.screenWidth;
    oldScreenHeight = Settings.screenHeight;
    oldMainWindowWidth = mainWindow.width;
    oldMainWindowHeight = mainWindow.height;
    oldPixels = Graphics.mainWindowPixels;
    oldNeedsPaint = Window.needsPaint;
    oldFingerTouch = Settings.fingerTouch;
    oldPolicy = ScrollRasterReuse.isEnabled();
    int screenWidth = Math.max(320, Math.max(Settings.screenWidth, mainWindow.width));
    int screenHeight = Math.max(480, Math.max(Settings.screenHeight, mainWindow.height));
    Settings.screenWidth = screenWidth;
    Settings.screenHeight = screenHeight;
    mainWindow.width = screenWidth;
    mainWindow.height = screenHeight;
    Graphics.configureMainWindowSurface(screenWidth, screenHeight, 1);
    Settings.fingerTouch = false;
    selectBackend(GraphicsBackend.RASTER);
    ScrollRasterReuseTestSupport.setEnabled(true);
  }

  @AfterEach
  void restoreFeature() {
    Settings.fingerTouch = oldFingerTouch;
    ScrollRasterReuseTestSupport.setEnabled(oldPolicy);
    if (currentFixture != null) {
      mainWindow.remove(currentFixture.sc);
      currentFixture = null;
    }
    if (temporaryOverlay != null) {
      mainWindow.remove(temporaryOverlay);
      temporaryOverlay = null;
    }
    ScrollRasterReuse.resetTestHooks();
    Settings.screenWidth = oldScreenWidth;
    Settings.screenHeight = oldScreenHeight;
    mainWindow.width = oldMainWindowWidth;
    mainWindow.height = oldMainWindowHeight;
    selectBackend(GraphicsBackend.RASTER);
    Graphics.configureMainWindowSurface(Math.max(0, oldScreenWidth), Math.max(0, oldScreenHeight), 1);
    Window.needsPaint = true;
    Window.repaintActiveWindows();
    Graphics.mainWindowPixels = oldPixels;
    Window.needsPaint = oldNeedsPaint;
  }

  @Test
  void enabledVerticalScrollMatchesForcedFullRepaint() {
    Fixture fixture = new Fixture();
    assertNotNull(Graphics.mainWindowPixels);
    assertNotNull(fixture.sc.bag0.getGraphics());
    assertTrue(fixture.sc.sbV.isVisible());
    int[] before = Graphics.mainWindowPixels.clone();
    Window.needsPaint = false;

    fixture.sc.scrollContent(0, 3, true);

    Graphics raster = fixture.sc.bag0.getGraphics();
    totalcross.ui.gfx.Rect clip = raster.getClip(new totalcross.ui.gfx.Rect());
    totalcross.ui.gfx.Coord translation = raster.getTranslation();
    assertTrue(clip.width > 0 && clip.height > 0, "invalid fixture clip: " + clip);
    assertNull(ScrollRasterReuse.lastFallbackReasonForTest(), "clip=" + clip + ", translation=" + translation
        + ", scale=" + raster.getContentScale() + ", surface=" + raster.getSurfacePixelWidth() + "x"
        + raster.getSurfacePixelHeight() + ", pitch=" + raster.getSurfacePixelPitch() + ", pixels="
        + (Graphics.mainWindowPixels == null ? -1 : Graphics.mainWindowPixels.length));
    assertFalse(Window.needsPaint);
    int[] reused = Graphics.mainWindowPixels.clone();
    Window.needsPaint = true;
    Window.repaintActiveWindows();
    assertArrayEquals(Graphics.mainWindowPixels, reused);
    assertPreservedAreaMatches(before, reused, fixture.sc, 3);
  }

  @Test
  void negativeAndRepeatedVerticalStepsMatchForcedFullRepaint() {
    Fixture fixture = new Fixture();
    fixture.sc.scrollContent(0, 10, true);
    assertReusedStepMatchesFullRepaint(fixture.sc, -3);
    assertReusedStepMatchesFullRepaint(fixture.sc, 11);
    assertReusedStepMatchesFullRepaint(fixture.sc, 7);
    assertReusedStepMatchesFullRepaint(fixture.sc, -4);
  }

  @Test
  void smoothScrollUpdateUsesTheSameSafeReusePath() {
    Fixture fixture = new Fixture();
    Window.needsPaint = false;
    int[] before = Graphics.mainWindowPixels.clone();

    fixture.sc.scrollContent(0, 5, false);
    fixture.sc.updateListenerTriggered(60);

    assertEquals(3, fixture.sc.sbV.getValue());
    assertNull(ScrollRasterReuse.lastFallbackReasonForTest());
    int[] reused = Graphics.mainWindowPixels.clone();
    Window.needsPaint = true;
    Window.repaintActiveWindows();
    assertArrayEquals(Graphics.mainWindowPixels, reused);
    assertPreservedAreaMatches(before, reused, fixture.sc, 3);
  }

  @Test
  void nearViewportDeltaRepaintsTheExposedAreaAndMatchesFullPaint() {
    Fixture fixture = new Fixture();
    assertReusedStepMatchesFullRepaint(fixture.sc, fixture.sc.bag0.height - 1);
  }

  @Test
  void policyDisabledPathLeavesRasterReuseStateAndFailureHookUntouched() {
    Fixture fixture = new Fixture();
    ScrollRasterReuse.recordFallback(ScrollRasterReuse.FallbackReason.NON_RASTER_BACKEND);
    ScrollRasterReuse.failNextMoveForTest();
    ScrollRasterReuseTestSupport.setEnabled(false);
    Window.needsPaint = false;

    fixture.sc.scrollContent(0, 3, true);

    assertEquals(ScrollRasterReuse.FallbackReason.NON_RASTER_BACKEND,
        ScrollRasterReuse.lastFallbackReasonForTest());
    assertTrue(Window.needsPaint);
    Window.repaintActiveWindows();
    int[] fullPaint = Graphics.mainWindowPixels.clone();
    assertEquals(3, fixture.sc.sbV.getValue());
    Window.needsPaint = true;
    Window.repaintActiveWindows();
    assertArrayEquals(fullPaint, Graphics.mainWindowPixels);

    ScrollRasterReuseTestSupport.setEnabled(true);
    Window.needsPaint = false;
    fixture.sc.scrollContent(0, 3, true);
    assertEquals(ScrollRasterReuse.FallbackReason.NATIVE_MOVE_FAILED,
        ScrollRasterReuse.lastFallbackReasonForTest());
    assertEquals(6, fixture.sc.sbV.getValue());
    assertFalse(Window.needsPaint);
    int[] recovered = Graphics.mainWindowPixels.clone();
    Window.needsPaint = true;
    Window.repaintActiveWindows();
    assertArrayEquals(recovered, Graphics.mainWindowPixels);
  }

  @Test
  void disabledPolicyDoesNotRecordRenderingAttemptOrFallback() {
    assumeTrue(RuntimeDiagnostics.isSupported());
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.RENDERING, true);
    try {
      Fixture fixture = new Fixture();
      ScrollRasterReuseTestSupport.setEnabled(false);
      RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();
      Window.needsPaint = false;

      fixture.sc.scrollContent(0, 3, true);

      RuntimeDiagnosticSnapshot delta = RuntimeDiagnostics.snapshot().deltaSince(before);
      assertEquals(0L, delta.getValue(RuntimeDiagnosticSnapshot.Domain.RENDERING,
          RuntimeDiagnosticSnapshot.Kind.COUNTER));
      assertTrue(Window.needsPaint);
    } finally {
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.RENDERING, false);
    }
  }

  @Test
  void resizedScrollContainerSelectsFullRepaint() {
    Fixture fixture = new Fixture();
    fixture.sc.changed = true;
    Window.needsPaint = false;

    fixture.sc.scrollContent(0, 3, true);

    assertEquals(ScrollRasterReuse.FallbackReason.FULL_REPAINT_REQUIRED,
        ScrollRasterReuse.lastFallbackReasonForTest());
    assertTrue(Window.needsPaint);
  }

  @Test
  void nonRasterBackendSelectsFullRepaint() {
    Fixture fixture = new Fixture();
    selectBackend(GraphicsBackend.GPU);
    Window.needsPaint = false;

    fixture.sc.scrollContent(0, 3, true);

    assertEquals(ScrollRasterReuse.FallbackReason.NON_RASTER_BACKEND,
        ScrollRasterReuse.lastFallbackReasonForTest());
    assertTrue(Window.needsPaint);
  }

  @Test
  void renderingDiagnosticsObserveReuseWithoutChangingFramebufferOutput() {
    assumeTrue(RuntimeDiagnostics.isSupported());
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.RENDERING, true);
    try {
      RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();
      Fixture fixture = new Fixture();
      assertReusedStepMatchesFullRepaint(fixture.sc, 3);
      RuntimeDiagnosticSnapshot delta = RuntimeDiagnostics.snapshot().deltaSince(before);

      assertEquals(2L, delta.getValue(RuntimeDiagnosticSnapshot.Domain.RENDERING,
          RuntimeDiagnosticSnapshot.Kind.COUNTER));
    } finally {
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.RENDERING, false);
    }
  }

  @Test
  void overlappingWindowSiblingSelectsFullRepaint() {
    Fixture fixture = new Fixture();
    temporaryOverlay = new Control();
    mainWindow.add(temporaryOverlay);
    temporaryOverlay.setRect(10, 10, 120, 100);
    Window.needsPaint = true;
    Window.repaintActiveWindows();
    Window.needsPaint = false;

    fixture.sc.scrollContent(0, 3, true);

    assertEquals(ScrollRasterReuse.FallbackReason.FULL_REPAINT_REQUIRED,
        ScrollRasterReuse.lastFallbackReasonForTest());
    mainWindow.remove(temporaryOverlay);
    temporaryOverlay = null;
  }

  @Test
  void pendingUnrelatedDamageSelectsTheExistingFullRepaintPath() {
    Fixture fixture = new Fixture();
    Window.needsPaint = true;

    fixture.sc.scrollContent(0, 3, true);

    assertEquals(ScrollRasterReuse.FallbackReason.PENDING_DAMAGE_CONFLICT,
        ScrollRasterReuse.lastFallbackReasonForTest());
    assertEquals(3, fixture.sc.sbV.getValue());
    assertTrue(Window.needsPaint);
  }

  @Test
  void failedMoveKeepsLogicalPositionAndImmediatelyRecoversByFullPaint() {
    Fixture fixture = new Fixture();
    Window.needsPaint = false;
    ScrollRasterReuse.failNextMoveForTest();

    fixture.sc.scrollContent(0, 3, true);

    assertEquals(ScrollRasterReuse.FallbackReason.NATIVE_MOVE_FAILED,
        ScrollRasterReuse.lastFallbackReasonForTest());
    assertEquals(3, fixture.sc.sbV.getValue());
    assertFalse(Window.needsPaint);
    int[] recovered = Graphics.mainWindowPixels.clone();
    Window.needsPaint = true;
    Window.repaintActiveWindows();
    assertArrayEquals(recovered, Graphics.mainWindowPixels);
  }

  private void assertReusedStepMatchesFullRepaint(ScrollContainer sc, int delta) {
    Window.needsPaint = false;
    int[] before = Graphics.mainWindowPixels.clone();
    sc.scrollContent(0, delta, true);
    assertNull(ScrollRasterReuse.lastFallbackReasonForTest());
    int[] reused = Graphics.mainWindowPixels.clone();
    Window.needsPaint = true;
    Window.repaintActiveWindows();
    assertArrayEquals(Graphics.mainWindowPixels, reused);
    assertPreservedAreaMatches(before, reused, sc, delta);
  }

  private static void selectBackend(GraphicsBackend backend) {
    RuntimeConfigurationStartup.initializeForSimulator(null, System.getProperty("os.name"),
        System.getProperty("os.arch"));
    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(backend);
  }

  private static void assertPreservedAreaMatches(int[] before, int[] after, ScrollContainer sc, int delta) {
    Graphics graphics = sc.bag0.getGraphics();
    totalcross.ui.gfx.Rect viewport = new totalcross.ui.gfx.Rect();
    totalcross.ui.gfx.Coord translation = graphics.getTranslation();
    graphics.getClip(viewport);
    int x = translation.x + viewport.x;
    int y = translation.y + viewport.y;
    int pitch = Graphics.getMainWindowPixelWidth();
    int magnitude = Math.abs(delta);
    assertTrue(pitch > 0 && viewport.width > 0 && viewport.height > magnitude);
    int firstDestination = delta > 0 ? 0 : magnitude;
    int firstSource = delta > 0 ? magnitude : 0;
    for (int row = 0; row < viewport.height - magnitude; row++) {
      int destinationY = y + firstDestination + row;
      int sourceY = y + firstSource + row;
      for (int column = 0; column < viewport.width; column++) {
        assertEquals(before[sourceY * pitch + x + column], after[destinationY * pitch + x + column],
            "delta=" + delta + ", viewport=" + x + "," + y + "," + viewport.width + "," + viewport.height
                + ", pixel=" + (x + column) + "," + destinationY + " sourceY=" + sourceY + ", value="
                + sc.sbV.value + ", bagY=" + sc.bag.y + ", childY=" + sc.bag.children.y + ", childHeight="
                + sc.bag.children.height);
      }
    }
  }

  private final class Fixture {
    final ScrollContainer sc = new ScrollContainer(false, true);

    Fixture() {
      currentFixture = this;
      mainWindow.add(sc);
      sc.setRect(10, 10, 120, 100);
      Container nestedContent = new Container();
      sc.add(nestedContent, 0, 0, 120, 300);
      nestedContent.add(new PatternControl(), 0, 0, 120, 300);
      sc.resize();
      Window.needsPaint = true;
      Window.repaintActiveWindows();
      Window.needsPaint = false;
    }
  }

  private static final class PatternControl extends Control {
    @Override
    public void onPaint(Graphics graphics) {
      for (int row = 0; row < height; row++) {
        graphics.backColor = (row * 977) & 0xFFFFFF;
        graphics.fillRect(0, row, width, 1);
      }
    }
  }
}
