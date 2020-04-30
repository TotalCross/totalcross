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
 * Declares Litebase native methods. 
 */

#ifndef LITEBASE_NATIVEMETHODS_H
#define LITEBASE_NATIVEMETHODS_H

#include "Litebase.h"

/**
 * Moves to the next record and fills the data members.
 *
 * @param p->obj[0] The row iterator. 
 * @param p->retI Receives <code>true</code> if it is possible to iterate to the next record. Otherwise, it will return <code>false</code>.
 */
LB_API void lRI_next(NMParams p);

/**
 * Moves to the next record with an attribute different of SYNCED.
 *
 * @param p->obj[0] The row iterator. 
 * @param p->retI Receives <code>true</code> if it is possible to iterate to a next record not synced. Otherwise, it will return <code>false</code>.
 */
LB_API void lRI_nextNotSynced(NMParams p);

/**
 * If the attribute is currently NEW or UPDATED, this method sets them to SYNCED. Note that if the row is DELETED, the change will be ignored.
 *
 * @param p->obj[0] The row iterator. 
 */
LB_API void lRI_setSynced(NMParams p);

/**
 * Forces the attribute to be NEW. This method will be useful if a row was marked as synchronized but was not sent to server for some problem.
 * If the row is marked as DELETED, its attribute won't be changed.
 *
 * @param p->obj[0] The row iterator. 
 */
LB_API void lRI_setNotSynced(NMParams p); // juliana@270_29: added RowIterator.setNotSynced().

/**
 * Closes this iterator.
 *
 */
LB_API void lRI_close(NMParams p);

/**
 * Resets the counter to zero so it is possible to restart to fetch records.
 *
 * @param p->obj[0] The row iterator.
 */
LB_API void lRI_reset(NMParams p);

/**
 * Returns a short contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The short column index, starting from 1.
 * @param p->retI Receives the value of the column or 0 if the column is <code>null</code>.
 */
LB_API void lRI_getShort_i(NMParams p);

/**
 * Returns an integer contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The integer column index, starting from 1.
 * @param p->retI Receives the value of the column or 0 if the column is <code>null</code>.
 */
LB_API void lRI_getInt_i(NMParams p);

/**
 * Returns a long integer contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The long integer column index, starting from 1.
 * @param p->retL Receives the value of the column or 0 if the column is <code>null</code>.
 */
LB_API void lRI_getLong_i(NMParams p);

/**
 * Returns a floating point number contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The floating point number column index, starting from 1.
 * @param p->retD Receives the value of the column or 0 if the column is <code>null</code>.
 */
LB_API void lRI_getFloat_i(NMParams p);

/**
 * Returns a double precision floating point number contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The double precision floating point number column index, starting from 1.
 * @param p->retD Receives the value of the column or 0 if the column is <code>null</code>.
 */
LB_API void lRI_getDouble_i(NMParams p);

/**
 * Returns a string contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The string column index, starting from 1.
 * @param p->retO Receives the value of the column or <code>null</code> if the column is <code>null</code>.
 */
LB_API void lRI_getString_i(NMParams p);

/**
 * Returns a blob contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The blob column index, starting from 1.
 * @param p->retO Receives the value of the column or <code>null</code> if the column is <code>null</code>.
 */
LB_API void lRI_getBlob_i(NMParams p);

/**
 * Returns a date contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The date column index, starting from 1.
 * @param p->retO Receives the value of the column or <code>null</code> if the column is <code>null</code>.
 */
LB_API void lRI_getDate_i(NMParams p);

/**
 * Returns a datetime contained in the current row.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The datetime column index, starting from 1.
 * @param p->retO Receives the value of the column or <code>null</code> if the column is <code>null</code>.
 */
LB_API void lRI_getDateTime_i(NMParams p);

/**
 * Indicates if this column has a <code>NULL</code>.
 *
 * @param p->i32[0] The column index, starting from 1.
 * @param p->retI Receives <code>true</code> if the value is SQL <code>NULL</code>; <code>false</code>, otherwise.
 * @throws IllegalArgumentException If the column index is invalid.
 */
LB_API void lRI_isNull_i(NMParams p);

/**
 * Creates a Litebase connection for the default creator id, storing the database as a flat file. This method avoids the creation of more than one 
 * instance with the same creator id, which would lead to performance and memory problems. Using this method, the strings are stored in the 
 * unicode format. 
 *
 * @param p->retO Receives a Litebase instance.
 */
LB_API void lLC_privateGetInstance(NMParams p);

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
LB_API void lLC_privateGetInstance_s(NMParams p);

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
LB_API void lLC_privateGetInstance_ss(NMParams p);

/**
 * Returns the path where the tables created/opened by this connection are stored.
 *
 * @param p->obj[0] The connection with Litebase.
 * @param p->retO Receives a string representing the path.
 * @throws IllegalStateException If the driver is closed.
 */
LB_API void lLC_getSourcePath(NMParams p);

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
LB_API void lLC_execute_s(NMParams p);

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
LB_API void lLC_executeUpdate_s(NMParams p);

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
LB_API void lLC_executeQuery_s(NMParams p);

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
LB_API void lLC_prepareStatement_s(NMParams p);

/**
 * Returns the current rowid for a given table.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->retI Receives the current rowid for the table.
 */
LB_API void lLC_getCurrentRowId_s(NMParams p);

/**
 * Returns the number of valid rows in a table. This may be different from the number of records if a row has been deleted.
 * 
 * @see #getRowCountDeleted(String)
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->retI Receives the number of valid rows in a table.
 */
LB_API void lLC_getRowCount_s(NMParams p);

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
LB_API void lLC_setRowInc_si(NMParams p);

/**
 * Indicates if the given table already exists. This method can be used before a drop table.
 *
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->retI Receives <code>true</code> if a table exists; <code>false</code> othewise.
 * @throws DriverException If tableName or path is too big.
 */
LB_API void lLC_exists_s(NMParams p);

/**
 * Releases the file handles (on the device) of a Litebase instance. Note that, after this is called, all <code>Resultset</code>s and 
 * <code>PreparedStatement</code>s created with this Litebase instance will be in an inconsistent state, and using them will probably reset the 
 * device. This method also deletes the active instance for this creator id from Litebase's internal table.
 *
 * @param p->obj[0] The connection with Litebase.
 * @throws IllegalStateException If the driver is closed.
 */
LB_API void lLC_closeAll(NMParams p);

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
LB_API void lLC_purge_s(NMParams p);

/**
 * Returns the number of deleted rows.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->retI Receives the total number of deleted records of the given table.
 */
LB_API void lLC_getRowCountDeleted_s(NMParams p);

/**
 * Gets an iterator for a table. With it, it is possible iterate through all the rows of a table in sequence and get its attributes. This is good for
 * synchronizing a table. While the iterator is active, it is not possible to do any queries or updates because this can cause dada corruption.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of a table.
 * @param p->retO receives a iterator for the given table.
 */
LB_API void lLC_getRowIterator_s(NMParams p);

/**
 * Gets the Litebase logger. The fields should be used unless using the logger within threads. 
 * 
 * @param p->retO receives the logger.
 */
LB_API void lLC_privateGetLogger(NMParams p);

/**
 * Sets the litebase logger. This enables log messages for all queries and statements of Litebase and can be very useful to help finding bugs in 
 * the system. Logs take up memory space, so turn them on only when necessary. The fields should be used unless using the logger within threads.
 * 
 * @param p->obj[0] The logger.
 */
LB_API void lLC_privateSetLogger_l(NMParams p);

/**                                                                                                                                                                                                                                                      
 * Gets the default Litebase logger. When this method is called for the first time, a new text file is created. In the subsequent calls, the same 
 * file is used.                                                                                                                                  
 *                                                                                                                                                
 * @param p->retO receives the default logger. 
 * @throws DriverException if an <code>IOException</code> occurs.                                                                                                   
 */   
LB_API void lLC_privateGetDefaultLogger(NMParams p);

/**
 * Deletes all the log files with the default format found in the default device folder. If log is enabled, the current log file is not affected by 
 * this command.
 * 
 * @param p->retI receives the number of files deleted.
 */
LB_API void lLC_privateDeleteLogFiles(NMParams p);

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
LB_API void lLC_privateProcessLogs_Ssb(NMParams p);

/**
 * Tries to recover a table not closed properly by marking and erasing logically the records whose crc are not valid.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The name of the table to be converted.
 * @param p->retI Receives the number of purged records.
 * @throws DriverException If the table name or path is too big.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lLC_recoverTable_s(NMParams p);

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
LB_API void lLC_convert_s(NMParams p);

/**
 * Used to returned the slot where the tables were stored on Palm OS. Not used anymore.
 * 
 * @param p->retI receives -1.
 */
LB_API void lLC_getSlot(NMParams p); // juliana@223_1: added a method to get the current slot being used.

// juliana@226_6: added LitebaseConnection.isOpen(), which indicates if a table is open in the current connection.
/**
 * Indicates if a table is open or not.
 * 
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The table name to be checked.
 * @param p->retI receives <code>true</code> if the table is open in the current connection; <code>false</code>, otherwise.
 * @throws DriverException If the table name is too big.
 */
LB_API void lLC_isOpen_s(NMParams p);

/**
 * Drops all the tables from a database represented by its application id and path.
 * 
 * @param p->obj[0] The application id of the database.
 * @param p->obj[1] The path where the files are stored.
 * @throws DriverException If the database is not found or a file error occurs.
 * @throws NullPointerException If one of the string parameters is null.
 */
LB_API void lLC_dropDatabase_ssi(NMParams p);

/**
 * Indicates if a table is closed properly or not.
 * 
 * @param p->obj[1] The table to be verified.
 * @param p->retI receives <code>true</code> if the table is closed properly or is open (a not properly closed table can't be opened); 
 * <code>false</code>, otherwise.
 * @throws DriverException If the table is corrupted.
 * @throws NullPointerException If tableName is null.
 */
LB_API void lLC_isTableProperlyClosed_s(NMParams p);

/**
 * Lists all table names of the current connection.
 * 
 * @param p->retO receives an array of all the table names of the current connection. If the current connection has no tables, an empty list is 
 * returned.
 * @throws DriverException If a file error occurs. 
 * @throws IllegalStateException If the driver is closed.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lLC_listAllTables(NMParams p);

/**
 * Encrypts all the tables of a connection given from the application id. All the files of the tables must be closed!
 * 
 * @param p->obj[0] The application id of the database.
 * @param p->obj[1] The path where the files are stored.
 */
LB_API void lLC_encryptTables_ssi(NMParams p);

/**
 * Decrypts all the tables of a connection given from the application id. All the files of the tables must be closed!
 * 
 * @param p->obj[0] The application id of the database.
 * @param p->obj[1] The path where the files are stored.
 */
LB_API void lLC_decryptTables_ssi(NMParams p);

/**
 * Returns the metadata for this result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retO receives the metadata for this result set.
 */
LB_API void lRS_getResultSetMetaData(NMParams p);

/**
 * Releases all memory allocated for this object. Its a good idea to call this when you no longer needs it, but it is also called by the GC when the 
 * object is no longer in use.
 *
 * @param p->obj[0] The result set.
 * @throws IllegalStateException If the result set is closed.
 */
LB_API void lRS_close(NMParams p);

/**
 * Places the cursor before the first record.
 *
 * @param p->obj[0] The result set.
 */
LB_API void lRS_beforeFirst(NMParams p);

/**
 * Places the cursor after the last record.
 *
 * @param p->obj[0] The result set.
 */
LB_API void lRS_afterLast(NMParams p);

/**
 * Places the cursor in the first record of the result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retI Receives <code>true</code> if it was possible to place the cursor in the first record; <code>false</code>, otherwise.
 */
LB_API void lRS_first(NMParams p);

/**
 * Places the cursor in the last record of the result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retI Receives <code>true</code> if it was possible to place the cursor in the last record; <code>false</code>, otherwise.
 */
LB_API void lRS_last(NMParams p);

/**
 * Gets the next record of the result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retI Receives <code>true</code> if there is a next record to go to in the result set; <code>false</code>, otherwise.
 */
LB_API void lRS_next(NMParams p);

/**
 * Returns the previous record of the result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retI Receives <code>true</code> if there is a previous record to go to in the result set; <code>false</code>, otherwise.
 */
LB_API void lRS_prev(NMParams p);

/**
 * Given the column index (starting from 1), returns a short value that is represented by this column. Note that it is only possible to request this 
 * column as short if it was created with this precision or if the data being fetched is the result of a DATE or DATETIME SQL function.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retI receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getShort_i(NMParams p);

/**
 * Given the column name (case insensitive), returns a short value that is represented by this column. Note that it is only possible to request this 
 * column as short if it was created with this precision or if the data being fetched is the result of a DATE or DATETIME SQL function. This method 
 * is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retI receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getShort_s(NMParams p);

/**
 * Given the column index (starting from 1), returns an integer value that is represented by this column. Note that it is only possible to request this 
 * column as integer if it was created with this precision.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retI receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getInt_i(NMParams p);

/**
 * Given the column name (case insensitive), returns an integer value that is represented by this column. Note that it is only possible to request this 
 * column as integer if it was created with this precision. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retI receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getInt_s(NMParams p);

/**
 * Given the column index (starting from 1), returns a long value that is represented by this column. Note that it is only possible to request this 
 * column as long if it was created with this precision.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retL receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getLong_i(NMParams p);

/**
 * Given the column name (case insensitive), returns a long value that is represented by this column. Note that it is only possible to request this 
 * column as long if it was created with this precision. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retL receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
LB_API void lRS_getLong_s(NMParams p);

/**
 * Given the column index (starting from 1), returns a float value that is represented by this column. Note that it is only possible to request this 
 * column as float if it was created with this precision.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retD receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
LB_API void lRS_getFloat_i(NMParams p);

/**
 * Given the column name (case insensitive), returns a float value that is represented by this column. Note that it is only possible to request this 
 * column as float if it was created with this precision. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retD receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
LB_API void lRS_getFloat_s(NMParams p);

/**
 * Given the column index (starting from 1), returns a double value that is represented by this column. Note that it is only possible to request this 
 * column as double if it was created with this precision.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retD receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
LB_API void lRS_getDouble_i(NMParams p);

/**
 * Given the column name (case insensitive), returns a double value that is represented by this column. Note that it is only possible to request this 
 * column as double if it was created with this precision. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retD receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
LB_API void lRS_getDouble_s(NMParams p);

/**
 * Given the column index (starting from 1), returns a char array that is represented by this column. Note that it is only possible to request this 
 * column as a char array if it was created as a string.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getChars_i(NMParams p);

/**
 * Given the column name (case insensitive), returns a char array that is represented by this column. Note that it is only possible to request this 
 * column as a char array if it was created as a string. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getChars_s(NMParams p);

/**
 * Given the column index (starting from 1), returns a string that is represented by this column. Any column type can be returned as a string. 
 * <code>Double</code>/<code>float</code> values formatting will use the precision set with the <code>setDecimalPlaces()</code> method.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>
 */
LB_API void lRS_getString_i(NMParams p);

/**
 * Given the column name (case insensitive), returns a string that is represented by this column. Any column type can be returned as a string. 
 * <code>Double</code>/<code>float</code> values formatting will use the precision set with the <code>setDecimalPlaces()</code> method. This 
 * method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column index.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>
 */
LB_API void lRS_getString_s(NMParams p);

/**
 * Given the column index (starting from 1), returns a byte (blob) array that is represented by this column. Note that it is only possible to request 
 * this column as a blob if it was created this way.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getBlob_i(NMParams p);

/**
 * Given the column name (case insensitive), returns a byte array (blob) that is represented by this column. Note that it is only possible to request 
 * this column as a blob if it was created this way. This method is slightly slower then the method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getBlob_s(NMParams p);

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
LB_API void lRS_getStrings_i(NMParams p);

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
LB_API void lRS_getStrings(NMParams p);

/**
 * Given the column index (starting from 1), returns a <code>Date</code> value that is represented by this column. Note that it is only possible 
 * to request this column as a date if it was created this way (DATE or DATETIME).
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getDate_i(NMParams p);

/**
 * Given the column name (case insensitive), returns a <code>Date</code> value that is represented by this column. Note that it is only possible 
 * to request this column as a date if it was created this way (DATE or DATETIME). This method is slightly slower then the method that accepts a 
 * column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retO receives the column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getDate_s(NMParams p);

/**
 * Given the column index (starting from 1), returns a <code>Time</code> (correspondent to a DATETIME data type) value that is represented by this 
 * column. Note that it is only possible to request this column as a date if it was created this way.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The colum index.
 * @param p->retO receives the time of the DATETIME. If the DATETIME value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getDateTime_i(NMParams p);

/**
 * Given the column name (case insensitive), returns a <code>Time</code> (correspondent to a DATETIME data type) value that is represented by this
 * column. Note that it is only possible to request this column as a date if it was created this way. This method is slightly slower then the 
 * method that accepts a column index.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[0] The colum name.
 * @param p->retO receives the time of the DATETIME. If the DATETIME value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
LB_API void lRS_getDateTime_s(NMParams p);

/**
 * Places this result set cursor at the given absolute row. This is the absolute physical row of the table. This method is usually used to restore
 * the row at a previous row got with the <code>getRow()</code> method.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The row to set the cursor.
 * @param p->retI receives <code>true</code> whenever this method does not throw an exception.
 */
LB_API void lRS_absolute_i(NMParams p);

/**
 * Moves the cursor <code>rows</code> in distance. The value can be greater or lower than zero.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The distance to move the cursor.
 * @param p->retI receives <code>true</code> whenever this method does not throw an exception.
 */
LB_API void lRS_relative_i(NMParams p);

/**
 * Returns the current physical row of the table where the cursor is. It must be used with <code>absolute()</code> method.
 *
 * @param p->obj[0] The result set.
 * @param p->retI receives the current physical row of the table where the cursor is.
 */
LB_API void lRS_getRow(NMParams p);

/**
 * Sets the number of decimal places that the given column (starting from 1) will have when being converted to <code>String</code>.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column.
 * @param p->i32[1] The number of decimal places.
 * @throws DriverException If the column index is invalid, or the value for decimal places is invalid.
 */
LB_API void lRS_setDecimalPlaces_ii(NMParams p);

/**
 * Returns the number of rows of the result set.
 *
 * @param p->obj[0] The result set.
 * @param p->retI receives the number of rows.
 */
LB_API void lRS_getRowCount(NMParams p);

/**
 * Given the column index (starting from 1), indicates if this column has a <code>NULL</code>.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retI receives <code>true</code> if the value is SQL <code>NULL</code>; <code>false</code>, otherwise.
 */
LB_API void lRS_isNull_i(NMParams p);

/**
 * Transforms a <code>ResultSet</code> row in a string.
 *
 * @param p->obj[0] The result set.
 * @param p->retO receives a whole current row of a <code>ResultSet</code> in a string with column data separated by tab. 
 */
LB_API void lRS_rowToString(NMParams p); // juliana@270_30: added ResultSet.rowToString().

/**
 * Given the column name (case insensitive), indicates if this column has a <code>NULL</code>.
 *
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param p->retI receives <code>true</code> if the value is SQL <code>NULL</code>; <code>false</code>, otherwise.
 * @throws NullPointerException If the column name is null.
 */
LB_API void lRS_isNull_s(NMParams p);

/**
 * Gets the number of columns for this <code>ResultSet</code>.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->retI receives the number of columns for this <code>ResultSet</code>.
 */
LB_API void lRSMD_getColumnCount(NMParams p);

/**
 * Given the column index (starting at 1), returns the display size. For chars, it will return the number of chars defined; for primitive types, it 
 * will return the number of decimal places it needs to be displayed correctly. Returns 0 if an error occurs.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->i32[0] The column index (starting at 1).
 * @param p->retI receives the display size or -1 if a problem occurs.
 * @throws IllegalArgumentException If the column index is invalid.
 */
LB_API void lRSMD_getColumnDisplaySize_i(NMParams p);

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
LB_API void lRSMD_getColumnLabel_i(NMParams p);

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
LB_API void lRSMD_getColumnType_i(NMParams p);

/**
 * Given the column index (starting at 1), returns the name of the column type.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->i32[0] The column index (starting at 1).
 * @param p->retO receives the name of the column type, which can be: <b><code>chars</b></code>, <b><code>short</b></code>, <b><code>int</b></code>, 
 * <b><code>long</b></code>, <b><code>float</b></code>, <b><code>double</b></code>, <b><code>date</b></code>, <b><code>datetime</b></code>, 
 * <b><code>blob</b></code>, or null if an error occurs.
 */
LB_API void lRSMD_getColumnTypeName_i(NMParams p);

/**
 * Given the column index, (starting at 1) returns the name of the table it came from.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->i32[0] The column index.
 * @param p->retO receives the name of the table it came from or <code>null</code> if the column index does not exist.
 * @throws IllegalArgumentException If the column index is invalid.
 */
LB_API void lRSMD_getColumnTableName_i(NMParams p);

/**
 * Given the column name or alias, returns the name of the table it came from.
 *
 * @param p->obj[0] The result set meta data.
 * @param p->obj[1] The column name.
 * @param p->retO receives the name of the table it came from or <code>null</code> if the column name does not exist.
 * @throws DriverException If the column was not found.
 * @throws NullPointerException if the column name is null.
 */
LB_API void lRSMD_getColumnTableName_s(NMParams p);

/**
 * Indicates if a column of the result set has default value.
 * 
 * @param p->i32[0] The column index.
 * @param p->retI receives <code>true</code> if the column has a default value; <code>false</code>, otherwise. 
 * @throws DriverException If the column does not have an underlining table.
 */
LB_API void lRSMD_hasDefaultValue_i(NMParams p);

/**
 * Indicates if a column of the result set has default value.
 * 
 * @param p->obj[1] The column name.
 * @param p->retI receives <code>true</code> if the column has a default value; <code>false</code>, otherwise. 
 * @throws DriverException If the column was not found or does not have an underlining table.
 * @throws NullPointerException if the column name is null.
 */
LB_API void lRSMD_hasDefaultValue_s(NMParams p);

/**
 * Indicates if a column of the result set is not null.
 * 
 * @param p->i32[0] The column index.
 * @param p->retI receives <code>true</code> if the column is not null; <code>false</code>, otherwise. 
 * @throws DriverException If the column does not have an underlining table.
 */
LB_API void lRSMD_isNotNull_i(NMParams p);

/**
 * Indicates if a column of the result set is not null.
 * 
 * @param p->obj[1] The column name.
 * @param p->retI receives <code>true</code> if the column is not null; <code>false</code>, otherwise. 
 * @throws DriverException If the column was not found or does not have an underlining table.
 * @throws NullPointerException if the column name is null.
 */
LB_API void lRSMD_isNotNull_s(NMParams p);

/**
 * Returns the primary key column indices of a table.
 * 
 * @param p->obj[1] The table name.
 * @param p->retO receives <code>null</code> if the given table does not have primary key or an array with the column indices of the primary key.
 * @throws NullPointerException if the table name is null.
 */
LB_API void lRSMD_getPKColumnIndices_s(NMParams p);

/**
 * Returns the primary key column names of a table.
 * 
 * @param p->obj[1] The table name.
 * @param p->retO <code>null</code> if the given table does not have primary key or an array with the column names of the primary key.
 * @throws NullPointerException if the table name is null.
 */
LB_API void lRSMD_getPKColumnNames_s(NMParams p);

/**
 * Returns the default value of a column.
 * 
 * @param p->i32[0] The column index.
 * @return p->retO receives the default value of the column as a string or <code>null</code> if there is no default value.
 * @throws DriverException If the column index does not have an underlining table.
 */
LB_API void lRSMD_getDefaultValue_i(NMParams p);

/**
 * Returns the default value of a column.
 * 
 * @param p->obj[1] The column name.
 * @return p->retO receives the default value of the column as a string or <code>null</code> if there is no default value.
 * @throws DriverException If the column name does not have an underlining table.
 * @throws NullPointerException if the column name is null.
 */
LB_API void lRSMD_getDefaultValue_s(NMParams p);

/**
 * This method executes a prepared SQL query and returns its <code>ResultSet</code>.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->retO receives the <code>ResultSet</code> of the SQL statement.
 * @throws DriverException If the statement to be execute is not a select, there are undefined parameters or the driver is closed.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lPS_executeQuery(NMParams p);

/**
 * This method executes a SQL <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code> statement. SQL statements that return nothing such as
 * SQL DDL statements can also be executed.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->retI receives the result is either the row count for <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code> statements; or 0 
 * for SQL statements that return nothing.
 * @throws DriverException If the query does not update the table or there are undefined parameters.
 */
LB_API void lPS_executeUpdate(NMParams p);

/**
 * This method sets the specified parameter from the given Java <code>short</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->i32[1] The value of the parameter.
 */
LB_API void lPS_setShort_is(NMParams p);

/**
 * This method sets the specified parameter from the given Java <code>int</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->i32[1] The value of the parameter.   
 */
LB_API void lPS_setInt_ii(NMParams p);

/**
 * This method sets the specified parameter from the given Java <code>long</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->i64[0] The value of the parameter.
 */
LB_API void lPS_setLong_il(NMParams p);

/**
 * This method sets the specified parameter from the given Java <code>float</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->dbl[0] The value of the parameter.
 */
LB_API void lPS_setFloat_id(NMParams p);

/**
 * This method sets the specified parameter from the given Java <code>double</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->dbl[0] The value of the parameter.
 */
LB_API void lPS_setDouble_id(NMParams p);

/**
 * This method sets the specified parameter from the given Java <code>String</code> value.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->obj[1] The value of the parameter. DO NOT SURROUND IT WITH '!.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lPS_setString_is(NMParams p);

/**
 * This method sets the specified parameter from the given array of bytes as a blob.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->obj[1] The value of the parameter.
 * @throws SQLParseException If the parameter to be set is in the where clause.
 */
LB_API void lPS_setBlob_iB(NMParams p);

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
LB_API void lPS_setDate_id(NMParams p);

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
LB_API void lPS_setDateTime_id(NMParams p);

/**
 * Formats the <code>Time</code> t into a string "YYYY/MM/DD HH:MM:SS:ZZZ"
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->obj[1] The value of the parameter.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
LB_API void lPS_setDateTime_it(NMParams p);

/**
 * Sets null in a given field. This can be used to set any column type as null. It must be just remembered that a parameter in a where clause can't 
 * be set to null.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set as null, starting from 0.
 * @throws SQLParseException If the parameter to be set as null is in the where clause.
 */
LB_API void lPS_setNull_i(NMParams p);

/**
 * This method clears all of the input parameters that have been set on this statement.
 * 
 * @param p->obj[0] The prepared statement.
 */
LB_API void lPS_clearParameters(NMParams p);

/**
 * Returns the sql used in this statement. If logging is disabled, returns the sql without the arguments. If logging is enabled, returns the real 
 * sql, filled with the arguments.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->obj[0] receives the sql used in this statement.
 */
LB_API void lPS_toString(NMParams p);

/**
 * Closes a prepared statement.
 * 
 * @param p->obj[0] The prepared statement.
 */
LB_API void lPS_close(NMParams p);

// juliana@230_19: removed some possible memory problems with prepared statements and ResultSet.getStrings().

/**
 * Indicates if a prepared statement is valid or not: the driver is open and its SQL is in the hash table.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->retI receives <code>true</code> if the prepared statement is valid; <code>false</code>, otherwise.
 */
LB_API void lPS_isValid(NMParams p);

#endif
