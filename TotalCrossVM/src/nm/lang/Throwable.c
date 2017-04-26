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
