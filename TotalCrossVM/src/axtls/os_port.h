/*
 * Copyright (c) 2007-2016, Cameron Rich
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * * Neither the name of the axTLS project nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @file os_port.h
 *
 * Some stuff to minimise the differences between windows and linux/unix
 */

#ifndef HEADER_OS_PORT_H
#define HEADER_OS_PORT_H

#ifdef __cplusplus
extern "C" {
#endif

#include "os_int.h"
#include "axssl_config.h"
#include <stdio.h>
#define TC_privateXfree                privateXfree
#define TC_privateXmalloc              privateXmalloc
#define TC_privateXrealloc             privateXrealloc
#define TC_privateXcalloc              privateXcalloc
#include "tcvm.h"  //flsobral@tc114_36: including tcvm.h to be able to use xmalloc and xfree.

#if defined(WIN32)
#define STDCALL                 __stdcall
#define EXP_FUNC                __declspec(dllexport)
#else
#define STDCALL
#define EXP_FUNC
#endif

#if defined(_WIN32_WCE)
#undef WIN32
#define WIN32

struct tm
{
  int   tm_sec;
  int   tm_min;
  int   tm_hour;
  int   tm_mday;
  int   tm_mon;
  int   tm_year;
  int   tm_wday;
  int   tm_yday;
  int   tm_isdst;
  int   tm_gmtoff;
};

time_t time( time_t *timer );
time_t mktime (struct tm *tp);
int _isatty(int fd);

#endif

#ifdef WIN32

/* Windows CE stuff */
#if defined(_WIN32_WCE)
#include <basetsd.h>
#define abort() tcabort("AXTLS",__FILE__,__LINE__) // TOTALCROSS
#else
#include <io.h>
#include <process.h>
#include <sys/timeb.h>
#include <fcntl.h>
#endif      /* _WIN32_WCE */

#include "winsockLib.h"
#if !defined(_WIN32_WCE)
#include <direct.h>
#endif
#undef getpid
#undef open
#undef close
#undef sleep
#undef gettimeofday
#undef dup2
#undef unlink

#define SOCKET_READ(A,B,C)      recv(A,B,C,0)
#define SOCKET_WRITE(A,B,C)     send(A,B,C,0)
#define SOCKET_CLOSE(A)         closesocket(A)
#define srandom(A)              srand(A)
#define random()                rand()
#define getpid()                _getpid()
#define snprintf                _snprintf
#define open(A,B)               _open(A,B)
#define dup2(A,B)               _dup2(A,B)
#define unlink(A)               _unlink(A)
#define close(A)                _close(A)
#define read(A,B,C)             _read(A,B,C)
#define write(A,B,C)            _write(A,B,C)
#define sleep(A)                Sleep(A*1000)
#define usleep(A)               Sleep(A/1000)
#define strdup(A)               _strdup(A)
#define chroot(A)               _chdir(A)
#define chdir(A)                _chdir(A)
#ifndef alloca
#define alloca(A)               _alloca(A)
#endif
#ifndef lseek
#define lseek(A,B,C)            _lseek(A,B,C)
#endif

/* This fix gets around a problem where a win32 application on a cygwin xterm
   doesn't display regular output (until a certain buffer limit) - but it works
   fine under a normal DOS window. This is a hack to get around the issue - 
   see http://www.khngai.com/emacs/tty.php  */
#define TTY_FLUSH()             if (!_isatty(_fileno(stdout))) fflush(stdout);

/*
 * automatically build some library dependencies.
 */
#if defined(WINCE)
#pragma comment(lib, "winsock.lib")
#else
#pragma comment(lib, "WS2_32.lib")
#endif

#ifdef CONFIG_WIN32_USE_CRYPTO_LIB
#pragma comment(lib, "AdvAPI32.lib")
#endif

typedef int socklen_t;

EXP_FUNC void STDCALL gettimeofday(struct timeval* t,void* timezone);
#ifndef __clang__
EXP_FUNC int STDCALL strcasecmp(const char *s1, const char *s2);
EXP_FUNC int STDCALL getdomainname(char *buf, int buf_size);
#endif

#ifdef _WIN32_WCE
EXP_FUNC size_t strnlen(const char *str, size_t max);
#endif

#else   /* Not Win32 */

#include <unistd.h>
#include <pwd.h>
#include <netdb.h>
#include <dirent.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#ifndef __APPLE__
#include <asm/byteorder.h>
#endif

#define SOCKET_READ(A,B,C)      read(A,B,C)
#define SOCKET_WRITE(A,B,C)     write(A,B,C)
#define SOCKET_CLOSE(A)         if (A >= 0) close(A)
#define TTY_FLUSH()

#endif  /* Not Win32 */

#ifndef be64toh
 #if defined __APPLE__ || BYTE_ORDER == LITTLE_ENDIAN
  #define be64toh(x) ntohll(x)
 #elif BYTE_ORDER == BIG_ENDIAN
  /* that would be xbox 360 */
  #define be64toh(x) (x)
 #else
  #define be64toh(x) __be64_to_cpu(x)
 #endif
#endif

//flsobral@tc115: Removed ifdef for PalmOS, it's the same code for all platforms from now on. (seems to fix the problem with corrupted VMs)
#if defined (TOTALCROSS_INTEGRATION) //flsobral@tc114_36: we'll use tcvm functions instead.
 #undef free
 #undef malloc
 #undef calloc
 #undef realloc

 #define free(A)         xfree(A)
 #define malloc(A)       xmalloc(A)
 #define calloc(A,B)     xcalloc(A,B)
 #define realloc(A, B)   xrealloc(A, B)

 extern int debug(const char *s, ...);
 #define printf debug


 #undef exit
 #define exit(A)                 TCABORT

extern int tcSocketReadWrite(int fd, char *buf, int count, int isRead);

#undef SOCKET_READ
#undef SOCKET_WRITE
#undef SOCKET_CLOSE
#undef TTY_FLUSH

#define SOCKET_READ(A,B,C)      tcSocketReadWrite(A,B,C,1)
#define SOCKET_WRITE(A,B,C)     tcSocketReadWrite(A,B,C,0)
#define SOCKET_CLOSE(A)         if (A >= 0) close(A)
#define TTY_FLUSH()

#else // previous code

/* some functions to mutate the way these work */
 #define malloc(A)       ax_malloc(A)
 #ifndef realloc
  #define realloc(A,B)    ax_realloc(A,B)
 #endif
 #define calloc(A,B)     ax_calloc(A,B)

 EXP_FUNC void * STDCALL ax_malloc(size_t s);
 EXP_FUNC void * STDCALL ax_realloc(void *y, size_t s);
 EXP_FUNC void * STDCALL ax_calloc(size_t n, size_t s);
#endif

// private time type - fdie@20090325 support certificate expiration dates beyond 2050
typedef struct
{
   uint32_t high; // approx number of hours since year 0 (non decreasing function useful for date comparison)
   uint32_t low;  // number of microsecs in the current hour
} time_h;

#define mk_time_h(pth, year, month, day, hour, min, sec, usec) \
{ \
   (pth)->high = (((year)*12+(month))*31+(day))*24+(hour); \
   (pth)->low  = ((min)*60+(sec))*1000000+(usec); \
}
#define isBefore(t1, t2) ((t1)->high < (t2)->high || ((t1)->high == (t2)->high && (t1)->low < (t2)->low))
#define TIME_H_STR_MAXLEN 19 // 2008/10/25 19:27:36

EXP_FUNC const char *asc_time_h(const time_h *t, char *buffer);
EXP_FUNC time_h *getNowUTC(time_h *t);

#if !defined _INC_TIME_INL
#define ASC_BUFF_SIZE	26  // Ascii buffer size is 26 bytes, (24 chars and CR+LF)

char* ctime(const time_t* timer);
#endif

//typedef int FILE;
#if !defined(TOTALCROSS_INTEGRATION)
EXP_FUNC FILE * STDCALL ax_fopen(const char *name, const char *type);
EXP_FUNC int STDCALL ax_open(const char *pathname, int flags);
#endif

#if !defined(TOTALCROSS_INTEGRATION)
#ifdef CONFIG_PLATFORM_LINUX
void exit_now(const char *format, ...) __attribute((noreturn));
#else
void exit_now(const char *format, ...);
#endif
#endif

/* Mutexing definitions */
#if defined(CONFIG_SSL_CTX_MUTEXING)
#if defined(WIN32)
#define SSL_CTX_MUTEX_TYPE          HANDLE
#define SSL_CTX_MUTEX_INIT(A)       A=CreateMutex(0, FALSE, 0)
#define SSL_CTX_MUTEX_DESTROY(A)    CloseHandle(A)
#define SSL_CTX_LOCK(A)             WaitForSingleObject(A, INFINITE)
#define SSL_CTX_UNLOCK(A)           ReleaseMutex(A)
#elif HAVE_PTHREAD
#include <pthread.h>
#define SSL_CTX_MUTEX_TYPE          pthread_mutex_t
#define SSL_CTX_MUTEX_INIT(A)       pthread_mutex_init(&A, NULL)
#define SSL_CTX_MUTEX_DESTROY(A)    pthread_mutex_destroy(&A)
#define SSL_CTX_LOCK(A)             pthread_mutex_lock(&A)
#define SSL_CTX_UNLOCK(A)           pthread_mutex_unlock(&A)
#else
#undef CONFIG_SSL_CTX_MUTEXING
#endif
#endif

#if !defined(CONFIG_SSL_CTX_MUTEXING) /* no mutexing */
#define SSL_CTX_MUTEX_INIT(A)
#define SSL_CTX_MUTEX_DESTROY(A)
#define SSL_CTX_LOCK(A)
#define SSL_CTX_UNLOCK(A)
#endif

#ifdef __cplusplus
}
#endif

#endif 
