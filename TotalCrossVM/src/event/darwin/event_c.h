/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: event_c.h,v 1.7 2011-01-04 13:31:16 guich Exp $

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
