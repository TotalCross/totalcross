// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#define BT_ERROR -999
#define BT_INVALID_PASSWORD -998
#define BT_NO_ERROR 0

static TCObject nativeRetrieveDevices(Context currentContext, bool isPaired)
{      
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, jBluetooth4A, isPaired ? "getPairedDevices" : "getUnpairedDevices", "()[Ljava/lang/String;");
   jobjectArray inArray = (*env)->CallStaticObjectMethod(env, jBluetooth4A, m);
   TCObject outArray = null;
   if (inArray != null)
   {   
      int32 len = (*env)->GetArrayLength(env, inArray), i;
      outArray = createArrayObject(currentContext, "[totalcross.io.device.bluetooth.RemoteDevice", len);
      if (outArray != null)
      {           
         TCObjectArray out = (TCObjectArray)ARRAYOBJ_START(outArray);
         for (i = 0; i < len; i++, out++)
         {
            // split the value 000000000000|name
            jstring inValue = (jstring)(*env)->GetObjectArrayElement(env, inArray, i);
            char *str = (char*)(*env)->GetStringUTFChars(env, inValue, 0);
            TCObject addr = createStringObjectFromCharP(currentContext, str, 12);
            TCObject name = createStringObjectFromCharP(currentContext, str+13, -1);
            (*env)->ReleaseStringUTFChars(env, str, str);
            (*env)->DeleteLocalRef(env, inValue);
            if (addr != null && name != null)
            {
               *out = createObjectWithoutCallingDefaultConstructor(currentContext, "totalcross.io.device.bluetooth.RemoteDevice");
               if (*out != null)
               {
                  RemoteDevice_friendlyName(*out) = name;
                  RemoteDevice_address(*out) = addr;
                  setObjectLock(*out,UNLOCKED);
               }
            }
            setObjectLock(addr,UNLOCKED);
            setObjectLock(name,UNLOCKED);
         }
      }
      (*env)->DeleteLocalRef(env, inArray);
   }

   return outArray;
}

#if 0
void nativeCancelInquiry(DeviceSearch deviceSearchP)
{
}

Err nativeStartInquiry(DeviceSearch deviceSearchP, boolean* inquiryStarted)
{
   return NO_ERROR;
}

Err getServicesHandle(Context currentContext, TCObject uuidSet, BTH_ADDR btAddress, ULONG** serviceRecordHandleArray, int32* serviceRecordHandleArrayLen)
{
	return NO_ERROR;
}

Err getServiceAttributes(Context currentContext, ULONG serviceHandle, BTH_ADDR btAddress, TCObject attrSet, TCObject* byteArray)
{
	return NO_ERROR;
}

Err nativeSearchServices(Context currentContext, TCObject remoteDevice, ServiceSearch serviceSearchP, TCObject attrSet, TCObject uuidSet, boolean* searchStarted)
{
	return NO_ERROR;
}
#endif
