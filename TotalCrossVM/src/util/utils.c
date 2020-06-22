// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

#include <math.h>

#ifndef WIN32     // TODO MOVE THESE TO THE PLATFORM SPECIFIC FILES
#include <time.h>
#if defined(linux) || defined(darwin)
#include <dlfcn.h>
#endif
#endif

#if defined(WINCE) || defined(WIN32)
 #include "win/utils_c.h"
#else
 #include "posix/utils_c.h"
#endif

#if __APPLE__
#undef bool
#define bool int
#endif

///////////////////////////////////////////////////////////////////////////
//                                 Hash                                  //
///////////////////////////////////////////////////////////////////////////

/* Returns the hashcode that will be used as key for the Hashtable. */
TC_API int32 hashCode(CharP s)
{
   int32 hash=0;
   if (s)
   while (*s)
      hash = (hash<<5) - hash + (int32)*s++;
   return hash;
}

TC_API int32 hashCodeFmt(CharP fmt, ...)
{
   int32 hash=0;
   CharP s;
   LongBuf buf;
   va_list vaargs;
   va_start(vaargs, fmt);
   while (*fmt)
      switch (*fmt++)
      {
         case 'v': // CharP converting to lowercase
         {
            s = va_arg(vaargs, CharP);
            while (*s)
            {
               char v = *s++;
               if (v >= 'A' && v <= 'Z') // guich@_104
                  v += 32;
               hash = (hash<<5) - hash + (int32)v;
            }
            break;
         }
         case 's':
         {
            s = va_arg(vaargs, CharP);
str:
            while (*s)
               hash = (hash<<5) - hash + (int32)*s++;
            break;
         }
         case 'S':
         {
            JCharP s = va_arg(vaargs, JCharP);
            while (*s)
               hash = (hash<<5) - hash + (int32)*s++;
            break;
         }
         case 'x': // for pointer, when we start to support 64-bit platforms, move this to case 'l' with the aid of a #ifdef
         case 'i':
         {
            int32 i = va_arg(vaargs, int32);
            s = int2str(i, buf);
            goto str;
         }
         case 'l':
         {
            int64 i = va_arg(vaargs, int64);
            s = long2str(i, buf);
            goto str;
         }
      }
   va_end(vaargs);
   return hash;
}

int32 hashCodeSlash2Dot(CharP s)
{
   int32 hash=0;
   for (; *s; s++)
      hash = (hash<<5) - hash + (int32)((*s == '/' || *s == '\\')? '.' : *s);
   return hash;
}

///////////////////////////////////////////////////////////////////////////
//                                 Math                                  //
///////////////////////////////////////////////////////////////////////////
int toBaseAsDecimal(int num, int baseFrom, int baseTo) // converts a number to the given base and keep it as a decimal number. actually, works with bases <= 10
{
   int k = 0;
   int m = 1;
   while (num > 0)
   {
      int n = num % baseTo;
      num /= baseTo;
      k += m * n;
      m *= baseFrom;
   }
   return k;
}

// converts an unsigned value to hex, filling with zeros. outBuf must have enough space to hold places+1.
TC_API void int2hex(int32 b, int32 places, CharP outBuf)
{
   CharP b2h = "0123456789ABCDEF";
   outBuf += places;
   *outBuf-- = 0;
   for (;places-- > 0; b>>=4)
      *outBuf-- = b2h[b & 0xF];
}

// converts an unsigned value to hex, filling with zeros. outBuf must have enough space to hold places+1.
void long2hex(int64 b, int32 places, CharP outBuf)
{
   CharP b2h = "0123456789ABCDEF";
   outBuf += places;
   *outBuf-- = 0;
   for (;places-- > 0; b>>=4)
      *outBuf-- = b2h[b & 0xF];
}

bool radix2int(CharP str, int32 radix, int32* result)
{
   int32 strLen = xstrlen(str);
   int32 i;
   char c;
   int32 digit;
   int32 r = 0;
   int32 m = 1;

   if (radix < 2 || radix > 16)
      return false;

   for (i = strLen-1; i >= 0; i--)
   {
      c = str[i];
      if (c == '+')
         break;
      if (c == '-')
      {
         r = -r;
         break;
      }
      if ('0' <= c && c <= '9')
         digit = c - '0';
      else
      if ('A' <= c && c <= 'F')
         digit = c - 'A' + 10;
      else
         digit = c - 'a' + 10;

      if (digit < 0 || digit >= radix)
         return false;

      r += m * digit;
      if (m == 1)
         m = radix;
      else
         m *= radix;
   }
   (*result) = r;
   return true;
}

bool radix2long(CharP str, int32 radix, int64* result)
{
   int32 strLen = xstrlen(str);
   int32 i;
   char c;
   int32 digit;
   int64 r = 0;
   int64 m = 1;

   if (radix < 2 || radix > 16)
      return false;

   for (i = strLen-1; i >= 0; i--)
   {
      c = str[i];
      if (c == '+')
         break;
      if (c == '-')
      {
         r = -r;
         break;
      }
      if ('0' <= c && c <= '9')
         digit = c - '0';
      else
      if ('A' <= c && c <= 'F')
         digit = c - 'A' + 10;
      else
         digit = c - 'a' + 10;

      if (digit < 0 || digit >= radix)
         return false;

      r += m * digit;
      if (m == 1)
         m = radix;
      else
         m *= radix;
   }
   (*result) = r;
   return true;
}

/* Returns the minimum between the two numbers. */
int32 min32(int32 i1, int32 i2)
{
   return (i1<=i2)?i1:i2;
}
/* Returns the maximum between the two numbers. */
int32 max32(int32 i1, int32 i2)
{
   return (i1>=i2)?i1:i2;
}
/* Returns the lowercase of the given character. */
TC_API char toLower(char c)
{
   return (char)((!('A' <= c && c <= 'Z'))?c:(c+32));
}
/* Returns the lowercase of the given character. */
TC_API char toUpper(char c)
{
   return (char)(!('a' <= c && c <= 'z'))?c:(c-32);
}

TC_API void CharPToLower(CharP c)
{
   for (; *c; c++)
      *c = toLower(*c);
}

TC_API void CharPToUpper(CharP c)
{
   for (; *c; c++)
      *c = toUpper(*c);
}

double dmod(double c1, double c2)
{
   return c1 - ((double)((int64)(c1 / c2))) * c2; // take the integer part of the division - guich@tc114_94: swapped c1 with c2.
}

TC_API int32 str2int(CharP str, bool *err)
{
   int32 r = 0;
   int32 m = 1;
   int n = 10; // vik@557_2: increased by 1 to correctly convert 1234567890
   CharP c = str;
   bool isNeg = false;
   if (err) *err = false;    
   while (*c) // test and go to end of string
   {
      if (*c == '-') //  guich@566_7: removed '+' - for Java, + is not a valid char
         isNeg = true;
      else
      if (*c < '0' || *c > '9' || n-- == 0) // guich@300_37: fixed int2str and long2str when an invalid char appeared. - guich@554_23: limit the number of decimals
      {
         if (err) *err = true;
         return 0;
      }
      c++;
   }
   for (c--; c >= str; c--)
   {
      if (*c == '-') 
      {r = -r; break;}
      r += m * (*c-'0');
      m = (m<<3) + (m<<1); // (m*8 + m*2) is faster than m*10
   }
   if (isNeg != (r < 0)) // check if the value was overflown or underflown
   {
      if (err) *err = true;
      return 0;
   }
   return r;
}

TC_API CharP int2str(int32 i, IntBuf buf)
{
   if (i == 0)
      return "0";
   else
   if (i == 0x80000000) // handle the only exception - guich@tc126_69: hex value, not decimal!
      return "-2147483648";
   else
   {
      CharP c = buf;
      bool negative = (i < 0);
      c += sizeof(IntBuf)-1;
      *c-- = 0;
      if (negative)
         i = -i;
      while (i > 0)
      {
         *c-- = (i % 10) + '0';
         i /= 10;
      }
      if (negative)
         *c-- = '-';
      c++;
      return c;
   }
}

static int64 str2longPriv(CharP str, bool *err, bool ignoreLen) // guich@tc114_40
{
   int64 r = 0;
   int64 m = 1;
   int n = 19; // vik@557_2: increased by 1 to correctly convert 1234567890
   CharP c = str;
   bool isNeg = false; // juliana@116_41: Added overflow and underflow check for longs.
   if (err) *err = false;
   while (*c) // test and go to end of string
   {
      if (*c == '-') //  guich@566_7: removed '+' - for Java, + is not a valid char
         isNeg = true;
      else
      if ((*c == 'L' || *c == 'l') && *(c+1) == 0) // last char is L?
      {
         *c = 0;
         break;
      }
      else
      if (n-- == 0)
      {
         if (ignoreLen)
         {
            *c = 0; // strip the number
            break;
         }
         else
         {
            if (err) *err = true;
            return 0;
         }
      }
      else
      if (*c < '0' || *c > '9') // guich@300_37: fixed int2str and long2str when an invalid char appeared. - guich@554_23: limit the number of decimals
      {
         if (err) *err = true;
         return 0;
      }
      c++;
   }
   if (n > 10) // if there are only 9 digits, we can use a faster routine
      return (int64)str2int(str, err);
   for (c--; c >= str; c--)
   {
      if (*c == '-') {r = -r; break;}
      r += m * (*c-'0');
      m = (m<<3) + (m<<1); // (m*8 + m*2) is faster than m*10
   }
   if (isNeg != (r < 0)) // check if the value was overflown or underflown
   {
      if (err) *err = true;
      return 0;
   }
   return r;
}

TC_API int64 str2long(CharP str, bool *err)
{
   return str2longPriv(str, err, false);
}

TC_API CharP long2str(int64 i, LongBuf buf)
{
   CharP c = buf+sizeof(LongBuf)-2;
   if (i == 0)
      return "0";
   else
   if (i >= -2147483647L && i <= 2147483647L) // guich@566_38: if the number fits in an int, then use the faster routine
      return int2str((int32)i, buf);
   buf[sizeof(LongBuf)-1] = 0;
   if (i == (int64)I64_CONST(0x8000000000000000))  // handle the only exception
      return "-9223372036854775808";
   else
   {
      bool negative = (i < 0);
      if (negative)
         i = -i;
      while (i > 0)
      {
         *c-- = ((int)(i % 10)) + '0';
         i /= 10;
      }
      if (negative)
         *c-- = '-';
      c++;
   }
   return c;
}

#define IEEE_I64_VALUE(x)  x
#define I64_BITS(VAL) (*(int64 *)(&VAL))
#define DOUBLE_NAN_VALUE               IEEE_I64_VALUE(I64_CONST(0x7ff8000000000000))
#define DOUBLE_POSITIVE_INFINITY_VALUE IEEE_I64_VALUE(I64_CONST(0x7ff0000000000000))
#define DOUBLE_NEGATIVE_INFINITY_VALUE IEEE_I64_VALUE(I64_CONST(0xfff0000000000000))
#define DOUBLE_MAX_NON_EXP 9.007199254740992E15 // 2^53
#define DOUBLE_MIN_NON_EXP 1.1102230246251565E-16 // 2^-53
#define DOUBLE_MAX_DIGITS 15

static double p1[] =
{
   1.0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7,
   1e8, 1e9, 1e10,1e11,1e12,1e13,1e14,1e15,
   1e16,1e17,1e18,1e19,1e20,1e21,1e22,1e23,
   1e24,1e25,1e26,1e27,1e28,1e29,1e30,1e31
};
static double p32[] =
{
   1.0,1e32,1e64,1e96,1e128,1e160,1e192,1e224,1e256,1e288
};
static double np32[] = // guich@570_51
{
   1.0,1e-32,1e-64,1e-96,1e-128,1e-160,1e-192,1e-224,1e-256,1e-288,1e-320,
};
static double Pow10(int32 exp) // fast pow(10,exp) - thanks to Peter M. Dickerson
{
   if (exp >=0)
      return p1[exp & 31]*p32[(exp>>5)];
   return np32[-exp >> 5]/p1[-exp & 31]; // guich@570_51: when exp >= 320, p32 index is 10, and 1e320 is impossible
}

/** recognizes exponencial term (e) */
TC_API double str2double(CharP str, bool *err) // guich@566_38: new routine
{
   CharP ePtr=0, c=0, dotPtr=0;
   double result = 0.0;
   int neg = 0,len=0;
   bool err2 = false;

   if (err) *err = false;

   if (*str == '-')
   {
      ++str;
      neg = 1;
   }
   else
   if (*str == '+') ++str; // guich@350_14: fixes problem when converting numbers that starts with +
   while (*str && *str <= ' ') // skip starting spaces - seems that JDK allows it. - guich@583_7: check if string is not null
      str++;
   if (*str == 0)
   {
      if (err) *err = true;
      return 0; // empty string or invalid char?
   }
   // analise the parts of the string, checking also for invalid chars
   for (c = str; *c; len++,c++)
   {
      if (*c == 'E' || *c == 'e') // guich@350_13: handle exponent in uppercase 'E'
         ePtr = c;
      else
      if (*c == 'D' || *c == 'd' || *c == 'F' || *c == 'f')
      {
         if (*(c+1) == 0) // if its the last char, truncate it
         {
            len--;
            *c = 0;
         }
         else
         {
            if (err) *err = true;
            return 0.0; // else, if followed by something, invalid!
         }
      }
      else
      if (*c == '.')
         dotPtr = c;
      else
      if (*c != '+' && *c != '-' && !('0' <= *c && *c <= '9'))
      {
         if (err) *err = true;
         return 0.0; // invalid char
      }
   }
   if (ePtr && dotPtr && ePtr < dotPtr) // 1e.2 ?
   {
      if (err) *err = true;
      return 0;
   }
   // the string will be broken in 3 parts: xxx.yyyEzzz
   if (ePtr)
      *ePtr = 0;
   if (dotPtr && str == dotPtr) // fix for .25
   {
      result = 0;
      err2 = false;
   }
   else
   {
      if (dotPtr)
         *dotPtr = 0;
      result = (double)str2longPriv(str, &err2, true); // convert the first part 'xxx'
      if (err2)
      {
         if (err) *err = true;
         return 0;
      }
   }
   if (dotPtr)
   {
      CharP end = ePtr ? ePtr : (str+len);
      int32 order = (int)(end - dotPtr-1);
      int64 v = str2longPriv(dotPtr+1, &err2, true);
      if (err2)
      {
         if (err) *err = true;
         return 0;
      }
      if (order > 19) // guich@tc114_40
         order = 19;
      result += (double)v * Pow10(-order);
   }
   if (ePtr) // scientific notation?
   {
      int32 expv;
      ePtr++;
      if (*ePtr == '+')
         ePtr++;
      expv = str2int(ePtr, err);
      if (expv > 308) // guich@tc111_8: handle +inf and -inf
         *((int64*)&result) = DOUBLE_POSITIVE_INFINITY_VALUE;
      else
      if (expv < -324)
         *((int64*)&result) = DOUBLE_NEGATIVE_INFINITY_VALUE;
      result *= Pow10(expv);
   }
   if (neg) result = -result;
   return result;
}

// guich@566_38: new double to string routines created by guich

#define LN10 2.30258509299404568402
static double rounds5[] = {5e-1,5e-2,5e-3,5e-4,5e-5,5e-6,5e-7,5e-8,5e-9,5e-10,5e-11,5e-12,5e-13,5e-14,5e-15,5e-16,5e-17,5e-18};

TC_API CharP double2str(double val,int32 decimalCount, DoubleBuf buffer)
{
   int64 bits = I64_BITS(val);
   CharP buf = buffer;
   CharP s;
   LongBuf lb;

   if (val == 0)
   {
      if (decimalCount == -1)
         xstrcpy(buf,"0.0");
      else
      {
         *buf++ = '0';
         if (decimalCount > 0)
         {
            *buf++ = '.';
            while (decimalCount-- > 0)
               *buf++ = '0';
         }
         *buf = 0;
      }
   }
   else
   if (bits == DOUBLE_NAN_VALUE)
      xstrcpy(buf,"NaN");
   else
   if (bits == DOUBLE_POSITIVE_INFINITY_VALUE || bits == DOUBLE_NEGATIVE_INFINITY_VALUE)
   {
      xstrcpy(buf, "-Inf");
      if (val > 0)
         buf[0] = '+';
   }
   else
   {
      // example: -1000.5432
      int64 integral, fract=0;
      int32 exponent;
      int floating = decimalCount < 0;
      if (floating) decimalCount = DOUBLE_MAX_DIGITS;
      if (val < 0)
      {
         val = -val; // 1000.5432
         *buf++ = '-';
      }

      exponent = (int32) (log(val) / LN10); // 3 : 1000.5432 = 1.0005432*10^3
      if (DOUBLE_MIN_NON_EXP <= val && val <= DOUBLE_MAX_NON_EXP) // does it fit without sci notation?
      {
         if (decimalCount == 0)
            val += 0.5;
         integral = (int64)val; // 1000
         exponent = 0;
      }
      else
      {
         bool adjusted = false; // guich@tc111_5
         while (1)
         {
            double mant = val / (double)Pow10(exponent);
            if (decimalCount < 18)
               mant += (double)rounds5[decimalCount];
            if (I64_BITS(mant) == DOUBLE_POSITIVE_INFINITY_VALUE) // case of converting the minimum double value
            {
               int32 e = exponent < 0 ? -exponent : exponent;
               mant = val;
               if (e > 300) {mant *= Pow10(300); e -= 300;} // guich@tc200: fix convertion of MIN_DOUBLE_VALUE
               mant *= Pow10(e); // remaining of exponent
               if (decimalCount < 18)
                  mant += (double)rounds5[decimalCount];
               val = mant;
               integral = (int64)val;
               break;
            }
            else
               integral = (int64)mant;
            if (integral == 0  && !adjusted) {adjusted = true; exponent--;} // 0.12345 ?
            else
            if (integral >= 10 && !adjusted) {adjusted = true; exponent++;} // 10.12345 ?
            else
            {
               val = mant;
               break;
            }
         }
      }
      if (decimalCount == 0)
      {
         s = long2str(integral, lb);
         while (*s)
            *buf++ = *s++;
      }
      else
      {
         int i,firstNonZero=-1; // number of zeros between . and first non-zero
         double pow10 = Pow10(decimalCount);
         int64 ipow10 = (int64)pow10;
         double f = val - integral; // 1000.5432-1000 = 0.5432
         if (f > 1.0e-16)
         {
            fract = (int64)(f * pow10 + (exponent == 0 ? 0.5 : 0));
            if (fract == ipow10) // case of Convert.toString(49.999,2)
            {
               fract = 0;
               integral++;
            }
         }
         s = long2str(integral, lb);
         while (*s)
            *buf++ = *s++;

         do
         {
            ipow10 /= 10;
            firstNonZero++;
         }
         while (ipow10 > fract);
         s = long2str(fract,lb);
         i = decimalCount - xstrlen(s);
         *buf++ = '.';
         if (0 < firstNonZero && firstNonZero < decimalCount)
         {
            i -= firstNonZero;
            while (firstNonZero-- > 0)
               *buf++ = '0';
         }
         while (*s)
            *buf++ = *s++;
         if (floating)
            while (buf[-2] != '.' && buf[-1] == '0')
               buf--;
         else
         if (i > 0) // fill with zeros if needed
         {
            if (i > 20) i = 20; // this should not respect the maximum allowed width, because its just for user formatting
            while (i-- > 0)
               *buf++ = '0';
         }
      }
      if (exponent != 0)
      {
         IntBuf ib;
         *buf++ = 'E';
         s = int2str(exponent,ib);
         while (*s)
            *buf++ = *s++;
      }
      *buf = 0;
   }
   if (buffer[0] == '-') // guich@tc200b5: check if its -0.00... and change to 0.00...
   {
      bool only0 = true;
      buf = buffer;
      for (buf++; *buf && only0; buf++)
         only0 &= *buf == '.' || *buf == '0';
      if (only0)
         xstrcpy(buffer,buffer+1); // remove the -
   }
   return buffer;
}

TC_API CharP int2CRID(int32 i, CharP crid)
{
   *crid++ = (char)((i >> 24) & 0xFF);
   *crid++ = (char)((i >> 16) & 0xFF);
   *crid++ = (char)((i >> 8 ) & 0xFF);
   *crid++ = (char)((i      ) & 0xFF);
   *crid = 0;
   return crid;
}

///////////////////////////////////////////////////////////////////////////
//                            Primitive read                             //
///////////////////////////////////////////////////////////////////////////

#ifdef WINCE
#define errno GetLastError()
#else
#include <errno.h>
#endif

int32 fread32(FILE* f)
{
   int32 i,err;
   err = (int32)fread(&i, 1, 4, f);
   if (err == -1)
      alert("Error on fread16: %d",errno);
   return i;
}
int16 fread16(FILE* f)
{
   int16 i;
   int32 err;
   err = (int32)fread(&i, 1, 2, f);
   if (err == -1)
      alert("Error on fread16: %d",errno);
   return i;
}

FILE* findFile(CharP name, CharP pathOut)
{
   FILE* f;
   char fullName[MAX_PATHNAME];

   // 1. search in current folder
   xstrprintf(fullName,"%s",name);
   f = fopen(fullName,"rb");
   // 2. search in vmPath
   if (f == null)
   {
      xstrprintf(fullName,"%s/%s",vmPath,name);
      f = fopen(fullName,"rb");
   }
   // 3. search in appPath
   if (f == null)
   {
      xstrprintf(fullName,"%s/%s",appPath,name);
      f = fopen(fullName,"rb");
   }
   // 4. search in ..
   if (f == null)
   {
      xstrprintf(fullName,"../%s",name);
      f = fopen(fullName,"rb");
   }
#if defined (WIN32) && !defined (WINCE)
   // 5. search on vm's parent folder
   if (f == null)
   {
      xstrprintf(fullName,"%s/../%s",vmPath,name);
      f = fopen(fullName,"rb");
   }
#ifdef _DEBUG
   // 6. may have to go up twice if debug
   if (f == null)
   {
      xstrprintf(fullName,"%s/../../%s",vmPath,name);
      f = fopen(fullName,"rb");
   }
#endif // _DEBUG
#ifdef ENABLE_TEST_SUITE
   // 7. search on SDK path if we are running the test suie
   if (f == null)
   {
      xstrprintf(fullName,"P:/gitrepo/TotalCross/TotalCrossSDK/dist/vm/%s",name);
      f = fopen(fullName,"rb");
   }
   // 8. Test.tcz
   if (f == null)
   {
      xstrprintf(fullName,"P:/gitrepo/TotalCross/TotalCrossVM/src/tests/java/install/win32/%s",name);
      f = fopen(fullName,"rb");
   }
#endif // ENABLE_TEST_SUITE only
#endif // WIN32 only
#ifdef ANDROID
   // 9. search also on litebase's path
   if (f == null)
   {
      xstrprintf(fullName,"/data/data/litebase.android/%s",name);
      f = fopen(fullName,"rb");
   }
#endif // _DEBUG

   if (f != null)
   {
      if (pathOut != null)
         xstrcpy(pathOut,fullName);
   }
   return f;
}

TC_API Err listFiles(TCHARP path, int32 slot, TCHARPs** list, int32* count, Heap h, int32 options)
{
   return privateListFiles(path, slot, list, count, h, options);
}

///////////////////////////////////////////////////////////////////////////
//                                Others                                 //
///////////////////////////////////////////////////////////////////////////

TC_API void getDateTime(int32* year, int32* month, int32* day, int32* hour, int32* minute, int32* second, int32* millis)
{      
   privateGetDateTime(year,month,day,hour,minute,second,millis);
}

TC_API int32 getTimeStamp()
{
   return privateGetTimeStamp() - firstTS;
}

#ifndef WIN32
TC_API void Sleep(uint32 ms)
{
   privateSleep(ms);
}
#endif

TC_API int32 getFreeMemory(bool maxblock)
{
   int32 s;
#ifdef INITIAL_MEM
   s = maxAvail;
#elif defined ANDROID
   JNIEnv *env = getJNIEnv();
   return (*env)->CallStaticIntMethod(env, applicationClass, jgetFreeMemory);
#else
   s = !maxblock ? 0 : privateGetFreeMemory(maxblock);
#endif
#if !defined(ANDROID) && !defined(FORCE_LIBC_ALLOC) && !defined(ENABLE_WIN32_POINTER_VERIFICATION)
   if (!maxblock)
      s += dlmallinfo().fordblks;
#else // for android and iphone, return something different of 0
   if (!maxblock)
      s += privateGetFreeMemory(maxblock);
#endif
   return s;
}

TC_API void normalizePath(TCHARP path)
{
   for (; *path; path++)
      if (*path == '\\')
         *path = '/';
}

///////////////////////////////////////////////////////////////////////////
//                        CharP/TCHAR convertions                        //
///////////////////////////////////////////////////////////////////////////

TC_API TCHARP CharP2TCHARP(CharP from)
{
   TCHARP buf = null;
   int32 bufLen = xstrlen(from);

   if ((buf = (TCHARP) xmalloc((bufLen+1)*sizeof(TCHARP))) != null)
#ifdef UNICODE
      CharP2JCharPBuf(from, bufLen, buf, true);
#else
      xstrncpy(buf, from, bufLen);
#endif
   return buf;
}

TC_API TCHARP CharP2TCHARPBuf(CharP from, TCHARP to)
{
   int32 fromLen = xstrlen(from);
#ifdef UNICODE
   CharP2JCharPBuf(from, fromLen, to, true);
#else
   xstrncpy(to, from, fromLen);
#endif
   return to;
}

TC_API CharP TCHARP2CharPBuf(TCHARP from, CharP to)
{
#if defined (UNICODE)
  return JCharP2CharPBuf(from, -1, to);
#else
  return xstrcpy(to, from);
#endif
}

TC_API CharP hstrdup(CharP s, Heap h) // duplicates a string allocating from a Heap
{
   int32 n = (s ? xstrlen(s) : 0) + 1;
   CharP ret = heapAlloc(h, n);
   xmemmove(ret, s, n);
   return ret;
}

void replaceChar(CharP s, char from, char to)
{
   for (; *s; s++)
      if (*s == from)
         *s = to;
}

#ifdef ANDROID
void jstring2CharP(jstring src, char* dest)
{
   jstring2CharPEnv(src, dest, getJNIEnv());
}
void jstring2CharPEnv(jstring src, char* dest, JNIEnv* env)
{
   const char *str = (*env)->GetStringUTFChars(env, src, 0);
   if (str) 
      xstrcpy(dest, str);
   else
      dest[0] = 0;
   (*env)->ReleaseStringUTFChars(env, src, str);
}
#endif

#ifdef ENABLE_TEST_SUITE
#include "utils_test.h"
#endif
