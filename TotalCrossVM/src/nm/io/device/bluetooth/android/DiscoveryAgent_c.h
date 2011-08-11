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

jclass gBluetooth4A;

static Object nativeRetrieveDevices(Context currentContext, bool isPaired)
{      
   JNIEnv* env = getJNIEnv();
   jclass jBluetooth4A = gBluetooth4A ? gBluetooth4A : (gBluetooth4A = (*env)->FindClass(env, "totalcross/android/Bluetooth4A"));
   jmethodID m = (*env)->GetStaticMethodID(env, jBluetooth4A, isPaired ? "getPairedDevices" : "getUnpairedDevices", "()[Ljava/lang/String;");
   jobjectArray inArray = (*env)->CallStaticObjectMethod(env, jBluetooth4A, m);
   Object outArray = null;
   if (inArray != null)
   {   
      int32 len = (*env)->GetArrayLength(env, inArray), i;
      outArray = createArrayObject(currentContext, "[totalcross.io.device.bluetooth.RemoteDevice", len);
      if (outArray != null)
      {           
         ObjectArray out = (ObjectArray)ARRAYOBJ_START(outArray);
         for (i = 0; i < len; i++, out++)
         {
            // split the value 000000000000|name
            jstring inValue = (jstring)(*env)->GetObjectArrayElement(env, inArray, i);
            char *str = (char*)(*env)->GetStringUTFChars(env, inValue, 0);
            Object addr = createStringObjectFromCharP(currentContext, str, 12);
            Object name = createStringObjectFromCharP(currentContext, str+13, -1);
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

Err getServicesHandle(Context currentContext, Object uuidSet, BTH_ADDR btAddress, ULONG** serviceRecordHandleArray, int32* serviceRecordHandleArrayLen)
{
	return NO_ERROR;
}

Err getServiceAttributes(Context currentContext, ULONG serviceHandle, BTH_ADDR btAddress, Object attrSet, Object* byteArray)
{
	return NO_ERROR;
}

Err nativeSearchServices(Context currentContext, Object remoteDevice, ServiceSearch serviceSearchP, Object attrSet, Object uuidSet, boolean* searchStarted)
{
	return NO_ERROR;
}
#endif