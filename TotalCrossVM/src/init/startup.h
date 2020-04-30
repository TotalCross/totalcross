// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef STARTUP_H
#define STARTUP_H


#include "tcvm.h"


#ifdef __cplusplus
extern "C" {
#endif
/**
 * Executes the program using the given arguments. This should be called only by the launcher.
 * if the commandline starts with "-" the mainloop is not executed, it will be executed on a second call.
 * (This two phase execution is required on the iPhone where the "view" have to be called
 * in the mainthread and the mainloop in a dedicated thread.
 */
TC_API int32 executeProgram(CharP args);
typedef int32 (*executeProgramFunc)(CharP args);

TC_API int32 startVM(CharP argsOriginal, Context* cOut);
TC_API int32 startProgram(Context currentContext);

bool wokeUp();  

#ifdef __cplusplus
}
#endif

#ifdef ANDROID
#ifdef __cplusplus
 extern "C" {
#endif
   jclass androidFindClass(JNIEnv* env, CharP className);
   char* getTotalCrossAndroidClass(CharP className);
   JNIEnv* getJNIEnv();
   #define JOBJ_CLASS(x) (*env)->GetObjectClass(env, x)
#ifdef __cplusplus
 } // __cplusplus
#endif
#endif

#endif
