#ifndef __CPPWRAPPER_H__
#define __CPPWRAPPER_H__
#pragma once

#include <basetsd.h>

#ifdef __cplusplus
extern "C" {
#endif

extern DWORD32 privHeight;
extern DWORD32 privWidth;

char *GetAppPathWP8();
void cppsleep(int ms);
void SetBounds();

// Graphics functions
void GetWidthAndHeight(DWORD32* width, DWORD32* height);
void InitDX(void);
void SetupDX(DWORD32 width, DWORD32 height);
void DisplayDX(void);
void ReleaseDX(void);

// Threads functions
void* cppthread_create(void (*func)(void *a), void *args);
void cppthread_detach(void *t);
void *cppget_current_thread();

DWORD32 getRemainingBatery();
void vibrate(DWORD32 milliseconds);

#ifdef __cplusplus
}
#endif

#endif
