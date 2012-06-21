#define Object NSObject*
#import "mainview.h"
#import "kbdview.h"

@implementation KeyboardView

- (id)init:(UIViewController *)controller
{
   self = [ super init ];
   ctrl = controller;
   entry = [ [ UITextView alloc ] init ];
   entry.font = [ UIFont fontWithName: @"Arial" size: 18.0 ];
   entry.autocapitalizationType = UITextAutocapitalizationTypeWords;
   entry.autocorrectionType = UITextAutocorrectionTypeDefault;
   entry.returnKeyType = UIReturnKeyDone;
   entry.keyboardAppearance = UIKeyboardAppearanceAlert;
   entry.keyboardType = UIKeyboardTypeDefault;
   [ entry setDelegate: self ];
   [ entry becomeFirstResponder ];
   [ self addSubview: entry ];
   return self; 
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range 
replacementText:(NSString *)text
{
  // Any new character added is passed in as the "text" parameter
  if ([text isEqualToString:@"\n"]) // Be sure to test for equality using the "isEqualToString" message
  {
      [textView resignFirstResponder];
      [ (MainView*)ctrl destroySIP ];
      return FALSE; // Return FALSE so that the final '\n' character doesn't get added
  }
  if ([text length] == 0)
  {
     [(MainView*)ctrl addEvent: 
        [[NSDictionary alloc] initWithObjectsAndKeys: @"keyPress", @"type", [NSNumber numberWithInt:(int) '\b'], @"key", nil]
     ];     
  }
  else if (lastRange.location > 0 && NSEqualRanges(range, lastRange)) //flsobral@tc126: avoid adding the same character twice when a single key press generates the same event twice.
     return TRUE;
  else
  {
     lastRange.location = range.location;
     lastRange.length = range.length;
     
     unsigned char* chars = (unsigned char*)[text cStringUsingEncoding: NSUnicodeStringEncoding];
     if (chars != null)
     {
        int charCode = chars[0] | (chars[1] << 8); //flsobral@tc126: characters are unicode
        [(MainView*)ctrl addEvent: 
           [[NSDictionary alloc] initWithObjectsAndKeys: @"keyPress", @"type", [NSNumber numberWithInt: charCode], @"key", nil]
        ];
     }
  }
  // For any other character return TRUE so that the text gets added to the view
  return TRUE;
}

@end
