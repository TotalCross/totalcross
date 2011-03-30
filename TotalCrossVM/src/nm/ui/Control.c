/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: Control.c,v 1.14 2011-01-04 13:31:03 guich Exp $

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
