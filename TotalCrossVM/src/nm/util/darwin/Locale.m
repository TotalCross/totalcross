//
//  Locale.m
//  tcvm
//
//  Created by Italo on 09/11/18.
//  Copyright © 2018 SuperWaba Ltda. All rights reserved.
//

#import <Foundation/Foundation.h>
#include <tcvm.h>
void getDefaultToString(NMParams p) {
    NSString *localeDefault = [[NSLocale currentLocale] localeIdentifier];
    const char* locale = [localeDefault cStringUsingEncoding:NSASCIIStringEncoding];
    p->retO = createStringObjectFromCharP(p->currentContext, locale, xstrlen(locale));
    setObjectLock(p->retO, UNLOCKED);
}
