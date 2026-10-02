// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys.runtime;

import totalcross.ui.image.ImagePrefetchWorkerMode;

/** Test-only fixture for selecting the internal image prefetch worker policy. */
public final class ImagePrefetchWorkerTestSupport {
  private static ImageRuntimePolicy originalPolicy;

  private ImagePrefetchWorkerTestSupport() {
  }

  public static void useSemaphoreWorker() {
    usePolicy(ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER);
  }

  public static void useLegacyWorker() {
    usePolicy(ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD);
  }

  private static void usePolicy(ImagePrefetchWorkerMode workerPolicy) {
    ImageRuntimePolicy current = ImageRuntimeConfigurationStartup.currentPolicy();
    if (originalPolicy == null) {
      originalPolicy = current;
    }
    ImageRuntimeConfigurationStartup.installInternalPolicy(current.withPrefetchWorkerPolicyForTest(workerPolicy));
  }

  public static void restore() {
    if (originalPolicy != null) {
      ImageRuntimeConfigurationStartup.installInternalPolicy(originalPolicy);
      originalPolicy = null;
    }
  }
}
