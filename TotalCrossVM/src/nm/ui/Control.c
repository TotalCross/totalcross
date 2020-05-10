// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

void updateScreen(Context currentContext);

//////////////////////////////////////////////////////////////////////////
TC_API void tuC_updateScreen(NMParams p) // totalcross/ui/Control native public static void updateScreen();
{
   updateScreen(p->currentContext);
}

#ifdef ENABLE_TEST_SUITE
#include "Control_test.h"
#endif
