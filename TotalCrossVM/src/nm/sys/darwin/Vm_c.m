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
#import <AudioToolbox/AudioToolbox.h>

#define Class __Class
#include "tcvm.h"
#undef Class


int32 vmExec(TCHARP szCommand, TCHARP szArgs, int32 launchCode, bool wait)
{
   if (strEq(szCommand,"viewer")/* && xstrstr(szArgs,".pdf") != 0*/)
   {
      if (!DEVICE_CTX->_childview.uidController)
      {
         DEVICE_CTX->_childview.uidController = [[UIDocumentInteractionController alloc] init];
         //DEVICE_CTX->_childview.uidController.UTI = @"com.adobe.pdf";
      }
      NSString* strl = [NSString stringWithFormat:@"%s", szArgs];
      NSURL *url = [NSURL URLWithString:strl];
      DEVICE_CTX->_childview.uidController = [UIDocumentInteractionController interactionControllerWithURL:[NSURL fileURLWithPath:url]];
      CGRect navRect = DEVICE_CTX->_childview.frame;
      [DEVICE_CTX->_childview.uidController presentOpenInMenuFromRect:navRect inView:[UIApplication sharedApplication].keyWindow animated:YES];
   }
   else
   if (strEq(szCommand,"url"))
   {
      NSString* launchUrl = [NSString stringWithFormat:@"%s", szArgs];
      [[UIApplication sharedApplication] openURL:[NSURL URLWithString: launchUrl]];   
   }
   if (!wait)
      keepRunning = false;      
}

void vmVibrate(int32 ms)
{
   AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
}
