// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// SPDX-License-Identifier: LGPL-2.1-only

#import <Foundation/Foundation.h>
#include <tcvm.h>
void getDefaultToString(NMParams p) {
    NSString *localeDefault = [[NSLocale currentLocale] localeIdentifier];
    const char* locale = [localeDefault cStringUsingEncoding:NSASCIIStringEncoding];
    p->retO = createStringObjectFromCharP(p->currentContext, locale, xstrlen(locale));
    setObjectLock(p->retO, UNLOCKED);
}
