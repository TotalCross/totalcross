#ifndef __CPPWRAPPER_H__
#define __CPPWRAPPER_H__
#pragma once

#include <basetsd.h>

#ifdef __cplusplus
extern "C" {
#endif

char *GetAppPathWP8();
void cppsleep(int ms);

// Graphics functions
void GetWidthAndHeight(DWORD32* width, DWORD32* height);
void SetupDX(void);
void ReleaseDX(void);

// Threads functions
void* cppthread_create(void (*func)(void *a), void *args);
void cppthread_detach(void *t);
void *cppget_current_thread();

#ifdef __cplusplus
}
#endif

#endif
