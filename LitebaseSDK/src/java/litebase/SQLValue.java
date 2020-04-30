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

package litebase;

import totalcross.io.IOException;
import totalcross.sys.*;
import totalcross.util.*;

/**
 * Represents a value which can be inserted in a column of a table.
 */
class SQLValue
{
   /**
    * Represents the <code>SHORT</code> data type.
    */
   int asShort;

   /**
    * Represents the <code>INT</code> data type.
    */
   int asInt;

   /**
    * Represents the <code>LONG<code> data type.
    */
   long asLong;

   /**
    * Represents the <code>DOUBLE</code> and <code>FLOAT</code> data type.
    */
   double asDouble;

   /**
    * Represents the <code>CHARS</code>, <code>VARCHAR</code>, <code>CHARS NOCASE</code>, and <code>VARCHAR NOCASE</code> data types.
    */
   String asString;

   /**
    * Represents the <code>BLOB</code> data type.
    */
   byte[] asBlob;
   
   /** 
    * Indicates if the record is null or not.
    */
   boolean isNull;

   /**
    * Creates an array of <code>SQLValue</code>s.
    * 
    * @param count The array size.
    * @return The <code>SQLValue</code> array.
    */
   static SQLValue[] newSQLValues(int count)
   {
      SQLValue[] a = new SQLValue[count];
      while (--count >= 0)
         a[count] = new SQLValue();
      return a;
   }
   
   // juliana@230_21: MAX() and MIN() now use indices on simple queries.
   /**
    * Clones a <code>SQLValue</code> for index usage.
    * 
    * @param The cloned SQLValue.
    */
   void cloneSQLValue(SQLValue sqlValue)
   {  
      sqlValue.asDouble = asDouble;
      sqlValue.asInt = asInt;
      sqlValue.asLong = asLong;
      sqlValue.asShort = asShort;
      sqlValue.asString = asString;
      sqlValue.isNull = false;
   }

   // rnovais@568_10
   // You must to handle the null values before. If the value is null, this method can't be called.
   /**
    * Applies the function on the value. 
    * 
    * @param sqlFunction The code of the function to be applied.
    * @param paramDataType The data type of the parameter.
    */
   void applyDataTypeFunction(int sqlFunction, int paramDataType)
   {
      switch (sqlFunction)
      {
         case SQLElement.FUNCTION_DT_YEAR:   
            asShort = asInt / 10000; 
            break;
         case SQLElement.FUNCTION_DT_MONTH:  
            asShort = asInt / 100 % 100; 
            break;
         case SQLElement.FUNCTION_DT_DAY:    
            asShort = asInt % 100; 
            break;
         case SQLElement.FUNCTION_DT_HOUR:   
            asShort = asShort / 10000000; 
            break;
         case SQLElement.FUNCTION_DT_MINUTE: 
            asShort = asShort / 100000 % 100; 
            break;
         case SQLElement.FUNCTION_DT_SECOND: 
            asShort = asShort / 1000 % 100; 
            break;
         case SQLElement.FUNCTION_DT_MILLIS: 
            asShort %= 1000; 
            break;
         case SQLElement.FUNCTION_DT_ABS:
            switch (paramDataType)
            {
               case SQLElement.SHORT:
                  asShort = (asShort < 0)? -asShort : asShort;
                  break;
               case SQLElement.INT:
                  asInt = (asInt < 0)? -asInt : asInt;
                  break;
               case SQLElement.LONG:
                  asLong = (asLong < 0)? -asLong : asLong;
                  break;
               case SQLElement.FLOAT:
               case SQLElement.DOUBLE:
                  asDouble = (asDouble <= 0.0D)? - asDouble : asDouble;
            }
            break;
         case SQLElement.FUNCTION_DT_UPPER:
            asString = asString.toUpperCase();
            break;
         case SQLElement.FUNCTION_DT_LOWER:
            asString = asString.toLowerCase();
      }
   }
   
   // juliana@253_5: removed .idr files from all indices and changed its format.
   /**
    * Compares 2 values.
    *
    * @param value The value to be compared against.
    * @param type The types of the values being compared.
    * @param isNull1 Indicates if the value being compared is null.
    * @param isNull2 Indicates if the value being compared against is null.
    * @param plainDB the plainDB of a table if it is necessary to load a string.
    * @return 0 if the values are identical; a positive number if the value being compared is greater than the one being compared against; otherwise,
    * a negative number.
    * @throws IOException If an internal method throws it.
    */
   int valueCompareTo(SQLValue value, int type, boolean isNull1, boolean isNull2, PlainDB plainDB) throws IOException
   {
      if (isNull1 || isNull2) // A null value is always considered to be the greatest value.
         return (isNull1 == isNull2)? 0 : (isNull1? 1 : -1);

      switch (type)
      {
         case SQLElement.CHARS:
         case SQLElement.CHARS_NOCASE:   
            if (asString == null)
               return 0;  
            if (value.asString == null)
            {
               plainDB.dbo.setPos(value.asInt);
               value.asString = plainDB.loadString();
            }    
            if (type == SQLElement.CHARS_NOCASE)
               return asString.toLowerCase().compareTo(value.asString.toLowerCase());
            return asString.compareTo(value.asString);

         case SQLElement.SHORT:
            return asShort - value.asShort;

         case SQLElement.DATE: // rnovais@567_2
         case SQLElement.INT:
            return asInt - value.asInt;

         case SQLElement.LONG:
            long vl = asLong - value.asLong;
            return (vl == 0)? 0 : (vl > 0)? 1 : -1;

         case SQLElement.FLOAT:
         case SQLElement.DOUBLE:
            double vd = asDouble - value.asDouble;
            return (vd == 0)? 0 : (vd > 0)? 1 : -1;

         case SQLElement.DATETIME: // rnovais@_567_2 @_570_10
            int i = asInt - value.asInt;
            if (i == 0)
               i = asShort - value.asShort;
            return i;
      }
      return 0;
   }

   // rnovais@567_2
   /**
    * Validates a string value as a date or datetime according to the value type.
    * 
    * @param tempDate A temporary <code>Date</code> Object reused to save memory.
    * @param valueType The type of the value being validate.
    * @throws InvalidDateException If it is thrown by an internal method.
    */
   void validateDateTime(Date tempDate, int valueType) throws SQLParseException, InvalidDateException
   {
      if (asString.indexOf('%') == -1)
         if (valueType == SQLElement.DATE)
            asInt = tempDate.set(asString, Settings.DATE_YMD);
         else
            parseDateTime(tempDate, asString);
   }
   
   // juliana@202_2: corrected a bug that might let Litebase issue an InvalidDateException if a date was inserted in a datetime field.
   /** 
    * Parses a datetime string into the Litebase format.
    * 
    * @param tempDate A temporary <code>Date</code> Object reused to save memory.
    * @param strVal The string to be parsed.
    * @throws SQLParseException If an the string is not a valid date time.
    * @throws InvalidDateException If an internal method throws it.
    */
   void parseDateTime(Date tempDate, String strVal) throws SQLParseException, InvalidDateException
   {
      strVal = strVal.trim();
      int pos = strVal.lastIndexOf(' '); // By using lastIndexOf another trim for the time is not necessary.
      if (pos == -1) // only date?
      {
         asInt = tempDate.set(strVal, Settings.DATE_YMD);
         asShort = 0; // If it has only a date, the time part must be 0.
      }
      else
      {
         asInt = tempDate.set(strVal.substring(0, pos), Settings.DATE_YMD);
         asShort = Utils.testAndPrepareTime(strVal.substring(pos + 1)); // Gets the time part, skipping the space.
      }
   }
}
