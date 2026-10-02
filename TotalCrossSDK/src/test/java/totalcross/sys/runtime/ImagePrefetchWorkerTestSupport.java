// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys.runtime;

/** Test-only fixture for selecting the internal image prefetch worker policy. */
public final class ImagePrefetchWorkerTestSupport {
  private static ImageRuntimePolicy originalPolicy;

  private ImagePrefetchWorkerTestSupport() {
  }

  public static void useSemaphoreWorker() {
    usePolicy(ImageRuntimePolicy.PrefetchWorkerPolicy.SEMAPHORE_PROCESS_WORKER);
  }

  public static void useLegacyWorker() {
    usePolicy(ImageRuntimePolicy.PrefetchWorkerPolicy.LEGACY_PER_ENTRY_THREAD);
  }

  private static void usePolicy(ImageRuntimePolicy.PrefetchWorkerPolicy workerPolicy) {
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
