// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import tc.preview.PreviewFrame;
import tc.preview.PreviewFrameSink;
import tc.simulator.Launcher;
import totalcross.ui.gfx.Color;

public class LegacySafeAreaVisualSmokeTest {
  private static final int PORTRAIT_TOP = 12;
  private static final int PORTRAIT_LEFT = 20;
  private static final int PORTRAIT_BOTTOM = 24;
  private static final int PORTRAIT_RIGHT = 36;
  private static final int LANDSCAPE_TOP = 8;
  private static final int LANDSCAPE_LEFT = 80;
  private static final int LANDSCAPE_BOTTOM = 12;
  private static final int LANDSCAPE_RIGHT = 30;

  @Test
  void capturesLegacyWindowsAcrossSafePortraitAndLandscape() throws Exception {
    RecordingSink sink = new RecordingSink();
    Launcher runtime = null;
    Path output = Paths.get("..", "artifacts", "legacy-safe-area-stabilization", "visual");
    Files.createDirectories(output);
    try {
      runtime = Launcher.startPreviewFrames(VisualApp.class.getName(), sink, getClass().getClassLoader(),
          "/scr", "320x480x32", "/density", "1");
      await(() -> VisualApp.instance != null && sink.size() > 0, "initial preview frame");
      VisualApp app = VisualApp.instance;
      assertEquals(320, sink.latest().getWidth());
      assertEquals(480, sink.latest().getHeight());
      assertSame(app, Window.getTopMost());

      TopMenu portraitMenu = showTopMenu(app, Control.LEFT, 220);
      captureAnimatedStage(sink, output, "portrait-top-menu-left", portraitMenu);
      dismissOutside(runtime, 300, 220, app);
      reopenTopMenu(app, portraitMenu);
      captureStable(sink, output.resolve("portrait-top-menu-left-reopen.png"));
      dismissOutside(runtime, 300, 220, app);

      SlidingWindow portraitSliding = showSliding(app, Control.BOTTOM);
      captureStable(sink, output.resolve("portrait-sliding-bottom.png"));
      dismissBack(runtime, app);
      assertTrue(portraitSliding.currentAnimation == null || Window.getTopMost() == app);
      reopenSliding(app, portraitSliding);
      captureStable(sink, output.resolve("portrait-sliding-bottom-reopen.png"));
      dismissBack(runtime, app);

      TopMenu portraitDrawer = showDrawer(app, Control.LEFT);
      captureStable(sink, output.resolve("portrait-side-menu-left.png"));
      assertTrue(portraitDrawer.widthInPixels == 208);
      dismissOutside(runtime, 300, 220, app);

      runtime.resizePreview(640, 320, 1);
      runUi(app, new Runnable() {
        @Override
        public void run() {
          app.applyLandscapeInsets();
        }
      });
      await(() -> sink.latest().getWidth() == 640 && sink.latest().getHeight() == 320,
          "landscape preview frame");

      TopMenu landscapeMenu = showTopMenu(app, Control.RIGHT, 260);
      captureAnimatedStage(sink, output, "landscape-top-menu-right", landscapeMenu);
      dismissOutside(runtime, 10, 150, app);

      showSliding(app, Control.LEFT);
      captureStable(sink, output.resolve("landscape-sliding-left.png"));
      dismissBack(runtime, app);

      TopMenu landscapeDrawer = showDrawer(app, Control.RIGHT);
      captureStable(sink, output.resolve("landscape-side-menu-right.png"));
      assertTrue(landscapeDrawer.widthInPixels == 320);
      dismissOutside(runtime, 10, 150, app);

      assertTrue(Files.size(output.resolve("portrait-top-menu-left.png")) > 0);
      assertTrue(Files.size(output.resolve("landscape-side-menu-right.png")) > 0);
    } finally {
      if (runtime != null) {
        runtime.stop();
      }
      Window.zStack.removeAllElements();
      Window.topMost = null;
      MainWindow.mainWindowInstance = null;
      Window._updateSafeAreaInsets(0, 0, 0, 0);
      VisualApp.instance = null;
    }
  }

  private static TopMenu showTopMenu(VisualApp app, final int direction, final int width) throws Exception {
    final AtomicReference<TopMenu> result = new AtomicReference<>();
    runUi(app, new Runnable() {
      @Override
      public void run() {
        TopMenu menu = new TopMenu(new Control[] {
            new TopMenu.Item("Inbox", (totalcross.ui.image.Image) null),
            new TopMenu.Item("Archive", (totalcross.ui.image.Image) null),
            new TopMenu.Item("Settings", (totalcross.ui.image.Image) null)
        }, direction, Window.NO_BORDER);
        menu.widthInPixels = width;
        menu.totalTime = 180;
        menu.setBackColor(0xFFF9F4);
        Label top = new Label("SAFE TOP BAR", Control.CENTER);
        top.setBackForeColors(0x263A5A, Color.WHITE);
        Label bottom = new Label("SAFE BOTTOM BAR", Control.CENTER);
        bottom.setBackForeColors(0x274E13, Color.WHITE);
        menu.setTopBar(top);
        menu.setBottomBar(bottom);
        menu.setRect(false);
        menu.popupNonBlocking();
        result.set(menu);
      }
    });
    await(() -> Window.getTopMost() == result.get(), "TopMenu popup");
    return result.get();
  }

  private static SlidingWindow showSliding(VisualApp app, final int direction) throws Exception {
    final AtomicReference<SlidingWindow> result = new AtomicReference<>();
    runUi(app, new Runnable() {
      @Override
      public void run() {
        SlidingWindow sliding = new SlidingWindow(new Presenter<Container>() {
          @Override
          public Container getView() {
            return new SafePanel("LEGACY SLIDING WINDOW", 0x1D3557);
          }
        });
        sliding.animDir = direction;
        sliding.totalTime = 180;
        sliding.prepareForPopup();
        sliding.popupNonBlocking();
        result.set(sliding);
      }
    });
    await(() -> Window.getTopMost() == result.get(), "SlidingWindow popup");
    Thread.sleep(260);
    return result.get();
  }

  private static TopMenu showDrawer(VisualApp app, final int direction) throws Exception {
    final AtomicReference<TopMenu> result = new AtomicReference<>();
    runUi(app, new Runnable() {
      @Override
      public void run() {
        SideMenuContainer side = new SideMenuContainer(direction, "LEGACY DRAWER", new Control[0]);
        side.topMenu.totalTime = 180;
        side.topMenu.setRect(false);
        side.topMenu.popupNonBlocking();
        result.set(side.topMenu);
      }
    });
    await(() -> Window.getTopMost() == result.get(), "SideMenu TopMenu popup");
    Thread.sleep(260);
    return result.get();
  }

  private static void reopenTopMenu(VisualApp app, TopMenu menu) throws Exception {
    runUi(app, new Runnable() {
      @Override
      public void run() {
        menu.setRect(false);
        menu.popupNonBlocking();
      }
    });
    await(() -> Window.getTopMost() == menu, "TopMenu reopen");
    Thread.sleep(260);
  }

  private static void reopenSliding(VisualApp app, SlidingWindow sliding) throws Exception {
    runUi(app, new Runnable() {
      @Override
      public void run() {
        sliding.prepareForPopup();
        sliding.popupNonBlocking();
      }
    });
    await(() -> Window.getTopMost() == sliding, "SlidingWindow reopen");
    Thread.sleep(260);
  }

  private static void captureAnimatedStage(RecordingSink sink, Path output, String name, TopMenu menu)
      throws Exception {
    int firstIndex = sink.size();
    await(() -> sink.size() >= firstIndex + 2, name + " animation frames");
    PreviewFrame transition = sink.get(firstIndex);
    Thread.sleep(260);
    PreviewFrame end = sink.latest();
    assertNotEquals(hash(transition), hash(end), name + " transition must change pixels");
    writePng(transition, output.resolve(name + "-transition.png"));
    writePng(end, output.resolve(name + ".png"));
    assertSame(menu, Window.getTopMost());
  }

  private static void captureStable(RecordingSink sink, Path path) throws Exception {
    Thread.sleep(80);
    writePng(sink.latest(), path);
  }

  private static void dismissOutside(Launcher runtime, int x, int y, VisualApp app) throws Exception {
    runtime.injectPreviewPointer(x, y, 1, true);
    runtime.injectPreviewPointer(x, y, 1, false);
    await(() -> Window.getTopMost() == app, "outside dismissal");
    Thread.sleep(220);
  }

  private static void dismissBack(Launcher runtime, VisualApp app) throws Exception {
    runtime.injectPreviewKey(totalcross.sys.SpecialKeys.ESCAPE, true, 0);
    runtime.injectPreviewKey(totalcross.sys.SpecialKeys.ESCAPE, false, 0);
    await(() -> Window.getTopMost() == app, "Back/Escape dismissal");
    Thread.sleep(220);
  }

  private static void runUi(VisualApp app, Runnable action) throws Exception {
    AtomicBoolean done = new AtomicBoolean();
    AtomicReference<Throwable> failure = new AtomicReference<>();
    app.runOnMainThread(new Runnable() {
      @Override
      public void run() {
        try {
          action.run();
          Window.repaintActiveWindows();
        } catch (Throwable throwable) {
          failure.set(throwable);
        } finally {
          done.set(true);
        }
      }
    });
    await(done::get, "UI command");
    if (failure.get() != null) {
      throw new AssertionError(failure.get());
    }
  }

  private static void await(BooleanSupplier condition, String description) throws Exception {
    long deadline = System.currentTimeMillis() + 5000;
    while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
      Thread.sleep(20);
    }
    assertTrue(condition.getAsBoolean(), "timed out waiting for " + description);
  }

  private static int hash(PreviewFrame frame) {
    int hash = 1;
    for (int pixel : frame.copyPixels()) {
      hash = 31 * hash + pixel;
    }
    return hash;
  }

  private static void writePng(PreviewFrame frame, Path path) throws Exception {
    BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, frame.getWidth(), frame.getHeight(), frame.copyPixels(), 0, frame.getStride());
    ImageIO.write(image, "png", path.toFile());
  }

  public static class VisualApp extends MainWindow {
    static volatile VisualApp instance;

    public VisualApp() {
      instance = this;
    }

    @Override
    public void initUI() {
      setBackColor(0xC2185B);
      setSafeAreaMode(SafeAreaMode.ENABLED);
      Window._updateSafeAreaInsets(PORTRAIT_TOP, PORTRAIT_LEFT, PORTRAIT_BOTTOM, PORTRAIT_RIGHT);
      SideMenuContainer base = new SideMenuContainer("LEGACY SAFE AREA", new Control[0]);
      base.setBackColor(0xE8EEF7);
      add(base, LEFT, TOP, FILL, FILL);
    }

    void applyLandscapeInsets() {
      Window._updateSafeAreaInsets(LANDSCAPE_TOP, LANDSCAPE_LEFT, LANDSCAPE_BOTTOM, LANDSCAPE_RIGHT);
      repositionChildren();
    }
  }

  private static final class SafePanel extends Container {
    private final String caption;

    SafePanel(String caption, int color) {
      this.caption = caption;
      setBackColor(color);
    }

    @Override
    public void initUI() {
      Label label = new Label(caption, CENTER);
      label.setForeColor(Color.WHITE);
      add(label, CENTER, CENTER);
    }
  }

  private static final class RecordingSink implements PreviewFrameSink {
    private final List<PreviewFrame> frames = new ArrayList<>();

    @Override
    public synchronized void present(PreviewFrame frame) {
      frames.add(frame);
    }

    synchronized int size() {
      return frames.size();
    }

    synchronized PreviewFrame get(int index) {
      return frames.get(index);
    }

    synchronized PreviewFrame latest() {
      return frames.get(frames.size() - 1);
    }
  }
}
