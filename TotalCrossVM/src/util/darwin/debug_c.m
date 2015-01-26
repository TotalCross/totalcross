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