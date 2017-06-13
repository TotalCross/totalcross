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
#if defined(ANDROID)
#warning "ANDROID DEFINIDO"
	fprintf(stderr, "%s %d\n", __FILE__, __LINE__);
	JNIEnv* env = getJNIEnv();

	jmethodID getToken = (*env)->GetStaticMethodID(env, jTcFirebaseUtils, "getTokenFromRegisteredFirebaseApp", "()Ljava/lang/String");
	jobject jtoken = (*env)->CallStaticObjectMethod(env, jTcFirebaseUtils, getToken);

	if (jtoken == null) {
		p->retO = null;
	} else {
		CharP sztoken = (*env)->GetStringUTFChars(env, jtoken, 0);
		TCObject tctoken = createStringObjectFromCharP(mainContext, tctoken, -1);

		p->retO = tctoken;
		setObjectLock(p->retO, UNLOCKED);

		(*env)->ReleaseStringUTFChars(env, jtoken, sztoken);
	}
#else
	p->retO = null;
#endif
}

