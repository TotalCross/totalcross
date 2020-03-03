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
TC_API void tueE_isAvailable(NMParams p) // totalcross/ui/event/Event native public static boolean isAvailable();
{
   p->retI = isEventAvailable();
}

#ifdef ENABLE_TEST_SUITE
#include "event_Event_test.h"
#endif
