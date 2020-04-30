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

import totalcross.util.*;

/**
 * Internal use only. Represents a SQL column list clause, like order by or group by. <BR>
 * <i>Note:</i> The select clause has a different class <code>SQLSelectClause</code>, since it has a different complexity.
 */
class SQLColumnListClause
{
   /**
    * The column field list.
    */
   SQLResultSetField[] fieldList = new SQLResultSetField[SQLElement.MAX_NUM_COLUMNS];

   /**
    * The number of fields.
    */
   int fieldsCount;
   
   /**
    * Indicates the index to use when doing a sort operation.
    */
   int index = -1; // juliana@230_29: order by and group by now use indices on simple queries.
   
   /**
    * Indicates that the index to be used is composed or not.
    */
   boolean isComposed; // juliana@230_29: order by and group by now use indices on simple queries.

   // juliana@266_1: corrected a possible AIOOBE when doing a order by or group by in a prepared statement in a table with more than 128 columns 
   // on Java and BlackBerry.
   /**
    * Backup for the tableColIndexes, used in prepared statements.
    */
   short[] fieldTableColIndexesBak; // guich@554_37

   /**
    * Compares two SQL column lists clauses. They can only be considered equal if they list the same column list in the same sequence.
    *
    * @param columnListClause the <code>SQLColumnListClause</code> to compare against.
    * @throws SQLParseException If both column lists do not list the same column sequence.
    */
   public void checkEquality(SQLColumnListClause columnListClause) throws SQLParseException
   {  
      int len = fieldsCount; // the length of the first column list.

      if (len != columnListClause.fieldsCount) // If the length of the column lists are different, the lists are different.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_ORDER_GROUPBY_MUST_MATCH));

      SQLResultSetField[] fieldList2 = columnListClause.fieldList;

      // If a field of one list has an index of a column different of the same member of the other list or they don't match concerning the ordering 
      // of the result, the lists are considered to be different.
      while (--len >= 0)
         if (fieldList[len].tableColIndex != fieldList2[len].tableColIndex)
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_ORDER_GROUPBY_MUST_MATCH));
   }
   
   /**
    * Checks if the column list contains the given column.
    *
    * @param colIndex The column index of the column being searched for.
    * @throws SQLParseException If the column is not in the column list clause.
    */
   void sqlcolumnlistclauseContains(int colIndex) throws SQLParseException
   {
      SQLResultSetField[] list = fieldList;
      int i = fieldsCount;
      while (--i >= 0)
         if (list[i].tableColIndex == colIndex)
            return;
      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_AGGREG_FUNCTION_ISNOT_ON_SELECT));
   }

   /** 
    * Binds the column information of the underlying order or group by clause to the select clause. 
    *
    * @param names2Index The select clause columns hash table.
    * @param columTypes The select clause tables column types.
    * @param tableList The select clause tables.
    * @throws SQLParseException If the column in a group or order by clause is not in the select clause or there is a column of type blob in the 
    * clause.
    */
   void bindColumnsSQLColumnListClause(IntHashtable names2Index, byte[] columnTypes, SQLResultSetTable[] tableList) throws SQLParseException
   {
      int i = fieldsCount,
          index;
      SQLResultSetField field;
      
      if (tableList == null) // Bind during a sorting.
         while (--i >= 0)
         {
            field = fieldList[i];
            field.dataType = columnTypes[field.tableColIndex = names2Index.get(field.tableColHashCode, -1)];
         }
      else // Bind before executing the query.  
      {
         int j,
         n = tableList.length;
         
         while (--i >= 0)
         {
            field = fieldList[i];
            index = -1;
            j = n;
            while (--j >= 0)
            {
               index = tableList[j].table.htName2index.get(field.tableColHashCode, -1);
               if (field.alias.indexOf('.') > 0 && !field.alias.startsWith(tableList[j].aliasTableName))
                  index = -1;
               if (index != -1)
               {  
                  field.table = tableList[j].table; // juliana@230_29
                  break;
               }
            }
            
            if (index == -1)
               throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_UNKNOWN_COLUMN) + field.tableColName);
   
            field.dataType = columnTypes[field.tableColIndex = index];
            if (field.dataType == SQLElement.BLOB)
               throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_BLOB_ORDER_GROUP));
         }
         
      }
      
   }
   
   // juliana@230_29: order by and group by now use indices on simple queries.
   /**
    * Finds the best index to use in a sort operation.
    */
   void findSortIndex()
   {
      int length = fieldsCount;
      SQLResultSetField[] fields = fieldList;
      SQLResultSetField field = fields[0];
      Table table = field.table;

      if (length == 1)
      {
         // To use an index for ordering, it must use only non-null columns because Litebase indices don't store nulls.
         // If there is only one field and it is a primary key, uses the primary key index (it is not null).
         if (table.primaryKeyCol == field.tableColIndex)
         {
            index = field.tableColIndex;
            isComposed = false;
         }
         
         // If it is another not null field, try to find an index for it.
         else if ((table.columnAttrs[field.tableColIndex] & Utils.ATTR_COLUMN_IS_NOT_NULL) != 0)
         {
            field.findMaxMinIndex();
            index = field.index;
            isComposed = field.isComposed;
         }
      }
      else
      {
         boolean isAscending = field.isAscending,
                 areAllNotNull = true;
         
         while (--length > 0)
         {
            // All the fields to be sorted must have the same table and ordering.
            if ((field = fields[length]).isAscending != isAscending || field.table != table)
               return;
            
            // To use an index for ordering, it must use only non-null columns because Litebase indices don't store nulls.
            if ((table.columnAttrs[field.tableColIndex] & Utils.ATTR_COLUMN_IS_NOT_NULL) == 0)
               areAllNotNull = false;
         }
         
         int i = -1;
         byte[] composedPKCols = table.composedPrimaryKeyCols;
         
         // Checks if the fields to be sorted are the first part of the composed PK.
         if (table.numberComposedPKCols >= (length = fieldsCount))
         {
            while (++i < length)
               if (composedPKCols[i] != fields[i].tableColIndex)
                  break;
            
            if (i == length) // If so, the composed PK can be used (it is not null).
            {
               index = table.composedPK;
               isComposed = true;
               return;
            }
               
         }
         
         // If the fields to be sorted are not part of a composed PK and are not all not null, does not use an index.
         if (!areAllNotNull)
            return;
         
         // If they are all not null, it is necessary to find a composed index for them.
         ComposedIndex[] compIndices = table.composedIndices;
         ComposedIndex compIndex;
         int j;
         
         i = table.numberComposedIndices;
         while (--i >= 0)
         {
            // Checks if the fields to be sorted are the first part of the composed index.
            if ((compIndex = compIndices[i]).columns.length >= length)
            {
               j = -1;
               while (++j < length)
                  if (compIndex.columns[j] != fields[j].tableColIndex)
                     break;
               
               if (j == length) // If so, the composed PK can be used (it is not null).
               {
                  index = i;
                  isComposed = true;
                  return;
               }
            }
         }
      }
   }
}
