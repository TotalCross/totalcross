// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////
#ifdef ENABLE_TEST_SUITE
CharP throwableTrace;
#endif

void printStackTraceFromObj(TCObject traceObj)
{
   if (traceObj)
   {
      CharP throwableTrace = String2CharP(traceObj);
      #ifndef ENABLE_TEST_SUITE
      if (throwableTrace)
         debug(throwableTrace);
      xfree(throwableTrace);
      #endif
   }
   else
   {
      #ifndef ENABLE_TEST_SUITE
      debug("No trace available");
      #endif
   }
}
TC_API void jlT_printStackTraceNative(NMParams p) // java/lang/Throwable native private void printStackTraceNative();
{
   TCObject ex;

   ex = p->obj[0];
   if (ex != null)  
      printStackTraceFromObj(*Throwable_trace(ex));
}

#ifdef ENABLE_TEST_SUITE
#include "Throwable_test.h"
#endif
