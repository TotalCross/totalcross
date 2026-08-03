// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator.awt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

class AwtWindowTest {
  @Test
  void preservesExplicitScaleAndDensity() {
    WindowConfiguration config = new WindowConfiguration(640, 960, 0.75, 2, "test", 0, 0, false, true, null, null,
        null);

    assertEquals(0.375, AwtWindow.resolveScale(config), 0.000001);
  }

  @Test
  void fitsAutomaticScaleToDisplayWithUsableMargin() {
    WindowConfiguration config = new WindowConfiguration(1920, 1080, -1, 1, "test", 0, 0, false, true, null, null,
        null);

    assertEquals(0.88 / 1.2, AwtWindow.resolveScale(config, new Rectangle(0, 0, 1600, 1200)), 0.000001);
  }

  @Test
  void usesDensityWhenAutomaticScaleCannotInspectDisplay() {
    WindowConfiguration config = new WindowConfiguration(1440, 2560, -1, 2, "test", 0, 0, false, true, null, null,
        null);

    assertEquals(0.5, AwtWindow.resolveScale(config, null), 0.000001);
  }
}
