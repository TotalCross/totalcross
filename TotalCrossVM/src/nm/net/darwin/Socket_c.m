// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#import <CoreFoundation/CoreFoundation.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/nameser.h>   // NS_MAXDNAME
#include <netdb.h>          // getaddrinfo, struct addrinfo, AI_NUMERICHOST
#include <unistd.h>         // getopt

bool debug(const char *s, ...);


int iphoneSocket(char* hostname, struct sockaddr_in6 *in_addr)
{
   CFStringRef hostnameStr;
   CFHostRef host;
   CFStreamError error;
   Boolean success;
   CFArrayRef addresses;
   CFIndex index, count;
   struct sockaddr_in6 *addr;
   char             ipAddress[INET6_ADDRSTRLEN];
   int err;
   
   hostnameStr = CFStringCreateWithCString(NULL, hostname, kCFStringEncodingASCII);
   if (hostnameStr == NULL)
      return -2;
   [(id)hostnameStr autorelease];
   
   host = CFHostCreateWithName(NULL, hostnameStr);
   if (host == NULL)
      return -3;
   [(id)host autorelease];
   
   success = CFHostStartInfoResolution(host, kCFHostAddresses, &error);
   if (!success)
   {
      debug("error in CFHostStartInfoResolution: %d %d",(int)error.domain,(int)error.error);
      return -4;
   }
   
   addresses = CFHostGetAddressing(host, &success);
   if (!success)
      return -5;
   //[(id)addresses autorelease]; - this causes SAV to abort the thread after synchronizing the photos
   
   if (addresses != NULL)
   {
      count = CFArrayGetCount(addresses);
      for (index = 0; index < count; index++)
      {
         addr = (struct sockaddr_in6 *)CFDataGetBytePtr(CFArrayGetValueAtIndex(addresses, index));
         if (addr != NULL)
         {
            memcpy(in_addr, addr, sizeof(struct sockaddr_in6));
            /* getnameinfo converts an IPv4 or IPv6 address into a text string. */
            err = getnameinfo(addr, addr->sin6_len, ipAddress, INET6_ADDRSTRLEN, NULL, 0, NI_NUMERICHOST);
            if (err != 0) 
               debug("getnameinfo returned %d\n", err);
            //break;
         }
      }
   }
   return 0;
}
