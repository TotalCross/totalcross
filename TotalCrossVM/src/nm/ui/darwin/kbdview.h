/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#import <UIKit/UIKit.h>
#import <UIKit/UITextView.h>
#import "sipargs.h"

@interface KeyboardView : UIView
{
   UINavigationBar *navBar;
   UITextView *entry;
   SipArguments *params;
   NSRange lastRange;
@public
   UIViewController* ctrl;
}

- (id)initWithFrame:(CGRect)rect params:(SipArguments*)args ;
- (void)navigationBar:(UINavigationBar *)navbar buttonClicked:(int)button;

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range 
                 replacementText:(NSString *)text;
- (void)onOk;
- (void)onCancel;   

@end
