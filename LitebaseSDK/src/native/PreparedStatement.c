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
 * Defines functions to deal with important prepared statements.
 */

#include "Litebase.h"

/**
 * Frees a prepared statement.
 *
 * @param unused Parameter for htFree().
 * @param statement The prepared statement to be freed.
 */
void freePreparedStatement(int32 unused, TCObject statement)
{
	TRACE("freePreparedStatement")
   UNUSED(unused)

   // juliana@230_19: removed some possible memory problems with prepared statements and ResultSet.getStrings().
   if (!OBJ_PreparedStatementDontFinalize(statement)) // The prepared statement shouldn't be finalized twice.
   {
      JCharP* paramsAsStrs = getPreparedStatementParamsAsStrs(statement);
      int32 numParams = OBJ_PreparedStatementStoredParams(statement);
		TCObjects* psList;
      Table* table;
      Heap heap = null;

      switch (OBJ_PreparedStatementType(statement)) // Destroy the statement.
      {
         case CMD_DELETE:
         {
            SQLDeleteStatement* deleteStmt = (SQLDeleteStatement*)getPreparedStatementStatement(statement);
				
            // Removes the prepared statement from table list.
            psList = (table = deleteStmt->rsTable->table)->preparedStmts;
				psList = TC_TCObjectsRemove(psList, statement);
				table->preparedStmts = psList;

            heap = deleteStmt->heap;
            break;
         }
         case CMD_INSERT:
         {
            SQLInsertStatement* insertStmt = (SQLInsertStatement*)getPreparedStatementStatement(statement);
				
            // Removes the prepared statement from table list.
            psList = insertStmt->table->preparedStmts;
				psList = TC_TCObjectsRemove(psList, statement);
				insertStmt->table->preparedStmts = psList;

            heap = insertStmt->heap;
            break;
         }
         case CMD_SELECT:
         {
            SQLSelectStatement* selectStmt = (SQLSelectStatement*)getPreparedStatementStatement(statement); 
            SQLSelectClause* selectClause = selectStmt->selectClause;
            SQLResultSetTable** tableList = selectClause->tableList;
            int32 i = selectClause->tableListSize;
            
            while (--i >= 0) // Removes the prepared statement from table list.
				{
					psList = (table = tableList[i]->table)->preparedStmts;
               psList = TC_TCObjectsRemove(psList, statement);
					table->preparedStmts = psList;
				}

            heap = selectClause->heap;
            break;
         }
         case CMD_UPDATE:
         {
            SQLUpdateStatement* updateStmt = (SQLUpdateStatement*)getPreparedStatementStatement(statement);
				
            // Removes the prepared statement from table list.
            psList = (table = updateStmt->rsTable->table)->preparedStmts;
				psList = TC_TCObjectsRemove(psList, statement);
				table->preparedStmts = psList;

            heap = updateStmt->heap;
         }
      }

      // Frees logger information.
      while (--numParams >= 0)
         xfree(paramsAsStrs[numParams]);
       
      heapDestroy(heap);
	   OBJ_PreparedStatementDontFinalize(statement) = true;
      TC_setObjectLock(statement, UNLOCKED); // juliana@226a_21
   }
}

// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Sets numeric parameters in a prepared statement.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->i32[1] The value of the parameter.   
 * @param type The type of the parameter.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
bool psSetNumericParamValue(NMParams p, int32 type)
{
   TRACE("psSetNumericParamValue")
   
   if (testPSClosed(p))
   {
      TCObject stmt = p->obj[0];
      Context context = p->currentContext;
      SQLSelectStatement* selectStmt = (SQLSelectStatement*)getPreparedStatementStatement(stmt);

      if (selectStmt) // Only sets the parameter if the statement is not null.
      {
         int32 index = p->i32[0];
         VoidP value = null;

         switch (type) // Gets a pointer for the value.
         {
            case SHORT_TYPE:
            case INT_TYPE:
               value = &p->i32[1];
               break;
            case LONG_TYPE:
               value = &p->i64[0];
               break;
            case FLOAT_TYPE:
               value = &p->dbl[0];
               break;
            case DOUBLE_TYPE:
               value = &p->dbl[0];
         }

         switch (selectStmt->type) // Sets the parameter.
         {
            case CMD_DELETE:
               if (!setNumericParamValueDel(context, (SQLDeleteStatement*)selectStmt, index, value, type))
                  return false;
               break;
            case CMD_INSERT:
               if (!setNumericParamValueIns(context, (SQLInsertStatement*)selectStmt, index, value, type))
                  return false;
               break;
            case CMD_SELECT:
               if (!setNumericParamValueSel(context, selectStmt, index, value, type))
                  return false;
               break;
            case CMD_UPDATE:
               if (!setNumericParamValueUpd(context, (SQLUpdateStatement*)selectStmt, index, value, type))
                  return false;
         }
         
         if (OBJ_PreparedStatementStoredParams(stmt)) // Only stores the parameter if there are parameters to be stored.
         {
            CharP ptr = null;
            int16* paramsLength = getPreparedStatementParamsLength(stmt);
            int32 length,
                  maxLength = paramsLength[index];

		      // juliana@214_2: corrected a bug that could crash the application when using logger.
            JCharP* paramsAsStrs = getPreparedStatementParamsAsStrs(stmt);
            JCharP string = paramsAsStrs[index];
            DoubleBuf buffer;

            switch (type) // Transforms the number into a string. 
            {
               case SHORT_TYPE:
               case INT_TYPE:
               {
                  ptr = TC_int2str(*(int32*)value, buffer);
                  break;
               }
               case LONG_TYPE:
               {
                  ptr = TC_long2str(*(int64*)value, buffer);
                  break;
               }
               case FLOAT_TYPE:
               case DOUBLE_TYPE:
                  ptr = TC_double2str(*(double*)value, -1, buffer);
            }

            // Stores the parameter.
            if ((length = xstrlen(ptr)) > maxLength)
            {
               paramsLength[index] = length;
               xfree(paramsAsStrs[index]);
               if (!(string = paramsAsStrs[index] = TC_CharP2JCharP(ptr, length)))
               {
                  TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
                  return false;
               }
            }
            else
               TC_CharP2JCharPBuf(ptr, length, string, true);
         }
      }
      return true;
   }
   return false;
}

// juliana@238_1: corrected the end quote not appearing in the log files after dates. 
/**
 * Sets a string parameter in a prepared statement.
 *
 * @param context The thread context where the function is being executed.
 * @param stmt The prepared statement object.
 * @param string The string object to be inserted.
 * @param index The parameter index.
 * @param stringLength The string length.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise. 
 * @throws OutOfMemoryError If a memory allocation fails.
 */
bool psSetStringParamValue(Context context, TCObject stmt, TCObject string, int32 index, int32 stringLength)
{   
   SQLSelectStatement* statement = (SQLSelectStatement*)getPreparedStatementStatement(stmt);
   JCharP stringChars = null;
   
   if (string)
      stringChars = String_charsStart(string);

   switch (statement->type) // Sets the parameter.
   {
      case CMD_DELETE:
         if (!setParamValueStringDel(context, (SQLDeleteStatement*)statement, index, stringChars, stringLength))
            return false;
         break;
      case CMD_INSERT:
         if (!setStrBlobParamValueIns(context, (SQLInsertStatement*)statement, index, stringChars, stringLength, true))
            return false;
         break;
      case CMD_SELECT:
         if (!setParamValueStringSel(context, statement, index, stringChars, stringLength))
            return false;
         break;
      case CMD_UPDATE:
         if (!setStrBlobParamValueUpd(context, (SQLUpdateStatement*)statement, index, stringChars, stringLength, true))
            return false;
   }

   if (OBJ_PreparedStatementStoredParams(stmt)) // Only stores the parameter if there are parameters to be stored.
   {
      JCharP* paramsAsStrs = getPreparedStatementParamsAsStrs(stmt);
      JCharP paramAsStr = paramsAsStrs[index];
      int16* paramsLength = getPreparedStatementParamsLength(stmt);

      if (string) // The parameter is not null.
      {
         if (stringLength + 2 > paramsLength[index]) // Reuses the buffer whenever possible
         {
            if (!(paramAsStr = paramsAsStrs[index] = (JCharP)xrealloc((uint8*)paramAsStr, (stringLength + 3) << 1)))      
            {
               TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
               return false;
            }
            paramsLength[index] = stringLength + 2;
         }
         paramAsStr[0] = '\'';
         xmemmove(&paramAsStr[1], stringChars, stringLength << 1);
         paramAsStr[stringLength + 1] = '\'';
         paramAsStr[stringLength + 2] = 0;
      }
      else // The parameter is null;
         TC_CharP2JCharPBuf("null", 4, paramAsStr, true);
   }
   
   return true;
}

// juliana@230_30: reduced log files size.
/**
 * Returns the sql used in this statement in a string buffer. If logging is disabled, returns the sql without the arguments. If logging is enabled, 
 * returns the real sql, filled with the arguments. Used only for the logger.
 *
 * @param context The thread context where the function is being executed.
 * @param statement The prepared statement.
 * @return the sql used in this statement as a <code>StringBuffer</code> object.
 */
TCObject toStringBuffer(Context context, TCObject statement)
{
   TRACE("toStringBuffer")
   TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
   
   StringBuffer_count(logSBuffer) = 0;
   if (OBJ_PreparedStatementStoredParams(statement)) // There are no parameters.
   {
      int16* paramsPos = getPreparedStatementParamsPos(statement);
		JCharP sql = String_charsStart(OBJ_PreparedStatementSqlExpression(statement));
      JCharP* paramsAsStrs = getPreparedStatementParamsAsStrs(statement);
      int32 storedParams = OBJ_PreparedStatementStoredParams(statement),
            i = -1;
      
      // PREP: + string before the first '?'.     
      if (!TC_appendCharP(context, logSBuffer, "PREP: ") || !TC_appendJCharP(context, logSBuffer, sql, paramsPos[0]))
         return null;
      
      // Concatenates each string part with the next parameter.
      while (++i < storedParams && TC_appendJCharP(context, logSBuffer, paramsAsStrs[i], TC_JCharPLen(paramsAsStrs[i]))
                                && TC_appendJCharP(context, logSBuffer, sql + paramsPos[i] + 1, (paramsPos[i + 1] - paramsPos[i] - 1)));
             
      if (i < storedParams)
         return null; 
   }
   else
   {
      TCObject sql = OBJ_PreparedStatementSqlExpression(statement);
      if (!TC_appendJCharP(context, logSBuffer, String_charsStart(sql), String_charsLen(sql)))
         return null;
   }
   
   return logSBuffer;
}

// juliana@226_15: corrected a bug that would make a prepared statement with where clause and indices not work correctly after the first execution.
/**
 * Resets a where clause because the <code>expression</code> may change between runs of a prepared statement with a where clause.
 *
 * @param whereClause the were clause to be reseted.
 * @param heap A heap to allocate the clone of the where clause expression tree.
 */
void resetWhereClause(SQLBooleanClause* whereClause, Heap heap)
{
	TRACE("resetWhereClause")
   if (whereClause) // guich@552_37: it may be null
   {
      whereClause->appliedIndexesBooleanOp = whereClause->appliedIndexesCount = 0;
      
      // After the first use of this, the tree is nulled. So, a copy of it is gotten.
      // guich@554_13: Use the expressionTreeBak as a condition instead of expressionTree (it should always be replaced after the first try).
      whereClause->expressionTree = cloneTree(whereClause->expressionTreeBak, whereClause->expressionTree, heap);
      
      whereClause->resultSet = null;
   }
}

// juliana@226_14: corrected a bug that would make a prepared statement with group by not work correctly after the first execution.
/**
 * Resets an order by or group by clause because the <code>tableColIndex</code> may change between runs of a prepared statement with a sort field. 
 * So, it is necessary to cache the <code>tableColIndex</code> of order by fields.
 *
 * @param orderByClause the order by clause to be reseted.
 */
void resetColumnListClause(SQLColumnListClause* columnListClause)
{
	TRACE("resetColumnListClause")
   if (columnListClause) // It may be null.
   {
      int32 n = columnListClause->fieldsCount;
      SQLResultSetField** fieldList = columnListClause->fieldList;
      uint8* fieldTableColIndexesBak = columnListClause->fieldTableColIndexesBak;

      while (--n >= 0)
         fieldList[n]->tableColIndex = fieldTableColIndexesBak[n];
   }
}

/**
 * Stores the null values of prepared statement in the table.
 *
 * @param table The Table used in the prepared statement.
 * @param record The prepared statement record.
 * @param storeNulls The prepared statement field that indicates if it is to store nulls in the parameters or not.
 * @param paramDefined Indicates which parameters are defined.
 * @param paramIndexes The parameters indexes.
 * @param nValues The number of fields or values of the prepared statement.
 * @param paramCount The number of parameters.
 * @param isPreparedUpdateStmt Indicates if the prepared statement is an update prepared statement or not.
 */
void rearrangeNullsInTable(Table* table, SQLValue** record, uint8* storeNulls, uint8* paramDefined,  uint8* paramIndexes, int32 nValues, 
                                                                                                                          int32 paramCount)
{
	TRACE("rearrangeNullsInTable")
   int32 length = nValues < paramCount? nValues : paramCount;

   // juliana@201_17: acessed an invalid if record[paramIndexes[length]] == null.
   while (--length >= 0)
      if (!paramDefined[length] && record[paramIndexes[length]]) 
         record[paramIndexes[length]]->isNull = true;

   xmemmove(table->storeNulls, storeNulls, NUMBEROFBYTES(table->columnCount));
}

/**
 * Tests if the prepared statement or the driver where it was created is closed.
 *
 * @param p->obj[0] The prepared statement object.
 * @throws IllegalStateException If the prepared statement or driver is closed.
 */
bool testPSClosed(NMParams params)
{
   TRACE("testPSClosed")
   TCObject statement = params->obj[0];

   if (OBJ_PreparedStatementDontFinalize(statement)) // Tests if the prepared statement is closed.
   {
      TC_throwExceptionNamed(params->currentContext, "java.lang.IllegalStateException", getMessage(ERR_PREPARED_STMT_CLOSED));
      return false;
   }
   if (OBJ_LitebaseDontFinalize(OBJ_PreparedStatementDriver(statement))) // The connection with Litebase can't be closed.
   {
      TC_throwExceptionNamed(params->currentContext, "java.lang.IllegalStateException", getMessage(ERR_DRIVER_CLOSED));
      return false;
   }
   return true;
}
