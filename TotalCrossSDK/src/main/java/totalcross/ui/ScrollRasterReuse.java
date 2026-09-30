// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

/** Conservative planning and internal execution support for vertical raster reuse. */
final class ScrollRasterReuse {
  enum FallbackReason {
    POLICY_DISABLED,
    NON_RASTER_BACKEND,
    ZERO_OR_HORIZONTAL_SCROLL,
    DELTA_TOO_LARGE,
    INVALID_VIEWPORT,
    UNSUPPORTED_TRANSFORM,
    UNSUPPORTED_PIXEL_FORMAT,
    INVALID_FRAMEBUFFER,
    PENDING_DAMAGE_CONFLICT,
    NATIVE_MOVE_UNAVAILABLE,
    NATIVE_MOVE_FAILED,
    FULL_REPAINT_REQUIRED
  }

  static final class Rect {
    final int x;
    final int y;
    final int width;
    final int height;

    Rect(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof Rect)) {
        return false;
      }
      Rect rectangle = (Rect) other;
      return x == rectangle.x && y == rectangle.y && width == rectangle.width && height == rectangle.height;
    }

    @Override
    public int hashCode() {
      int result = x;
      result = 31 * result + y;
      result = 31 * result + width;
      return 31 * result + height;
    }
  }

  static final class Surface {
    final int width;
    final int height;
    final int stridePixels;
    final int bytesPerPixel;
    final boolean ready;
    final boolean stableStride;
    final boolean sourceValid;

    Surface(int width, int height, int stridePixels, int bytesPerPixel, boolean ready,
        boolean stableStride, boolean sourceValid) {
      this.width = width;
      this.height = height;
      this.stridePixels = stridePixels;
      this.bytesPerPixel = bytesPerPixel;
      this.ready = ready;
      this.stableStride = stableStride;
      this.sourceValid = sourceValid;
    }
  }

  static final class Plan {
    final FallbackReason fallbackReason;
    final Rect viewport;
    final Rect source;
    final Rect destination;
    final Rect exposed;
    final boolean copyBottomUp;

    private Plan(FallbackReason fallbackReason, Rect viewport, Rect source, Rect destination,
        Rect exposed, boolean copyBottomUp) {
      this.fallbackReason = fallbackReason;
      this.viewport = viewport;
      this.source = source;
      this.destination = destination;
      this.exposed = exposed;
      this.copyBottomUp = copyBottomUp;
    }

    boolean eligible() {
      return fallbackReason == null;
    }

    static Plan fallback(FallbackReason reason) {
      return new Plan(reason, null, null, null, null, false);
    }
  }

  private ScrollRasterReuse() {
  }

  /** Converts logical rectangle edges with the same nearest-edge rule as Graphics rasterization. */
  static Rect toPhysical(Rect logical, double scale) {
    if (logical == null || logical.width <= 0 || logical.height <= 0
        || !Double.isFinite(scale) || scale <= 0) {
      return null;
    }
    long left = scaleEdge(logical.x, scale);
    long top = scaleEdge(logical.y, scale);
    long right = scaleEdge((long) logical.x + logical.width, scale);
    long bottom = scaleEdge((long) logical.y + logical.height, scale);
    if (left < Integer.MIN_VALUE || left > Integer.MAX_VALUE || top < Integer.MIN_VALUE
        || top > Integer.MAX_VALUE || right < Integer.MIN_VALUE || right > Integer.MAX_VALUE
        || bottom < Integer.MIN_VALUE || bottom > Integer.MAX_VALUE) {
      return null;
    }
    long width = right - left;
    long height = bottom - top;
    if (width <= 0 || width > Integer.MAX_VALUE || height <= 0 || height > Integer.MAX_VALUE) {
      return null;
    }
    return new Rect((int) left, (int) top, (int) width, (int) height);
  }

  private static long scaleEdge(long value, double scale) {
    double scaled = value * scale;
    if (!Double.isFinite(scaled) || scaled < Long.MIN_VALUE || scaled > Long.MAX_VALUE) {
      return Long.MIN_VALUE;
    }
    return Math.round(scaled);
  }

  /** Clips a physical rectangle to the half-open framebuffer bounds. */
  static Rect clip(Rect rectangle, int surfaceWidth, int surfaceHeight) {
    if (rectangle == null || rectangle.width <= 0 || rectangle.height <= 0
        || surfaceWidth <= 0 || surfaceHeight <= 0) {
      return null;
    }
    long left = Math.max(0L, rectangle.x);
    long top = Math.max(0L, rectangle.y);
    long right = Math.min((long) surfaceWidth, (long) rectangle.x + rectangle.width);
    long bottom = Math.min((long) surfaceHeight, (long) rectangle.y + rectangle.height);
    if (right <= left || bottom <= top) {
      return null;
    }
    return new Rect((int) left, (int) top, (int) (right - left), (int) (bottom - top));
  }

  static Plan plan(boolean policyEnabled, boolean rasterBackend, int dx, int dy,
      Rect logicalViewport, double contentScale, Surface surface, boolean unsupportedTransform,
      boolean pendingDamageConflict, boolean fullRepaintRequired) {
    if (!policyEnabled) {
      return Plan.fallback(FallbackReason.POLICY_DISABLED);
    }
    if (!rasterBackend) {
      return Plan.fallback(FallbackReason.NON_RASTER_BACKEND);
    }
    if (dx != 0 || dy == 0) {
      return Plan.fallback(FallbackReason.ZERO_OR_HORIZONTAL_SCROLL);
    }
    if (unsupportedTransform || !Double.isFinite(contentScale) || contentScale <= 0
        || contentScale != Math.rint(contentScale) || contentScale > Integer.MAX_VALUE) {
      return Plan.fallback(FallbackReason.UNSUPPORTED_TRANSFORM);
    }
    if (logicalViewport == null || logicalViewport.width <= 0 || logicalViewport.height <= 0) {
      return Plan.fallback(FallbackReason.INVALID_VIEWPORT);
    }
    Rect physicalViewport = toPhysical(logicalViewport, contentScale);
    if (physicalViewport == null) {
      return Plan.fallback(FallbackReason.INVALID_VIEWPORT);
    }
    if (surface == null || !surface.ready || !surface.stableStride || surface.width <= 0
        || surface.height <= 0 || surface.stridePixels < surface.width) {
      return Plan.fallback(FallbackReason.INVALID_FRAMEBUFFER);
    }
    if (surface.bytesPerPixel != Integer.BYTES) {
      return Plan.fallback(FallbackReason.UNSUPPORTED_PIXEL_FORMAT);
    }
    if (!surface.sourceValid) {
      return Plan.fallback(FallbackReason.INVALID_FRAMEBUFFER);
    }
    Rect clippedViewport = clip(physicalViewport, surface.width, surface.height);
    if (clippedViewport == null) {
      return Plan.fallback(FallbackReason.INVALID_VIEWPORT);
    }
    if (pendingDamageConflict) {
      return Plan.fallback(FallbackReason.PENDING_DAMAGE_CONFLICT);
    }
    if (fullRepaintRequired) {
      return Plan.fallback(FallbackReason.FULL_REPAINT_REQUIRED);
    }

    long physicalDelta = (long) dy * (long) contentScale;
    long magnitude = Math.abs(physicalDelta);
    if (magnitude >= clippedViewport.height) {
      return Plan.fallback(FallbackReason.DELTA_TOO_LARGE);
    }
    long sourceY = physicalDelta > 0 ? (long) clippedViewport.y + physicalDelta : clippedViewport.y;
    long destinationY = physicalDelta < 0 ? (long) clippedViewport.y - physicalDelta : clippedViewport.y;
    long copyHeight = (long) clippedViewport.height - magnitude;
    if (sourceY < 0 || destinationY < 0 || copyHeight <= 0
        || sourceY + copyHeight > surface.height || destinationY + copyHeight > surface.height) {
      return Plan.fallback(FallbackReason.INVALID_VIEWPORT);
    }

    Rect source = new Rect(clippedViewport.x, (int) sourceY, clippedViewport.width, (int) copyHeight);
    Rect destination = new Rect(clippedViewport.x, (int) destinationY, clippedViewport.width, (int) copyHeight);
    Rect exposed;
    if (physicalDelta > 0) {
      exposed = new Rect(clippedViewport.x, clippedViewport.y + clippedViewport.height - (int) magnitude,
          clippedViewport.width, (int) magnitude);
    } else {
      exposed = new Rect(clippedViewport.x, clippedViewport.y, clippedViewport.width, (int) magnitude);
    }
    return new Plan(null, clippedViewport, source, destination, exposed, destinationY > sourceY);
  }
}
