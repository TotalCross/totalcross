// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SyntheticPacingTest {
  @Test
  void preservesTheLegacySixteenMillisecondTarget() {
    assertEquals(16_000_000L,
        ImageScrollRealWorkloadBenchmarkApp.syntheticPacingIntervalNs(
            "synthetic-current-16ms"));
  }

  @Test
  void targetsSixtyHertzAtExactlySixteenMillionSixHundredSixtySixThousandSixHundredSixtySevenNs() {
    assertEquals(16_666_667L,
        ImageScrollRealWorkloadBenchmarkApp.syntheticPacingIntervalNs("synthetic-60hz"));
  }

  @Test
  void measuresSleepOvershootAndClampsOnlyNegativeNoise() {
    assertEquals(750_000L,
        ImageScrollRealWorkloadBenchmarkApp.sleepOvershootNs(1_000_000L, 1_750_000L));
    assertEquals(0L,
        ImageScrollRealWorkloadBenchmarkApp.sleepOvershootNs(1_000_000L, 999_999L));
  }

  @Test
  void measuresDeadlineErrorFromTheConfiguredAbsoluteDeadline() {
    assertEquals(3_000L,
        ImageScrollRealWorkloadBenchmarkApp.deadlineErrorNs(50_000L, 53_000L));
    assertEquals(-2_000L,
        ImageScrollRealWorkloadBenchmarkApp.deadlineErrorNs(50_000L, 48_000L));
  }

  @Test
  void keepsFrameThresholdsIndependentFromThePacingInterval() {
    long[] expectedThresholds = {
        16_670_000L, 20_000_000L, 25_000_000L, 33_300_000L,
        50_000_000L, 100_000_000L
    };
    assertArrayEquals(expectedThresholds,
        ImageScrollRealWorkloadBenchmarkApp.frameThresholdsNs());

    long[] frameTimesNs = {16_670_001L, 20_000_001L, 25_000_001L,
        33_300_001L, 50_000_001L, 100_000_001L};
    int[] expectedCounts = {6, 5, 4, 3, 2, 1};
    assertEquals(16_000_000L,
        ImageScrollRealWorkloadBenchmarkApp.syntheticPacingIntervalNs(
            "synthetic-current-16ms"));
    for (int i = 0; i < expectedThresholds.length; i++) {
      assertEquals(expectedCounts[i],
          ImageScrollRealWorkloadBenchmarkApp.countFramesOverThresholdNs(
              frameTimesNs, expectedThresholds[i]));
    }
    assertEquals(16_666_667L,
        ImageScrollRealWorkloadBenchmarkApp.syntheticPacingIntervalNs("synthetic-60hz"));
    for (int i = 0; i < expectedThresholds.length; i++) {
      assertEquals(expectedCounts[i],
          ImageScrollRealWorkloadBenchmarkApp.countFramesOverThresholdNs(
              frameTimesNs, expectedThresholds[i]));
    }
  }
}
