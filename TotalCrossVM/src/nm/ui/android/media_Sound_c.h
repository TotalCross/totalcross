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

// $Id$

static void soundSetEnabled(bool b)
{
   JNIEnv* env = getJNIEnv();
   (*env)->CallStaticVoidMethod(env, applicationClass, jsoundEnable, b);
}

static void soundBeep(void)
{
   JNIEnv* env = getJNIEnv();
   (*env)->CallStaticVoidMethod(env, applicationClass, jtone, 99999,0);
}

bool soundTone(int32 freq, uint16 duration)
{
   JNIEnv* env = getJNIEnv();
   (*env)->CallStaticVoidMethod(env, applicationClass, jtone, freq, duration);
}
