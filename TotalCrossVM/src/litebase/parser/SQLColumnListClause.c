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
 * Defines functions to deal with a SQL column list clause, like order by or group by.
 */

#include "SQLColumnListClause.h"

/**
 * Compares two SQL column lists clauses. They can only be considered equal if they list the same column list in the same sequence.
 *
 * @param clause1 The first list used in the comparison.
 * @param clause1 The second list used in the comparison.
 * @return <code>true</code>, if both column lists list the same column sequence; <code>false</code>, otherwise.
 */
bool sqlcolumnlistclauseEquals(SQLColumnListClause* clause1, SQLColumnListClause* clause2)
{
   TRACE("sqlcolumnlistclauseEquals")
	int32 length = clause1->fieldsCount; // the length of the first column list.
   SQLResultSetField** fieldList1 = clause1->fieldList;
   SQLResultSetField** fieldList2 = clause2->fieldList;

   if (length != clause2->fieldsCount) // If the length of the column lists are different, the lists are different.
      return false;

   // If a field of one list has an index of a column different of the same member of the other list or they don�t match concerning the ordering 
   // of the result, the lists are considered to be different.
   while (--length >= 0)
      if ((*fieldList1++)->tableColIndex != (*fieldList2++)->tableColIndex)
         return false;
   return true;
}

/**
 * Checks if the column list contains the given column.
 *
 * @param clause The column list clause.
 * @param colIndex The column index of the column being searched for.
 * @return <code>true</code> if the column is in the column list clause; <code>false</code>, otherwise.
 */
bool sqlcolumnlistclauseContains(SQLColumnListClause* clause, int32 colIndex)
{
	TRACE("sqlcolumnlistclauseContains")
   int32 i = clause->fieldsCount;
   SQLResultSetField** fieldList = clause->fieldList;
   
   while (--i >= 0)
      if ((*fieldList++)->tableColIndex == colIndex)
         return true;

   return false;
}

/** 
 * Binds the column information of the underlying order or group by clause to the select clause. 
 *
 * @param context The thread context where the function is being executed.
 * @param clause The column list clause.
 * @param names2Index The select clause columns hash table.
 * @param columTypes The select clause tables column types.
 * @param tableList The select clause tables.
 * @param tableListSize The number of tables of the select clause.
 * @throws SQLParseException If the column in a group or order by clause is not in the select clause or there is a column of type blob in the 
 * clause.
 */
bool bindColumnsSQLColumnListClause(Context context, SQLColumnListClause* clause, Hashtable* names2Index, int8* columnTypes, 
                                                                                  SQLResultSetTable** tableList, int32 tableListSize)
{
	TRACE("bindColumnsSQLColumnListClause")
   int32 i = clause->fieldsCount,  
         index = -1;
   SQLResultSetField** fieldList = clause->fieldList;
   SQLResultSetField* field;

   if (tableList) // Binds before executing the query.  
   {
      SQLResultSetTable* rsTable;
      int32 j;

      while (--i >= 0)
      {
         field = fieldList[i];
         j = tableListSize;

         while (--j >=0)
         {
            index = TC_htGet32Inv(&(rsTable = tableList[j])->table->htName2index, field->tableColHashCode);
            if (xstrchr(field->alias, '.') && xstrncmp(field->alias, rsTable->aliasTableName, xstrlen(rsTable->aliasTableName)))
               index = -1;
            if (index >= 0) 
            {
               field->table = rsTable->table;
               break;
            }
         }

         if (index < 0)
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_UNKNOWN_COLUMN), field->tableColName);
            return false;
         }

         if ((field->dataType = (int8)columnTypes[field->tableColIndex = index]) == BLOB_TYPE)
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_BLOB_ORDER_GROUP));
            return false;
         }
      }
   }
   else // Bind during a sorting.
   {
      while (--i >= 0)
      {
         index = TC_htGet32Inv(names2Index, (field = fieldList[i])->tableColHashCode);
         field->dataType = (int8)columnTypes[field->tableColIndex = index];
      }
   }
   
   return true;
}

// juliana@230_29: order by and group by now use indices on simple queries.
/**
 * Finds the best index to use in a sort operation.
 *
 * @param clause An order or group by clause.
 */
void findSortIndex(SQLColumnListClause* clause)
{
   TRACE("findSortIndex")
   int32 length = clause->fieldsCount;
   SQLResultSetField** fieldList = clause->fieldList;
   SQLResultSetField* field = fieldList[0];
   Table* table = field->table;

   clause->index = -1;
   if (length == 1)
   {
      // To use an index for ordering, it must use only non-null columns because Litebase indices don't store nulls.
      // If there is only one field and it is a primary key, uses the primary key index (it is not null).
      if (table->primaryKeyCol == field->tableColIndex)
      {
         clause->index = field->tableColIndex;
         clause->isComposed = false;
      }
      
      // If it is another not null field, try to find an index for it.
      else if ((table->columnAttrs[field->tableColIndex] & ATTR_COLUMN_IS_NOT_NULL))
      {
         findMaxMinIndex(field);
         clause->index = field->index;
         clause->isComposed = field->isComposed;
      }
   }
   else
   {
      bool isAscending = field->isAscending,
           areAllNotNull = true;
      int32 i = -1,
            j;
      uint8* composedPKCols = table->composedPrimaryKeyCols;
      ComposedIndex** compIndices = table->composedIndexes;
      ComposedIndex* compIndex;
      
      while (--length > 0)
      {
         // All the fields to be sorted must have the same table and ordering.
         if ((field = fieldList[length])->isAscending != isAscending || field->table != table)
            return;
         
         // To use an index for ordering, it must use only non-null columns because Litebase indices don't store nulls.
         if ((table->columnAttrs[field->tableColIndex] & ATTR_COLUMN_IS_NOT_NULL))
            areAllNotNull = false;
      }
      
      // Checks if the fields to be sorted are the first part of the composed PK.
      if (table->numberComposedPKCols >= (length = clause->fieldsCount))
      {
         while (++i < length)
            if (composedPKCols[i] != fieldList[i]->tableColIndex)
               break;
         
         if (i == length) // If so, the composed PK can be used (it is not null).
         {
            clause->index = table->composedPK;
            clause->isComposed = true;
            return;
         }
            
      }
      
      // If the fields to be sorted are not part of a composed PK and are not all not null, does not use an index.
      if (!areAllNotNull)
         return;
      
      // If they are all not null, it is necessary to find a composed index for them.
      i = table->numberComposedIndexes;
      while (--i >= 0)
      {
         // Checks if the fields to be sorted are the first part of the composed index.
         if ((compIndex = compIndices[i])->numberColumns >= length)
         {
            j = -1;
            while (++j < length)
               if (compIndex->columns[j] != fieldList[j]->tableColIndex)
                  break;
            
            if (j == length) // If so, the composed PK can be used (it is not null).
            {
               clause->index = i;
               clause->isComposed = true;
               return;
            }
         }
      }
   }
}
