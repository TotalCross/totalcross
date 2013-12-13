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



#include <fcntl.h>
#include <sys/types.h>
#include <dirent.h>
#include <unistd.h>
#include <ctype.h>
#include <errno.h>
#include <sys/time.h>
#include <time.h>
#include <sys/stat.h>

#if (defined __SYMBIAN32__ && !defined __WINS__)  // ARMI

#define LONG_BITS       (sizeof(long) << 3)
#define LONGLONG_BITS   (sizeof(long long) << 3)

#define UL_HIGH   1
#define UL_LOW    0

union uu
{
   long long ll;           /* as a signed int64 */
   unsigned long ul[2];    /* as two unsigned int32 */
};

static long long my_fixunsdfdi(double a)   // unsigned dbl float to dbl int
{
   unsigned long hi, lo;

   hi = a / (((unsigned long long)1) << LONG_BITS);
   lo = (a - ((double)hi) * (((unsigned long long)1) << LONG_BITS));
   return ((unsigned long long)hi << LONG_BITS) | lo;
}

static long long my_fixunssfdi(float a0)   // unsigned single float to dbl int
{
   double a = a0;
   unsigned long hi, lo;

   hi = a / (((unsigned long long)1) << LONG_BITS);
   lo = (a - ((double)hi) * (((unsigned long long)1) << LONG_BITS));
   return ((unsigned long long)hi << LONG_BITS) | lo;
}

long long __fixdfdi(double a)              // dbl float to dbl int
{
   return (a < 0) ? -my_fixunsdfdi(-a) : my_fixunsdfdi(a);
}

long long __fixsfdi(float a)              // single float to dbl int
{
   return (a < 0) ? -my_fixunssfdi(-a) : my_fixunssfdi(a);
}

double __floatdidf(long long u)
{
   double d;
   union uu m;
   m.ll = (u < 0) ? -u : u; //fdie@560_27 fix neg values issue on Symbian
   d = (4.0 * m.ul[UL_HIGH]) * (1L << (LONG_BITS - 2));
   d += (2.0 * (m.ul[UL_LOW] >> 1)) + (m.ul[UL_LOW] & 1); // fdie@ grant that the long isn't signed before the double conversion
   return u < 0 ? -d : d;
}

float __floatdisf(long long u)
{
#if 1
   double f;

   if (
      ((53 < LONGLONG_BITS) && (53 > (LONGLONG_BITS-53+24))) &&
      ((-((long long)1 << 53) >= u) || (u >= ((long long)1 << 53))) &&
      ((unsigned long long)u & (((unsigned long long)1 << (LONGLONG_BITS-53))-1))
   ) {
      u &= ~ (((unsigned long long)1 << (LONGLONG_BITS-53))-1);
      u |= ((unsigned long long)1 << (LONGLONG_BITS-53));
   }
   f = (long)(u >> LONG_BITS);
   f *= 2.0 * (((unsigned long long)1) << (LONG_BITS - 1));
   f += (unsigned long)(u & ((((unsigned long long)1) << LONG_BITS) - 1));
   return (float)f;
#else
   // or even simpler
   return __floatdidf(u);
#endif
}
#endif // (defined __SYMBIAN32__ && !defined __WINS__)  // ARMI


#if defined(darwin)

#include <mach/mach.h>
#include <mach/mach_host.h>

//flsobral@tc126_66: fixed implementation of Vm.getFreeMemory on iPhone.
static int32 privateGetFreeMemory(bool maxblock)
{
    mach_port_t host_port;
    mach_msg_type_number_t host_size;
    vm_size_t pagesize;
    
    host_port = mach_host_self();
    host_size = sizeof(vm_statistics_data_t) / sizeof(integer_t);
    host_page_size(host_port, &pagesize);        
 
    vm_statistics_data_t vm_stat;
              
    if (host_statistics(host_port, HOST_VM_INFO, (host_info_t)&vm_stat, &host_size) != KERN_SUCCESS)
        return 0;
 
    /* Stats in bytes */
    //flsobral@tc126: kept code to find out the used memory for further reference.
//    natural_t mem_used = (vm_stat.active_count + vm_stat.inactive_count + vm_stat.wire_count) * pagesize;
    natural_t mem_free = vm_stat.free_count * pagesize;
//    natural_t mem_total = mem_used + mem_free;
    
    return mem_free;
}

#else // defined(darwin)

#define BUFFER_SIZE 338 /* We are bothered about only the first 338 bytes of the /proc/meminfo file */
#define PROC_MEM_FILE "/proc/meminfo"

/* Extract numbers from a string between the start and end indices */
static unsigned long extract_number(char *str, int start, int end)
{
   int i, j;
   char buf[end-start];

   for (i=start, j=0; i<end; i++)
      if (isdigit(str[i]))
         buf[j++] = str[i];
   buf[j] = '\0';

   return strtoul(buf, NULL, 0) * 1024;
}

/* see http://www.chandrashekar.info/vault/linux-system-programs.html */
static int32 privateGetFreeMemory(bool maxblock)
{
   char buf[BUFFER_SIZE];
   int in;
   UNUSED(maxblock)

   if ((in = open(PROC_MEM_FILE, O_RDONLY)) < 0)
      return 2048*1024; // 2mb at least, or some litebase tests will just skip over.

   if (read(in, buf, sizeof(buf)) < sizeof(buf))
      return 2048*1024;

   close(in);

#ifdef ANDROID // MemFree:            3316 kB
   {                      
      int i0,i1;
      i0 = (int)xstrstr(buf,"MemFree:");
      if (!i0)
         return 2048*1024;
      i1 = (int)xstrstr((char*)i0, "kB");
      if (!i1)
         i1 = i0 + 25;
      in = extract_number(buf, i0 - (int)buf, i1 - (int)buf);
   }
#else
   in =  extract_number(buf, 35, 49);
#endif
   return in;
}

#endif //defined(HAVE_STATFS) && defined(darwin)

static void privateSleep(uint32 millis)
{
#if !defined(__SYMBIAN32__)
   usleep(1000UL * millis);
#endif
}

static int32 privateGetTimeStamp()
{
   struct timeval now;
   gettimeofday(&now, NULL);
   return now.tv_sec * 1000 + now.tv_usec / 1000;
}

static bool pfileIsDir(TCHARP path, int32 slot)
{
   struct stat statData;

   if (stat(path, &statData))
      return false;

   return S_ISDIR(statData.st_mode);
}

static Err privateListFiles(TCHARP path, int32 slot, TCHARPs** list, int32* count, Heap h, int32 options)
{
   struct dirent * entry;
   DIR * dir;
   int32 pathlen = 0;
   bool isDir;
   Err err;
   bool recursive = options & LF_RECURSIVE;
#ifdef ANDROID
   bool exists;
   int32 si;
   TCHARPs* search;
#endif
   
   dir = opendir(path);
   if (!dir)
      return NO_ERROR; // guich@tc200: don't throw error, let the user handle it when opening the file. (issue 2254)
   if (recursive)
      pathlen = tcslen(path) + 1;

   while ((entry = readdir(dir)))
   {
      if ((entry->d_name[0] != '.') || ((entry->d_name[1] != '\0') && ((entry->d_name[1] != '.') || (entry->d_name[2] != '\0')))) /* warning: order matters! */
      {
         int32 fileNameSize = tcslen(entry->d_name)+2; //One for null and one extra in case it is a directory.
         TCHARP fileName = (TCHARP)heapAlloc(h, sizeof(TCHAR)*(fileNameSize+pathlen) ); // note: if an heap error occurs, the FindClose will never be called; TODO change the implementation to allow that.

         if (recursive)
         {
            tcscpy(fileName, path);
            if (path[pathlen-2] != '/')
               tcscat(fileName, TEXT("/"));
         }
         tcscat(fileName, entry->d_name);

         isDir = pfileIsDir(fileName, 0);
         if (isDir)
            tcscat(fileName, TEXT("/"));
#ifdef ANDROID // Android has a bug that result in files being added more than once. so, check if it already exists
         for (exists = false, search = *list, si = 0; si < *count && !exists; search = search->next, si++)
            exists |= strEq(search->value, fileName);
         if (exists)
            continue;
#endif
         *list = TCHARPsAdd(*list, fileName, h); // add entry to list - if recursive, include the pathname
         (*count)++;

         if (isDir && recursive)
         {
            err = privateListFiles(fileName, slot, list, count, h, recursive);
            if (err != NO_ERROR)
            {
               closedir(dir);
               return err;
            }
         }
      }
   }
   closedir(dir);
   return NO_ERROR;
}

/*****   timeUpdate   *****
 *
 * struct timeval
 * struct timezone
 * struct tm
 *
 * gettimeofday
 * localtime_r
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *******************************/

static void privateGetDateTime(int32* year, int32* month, int32* day, int32* hour, int32* minute, int32* second, int32* millis)
{
   time_t secsSince;
   struct tm tm;
   struct timeval tv;

   // "The use of the timezone structure (2nd arg of gettimeofday) is obsolete; the tz argument should normally be specified as NULL"
   // as explained here http://linux.die.net/man/2/gettimeofday
   gettimeofday(&tv, NULL);
   secsSince = tv.tv_sec;
   localtime_r(&secsSince, &tm);

   *year   = tm.tm_year + 1900;
   *month  = tm.tm_mon + 1;
   *day    = tm.tm_mday;
   *hour   = tm.tm_hour;
   *minute = tm.tm_min;
   *second = tm.tm_sec;
   *millis = tv.tv_usec/1000;;
}

