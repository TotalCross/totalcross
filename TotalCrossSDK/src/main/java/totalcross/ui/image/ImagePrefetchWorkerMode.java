// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.image;

/** Selects the worker used for explicit asynchronous image preparation. */
public enum ImagePrefetchWorkerMode {
  /** Leave this property unassigned so another matching rule or the runtime default can decide it. */
  DEFAULT,
  /** Use the legacy worker thread created for each image entry. */
  LEGACY_PER_ENTRY_THREAD,
  /** Use the shared semaphore-backed process worker. */
  SEMAPHORE_PROCESS_WORKER
}
