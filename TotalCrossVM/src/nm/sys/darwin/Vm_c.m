// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#import <UIKit/UIKit.h>
#import <WebKit/WebKit.h>
#import <AudioToolbox/AudioToolbox.h>
@import SafariServices;
#include "tcvm.h"
#include "../../nm/ui/darwin/mainview.h"
#define Class __Class
#include "GraphicsPrimitives.h"
#undef Class


int32 vmExec(TCHARP szCommand, TCHARP szArgs, int32 launchCode, bool wait)
{
   bool ret = false;
   NSString *args = [NSString stringWithFormat:@"%s", szArgs];
    if (strEq(szCommand,"webview"))
    {
        dispatch_sync(dispatch_get_main_queue(), ^
                      {
                          SFSafariViewController *svc = [[SFSafariViewController alloc] initWithURL:[NSURL URLWithString: args]];
                          [DEVICE_CTX->_mainview presentViewController:svc animated:YES completion:nil];
                      });
    }
   else if (strEq(szCommand,"viewer"))
   {
      dispatch_sync(dispatch_get_main_queue(), ^
      {
          UIDocumentInteractionController *dc = [UIDocumentInteractionController interactionControllerWithURL:[NSURL fileURLWithPath:args]];
          dc.delegate = DEVICE_CTX->_mainview;
          [dc presentPreviewAnimated:YES];
      });
   }
   else
   if (strEq(szCommand,"url"))
      ret = [[UIApplication sharedApplication] openURL:[NSURL URLWithString: args]];
   if (!wait)
      keepRunning = false;
    return ret ? 0 : 1;
}

void vmVibrate(int32 ms)
{
   AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
}
