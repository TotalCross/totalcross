/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/

#import <CoreFoundation/CoreFoundation.h>
#include <sys/socket.h>
#include <netinet/in.h>

int iphoneSocket()
{
   CFSocketRef socketRef = CFSocketCreate(
                              NULL,       // default allocator
                              PF_INET,
                              SOCK_STREAM,
                              IPPROTO_TCP,
                              0,          // kCFSocketNoCallBack
                              NULL,       // no callback function
                              NULL);         // CFSocketContext
   
   if (socketRef == NULL)
      return -1;
   
   int handle = CFSocketGetNative(socketRef);
   CFRelease(socketRef);
   
   return handle;
}

bool allowMainThread();

void windowSetSIP(Context currentContext, int32 sipOption, Object control, bool secret)
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
   SipArguments *args = [ [ SipArguments alloc ] init: SipArgsMake(sipOption, (id)control, secret, str) ];
   [ args retain ];

   if (DEVICE_CTX && DEVICE_CTX->_mainview)
   {
      DEBUG2("view=%x str value: %x\n", DEVICE_CTX->_mainview, str);
      if (allowMainThread)
         [ DEVICE_CTX->_mainview  performSelectorOnMainThread:@selector(showSIP:) withObject:args waitUntilDone: YES ];
   }
}

void setEditText(Context currentContext, Object control, NSString *str)
{
   if (control)
   {
      int len = [ str length ];
      unichar *data = xmalloc(len * sizeof(unichar));
      [ str getCharacters: data ];
      if (control && OBJ_CLASS(control))
      {
         Object msgObj = createStringObjectFromJCharP(currentContext, data, len);
         Method m = getMethod(OBJ_CLASS(control), true, "setText", 1, "java.lang.String");
         if (!m) return; // guich@tc115_15
         executeMethod(currentContext, m, control, msgObj);
         setObjectLock(msgObj, UNLOCKED);
      }
      xfree(data);
   }
}
