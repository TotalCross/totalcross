// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "winsockLib.h"

#pragma pack(8)
#include <ws2bth.h>
#pragma pack()

#if defined (WINCE)
 #include <Bt_sdp.h>

 #define BTH_ADDR BT_ADDR
 #define getCOD(bthInquiry) bthInquiry->cod
#else
 #include <Bthdef.h>
 #include <BluetoothAPIs.h>

 #define getCOD(bthInquiry) bthInquiry->classOfDevice
 #define BthInquiryResult BTH_DEVICE_INFO
#endif

typedef struct
{
   HANDLE hLookup;
   BTH_ADDR btAddr;
} TNATIVE_FIELDS, *NATIVE_FIELDS;

#define QUERY_BUF_SIZE 1024

DWORD WINAPI ThreadStartInquiry(LPVOID lpParameter);
DWORD WINAPI ThreadStartSearch(LPVOID lpParameter);

static Err nativeRetrieveDevices(VoidPs** devices, Heap h, int32* count)
{
   WSADATA wsaData;
   WSAQUERYSET querySet;
   HANDLE hLookup = INVALID_HANDLE_VALUE;
   uint8 queryBuf[QUERY_BUF_SIZE];
   int32 queryBufLen;
   LPWSAQUERYSET queryBufP = (LPWSAQUERYSET) queryBuf;
   BTDEVICE* btDeviceP;
   BTH_ADDR btAddr;
   Err err;

   *count = 0;

   if ((err = WSAStartup(MAKEWORD(2, 2), &wsaData)) != NO_ERROR)
      return err;

   xmemzero(&querySet, sizeof(WSAQUERYSET));
   querySet.dwNameSpace = NS_BTH;
   querySet.dwSize = sizeof(WSAQUERYSET);

   if ((err = WSALookupServiceBegin(&querySet, LUP_CONTAINERS, &hLookup)) != NO_ERROR)
   {
      err = WSAGetLastError();
      WSACleanup();
      return err;      
   }
      
   do
   {
      queryBufLen = QUERY_BUF_SIZE;
      if ((err = WSALookupServiceNext(hLookup, LUP_RETURN_NAME | LUP_RETURN_ADDR, &queryBufLen, queryBufP)) == NO_ERROR)
      {
         btDeviceP = (BTDEVICE*) heapAlloc(h, sizeof(BTDEVICE));
         // Copy device name
         tcscpy(btDeviceP->friendlyName, queryBufP->lpszServiceInstanceName);
         // Copy bluetooth device address
         btAddr = ((SOCKADDR_BTH *)queryBufP->lpcsaBuffer->RemoteAddr.lpSockaddr)->btAddr;
         _stprintf(btDeviceP->address, TEXT("%04x%08x"), GET_NAP(btAddr), GET_SAP(btAddr));

         *devices = VoidPsAdd(*devices, btDeviceP ,h); // add entry to list
         (*count)++;
      }
      else
         err = WSAGetLastError();
   } while (err == NO_ERROR);

   WSALookupServiceEnd(hLookup);
   
   if (err == WSA_E_NO_MORE) // lookup actually finished successfuly
      err = NO_ERROR;

   WSACleanup();
   return err;
}

// Mutex will be already locked by tidbDA_cancelInquiry_d, we'll always have exclusive access to the search structure from here.
void nativeCancelInquiry(DeviceSearch deviceSearchP)
{
   HANDLE* hLookupP = &((NATIVE_FIELDS) deviceSearchP->nativeFields)->hLookup;
   WSALookupServiceEnd(*hLookupP);
}

// Mutex will be already locked by tidbDA_startInquiry_id, we'll always have exclusive access to the search structure from here.
Err nativeStartInquiry(DeviceSearch deviceSearchP, boolean* inquiryStarted)
{
   WSADATA wsaData;
   WSAQUERYSET querySet;
   HANDLE* hLookupP = &((NATIVE_FIELDS) deviceSearchP->nativeFields)->hLookup;
	HANDLE inquiryThread;
   Err err;

   *inquiryStarted = false;
   if ((err = WSAStartup(MAKEWORD(2, 2), &wsaData)) != NO_ERROR)
	   return err;

   // prepare querySet structure to search for Bluetooth devices.
   xmemzero(&querySet, sizeof(WSAQUERYSET));
   querySet.dwNameSpace = NS_BTH;
   querySet.dwSize = sizeof(WSAQUERYSET);

   if ((err = WSALookupServiceBegin(&querySet, LUP_CONTAINERS, hLookupP)) != NO_ERROR)
   {
      err = WSAGetLastError();
      WSACleanup();
      return err;
   }

	if ((inquiryThread = CreateThread(null, 0, ThreadStartInquiry, deviceSearchP, 0, null)) == null)
   {
      err = GetLastError();
      WSALookupServiceEnd(*hLookupP);
      WSACleanup();
      return err;
   }

   // ok, inquiry started!
   *inquiryStarted = true;
   return NO_ERROR;
}

DWORD WINAPI ThreadStartInquiry(LPVOID lpParameter)
{
   DeviceSearch deviceSearchP = (DeviceSearch) lpParameter;
   TCObject remoteDevice;
   TCObject deviceClass;
   
   TCObject friendlyName;
   TCObject address;

   TCHAR szAddress[13];

   HANDLE* hLookupP = &((NATIVE_FIELDS) deviceSearchP->nativeFields)->hLookup;
   uint8 queryBuf[QUERY_BUF_SIZE];
   int32 queryBufLen;
   LPWSAQUERYSET queryBufP = (LPWSAQUERYSET) queryBuf;
   BTH_ADDR btAddr;
   BthInquiryResult* bthInquiry;
   boolean keepRunning = true;
   Err err = NO_ERROR;

   while (keepRunning)
   {
      queryBufLen = QUERY_BUF_SIZE;
      if ((err = WSALookupServiceNext(*hLookupP, LUP_RETURN_NAME | LUP_RETURN_ADDR | LUP_RETURN_BLOB, &queryBufLen, queryBufP)) != NO_ERROR)
      {
         err = WSAGetLastError();
         break; // only case we break out of the loop.
      }
      else if ((remoteDevice = createObjectWithoutCallingDefaultConstructor(deviceSearchP->currentContext, "totalcross.io.device.bluetooth.RemoteDevice")) == null)
         keepRunning = false;
      else
      {
         if ((deviceClass = createObjectWithoutCallingDefaultConstructor(deviceSearchP->currentContext, "totalcross.io.device.bluetooth.DeviceClass")) == null)
            keepRunning = false;
         else
         {
            if (deviceSearchP->remoteDeviceConstructor == null && (deviceSearchP->remoteDeviceConstructor = getMethod(OBJ_CLASS(remoteDevice), false, CONSTRUCTOR_NAME, 1, "java.lang.String")) == null)
               keepRunning = false;
            else if (deviceSearchP->deviceClassConstructor == null && (deviceSearchP->deviceClassConstructor = getMethod(OBJ_CLASS(deviceClass), false, CONSTRUCTOR_NAME, 1, J_INT)) == null)
               keepRunning = false;
            else
            {
               bthInquiry = (BthInquiryResult*) queryBufP->lpBlob->pBlobData;
               // Copy bluetooth device address
               btAddr = ((SOCKADDR_BTH *)queryBufP->lpcsaBuffer->RemoteAddr.lpSockaddr)->btAddr;
               _stprintf(szAddress, TEXT("%04x%08x"), GET_NAP(btAddr), GET_SAP(btAddr));

               // Copy device name
               if (queryBufP->lpszServiceInstanceName == null)
                  friendlyName = createStringObjectWithLen(deviceSearchP->currentContext, 0);
               else
                  friendlyName = createStringObjectFromTCHARP(deviceSearchP->currentContext, queryBufP->lpszServiceInstanceName, -1);

               if (friendlyName == null)
                  keepRunning = false;
               else
               {
                  if ((address = createStringObjectFromTCHARP(deviceSearchP->currentContext, szAddress, -1)) == null)
                     keepRunning = false;
                  else
                  {
                     // Call RemoteDevice constructor and set the object's fields
                     executeMethod(deviceSearchP->currentContext, deviceSearchP->remoteDeviceConstructor, remoteDevice, address);
                     RemoteDevice_friendlyName(remoteDevice) = friendlyName;
                     setObjectLock(address, UNLOCKED);

                     // Call DeviceClass constructor
                     executeMethod(deviceSearchP->currentContext, deviceSearchP->deviceClassConstructor, deviceClass, getCOD(bthInquiry));
                     executeMethod(deviceSearchP->currentContext, deviceSearchP->deviceDiscovered, deviceSearchP->listener, remoteDevice, deviceClass);
                  }
                  setObjectLock(friendlyName, UNLOCKED);
               }
               setObjectLock(deviceClass, UNLOCKED);
            }
         }
         setObjectLock(remoteDevice, UNLOCKED);
      }
   }

   LOCKVAR(deviceSearchP->deviceInquiry);
   if (err == WSA_E_CANCELLED || err == WSAECANCELLED)
      deviceSearchP->inquiryStatus = INQUIRY_TERMINATED;
   else
   {
      if (err == NO_ERROR || err == WSA_E_NO_MORE || err == WSAENOMORE)
         deviceSearchP->inquiryStatus = INQUIRY_COMPLETED;
      else
         deviceSearchP->inquiryStatus = INQUIRY_ERROR;
      WSALookupServiceEnd(*hLookupP);
   }
   WSACleanup();
   // call inquiryCompleted
   executeMethod(deviceSearchP->currentContext, deviceSearchP->inquiryCompleted, deviceSearchP->listener, deviceSearchP->inquiryStatus);
   UNLOCKVAR(deviceSearchP->deviceInquiry);

   // the thread returns the error value - which may be retrieved with GetExitCodeThread
   return err;
}

void convertUUIDBytesToGUID(uint8* bytes, GUID* uuid)
{
   int32 i;

   uuid->Data1 = bytes[0]<<24&0xff000000|bytes[1]<<16&0x00ff0000|bytes[2]<<8&0x0000ff00|bytes[3]&0x000000ff;
   uuid->Data2 = bytes[4]<<8&0xff00|bytes[5]&0x00ff;
   uuid->Data3 = bytes[6]<<8&0xff00|bytes[7]&0x00ff;

   for (i = 0; i < 8; i++)
      uuid->Data4[i] = bytes[i + 8];
}

Err getServicesHandle(Context currentContext, TCObject uuidSet, BTH_ADDR btAddress, ULONG** serviceRecordHandleArray, int32* serviceRecordHandleArrayLen)
{
   LPWSAQUERYSET pwsaResults;
   HANDLE hLookupSearchServices;
   CSADDR_INFO csai;
   SOCKADDR_BTH sa;
   WSAQUERYSET querySet;
   BLOB blob;
   BTHNS_RESTRICTIONBLOB queryService;
   TCObjectArray uuidArray = (TCObjectArray) ARRAYOBJ_START(uuidSet);
   int32 uuidArrayLen = ARRAYOBJ_LEN(uuidSet);
   GUID guid;
   int32 i;
   Err err = -1;
   
   // set array len to 0
   *serviceRecordHandleArrayLen = 0;

   xmemzero(&queryService, sizeof(queryService));
   queryService.type = SDP_SERVICE_SEARCH_REQUEST;

   for (i = 0 ; i < uuidArrayLen ; uuidArray++, i++)
   {
      queryService.uuids[i].uuidType = SDP_ST_UUID128;
      convertUUIDBytesToGUID(ARRAYOBJ_START(UUID_bytes(*uuidArray)), &guid);
      queryService.uuids[i].u.uuid128 = guid;
   }

   // build BLOB pointing to service query
   blob.cbSize = sizeof(queryService);
   blob.pBlobData = (BYTE *)&queryService;

   // build query
   xmemzero(&querySet, sizeof(WSAQUERYSET));
   querySet.dwSize = sizeof(WSAQUERYSET);
   querySet.dwNameSpace = NS_BTH;
   querySet.lpBlob = &blob;

   // Build address
   xmemzero(&sa, sizeof(sa));
   sa.addressFamily = AF_BTH;
   sa.btAddr = btAddress;

   xmemzero(&csai, sizeof(csai));
   csai.RemoteAddr.lpSockaddr = (struct sockaddr *) &sa;
   csai.RemoteAddr.iSockaddrLength = sizeof(sa);
   querySet.lpcsaBuffer = &csai;

   // begin query
   if (WSALookupServiceBegin(&querySet, 0, &hLookupSearchServices) != NO_ERROR)
      err = WSAGetLastError();
   else
   {
      int32 wsaResultSize = 0x2000;

      if ((pwsaResults = (LPWSAQUERYSET) xmalloc(wsaResultSize)) == null)
         throwException(currentContext, OutOfMemoryError, null);
      else
      {
         xmemzero(pwsaResults, wsaResultSize);
         pwsaResults->dwSize = sizeof(WSAQUERYSET);
         pwsaResults->dwNameSpace = NS_BTH;
         pwsaResults->lpBlob = null;

         if ((err = WSALookupServiceNext(hLookupSearchServices, 0, &wsaResultSize, pwsaResults)) != NO_ERROR)
         {
            err = WSAGetLastError();
            if (err == WSANO_DATA || err == WSA_E_NO_MORE || err == WSAENOMORE) // NO MORE RECORDS
               err = NO_ERROR;
         }
         else if(((*serviceRecordHandleArray) = (ULONG*) xmalloc(pwsaResults->lpBlob->cbSize)) == null)
            throwException(currentContext, OutOfMemoryError, null);
         else
         {
            *serviceRecordHandleArrayLen = pwsaResults->lpBlob->cbSize/sizeof(ULONG);
            xmemmove(*serviceRecordHandleArray, pwsaResults->lpBlob->pBlobData, pwsaResults->lpBlob->cbSize);
         }
         xfree(pwsaResults);
      }
      WSALookupServiceEnd(hLookupSearchServices);
   }

   if (currentContext->thrownException != null)
      return -1;
	return err;
}

Err getServiceAttributes(Context currentContext, ULONG serviceHandle, BTH_ADDR btAddress, TCObject attrSet, TCObject* byteArray)
{
   HANDLE hLookupServiceAttributes;
   CSADDR_INFO csai;
   SOCKADDR_BTH sa;
   WSAQUERYSET queryset;
   BLOB blob;
	BTHNS_RESTRICTIONBLOB *queryService;
   TCObjectArray attrArray = (TCObjectArray) ARRAYOBJ_START(attrSet);
   int32 attrArrayLen = ARRAYOBJ_LEN(attrSet);
   Err err = -1;
   
   if ((queryService = (BTHNS_RESTRICTIONBLOB*) xmalloc(sizeof(BTHNS_RESTRICTIONBLOB) + sizeof(SdpAttributeRange))) == null)
      throwException(currentContext, OutOfMemoryError, null);
   else
   {
      xmemzero(queryService, sizeof(BTHNS_RESTRICTIONBLOB));
      queryService->type = SDP_SERVICE_ATTRIBUTE_REQUEST;
      queryService->numRange = 1;
      queryService->serviceHandle = serviceHandle;

	   // set attribute ranges
	   queryService->pRange[0].minAttribute = *((int32*) ARRAYOBJ_START(attrSet)); //SDP_ATTRIB_PROTOCOL_DESCRIPTOR_LIST; //(USHORT)ints[0];
	   queryService->pRange[0].maxAttribute = *((int32*) ARRAYOBJ_START(attrSet) + (ARRAYOBJ_LEN(attrSet) - 1)); //SDP_ATTRIB_PROTOCOL_DESCRIPTOR_LIST; //(USHORT)ints[env->GetArrayLength(attrIDs)-1];

	   // build BLOB pointing to attribute query
	   blob.cbSize = sizeof(BTHNS_RESTRICTIONBLOB);
	   blob.pBlobData = (BYTE*) queryService;

	   // build query
	   xmemzero(&queryset, sizeof(WSAQUERYSET));
	   queryset.dwSize = sizeof(WSAQUERYSET);
	   queryset.dwNameSpace = NS_BTH;

	   // Build address
	   xmemzero(&sa, sizeof(sa));
	   sa.addressFamily = AF_BTH;
	   sa.btAddr = btAddress;

	   xmemzero(&csai, sizeof(csai));
	   csai.RemoteAddr.lpSockaddr = (struct sockaddr*) &sa;
	   csai.RemoteAddr.iSockaddrLength = sizeof(sa);
	   queryset.lpcsaBuffer = &csai;
	   queryset.lpBlob = &blob;

	   // begin query
	   if (WSALookupServiceBegin(&queryset, 0, &hLookupServiceAttributes) != NO_ERROR)
         err = WSAGetLastError();
      else
      {
	      // fetch results
         LPWSAQUERYSET pwsaResults;
	      int32 pwsaResultsSize = 0x2000;

         if ((pwsaResults = (LPWSAQUERYSET) xmalloc(pwsaResultsSize)) == null)
            throwException(currentContext, OutOfMemoryError, null);
         else
         {
	         xmemzero(pwsaResults, pwsaResultsSize);
	         pwsaResults->dwSize = sizeof(WSAQUERYSET);
	         pwsaResults->dwNameSpace = NS_BTH;
	         pwsaResults->lpBlob = null;

	         if (WSALookupServiceNext(hLookupServiceAttributes, 0, &pwsaResultsSize, pwsaResults) != NO_ERROR)
               err = WSAGetLastError();
            else
            {
               *byteArray = createByteArray(currentContext, pwsaResults->lpBlob->cbSize);
               xmemmove(ARRAYOBJ_START(*byteArray), pwsaResults->lpBlob->pBlobData, pwsaResults->lpBlob->cbSize);
	         }
            xfree(pwsaResults);
         }
	      WSALookupServiceEnd(hLookupServiceAttributes);
      }
      xfree(queryService);
   }

	return err;
}

// Mutex will be already locked by tidbDA_startInquiry_id, we'll always have exclusive access to the search structure from here.
Err nativeSearchServices(Context currentContext, TCObject remoteDevice, ServiceSearch serviceSearchP, TCObject attrSet, TCObject uuidSet, boolean* searchStarted)
{
   HANDLE* hLookupP = &((NATIVE_FIELDS) serviceSearchP->nativeFields)->hLookup;
   BTH_ADDR _btAddr;
   BTH_ADDR* btAddr = &_btAddr;
   Err err = -1; // return -1 when exception is thrown
   int32 i;

	if (ARRAYOBJ_LEN(uuidSet) > MAX_UUIDS_IN_QUERY)
      throwException(currentContext, IllegalArgumentException, "Maximum number of service attributes to be retrieved per service record exceeded.");
   else if (radix2long(serviceSearchP->address, 16, btAddr) == false)
      throwException(currentContext, IllegalArgumentException, "Device address is not valid.");
   else
   {
      ULONG* serviceRecordHandleArray;
      int32 serviceRecordHandleArrayLen;

      err = getServicesHandle(currentContext, uuidSet, *btAddr, &serviceRecordHandleArray, &serviceRecordHandleArrayLen);
      if (err != NO_ERROR && currentContext->thrownException == null)
         throwExceptionWithCode(currentContext, IOException, err);
      else if (err == NO_ERROR)
      {
         if (serviceRecordHandleArrayLen == 0) // no service records found, just finish the search.
            executeMethod(serviceSearchP->currentContext, serviceSearchP->serviceSearchCompleted, serviceSearchP->listener, 0, SERVICE_SEARCH_NO_RECORDS);
         else
         {
            int32 result;
            TCObject byteArray;
            TCObject serviceRecordArray = createArrayObject(serviceSearchP->currentContext, "[totalcross.io.device.bluetooth.ServiceRecord", serviceRecordHandleArrayLen);
            if (serviceRecordArray != null) {
                Method readSDP = getMethod(loadClass(serviceSearchP->currentContext,"totalcross.io.device.bluetooth.ServiceRecord",false), false, "readSDP", 3, "totalcross.io.device.bluetooth.RemoteDevice", BYTE_ARRAY, INT_ARRAY);
                if (readSDP != null) {
                    for (i = 0 ; i < serviceRecordHandleArrayLen ; i++) {
                       TCObject serviceRecord = createObject(serviceSearchP->currentContext, "totalcross.io.device.bluetooth.ServiceRecord");
                       if (serviceRecord != null) {
                           getServiceAttributes(currentContext, serviceRecordHandleArray[i], *btAddr, attrSet, &byteArray);
                           result = executeMethod(serviceSearchP->currentContext, readSDP, serviceRecord, remoteDevice, byteArray, attrSet).asInt32;
            
                           *((TCObjectArray) ARRAYOBJ_START(serviceRecordArray) + i) = serviceRecord;
                           setObjectLock(serviceRecord, UNLOCKED);
                       }
                    }
                    xfree(serviceRecordHandleArray);
                    executeMethod(serviceSearchP->currentContext, serviceSearchP->servicesDiscovered, serviceSearchP->listener, 0, serviceRecordArray);
                    executeMethod(serviceSearchP->currentContext, serviceSearchP->serviceSearchCompleted, serviceSearchP->listener, 0, SERVICE_SEARCH_COMPLETED);
                }
                setObjectLock(serviceRecordArray, UNLOCKED);
            }
         }
      }
   }
   return err;
}
