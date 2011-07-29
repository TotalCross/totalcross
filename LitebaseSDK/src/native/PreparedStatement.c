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
 * Defines functions to deal with important prepared statements.
 */

#include "Litebase.h"

/**
 * Frees a prepared statement.
 *
 * @param statement The prepared statement to be freed.
 */
void freePreparedStatement(Object statement)
{
	TRACE("freePreparedStatement")

   // juliana@230_19: removed some possible memory problems with prepared statements and ResultSet.getStrings().
   if (!OBJ_PreparedStatementDontFinalize(statement)) // The prepared statement shouldn't be finalized twice.
   {
      JCharP* paramsAsStrs = (JCharP*)OBJ_PreparedStatementParamsAsStrs(statement);
      int32* paramsPos = (int32*)OBJ_PreparedStatementParamsPos(statement); 
      int32* paramsLength = paramsLength = (int32*)OBJ_PreparedStatementParamsLength(statement);
      int32 numParams = OBJ_PreparedStatementStoredParams(statement);
		Objects* psList;
      Table* table;

      switch (OBJ_PreparedStatementType(statement)) // Destroy the statement.
      {
         case CMD_DELETE:
         {
            SQLDeleteStatement* deleteStmt = (SQLDeleteStatement*)(OBJ_PreparedStatementStatement(statement));
				
            // Removes the prepared statement from table list.
            psList = (table = deleteStmt->rsTable->table)->preparedStmts;
				psList = TC_ObjectsRemove(psList, statement);
				table->preparedStmts = psList;

            heapDestroy(deleteStmt->heap);
            break;
         }
         case CMD_INSERT:
         {
            SQLInsertStatement* insertStmt = (SQLInsertStatement*)(OBJ_PreparedStatementStatement(statement));
				
            // Removes the prepared statement from table list.
            psList = insertStmt->table->preparedStmts;
				psList = TC_ObjectsRemove(psList, statement);
				insertStmt->table->preparedStmts = psList;

            heapDestroy(insertStmt->heap);
            break;
         }
         case CMD_SELECT:
         {
            SQLSelectStatement* selectStmt = (SQLSelectStatement*)(OBJ_PreparedStatementStatement(statement)); 
            SQLSelectClause* selectClause = selectStmt->selectClause;
            SQLResultSetTable** tableList = selectClause->tableList;
            int32 i = selectClause->tableListSize;
            
            while (--i >= 0) // Removes the prepared statement from table list.
				{
					psList = (table = tableList[i]->table)->preparedStmts;
               psList = TC_ObjectsRemove(psList, statement);
					table->preparedStmts = psList;
				}

            heapDestroy(selectClause->heap);
            break;
         }
         case CMD_UPDATE:
         {
            SQLUpdateStatement* updateStmt = (SQLUpdateStatement*)(OBJ_PreparedStatementStatement(statement));
				
            // Removes the prepared statement from table list.
            psList = (table = updateStmt->rsTable->table)->preparedStmts;
				psList = TC_ObjectsRemove(psList, statement);
				table->preparedStmts = psList;

            heapDestroy(updateStmt->heap);
         }
      }

      // Frees logger information.
      xfree(paramsPos);
      xfree(paramsLength);
      while (--numParams >= 0)
         xfree(paramsAsStrs[numParams]);
      xfree(paramsAsStrs);
      
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
 * @throws DriverException If the query does not update the table or there are undefined parameters.
 * @throws IllegalStateException If the driver or prepared statement is closed.
 */
void psSetNumericParamValue(NMParams p, int32 type)
{
   TRACE("psSetNumericParamValue")
   Object stmt = p->obj[0],
          driver = OBJ_PreparedStatementDriver(stmt);
   Context context = p->currentContext;
   
   if (OBJ_PreparedStatementDontFinalize(stmt)) // Prepared Statement Closed.
      TC_throwExceptionNamed(context, "java.lang.IllegalStateException", getMessage(ERR_PREPARED_STMT_CLOSED));
   else if (OBJ_LitebaseDontFinalize(driver)) // The connection with Litebase can't be closed.
      TC_throwExceptionNamed(context, "java.lang.IllegalStateException", getMessage(ERR_DRIVER_CLOSED));
   else
   {
      SQLSelectStatement* selectStmt = (SQLSelectStatement*)OBJ_PreparedStatementStatement(stmt);

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
               setNumericParamValueDel(context, (SQLDeleteStatement*)selectStmt, index, value, type);
               break;
            case CMD_INSERT:
               setNumericParamValueIns(context, (SQLInsertStatement*)selectStmt, index, value, type);
               break;
            case CMD_SELECT:
               setNumericParamValueSel(context, selectStmt, index, value, type);
               break;
            case CMD_UPDATE:
               setNumericParamValueUpd(context, (SQLUpdateStatement*)selectStmt, index, value, type);
               break;
         }
         
         if (OBJ_PreparedStatementStoredParams(stmt)) // Only stores the parameter if there are parameters to be stored.
         {
            CharP ptr = null;
            int32* paramsLength = (int32*)OBJ_PreparedStatementParamsLength(stmt);
            int32 length,
                  maxLength = paramsLength[index];

		      // juliana@214_2: corrected a bug that could crash the application when using logger.
            JCharP* paramsAsStrs = (JCharP*)OBJ_PreparedStatementParamsAsStrs(stmt);
            JCharP string = paramsAsStrs[index];

            switch (type) // Transforms the number into a string. 
            {
               case SHORT_TYPE:
               case INT_TYPE:
               {
                  IntBuf intBuf; 
                  ptr = TC_int2str(*(int32*)value, intBuf);
                  break;
               }
               case LONG_TYPE:
               {
                  LongBuf longBuf;
                  ptr = TC_long2str(*(int64*)value, longBuf);
                  break;
               }
               case FLOAT_TYPE:
               case DOUBLE_TYPE:
               {
                  DoubleBuf doubleBuf;
                  ptr = TC_double2str(*(double*)value, -1, doubleBuf);
               }
            }

            // Stores the parameter.
            if ((length = xstrlen(ptr)) > maxLength)
            {
               paramsLength[index] = length;
               xfree(paramsAsStrs[index]);
               if (!(string = paramsAsStrs[index] = TC_CharP2JCharP(ptr, length)))
                  TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            }
            else
               TC_CharP2JCharPBuf(ptr, length, string, true);
         
         }
      }
   }
}

/**
 * Returns the sql used in this statement. If logging is disabled, returns the sql without the arguments. If logging is enabled, returns the real 
 * sql, filled with the arguments.
 *
 * @param context The thread context where the function is being executed.
 * @param statement The prepared statement.
 * @return the sql used in this statement as a <code>String</code> object.
 * @throws DriverException If the driver is closed.
 */
Object toString(Context context, Object statement)
{
	TRACE("toString")
   Object string;
   JCharP charsStart;

	if (OBJ_PreparedStatementStoredParams(statement)) // There are no parameters o the logger is not being used.
   {
      int32* paramsPos = (int32*)OBJ_PreparedStatementParamsPos(statement);
		JCharP sql = String_charsStart(OBJ_PreparedStatementSqlExpression(statement));
      JCharP* paramsAsStrs = (JCharP*)OBJ_PreparedStatementParamsAsStrs(statement);

      // juliana@202_16: Now prepared statement logging is equal in all platfotms.
      int32 debugLen = 6 + paramsPos[0],
            storedParams = OBJ_PreparedStatementStoredParams(statement),
            
            i = -1,
            length;

		// juliana@202_15: Corrected a bug that would cause a gpf or a reset when logging a prepared statement with a null value.
      while (++i < storedParams)
			debugLen += TC_JCharPLen(paramsAsStrs[i]) + paramsPos[i + 1] - paramsPos[i] - 1;

      // juliana@230_30: reduced log files size.
      if (!(string = TC_createStringObjectWithLen(context, debugLen)))
         return null;
      
      // PREP: + string before the first '?'.     
      TC_CharP2JCharPBuf("PREP: ", 6, (charsStart = String_charsStart(string)), false);
      xmemmove(&charsStart[6], sql, paramsPos[0] << 1); 
      debugLen = 6 + paramsPos[0];
      i = -1;

      while (++i < storedParams) // Concatenates each string part with the next parameter.
      {
         xmemmove(&charsStart[debugLen], paramsAsStrs[i], (length = TC_JCharPLen(paramsAsStrs[i])) << 1);
         debugLen += length;
			xmemmove(&charsStart[debugLen], &sql[paramsPos[i] + 1], (length = (paramsPos[i + 1] - paramsPos[i] - 1)) << 1); 
         debugLen += length;
      }

      return string;
   }

   return OBJ_PreparedStatementSqlExpression(statement);
}

// juliana@230_30: reduced log files size.
/**
 * Returns the sql used in this statement in a string buffer. If logging is disabled, returns the sql without the arguments. If logging is enabled, 
 * returns the real sql, filled with the arguments. Used only for the logger.
 *
 * @param context The thread context where the function is being executed.
 * @param statement The prepared statement.
 * @return the sql used in this statement as a <code>StringBuffer</code> object.
 * @throws DriverException If the driver is closed.
 */
Object toStringBuffer(Context context, Object statement)
{
   TRACE("toStringBuffer")
   Object logSBuffer = litebaseConnectionClass->objStaticValues[2];
   
   StringBuffer_count(logSBuffer) = 0;
   if (OBJ_PreparedStatementStoredParams(statement)) // There are no parameters.
   {
      int32* paramsPos = (int32*)OBJ_PreparedStatementParamsPos(statement);
		JCharP sql = String_charsStart(OBJ_PreparedStatementSqlExpression(statement));
      JCharP* paramsAsStrs = (JCharP*)OBJ_PreparedStatementParamsAsStrs(statement);
      int32 storedParams = OBJ_PreparedStatementStoredParams(statement),
            i = -1;
      
      // PREP: + string before the first '?'.     
      TC_appendCharP(context, logSBuffer, "PREP: ");
      TC_appendJCharP(context, logSBuffer, sql, paramsPos[0]);
      
      while (++i < storedParams) // Concatenates each string part with the next parameter.
      {
         TC_appendJCharP(context, logSBuffer, paramsAsStrs[i], TC_JCharPLen(paramsAsStrs[i]));
         TC_appendJCharP(context, logSBuffer, sql + paramsPos[i] + 1, (paramsPos[i + 1] - paramsPos[i] - 1)); 
      }
   }
   else
   {
      Object sql = OBJ_PreparedStatementSqlExpression(statement);
      TC_appendJCharP(context, logSBuffer, String_charsStart(sql), String_charsLen(sql));
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

   xmemmove(table->storeNulls, storeNulls, table->columnCount);
}
