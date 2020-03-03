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

void updateScreen(Context currentContext);

//////////////////////////////////////////////////////////////////////////
TC_API void tuC_updateScreen(NMParams p) // totalcross/ui/Control native public static void updateScreen();
{
   updateScreen(p->currentContext);
}

#ifdef ENABLE_TEST_SUITE
#include "Control_test.h"
#endif
