#ifndef __CPPWRAPPER_H__
#define __CPPWRAPPER_H__
#pragma once

#include <basetsd.h>
#include "../Window.h"
#include "tcclass.h"

#ifdef __cplusplus
extern "C" {
#endif

struct eventQueueMember {
	int type;
	int key;
	int x;
	int y;
	int modifiers;
};

char *GetAppPathWP8();
char *GetVmPathWP8();
char *GetDisplayNameWP8();

// WP8 dispatcher fuctions
void set_dispatcher();
void dispatcher_dispath();

// WP8 keyboard functions
void windowSetSIP(enum TCSIP kb);

// Dummy functions?
void windowSetDeviceTitle(TCObject titleObj);

// Vm
DWORD32 getRemainingBatery();
void vibrate(DWORD32 milliseconds);
DWORD32 getFreeMemoryWP8();
void alertCPP(JCharP jCharStr);
void vmSetAutoOffCPP(bool enable);

// Dial
void dialNumberCPP(JCharP number);

// SMS
void smsSendCPP(JCharP szMessage, JCharP szDestination);

// RadioDevice
int rdGetStateCPP(int type);

// ConnectionManager
bool isAvailableCPP(int type);

// GPS
bool nativeStartGPSCPP();
int nativeUpdateLocationCPP(Context context, TCObject gpsObject);
void nativeStopGPSCPP();

//Event Queue functions
void eventQueuePush(int type, int key, int x, int y, int modifiers);
struct eventQueueMember eventQueuePop(void);
int eventQueueEmpty(void);

bool dxSetup();
void dxUpdateScreen();
void dxDrawLine(int x1, int y1, int x2, int y2, int color);
void dxFillRect(int x1, int y1, int x2, int y2, int color);
void dxDrawPixels(int *x, int *y, int count, int color);

// Etc
double getFontHeightCPP();

#ifdef __cplusplus
}
#endif


#endif
