// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////
TC_API void jlT_yield(NMParams p) // java/lang/Thread native public static void yield();
{
   UNUSED(p)
   Sleep(1);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlT_start(NMParams p) // java/lang/Thread native public void start();
{
   threadCreateJava(p->currentContext, p->obj[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlT_currentThread(NMParams p) // java/lang/Thread native public static java.lang.Thread currentThread();
{
   p->retO = p->currentContext->threadObj; // guich@tc122_6
}

#ifdef ENABLE_TEST_SUITE
#include "Thread_test.h"
#endif
