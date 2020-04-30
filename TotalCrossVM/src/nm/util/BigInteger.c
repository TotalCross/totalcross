// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

// guich@tc115_1: fixed java's >>> (some places was converting to uint64 but the correct was uint32, and changed 0x80000000 to (int32)0x80000000

static int32 add_1(int32* dest, int32* x, int32 size, int32 y);
static int32 add_n(int32* dest, int32* x, int32* y, int32 len);
static int32 sub_n(int32* dest, int32* X, int32* Y, int32 size);
static int32 mul_1(int32* dest, int32* x, int32 len, int32 y);
static void mul(int32* dest, int32* x, int32 xlen, int32* y, int32 ylen);
static int64 udiv_qrnnd(int64 N, int32 D);
static int32 divmod_1(int32* quotient, int32* dividend, int32 len, int32 divisor);
static int32 submul_1(int32* dest, int32 offset, int32* x, int32 len, int32 y);
static void divide(int32* zds, int32 nx, int32* y, int32 ny);
static int32 chars_per_word(int32 radix);
static int32 count_leading_zeros(int32 i);
static int32 set_str(int32* dest, int8* str, int32 str_len, int32 base);
static int32 cmp(int32* x, int32* y, int32 size);
static int32 cmp2(int32* x, int32 xlen, int32* y, int32 ylen);
static int32 rshift(int32* dest, int32* x, int32 x_start, int32 len, int32 count);
static void rshift0(int32* dest, int32* x, int32 x_start, int32 len, int32 count);
static int64 rshift_long(int32* x, int32 len, int32 count);
static int32 lshift(int32* dest, int32 d_offset, int32* x, int32 len, int32 count);
static int32 findLowestBit(int32 word);
static int32 findLowestBit2(int32* words);
static int32 gcd(int32* x, int32* y, int32 len);
static int32 intLength(int32 i);
static int32 intLength2(int32* words, int32 len);

#define LMASK (int64)I64_CONST(0xffffffff)

static int32 add_1(int32* dest, int32* x, int32 size, int32 y)
{
   int64 carry = (int64) y & LMASK;
   while (--size >= 0)
   {
      carry += ((int64) *x++ & LMASK);
      *dest++ = (int32) carry;
      carry = (int64)(((uint64)carry) >> 32); // !!!! estava carry >>= 32; diferente de todos os outros, que usava >>>
   }
   return (int32) carry;
}

static int32 add_n(int32 *dest, int32* x, int32* y, int32 len)
{
   int64 carry = 0;
   while (--len >= 0)
   {
      carry += ((int64) *x++ & LMASK) + ((int64) *y++ & LMASK);
      *dest++ = (int32) carry;
      carry = (int64)(((uint64)carry) >> 32);
   }
   return (int32) carry;
}

static int32 sub_n(int32* dest, int32* X, int32* Y, int32 size)
{
   int32 cy = 0;
   while (--size >= 0)
   {
      int32 y = *Y++;
      int32 x = *X++;
      y += cy; /* add previous carry to subtrahend */
      // Invert the high-order bit, because: (unsigned) X > (unsigned) Y
      // iff: (int32) (X^(int32)0x80000000) > (int32) (Y^(int32)0x80000000).
      cy = (y ^ (int32)0x80000000) < (cy ^ (int32)0x80000000) ? 1 : 0;
      y = x - y;
      cy += (y ^ (int32)0x80000000) > (x ^ (int32)0x80000000) ? 1 : 0;
      *dest++ = y;
   }
   return cy;
}

static int32 mul_1(int32* dest, int32* x, int32 len, int32 y)
{
   int64 yword = (int64) y & LMASK;
   int64 carry = 0;
   while (--len >= 0)
   {
      carry += ((int64) *x++ & LMASK) * yword;
      *dest++ = (int32) carry;
      carry = (int64)(((uint64)carry) >> 32); // carry >>>= 32;
   }
   return (int32) carry;
}

static void mul(int32* dest, int32* x, int32 xlen, int32* y, int32 ylen)
{
   int32 i,j;
   dest[xlen] = mul_1(dest, x, xlen, y[0]);

   for (i = 1; i < ylen; i++)
   {
      int64 yword = (int64) y[i] & LMASK;
      int64 carry = 0;
      for (j = 0; j < xlen; j++)
      {
         carry += ((int64) x[j] & LMASK) * yword + ((int64) dest[i + j] & LMASK);
         dest[i + j] = (int32) carry;
         carry = (int64)(((uint64)carry) >> 32); // carry >>>= 32;
      }
      dest[i + xlen] = (int32) carry;
   }
}

static int64 udiv_qrnnd(int64 N, int32 D)
{
   int64 q, r;
   int64 a1 = (int64)((uint64)N >> 32); // a1 = N >>> 32;
   int64 a0 = N & LMASK;
   if (D >= 0)
   {
      if (a1 < (int64)((D - a1 - ((int64)((uint64)a0 >> 31))) & LMASK)) // if (a1 < ((D - a1 - (a0 >>> 31)) & 0xffffffffL))
      {
         /* dividend, divisor, and quotient are nonnegative */
         q = N / D;
         r = N % D;
      }
      else
      {
         /* Compute c1*2^32 + c0 = a1*2^32 + a0 - 2^31*d */
         int64 c = N - ((int64) D << 31);
         /* Divide (c1*2^32 + c0) by d */
         q = c / D;
         r = c % D;
         /* Add 2^31 to quotient */
         q += 1 << 31;
      }
   }
   else
   {
      int64 b1 = (uint32)D >> 1; /* d/2, between 2^30 and 2^31 - 1 */ // long b1 = D >>> 1
      // long c1 = (a1 >> 1); /* A/2 */
      // int32 c0 = (a1 << 31) + (a0 >> 1);
      int64 c = (uint64)N >> 1; // c = N >>> 1;
      if (a1 < b1 || (a1 >> 1) < b1)
      {
         if (a1 < b1)
         {
            q = c / b1;
            r = c % b1;
         }
         else
         /* c1 < b1, so 2^31 <= (A/2)/b1 < 2^32 */
         {
            c = ~(c - (b1 << 32));
            q = c / b1; /* (A/2) / (d/2) */
            r = c % b1;
            q = (~q) & LMASK; /* (A/2)/b1 */
            r = (b1 - 1) - r; /* r < b1 => new r >= 0 */
         }
         r = 2 * r + (a0 & 1);
         if ((D & 1) != 0)
         {
            if (r >= q)
            {
               r = r - q;
            }
            else if (q - r <= ((int64) D & LMASK))
            {
               r = r - q + D;
               q -= 1;
            }
            else
            {
               r = r - q + D + D;
               q -= 2;
            }
         }
      }
      else
      /* Implies c1 = b1 */
      { /* Hence a1 = d - 1 = 2*b1 - 1 */
         if (a0 >= ((int64) (-D) & LMASK))
         {
            q = -1;
            r = a0 + D;
         }
         else
         {
            q = -2;
            r = a0 + D + D;
         }
      }
   }

   return (r << 32) | (q & LMASK);
}

static int32 divmod_1(int32* quotient, int32* dividend, int32 len, int32 divisor)
{
   int32 i = len - 1;
   int64 r = dividend[i];
   if ((r & LMASK) >= ((int64) divisor & LMASK))
      r = 0;
   else
   {
      quotient[i--] = 0;
      r <<= 32;
   }

   for (; i >= 0; i--)
   {
      int32 n0 = dividend[i];
      r = (r & ~LMASK) | (n0 & LMASK);
      r = udiv_qrnnd(r, divisor);
      quotient[i] = (int32) r;
   }
   return (int32) (r >> 32);
}

static int32 submul_1(int32* dest, int32 offset, int32* x, int32 len, int32 y)
{
   int64 yl = (int64) y & LMASK;
   int32 carry = 0;
   int32 j = 0,x_j;
   do
   {
      int64 prod = ((int64) x[j] & LMASK) * yl;
      int32 prod_low = (int32) prod;
      int32 prod_high = (int32) (prod >> 32);
      prod_low += carry;
      // Invert the high-order bit, because: (unsigned) X > (unsigned) Y
      // iff: (int32) (X^(int32)0x80000000) > (int32) (Y^(int32)0x80000000).
      carry = ((prod_low ^ (int32)0x80000000) < (carry ^ (int32)0x80000000) ? 1 : 0) + prod_high;
      x_j = dest[offset + j];
      prod_low = x_j - prod_low;
      if ((prod_low ^ (int32)0x80000000) > (x_j ^ (int32)0x80000000)) 
         carry++;
      dest[offset + j] = prod_low;
   } while (++j < len);
   return carry;
}

static void divide(int32* zds, int32 nx, int32* y, int32 ny)
{
   // This is basically Knuth's formulation of the classical algorithm,
   // but translated from in scm_divbigbig in Jaffar's SCM implementation.

   // Correspondance with Knuth's notation:
   // Knuth's u[0:m+n] == zds[nx:0].
   // Knuth's v[1:n] == y[ny-1:0]
   // Knuth's n == ny.
   // Knuth's m == nx-ny.
   // Our nx == Knuth's m+n.

   // Could be re-implemented using gmp's mpn_divrem:
   // zds[nx] = mpn_divrem (&zds[ny], 0, zds, nx, y, ny).

   int32 i,j = nx;
   do
   { // loop over digits of quotient
      // Knuth's j == our nx-j.
      // Knuth's u[j:j+n] == our zds[j:j-ny].
      int32 qhat; // treated as unsigned
      if (zds[j] == y[ny - 1])
         qhat = -1; // 0xffffffff
      else
      {
         int64 w = (((int64) (zds[j])) << 32) + ((int64) zds[j - 1] & LMASK);
         qhat = (int32) udiv_qrnnd(w, y[ny - 1]);
      }
      if (qhat != 0)
      {
         int32 borrow = submul_1(zds, j - ny, y, ny, qhat);
         int64 carry;
         int32 save = zds[j];
         int64 num = ((int64) save & LMASK) - ((int64) borrow & LMASK);
         while (num != 0)
         {
            qhat--;
            carry = 0;
            for (i = 0; i < ny; i++)
            {
               carry += ((int64) zds[j - ny + i] & LMASK) + ((int64) y[i] & LMASK);
               zds[j - ny + i] = (int32) carry;
               carry = (int64)(((uint64)carry) >> 32); // carry >>>= 32;
            }
            zds[j] += (int32)carry;
            num = carry - 1;
         }
      }
      zds[j] = qhat;
   } while (--j >= ny);
}

static int32 chars_per_word(int32 radix)
{
   switch (radix)
   {
      case 0: 
      case 1: 
      case 2: return 32;
      case 3: return 20;
      case 4: return 16;
      case 5: return 13;
      case 6: return 12;
      case 7: return 11;
      case 8: 
      case 9: return 10;
      case 10:
      case 11: return 9;
      case 12:
      case 13:
      case 14:
      case 15:
      case 16: return 8;
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
      case 22:
      case 23: return 7;
      default: 
         if (radix <= 40)
            return 6;
         // The following are conservative, but we don't care.
         else if (radix <= 256)
            return 4;
         else
            return 1;
   }
}

static int32 count_leading_zeros(int32 i)
{
   int32 count=0,j,k;
   if (i == 0) return 32;
   for (k = 16; k > 0; k = k >> 1)
   {
      j = (uint32)i >> k; // int j = i >>> k;
      if (j == 0)
         count += k;
      else
         i = j;
   }
   return count;
}

static int32 set_str(int32* dest, int8* str, int32 str_len, int32 base)
{
   int32 size = 0;
   if ((base & (base - 1)) == 0)
   {
      // The base is a power of 2. Read the input string from
      // least to most significant character/digit. */

      int32 next_bitpos = 0, res_digit = 0, i;
      int32 bits_per_indigit = 0;
      for (i = base; (i >>= 1) != 0;)
         bits_per_indigit++;

      for (i = str_len; --i >= 0;)
      {
         int32 inp_digit = str[i];
         res_digit |= inp_digit << next_bitpos;
         next_bitpos += bits_per_indigit;
         if (next_bitpos >= 32)
         {
            dest[size++] = res_digit;
            next_bitpos -= 32;
            res_digit = inp_digit >> (bits_per_indigit - next_bitpos);
         }
      }

      if (res_digit != 0) dest[size++] = res_digit;
   }
   else
   {
      // General case. The base is not a power of 2.
      int32 indigits_per_limb = chars_per_word(base);
      int32 str_pos = 0;

      while (str_pos < str_len)
      {
         int32 chunk = str_len - str_pos, res_digit, big_base, cy_limb;
         if (chunk > indigits_per_limb) chunk = indigits_per_limb;
         res_digit = str[str_pos++];
         big_base = base;

         while (--chunk > 0)
         {
            res_digit = res_digit * base + str[str_pos++];
            big_base *= base;
         }

         if (size == 0)
            cy_limb = res_digit;
         else
         {
            cy_limb = mul_1(dest, dest, size, big_base);
            cy_limb += add_1(dest, dest, size, res_digit);
         }
         if (cy_limb != 0) dest[size++] = cy_limb;
      }
   }
   return size;
}

static int32 cmp(int32* x, int32* y, int32 size)
{
   while (--size >= 0)
   {
      int32 x_word = x[size];
      int32 y_word = y[size];
      if (x_word != y_word)
      {
         // Invert the high-order bit, because:
         // (unsigned) X > (unsigned) Y iff
         // (int32) (X^(int32)0x80000000) > (int32) (Y^(int32)0x80000000).
         return (x_word ^ (int32)0x80000000) > (y_word ^ (int32)0x80000000) ? 1 : -1;
      }
   }
   return 0;
}

static int32 cmp2(int32* x, int32 xlen, int32* y, int32 ylen)
{
   return xlen > ylen ? 1 : xlen < ylen ? -1 : cmp(x, y, xlen);
}

static int32 rshift(int32* dest, int32* x, int32 x_start, int32 len, int32 count)
{
   int32 count_2 = 32 - count;
   int32 low_word = x[x_start];
   int32 retval = low_word << count_2;
   int32 i = 1;
   for (; i < len; i++)
   {
      int32 high_word = x[x_start + i];
      dest[i - 1] = (int32)((uint32)low_word >> count) | ((int32)high_word << count_2);  // dest[i - 1] = (low_word >>> count) | (high_word << count_2);
      low_word = high_word;
   }
   dest[i - 1] = (int32)((uint32)low_word >> count); // dest[i - 1] = low_word >>> count;
   return retval;
}

static void rshift0(int32* dest, int32* x, int32 x_start, int32 len, int32 count)
{
   int32 i;
   if (count > 0)
      rshift(dest, x, x_start, len, count);
   else
      for (i = 0; i < len; i++)
         dest[i] = x[i + x_start];
}

static int64 rshift_long(int32* x, int32 len, int32 count)
{
   int32 wordno,sign,w0,w1,w2;
   wordno = count >> 5;
   count &= 31;
   sign = x[len - 1] < 0 ? -1 : 0;
   w0 = wordno >= len ? sign : x[wordno];
   wordno++;
   w1 = wordno >= len ? sign : x[wordno];
   if (count != 0)
   {
      wordno++;
      w2 = wordno >= len ? sign : x[wordno];
      w0 = (int32)(((uint32)w0 >> count) | ((int32)w1 << (32 - count))); // w0 = (w0 >>> count) | (w1 << (32 - count));
      w1 = (int32)(((uint32)w1 >> count) | ((int32)w2 << (32 - count))); // w1 = (w1 >>> count) | (w2 << (32 - count));
   }
   return ((int64) w1 << 32) | ((int64) w0 & LMASK);
}

static int32 lshift(int32* dest, int32 d_offset, int32* x, int32 len, int32 count)
{
   int32 count_2 = 32 - count;
   int32 i = len - 1;
   uint32 high_word = (uint32)x[i];
   int32 retval = high_word >> count_2; // int retval = high_word >>> count_2; - ps: high_word is already unsigned
   d_offset++;
   while (--i >= 0)
   {
      uint32 low_word = (uint32)x[i];
      dest[d_offset + i] = (high_word << count) | (low_word >> count_2);
      high_word = low_word;
   }
   dest[d_offset + i] = high_word << count;
   return retval;
}

static int32 findLowestBit(int32 word)
{
   int32 i = 0;
   while ((word & 0xF) == 0)
   {
      word >>= 4;
      i += 4;
   }
   if ((word & 3) == 0)
   {
      word >>= 2;
      i += 2;
   }
   if ((word & 1) == 0) i += 1;
   return i;
}

static int32 findLowestBit2(int32* words)
{
   int32 i;
   for (i = 0;; i++)
   {
      if (words[i] != 0) return 32 * i + findLowestBit(words[i]);
   }
}

static int32 gcd(int32* x, int32* y, int32 len)
{
   int32 *odd_arg, *other_arg, *tmp; /* One of x or y which is odd. */ /* The other one can be even or odd. */
   int32 i, word, initShiftWords, initShiftBits;
   // Find sh such that both x and y are divisible by 2**sh.
   for (i = 0;; i++)
   {
      word = x[i] | y[i];
      if (word != 0)
      {
         // Must terminate, since x and y are non-zero.
         break;
      }
   }
   initShiftWords = i;
   initShiftBits = findLowestBit(word);
   // Logically: sh = initShiftWords * 32 + initShiftBits

   // Temporarily devide both x and y by 2**sh.
   len -= initShiftWords;
   rshift0(x, x, initShiftWords, len, initShiftBits);
   rshift0(y, y, initShiftWords, len, initShiftBits);

   if ((x[0] & 1) != 0)
   {
      odd_arg = x;
      other_arg = y;
   }
   else
   {
      odd_arg = y;
      other_arg = x;
   }

   for (;;)
   {
      // Shift other_arg until it is odd; this doesn't
      // affect the gcd, since we divide by 2**k, which does not
      // divide odd_arg.
      for (i = 0; other_arg[i] == 0;)
         i++;
      if (i > 0)
      {
         int32 j;
         for (j = 0; j < len - i; j++)
            other_arg[j] = other_arg[j + i];
         for (; j < len; j++)
            other_arg[j] = 0;
      }
      i = findLowestBit(other_arg[0]);
      if (i > 0) rshift(other_arg, other_arg, 0, len, i);

      // Now both odd_arg and other_arg are odd.

      // Subtract the smaller from the larger.
      // This does not change the result, since gcd(a-b,b)==gcd(a,b).
      i = cmp(odd_arg, other_arg, len);
      if (i == 0) break;
      if (i > 0)
      { // odd_arg > other_arg
         sub_n(odd_arg, odd_arg, other_arg, len);
         // Now odd_arg is even, so swap with other_arg;
         tmp = odd_arg;
         odd_arg = other_arg;
         other_arg = tmp;
      }
      else
      { // other_arg > odd_arg
         sub_n(other_arg, other_arg, odd_arg, len);
      }
      while (odd_arg[len - 1] == 0 && other_arg[len - 1] == 0)
         len--;
   }
   if (initShiftWords + initShiftBits > 0)
   {
      if (initShiftBits > 0)
      {
         int32 sh_out = lshift(x, initShiftWords, x, len, initShiftBits);
         if (sh_out != 0) x[(len++) + initShiftWords] = sh_out;
      }
      else
      {
         for (i = len; --i >= 0;)
            x[i + initShiftWords] = x[i];
      }
      for (i = initShiftWords; --i >= 0;)
         x[i] = 0;
      len += initShiftWords;
   }
   return len;
}

static int32 intLength(int32 i)
{
   return 32 - count_leading_zeros(i < 0 ? ~i : i);
}

static int32 intLength2(int32* words, int32 len)
{
   len--;
   return intLength(words[len]) + 32 * len;
}

//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_add_1_IIii(NMParams p) // totalcross/util/BigInteger native static int add_1(int []dest, int []x, int size, int y);
{
   p->retI = add_1((int32*)ARRAYOBJ_START(p->obj[0]), (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[0], p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_add_n_IIIi(NMParams p) // totalcross/util/BigInteger native static int add_n(int []dest, int []x, int []y, int len);
{
   p->retI = add_n((int32*)ARRAYOBJ_START(p->obj[0]), (int32*)ARRAYOBJ_START(p->obj[1]), (int32*)ARRAYOBJ_START(p->obj[2]), p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_sub_n_IIIi(NMParams p) // totalcross/util/BigInteger native static int sub_n(int []dest, int []x, int []y, int size);
{
   p->retI = sub_n((int32*)ARRAYOBJ_START(p->obj[0]), (int32*)ARRAYOBJ_START(p->obj[1]), (int32*)ARRAYOBJ_START(p->obj[2]), p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_mul_1_IIii(NMParams p) // totalcross/util/BigInteger native static int mul_1(int []dest, int []x, int len, int y);
{
   p->retI = mul_1((int32*)ARRAYOBJ_START(p->obj[0]), (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[0], p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_mul_IIiIi(NMParams p) // totalcross/util/BigInteger native static void mul(int []dest, int []x, int xlen, int []y, int ylen);
{
   mul((int32*)ARRAYOBJ_START(p->obj[0]), (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[0], (int32*)ARRAYOBJ_START(p->obj[2]), p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_udiv_qrnnd_li(NMParams p) // totalcross/util/BigInteger native static long udiv_qrnnd(long n, int d);
{
   p->retL = udiv_qrnnd(p->i64[0], p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_divmod_1_IIii(NMParams p) // totalcross/util/BigInteger native static int divmod_1(int []quotient, int []dividend, int len, int divisor);
{
   p->retI = divmod_1((int32*)ARRAYOBJ_START(p->obj[0]), (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[0], p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_submul_1_IiIii(NMParams p) // totalcross/util/BigInteger native static int submul_1(int []dest, int offset, int []x, int len, int y);
{
   p->retI = submul_1((int32*)ARRAYOBJ_START(p->obj[0]), p->i32[0], (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[1], p->i32[2]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_divide_IiIi(NMParams p) // totalcross/util/BigInteger native static void divide(int []zds, int nx, int []y, int ny);
{
   divide((int32*)ARRAYOBJ_START(p->obj[0]), p->i32[0], (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_chars_per_word_i(NMParams p) // totalcross/util/BigInteger native static int chars_per_word(int radix);
{
   p->retI = chars_per_word(p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_count_leading_zeros_i(NMParams p) // totalcross/util/BigInteger native static int count_leading_zeros(int i);
{
   p->retI = count_leading_zeros(p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_set_str_IBii(NMParams p) // totalcross/util/BigInteger native static int set_str(int []dest, byte []str, int str_len, int base);
{
   p->retI = set_str((int32*)ARRAYOBJ_START(p->obj[0]), (int8*)ARRAYOBJ_START(p->obj[1]), p->i32[0], p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_cmp_IIi(NMParams p) // totalcross/util/BigInteger native static int cmp(int []x, int []y, int size);
{
   p->retI = cmp((int32*)ARRAYOBJ_START(p->obj[0]), (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_cmp_IiIi(NMParams p) // totalcross/util/BigInteger native static int cmp(int []x, int xlen, int []y, int ylen);
{
   p->retI = cmp2((int32*)ARRAYOBJ_START(p->obj[0]), p->i32[0], (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_rshift_IIiii(NMParams p) // totalcross/util/BigInteger native static int rshift(int []dest, int []x, int x_start, int len, int count);
{
   p->retI = rshift((int32*)ARRAYOBJ_START(p->obj[0]), (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[0], p->i32[1], p->i32[2]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_rshift0_IIiii(NMParams p) // totalcross/util/BigInteger native static void rshift0(int []dest, int []x, int x_start, int len, int count);
{
   rshift0((int32*)ARRAYOBJ_START(p->obj[0]), (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[0], p->i32[1], p->i32[2]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_rshift_long_Iii(NMParams p) // totalcross/util/BigInteger native static long rshift_long(int []x, int len, int count);
{
   p->retL = rshift_long((int32*)ARRAYOBJ_START(p->obj[0]), p->i32[0], p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_lshift_IiIii(NMParams p) // totalcross/util/BigInteger native static int lshift(int []dest, int d_offset, int []x, int len, int count);
{
   p->retI = lshift((int32*)ARRAYOBJ_START(p->obj[0]), p->i32[0], (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[1], p->i32[2]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_findLowestBit_i(NMParams p) // totalcross/util/BigInteger native static int findLowestBit(int word);
{
   p->retI = findLowestBit(p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_findLowestBit_I(NMParams p) // totalcross/util/BigInteger native static int findLowestBit(int []words);
{
   p->retI = findLowestBit2((int32*)ARRAYOBJ_START(p->obj[0]));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_gcd_IIi(NMParams p) // totalcross/util/BigInteger native static int gcd(int []x, int []y, int len);
{
   p->retI = gcd((int32*)ARRAYOBJ_START(p->obj[0]), (int32*)ARRAYOBJ_START(p->obj[1]), p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_intLength_i(NMParams p) // totalcross/util/BigInteger native static int intLength(int i);
{
   p->retI = intLength(p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuBI_intLength_Ii(NMParams p) // totalcross/util/BigInteger native static int intLength(int []words, int len);
{
   p->retI = intLength2((int32*)ARRAYOBJ_START(p->obj[0]), p->i32[0]);
}
