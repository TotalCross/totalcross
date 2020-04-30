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

#define GET_WH       1
#define CONFIGURE    2
#define SET_SIZE     3
#define SET_POSITION 4
#define SET_VISIBLE  5
#define IS_VISIBLE   6

static int32 callAdsFunc(int32 func, int32 i, TCObject str)
{                                                         
#ifdef ANDROID
   JNIEnv* env = getJNIEnv();
   jstring jstr = str == null ? null : (*env)->NewString(env, (jchar*) String_charsStart(str), String_charsLen(str));
   int32 ret = (*env)->CallStaticIntMethod(env, applicationClass, jadsFunc, func, i, jstr);
   if (jstr != null) (*env)->DeleteLocalRef(env, jstr);
   //int32 ii = debug("callAdsFunc(%d,%d,%X) -> %d", func, i, str, ret);
   return ret;
#else
   return 0;   
#endif   
}
//////////////////////////////////////////////////////////////////////////
TC_API void tmA_getHeightD_i(NMParams p) // totalcross/money/Ads native static int getHeightD(int size);
{
   p->retI = callAdsFunc(GET_WH, p->i32[0], null);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tmA_configureD_s(NMParams p) // totalcross/money/Ads native static void configureD(String id);
{
   callAdsFunc(CONFIGURE, 0, p->obj[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tmA_setSizeD_i(NMParams p) // totalcross/money/Ads native static void setSizeD(int s);
{
   callAdsFunc(SET_SIZE, p->i32[0], null);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tmA_setPositionD_i(NMParams p) // totalcross/money/Ads native static void setPositionD(int p);
{
   callAdsFunc(SET_POSITION, p->i32[0], null);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tmA_setVisibleD_b(NMParams p) // totalcross/money/Ads native static void setVisibleD(boolean b);
{
   callAdsFunc(SET_VISIBLE, p->i32[0], null);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tmA_isVisibleD(NMParams p) // totalcross/money/Ads native static boolean isVisibleD();
{
   p->retI = callAdsFunc(IS_VISIBLE, p->i32[0], null);
}

#ifdef ENABLE_TEST_SUITE
//#include "xml_XmlTokenizer_test.h"
#endif
