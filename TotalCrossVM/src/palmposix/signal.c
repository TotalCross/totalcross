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



#if HAVE_CONFIG_H
#include "config.h"
#endif

#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#endif
#include "signal.h"

sighandler_t signal(int signum, sighandler_t handler)
{
   ErrFatalDisplayIf(signum != SIGALRM, "signal not supported");

   // not yet implemented
   ErrNonFatalDisplayIf(signum == SIGALRM, "SIGALRM not yet implemented");

   return handler;
}
