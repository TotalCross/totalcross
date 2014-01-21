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


#if defined (darwin)
#ifdef __cplusplus
extern "C" {
#endif
    bool iphone_mapsShowAddress(char* addr, bool showSatellitePhotos);
#ifdef __cplusplus
};
#endif
#endif // darwin

//////////////////////////////////////////////////////////////////////////
TC_API void tmGM_showAddress_sb(NMParams p) // totalcross/map/GoogleMaps native static boolean showAddress(String address, boolean showSatellitePhotos);
{
#ifdef ANDROID
   JNIEnv* env = getJNIEnv();         
   TCObject addr = p->obj[0];
   jstring jaddr = (*env)->NewString(env, (jchar*) String_charsStart(addr), String_charsLen(addr));
   jboolean result = (*env)->CallStaticBooleanMethod(env, applicationClass, jshowGoogleMaps, jaddr, (jboolean) p->i32[0]);
   (*env)->DeleteLocalRef(env, jaddr);
   p->retI = result != 0;
#elif defined darwin
   TCObject addr = p->obj[0];
   CharP addrp = JCharP2CharP(String_charsStart(addr), String_charsLen(addr));
   bool sat = p->i32[0];
   p->retI = addrp ? iphone_mapsShowAddress(addrp,sat) : 0;
   xfree(addrp);
#else
   p->retI = false;
#endif	
}
//////////////////////////////////////////////////////////////////////////
TC_API void tmGM_showRoute_sssb(NMParams p) // totalcross/map/GoogleMaps native static boolean showRoute(String addressI, String addressF, String traversedPoints, boolean showSatellitePhotos);
{
#ifdef ANDROID
   JNIEnv* env = getJNIEnv();         
   TCObject addrI = p->obj[0];
   TCObject addrF = p->obj[1];
   TCObject coord = p->obj[2];
   jstring jaddrI = (*env)->NewString(env, (jchar*) String_charsStart(addrI), String_charsLen(addrI));
   jstring jaddrF = (*env)->NewString(env, (jchar*) String_charsStart(addrF), String_charsLen(addrF));
   jstring jcoord = !coord ? null : (*env)->NewString(env, (jchar*) String_charsStart(coord), String_charsLen(coord));
   jboolean result = (*env)->CallStaticBooleanMethod(env, applicationClass, jshowRoute, jaddrI, jaddrF, jcoord, (jboolean) p->i32[0]);
   (*env)->DeleteLocalRef(env, jaddrI);
   (*env)->DeleteLocalRef(env, jaddrF);
   if (jcoord) (*env)->DeleteLocalRef(env, jcoord);
   p->retI = result != 0;
#elif defined darwin
   p->retI = false;
#endif	
}
