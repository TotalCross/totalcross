// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// SPDX-License-Identifier: LGPL-2.1-only

#import <UIKit/UIKit.h>

typedef id Context;
typedef void *dlHandle;

@class ViewController;

@interface AppDelegate : UIResponder <UIApplicationDelegate>
{
    int startupRC;
    dlHandle tcvm;
    Context context;
}
- (void) initApp;
- (void) mainLoop: (id)param;

@end
