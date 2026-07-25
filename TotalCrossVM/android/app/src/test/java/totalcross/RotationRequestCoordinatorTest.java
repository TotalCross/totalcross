// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RotationRequestCoordinatorTest
{
   private static final int PORTRAIT = 1;
   private static final int LANDSCAPE = 2;

   @Test
   public void acceptsFirstRequestAndPreservesIt()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object surface = new Object();

      assertTrue(coordinator.accept(surface, 1080, 2220, PORTRAIT, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
      assertSame(surface, coordinator.getLastAccepted().surface);
      assertEquals(1080, coordinator.getLastAccepted().width);
      assertEquals(0, coordinator.getDuplicateCount());
   }

   @Test
   public void dropsExactDuplicate()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object surface = new Object();
      accept(coordinator, surface, PORTRAIT, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      assertFalse(coordinator.accept(surface, 1080, 2220, PORTRAIT, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
      assertEquals(1, coordinator.getDuplicateCount());
   }

   @Test
   public void acceptsSameSizeWithNewSurface()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      accept(coordinator, new Object(), PORTRAIT, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      Object replacement = new Object();
      assertTrue(coordinator.accept(replacement, 1080, 2220, PORTRAIT, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
      assertSame(replacement, coordinator.getLastAccepted().surface);
   }

   @Test
   public void acceptsOrientationChange()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object surface = new Object();
      accept(coordinator, surface, PORTRAIT, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      assertTrue(coordinator.accept(surface, 1080, 2220, LANDSCAPE, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
   }

   @Test
   public void acceptsKeyboardTransition()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object surface = new Object();
      accept(coordinator, surface, PORTRAIT, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      assertTrue(coordinator.accept(surface, 1080, 2220, PORTRAIT, true,
            RotationRequestCoordinator.KEYBOARD_VISIBLE, RotationRequestCoordinator.LIFECYCLE_NORMAL));
   }

   @Test
   public void acceptsInteractiveStateChange()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object surface = new Object();
      accept(coordinator, surface, PORTRAIT, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      assertTrue(coordinator.accept(surface, 1080, 2220, PORTRAIT, false,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
   }

   @Test
   public void acceptsActivityResume()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object surface = new Object();
      accept(coordinator, surface, PORTRAIT, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      assertTrue(coordinator.accept(surface, 1080, 2220, PORTRAIT, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_RESUMED));
   }

   @Test
   public void keepsNewestAcceptedRequestAfterDuplicate()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object first = new Object();
      Object newest = new Object();
      accept(coordinator, first, PORTRAIT, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);
      assertTrue(coordinator.accept(newest, 2220, 1080, LANDSCAPE, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));

      assertFalse(coordinator.accept(newest, 2220, 1080, LANDSCAPE, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
      assertSame(newest, coordinator.getLastAccepted().surface);
      assertEquals(2220, coordinator.getLastAccepted().width);
      assertEquals(1, coordinator.getDuplicateCount());
   }

   private static void accept(RotationRequestCoordinator coordinator, Object surface, int orientation,
         int keyboardCategory, int lifecycleCategory)
   {
      assertTrue(coordinator.accept(surface, 1080, 2220, orientation, true,
            keyboardCategory, lifecycleCategory));
   }
}
