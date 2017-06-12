/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2017 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

#include "tcvm.h"

#include <stdio.h>

TC_API void tfiFII_getToken(NMParams p)
{
#ifdef defined(ANDROID)
	fprintf(stderr, "%s %d\n", __FILE__, __LINE__);
#endif
}

