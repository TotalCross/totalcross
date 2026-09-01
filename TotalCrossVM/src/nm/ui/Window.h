// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef WINDOW_H
#define WINDOW_H

#include "tcvm.h"
#include "WindowSIP.h"

bool windowBackendSetSize(int32 width, int32 height);
bool windowResolveStartupSize(
   int16 appTczAttr,
   int32 displayWidth,
   int32 displayHeight,
   int32 workAreaHeight,
   int32 commandLineWidth,
   int32 commandLineHeight,
   int32 environmentWidth,
   int32 environmentHeight,
   int32 *width,
   int32 *height,
   bool *tczSizeApplied);

#endif
