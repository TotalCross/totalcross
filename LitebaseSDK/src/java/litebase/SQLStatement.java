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

import totalcross.sys.InvalidNumberException;
import totalcross.util.InvalidDateException;

/**
 * Internal use only. Abstract class for SQL statements.
 */
abstract class SQLStatement
{
   /**
    * The type of the statement, which can be one of: <b><code>CMD_CREATE_TABLE</b></code>, <b><code>CMD_CREATE_INDEX</b></code>, 
    * <b><code>CMD_DROP_TABLE</b></code>, <b><code>CMD_DROP_INDEX</b></code>, <b><code>CMD_ALTER_DROP_PK</b></code>, 
    * <b><code>CMD_ALTER_ADD_PK</b></code>, <b><code>CMD_ALTER_RENAME_TABLE</b></code>, <b><code>CMD_ALTER_RENAME_COLUMN</b></code>, 
    * <b><code>CMD_SELECT</b></code>, <b><code>CMD_INSERT</b></code>, <b><code>CMD_UPDATE</b></code>, or <b><code>CMD_DELETE</b></code>.
    */
   int type;

   /**
    * Sets a short value at the parameter of the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the parameter.
    */
   abstract void setParamValue(int index, short val);

   /**
    * Sets an integer value at the parameter of the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the parameter.
    */
   abstract void setParamValue(int index, int val);

   /**
    * Sets a long value at the parameter of the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the parameter.
    */
   abstract void setParamValue(int index, long val);

   /**
    * Sets a float value at the parameter of the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the parameter.
    */
   abstract void setParamValue(int index, float val); 

   /**
    * Sets a double value at the parameter of the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the parameter.
    */
   abstract void setParamValue(int index, double val);

   /**
    * Sets a string value at the parameter of the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the parameter.
    * @throws InvalidNumberException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   abstract void setParamValue(int index, String val) throws InvalidNumberException, InvalidDateException;

   /**
    * Sets a byte array value (blob) at the parameter of the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the parameter.
    */
   abstract void setParamValue(int index, byte[] value); 

   /**
    * Sets null in a given field. 
    *
    * @param index The index of the parameter value to be set as null, starting from 0.
    */
   abstract void setNull(int index);
   
   /**
    * Clears the parameter values.
    */
   abstract void clearParamValues();

   /**
    * Checks if all the parameters values are defined.
    */
   abstract void allParamValuesDefined();
}
