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

#define CONVERT_PRIORITY(v) (7 - ((v-1)*7/9)) // java 1=min, 10=max; windows: 7=min, 0=max

DWORD WINAPI privateThreadFunc(VoidP argP);

static ThreadHandle privateThreadCreateNative(Context context, ThreadFunc t, VoidP this_)
{
   ThreadHandle h = null;
   ThreadArgs targs = ThreadArgsFromObject(this_);

   targs->context = context;
   targs->threadObject = this_;
   h = CreateThread(NULL, 0, t, targs, CREATE_SUSPENDED, NULL);
   if (h != null)
   {
      ThreadHandleFromObject(this_) = h;
      SetThreadPriority(h, CONVERT_PRIORITY(Thread_priority(this_)));
      ResumeThread(h);
   }
   else throwException(context, RuntimeException, "Can't create thread");
   return h;
}

static ThreadHandle privateThreadGetCurrent()
{
   return GetCurrentThread();
}

static void privateThreadDestroy(ThreadHandle h, bool threadDestroyingItself)
{
#ifndef WP8
   if (!threadDestroyingItself) TerminateThread(h, 0);
#endif
   CloseHandle(h);
}

DWORD WINAPI privateThreadFunc(VoidP argP)
{
   ThreadArgs targs = (ThreadArgs)argP;
   executeThreadRun(targs->context, targs->threadObject);
   return (DWORD)0;
}
