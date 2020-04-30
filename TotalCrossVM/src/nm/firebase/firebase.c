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

#include "tcvm.h"

#include <stdio.h>

#if defined(darwin)
    void privateFirebaseGetToken(NMParams p);
#endif

TC_API void tfiFII_getToken(NMParams p)
{
#if defined(ANDROID)
	fprintf(stderr, "%s %d\n", __FILE__, __LINE__);
	JNIEnv* env = getJNIEnv();

	jmethodID getToken = (*env)->GetStaticMethodID(env, jTcFirebaseUtils, "getTokenFromRegisteredFirebaseApp", "()Ljava/lang/String;");
	jobject jtoken = (*env)->CallStaticObjectMethod(env, jTcFirebaseUtils, getToken);

	if (jtoken == null) {
		p->retO = null;
	} else {
		CharP sztoken = (*env)->GetStringUTFChars(env, jtoken, 0);
		TCObject tctoken = createStringObjectFromCharP(mainContext, sztoken, -1);

		p->retO = tctoken;
		setObjectLock(p->retO, UNLOCKED);

		(*env)->ReleaseStringUTFChars(env, jtoken, sztoken);
	}
#elif defined (darwin)
    privateFirebaseGetToken(p);
#else
	p->retO = null;
#endif
}

#if defined(ANDROID)
void JNICALL Java_totalcross_Launcher4A_nativeOnTokenRefresh(JNIEnv *env, jclass _class) {
	TCClass c = loadClass(mainContext,"totalcross.firebase.FirebaseManager",true);
	if(mainContext -> thrownException) return;

	Method getInstance = getMethod(c, false, "getInstance", 0);
	if(getInstance == null) debug("getInstance is null");
	
	TCObject fmInstance = executeMethod(mainContext, getInstance).asObj;
	
	if(fmInstance != null) {
		Method onTknRefresh = getMethod(OBJ_CLASS(fmInstance), false, "onTokenRefresh", 0);
		if(onTknRefresh != null) {
			executeMethod(mainContext, onTknRefresh, fmInstance);
		}
	}
}


void JNICALL Java_totalcross_Launcher4A_nativeOnMessageReceived(JNIEnv *env, jclass _class, jstring messageId, jstring messageType,
 jobjectArray keys, jobjectArray values, jstring collapsedKey, jint ttl) {
	
	TCObject keysArray = null;
	TCObject valuesArray = null;

	int32 size = 0, i;

	if(keys != null) {
		size = (*env)->GetArrayLength(env, keys); 
		keysArray = createArrayObject(mainContext,"[java.lang.String", size);
		valuesArray = createArrayObject(mainContext,"[java.lang.String", size);
		
		for (i = 0; i < size; ++i)
		{
	  		CharP key = (*env)->GetStringUTFChars(env, (jstring) (*env)->GetObjectArrayElement(env, keys, i), 0);
	  		CharP value = (*env)->GetStringUTFChars(env, (jstring) (*env)->GetObjectArrayElement(env, values, i), 0);
	  		*((TCObjectArray) ARRAYOBJ_START(keysArray) + i) = createStringObjectFromCharP(mainContext, key, -1);
	  		*((TCObjectArray) ARRAYOBJ_START(valuesArray) + i) = createStringObjectFromCharP(mainContext, value, -1);  
	  	}
	}
	
	TCClass c = loadClass(mainContext,"totalcross.firebase.FirebaseManager",true);
	if(mainContext -> thrownException) return;

	Method getInstance = getMethod(c, false, "getInstance", 0);
	if(getInstance == null) debug("getInstance is null");
	
	TCObject fmInstance = executeMethod(mainContext, getInstance).asObj;
	
	if(fmInstance != null) {
		Method onMsgRcvd = getMethod(OBJ_CLASS(fmInstance), false, "onMessageReceived", 6, 
			"java.lang.String", "java.lang.String", "[java.lang.String", "[java.lang.String", "java.lang.String",
			J_INT);
		if(onMsgRcvd != null) {
			TCObject messageIdObj = null;
			if (messageId != null) {
				messageIdObj = createStringObjectFromCharP(mainContext, (*env)->GetStringUTFChars(env, messageId, 0), -1);
			}
			TCObject messageTypeObj = null;
			if (messageType != null) {
				messageTypeObj = createStringObjectFromCharP(mainContext, (*env)->GetStringUTFChars(env, messageType, 0), -1);
			}
			TCObject collapsedKeyObj = null;
			if (collapsedKey != null) {
				collapsedKeyObj = createStringObjectFromCharP(mainContext, (*env)->GetStringUTFChars(env, collapsedKey, 0), -1);
			}
			executeMethod(mainContext, onMsgRcvd, fmInstance, 
				messageIdObj,
				messageTypeObj,
				keysArray, valuesArray,
				collapsedKeyObj,
				 ttl 
			);
				
			setObjectLock(messageIdObj, UNLOCKED);
			setObjectLock(messageTypeObj, UNLOCKED);
			setObjectLock(collapsedKeyObj, UNLOCKED);
			for (i = 0; i < size; ++i)
			{
				setObjectLock(*((TCObjectArray) ARRAYOBJ_START(keysArray) + i), UNLOCKED);
				setObjectLock(*((TCObjectArray) ARRAYOBJ_START(valuesArray) + i), UNLOCKED);
			}
		}
	}
}
#endif

