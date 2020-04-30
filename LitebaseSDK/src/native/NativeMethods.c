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
 * Defines Litebase native methods. 
 */

#include "NativeMethods.h"

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Moves to the next record and fills the data members.
 *
 * @param p->obj[0] The row iterator. 
 * @param p->retI Receives <code>true</code> if it is possible to iterate to the next record. Otherwise, it will return <code>false</code>.
 */
LB_API void lRI_next(NMParams p) // litebase/RowIterator public native boolean next();
{
	TRACE("lRI_next")	
   
   MEMORY_TEST_START

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   if (testRIClosed(p)) 
   {
      TCObject rowIterator = p->obj[0];
      Table* table = getRowIteratorTable(rowIterator);
      int32 rowNumber = OBJ_RowIteratorRowNumber(rowIterator),
         id;
      PlainDB* plainDB = &table->db; 
      uint8* basbuf = plainDB->basbuf;

	   if (++rowNumber < plainDB->rowCount && plainRead(p->currentContext, plainDB, rowNumber))
      {
         xmove4(&id, basbuf);
         xmemmove((uint8*)ARRAYOBJ_START(OBJ_RowIteratorData(rowIterator)), basbuf, plainDB->rowSize);
         OBJ_RowIteratorRowid(rowIterator) = id & ROW_ID_MASK;
         OBJ_RowIteratorAttr(rowIterator) = ((id & ROW_ATTR_MASK) >> ROW_ATTR_SHIFT) & 3; // Masks out the attributes.
         p->retI = true;
      }
      else
         p->retI = false;
      OBJ_RowIteratorRowNumber(rowIterator) = rowNumber;

      // juliana@223_5: now possible null values are treated in RowIterator.
      xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
   }

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Moves to the next record with an attribute different of SYNCED.
 *
 * @param p->obj[0] The row iterator. 
 * @param p->retI Receives <code>true</code> if it is possible to iterate to a next record not synced. Otherwise, it will return <code>false</code>.
 */
LB_API void lRI_nextNotSynced(NMParams p) // litebase/RowIterator public native boolean nextNotSynced();
{
	TRACE("lRI_nextNotSynced")
    
   MEMORY_TEST_START

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   if (testRIClosed(p))
   {
      TCObject rowIterator = p->obj[0];
      Context context = p->currentContext;
      Table* table = getRowIteratorTable(rowIterator);
      PlainDB* plainDB = &table->db;
      uint8* basbuf = plainDB->basbuf;
      int32 rowNumber = OBJ_RowIteratorRowNumber(rowIterator),
            rowSize = plainDB->rowSize,
            id; 
      
      p->retI = false;

	   while (++rowNumber < plainDB->rowCount && plainRead(context, plainDB, rowNumber))
      {
         xmove4(&id, basbuf);
         if ((id & ROW_ATTR_MASK) == ROW_ATTR_SYNCED)
            continue;
         xmemmove((uint8*)ARRAYOBJ_START(OBJ_RowIteratorData(rowIterator)), basbuf, rowSize);
         OBJ_RowIteratorRowid(rowIterator) = id & ROW_ID_MASK;
         OBJ_RowIteratorAttr(rowIterator) = ((id & ROW_ATTR_MASK) >> ROW_ATTR_SHIFT) & 3; // Masks out the attributes.
         p->retI = true;
         break;
      }
      OBJ_RowIteratorRowNumber(rowIterator) = rowNumber;
      
      // juliana@223_5: now possible null values are treated in RowIterator.
      xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
   }
     
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * If the attribute is currently NEW or UPDATED, this method sets them to SYNCED. Note that if the row is DELETED, the change will be ignored.
 *
 * @param p->obj[0] The row iterator. 
 */
LB_API void lRI_setSynced(NMParams p) // litebase/RowIterator public native void setSynced();
{
	TRACE("lRI_setSynced")
   
   MEMORY_TEST_START

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   if (testRIClosed(p))
   {
      TCObject rowIterator = p->obj[0];
      Table* table = getRowIteratorTable(rowIterator);
      PlainDB* plainDB = &table->db; 
      uint8* basbuf = plainDB->basbuf;
      int32 rowNumber = OBJ_RowIteratorRowNumber(rowIterator),
            id,
            oldAttr,
            newAttr;

      // The record is assumed to have been already read.
      xmove4(&id, basbuf);
      
      // guich@560_19 // juliana@230_16: solved a bug with row iterator.
      if ((newAttr = OBJ_RowIteratorAttr(rowIterator) = ROW_ATTR_SYNCED_MASK) != (oldAttr = ((id & ROW_ATTR_MASK) >> ROW_ATTR_SHIFT) & 3) 
       && oldAttr != (int32)ROW_ATTR_DELETED_MASK)
      {
         id = (id & ROW_ID_MASK) | newAttr; // Sets the new attribute.
         xmove4(basbuf, &id); 
		   plainRewrite(p->currentContext, plainDB, rowNumber);
      }
   }

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@270_29: added RowIterator.setNotSynced().
/**
 * Forces the attribute to be NEW. This method will be useful if a row was marked as synchronized but was not sent to server for some problem.
 * If the row is marked as DELETED, its attribute won't be changed.
 *
 * @param p->obj[0] The row iterator. 
 */
LB_API void lRI_setNotSynced(NMParams p) // litebase/RowIterator public native void setNotSynced();
{
	TRACE("lRI_setNotSynced")
   
   MEMORY_TEST_START

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   if (testRIClosed(p))
   {
      TCObject rowIterator = p->obj[0];
      Table* table = getRowIteratorTable(rowIterator);
      PlainDB* plainDB = &table->db; 
      uint8* basbuf = plainDB->basbuf;
      int32 rowNumber = OBJ_RowIteratorRowNumber(rowIterator),
            id,
            oldAttr,
            newAttr;

      // The record is assumed to have been already read.
      xmove4(&id, basbuf);
      
      // guich@560_19 // juliana@230_16: solved a bug with row iterator.
      if ((newAttr = OBJ_RowIteratorAttr(rowIterator) = ROW_ATTR_NEW) != (oldAttr = ((id & ROW_ATTR_MASK) >> ROW_ATTR_SHIFT) & 3) 
       && oldAttr != (int32)ROW_ATTR_DELETED_MASK)
      {
         id = (id & ROW_ID_MASK) | newAttr; // Sets the new attribute.
         xmove4(basbuf, &id); 
		   plainRewrite(p->currentContext, plainDB, rowNumber);
      }
   }

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Closes this iterator.
 *
 * @param p->obj[0] The row iterator.
 */
LB_API void lRI_close(NMParams p) // litebase/RowIterator public native void close();
{
	TRACE("lRI_close")
   
   MEMORY_TEST_START

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   if (testRIClosed(p))
   {
      TCObject rowIterator = p->obj[0];
   
      // juliana@227_22: RowIterator.close() now flushes the setSynced() calls.
      XFile* dbFile = &getRowIteratorTable(rowIterator)->db.db;
      if (dbFile->cacheIsDirty)
         flushCache(p->currentContext, dbFile);

      setRowIteratorTable(rowIterator, null);
	   OBJ_RowIteratorData(rowIterator) = null;
   }

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Resets the counter to zero so it is possible to restart to fetch records.
 *
 * @param p->obj[0] The row iterator.
 */
LB_API void lRI_reset(NMParams p) // litebase/RowIterator public native void reset();
{
	TRACE("lRI_reset")
   MEMORY_TEST_START
   OBJ_RowIteratorRowNumber(p->obj[0]) = -1;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@223_5: now possible null values are treated in RowIterator.
/**
 * Returns a short contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The short column index, starting from 1.
 * @param p->retI Receives the value of the column or 0 if the column is <code>null</code>.
 */
LB_API void lRI_getShort_i(NMParams p) // litebase/RowIterator public native short getShort(int column);
{
	TRACE("lRI_getShort_i")
   MEMORY_TEST_START
   getByIndex(p, SHORT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@223_5: now possible null values are treated in RowIterator.
/**
 * Returns an integer contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The integer column index, starting from 1.
 * @param p->retI Receives the value of the column or 0 if the column is <code>null</code>.
 */
LB_API void lRI_getInt_i(NMParams p) // litebase/RowIterator public native int getInt(int column);
{
	TRACE("lRI_getInt_i")
   MEMORY_TEST_START
   getByIndex(p, INT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@223_5: now possible null values are treated in RowIterator.
/**
 * Returns a long integer contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The long integer column index, starting from 1.
 * @param p->retL Receives the value of the column or 0 if the column is <code>null</code>.
 */
LB_API void lRI_getLong_i(NMParams p) // litebase/RowIterator public native long getLong(int column);
{
	TRACE("lRI_getLong_i")
   MEMORY_TEST_START
   getByIndex(p, LONG_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@223_5: now possible null values are treated in RowIterator.
/**
 * Returns a floating point number contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The floating point number column index, starting from 1.
 * @param p->retD Receives the value of the column or 0 if the column is <code>null</code>.
 */
LB_API void lRI_getFloat_i(NMParams p) // litebase/RowIterator public native double getFloat(int column);
{
	TRACE("lRI_getFloat_i")
   MEMORY_TEST_START
   getByIndex(p, FLOAT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@223_5: now possible null values are treated in RowIterator.
/**
 * Returns a double precision floating point number contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The double precision floating point number column index, starting from 1.
 * @param p->retD Receives the value of the column or 0 if the column is <code>null</code>.
 */
LB_API void lRI_getDouble_i(NMParams p) // litebase/RowIterator public native double getDouble(int column);
{
	TRACE("lRI_getDouble_i")
   MEMORY_TEST_START
   getByIndex(p, DOUBLE_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@223_5: now possible null values are treated in RowIterator.
/**
 * Returns a string contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The string column index, starting from 1.
 * @param p->retO Receives the value of the column or <code>null</code> if the column is <code>null</code>.
 */
LB_API void lRI_getString_i(NMParams p) // litebase/RowIterator public native String getString(int column);
{
	TRACE("lRI_getString_i")
   MEMORY_TEST_START
   getByIndex(p, CHARS_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@223_5: now possible null values are treated in RowIterator.
/**
 * Returns a blob contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The blob column index, starting from 1.
 * @param p->retO Receives the value of the column or <code>null</code> if the column is <code>null</code>.
 */
LB_API void lRI_getBlob_i(NMParams p) // litebase/RowIterator public native byte[] getBlob(int column);
{
	TRACE("lRI_getBlob_i")
   MEMORY_TEST_START
   getByIndex(p, BLOB_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@223_5: now possible null values are treated in RowIterator.
/**
 * Returns a date contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The date column index, starting from 1.
 * @param p->retO Receives the value of the column or <code>null</code> if the column is <code>null</code>.
 */
LB_API void lRI_getDate_i(NMParams p) // litebase/RowIterator public native totalcross.util.Date getDate(int column);
{
	TRACE("lRI_getDate_i")
   MEMORY_TEST_START
   getByIndex(p, DATE_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@223_5: now possible null values are treated in RowIterator.
/**
 * Retur
 ns a datetime contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The datetime column index, starting from 1.
 * @param p->retO Receives the value of the column or <code>null</code> if the column is <code>null</code>.
 */
LB_API void lRI_getDateTime_i(NMParams p) // litebase/RowIterator public native totalcross.sys.Time getDateTime(int column);
{
	TRACE("lRI_getDateTime_i")
   MEMORY_TEST_START
   getByIndex(p, DATETIME_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.
// juliana@223_5: now possible null values are treated in RowIterator.
/**
 * Indicates if this column has a <code>NULL</code>.
 *
 * @param p->i32[0] The column index, starting from 1.
 * @param p->retI Receives <code>true</code> if the value is SQL <code>NULL</code>; <code>false</code>, otherwise.
 * @throws IllegalArgumentException If the column index is invalid.
 */
LB_API void lRI_isNull_i(NMParams p) // litebase/RowIterator public native boolean isNull(int column) IllegalArgumentException; 
{
   TRACE("lRI_isNull_i")
   
   MEMORY_TEST_START
	
   // juliana@225_14: RowIterator must throw an exception if its driver is closed.	
   if (testRIClosed(p))
   {
      TCObject rowIterator = p->obj[0];
      Table* table = getRowIteratorTable(rowIterator);
      int32 column = p->i32[0];
      
      if (column < 0 || column >= table->columnCount) // Checks if the column index is within range.
         TC_throwExceptionNamed(p->currentContext, "java.lang.IllegalArgumentException", getMessage(ERR_INVALID_COLUMN_NUMBER), column);
      else
         p->retI = isBitSet(table->columnNulls, column); // juliana@223_5: now possible null values are treated in RowIterator.
   }
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Creates a Litebase connection for the default creator id, storing the database as a flat file. This method avoids the creation of more than one 
 * instance with the same creator id, which would lead to performance and memory problems. Using this method, the strings are stored in the 
 * unicode format. 
 *
 * @param p->retO Receives a Litebase instance.
 */
LB_API void lLC_privateGetInstance(NMParams p) // litebase/LitebaseConnection public static native litebase.LitebaseConnection privateGetInstance();
{
	TRACE("lLC_privateGetInstance")
	MEMORY_TEST_START
   TC_setObjectLock(p->retO = create(p->currentContext, TC_getApplicationId(), null), UNLOCKED);
	MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/LitebaseConnection public static native litebase.LitebaseConnection privateGetInstance(String appCrid) throws DriverException, 
//                                                                                                                        NullPointerException;
/**
 * Creates a Litebase connection for the given creator id, storing the database as a flat file. This method avoids the creation of more than one 
 * instance with the same creator id, which would lead to performance and memory problems. Using this method, the strings are stored in the 
 * unicode format.
 *
 * @param p->obj[0] The creator id, which may (or not) be the same one of the current application and MUST be 4 characters long.
 * @param p->retO Receives a Litebase instance.
 * @throws DriverException If an application id with more or less than four characters is specified.
 * @throws NullPointerException If <code>appCrid == null</code>.
 */
LB_API void lLC_privateGetInstance_s(NMParams p) 
{
	TRACE("lLC_privateGetInstance_s")
	char strAppId[5];
   TCObject appCrid = p->obj[0];

   MEMORY_TEST_START
	
   if (!appCrid) // The application can't be null.
		TC_throwNullArgumentException(p->currentContext, "appCrid");
   else if (String_charsLen(appCrid) != 4) // The application id must have 4 characters.
      TC_throwExceptionNamed(p->currentContext, "litebase.DriverException",  getMessage(ERR_INVALID_CRID));
   else
   {
      TC_JCharP2CharPBuf(String_charsStart(appCrid), 4, strAppId);
	   TC_setObjectLock(p->retO = create(p->currentContext, getAppCridInt(strAppId), null), UNLOCKED);
   }

	MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/LitebaseConnection public static native litebase.LitebaseConnection privateGetInstance(String appCrid, String params) 
//
// juliana@253_8: now Litebase supports weak cryptography.                                                                                                 throws DriverException, NullPointerException;
/**
 * Creates a connection with Litebase.
 *
 * @param p->obj[0] The creator id, which may be the same one of the current application.
 * @param p->obj[1] Only the folder where it is desired to store the tables, <code>null</code>, if it is desired to use the current data 
 * path, or <code>chars_type = chars_format; path = source_path[;crypto] </code>, where <code>chars_format</code> can be <code>ascii</code> or 
 * <code>unicode</code>, <code>source_path</code> is the folder where the tables will be stored, and crypto must be used if the tables of the 
 * connection use cryptography. The params can be entered in any order. If only the path is passed as a parameter, unicode is used and there is no 
 * cryptography. Notice that path must be absolute, not relative. 
 * <p>Note that databases belonging to multiple applications can be stored in the same path, since all tables are prefixed by the application's 
 * creator id.
 * <p>Also notice that to store Litebase files on card on Pocket PC, just set the second parameter to the correct directory path.
 * <p>It is not recommended to create the databases directly on the PDA. Memory cards are FIVE TIMES SLOWER than the main memory, so it will take 
 * a long time to create the tables. Even if the NVFS volume is used, it can be very slow. It is better to create the tables on the desktop, and copy 
 * everything to the memory card or to the NVFS volume.
 * <p>Due to the slowness of a memory card and the NVFS volume, all queries will be stored in the main memory; only tables and indexes will be stored 
 * on the card or on the NVFS volume.
 * <p> An exception will be raised if tables created with an ascii kind of connection are oppened with an unicode connection and vice-versa. 
 * @param p->retO Receives a Litebase instance.
 * @throws DriverException If an application id with more or less than four characters is specified.
 * @throws NullPointerException If <code>appCrid == null</code>.
 */
LB_API void lLC_privateGetInstance_ss(NMParams p) 
{
	TRACE("lLC_privateGetInstance_ss")
	char strAppId[5];
   TCObject appCrid = p->obj[0],
          params = p->obj[1];

   MEMORY_TEST_START
	
   if (!appCrid) // The application can't be null.
      TC_throwNullArgumentException(p->currentContext, "appCrid"); 
	else if (String_charsLen(appCrid) != 4) // The application id must have 4 characters.
      TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_INVALID_CRID));
   else
   {
      TC_JCharP2CharPBuf(String_charsStart(appCrid), 4, strAppId);
	   TC_setObjectLock(p->retO = create(p->currentContext, getAppCridInt(strAppId), params), UNLOCKED);
   }

	MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Returns the path where the tables created/opened by this connection are stored.
 *
 * @param p->obj[0] The connection with Litebase.
 * @param p->retO Receives a string representing the path.
 * @throws IllegalStateException If the driver is closed.
 */
LB_API void lLC_getSourcePath(NMParams p) // litebase/LitebaseConnection public native String getSourcePath() throws IllegalStateException;
{
	TRACE("lLC_getSourcePath")
   TCObject driver = p->obj[0];

   MEMORY_TEST_START

   if (OBJ_LitebaseDontFinalize(driver)) // The driver can't be closed.
      TC_throwExceptionNamed(p->currentContext, "java.lang.IllegalStateException", getMessage(ERR_DRIVER_CLOSED));
   else
      TC_setObjectLock(p->retO = TC_createStringObjectFromTCHARP(p->currentContext, getLitebaseSourcePath(driver), -1), UNLOCKED);

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Used to execute a <code>create table</code> or <code>create index</code> SQL commands.
 * 
 * <p>Examples:
 * <ul>
 *     <li><code>driver.execute("create table PERSON (NAME CHAR(30), SALARY DOUBLE, AGE INT, EMAIL CHAR(50))");</code>
 *     <li><code>driver.execute("CREATE INDEX IDX_NAME ON PERSON(NAME)");</code>
 * </ul>
 * 
 * <p>When creating an index, its name is ignored but must be given. The index can be created after data was added to the table.
 *
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The SQL creation command.
 */
LB_API void lLC_execute_s(NMParams p) // litebase/LitebaseConnection public native void execute(String sql); 
{
	TRACE("lLC_execute_s")

   MEMORY_TEST_START

	if (checkParamAndDriver(p, "sql")) // The sql can't be null and the driver can't be closed.
   {
      Context context = p->currentContext;
      TCObject driver = p->obj[0],
             sqlString = p->obj[1],
	          logger = litebaseConnectionClass->objStaticValues[1];

      if (logger)
		{
			LOCKVAR(log);
			TC_executeMethod(context, loggerLog, logger, 16, sqlString, false);
			UNLOCKVAR(log);
         if (context->thrownException)
            goto finish;
		}
      litebaseExecute(context, driver, String_charsStart(sqlString), String_charsLen(sqlString));
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Used to execute updates in a table (insert, delete, update, alter table, drop). E.g.:
 *
 * <p><code>driver.executeUpdate(&quot;drop table person&quot;);</code> will drop also the indices.
 * <p><code>driver.executeUpdate(&quot;drop index * on person&quot;);</code> will drop all indices but not the primary key index.
 * <p><code>driver.executeUpdate(&quot;drop index name on person&quot;);</code> will drop the index for the &quot;name&quot; column.
 * <p><code> driver.executeUpdate(&quot;ALTER TABLE person DROP primary key&quot;);</code> will drop the primary key.
 * <p><code>driver.executeUpdate(&quot;update person set age=44, salary=3200.5 where name = 'guilherme campos hazan'&quot;);</code> will update the
 * table.
 * <p><code>driver.executeUpdate(&quot;delete person where name like 'g%'&quot;);</code> will delete records of the table.
 * <p><code> driver.executeUpdate(&quot;insert into person (age, salary, name, email)
 * values (32, 2000, 'guilherme campos hazan', 'guich@superwaba.com.br')&quot;);</code> will insert a record in the table.
 *
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The SQL update command.
 * @param p->retI Receives the number of rows affected or <code>0</code> if a drop or alter operation was successful.
 */
LB_API void lLC_executeUpdate_s(NMParams p) // litebase/LitebaseConnection public native int executeUpdate(String sql);
{
	TRACE("lLC_executeUpdate_s")
   
   MEMORY_TEST_START

   if (checkParamAndDriver(p, "sql")) // The sql can't be null and the driver can't be closed.
   {
      Context context = p->currentContext;
      TCObject driver = p->obj[0],
             sqlString = p->obj[1],
	          logger = litebaseConnectionClass->objStaticValues[1];

		if (logger)
		{
			LOCKVAR(log);
			TC_executeMethod(context, loggerLog, logger, 16, sqlString, false);
			UNLOCKVAR(log);
         if (context->thrownException)
            goto finish;
		}
      
      p->retI = litebaseExecuteUpdate(context, driver, String_charsStart(sqlString), String_charsLen(sqlString));
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Used to execute queries in a table. Example:
 * 
 * <pre>
 * ResultSet rs = driver.executeQuery(&quot;select rowid, name, salary, age from person where age != 44&quot;);
 * rs.afterLast();
 * while (rs.prev())
 *    Vm.debug(rs.getString(1) + &quot;. &quot; + rs.getString(2) + &quot; - &quot; + rs.getInt(&quot;age&quot;) + &quot; years&quot;);
 * </pre>
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The SQL query command.
 * @param p->retO Receives a result set with the values returned from the query.
 */
LB_API void lLC_executeQuery_s(NMParams p) // litebase/LitebaseConnection public native litebase.ResultSet executeQuery(String sql);  
{
	TRACE("lLC_executeQuery_s")
  
   MEMORY_TEST_START

   if (checkParamAndDriver(p, "sql")) // The sql can't be null and the driver can't be closed.
   {
      Context context = p->currentContext;
      TCObject driver = p->obj[0],
             sqlString = p->obj[1],
	          logger = litebaseConnectionClass->objStaticValues[1];
	          
      // juliana@253_18: now it is possible to log only changes during Litebase operation.
      if (logger && !litebaseConnectionClass->i32StaticValues[6])
      {
	      LOCKVAR(log);
	      TC_executeMethod(context, loggerLog, logger, 16, sqlString, false);
	      UNLOCKVAR(log);
         if (context->thrownException)
            goto finish;
      }

      TC_setObjectLock(p->retO = litebaseExecuteQuery(context, driver, String_charsStart(sqlString), String_charsLen(sqlString)), UNLOCKED);
   }
      
finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Creates a pre-compiled statement with the given sql. Prepared statements are faster for repeated queries. Instead of parsing the same query 
 * where only a few arguments change, it is better to create a prepared statement and the query is pre-parsed. Then, it is just needed to set the 
 * arguments (defined as ? in the sql) and run the sql.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The SQL query command.
 * @param p->retO Receives a pre-compiled SQL statement.
 * @throws OutOfMemoryError If there is not enough memory to create the preparedStatement.
 */
LB_API void lLC_prepareStatement_s(NMParams p) // litebase/LitebaseConnection public native litebase.PrepareStatement prepareStatement(String sql) throws OutOfMemoryError; 
{
	TRACE("lLC_prepareStatement_s")
   MEMORY_TEST_START

   if (checkParamAndDriver(p, "sql")) // The sql can't be null and the driver can't be closed.
   {  
      TCObject driver = p->obj[0],
             sqlObj = p->obj[1],
             oldSqlObj,
             logger = litebaseConnectionClass->objStaticValues[1],
             prepStmt = null;
      Context context = p->currentContext;
      Heap heapParser = null;
      LitebaseParser* parse;
      Hashtable* htPS;
	   JCharP sqlChars = String_charsStart(sqlObj),
             sqlCharsAux;
      char command[MAX_RESERVED_SIZE];
	   int32 sqlLength = String_charsLen(sqlObj),
            sqlLengthAux,
            numParams = 0,
            i,
            hashCode;
      bool isSelect = false;

      // juliana@253_18: now it is possible to log only changes during Litebase operation.
      if (logger && !litebaseConnectionClass->i32StaticValues[6]) // juliana@230_30: reduced log files size.
	   {
	      TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
         
         LOCKVAR(log);

         // Builds the logger StringBuffer contents.
         StringBuffer_count(logSBuffer) = 0;
         if (TC_appendCharP(context, logSBuffer, "prepareStatement ") && TC_appendJCharP(context, logSBuffer, sqlChars, sqlLength))
            TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation.  
    
         UNLOCKVAR(log);
         if (context->thrownException)
            goto finish;
	   }
      
      // juliana@226_16: prepared statement is now a singleton.
      // juliana@226a_21: solved a problem which could cause strange errors when using prepared statements.
      htPS = getLitebaseHtPS(driver);
      if ((prepStmt = p->retO = p->obj[0] = TC_htGetPtr(htPS, hashCode = TC_JCharPHashCode(sqlChars, sqlLength))) 
       && !OBJ_PreparedStatementDontFinalize(prepStmt) && (oldSqlObj = OBJ_PreparedStatementSqlExpression(prepStmt))
       && TC_JCharPEqualsJCharP(String_charsStart(oldSqlObj), sqlChars, String_charsLen(oldSqlObj), sqlLength))
      {
         lPS_clearParameters(p);
         goto finish;
      }

      // The prepared statement.
	   if (!(prepStmt = p->retO = TC_createObject(context, "litebase.PreparedStatement")))
		   goto finish;
	   OBJ_PreparedStatementDriver(prepStmt) = driver;
	   OBJ_PreparedStatementSqlExpression(prepStmt) = sqlObj;
      
      // Only parses commands that create statements.
      sqlLengthAux = sqlLength;
      sqlCharsAux = str16LeftTrim(sqlChars, &sqlLengthAux);
      TC_CharPToLower(TC_JCharP2CharPBuf(sqlCharsAux, 8, command));
      if (!sqlLengthAux) // juliana@230_20
      {
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_SYNTAX_ERROR));
         goto finish;
      }
      
      if (xstrstr(command, "create"))
         OBJ_PreparedStatementType(p->retO) = CMD_CREATE_TABLE;
      else if (xstrstr(command, "delete") || xstrstr(command, "insert") || (isSelect = (xstrstr(command, "select") != null)) || xstrstr(command, "update"))
      {
         bool locked = false;
         Table* table;
         
         heapParser = heapCreate();
	      IF_HEAP_ERROR(heapParser)
         {
		      if (locked)
               UNLOCKVAR(parser);
            TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            
free:
            heapDestroy(heapParser);
            goto finish;
         }

         // Parses the sql string.
	      locked = true;
	      LOCKVAR(parser);
	      parse = initLitebaseParser(context, sqlChars, sqlLength, isSelect, heapParser);
         UNLOCKVAR(parser);
	      locked = false;
         
         // Error checking.
         if (!parse)
            goto free;
         IF_HEAP_ERROR(heapParser)
         {
            TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            goto free;
         }

         // juliana@226_15: corrected a bug that would make a prepared statement with where clause and indices not work correctly after the first 
         // execution.
         switch (parse->command) // Gets the command in the SQL expression and creates the apropriate statement.
         {
            case CMD_DELETE:
            {
               SQLDeleteStatement* deleteStmt = initSQLDeleteStatement(parse, true);  
               
               if (litebaseBindDeleteStatement(context, driver, deleteStmt))
			      {
				      SQLBooleanClause* whereClause = deleteStmt->whereClause;

                  if (whereClause)
                     whereClause->expressionTreeBak = cloneTree(whereClause->expressionTree, null, heapParser);
                  
                  table = deleteStmt->rsTable->table;
				      IF_HEAP_ERROR(table->heap)
                  {
                     TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
                     goto free;
                  }
                  OBJ_PreparedStatementType(prepStmt) = CMD_DELETE;
                  setPreparedStatementStatement(prepStmt, deleteStmt);
			         table->preparedStmts = TC_TCObjectsAdd(table->preparedStmts, prepStmt, table->heap);
			      }
			      else
                  goto free;
               break;
            }

            case CMD_INSERT:
            {
               SQLInsertStatement* insertStmt = initSQLInsertStatement(context, driver, parse);
               
               if (!insertStmt || !litebaseBindInsertStatement(context, insertStmt))
                  goto free;

			      OBJ_PreparedStatementType(prepStmt) = CMD_INSERT;
			      table = insertStmt->table;
               IF_HEAP_ERROR(table->heap)
               {
                  TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
                  goto free;
               }
               setPreparedStatementStatement(prepStmt, insertStmt);
			      table->preparedStmts = TC_TCObjectsAdd(table->preparedStmts, prepStmt, table->heap);
               break;
            }

            case CMD_SELECT:
            {
               SQLSelectStatement* selectStmt = initSQLSelectStatement(parse, true);

               if (litebaseBindSelectStatement(context, driver, selectStmt))
			      {
                  SQLSelectClause* selectClause = selectStmt->selectClause;
				      SQLResultSetTable** tableList = selectClause->tableList;
                  int32 len = selectClause->tableListSize;
                  SQLBooleanClause* whereClause = selectStmt->whereClause;
                  SQLColumnListClause* orderByClause = selectStmt->orderByClause;
                  SQLColumnListClause* groupByClause = selectStmt->groupByClause;
                  SQLResultSetField** fieldList;
                  uint8* fieldTableColIndexesBak;
                  Heap heap = selectClause->heap;
                  int32 count;

                  IF_HEAP_ERROR(heap)
                  {
                     TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
                     goto free;
                  }

                  if (orderByClause)
                  {
                     fieldList = orderByClause->fieldList;
                     count = orderByClause->fieldsCount;
                     fieldTableColIndexesBak = orderByClause->fieldTableColIndexesBak = TC_heapAlloc(heap, count);
                     while (--count >= 0)
                        fieldTableColIndexesBak[count] = fieldList[count]->tableColIndex;
                  }

                  // juliana@226_14: corrected a bug that would make a prepared statement with group by not work correctly after the first execution.
                  if (groupByClause)
                  {
                     fieldList = groupByClause->fieldList;
                     count = groupByClause->fieldsCount;
                     fieldTableColIndexesBak = groupByClause->fieldTableColIndexesBak = TC_heapAlloc(heap, count);
                     while (--count >= 0)
                        fieldTableColIndexesBak[count] = fieldList[count]->tableColIndex;
                  }

                  if (whereClause)
                     whereClause->expressionTreeBak = cloneTree(whereClause->expressionTree, null, heapParser);

				      OBJ_PreparedStatementType(prepStmt) = CMD_SELECT;
				      setPreparedStatementStatement(prepStmt, selectStmt);
			         selectStmt->selectClause->sqlHashCode = TC_JCharPHashCode(sqlChars, sqlLength);
				      while (--len >= 0)
				      {
					      table = tableList[len]->table;
                     IF_HEAP_ERROR(table->heap)
                     {
                        TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
                        goto free;
                     }
				         table->preparedStmts = TC_TCObjectsAdd(table->preparedStmts, prepStmt, table->heap);
				      }
			      }
			      else
                  goto free;
               break;
            }

            case CMD_UPDATE:
            {
               SQLUpdateStatement* updateStmt = initSQLUpdateStatement(context, driver, parse, true);
               SQLBooleanClause* whereClause;

               if (!updateStmt || !litebaseBindUpdateStatement(context, updateStmt))
                  goto free;

               if ((whereClause = (updateStmt->whereClause)))
                  whereClause->expressionTreeBak = cloneTree(whereClause->expressionTree, null, heapParser);

               OBJ_PreparedStatementType(prepStmt) = CMD_UPDATE;
			      table = updateStmt->rsTable->table;
               IF_HEAP_ERROR(table->heap)
               {
                  TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
                  goto free;
               }
               setPreparedStatementStatement(prepStmt, updateStmt);
		         table->preparedStmts = TC_TCObjectsAdd(table->preparedStmts, prepStmt, table->heap);
            }
         }
      }

      if ((i = sqlLength)) // Tokenizes the sql string looking for '?'.
         while (--i)
            if (sqlChars[i] == '?')
               numParams++;

      // juliana@222_8: an array to hook the prepared statement object parameters.
      if (!(OBJ_PreparedStatementObjParams(prepStmt) = TC_createArrayObject(context, "[java.lang.Object", numParams)))
         goto finish;
      TC_setObjectLock(OBJ_PreparedStatementObjParams(prepStmt), UNLOCKED);
      
      // If the statement is to be used as a prepared statement, it is possible to use log.
      if (getPreparedStatementStatement(prepStmt) && logger) 
      {
         int16* paramsPos;
         int16* paramsLength;
         JCharP* paramsAsStrs;
         
         if (numParams > 0)
         {
            // Creates the array of parameters.
            setPreparedStatementParamsAsStrs(prepStmt, (paramsAsStrs = (JCharP*)TC_heapAlloc(heapParser, numParams * TSIZE)));
            
            // Creates the array of the parameters length
            setPreparedStatementParamsLength(prepStmt, (paramsLength = (int16*)TC_heapAlloc(heapParser, numParams << 1)));

            i = numParams;

			   // juliana@201_15: The prepared statement parameters for logging must be set as "unfilled" when creating it.
			   while (--i >= 0)
            {
               if (!(paramsAsStrs[i] = TC_CharP2JCharP("unfilled", 8)))
               {
                  TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
                  goto finish;
               }
               paramsLength[i] = 8;
            }

            OBJ_PreparedStatementStoredParams(prepStmt) = numParams;
         }

         // The array of positions of the '?' in the sql.
         setPreparedStatementParamsPos(prepStmt, (paramsPos = (int16*)TC_heapAlloc(heapParser, (numParams + 1) << 1)));

         // Marks the positions of the '?'.
         paramsPos[numParams] = sqlLength;
         while (--sqlLength >= 0)
            if (sqlChars[sqlLength] == '?')
               paramsPos[--numParams] = sqlLength;
      }
      if (!TC_htPutPtr(htPS, hashCode, prepStmt))
         TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);

finish: ;

      // juliana@230_19: removed some possible memory problems with prepared statements and ResultSet.getStrings().
      if (context->thrownException && prepStmt)
         freePreparedStatement(0, prepStmt);
   }
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// litebase/LitebaseConnection public native int getCurrentRowId(String tableName) throws IllegalStateException, NullPointerException;
/**
 * Returns the current rowid for a given table.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->retI Receives the current rowid for the table.
 */
LB_API void lLC_getCurrentRowId_s(NMParams p) 
{
	TRACE("lLC_getCurrentRowId_s")
	
   MEMORY_TEST_START

   if (checkParamAndDriver(p, "tableName")) // The driver can't be closed and the table name can't be null.
   {
      Context context = p->currentContext;
      TCObject driver = p->obj[0],  
             tableName = p->obj[1],
	          logger = litebaseConnectionClass->objStaticValues[1];
      Table* table;

	   // juliana@253_18: now it is possible to log only changes during Litebase operation.
      if (logger && !litebaseConnectionClass->i32StaticValues[6]) // juliana@230_30: reduced log files size.
	   {
		   TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
      
         LOCKVAR(log);

         // Builds the logger StringBuffer contents.
         StringBuffer_count(logSBuffer) = 0;
         if (TC_appendCharP(context, logSBuffer, "getCurrentRowId ") 
          && TC_appendJCharP(context, logSBuffer, String_charsStart(tableName), String_charsLen(tableName)))
            TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation.  
        
         UNLOCKVAR(log);
         if (context->thrownException)
            goto finish;
	   }

      if ((table = getTableFromName(context, driver, tableName)))
         p->retI = table->currentRowId;     
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// litebase/LitebaseConnection public native int getRowCount(String tableName) throws IllegalStateException, NullPointerException;
/**
 * Returns the number of valid rows in a table. This may be different from the number of records if a row has been deleted.
 * 
 * @see #getRowCountDeleted(String)
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->retI Receives the number of valid rows in a table.
 */
LB_API void lLC_getRowCount_s(NMParams p) 
{
	TRACE("lLC_getRowCount_s")

   MEMORY_TEST_START

   if (checkParamAndDriver(p, "tableName")) // The driver can't be closed and the table name can't be null.
   {
      Context context = p->currentContext;
      TCObject driver = p->obj[0],
             tableName = p->obj[1],
	          logger = litebaseConnectionClass->objStaticValues[1];
      Table* table;

		// juliana@253_18: now it is possible to log only changes during Litebase operation.
      if (logger && !litebaseConnectionClass->i32StaticValues[6]) // juliana@230_30: reduced log files size.
		{
			TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
      
         LOCKVAR(log);

         // Builds the logger StringBuffer contents.
         StringBuffer_count(logSBuffer) = 0;
         if (TC_appendCharP(context, logSBuffer, "getRowCount ") 
          && TC_appendJCharP(context, logSBuffer, String_charsStart(tableName), String_charsLen(tableName)))
            TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation.  
         
         UNLOCKVAR(log);         
         if (context->thrownException)
            goto finish;
		}

      if ((table = getTableFromName(context, driver, tableName)))
	      p->retI = table->db.rowCount - table->deletedRowsCount;     
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// litebase/LitebaseConnection public native void setRowInc(String tableName, int inc) throws IllegalArgumentException;
/**
 * Sets the row increment used when creating or updating big amounts of data. Using this method greatly increases the speed of bulk insertions 
 * (about 3x faster). To use it, it is necessary to call it (preferable) with the amount of lines that will be inserted. After the insertion is 
 * finished, it is <b>NECESSARY</b> to call it again, passing <code>-1</code> as the increment argument. Without doing this last step, data may be
 * lost because some writes will be delayed until the method is called with -1. Another good optimization on bulk insertions is to drop the indexes
 * and then create them afterwards. So, to correctly use <code>setRowInc()</code>, it is necessary to:
 *
 * <pre>
 * driver.setRowInc(&quot;table&quot;, totalNumberOfRows);
 * // Fetch the data and insert them.
 * driver.setRowInc(&quot;table&quot;, -1);
 * </pre>
 *
 * Using prepared statements on insertion makes it another a couple of times faster.
 *
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->i32[0] The increment value.
 * @throws IllegalArgumentException If the increment is equal to 0 or less than -1.
 */
LB_API void lLC_setRowInc_si(NMParams p) 
{
	TRACE("lLC_setRowInc_si")

   MEMORY_TEST_START

   if (checkParamAndDriver(p, "tableName")) // The driver can't be closed and the table name can't be null.
   {
      Context context = p->currentContext;
      TCObject driver = p->obj[0],
             tableName = p->obj[1],
	          logger = litebaseConnectionClass->objStaticValues[1];
      Table* table;
      int32 inc = p->i32[0];
      
      if (!inc || inc < -1)
      {
         TC_throwExceptionNamed(context, "java.lang.IllegalArgumentException", getMessage(ERR_INVALID_INC));
         goto finish;
      }

      if (logger) // juliana@230_30: reduced log files size.
		{
			TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
         IntBuf intBuf;
         
         LOCKVAR(log);

         // Builds the logger StringBuffer contents.
         StringBuffer_count(logSBuffer) = 0;
         if (TC_appendCharP(context, logSBuffer, "setRowInc ")
          && TC_appendJCharP(context, logSBuffer, String_charsStart(tableName), String_charsLen(tableName))
          && TC_appendCharP(context, logSBuffer, " ") && TC_appendCharP(context, logSBuffer, TC_int2str(inc, intBuf)))
            
         TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation.  
         
         UNLOCKVAR(log);
         if (context->thrownException)
            goto finish;
		}
      
      if ((table = getTableFromName(context, driver, tableName)))
      {
         bool setting = inc != -1;
         int32 i = table->columnCount;
		   PlainDB* plainDB = &table->db;
         Index** columnIndexes = table->columnIndexes;
		   ComposedIndex** composedIndexes = table->composedIndexes;
         XFile* dbFile = &plainDB->db;
         XFile* dboFile = &plainDB->dbo;

         plainDB->rowInc = setting? inc : DEFAULT_ROW_INC;
         while (--i >= 0) // Flushes the simple indices.
            if (columnIndexes[i])
               indexSetWriteDelayed(context, columnIndexes[i], setting);
			
		   // juliana@202_18: The composed indices must also be written delayed when setting row increment to a value different to -1.
		   i = table->numberComposedIndexes;
		   while (--i >= 0)
			   indexSetWriteDelayed(context, composedIndexes[i]->index, setting);

         // juliana@227_3: improved table files flush dealing.
		   if (inc == -1) // juliana@202_17: Flushs the files to disk when setting row increment to -1.
		   {
            dbFile->dontFlush = dboFile->dontFlush = false;
            if (dbFile->cacheIsDirty)
			      flushCache(context, dbFile); // Flushs .db.
            if (dboFile->cacheIsDirty)
			      flushCache(context, dboFile); // Flushs .dbo.
		   }
         else
            dbFile->dontFlush = dboFile->dontFlush = true;
      }
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Indicates if the given table already exists. This method can be used before a drop table.
 *
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->retI Receives <code>true</code> if a table exists; <code>false</code> othewise.
 * @throws DriverException If tableName or path is too big.
 */
LB_API void lLC_exists_s(NMParams p) // litebase/LitebaseConnection public native boolean exists(String tableName) throws DriverException; 
{
	TRACE("lLC_exists_s")

   MEMORY_TEST_START

   if (checkParamAndDriver(p, "tableName")) // The driver can't be closed and the table name can't be null.
   {
      TCObject driver = p->obj[0],
             tableNameObj = p->obj[1];
      char tableNameCharP[DBNAME_SIZE],
           bufName[DBNAME_SIZE];
      TCHAR fullName[MAX_PATHNAME];
   
      if (String_charsLen(tableNameObj) > MAX_TABLE_NAME_LENGTH)
         TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_MAX_TABLE_NAME_LENGTH));
      else
      {
         int32 length = String_charsLen(tableNameObj);
         TCHARP sourcePath = getLitebaseSourcePath(driver);

         // juliana@252_3: corrected a possible crash if the path had more than 255 characteres.
         if (length + tcslen(sourcePath) + 10 > MAX_PATHNAME)
         {
            char buffer[1024];
            TC_TCHARP2CharPBuf(sourcePath, buffer);
            TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_INVALID_PATH), buffer);
         }
         else
         {
            TC_JCharP2CharPBuf(String_charsStart(tableNameObj), length, tableNameCharP);
            getDiskTableName(p->currentContext, OBJ_LitebaseAppCrid(driver), tableNameCharP, bufName);
            xstrcat(bufName, DB_EXT);
            getFullFileName(bufName, sourcePath, fullName);
            p->retI = lbfileExists(fullName);
         
            // juliana@253_10: now a DriverException will be thown if the .db file exists but not .dbo.
            length = tcslen(fullName);
            fullName[length] = 'o';
            fullName[length + 1] = 0;
            if (p->retI && !lbfileExists(fullName))
            {
               char path[MAX_PATHNAME];
               TC_TCHARP2CharPBuf(fullName, path);
               TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_INVALID_PATH), path);                      
            }
         }
      }
   }

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Releases the file handles (on the device) of a Litebase instance. Note that, after this is called, all <code>Resultset</code>s and 
 * <code>PreparedStatement</code>s created with this Litebase instance will be in an inconsistent state, and using them will probably reset the 
 * device. This method also deletes the active instance for this creator id from Litebase's internal table.
 *
 * @param p->obj[0] The connection with Litebase.
 * @throws IllegalStateException If the driver is closed.
 */
LB_API void lLC_closeAll(NMParams p) // litebase/LitebaseConnection public native void closeAll() throws IllegalStateException;
{
	TRACE("lLC_closeAll")
	TCObject driver = p->obj[0];
   Context context = p->currentContext;
   
   MEMORY_TEST_START
   
   if (OBJ_LitebaseDontFinalize(driver)) // The driver can't be closed.
      TC_throwExceptionNamed(context, "java.lang.IllegalStateException", getMessage(ERR_DRIVER_CLOSED));
   else
   {
      TCObject logger = litebaseConnectionClass->objStaticValues[1];

      if (logger) // juliana@230_30: reduced log files size.
	   {
		   TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
      
         LOCKVAR(log);

         // Builds the logger StringBuffer contents.
         StringBuffer_count(logSBuffer) = 0;
         if (TC_appendCharP(context, logSBuffer, "closeAll"))
            TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation.  
         
         UNLOCKVAR(log);
         if (context->thrownException)
            goto finish;
	   }

finish: // juliana@214_7: must free Litebase even if the log string creation fails.
      freeLitebase(context, (size_t)driver);
      xfree(context->litebasePtr);	   
   }
   	
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// juliana@201_13: .dbo is now being purged.
// litebase/LitebaseConnection public native int purge(String tableName) throws DriverException, OutOfMemoryError;
// juliana@253_8: now Litebase supports weak cryptography.
/**
 * Used to delete physically the records of the given table. Records are always deleted logically, to avoid the need of recreating the indexes. When 
 * a new record is added, it doesn't uses the position of the previously deleted one. This can make the table big, if a table is created, filled and 
 * has a couple of records deleted. This method will remove all deleted records and recreate the indexes accordingly. Note that it can take some time 
 * to run.
 * <p>
 * Important: the rowid of the records is NOT changed with this operation.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The table name to purge.
 * @param p->retI Receives the number of purged records.
 * @throws DriverException If a row can't be read or written.
 * @throws OutOfMemoryError If there is not enough memory to purge the table.
 */
LB_API void lLC_purge_s(NMParams p) 
{
	TRACE("lLC_purge_s")

   MEMORY_TEST_START

   if (checkParamAndDriver(p, "tableName")) // The driver can't be closed and the table name can't be null.
   {
      Context context = p->currentContext;
      TCObject driver = p->obj[0],
             tableName = p->obj[1],
             logger = litebaseConnectionClass->objStaticValues[1];
      Table* table = getTableFromName(context, driver, tableName);
      int32 deleted = 0;

      if (logger) // juliana@230_30: reduced log files size.
	   {
		   TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
      
         LOCKVAR(log);

         // Builds the logger StringBuffer contents.
         StringBuffer_count(logSBuffer) = 0;
         if (TC_appendCharP(context, logSBuffer, "purge ")
          && TC_appendJCharP(context, logSBuffer, String_charsStart(tableName), String_charsLen(tableName)))   
            TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation.  
         
         UNLOCKVAR(log);
         if (context->thrownException)
            goto finish;
      }

      // juliana@270_27: now purge will also really purge the table if it only suffers updates.
      if (table && ((deleted = table->deletedRowsCount) > 0 || table->wasUpdated)) // Removes the deleted records from the table.
      {
         PlainDB* plainDB = &table->db;
         XFile* dbFile = &plainDB->db;
         Index** columnIndexes = table->columnIndexes;
         ComposedIndex** composedIndexes = table->composedIndexes;
         int32 willRemain = plainDB->rowCount - deleted,
               columnCount = table->columnCount,
               i;

         // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified 
         // since its last opening. 
         if (!setModified(context, table))
            goto finish;

         if (willRemain) 
         {
            // rnovais@570_75: inserts all records at once.
            // juliana@223_12: purge now only recreates the .dbo file, reusing .db file on Windows 32, Windows CE, Palm, iPhone, and Android.
            char buffer[DBNAME_SIZE];
            XFile newdbo,
                  olddbo;
            uint8* basbuf = plainDB->basbuf;
            uint8* columnNulls0 = table->columnNulls;
            int8* columnTypes = table->columnTypes;
            uint16* columnOffsets = table->columnOffsets;
            int32* columnSizes = table->columnSizes;
            int32 rowCount = plainDB->rowCount,
                  id,
                  j,
                 
            // juliana@230_12: improved recover table to take .dbo data into consideration.
                  k,
                  crc32,
                  dataLength,
                  length = table->columnOffsets[columnCount] + NUMBEROFBYTES(columnCount),
                  remain = 0,
                  type,
                  numberOfBytes = NUMBEROFBYTES(columnCount);
            bool useCrypto = dbFile->useCrypto;
            TCHARP sourcePath = getLitebaseSourcePath(driver);
            SQLValue* record[MAXIMUMS + 1];
            Heap heap = heapCreate(); 

            IF_HEAP_ERROR(heap)
            {
               TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
               
free:                  
               heapDestroy(heap);
               goto finish;
            }

            // Allocates the temporary records.
            i = columnCount;
		      while (--i >= 0)
		      {
			      record[i] = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));
			      if (columnTypes[i] == CHARS_TYPE || columnTypes[i] == CHARS_NOCASE_TYPE)
				      record[i]->asChars = (JCharP)TC_heapAlloc(heap, (columnSizes[i] << 1) + 2); 
               else if (columnTypes[i] == BLOB_TYPE)
				      record[i]->asBlob = (uint8*)TC_heapAlloc(heap, columnSizes[i]);
		      }

            // rnovais@570_61: verifies if it needs to store the currentRowId.
		      if (plainRead(context, plainDB, rowCount - 1))
            {
               xmove4(&id, basbuf); 
               if ((id & ROW_ATTR_MASK) == ROW_ATTR_DELETED) // Is the last record deleted?
                  table->auxRowId = table->currentRowId; // rnovais@570_61
            }
            else
               goto free;
            
            // Creates the temporary .dbo file.
            xstrcpy(buffer, plainDB->dbo.name);
            xstrcat(buffer, "_");
            if (!nfCreateFile(context, buffer, true, useCrypto, sourcePath, &newdbo, -1)) // Creates the new .dbo file.
               goto free;

		      plainDB->rowInc = willRemain;
            i = -1;
            while (++i < rowCount)
            {
			      if (!readRecord(context, table, record, i, columnNulls0, null, 0, false, null, null)) // juliana@227_20
                  goto free;

			      xmove4(&record[0]->asInt, plainDB->basbuf); 
               if ((record[0]->asInt & ROW_ATTR_MASK) != ROW_ATTR_DELETED) // is record ok?
               {
                  xmemmove(&olddbo, &plainDB->dbo, sizeof(XFile));
                  xmemmove(&plainDB->dbo, &newdbo, sizeof(XFile));
                  
                  // juliana@225_3: corrected a possible "An attempt was made to move the file pointer before the beginning of the file." on 
                  // some Windows CE devices when doing a purge.
                  j = -1;
                  while (++j < columnCount)
					      if (!writeValue(context, plainDB, record[j], &basbuf[columnOffsets[j]], columnTypes[j], columnSizes[j], true, true, false, 
                                                                                                                                         false))
                     {
                        nfRemove(context, &newdbo, sourcePath);
                        xmemmove(&plainDB->dbo, &olddbo, sizeof(XFile));
                        goto free;
                     }
						
				      xmemmove(&basbuf[columnOffsets[j]], columnNulls0, numberOfBytes); 
						
                  // juliana@223_8: corrected a bug on purge that would not copy the crc32 codes for the rows.
                  // juliana@220_4: added a crc32 code for every record. Please update your tables.
                  j = basbuf[3];
                  basbuf[3] = 0; // juliana@222_5: The crc was not being calculated correctly for updates.
                  
                  // juliana@230_12: improved recover table to take .dbo data into consideration.
                  crc32 = updateCRC32(basbuf, length, 0);
                  
                  if (table->version == VERSION_TABLE)
                  {
                     k = columnCount;
                     while (--k >= 0)
                        if (((type = columnTypes[k]) == CHARS_TYPE || type == CHARS_NOCASE_TYPE) && isBitUnSet(columnNulls0, k))
                           crc32 = updateCRC32((uint8*)record[k]->asChars, record[k]->length << 1, crc32);
                        else if (type == BLOB_TYPE && isBitUnSet(columnNulls0, k))
                        {
                           dataLength = record[k]->length;
                           crc32 = updateCRC32((uint8*)&dataLength, 4, crc32);
                        }
                  }
                  
                  xmove4(&basbuf[length], &crc32); // Computes the crc for the record and stores at the end of the record.
                  basbuf[3] = j;

				      if (!plainRewrite(context, plainDB, remain++))
                     goto free;
                  xmemmove(&newdbo, &plainDB->dbo, sizeof(XFile));
                  xmemmove(&plainDB->dbo, &olddbo, sizeof(XFile));
               }
            }
            
            if (!nfRemove(context, &olddbo, sourcePath) || !nfRename(context, &newdbo, olddbo.name, sourcePath))
               goto free;
		      xmemmove(&plainDB->dbo, &newdbo, sizeof(XFile));
            plainDB->rowInc = DEFAULT_ROW_INC;
            plainDB->rowCount = remain;
            heapDestroy(heap);

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
#if defined(POSIX) || defined(ANDROID)
            removeFileFromList(&newdbo);
#endif

         }
         else // If no rows will remain, just deletes everyone.
         {
            XFile* dbo = &plainDB->dbo;

            if ((i = lbfileSetSize(&dbFile->file, 0)) || (i = lbfileSetSize(&dbo->file, 0)))
            {
               fileError(context, i, dbFile->name);
               goto finish;
            }
            
            dbo->finalPos = dbFile->finalPos = dbFile->size = dbo->size = plainDB->rowAvail = plainDB->rowCount = 0;
         }

         table->deletedRowsCount = 0; // Empties the deletedRows.  
         table->wasUpdated = false; // juliana@270_27: now purge will also really purge the table if it only suffers updates.

         // Recreates the simple indices.
         i = table->columnCount;
         while (--i >= 0)

            // juliana@202_14: Corrected the simple index re-creation when purging the table. 
            if (columnIndexes[i] && !tableReIndex(context, table, i, false, null))
               goto finish;

         // recreate the composed indexes
         if ((i = table->numberComposedIndexes) > 0)
            while (--i >= 0)
               if (!tableReIndex(context, table, -1, false, composedIndexes[i]))
                  goto finish;

         // juliana@115_8: saving metadata before recreating the indices does not let .db header become empty.
         // Updates the metadata.
         plainDB->useOldCrypto = false;
         if (!tableSaveMetaData(context, table, TSMD_EVERYTHING)) // guich@560_24 table->saveOnExit = 1;
            goto finish; 

         // juliana@227_16: purge must truncate the .db file and flush .dbo file in order to ensure that a future recoverTable() won't corrupt the 
         // table.
         if (plainDB->dbo.cacheIsDirty && !flushCache(context, &plainDB->dbo)) // Flushs .dbo.
            goto finish;
         
         // juliana@250_6: corrected a bug on LitebaseConnection.purge() that could corrupt the table.
         plainDB->rowAvail = 0;
         if ((i = lbfileSetSize(&dbFile->file, dbFile->size = plainDB->rowCount * plainDB->rowSize + plainDB->headerSize))
          || (i = lbfileFlush(dbFile->file)))
         {
            fileError(context, i, dbFile->name);
            goto finish;
         }
      }
      p->retI = deleted;
      
   }
     
finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Returns the number of deleted rows.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->retI Receives the total number of deleted records of the given table.
 */
LB_API void lLC_getRowCountDeleted_s(NMParams p) // litebase/LitebaseConnection public native int getRowCountDeleted(String tableName); 
{
	TRACE("lLC_getRowCountDeleted_s")

   MEMORY_TEST_START

   if (checkParamAndDriver(p, "tableName")) // The driver can't be closed and the table name can't be null.
   {
      Context context = p->currentContext;
      TCObject driver = p->obj[0],
             tableName = p->obj[1],
	          logger = litebaseConnectionClass->objStaticValues[1];
      Table* table;

		// juliana@253_18: now it is possible to log only changes during Litebase operation.
      if (logger && !litebaseConnectionClass->i32StaticValues[6]) // juliana@230_30: reduced log files size.
		{
			TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
      
         LOCKVAR(log);

         // Builds the logger StringBuffer contents.
         StringBuffer_count(logSBuffer) = 0;
         if (TC_appendCharP(context, logSBuffer, "getRowCountDeleted ")
          && TC_appendJCharP(context, logSBuffer, String_charsStart(tableName), String_charsLen(tableName)))  
            TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation.  
         
         UNLOCKVAR(log);
         if (context->thrownException)
            goto finish;
		}

      if ((table = getTableFromName(context, driver, tableName)))
	      p->retI = table->deletedRowsCount;
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Gets an iterator for a table. With it, it is possible iterate through all the rows of a table in sequence and get its attributes. This is good for
 * synchronizing a table. While the iterator is active, it is not possible to do any queries or updates because this can cause dada corruption.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->retO receives a iterator for the given table.
 */
LB_API void lLC_getRowIterator_s(NMParams p) // litebase/LitebaseConnection public native litebase.RowIterator getRowIterator(String tableName); 
{
	TRACE("lLC_getRowIterator_s")

   MEMORY_TEST_START

   if (checkParamAndDriver(p, "tableName")) // The driver can't be closed and the table name can't be null.
   {
      Context context = p->currentContext;
      TCObject driver = p->obj[0],
             tableName = p->obj[1],
	          logger = litebaseConnectionClass->objStaticValues[1];
      Table* table = getTableFromName(context, driver, tableName);

	   // juliana@253_18: now it is possible to log only changes during Litebase operation.
      if (logger && !litebaseConnectionClass->i32StaticValues[6]) // juliana@230_30: reduced log files size.
	   {
		   TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
      
         LOCKVAR(log);

         // Builds the logger StringBuffer contents.
         StringBuffer_count(logSBuffer) = 0;
         if (TC_appendCharP(context, logSBuffer, "getRowIterator ")
          && TC_appendJCharP(context, logSBuffer, String_charsStart(tableName), String_charsLen(tableName)))
            TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation.  
         
         UNLOCKVAR(log);
         if (context->thrownException)
            goto finish;
	   }
    
      if (table)
      {
         TCObject rowIterator = p->retO = TC_createObject(context, "litebase.RowIterator");

         // Creates and populates the row iterator object.
         if (rowIterator)
         {
            TC_setObjectLock(rowIterator, UNLOCKED);
            setRowIteratorTable(rowIterator, table);
            OBJ_RowIteratorRowNumber(rowIterator) = -1;
            OBJ_RowIteratorData(rowIterator) = TC_createArrayObject(context, BYTE_ARRAY, table->db.rowSize);
            OBJ_RowIteratorDriver(rowIterator) = driver;
            TC_setObjectLock(OBJ_RowIteratorData(rowIterator), UNLOCKED);
         }

      }
   }
   
finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Gets the Litebase logger. The fields should be used unless using the logger within threads. 
 * 
 * @param p->retO receives the logger.
 */
LB_API void lLC_privateGetLogger(NMParams p) // litebase/LitebaseConnection public static native totalcross.util.Logger getLogger(); 
{
	TRACE("lLC_privateGetLogger")
	MEMORY_TEST_START
	LOCKVAR(log); 
   p->retO = litebaseConnectionClass->objStaticValues[1];
	UNLOCKVAR(log);
	MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Sets the litebase logger. This enables log messages for all queries and statements of Litebase and can be very useful to help finding bugs in 
 * the system. Logs take up memory space, so turn them on only when necessary. The fields should be used unless using the logger within threads.
 * 
 * @param p->obj[0] The logger.
 */
LB_API void lLC_privateSetLogger_l(NMParams p) // litebase/LitebaseConnection public static native void setLogger(totalcross.util.Logger logger);
{
	TRACE("lLC_privateSetLogger_l")
   MEMORY_TEST_START
	LOCKVAR(log); 
	litebaseConnectionClass->objStaticValues[1] = p->obj[0];
	UNLOCKVAR(log);
	MEMORY_TEST_END
}

// juliana@230_4: Litebase default logger is now a plain text file instead of a PDB file.                                                                                             
//////////////////////////////////////////////////////////////////////////                                                                           
 // litebase/LitebaseConnection public static native totalcross.util.Logger getDefaultLogger() throws DriverException;
/**                                                                                                                                                  
 * Gets the default Litebase logger. When this method is called for the first time, a new text file is created. In the subsequent calls, the same    
 * file is used.                                                                                                                                     
 *                                                                                                                                                   
 * @param p->retI receives the number of files deleted.
 * @throws DriverException if an <code>IOException</code> occurs.                                                                                              
 */                                                                                                                                                  
LB_API void lLC_privateGetDefaultLogger(NMParams p)   
{                                                                                                                                                    
	TRACE("lLC_privateGetDefaultLogger")                                                                                                               
   Context context = p->currentContext;                                                                                                              
   TCObject nameStr,                                                                                                                                   
          logger,                                                                                                                                    
          file = null;                                                                                                                                      
   TCHAR name[MAX_PATHNAME];                                                                                                                     
                                                                                                                                                     
   MEMORY_TEST_START                                                                                                                                 
	LOCKVAR(log);                                                                                                                                      
	                                                                                                                                                   
   // Creates the logger string.                                                                                                                     
   // juliana@225_10: Corrected a possible crash when using the default logger.                                                                      
   if (!(nameStr = TC_createStringObjectFromCharP(context, "litebase", 8))                                                                          
    || !(p->retO = logger = TC_executeMethod(context, getLogger, nameStr, -1, null).asObj)                                                                                                                                                                                             
    || context->thrownException)                                                                                                                     
      goto finish;                                                                                                                                   
                                                                                                                                                     
	if (!FIELD_I32(FIELD_OBJ(logger, loggerClass, 1), 0)) // Only gets a new default logger if no one exists.                                          
	{                                                                                                                                                  
		LongBuf timeLong;                                                                                                                                
		char strAppId[5];                                                                                                                                
      int32 year,                                                                                                                                    
            month,                                                                                                                                   
            day,                                                                                                                                     
            hour,                                                                                                                                    
            minute,                                                                                                                                  
            second,                                                                                                                                  
            millis;                                                                                                                                  
                                                                                                                                                     
      getCurrentPath(name);                                                                                                                     
      tcscat(name, TEXT("/LITEBASE_"));
      TC_getDateTime(&year, &month, &day, &hour, &minute, &second, &millis);                                                                         
      TC_CharP2TCHARPBuf(TC_long2str(getTimeLong(year, month, day, hour, minute, second), timeLong), &name[tcslen(name)]);
      tcscat(name, TEXT("."));
      TC_int2CRID(TC_getApplicationId(), strAppId);                                                                                                  
      TC_CharP2TCHARPBuf(strAppId, &name[tcslen(name)]);
      tcscat(name, TEXT(".LOGS"));
		TC_setObjectLock(nameStr, UNLOCKED);
		nameStr = null;                                                                                                                     
		if (!(file = TC_createObject(context, "totalcross.io.File"))                                                     
       || !(nameStr = TC_createStringObjectFromTCHARP(context, name, -1)))                                                                       
         goto finish;                                                                                                                                
         
      FIELD_OBJ(p->obj[0] = file, OBJ_CLASS(file), 0) = p->obj[1] = nameStr; // path
      FIELD_I32(file, 1) = p->i32[0] = CREATE_EMPTY; // mode 
      FIELD_I32(file, 2) = p->i32[1] = -1; // slot                                                                                                                                
		TC_tiF_create_sii(p);
      if (context->thrownException)                                                                                                                  
         goto finish;                                                                                                                                
                                                                                                                                                     
		TC_executeMethod(context, addOutputHandler, logger, file);                                                                                       
      if (context->thrownException)                                                                                                                  
         goto finish;                                                                                                                                
                                                                                                                                                     
	}                                                                                                                                                  
                                                                                                                                                     
	FIELD_I32(logger, 0) = 16;                                                                                                                         
   p->retO = logger;                                                                                                                                 
                                                                                                                                                     
finish: ;                                                                                                                                            
   TC_setObjectLock(file, UNLOCKED);                                                                                                                 
   TC_setObjectLock(nameStr, UNLOCKED); 
   
   // juliana@230_23: now LitebaseConnection.getDefaultLogger() will throw a DriverException instead of an IOException if a file error occurs.
   if (context->thrownException && TC_areClassesCompatible(context, OBJ_CLASS(context->thrownException), "totalcross.io.IOException"))                                                                                                             
   {
      TCObject exception = context->thrownException,
			    exceptionMsg = FIELD_OBJ(exception, OBJ_CLASS(exception), 0);
      char msgError[1024];
      
      if (exceptionMsg)
      {
         int32 length = String_charsLen(exceptionMsg);
         TC_JCharP2CharPBuf(String_charsStart(exceptionMsg), length < 1024? length : 1023, msgError);
	   }
	   else
	      xstrcpy(msgError, "null");
      
      context->thrownException = null;
      TC_throwExceptionNamed(context, "litebase.DriverException", msgError);

      if (strEq(OBJ_CLASS(context->thrownException)->name, "litebase.DriverException"))
		   OBJ_DriverExceptionCause(context->thrownException) = exception;
   }
   UNLOCKVAR(log);                                                                                                                                   
	MEMORY_TEST_END                                                                                                                                    
}                                                                                                                                                    
                                                                                                                                                     
//////////////////////////////////////////////////////////////////////////                                                                           
/**                                                                                                                                                  
 * Deletes all the log files with the default format found in the default device folder. If log is enabled, the current log file is not affected by 
 * this command.
 *                                                                                                                                                   
 * @param p->retI receives the number of files deleted.                                                                                              
 */                                                                                                                                                  
LB_API void lLC_privateDeleteLogFiles(NMParams p) // litebase/LitebaseConnection public static native int deleteLogFiles();                          
{                                                                                                                                                    
	TRACE("lLC_privateDeleteLogFiles")                                                                                                                 
   Context context = p->currentContext;                                                                                                                                                                                                                                 
   TCHARPs* list = null;                                                                                                                             
   TCHAR fullPath[MAX_PATHNAME],
         path[MAX_PATHNAME];
   char name[MAX_PATHNAME],
        value[DBNAME_SIZE];
   int32 count = 0,                                                                                                                                  
         i = 0,
         ret;
   Heap heap = heapCreate();                                                                                                                         
                                                                                                                                             	                                                                                                                                                   
   MEMORY_TEST_START                                                                                                                                 
   LOCKVAR(log);                                                                                                                                     
   IF_HEAP_ERROR(heap)                                                                                                                               
   {                                                                                                                                                                                                                                                                             
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);                                                                           
      goto finish;                                                                                                                                   
   }                                                                                                                                                 
   getCurrentPath(path);                                                                                                                                                                                                                                                                                                                                                                                                                      
	                                                                                                                                                   
   if ((ret = TC_listFiles(path, 1, &list, &count, heap, 0))) // Lists all the files of the folder.                                              
   {                                                                                                                                                 
      fileError(context, ret, "");                                                                                                                     
      goto finish;                                                                                                                                   
   }                                                                                                                                                 
	        
   name[0] = 0;
   if (count)
   {
      TCObject logger = litebaseConnectionClass->objStaticValues[1],
             nameObj;

      if (logger)
      {
		   nameObj = ((TCObject*)ARRAYOBJ_START(FIELD_OBJ(FIELD_OBJ(logger, loggerClass, 1), OBJ_CLASS(FIELD_OBJ(logger, loggerClass, 1)), 0)))[0];
		   nameObj = FIELD_OBJ(nameObj, OBJ_CLASS(nameObj), 0);
         TC_JCharP2CharPBuf(String_charsStart(nameObj), String_charsLen(nameObj), name);
      }
   }
                                                                                                                                           
   while (--count >= 0)                                                                                                                              
   {                                                                                                                                                                                                                                                                                                
      TC_TCHARP2CharPBuf(list->value, value);                                                                                                                                                                                                                                                   
      if (xstrstr(value, "LITEBASE") == value && xstrstr(value, ".LOGS") && !xstrstr(name, value)) // Deletes only the closed log files.                                      
      {  
         getFullFileName(value, path, fullPath);                                                                                                
         if ((ret = lbfileDelete(null, fullPath, false)))                                                                                                  
         {                                                                                                                                                 
            fileError(context, ret, "");                                                                                                                     
            goto finish;                                                                                                                                   
         }        
         i++;
      }                                                                                                                                              
                                                                                                                                                     
      list = list->next;                                                                                                                             
   }                                                                                                                                                 
	 
   p->retI = i; // The number of log files deleted.

finish: ;                                                                                                                                            
   heapDestroy(heap);                                                                                                                                
	UNLOCKVAR(log);                                                                                                                                    
	MEMORY_TEST_END                                                                                                                                    
}

//////////////////////////////////////////////////////////////////////////
// litebase/LitebaseConnection public static native litebase.LitebaseConnection processLogs(String []sql, String params, boolean isDebug) 
// throws DriverException, NullPointerException, OutOfMemoryError;
/**
 * This is a handy method that can be used to reproduce all commands of a log file. This is intended to be used by the development team only. 
 * Here's a sample on how to use it:
 * 
 * <pre>
 * String []sql =
 * {
 *    &quot;new LitebaseConnection(MBSL,null)&quot;,
 *    &quot;create table PRODUTO (IDPRODUTO int, IDPRODUTOERP char(10), IDGRUPOPRODUTO int, IDSUBGRUPOPRODUTO int, IDEMPRESA char(20), 
 *                                DESCRICAO char(100), UNDCAIXA char(10), PESO float, UNIDADEMEDIDA char(3),
 *                                EMBALAGEM char(10), PORCTROCA float, PERMITETROCA int)&quot;,
 *    &quot;create index IDX_PRODUTO_1 on PRODUTO(IDPRODUTO)&quot;,
 *    &quot;create index IDX_PRODUTO_2 on PRODUTO(IDGRUPOPRODUTO)&quot;,
 *    &quot;create index IDX_PRODUTO_3 on PRODUTO(IDEMPRESA)&quot;,
 *    &quot;create index IDX_PRODUTO_4 on PRODUTO(DESCRICAO)&quot;,
 *    &quot;closeAll&quot;,
 *    &quot;new LitebaseConnection(MBSL,null)&quot;,
 *    &quot;insert into PRODUTO values(1,'19132', 2, 1, '1', 2, '3', 'ABSORVENTE SILHO ABAS', '5', 13, 'PCT', '20X30', 10, 0)&quot;,
 *  };
 *  LitebaseConnection.processLogs(sql, true);
 * </pre>
 * 
 * @param p->obj[0] The string array of SQL commands to be executed.
 * @param p->obj[1] The parameters to open a connection.
 * @param p->i32[0] Indicates if debug information is to displayed on the debug console.
 * @param p->retO Receives the LitebaseConnection instance created, or <code>null</code> if <code>closeAll</code> was the last command executed (or 
 * no commands were executed at all).
 * @throws DriverException If an exception occurs.
 * @throws NullPointerException If <code>p->obj[0]</code> is null.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lLC_privateProcessLogs_Ssb(NMParams p) 
{
	TRACE("lLC_privateProcessLogs_Ssb")
	TCObject driver = null,
	       sql = p->obj[0],
	       params = p->obj[1],
          string,
          resultSetObj;
   TCObject* sqlArray = (TCObject*)ARRAYOBJ_START(sql);
   Context context = p->currentContext;
	bool isDebug = p->i32[0];
	int32 i,
         j = -1,
		   length = ARRAYOBJ_LEN(sql),
         sqlLen;
   JCharP sqlStr;
	ResultSet* resultSet;

	MEMORY_TEST_START
   i = j;

   if (sql)
	   while (++i < length)
	   {
         if (isDebug)
			   TC_debug("running command # %d", (i + 1));
         string = sqlArray[i];

         // Gets a new Litebase Connection.
         if (JCharPStartsWithCharP(sqlStr = String_charsStart(string), "new LitebaseConnection", sqlLen = String_charsLen(string), 22))
			   TC_setObjectLock(p->retO = driver = create(context, getAppCridInt(&sqlStr[23]), params), UNLOCKED);
		   
         // Create command.
         else if (JCharPStartsWithCharP(sqlStr, "create", sqlLen, 6))
			   litebaseExecute(context, driver, sqlStr, sqlLen);
		   
         // closeAll() command.
         else if (JCharPEqualsCharP(sqlStr, "closeAll", sqlLen, 8, true))
		   {
            freeLitebase(context, (size_t)driver);
			   p->retO = driver = null;
		   }

         // Select command.
		   else if (JCharPStartsWithCharP(sqlStr, "select", sqlLen, 6))
		   {
			   if ((resultSetObj = litebaseExecuteQuery(context, driver, sqlStr, sqlLen)))
            {
               resultSet = getResultSetBag(resultSetObj);
			      while (resultSetNext(context, resultSet));
			         freeResultSet(resultSet);
               TC_setObjectLock(resultSetObj, UNLOCKED);
            }
		   }

         // Commands that update the table.
		   else if (sqlLen > 0)
			   litebaseExecuteUpdate(context, driver, sqlStr, sqlLen);

		   if (context->thrownException)
		   {
			   TCObject exception = context->thrownException,
			          exceptionMsg = FIELD_OBJ(exception, OBJ_CLASS(exception), 0);
            char msgError[1024];
            
            if (exceptionMsg)
            {
               int32 length = String_charsLen(exceptionMsg);
               TC_JCharP2CharPBuf(String_charsStart(exceptionMsg), length < 1024? length : 1023, msgError);
			   }
			   else
			      xstrcpy(msgError, "null");
            
            if (isDebug)
            {
               char sqlErr[1024];
               TC_JCharP2CharPBuf(sqlStr, sqlLen < 1024? sqlLen : 1023, sqlErr);
               TC_debug("%s - %s", sqlErr, msgError);
            }

            context->thrownException = null;
            TC_throwExceptionNamed(context, "litebase.DriverException", msgError);

            if (strEq(OBJ_CLASS(context->thrownException)->name, "litebase.DriverException"))
				   OBJ_DriverExceptionCause(context->thrownException) = exception;
			   break;
		   }
         else
            j++;
	   }
   else
      TC_throwNullArgumentException(context, "sql");

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// juliana@220_5: added a method to recover possible corrupted tables, the ones that were not closed properly.
// litebase/LitebaseConnection public native boolean recoverTable(String tableName) throws DriverException, OutOfMemoryError;
// juliana@253_8: now Litebase supports weak cryptography.
/**
 * Tries to recover a table not closed properly by marking and erasing logically the records whose crc are not valid.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of the table to be converted.
 * @param p->retI Receives the number of purged records.
 * @throws DriverException If the table name or path is too big.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lLC_recoverTable_s(NMParams p) 
{
   TRACE("lLC_recoverTable_s")
   
   MEMORY_TEST_START

   if (checkParamAndDriver(p, "tableName")) // The driver can't be closed and the table name can't be null.
   { 
      TCObject tableName = p->obj[1];
      Context context = p->currentContext; 
      
      if (String_charsLen(tableName) > MAX_TABLE_NAME_LENGTH)
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_MAX_TABLE_NAME_LENGTH));
      else
      {
         TCObject driver = p->obj[0],
	             logger = litebaseConnectionClass->objStaticValues[1];
         char name[DBNAME_SIZE];
         TCHARP sourcePath = getLitebaseSourcePath(driver);
         TCHAR buffer[MAX_PATHNAME];
         Heap heap = null;
         Table* table = null;
	      PlainDB* plainDB;
         uint8* basbuf;
         
         // juliana@230_12: improved recover table to take .dbo data into consideration.
         uint8* columnNulls0;
         int32* columnSizes;
         Index** columnIndexes;
         NATIVE_FILE tableDb;
         SQLValue** record;
         int32 crid = OBJ_LitebaseAppCrid(driver),
               i,
               read,
               rows,
               dataLength,
			      columnCount,
               crcPos,
	            crc32Lido = 0,
               crc32Calc,
               deleted = 0, // Invalidates the number of deleted rows.
               type;
         bool useCrypto = OBJ_LitebaseUseCrypto(driver),
              useOldCrypto;
         uint32 j;
         int32 auxRowId = -1, // juliana@270_26: solved a possible duplicate rowid after issuing LitebaseConnection.recoverTable() on a table.
               currentRowId = -1; // juliana@270_26: solved a possible duplicate rowid after issuing LitebaseConnection.recoverTable() on a table.
         int8* types;       
         
         // juliana@230_12
#if defined(POSIX) || defined(ANDROID)
         Hashtable* htTables = (Hashtable*)getLitebaseHtTables(driver);
#endif

         if (logger) // juliana@230_30: reduced log files size.
	      {
		      TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
         
            LOCKVAR(log);

            // Builds the logger StringBuffer contents.
            StringBuffer_count(logSBuffer) = 0;
            if (TC_appendCharP(context, logSBuffer, "recover table ")
             && TC_appendJCharP(context, logSBuffer, String_charsStart(tableName), String_charsLen(tableName)))
               TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation.  
            
            UNLOCKVAR(log);
            if (context->thrownException)
               goto finish;
	      }

         if ((j = String_charsLen(tableName)) + tcslen(sourcePath) + 10 > MAX_PATHNAME)
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), sourcePath);
	         goto finish;
         }
         
         // Opens the table file.
	      TC_JCharP2CharPBuf(String_charsStart(tableName), j, &name[5]);
	      TC_CharPToLower(&name[5]); // juliana@227_19: corrected a bug in convert() and recoverTable() which could not find the table .db file. 
         TC_int2CRID(crid, name);
         
// juliana@230_12      
#if defined(POSIX) || defined(ANDROID)
         if (TC_htGetPtr(htTables, TC_hashCode(&name[5])))
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_OPENED), &name[5]);
            goto finish;
         }
#endif
         
         name[4] = '-';
         xstrcat(name, ".db");
         getFullFileName(name, sourcePath, buffer);
         
         if ((j = lbfileCreate(&tableDb, buffer, READ_WRITE))) // Opens the .db table file.
	      {
		      fileError(context, j, name);
		      goto finish;
	      }

         // juliana@222_2: the table must be not closed properly in order to recover it.
	      if ((j = lbfileSetPos(tableDb, 6)) || (j = lbfileReadBytes(tableDb, (CharP)&crc32Lido, 0, 1, &read)))
         {
		      fileError(context, j, name);
            lbfileClose(&tableDb);
		      goto finish;
	      }
         if (read != 1) // juliana@226_8: a table without metadata (with an empty .db, for instance) can't be recovered: it is corrupted.
         {
            lbfileClose(&tableDb);
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_CORRUPTED), name);
            goto finish;
         }
         
         if (useCrypto)
            crc32Lido ^= 0xAA;
         
	      if ((crc32Lido & IS_SAVED_CORRECTLY) == IS_SAVED_CORRECTLY) 
	      {
		      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_CLOSED), name);
            lbfileClose(&tableDb);
		      goto finish;
         }
	      lbfileClose(&tableDb);

         heap = heapCreate();
	      IF_HEAP_ERROR(heap)
	      {
		      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            heapDestroy(heap);
            goto finish;
	      }

         name[xstrlen(name) - 3] = 0;

          // juliana@253_6: the maximum number of keys of a index was duplicated.
	      // Opens the table even if it was not cloded properly.
	      if (!(table = tableCreate(context, name, sourcePath, false, (bool)OBJ_LitebaseIsAscii(driver), useCrypto, getLitebaseNodes(driver), 
	                                                                                                                      false, heap)))
            goto finish;

	      i = rows = (plainDB = &table->db)->rowCount;
	      basbuf = plainDB->basbuf;
         columnIndexes = table->columnIndexes;
         
         // juliana@230_12: improved recover table to take .dbo data into consideration.
         record = newSQLValues(columnCount = table->columnCount, heap);
         crcPos = (int32)table->columnOffsets[columnCount] + NUMBEROFBYTES(columnCount);
         types = table->columnTypes;
         columnNulls0 = table->columnNulls;
         columnSizes = table->columnSizes;
         useOldCrypto = plainDB->useOldCrypto;
         
         j = columnCount;
         while (--j)
            if (((type = types[j]) == CHARS_TYPE || type == CHARS_NOCASE_TYPE))
               record[j]->asChars = TC_heapAlloc(heap, columnSizes[j] << 1);
            else if (type == BLOB_TYPE)
               record[j]->asBlob = TC_heapAlloc(heap, columnSizes[j]);

	      while (--i >= 0) // Checks all table records.
	      {
		      if (!plainRead(context, plainDB, i))
			      goto finish;

            if (isZero(basbuf, crcPos + 4)) // juliana@268_3: Now does not do anything if there are only zeros in a row and removes them.
            {
               rows--;
               continue;
            }

		      xmove4(&read, basbuf);
		      if ((read & ROW_ATTR_MASK) == ROW_ATTR_DELETED) // Counts the number of deleted records.
               deleted++;
		      else 
		      {
			      xmove4(&crc32Lido, &basbuf[crcPos]);
			      basbuf[3] = 0; // Erases rowid information.
   			   
			      // juliana@230_12: improved recover table to take .dbo data into consideration.
               crc32Calc = updateCRC32(basbuf, crcPos, 0);

               if (table->version == VERSION_TABLE)
               {  
                  if (readRecord(context, table, record, i, columnNulls0, null, 0, false, heap, null))
                  {                                   
                     j = columnCount;
                     while (--j)
                        if (((type = types[j]) == CHARS_TYPE || type == CHARS_NOCASE_TYPE) && isBitUnSet(columnNulls0, j))
                           crc32Calc = updateCRC32((uint8*)record[j]->asChars, record[j]->length << 1, crc32Calc);
                        else if (type == BLOB_TYPE && isBitUnSet(columnNulls0, j))
                        {
                           dataLength = record[j]->length;
                           crc32Calc = updateCRC32((uint8*)&dataLength, 4, crc32Calc);
                        }
                  }
                  else
                  {
                     context->thrownException = null;
                     crc32Calc = crc32Lido + 1;
                  }
               }
               
               if (useOldCrypto)
               {
                  xmove4(&basbuf[crcPos], &crc32Calc);
                  table->auxRowId = (read & ROW_ID_MASK) + 1;
                  if (!plainRewrite(context, plainDB, i))
					      goto finish;
               }
               else if (crc32Calc != crc32Lido) // Deletes and invalidates corrupted records.
			      {
                  j = ROW_ATTR_DELETED;
                  xmove4(basbuf, &j);
				      if (!plainRewrite(context, plainDB, i))
					      goto finish;
				      deleted++;

                  // juliana@270_26: solved a possible duplicate rowid after issuing LitebaseConnection.recoverTable() on a table.
                  if (currentRowId < 0) 
                     currentRowId = (read & ROW_ID_MASK) + 1;
			      }
               else // juliana@224_3: corrected a bug that would make Litebase not use the correct rowid after a recoverTable().
               {
                  // juliana@270_26: solved a possible duplicate rowid after issuing LitebaseConnection.recoverTable() on a table.
                  read = (read & ROW_ID_MASK) + 1;
                  if (currentRowId < 0)
                     currentRowId = read;
                  if (auxRowId < 0) 
                     auxRowId = read;
               }
		      }
	      }

         table->deletedRowsCount = p->retI = deleted;
         plainDB->rowCount = rows;

         // juliana@270_26: solved a possible duplicate rowid after issuing LitebaseConnection.recoverTable() on a table.
         table->currentRowId = currentRowId;
         table->auxRowId = auxRowId;

         // Recreates the indices.
         // Simple indices.
         while (--columnCount >= 0)
			   if (columnIndexes[columnCount] && !tableReIndex(context, table, columnCount, false, null))
               goto finish;

         // Recreates the composed indexes.
         if ((i = table->numberComposedIndexes))
	      {
            ComposedIndex** compIndexes = table->composedIndexes;
            while (--i >= 0)
               if (!tableReIndex(context, table, -1, false, compIndexes[i]))
                  goto finish;
	      }

         plainDB->wasNotSavedCorrectly = false;

         // juliana@224_3: corrected a bug that would make Litebase not use the correct rowid after a recoverTable().	   

         plainDB->useOldCrypto = false;
         
         // juliana@270_26: solved a possible duplicate rowid after issuing LitebaseConnection.recoverTable() on a table.
         tableSaveMetaData(context, table, TSMD_EVERYTHING); // Saves information concerning deleted rows.
      
finish: 
	      if (table)
            freeTable(context, table, false, true); // Closes the table.
        
      }
   }

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// litebase/LitebaseConnection public native void convert(String tableName) throws DriverException, OutOfMemoryError;
// juliana@253_8: now Litebase supports weak cryptography.
/**
 * Converts a table from the previous Litebase table version to the current one. If the table format is older than the previous table version, this 
 * method can't be used. It is possible to know if the table version is not compativel with the current version used in Litebase because an exception
 * will be thrown if one tries to open a table with the old format. The table will be closed after using this method. Notice that the table .db file 
 * will be overwritten. 
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of the table to be converted.
 * @throws DriverException If the table version is not the previous one (too old or the actual used by Litebase) or the table name or path is too big.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lLC_convert_s(NMParams p) 
{
	TRACE("lLC_convert_s")

	MEMORY_TEST_START
	
   if (checkParamAndDriver(p, "tableName")) // The driver can't be closed and the table name can't be null.
   {
      TCObject tableName = p->obj[1];
      Context context = p->currentContext;
   
      if (String_charsLen(tableName) > MAX_TABLE_NAME_LENGTH)
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_MAX_TABLE_NAME_LENGTH));
      else
      {
         TCObject driver = p->obj[0],
                logger = litebaseConnectionClass->objStaticValues[1];
         Heap heap;
         char name[DBNAME_SIZE];
         TCHARP sourcePath = getLitebaseSourcePath(driver);
         TCHAR buffer[MAX_PATHNAME];
         Table* table = null;
	      PlainDB* plainDB;
         uint8* basbuf;
         
         // juliana@230_12: improved recover table to take .dbo data into consideration.
         uint8* columnNulls0;
         XFile dbFile;
         NATIVE_FILE tableDb;
         SQLValue** record;
	      int32 crid = OBJ_LitebaseAppCrid(driver),
               i,
               rowid,
               crc32,
               length,
			      rows,
               dataLength,
               rowSize,
			      headerSize,
               columnCount,
               read,
               type;
         uint32 j = 0;
         bool useCrypto = OBJ_LitebaseUseCrypto(driver);
         int8* types;
         int32* sizes;         
            
// juliana@230_12
#if defined(POSIX) || defined(ANDROID)
         Hashtable* htTables = (Hashtable*)getLitebaseHtTables(driver);
#endif

         if (logger) // juliana@230_30: reduced log files size.
	      {  
            TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
         
            LOCKVAR(log);

            // Builds the logger StringBuffer contents.
            StringBuffer_count(logSBuffer) = 0;
            if (TC_appendCharP(context, logSBuffer, "convert ")
             && TC_appendJCharP(context, logSBuffer, String_charsStart(tableName), String_charsLen(tableName)))
               TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation.  
            
            UNLOCKVAR(log);
            if (context->thrownException)
               goto finish;
	      }
	      
         if ((i = String_charsLen(tableName)) + tcslen(sourcePath) + 10 > MAX_PATHNAME)
         {
            char buffer[1024];
            TC_TCHARP2CharPBuf(sourcePath, buffer);
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), buffer);
	         goto finish;
         }
    
         // Opens the .db table file.
	      TC_JCharP2CharPBuf(String_charsStart(tableName), i, &name[5]);
	      TC_CharPToLower(&name[5]); // juliana@227_19: corrected a bug in convert() and recoverTable() which could not find the table .db file. 
         TC_int2CRID(crid, name);
      
// juliana@230_12      
#if defined(POSIX) || defined(ANDROID)
         if (TC_htGetPtr(htTables, TC_hashCode(&name[5])))
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_OPENED), &name[5]);
            goto finish;
         }
#endif

         name[4] = '-';
         xstrcat(name, ".db");

         getFullFileName(name, sourcePath, buffer);
	      if ((i = lbfileCreate(&tableDb, buffer, READ_WRITE))) // Opens the .db table file.
	      {
		      fileError(context, i, name);
            goto finish;
	      }

	      // The version must be the previous of the current one.
	      if ((i = lbfileSetPos(tableDb, 7)) || (i = lbfileReadBytes(tableDb, (CharP)&j, 0, 1, &read))) 
         {
		      fileError(context, i, name);
            lbfileClose(&tableDb);
            goto finish;
	      }
	      
	      if (useCrypto)
	         j ^= 0xAA;
	      
	      if (j != VERSION_TABLE - 1) 
	      {
		      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_WRONG_PREV_VERSION), name);
            lbfileClose(&tableDb);
		      goto finish;
         }

         // Changes the version to be current one and closes it.
	      j = useCrypto? VERSION_TABLE ^ 0xAA : VERSION_TABLE;
         if ((i = lbfileSetPos(tableDb, 7)) || (i = lbfileWriteBytes(tableDb, (CharP)&j, 0, 1, &read)))
         {
		      fileError(context, i, name);
            lbfileClose(&tableDb);
            goto finish;
	      }
	      lbfileClose(&tableDb);

	      name[xstrlen(name) - 3] = 0;

         // juliana@225_11: corrected possible memory leaks and crashes when LitebaseConnection.convert() fails.
         heap = heapCreate();
	      IF_HEAP_ERROR(heap)
	      {
		      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            heapDestroy(heap);
            goto finish;
	      }

          // juliana@253_6: the maximum number of keys of a index was duplicated.
	      // Opens the table even if it was not cloded properly.
	      if (!(table = tableCreate(context, name, sourcePath, false, (bool)OBJ_LitebaseIsAscii(driver), useCrypto, getLitebaseNodes(driver), 
	                                                                                                                      false, heap)))
            goto finish;

	      dbFile = (plainDB = &table->db)->db;
	      headerSize = plainDB->headerSize;
	      basbuf = plainDB->basbuf;
	      rows = (dbFile.size - headerSize) / (length = (rowSize = plainDB->rowSize) - 4);
	      plainDB->rowCount = rows;
         
         // juliana@230_12: improved recover table to take .dbo data into consideration.
         record = newSQLValues(columnCount = table->columnCount, heap);
         types = table->columnTypes;
         sizes = table->columnSizes;
         columnNulls0 = table->columnNulls;

         j = columnCount;
         while (--j)
            if (((type = types[j]) == CHARS_TYPE || type == CHARS_NOCASE_TYPE))
               record[j]->asChars = TC_heapAlloc(heap, sizes[j] << 1);
            else if (type == BLOB_TYPE)
               record[j]->asBlob = TC_heapAlloc(heap, sizes[j]); 
         
	      while (--rows >= 0) // Converts all the records adding a crc code to them.
	      {
		      nfSetPos(&dbFile, rows * length + headerSize);
		      if (!nfReadBytes(context, &dbFile, basbuf, length))
               goto finish;
		      rowid = basbuf[3];
		      basbuf[3] = 0;
            
            // juliana@230_12: improved recover table to take .dbo data into consideration.
            crc32 = updateCRC32(basbuf, length, 0);

            if (table->version == VERSION_TABLE)
            {
               if (!readRecord(context, table, record, i, columnNulls0, null, 0, false, heap, null))
                  goto finish;
               j = columnCount;
               while (--j)
                  if (((type = types[j]) == CHARS_TYPE || type == CHARS_NOCASE_TYPE) && isBitUnSet(columnNulls0, j))
                     crc32 = updateCRC32((uint8*)record[j]->asChars, record[j]->length << 1, crc32);
                  else if (type == BLOB_TYPE && isBitUnSet(columnNulls0, j))
                  {
                     dataLength = record[j]->length;
                     crc32 = updateCRC32((uint8*)&dataLength, 4, crc32);
                  }
            }

            xmove4(&basbuf[length], &crc32);
		      basbuf[3] = rowid;
		      nfSetPos(&dbFile, rows * rowSize + headerSize);
		      nfWriteBytes(context, &dbFile, basbuf, rowSize);      
	      }
	      
finish:
	      if (table)
	         freeTable(context, table, false, false); // Closes the table.
      
      }
   }
      
	MEMORY_TEST_END
}

// juliana@223_1: added a method to get the current slot being used.
//////////////////////////////////////////////////////////////////////////
/**
 * Used to returned the slot where the tables were stored on Palm OS. Not used anymore.
 * 
 * @param p->retI receives -1.
 */
LB_API void lLC_getSlot(NMParams p) // litebase/LitebaseConnection public native int getSlot(); 
{
   TRACE("lLC_getSlot")
   p->retI = -1;
}

// juliana@226_6: added LitebaseConnection.isOpen(), which indicates if a table is open in the current connection.
//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Indicates if a table is open or not.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The table name to be checked.
 * @param p->retI receives <code>true</code> if the table is open in the current connection; <code>false</code>, otherwise.
 * @throws DriverException If the table name is too big.
 */
LB_API void lLC_isOpen_s(NMParams p) // litebase/LitebaseConnection public native boolean isOpen(String tableName) throws DriverException; 
{
   TRACE("lLC_isOpen_s")

   MEMORY_TEST_START

   if (checkParamAndDriver(p, "tableName")) // The driver can't be closed and the table name can't be null.
   {
      TCObject tableName = p->obj[1];
   
      if (String_charsLen(tableName) > MAX_TABLE_NAME_LENGTH)
         TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_MAX_TABLE_NAME_LENGTH));
      else
      {
         TCObject driver = p->obj[0];
         Hashtable* htTables = getLitebaseHtTables(driver);
         int32 length = String_charsLen(tableName);
         char nameCharP[DBNAME_SIZE];

         // Checks if the table name hash code is in the driver hash table.
         TC_JCharP2CharPBuf(String_charsStart(tableName), length, nameCharP);
         TC_CharPToLower(nameCharP);
         p->retI = TC_htGetPtr(htTables, TC_hashCode(nameCharP)) != null;
      } 
   }

   MEMORY_TEST_END
}

// juliana@226_10: added LitebaseConnection.dropDatabase().
//////////////////////////////////////////////////////////////////////////
// litebase/LitebaseConnection public native static void dropDatabase(String crid, String sourcePath, int slot) throws DriverException, 
// NullPointerException;
/**
 * Drops all the tables from a database represented by its application id and path.
 * 
 * @param p->obj[0] The application id of the database.
 * @param p->obj[1] The path where the files are stored.
 * @throws DriverException If the database is not found or a file error occurs.
 * @throws NullPointerException If one of the string parameters is null.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lLC_dropDatabase_ssi(NMParams p)
{
   TRACE("lLC_dropDatabase_ssi")
   TCObject cridObj = p->obj[0],
          pathObj = p->obj[1];
   
   MEMORY_TEST_START
   if (cridObj) 
      if (pathObj)
         if (String_charsLen(pathObj) >= MAX_PATHNAME - 4 - DBNAME_SIZE) // The path length can't be greater than the buffer size.
            TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_INVALID_PATH));
         else
         {
            TCHARPs* list = null;
            char cridStr[5];
            TCHAR fullPath[MAX_PATHNAME], // juliana@230_6
                  buffer[MAX_PATHNAME];
            char value[DBNAME_SIZE];
            int32 i,
                  count = 0;
            bool deleted = false;
            Heap heap = heapCreate();

            IF_HEAP_ERROR(heap)
            {
               TC_throwExceptionNamed(p->currentContext, "java.lang.OutOfMemoryError", null);
               
error:               
               heapDestroy(heap);
               goto finish;
            }

            TC_JCharP2CharPBuf(String_charsStart(cridObj), 4, cridStr);
            TC_JCharP2TCHARPBuf(String_charsStart(pathObj), String_charsLen(pathObj), fullPath);

            if ((i = TC_listFiles(fullPath, -1, &list, &count, heap, 0))) // Lists all the files of the folder. 
            {
               fileError(p->currentContext, i, "");
               goto error;
            }

            while (--count >= 0) // Deletes only the files of the chosen database.
            {
               TC_TCHARP2CharPBuf(list->value, value);
               if (xstrstr(value, cridStr) == value)
               {
                  getFullFileName(value, fullPath, buffer);
                  if ((i = lbfileDelete(null, buffer, false)))
                  {
                     fileError(p->currentContext, i, value);
                     goto error;
                  }
                  deleted = true;
               }

               list = list->next;
            }
            
            heapDestroy(heap);
            if (!deleted)
               TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_DB_NOT_FOUND));
         }  
      else // The string argument can't be null.
         TC_throwNullArgumentException(p->currentContext, "sourcePath");
   else // The string argument can't be null.
      TC_throwNullArgumentException(p->currentContext, "crid");

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/LitebaseConnection public native boolean isTableProperlyClosed(String tableName) throws DriverException, NullPointerException;

/**
 * Indicates if a table is closed properly or not.
 * 
 * @param p->obj[1] The table to be verified.
 * @param p->retI receives <code>true</code> if the table is closed properly or is open (a not properly closed table can't be opened); 
 * <code>false</code>, otherwise.
 * @throws DriverException If the table is corrupted.
 * @throws NullPointerException If tableName is null.
 */
LB_API void lLC_isTableProperlyClosed_s(NMParams p)
{
   TRACE("lLC_isTableProperlyClosed_s")
   
   MEMORY_TEST_START

   lLC_isOpen_s(p);
   
   if (!p->currentContext->thrownException && !p->retI) // If the table is open, then it was closed properly.
   {
      TCObject tableName = p->obj[1];
      Context context = p->currentContext; 
      
      if (String_charsLen(tableName) > MAX_TABLE_NAME_LENGTH)
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_MAX_TABLE_NAME_LENGTH));
      else
      {
         TCObject driver = p->obj[0];

#if defined(POSIX) || defined(ANDROID)
         Hashtable* htTables = (Hashtable*)getLitebaseHtTables(driver);
#endif

         char name[DBNAME_SIZE];
         TCHARP sourcePath = getLitebaseSourcePath(driver);
         TCHAR buffer[MAX_PATHNAME];         
         NATIVE_FILE tableDb;
	      int32 crid = OBJ_LitebaseAppCrid(driver),
               i = 0,
               j = 0,
               read;    
         
         // Opens the table file.
	      TC_JCharP2CharPBuf(String_charsStart(tableName), String_charsLen(tableName), &name[5]);
	      TC_CharPToLower(&name[5]); 
         TC_int2CRID(crid, name);
         
// juliana@230_12      
#if defined(POSIX) || defined(ANDROID)
         if (TC_htGetPtr(htTables, TC_hashCode(&name[5])))
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_OPENED), &name[5]);
            goto finish;
         }
#endif
         
         name[4] = '-';
         xstrcat(name, ".db");
         getFullFileName(name, sourcePath, buffer);
         
         if ((j = lbfileCreate(&tableDb, buffer, READ_WRITE))) // Opens the .db table file.
	      {
		      fileError(context, j, name);
		      goto finish;
	      }

         // Reads the flag.
	      if ((j = lbfileSetPos(tableDb, 6)) || (j = lbfileReadBytes(tableDb, (CharP)&i, 0, 1, &read)))
         {
		      fileError(context, j, name);
            lbfileClose(&tableDb);
		      goto finish;
	      }
         if (read != 1) // juliana@226_8: a table without metadata (with an empty .db, for instance) is corrupted.
         {
            fileError(context, j, name);
            lbfileClose(&tableDb);
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_CORRUPTED), name);
            goto finish;
         }
	      if ((i & IS_SAVED_CORRECTLY) == IS_SAVED_CORRECTLY) 
	         p->retI = true; // The table was closed properly.
	      else
	         p->retI = false; // The table was not closed properly.
	      lbfileClose(&tableDb); 
      }   
   } 

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/LitebaseConnection public native String[] listAllTables() throws DriverException, IllegalStateException, OutOfMemoryError;
/**
 * Lists all table names of the current connection.
 * 
 * @param p->retO receives an array of all the table names of the current connection. If the current connection has no tables, an empty list is 
 * returned.
 * @throws DriverException If a file error occurs. 
 * @throws IllegalStateException If the driver is closed.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lLC_listAllTables(NMParams p) 
{
   TRACE("lLC_listAllTables")
   TCObject driver = p->obj[0];
   
   MEMORY_TEST_START

   if (OBJ_LitebaseDontFinalize(driver)) // The driver can't be closed.
      TC_throwExceptionNamed(p->currentContext, "java.lang.IllegalStateException", getMessage(ERR_DRIVER_CLOSED)); 
   else   
   {      
      TCHARPs* list = null; 
      TCObject* array;
      Context context = p->currentContext;
      char crid[5],      
           value[DBNAME_SIZE];
      TCHARP path = getLitebaseSourcePath(driver);
      int32 i,
            j,
            count = 0;
      Heap heap = heapCreate();

      IF_HEAP_ERROR(heap)
      {
         TC_throwExceptionNamed(p->currentContext, "java.lang.OutOfMemoryError", null);
         
error:               
         heapDestroy(heap);
         goto finish;
      }
     
      TC_int2CRID(OBJ_LitebaseAppCrid(driver), crid);
      
      if ((i = TC_listFiles(path, -1, &list, &count, heap, 0))) // Lists all the files of the folder. 
      {
         fileError(context, i, "");
         goto error;
      }

      i = 0;
      j = count;
      while (--j >= 0) // Deletes only the files of the chosen database.
      {
         TC_TCHARP2CharPBuf(list->value, value);        
         
         // Selects the .db files that are from the tables of the current connection. 
         if (xstrstr(value, crid) == value && xstrstr(value, ".db") && !xstrstr(value, ".dbo"))
         { 
            list->value[xstrlen(value) - 3] = 0;
            list->value = &list->value[5];
            i++;
         }
         else
            list->value = null;
         list = list->next;
      }
      
      if (!(p->retO = TC_createArrayObject(context, "[java.lang.String", i)))
         goto finish;
      array = (TCObject*)ARRAYOBJ_START(p->retO);
      
      while (--count >= 0) // Gets only the table names that are from this connection.
      {
         if (list->value)
         {
            if (!(*array = TC_createStringObjectFromTCHARP(context, list->value, -1)))
               goto error;
            TC_setObjectLock(*array++, UNLOCKED);
         }
         list = list->next;   
      }
      
      heapDestroy(heap);
   }  
      

finish: 
   TC_setObjectLock(p->retO, UNLOCKED); 
   
   MEMORY_TEST_END
}

// juliana@253_16: created static methods LitebaseConnection.encryptTables() and decryptTables().
//////////////////////////////////////////////////////////////////////////
// litebase/LitebaseConnection public native void encryptTables(String crid, String sourcePath, int slot);
/**
 * Encrypts all the tables of a connection given from the application id. All the files of the tables must be closed!
 * 
 * @param p->obj[0] The application id of the database.
 * @param p->obj[1] The path where the files are stored.
 */
LB_API void lLC_encryptTables_ssi(NMParams p) 
{
   TRACE("lLC_encryptTables_ssi")
   MEMORY_TEST_START
   encDecTables(p, true);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/LitebaseConnection public native void decryptTables(String crid, String sourcePath, int slot);
/**
 * Decrypts all the tables of a connection given from the application id. All the files of the tables must be closed!
 * 
 * @param p->obj[0] The application id of the database.
 * @param p->obj[1] The path where the files are stored.
 */
LB_API void lLC_decryptTables_ssi(NMParams p) 
{
   TRACE("lLC_decryptTables_ssi")
   MEMORY_TEST_START
   encDecTables(p, false);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Returns the metadata for this result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retO receives the metadata for this result set.
 */
LB_API void lRS_getResultSetMetaData(NMParams p) // litebase/ResultSet public native litebase.ResultSetMetaData getResultSetMetaData(); 
{
	TRACE("lRS_getResultSetMetaData")
   TCObject resultSet = p->obj[0],
          rsMetaData;
	
   MEMORY_TEST_START
   
   // The driver and the result set can't be closed.
   if (testRSClosed(p->currentContext, resultSet) && (p->retO = rsMetaData = TC_createObject(p->currentContext, "litebase.ResultSetMetaData")))
   {
      TC_setObjectLock(rsMetaData, UNLOCKED);
      OBJ_ResultSetMetaData_ResultSet(rsMetaData) = resultSet;	   
   }
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Releases all memory allocated for this object. Its a good idea to call this when you no longer needs it, but it is also called by the GC when the 
 * object is no longer in use.
 *
 * @param p->obj[0] The result set.
 * @throws IllegalStateException If the result set is closed.
 */
LB_API void lRS_close(NMParams p) // litebase/ResultSet private native void rsClose() throws IllegalStateException;
{
	TRACE("lRS_close")
   TCObject resultSet = p->obj[0];
	
   MEMORY_TEST_START
   
	if (OBJ_ResultSetDontFinalize(resultSet)) // The result set can't be closed.
	   TC_throwExceptionNamed(p->currentContext, "java.lang.IllegalStateException", getMessage(ERR_RESULTSET_CLOSED));
	else  // juliana@211_4: solved bugs with result set dealing.
   {
      freeResultSet(getResultSetBag(resultSet));
      OBJ_ResultSetDontFinalize(resultSet) = true;
   }
      
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Places the cursor before the first record.
 *
 * @param p->obj[0] The result set.
 */
LB_API void lRS_beforeFirst(NMParams p) // litebase/ResultSet public native void beforeFirst();
{
	TRACE("lRS_beforeFirst")
   TCObject resultSet = p->obj[0];
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
      getResultSetBag(resultSet)->pos = -1;
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Places the cursor after the last record.
 *
 * @param p->obj[0] The result set.
 */
LB_API void lRS_afterLast(NMParams p) // litebase/ResultSet public native void afterLast();
{
	TRACE("lRS_afterLast")
   TCObject resultSet = p->obj[0];
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      rsBag->pos = rsBag->table->db.rowCount;
   }
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Places the cursor in the first record of the result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retI Receives <code>true</code> if it was possible to place the cursor in the first record; <code>false</code>, otherwise.
 */
LB_API void lRS_first(NMParams p) // litebase/ResultSet public native bool first();
{
	TRACE("lRS_first")
   TCObject resultSet = p->obj[0];
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);

      rsBag->pos = -1; // Sets the position before the first record.
      if (resultSetNext(p->currentContext, rsBag)) // Reads the first record. 
         p->retI = true;
      else
      {
         rsBag->pos = -1; // guich@_105: sets the record to -1 if it can't read the first position.
         p->retI = false;
      }
   }   
  
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Places the cursor in the last record of the result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retI Receives <code>true</code> if it was possible to place the cursor in the last record; <code>false</code>, otherwise.
 */
LB_API void lRS_last(NMParams p) // litebase/ResultSet public native bool last();
{
	TRACE("lRS_last")
   TCObject resultSet = p->obj[0];
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
         
      rsBag->pos = rsBag->table->db.rowCount; // Sets the position after the last record.
      if (resultSetPrev(p->currentContext, rsBag)) // Reads the last record. 
         p->retI = true;
      else
      {
         rsBag->pos = -1; // guich@_105: sets the record to -1 if it can't read the last position.
         p->retI = false;
      }
   }
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Gets the next record of the result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retI Receives <code>true</code> if there is a next record to go to in the result set; <code>false</code>, otherwise.
 */
LB_API void lRS_next(NMParams p) // litebase/ResultSet public native bool next();
{
	TRACE("lRS_next")
   TCObject resultSet = p->obj[0];
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
      p->retI = resultSetNext(p->currentContext, getResultSetBag(resultSet));
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Returns the previous record of the result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retI Receives <code>true</code> if there is a previous record to go to in the result set; <code>false</code>, otherwise.
 */
LB_API void lRS_prev(NMParams p) // litebase/ResultSet public native bool prev();
{
	TRACE("lRS_prev")
   TCObject resultSet = p->obj[0];
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
      p->retI = resultSetPrev(p->currentContext, getResultSetBag(resultSet));
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column index (starting from 1), returns a short value that is represented by this column. Note that it is only possible to request this 
 * column as short if it was created with this precision or if the data being fetched is the result of a DATE or DATETIME SQL function.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retI receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getShort_i(NMParams p) // litebase/ResultSet public native short getShort(int col) throws DriverException;
{
   TRACE("lRS_getShort_i");
   MEMORY_TEST_START
   rsGetByIndex(p, SHORT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column name (case insensitive), returns a short value that is represented by this column. Note that it is only possible to request this 
 * column as short if it was created with this precision or if the data being fetched is the result of a DATE or DATETIME SQL function. This method 
 * is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retI receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getShort_s(NMParams p) // litebase/ResultSet public native short getShort(String colName) throws DriverException;
{
   TRACE("lRS_getShort_s");
   MEMORY_TEST_START
   rsGetByName(p, SHORT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column index (starting from 1), returns an integer value that is represented by this column. Note that it is only possible to request this 
 * column as integer if it was created with this precision.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retI receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getInt_i(NMParams p) // litebase/ResultSet public native int getInt(int col) throws DriverException;
{
   TRACE("lRS_getInt_i");
   MEMORY_TEST_START
   rsGetByIndex(p, INT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column name (case insensitive), returns an integer value that is represented by this column. Note that it is only possible to request this 
 * column as integer if it was created with this precision. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retI receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getInt_s(NMParams p) // litebase/ResultSet public native int getInt(String colName) throws DriverException;
{
   TRACE("lRS_getInt_s");
   MEMORY_TEST_START
   rsGetByName(p, INT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column index (starting from 1), returns a long value that is represented by this column. Note that it is only possible to request this 
 * column as long if it was created with this precision.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retL receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getLong_i(NMParams p) // litebase/ResultSet public native long getLong(int col) throws DriverException;
{
   TRACE("lRS_getLong_i");
   MEMORY_TEST_START
   rsGetByIndex(p, LONG_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column name (case insensitive), returns a long value that is represented by this column. Note that it is only possible to request this 
 * column as long if it was created with this precision. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retL receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getLong_s(NMParams p) // litebase/ResultSet public native long getLong(String colName) throws DriverException;
{
   TRACE("lRS_getLong_s");
   MEMORY_TEST_START
   rsGetByName(p, LONG_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column index (starting from 1), returns a float value that is represented by this column. Note that it is only possible to request this 
 * column as float if it was created with this precision.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retD receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
LB_API void lRS_getFloat_i(NMParams p) // litebase/ResultSet public native double getFloat(int col) throws DriverException;
{
   TRACE("lRS_getFloat_i");
   MEMORY_TEST_START
   rsGetByIndex(p, FLOAT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column name (case insensitive), returns a float value that is represented by this column. Note that it is only possible to request this 
 * column as float if it was created with this precision. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retD receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
LB_API void lRS_getFloat_s(NMParams p) // litebase/ResultSet public native double getFloat(String colName) throws DriverException;
{
   TRACE("lRS_getFloat_s");
   MEMORY_TEST_START
   rsGetByName(p, FLOAT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column index (starting from 1), returns a double value that is represented by this column. Note that it is only possible to request this 
 * column as double if it was created with this precision.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retD receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
LB_API void lRS_getDouble_i(NMParams p) // litebase/ResultSet public native double getDouble(int col) throws DriverException;
{
   TRACE("lRS_getDouble_i");
   MEMORY_TEST_START
   rsGetByIndex(p, DOUBLE_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column name (case insensitive), returns a double value that is represented by this column. Note that it is only possible to request this 
 * column as double if it was created with this precision. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retD receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
LB_API void lRS_getDouble_s(NMParams p) // litebase/ResultSet public native double getDouble(String colName) throws DriverException;
{
   TRACE("lRS_getDouble_s");
   MEMORY_TEST_START
   rsGetByName(p, DOUBLE_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column index (starting from 1), returns a char array that is represented by this column. Note that it is only possible to request this 
 * column as a char array if it was created as a string.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getChars_i(NMParams p) // litebase/ResultSet public native char[] getChars(int col) throws DriverException;
{
   TRACE("lRS_getChars_i");
   MEMORY_TEST_START
   rsGetByIndex(p, CHARS_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column name (case insensitive), returns a char array that is represented by this column. Note that it is only possible to request this 
 * column as a char array if it was created as a string. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getChars_s(NMParams p) // litebase/ResultSet public native char[] getChars(String colName) throws DriverException;
{
   TRACE("lRS_getChars_s");
   MEMORY_TEST_START
   rsGetByName(p, CHARS_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column index (starting from 1), returns a string that is represented by this column. Any column type can be returned as a string. 
 * <code>Double</code>/<code>float</code> values formatting will use the precision set with the <code>setDecimalPlaces()</code> method.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>
 */
LB_API void lRS_getString_i(NMParams p) // litebase/ResultSet public native String getString(int col) throws DriverException;
{
   TRACE("lRS_getString_i");
   MEMORY_TEST_START
   rsGetByIndex(p, UNDEFINED_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column name (case insensitive), returns a string that is represented by this column. Any column type can be returned as a string. 
 * <code>Double</code>/<code>float</code> values formatting will use the precision set with the <code>setDecimalPlaces()</code> method. This 
 * method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column index.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>
 */
LB_API void lRS_getString_s(NMParams p) // litebase/ResultSet public native String getString(String colName) throws DriverException;
{
   TRACE("lRS_getString_s");
   MEMORY_TEST_START
   rsGetByName(p, -1);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column index (starting from 1), returns a byte (blob) array that is represented by this column. Note that it is only possible to request 
 * this column as a blob if it was created this way.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getBlob_i(NMParams p) // litebase/ResultSet public native uint8[] getBlob(int col) throws DriverException;
{
   TRACE("lRS_getBlob_i");
   MEMORY_TEST_START
   rsGetByIndex(p, BLOB_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column name (case insensitive), returns a byte array (blob) that is represented by this column. Note that it is only possible to request 
 * this column as a blob if it was created this way. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getBlob_s(NMParams p) // litebase/ResultSet public native uint8[] getBlob(String colName) throws DriverException;
{
   TRACE("lRS_getBlob_s");
   MEMORY_TEST_START
   rsGetByName(p, BLOB_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Starting from the current cursor position, it reads all result set rows that are being requested. <code>first()</code>,  <code>last()</code>, 
 * <code>prev()</code>, or <code>next()</code> must be used to set the current position, but not  <code>beforeFirst()</code> or 
 * <code>afterLast()</code>. It doesn't return BLOB values. <code>null</code> is returned in their places instead.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The number of rows to be fetched, or -1 for all. 
 * @param p->retO receives a matrix, where <code>String[0]<code> is the first row, and <code>String[0][0], String[0][1]...</code> are the column 
 * elements of the first row. Returns <code>null</code> if here's no more element to be fetched. Double/float values will be formatted using the 
 * <code>setDecimalPlaces()</code> settings. If the value is SQL <code>NULL</code> or a <code>blob</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getStrings_i(NMParams p) // litebase/ResultSet public native String[][] getStrings(int count);
{  
	TRACE("lRS_getStrings_i")
   MEMORY_TEST_START
   getStrings(p, p->i32[0]); // juliana@201_2: corrected a bug that would let garbage in the number of records parameter.
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Starting from the current cursor position, it reads all result set rows of the result set. <code>first()</code>,  <code>last()</code>, 
 * <code>prev()</code> or <code>next()</code> must be used to set the current position, but not <code>beforeFirst()</code> or 
 * <code>afterLast()</code>. It doesn't return BLOB values. <code>null</code> is returned in their places instead. 
 *
 * @param p->obj[0] The result set.
 * @param p->retO receives a matrix, where <code>String[0]<code> is the first row, and <code>String[0][0], String[0][1]...</code> are the column 
 * elements of the first row. Returns <code>null</code> if here's no more element to be fetched. Double/float values will be formatted using the 
 * <code>setDecimalPlaces()</code> settings. If the value is SQL <code>NULL</code> or a <code>blob</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getStrings(NMParams p) // litebase/ResultSet public native String[][] getStrings();
{  
	TRACE("lRS_getStrings")
   MEMORY_TEST_START
   getStrings(p, -1); // juliana@201_2: corrected a bug that would let garbage in the number of records parameter.
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column index (starting from 1), returns a <code>Date</code> value that is represented by this column. Note that it is only possible 
 * to request this column as a date if it was created this way (DATE or DATETIME).
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getDate_i(NMParams p) // litebase/ResultSet public native totalcross.util.Date getDate(int col);
{
	TRACE("lRS_getDate_i")
	MEMORY_TEST_START
	rsGetByIndex(p, DATE_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Given the column name (case insensitive), returns a <code>Date</code> value that is represented by this column. Note that it is only possible 
 * to request this column as a date if it was created this way (DATE or DATETIME). This method is slightly slower then the method that accepts a 
 * column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getDate_s(NMParams p) // litebase/ResultSet public native totalcross.util.Date getDate(String colName);
{
	TRACE("lRS_getDate_s")
	MEMORY_TEST_START
	rsGetByName(p, DATE_TYPE);
	MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Given the column index (starting from 1), returns a <code>Time</code> (correspondent to a DATETIME data type) value that is represented by this 
 * column. Note that it is only possible to request this column as a date if it was created this way.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The colum index.
 * @param p->retO receives the time of the DATETIME. If the DATETIME value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getDateTime_i(NMParams p) // litebase/ResultSet public native totalcross.sys.Time getDateTime(int colIdx);
{
	TRACE("lRS_getDateTime_i")
   MEMORY_TEST_START
	rsGetByIndex(p, DATETIME_TYPE);
	MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Given the column name (case insensitive), returns a <code>Time</code> (correspondent to a DATETIME data type) value that is represented by this
 * column. Note that it is only possible to request this column as a date if it was created this way. This method is slightly slower then the 
 * method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[0] The colum name.
 * @param p->retO receives the time of the DATETIME. If the DATETIME value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getDateTime_s(NMParams p) // litebase/ResultSet public native totalcross.sys.Time getDateTime(String colName); 
{
	TRACE("lRS_getDateTime_s")
	MEMORY_TEST_START
	rsGetByName(p, DATETIME_TYPE);
	MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Places this result set cursor at the given absolute row. This is the absolute physical row of the table. This method is usually used to restore
 * the row at a previous row got with the <code>getRow()</code> method.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The row to set the cursor.
 */
LB_API void lRS_absolute_i(NMParams p) // litebase/ResultSet public native bool absolute(int row);
{
	TRACE("lRS_absolute_i")
   Context context = p->currentContext;
   TCObject resultSet = p->obj[0];
   int32 row = p->i32[0],
         i = 0;
   
   MEMORY_TEST_START

   if (testRSClosed(context, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(p->obj[0]);
      Table* table = rsBag->table;
      PlainDB* plainDB = &table->db;
      uint8* rowsBitmap = rsBag->allRowsBitmap;
      int32 rowCountLess1 = plainDB->rowCount - 1;

      // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
      if (rowsBitmap != null)
      {
         while (i <= rowCountLess1 && i <= row)
         {
            if (isBitUnSet(rowsBitmap, i))
               row++;
            i++;
         }
         
         if ((p->retI = plainRead(context, plainDB, rsBag->pos = i - 1)))
            xmemmove(table->columnNulls, plainDB->basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
         else
            goto finish;
      }
	   else if (table->deletedRowsCount > 0) // juliana@210_1: select * from table_name does not create a temporary table anymore.
      {
         int32 rowCount = 0;
         
         // Continues searching the position until finding the right row or the end of the result set table.
         while (rowCount <= rowCountLess1 && rowCount <= row)
         {   
            // Reads the next row.
            rsBag->pos = rowCount;
			   if (!plainRead(context, plainDB, rowCount++))	
				   goto finish;
            xmove4(&i, plainDB->basbuf);
            
			   if ((i & ROW_ATTR_MASK) == ROW_ATTR_DELETED) // If it was deleted, one more row will be read in total.
               row++;
         }
         xmemmove(table->columnNulls, plainDB->basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
      } 
      else if (0 <= row && row <= rowCountLess1)
      {
         rsBag->pos = row;
		   if ((p->retI = plainRead(context, plainDB, row)))				
            xmemmove(table->columnNulls, plainDB->basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
	   }
   }
   
finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Moves the cursor <code>rows</code> in distance. The value can be greater or lower than zero.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The distance to move the cursor.
 * @param p->retI receives <code>true</code> whenever this method does not throw an exception.
 */
LB_API void lRS_relative_i(NMParams p) // litebase/ResultSet public native bool relative(int rows);
{
	TRACE("lRS_relative_i")
   TCObject resultSet = p->obj[0];
   Context context = p->currentContext;
   
   MEMORY_TEST_START

   if (testRSClosed(context, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      Table* table = rsBag->table;
      PlainDB* plainDB = &table->db;
      uint8* rowsBitmap = rsBag->allRowsBitmap;
      int32 rows = p->i32[0],
            rowCountLess1 = plainDB->rowCount - 1,
            pos = rsBag->pos;
		
	   if (rowsBitmap) // juliana@210_1: select * from table_name does not create a temporary table anymore.
      {
         // Continues searching the position until finding the right row or the end or the beginning of the result set table.
         if (rows > 0)
            while (--rows >= 0)
				   while (pos++ < rowCountLess1 && isBitUnSet(rowsBitmap, pos));
         else
            while (++rows <= 0)
               while (pos-- > 0 && isBitUnSet(rowsBitmap, pos));
         
         if (pos < 0)
            while (pos++ < rowCountLess1 && isBitUnSet(rowsBitmap, pos));
         if (pos > plainDB->rowCount - 1)
            while (pos-- > 0 && isBitUnSet(rowsBitmap, pos));
         
         if (plainRead(context, plainDB, rsBag->pos = pos))
            xmemmove(table->columnNulls, plainDB->basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
      } 
      else if (table->deletedRowsCount) // juliana@210_1: select * from table_name does not create a temporary table anymore.
      {
         int32 value;
         uint8* basbuf = plainDB->basbuf;
      
         // Continues searching the position until finding the right row or the end or the beginning of the result set table.
         if (rows > 0)
            while (--rows >= 0)
				   while (pos++ < rowCountLess1) // juliana@210_1: select * from table_name does not create a temporary table anymore. 
               {
			         if (!plainRead(context, plainDB, pos))
				         goto finish;
			         xmove4(&value, basbuf); 
			         if ((value & ROW_ATTR_MASK) != ROW_ATTR_DELETED)
                     break;
               }
         else
            while (++rows <= 0)
               while (pos-- > 0) // juliana@210_1: select * from table_name does not create a temporary table anymore. 
               {
			         if (!plainRead(context, plainDB, pos))
				         goto finish;
			         xmove4(&value, basbuf); 
			         if ((value & ROW_ATTR_MASK) != ROW_ATTR_DELETED)
                     break;
               }
         if (pos < 0)
            while (pos++ < rowCountLess1) // juliana@210_1: select * from table_name does not create a temporary table anymore. 
            {
		         if (!plainRead(context, plainDB, pos))
			         goto finish;
		         xmove4(&value, basbuf); 
		         if ((value & ROW_ATTR_MASK) != ROW_ATTR_DELETED)
                  break;
            }
         if (pos > plainDB->rowCount - 1)
            while (pos-- > 0) // juliana@210_1: select * from table_name does not create a temporary table anymore. 
            {
		         if (!plainRead(context, plainDB, pos))
			         goto finish;
		         xmove4(&value, basbuf); 
		         if ((value & ROW_ATTR_MASK) != ROW_ATTR_DELETED)
                  break;
            }
            
         rsBag->pos = pos;
         xmemmove(table->columnNulls, plainDB->basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
      } 
	   else
	   {
         // The new pos is pos + rows or 0 (if pos + rows < 0) or bag.lastRecordIndex (if pos + rows > bag.lastRecordIndex).
		   int32 newPos = MAX(0, MIN(plainDB->rowCount - 1, rsBag->pos + rows));
		   if (rsBag->pos != newPos) // If there are no deleted rows, just reads the row in the right position.
		   {
			   rsBag->pos = newPos;
			   if ((p->retI = plainRead(context, plainDB, newPos)))
				   xmemmove(table->columnNulls, plainDB->basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
		   }
	   }
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// juliana@265_1: corrected getRow() behavior, which must match with absolute(). 
/**
 * Returns the current physical row of the table where the cursor is. It must be used with <code>absolute()</code> method.
 *
 * @param p->obj[0] The result set.
 * @param p->retI receives the current physical row of the table where the cursor is.
 */
LB_API void lRS_getRow(NMParams p) // litebase/ResultSet public native int getRow();
{
	TRACE("lRS_getRow")
   TCObject resultSet = p->obj[0];
   Context context = p->currentContext;
   
   MEMORY_TEST_START
   
   if (testRSClosed(context, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* resultSetBag = getResultSetBag(resultSet);
      Table* table = resultSetBag->table;
      PlainDB* plainDB = &table->db;
      uint8* rowsBitmap = resultSetBag->allRowsBitmap;
      uint8* basbuf = plainDB->basbuf;
      int32 pos = resultSetBag->pos;
      
      if (pos == -1 || pos == plainDB->rowCount)
         p->retI = pos;
      else if (rowsBitmap)
      {
         int32 i = -1,
               absolute = 0;
            
         while (++i < pos)
            if (isBitSet(rowsBitmap, i))
               absolute++;
         p->retI = absolute;
      }
      else if (table->deletedRowsCount)
      {
         int32 i = -1,
               absolute = 0,
               value;
   
            // juliana@201_27: solved a bug in next() and prev() that would happen after doing a delete from table_name. 
            while (++i < pos) 
            {
               if (!plainRead(context, plainDB, i))
				      goto finish;
				   xmove4(&value, basbuf); 
               if ((value & ROW_ATTR_MASK) != ROW_ATTR_DELETED)
                  absolute++;
            }

            if (plainRead(context, plainDB, i - 1))
		         xmemmove(table->columnNulls, basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
            p->retI = absolute;
      }
      else
         p->retI = pos; // Returns the current position of the cursor.
   }
finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Sets the number of decimal places that the given column (starting from 1) will have when being converted to <code>String</code>.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column.
 * @param p->i32[1] The number of decimal places.
 * @throws DriverException If the column index is invalid, or the value for decimal places is invalid.
 */
LB_API void lRS_setDecimalPlaces_ii(NMParams p) // litebase/ResultSet public native void setDecimalPlaces(int col, int places) throws DriverException;
{
	TRACE("lRS_setDecimalPlaces_ii")
   TCObject resultSet = p->obj[0];
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      int32 column = p->i32[0] - 1,
            places = p->i32[1];

      if (column < 0 || column >= rsBag->selectClause->fieldsCount) // The columns given by the user ranges from 1 to n.
         TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_INVALID_COLUMN_NUMBER), column);
      else
      {
         int32 type;
      
         // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
         if (rsBag->allRowsBitmap || rsBag->isSimpleSelect)
         {
            SQLResultSetField* field = rsBag->selectClause->fieldList[column];
            column = field->parameter? field->parameter->tableColIndex : field->tableColIndex;
         }
            
         type = rsBag->table->columnTypes[column]; // Gets the column type.
         
         if (places < -1 || places > 40) // Invalid value for decimal places.
            TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_RS_DEC_PLACES_START), places);
         else if (type == FLOAT_TYPE || type == DOUBLE_TYPE) // Only sets the decimal places if the type is FLOAT or DOUBLE.
            rsBag->decimalPlaces[column] = places;
         else
            TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES));
      }
   }
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Returns the number of rows of the result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retI receives the number of rows.
 */
LB_API void lRS_getRowCount(NMParams p) // litebase/ResultSet public native int getRowCount();
{
	TRACE("lRS_getRowCount")
   TCObject resultSet = p->obj[0];
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
         
      // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
      // juliana@114_10: removes the deleted rows.
      p->retI = rsBag->allRowsBitmap? rsBag->answerCount : rsBag->table->db.rowCount - rsBag->table->deletedRowsCount;  
   }
   
   MEMORY_TEST_END
}
//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Given the column index (starting from 1), indicates if this column has a <code>NULL</code>.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retI receives <code>true</code> if the value is SQL <code>NULL</code>; <code>false</code>, otherwise.
 */
LB_API void lRS_isNull_i(NMParams p) // litebase/ResultSet public native boolean isNull(int col);
{
	TRACE("lRS_isNull_i")
   TCObject resultSet = p->obj[0];
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
      rsPrivateIsNull(p);
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Given the column name (case insensitive), indicates if this column has a <code>NULL</code>.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retI receives <code>true</code> if the value is SQL <code>NULL</code>; <code>false</code>, otherwise.
 * @throws NullPointerException If the column name is null.
 */
LB_API void lRS_isNull_s(NMParams p) // litebase/ResultSet public native boolean isNull(String colName) throws NullPointerException;
{
	TRACE("lRS_isNull_s")
   TCObject resultSet = p->obj[0],
          colName = p->obj[1];
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      if (colName)
      {
         p->i32[0] = TC_htGet32Inv(&getResultSetBag(resultSet)->intHashtable, identHashCode(colName)) + 1;
         rsPrivateIsNull(p);
      }         
      else
         TC_throwNullArgumentException(p->currentContext, "colName");
   }
  
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@270_30: added ResultSet.rowToString().
/**
 * Transforms a <code>ResultSet</code> row in a string.
 *
 * @param p->obj[0] The result set.
 * @param p->retO receives a whole current row of a <code>ResultSet</code> in a string with column data separated by tab. 
 */
LB_API void lRS_rowToString(NMParams p)
{
   TRACE("lRS_rowToString")
   TCObject resultSetObj = p->obj[0];
   Context context = p->currentContext;

   MEMORY_TEST_START
   
   if (testRSClosed(context, resultSetObj)) // The driver and the result set can't be closed.
   {
      ResultSet* resultSet = getResultSetBag(resultSetObj);
      Table* table = resultSet->table;
      int32 position = resultSet->pos;

      if (position >= 0 && position <= table->db.rowCount - 1) // Invalid result set position.
      {
         // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
         int8* columnTypes = table->columnTypes;
         int8* decimalPlaces = resultSet->decimalPlaces;
         uint8* columnNulls0 = table->columnNulls;
         SQLValue value;
         bool notTemporary = resultSet->answerCount >= 0 || resultSet->isSimpleSelect;
         SQLResultSetField** fields = resultSet->selectClause->fieldList;
         SQLResultSetField* field;
         TCObject strings[MAXIMUMS];
         TCObject result;
         JCharP resultStr;

         // juliana@211_4: solved bugs with result set dealing.
         // juliana@211_3: the string matrix size can't take into consideration rows that are before the result set pointer.
         int32 cols = resultSet->selectClause->fieldsCount,
               i = cols, 
               j = 0,
               k,
               finalSize = 0;

         while (--i >= 0) // Fetches all the strings.
         {
            field = fields[i];
            k = notTemporary? (field->parameter? field->parameter->tableColIndex : field->tableColIndex) : i;   

            if (isBitUnSet(columnNulls0, k) && columnTypes[k] != BLOB_TYPE) 
            {
               // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
               if (!(strings[i] = rsGetString(context, resultSet, k, &value)) || field->isDataTypeFunction)
               {
                  if (field->isDataTypeFunction)
                  {
                     rsApplyDataTypeFunction(p, &value, field, UNDEFINED_TYPE);
                     if (columnTypes[k] == CHARS_TYPE || columnTypes[k] == CHARS_NOCASE_TYPE)
                     {
                        if ((strings[i] = TC_createStringObjectWithLen(context, value.length)))
                           xmemmove(String_charsStart(strings[i]), value.asChars, value.length << 1); 
                     }
                     else
                        TC_setObjectLock(strings[i] = p->retO, LOCKED); 
                  }
                  else
                  {
                     createString(p, &value, columnTypes[k], decimalPlaces? decimalPlaces[k] : -1);
                     TC_setObjectLock(strings[i] = p->retO, LOCKED);
                  }
                  
               }
            }
            else
               strings[i] = null;
         }

         // Fetches the final size of the resultant string
         i = cols;
         while (--i >= 0)
            if (strings[i])
			      finalSize += String_charsLen(strings[i]);
         finalSize += cols - 1;

         TC_setObjectLock(p->retO = result = TC_createStringObjectWithLen(context, finalSize), UNLOCKED); 
         resultStr = String_charsStart(result);

         // Copies the strings to the resultant string.
         i = -1;
         while (++i < cols)
         {
            if (strings[i])
               xmemmove(&resultStr[j], String_charsStart(strings[i]), (k = String_charsLen(strings[i])) << 1);
            else
               k = 0;
            if (i + 1 < cols)
               resultStr[j += k] = '\t';
            j++;
            TC_setObjectLock(strings[i], UNLOCKED);
         }        
      }
      else
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_RS_INV_POS), position);
   }
  
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Gets the number of columns for this <code>ResultSet</code>.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->retI receives the number of columns for this <code>ResultSet</code>.
 */
LB_API void lRSMD_getColumnCount(NMParams p) // litebase/ResultSetMetaData public native int getColumnCount();
{
	TRACE("lRSMD_getColumnCount")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
         
      // juliana@230_36: corrected ResultSetMetaData returning extra columns in queries with order by where there are ordered fields that are not 
      // in the select clause.
      // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
      // juliana@210_1: select * from table_name does not create a temporary table anymore.
      p->retI = getResultSetBag(resultSet)->selectClause->fieldsCount;
   }
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.
// litebase/ResultSetMetaData public native int getColumnDisplaySize(int column) throws IllegalArgumentException;
/**
 * Given the column index (starting at 1), returns the display size. For chars, it will return the number of chars defined; for primitive types, it 
 * will return the number of decimal places it needs to be displayed correctly. Returns 0 if an error occurs.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->i32[0] The column index (starting at 1).
 * @param p->retI receives the display size or -1 if a problem occurs.
 * @throws IllegalArgumentException If the column index is invalid.
 */
LB_API void lRSMD_getColumnDisplaySize_i(NMParams p) 
{
	TRACE("lRSMD_getColumnDisplaySize_i")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      int32 column = p->i32[0] - 1;

      // juliana@230_36: corrected ResultSetMetaData returning extra columns in queries with order by where there are ordered fields that are not 
      // in the select clause. 
      // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
	   // juliana@213_5: Now a DriverException is thrown instead of returning an invalid value.
      if (column < 0 || column >= rsBag->selectClause->fieldsCount)
         TC_throwExceptionNamed(p->currentContext, "java.lang.IllegalArgumentException", getMessage(ERR_INVALID_COLUMN_NUMBER));
      else
      {
         if (rsBag->allRowsBitmap || rsBag->isSimpleSelect)
         {
            SQLResultSetField* field = rsBag->selectClause->fieldList[column];
            column = field->parameter? field->parameter->tableColIndex : field->tableColIndex;
         } 
         
         // juliana@210_1: select * from table_name does not create a temporary table anymore.

         switch (rsBag->table->columnTypes[column])
         {
            case SHORT_TYPE:  
               p->retI = 6; 
               break;
            case INT_TYPE:    
               p->retI = 11; 
               break;
            case LONG_TYPE:   
               p->retI = 20; 
               break;
            case FLOAT_TYPE:  
               p->retI = 13; 
               break;
            case DOUBLE_TYPE: 
               p->retI = 21; 
               break;
            case CHARS_TYPE:
            case CHARS_NOCASE_TYPE: 
               p->retI = rsBag->table->columnSizes[column]; 
               break;
            case DATE_TYPE: // rnovais@570_12 
               p->retI = 11; 
               break; 
            case DATETIME_TYPE: // rnovais@570_12
               p->retI = 31; // (10 + 19) 
               break; 
            case BLOB_TYPE:     
               p->retI = -1; 
         }
      }
   }

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.
// litebase/ResultSetMetaData public native String getColumnLabel(int column) throws IllegalArgumentException;
/**
 * Given the column index (starting at 1), returns the column name. Note that if an alias is used to the column, the alias will be returned instead. 
 * If an error occurs, an empty string is returned. Note that LitebaseConnection 2.x tables must be recreated to be able to return this label 
 * information.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->i32[0] The column index (starting at 1).
 * @param p->retO receives the name or alias of the column, which can be an empty string if an error occurs.
 * @throws IllegalArgumentException If the column index is invalid.
 */
LB_API void lRSMD_getColumnLabel_i(NMParams p) 
{
	TRACE("lRSMD_getColumnLabel_i")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      int32 column = p->i32[0];

      // juliana@230_36: corrected ResultSetMetaData returning extra columns in queries with order by where there are ordered fields that are not 
      // in the select clause.
      // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
	   // juliana@213_5: Now a DriverException is thrown instead of returning an invalid value.
	   if (column <= 0 || column > rsBag->selectClause->fieldsCount)
         TC_throwExceptionNamed(p->currentContext, "java.lang.IllegalArgumentException", getMessage(ERR_INVALID_COLUMN_NUMBER));
      else // juliana@210_1: select * from table_name does not create a temporary table anymore. 
         TC_setObjectLock(p->retO = TC_createStringObjectFromCharP(p->currentContext, rsBag->selectClause->fieldList[column - 1]->alias, -1), 
                                                                                                                                         UNLOCKED); 
   }

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.
// litebase/ResultSetMetaData public native int getColumnType(int column) throws IllegalArgumentException;
/**
 * Given the column index (starting at 1), returns the column type.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->i32[0] The column index (starting at 1).
 * @param p->retI receives the column type, which can be: <b><code>SHORT_TYPE</b></code>, <b><code>INT_TYPE</b></code>, 
 * <b><code>LONG_TYPE</b></code>, <b><code>FLOAT_TYPE</b></code>, <b><code>DOUBLE_TYPE</b></code>, <b><code>CHAR_TYPE</b></code>, 
 * <b><code>CHAR_NOCASE_TYPE</b></code>, <b><code>DATE_TYPE</b></code>, <b><code>DATETIME_TYPE</b></code>, or <b><code>BLOB_TYPE</b></code>.
 * @throws IllegalArgumentException If the column index is invalid.
 */
LB_API void lRSMD_getColumnType_i(NMParams p) 
{
	TRACE("lRSMD_getColumnType_i")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      int32 column = p->i32[0] - 1;

      // juliana@230_36: corrected ResultSetMetaData returning extra columns in queries with order by where there are ordered fields that are not 
      // in the select clause.
      // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
      // juliana@213_5: Now a DriverException is thrown instead of returning an invalid value.
	   if (column < 0 || column >= rsBag->selectClause->fieldsCount)
         TC_throwExceptionNamed(p->currentContext, "java.lang.IllegalArgumentException", getMessage(ERR_INVALID_COLUMN_NUMBER));
      else
         p->retI = rsBag->selectClause->fieldList[column]->dataType;
   }
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/ResultSetMetaData public native String getColumnTypeName(int column) throws DriverException;
/**
 * Given the column index (starting at 1), returns the name of the column type.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->i32[0] The column index (starting at 1).
 * @param p->retO receives the name of the column type, which can be: <b><code>chars</b></code>, <b><code>short</b></code>, <b><code>int</b></code>, 
 * <b><code>long</b></code>, <b><code>float</b></code>, <b><code>double</b></code>, <b><code>date</b></code>, <b><code>datetime</b></code>, 
 * <b><code>blob</b></code>, or null if an error occurs.
 */
LB_API void lRSMD_getColumnTypeName_i(NMParams p) 
{
	TRACE("lRSMD_getColumnTypeName_i")
   CharP ret = "";

   lRSMD_getColumnType_i(p);
   MEMORY_TEST_START

   switch (p->retI)
   {
      case CHARS_TYPE:
      case CHARS_NOCASE_TYPE:
         ret = "chars";
         break;
      case SHORT_TYPE:
         ret = "short";
         break;
      case INT_TYPE:
         ret = "int";
         break;
      case LONG_TYPE:
         ret = "long";
         break;
      case FLOAT_TYPE:
         ret = "float";
         break;
      case DOUBLE_TYPE:
         ret = "double";
         break;
      case DATE_TYPE:
         ret = "date";
         break;
      case DATETIME_TYPE:
         ret = "datetime";
         break;
      case BLOB_TYPE:
         ret = "blob";
   } 

   TC_setObjectLock(p->retO = TC_createStringObjectFromCharP(p->currentContext, ret, -1), UNLOCKED);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/ResultSetMetaData public native String getColumnTableName(int columnIdx) throws IllegalArgumentException;
/**
 * Given the column index, (starting at 1) returns the name of the table it came from.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->i32[0] The column index.
 * @param p->retO receives the name of the table it came from or <code>null</code> if the column index does not exist.
 * @throws IllegalArgumentException If the column index is invalid.
 */
LB_API void lRSMD_getColumnTableName_i(NMParams p) 
{
	TRACE("lRSMD_getColumnTableName_i")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      SQLResultSetField** fields = rsBag->selectClause->fieldList;
      int32 column = p->i32[0] - 1;

      p->retO = null;

      // juliana@230_36: corrected ResultSetMetaData returning extra columns in queries with order by where there are ordered fields that are not 
      // in the select clause.
      // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
	   // juliana@213_5: Now a DriverException is thrown instead of returning an invalid value.
	   if (column < 0 || column >= rsBag->selectClause->fieldsCount)
         TC_throwExceptionNamed(p->currentContext, "java.lang.IllegalArgumentException", getMessage(ERR_INVALID_COLUMN_NUMBER));
      else

	      // null is a valid return value.
         TC_setObjectLock(p->retO = fields[column]->tableName? TC_createStringObjectFromCharP(p->currentContext, fields[column]->tableName, -1) 
                                                             : null, UNLOCKED);
   }
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// litebase/ResultSetMetaData public native String getColumnTableName(String columnName) throws DriverException, NullPointerException;
/**
 * Given the column name or alias, returns the name of the table it came from.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->obj[1] The column name.
 * @param p->retO receives the name of the table it came from or <code>null</code> if the column name does not exist. 
 * @throws DriverException If the column was not found.
 * @throws NullPointerException if the column name is null.
 */
LB_API void lRSMD_getColumnTableName_s(NMParams p) 
{
	TRACE("lRSMD_getColumnTableName_s")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   
   MEMORY_TEST_START
   
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      TCObject columnNameStr = p->obj[1];

      if (columnNameStr)
      {
         SQLSelectClause* clause = getResultSetBag(resultSet)->selectClause;
         SQLResultSetField** fields = clause->fieldList;
         int32 i = -1,
               length = clause->fieldsCount;
         JCharP columnNameJCharP = String_charsStart(columnNameStr);
         int32 columnNameLength = String_charsLen(columnNameStr);
         CharP tableColName,
               tableName;

         p->retO = null;
         
         while (++i < length) // Gets the name of the table or its alias given the column name.
         {
            if ((((tableColName = fields[i]->tableColName) 
               && JCharPEqualsCharP(columnNameJCharP, tableColName, columnNameLength, xstrlen(tableColName), true))) 
              || ((tableColName = fields[i]->alias) 
               && JCharPEqualsCharP(columnNameJCharP, tableColName, columnNameLength, xstrlen(tableColName), true)))
            {
               TC_setObjectLock(p->retO = (tableName = fields[i]->tableName)? TC_createStringObjectFromCharP(p->currentContext, tableName, -1) 
                                                                            : null, UNLOCKED);
               break;
            }
         }
         if (i == length) // Column name or alias not found.
         {
            tableColName = TC_JCharP2CharP(columnNameJCharP, columnNameLength);
            TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_COLUMN_NOT_FOUND), tableColName? tableColName : "");
            xfree(tableColName);
         }
      }
      else // The column name can't be null.
         TC_throwNullArgumentException(p->currentContext, "columnName");
   }
   
   MEMORY_TEST_END
}

// juliana@227_2: added methods to indicate if a column of a result set is not null or has default values.
//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// juliana@227_2: added methods to indicate if a column of a result set is not null or has default values.
// litebase/ResultSetMetaData public native boolean hasDefaultValue(int columnIndex) throws DriverException;
/**
 * Indicates if a column of the result set has default value.
 * 
 * @param p->i32[0] The column index.
 * @param p->retI receives <code>true</code> if the column has a default value; <code>false</code>, otherwise. 
 * @throws DriverException If the column does not have an underlining table.
 */
LB_API void lRSMD_hasDefaultValue_i(NMParams p) 
{
   TRACE("lRSMD_hasDefaultValue_i")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]),   
          nameObj = null;
   
   MEMORY_TEST_START
   
   lRSMD_getColumnTableName_i(p); // It already tests if the result set is valid.
   
   if (!p->currentContext->thrownException && (nameObj = p->retO))
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      Table* table;
      char nameCharP[DBNAME_SIZE];
      
      // Gets the table column info.
      TC_JCharP2CharPBuf(String_charsStart(nameObj), String_charsLen(nameObj), nameCharP);
      if ((table = getTable(p->currentContext, rsBag->driver, nameCharP)))
      {
         SQLResultSetField* field = rsBag->selectClause->fieldList[p->i32[0] - 1];

         // juliana@252_6: corrected a possible bug when using ResultSetMetaData in tables with more than 128 columns.
         p->retI = (table->columnAttrs[field->parameter? field->parameter->tableColIndex : field->tableColIndex] 
                                                                                         & ATTR_COLUMN_HAS_DEFAULT) != 0;
      }
   }
   else if (!nameObj) // The column does not have an underlining table.    
   {
      IntBuf buffer;
      TC_throwExceptionNamed(p->currentContext, "litebase.DriverException", getMessage(ERR_COLUMN_NOT_FOUND), TC_int2str(p->i32[0], buffer)); 
   }

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// litebase/ResultSetMetaData public native boolean hasDefaultValue(String columnName) throws DriverException, NullPointerException;
/**
 * Indicates if a column of the result set has default value.
 * 
 * @param p->obj[1] The column name.
 * @param p->retI receives <code>true</code> if the column has a default value; <code>false</code>, otherwise. 
 * @throws DriverException If the column was not found or does not have an underlining table.
 * @throws NullPointerException if the column name is null.
 */
LB_API void lRSMD_hasDefaultValue_s(NMParams p) 
{
   TRACE("lRSMD_hasDefaultValue_s")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   Context context = p->currentContext;

   MEMORY_TEST_START
   if (testRSClosed(context, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      TCObject columnNameStr = p->obj[1];

      if (columnNameStr)
      {
         SQLResultSetField** fields = rsBag->selectClause->fieldList; 
         SQLResultSetField* field; 
         JCharP columnNameJCharP = String_charsStart(columnNameStr);
         int32 i = -1,
               length = rsBag->selectClause->fieldsCount,
               columnNameLength = String_charsLen(columnNameStr);
         CharP tableColName;

         p->retO = null;
         
         while (++i < length) // Gets the name of the table or its alias given the column name.
         {
            if ((((tableColName = (field = fields[i])->tableColName) 
               && JCharPEqualsCharP(columnNameJCharP, tableColName, columnNameLength, xstrlen(tableColName), true))) 
              || ((tableColName = fields[i]->alias) 
               && JCharPEqualsCharP(columnNameJCharP, tableColName, columnNameLength, xstrlen(tableColName), true)))
            {
               if (field->tableName)
               {
                  Table* table;
                  if ((table = getTable(context, rsBag->driver, field->tableName)))
                     
                     // juliana@252_6: corrected a possible bug when using ResultSetMetaData in tables with more than 128 columns.
                     p->retI = (table->columnAttrs[field->parameter? field->parameter->tableColIndex : field->tableColIndex] 
                                                                                                     & ATTR_COLUMN_HAS_DEFAULT) != 0;
               }
               else
                  i = length;
               break;
            }
         }
         if (i == length) // Column name or alias not found.
         {   
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_COLUMN_NOT_FOUND), 
                                            (tableColName = TC_JCharP2CharP(columnNameJCharP, columnNameLength))? tableColName : "");
            xfree(tableColName);
         }
      }
      else // The column name can't be null.
         TC_throwNullArgumentException(context, "columnName");
   }
 
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * Indicates if a column of the result set is not null.
 * 
 * @param p->i32[0] The column index.
 * @param p->retI receives <code>true</code> if the column is not null; <code>false</code>, otherwise. 
 * @throws DriverException If the column does not have an underlining table.
 */
LB_API void lRSMD_isNotNull_i(NMParams p) // litebase/ResultSetMetaData public native boolean isNotNull(int columnIndex) throws DriverException;
{
   TRACE("lRSMD_isNotNull_i")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]),
          nameObj = null;
   Context context = p->currentContext;

   MEMORY_TEST_START
   
   lRSMD_getColumnTableName_i(p); // It already tests if the result set is valid.
   
   if (!context->thrownException && (nameObj = p->retO))
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      Table* table;
      char nameCharP[DBNAME_SIZE];
      
      // Gets the table column info.
      TC_JCharP2CharPBuf(String_charsStart(nameObj), String_charsLen(nameObj), nameCharP);
      if ((table = getTable(context, rsBag->driver, nameCharP)))
      {
         SQLResultSetField* field = rsBag->selectClause->fieldList[p->i32[0] - 1];

         // juliana@252_6: corrected a possible bug when using ResultSetMetaData in tables with more than 128 columns.
         p->retI = (table->columnAttrs[field->parameter? field->parameter->tableColIndex : field->tableColIndex] 
                                                                                         & ATTR_COLUMN_IS_NOT_NULL) != 0;
      }
   }
   else if (!nameObj) // The column does not have an underlining table.    
   {
      IntBuf buffer;
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_COLUMN_NOT_FOUND), TC_int2str(p->i32[0], buffer)); 
   }

   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// litebase/ResultSetMetaData public native boolean isNotNull(String columnName) throws DriverException, NullPointerException;
/**
 * Indicates if a column of the result set is not null.
 * 
 * @param p->obj[1] The column name.
 * @param p->retI receives <code>true</code> if the column is not null; <code>false</code>, otherwise. 
 * @throws DriverException If the column was not found or does not have an underlining table.
 * @throws NullPointerException if the column name is null.
 */
LB_API void lRSMD_isNotNull_s(NMParams p) 
{
   TRACE("lRSMD_isNotNull_s")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   Context context = p->currentContext;

   MEMORY_TEST_START
   if (testRSClosed(context, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      TCObject columnNameStr = p->obj[1];

      if (columnNameStr)
      {
         SQLResultSetField** fields = rsBag->selectClause->fieldList; 
         SQLResultSetField* field; 
         JCharP columnNameJCharP = String_charsStart(columnNameStr);
         int32 i = -1,
               length = rsBag->selectClause->fieldsCount,
               columnNameLength = String_charsLen(columnNameStr);
         CharP tableColName;

         p->retO = null;
         
         while (++i < length) // Gets the name of the table or its alias given the column name.
         {
            if ((((tableColName = (field = fields[i])->tableColName) 
               && JCharPEqualsCharP(columnNameJCharP, tableColName, columnNameLength, xstrlen(tableColName), true))) 
              || ((tableColName = fields[i]->alias) 
               && JCharPEqualsCharP(columnNameJCharP, tableColName, columnNameLength, xstrlen(tableColName), true)))
            {
               if (field->tableName)
               {
                  Table* table;
                  if ((table = getTable(context, rsBag->driver, field->tableName)))
               
                     // juliana@252_6: corrected a possible bug when using ResultSetMetaData in tables with more than 128 columns.
                     p->retI = (table->columnAttrs[field->parameter? field->parameter->tableColIndex : field->tableColIndex] 
                                                                                                     & ATTR_COLUMN_IS_NOT_NULL) != 0;          
               }
               else
                  i = length;
               break;
            }
         }
         if (i == length) // Column name or alias not found.
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_COLUMN_NOT_FOUND), 
                                            (tableColName = TC_JCharP2CharP(columnNameJCharP, columnNameLength))? tableColName : "");
            xfree(tableColName);
         }
      }
      else // The column name can't be null.
         TC_throwNullArgumentException(context, "columnName");
   }
   
   MEMORY_TEST_END
}

// juliana@253_3: added methods to return the primary key columns of a table.
//////////////////////////////////////////////////////////////////////////
// litebase/ResultSetMetaData public native byte[] getPKColumnIndices(String tableName) throws NullPointerException;
/**
 * Returns the primary key column indices of a table.
 * 
 * @param p->obj[1] The table name.
 * @param p->retO receives <code>null</code> if the given table does not have primary key or an array with the column indices of the primary key.
 * @throws NullPointerException if the table name is null.
 */
LB_API void lRSMD_getPKColumnIndices_s(NMParams p) 
{
   TRACE("lRSMD_getPKColumnIndices_s")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   Context context = p->currentContext;

   MEMORY_TEST_START
   
   if (testRSClosed(context, resultSet)) // The driver and the result set can't be closed.
   {
      TCObject tableNameStr = p->obj[1];
      
      if (tableNameStr)
      {
         ResultSet* rsBag = getResultSetBag(resultSet);
         char tableNameCharP[DBNAME_SIZE];
         Table* table; 
         
         // Gets the table given its name in the result set.
         TC_JCharP2CharPBuf(String_charsStart(tableNameStr), String_charsLen(tableNameStr), tableNameCharP); 
         TC_CharPToLower(tableNameCharP);
         if (!(table = getTableRS(context, rsBag, tableNameCharP))) 
            goto finish;
            
         if (table->primaryKeyCol != NO_PRIMARY_KEY) // Simple primary key.
         {
            if (!(p->retO = TC_createArrayObject(context, BYTE_ARRAY, 1)))
               goto finish;
            TC_setObjectLock(p->retO, UNLOCKED);
            ARRAYOBJ_START(p->retO)[0] = table->primaryKeyCol;            
         }
         else if (table->composedPK != NO_PRIMARY_KEY) // Composed primary key.
         { 
            if (!(p->retO = TC_createArrayObject(context, BYTE_ARRAY, table->numberComposedPKCols)))
               goto finish;
            TC_setObjectLock(p->retO, UNLOCKED);   
            xmemmove(ARRAYOBJ_START(p->retO), table->composedPrimaryKeyCols, table->numberComposedPKCols);
         }
         else
            p->retO = null;
      }
      else // The table name can't be null.      
         TC_throwNullArgumentException(context, "tableName");
   }
   
finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/ResultSetMetaData public native String[] getPKColumnNames(String tableName) throws NullPointerException;
/**
 * Returns the primary key column names of a table.
 * 
 * @param p->obj[1] The table name.
 * @param p->retO <code>null</code> if the given table does not have primary key or an array with the column names of the primary key.
 * @throws NullPointerException if the table name is null.
 */
LB_API void lRSMD_getPKColumnNames_s(NMParams p) 
{
   TRACE("lRSMD_getPKColumnNames_s")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   Context context = p->currentContext;

   MEMORY_TEST_START
   
   if (testRSClosed(context, resultSet)) // The driver and the result set can't be closed.
   {
      TCObject tableNameStr = p->obj[1];
      
      if (tableNameStr)
      {
         ResultSet* rsBag = getResultSetBag(resultSet);
         char tableNameCharP[DBNAME_SIZE];
         Table* table; 
         
         // Gets the table given its name in the result set.
         TC_JCharP2CharPBuf(String_charsStart(tableNameStr), String_charsLen(tableNameStr), tableNameCharP); 
         TC_CharPToLower(tableNameCharP);
         if (!(table = getTableRS(context, rsBag, tableNameCharP))) 
            goto finish;
      
         if (table->primaryKeyCol != NO_PRIMARY_KEY) // Simple primary key.
         {
            TCObject* array;
            
            if (!(p->retO = TC_createArrayObject(context, "[java.lang.String", 1)))
               goto finish;
            TC_setObjectLock(p->retO, UNLOCKED);
            
            array = (TCObject*)ARRAYOBJ_START(p->retO);
            if (!(array[0] = TC_createStringObjectFromCharP(context, table->columnNames[table->primaryKeyCol], -1)))
               goto finish;
            TC_setObjectLock(array[0], UNLOCKED);            
         }
         else if (table->composedPK != NO_PRIMARY_KEY) // Composed primary key.
         {  
            TCObject* array;
            int32 i = table->numberComposedPKCols;
            uint8* composedPKCols = table->composedPrimaryKeyCols;
            CharP* columnNames = table->columnNames;
            
            if (!(p->retO = TC_createArrayObject(context, "[java.lang.String", i)))
               goto finish;
            TC_setObjectLock(p->retO, UNLOCKED);
            array = (TCObject*)ARRAYOBJ_START(p->retO);
            
            while (--i >= 0)
            {
               if (!(array[i] = TC_createStringObjectFromCharP(context, columnNames[composedPKCols[i]], -1)))
                  goto finish;
               TC_setObjectLock(array[i], UNLOCKED);    
            }      
         }
         else
            p->retO = null;
      }
      else // The table name can't be null.      
         TC_throwNullArgumentException(context, "tableName");
   }
   
finish: ;
   MEMORY_TEST_END
}

// juliana@253_4: added methods to return the default value of a column.
//////////////////////////////////////////////////////////////////////////
// litebase/ResultSetMetaData public native String getDefaultValue(int columnIndex) throws DriverException;
/**
 * Returns the default value of a column.
 * 
 * @param p->i32[0] The column index.
 * @return p->retO receives the default value of the column as a string or <code>null</code> if there is no default value.
 * @throws DriverException If the column index does not have an underlining table.
 */
LB_API void lRSMD_getDefaultValue_i(NMParams p) 
{
   TRACE("lRSMD_getDefaultValue_i")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]),   
          nameObj = null;
   Context context = p->currentContext;
   
   MEMORY_TEST_START
   
   lRSMD_getColumnTableName_i(p); // It already tests if the result set is valid.
   
   if (!context->thrownException && (nameObj = p->retO))
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      char nameCharP[DBNAME_SIZE];
      SQLResultSetField* field = rsBag->selectClause->fieldList[p->i32[0] - 1];
      
      // Gets the table column info.
      TC_JCharP2CharPBuf(String_charsStart(nameObj), String_charsLen(nameObj), nameCharP);
      
      // Returns the default value of the column or the parameter of a function.
      TC_setObjectLock(p->retO = getDefault(context, rsBag, nameCharP, field->parameter? field->parameter->tableColIndex : field->tableColIndex), UNLOCKED);
   }
   else if (!nameObj) // The column does not have an underlining table.
   {
      IntBuf buffer;
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_COLUMN_NOT_FOUND), TC_int2str(p->i32[0], buffer)); 
   }
}

//////////////////////////////////////////////////////////////////////////
// litebase/ResultSetMetaData public native String getDefaultValue(String columnName) throws DriverException, NullPointerException;
/**
 * Returns the default value of a column.
 * 
 * @param p->obj[1] The column name.
 * @return p->retO receives the default value of the column as a string or <code>null</code> if there is no default value.
 * @throws DriverException If the column name does not have an underlining table.
 * @throws NullPointerException if the column name is null.
 */
LB_API void lRSMD_getDefaultValue_s(NMParams p) 
{
   TRACE("lRSMD_getDefaultValue_s")
   TCObject resultSet = OBJ_ResultSetMetaData_ResultSet(p->obj[0]);
   Context context = p->currentContext;

   MEMORY_TEST_START
   if (testRSClosed(p->currentContext, resultSet)) // The driver and the result set can't be closed.
   {
      ResultSet* rsBag = getResultSetBag(resultSet);
      TCObject columnNameStr = p->obj[1];

      if (columnNameStr)
      {
         SQLResultSetField** fields = rsBag->selectClause->fieldList; 
         SQLResultSetField* field; 
         JCharP columnNameJCharP = String_charsStart(columnNameStr);
         int32 i = -1,
               length = rsBag->selectClause->fieldsCount,
               columnNameLength = String_charsLen(columnNameStr);
         CharP tableColName;

         p->retO = null;
         
         while (++i < length) // Gets the name of the table or its alias given the column name.
         {
            if ((((tableColName = (field = fields[i])->tableColName) 
               && JCharPEqualsCharP(columnNameJCharP, tableColName, columnNameLength, xstrlen(tableColName), true))) 
              || ((tableColName = fields[i]->alias) 
               && JCharPEqualsCharP(columnNameJCharP, tableColName, columnNameLength, xstrlen(tableColName), true)))
            {
               if (field->tableName) // Returns the default value of the column or the parameter of a function.
                  TC_setObjectLock(p->retO = getDefault(context, rsBag, field->tableName, field->parameter? field->parameter->tableColIndex : field->tableColIndex), UNLOCKED);     
               else
                  i = length;
               break;
            }
         }
         if (i == length) // Column name or alias not found.
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_COLUMN_NOT_FOUND), 
                                            (tableColName = TC_JCharP2CharP(columnNameJCharP, columnNameLength))? tableColName : "");
            xfree(tableColName);
         }
      }
      else // The column name can't be null.
         TC_throwNullArgumentException(context, "columnName");
   }
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// guich@550_43: fixed problem when reusing the statement.
// litebase/PreparedStatement public native litebase.ResultSet executeQuery() throws DriverException, OutOfMemoryError;
/**
 * This method executes a prepared SQL query and returns its <code>ResultSet</code>.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->retO receives the <code>ResultSet</code> of the SQL statement.
 * @throws DriverException If the statement to be execute is not a select or there are undefined parameters.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lPS_executeQuery(NMParams p) 
{
	TRACE("lPS_executeQuery")

   MEMORY_TEST_START
 
   if (testPSClosed(p))
   {
      TCObject stmt = p->obj[0];
      Context context = p->currentContext;
      
      if (OBJ_PreparedStatementType(stmt) != CMD_SELECT) // The statement must be a select.
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_QUERY_DOESNOT_RETURN_RESULTSET));
      else 
      {
         SQLSelectStatement* selectStmt = (SQLSelectStatement*)getPreparedStatementStatement(stmt); // The select statement.
         TCObject driver = OBJ_PreparedStatementDriver(stmt);

         if (!allParamValuesDefinedSel(selectStmt)) // All the parameters of the select statement must be defined.
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_NOT_ALL_PARAMETERS_DEFINED));
         else
         {
            ResultSet* resultSetBag;
            SQLSelectClause* selectClause = selectStmt->selectClause;
            Heap heap = selectClause->heap;
            bool locked = false;
            PlainDB* plainDB;
            TCObject logger = litebaseConnectionClass->objStaticValues[1];
          
            // juliana@253_18: now it is possible to log only changes during Litebase operation.
            if (logger && !litebaseConnectionClass->i32StaticValues[6]) // If log is on, adds information to it.
            { 
               LOCKVAR(log);
               if (OBJ_PreparedStatementStoredParams(stmt))
               {
                  TCObject string = toStringBuffer(context, stmt);
                  if (string)
                     TC_executeMethod(context, loggerLogInfo, logger, string); // juliana@230_30
               }
               else
                  TC_executeMethod(context, loggerLog, logger, 16, OBJ_PreparedStatementSqlExpression(stmt), false);
                            
               UNLOCKVAR(log);  
               if (context->thrownException)
                  goto finish;
            }

	         resetWhereClause(selectStmt->whereClause, heap);

            IF_HEAP_ERROR(heap)
            {
               TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
               goto finish;
            }

            // guich@554_37: tableColIndex may change between runs of a prepared statement with a sort field so we have to cache the tableColIndex of 
            // the order by fields.
            resetColumnListClause(selectStmt->orderByClause);

            // juliana@226_14: corrected a bug that would make a prepared statement with group by not work correctly after the first execution.
            resetColumnListClause(selectStmt->groupByClause);

            selectClause->isPrepared = true;
            TC_setObjectLock(p->retO = litebaseDoSelect(context, driver, selectStmt), UNLOCKED);

            if (p->retO)
            {
               // Gets the query result table size and stores it.
               locked = true;
	            LOCKVAR(parser);
               resultSetBag = (ResultSet*)getResultSetBag(p->retO);
               plainDB = &resultSetBag->table->db;
               if (!muPut(&memoryUsage, selectStmt->selectClause->sqlHashCode, plainDB->db.size, plainDB->dbo.size))
                  TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
	            UNLOCKVAR(parser);
	            locked = false;
            }
         }
      }
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * This method executes a SQL <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code> statement. SQL statements that return nothing such as
 * SQL DDL statements can also be executed.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->retI receives the result is either the row count for <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code> statements; or 0 
 * for SQL statements that return nothing.
 * @throws DriverException If the query does not update the table or there are undefined parameters.
 */
LB_API void lPS_executeUpdate(NMParams p) // litebase/PreparedStatement public native int executeUpdate() throws DriverException;
{
	TRACE("lPS_executeUpdate")

   MEMORY_TEST_START

   if (testPSClosed(p))
   {
      TCObject stmt = p->obj[0];   
      Context context = p->currentContext;
   
      if (OBJ_PreparedStatementType(stmt) == CMD_SELECT) // The statement must be a select.
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_QUERY_DOESNOT_PERFORM_UPDATE));
      else 
      {
         TCObject logger = litebaseConnectionClass->objStaticValues[1],
                driver = OBJ_PreparedStatementDriver(stmt);
      
         if (logger) // If log is on, adds information to it.
         {
            LOCKVAR(log);
            if (OBJ_PreparedStatementStoredParams(stmt))
            {
               TCObject string = toStringBuffer(context, stmt);
               if (string)
                  TC_executeMethod(context, loggerLogInfo, logger, string); // juliana@230_30
            }
            else
               TC_executeMethod(context, loggerLog, logger, 16, OBJ_PreparedStatementSqlExpression(stmt), false);
                      
            UNLOCKVAR(log);
            if (context->thrownException)
               goto finish;
         }
        
         // juliana@226_15: corrected a bug that would make a prepared statement with where clause and indices not work correctly after the first 
         // execution.
         switch (OBJ_PreparedStatementType(stmt)) // Returns the number of rows affected or if the command was successfully executed.
         {
            case CMD_INSERT:
            {
               SQLInsertStatement* insertStmt = (SQLInsertStatement*)getPreparedStatementStatement(stmt);
   			   
               rearrangeNullsInTable(insertStmt->table, insertStmt->record, insertStmt->storeNulls, insertStmt->paramDefined, 
                                                        insertStmt->paramIndexes, insertStmt->nFields, insertStmt->paramCount);
               if (convertStringsToValues(context, insertStmt->table, insertStmt->record, insertStmt->nFields))
                  p->retI = litebaseDoInsert(context, insertStmt);
               break;
            }
            case CMD_UPDATE:
            {
               SQLUpdateStatement* updateStmt = (SQLUpdateStatement*)getPreparedStatementStatement(stmt);
            
               resetWhereClause(updateStmt->whereClause, updateStmt->heap); // guich@554_13            
               rearrangeNullsInTable(updateStmt->rsTable->table, updateStmt->record, updateStmt->storeNulls, updateStmt->paramDefined, 
                                                                 updateStmt->paramIndexes, updateStmt->nValues, updateStmt->paramCount); 
               if (allParamValuesDefinedUpd(updateStmt) 
                && convertStringsToValues(context, updateStmt->rsTable->table, updateStmt->record, updateStmt->nValues))
                  p->retI = litebaseDoUpdate(context, updateStmt);
               break;
            }
            case CMD_DELETE:
            {
               SQLDeleteStatement* deleteStmt = (SQLDeleteStatement*)getPreparedStatementStatement(stmt);
               
               resetWhereClause(deleteStmt->whereClause, deleteStmt->heap); // guich@554_13
               if (allParamValuesDefinedDel(deleteStmt))
                  p->retI = litebaseDoDelete(context, deleteStmt);
               break;
            }
            case CMD_CREATE_TABLE:
            {
               TCObject sqlExpression = OBJ_PreparedStatementSqlExpression(stmt);
               
               litebaseExecute(context, driver, String_charsStart(sqlExpression), String_charsLen(sqlExpression));
               p->retI = 0;
               break;
            }
            default: // alter table or drop
            {
               TCObject sqlExpression = OBJ_PreparedStatementSqlExpression(stmt);               
               p->retI = litebaseExecuteUpdate(context, driver, String_charsStart(sqlExpression), String_charsLen(sqlExpression));
            }
         }
      }
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/PreparedStatement public native void setShort(int index, short value);
/**
 * This method sets the specified parameter from the given Java <code>short</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->i32[1] The value of the parameter.
 */
LB_API void lPS_setShort_is(NMParams p) 
{
	TRACE("lPS_setShort_is")
   MEMORY_TEST_START
   psSetNumericParamValue(p, SHORT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/PreparedStatement public native void setInt(int index, int value);
/**
 * This method sets the specified parameter from the given Java <code>int</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->i32[1] The value of the parameter.   
 */
LB_API void lPS_setInt_ii(NMParams p) 
{
	TRACE("lPS_setInt_ii")
	MEMORY_TEST_START
   psSetNumericParamValue(p, INT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/PreparedStatement public native void setLong(int index, long value);
/**
 * This method sets the specified parameter from the given Java <code>long</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->i64[0] The value of the parameter.
 */
LB_API void lPS_setLong_il(NMParams p) 
{
	TRACE("lPS_setLong_il")
   MEMORY_TEST_START
   psSetNumericParamValue(p, LONG_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/PreparedStatement public native void setFloat(int index, float value);
/**
 * This method sets the specified parameter from the given Java <code>float</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->dbl[0] The value of the parameter.
 */
LB_API void lPS_setFloat_id(NMParams p) 
{
	TRACE("lPS_setFloat_id")
   MEMORY_TEST_START
   psSetNumericParamValue(p, FLOAT_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// litebase/PreparedStatement public native void setDouble(int index, double value);
/**
 * This method sets the specified parameter from the given Java <code>double</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->dbl[0] The value of the parameter.
 */
LB_API void lPS_setDouble_id(NMParams p) 
{
	TRACE("lPS_setDouble_id")
   MEMORY_TEST_START
   psSetNumericParamValue(p, DOUBLE_TYPE);
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * This method sets the specified parameter from the given Java <code>String</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->obj[1] The value of the parameter. DO NOT SURROUND IT WITH '!.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lPS_setString_is(NMParams p) // litebase/PreparedStatement public native void setString(int index, String value) OutOfMemoryError;
{
	TRACE("lPS_setString_is")

   MEMORY_TEST_START
   if (testPSClosed(p))
   {
      TCObject stmt = p->obj[0];    
      SQLSelectStatement* statement = (SQLSelectStatement*)getPreparedStatementStatement(stmt);
      
      if (statement) // Only sets the parameter if the statement is not null.
      {
         TCObject string = p->obj[1];
         int32 index = p->i32[0];
      
         // juliana@238_1: corrected the end quote not appearing in the log files after dates. 
         // juliana@222_8: stores the object so that it won't be collected.
         if (psSetStringParamValue(p->currentContext, stmt, string, index, string? String_charsLen(string) : 0)) // Sets the string parameter.
            ((TCObject*)ARRAYOBJ_START(OBJ_PreparedStatementObjParams(stmt)))[index] = string; 
      }
   }
   
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * This method sets the specified parameter from the given array of bytes as a blob.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->obj[1] The value of the parameter.
 * @throws SQLParseException If the parameter to be set is in the where clause.
 */
LB_API void lPS_setBlob_iB(NMParams p) // litebase/PreparedStatement public native void setBlob(int index, uint8 []value) throws SQLParseException; 
{
	TRACE("lPS_setBlob_iB")
 
   MEMORY_TEST_START
   
   if (testPSClosed(p))
   {
      TCObject stmt = p->obj[0];
      SQLSelectStatement* statement = (SQLSelectStatement*)getPreparedStatementStatement(stmt);
      
      if (statement) // Only sets the parameter if the statement is not null.
      {
         TCObject blob = p->obj[1];
         TCObject* objParams = (TCObject*)ARRAYOBJ_START(OBJ_PreparedStatementObjParams(stmt));
         uint8* blobArray = null;
         int32 index = p->i32[0],
               blobLength = 0;
  
         if (blob)
         {
            blobLength = ARRAYOBJ_LEN(p->obj[1]);
            blobArray = (uint8*)ARRAYOBJ_START(p->obj[1]);
         }
        
         switch (statement->type) // Sets the parameter.
         {
            case CMD_INSERT:
               if (!setStrBlobParamValueIns(p->currentContext, (SQLInsertStatement*)statement, index, blobArray, blobLength, false))
                  goto finish;
               break;
            case CMD_UPDATE:
               if (!setStrBlobParamValueUpd(p->currentContext, (SQLUpdateStatement*)statement, index, blobArray, blobLength, false))
                  goto finish;
               break;

            // A blob can't be used in a where clause.
            case CMD_SELECT:
            case CMD_DELETE:
               TC_throwExceptionNamed(p->currentContext, "litebase.SQLParseException", getMessage(ERR_BLOB_WHERE));
               goto finish;
         }

         objParams[index] = p->obj[1]; // juliana@222_8: stores the object so that it won't be collected.

         if (OBJ_PreparedStatementStoredParams(stmt)) // Only stores the parameter if there are parameters to be stored.
         {
            JCharP* paramsAsStrs = getPreparedStatementParamsAsStrs(stmt);

            if (blob) // The parameter is not null.
               TC_CharP2JCharPBuf("[BLOB]", 6, paramsAsStrs[index], true);
            else // The parameter is null;
               TC_CharP2JCharPBuf("null", 4, paramsAsStrs[index], true);
         }
      }
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// litebase/PreparedStatement public native void setDate(int index, totalcross.Util.Date) throws OutOfMemoryError;
/**
 * This method sets the specified parameter from the given Java <code>Date</code> value formated as "YYYY/MM/DD" <br>
 * <b>IMPORTANT</b>: The constructor <code>new Date(string_date)</code> must be used with care. Some devices can construct different dates, according
 * to the device's date format. For example, the constructor <code>new Date("12/09/2006")</code>, depending on the device's date format, can generate 
 * a date like "12 of September of 2006" or "09 of December of 2006". To avoid this, use the constructor
 * <code>new Date(string_date, totalcross.sys.Settings.DATE_XXX)</code> instead, where <code>totalcross.sys.Settings.DATE_XXX</code> is a date format 
 * parameter that must be one of the <code>totalcross.sys.Settings.DATE_XXX</code> constants.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->obj[1] The value of the parameter.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lPS_setDate_id(NMParams p) 
{
	TRACE("lPS_setDate_id")
 
   MEMORY_TEST_START
   
   if (testPSClosed(p))
   {
      TCObject stmt = p->obj[0];
      SQLSelectStatement* statement = (SQLSelectStatement*)getPreparedStatementStatement(stmt);
      
      if (statement) // Only sets the parameter if the statement is not null.
      {
         Context context = p->currentContext;
   	   TCObject date = p->obj[1];
         TCObject* objParams = (TCObject*)ARRAYOBJ_START(OBJ_PreparedStatementObjParams(stmt));
         JCharP stringChars = null;
         int32 index = p->i32[0];
         TCObject dateBufObj = objParams[index];

         // juliana@238_1: corrected the end quote not appearing in the log files after dates. 
         if (date)
         {
            if (!dateBufObj || String_charsLen(dateBufObj) < 10)
            {
               if (!(dateBufObj = TC_createStringObjectWithLen(context, 10)))
		            goto finish;
               TC_setObjectLock(dateBufObj, UNLOCKED);
               objParams[index] = dateBufObj; // juliana@222_8: stores the object so that it won't be collected.
            }
            else
               xmemzero(String_charsStart(dateBufObj), String_charsLen(dateBufObj) << 1);
		      date2JCharP(FIELD_I32(date, 2), FIELD_I32(date, 1), FIELD_I32(date, 0), stringChars = String_charsStart(dateBufObj)); 
         }
           
         psSetStringParamValue(p->currentContext, stmt, dateBufObj, index, 10); // Sets the string parameter.
      }
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * This method sets the specified parameter from the given Java <code>DateTime</code> value formated as "YYYY/MM/DD HH:MM:SS:ZZZ". <br>
 * <b>IMPORTANT</b>: The constructor <code>new Date(string_date)</code> must be used with care. Some devices can construct different dates, according 
 * to the device's date format. For example, the constructor <code>new Date("12/09/2006")</code>, depending on the device's date format, can generate 
 * a date like "12 of September of 2006" or "09 of December of 2006". To avoid this, use the constructor 
 * <code>new Date(string_date, totalcross.sys.Settings.DATE_XXX)</code> instead, where <code>totalcross.sys.Settings.DATE_XXX</code> is a date format 
 * parameter that must be one of the <code>totalcross.sys.Settings.DATE_XXX</code> constants.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->obj[1] The value of the parameter.
 */
LB_API void lPS_setDateTime_id(NMParams p) // litebase/PreparedStatement public native void setDate(int index, totalcross.Util.Date);
{
	TRACE("lPS_setDateTime_id")
   lPS_setDate_id(p);
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// litebase/PreparedStatement public native void setDateTime(int index, totalcross.sys.Time) throws OutOfMemoryError;
/**
 * Formats the <code>Time</code> t into a string "YYYY/MM/DD HH:MM:SS:ZZZ"
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->obj[1] The value of the parameter.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lPS_setDateTime_it(NMParams p) 
{
	TRACE("lPS_setDateTime_it")
	
   MEMORY_TEST_START
   
   if (testPSClosed(p))
   {
      TCObject stmt = p->obj[0];
      SQLSelectStatement* statement = (SQLSelectStatement*)getPreparedStatementStatement(stmt);
      
      if (statement) // Only sets the parameter if the statement is not null.
      {
         Context context = p->currentContext;
   	   TCObject time = p->obj[1];
         TCObject* objParams = (TCObject*)ARRAYOBJ_START(OBJ_PreparedStatementObjParams(stmt));
         JCharP stringChars = null;
         int32 index = p->i32[0];
         TCObject dateTimeBufObj = objParams[index];      

         // juliana@238_1: corrected the end quote not appearing in the log files after dates. 
         if (time)
         {
            if (!dateTimeBufObj || String_charsLen(dateTimeBufObj) < 23)
            {
               if (!(dateTimeBufObj = TC_createStringObjectWithLen(context, 23)))
		            goto finish;
               TC_setObjectLock(dateTimeBufObj, UNLOCKED);
               objParams[index] = dateTimeBufObj; // juliana@222_8: stores the object so that it won't be collected.
            }
            else
               xmemzero(String_charsStart(dateTimeBufObj), String_charsLen(dateTimeBufObj) << 1);
		      dateTime2JCharP(Time_year(time), Time_month(time), Time_day(time), Time_hour(time), Time_minute(time), Time_second(time), 		                                                                         Time_millis(time), stringChars = String_charsStart(dateTimeBufObj));
         }
           
         psSetStringParamValue(p->currentContext, stmt, dateTimeBufObj, index, 23); // Sets the string parameter.
      }
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
// juliana@223_3: PreparedStatement.setNull() now works for blobs.
/**
 * Sets null in a given field. This can be used to set any column type as null. It must be just remembered that a parameter in a where clause can't 
 * be set to null.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set as null, starting from 0.
 * @throws SQLParseException If the parameter to be set as null is in the where clause.
 */
LB_API void lPS_setNull_i(NMParams p) // litebase/PreparedStatement public native void setNull(int index) throws SQLParseException;
{
	TRACE("lPS_setNull_i")
 
   if (testPSClosed(p))
   {
      TCObject stmt = p->obj[0];
      SQLSelectStatement* statement = (SQLSelectStatement*)getPreparedStatementStatement(stmt);
      
      if (statement) // Only sets the parameter if the statement is not null.
      {
         int32 index = p->i32[0];

         switch (statement->type)
         {
            case CMD_INSERT:
               if (!setNullIns(p->currentContext, (SQLInsertStatement*)statement, index))
                  goto finish;
               break;
            case CMD_DELETE:
            case CMD_SELECT:
               TC_throwExceptionNamed(p->currentContext, "litebase.SQLParseException", getMessage(ERR_PARAM_NULL)); 
               goto finish;
               break;
            case CMD_UPDATE:
               if (!setNullUpd(p->currentContext, (SQLUpdateStatement*)statement, index))
                  goto finish;
         }
         if (OBJ_PreparedStatementStoredParams(stmt))
            TC_CharP2JCharPBuf("null", 4, getPreparedStatementParamsAsStrs(stmt)[index], true);
      }
   }

finish: ;
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
// DriverException.
/**
 * This method clears all of the input parameters that have been set on this statement.
 * 
 * @param p->obj[0] The prepared statement.
 */
LB_API void lPS_clearParameters(NMParams p) // litebase/PreparedStatement public native void clearParamValues();
{
	TRACE("lPS_clearParameters")
 
   MEMORY_TEST_START
   
   if (testPSClosed(p))
   {
      TCObject stmt = p->obj[0];
      SQLSelectStatement* statement = (SQLSelectStatement*)getPreparedStatementStatement(stmt);
      
      if (statement) // Only clears the parameter if the statement is not null.
      {
         int32 length = OBJ_PreparedStatementStoredParams(stmt);

         if (length)
         {
            JCharP* paramsAsStrs = getPreparedStatementParamsAsStrs(stmt);
            
            while (--length >= 0)
               TC_CharP2JCharPBuf("unfilled", 8, paramsAsStrs[length], true);
         }

         switch (statement->type)
         {
            case CMD_DELETE:
               clearParamValuesDel((SQLDeleteStatement*)statement);
               break;
            case CMD_INSERT:
               clearParamValuesIns((SQLInsertStatement*)statement);
               break;
            case CMD_SELECT:
               clearParamValuesSel(statement);
               break;
            case CMD_UPDATE:
               clearParamValuesUpd((SQLUpdateStatement*)statement);
         }
      }
   }
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
/**
 * Returns the sql used in this statement. If logging is disabled, returns the sql without the arguments. If logging is enabled, returns the real 
 * sql, filled with the arguments.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->obj[0] receives the sql used in this statement.
 */
LB_API void lPS_toString(NMParams p) // litebase/PreparedStatement public native String toString();
{
	TRACE("lPS_toString")

   MEMORY_TEST_START
   
   if (testPSClosed(p))
   {
      TCObject statement = p->obj[0];

	   if (OBJ_PreparedStatementStoredParams(statement)) // There are no parameters o the logger is not being used.
      {
         TCObject string;
         int16* paramsPos = getPreparedStatementParamsPos(statement);
		   JCharP sql = String_charsStart(OBJ_PreparedStatementSqlExpression(statement)),
		          charsStart;
         JCharP* paramsAsStrs = getPreparedStatementParamsAsStrs(statement);

         // juliana@202_16: Now prepared statement logging is equal in all platfotms.
         int32 debugLen = 6 + paramsPos[0],
               storedParams = OBJ_PreparedStatementStoredParams(statement),
               i = -1,
               length;

		   // juliana@202_15: Corrected a bug that would cause a gpf or a reset when logging a prepared statement with a null value.
         while (++i < storedParams)
			   debugLen += TC_JCharPLen(paramsAsStrs[i]) + paramsPos[i + 1] - paramsPos[i] - 1;

         // juliana@230_30: reduced log files size.
         if (!(p->retO = string = TC_createStringObjectWithLen(p->currentContext, debugLen)))
            goto finish;
         TC_setObjectLock(p->retO, UNLOCKED);
         
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
      }
      else
         p->retO = OBJ_PreparedStatementSqlExpression(statement);
   }
   
finish: ;
   MEMORY_TEST_END
}

// juliana@230_19: removed some possible memory problems with prepared statements and ResultSet.getStrings().

//////////////////////////////////////////////////////////////////////////
// juliana@253_20: added PreparedStatement.close().
/**
 * Closes a prepared statement.
 * 
 * @param p->obj[0] The prepared statement.
 */
LB_API void lPS_close(NMParams p) // litebase/PreparedStatement public native void close();
{
   TRACE("lPS_close")
   MEMORY_TEST_START
   if (testPSClosed(p))
   {
      TCObject statement = p->obj[0];
      Hashtable* htPS = getLitebaseHtPS(OBJ_PreparedStatementDriver(statement));
      TCObject sqlExpression = OBJ_PreparedStatementSqlExpression(statement);
      int32 hashCode = TC_JCharPHashCode(String_charsStart(sqlExpression), String_charsLen(sqlExpression));
      TC_htRemove(htPS, hashCode);
      freePreparedStatement(0, statement);
   }
   MEMORY_TEST_END
}

//////////////////////////////////////////////////////////////////////////
// juliana@253_21: added PreparedStatement.isValid().
/**
 * Indicates if a prepared statement is valid or not: the driver is open and its SQL is in the hash table.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->retI receives <code>true</code> if the prepared statement is valid; <code>false</code>, otherwise.
 */
LB_API void lPS_isValid(NMParams p) // litebase/PreparedStatement public native boolean isValid();
{
   TRACE("lPS_isValid")
   TCObject statement = p->obj[0];
   
   MEMORY_TEST_START
   
   // Tests if the prepared statement isclosed or The connection with Litebase is closed.
   if (OBJ_PreparedStatementDontFinalize(statement) || OBJ_LitebaseDontFinalize(OBJ_PreparedStatementDriver(statement))) // The connection with Litebase can't be closed.
      p->retI = false;
   else
      p->retI = true;
   
   MEMORY_TEST_END
}
