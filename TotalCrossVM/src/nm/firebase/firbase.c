/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2017 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

#include "tcvm.h"

#include <stdio.h>

TC_API void tfiFII_getToken(NMParams p)
{
#ifdef defined(ANDROID)
#error "ERRO DESEJADO"
	fprintf(stderr, "%s %d\n", __FILE__, __LINE__);
	JNIEnv* env = getJNIEnv();

	jmethodID getInstance = (*env)->GetStaticMethodID(env, jFirebaseInstanceId, "getInstance", "()Lcom/google/firebase/iid/FirebaseInstanceId");
	jmethodID getToken = (*env)->GetMethodID(env, jFirebaseInstanceId, "getToken", "()Ljava/lang/String");
	jobject firebase_instance = (*env)->CallStaticObjectMethod(env, jFirebaseInstanceId, getInstance);
	jobject jtoken = (*env)->CallObjectMethod(firebase_instance, getToken);

	if (jtoken == null) {
		p->retO = null;
	} else {
		CharP sztoken = (*env)->GetStringUTFChars(env, jtoken, 0);
		TCObject tctoken = createStringObjectFromCharP(mainContext, tctoken, -1);

		p->retO = tctoken;
		setObjectLock(p->retO, UNLOCKED);

		(*env)->ReleaseStringUTFChars(env, jtoken, szDisplayOriginatingAddress);
	}
#else
#error "ERRO NAO DEFINIDO ANDROID"
	p->retO = null;
#endif
}

