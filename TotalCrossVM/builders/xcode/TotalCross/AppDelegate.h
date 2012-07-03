//
//  AppDelegate.h
//  TotalCross
//
//  Created by Guilherme Hazan on 3/7/12.
//  Copyright (c) 2012 SuperWaba Ltda. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef id Context;

typedef int  (*StartVMProc)             (char* args, Context context);
typedef void (*NotifyStopVMProc)        ();
typedef int  (*StartProgramProc)        (Context context);
typedef void (*OrientationChangedProc)  ();

typedef void *dlHandle;

@class ViewController;

@interface AppDelegate : UIResponder <UIApplicationDelegate>
{
    int startupRC;
    StartProgramProc fStartProgram;
    OrientationChangedProc fOrientationChanged;
    dlHandle tcvm;
    Context context;
}
- (void) initApp;
- (void)   mainLoop: (id)param;
- (float)  systemVolume;
- (void) fatalError: (NSString*)msg;

@end
