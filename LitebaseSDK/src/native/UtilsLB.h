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
 * This module declares useful functions for other Litebase modules.
 */

#ifndef LITEBASE_UTILSLB_H
#define LITEBASE_UTILSLB_H

#include "Litebase.h"

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
int32 str16CompareTo(JCharP string1, JCharP string2, int32 length1, int32 length2, bool isCaseless);

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
bool str16StartsWith(JCharP charsStr, JCharP prefixStr, int32 charsLen, int32 prefixLen, int32 srcOffset, bool isCaseless);

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
int32 str16IndexOf(JCharP charsStr, JCharP subStr, int32 charsLen, int32 subLen, bool isCaseless);

/**
 * Returns the number of days that a specific month has.
 * 
 * @param month The month number: from 1 to 12.
 * @param year The year.
 * @return The number of days that the month has, taking a possible leap year into consideration. 
 */
int32 getDaysInMonth(int32 month, int32 year);

/**  
 * Verifies if year, month, and day forms a valid date. 
 *
 * @param year The year.
 * @param month The month.
 * @param day The day.
 * @return An int of the format YYYYMMDD if the date is valid; otherwise, returns -1.
*/
int32 verifyDate(int32 year, int32 month, int32 day);

/**
 * Does a left trim in a string.
 *
 * @param chars The string to be trimed.
 * @return The string with the blanks in the beggining trimmed.
 */
CharP strLeftTrim(CharP chars);

/**
 * Does a left and right trim in a string.
 *
 * @param chars The string to be trimmed.
 * @return The string with the blanks in the beggining and in the end trimmed.
 */
CharP strTrim(CharP chars);

/**
 * Does a left and right trim in a unicode string.
 *
 * @param string16Str The string to be trimmed.
 * @param string16Len The length of the string to be trimmed, which is updated to return the length of the string trimmed.
 * @return The string with blanks in the beggining and in the end trimmed.
 */
JCharP str16Trim(JCharP string16Str, int32* string16Len);

/**
 * Does a left trim in a unicode string.
 *
 * @param string16Str The string to be trimmed.
 * @param string16Len The length of the string to be trimmed, which is updated to return the length of the string trimmed.
 * @return The string with blanks in the beggining.
 */
JCharP str16LeftTrim(JCharP string16Str, int32* string16Len);

/**
 * Verifies if a string is a valid Date and transforms it into a correspondent int date. 
 *
 * @param chars A string in a date format.
 * @returns A correspondent int datetime or -1 if the date is invalid.
 */
int32 testAndPrepareDate(CharP chars);

/**
 * Verifies if a string is a valid Time and transforms it into a correspondent int datetime. The time ranges from 00:00:00:000 to 23:59:59:9999 (it 
 * accepts dots and colons). This method is very flexible. For instance: 2:-:8:10 is the same as 2:0:8:10 and returns 20008010; 02:.:8:1 is the same 
 * as 02:0.0:8 and returns 20000008; :4:8:19 is the same as 0:4:8:19 and returns; 408019 2.4.a.876 is the same as 2.4.0.876 and returns 20400876.
 * 
 * @param chars A string in a time format.
 * @returns A correspondent int datetime or -1 if the value is not a valid time.
 */
int32 testAndPrepareTime(CharP chars);

/**
 * Creates an <code>IntVector</code> with the given initial capacity.
 *
 * @param context The thread context where the function is being executed.
 * @param count The <code>IntVector</code> initial capacity.
 * @param heap A heap to allocate the <code>IntVector</code>. If it is null, <code>xmalloc</code> is used and its array must be verified. 
 * @throws OutOfMemoryError If there is not enougth memory allocate memory.
 */
IntVector newIntVector(Context context, int32 count, Heap heap);

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
bool IntVectorAdd(Context context, IntVector* intVector, int32 value);

/**
 * Transforms the <code>IntVector</code> into an integer array when is necessary to create a copy of it.
 *
 * @param The <code>IntVector</code> whose array will be copied.
 * @param heap The heap to allocate the array.
 * @return The integer array.
 */
int32* intVector2Array(IntVector* intVector, Heap heap);

/**
 * Creates an <code>IntVector</code> with a <code>Hashtable</code> items.
 *
 * @param table The <code>Hashtable</code>.
 * @param heap A heap to allocate the <code>IntVector</code> integer array, which can't be null.
 * @return The <code>IntVector</code> with the <code>Hashtable</code> items.
 */
IntVector htGetKeys(Hashtable* table, Heap heap);

/**
 * Creates an empty full <code>IntVector</code>.
 *
 * @param count The size of the <code>IntVector</code>, which can't be null.
 * @param heap A heap to allocate the <code>IntVector</code> integer array.
 * @return The <code>IntVector</code>. 
 */
IntVector newIntBits(int32 count, Heap heap);

/**
 * Finds the next bit set from an b-tree.
 *
 * @param intVector The <code>IntVector</code> with the index bitmap.
 * @param start The first value to search.
 * @return The position of the next bit set.
 */
int32 findNextBitSet(IntVector* intVector, int32 start);

/**
 * Finds the previous bit set from an b-tree.
 *
 * @param intVector The <code>IntVector</code> with the index bitmap.
 * @param start The first value to search.
 * @return The position of the previous bit set.
 */
int32 findPrevBitSet(IntVector* intVector, int32 start);

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
                                                                                                   SQLResultSetField** sortFieldList);

/** 
 * Sets and resets one bit in an array of bytes.
 *
 * @param items The array of bytes
 * @param index The bit index to be set or reset.
 * @param isOn A bool that defines whether the bit will be set or reset.
 */
void setBit(uint8 *items, int32 index, bool isOn);

/**
 * Gets the full name of a file: path + file name.
 * 
 * @param fileName The file name.
 * @param sourcePath The path where the table is stored.
 * @param buffer Receives path + file name with a path separator if necessary.
 */
void getFullFileName(CharP fileName, CharP sourcePath, TCHARP buffer);

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
int64 getTimeLong(int32 year, int32 month, int32 day, int32 hour, int32 minute, int32 second);

/**
 * Checks if a unicode string starts with a substring in the ascii format.
 *
 * @param unicodeStr The unicode string.
 * @param asciiStr The ascii string.
 * @param unicodeLen The unicode string length.
 * @param asciiLen The ascii string length.
 * @return <code>true</code> if the unicode string starts with the ascii string; <code>false</code>, otherwise.
 */
bool JCharPStartsWithCharP(JCharP unicodeStr, CharP asciiStr, int32 unicodeLen, int32 asciiLen);

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
bool JCharPEqualsCharP(JCharP unicodeStr, CharP asciiStr, int32 unicodeLen, int32 asciiLen, bool ignoreCase);

/**
 * Gets the current path used by the system to store application files.
 *
 * @param sourcePath The path used by the system to store application files.
 */ 
void getCurrentPath(CharP sourcePath);

#endif
