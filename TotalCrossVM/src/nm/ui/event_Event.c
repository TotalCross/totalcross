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
TC_API void tueE_isAvailable(NMParams p) // totalcross/ui/event/Event native public static boolean isAvailable();
{
   p->retI = isEventAvailable();
}

#ifdef ENABLE_TEST_SUITE
#include "event_Event_test.h"
#endif
