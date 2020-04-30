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

#include <pthread.h>

#define CONVERT_PRIORITY(p,v) sched_get_priority_min(p)+(sched_get_priority_max(p)-sched_get_priority_min(p))*(v-1)/9; // java 1=min, 10=max

static ThreadHandle privateThreadCreateNative(Context context, ThreadFunc t, VoidP this_)
{
   pthread_t h = 0;
   pthread_attr_t attr;
   ThreadArgs targs = ThreadArgsFromObject(this_);
   targs->threadObject = this_;
   targs->context = context;
   pthread_mutex_init(&targs->state_mutex, NULL);
   pthread_cond_init(&targs->state_cv, NULL);
   targs->start = false;
   
   pthread_create(&h, NULL, t, targs); // Create, but thread will suspend until we send a resume signal

   if (h != null)
   {
      int policy,ret;
      size_t ssize=0;
      struct sched_param param;
      pthread_getschedparam(h, &policy, &param);
      param.sched_priority = CONVERT_PRIORITY(policy, Thread_priority(this_));
      pthread_setschedparam(h, policy, &param);

      // make sure that the stack size is at 1MB
      ret = pthread_attr_init(&attr);
      ret = pthread_attr_getstacksize(&attr, &ssize);
      if (ssize < 1*1024*1024)
         ret = pthread_attr_setstacksize(&attr, 1*1024*1024);
      ret = pthread_attr_destroy(&attr);

      ThreadHandleFromObject(this_) = h;
      // now start/resume the thread
      pthread_mutex_lock(&targs->state_mutex);
      targs->start = true;
      pthread_cond_signal(&targs->state_cv);
      pthread_mutex_unlock(&targs->state_mutex);
   }
   else throwException(context, OutOfMemoryError, "Can't create thread (2)");
   return h;
}

static ThreadHandle privateThreadGetCurrent()
{
   return pthread_self();
}

static void privateThreadDestroy(ThreadHandle h, bool threadDestroyingItself)
{
#if !defined(ANDROID)
   if (!threadDestroyingItself) pthread_cancel(h);
   void *ret;
   pthread_join(h, &ret); // wait child thread termination and get its exit code
#endif
}

static VoidP privateThreadFunc(VoidP argP)
{
   ThreadArgs targs = (ThreadArgs)argP;

   pthread_mutex_lock(&targs->state_mutex);
   while(!targs->start)
      pthread_cond_wait(&targs->state_cv, &targs->state_mutex);
   pthread_mutex_unlock(&targs->state_mutex);

#ifdef ANDROID
   {JNIEnv* env; (*androidJVM)->AttachCurrentThreadAsDaemon(androidJVM, &env, NULL);}
#endif
   executeThreadRun(targs->context, targs->threadObject);

#ifdef ANDROID
   (*androidJVM)->DetachCurrentThread(androidJVM);
#endif

   pthread_mutex_destroy(&targs->state_mutex);
   pthread_cond_destroy(&targs->state_cv);
   pthread_exit((VoidP)0);
}
