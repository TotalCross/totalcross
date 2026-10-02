// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** JavaSE test helper that joins and resets the package-private process worker. */
final class ImagePreparationSchedulerTestSupport {
  private ImagePreparationSchedulerTestSupport() {
  }

  static void shutdownAndReset() throws InterruptedException {
    Thread worker = ImagePreparationScheduler.processWorkerForTest();
    if (worker == null) {
      ImagePreparationScheduler.resetWorkerTestCountersForTest();
      return;
    }
    ImagePreparationScheduler.shutdownSemaphoreWorkerForTest();
    worker.join(5000L);
    if (worker.isAlive()) {
      throw new IllegalStateException("the image prefetch worker did not exit after shutdown");
    }
    ImagePreparationScheduler.resetSemaphoreWorkerForTest();
  }
}
