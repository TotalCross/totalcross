// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#import <Foundation/Foundation.h>
#include "xtypes.h"

enum
{
   SIP_HIDE    = 10000, /** Used to hide the virtual keyboard */
   SIP_TOP     = 10001, /** Used to place the keyboard on top of screen */
   SIP_BOTTOM  = 10002, /** Used to place the keyboard on bottom of screen */
   SIP_SHOW    = 10003  /** Used to show the virtual keyboard, without changing the position */
};

struct SipArgs
{
   int options;
   __unsafe_unretained id control;
   bool numeric;
   __unsafe_unretained NSString *text;
};

typedef struct SipArgs SipArgs;

SipArgs SipArgsMake(int options, id control, bool secret, NSString *text);

@interface SipArguments : NSObject
{
   SipArgs v;
}

- (id)init:(SipArgs)args;
- (SipArgs)values;
- (void)dealloc;

@end
