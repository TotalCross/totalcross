//
//  AppDelegate.h
//  TotalCross
//
//  Created by Guilherme Hazan on 3/7/12.
//  Copyright (c) 2012 SuperWaba Ltda. All rights reserved.
//

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
