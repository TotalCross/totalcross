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

//////////////////////////////////////////////////////////////////////////
#ifdef ENABLE_TEST_SUITE
CharP throwableTrace;
#endif

TC_API void jlT_printStackTraceNative(NMParams p) // java/lang/Throwable native private void printStackTraceNative();
{
   TCObject ex,traceObj;
#ifndef ENABLE_TEST_SUITE
   CharP throwableTrace;
#endif

   ex = p->obj[0];
   if (ex != null)
   {
      traceObj = *Throwable_trace(ex);
      if (traceObj)
      {
         throwableTrace = String2CharP(traceObj);
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
}

#ifdef ENABLE_TEST_SUITE
#include "Throwable_test.h"
#endif
