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
