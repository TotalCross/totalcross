// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

CharP privateGetErrorMessage(int32 errorCode, CharP buffer, uint32 size)
{
   TCHAR tmpbuf[1024];

   if (!FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM |FORMAT_MESSAGE_IGNORE_INSERTS, null, errorCode, 0, tmpbuf, sizeof(tmpbuf)/sizeof(TCHAR)-1, null))
       return null;

   return TCHARP2CharPBuf(tmpbuf, buffer);
}
