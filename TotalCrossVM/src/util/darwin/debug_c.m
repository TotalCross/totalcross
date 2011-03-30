#define Object NSObject*
#import <UIKit/UIKit.h>
#ifdef darwin9
#import <UIKit/UIAlert.h>
#else
#import <UIKit/UIAlertSheet.h>
#endif
#include "mainview.h"

bool allowMainThread();

#ifdef darwin9

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

#else

@interface AlertPopup : UITransitionView // todo@ may extend UIAlertSheet
{
}
- (void)popup:(id)message;
- (void)alertSheet:(UIAlertSheet *)sheet buttonClicked:(int)button;
@end

@implementation AlertPopup

- (void)popup:(id)message
{
   UIAlertSheet *sheet = [ [ UIAlertSheet alloc ] initWithFrame: CGRectMake(0, 240, 320, 240) ];
   [ sheet setTitle: @"ALERT" ];
   [ sheet setBodyText: message ];
   [ sheet setRunsModal: YES ];
   [ sheet addButtonWithTitle:@"Continue" ];
   [ sheet setDelegate: self ];
   [ sheet popupAlertAnimated: YES ];
}

- (void)alertSheet:(UIAlertSheet *)sheet buttonClicked:(int)button
{
   [ sheet dismiss ];
}

@end

#endif

void privateAlert(CharP cstr)
{
   AlertPopup *alert = [AlertPopup alloc];
   NSString *message =  [NSString stringWithCString: cstr encoding: NSISOLatin1StringEncoding];
   if ([NSThread isMainThread])
      [alert popup: message];
   else if (allowMainThread())
      [alert performSelectorOnMainThread:@selector(popup:) withObject: message waitUntilDone: YES];
}

