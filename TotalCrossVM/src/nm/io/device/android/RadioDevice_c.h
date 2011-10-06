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

static jclass gRadioClass;
static jmethodID ggetStateMethod;

static bool RdIsSupported(Context currentContext, int32 type)
{
   JNIEnv* env = getJNIEnv();
   jclass jRadioClass = gRadioClass ? gRadioClass : (gRadioClass = androidFindClass(env, "totalcross/android/RadioDevice4A"));
   if (jRadioClass == null)
   {
      throwException(currentContext, IOException, "RadioDevice cannot be called from inside a thread.");
      return false;
   }
   else
   {
      jmethodID isSupportedMethod = (*env)->GetStaticMethodID(env, jRadioClass, "isSupported", "(I)Z");
      jboolean result = (*env)->CallStaticBooleanMethod(env, jRadioClass, isSupportedMethod, (jint) type);
      return result != 0;
   }
}

static int32 RdGetState(Context currentContext, int32 type)
{
   JNIEnv* env = getJNIEnv();
   jclass jRadioClass = gRadioClass ? gRadioClass : (gRadioClass = androidFindClass(env, "totalcross/android/RadioDevice4A"));
   if (jRadioClass == null)
   {
      throwException(currentContext, IOException, "RadioDevice cannot be called from inside a thread.");
      return 0;
   }
   else
   {
      jmethodID getStateMethod = ggetStateMethod ? ggetStateMethod : (ggetStateMethod = (*env)->GetStaticMethodID(env, jRadioClass, "getState", "(I)I"));
      jint result = (*env)->CallStaticIntMethod(env, jRadioClass, getStateMethod, (jint) type);
      return (int32) result;
   }
}

static void RdSetState(Context currentContext, int32 type, int32 state)
{
   JNIEnv* env = getJNIEnv();
   jclass jRadioClass = gRadioClass ? gRadioClass : (gRadioClass = androidFindClass(env, "totalcross/android/RadioDevice4A"));
   if (jRadioClass == null)
      throwException(currentContext, IOException, "RadioDevice cannot be called from inside a thread.");
   else
   {
      jmethodID setStateMethod = (*env)->GetStaticMethodID(env, jRadioClass, "setState", "(II)V");
      (*env)->CallStaticVoidMethod(env, jRadioClass, setStateMethod, (jint) type, (jint) state);
   }
}
