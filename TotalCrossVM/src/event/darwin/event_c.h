// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef GFX_GRAPHICS_H
#define GFX_GRAPHICS_H

#define privateIsEventAvailable     iphone_privateIsEventAvailable
#define privatePumpEvent            iphone_privatePumpEvent
#define privateInitEvent            iphone_privateInitEvent
#define privateDestroyEvent         iphone_privateDestroyEvent

#ifdef __cplusplus
extern "C" {
#endif

int32 iphone_privateIsEventAvailable       ();
void iphone_privatePumpEvent              ();
int32 iphone_privateInitEvent();
void iphone_privateDestroyEvent();

void notifyStopVM();

#ifdef __cplusplus
};
#endif

#endif
