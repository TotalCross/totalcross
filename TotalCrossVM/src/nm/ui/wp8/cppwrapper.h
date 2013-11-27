#ifndef __CPPWRAPPER_H__
#define __CPPWRAPPER_H__
#pragma once
#ifdef __cplusplus
extern "C" {
#endif

	char *GetAppPathWP8();
	void cppsleep(int ms);

	// Threads functions
	void* cppthread_create(void (*func)(void *a), void *args);
	void cppthread_detach(void *t);
	void *cppget_current_thread();

#ifdef __cplusplus
}
#endif

#endif
