// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

      assertNotNull(coordinator.accept(1, surface, 1080, 2220, PORTRAIT, true,
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

      assertNull(coordinator.accept(2, surface, 1080, 2220, PORTRAIT, true,
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
      assertNotNull(coordinator.accept(2, replacement, 1080, 2220, PORTRAIT, true,
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

      assertNotNull(coordinator.accept(2, surface, 1080, 2220, LANDSCAPE, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
   }

   @Test
   public void acceptsKeyboardTransition()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object surface = new Object();
      accept(coordinator, surface, PORTRAIT, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      assertNotNull(coordinator.accept(2, surface, 1080, 2220, PORTRAIT, true,
            RotationRequestCoordinator.KEYBOARD_VISIBLE, RotationRequestCoordinator.LIFECYCLE_NORMAL));
      assertEquals(RotationRequestCoordinator.KEYBOARD_VISIBLE,
            coordinator.getLastAccepted().keyboardCategory);
   }

   @Test
   public void acceptsInteractiveStateChange()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object surface = new Object();
      accept(coordinator, surface, PORTRAIT, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      assertNotNull(coordinator.accept(2, surface, 1080, 2220, PORTRAIT, false,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
   }

   @Test
   public void acceptsActivityResume()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object surface = new Object();
      accept(coordinator, surface, PORTRAIT, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      assertNotNull(coordinator.accept(2, surface, 1080, 2220, PORTRAIT, true,
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
      assertNotNull(coordinator.accept(2, newest, 2220, 1080, LANDSCAPE, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));

      assertNull(coordinator.accept(3, newest, 2220, 1080, LANDSCAPE, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
      assertSame(newest, coordinator.getLastAccepted().surface);
      assertEquals(2220, coordinator.getLastAccepted().width);
      assertEquals(1, coordinator.getDuplicateCount());
   }

   @Test
   public void invalidatesOlderRapidPortraitLandscapePortraitTask()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object portraitSurface = new Object();
      Object landscapeSurface = new Object();
      RotationRequestCoordinator.Request portrait = coordinator.accept(1, portraitSurface,
            1080, 2220, PORTRAIT, true, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);
      RotationRequestCoordinator.Request landscape = coordinator.accept(2, landscapeSurface,
            2220, 1080, LANDSCAPE, true, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);
      RotationRequestCoordinator.Request finalPortrait = coordinator.accept(3, new Object(),
            1080, 2220, PORTRAIT, true, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      assertNotNull(portrait);
      assertNotNull(landscape);
      assertNotNull(finalPortrait);
      assertEquals(3, coordinator.getLastAccepted().generation);
      assertFalseCurrent(portrait, 3);
      assertFalseCurrent(landscape, 3);
      assertTrue(finalPortrait.isCurrent(3));
   }

   @Test
   public void preservesFinalRequestWhenOlderTaskStartsAfterNewSurface()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object firstSurface = new Object();
      Object finalSurface = new Object();
      RotationRequestCoordinator.Request first = coordinator.accept(10, firstSurface,
            1080, 2220, PORTRAIT, true, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);
      RotationRequestCoordinator.Request finalRequest = coordinator.accept(11, finalSurface,
            1080, 2220, PORTRAIT, true, RotationRequestCoordinator.KEYBOARD_HIDDEN,
            RotationRequestCoordinator.LIFECYCLE_NORMAL);

      assertNotNull(first);
      assertNotNull(finalRequest);
      assertFalseCurrent(first, 11);
      assertTrue(finalRequest.isCurrent(11));
      assertSame(finalSurface, coordinator.getLastAccepted().surface);
   }

   @Test
   public void keepsSentinelOperationsOutOfResizeCoordination()
   {
      RotationRequestCoordinator coordinator = new RotationRequestCoordinator();
      Object surface = new Object();

      assertNull(coordinator.accept(1, surface, -999, 100, PORTRAIT, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
      assertNull(coordinator.accept(2, surface, -998, 0, PORTRAIT, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
      assertNull(coordinator.accept(3, surface, -997, 0, PORTRAIT, true,
            RotationRequestCoordinator.KEYBOARD_HIDDEN, RotationRequestCoordinator.LIFECYCLE_NORMAL));
      assertNull(coordinator.getLastAccepted());
   }

   private static void assertFalseCurrent(RotationRequestCoordinator.Request request, int generation)
   {
      assertNotNull(request);
      assertTrue(!request.isCurrent(generation));
   }

   private static void accept(RotationRequestCoordinator coordinator, Object surface, int orientation,
         int keyboardCategory, int lifecycleCategory)
   {
      assertNotNull(coordinator.accept(1, surface, 1080, 2220, orientation, true,
            keyboardCategory, lifecycleCategory));
   }
}
