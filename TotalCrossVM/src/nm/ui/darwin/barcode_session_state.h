// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TC_BARCODE_SESSION_STATE_H
#define TC_BARCODE_SESSION_STATE_H

#include <stdbool.h>
#include <stdint.h>

typedef enum
{
   TCBarcodeSessionIdle,
   TCBarcodeSessionRequestingPermission,
   TCBarcodeSessionPresenting,
   TCBarcodeSessionConfiguring,
   TCBarcodeSessionRunning,
   TCBarcodeSessionFinishing,
   TCBarcodeSessionFinished
} TCBarcodeSessionState;

static inline bool tcBarcodeSessionCanStart(TCBarcodeSessionState state)
{
   return state == TCBarcodeSessionIdle;
}

static inline bool tcBarcodeSessionCanFinish(TCBarcodeSessionState state)
{
   return state != TCBarcodeSessionIdle && state != TCBarcodeSessionFinishing && state != TCBarcodeSessionFinished;
}

static inline bool tcBarcodeSessionCanTransition(TCBarcodeSessionState from, TCBarcodeSessionState to)
{
   return (from == TCBarcodeSessionIdle && to == TCBarcodeSessionRequestingPermission)
      || (from == TCBarcodeSessionRequestingPermission && (to == TCBarcodeSessionPresenting || to == TCBarcodeSessionFinishing))
      || (from == TCBarcodeSessionPresenting && (to == TCBarcodeSessionConfiguring || to == TCBarcodeSessionFinishing))
      || (from == TCBarcodeSessionConfiguring && (to == TCBarcodeSessionRunning || to == TCBarcodeSessionFinishing))
      || (from == TCBarcodeSessionRunning && to == TCBarcodeSessionFinishing)
      || (from == TCBarcodeSessionFinishing && to == TCBarcodeSessionFinished);
}

static inline bool tcBarcodeSessionMatchesGeneration(uint64_t expected, uint64_t actual)
{
   return expected != 0 && expected == actual;
}

#endif
