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

   // If a field of one list has an index of a column different of the same member of the other list or they don´t match concerning the ordering 
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
bool bindColumnsSQLColumnListClause(Context context, SQLColumnListClause* clause, Hashtable* names2Index, int16* columnTypes, 
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
               break;
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
