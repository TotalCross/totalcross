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



#ifndef ERRORMSG_H
#define ERRORMSG_H

typedef CharP (*getErrorMessageFunc)(int32 errorCode, CharP buffer, uint32 size);

TC_API CharP getErrorMessage(int32 errorCode, CharP buffer, uint32 size);

#endif
