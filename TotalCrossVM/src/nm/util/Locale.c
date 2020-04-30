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
#ifdef ANDROID
#include <android/configuration.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "sys/system_properties.h"
#endif

#if defined (darwin)
void getDefaultToString(NMParams p);
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void juL_getDefaultToString(NMParams p) // java/util/Locale native String getDefaultToString();
{
#if defined (darwin)
    getDefaultToString(p);
#elif (ANDROID)
    JNIEnv* env = getJNIEnv();
    jstring defaultString = (*env)->CallStaticObjectMethod(env, applicationClass, jgetDefaultToString);
    char defaultChar[128];
    jstring2CharP(defaultString, defaultChar);
    debug(defaultChar);
    p->retO = createStringObjectFromCharP(p->currentContext, defaultChar, xstrlen(defaultChar));
    setObjectLock(p->retO, UNLOCKED);
#elif defined(WINCE) || defined(WIN32)
	TCHAR value[255];
	GetLocaleInfo(LOCALE_USER_DEFAULT,	LOCALE_SNAME, value,
	255);
	p->retO = createStringObjectFromTCHARP(p->currentContext, value, tcslen(value));
    setObjectLock(p->retO, UNLOCKED);
#else
    p->retO = NULL;
#endif

}
