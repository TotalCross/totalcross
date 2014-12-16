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
   NSString *args = [NSString stringWithFormat:@"%s", szArgs];
   if (strEq(szCommand,"viewer"))
   {
      dispatch_sync(dispatch_get_main_queue(), ^
      {
         UIWebView *webView = DEVICE_CTX->_mainview->webView =[[UIWebView alloc] initWithFrame:[DEVICE_CTX->_childview bounds]];
         [webView setContentMode:UIViewContentModeScaleAspectFit];
         [webView setAutoresizingMask:UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth];
         [webView setScalesPageToFit:YES];
         DEVICE_CTX->_mainview.view = webView;
         UIButton *button = [UIButton buttonWithType:UIButtonTypeRoundedRect];
         [button addTarget:DEVICE_CTX->_mainview action:@selector(closeWebView:) forControlEvents:UIControlEventTouchUpInside];
         [button setTitle:@" X " forState:UIControlStateNormal];
         int h = DEVICE_CTX->_childview->taskbarHeight;
         button.frame = CGRectMake(0,0,h,h);
         [webView addSubview:button];
         [webView loadRequest: [NSURLRequest requestWithURL:[NSURL fileURLWithPath:args]]];
         [webView release];
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
