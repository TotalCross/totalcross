// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef TC_EVENT_H
#define TC_EVENT_H

#ifdef __cplusplus
extern "C" {
#endif

void mainEventLoop(Context currentContext);
void pumpEvents(Context currentContext);
bool isEventAvailable();

bool initEvent();
void destroyEvent();

/// hardware events that can be sent by the vm to the running instance
typedef enum
{
   KEYEVENT_KEY_PRESS = 100,
   KEYEVENT_SPECIALKEY_PRESS = 102,
   PENEVENT_PEN_DOWN = 200,
   PENEVENT_PEN_UP,
   PENEVENT_PEN_DRAG,
   MOUSEEVENT_MOUSE_MOVE = 205,
   MOUSEEVENT_MOUSE_WHEEL = 208,
   MULTITOUCHEVENT_SCALE = 250,
   CONTROLEVENT_SIP_CLOSED = 306,
   TIMEREVENT_TRIGGERED = 350,
   PUSHNOTIFICATIONEVENT_TOKEN_RECEIVED = 360,
   PUSHNOTIFICATIONEVENT_MESSAGE_RECEIVED = 361,
   MEDIACLIPEVENT_STARTED = 550,
   MEDIACLIPEVENT_STOPPED,
   MEDIACLIPEVENT_CLOSED,
   MEDIACLIPEVENT_ERROR,
   MEDIACLIPEVENT_END_OF_MEDIA,
} TotalCrossUiEvent;

typedef enum
{
   WHEEL_RIGHT = 1,
   WHEEL_LEFT  = 2,
   WHEEL_UP    = 3,
   WHEEL_DOWN  = 4,
} MouseWheelDir;

#ifdef WIN32
typedef struct
{
   HWND hWnd;
   UINT msg;
   WPARAM wParam;
   LONG lParam;
   Context currentContext;
} WinEvent;
#endif

/// post an event to the running Java application. If mods is -1, the asynch mods will be retrieved; otherwise, pass the mods given in the key event
void postEvent(Context currentContext, TotalCrossUiEvent type, int32 key, int32 x, int32 y, int32 mods); // guich@tc126_70
void postOnMinimizeOrRestore(bool isMinimized);

#ifdef __cplusplus
}
#endif

#endif
