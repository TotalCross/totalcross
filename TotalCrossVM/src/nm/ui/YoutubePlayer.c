// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "YoutubePlayer.h"
#include "tcvm.h"

extern Context mainContext;

#if defined(darwin)
void playYoutube(NMParams p);
#endif

TC_API void tumYP_play_scbii(NMParams p) {
    callback = p->obj[2];
#if defined(darwin)
    playYoutube(p);
#endif
#if defined(ANDROID)
    TCObject url = p->obj[1];
    int32 autoPlay = p->i32[0];
    int32 end = p->i32[1];
    int32 start = p->i32[2];
    JNIEnv* env = getJNIEnv();
    jstring jUrl = (*env)->NewString(env, (jchar*) String_charsStart(url), String_charsLen(url));
    (*env)->CallStaticObjectMethod(env, applicationClass, jplayYoutube, jUrl, autoPlay == 1 ? JNI_TRUE : JNI_FALSE, start, end);
#endif
}
#if defined(ANDROID)
void JNICALL Java_totalcross_Launcher4A_nativeYoutubeCallback(JNIEnv* env, jclass class, jint message) {
    if(callback == null) return;
    Method  onStateChange = getMethod(OBJ_CLASS(callback), false, "onStateChange", 1,
                                      J_INT);
    executeMethod(lifeContext, onStateChange, callback, message);
}
#endif

