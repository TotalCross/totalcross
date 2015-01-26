/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



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
