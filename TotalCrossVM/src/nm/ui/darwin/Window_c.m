/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


#import <UIKit/UIKit.h>

#define Class __Class
#include "xtypes.h"
#include "tcvm.h"
#include "Window_c.h"
#include "sipargs.h"
#undef Class
#include "mainview.h"
#include "gfx_ex.h"

bool allowMainThread();

void windowSetSIP(Context currentContext, int32 sipOption, TCObject control, bool secret)
{
   NSString *str = nil;
   if (control)
   {               
     Method m = getMethod(OBJ_CLASS(control), true, "getText", 0);
     if (!m) return; // guich@tc115_15: the ideal would be to "paste" the text in the current event queue instead of assuming this is an Edit or MultiEdit.
	  assert(sizeof(unichar) == sizeof(JChar));
     TValue ret = executeMethod(currentContext, m, control);
	  str = [ [ NSString alloc ] initWithCharacters: String_charsStart(ret.asObj) length: String_charsLen(ret.asObj) ];
   }
   SipArguments *args = [ [ SipArguments alloc ] init: SipArgsMake(sipOption, (__bridge id)control, secret, str) ];
   if (DEVICE_CTX && DEVICE_CTX->_mainview && allowMainThread)
      [ DEVICE_CTX->_mainview  performSelectorOnMainThread:@selector(showSIP:) withObject:args waitUntilDone: YES ];
   if (str) [str release];
   [args release];
}

void setEditText(Context currentContext, TCObject control, NSString *str)
{
   if (control)
   {
      int len = (int)[ str length ];
      unichar *data = (unichar*)xmalloc(len * sizeof(unichar));
      [ str getCharacters: data ];
      if (control && OBJ_CLASS(control))
      {
         TCObject msgObj = createStringObjectFromJCharP(currentContext, data, len);
         Method m = getMethod(OBJ_CLASS(control), true, "setText", 1, "java.lang.String");
         if (!m) return; // guich@tc115_15
         executeMethod(currentContext, m, control, msgObj);
         setObjectLock(msgObj, UNLOCKED);
      }
      xfree(data);
   }
}
