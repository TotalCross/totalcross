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

#define Object NSObject*
#import <UIKit/UIKit.h>
#import <UIKit/UIAlert.h>
#include "mainview.h"

bool allowMainThread();

@interface AlertPopup : UIAlertView
{
}
- (void)popup:(id)message;
@end

@implementation AlertPopup
bool showingAlert;

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
   showingAlert = false;
}
- (void)popup:(id)message
{
   [[ self initWithTitle:@"ALERT"
         message: message
         delegate: self
         cancelButtonTitle: @"Continue"
         otherButtonTitles: nil, nil]
      show 
   ];
}

@end

void privateAlert(CharP cstr)
{
   AlertPopup *alert = [AlertPopup alloc];
   NSString *message =  [NSString stringWithCString: cstr encoding: NSISOLatin1StringEncoding];
   if ([NSThread isMainThread])
      [alert popup: message];
   else
   if (allowMainThread())
   {
      showingAlert = true;
      [alert performSelectorOnMainThread:@selector(popup:) withObject: message waitUntilDone: YES];
      while (showingAlert) sleep(1);
   }
}

void iphoneDebug(CharP s)
{
    NSLog(@"%s",s);
}