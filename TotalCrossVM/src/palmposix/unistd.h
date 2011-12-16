/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#ifndef __UNISTD_H__
#define __UNISTD_H__

#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#endif

#include <stdio.h>

/*
 * Flag values for open(2) and fcntl(2)
 * The kernel adds 1 to the open modes to turn it into some
 * combination of FREAD and FWRITE.
 */
#define O_RDONLY        0               /* +1 == FREAD */
#define O_WRONLY        1               /* +1 == FWRITE */
#define O_RDWR          2               /* +1 == FREAD|FWRITE */
#define O_APPEND        0x0008
#define O_CREAT         0x0200
#define O_TRUNC         0x0400

typedef UInt32 ssize_t;

extern int     open     (const char *pathname, int flags, ...);
//extern int   open     (const char *pathname, int flags, mode_t mode);
extern ssize_t read     (int fd, void *buf, size_t nbytes);
extern ssize_t write    (int fd, const void *buf, size_t n);
extern int     close    (int fd);

#endif // __UNISTD_H__
