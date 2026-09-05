// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.ArrayList;

/** Chooses a conservative JPEG decode denominator for one semantic pipeline. */
final class ImageDecodeRequirement {
  private ImageDecodeRequirement() {
  }

  static int choose(EncodedImageSource source, ImagePipeline pipeline,
      int requestedPhysicalWidth, int requestedPhysicalHeight) {
    if (source == null || pipeline == null
        || source.getFormat() != ImageEncodedStructure.Format.JPEG
        || requestedPhysicalWidth <= 0 || requestedPhysicalHeight <= 0) {
      return 1;
    }

    int visibleWidth = source.getIntrinsicWidth();
    int visibleHeight = source.getIntrinsicHeight();
    ArrayList<ImagePipeline> nodes = new ArrayList<ImagePipeline>();
    for (ImagePipeline node = pipeline; node.previous() != null; node = node.previous()) {
      nodes.add(node);
    }
    boolean transformed = false;
    for (int index = nodes.size() - 1; index >= 0; index--) {
      ImagePipeline node = nodes.get(index);
      switch (node.operationType()) {
      case ImagePipeline.FRAME_SELECT:
        if (node.parameter1() < 0 || source.getFrameCount() <= 1) {
          return 1;
        }
        visibleWidth = source.getIntrinsicWidth() / source.getFrameCount();
        break;
      case ImagePipeline.FRAME_LAYOUT:
        if (node.parameter1() <= 0 || node.parameter2() <= 0
            || source.getIntrinsicWidth() % node.parameter1() != 0) {
          return 1;
        }
        visibleWidth = source.getIntrinsicWidth() / node.parameter1();
        break;
      case ImagePipeline.CROP:
        if (transformed || node.parameter1() < 0 || node.parameter2() < 0
            || node.parameter3() <= 0 || node.parameter4() <= 0
            || (long) node.parameter1() + node.parameter3() > visibleWidth
            || (long) node.parameter2() + node.parameter4() > visibleHeight) {
          return 1;
        }
        visibleWidth = node.parameter3();
        visibleHeight = node.parameter4();
        break;
      case ImagePipeline.SCALE:
        return 1;
      case ImagePipeline.SMOOTH_SCALE:
        transformed = true;
        break;
      case ImagePipeline.ROTATE_SCALE:
        // The inverse rotated extent is not represented in ImagePipeline metadata.
        // Full decode is the safe choice when rotation can inflate the bounding box.
        return 1;
      case ImagePipeline.TOUCH_UP:
      case ImagePipeline.FADE:
      case ImagePipeline.ALPHA:
      case ImagePipeline.APPLY_COLOR:
      case ImagePipeline.APPLY_COLOR2:
      case ImagePipeline.APPLY_FADE:
      case ImagePipeline.CHANGE_COLORS:
      case ImagePipeline.SET_TRANSPARENT_COLOR:
        if (!transformed) {
          return 1;
        }
        break;
      default:
        return 1;
      }
    }
    if (visibleWidth <= 0 || visibleHeight <= 0) {
      return 1;
    }

    double requiredFraction = Math.min(1,
        Math.max((double) requestedPhysicalWidth / visibleWidth,
            (double) requestedPhysicalHeight / visibleHeight));
    int denominator = requiredFraction <= 0.125 ? 8
        : requiredFraction <= 0.25 ? 4
        : requiredFraction <= 0.5 ? 2 : 1;
    while (denominator > 1 && !coversRequested(source.getIntrinsicWidth(), source.getIntrinsicHeight(),
        denominator, requestedPhysicalWidth, requestedPhysicalHeight)) {
      denominator /= 2;
    }
    return denominator;
  }

  private static boolean coversRequested(int sourceWidth, int sourceHeight, int denominator,
      int requestedWidth, int requestedHeight) {
    return ceilDiv(sourceWidth, denominator) >= requestedWidth
        && ceilDiv(sourceHeight, denominator) >= requestedHeight;
  }

  private static int ceilDiv(int numerator, int denominator) {
    return (int) (((long) numerator + denominator - 1) / denominator);
  }
}
