// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCTHREAD_H
#define TCTHREAD_H

#include "xtypes.h"

/***********************   MULTI-PLATFORM CONCURRENCE SUPPORT  *************************

  !!!! IMPORTANT: EACH DECLARED MUTEX MUST BE DESTROYED IN destroyGlobals !!!!

USAGE

   In this .h, you add:

      extern DECLARE_MUTEX(myvar);

   In globals.c:

      DECLARE_MUTEX(myvar);

      and add init/destroy mutex to the initGlobals/destroyGlobals functions.

   Before you access the variable:

      LOCKVAR(myvar);

   After you access it:

      UNLOCKVAR(myvar);


IMPLEMENTATION DETAILS

Windows: (we use critical section instead of mutex because it is faster)

   CRITICAL_SECTION lock;
   InitializeCriticalSection(&lock);
   EnterCriticalSection(&lock);
   LeaveCriticalSection(&lock);
   DeleteCriticalSection(&lock);

Palm OS:

   UInt32 lock;
   KALMutexCreate(&lock, applicationId);
   KALMutexReserve(lock);
   KALMutexRelease(lock, 0); // timeout
   KALMutexDelete(lock);

Pthreads on Posix (iPhone, Linux, ...)

   pthread_mutex_t lock;
   pthread_mutex_init(&lock, NULL)
   pthread_mutex_lock(&lock)
   pthread_mutex_unlock(&lock)
   pthread_mutex_destroy(&lock)

*/

#define MUTEX_VAR(x) x##Mutex

#if defined(WIN32)
 #define MUTEX_TYPE CRITICAL_SECTION
 #define SETUP_MUTEX
 #define INIT_MUTEX_VAR(x)    InitializeCriticalSection(&(x))
 #define RESERVE_MUTEX_VAR(x)  do { /*debug("LOCKVAR %s", #x);*/ EnterCriticalSection(&(x)); } while(0)
 #define RELEASE_MUTEX_VAR(x) LeaveCriticalSection(&(x))
 #define DESTROY_MUTEX_VAR(x) DeleteCriticalSection(&(x))
#elif defined(POSIX) || defined(ANDROID)
 #include <pthread.h>
 #if !defined(PTHREAD_MUTEX_RECURSIVE)
 #define PTHREAD_MUTEX_RECURSIVE PTHREAD_MUTEX_RECURSIVE_NP
 #endif
 #define SETUP_MUTEX \
   pthread_mutexattr_t mutex_attrs; \
   pthread_mutexattr_init(&mutex_attrs); \
   pthread_mutexattr_settype(&mutex_attrs, PTHREAD_MUTEX_RECURSIVE)
 #define MUTEX_TYPE pthread_mutex_t
 #define INIT_MUTEX_VAR(x)    pthread_mutex_init(&(x), &mutex_attrs)
 #define RESERVE_MUTEX_VAR(x) pthread_mutex_lock(&(x))
 #define RELEASE_MUTEX_VAR(x) pthread_mutex_unlock(&(x))
 #define DESTROY_MUTEX_VAR(x) pthread_mutex_destroy(&(x))
#else
 #error "Mutexes are not implemented"
#endif

/* Condition waits block in the OS; callers must loop on their own predicate. */
#if defined(WIN32)
 typedef struct
 {
    HANDLE event;
 } THREAD_CONDITION_TYPE;

 /* Auto-reset events preserve legacy Windows/WinCE support. Signals do not
    count permits: the permit count remains authoritative, and a waiter must
    chain a signal while permits and waiters remain. */
 #define INIT_THREAD_CONDITION(condition) \
   (((condition)->event = CreateEvent(NULL, FALSE, FALSE, NULL)) != NULL)
 #define WAIT_THREAD_CONDITION(condition, mutex) \
   do { \
      LeaveCriticalSection(mutex); \
      WaitForSingleObject((condition)->event, INFINITE); \
      EnterCriticalSection(mutex); \
   } while (0)
#if defined(TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS)
 #define WAIT_THREAD_CONDITION_WITH_DIAGNOSTIC(condition, mutex, diagnosticEvent, pending) \
   do { \
      LeaveCriticalSection(mutex); \
      SignalObjectAndWait((diagnosticEvent), (condition)->event, INFINITE, FALSE); \
      /* Report a wake before reacquiring the lock so awaitWaiters can reject it. */ \
      InterlockedExchange((volatile LONG *)(pending), 0); \
      EnterCriticalSection(mutex); \
   } while (0)
#endif
 #define SIGNAL_THREAD_CONDITION(condition) SetEvent((condition)->event)
 #define DESTROY_THREAD_CONDITION(condition) CloseHandle((condition)->event)
#elif defined(POSIX) || defined(ANDROID)
 typedef pthread_cond_t THREAD_CONDITION_TYPE;
 #define INIT_THREAD_CONDITION(condition) pthread_cond_init((condition), NULL)
 #define WAIT_THREAD_CONDITION(condition, mutex) \
   pthread_cond_wait((condition), (mutex))
 #define SIGNAL_THREAD_CONDITION(condition) pthread_cond_signal((condition))
 #define DESTROY_THREAD_CONDITION(condition) pthread_cond_destroy((condition))
#endif

#define INIT_MUTEX(x)    INIT_MUTEX_VAR(MUTEX_VAR(x))
#define RESERVE_MUTEX(x) RESERVE_MUTEX_VAR(MUTEX_VAR(x))
#define RELEASE_MUTEX(x) RELEASE_MUTEX_VAR(MUTEX_VAR(x))
#define DESTROY_MUTEX(x) DESTROY_MUTEX_VAR(MUTEX_VAR(x))
#define DECLARE_MUTEX(x) MUTEX_TYPE x##Mutex
#define LOCKVAR(x)       RESERVE_MUTEX(x)
#define UNLOCKVAR(x)     RELEASE_MUTEX(x)

/************  PUBLIC MUTEXES *************/

extern DECLARE_MUTEX(omm);
extern DECLARE_MUTEX(tcz);
extern DECLARE_MUTEX(metAndCls);
extern DECLARE_MUTEX(screen);
extern DECLARE_MUTEX(createdHeaps);
extern DECLARE_MUTEX(alloc);
extern DECLARE_MUTEX(fonts);
extern DECLARE_MUTEX(mutexes);

#if defined(WIN32)

 typedef DWORD WINAPI ThreadFunc(VoidP argP);
 typedef HANDLE ThreadHandle;
 typedef struct
 {
    Context context;
    TCObject threadObject;
    ThreadHandle h;
 } *ThreadArgs, TThreadArgs;

#elif defined(POSIX) || defined(ANDROID)

 #include <pthread.h>
 typedef VoidP (*ThreadFunc)(VoidP argP);
 typedef pthread_t ThreadHandle;
 typedef struct
 {
    TCObject threadObject;
    Context context;
    pthread_cond_t state_cv;
    pthread_mutex_t state_mutex;
    bool start;
    ThreadHandle h;
 } *ThreadArgs, TThreadArgs;

#endif

ThreadHandle threadCreateNative(Context context, ThreadFunc t, VoidP args);
ThreadHandle threadGetCurrent();
void threadCreateJava(Context currentContext, TCObject this_);
void threadDestroy(ThreadHandle h, bool threadDestroyingItself); // must be used when exiting the application or the thread itself
void threadDestroyAll(); // destroy all threads

void freeMutex(int32 hash, VoidP mutex);
bool lockMutex(size_t address);
void unlockMutex(size_t address);

#define ThreadArgsFromObject(o) ((ThreadArgs)ARRAYOBJ_START(Thread_taskID(o)))
#define ThreadHandleFromObject(o) ThreadArgsFromObject(o)->h

#endif
