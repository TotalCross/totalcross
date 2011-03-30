/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: in.h,v 1.6 2011-01-04 13:31:15 guich Exp $

#ifndef __IN_H__
#define __IN_H__

/* see http://www.netrino.com/Publications/Glossary/Endianness.php for more details */

#if  defined(__i386__) || defined(__ia64__) || defined(WIN32) || \
    (defined(__alpha__) || defined(__alpha)) || \
     defined(__arm__) || \
    (defined(__mips__) && defined(__MIPSEL__)) || \
     defined(__SYMBIAN32__) || \
     defined(__x86_64__) || \
     defined(__LITTLE_ENDIAN__)
 #define __bswap_16(n) (((((unsigned int) n) << 8) & 0xFF00) | \
                        ((((unsigned int) n) >> 8) & 0x00FF))

 #define __bswap_32(n) (((((unsigned long) n) << 24) & 0xFF000000) | \
                        ((((unsigned long) n) <<  8) & 0x00FF0000) | \
                        ((((unsigned long) n) >>  8) & 0x0000FF00) | \
                        ((((unsigned long) n) >> 24) & 0x000000FF))

 #define ntohl(x) __bswap_32 (x)
 #define ntohs(x) __bswap_16 (x)
 #define htonl(x) __bswap_32 (x)
 #define htons(x) __bswap_16 (x)
#else
 /* The host byte order is the same as the network byte order (BIG ENDIAN),
  * so these functions are all just identity.  */
 #define ntohl(x) (x)
 #define ntohs(x) (x)
 #define htonl(x) (x)
 #define htons(x) (x)
#endif

#endif
