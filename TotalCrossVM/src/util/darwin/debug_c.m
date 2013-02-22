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
   else if (allowMainThread())
      [alert performSelectorOnMainThread:@selector(popup:) withObject: message waitUntilDone: YES];
}

void iphoneDebug(CharP s)
{
    NSLog(@"%s",s);
}