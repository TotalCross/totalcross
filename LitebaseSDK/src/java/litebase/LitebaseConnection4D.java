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

import totalcross.sys.*;
import totalcross.util.Logger;

/**
 * This class is the native one used to issue SQL commands. Read Litebase Companion chapters for more information.
 */
public class LitebaseConnection4D
{   
   /**
    * English language.
    */
   public static final int LANGUAGE_EN = 1;

   /**
    * Portuguese language.
    */
   public static final int LANGUAGE_PT = 2;
   
   // guich@223_10: the Litebase version is not declared as final anymore, otherwise the compiler replaces the constant by the value.
   /**
    * The string corresponding to the current Litebase version.
    */
   public static String versionStr = "2.8.5";

   /**
    * The integer corresponding to the current Litebase version.
    */
   public static int version = 285;
   
   /** 
    * Current build number.
    */
   public static int buildNumber = 000;
   
   /**
    * Indicates if the tables of this connection use ascii or unicode strings.
    */
   boolean isAscii; // juliana@210_2: now Litebase supports tables with ascii strings.
   
   /**
    * Indicates if the tables of this connection use cryptography.
    */
   private boolean useCrypto; // juliana@253_8: now Litebase supports weak cryptography.
   
   /**
    * A flag that indicates that this class has already been finalized.
    */
   boolean dontFinalize;
   
   /**
    * The key which identifies one Litebase connection instance.
    */
   int key;
   
   /**
    * The creator id for the tables managed by Litebase.
    */
   int appCrid;
   
   /**
    * Given the table name, returns the Table structure.
    */
   long htTables;
   
   /**
    * The source path, where the tables will be stored.
    */
   long sourcePath;
   
   /**
    * A hash table of prepared statements.
    */
   long htPS; // juliana@226_16
   
   /**
    * An array of node indices. 
    */
   long nodes; // juliana@253_6: the maximum number of keys of a index was duplicated. 
   
   /**
    * Indicates if the native library is already attached.
    */
   private static boolean isDriverLoaded;

   /**
    * The logger.
    */
   public static Logger logger;
   
   // juliana@230_30: reduced log files size.
   /**
    * A <code>StringBuffer</code> to hold the logger string.
    */
   static StringBuffer loggerString = new StringBuffer();

   // juliana@211_1: language is now a public field. It must be accessed directly.
   /**
    * The language of the Litebase messages.
    */
   public static int language = LANGUAGE_EN;
   
   // juliana@253_18: now it is possible to log only changes during Litebase operation.
   /**
    * Indicates if only changes during Litebase operation must be logged or not.
    */
   public static boolean logOnlyChanges;
   
   // juliana@222_10: corrected a bug that would possibly not load Litebase native library on Android inside a thread. 
   static
   {
      // Attachs the native library.
      if (!isDriverLoaded && !(isDriverLoaded = totalcross.sys.Vm.attachNativeLibrary("Litebase")))
         if (language == LitebaseConnection.LANGUAGE_EN)   
            throw new DriverException("Can't find native methods implementation for LitebaseConnection. Please install Litebase.dll/prc file.");
         else
            throw new DriverException("N�o � poss�vel encontrar a implementa��o dos m�todos nativos para o LitebaseConnection. Por favor, instale o arquivo Litebase.dll/prc.");
   }

   // juliana@230_11: Litebase public class constructors are now not public any more. 
   /**
    * The constructor.
    */
   private LitebaseConnection4D() {}
   
  // juliana@201_26: created a default getInstance() which creates a new Litebase connection with the current application id.
  /**
   * Creates a Litebase connection for the default creator id, storing the database as a flat file.
   * This method avoids the creation of more than one instance with the same creator id, which would
   * lead to performance and memory problems. Using this method, the strings are stored in the
   * unicode format.
   *
   * @return A Litebase instance.
   */
  public static LitebaseConnection4D getInstance() {
    while (!isDriverLoaded) {
      Thread.yield();
    }
    return privateGetInstance();
  }
   
   /**
    * Creates a Litebase connection for the default creator id, storing the database as a flat file. This method avoids the creation of more than one 
    * instance with the same creator id, which would lead to performance and memory problems. Using this method, the strings are stored in the 
    * unicode format. 
    *
    * @return A Litebase instance.
    */
   private static native LitebaseConnection4D privateGetInstance();
   
  /**
   * Creates a Litebase connection for the given creator id, storing the database as a flat file.
   * This method avoids the creation of more than one instance with the same creator id, which would
   * lead to performance and memory problems. Using this method, the strings are stored in the
   * unicode format.
   *
   * @param appCrid The creator id, which may (or not) be the same one of the current application
   *     and MUST be 4 characters long.
   * @return A Litebase instance.
   */
  public static LitebaseConnection4D getInstance(String appCrid) {
    while (!isDriverLoaded) {
      Thread.yield();
    }
    return privateGetInstance(appCrid);
  }
   
   /**
    * Creates a Litebase connection for the given creator id, storing the database as a flat file. This method avoids the creation of more than one 
    * instance with the same creator id, which would lead to performance and memory problems. Using this method, the strings are stored in the 
    * unicode format.
    *
    * @param appCrid The creator id, which may (or not) be the same one of the current application and MUST be 4 characters long.
    * @return A Litebase instance.
    * @throws DriverException If an application id with more or less than four characters is specified.
    * @throws NullPointerException If <code>appCrid == null</code>.
    */
   private static native LitebaseConnection4D privateGetInstance(String appCrid) throws DriverException, NullPointerException;
   
  /**
   * Creates a LitebaseConnection for the given creator id and with the given connection param list.
   * This method avoids the creation of more than one instance with the same creator id and
   * parameters, which would lead to performance and memory problems.
   *
   * @param appCrid The creator id, which may be the same one of the current application and MUST be
   *     4 characters long.
   * @param params Only the folder where it is desired to store the tables, <code>null</code>, if it
   *     is desired to use the current data path, or <code>
   *     chars_type = chars_format; path = source_path[;crypto] </code>, where <code>chars_format
   *     </code> can be <code>ascii</code> or <code>unicode</code>, <code>source_path</code> is the
   *     folder where the tables will be stored, and crypto must be used if the tables of the
   *     connection use cryptography. The params can be entered in any order. If only the path is
   *     passed as a parameter, unicode is used and there is no cryptography. Notice that path must
   *     be absolute, not relative.
   *     <p>Note that databases belonging to multiple applications can be stored in the same path,
   *     since all tables are prefixed by the application's creator id.
   *     <p>Also notice that to store Litebase files on card on Pocket PC, just set the second
   *     parameter to the correct directory path.
   *     <p>It is not recommended to create the databases directly on the PDA. Memory cards are FIVE
   *     TIMES SLOWER than the main memory, so it will take a long time to create the tables. Even
   *     if the NVFS volume is used, it can be very slow. It is better to create the tables on the
   *     desktop, and copy everything to the memory card or to the NVFS volume.
   *     <p>Due to the slowness of a memory card and the NVFS volume, all queries will be stored in
   *     the main memory; only tables and indexes will be stored on the card or on the NVFS volume.
   *     <p>An exception will be raised if tables created with an ascii kind of connection are
   *     oppened with an unicode connection and vice-versa.
   * @return A Litebase instance.
   */
  public static LitebaseConnection4D getInstance(String appCrid, String params) {
    while (!isDriverLoaded) {
      Thread.yield();
    }
    return privateGetInstance(appCrid, params);
  }
   
   /**
    * Creates a LitebaseConnection for the given creator id and with the given connection param list. This method avoids the creation of more than
    * one instance with the same creator id and parameters, which would lead to performance and memory problems.
    *
    * @param appCrid The creator id, which may be the same one of the current application and MUST be 4 characters long.
    * @param params Only the folder where it is desired to store the tables, <code>null</code>, if it is desired to use the current data 
    * path, or <code>chars_type = chars_format; path = source_path[;crypto] </code>, where <code>chars_format</code> can be <code>ascii</code> or 
    * <code>unicode</code>, <code>source_path</code> is the folder where the tables will be stored, and crypto must be used if the tables of the 
    * connection use cryptography. The params can be entered in any order. If only the path is passed as a parameter, unicode is used and there is no 
    * cryptography. Notice that path must be absolute, not relative.
    * <p>Note that databases belonging to multiple applications can be stored in the same path, since all tables are prefixed by the application's 
    * creator id.
    * <p>Also notice that to store Litebase files on card on Pocket PC, just set the second parameter to the correct directory path.
    * <p>It is not recommended to create the databases directly on the PDA. Memory cards are FIVE TIMES SLOWER than the main memory, so it will take
    * a long time to create the tables. Even if the NVFS volume is used, it can be very slow. It is better to create the tables on the desktop, and 
    * copy everything to the memory card or to the NVFS volume.
    * <p>Due to the slowness of a memory card and the NVFS volume, all queries will be stored in the main memory; only tables and indexes will be 
    * stored on the card or on the NVFS volume.
    * <p> An exception will be raised if tables created with an ascii kind of connection are oppened with an unicode connection and vice-versa.
    * @return A Litebase instance.
    * @throws DriverException If an application id with more or less than four characters is specified.
    * @throws NullPointerException If <code>appCrid == null</code>. 
    */
   private static native LitebaseConnection4D privateGetInstance(String appCrid, String params) throws DriverException, NullPointerException;
  
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Returns the path where the tables created/opened by this connection are stored.
    *
    * @return A string representing the path.
    * @throws IllegalStateException If the driver is closed.
    */
   public native String getSourcePath() throws IllegalStateException;

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
    * @param sql The SQL creation command.
    */
   public native void execute(String sql);

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Used to execute updates in a table (insert, delete, update, alter table, drop). E.g.:
    *
    * <p><code>driver.executeUpdate(&quot;drop table person&quot;);</code> will drop also the indices.
    * <p><code>driver.executeUpdate(&quot;drop index * on person&quot;);</code> will drop all indices but not the primary key index.
    * <p><code>driver.executeUpdate(&quot;drop index name on person&quot;);</code> will drop the index for the &quot;name&quot; column.
    * <p><code> driver.executeUpdate(&quot;ALTER TABLE person DROP primary key&quot;);</code> will drop the primary key.
    * <p><code>driver.executeUpdate(&quot;update person set age=44, salary=3200.5 where name = 'guilherme campos hazan'&quot;);</code> 
    * will update the table.
    * <p><code>driver.executeUpdate(&quot;delete person where name like 'g%'&quot;);</code> will delete records of the table.
    * <p><code> driver.executeUpdate(&quot;insert into person (age, salary, name, email)
    * values (32, 2000, 'guilherme campos hazan', 'guich@superwaba.com.br')&quot;);</code> will insert a record in the table.
    *
    * @param sql The SQL update command.
    * @return The number of rows affected or <code>0</code> if a drop or alter operation was successful.
    */
   public native int executeUpdate(String sql);

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
    * @param sql The SQL query command.
    * @return A result set with the values returned from the query.
    */
   public native ResultSet executeQuery(String sql);

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Creates a pre-compiled statement with the given sql. Prepared statements are faster for repeated queries. Instead of parsing the same query 
    * where only a few arguments change, it is better to create a prepared statement and the query is pre-parsed. Then, it is just needed to set the 
    * arguments (defined as ? in the sql) and run the sql.
    * 
    * @param sql The SQL query command.
    * @return A pre-compiled SQL statement.
    * @throws OutOfMemoryError If there is not enough memory to create the preparedStatement.
    */
   public native PreparedStatement4D prepareStatement(String sql) throws OutOfMemoryError;
   
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Returns the current rowid for a given table.
    * 
    * @param tableName The name of a table.
    * @return The current rowid for the table.
    */
   public native int getCurrentRowId(String tableName);
  
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@201_31: LitebaseConnection.getRowCount() will now throw an exception if tableName is null or invalid instead of returning -1.
   /**
    * Returns the number of valid rows in a table. This may be different from the number of records if a row has been deleted.
    * 
    * @see #getRowCountDeleted(String)
    * @param tableName The name of a table.
    * @return The number of valid rows in a table.
    */
   public native int getRowCount(String tableName);

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Sets the row increment used when creating or updating big amounts of data. Using this method greatly increases the
    * speed of bulk insertions (about 3x faster). To use it, it is necessary to call it (preferable) with the amount of
    * lines that will be inserted. After the insertion is finished, it is <b>NECESSARY</b> to call it again, passing
    * <code>-1</code> as the increment argument. Without doing this last step, data may be lost because some writes will
    * be delayed until the method is called with -1. Another good optimization on bulk insertions is to drop the indexes
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
    * @param tableName The associated table name.
    * @param inc The increment value.
    * @throws IllegalArgumentException If the increment is equal to 0 or less than -1.
    */
   public native void setRowInc(String tableName, int inc) throws IllegalArgumentException;

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Indicates if the given table already exists. This method can be used before a drop table.
    *
    * @param tableName The name of a table.
    * @return <code>true</code> if a table exists; <code>false</code> othewise.
    * @throws DriverException If tableName or path is too big.
    */
   public native boolean exists(String tableName) throws DriverException;

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Releases the file handles (on the device) of a Litebase instance. Note that, after this is called, all <code>Resultset</code>s and 
    * <code>PreparedStatement</code>s created with this Litebase instance will be in an inconsistent state, and using them will probably reset the 
    * device. This method also deletes the active instance for this creator id from Litebase's internal table.
    *
    * @throws IllegalStateException If the driver is closed.
    */
   public native void closeAll() throws IllegalStateException; // guich@109

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Used to delete physically the records of the given table. Records are always deleted logically, to avoid the need of recreating the indexes.
    * When a new record is added, it doesn't uses the position of the previously deleted one. This can make the table big, if a table is created, 
    * filled and has a couple of records deleted. This method will remove all deleted records and recreate the indexes accordingly. Note that it 
    * can take some time to run.
    * <p>
    * Important: the rowid of the records is NOT changed with this operation.
    * 
    * @param tableName The table name to purge.
    * @return The number of purged records.
    * @throws DriverException If a row can't be read or written.
    * @throws OutOfMemoryError If there is not enough memory to purge the table.
    */
   public native int purge(String tableName) throws DriverException, OutOfMemoryError;

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Returns the number of deleted rows.
    * 
    * @param tableName The name of a table.
    * @return The total number of deleted records of the given table.
    */
   public native int getRowCountDeleted(String tableName);

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Gets an iterator for a table. With it, it is possible iterate through all the rows of a table in sequence and get
    * its attributes. This is good for synchronizing a table. While the iterator is active, it is not possible to do any
    * queries or updates because this can cause dada corruption.
    * 
    * @param tableName The name of a table.
    * @return A iterator for the given table.
    */
   public native RowIterator4D getRowIterator(String tableName);

  // juliana@210_3: LitebaseConnection.getLogger() and LitebaseConnection.setLogger() are no longer deprecated.
  /**
   * Gets the Litebase logger. The fields should be used unless using the logger within threads.
   *
   * @return The logger.
   */
  public static Logger getLogger() {
    while (!isDriverLoaded) {
      Thread.yield();
    }
    return privateGetLogger();
  }
   
   /**
    * Gets the Litebase logger. The fields should be used unless using the logger within threads. 
    * 
    * @return The logger.
    * @throws DriverException if an <code>IOException</code> occurs.
    */
   private static native Logger privateGetLogger() throws DriverException;

  /**
   * Sets the litebase logger. This enables log messages for all queries and statements of Litebase
   * and can be very useful to help finding bugs in the system. Logs take up memory space, so turn
   * them on only when necessary. The fields should be used unless using the logger within threads.
   *
   * @param logger The logger.
   */
  public static void setLogger(Logger logger) {
    while (!isDriverLoaded) {
      Thread.yield();
    }
    privateSetLogger(logger);
  }
   
   /**
    * Sets the litebase logger. This enables log messages for all queries and statements of Litebase and can be very useful to help finding bugs in 
    * the system. Logs take up memory space, so turn them on only when necessary. The fields should be used unless using the logger within threads.
    * 
    * @param logger The logger.
    */
   private static native void privateSetLogger(Logger logger);

  /**
   * Gets the default Litebase logger. When this method is called for the first time, a new <code>
   * PDBFile</code> is created and a log record started. In the subsequent calls, the same <code>
   * PDBFile</code> is used, but in different log records.
   *
   * @return The default Litebase logger.
   */
  public static Logger getDefaultLogger() {
    while (!isDriverLoaded) {
      Thread.yield();
    }
    return privateGetDefaultLogger();
  }
   
   /**
    * Gets the default Litebase logger. When this method is called for the first time, a new <code>PDBFile</code> is created and a log record 
    * started. In the subsequent calls, the same <code>PDBFile</code> is used, but in different log records.
    * 
    * @return The default Litebase logger.
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   private static native Logger privateGetDefaultLogger() throws DriverException;

  /**
   * Deletes all log files found in the device. If log is enabled, the current log file is not
   * affected by this command. It only deletes PDB log files.
   *
   * @return the number of files deleted.
   */
  public static int deleteLogFiles() {
    while (!isDriverLoaded) {
      Thread.yield();
    }
    return privateDeleteLogFiles();
  }
   
   /**
    * Deletes all log files found in the device. If log is enabled, the current log file is not affected by this command. It only deletes PDB log 
    * files.
    * 
    * @return the number of files deleted.
    */
   private static native int privateDeleteLogFiles();

  // guich@566_32 rnovais@570_77
  /**
   * This is a handy method that can be used to reproduce all commands of a log file. This is
   * intended to be used by the development team only. Here's a sample on how to use it:
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
   * @param sql The string array of SQL commands to be executed.
   * @param params The parameters to open a connection.
   * @param isDebug Indicates if debug information is to displayed on the debug console.
   * @return The LitebaseConnection instance created, or <code>null</code> if <code>closeAll</code>
   *     was the last command executed (or no commands were executed at all).
   */
  public static LitebaseConnection4D processLogs(String[] sql, String params, boolean isDebug) {
    while (!isDriverLoaded) {
      Thread.yield();
    }
    return privateProcessLogs(sql, params, isDebug);
  }
   
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
    * @param sql The string array of SQL commands to be executed.
    * @param params The parameters to open a connection.
    * @param isDebug Indicates if debug information is to displayed on the debug console.
    * @return The LitebaseConnection instance created, or <code>null</code> if <code>closeAll</code> was the last command executed (or no commands 
    * were executed at all).
    * @throws DriverException If an exception occurs.
    * @throws NullPointerException If <code>sql</code> is null.
    * @throws OutOfMemoryError If a memory allocation fails.
    */
   private static native LitebaseConnection4D privateProcessLogs(String[] sql, String params, boolean isDebug) throws DriverException, 
                                                                                                      NullPointerException, OutOfMemoryError;
   
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@220_5: added a method to recover possible corrupted tables, the ones that were not closed properly.
   /**
    * Tries to recover a table not closed properly by marking and erasing logically the records whose crc are not valid.
    * 
    * @param tableName The table to be recovered.
    * @return The number of purged records.
    * @throws DriverException If the table name or path is too big.
    * @throws OutOfMemoryError If a memory allocation fails.
    */
   public native boolean recoverTable(String tableName) throws DriverException, OutOfMemoryError;
   
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@220_11: added a method to convert a table from the previous format to the current one being used.
   /**
    * Converts a table from the previous Litebase table version to the current one. If the table format is older than the previous table version, 
    * this method can't be used. It is possible to know if the table version is not compativel with the current version used in Litebase because 
    * an exception will be thrown if one tries to open a table with the old format. The table will be closed after using this method. Notice that 
    * the table .db file will be overwritten. 
    * 
    * @param tableName The name of the table to be converted.
    * @throws DriverException If the table version is not the previous one (too old or the actual used by Litebase) or table name or path is too big.
    * @throws OutOfMemoryError If a memory allocation fails.
    */
   public native void convert(String tableName) throws DriverException, OutOfMemoryError;
   
   /**
    * Finalizes the <code>LitebaseConnection</code> object.
    */
   protected void finalize()
   {
      closeAll();
   }
   
   /**
    * Used to returned the slot where the tables were stored on Palm OS. Not used anymore.
    * 
    * @return -1.
    * @deprecated Not used anymore.
	*/
   public native int getSlot(); // juliana@223_1: added a method to get the current slot being used. Returns -1 except on palm.

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@226_6: added LitebaseConnection.isOpen(), which indicates if a table is open in the current connection.
   /**
    * Indicates if a table is open or not.
    * 
    * @param tableName The table name to be checked
    * @return <code>true</code> if the table is open in the current connection; <code>false</code>, otherwise.
    * @throws DriverException If the table name is too big.
    */
   public native boolean isOpen(String tableName) throws DriverException;

   // juliana@226_10: added LitebaseConnection.dropDatabase().
   /**
    * Drops all the tables from a database represented by its application id and path.
    * 
    * @param crid The application id of the database.
    * @param sourcePath The path where the files are stored.
    * @param slot Not used anymore.
    * @throws DriverException If the database is not found or a file error occurs.
    * @throws NullPointerException If one of the string parameters is null.
    */
   public native static void dropDatabase(String crid, String sourcePath, int slot) throws DriverException, NullPointerException;

   // juliana@250_5: added LitebaseConnection.isTableProperlyClosed() and LitebaseConnection.listAllTables().
   /**
    * Indicates if a table is closed properly or not.
    * 
    * @param tableName The table to be verified.
    * @return <code>true</code> if the table is closed properly or is open (a not properly closed table can't be opened); <code>false</code>, 
    * otherwise.
    * @throws DriverException If the table is corrupted.
    * @throws NullPointerException If tableName is null.
    */
   public native boolean isTableProperlyClosed(String tableName) throws DriverException, NullPointerException;
   
   /**
    * Lists all table names of the current connection. If the current connection has no tables, an empty list is returned.
    * 
    * @return An array of all the table names of the current connection.
    * @throws DriverException If a file error occurs.
    * @throws IllegalStateException If the driver is closed. 
    * @throws OutOfMemoryError If a memory allocation fails.
    */
   public native String[] listAllTables() throws DriverException, IllegalStateException, OutOfMemoryError;

   // juliana@253_16: created static methods LitebaseConnection.encryptTables() and decryptTables().
   /**
    * Encrypts all the tables of a connection given from the application id. All the files of the tables must be closed!
    * 
    * @param crid The application id of the database.
    * @param sourcePath The path where the files are stored.
    * @param slot Not used anymore.
    */
   public native static void encryptTables(String crid, String sourcePath, int slot);
   
   /**
    * Decrypts all the tables of a connection given from the application id. All the files of the tables must be closed!
    * 
    * @param crid The application id of the database.
    * @param sourcePath The path where the files are stored.
    * @param slot Not used anymore.
    */
   public native static void decryptTables(String crid, String sourcePath, int slot);
}
