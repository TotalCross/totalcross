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

/**
 * Represents a field of a statement except for SELECTs.
 */
class SQLFieldDefinition
{
   /**
    * The name of the field.
    */
   String fieldName;

   /**
    * The type of the field. It can be: <B><code>NUMBER</code></B>, <B><code>UNDEFINED</code></B>, <B><code>CHARS</code></B>, 
    * <B><code>SHORT</code></B>, <B><code>INT</code></B>, <B><code>LONG</code></B>, <B><code>FLOAT</code></B>, <B><code>DOUBLE</code></B>, 
    * <B><code>CHARS_NOCASE</code></B>, <B><code>BOOLEAN</code></B>, <B><code>DATE</code></B>, or <B><code>DATE_TIME</code></B>.
    */
   int fieldType;

   /**
    * Only used for chars / chars no case / blob types. For other types it is equal to zero.
    */
   int fieldSize;

   /**
    * Indicates if the field is the primary key.
    */
   boolean isPrimaryKey;

   /**
    * The default value of a field can contain a string, a number or a Date/Datetime value. This must be converted to the correct type later. If 
    * <code>defaultValue</code> is <code>null</code>, the default value was not defined.
    */
   String defaultValue;

   /**
    * Defines if the field can be null or not.
    */
   boolean isNotNull;

   /**
    * Constructs a new <code>SQLFieldDefinition</code> object.
    *
    * @param aFieldName The name of the new field.
    * @param aFieldType The type of the new field.
    * @param aFieldSize The size of the new field (it is zero if the type is not <b><code>CHARS</code></b> or <b><code>CHARS_NOCASE</code></b>).
    * @param anIsPrimaryKey Indicates if the new field is the primary key.
    * @param aDefaultValue The default value of the new field (it can be null if there is no default value).
    * @param anIsNotNull Indicates if the new field can be null or not.
    */
   public SQLFieldDefinition(String aFieldName, int aFieldType, int aFieldSize, boolean anIsPrimaryKey, String aDefaultValue, boolean anIsNotNull)
   {
      fieldName = aFieldName;
      fieldType = aFieldType;
      fieldSize = aFieldSize;
      isPrimaryKey = anIsPrimaryKey;
      defaultValue = aDefaultValue;
      isNotNull = anIsNotNull;
   }
}
