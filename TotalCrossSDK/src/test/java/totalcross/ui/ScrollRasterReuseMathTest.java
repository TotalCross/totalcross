// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import totalcross.ui.ScrollRasterReuse.FallbackReason;
import totalcross.ui.ScrollRasterReuse.Plan;
import totalcross.ui.ScrollRasterReuse.Rect;
import totalcross.ui.ScrollRasterReuse.Surface;

class ScrollRasterReuseMathTest {
  private static final Surface SURFACE = new Surface(100, 100, 100, Integer.BYTES, true, true, true);

  @Test
  void positiveContentDeltaMovesFramebufferUpAndExposesBottom() {
    Plan plan = plan(0, 3, new Rect(10, 20, 40, 30), 1, SURFACE);

    assertTrue(plan.eligible());
    assertEquals(new Rect(10, 20, 40, 30), plan.viewport);
    assertEquals(new Rect(10, 23, 40, 27), plan.source);
    assertEquals(new Rect(10, 20, 40, 27), plan.destination);
    assertEquals(new Rect(10, 47, 40, 3), plan.exposed);
    assertFalse(plan.copyBottomUp);
  }

  @Test
  void negativeContentDeltaMovesFramebufferDownAndCopiesBottomUp() {
    Plan plan = plan(0, -4, new Rect(3, 5, 20, 18), 1, SURFACE);

    assertTrue(plan.eligible());
    assertEquals(new Rect(3, 5, 20, 14), plan.source);
    assertEquals(new Rect(3, 9, 20, 14), plan.destination);
    assertEquals(new Rect(3, 5, 20, 4), plan.exposed);
    assertTrue(plan.copyBottomUp);
  }

  @Test
  void integralScaleConvertsLogicalEdgesAndScrollDeltaOnce() {
    Rect physical = ScrollRasterReuse.toPhysical(new Rect(3, 4, 7, 5), 2);
    Plan plan = plan(0, 2, new Rect(3, 4, 7, 5), 2,
        new Surface(50, 50, 50, Integer.BYTES, true, true, true));

    assertEquals(new Rect(6, 8, 14, 10), physical);
    assertTrue(plan.eligible());
    assertEquals(new Rect(6, 12, 14, 6), plan.source);
    assertEquals(new Rect(6, 14, 14, 4), plan.exposed);
  }

  @Test
  void physicalViewportClipsToSurfaceBounds() {
    Plan plan = plan(0, 1, new Rect(-2, 2, 8, 7), 1,
        new Surface(10, 10, 10, Integer.BYTES, true, true, true));

    assertTrue(plan.eligible());
    assertEquals(new Rect(0, 2, 6, 7), plan.viewport);
  }

  @Test
  void rejectsDeltaEqualToOrLargerThanViewport() {
    assertReason(FallbackReason.DELTA_TOO_LARGE, plan(0, 10, new Rect(0, 0, 20, 10), 1, SURFACE));
    assertReason(FallbackReason.DELTA_TOO_LARGE, plan(0, -11, new Rect(0, 0, 20, 10), 1, SURFACE));
  }

  @Test
  void rejectsEveryUnprovenEligibilityFactWithStableReason() {
    assertReason(FallbackReason.POLICY_DISABLED,
        ScrollRasterReuse.plan(false, true, 0, 1, new Rect(0, 0, 20, 20), 1, SURFACE, false, false, false));
    assertReason(FallbackReason.NON_RASTER_BACKEND,
        ScrollRasterReuse.plan(true, false, 0, 1, new Rect(0, 0, 20, 20), 1, SURFACE, false, false, false));
    assertReason(FallbackReason.ZERO_OR_HORIZONTAL_SCROLL,
        ScrollRasterReuse.plan(true, true, 0, 0, new Rect(0, 0, 20, 20), 1, SURFACE, false, false, false));
    assertReason(FallbackReason.ZERO_OR_HORIZONTAL_SCROLL,
        ScrollRasterReuse.plan(true, true, 1, 2, new Rect(0, 0, 20, 20), 1, SURFACE, false, false, false));
    assertReason(FallbackReason.UNSUPPORTED_TRANSFORM,
        ScrollRasterReuse.plan(true, true, 0, 1, new Rect(0, 0, 20, 20), 1.5, SURFACE, false, false, false));
    assertReason(FallbackReason.UNSUPPORTED_TRANSFORM,
        ScrollRasterReuse.plan(true, true, 0, 1, new Rect(0, 0, 20, 20), 1, SURFACE, true, false, false));
    assertReason(FallbackReason.INVALID_VIEWPORT,
        ScrollRasterReuse.plan(true, true, 0, 1, new Rect(0, 0, 0, 20), 1, SURFACE, false, false, false));
    assertReason(FallbackReason.UNSUPPORTED_PIXEL_FORMAT,
        ScrollRasterReuse.plan(true, true, 0, 1, new Rect(0, 0, 20, 20), 1,
            new Surface(100, 100, 100, 2, true, true, true), false, false, false));
    assertReason(FallbackReason.INVALID_FRAMEBUFFER,
        ScrollRasterReuse.plan(true, true, 0, 1, new Rect(0, 0, 20, 20), 1,
            new Surface(100, 100, 99, Integer.BYTES, true, true, true), false, false, false));
    assertReason(FallbackReason.INVALID_FRAMEBUFFER,
        ScrollRasterReuse.plan(true, true, 0, 1, new Rect(0, 0, 20, 20), 1,
            new Surface(100, 100, 100, Integer.BYTES, false, true, true), false, false, false));
    assertReason(FallbackReason.PENDING_DAMAGE_CONFLICT,
        ScrollRasterReuse.plan(true, true, 0, 1, new Rect(0, 0, 20, 20), 1, SURFACE, false, true, false));
    assertReason(FallbackReason.FULL_REPAINT_REQUIRED,
        ScrollRasterReuse.plan(true, true, 0, 1, new Rect(0, 0, 20, 20), 1, SURFACE, false, false, true));
    assertReason(FallbackReason.INVALID_FRAMEBUFFER,
        ScrollRasterReuse.plan(true, true, 0, 1, new Rect(0, 0, 20, 20), 1,
            new Surface(100, 100, 100, Integer.BYTES, true, true, false), false, false, false));
  }

  @Test
  void rejectsOverflowingCoordinateConversionsAndClipsEmptyRectangles() {
    assertNull(ScrollRasterReuse.toPhysical(new Rect(Integer.MAX_VALUE, 0, 1, 1), 2));
    assertNull(ScrollRasterReuse.toPhysical(new Rect(0, 0, 1, 1), Double.NaN));
    assertNull(ScrollRasterReuse.clip(new Rect(20, 20, 1, 1), 10, 10));
    assertReason(FallbackReason.INVALID_VIEWPORT,
        plan(0, 1, new Rect(Integer.MAX_VALUE, 0, 1, 10), 2, SURFACE));
  }

  @Test
  void overlapDirectionIsBasedOnSourceAndDestinationRows() {
    assertFalse(plan(0, 5, new Rect(0, 0, 10, 20), 1, SURFACE).copyBottomUp);
    assertTrue(plan(0, -5, new Rect(0, 0, 10, 20), 1, SURFACE).copyBottomUp);
  }

  private static Plan plan(int dx, int dy, Rect logicalViewport, double scale, Surface surface) {
    return ScrollRasterReuse.plan(true, true, dx, dy, logicalViewport, scale, surface, false, false, false);
  }

  private static void assertReason(FallbackReason expected, Plan plan) {
    assertFalse(plan.eligible());
    assertEquals(expected, plan.fallbackReason);
    assertNull(plan.source);
    assertNull(plan.destination);
  }
}
