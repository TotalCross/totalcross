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

#include "tcvm.h"
#include "../../nm/ui/darwin/mainview.h"
#define Class __Class
#include "GraphicsPrimitives.h"
#undef Class


int32 vmExec(TCHARP szCommand, TCHARP szArgs, int32 launchCode, bool wait)
{
    bool ret = false;
    // does not work: seems that the ui is gone, because it misses the first click, but nothing is displayed
   if (strEq(szCommand,"viewer")/* && xstrstr(szArgs,".pdf") != 0*/)
   {
      if (DEVICE_CTX->_childview->uidController)
          [DEVICE_CTX->_childview->uidController release];
      DEVICE_CTX->_childview->uidController = [[UIDocumentInteractionController alloc] init];
      //DEVICE_CTX->_childview->uidController.UTI = @"com.adobe.pdf";
      DEVICE_CTX->_childview->uidController = [UIDocumentInteractionController interactionControllerWithURL:[NSURL fileURLWithPath:[NSString stringWithFormat:@"%s", szArgs]]];
      if (![DEVICE_CTX->_childview->uidController presentOpenInMenuFromRect:DEVICE_CTX->_childview.frame inView:[UIApplication sharedApplication].keyWindow animated:YES])
          ret = [[UIApplication sharedApplication] openURL:[NSURL fileURLWithPath:[NSString stringWithFormat:@"%s", szArgs]]];
   }
   else
   if (strEq(szCommand,"url"))
      ret = [[UIApplication sharedApplication] openURL:[NSURL URLWithString: [NSString stringWithFormat:@"%s", szArgs]]];
   if (!wait)
      keepRunning = false;
    return ret ? 0 : 1;
}

void vmVibrate(int32 ms)
{
   AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
}
