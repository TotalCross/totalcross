/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: tcthread_c.h,v 1.26 2011-03-28 19:29:36 guich Exp $

#define CONVERT_PRIORITY(v) (10 * (11-(v))) // java 1=min, 10=max; palm: 100=min, 5=max

static ThreadHandle privateThreadCreateNative(Context context, ThreadFunc t, VoidP this_)
{
   UInt32 id=0;
   Err err;
   register uint32 got asm("r10");
   SysTaskCreateParamType tp;
   ThreadArgs targs = ThreadArgsFromObject(this_);

   tp.taskProc = t;
   tp.stackSize = 16384;
   tp.priority = CONVERT_PRIORITY(Thread_priority(this_));
   tp.tag = applicationId;

   targs->got = got;
   targs->args = this_;
   targs->context = context;
   err = SysTaskCreate(&id, &tp);
   if (err == errNone)
   {
      ThreadHandleFromObject(this_) = id;
      KALTaskStart(id, targs);
   }
   else
   {
      throwException(context, RuntimeException, "Can't create thread");
      id = 0;
   }
   return id;
}

static ThreadHandle privateThreadGetCurrent()
{
   UInt32 id;
   return KALTaskGetCurrentID(&id) == errNone ? id : 0;
}

static void privateThreadDestroy(ThreadHandle h, bool threadDestroyingItself)
{
   if (!threadDestroyingItself) KALTaskExit(h);
   SysTaskDelete(h);
}

static void privateThreadFunc(VoidP argP)
{
   ThreadArgs targs = (ThreadArgs)argP;
   // get the offset to the global variables
   register uint32 got asm("r10");
   got = targs->got;

   Object threadObject = (Object)targs->args;
   executeThreadRun(targs->context, threadObject);
}
