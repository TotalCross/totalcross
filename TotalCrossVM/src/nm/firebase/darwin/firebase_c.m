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
#include "../firebase.h"
@import Firebase;

void privateFirebaseGetToken(NMParams p)
{
	NSString *fcmToken = [FIRMessaging messaging].FCMToken;
	if (fcmToken != null)
	{
		const char* token = [fcmToken cStringUsingEncoding:NSASCIIStringEncoding];
		p->retO = createStringObjectFromCharP(p->currentContext, token, xstrlen(token));
		setObjectLock(p->retO, UNLOCKED);
	}
}

@implementation FirebaseTCCallback

+ (void) privateOnTokenRefresh {
    TCClass c = loadClass(mainContext,"totalcross.firebase.FirebaseManager",true);
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

+ (void) privateOnMessageReceived: (NSString *)messageId messageType:(NSString *)messageType keys:(NSArray *) keys values:(NSArray *)values collapsedKey:(NSString *) collapsedKey ttl:(int)ttl {
    
    TCObject keysArray = null;
    TCObject valuesArray = null;
    
    int32 size = 0, i;
    
    if(keys != null) {
        size = [keys count];
        keysArray = createArrayObject(mainContext,"[java.lang.String", size);
        valuesArray = createArrayObject(mainContext,"[java.lang.String", size);
        
        for (i = 0; i < size; ++i)
        {
            const char* key = [keys[i] cStringUsingEncoding:NSASCIIStringEncoding];
            const char* value = [values[i] cStringUsingEncoding:NSASCIIStringEncoding];
            *((TCObjectArray) ARRAYOBJ_START(keysArray) + i) = createStringObjectFromCharP(mainContext, key, -1);
            *((TCObjectArray) ARRAYOBJ_START(valuesArray) + i) = createStringObjectFromCharP(mainContext, value, -1);
        }
    }
    
    TCClass c = loadClass(mainContext,"totalcross.firebase.FirebaseManager",true);
    
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
                messageIdObj = createStringObjectFromCharP(mainContext,
                                    [messageId cStringUsingEncoding:NSASCIIStringEncoding], sizeof(messageId));
            }
            TCObject messageTypeObj = null;
            if (messageType != null) {
                messageTypeObj = createStringObjectFromCharP(mainContext,
                                                             [messageType cStringUsingEncoding:NSASCIIStringEncoding], sizeof(messageType));
            }
            TCObject collapsedKeyObj = null;
            if (collapsedKey != null) {
                collapsedKeyObj = createStringObjectFromCharP(mainContext,
                                                              [collapsedKey cStringUsingEncoding:NSASCIIStringEncoding], sizeof(collapsedKey));
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


@end
