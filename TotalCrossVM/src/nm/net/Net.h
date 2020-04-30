// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef NET_H
#define NET_H

//#if defined(WINCE) || defined(WIN32)
// #include <winsock.h>
//#endif

#include "tcvm.h"

#ifdef WIN32

 #ifndef _WINSOCK2API_
  #define SD_SEND         0x01

/* According to the documentation this struct is only available on WINCE 4.0+
   struct in_addr
   {
      union
      {
         struct { u_char s_b1, s_b2, s_b3, s_b4; } S_un_b;
         struct { u_short s_w1, s_w2; } S_un_w;
         u_long S_addr;
      } S_un;
   };
*/

 #endif

 #if !defined SOCKET
  #ifdef _WIN64
   typedef UINT_PTR SOCKET;
  #else
   typedef u_int SOCKET;
  #endif
#endif

#endif

   enum
   {
      NC_UNDEFINED      = -1,
      NC_DEFAULT        =  0,
      NC_GPRS           =  1
   };

   enum
   {
      CM_CRADLE      = 1,
      CM_WIFI        = 2,
      CM_CELLULAR    = 3
   };

#endif
