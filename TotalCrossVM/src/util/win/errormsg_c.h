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



CharP privateGetErrorMessage(int32 errorCode, CharP buffer, uint32 size)
{
   TCHAR tmpbuf[1024];

   if (!FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM |FORMAT_MESSAGE_IGNORE_INSERTS, null, errorCode, 0, tmpbuf, sizeof(tmpbuf)/sizeof(TCHAR)-1, null))
       return null;

   return TCHARP2CharPBuf(tmpbuf, buffer);
}
