/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/

#include "tcvm.h"

@import Firebase;

void privateFirebaseGetToken(NMParams p)
{
	NSString *fcmToken = [FIRMessaging messaging].FCMToken;
	const char* token = [fcmToken cStringUsingEncoding:NSASCIIStringEncoding];
	p->retO = createStringObjectFromCharP(p->currentContext, token, xstrlen(token));
	setObjectLock(p->retO, UNLOCKED);
} 
