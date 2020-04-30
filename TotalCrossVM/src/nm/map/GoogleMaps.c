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


#if defined (darwin)
#ifdef __cplusplus
extern "C" {
#endif
    bool iphone_mapsShowAddress(char* addr, int flags);
#ifdef __cplusplus
};
#endif
#endif // darwin

//////////////////////////////////////////////////////////////////////////
TC_API void tmGM_showAddress_sb(NMParams p) // totalcross/map/GoogleMaps native static boolean showAddress(String address, boolean showSatellitePhotos);
{
   TCObject addr = p->obj[0];
   if (!addr)
   {
      p->retI = false;
      return;
   }
#ifdef WP8
   p->retI = showMap(String_charsStart(addr), String_charsLen(addr), 0, 0);
#elif defined ANDROID
   JNIEnv* env = getJNIEnv();         
   jstring jaddr = (*env)->NewString(env, (jchar*) String_charsStart(addr), String_charsLen(addr));
   jboolean result = (*env)->CallStaticBooleanMethod(env, applicationClass, jshowGoogleMaps, jaddr, (jboolean) p->i32[0]);
   (*env)->DeleteLocalRef(env, jaddr);
   p->retI = result != 0;
#elif defined darwin
   CharP addrp = JCharP2CharP(String_charsStart(addr), String_charsLen(addr));
   p->retI = addrp ? iphone_mapsShowAddress(addrp,p->i32[0]) : 0;
   xfree(addrp);
#else
   p->retI = false;
#endif	
}
//////////////////////////////////////////////////////////////////////////
TC_API void tmGM_showRoute_sssi(NMParams p) // totalcross/map/GoogleMaps native static boolean showRoute(String addressI, String addressF, String traversedPoints, int flags);
{
   TCObject addrI = p->obj[0];
   TCObject addrF = p->obj[1];
   if (!addrI)
   {
      p->retI = false;
      return;
   }
#ifdef WP8
   p->retI = showMap(String_charsStart(addrI), String_charsLen(addrI), String_charsStart(addrF), String_charsLen(addrF));
#elif defined ANDROID
   JNIEnv* env = getJNIEnv();         
   TCObject coord = p->obj[2];
   jstring jaddrI = (*env)->NewString(env, (jchar*) String_charsStart(addrI), String_charsLen(addrI));
   jstring jaddrF = addrF ? (*env)->NewString(env, (jchar*) String_charsStart(addrF), String_charsLen(addrF)) : 0;
   jstring jcoord = !coord ? null : (*env)->NewString(env, (jchar*) String_charsStart(coord), String_charsLen(coord));
   jint result = (*env)->CallStaticIntMethod(env, applicationClass, jshowRoute, jaddrI, jaddrF, jcoord, p->i32[0]);
   (*env)->DeleteLocalRef(env, jaddrI);
   if (jaddrF) (*env)->DeleteLocalRef(env, jaddrF);
   if (jcoord) (*env)->DeleteLocalRef(env, jcoord);
   if (result == -2)
      throwException(p->currentContext, NotInstalledException, null);
   p->retI = result == 0;
#elif defined darwin
   CharP addrp = JCharP2CharP(String_charsStart(addrI), String_charsLen(addrI));
   p->retI = addrp ? iphone_mapsShowAddress(addrp,p->i32[0] | 2) : 0;
   xfree(addrp);
#endif
}
