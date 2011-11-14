#define Object NSObject*
#import "mainview.h"
#import "kbdview.h"
#import <UIKit/UIHardware.h>

#define IPAD_KEYBOARD_PORTRAIT      264
#define IPAD_KEYBOARD_LANDSCAPE     352
#define IPHONE_KEYBOARD_PORTRAIT    216
#define IPHONE_KEYBOARD_LANDSCAPE   162

#define NAVIGATION_BAR_HEIGHT       48

#define HIDE_SIP 1

@implementation KeyboardView

- (id)initWithFrame:(CGRect)rect params:(SipArguments*)args 
{
   self = [ super initWithFrame: rect ];

   int orientation = [ UIHardware deviceOrientation: YES ];
   bool landscape = (orientation == kOrientationHorizontalLeft || orientation == kOrientationHorizontalRight);

#ifndef darwin9
   int bar_size = [ UIHardware statusBarHeight ];
#endif

   CGRect viewFrame;
#if HIDE_SIP
   viewFrame = CGRectMake(-1, -1, 0, 0);
#else   
   if (landscape)
   {
#ifndef darwin9      
      float ofs = (bar_size > 0) ? 90 : 80;
      viewFrame = CGRectMake(-ofs, ofs, 480, 320 - bar_size);
#endif
      viewFrame = CGRectMake(80, 80, rect.size.width, 320);//rect.size.height, rect.size.width);
   }
   else      
      viewFrame = CGRectMake(0, rect.size.height - 264 - 80 - NAVIGATION_BAR_HEIGHT, rect.size.width, NAVIGATION_BAR_HEIGHT + 80);
#endif
   [ self setFrame: viewFrame ];

   DEBUG4("KBDVIEW: %dx%d,%dx%d\n", 
   			(int)viewFrame.origin.x, (int)viewFrame.origin.y, (int)viewFrame.size.width, (int)viewFrame.size.height);

   CGRect navFrame;
#if HIDE_SIP
   navFrame = CGRectMake(0, 0, 0, 0);
#else   
   if (landscape)
//      navFrame = CGRectMake(0, 0, 480, 48);
      navFrame = CGRectMake(viewFrame.origin.x, rect.size.width - 352 - 48, rect.size.height, 48);      
   else      
      navFrame = CGRectMake(0, 0, viewFrame.size.width, NAVIGATION_BAR_HEIGHT);
#endif
   
   rect = navFrame;
   if (navBar == nil)
   {
      navBar = [ [UINavigationBar alloc] initWithFrame: navFrame ];
      UINavigationItem *navItem = [ [ UINavigationItem alloc ] initWithTitle:@"text entry" ];
#ifdef darwin9
      [ navBar pushNavigationItem: navItem animated:true ];
      [ navItem release ];
      UIBarButtonItem *okButton = [ [ [ UIBarButtonItem alloc ]
            initWithTitle: @"OK"
            style: UIBarButtonItemStylePlain
            target: self
            action: @selector(onOk)
         ] autorelease ];
      navItem.leftBarButtonItem = okButton;
      UIBarButtonItem *cancelButton = [ [ [ UIBarButtonItem alloc ]
            initWithTitle: @"Cancel"
            style: UIBarButtonItemStylePlain
            target: self
            action: @selector(onCancel)
         ] autorelease ];
      navItem.rightBarButtonItem = cancelButton;
#else
      [ navBar pushNavigationItem: navItem ];
      [ navItem release ];
      [ navBar showLeftButton:@"OK" withStyle: 0 rightButton:@"Cancel" withStyle: 1 ];
      [ navBar enableAnimation ];
#endif      
      [ navBar setDelegate: self ];      
      [ self addSubview: navBar ];
   }
   else
      [ navBar setFrame: navFrame ];

#if 0
   // @TODO with entry label ?!   
   CGRect textFrame = rect;
   textFrame.origin.y = navFrame.origin.y + navFrame.size.height;
   textFrame.size.height = 24;
   UITextView *prompt = [ [ UITextView alloc ] initWithFrame: textFrame ];
   [ prompt setTextSize: 14 ];
   [ prompt setText: @"Your name" ];
   [ prompt setEditable: FALSE ];
   [ self addSubview: prompt ];
   [ prompt release ];

   CGRect entryFrame = rect;
   entryFrame.origin.y = textFrame.origin.y+textFrame.size.height;
#else
   CGRect entryFrame = rect;
   entryFrame.origin.y = navFrame.origin.y + navFrame.size.height;
#endif

#if 0 //def darwin9
   entryFrame.size.height = 0;
#else
   entryFrame.size.height = landscape ? 80 : 80;
#endif
      
   if (entry == nil)
   {
      entry = [ [ UITextView alloc ] initWithFrame: entryFrame ];
#ifdef darwin9
      entry.font = [ UIFont fontWithName: @"Arial" size: 18.0 ];
      entry.autocapitalizationType = UITextAutocapitalizationTypeWords;
      entry.autocorrectionType = UITextAutocorrectionTypeDefault;
      entry.returnKeyType = UIReturnKeyDone;
      entry.keyboardAppearance = UIKeyboardAppearanceAlert;
      entry.keyboardType = UIKeyboardTypeDefault;
      
	  /*
	    NOTE: the SDK below UIKeyboardType enum values end at 7, it appears that 8 enables an alphabet keyboard with a switch button
	    (world icon next to the spacebar) to switch among all enabled keyboards. Keyboards are enabled in "Settings/General/Keyboard".
	    
	  	typedef enum {
    		UIKeyboardTypeDefault,
    		UIKeyboardTypeASCIICapable,
    		UIKeyboardTypeNumbersAndPunctuation,
    		UIKeyboardTypeURL,
	    	UIKeyboardTypeNumberPad,
    		UIKeyboardTypePhonePad,
    		UIKeyboardTypeNamePhonePad,
    		UIKeyboardTypeEmailAddress,
	    	UIKeyboardTypeAlphabet = UIKeyboardTypeASCIICapable,
		} UIKeyboardType
	  */
#else
      [ entry setTextSize: 18 ];
      [ entry setAutoCapsType: 1 ];
      [ entry setAutoCorrectionType: 0 ];
      [ entry setPreferredKeyboardType: 0 ];
#endif      
      [ entry setDelegate: self ];
      [ entry becomeFirstResponder ];
      [ self addSubview: entry ];
   }
   else
      [ entry setFrame: entryFrame ];
   
   params = args;
   if (params != nil)
   {
      [ params retain ];
      [ entry setText: [params values].text ];
      bool secret = [params values].secret ? YES:NO;
      [ entry setSecureTextEntry: secret ];
#ifdef darwin9
      entry.secureTextEntry = TRUE;
#else
      [ entry setSecure: secret ];
#endif      
   }
   
#ifndef darwin9 // since kbd is displayed automatically
   CGRect kbdFrame = rect;
   kbdFrame.origin.y = entryFrame.origin.y+entryFrame.size.height;
   kbdFrame.size = [UIKeyboard defaultSizeForOrientation:(landscape ? 90 : 0)];

   if (kbd == nil)
   {
      kbd = [ [ UIKeyboard alloc ] initWithFrame: kbdFrame ];
      [ self addSubview: kbd ];
   }
   else
      [ kbd setFrame: kbdFrame ];
#endif

   struct CGAffineTransform transEnd = CGAffineTransformIdentity;
   if (orientation == kOrientationHorizontalLeft)
	  transEnd = CGAffineTransformMake(0,  1, -1, 0, 0, 0);
   else if (orientation == kOrientationHorizontalRight)
	  transEnd = CGAffineTransformMake(0, -1,  1, 0, 0, 0);

   [ self setTransform:transEnd];

   return self; 
}

- (void)navigationBar:(UINavigationBar *)navbar buttonClicked:(int)button
{
   switch(button) 
   {
      case 1:
         [ self onOk ];
         break;
      case 0:
         [ self onCancel ];
         break;
   }
}

- (void)dealloc
{
   [ params release ];
   [ super dealloc ];
}

- (void)onOk
{
#ifndef darwin9   
   if (params != nil)
	  iphone_postEditUpdate([ params values ].control, [ entry text ]);
#endif
   [ (MainView*)[ self superview ] destroySIP ];
}

- (void)onCancel
{
   [ (MainView*)[ self superview ] destroySIP ];
}

#ifdef darwin9
- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range 
replacementText:(NSString *)text
{
  // Any new character added is passed in as the "text" parameter
  if ([text isEqualToString:@"\n"]) {
      // Be sure to test for equality using the "isEqualToString" message
      [textView resignFirstResponder];
      [self onOk];

      // Return FALSE so that the final '\n' character doesn't get added
      return FALSE;
  }
  if ([text length] == 0 && params != nil)
  {
     [(MainView*)[self superview] addEvent: 
        [[NSDictionary alloc] initWithObjectsAndKeys:
           @"keyPress", @"type",
           [NSNumber numberWithInt:(int) '\b'], @"key",                 
         nil
        ]
     ];     
  }
  else if (lastRange.location > 0 && NSEqualRanges(range, lastRange)) //flsobral@tc126: avoid adding the same character twice when a single key press generates the same event twice.
     return TRUE;
  else
  {
     lastRange.location = range.location;
     lastRange.length = range.length;
     
     char* chars = [text cStringUsingEncoding: NSUnicodeStringEncoding];
     if (chars != null)
     {
        int charCode = chars[0] | (((int)chars[1]) << 8); //flsobral@tc126: characters are unicode
        debug("charcode: %d %d %X",(int)chars[0],(int)chars[1],charCode);
        [(MainView*)[self superview] addEvent: 
           [[NSDictionary alloc] initWithObjectsAndKeys:
              @"keyPress", @"type",
              [NSNumber numberWithInt: charCode], @"key",                 
            nil
           ]
        ];
     }
  }
  // For any other character return TRUE so that the text gets added to the view
  return TRUE;
}
#endif

@end
