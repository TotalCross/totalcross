// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross;

/**
 * Tracks the last resize request accepted for the native rendering pipeline.
 * Surface identity is deliberately compared by reference, not by equals().
 */
final class RotationRequestCoordinator
{
   static final int KEYBOARD_HIDDEN = 0;
   static final int KEYBOARD_VISIBLE = 1;

   static final int LIFECYCLE_NORMAL = 0;
   static final int LIFECYCLE_SAFE_AREA = 1;
   static final int LIFECYCLE_RESUMED = 2;

   private Request lastAccepted;
   private int duplicateCount;

   boolean accept(Object surface, int width, int height, int orientation, boolean interactive,
         int keyboardCategory, int lifecycleCategory)
   {
      Request candidate = new Request(surface, width, height, orientation, interactive,
            keyboardCategory, lifecycleCategory);
      if (lastAccepted != null && lastAccepted.matches(candidate))
      {
         duplicateCount++;
         return false;
      }
      lastAccepted = candidate;
      return true;
   }

   Request getLastAccepted()
   {
      return lastAccepted;
   }

   int getDuplicateCount()
   {
      return duplicateCount;
   }

   static final class Request
   {
      final Object surface;
      final int width;
      final int height;
      final int orientation;
      final boolean interactive;
      final int keyboardCategory;
      final int lifecycleCategory;

      Request(Object surface, int width, int height, int orientation, boolean interactive,
            int keyboardCategory, int lifecycleCategory)
      {
         this.surface = surface;
         this.width = width;
         this.height = height;
         this.orientation = orientation;
         this.interactive = interactive;
         this.keyboardCategory = keyboardCategory;
         this.lifecycleCategory = lifecycleCategory;
      }

      private boolean matches(Request other)
      {
         return surface == other.surface
               && width == other.width
               && height == other.height
               && orientation == other.orientation
               && interactive == other.interactive
               && keyboardCategory == other.keyboardCategory
               && lifecycleCategory == other.lifecycleCategory;
      }
   }
}
