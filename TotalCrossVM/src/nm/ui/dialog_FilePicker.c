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

#if defined(darwin)
 #include "darwin/dialog_FilePicker_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tudFP_nativePresent(NMParams p) // totalcross/ui/dialog/FilePicker native private URI nativePresent();
{
#if defined(darwin)
   nativePickFile(p);
#else
   UNUSED(p);
#endif
}
