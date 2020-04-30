// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

#define LIAC 0x9E8B00
#define GIAC 0x9E8B33
#define PREKNOWN 1
#define CACHED 0

enum
{
   OPERATION_IN_PROGRESS = -1,            // -1
   INQUIRY_COMPLETED,                     //  0
   SERVICE_SEARCH_COMPLETED,              //  1
   SERVICE_SEARCH_TERMINATED,             //  2
   SERVICE_SEARCH_ERROR,                  //  3
   SERVICE_SEARCH_NO_RECORDS,             //  4
   INQUIRY_TERMINATED,                    //  5
   SERVICE_SEARCH_DEVICE_NOT_REACHABLE,   //  6
   INQUIRY_ERROR                          //  7
};

typedef struct
{
   TCHAR friendlyName[256];
   TCHAR address[33]; // or uuid
} BTDEVICE, *BTDEVICEP;

typedef struct
{
	Context currentContext;
   DECLARE_MUTEX(deviceInquiry);

	int32 accessCode;

   TCObject listener;
	Method deviceDiscovered;
	Method inquiryCompleted;

   Method remoteDeviceConstructor;
   Method deviceClassConstructor;
   Method serviceSearchCompleted;

   int32 inquiryStatus;
   TCObject nativeFieldsObj;
   VoidP nativeFields;
} TDeviceSearch, *DeviceSearch;

typedef struct
{
	Context currentContext;
   DECLARE_MUTEX(searchInquiry);

   int32 channel;

   TCObject attrSet;
   TCObject listener;
	Method servicesDiscovered;
	Method serviceSearchCompleted;

   Method serviceRecordConstructor;

   CharP address;
   int32 addressLen;

   int32 searchStatus;
   TCObject nativeFieldsObj;
   VoidP nativeFields;
} TServiceSearch, *ServiceSearch;

#if defined (WP8)
 
#elif defined (WIN32) || defined (WINCE)
 #include "win/DiscoveryAgent_c.h"
#elif defined ANDROID
 #include "android/DiscoveryAgent_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tidbDA_nativeDiscoveryAgent(NMParams p) // totalcross/io/device/bluetooth/DiscoveryAgent native private void nativeDiscoveryAgent();
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   TCObject discoveryAgent = p->obj[0];
   TCObject inquiryNativeFields = DiscoveryAgent_inquiryNativeFields(discoveryAgent);
   TCObject nativeFieldsObj;
   DeviceSearch deviceSearchP;

   if ((inquiryNativeFields = createByteArray(p->currentContext, sizeof(TDeviceSearch))) == null)
      throwException(p->currentContext, OutOfMemoryError, null);
   else if ((nativeFieldsObj = createByteArray(p->currentContext, sizeof(TNATIVE_FIELDS))) == null)
      throwException(p->currentContext, OutOfMemoryError, null);
   else
   {
      deviceSearchP = (DeviceSearch) ARRAYOBJ_START(inquiryNativeFields);
      INIT_MUTEX(deviceSearchP->deviceInquiry);
      LOCKVAR(deviceSearchP->deviceInquiry);
      deviceSearchP->inquiryStatus = INQUIRY_COMPLETED; // initialize with a status different from OPERATION_IN_PROGRESS
      deviceSearchP->nativeFieldsObj = nativeFieldsObj;
      deviceSearchP->nativeFields = (NATIVE_FIELDS) ARRAYOBJ_START(nativeFieldsObj);
      DiscoveryAgent_inquiryNativeFields(discoveryAgent) = inquiryNativeFields;
      UNLOCKVAR(deviceSearchP->deviceInquiry);
      setObjectLock(nativeFieldsObj, UNLOCKED);
   }
   setObjectLock(inquiryNativeFields, UNLOCKED);
#else
   p = 0;
#endif   
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidbDA_cancelInquiry_d(NMParams p) // totalcross/io/device/bluetooth/DiscoveryAgent native public boolean cancelInquiry(totalcross.io.device.bluetooth.DiscoveryListener listener);
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   TCObject listener = p->obj[1];

   TCObject discoveryAgent = p->obj[0];
   TCObject inquiryNativeFields = DiscoveryAgent_inquiryNativeFields(discoveryAgent);
   DeviceSearch deviceSearchP = (DeviceSearch) ARRAYOBJ_START(inquiryNativeFields);

   if (listener == null)
      throwException(p->currentContext, NullPointerException, null);
   else
   {
      LOCKVAR(deviceSearchP->deviceInquiry); // lock access to deviceSearch structure!
      if (deviceSearchP->inquiryStatus == OPERATION_IN_PROGRESS)
      {
         if (DiscoveryAgent_deviceInquiryListener(discoveryAgent) != listener)
            p->retI = false;
         else
         {
            nativeCancelInquiry(deviceSearchP);
            p->retI = true;
         }
      }
      UNLOCKVAR(deviceSearchP->deviceInquiry); // don't forget to unlock the structure!
   }
#else
   p = 0;
#endif   
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidbDA_cancelServiceSearch_i(NMParams p) // totalcross/io/device/bluetooth/DiscoveryAgent native public boolean cancelServiceSearch(int transID);
{
}
//////////////////////////////////////////////////////////////////////////

TC_API void tidbDA_retrieveDevices_i(NMParams p) // totalcross/io/device/bluetooth/DiscoveryAgent native public totalcross.io.device.bluetooth.RemoteDevice []retrieveDevices(int option);
{
#ifdef ANDROID
   setObjectLock(p->retO = nativeRetrieveDevices(p->currentContext, p->i32[0] == PREKNOWN), UNLOCKED);
#endif   
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidbDA_nativeSearchServices_IUrd(NMParams p) // totalcross/io/device/bluetooth/DiscoveryAgent native public int nativeSearchServices(int []attrSet, totalcross.io.device.bluetooth.UUID []uuidSet, totalcross.io.device.bluetooth.RemoteDevice btDev, totalcross.io.device.bluetooth.DiscoveryListener discListener) throws IOException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   TCObject attrSet = p->obj[1];
   TCObject uuidSet = p->obj[2];
   TCObject remoteDevice = p->obj[3];
   TCObject discListener = p->obj[4];
   int32* attrSetP = (int32*) ARRAYOBJ_START(attrSet);
   int32 attrSetLen = ARRAYOBJ_LEN(attrSet);
   
   TCObject addressObj = RemoteDevice_address(remoteDevice);

   // arguments were already checked on java, in DiscoveryAgent4D
   TCObject discoveryAgent = p->obj[0];
   TCObject inquiryNativeFields = DiscoveryAgent_inquiryNativeFields(discoveryAgent);
   ServiceSearch serviceSearchP = (ServiceSearch) ARRAYOBJ_START(inquiryNativeFields);

   boolean searchStarted = false;
   Err err;

   serviceSearchP->address = String2CharP(addressObj);
   serviceSearchP->addressLen = String_charsLen(addressObj);

   LOCKVAR(serviceSearchP->searchInquiry); // lock access to deviceSearch structure!
   if (serviceSearchP->searchStatus == OPERATION_IN_PROGRESS)
      throwException(p->currentContext, IOException, "A service search is already in progress.");
   else
   {
      serviceSearchP->servicesDiscovered = getMethod(OBJ_CLASS(discListener), true, "servicesDiscovered", 2, J_INT, "[totalcross.io.device.bluetooth.ServiceRecord");
      serviceSearchP->serviceSearchCompleted = getMethod(OBJ_CLASS(discListener), true, "serviceSearchCompleted", 2, J_INT, J_INT);

      if (serviceSearchP->servicesDiscovered == null || serviceSearchP->serviceSearchCompleted == null)
         throwException(p->currentContext, NoSuchMethodError, null); // methods not found?
      else
      {
         serviceSearchP->currentContext = p->currentContext;
         serviceSearchP->listener = discListener;
         if ((err = nativeSearchServices(p->currentContext, remoteDevice, serviceSearchP, attrSet, uuidSet, &searchStarted)) != NO_ERROR)
         {
            if (p->currentContext->thrownException != null) // check if an exception was already thrown by nativeSearchServices
               throwExceptionWithCode(p->currentContext, IOException, err);
         }
/* For now, we'll assume the user will behave and won't start more than one inquiry at once.
         else if ((p->retI = searchStarted) == true)
         {
            serviceSearchP->searchStatus = OPERATION_IN_PROGRESS; // searchStarted return true, set status to OPERATION_IN_PROGRESS.
            DiscoveryAgent_deviceInquiryListener(discoveryAgent) = discListener;
         }
*/
      }
   }
   UNLOCKVAR(serviceSearchP->searchInquiry); // don't forget to unlock the structure!
#else
   p = 0;
#endif   
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidbDA_selectService_uib(NMParams p) // totalcross/io/device/bluetooth/DiscoveryAgent native public String selectService(totalcross.io.device.bluetooth.UUID uuid, int security, boolean master) throws IOException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidbDA_startInquiry_id(NMParams p) // totalcross/io/device/bluetooth/DiscoveryAgent native public boolean startInquiry(int accessCode, totalcross.io.device.bluetooth.DiscoveryListener listener) throws IOException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   int32 accessCode = p->i32[0];
   TCObject listener = p->obj[1];

   TCObject discoveryAgent = p->obj[0];
   TCObject inquiryNativeFields = DiscoveryAgent_inquiryNativeFields(discoveryAgent);
   DeviceSearch deviceSearchP = (DeviceSearch) ARRAYOBJ_START(inquiryNativeFields);

   boolean inquiryStarted = false;
   Err err;

   // check if accessCode is valid
   if (accessCode != LIAC && accessCode != GIAC && (accessCode < 0x9E8B00 || accessCode > 0x9E8B3F))
      throwIllegalArgumentException(p->currentContext, "accessCode");
   else if (listener == null)
	   throwException(p->currentContext, NullPointerException, null);
   else
   {
      LOCKVAR(deviceSearchP->deviceInquiry); // lock access to deviceSearch structure!
      if (deviceSearchP->inquiryStatus == OPERATION_IN_PROGRESS)
         throwException(p->currentContext, IOException, "A device inquiry is already in progress.");
      else
      {
         deviceSearchP->deviceDiscovered = getMethod(OBJ_CLASS(listener), true, "deviceDiscovered", 2, "totalcross.io.device.bluetooth.RemoteDevice", "totalcross.io.device.bluetooth.DeviceClass");
         deviceSearchP->inquiryCompleted = getMethod(OBJ_CLASS(listener), true, "inquiryCompleted", 1, J_INT);

         if (deviceSearchP->deviceDiscovered == null || deviceSearchP->inquiryCompleted == null) // || servicesDiscovered == null || serviceSearchCompleted == null)
	         throwException(p->currentContext, NoSuchMethodError, null); // methods not found?
         else
         {
            deviceSearchP->currentContext = p->currentContext;
            deviceSearchP->accessCode = accessCode;
            deviceSearchP->listener = listener;
            if ((err = nativeStartInquiry(deviceSearchP, &inquiryStarted)) != NO_ERROR)
               throwExceptionWithCode(p->currentContext, IOException, err);
            else if ((p->retI = inquiryStarted) == true)
            {
               deviceSearchP->inquiryStatus = OPERATION_IN_PROGRESS; // inquiryStarted return true, set status to OPERATION_IN_PROGRESS.
               DiscoveryAgent_deviceInquiryListener(discoveryAgent) = listener;
            }
         }
      }
      UNLOCKVAR(deviceSearchP->deviceInquiry); // don't forget to unlock the structure!
   }
#else
   p = 0;
#endif
}

#ifdef ENABLE_TEST_SUITE
//#include "DiscoveryAgent_test.h"
#endif
