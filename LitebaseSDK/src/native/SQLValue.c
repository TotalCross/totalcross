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
 * Defines functions to deal with a a value which can be inserted in a column of a table.
 */

#include "SQLValue.h"

/**
 * Creates an array of <code>SQLValue</code>s.
 * 
 * @param count The array size.
 * @param heap The heap to allocate the array.
 * @return The <code>SQLValue</code> array.
 */
SQLValue** newSQLValues(int32 count, Heap heap)
{
	TRACE("newSQLValues")
   SQLValue** values;

   values = (SQLValue**)TC_heapAlloc(heap, count * TSIZE);
   while (--count >= 0)
      values[count] = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));
   return values;
}

// rnovais@568_10 rnovais@570_5
// null values must be handled before. If the value is null, this function can't be called.
/**
 * Applies the function on the value. 
 * 
 * @param value The value where the function will be applied.
 * @param sqlFunction The code of the function to be applied.
 * @param paramDataType The data type of the parameter.
 */
void applyDataTypeFunction(SQLValue* value, int32 sqlFunction, int32 paramDataType)
{
	TRACE("applyDataTypeFunction")
   switch (sqlFunction)
   {
      case FUNCTION_DT_YEAR:
         value->asShort = (int16)(value->asInt / 10000);
         break;
      case FUNCTION_DT_MONTH:
         value->asShort = (int16)(value->asInt / 100 % 100);
         break;
      case FUNCTION_DT_DAY:
         value->asShort = (int16)(value->asInt % 100);
         break;
      case FUNCTION_DT_HOUR:
         value->asShort = (int16)(value->asTime / 10000000);
         break;
      case FUNCTION_DT_MINUTE:
         value->asShort = (int16)(value->asTime / 100000 % 100);
         break;
      case FUNCTION_DT_SECOND:
         value->asShort = (int16)(value->asTime / 1000 % 100);
         break;
      case FUNCTION_DT_MILLIS:
         value->asShort = (int16)(value->asTime % 1000);
         break;
      case FUNCTION_DT_ABS: // rnovais@570_1
         switch (paramDataType) // rnovais@570_5
         {
            case SHORT_TYPE:
               if (value->asShort < 0)
                  value->asShort = -value->asShort;
               break;
            case INT_TYPE:
               if (value->asInt < 0)
                  value->asInt = -value->asInt;
               break;
            case LONG_TYPE:
               if (value->asLong < 0)
                  value->asLong = -value->asLong;
               break;
            case FLOAT_TYPE:
               if (value->asFloat < 0.0F)
                  value->asFloat = -value->asFloat;
               break;
            case DOUBLE_TYPE:
               if (value->asDouble < 0.0)
                  value->asDouble = -value->asDouble;
               break;
         }
         break;
      case FUNCTION_DT_UPPER: // rnovais@570_1
      {
         int32 length = value->length;
         JChar* asChars = value->asChars;
         while (--length >= 0)
         {
            *asChars = TC_JCharToUpper(*asChars);
            asChars++;
         }
         break;
      }
      case FUNCTION_DT_LOWER: // rnovais@570_1
      {
         int32 length = value->length;
         JChar* asChars = value->asChars;
         while (--length >= 0)
         {
            *asChars = TC_JCharToLower(*asChars);
            asChars++;
         }
      }
   }
}

// juliana@253_5: removed .idr files from all indices and changed its format. 
/**
 * Compares 2 values.
 *
 * @param context The thread context where the function is being executed. 
 * @param value1 The fist value used in the comparison.
 * @param value1 The second value used in the comparison.
 * @param type The types of the values being compared.
 * @param isNull1 Indicates if the value being compared is null.
 * @param isNull2 Indicates if the value being compared against is null.
 * @param plainDB the plainDB of a table if it is necessary to load a string.
 * @return 0 if the values are identical; a positive number if the value being compared is greater than the one being compared against; otherwise,
 * a negative number.
 */
int32 valueCompareTo(Context context, SQLValue* value1, SQLValue* value2, int32 type, bool isNull1, bool isNull2, PlainDB* plainDB)
{
	TRACE("valueCompareTo")
  
   if (isNull1 || isNull2) // A null value is always considered to be the greatest value.
      return (isNull1 == isNull2)? 0 : (isNull1? 1 : -1); 

   switch (type)
   {
      case CHARS_NOCASE_TYPE:
      case CHARS_TYPE: 
         if (!value2->length && plainDB)
         {
            int32 length = 0;
         
            nfSetPos(&plainDB->dbo, value2->asInt);
            if (!nfReadBytes(context, &plainDB->dbo, (uint8*)&length, 2) || !loadString(context, plainDB, value2->asChars, value2->length = length))
               return false;
            value2->asChars[length] = 0;
         }
         return str16CompareTo(value1->asChars, value2->asChars, value1->length, value2->length, type == CHARS_NOCASE_TYPE);
      
      case SHORT_TYPE: 
         return value1->asShort - value2->asShort;
      case DATE_TYPE: // rnovais@567_2
      case INT_TYPE: 
         return value1->asInt - value2->asInt;

      case LONG_TYPE:
      {
         int64 ret = value1->asLong - value2->asLong;
         return (ret == 0)? 0 : (ret > 0)? 1 : -1;
      }
      case FLOAT_TYPE :
      {
         float ret = value1->asFloat - value2->asFloat;
         return (ret == 0.0)? 0 : (ret > 0)? 1 : -1;
      }
      case DOUBLE_TYPE:
      {
         double ret = value1->asDouble - value2->asDouble;
         return (ret == 0.0)? 0 : (ret > 0)? 1 : -1;
      }
      case DATETIME_TYPE: // rnovais@567_2 rnovais@570_10
      {
         int32 ret = value1->asDate - value2->asDate;
         return (ret == 0)? value1->asTime - value2->asTime : ret;
      }
   }
   return 0;
}

#ifdef ENABLE_TEST_SUITE

/**
 * Checks if <code>applyDataTypeFunction()<code> correctly applies the data type functions.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(applyDataTypeFunction)
{
   SQLValue value;
   int32 hour = 0,
         minute = 0,
         second = 0,
         time,
         day = 1,
         month = 1,
         year = 1000,
         date;
   bool changed = false;
   int64 asLong;
   double asDouble;
   char bufferChar[27];
   JChar bufferJChar[27];
   UNUSED(currentContext)

   while (hour < 24) // Tests time functions.
   {
      getNextTime(&hour, &minute, &second);
      
      // hour
      value.asTime = time = 1000 * (int32)getTimeLong(0, 0, 0, hour, minute, second);
      applyDataTypeFunction(&value, FUNCTION_DT_HOUR, -1);
      ASSERT2_EQUALS(I32, hour, value.asShort);

      // minute
      value.asTime = time;
      applyDataTypeFunction(&value, FUNCTION_DT_MINUTE, -1);
      ASSERT2_EQUALS(I32, minute, value.asShort);

      // second
      value.asTime = time;
      applyDataTypeFunction(&value, FUNCTION_DT_SECOND, -1);
      ASSERT2_EQUALS(I32, second, value.asShort);

      // millis
      value.asTime = time;
      applyDataTypeFunction(&value, FUNCTION_DT_MILLIS, -1);
      ASSERT2_EQUALS(I32, 0, value.asShort);
   }

   while (year < 3000) // Tests date functions.
   {
      getNextDate(&year, &month, &day);
      
      // year
      value.asInt = date = year * 10000 + month * 100 + day;
      applyDataTypeFunction(&value, FUNCTION_DT_YEAR, -1);
      ASSERT2_EQUALS(I32, year, value.asShort); 

      // month
      value.asInt = date;
      applyDataTypeFunction(&value, FUNCTION_DT_MONTH, -1);
      ASSERT2_EQUALS(I32, month, value.asShort); 

      // day
      value.asInt = date;
      applyDataTypeFunction(&value, FUNCTION_DT_DAY, -1);
      ASSERT2_EQUALS(I32, day, value.asShort); 
   }

   // Tests ABS(short).
   date = -32768;
   while (date++ < 32767)
   {
      value.asShort = date;
      applyDataTypeFunction(&value, FUNCTION_DT_ABS, SHORT_TYPE);
      ASSERT2_EQUALS(I32, date < 0? -date : date, value.asShort);
   }

   // Tests ABS(int).
   date = -2147483646;
   while ((date += 131072) < 2147483646)
   {
      if (date > 0)
         changed = true;
      if (changed && date < 0)
         break;
      value.asInt = date;
      applyDataTypeFunction(&value, FUNCTION_DT_ABS, INT_TYPE);
      ASSERT2_EQUALS(I32, date < 0? -date : date, value.asInt);
   }
   
   // Tests ABS(long).
   changed = false;
   asLong = -9223372036854775807L;
   while ((asLong += 274877906944L) < 9223372036854775807L)
   {
      if (asLong > 0)
         changed = true;
      if (changed && asLong < 0)
         break;
      value.asLong = asLong;
      applyDataTypeFunction(&value, FUNCTION_DT_ABS, LONG_TYPE);
      ASSERT2_EQUALS(I64, asLong < 0? -asLong : asLong, value.asLong);
   }

   // Tests ABS(float).
   asDouble = MIN_FLOAT_VALUE;
   while ((asDouble *= 10.0) < MAX_FLOAT_VALUE)
   {
      // positive
      value.asFloat = (float)asDouble;
      applyDataTypeFunction(&value, FUNCTION_DT_ABS, FLOAT_TYPE);
      ASSERT2_EQUALS(Dbl, (float)asDouble, value.asFloat);

      // negative
      value.asFloat = -(float)asDouble;
      applyDataTypeFunction(&value, FUNCTION_DT_ABS, FLOAT_TYPE);
      ASSERT2_EQUALS(Dbl, (float)asDouble, value.asFloat);
   }

   // Tests ABS(double).
   asDouble = 4.9E-324;
   while ((asDouble *= 10.0) < 1.7976931348623157E308)
   {
      // positive
      value.asDouble = asDouble;
      applyDataTypeFunction(&value, FUNCTION_DT_ABS, DOUBLE_TYPE);
      ASSERT2_EQUALS(Dbl, asDouble, value.asDouble);

      // negative
      value.asDouble = -asDouble;
      applyDataTypeFunction(&value, FUNCTION_DT_ABS, DOUBLE_TYPE);
      ASSERT2_EQUALS(Dbl, asDouble, value.asDouble);
   }

   // Tests UPPER() and LOWER().
   xmemzero(bufferChar, 27);
   date = 'A' - 1;
   value.asChars = bufferJChar;
   while (++date <= 'Z')
   {
      // LOWER
      xmemset(bufferChar, date, value.length = time = date - 'A' + 1);
      TC_CharP2JCharPBuf(bufferChar, time, bufferJChar, true);
      applyDataTypeFunction(&value, FUNCTION_DT_LOWER, -1);
      while (--time >= 0)
         ASSERT2_EQUALS(I32, date + 32, bufferJChar[time]);
      xmemset(bufferChar, date + 32, time = date - 'A' + 1); 
      TC_CharP2JCharPBuf(bufferChar, time, bufferJChar, true);
      applyDataTypeFunction(&value, FUNCTION_DT_LOWER, -1);
      while (--time >= 0)
         ASSERT2_EQUALS(I32, date + 32, bufferJChar[time]);

      // UPPER
      xmemset(bufferChar, date, value.length = time = date - 'A' + 1);
      TC_CharP2JCharPBuf(bufferChar, time, bufferJChar, true);
      applyDataTypeFunction(&value, FUNCTION_DT_UPPER, -1);
      while (--time >= 0)
         ASSERT2_EQUALS(I32, date, bufferJChar[time]);
      xmemset(bufferChar, date + 32, time = date - 'A' + 1); 
      TC_CharP2JCharPBuf(bufferChar, time, bufferJChar, true);
      applyDataTypeFunction(&value, FUNCTION_DT_UPPER, -1);
      while (--time >= 0)
         ASSERT2_EQUALS(I32, date, bufferJChar[time]);
   }

finish : ;
}

/**
 * Checks if <code>newSQLValues()<code> correctly creates an array of SQLValues.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(newSQLValues)
{
   Heap heap = heapCreate();
   int32 i = 256,
         j;
   SQLValue** sqlValues;
   UNUSED(currentContext)
   
   IF_HEAP_ERROR(heap)
   {
      heapDestroy(heap);
      TEST_FAIL(tc, "OutOfMemoryError");
      goto finish;
   }

   while ((i -= 4) >= 0) // Creates arrays of SQLValues of various sizes.
   {
      sqlValues = newSQLValues(j = i, heap);
      
      while (--j >= 0)
      {
         ASSERT1_EQUALS(Null, sqlValues[j]->asChars);
         ASSERT1_EQUALS(Null, sqlValues[j]->asBlob);
         ASSERT2_EQUALS(I32, 0, sqlValues[j]->length);
         ASSERT2_EQUALS(I32, 0, sqlValues[j]->isNull);
         ASSERT2_EQUALS(I16, 0, sqlValues[j]->asShort);
         ASSERT2_EQUALS(I32, 0, sqlValues[j]->asInt);
         ASSERT2_EQUALS(I64, 0, sqlValues[j]->asLong);
         ASSERT2_EQUALS(Dbl, 0, sqlValues[j]->asFloat);
         ASSERT2_EQUALS(Dbl, 0, sqlValues[j]->asDouble);
         ASSERT2_EQUALS(I32, 0, sqlValues[j]->asDate);
         ASSERT2_EQUALS(I32, 0, sqlValues[j]->asTime);
      }
   }

   heapDestroy(heap);

finish: ;
}

/**
 * Tests if <code>valueCompareTo</code> correctly compares <code>SQLValues</code>.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(valueCompareTo)
{
   SQLValue value1,
            value2;
   bool changed = false;
   int32 asInt,
         length;
   int64 asLong;
   double asDouble;
   char bufferChar[27];
   JChar bufferJChar1[27],
         bufferJChar2[27];

   // Tests when one of the values is null.
   ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, null, null, -1, true, true, null));
   ASSERT2_EQUALS(I32, 1, valueCompareTo(currentContext, null, null, -1, true, false, null));
   ASSERT2_EQUALS(I32, -1, valueCompareTo(currentContext, null, null, -1, false, true, null));

   // Tests short comparison.
   asInt = -32768;
   while (++asInt < 32767)
   {
      value1.asShort = asInt;
      ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, &value1, &value1, SHORT_TYPE, false, false, null));
      value2.asShort = asInt + 1;
      ASSERT2_EQUALS(I32, -1, valueCompareTo(currentContext, &value1, &value2, SHORT_TYPE, false, false, null));
      value2.asShort = asInt - 1;
      ASSERT2_EQUALS(I32, 1, valueCompareTo(currentContext, &value1, &value2, SHORT_TYPE, false, false, null));
   }

   // Tests int comparison.
   asInt = -2147483647;
   while ((asInt += 131072) < 2147483647)
   {
      if (asInt > 0)
         changed = true;
      if (changed && asInt < 0)
         break;
      value1.asInt = asInt;
      ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, &value1, &value1, INT_TYPE, false, false, null));
      value2.asInt = asInt + 1;
      ASSERT2_EQUALS(I32, -1, valueCompareTo(currentContext, &value1, &value2, INT_TYPE, false, false, null));
      value2.asInt = asInt - 1;
      ASSERT2_EQUALS(I32, 1, valueCompareTo(currentContext, &value1, &value2, INT_TYPE, false, false, null));
   }
   
   // Tests long comparison.
   changed = false;
   asLong = -9223372036854775807;
   while ((asLong += 274877906944L) < 9223372036854775807)
   {
      if (asLong > 0)
         changed = true;
      if (changed && asLong < 0)
         break;
      value1.asLong = asLong;
      ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, &value1, &value1, LONG_TYPE, false, false, null));
      value2.asLong = asLong + 1;
      ASSERT2_EQUALS(I32, -1, valueCompareTo(currentContext, &value1, &value2, LONG_TYPE, false, false, null));
      value2.asLong = asLong - 1;
      ASSERT2_EQUALS(I32, 1, valueCompareTo(currentContext, &value1, &value2, LONG_TYPE, false, false, null));
   }

   // Tests float comparison.
   asDouble = MIN_FLOAT_VALUE;
   while ((asDouble *= 10.0) < MAX_FLOAT_VALUE)
   {
      // positive
      value1.asFloat = (float)asDouble;
      ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, &value1, &value1, FLOAT_TYPE, false, false, null));
      value2.asFloat = (float)(asDouble * 10.0);
      ASSERT2_EQUALS(I32, -1, valueCompareTo(currentContext, &value1, &value2, FLOAT_TYPE, false, false, null));
      value2.asFloat = (float)(asDouble / 10.0);
      ASSERT2_EQUALS(I32, 1, valueCompareTo(currentContext, &value1, &value2, FLOAT_TYPE, false, false, null));
      
      // negative
      value1.asFloat = -(float)asDouble;
      ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, &value1, &value1, FLOAT_TYPE, false, false, null));
      value2.asFloat = -(float)(asDouble * 10.0);
      ASSERT2_EQUALS(I32, 1, valueCompareTo(currentContext, &value1, &value2, FLOAT_TYPE, false, false, null));
      value2.asFloat = -(float)(asDouble / 10.0);
      ASSERT2_EQUALS(I32, -1, valueCompareTo(currentContext, &value1, &value2, FLOAT_TYPE, false, false, null));
   }

   // Tests double comparison.
   asDouble = MIN_DOUBLE_VALUE;
   while ((asDouble *= 10.0) < 1.7976931348623157E308)
   {
      // positive
      value1.asDouble = asDouble;
      ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, &value1, &value1, DOUBLE_TYPE, false, false, null));
      value2.asDouble = asDouble * 10.0;
      ASSERT2_EQUALS(I32, -1, valueCompareTo(currentContext, &value1, &value2, DOUBLE_TYPE, false, false, null));
      value2.asDouble = asDouble / 10.0;
      ASSERT2_EQUALS(I32, 1, valueCompareTo(currentContext, &value1, &value2, DOUBLE_TYPE, false, false, null));
      
      // negative
      value1.asDouble = -asDouble;
      ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, &value1, &value1, DOUBLE_TYPE, false, false, null));
      value2.asDouble = -asDouble * 10.0;
      ASSERT2_EQUALS(I32, 1, valueCompareTo(currentContext, &value1, &value2, DOUBLE_TYPE, false, false, null));
      value2.asDouble = -asDouble / 10.0;
      ASSERT2_EQUALS(I32, -1, valueCompareTo(currentContext, &value1, &value2, DOUBLE_TYPE, false, false, null));
   }

   // Tests CHARS and CHARS NOCASE comparison.
   xmemzero(bufferChar, 27);
   asInt = 'A' - 1;
   value1.asChars = bufferJChar1;
   value2.asChars = bufferJChar2;
   while (++asInt <= 'Z')
   {
      xmemset(bufferChar, asInt, length = value1.length = value2.length = asInt - 'A' + 1);
      TC_CharP2JCharPBuf(bufferChar, length, bufferJChar1, true);
      TC_CharP2JCharPBuf(bufferChar, length, bufferJChar2, true);
      ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, &value1, &value2, CHARS_TYPE, false, false, null));
      ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, &value1, &value2, CHARS_NOCASE_TYPE, false, false, null));
      applyDataTypeFunction(&value2, FUNCTION_DT_LOWER, -1);
      ASSERT2_EQUALS(I32, -32, valueCompareTo(currentContext, &value1, &value2, CHARS_TYPE, false, false, null));
      ASSERT2_EQUALS(I32, +32, valueCompareTo(currentContext, &value2, &value1, CHARS_TYPE, false, false, null));
      ASSERT2_EQUALS(I32, 0, valueCompareTo(currentContext, &value1, &value2, CHARS_NOCASE_TYPE, false, false, null));
      applyDataTypeFunction(&value2, FUNCTION_DT_UPPER, -1);
      value2.length--;
      ASSERT2_EQUALS(I32, 1, valueCompareTo(currentContext, &value1, &value2, CHARS_TYPE, false, false, null));
      ASSERT2_EQUALS(I32, -1, valueCompareTo(currentContext, &value2, &value1, CHARS_TYPE, false, false, null));
      ASSERT2_EQUALS(I32, 1, valueCompareTo(currentContext, &value1, &value2, CHARS_NOCASE_TYPE, false, false, null));
      ASSERT2_EQUALS(I32, -1, valueCompareTo(currentContext, &value2, &value1, CHARS_NOCASE_TYPE, false, false, null)); 
   }

finish : ;
}

/**
 * Gets the next time instant, increasing second by one and adjusting the other time values.
 *
 * @param hour The hour.
 * @param minute The minute.
 * @param second The second.
 */
void getNextTime(int32* hour, int32* minute, int32* second)
{
   if ((*second)++ == 60)
   {
      (*second) = 0;

      if ((*minute)++ == 60)
      {
         *minute = 0;
         (*hour)++;
      }
   }
}

/**
 * Gets the next day, increasing day by one and adjusting the other date values. It takes lap years into consideration.
 *
 * @param year The year.
 * @param month The month.
 * @param day The day.
 */
void getNextDate(int32* year, int32* month, int32* day)
{
   (*day)++;
   if (monthDays[*month - 1] < *day && !(*day == 29 && *month == 2 && (*year % 400 == 0 || (*year % 100 != 0 && *year % 4 == 0))))
   {   
      *day = 1;
      if ((*month)++ > 12)
      {
         *month = 1;
         (*year)++;
      }
   }
}

#endif
