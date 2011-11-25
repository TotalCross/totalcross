#define Object NSObject*
#import <UIKit/UIKit.h>
#import <UIKit/UIAlert.h>
#include "mainview.h"

bool allowMainThread();

@interface AlertPopup : UIAlertView
{
}
- (void)popup:(id)message;
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;
@end

@implementation AlertPopup

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
   [ alertView release ];
}

- (void)popup:(id)message
{
   [[ self initWithTitle:@"ALERT"
         message: message
         delegate: self
         cancelButtonTitle: nil
         otherButtonTitles: @"Continue", nil]
      show 
   ];
}

@end

void privateAlert(CharP cstr)
{
/*   AlertPopup *alert = [AlertPopup alloc];
   NSString *message =  [NSString stringWithCString: cstr encoding: NSISOLatin1StringEncoding];
   if ([NSThread isMainThread])
      [alert popup: message];
   else if (allowMainThread())
      [alert performSelectorOnMainThread:@selector(popup:) withObject: message waitUntilDone: YES];*/
}

