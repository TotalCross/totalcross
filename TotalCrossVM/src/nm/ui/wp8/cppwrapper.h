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
void windowSetDeviceTitle(Object titleObj);

// VM
DWORD32 getRemainingBatery();
void vibrate(DWORD32 milliseconds);
DWORD32 getFreeMemoryWP8();

//Event Queue functions
void eventQueuePush(int type, int key, int x, int y, int modifiers);
struct eventQueueMember eventQueuePop(void);
int eventQueueEmpty(void);

#ifdef __cplusplus
}
#endif


#endif
