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
 * Internal use only. Represents a field of a <code>ResultSet</code>.
 */
class SQLResultSetField
{
   /** 
    * The column name hash code. 
    */
   int tableColHashCode;
   
   /**
    * The field alias hash code.
    */
   int aliasHashCode;

   /**
    * The index of the column that this field represents in the underlying table. For virtual fields, this value equals -1.
    */
   int tableColIndex = -1;
   
   /**
    * Indicates what resultset it belongs.
    */
   int indexRs;

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
    * Indicates the index to use when doing a max() or min() operation.
    */
   int index = -1; // juliana@230_21: MAX() and MIN() now use indices on simple queries.
   
   /**
    * Indicates that the index to be used is composed or not.
    */
   boolean isComposed; // juliana@230_21: MAX() and MIN() now use indices on simple queries.
   
   /**
    * Indicates if this is a wildcard field.
    */
   boolean isWildcard;

   /**
    * Indicates if the field represents a virtual column (not mapped directly to the underlying table).
    */
   boolean isVirtual;

   /**
    * Indicates if the function is a data type function.
    */
   boolean isDataTypeFunction;
   
   /**
    * Indicates if the function is an aggregated function.
    */
   boolean isAggregatedFunction;

   /**
    * Indicates if the result is to be shown in ascending or decreasing order for fields from order by clause.
    */
   boolean isAscending = true;

   /**
    * The name of the column that this field represents in the underlying table. For virtual fields, this value equals <code>null</code>.
    */
   String tableColName;
   
   /**
    * The field alias.
    */
   String alias;
   
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
    * The parameter of the function.<br>
    * <i>Note:</i> It is declared as <code>ResultSetField</code> to allow nested function calls in the future.
    */
   SQLResultSetField parameter;
   
   // juliana@230_21: MAX() and MIN() now use indices on simple queries.
   /**
    * Finds the best index to use in a min() or max() operation.
    */
   void findMaxMinIndex()
   {
      Table tableAux = table;
      int column = parameter != null? parameter.tableColIndex : tableColIndex, // juliana@230_29
      i = tableAux.numberComposedIndices;
      ComposedIndex[] composedIndices = tableAux.composedIndices;
      
      if (tableAux.columnIndices[column] != null) // If the field has a simple index, uses it.
      {
         index = column;
         isComposed = false;
      }
      else 
         while (--i >= 0)
            if (composedIndices[i].columns[0] == column) // Else, if the field is the first field of a composed index, uses it.
            {
               index = i;
               isComposed = true;
               break;
            }
   }
}