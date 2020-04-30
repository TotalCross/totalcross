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
   
   // juliana@263_3: corrected a bug where a new result set data could overlap an older result set data if both were related to the same table.
   xfree(resultSet->allRowsBitmap);
   
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
   resultSet->pos = resultSet->answerCount = -1; // juliana@230_14
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
   resultSet->pos = resultSet->indexRs = resultSet->answerCount = -1; // juliana@230_14
   resultSet->table = table;
   resultSet->isTempTable = !*table->name;
   resultSet->whereClause = whereClause;
   resultSet->indexes = newIntVector(3, heap);
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
   
   resultSet->pos = resultSet->answerCount = -1; // juliana@230_14
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
   PlainDB* plainDB = &table->db;
   uint8* basbuf = plainDB->basbuf;
   uint8* rowsBitmap = resultSet->allRowsBitmap; 
   int32 rowCountLess1 = plainDB->rowCount - 1;
   
   // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
   if (rowsBitmap)
   {
      int32 i = resultSet->pos;
      
      while (i++ < rowCountLess1)
         if (isBitSet(rowsBitmap, i))
         {
            if (plainRead(context, plainDB, resultSet->pos = i))
            {
               xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
               return true;
            }
            return false;
         }
      return false;
   }

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
         xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
         return true;
      }
		
		// juliana@211_4: solved bugs with result set dealing.
		if (plainRead(context, plainDB, resultSet->pos = lastPos))
		   xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
      return false;
   }
	
	if ((ret = resultSet->pos < rowCountLess1 && plainRead(context, plainDB, ++resultSet->pos)))
      xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
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
   PlainDB* plainDB = &table->db;
   uint8* basbuf = plainDB->basbuf;
   uint8* rowsBitmap = resultSet->allRowsBitmap;

   // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
   if (rowsBitmap)
   {
      int32 i = resultSet->pos;
      
      while (i-- > 0)
         if (isBitSet(rowsBitmap, i))
         {
            if (plainRead(context, plainDB, resultSet->pos = i))
            {
               xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
               return true;
            }
            return false;
         }
      return false;
   }

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
         xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
         return true;
      }

		// juliana@211_4: solved bugs with result set dealing.
		if (plainRead(context, plainDB, resultSet->pos = lastPos))
		   xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
      return false;
   }
	
   if ((ret = resultSet->pos > 0 && plainRead(context, plainDB, --resultSet->pos)))
      xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
   return ret;
}

/**
 * Given the column index (starting from 1), returns a short value that is represented by this column. Note that it is only possible to request 
 * this column as short if it was created with this precision or if the data being fetched is the result of a DATE or DATETIME SQL function.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
int32 rsGetShort(ResultSet* resultSet, int32 column)
{
   TRACE("rsGetShort")
   int16 value; // juliana@227_18: corrected a possible insertion of a negative short column being recovered in the select as positive.
	xmove2(&value, &resultSet->table->db.basbuf[resultSet->table->columnOffsets[column]]);
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
   Table* table = resultSet->table;
	xmove4(&value, &table->db.basbuf[table->columnOffsets[column]]);
   
   // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
   if (!column && *table->name)
      value &= ROW_ID_MASK;
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
	xmove8(&value, &resultSet->table->db.basbuf[resultSet->table->columnOffsets[column]]);
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
   xmove4(&value, &resultSet->table->db.basbuf[resultSet->table->columnOffsets[column]]);
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
   READ_DOUBLE((uint8*)&value, &resultSet->table->db.basbuf[resultSet->table->columnOffsets[column]]);
   return value;
}

// juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
// juliana@226_9: strings are not loaded anymore in the temporary table when building result sets. 
/**
 * Given the column index (starting from 1), returns a char array that is represented by this column. Note that it is only possible to request 
 * this column as a char array if it was created as a string.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @param value A <code>SQLValue</code> to hold the char array.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
TCObject rsGetChars(Context context, ResultSet* resultSet, int32 column, SQLValue* value)
{
	TRACE("rsGetChars")
   int32 length = 0,
         position;
   Table* table = resultSet->table;
   PlainDB* plainDB = &table->db;
   XFile* dbo; 
   TCObject object;

   // Fetches the string position in the .dbo of the disk table.
   loadPlainDBAndPosition(&plainDB->basbuf[table->columnOffsets[column]], &plainDB, &position);
   
   nfSetPos(dbo = &plainDB->dbo, position);
   if (position >= dbo->finalPos)
      length = 0;
   else if (!nfReadBytes(context, dbo, (uint8*)&length, 2))
      return null;

   if (length > table->columnSizes[column]) // juliana@270_22: solved a possible crash when the table is corrupted.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_CORRUPTED), table->name);
      return null;
   }

   // Creates the returning object and loads the string inside it.
   if ((object = TC_createArrayObject(context, CHAR_ARRAY, length))) // guich@570_97: Checks often.
	{   
		if (length)
		{		
         value->asChars = (JCharP)ARRAYOBJ_START(object);
         value->length = length;

         if (!loadString(context, plainDB, (JCharP)ARRAYOBJ_START(object), length))
         {
            TC_setObjectLock(object, UNLOCKED);
            return null;
         }
		}
      return object;
	}
   return null;
}

// juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
/**
 * Given the column index (starting from 1), fetches two integers values that are represented by this column. Note that it is only possible to 
 * request this column as date time if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @param The structure that will hold the two returned integers.
 */
void rsGetDateTimeValue(ResultSet* resultSet, int32 column, SQLValue* value)
{
	TRACE("rsGetInt")
   Table* table = resultSet->table;
   uint8* basbuf = table->db.basbuf;
   int32 offset = table->columnOffsets[column];

   xmove4(&value->asDate, &basbuf[offset]);
   xmove4(&value->asTime, &basbuf[offset + 4]);
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
TCObject rsGetBlob(Context context, ResultSet* resultSet, int32 column)
{
	TRACE("rsGetBlob")
   int32 length,
         position;
   Table* table = resultSet->table;
   PlainDB* plainDB = &table->db;
   TCObject object;

   // Fetches the blob position in the .dbo of the disk table.
   loadPlainDBAndPosition(&plainDB->basbuf[table->columnOffsets[column]], &plainDB, &position);
   
   nfSetPos(&plainDB->dbo, position);
   if (position >= plainDB->dbo.finalPos)
      length = 0;
   else if (!nfReadBytes(context, &plainDB->dbo, (uint8*)&length, 4))
      return null;

   if (length > table->columnSizes[column]) // juliana@270_22: solved a possible crash when the table is corrupted.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_CORRUPTED), table->name);
      return null;
   }

   // guich@570_97: checks often.
   // Creates the returning object and copies the blob to it.
   if ((object = TC_createArrayObject(context, BYTE_ARRAY, length)) && nfReadBytes(context, &plainDB->dbo, ARRAYOBJ_START(object), length))
      return object;
   
   TC_setObjectLock(object, UNLOCKED);
   return null;
}

// juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
// juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
/**
 * Given the column index (starting from 1), returns a string that is represented by this column. Any column type can be returned as a string. 
 * <code>Double</code>/<code>float</code> values formatting will use the precision set with the <code>setDecimalPlaces()</code> method.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @param value A <code>SQLValue</code> to hold the char array.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
TCObject rsGetString(Context context, ResultSet* resultSet, int32 column, SQLValue* value)
{
	TRACE("rsGetString")
   Table* table = resultSet->table;
   PlainDB* plainDB = &table->db;
   uint8 *ptr = &plainDB->basbuf[table->columnOffsets[column]];
   switch (table->columnTypes[column])
   {
      case SHORT_TYPE:
         xmove2(&value->asShort, ptr); // juliana@227_18: corrected a possible insertion of a negative short column being recovered in the select as positive.
         break;
      case INT_TYPE:
         xmove4(&value->asInt, ptr);
			if (!column && *table->name) // juliana@213_4: rowid was not being returned correctly if the table was not temporary.
				value->asInt &= ROW_ID_MASK;
         break;
      case LONG_TYPE:
         xmove8(&value->asLong, ptr);
         break;
      case FLOAT_TYPE:
         xmove4(&value->asFloat, ptr);
         break;
      case DOUBLE_TYPE:
         READ_DOUBLE((uint8*)&value->asDouble, ptr);
         break;
      case DATE_TYPE:  // rnovais@567_2
			xmove4(&value->asInt, ptr);
         break;
      case DATETIME_TYPE: // rnovais@_567_2
         xmove4(&value->asDate, ptr);
         xmove4(&value->asTime, ptr + 4);
         break;
      case CHARS_TYPE:
      case CHARS_NOCASE_TYPE:
      {
         int32 length = 0,
               position;
         XFile* dbo;
         TCObject object;

         // Fetches the string position in the .dbo of the disk table.
         loadPlainDBAndPosition(ptr, &plainDB, &position);

			nfSetPos(dbo = &plainDB->dbo, position);
         if (position >= dbo->finalPos)
            length = 0;
         else if (!nfReadBytes(context, dbo, (uint8*)&length, 2))
            return null;

         if (length > table->columnSizes[column]) // juliana@270_22: solved a possible crash when the table is corrupted.
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_CORRUPTED), table->name);
            return null;
         }

         // Creates the returning object and loads the string inside it.
         if ((object = TC_createStringObjectWithLen(context, length))) // guich@570_97: check often
			{
				if (length)
				{
               value->asChars = String_charsStart(object);

				   if (!loadString(context, plainDB, (JCharP)String_charsStart(object), value->length = length))
				   {
				      TC_setObjectLock(object, UNLOCKED);
				      return null;
				   }

				}
			}
			return object;
      }
   }
   return null;
}

// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.
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
 * @throws DriverException If the result set position is invalid.
 * @throws IllegalArgumentException If count is less then -1.
 */
void getStrings(NMParams params, int32 count) // juliana@201_2: corrected a bug that would let garbage in the number of records parameter.
{
	TRACE("getStrings")
   ResultSet* resultSet = getResultSetBag(*params->obj);
   Table* table;
   Context context = params->currentContext;
   int32 position;

   if (testRSClosed(context, *params->obj)) // The driver and the result set can't be closed.
   {
      if ((position = resultSet->pos) >= 0 && position <= (table = resultSet->table)->db.rowCount - 1) // Invalid result set position.
      {
         // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
         TCObject* strings; 
         TCObject* matrixEntry;
         TCObject result;
         int8* columnTypes = table->columnTypes;
         uint8* columnNulls0 = table->columnNulls;
         SQLValue value;
         bool notTemporary = resultSet->answerCount >= 0 || resultSet->isSimpleSelect;
         SQLResultSetField** fields = resultSet->selectClause->fieldList;
         SQLResultSetField* field;

         // juliana@211_4: solved bugs with result set dealing.
         // juliana@211_3: the string matrix size can't take into consideration rows that are before the result set pointer.
         int32 cols = resultSet->selectClause->fieldsCount,
			      validRecords = 0,	
               i, 
               column,  
				   records = table->db.rowCount - resultSet->pos; // juliana@210_1: select * from table_name does not create a temporary table anymore. 

         // juliana@210_1: select * from table_name does not create a temporary table anymore.

         if (count < -1) // juliana@211_4: solved bugs with result set dealing.
		   {
			   TC_throwExceptionNamed(context, "java.lang.IllegalArgumentException", getMessage(ERR_RS_INV_POS), count);
			   return;
		   }

         // Checks the ranges
         if (count == -1)
            count = 0xFFFFFFF;
         count = MIN(count, records); 

         // juliana@230_19: removed some possible memory problems with prepared statements and ResultSet.getStrings().
         if (!(params->retO = result = TC_createArrayObject(context,"[[java.lang.String", count)) || !count) // juliana@211_4: solved bugs with result set dealing.
         {
            TC_setObjectLock(result, UNLOCKED); 
            return;
         }
         matrixEntry = (TCObject*)ARRAYOBJ_START(params->retO);

         do
         {
            if (!(*matrixEntry = TC_createArrayObject(context, "[java.lang.String", cols))) // juliana@201_19: Does not consider rowid.
            {
               TC_setObjectLock(result, UNLOCKED); 
               return;
            }
            TC_setObjectLock(*matrixEntry, UNLOCKED);
            
            // We will hold the found objects in the native stack to avoid them being collected.
            strings = (TCObject*)ARRAYOBJ_START(*(matrixEntry++));
            i = -1;
            while (++i < cols)
            {
               field = fields[i];
               column = notTemporary? (field->parameter? field->parameter->tableColIndex : field->tableColIndex) : i;   

               if (isBitUnSet(columnNulls0, column) && columnTypes[column] != BLOB_TYPE) 
               {
                  // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
                  *strings = rsGetString(context, resultSet, column, &value);
                  
                  // juliana@270_31: Corrected bug of ResultSet.getStrings() don't working properly when there is a data function in the columns 
                  // being fetched.
                  if (!(*strings) || field->isDataTypeFunction)
                  {
                     if (field->isDataTypeFunction)
                     {
                        rsApplyDataTypeFunction(params, &value, field, UNDEFINED_TYPE);
                        if (!(columnTypes[column] == CHARS_TYPE || columnTypes[column] == CHARS_NOCASE_TYPE))
                           *strings++ = params->retO;
                        else
                           TC_setObjectLock(*strings++, UNLOCKED);
                     }
                     else 
                     {
                        createString(params, &value, columnTypes[column], resultSet->decimalPlaces? resultSet->decimalPlaces[column] : -1);
                        *strings++ = params->retO;
                     }
                  }
                  else
                     TC_setObjectLock(*strings++, UNLOCKED);
                  if (params->currentContext->thrownException)
                  {
                     TC_setObjectLock(result, UNLOCKED); 
                     return;
                  }
               }
               else
                  *strings++ = null;
            }
			   validRecords++; // juliana@211_4: solved bugs with result set dealing.
         }
		   while (--count && resultSetNext(context, resultSet));         

         TC_setObjectLock(params->retO = result, UNLOCKED); 
         if ((int32)ARRAYOBJ_LEN(result) > validRecords) // juliana@211_4: solved bugs with result set dealing.
		   {
			   TCObject matrix;
   			
			   matrixEntry = (TCObject*)ARRAYOBJ_START(params->retO);
            if (!(matrix = TC_createArrayObject(context,"[[java.lang.String", validRecords)))
				   return;
			   xmemmove(ARRAYOBJ_START(matrix), matrixEntry, TSIZE * validRecords); 
			   TC_setObjectLock(params->retO = matrix, UNLOCKED);
		   }
      }
      else
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_RS_INV_POS), position);
   }      
}

// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Returns a column value of the result set given its type and column index. 
 * 
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param type The type of the column. <code>UNDEFINED</code> must be used to return anything except for blobs as strings.
 * @param p->retI receives an int or a short if type is <code>INT</code> or <code>SHORY</code>, respectively.
 * @param p->retL receives a long if type is <code>LONG</code>.
 * @param p->retD receives a float or a double if type is <code>FLOAT</code> or <code>DOUBLE</code>, respectively.
 * @param p->retO receives a string, a character array, a blob, a date, or a time if type is <code>UNDEFINED</code>, <code>CHARS</code>, 
 * <code>BLOB</code>, <code>DATE</code>, or <code>DATETIME</code>.
 * respectively.
 */
void rsGetByIndex(NMParams p, int32 type)
{
	TRACE("rsGetByIndex")
   TCObject resultSet = p->obj[0];
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
      rsPrivateGetByIndex(p, type);
}

// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
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
 * @throws NullPointerException If the column name is <code>null</code>.
 */
void rsGetByName(NMParams p, int32 type)
{
	TRACE("rsGetByName")
   TCObject resultSet = p->obj[0],
          colName = p->obj[1];
 
   // juliana@227_4: the connection where the result set was created can't be closed while using it.
   if (!colName)
      TC_throwNullArgumentException(p->currentContext, "colName");
   else if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      if ((p->i32[0] = TC_htGet32Inv(&getResultSetBag(resultSet)->intHashtable, identHashCode(colName)) + 1) >= 0)
         rsPrivateGetByIndex(p, type);
      else // juliana@266_2: corrected exception message when an unknown column name was passed to a ResultSet method.
         TC_throwExceptionNamed(p->currentContext, "java.lang.IllegalArgumentException", getMessage(ERR_INVALID_COLUMN_NAME), colName);
   }
}

/**
 * Returns a column value of the result set given its type and column index. 
 * 
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param type The type of the column. <code>UNDEFINED</code> must be used to return anything except for blobs as strings.
 * @param p->retI receives an int or a short if type is <code>INT</code> or <code>SHORY</code>, respectively.
 * @param p->retL receives a long if type is <code>LONG</code>.
 * @param p->retD receives a float or a double if type is <code>FLOAT</code> or <code>DOUBLE</code>, respectively.
 * @param p->retO receives a string, a character array, a blob, a date, or a time if type is <code>UNDEFINED</code>, <code>CHARS</code>, 
 * <code>BLOB</code>, <code>DATE</code>, or <code>DATETIME</code>.
 * respectively.
 * @throws DriverException If the kind of return type asked is incompatible from the column definition type.
 */
void rsPrivateGetByIndex(NMParams p, int32 type)
{
	TRACE("rsGetByIndex")
   ResultSet* rsBag = getResultSetBag(p->obj[0]);
      
   // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
   // juliana@227_14: corrected a DriverException not being thrown when fetching in some cases when trying to fetch data from an invalid result 
   // set column.
   // juliana@210_1: select * from table_name does not create a temporary table anymore.
	// juliana@201_23: the types must be compatible.
   int32 col = *p->i32,
         typeCol;
   SQLResultSetField* field;
   SQLValue value;
   
   if (!verifyRSState(p->currentContext, rsBag, col--))
      return;

   field = rsBag->selectClause->fieldList[col];
   if (rsBag->allRowsBitmap || rsBag->isSimpleSelect)
      col = field->parameter? field->parameter->tableColIndex : field->tableColIndex;
      
   // juliana@227_13: corrected a DriverException not being thrown when issuing ResultSet.getChars() for a column that is not of CHARS, CHARS 
   // NOCASE, VARCHAR, or VARCHAR NOCASE.
   typeCol = rsBag->table->columnTypes[col];
   
   // juliana@270_28: now it is not allowed to fetch a string field in ResultSet with methods that aren't getString() or getChars().
	if (type != UNDEFINED_TYPE)
	   if (!(field->isDataTypeFunction && type == SHORT_TYPE && (typeCol == DATE_TYPE || typeCol == DATETIME_TYPE))
       && (typeCol != type && ((typeCol != CHARS_NOCASE_TYPE && typeCol != CHARS_TYPE) || (type != CHARS_NOCASE_TYPE && type != CHARS_TYPE))))
	   {
		   TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES));
         return;
	   }

   xmemzero(&value, sizeof(value));

   // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
   if (isBitUnSet(rsBag->table->columnNulls, col))
   {
      switch (typeCol)
      {
         case SHORT_TYPE: 
            p->retI = value.asShort = rsGetShort(rsBag, col); 
            break;
         case INT_TYPE:
            p->retI = value.asInt = rsGetInt(rsBag, col); 
            break;
         case LONG_TYPE: 
            p->retL = value.asLong = rsGetLong(rsBag, col); 
            break;
         case FLOAT_TYPE: 
            p->retD = value.asFloat = rsGetFloat(rsBag, col); 
            break;
         case DOUBLE_TYPE: 
            p->retD = value.asDouble = rsGetDouble(rsBag, col); 
            break;
         case CHARS_TYPE:
         case CHARS_NOCASE_TYPE: 
            if (type == CHARS_TYPE)
               TC_setObjectLock(p->retO = rsGetChars(p->currentContext, rsBag, col, &value), UNLOCKED);
            else
               TC_setObjectLock(p->retO = rsGetString(p->currentContext, rsBag, col, &value), UNLOCKED); // STRING
            break;
         case DATE_TYPE: 
            value.asInt = rsGetInt(rsBag, col);
            if (type == DATE_TYPE)
               setDateObject(p, value.asInt);
            break; 
         case DATETIME_TYPE:
            rsGetDateTimeValue(rsBag, col, &value);
            if (type == DATETIME_TYPE)
               setTimeObject(p, value.asDate, value.asTime);
            break;
         case BLOB_TYPE: 
            if (type == BLOB_TYPE)
               TC_setObjectLock(p->retO = rsGetBlob(p->currentContext, rsBag, col), UNLOCKED);  
            else
               p->retO = null;
      }
      if (field->isDataTypeFunction)
         rsApplyDataTypeFunction(p, &value, field, type);
      else if (type == UNDEFINED_TYPE)
         createString(p, &value, typeCol, rsBag->decimalPlaces? rsBag->decimalPlaces[col] : -1);
   }
   else
   {
      p->retD = 0; // Since this is a union, just assigns 0 to the widest type.
      p->retO = null; // p->retO is not in the union.
   }
}

/**
 * Given the column index (starting from 1), indicates if this column has a <code>NULL</code>.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retI receives <code>true</code> if the value is SQL <code>NULL</code>; <code>false</code>, otherwise.
 */
void rsPrivateIsNull(NMParams params)
{
   ResultSet* rsBag = getResultSetBag(params->obj[0]);
         
   // juliana@227_14: corrected a DriverException not being thrown when fetching in some cases when trying to fetch data from an invalid result 
   // set column.
   // juliana@210_1: select * from table_name does not create a temporary table anymore.
   int32 column = params->i32[0]; 

   // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
   if (verifyRSState(params->currentContext, rsBag, column--))
   {
      if (rsBag->allRowsBitmap || rsBag->isSimpleSelect)
      {
         SQLResultSetField* field = rsBag->selectClause->fieldList[column];
         column = field->parameter? field->parameter->tableColIndex : field->tableColIndex;
      }
      params->retI = isBitSet(rsBag->table->columnNulls, column); 
   }
} 

// juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.
/**
 * Verifies if the result set and the column index are valid.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set.
 * @param column The result set table column being searched.
 * @return <code>true</code> if the the result set and the column index are valid; <code>false</code>, otherwise.
 * @throws DriverException If the result set position is invalid.
 * @throws IllegalArgumentException If the column index is invalid.
 */
bool verifyRSState(Context context, ResultSet* resultSet, int32 column)
{
	TRACE("verifyRSState")
   int32 position = resultSet->pos;
   if (position < 0 || position > resultSet->table->db.rowCount - 1)
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_RS_INV_POS), position);
      return false;
   }
   if (column <= 0 || column > resultSet->selectClause->fieldsCount) // Cols given by the user range from 1 to n.
   {
      TC_throwExceptionNamed(context, "java.lang.IllegalArgumentException", getMessage(ERR_INVALID_COLUMN_NUMBER), column);
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
   PlainDB* plainDB = &resultSet->table->db;
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
               // juliana@230_22: solved a bug of not finding rows in a where clause with AND where only one of its sides does not use an index and
               // there are deleted rows. 
               while ((position = findNextBitSet(rowsBitmap, resultSet->pos + 1)) != -1 && position <= rowCountLess1)
                  if (plainRead(context, plainDB, resultSet->pos = position))
                  {
                     if (recordNotDeleted(basbuf))
                     {
                        if ((ret = sqlBooleanClauseSatisfied(context, whereClause, resultSet, heap)) == -1)
                           return false;
                        if (ret) 
                           return true;
                     }
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
CharP zeroPad(CharP buffer, int32 value, uint32 order) // rnovais@567_2
{
	TRACE("zeroPad")
   while (order)
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
int32 identHashCode(TCObject stringObj)
{
	TRACE("identHashCode")
   int32 hash = 0,
         value;
   uint32 length = String_charsLen(stringObj);
   JCharP chars = String_charsStart(stringObj);
   while (length--)
   {
      value = (int32)*chars++;
      if (value >= (int32)'A' && value <= (int32)'Z') // guich@104
         value += 32;
      hash = (hash << 5) - hash + value; // It was 31 * hash.
   }
   return hash;
}

// juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
/**
 * Applies a function when fetching data from the result set.
 * 
 * @param params->currentContext The thread context where the function is being executed.
 * @param params->retO The returned data as a string if the user wants the table data in this format.
 * @param value The value where the function will be applied.
 * @param field The field where the function is being applied.
 * @param type The type of the field being returned.
 */
void rsApplyDataTypeFunction(NMParams params, SQLValue* value, SQLResultSetField* field, int32 type)
{
   TRACE("rsApplyDataTypeFunction")
   
   applyDataTypeFunction(value, field->sqlFunction, field->parameter->dataType);
   switch (field->sqlFunction)
   {
      case FUNCTION_DT_YEAR:      
      case FUNCTION_DT_MONTH:  
      case FUNCTION_DT_DAY:    
      case FUNCTION_DT_HOUR:   
      case FUNCTION_DT_MINUTE: 
      case FUNCTION_DT_SECOND: 
      case FUNCTION_DT_MILLIS:
      {
         if (type == UNDEFINED_TYPE)
         {         
            IntBuf buffer;
            TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_int2str(value->asShort, buffer), -1), 
                                                                                                                                       UNLOCKED);
         }
         else
            params->retI = value->asShort;
         break;
      }
      case FUNCTION_DT_ABS:
         switch (field->parameter->dataType)
         {
            case SHORT_TYPE:
            {
               if (type == UNDEFINED_TYPE)
               {
                  IntBuf buffer;
                  TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_int2str(value->asShort, buffer), -1), 
                                                                                                                                    UNLOCKED);
               }
               else
                  params->retI = value->asShort;
               break;
            }
            case INT_TYPE:
            {
               if (type == UNDEFINED_TYPE)
               {
                  IntBuf buffer;
                  TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_int2str(value->asInt, buffer), -1), 
                                                                                                                                  UNLOCKED);
               }
               else
                  params->retI = value->asInt;
               break;
            }
            case LONG_TYPE:
            {
               if (type == UNDEFINED_TYPE)
               {
                  LongBuf buffer;
                  TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_long2str(value->asLong, buffer), -1), 
                                                                                                                                    UNLOCKED);
               }
               else
                  params->retL = value->asLong;
               break;
            }
            case FLOAT_TYPE:
            {
               if (type == UNDEFINED_TYPE)
               {
                  DoubleBuf buffer;
                  TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_double2str(value->asFloat, value->length, 
                                                                                                                       buffer), -1), UNLOCKED);
               }
               else
                  params->retD = value->asFloat;
               break;
            }
            case DOUBLE_TYPE:
            {
               if (type == UNDEFINED_TYPE)
               {
                  DoubleBuf buffer;
                  TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_double2str(value->asDouble, value->length, 
                                                                                                                       buffer), -1), UNLOCKED);
               }
               else
                  params->retD = value->asDouble;
            }

         }
   }
}

// juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
/**
 * Creates a string to return to the user.
 * 
 * @param params->currentContext The thread context where the function is being executed.
 * @param params->retO The returned data as a string if the user wants the table data in this format.
 * @param value The value where the function will be applied.
 * @param type The type of the value being returned to the user.
 * @param decimalPlaces The number of decimal places if the value is a floating point number.
 */
void createString(NMParams params, SQLValue* value, int32 type, int32 decimalPlaces)
{
   TRACE("createString")
   
   switch (type)
   {
      case SHORT_TYPE:
      {
         IntBuf buffer;
         TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_int2str(value->asShort, buffer), -1), UNLOCKED);
         break;
      }
      case INT_TYPE:
      {
         IntBuf buffer;
         TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_int2str(value->asInt, buffer), -1), UNLOCKED);
         break;
      }
      case LONG_TYPE:
      {
         LongBuf buffer;
         TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_long2str(value->asLong, buffer), -1), UNLOCKED);
         break;
      }
      case FLOAT_TYPE:
      {
         DoubleBuf buffer;
         TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_double2str((double)value->asFloat, decimalPlaces, 
                                                                                                              buffer), -1), UNLOCKED);
         break;
      }
      case DOUBLE_TYPE:
      {
         DoubleBuf buffer;
         TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, TC_double2str(value->asDouble, decimalPlaces, 
                                                                                                              buffer), -1), UNLOCKED);
         break;
      }
      case DATE_TYPE:
      {
         DateBuf dateBuf;
         formatDate(dateBuf, value->asInt);
         TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, dateBuf, 10), UNLOCKED);
         break;
      }
      case DATETIME_TYPE: // rnovais@_567_2
      {
         DateTimeBuf dateTimeBuf;
         formatDate(dateTimeBuf, value->asDate);
         formatTime(&dateTimeBuf[11], value->asTime);
         dateTimeBuf[10] = ' ';
         TC_setObjectLock(params->retO = TC_createStringObjectFromCharP(params->currentContext, dateTimeBuf, 23), UNLOCKED);
      }
   }  
}

/**
 * Loads the physical table where a string or blob is stored and its position in the .dbo file. 
 *
 * @param buffer A buffer where is stored the string position in the result set dbo.
 * @param plainDB The result set plainDB, which will become the physical one if the query uses a temporary table.
 * @param position The position of the string or blob in the physical dbo.
 */
void loadPlainDBAndPosition(uint8* buffer, PlainDB** plainDB, int32* position)
{
   TRACE("loadPlainDBAndPosition")
 
   xmove4(position, buffer); // Loads the string or blob position in the .dbo.
	
   // Loads the string or blob length in the .dbo.
   if (!*(*plainDB)->name) // juliana@210_1: select * from table_name does not create a temporary table anymore.
	{
      // juliana@212_5: correct a bug that would crash the program when issuing ResultSet.getChars() with a select which does not use a temporary table.
      uint8* ptrStr = (*plainDB)->dbo.fbuf + *position;
      
      xmove4(position, ptrStr);
      ptrStr += 4; 
		xmoveptr(plainDB, ptrStr);
	}
}

/**
 * Tests if the result set or the driver where it was created is closed.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set object.
 * @throws IllegalStateException If the result set or driver is closed.
 */
bool testRSClosed(Context context, TCObject resultSet)
{
   TRACE("testRSClosed")
   if (OBJ_ResultSetDontFinalize(resultSet)) // Prepared Statement Closed.
   {
      TC_throwExceptionNamed(context, "java.lang.IllegalStateException", getMessage(ERR_RESULTSET_CLOSED));
      return false;
   }
   if (OBJ_LitebaseDontFinalize(getResultSetBag(resultSet)->driver)) // The connection with Litebase can't be closed.
   {
      TC_throwExceptionNamed(context, "java.lang.IllegalStateException", getMessage(ERR_DRIVER_CLOSED));
      return false;
   }
   return true;
}

// juliana@253_3: added methods to return the primary key columns of a table.
/**
 * Returns a table used in a select given its name.
 * 
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set.
 * @param tableName The table name.
 * @return The table with the given name or <code>null</code> if an exception occurs.
 * @throws DriverException if the given table name is not used in the select.
 */
Table* getTableRS(Context context, ResultSet* resultSet, CharP tableName)
{
   SQLResultSetField** fields = resultSet->selectClause->fieldList;
   int32 i = resultSet->selectClause->fieldsCount;
      
   // The table name must be used in the select.
   while (--i >= 0 && (!fields[i]->tableName || xstrcmp(fields[i]->tableName, tableName)));         
   if (i == -1)
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_NAME_NOT_FOUND), tableName);
      return null;
   }
   return getTable(context, resultSet->driver, tableName);  
}

//juliana@253_4: added methods to return the default value of a column.
/**
 * Gets the default value of a column.
 * 
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set.
 * @param tableName The name of the table.
 * @param index The column index.
 * @return The default value of the column as a string or <code>null</code> if there is no default value.
 * @throws DriverException If the column index is of a column of type <code>BLOB</code>.
 */
TCObject getDefault(Context context, ResultSet* resultSet, CharP tableName, int32 index) 
{
   Table* table;
   
   if ((table = getTable(context, resultSet->driver, tableName)))
   {
      int32 type = table->columnTypes[index];
      SQLValue* value = table->defaultValues[index];  
      DoubleBuf buffer;
      CharP valueCharP = "";     
                
      if (!value) // No default value, returns null.
         return null;
      
      switch (type)
      {
         case CHARS_TYPE:
         case CHARS_NOCASE_TYPE:
         {
            TCObject string = TC_createStringObjectWithLen(context, value->length);
            if (!string)
               return null;
            xmemmove(String_charsStart(string), value->asChars, value->length << 1); 
            return string;
         }
         case SHORT_TYPE: 
         {
            valueCharP = TC_int2str(value->asShort, buffer);
            break;
         }   
         case INT_TYPE:
         {
            valueCharP = TC_int2str(value->asInt, buffer);
            break;
         }
         case LONG_TYPE:
         {
            valueCharP = TC_long2str(value->asLong, buffer);
            break;
         }
         case FLOAT_TYPE:
         {
            valueCharP = TC_double2str(value->asFloat, -1, buffer);
            break;
         }
         case DOUBLE_TYPE:
         {
            valueCharP = TC_double2str(value->asDouble, -1, buffer);
            break;
         }
         case DATE_TYPE:
         {
            int32 dateInt = value->asDate;
            
            xstrprintf(valueCharP = buffer, "%04d/%02d/%02d", dateInt / 10000, dateInt / 100 % 100, dateInt % 100);
            break;
         }    
         case DATETIME_TYPE:
         {
            int32 dateInt = value->asDate,
                  timeInt = value->asTime;
            
            xstrprintf(valueCharP = buffer, "%04d/%02d/%02d", dateInt / 10000, dateInt / 100 % 100, dateInt % 100);               
            xstrprintf(&buffer[11], "%02d:%02d:%02d:%03d", timeInt / 10000000, timeInt / 100000 % 100, timeInt / 1000 % 100, timeInt % 1000);
            buffer[10] = ' ';
            break;
         }
         case BLOB_TYPE: // Blob can't be used with default.
         {
             TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_BLOB_STRING));
             return null;
         }
      }
      
      // Types that are not string.
      return TC_createStringObjectFromCharP(context, valueCharP, -1);
   }
   
   return null;
}
