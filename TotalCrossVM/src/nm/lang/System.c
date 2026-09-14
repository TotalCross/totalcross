// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "NativeMethods.h"

TC_API void jlS_nanoTime(NMParams p) // java/lang/System native public static long nanoTime();
{
   p->retL = getNanoTime();
}
