// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ImageDecodePolicyTest {
  @Test
  void targetDecodeIsTheOrdinaryPipelineDefault() {
    assertEquals(ImageDecodePolicy.Mode.TARGET_DECODE, ImageDecodePolicy.targetDecode().mode());
  }

  @Test
  void bestFitCapturesTargetDecodeChoiceAndLogicalSize() {
    ImageDecodePolicy policy = ImageDecodePolicy.bestFit(800, 600, 4, 200, 150);

    assertEquals(ImageDecodePolicy.Mode.BEST_FIT, policy.mode());
    assertEquals(800, policy.requestedWidth());
    assertEquals(600, policy.requestedHeight());
    assertEquals(4, policy.decodeDenominator());
    assertEquals(200, policy.logicalWidth());
    assertEquals(150, policy.logicalHeight());
  }

  @Test
  void explicitRatioKeepsTheRequestedRatioSeparateFromDecodeTier() {
    ImageDecodePolicy policy = ImageDecodePolicy.explicitRatio(2, 7, 2, 229, 172);

    assertEquals(ImageDecodePolicy.Mode.EXPLICIT_RATIO, policy.mode());
    assertEquals(2, policy.scaleNumerator());
    assertEquals(7, policy.scaleDenominator());
    assertEquals(2, policy.decodeDenominator());
    assertEquals(229, policy.logicalWidth());
    assertEquals(172, policy.logicalHeight());
  }

  @Test
  void rejectsInvalidFactoryPolicyValues() {
    assertThrows(IllegalArgumentException.class, () -> ImageDecodePolicy.bestFit(0, 600, 4, 200, 150));
    assertThrows(IllegalArgumentException.class, () -> ImageDecodePolicy.bestFit(800, 600, 3, 200, 150));
    assertThrows(IllegalArgumentException.class, () -> ImageDecodePolicy.explicitRatio(1, 0, 2, 200, 150));
  }
}
