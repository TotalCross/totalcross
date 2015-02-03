/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



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
