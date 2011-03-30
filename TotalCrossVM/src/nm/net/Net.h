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

// $Id: Net.h,v 1.12 2011-01-04 13:31:08 guich Exp $

#ifndef NET_H
#define NET_H

//#if defined(WINCE) || defined(WIN32)
// #include <winsock.h>
//#endif

#ifdef PALMOS
#define NetLibOpen              NetLibOpenm68k
#define NetLibClose             NetLibClosem68k
#define NetLibOpenConfig        NetLibOpenConfigm68k
#define NetLibOpenCount         NetLibOpenCountm68k
#define NetLibFinishCloseWait   NetLibFinishCloseWaitm68k
#define NetLibConnectionRefresh NetLibConnectionRefreshm68k
#define NetLibSocketConnect     NetLibSocketConnectm68k
#define NetLibSocketClose       NetLibSocketClosem68k
#define NetLibSocketShutdown    NetLibSocketShutdown68k
#define NetLibGetHostByName     NetLibGetHostByNamem68k
#define NetLibSocketOpen        NetLibSocketOpenm68k
#define NetLibSocketClose       NetLibSocketClosem68k
#define NetLibSocketOptionSet   NetLibSocketOptionSetm68k
#define NetLibSocketOptionGet   NetLibSocketOptionGetm68k
#define NetLibSend              NetLibSendm68k
#define NetLibReceive           NetLibReceivem68k
#define NetLibSocketBind        NetLibSocketBindm68k
#define NetLibSocketListen      NetLibSocketListenm68k
#define NetLibSocketAccept      NetLibSocketAcceptm68k
#define NetLibSettingGet        NetLibSettingGetm68k
#define NetLibAddrINToA         NetLibAddrINToAm68k
#define NetLibAddrAToIN         NetLibAddrAToINm68k
#define NetLibIFGet             NetLibIFGet68k
#define NetLibIFUp              NetLibIFUp68k
#define NetLibConfigMakeActive  NetLibConfigMakeActive68k
#define NetLibConfigList        NetLibConfigList68k
#define NetLibGetHostByAddr     NetLibGetHostByAddr68k
#define NetLibIFAttach          NetLibIFAttach68k
#define NetLibConfigSaveAs      NetLibConfigSaveAs68k
#define NetLibConfigIndexFromName   NetLibConfigIndexFromName68k
#define NetLibIFDetach          NetLibIFDetach68k
#define NetLibConfigSaveAs      NetLibConfigSaveAs68k
#define NetLibConfigAliasSet    NetLibConfigAliasSet68k
#define NetLibIFSettingGet      NetLibIFSettingGet68k
#define NetLibConfigAliasGet    NetLibConfigAliasGet68k
#define NetLibIFSettingSet      NetLibIFSettingSet68k

#include <PalmOSARM.h>
#include <PalmCompatibility.h>
#undef NetLibOpen
#undef NetLibClose
#undef NetLibOpenConfig
#undef NetLibOpenCount
#undef NetLibFinishCloseWait
#undef NetLibConnectionRefresh
#undef NetLibSocketConnect
#undef NetLibSocketClose
#undef NetLibSocketShutdown
#undef NetLibGetHostByName
#undef NetLibSocketOpen
#undef NetLibSocketClose
#undef NetLibSocketOptionSet
#undef NetLibSocketOptionGet
#undef NetLibSend
#undef NetLibReceive
#undef NetLibSocketBind
#undef NetLibSocketListen
#undef NetLibSocketAccept
#undef NetLibSettingGet
#undef NetLibAddrINToA
#undef NetLibAddrAToIN
#undef NetLibIFGet
#undef NetLibIFUp
#undef NetLibConfigMakeActive
#undef NetLibConfigList
#undef NetLibConfigSaveAs
#undef NetLibConfigIndexFromName
#undef NetLibGetHostByAddr
#undef NetLibIFAttach
#undef NetLibIFDetach
#undef NetLibConfigSaveAs
#undef NetLibConfigAliasSet
#undef NetLibIFSettingGet
#undef NetLibConfigAliasGet
#undef NetLibIFSettingSet
//=====================================================
// Macros that convert native integers to and from
//  big-endian (network) order which is the order used to store
//  variable length integers by the BitMove utilities.
//====================================================

#define _NetSwap16(x) \
    ((((x) >> 8) & 0xFF) | \
     (((x) & 0xFF) << 8))

#define _NetSwap32(x) \
    ((((x) >> 24) & 0x00FF) | \
     (((x) >>  8) & 0xFF00) | \
     (((x) & 0xFF00) <<  8) | \
     (((x) & 0x00FF) << 24))

#define NetHToNS(x) _NetSwap16(x)
#define NetHToNL(x) _NetSwap32(x)
#define NetNToHS(x) _NetSwap16(x)
#define NetNToHL(x) _NetSwap32(x)

#define NetLibConfigSaveAs NetLibConfigSave // fdie@ seems as the entry is badly named, name fixed in clients

Err NetLibOpen                  (UInt16 *netIFErrsP);
Err NetLibClose                 (UInt16 immediate);
Err NetLibOpenCount             (UInt16 *countP);
Err NetLibOpenConfig            (UInt16 configIndex, UInt32 openFlags, UInt16 *netIFErrP);
Err NetLibFinishCloseWait       ();
Err NetLibConnectionRefresh     (Boolean refresh, UInt8 *allInterfacesUpP, UInt16 *netIFErrP);
Int16 NetLibSocketConnect       (NetSocketRef socket, NetSocketAddrType *sockAddrP, Int16 addrLen,
                                    Int32 timeout, Err *errP);
Int16 NetLibSocketClose         (NetSocketRef socket, Int32 timeout, Err *errP);
NetHostInfoPtr NetLibGetHostByName
                                (const Char *nameP, NetHostInfoBufPtr bufP, Int32 timeout, Err *errP);
NetSocketRef NetLibSocketOpen   (NetSocketAddrEnum domain, NetSocketTypeEnum type,
                                    Int16 protocol, Int32 timeout, Err *errP);
Int16 NetLibSocketClose         (NetSocketRef socket, Int32 timeout, Err *errP);
Int16	NetLibSocketShutdown      (NetSocketRef socket, Int16 /*NetSocketDirEnum*/ direction, Int32 timeout, Err *errP);
Int16 NetLibSocketOptionSet     (NetSocketRef socket, UInt16 /*NetSocketOptLevelEnum*/ level,
                                    UInt16 /*NetSocketOptEnum*/ option, void *optValueP, UInt16 optValueLen,
                                    Int32 timeout, Err *errP);
Int16 NetLibSocketOptionGet     (NetSocketRef socket, UInt16 /*NetSocketOptLevelEnum*/ level,
                                UInt16 /*NetSocketOptEnum*/ option, void *optValueP, UInt16 *optValueLenP,
                                Int32 timeout, Err *errP);

Int16 NetLibSend                (NetSocketRef socket, void *bufP, UInt16 bufLen, UInt16 flags,
                                    void *toAddrP, UInt16 toLen, Int32 timeout, Err *errP);
Int16 NetLibReceive             (NetSocketRef socket, void *bufP, UInt16 bufLen, UInt16 flags,
                                    void *fromAddrP, UInt16 *fromLenP, Int32 timeout, Err *errP);
Int16 NetLibSocketBind          (NetSocketRef socket, NetSocketAddrType *sockAddrP, Int16 addrLen,
                                    Int32 timeout, Err *errP);
Int16 NetLibSocketListen        (NetSocketRef socket, UInt16 queueLen, Int32 timeout, Err *errP);
Err NetLibSettingGet            (UInt16 setting, void *valueP, UInt16 *valueLenP);
Int16 NetLibSocketAccept        (NetSocketRef socket, NetSocketAddrType *sockAddrP, Int16 *addrLenP,
                                    Int32 timeout, Err *errP);
Char *NetLibAddrINToA           (NetIPAddr inet, Char *spaceP);
NetIPAddr NetLibAddrAToIN       (const Char *a);
Err NetLibIFGet                 (UInt16 index, UInt32 *ifCreatorP, UInt16 *ifInstanceP);
Err NetLibIFUp                  (UInt32 ifCreator, UInt16 ifInstance);
Err NetLibConfigMakeActive      (UInt16 configIndex);
Err NetLibConfigList            (NetConfigNameType nameArray[], UInt16 *arrayEntriesP);
Err NetLibConfigSave            (NetConfigNamePtr nameP);
Err NetLibConfigIndexFromName   (NetConfigNamePtr nameP, UInt16 *indexP);
NetHostInfoPtr  NetLibGetHostByAddr (UInt8 *addrP, UInt16 len, UInt16 type, NetHostInfoBufPtr bufP, Int32 timeout, Err *errP);
Err NetLibIFAttach              (UInt32 ifCreator, UInt16 ifInstance, Int32 timeout);
Err NetLibIFDetach              (UInt32 ifCreator, UInt16 ifInstance, Int32 timeout);
Err NetLibConfigSaveAs          (NetConfigNamePtr nameP);
Err NetLibConfigAliasSet        (UInt16 configIndex, UInt16 aliasToIndex);
Err NetLibIFSettingGet          (UInt32 ifCreator, UInt16 ifInstance, UInt16 setting, void *valueP, UInt16 *valueLenP);
Err NetLibConfigAliasGet        (UInt16 configIndex, UInt16 *aliasToIndex, Boolean *isAnotherAliasP);
Err NetLibIFSettingSet          (UInt32 ifCreator, UInt16 ifInstance, UInt16 setting, void *valueP, UInt16 valueLenP);

#define netLibCreator 'netl'
#define netLibType 'libr'
#endif

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
