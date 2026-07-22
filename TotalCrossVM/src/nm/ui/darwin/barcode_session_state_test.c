// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <assert.h>
#include "barcode_session_state.h"

int main(void)
{
   assert(tcBarcodeSessionCanStart(TCBarcodeSessionIdle));
   assert(!tcBarcodeSessionCanStart(TCBarcodeSessionRunning));
   assert(tcBarcodeSessionCanTransition(TCBarcodeSessionIdle, TCBarcodeSessionRequestingPermission));
   assert(tcBarcodeSessionCanTransition(TCBarcodeSessionRequestingPermission, TCBarcodeSessionFinishing));
   assert(tcBarcodeSessionCanTransition(TCBarcodeSessionConfiguring, TCBarcodeSessionRunning));
   assert(tcBarcodeSessionCanTransition(TCBarcodeSessionFinishing, TCBarcodeSessionFinished));
   assert(!tcBarcodeSessionCanTransition(TCBarcodeSessionFinished, TCBarcodeSessionFinished));
   assert(tcBarcodeSessionCanFinish(TCBarcodeSessionPresenting));
   assert(!tcBarcodeSessionCanFinish(TCBarcodeSessionFinished));
   assert(tcBarcodeSessionMatchesGeneration(7, 7));
   assert(!tcBarcodeSessionMatchesGeneration(7, 8));
   assert(!tcBarcodeSessionMatchesGeneration(0, 0));
   return 0;
}
