// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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

bool iphone_privateIsEventAvailable       ();
void iphone_privatePumpEvent              ();
bool iphone_privateInitEvent();
void iphone_privateDestroyEvent();

void notifyStopVM();

#ifdef __cplusplus
};
#endif

#endif
