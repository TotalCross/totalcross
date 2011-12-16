/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tmGM_showAddress_sb(NMParams p) // totalcross/map/GoogleMaps native static boolean showAddress(String address, boolean showSatellitePhotos);
{
#ifdef ANDROID
   JNIEnv* env = getJNIEnv();         
   Object addr = p->obj[0];
   jstring jaddr = (*env)->NewString(env, (jchar*) String_charsStart(addr), String_charsLen(addr));
   jboolean result = (*env)->CallStaticBooleanMethod(env, applicationClass, jshowGoogleMaps, jaddr, (jboolean) p->i32[0]);
   (*env)->DeleteLocalRef(env, jaddr);
   p->retI = result != 0;
#else
   p->retI = false;
#endif	
}
