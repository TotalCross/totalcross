#ifndef __CPPWRAPPER_H__
#define __CPPWRAPPER_H__
#pragma once

#include <basetsd.h>

#ifdef __cplusplus
extern "C" {
#endif

char *GetAppPathWP8();
void cppsleep(int ms);

// WP8 dispatcher fuctions
void set_dispatcher();
void dispatcher_dispath();

// WP8 keyboard functions
void setKeyboard(int state);

// Threads functions
void* cppthread_create(void (*func)(void *a), void *args);
void cppthread_detach(void *t);
void *cppget_current_thread();

// VM
DWORD32 getRemainingBatery();
void vibrate(DWORD32 milliseconds);
DWORD32 getFreeMemoryWP8();

#ifdef __cplusplus
}
#endif

#endif
