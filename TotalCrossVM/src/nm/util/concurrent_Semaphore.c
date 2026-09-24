// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

typedef struct
{
   MUTEX_TYPE mutex;
   THREAD_CONDITION_TYPE condition;
   THREAD_CONDITION_TYPE diagnosticCondition;
   int32 permits;
   int32 waiters;
   int32 diagnosticWaiters;
   bool diagnosticConditionInitialized;
   bool initialized;
} SemaphoreState;

static TCObject semaphoreStateObject(TCObject semaphore)
{
   return FIELD_OBJ(semaphore, OBJ_CLASS(semaphore), 0);
}

static SemaphoreState *requireSemaphoreStateForObject(NMParams p, TCObject semaphore)
{
   TCObject stateObject = semaphoreStateObject(semaphore);
   SemaphoreState *state;

   if (stateObject == null)
   {
      throwException(p->currentContext, RuntimeException, "Semaphore is not initialized");
      return null;
   }

   state = (SemaphoreState *)ARRAYOBJ_START(stateObject);
   if (!state->initialized)
   {
      throwException(p->currentContext, RuntimeException, "Semaphore is not initialized");
      return null;
   }
   return state;
}

static SemaphoreState *requireSemaphoreState(NMParams p)
{
   return requireSemaphoreStateForObject(p, p->obj[0]);
}

static bool initializeSemaphoreMutex(MUTEX_TYPE *mutex)
{
#if defined(WIN32)
   InitializeCriticalSection(mutex);
   return true;
#else
   pthread_mutexattr_t attributes;
   int result = pthread_mutexattr_init(&attributes);
   if (result != 0)
      return false;

   result = pthread_mutexattr_settype(&attributes, PTHREAD_MUTEX_RECURSIVE);
   if (result == 0)
      result = pthread_mutex_init(mutex, &attributes);
   pthread_mutexattr_destroy(&attributes);
   return result == 0;
#endif
}

static void signalNextSemaphoreWaiter(SemaphoreState *state)
{
#if defined(WIN32)
   /* Auto-reset event signals coalesce, so pass the wakeup to another waiter. */
   if (state->permits > 0 && state->waiters > 0)
      SIGNAL_THREAD_CONDITION(&state->condition);
#else
   (void)state;
#endif
}

//////////////////////////////////////////////////////////////////////////
TC_API void jucS_create_i(NMParams p) // java/util/concurrent/Semaphore native private void create(int permits);
{
   TCObject semaphore = p->obj[0];
   TCObject stateObject = createByteArray(p->currentContext, sizeof(SemaphoreState));
   SemaphoreState *state;

   if (stateObject == null)
      return;

   FIELD_OBJ(semaphore, OBJ_CLASS(semaphore), 0) = stateObject;
   state = (SemaphoreState *)ARRAYOBJ_START(stateObject);
   state->permits = p->i32[0];
   if (!initializeSemaphoreMutex(&state->mutex))
   {
      FIELD_OBJ(semaphore, OBJ_CLASS(semaphore), 0) = null;
      setObjectLock(stateObject, UNLOCKED);
      throwException(p->currentContext, RuntimeException, "Could not initialize Semaphore mutex");
      return;
   }

#if defined(WIN32)
   if (!INIT_THREAD_CONDITION(&state->condition))
#else
   if (INIT_THREAD_CONDITION(&state->condition) != 0)
#endif
   {
      DESTROY_MUTEX_VAR(state->mutex);
      FIELD_OBJ(semaphore, OBJ_CLASS(semaphore), 0) = null;
      setObjectLock(stateObject, UNLOCKED);
      throwException(p->currentContext, RuntimeException, "Could not initialize Semaphore condition");
      return;
   }

   state->initialized = true;
}

//////////////////////////////////////////////////////////////////////////
TC_API void jucS_destroy(NMParams p) // java/util/concurrent/Semaphore native private void destroy();
{
   TCObject semaphore = p->obj[0];
   TCObject stateObject = semaphoreStateObject(semaphore);

   if (stateObject != null)
   {
      SemaphoreState *state = (SemaphoreState *)ARRAYOBJ_START(stateObject);
      if (state->initialized)
      {
         state->initialized = false;
         if (state->diagnosticConditionInitialized)
            DESTROY_THREAD_CONDITION(&state->diagnosticCondition);
         DESTROY_THREAD_CONDITION(&state->condition);
         DESTROY_MUTEX_VAR(state->mutex);
      }
      FIELD_OBJ(semaphore, OBJ_CLASS(semaphore), 0) = null;
      setObjectLock(stateObject, UNLOCKED);
   }
}

static void acquireSemaphore(NMParams p)
{
   SemaphoreState *state = requireSemaphoreState(p);
   if (state == null)
      return;

   RESERVE_MUTEX_VAR(state->mutex);
   while (state->permits <= 0)
   {
      state->waiters++;
      if (state->diagnosticWaiters > 0)
         SIGNAL_THREAD_CONDITION(&state->diagnosticCondition);
      WAIT_THREAD_CONDITION(&state->condition, &state->mutex);
      state->waiters--;
   }
   state->permits--;
   signalNextSemaphoreWaiter(state);
   RELEASE_MUTEX_VAR(state->mutex);
}

//////////////////////////////////////////////////////////////////////////
TC_API void jucS_acquire(NMParams p) // java/util/concurrent/Semaphore native public void acquire() throws InterruptedException;
{
   acquireSemaphore(p);
}

//////////////////////////////////////////////////////////////////////////
TC_API void jucS_acquireUninterruptibly(NMParams p) // java/util/concurrent/Semaphore native public void acquireUninterruptibly();
{
   acquireSemaphore(p);
}

//////////////////////////////////////////////////////////////////////////
TC_API void tucSTD_awaitWaiters_si(NMParams p) // totalcross/util/concurrent/SemaphoreTestDiagnostics native public static int awaitWaiters(java.util.concurrent.Semaphore semaphore, int minimumWaiters);
{
   SemaphoreState *state;
   bool initialized;
   int32 minimumWaiters = p->i32[0];

   p->retI = 0;
   if (minimumWaiters <= 0)
   {
      throwException(p->currentContext, IllegalArgumentException, "minimumWaiters must be positive");
      return;
   }

   state = requireSemaphoreStateForObject(p, p->obj[0]);
   if (state == null)
      return;

   RESERVE_MUTEX_VAR(state->mutex);
   if (state->waiters < minimumWaiters)
   {
      if (!state->diagnosticConditionInitialized)
      {
#if defined(WIN32)
         initialized = INIT_THREAD_CONDITION(&state->diagnosticCondition) != 0;
#else
         initialized = INIT_THREAD_CONDITION(&state->diagnosticCondition) == 0;
#endif
         if (!initialized)
         {
            RELEASE_MUTEX_VAR(state->mutex);
            throwException(p->currentContext, RuntimeException, "Could not initialize Semaphore diagnostic condition");
            return;
         }
         state->diagnosticConditionInitialized = true;
      }

      state->diagnosticWaiters++;
      while (state->waiters < minimumWaiters)
         WAIT_THREAD_CONDITION(&state->diagnosticCondition, &state->mutex);
      state->diagnosticWaiters--;
   }

   p->retI = state->waiters;
   RELEASE_MUTEX_VAR(state->mutex);
}

//////////////////////////////////////////////////////////////////////////
TC_API void jucS_tryAcquire(NMParams p) // java/util/concurrent/Semaphore native public boolean tryAcquire();
{
   SemaphoreState *state = requireSemaphoreState(p);
   p->retI = 0;
   if (state == null)
      return;

   RESERVE_MUTEX_VAR(state->mutex);
   if (state->permits > 0)
   {
      state->permits--;
      p->retI = 1;
      signalNextSemaphoreWaiter(state);
   }
   RELEASE_MUTEX_VAR(state->mutex);
}

//////////////////////////////////////////////////////////////////////////
TC_API void jucS_release(NMParams p) // java/util/concurrent/Semaphore native public void release();
{
   SemaphoreState *state = requireSemaphoreState(p);
   if (state == null)
      return;

   RESERVE_MUTEX_VAR(state->mutex);
   if (state->permits == INT32_MAX)
   {
      RELEASE_MUTEX_VAR(state->mutex);
      throwException(p->currentContext, ErrorClass, "Maximum permit count exceeded");
      return;
   }

   state->permits++;
   if (state->waiters > 0)
      SIGNAL_THREAD_CONDITION(&state->condition);
   RELEASE_MUTEX_VAR(state->mutex);
}
