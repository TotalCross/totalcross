// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "nm/NativeMethods.h"
#include "utils.h"

void fillNativeProcAddressesTC()
{
#include "nativeProcAddressesTC.generated.inc"

   /* VM-internal entry: this is not a Java native method. */
   htPutPtr(&htNativeProcAddresses, hashCode("getMainContext"), &getMainContext);
}
