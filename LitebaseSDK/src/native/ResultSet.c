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
 * This module implements functions for fetching a set or rows resulting from a <code>LitebaseConnection.executeQuery()</code> method call.
 * Here's an example:
 *
 * <pre>
 * ResultSet rs = driver.executeQuery(&quot;select name, salary, age from person&quot;);
 * while (rs.next())
 *    Vm.debug(pad(rs.getString(&quot;name&quot;), 32) + pad(rs.getString(&quot;salary&quot;), 16) 
 *                                                     + rs.getInt(&quot;age&quot;) + &quot; years&quot;);
 * </pre>
 *
 * Result sets cannot be constructed directly; instead, you must issue a sql to the driver.
 */

#include "ResultSet.h"

/**
 * Frees a result set structure.
 *
 * @param resultSet The resultSet to be freed.
 */
void freeResultSet(ResultSet* resultSet)
{
	TRACE("freeResultSet")

   // Only frees temporary tables.
   // juliana@210_1: select * from table_name does not create a temporary table anymore.
   if (resultSet->isTempTable) // juliana@223_14: solved possible memory problems.
      freeTable(null, resultSet->table, false, false); 
   
   // juliana@223_13: corrected a bug that could break the application when freeing a result set of a prepared statement.
   // Only frees the select clause if it is not from a prepared statement, which might be used again.
   if (resultSet->selectClause && !resultSet->isPrepared)
      heapDestroy(resultSet->selectClause->heap);

   heapDestroy(resultSet->heap); // Frees the structure
}

/**
 * Creates a result set structure.
 * 
 * @param table The table to be used by the result set, which can be temporary or not.
 * @param whereClause The where clause to evaluate the records of the table to be returned to the user.
 * @param heap The heap to allocate the result set structure.
 * @return The result set created.
 */
ResultSet* createResultSet(Table* table, SQLBooleanClause* whereClause, Heap heap)
{
	TRACE("createResultSet")
   ResultSet* resultSet = (ResultSet*)TC_heapAlloc(heap, sizeof(ResultSet));
   resultSet->pos = -1;
   resultSet->table = table;
   resultSet->whereClause = whereClause;
   resultSet->isTempTable = !*table->name;
   return resultSet;
}

/** 
 * Creates a simple result set structure and computes the indices.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table to be used by the result set, which can be temporary or not.
 * @param whereClause The where clause to evaluate the records of the table to be returned to the user.
 * @param heap The heap to allocate the result set structure.
 * @return The result set created.
 */
ResultSet* createSimpleResultSet(Context context, Table* table, SQLBooleanClause* whereClause, Heap heap)
{
	TRACE("createSimpleResultSet")
   ResultSet* resultSet;
   resultSet = (ResultSet*)TC_heapAlloc(heap, sizeof(ResultSet));
   resultSet->pos = resultSet->indexRs = -1;
   resultSet->table = table;
   resultSet->isTempTable = !*table->name;
   resultSet->whereClause = whereClause;
   resultSet->indexes = newIntVector(null, 3, heap);
   resultSet->heap = heap;

   // Tries to use the table indexes to generate a bitmap of the rows to be returned.
   if (whereClause && !generateIndexedRowsMap(context, &resultSet, 1, table->numberComposedIndexes > 0, heap))
   {
      freeResultSet(resultSet);
      return null;
   }
   return resultSet;
}

/** 
 * Creates a result set structure and computes the indices for the returning of a select statement.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table to be used by the result set, which can be temporary or not.
 * @param whereClause The where clause to evaluate the records of the table to be returned to the user.
 * @param heap The heap to allocate the result set structure.
 * @return The result set created.
 */
ResultSet* createResultSetForSelect(Context context, Table* table, SQLBooleanClause* whereClause, Heap heap)
{
	TRACE("createResultSetForSelect")
   ResultSet* resultSet;
   
   if (!(resultSet = createSimpleResultSet(context, table, whereClause, heap)))
      return null;
   
   resultSet->pos = -1;
   resultSet->decimalPlaces = (int8*)TC_heapAlloc(resultSet->heap, resultSet->columnCount = table->columnCount);
   xmemset(resultSet->decimalPlaces, -1, resultSet->columnCount);
   return resultSet;
}

/**
 * Gets the next record of the result set.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @return <code>true</code> if there is a next record to go to in the result set; <code>false</code>, otherwise.
 */
bool resultSetNext(Context context, ResultSet* resultSet)
{
	TRACE("resultSetNext")
   bool ret;
   Table* table = resultSet->table;
   PlainDB* plainDB = table->db;
   uint8* basbuf = plainDB->basbuf;
   int32 rowCountLess1 = plainDB->rowCount - 1;

   if (table->deletedRowsCount > 0)
   {
      bool isDeleted = false; // Indicates if it was deleted.
		int32 lastPos = resultSet->pos, // juliana@211_4: solved bugs with result set dealing.
            value;
            
      // juliana@201_27: solved a bug in next() and prev() that would happen after doing a delete from table_name. 
      while (resultSet->pos++ < rowCountLess1) // juliana@210_1: select * from table_name does not create a temporary table anymore. 
      {
			if (!plainRead(context, plainDB, resultSet->pos))
				return false;
			xmove4(&value, basbuf); 
			if (!(isDeleted = (value & ROW_ATTR_MASK) == ROW_ATTR_DELETED))
            break;
      }
      
		if (resultSet->pos <= rowCountLess1 && !isDeleted) // Sets the position after the last record.
      {
         xmemmove(*table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
         return true;
      }
		
		// juliana@211_4: solved bugs with result set dealing.
		if (plainRead(context, plainDB, resultSet->pos = lastPos))
		   xmemmove(*table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
      return false;
   }
	
	if ((ret = resultSet->pos < rowCountLess1 && plainRead(context, plainDB, ++resultSet->pos)))
      xmemmove(*table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
   return ret;
}

/**
 * Gets the previous record of the result set.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @return <code>true</code> if there is a next record to go to in the result set; <code>false</code>, otherwise.
 */
bool resultSetPrev(Context context, ResultSet* resultSet)
{
	TRACE("resultSetPrev")
   bool ret;
   Table* table = resultSet->table;
   PlainDB* plainDB = table->db;
   uint8* basbuf = plainDB->basbuf;

   if (table->deletedRowsCount > 0) // juliana@210_1: select * from table_name does not create a temporary table anymore.
   {
      bool isDeleted = false; // Indicates if it was deleted.
      int32 lastPos = resultSet->pos, // juliana@211_4: solved bugs with result set dealing. 
            value;  
      
      while (resultSet->pos-- > 0) // juliana@201_27: solved a bug in next() and prev() that would happen after doing a delete from table_name. 
      {
			if (!plainRead(context, plainDB, resultSet->pos))
				return false;
			xmove4(&value, basbuf); 
			if (!(isDeleted = (value & ROW_ATTR_MASK) == ROW_ATTR_DELETED))
            break;
      }
      
		if (resultSet->pos >= 0 && !isDeleted) // Sets the position after the last record.
      {
         xmemmove(*table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
         return true;
      }

		// juliana@211_4: solved bugs with result set dealing.
		if (plainRead(context, plainDB, resultSet->pos = lastPos))
		   xmemmove(*table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
      return false;
   }
	
   if ((ret = resultSet->pos > 0 && plainRead(context, plainDB, --resultSet->pos)))
      xmemmove(*table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
   return ret;
}

/**
 * Given the column index (starting from 1), returns a short value that is represented by this column. Note that it is only possible to request 
 * this column as short if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
int32 rsGetShort(ResultSet* resultSet, int32 column)
{
   TRACE("rsGetShort")
   int16 value; // juliana@227_18: corrected a possible insertion of a negative short column being recovered in the select as positive.
	xmove2(&value, &resultSet->table->db->basbuf[resultSet->table->columnOffsets[column]]);
   return value; 
}

/**
 * Given the column index (starting from 1), returns an integer value that is represented by this column. Note that it is only possible to request 
 * this column as integer if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
int32 rsGetInt(ResultSet* resultSet, int32 column)
{
	TRACE("rsGetInt")
	int32 value;
	xmove4(&value, &resultSet->table->db->basbuf[resultSet->table->columnOffsets[column]]);
   return value;
}

/**
 * Given the column index (starting from 1), returns a long value that is represented by this column. Note that it is only possible to request 
 * this column as long if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
int64 rsGetLong(ResultSet* resultSet, int32 column)
{
	TRACE("rsGetLong")
   int64 value;
	xmove8(&value, &resultSet->table->db->basbuf[resultSet->table->columnOffsets[column]]);
   return value;
}

/**
 * Given the column index (starting from 1), returns a float value that is represented by this column. Note that it is only possible to request 
 * this column as float if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
float rsGetFloat(ResultSet* resultSet, int32 column)
{
	TRACE("rsGetFloat")
   float value;
   xmove4(&value, &resultSet->table->db->basbuf[resultSet->table->columnOffsets[column]]);
   return value;
}

/**
 * Given the column index (starting from 1), returns a double value that is represented by this column. Note that it is only possible to request 
 * this column as double if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
double rsGetDouble(ResultSet* resultSet, int32 column)
{
	TRACE("rsGetDouble")
   double value;
   READ_DOUBLE((uint8*)&value, &resultSet->table->db->basbuf[resultSet->table->columnOffsets[column]]);
   return value;
}

// juliana@226_9: strings are not loaded anymore in the temporary table when building result sets. 
/**
 * Given the column index (starting from 1), returns a char array that is represented by this column. Note that it is only possible to request 
 * this column as a char array if it was created as a string.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @param fieldIdx A field index for computing data type functions.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
Object rsGetChars(Context context, ResultSet* resultSet, int32 column, int32 fieldIdx)
{
	TRACE("rsGetChars")
   int32 length = 0,
         position;
   Table* table = resultSet->table;
   PlainDB* plainDB = table->db;
   XFile* dbo; 
   Object object;

   xmove4(&position, &plainDB->basbuf[table->columnOffsets[column]]); // Loads the string position in the .dbo.
	
   // Loads the string length in the .dbo.
   if (!*plainDB->name) // juliana@210_1: select * from table_name does not create a temporary table anymore.
	{
      // juliana@212_5: correct a bug that would crash the program when issuing ResultSet.getChars() with a select which does not use a temporary table.
      uint8* ptrStr = plainDB->dbo.fbuf + position;
      
      xmove4(&position, ptrStr);
      ptrStr += 4; 
		xmoveptr(&plainDB, ptrStr);
	}

   nfSetPos(dbo = &plainDB->dbo, position);
   if (nfReadBytes(context, dbo, (uint8*)&length, 2) != 2)
      return null;

   // Creates the returning object and loads the string inside it.
   if ((object = TC_createArrayObject(context, CHAR_ARRAY, length))) // guich@570_97: Checks often.
	{   
      int32 length2X = length << 1;
      TC_setObjectLock(object, UNLOCKED);
		
		if (length)
		{		
         SQLResultSetField* field = resultSet->selectClause->fieldList[fieldIdx];

			if (plainDB->isAscii) // Must put an empty space for each charater to transform it in unicode.
			{
            int32 i = length - 1;
				CharP buf = ARRAYOBJ_START(object),
                  from = buf + i,
					   to = from + i;
				
			   if (nfReadBytes(context, dbo, (uint8*)buf, length) != length)
			      return null;
				while (--i >= 0)
				{
				   *to = *from;
				   *from-- = 0;
					to -= 2;
				}
			}
			else if (nfReadBytes(context, &plainDB->dbo, ARRAYOBJ_START(object), length2X) != length2X)
            return null;

         if (field->isDataTypeFunction)
         {
            SQLValue value;
            value.asChars = (JCharP)ARRAYOBJ_START(object);
            value.length = ARRAYOBJ_LEN(object);
            applyDataTypeFunction(&value, field->sqlFunction, field->parameter->dataType);
         }
		}
      return object;
	}
   return null;
}

// juliana@220_3: blobs are not loaded anymore in the temporary table when building result sets.
/**
 * Given the column index (starting from 1), returns a byte array (blob) that is represented by this column. Note that it is only possible to request 
 * this column as a blob if it was created as a string.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
Object rsGetBlob(Context context, ResultSet* resultSet, int32 column)
{
	TRACE("rsGetBlob")
   int32 length,
         position;
   uint8* ptrBlob;
   Table* table = resultSet->table;
   PlainDB* plainDB = table->db;
   Object object;

   // Loads the blob position on .dbo.
   xmove4(&position, &plainDB->basbuf[table->columnOffsets[column]]);
	if (!*plainDB->name) // juliana@210_1: select * from table_name does not create a temporary table anymore.
	{
      // The plainDB of the disk table of the blob is loaded.
      ptrBlob = plainDB->dbo.fbuf + position;
      xmove4(&position, ptrBlob);
      ptrBlob += 4; 
		xmoveptr(&plainDB, ptrBlob);
	} 

   // Fetches the blob position in the .dbo of the disk table.
   nfSetPos(&plainDB->dbo, position);
   if (nfReadBytes(context, &plainDB->dbo, (uint8*)&length, 4) != 4)
      return null;

   // guich@570_97: checks often.
   // Creates the returning object and copies the blob to it.
   if ((object = TC_createArrayObject(context, BYTE_ARRAY, length)) 
    && nfReadBytes(context, &plainDB->dbo, ARRAYOBJ_START(object), length) == length)
   {
      TC_setObjectLock(object, UNLOCKED);
      return object;
   }

   return null;
}

// juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
/**
 * Given the column index (starting from 1), returns a string that is represented by this column. Any column type can be returned as a string. 
 * <code>Double</code>/<code>float</code> values formatting will use the precision set with the <code>setDecimalPlaces()</code> method.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @param fieldIdx A field index for computing data type functions.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
Object rsGetString(Context context, ResultSet* resultSet, int32 column, int32 fieldIdx)
{
	TRACE("rsGetString")
   Table* table = resultSet->table;
   PlainDB* plainDB = table->db;
   uint8 *ptr = &plainDB->basbuf[table->columnOffsets[column]];
   Object object = null;
   switch (table->columnTypes[column])
   {
      case SHORT_TYPE:
      {
         IntBuf buffer;
         int16 value;
			xmove2(&value, ptr); // juliana@227_18: corrected a possible insertion of a negative short column being recovered in the select as positive.
         TC_setObjectLock(object = TC_createStringObjectFromCharP(context, TC_int2str(value, buffer), -1), UNLOCKED);
         break;
      }
      case INT_TYPE:
      {
         IntBuf buffer;
         int32 value; 
			xmove4(&value, ptr);
			if (!column && *table->name) // juliana@213_4: rowid was not being returned correctly if the table was not temporary.
				value = value & ROW_ID_MASK;
         TC_setObjectLock(object = TC_createStringObjectFromCharP(context, TC_int2str(value, buffer), -1), UNLOCKED);
         break;
      }
      case DATE_TYPE:  // rnovais@567_2
      {
         DateBuf dateBuf;
         int32 value; 
			xmove4(&value, ptr);
         formatDate(dateBuf, value);
         TC_setObjectLock(object = TC_createStringObjectFromCharP(context, dateBuf, 10), UNLOCKED);
         break;
      }
      case DATETIME_TYPE: // rnovais@_567_2
      {
         DateTimeBuf dateTimeBuf;
         int32 intDate,
               intTime; 
			xmove4(&intDate, ptr);
			xmove4(&intTime, ptr + 4);
         formatDate(dateTimeBuf, intDate);
         formatTime(&dateTimeBuf[11], intTime);
         dateTimeBuf[10] = dateTimeBuf[23] = dateTimeBuf[24] = dateTimeBuf[25] = ' ';
         TC_setObjectLock(object = TC_createStringObjectFromCharP(context, dateTimeBuf, 26), UNLOCKED);
         break;
      }
      case LONG_TYPE:
      {
         int64 value;
         LongBuf buffer;
			xmove8(&value, ptr);
         TC_setObjectLock(object = TC_createStringObjectFromCharP(context, TC_long2str(value, buffer), -1), UNLOCKED);
         break;
      }
      case FLOAT_TYPE:
      {
         float value;
         int32 places = resultSet->decimalPlaces[column];
         DoubleBuf buffer;
         xmove4(&value, ptr);
         TC_setObjectLock(object = TC_createStringObjectFromCharP(context, TC_double2str(value, places, buffer), -1), UNLOCKED);
         break;
      }
      case DOUBLE_TYPE:
      {
         double value;
         int32 places = resultSet->decimalPlaces[column];
         DoubleBuf buffer;
         READ_DOUBLE((uint8*)&value, ptr);
         TC_setObjectLock(object = TC_createStringObjectFromCharP(context, TC_double2str(value, places, buffer), -1), UNLOCKED);
         break;
      }
      case CHARS_TYPE:
      case CHARS_NOCASE_TYPE:
      {
         int32 length = 0,
               position;
         XFile* dbo;

         xmove4(&position, ptr); // Loads the string position in the .db.

         // Loads the string length in the .dbo.
			if (!*plainDB->name) // juliana@210_1: select * from table_name does not create a temporary table anymore.
			{
            uint8* ptrStr = plainDB->dbo.fbuf + position;
            xmove4(&position, ptrStr);
            ptrStr += 4; 
		      xmoveptr(&plainDB, ptrStr);
			}

			nfSetPos(dbo = &plainDB->dbo, position);
         if (nfReadBytes(context, dbo, (uint8*)&length, 2) != 2)
            return null;

         // Creates the returning object and loads the string inside it.
         if ((object = TC_createStringObjectWithLen(context, length))) // guich@570_97: check often
			{
            TC_setObjectLock(object, UNLOCKED);
				if (length)
				{
               SQLResultSetField* field = resultSet->selectClause->fieldList[fieldIdx];

				   if (plainDB->isAscii) // Must put an empty space for each charater to transform it in unicode.
					{
                  int32 i = length - 1;
						CharP buffer = (CharP)String_charsStart(object),
                        from = buffer + i,
							   to = from + i;
						
					   if (nfReadBytes(context, dbo, (uint8*)buffer, length) != length)
					      return null;

						while (--i >= 0)
				      {
				         *to = *from;
				         *from-- = 0;
					      to -= 2;
				      }
					}
				   else if (nfReadBytes(context, dbo, (uint8*)String_charsStart(object), length << 1) != length << 1)
                  return null;

               if (field->isDataTypeFunction)
               {
                  SQLValue value;
                  value.asChars = String_charsStart(object);
                  value.length = String_charsLen(object);
                  applyDataTypeFunction(&value, field->sqlFunction, field->parameter->dataType);
               }
				}
			}
      }
   }
   return object;
}

/**
 * Starting from the current cursor position, it reads all result set rows that are being requested. <code>first()</code>,  <code>last()</code>, 
 * <code>prev()</code>, or <code>next()</code> must be used to set the current position, but not  <code>beforeFirst()</code> or 
 * <code>afterLast()</code>. It doesn't return BLOB values. <code>null</code> is returned in their places instead.
 *
 * @param p->obj[0] The result set. 
 * @param p->retO receives a matrix, where <code>String[0]<code> is the first row, and <code>String[0][0], String[0][1]...</code> are the column 
 * elements of the first row. Returns <code>null</code> if here's no more element to be fetched. Double/float values will be formatted using the 
 * <code>setDecimalPlaces()</code> settings. If the value is SQL <code>NULL</code> or a <code>blob</code>, the value returned is <code>null</code>.
 * @param count The number of rows to be fetched, or -1 for all.
 * @throws DriverException If the result set or the driver is closed, or the result set position is invalid.
 */
void getStrings(NMParams p, int32 count) // juliana@201_2: corrected a bug that would let garbage in the number of records parameter.
{
	TRACE("getStrings")
   ResultSet* resultSet = (ResultSet*)OBJ_ResultSetBag(*p->obj);
   Table* table;
   Context context = p->currentContext;
   int32 position;

   p->retO = null;
   if (!resultSet) // The result set is closed.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_RESULTSET_CLOSED));
      return;
   }
   if (OBJ_LitebaseDontFinalize(resultSet->driver)) // juliana@227_4: the connection where the result set was created can't be closed while using it.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_DRIVER_CLOSED));
      return;
   }

   if ((position = resultSet->pos) < 0 || position > (table = resultSet->table)->db->rowCount - 1) // Invalid result set position.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_RS_INV_POS), position);
      return;
   }
   else
   {
      Object* strings; 
      Object* matrixEntry;
      int32* columnTypes = table->columnTypes;
      uint8* columnNulls0 = *table->columnNulls;

      // juliana@211_4: solved bugs with result set dealing.
      // juliana@211_3: the string matrix size can't take into consideration rows that are before the result set pointer.
      int32 cols = resultSet->columnCount,
			   validRecords = 0,	
            i, 
			   init = resultSet->isSimpleSelect? 1 : 0,  // juliana@210_1: select * from table_name does not create a temporary table anymore.
				records = table->db->rowCount - resultSet->pos; // juliana@210_1: select * from table_name does not create a temporary table anymore. 

      if (count < -1) // juliana@211_4: solved bugs with result set dealing.
		{
			TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_RS_INV_POS), count);
			return;
		}

      // Checks the ranges
      if (count == -1)
         count = 0xFFFFFFF;
      count = MIN(count, records); 

      if (!(p->retO = TC_createArrayObject(context,"[[java.lang.String", count)) || !count) // juliana@211_4: solved bugs with result set dealing.
         return;

      matrixEntry = (Object*)ARRAYOBJ_START(p->retO);

      do
      {
         if (!(*matrixEntry = TC_createArrayObject(context, "[java.lang.String", cols - init))) // juliana@201_19: Does not consider rowid.
         {
            p->retO = null;
            return;
         }
         TC_setObjectLock(*matrixEntry, UNLOCKED);
         
         // We will hold the found objects in the native stack to avoid them being collected.
         strings = (Object*)ARRAYOBJ_START(*(matrixEntry++));
         i = init - 1;
         while (++i < cols)
         {
            if (isBitUnSet(columnNulls0, i) && columnTypes[i] != BLOB_TYPE) 
            {
               // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
               if (!(*strings++ = rsGetString(context, resultSet, i, i - init)))
               {
                  p->retO = null;
                  return;
               }
            }
            else
               *strings++ = null;
         }
			validRecords++; // juliana@211_4: solved bugs with result set dealing.
      }
		while (--count > 0 && resultSetNext(context, resultSet));         
		if (table->deletedRowsCount) // juliana@211_4: solved bugs with result set dealing.
		{
			Object matrix = p->retO;
			matrixEntry = (Object*)ARRAYOBJ_START(matrix);
         if (!(p->retO = TC_createArrayObject(context,"[[java.lang.String", validRecords)))
				return;
			xmemmove(ARRAYOBJ_START(p->retO), matrixEntry, PTRSIZE * validRecords); 
			TC_setObjectLock(matrix, UNLOCKED);
		}

		TC_setObjectLock(p->retO, UNLOCKED);
   }
}

/**
 * Returns a date object from an int date in the format YYYYMMDD.
 *
 * @param p->retI The int date.
 * @param p->retO Receives the date object.
 */
void rsGetDate(NMParams p)
{
   TRACE("rsGetDate")
   int32 date = p->retI;

   if (date)
   {
      Object object = p->retO = TC_createObject(p->currentContext, "totalcross.util.Date");
      
      if (object)
      {
         TC_setObjectLock(object, UNLOCKED);
         FIELD_I32(object, 0) = date % 100;
         FIELD_I32(object, 1) = (date /= 100) % 100;
         FIELD_I32(object, 2) = date / 100;
      }
   }
   else
      p->retO = null;
}

/**
 * Returns a datetime object from 2 ints in date and time format: YYYYMMDD and HHMMSSmmm read from the query table.
 *
 * @param context The thread context where the function is being executed.
 * @param datetime Receives the datetime object.
 * @param rsBag The result set bag.
 * @param column The column index where the datetime data is supposed to be stored.
 * @throws DriverException If the column index is not of a datetime column.
 */
void rsGetDateTime(Context context, Object* datetime, ResultSet* rsBag, int32 column)
{
   TRACE("rsGetDateTime")
   Table* table = rsBag->table;

   if (rsBag->isSimpleSelect)
      column++;

   if (verifyRSState(context, rsBag, column + 1)) // juliana@221_2: It was getting the wrong column.
   {
      // juliana@201_23: the types must be compatible.
      if (table->columnTypes[column] != DATETIME_TYPE)
      {
	      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES));
	      return;
      }
      
      if (isBitUnSet(table->columnNulls[0], column))
      {
	      uint8* buffer = &table->db->basbuf[table->columnOffsets[column]];
         int32 value;
         Object auxObject = *datetime = TC_createObject(context, "totalcross.sys.Time");
         if (auxObject)
         {
	         TC_setObjectLock(auxObject, UNLOCKED);
		      
            // Sets the date part of the Time object.
            xmove4(&value, buffer);
	         Time_day(auxObject) = value % 100;
	         Time_month(auxObject) = (value /= 100) % 100;
	         Time_year(auxObject) = value / 100;

	         // Sets the time part of the Time object.
            xmove4(&value, buffer + 4); 
	         Time_millis(auxObject) = value % 1000;
	         Time_second(auxObject) = (value /= 1000) % 100;
	         Time_minute(auxObject) = (value /= 100) % 100;
	         Time_hour(auxObject) = (value / 100) % 100;
         }
      }
   }
   
}

/**
 * Returns a column value of the result set given its type and column index. DATE and DATETIME values will be returned as a single int or as a 
 * short and an int, respectivelly.
 * 
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param type The type of the column. <code>UNDEFINED</code> must be used to return anything except for blobs as strings.
 * @param p->retI receives an int or a short if type is <code>INT</code> or <code>SHORY</code>, respectively.
 * @param p->retL receives a long if type is <code>LONG</code>.
 * @param p->retD receives a float or a double if type is <code>FLOAT</code> or <code>DOUBLE</code>, respectively.
 * @param p->retO receives a string, a character array or a blob if type is <code>UNDEFINED</code>, <code>CHARS</code>, or <code>BLOB</code>, 
 * respectively.
 * @throws DriverException If the result set or the driver is closed, or the kind of return type asked is incompatible from the column definition 
 * type.
 */
void rsGetByIndex(NMParams p, int32 type)
{
	TRACE("rsGetByIndex")
   ResultSet* resultSet = (ResultSet*)OBJ_ResultSetBag(*p->obj);
   
   // juliana@227_4: the connection where the result set was created can't be closed while using it.
   if (!resultSet)
      TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_RESULTSET_CLOSED));
   else if (OBJ_LitebaseDontFinalize(resultSet->driver)) 
      TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_DRIVER_CLOSED));
   else
   {
      // juliana@227_14: corrected a DriverException not being thrown when fetching in some cases when trying to fetch data from an invalid result 
      // set column.
      // juliana@210_1: select * from table_name does not create a temporary table anymore.
		// juliana@201_23: the types must be compatible.
      int32 colGiven = *p->i32,
            col = *p->i32 + (resultSet->isSimpleSelect? 1: 0),
            colLess1 = col - 1,
            colFunc = colGiven - 1,
            typeCol;
      
      if (!verifyRSState(p->currentContext, resultSet, colGiven))
         return;

      // juliana@227_13: corrected a DriverException not being thrown when issuing ResultSet.getChars() for a column that is not of CHARS, CHARS 
      // NOCASE, VARCHAR, or VARCHAR NOCASE.
		if ((typeCol = resultSet->table->columnTypes[colLess1]) != type && type != UNDEFINED_TYPE && typeCol != CHARS_NOCASE_TYPE 
       && typeCol != CHARS_TYPE)
		{
			TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES));
         return;
		}
      
      if (type == DATE_TYPE)
			type = INT_TYPE;

      // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
      if (isBitUnSet(*resultSet->table->columnNulls, colLess1))
         switch (type)
         {
            case SHORT_TYPE: 
               p->retI = rsGetShort(resultSet, colLess1); 
               break;
            case INT_TYPE: 
               p->retI = rsGetInt(resultSet, colLess1); 
               break;
            case LONG_TYPE: 
               p->retL = rsGetLong(resultSet, colLess1); 
               break;
            case FLOAT_TYPE: 
               p->retD = rsGetFloat(resultSet, colLess1); 
               break;
            case DOUBLE_TYPE: 
               p->retD = rsGetDouble(resultSet, colLess1); 
               break;
            case CHARS_TYPE: 
               p->retO = rsGetChars(p->currentContext, resultSet, colLess1, colFunc);  
               break;
            case BLOB_TYPE: 
               p->retO = rsGetBlob(p->currentContext, resultSet, colLess1);  
               break;
            default: 
               p->retO = rsGetString(p->currentContext, resultSet, colLess1, colFunc); // STRING
         }
      else
      {
         p->retD = 0; // Since this is a union, just assigns 0 to the widest type.
         p->retO = null; // p->retO is not in the union.
      }
      
   }
}

/**
 * Returns a column value of the result set given its type and column name. DATE will be returned as a single int. This function can't be used to
 * return a DATETIME.
 * 
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param type The type of the column. <code>UNDEFINED</code> must be used to return anything except for blobs as strings.
 * @param p->retI receives an int or a short if type is <code>INT</code> or <code>SHORY</code>, respectively.
 * @param p->retL receives a long if type is <code>LONG</code>.
 * @param p->retD receives a float or a double if type is <code>FLOAT</code> or <code>DOUBLE</code>, respectively.
 * @param p->retO receives a string, a character array or a blob if type is <code>UNDEFINED</code>, <code>CHARS</code>, or <code>BLOB</code>, 
 * respectively.
 * @throws DriverException If the result set or the driver is closed, or the kind of return type asked is incompatible from the column definition 
 * type.
 */
void rsGetByName(NMParams p, int32 type)
{
	TRACE("rsGetByName")
   ResultSet* resultSet = (ResultSet*)OBJ_ResultSetBag(*p->obj);
 
   // juliana@227_4: the connection where the result set was created can't be closed while using it.
   if (!p->obj[1])
      TC_throwNullArgumentException(p->currentContext, "colName");
   else if (!resultSet)
      TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_RESULTSET_CLOSED));
   else if (OBJ_LitebaseDontFinalize(resultSet->driver))
      TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_DRIVER_CLOSED));
   else
   {
      Object colName = p->obj[1];

		// juliana@210_1: select * from table_name does not create a temporary table anymore.
		// juliana@201_23: the types must be compatible.
      int32 colFunc = TC_htGet32Inv(&resultSet->intHashtable, identHashCode(colName)),
            col = resultSet->isSimpleSelect? colFunc + 1 : colFunc,
		      typeCol;
		
      // juliana@227_14: corrected a DriverException not being thrown when fetching in some cases when trying to fetch data from an invalid result 
      // set column.
      if (!verifyRSState(p->currentContext, resultSet, colFunc + 1))
         return;

      // juliana@227_13: corrected a DriverException not being thrown when issuing ResultSet.getChars() for a column that is not of CHARS, CHARS 
      // NOCASE, VARCHAR, or VARCHAR NOCASE.
      if ((typeCol = resultSet->table->columnTypes[col]) != type && type != UNDEFINED_TYPE && typeCol != CHARS_NOCASE_TYPE && typeCol != CHARS_TYPE)
		{
			TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES));
         return;
		}

      if (type == DATE_TYPE)
			type = INT_TYPE;
      
      // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
      if (isBitUnSet(*resultSet->table->columnNulls, col)) 
         switch (type)
         {
            case SHORT_TYPE: 
               p->retI = rsGetShort(resultSet, col); 
               break;
            case INT_TYPE: 
               p->retI = rsGetInt(resultSet, col); 
               break;
            case LONG_TYPE: 
               p->retL = rsGetLong(resultSet, col); 
               break;
            case FLOAT_TYPE: 
               p->retD = rsGetFloat(resultSet, col); 
               break;
            case DOUBLE_TYPE: 
               p->retD = rsGetDouble(resultSet, col); 
               break;
            case CHARS_TYPE: 
               p->retO = rsGetChars(p->currentContext, resultSet, col, colFunc);  
               break;
            case BLOB_TYPE: 
               p->retO = rsGetBlob(p->currentContext, resultSet, col);   
               break;
            default: 
               p->retO = rsGetString(p->currentContext, resultSet, col, colFunc); // STRING
         }
      else
      {
         p->retD = 0; // Since this is a union, just assigns 0 to the widest type.
         p->retO = null; // p->retO is not in the union.
      }
      
   }
}

/**
 * Verifies if the result set and the column index are valid.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set.
 * @param column The result set table column being searched.
 * @return <code>true</code> if the the result set and the column index are valid; <code>false</code>, otherwise.
 * @throws DriverException if the result set position or the column index are invalid.
 */
bool verifyRSState(Context context, ResultSet* resultSet, int32 column)
{
	TRACE("verifyRSState")
   int32 position = resultSet->pos;
   if (position < 0 || position > resultSet->table->db->rowCount - 1)
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_RS_INV_POS), position);
      return false;
   }
   if (column <= 0 || column > resultSet->columnCount) // Cols given by the user range from 1 to n.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_COLUMN_NUMBER), column);
      return false;
   }
   return true;
}


/**
 * Gets the next record of a result set. This function is to be used by the result sets created internally by the Litebase code, not by external 
 * result sets.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set.
 * @param heap A heap to alocate temporary strings in the expression tree.
 * @return <code>true</code> if there is a next record to go to in the result set; <code>false</code>, otherwise.
 */
bool getNextRecord(Context context, ResultSet* resultSet, Heap heap)
{
	TRACE("getNextRecord")
   PlainDB* plainDB = resultSet->table->db;
   IntVector* rowsBitmap = &resultSet->rowsBitmap;
   uint8* basbuf = plainDB->basbuf;
   SQLBooleanClause* whereClause = resultSet->whereClause;
   int32 rowCountLess1 = plainDB->rowCount - 1;
   bool ret;

   if (rowsBitmap->size > 0) // Desired rows partially computed using the indexes?
   {
      int32 position;
            
      if (resultSet->pos < rowCountLess1) 
      {
         if (!whereClause)
         {
            // juliana@227_7: solved a bug on delete when trying to delete a key from a column which has index and there are deleted rows with the
            // same key.
            // No WHERE clause. Just returns the rows marked in the bitmap.
            while ((position = findNextBitSet(rowsBitmap, resultSet->pos + 1)) != -1 && position <= rowCountLess1 
                && plainRead(context, plainDB, resultSet->pos = position))
               if (recordNotDeleted(basbuf))
                  return true;
         }
         else
         {
            // With a remaining WHERE clause there are 2 situations.
            // 1) The relationship between the bitmap and the WHERE clause is an AND relationship, and
            // 2) The relationship between the bitmap and the WHERE clause is an OR relationship.
            if (resultSet->rowsBitmapBoolOp == OP_BOOLEAN_AND)
            {
               // juliana@227_7: solved a bug on delete when trying to delete a key from a column which has index and there are deleted rows with the
               // same key.
               // AND case - walks through the bits that are set in the bitmap and checks if rows satisfies the where clause.
               while ((position = findNextBitSet(rowsBitmap, resultSet->pos + 1)) != -1 && position <= rowCountLess1)
                  if (plainRead(context, plainDB, resultSet->pos = position) && recordNotDeleted(basbuf))
                  {
                     if ((ret = sqlBooleanClauseSatisfied(context, whereClause, resultSet, heap)) == -1)
                        return false;
                     if (ret) 
                        return true;
                  }
                  else
                     return false;
            }
            else
            {
               // OR case - walks through all records. If the corresponding bit is set in the bitmap, do not need to evaluate WHERE clause.
               // Otherwise, checks if the row satisifies the WHERE clause.
               // juliana@201_27: solved a bug in next() and prev() that would happen after doing a delete from table_name. 
               while (resultSet->pos < rowCountLess1 && plainRead(context, plainDB, ++resultSet->pos))
               {
                  if (IntVectorisBitSet(rowsBitmap, resultSet->pos))
                     return true;

                  if (recordNotDeleted(basbuf))
                  {  
                     if ((ret = sqlBooleanClauseSatisfied(context, whereClause, resultSet, heap)) == -1)
                        return false;
                     if (ret) 
                        return true;
                  }
               }
            }
         }
      }
   }
   else
   {
      // If the where clause exists, it needs to be satisfied.
      // juliana@201_27: solved a bug in next() and prev() that would happen after doing a delete from table_name.
      while (resultSet->pos < rowCountLess1 && plainRead(context, plainDB, ++resultSet->pos)) 
         if (recordNotDeleted(basbuf))
			{         
				if (whereClause)
            {
               if ((ret = sqlBooleanClauseSatisfied(context, whereClause, resultSet, heap)) == -1)
                  return false;
               if (ret) 
                  return true;
            }
            else
               return true;
			}
   }
   return false;
}

// rnovais@567_2
/**
 * Formats a int date into a date string according with the device formatting settings.
 *
 * @param buffer The buffer where the date will be stored as a string.
 * @param intDate An integer representing a time.
 */
void formatDate(CharP buffer, int32 intDate)
{
	TRACE("formatDate")
   int32 day = intDate % 100,
         month = (intDate /= 100) % 100,
         year = intDate / 100,
         value1, 
         value2, 
         value3;
   TCSettings settings = TC_getSettingsPtr();
	char dateSeparator = *settings->dateSeparatorPtr;
   int32 dateFormat = *settings->dateFormatPtr;

   if (dateFormat == DATE_MDY)
   {
      value1 = month; 
      value2 = day;
      value3 = year;
   }
   else if (dateFormat == DATE_YMD)
   {
      value1 = year; 
      value2 = month; 
      value3 = day;
   }
   else
   {
      value1 = day; 
      value2 = month; 
      value3 = year;
   }
   buffer = zeroPad(buffer, value1, 10);
   *buffer++ = dateSeparator;
   buffer = zeroPad(buffer, value2, 10);
   *buffer++ = dateSeparator;
   buffer = zeroPad(buffer, value3, 1000);
   *buffer = 0;
}

/**
 * Formats an int time into a time according with the device formatting settings.
 * 
 * @param buffer The buffer where the date will be stored as a string.
 * @param intDate An integer representing a time.
 */
void formatTime(CharP buffer, int32 intTime)
{
	TRACE("formatTime")
   int32 mills = intTime % 1000, 
   second = (intTime /= 1000) % 100, 
   minute = (intTime /= 100) % 100, 
   hour = (intTime / 100) % 100;
   TCSettings settings = TC_getSettingsPtr();
   int32 useAmPm = !*settings->is24HourPtr;
   char timeSeparator = *settings->timeSeparatorPtr;
   int32 h;

   if (useAmPm)
   {
      if (hour == 0 || hour == 12)
         h = 12;
      else
         h = hour < 12? hour : (hour - 12);
   }
   else
      h = hour;

   buffer = zeroPad(buffer, h, 10);
   *buffer++ = timeSeparator;
   buffer = zeroPad(buffer, minute, 10);
   *buffer++ = timeSeparator;
   buffer = zeroPad(buffer, second, 10);
   *buffer++ = timeSeparator;
   buffer = zeroPad(buffer, mills, 100);
   if (useAmPm)
   {
      *buffer++ = ' ';
      if (hour >= 12)
         *buffer++ = 'P';
      else
         *buffer++ = 'A';
      *buffer++ = 'M';
   }
   *buffer = 0;
}

/**
 * Pads a numeric string with zeros on the left to format dates and times.
 *
 * @param buffer The string which stores a date or a time.
 * @param value The date or time part to be inserted in the string.
 * @param order The decimal order of the value being inserted in the string.
 * @return The buffer string address offset by the number of decimal orders.
 */
CharP zeroPad(CharP buffer, int32 value, int32 order) // rnovais@567_2
{
	TRACE("zeroPad")
   while (order > 0)
   {
      *buffer++ = ((value / order) % 10) + '0';
      order /= 10;
   }
   return buffer;
}

/** 
 * Calculates the hash code of a string object.
 *
 * @param stringObj The string object.
 * @return The hash code of the string object.
 */
int32 identHashCode(Object stringObj)
{
	TRACE("identHashCode")
   int32 hash = 0,
         length = String_charsLen(stringObj),
         value;
   JCharP chars = String_charsStart(stringObj);
   while (length-- > 0)
   {
      value = (int32)*chars++;
      if (value >= (int32)'A' && value <= (int32)'Z') // guich@104
         value += 32;
      hash = (hash << 5) - hash + value; // It was 31 * hash.
   }
   return hash;
}
