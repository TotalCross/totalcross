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



package litebase;

/**
 * Internal use only. Represents a field of a <code>ResultSet</code>.
 */
class SQLResultSetField
{
   /**
    * The name of the column that this field represents in the underlying table. For virtual fields, this value equals <code>null</code>.
    */
   String tableColName;

   /** 
    * The column name hash code. 
    */
   int tableColHashCode;

   /**
    * The index of the column that this field represents in the underlying table. For virtual fields, this value equals -1.
    */
   int tableColIndex = -1;

   /**
    * Indicates if this is a wildcard field.
    */
   boolean isWildcard;

   /**
    * Indicates if the field represents a virtual column (not mapped directly to the underlying table).
    */
   boolean isVirtual;

   /**
    * The field alias.
    */
   String alias;

   /**
    * The field alias hash code.
    */
   int aliasHashCode;

   /**
    * The sql function that this field represents.
    */
   int sqlFunction = SQLElement.FUNCTION_AGG_NONE;

   /**
    * The data type.
    */
   int dataType;

   /**
    * The size of the field; only used in chars types and blob.
    */
   int size;

   /**
    * Indicates if the function is an aggregated function.
    */
   boolean isAggregatedFunction;

   /**
    * The parameter of the function.<br>
    * <i>Note:</i> It is declared as <code>ResultSetField</code> to allow nested function calls in the future.
    */
   SQLResultSetField parameter;

   /**
    * Indicates if the result is to be shown in ascending or decreasing order for fields from order by clause.
    */
   boolean isAscending = true;

   /**
    * Indicates if the function is a data type function.
    */
   boolean isDataTypeFunction;

   /**
    * Indicates the table name it belongs. The parser sets its value. e. g.<br>
    * <code>select person.age from test</code>: tableName = person.
    */
   String tableName;

   /**
    * Indicates what table it belongs.
    */
   Table table;

   /**
    * Indicates what resultset it belongs.
    */
   int indexRs;
}