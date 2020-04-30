// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

CharP privateGetErrorMessage(int32 errorCode, CharP buffer, uint32 size)
{
   TCHAR tmpbuf[1024];

   if (!FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM |FORMAT_MESSAGE_IGNORE_INSERTS, null, errorCode, 0, tmpbuf, sizeof(tmpbuf)/sizeof(TCHAR)-1, null))
       return null;

   return TCHARP2CharPBuf(tmpbuf, buffer);
}
