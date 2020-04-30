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
   uint32 n = (length1 < length2)? length1 : length2;

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
         i = 0;
   uint32 len;
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
* Does a left and right trim in tchar a string.
*
* @param chars The tchar string to be trimmed.
* @return The tchar string with the blanks in the beggining and in the end trimmed.
*/
TCHARP tstrTrim(TCHARP chars)
{
   TRACE("tstrTrim")
   int32 j;

   while (*chars == ' ') // Left trim.
      chars++;
   if ((j = tcslen(chars) - 1) < 0)
      return chars;
   while (chars[j] == ' ') // Right trim. 
      j--;
   chars[j + 1] = 0; // Zeroes the end of the string.
   return chars;
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
      (*string16Len)--;
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
		 else if (j == 3) // If there's already three separators, error!
            break;
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

      if ((hour = p[0]) < 0 || hour > 23 || (minutes = p[1]) < 0 || minutes > 59 || (seconds = p[2]) < 0 || seconds > 59 || (millis = p[3]) < 0 || millis > 999)
         return -1;
      return hour * 10000000 + minutes * 100000 + seconds * 1000 + millis;
   }
   else 
      return -1;
}

/**
 * Verifies if a string is a valid date or datetime and transforms it into a corresponding date or datetime.
 *
 * @param context The thread context where the function is being executed.
 * @param value The record value which will hold the date or datetime as integer(s).
 * @param chars The date or datetime as a string.
 * @param type <code>DATE_TYPE</code> or </code>DATETIME_TYPE</code>.
 * @return <code>false</code> if the string format is wrong; <code>true</code>, otherwise. 
 * @throws SQLParseException If the string format is wrong.
 */
bool testAndPrepareDateAndTime(Context context, SQLValue* value, CharP chars, int32 type)
{
   TRACE("testAndPrepareDateAndTime")
   CharP str = strTrim(chars);
   
   if (type == DATE_TYPE && (value->asInt = testAndPrepareDate(str)) == -1)
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_VALUE_ISNOT_DATE), chars);
      return false;
   }
   else if (type == DATETIME_TYPE)
   {
      CharP posSpace = xstrchr(str, ' ');
      if (posSpace)
      {
         *posSpace = 0;
         value->asDate = testAndPrepareDate(strTrim(str)); // Gets the date part. 
         value->asTime = testAndPrepareTime(strTrim(posSpace + 1)); // Gets the time part. 
      }
      else
      {
         value->asInt = testAndPrepareDate(str);
         value->asTime = 0; // The time part is 0.
      }
         
      if ((value->asDate == -1) || (value->asTime == -1))
      {
         if (posSpace)
            *posSpace = ' ';
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_VALUE_ISNOT_DATETIME), chars);
         return false;
      }
   }
   return true;
}

/**
 * Creates an <code>IntVector</code> with the given initial capacity.
 *
 * @param count The <code>IntVector</code> initial capacity.
 * @param heap A heap to allocate the <code>IntVector</code>.
 * @return The new int vector created.
 */
IntVector newIntVector(int32 count, Heap heap)
{
	TRACE("newIntVector")
   IntVector iv;
   
   iv.length = count;
   iv.size = 0;
   iv.items = (int32*)TC_heapAlloc(iv.heap = heap, count << 2); // Allocates in the heap.
   return iv;
}

/**
 * Adds an integer to the <code>IntVector</code>, enlarging it if necessary.
 *
 * @param intVector The <code>IntVector</code>.
 * @param value The integer value to be inserted in the <code>IntVector</code>.
 */
void IntVectorAdd(IntVector* intVector, int32 value)
{
	TRACE("IntVectorAdd")
   if (intVector->size == intVector->length)
   {
      int32 length = intVector->length;
      int32* items = (int32*)TC_heapAlloc(intVector->heap, (length + 1) << 3); // Allocates in the heap. 
      
      xmemmove(items, intVector->items, length << 2);
      intVector->items = items;
      intVector->length <<= 1;
   }
   intVector->items[intVector->size++] = value;
}

/**
 * Duplicates an int array when is necessary to create a copy of it.
 *
 * @param intArray The int array to be duplicated.
 * @param size The size of the array.
 * @param heap The heap to allocate the array.
 * @return The duplicated int array.
 */
int32* duplicateIntArray(int32* intArray, int32 size, Heap heap)
{
	TRACE("shortVector2Array")
   int32* newArray = (int32*)TC_heapAlloc(heap, size << 2);
   xmemmove(newArray, intArray, size << 2);
   return newArray;
}

/**
 * Duplicates a byte array when is necessary to create a copy of it.
 *
 * @param byteArray The byte array to be duplicated.
 * @param size The size of the array.
 * @param heap The heap to allocate the array.
 * @return The duplicated byte array.
 */
int8* duplicateByteArray(int8* byteArray, int32 size, Heap heap)
{
	TRACE("shortVector2Array")
   int8* newArray = (int8*)TC_heapAlloc(heap, size);
   xmemmove(newArray, byteArray, size);
   return newArray;
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
   intVector = newIntVector(count = (count >> 5) + 1, heap);
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
int32 findNextBitSet(IntVector* intVector, int32 start)
{
	TRACE("findNextBitSet")
   int32 index = start >> 5, // Converts from bits to int.
         n;
   uint32 b;
   start &= 31;
   while (1)
   {
      if ((n = intVector->size - index) > 0 && !intVector->items[index]) // guich@104
      {
         start = 0; // guich@104
         while (n > 0 && !intVector->items[index]) // Finds the next int with any bit set.
         {
            n--;
            index++;
         }
      }
      if (n > 0) // Found?
      {
         b = intVector->items[index];
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
      result = valueCompareTo(null, record1[index], record2[index], field->dataType, isBitSet(nullsRecord1, index), isBitSet(nullsRecord2, index), null);

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
      setBitOn(items, index);  // Sets
   else
      setBitOff(items, index); // Resets.
}

/**
 * Gets the full name of a file: path + file name.
 * 
 * @param fileName The file name.
 * @param sourcePath The path where the table is stored.
 * @param buffer Receives path + file name with a path separator if necessary.
 */
void getFullFileName(CharP fileName, TCHARP sourcePath, TCHARP buffer)
{
	TRACE("getFullFileName")
   int32 endChar = tcslen(sourcePath) - 1;

   // juliana@223_6: Corrected a bug that would create spourious paths if they had a stress on Windows CE. 
   tcscpy(buffer, sourcePath);
   if (sourcePath[endChar] != PATH_SEPARATOR && sourcePath[endChar] != NO_PATH_SEPARATOR)
      tcscat(buffer, TEXT("/"));
   TC_CharP2TCHARPBuf(fileName, &buffer[tcslen(buffer)]);
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
   TRACE("getTimeLong")
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
   TRACE("JCharPStartsWithCharP")
   
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
   TRACE("JCharPEqualsCharP")

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
void getCurrentPath(TCHARP sourcePath)                                       
{                     
   TRACE("getCurrentPath")
   char buffer[MAX_PATHNAME];
                                                         
   if (!TC_getDataPath(buffer) || buffer[0] == 0)
      xstrcpy(buffer, TC_getAppPath());
   TC_CharP2TCHARPBuf(buffer, sourcePath);
}    

/**
 * Formats a date in a unicode buffer.
 *
 * @param year Year.
 * @param month Month.
 * @param day Day.
 * @param buffer The buffer for the unicode formated date.
 */
void date2JCharP(int32 year, int32 month, int32 day, JCharP buffer)
{
   TRACE("date2JCharP")
   DateBuf dateTimeBuf;
   
   xstrprintf(dateTimeBuf, "%04d/%02d/%02d", year, month, day);
   TC_CharP2JCharPBuf(dateTimeBuf, 10, buffer, false); // juliana@238_1: corrected the end quote not appearing in the log files after dates. 
}

/**
 * Formats a date time in a unicode buffer.
 *
 * @param year Year.
 * @param month Month.
 * @param day Day.
 * @param hour Hour.
 * @param minute Minute.
 * @param second Second.
 * @param millis Millis.
 * @param buffer The buffer for the unicode formated date.
 */
void dateTime2JCharP(int32 year, int32 month, int32 day, int32 hour, int32 minute, int32 second, int32 millis, JCharP buffer)
{
   TRACE("dateTime2JCharP")
   DateTimeBuf dateTimeBuf;
   
   xstrprintf(dateTimeBuf, "%04d/%02d/%02d", year, month, day);
   xstrprintf(&dateTimeBuf[11], "%02d:%02d:%02d:%03d", hour, minute, second, millis);
   dateTimeBuf[10] = ' ';
   TC_CharP2JCharPBuf(dateTimeBuf, 23, buffer, false);
}

/**
 * Converts a short stored in a string into a short.
 *
 * @param chars The string storing a short.
 * @param error Receives <code>true</code> if an error occured during the conversion; <code>false</code>, otherwise.
 * @return The short if the convertion succeeds.
 */
int32 str2short(CharP chars, bool* error)
{
   TRACE("str2short")
   int32 value = TC_str2int(chars, error);
   
   // juliana@227_18: corrected a possible insertion of a negative short column being recovered in the select as positive.
   // juliana@225_15: when using short values, if it is out of range an exception must be thrown.
   if (value < MIN_SHORT_VALUE || value > MAX_SHORT_VALUE)
      *error = true;
      
   return value;
}

/**
 * Converts a float stored in a string into a float.
 *
 * @param chars The string storing a float.
 * @param error Receives <code>true</code> if an error occured during the conversion; <code>false</code>, otherwise.
 * @return The float if the convertion succeeds.
 */
float str2float(CharP chars, bool* error)
{
   TRACE("str2float")
   float value = (float)TC_str2double(chars, error);
	
   if ((value = (value < 0)? - value : value) && (value < MIN_FLOAT_VALUE || value > MAX_FLOAT_VALUE))
      *error = true;
   
   return value;
}

/**
 * Creates and sets a date object fields using a date stored in a int.
 *
 * @param p->retO receives The date object to be set.
 * @param date The date as an int in the format YYYYMMAA.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setDateObject(NMParams params, int32 date)
{
   TCObject object = params->retO = TC_createObject(params->currentContext, "totalcross.util.Date");
      
   if (object)
   {
      TC_setObjectLock(object, UNLOCKED);
      
      // Sets the Date object.
      FIELD_I32(object, 0) = date % 100;
      FIELD_I32(object, 1) = (date /= 100) % 100;
      FIELD_I32(object, 2) = date / 100;
      
      return true;
   }
   return false;
}

/**
 * Creates and sets a time object fields using a date and a time stored in two ints.
 *
 * @param p->retO Receives the time object to be set.
 * @param date The date stored into a int in the format YYYYMMAA.
 * @param time The time stored into a int in the format HHMMSSmmm.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setTimeObject(NMParams params, int32 date, int32 time)
{
   TCObject object = params->retO = TC_createObject(params->currentContext, "totalcross.sys.Time");
   
   if (object)
   {
      TC_setObjectLock(object, UNLOCKED);
      
      // Sets the date part of the Time object.
      Time_day(object) = date % 100;
      Time_month(object) = (date /= 100) % 100;
      Time_year(object) = date / 100;

      // Sets the time part of the Time object.
      Time_millis(object) = time % 1000;
      Time_second(object) = (time /= 1000) % 100;
      Time_minute(object) = (time /= 100) % 100;
      Time_hour(object) = (time / 100) % 100;
      
      return true;
   }
   return false;
}

/** 
 * Creates a new hash table for the temporary tables size statistics. 
 * 
 * @param count The initial size. 
 * @return A hash table for the temporary tables size statistics.
 */
MemoryUsageHT muNew(int32 count)
{
   MemoryUsageHT table;
   
   table.size = 0;
   table.items = (MemoryUsageEntry**)xmalloc(count * TSIZE);
   table.hash  = (table.threshold = count) - 1;
   
   return table;
}

/** 
 * Gets the stored statistics item with the given key.
 *
 * @param table A hash table for the temporary tables size statistics.
 * @param key The hash key.
 * @param dbSize Receives the stored .db file size.
 * @param dboSize Receives the stored .dbo file size.
 * @return <code>true</code> if there are statistics stored for the given select hash code; <code>false</code>, otherwise. 
 */
bool muGet(MemoryUsageHT* table, int32 key, int32* dbSize, int32* dboSize)
{
   if (table->items && table->size > 0) // guich@tc113_14: check size
   {
      int32 index = key & table->hash;
      MemoryUsageEntry* entry = table->items[index];
      
      while (entry)
      { 
         if (entry->key == key)
         {
            *dbSize = entry->dbSize;
            *dboSize = entry->dboSize;
            return true;
         }
         entry = entry->next;
      }
   }
   return false;
}

/**
 * Once the number of elements gets above the load factor, rehashes the hash table.
 *
 * @param table A hash table for the temporary tables size statistics.
 * @return <code>true</code> if there is enough memory to rehashes the table; <code>false</code>, otherwise. 
 */
bool muRehash(MemoryUsageHT* table)
{
   int32 oldCapacity = table->hash + 1, 
         i = oldCapacity, 
         index,
         newCapacity = oldCapacity << 1; 
   MemoryUsageEntry** oldTable = table->items;
   MemoryUsageEntry** newTable = (MemoryUsageEntry **)xmalloc(TSIZE * newCapacity);
   MemoryUsageEntry* entry;
   MemoryUsageEntry* old;
  
   if (!newTable)
      return false;
      
   table->threshold = newCapacity * 75 / 100;
   table->items = newTable;
   table->hash = newCapacity - 1;

   while (i-- > 0)
   {
      old = oldTable[i];
      while ((entry = old))
      {
         old = old->next;
         entry->next = newTable[index = entry->key & table->hash];
         newTable[index] = entry;
      }
   }
   xfree(oldTable);

   return true;
}

/** 
 * Puts the given pair of key/values in the hash table. If the key already exists, the value will be replaced.
 *
 * @param table A hash table for the temporary tables size statistics.
 * @param key The hash key.
 * @param dbSize The .db file size to be stored.
 * @param dboSize The .dbo file size to be stored.
 * @return <code>true</code> if its is not possible to store a new element; <code>false</code>, otherwise. 
 */
bool muPut(MemoryUsageHT* table, int32 key, int32 dbSize, int32 dboSize)
{  
   int32 index = key & table->hash;
   MemoryUsageEntry* entry = table->items[index];
   if (table->size > 0) // Only searchs in non-empty hash tables.
   {
      while (entry) // Makes sure the key is not already in the hashtable.
      { 
         if (entry->key == key)
         {
            entry->dbSize = dbSize;
            entry->dboSize = dboSize;
            return true;
         }
         entry = entry->next;
      }
   }
   if (table->size >= table->threshold) // Rehashs the table if the threshold is exceeded.
   {      
      muRehash(table);
      index = key & table->hash;
   }

   if (!(entry = (MemoryUsageEntry*)xmalloc(sizeof(MemoryUsageEntry)))) // Creates the new entry.
      return false;
   entry->key = key;
   entry->dbSize = dbSize;
   entry->dboSize = dboSize;
   entry->next = table->items[index];
   table->items[index] = entry;
   table->size++;
   return true;
}

/** 
 * Frees the hashtable. 
 *
 * @param iht A hash table for the temporary tables size statistics.
 */
void muFree(MemoryUsageHT* table)
{
   MemoryUsageEntry** tab = table->items;
   MemoryUsageEntry* entry;
   MemoryUsageEntry* next;
   int32 n = table->hash;
   
   if (!tab)
      return;
   while (n-- >= 0)
   {
      entry = *tab++;
      while (entry)
      {
         next = entry->next;
         xfree(entry);
         entry = next;
      }
   }
   xfree(table->items);
   table->size = 0;
}

/**
 * Indicates if a buffer is only composed by zeros or not.
 * 
 * @param buffer The buffer.
 * @param length The size of the buffer.
 * @return <code>true</code> if the buffer is only composed by zeros; <code>false</code>, otherwise.
 */
bool isZero(uint8* buffer, int32 length)
{
   while (--length >= 0)
      if (buffer[length])
         return false;
   return true;
}
