// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys.runtime;

/** Test-only policy fixture for the deployed image prefetch worker smoke. */
public final class ImagePrefetchWorkerSmokeTestSupport {
  private static ImageRuntimePolicy originalPolicy;

  private ImagePrefetchWorkerSmokeTestSupport() {
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
