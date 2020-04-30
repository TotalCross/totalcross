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

#if defined (WIN32)
#ifndef __WINSOCKLIB__
#define __WINSOCKLIB__

#include <winsock2.h>

#ifndef WP8
LPFN_WSASTARTUP        WSAStartupProc;
LPFN_SOCKET            socketProc;
LPFN_INET_ADDR         inet_addrProc;
LPFN_GETHOSTBYNAME     gethostbynameProc;
LPFN_WSAGETLASTERROR   WSAGetLastErrorProc;
LPFN_HTONS             htonsProc;
LPFN_IOCTLSOCKET       ioctlsocketProc;
LPFN_CONNECT           connectProc;
LPFN_SELECT            selectProc;
LPFN_SHUTDOWN          shutdownProc;
LPFN_CLOSESOCKET       closesocketProc;
LPFN_WSACLEANUP        WSACleanupProc;
LPFN_RECV              recvProc;
LPFN_SEND              sendProc;
LPFN_BIND              bindProc;
LPFN_INET_NTOA         inet_ntoaProc;
LPFN_GETHOSTBYADDR     gethostbyaddrProc;
LPFN_GETHOSTNAME       gethostnameProc;
LPFN_LISTEN            listenProc;
LPFN_ACCEPT            acceptProc;
LPFN_NTOHL             ntohlProc;
LPFN_HTONL             htonlProc;
LPFN_GETSOCKNAME       getsocknameProc;

LPFN_WSALOOKUPSERVICEBEGIN   WSALookupServiceBeginProc;
LPFN_WSALOOKUPSERVICENEXT    WSALookupServiceNextProc;
LPFN_WSALOOKUPSERVICEEND     WSALookupServiceEndProc;
LPFN_WSASETSERVICE           WSASetServiceProc;
#endif

#if defined (WINCE)
// Types of radio device
typedef enum _RADIODEVTYPE
{
   RADIODEVICES_MANAGED = 1,
   RADIODEVICES_PHONE,
   RADIODEVICES_BLUETOOTH,
} RADIODEVTYPE;

// whether to save before or after changing state
typedef enum _SAVEACTION
{
   RADIODEVICES_DONT_SAVE = 0,
   RADIODEVICES_PRE_SAVE,
   RADIODEVICES_POST_SAVE,
} SAVEACTION;

// Details of radio devices
typedef struct _RDD
{
   LPTSTR pszDeviceName; // Device name for registry setting.
   LPTSTR pszDisplayName; // Name to show the world
   DWORD dwState; // ON/off/[Discoverable for BT]
   DWORD dwDesired; // desired state - used for setting registry etc.
   RADIODEVTYPE DeviceType; // Managed, phone, BT etc.
   struct _RDD* pNext; // Next device in list
} RDD; //radio device details

#define GetWirelessDevice_ORDINAL 276
#define ChangeRadioState_ORDINAL 273
#define FreeDeviceList_ORDINAL 280

//imports from ossvcs.dll
typedef LRESULT (CALLBACK* GetWirelessDevicesProc)(RDD **pDevices, DWORD dwFlags);
typedef LRESULT (CALLBACK* ChangeRadioStateProc)(RDD* pDev, DWORD dwState, SAVEACTION sa);
typedef LRESULT (CALLBACK* FreeDeviceListProc)(RDD *pRoot);

GetWirelessDevicesProc  pGetWirelessDevices;
ChangeRadioStateProc    pChangeRadioState;
FreeDeviceListProc      pFreeDeviceList;
#endif

#ifndef WP8
#ifdef UNICODE
#define _WSALookupServiceBegin_ TEXT("WSALookupServiceBeginW")
#define _WSALookupServiceNext_  TEXT("WSALookupServiceNextW")
#define _WSASetService_         TEXT("WSASetServiceW")
#else
#define _WSALookupServiceBegin_ TEXT("WSALookupServiceBeginA")
#define _WSALookupServiceNext_  TEXT("WSALookupServiceNextA")
#define _WSASetService_         TEXT("WSASetServiceA")
#endif /* !UNICODE */

#define WSAStartup(a, b)      (WSAStartupProc         == null ? WSAVERNOTSUPPORTED : WSAStartupProc(a, b))
#define socket(a, b, c)       (socketProc             == null ? INVALID_SOCKET : socketProc(a, b, c))
#define inet_addr(a)          (inet_addrProc          == null ? INADDR_NONE : inet_addrProc(a))
#define gethostbyname(a)      (gethostbynameProc      == null ? null : gethostbynameProc(a))
#define WSAGetLastError()     (WSAGetLastErrorProc    == null ? ERROR_CALL_NOT_IMPLEMENTED : WSAGetLastErrorProc())
#define htons(a)              (htonsProc              == null ? a : htonsProc(a))
#define ioctlsocket(a, b, c)  (ioctlsocketProc        == null ? SOCKET_ERROR : ioctlsocketProc(a, b, c))
#define connect(a, b, c)      (connectProc            == null ? 1 : connectProc(a, b, c))
#define select(a, b, c, d, e) (selectProc             == null ? SOCKET_ERROR : selectProc(a, b, c, d, e))
#define shutdown(a, b)        (shutdownProc           == null ? SOCKET_ERROR : shutdownProc(a, b))
#define closesocket(a)        (closesocketProc        == null ? SOCKET_ERROR : closesocketProc(a))
#define WSACleanup()          (WSACleanupProc         == null ? SOCKET_ERROR : WSACleanupProc())
#define recv(a, b, c, d)      (recvProc               == null ? SOCKET_ERROR : recvProc(a, b, c, d))
#define send(a, b, c, d)      (sendProc               == null ? SOCKET_ERROR : sendProc(a, b, c, d))
#define bind(a, b, c)         (bindProc               == null ? SOCKET_ERROR : bindProc(a, b, c))
#define inet_ntoa(a)          (inet_ntoaProc          == null ? null : inet_ntoaProc(a))
#define gethostbyaddr(a, b, c)(gethostbyaddrProc      == null ? null : gethostbyaddrProc(a, b, c))
#define gethostname(a, b)     (gethostnameProc        == null ? SOCKET_ERROR : gethostnameProc(a, b))
#define listen(a, b)          (listenProc             == null ? SOCKET_ERROR : listenProc(a, b))
#define accept(a, b, c)       (acceptProc             == null ? INVALID_SOCKET : acceptProc(a, b, c))
#define ntohl(a)              (ntohlProc              == null ? a : ntohlProc(a))
#define htonl(a)              (htonlProc              == null ? a : htonlProc(a))
#define getsockname(a, b, c)  (getsocknameProc        == null ? SOCKET_ERROR : getsocknameProc(a, b, c))

#define WSALookupServiceBegin(a, b, c)    (WSALookupServiceBeginProc == null ? SOCKET_ERROR : WSALookupServiceBeginProc(a, b, c))
#define WSALookupServiceNext(a, b, c, d)  (WSALookupServiceNextProc  == null ? SOCKET_ERROR : WSALookupServiceNextProc(a, b, c, d))
#define WSALookupServiceEnd(a)            (WSALookupServiceEndProc   == null ? SOCKET_ERROR : WSALookupServiceEndProc(a))
#define WSASetService(a, b, c)            (WSASetServiceProc         == null ? SOCKET_ERROR : WSASetServiceProc(a, b, c))
#endif

bool initWinsock();
void closeWinsock();

#if defined (WINCE)
#define _GetWirelessDevices(a, b)   (pGetWirelessDevices  == null ? WSAVERNOTSUPPORTED : pGetWirelessDevices(a, b))
#define _ChangeRadioState(a, b, c)  (pChangeRadioState    == null ? WSAVERNOTSUPPORTED : pChangeRadioState(a, b, c))
#define _FreeDeviceList(a)          (pFreeDeviceList      == null ? WSAVERNOTSUPPORTED : pFreeDeviceList(a))
#endif

#if defined (WINCE) // some type definitions for WINCE only
typedef struct _SdpAttributeRange
    {
    USHORT minAttribute;
    USHORT maxAttribute;
    }	SdpAttributeRange;

typedef /* [switch_type] */ union SdpQueryUuidUnion
    {
    /* [case()] */ GUID uuid128;
    /* [case()] */ ULONG uuid32;
    /* [case()] */ USHORT uuid16;
    }	SdpQueryUuidUnion;

typedef struct _SdpQueryUuid
    {
    /* [switch_is] */ SdpQueryUuidUnion u;
    USHORT uuidType;
    }	SdpQueryUuid;

typedef struct _BTHNS_RESTRICTIONBLOB
    {
    ULONG type;
    ULONG serviceHandle;
    SdpQueryUuid uuids[ 12 ];
    ULONG numRange;
    SdpAttributeRange pRange[ 1 ];
    }	BTHNS_RESTRICTIONBLOB;


#define MAX_UUIDS_IN_QUERY 12
#define SDP_SERVICE_SEARCH_REQUEST           1
#define SDP_SERVICE_ATTRIBUTE_REQUEST        2
#define SDP_SERVICE_SEARCH_ATTRIBUTE_REQUEST 3

enum SDP_SPECIFICTYPE
    {	SDP_ST_NONE	= 0,
	SDP_ST_UINT8	= 0x10,
	SDP_ST_UINT16	= 0x110,
	SDP_ST_UINT32	= 0x210,
	SDP_ST_UINT64	= 0x310,
	SDP_ST_UINT128	= 0x410,
	SDP_ST_INT8	= 0x20,
	SDP_ST_INT16	= 0x120,
	SDP_ST_INT32	= 0x220,
	SDP_ST_INT64	= 0x320,
	SDP_ST_INT128	= 0x420,
	SDP_ST_UUID16	= 0x130,
	SDP_ST_UUID32	= 0x230,
	SDP_ST_UUID128	= 0x430
    };
typedef enum SDP_SPECIFICTYPE SDP_SPECIFICTYPE;

#define BTH_SDP_VERSION 1
typedef struct _BTHNS_SETBLOB
    {
    ULONG __RPC_FAR *pSdpVersion;
    ULONG __RPC_FAR *pRecordHandle;
    ULONG Reserved[ 4 ];
    ULONG fSecurity;
    ULONG fOptions;
    ULONG ulRecordLength;
    UCHAR pRecord[ 1 ];
    }	BTHNS_SETBLOB;
#endif // ifdef WINCE



#endif
#endif // ifdef WIN32
