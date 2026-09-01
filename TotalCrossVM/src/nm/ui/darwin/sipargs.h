// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only



#import <Foundation/Foundation.h>
#include "xtypes.h"
#include "../WindowSIP.h"

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
