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
    if (originalPolicy != null) {
      throw new IllegalStateException("image prefetch worker fixture is already active");
    }
    originalPolicy = ImageRuntimeConfigurationStartup.currentPolicy();
    ImageRuntimeConfigurationStartup.installInternalPolicy(originalPolicy.withPrefetchWorkerPolicyForTest(
        ImageRuntimePolicy.PrefetchWorkerPolicy.SEMAPHORE_PROCESS_WORKER));
  }

  public static void restore() {
    if (originalPolicy != null) {
      ImageRuntimeConfigurationStartup.installInternalPolicy(originalPolicy);
      originalPolicy = null;
    }
  }
}
