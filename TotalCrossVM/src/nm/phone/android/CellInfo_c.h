// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



static void CellInfoLoadResources(Context currentContext)
{
}

static void CellInfoReleaseResources()
{
}

static void CellInfoUpdate(int32* mcc, int32* mnc, int32* lac, int32* cellid, int32* signal)
{
   JNIEnv *env = getJNIEnv();
   jintArray arr;
   if (!env)
      return;
   arr = (*env)->CallStaticObjectMethod(env, applicationClass, jcellinfoUpdate);
   if (arr != null)
   {
      jint *data = (*env)->GetIntArrayElements(env, arr, 0);
      *mcc = data[0];
      *mnc = data[1];
      *lac = data[2];
      *cellid = data[3];
      *signal = data[4];
      (*env)->ReleaseIntArrayElements(env, arr, data, 0); // guich@tc125_1
      (*env)->DeleteLocalRef(env, arr); // guich@tc125_1
   }
}
