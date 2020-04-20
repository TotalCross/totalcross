/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
 *                                                                               *
 *********************************************************************************/

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