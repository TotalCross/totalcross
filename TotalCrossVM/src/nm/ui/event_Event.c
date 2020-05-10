// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tueE_isAvailable(NMParams p) // totalcross/ui/event/Event native public static boolean isAvailable();
{
   p->retI = isEventAvailable();
}

#ifdef ENABLE_TEST_SUITE
#include "event_Event_test.h"
#endif
