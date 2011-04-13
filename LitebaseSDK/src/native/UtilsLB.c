/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

/**
 * This module defines useful functions for other Litebase modules.
 */

#include "UtilsLB.h"

/**
 * Compares 2 unicode strings, similar to <code>xstrcmp()</code>.
 *
 * @param string1 The first string to be compared.
 * @param string2 The second string to be compared.
 * @param length1 The length of the first string.
 * @param length2 The length of the second string.
 * @param isCaseless Indicates if the comparison is caseless or not.
 * @return 0 if both strings are equal; a positive value if first character that does not match has a greater value in string1 than in string2; a 
 * negative value, otherwise.
 */
int32 str16CompareTo(JCharP string1, JCharP string2, int32 length1, int32 length2, bool isCaseless)
{
	TRACE("str16CompareTo")
   int32 n = (length1 < length2)? length1 : length2;

   if (isCaseless)
   {
      JChar char1,
            char2;
      while (n--)
      {
         if ((char1 = TC_JCharToLower(*string1)) == (char2 = TC_JCharToLower(*string2)))
         {
            string1++;
            string2++;
         }
         else return (int32)char1 - (int32)char2;
      }
   }
   else
   {
      while (n--)
      {
         if (*string1 == *string2)
         {
            string1++;
            string2++;
         }
         else return (int32)*string1 - (int32)*string2;
      }
   }

   return length1 - length2;
}

/**
 * Compares 2 unicode strings to see one string is the prefix of another string.
 *
 * @param charsStr The string where the prefix will be searched.
 * @param prefixStr The prefix to search in charsStr.
 * @param charsLen The length of <code>charsStr</code>.
 * @param prefixLen The length of <code>prefixStr</code>.
 * @param srcOffset The offset on <code>charsStr</code> where to start searching.
 * @param isCaseless Indicates if the comparison is caseless or not.
 * @return <code>true</code> if <code>prefixStr</code> is a prefix of <code>charsStr</code> with the given offset; <code>false</code>, otherwise.
 */
bool str16StartsWith(JCharP charsStr, JCharP prefixStr, int32 charsLen, int32 prefixLen, int32 srcOffset, bool isCaseless)
{
	TRACE("str16StartsWith")
   int32 n = prefixLen;
   if (!n || srcOffset < 0 || srcOffset > charsLen - n) 
      return false;
   charsStr += srcOffset;
   if (isCaseless)
      while (--n >= 0)
      {
         if (TC_JCharToLower(*charsStr++) != TC_JCharToLower(*prefixStr++))
            return false;
      }
   else
      while (--n >= 0)
         if (*charsStr++ != *prefixStr++)
            return false;
   return true;
}

/**
 * Returns the index where a substring beggins in another string.
 *
 * @param charsStr The string where to find the substring.
 * @param subStr The substring to be found in the first string.
 * @param charsLen The length of <code>charsStr</code>.
 * @param subLen The length of <code>subStr</code>.
 * @param isCaseless Indicates if the search is caseless or not.
 * @return 0 if the substring is empty; the index of the substring in the first string, or -1 if the substring does not occur in the other string. 
 */
int32 str16IndexOf(JCharP charsStr, JCharP subStr, int32 charsLen, int32 subLen, bool isCaseless)
{
	TRACE("str16IndexOf")
   JChar c;
   int32 j = (charsLen - subLen),
         i = 0,
         len;
   bool found = false;
   JCharP string1,
          string2;
 
   if (!subLen--)
      return 0;

   if (isCaseless)
   {
      c = TC_JCharToLower(*subStr++);
      while (!found)
      {
         while (i <= j && TC_JCharToLower(*charsStr) != c) // Searches for the next ocurrence of the first char.
         {
            i++;
            charsStr++;
         }
         if (i > j) // Has passed the end of the string?
            return -1;

         // Now searches the rest of the string.
         len = subLen;
         string1 = charsStr + 1;
         string2 = subStr;
         if (!len) // juliana@223_7: corrected a bug on like contains ('%...%').
            found = true;
         while (len--)
         {
            if (TC_JCharToLower(*string1++) != TC_JCharToLower(*string2++))
            {
               i++;
               charsStr++; // guich@321_3
               found = false; // juliana@223_7: corrected a bug on like contains ('%...%').
               break;
            }
            found = true;
         }
      }
   }
   else
   {
      c = *subStr++;
      while (!found)
      {
         while (i <= j && *charsStr != c) // Searches for the next ocurrence of the first char.
         {
            i++;
            charsStr++;
         }
         if (i > j) // Has passed the end of the string?
            return -1;

         // Now searches the rest of the string.
         len = subLen;
         string1 = charsStr + 1;
         string2 = subStr;
         if (!len) // juliana@223_7: corrected a bug on like contains ('%...%').
            found = true;
         while (len--)
         {
            if (*string1++ != *string2++)
            {
               i++;
               charsStr++; // guich@321_3
               found = false; // juliana@223_7: corrected a bug on like contains ('%...%').
               break;
            }
            found = true;
         }
      }
   }
   return i;
}

/**
 * Returns the number of days that a specific month has.
 * 
 * @param month The month number: from 1 to 12.
 * @param year The year.
 * @return The number of days that the month has, taking a possible leap year into consideration. 
 */
int32 getDaysInMonth(int32 month, int32 year) // rnovais@567_2
{
	TRACE("getDaysInMonth")
   if (month != 2) 
      return monthDays[month - 1];
   return (!(year % 4) && ((year % 100) || !(year % 400)))? 29 : 28;
}

/**  
 * Verifies if year, month, and day forms a valid date. 
 *
 * @param year The year.
 * @param month The month.
 * @param day The day.
 * @return An int of the format YYYYMMDD if the date is valid; otherwise, returns -1.
*/
int32 verifyDate(int32 year, int32 month, int32 day) // rnovais@567_2
{
	TRACE("verifyDate")
   if (100 <= year && year <= 999) // year contains 3 digits. 
      return -1; 
   if (0 <= year && year < 20) // From 2000 to 2019
      year += 2000; 
   else
   if (20 <= year && year < 100) // From 1920 to 1999
      year += 1900;

   // Checks if the day and month values are valid, if the number of days in the month is valid and if the year is not too big.
   if (day > 0 && month >= 1 && month <= 12 && day <= getDaysInMonth(month, year) && year < 3000)
      return  (year * 10000) + (month * 100) + day;
   else
      return -1;
}

/**
 * Does a left trim in a string.
 *
 * @param chars The string to be trimed.
 * @return The string with the blanks in the beggining trimmed.
 */
CharP strLeftTrim(CharP chars)
{
   TRACE("strLeftTrim")
   while (*chars == ' ') 
      chars++;
   return chars;
}

/**
 * Does a left and right trim in a string.
 *
 * @param chars The string to be trimmed.
 * @return The string with the blanks in the beggining and in the end trimmed.
 */
CharP strTrim(CharP chars)
{
	TRACE("strTrim")
   int32 j;

   while (*chars == ' ') // Left trim.
      chars++;
   if ((j = xstrlen(chars) - 1) < 0)
		return chars;
   while (chars[j] == ' ') // Right trim. 
      j--;
   chars[j + 1] = 0; // Zeroes the end of the string.
   return chars;
}

/**
 * Does a left and right trim in a unicode string.
 *
 * @param string16Str The string to be trimmed.
 * @param string16Len The length of the string to be trimmed, which is updated to return the length of the string trimmed.
 * @return The string with blanks in the beggining and in the end trimmed.
 */
JCharP str16Trim(JCharP string16Str, int32* string16Len)
{
	TRACE("str16Trim")
   while (*string16Str == ' ') // Left trim.
   { 
      string16Str++; 
      *string16Len--;
   }
   while (string16Str[*string16Len - 1] == ' ') // Right trim. 
      *string16Len--;
   string16Str[*string16Len] = 0; // Zeroes the end of the string.
   return string16Str;
}

/**
 * Does a left trim in a unicode string.
 *
 * @param string16Str The string to be trimmed.
 * @param string16Len The length of the string to be trimmed, which is updated to return the length of the string trimmed.
 * @return The string with blanks in the beggining.
 */
JCharP str16LeftTrim(JCharP string16Str, int32* string16Len)
{
	TRACE("str16LeftTrim")
   while (*string16Str == ' ') // Left trim.
   { 
      string16Str++; 
      *string16Len--;
   }
   return string16Str;
}

/**
 * Verifies if a string is a valid Date and transforms it into a correspondent int date. 
 *
 * @param chars A string in a date format.
 * @returns A correspondent int datetime or -1 if the date is invalid.
 */
int32 testAndPrepareDate(CharP chars)
{
	TRACE("testAndPrepareDate")
   int32 i = -1, 
         j = 0,
         start = 0,
         len = 0,
         n = xstrlen(chars),
         pos;
   int32 p[3];
   bool err;
   char c;
   if (5 <= n && n <= 10) // 1/1/1 to DD/MM/YYYY
   { 
      while (++i < n)
      {
         c = chars[i];
         if ('0' <= c && c <= '9')
            len++;
         else if (j == 2) // If there's already two separators, error!
            break;
         else
         {
            chars[pos = start + len] = 0;
            p[j++] = TC_str2int(&chars[start], &err);
            if (err)
               return -1;
            start += len + 1;
            len = 0;
            chars[pos] = c; // Returns the original character.
         }
      }
      if (j == 2) // Has it found exactly 2 separators?
      {
         p[2] = TC_str2int(&chars[start], &err);
         if (err)
            return -1;
         return verifyDate(p[0], p[1], p[2]);
      }
   }
   return -1;
}

/**
 * Verifies if a string is a valid Time and transforms it into a correspondent int datetime. The time ranges from 00:00:00:000 to 23:59:59:9999 (it 
 * accepts dots and colons). This method is very flexible. For instance: 2:-:8:10 is the same as 2:0:8:10 and returns 20008010; 02:.:8:1 is the same 
 * as 02:0.0:8 and returns 20000008; :4:8:19 is the same as 0:4:8:19 and returns; 408019 2.4.a.876 is the same as 2.4.0.876 and returns 20400876.
 * 
 * @param chars A string in a time format.
 * @returns A correspondent int datetime or -1 if the value is not a valid time.
 */
int32 testAndPrepareTime(CharP chars)
{
	TRACE("testAndPrepareTime")
   int32 i = -1, 
         j = 0,
         start = 0, 
         len = 0,
         n = xstrlen(chars), 
         hour, 
         minutes, 
         seconds, 
         millis,
         pos;
   int32 p[4];
   char c;
   bool err;
   p[0] = p[1] = p[2] = p[3] = 0;
   if (n > 0 && n <= 13)
   {
      while (++i < n)
      {
         c = chars[i];
         if ('0' <= c && c <= '9')
            len++;
         else
         {
            chars[pos = start + len] = 0;
            p[j++] = TC_str2int(&chars[start], &err);
            if (err)
               return -1;
            start += len + 1;
            len = 0;
            chars[pos] = c;
         }
      }
      p[j++] = TC_str2int(&chars[start], &err);
      if (err)
         return -1;
      hour = p[0];
      minutes = p[1];
      seconds = p[2];
      millis = p[3];

      if (hour < 0 || hour > 23 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59 || millis < 0 || millis > 999)
         return -1;
      return hour * 10000000 + minutes * 100000 + seconds * 1000 + millis;
   }
   else 
      return -1;
}

/**
 * Creates an <code>IntVector</code> with the given initial capacity.
 *
 * @param context The thread context where the function is being executed.
 * @param count The <code>IntVector</code> initial capacity.
 * @param heap A heap to allocate the <code>IntVector</code>. If it is null, <code>xmalloc</code> is used and its array must be verified. 
 */
IntVector newIntVector(Context context, int32 count, Heap heap)
{
	TRACE("newIntVector")
   IntVector iv;
   iv.length = count;
   iv.size = 0;
       
   if ((iv.heap = heap))
      iv.items = (int32*)TC_heapAlloc(heap, count << 2); // Allocates in the heap.
   else if (!(iv.items = (int32*)xmalloc(count << 2))) // Normal allocation.
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
   return iv;
}

/**
 * Adds an integer to the <code>IntVector</code>, enlarging it if necessary.
 *
 * @param context The thread context where the function is being executed.
 * @param intVector The <code>IntVector</code>.
 * @param value The integer value to be inserted in the <code>IntVector</code>.
 * @return <code>false</code> If the <code>IntVector</code> needs to be increase withou using a heap and the memory allocation fail; 
 * <code>true</code>, otherwise.
 * @throws OutOfMemoryError If there is not enougth memory allocate memory.
 */
bool IntVectorAdd(Context context, IntVector* intVector, int32 value)
{
	TRACE("IntVectorAdd")
   if (intVector->size == intVector->length)
   {
      int32 length = intVector->length;
      Heap heap = intVector->heap;
      if (heap)
      {
         int32* items = (int32*)TC_heapAlloc(heap, length << 3); // Allocates in the heap. 
         xmemmove(items, intVector->items, length << 2);
         intVector->items = items;
      }
      else
      {
         if (!(intVector->items = (int32*)xrealloc((uint8*)intVector->items, length << 3))) // Normal allocation.
         {
            TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            return false;
         }
      }
      intVector->length <<= 1;
   }
   intVector->items[intVector->size++] = value;
   return true;
}

/**
 * Transforms the <code>IntVector</code> into an integer array when is necessary to create a copy of it.
 *
 * @param The <code>IntVector</code> whose array will be copied.
 * @param heap The heap to allocate the array.
 * @return The integer array.
 */
int32* intVector2Array(IntVector* intVector, Heap heap)
{
	TRACE("intVector2Array")
   int32* intArray = (int32*)TC_heapAlloc(heap, intVector->size << 2);
   xmemmove(intArray, intVector->items, intVector->size << 2);
   return intArray;
}

/**
 * Creates an <code>IntVector</code> with a <code>Hashtable</code> items.
 *
 * @param table The <code>Hashtable</code>.
 * @param heap A heap to allocate the <code>IntVector</code> integer array.
 * @return The <code>IntVector</code> with the <code>Hashtable</code> items.
 */
IntVector htGetKeys(Hashtable* table, Heap heap)
{
	TRACE("htGetKeys")
   IntVector intVector = newIntVector(null, table->size, heap);
   int32* items = intVector.items;
   int32 i = table->hash;
   HtEntry** oldTable = table->items;
   HtEntry* e; 
   HtEntry* old;

   while (--i >= 0)
   {
      old = oldTable[i];
      while (old)
      {
         old = (e = old)->next;
         items[intVector.size++] = e->key;
      }
   }
   return intVector;
}

/**
 * Creates an empty full <code>IntVector</code>.
 *
 * @param count The size of the <code>IntVector</code>, which can't be null.
 * @param heap A heap to allocate the <code>IntVector</code> integer array.
 * @return The <code>IntVector</code>. 
 */
IntVector newIntBits(int32 count, Heap heap)
{
	TRACE("newIntBits")
   IntVector intVector;
   intVector = newIntVector(null, count = (count >> 5) + 1, heap);
   intVector.size = count;
   return intVector;
}

/**
 * Finds the next bit set from an b-tree.
 *
 * @param intVector The <code>IntVector</code> with the index bitmap.
 * @param start The first value to search.
 * @return The position of the next bit set.
 */
int32 findNextBitSet(IntVector *v, int32 start)
{
	TRACE("findNextBitSet")
   int32 index = start >> 5, // Converts from bits to int.
         n;
   uint32 b;
   start &= 31;
   while (1)
   {
      if ((n = v->size - index) > 0 && !v->items[index]) // guich@104
      {
         start = 0; // guich@104
         while (n > 0 && !v->items[index]) // Finds the next int with any bit set.
         {
            n--;
            index++;
         }
      }
      if (n > 0) // Found?
      {
         b = v->items[index];
         while (start < 32 && !(b & ((int32)1 << start)))
            start++;
         if (start == 32)
         {
            start = 0;
            index++; // No more bits in this int? Tests next ints.
            continue;
         }
         return start + (index << 5);
      }
      return -1;
   }
}

/**
 * Finds the previous bit set from an b-tree.
 *
 * @param intVector The <code>IntVector</code> with the index bitmap.
 * @param start The first value to search.
 * @return The position of the previous bit set.
 */
int32 findPrevBitSet(IntVector *v, int32 start)
{
	TRACE("findPrevBitSet")
   int32 index = start >> 5; // Converts from bits to int.
   uint32 b;
   start &= 31;
   while (1)
   {
      if (index >= 0 && !v->items[index]) // guich@104
      {
         start = 31; // guich@104
         while (index >= 0 && v->items[index] == 0) // Finds the next int with any bit set.
            index--;
      }
      if (index >= 0) // Found?
      {
         b = v->items[index];
         while (start >= 0 && (b & ((int32)1 << start)) == 0)
            start--;
         if (start < 0)
         {
            start = 31;
            index--; // No more bits in this int? Tests next ints.
            continue;
         }
         return start + (index << 5);
      }
      return -1;
   }
}

/**
 * Compares the two records, using the sort column list.
 * 
 * @param record1 The first record to be compared.
 * @param record2 The second record to be compared.
 * @param nullsRecord1 The null values of the first record.
 * @param nullsRecord2 The null values of the second record.
 * @param sortFieldListCount The number of elements of <code>sortFieldList</code>.
 * @param sortFieldList The order of evaluation of the record.
 * @return 0 if the arrays are identical in the comparison order; a positive number if <code>record1[]</code> is greater than <code>record2[]</code>; 
 * otherwise, a negative number.
 */ 
int32 compareRecords(SQLValue** record1, SQLValue** record2, uint8* nullsRecord1, uint8* nullsRecord2, int32 sortFieldListCount, 
                                                                                                   SQLResultSetField** sortFieldList)
{
   TRACE("compareRecords")
   int32 i = -1,
         index,
         result;
   SQLResultSetField* field;
   
   while (++i < sortFieldListCount) // Compares the records, using the sequence used by the sort column List.
   {
      index = (field = sortFieldList[i])->tableColIndex;
      
      // Compares the elements checking if they are null.
      result = valueCompareTo(record1[index], record2[index], field->dataType, isBitSet(nullsRecord1, index), 
                                                                                        isBitSet(nullsRecord2, index));

      if (!field->isAscending)
         result = -result;

      if (result)
         return result;
   }

   return 0;
}

/** 
 * Sets and resets one bit in an array of bytes.
 *
 * @param items The array of bytes
 * @param index The bit index to be set or reset.
 * @param isOn A bool that defines whether the bit will be set or reset.
 */
void setBit(uint8* items, int32 index, bool isOn)
{
	TRACE("setBit")
   if (isOn)
      items[index >> 3] |= ((int32)1 << (index & 7));  // Sets
   else
      items[index >> 3] &= ~((int32)1 << (index & 7)); // Resets.
}

/**
 * Gets the full name of a file: path + file name.
 * 
 * @param fileName The file name.
 * @param sourcePath The path where the table is stored.
 * @param buffer Receives path + file name with a path separator if necessary.
 */
void getFullFileName(CharP fileName, CharP sourcePath, TCHARP buffer)
{
	TRACE("getFullFileName")
   int32 endChar = xstrlen(sourcePath) - 1;

// juliana@223_6: Corrected a bug that would create spourious paths if they had a stress on Windows CE. 
#ifdef WINCE
   TC_CharP2JCharPBuf(sourcePath, endChar + 1, buffer, true);
   if (sourcePath[endChar] != PATH_SEPARATOR && sourcePath[endChar] != NO_PATH_SEPARATOR)
   {
      buffer[endChar + 1] = PATH_SEPARATOR;
      buffer[endChar + 2] = 0;
      endChar += 2;
   }
   else
      endChar++;
   TC_CharP2JCharPBuf(fileName, -1, &buffer[endChar], true);
#else
   xstrcpy(buffer, sourcePath);
   if (sourcePath[endChar] != PATH_SEPARATOR && sourcePath[endChar] != NO_PATH_SEPARATOR)
   {
      buffer[endChar + 1] = PATH_SEPARATOR;
      buffer[endChar + 2] = 0;
   }
   xstrcat(buffer, fileName);
#endif
}

/**
 * Returns the time in the format YYYYMMDDHHMMSS as a long value. It does not include the millis.
 *
 * @param year The year.
 * @param month The month.
 * @param day The day.
 * @param hour The hour.
 * @param minute The minute.
 * @param second The second.
 * @return The time in the format YYYYMMDDHHMMSS.
 */
int64 getTimeLong(int32 year, int32 month, int32 day, int32 hour, int32 minute, int32 second)
{
   return (int64)year * (int64)1000000000L * (int64)10L + month * 100000000L + day * 1000000 + hour * 10000 + minute * 100 + second;
}

/**
 * Checks if a unicode string starts with a substring in the ascii format.
 *
 * @param unicodeStr The unicode string.
 * @param asciiStr The ascii string.
 * @param unicodeLen The unicode string length.
 * @param asciiLen The ascii string length.
 * @return <code>true</code> if the unicode string starts with the ascii string; <code>false</code>, otherwise.
 */
bool JCharPStartsWithCharP(JCharP unicodeStr, CharP asciiStr, int32 unicodeLen, int32 asciiLen)
{
   if (asciiLen > unicodeLen) // If the substring is greater than the string, the result i false.
      return false;

   else // Checks if each asciiStr character equals each unicodeStr character.
      while (asciiLen-- > 0)
         if (*unicodeStr++ != *asciiStr++)
            return false;
   return true;
}

/**
 * Checks if a unicode string is equal to the ascii format.
 *
 * @param unicodeStr The unicode string.
 * @param asciiStr The ascii string.
 * @param unicodeLen The unicode string length.
 * @param asciiLen The ascii string length.
 * @param ignoreCase Indicates if the case is to be taken into consideration or not.
 * @return <code>true</code> if the unicode is equal to the ascii string; <code>false</code>, otherwise.
 */
bool JCharPEqualsCharP(JCharP unicodeStr, CharP asciiStr, int32 unicodeLen, int32 asciiLen, bool ignoreCase)
{
   if (asciiLen > unicodeLen) // If the substring is greater than the string, the result i false.
      return false;

   if (ignoreCase) // caseless
   {
      while (asciiLen-- > 0)
         if (TC_JCharToLower(*unicodeStr++) != TC_JCharToLower(*asciiStr++))
            return false;
   }
   else // case sensitive
      while (asciiLen-- > 0)
         if (*unicodeStr++ != *asciiStr++)
            return false;
   return true;
}

// juliana@230_4
/**
 * Gets the current path used by the system to store application files.
 *
 * @param sourcePath The path used by the system to store application files.
 */ 
void getCurrentPath(CharP sourcePath)
{
   if (!TC_getDataPath(sourcePath) || sourcePath[0] == 0)
      xstrcpy(sourcePath, TC_getAppPath());
}
