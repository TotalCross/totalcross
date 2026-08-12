// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef WINDOW_SAFE_AREA_H
#define WINDOW_SAFE_AREA_H

#include "tcvm.h"

void windowUpdateSafeAreaInsetsPhysical(
   Context currentContext,
   int32 top,
   int32 left,
   int32 bottom,
   int32 right);

#endif
