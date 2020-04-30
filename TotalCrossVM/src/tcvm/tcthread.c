// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

void executeThreadRun(Context context, TCObject thread);

#if defined WINCE || defined WIN32
 #include "win/tcthread_c.h"
#elif defined POSIX || defined ANDROID
 #include "posix/tcthread_c.h"
#endif

void executeThreadRun(Context context, TCObject thread)
{
   TCClass c = OBJ_CLASS(thread);
   Method run = getMethod(c, true, "run", 0);
   if (run != null)
   {
      Context cc;
      ThreadHandle h;
      h = ThreadHandleFromObject(thread);
      cc = newContext(h, thread, true);
      if (cc == null)
         throwException(context, OutOfMemoryError, "Can't create thread context");
      else
      {              
         Sleep(1);
         setObjectLock(thread, UNLOCKED); // now is safe to unlock, because the context will mark the threadObj
         executeMethod(cc, run, thread);
         deleteContext(cc, false);
      }
      Thread_alive(thread) = false;
      threadDestroy(h, true);
      // this line is never reached
   }
}

ThreadHandle threadCreateNative(Context context, ThreadFunc t, VoidP args)
{
   return privateThreadCreateNative(context, t, args);
}

ThreadHandle threadGetCurrent()
{
   return privateThreadGetCurrent();
}

void threadCreateJava(Context currentContext, TCObject this_)
{
   TCObject a;
   setObjectLock(this_,LOCKED); // prevent the java.lang.Thread object from being collected, because another thread may collect it before the thread is started
   a = Thread_taskID(this_) = createByteArray(currentContext, sizeof(TThreadArgs));
   if (a != null)
   {
      ThreadHandle h;
      h = threadCreateNative(currentContext, privateThreadFunc, this_);
      setObjectLock(a, UNLOCKED);
      if (h != null) // exception already thrown
         threadCount++;
   }
}

void threadDestroy(ThreadHandle h, bool threadDestroyingItself)
{
   threadCount--;
   privateThreadDestroy(h,threadDestroyingItself);
}

void threadDestroyAll()
{     
   Context c;              
   int32 i;
   for (i = 0; i < MAX_CONTEXTS; i++)
      if ((c=contexts[i]) != null && c->thread != null)
      {
         threadDestroy(c->thread,false);
         c->thread = null;
      }
}

void freeMutex(int32 hash, VoidP pmutex)
{
   MUTEX_TYPE* mutex = (MUTEX_TYPE*)pmutex;
   UNUSED(hash);
   DESTROY_MUTEX_VAR(*mutex);
   xfree(mutex);
}

bool lockMutex(size_t address)
{
   MUTEX_TYPE* mutex;

   LOCKVAR(mutexes);
   if (!(mutex = htGetPtr(&htMutexes, address)))
   {
      if (!(mutex = (MUTEX_TYPE*)xmalloc(sizeof(MUTEX_TYPE))))
      {
         UNLOCKVAR(mutexes);
         return false;
      }
      SETUP_MUTEX;
      INIT_MUTEX_VAR(*mutex);
      if (!htPutPtr(&htMutexes, address, mutex))
      {                  
         DESTROY_MUTEX_VAR(*mutex);
         UNLOCKVAR(mutexes);
         return false;
      }
   }
   UNLOCKVAR(mutexes);
   RESERVE_MUTEX_VAR(*mutex); 
   return true;
}

void unlockMutex(size_t address)
{
   MUTEX_TYPE* mutex = htGetPtr(&htMutexes, address);
   RELEASE_MUTEX_VAR(*mutex);
}