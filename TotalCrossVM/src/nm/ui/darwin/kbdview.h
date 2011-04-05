/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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
#ifndef darwin9 // since kbd is displayed automatically
   UIKeyboard *kbd;
#else
   NSRange lastRange;
#endif
}

- (id)initWithFrame:(CGRect)rect params:(SipArguments*)args;
- (void)navigationBar:(UINavigationBar *)navbar buttonClicked:(int)button;
- (void)dealloc;

#ifdef darwin9 //flsobral@tc126_59: now we support text edition directly in TotalCross controls!
- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range 
replacementText:(NSString *)text;
#endif
- (void)onOk;
- (void)onCancel;   

@end
