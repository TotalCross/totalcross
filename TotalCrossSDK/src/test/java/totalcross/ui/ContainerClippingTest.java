// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;
import totalcross.ui.gfx.Rect;

class ContainerClippingTest {
  private int originalWidth;
  private int originalHeight;

  @BeforeAll
  static void initializeFontBackend() {
    new Launcher();
  }

  @BeforeEach
  void setScreenSize() {
    originalWidth = Settings.screenWidth;
    originalHeight = Settings.screenHeight;
    Settings.screenWidth = 100;
    Settings.screenHeight = 100;
  }

  @AfterEach
  void restoreScreenSize() {
    Settings.screenWidth = originalWidth;
    Settings.screenHeight = originalHeight;
  }

  @Test
  void ancestorClippingDefaultsOnAndCanBeDisabledPerContainer() {
    Window window = new Window();
    window.setRect(0, 0, 100, 100);
    Container outer = new Container();
    Container inner = new Container();
    Control child = new Control();
    window.add(outer, 10, 10, 80, 80);
    outer.add(inner, 20, 20, 20, 20);
    inner.add(child, -30, -30, 100, 100);
    Settings.screenWidth = 100;
    Settings.screenHeight = 100;

    assertEquals(new Rect(30, 30, 20, 20), child.getGraphics().getClip(new Rect()));

    inner.setClipChildrenToBounds(false);
    assertEquals(new Rect(10, 10, 80, 80), child.getGraphics().getClip(new Rect()));
  }
}
