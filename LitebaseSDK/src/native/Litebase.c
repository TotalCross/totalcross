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
 * Defines functions to deal with important Litebase funcionalities.
 */

#include "Litebase.h"

#ifdef ENABLE_TEST_SUITE // Enable internal test cases running.
bool ranTests;
#endif

/**
 * A list of objects used to hold prepared statements that uses a specific table.
 */
TC_ImplementList(TCObject);

/**
 * Loads the necessary data when using Litebase for the first time.
 *
 * @param params Some parameters and function pointers in order to load a .dll.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
LB_API bool LibOpen(OpenParams params)
{
   if (!initVars(params)) // Initializes Litebase structures.
      return false;
#ifdef ENABLE_TEST_SUITE // Runs internal test cases.
   if (!ranTests)
   {
      TestSuite testSuite;
      Context currentContext = params->currentContext;

      ranTests = true;

      // Initializes the used test functions.
      testSuite.assertEqualsI16 = assertEqualsI16;
      testSuite.assertEqualsI32 = assertEqualsI32;
      testSuite.assertEqualsI64 = assertEqualsI64;
      testSuite.assertEqualsU8 = assertEqualsU8;
      testSuite.assertEqualsDbl = assertEqualsDbl;
      testSuite.assertEqualsSz = assertEqualsSz;
      testSuite.assertEqualsPtr = assertEqualsPtr;
      testSuite.assertEqualsBlock = assertEqualsBlock;
      testSuite.assertEqualsNull = assertEqualsNull;
      testSuite.assertEqualsNotNull = assertEqualsNotNull;
      testSuite.assertEqualsTrue = assertEqualsTrue;
      testSuite.assertEqualsFalse = assertEqualsFalse;
      testSuite.fail = fail;
      testSuite.doubleError = 1e-8;
      testSuite.failed = 0;

      // The test cases.
      test_createComposedIndex(&testSuite, currentContext);
      test_initLex(&testSuite, currentContext);
      test_getMessage(&testSuite, currentContext);
      test_initLitebaseMessage(&testSuite, currentContext);
      test_initLitebaseParser(&testSuite, currentContext);
      test_lbError(&testSuite, currentContext);
      test_lbErrorWithMessage(&testSuite, currentContext);
      test_LibClose(&testSuite, currentContext);
      test_LibOpen(&testSuite, currentContext);
      test_bindFunctionDataType(&testSuite, currentContext);
      test_checkApppath(&testSuite, currentContext);
      test_dataTypeFunctionsName(&testSuite, currentContext);
      test_initVars(&testSuite, currentContext);
      test_markBitsOnValue(&testSuite, currentContext);
      test_markBitsReset(&testSuite, currentContext);
      test_mfClose(&testSuite, currentContext);
      test_mfGrowTo(&testSuite, currentContext);
      test_mfReadBytes(&testSuite, currentContext);
      test_mfSetPos(&testSuite, currentContext);
      test_mfWriteBytes(&testSuite, currentContext);
      test_applyDataTypeFunction(&testSuite, currentContext);
      test_newSQLValues(&testSuite, currentContext);
      test_valueCompareTo(&testSuite, currentContext);
      test_initTCVMLib(&testSuite, currentContext);
      test_rowUpdated(&testSuite, currentContext);
      currentContext->thrownException = null;
      
      // The test results.
      TC_alert("%02d test total\n%02d succeeded\n%02d failed", 30, 30 - testSuite.failed, testSuite.failed);
   }
#endif
   return true;
}

/**
 * Flushs all pending data and destroy all Litebase structures when closing the application.
 */
LB_API void LibClose()
{
	TRACE("LibClose")
   getMainContextFunc TC_getMainContext = TC_getProcAddress(null, "getMainContext");
   
   TC_htFreeContext(TC_getMainContext(), &htCreatedDrivers, (VisitElementContextFunc)freeLitebase); // Flushs pending data and closes all tables. 
   muFree(&memoryUsage); // Destroys memory usage hash table.
   TC_htFree(&reserved, null); // Destroys the reserved words hash table.

   // Destroy the mutexes.
   DESTROY_MUTEX(parser);
   DESTROY_MUTEX(log);
}

/**
 * Loads the necessary data when using Litebase for the first time.
 *
 * @param params Some parameters and function pointers in order to load a .dll.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws OutOfMemoryError if a memory allocation fails.
 */
bool initVars(OpenParams params)
{
   Context context = params->currentContext; 

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
// Initializes the list of Litebase opened files.
#if defined(POSIX) || defined(ANDROID)
   xmemzero(&filesList, sizeof(XFilesList));
#endif

   // Initializes the mutexes.
	SETUP_MUTEX;
   INIT_MUTEX(parser);
   INIT_MUTEX(log);
   INIT_MUTEX(files);

   memoryUsage.items = null;
   reserved.items = null;

   // Initializes the TCVM functions needed by Litebase.
   TC_getProcAddress = (getProcAddressFunc)params->getProcAddress;
   initTCVMLib();

   if (!(htCreatedDrivers = TC_htNew(10, null)).items // Allocates a hash table for the loaded connections.
    || !(memoryUsage = muNew(100)).items // Allocates a hash table for select statistics.
    || !initLex()) // Initializes the lex structures.
   {
      TC_htFree(&htCreatedDrivers, null);
      TC_htFree(&reserved, null);
      muFree(&memoryUsage);
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
      return false; 
   }  
	
	initLitebaseMessage(); // Loads Litebase error messages.

	make_crc_table(); // Initializes the crc table for calculating crc32 codes.
	
   // Loads classes.                                                                                                                    
   litebaseConnectionClass = TC_loadClass(context, "litebase.LitebaseConnection", false);            
	loggerClass = TC_loadClass(context, "totalcross.util.Logger", false);                                                                 

   return true;                                                                                             
}

// juliana@253_8: now Litebase supports weak cryptography.
/**
 * Creates a LitebaseConnection for the given creator id and with the given connection param list. This method avoids the creation of more than
 * one instance with the same creator id and parameters, which would lead to performance and memory problems.
 *
 * @param context The thread context where the function is being executed.
 * @param crid The creator id, which may be the same one of the current application and MUST be 4 characters long.
 * @param objParams Only the folder where it is desired to store the tables, <code>null</code>, if it is desired to use the current data 
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
 * @throws OutOfMemoryError If memory allocation fails.
 */
TCObject create(Context context, int32 crid, TCObject objParams) 
{
	TRACE("create")
	TCObject driver,
          logger = litebaseConnectionClass->objStaticValues[1];
   int32 hash;
   bool isAscii = false,
        useCrypto = false;
   TCHAR sourcePath[1024];
	TCHARP path = null;
   char params[1024];

   if (logger) // juliana@230_30: reduced log files size.
   {
		TCObject logSBuffer = litebaseConnectionClass->objStaticValues[2];
      char cridBuffer[5];
      
      LOCKVAR(log);
      
      // Builds the logger StringBuffer contents.
      StringBuffer_count(logSBuffer) = 0;
      TC_int2CRID(crid, cridBuffer);
      if (!TC_appendCharP(context, logSBuffer, "new LitebaseConnection(") || !TC_appendCharP(context, logSBuffer, cridBuffer)
       || !TC_appendCharP(context, logSBuffer, ","))
         goto error;
          
      if (objParams)
      {
         if (!TC_appendJCharP(context, logSBuffer, String_charsStart(objParams), String_charsLen(objParams)))
	         goto error;
	   }
	   else if (!TC_appendCharP(context, logSBuffer, "null"))
	      goto error;
	   if (!TC_appendCharP(context, logSBuffer, ")"))
	      goto error;
	  
      TC_executeMethod(context, loggerLogInfo, logger, logSBuffer); // Logs the Litebase operation. 
      
error:
      UNLOCKVAR(log);
      if (context->thrownException) // juliana@223_14: solved possible memory problems.
         return null;
   }

   if (objParams)
	{
	   CharP tempParams[3];
		int32 i = 1,
		      numParams;
		
      params[0] = 0;
      tempParams[0] = tempParams[1] = tempParams[2] = null;

      // juliana@250_4: now getInstance() can receive only the parameter chars_type = ...
      // juliana@210_2: now Litebase supports tables with ascii strings.
      TC_JCharP2CharPBuf(String_charsStart(objParams), String_charsLen(objParams), params);
		tempParams[0] = params;
      tempParams[1] = xstrchr(params, ';'); // Separates the parameters.
		if (tempParams[1])
		{
		   tempParams[1][0] = 0;
		   tempParams[2] = xstrchr(++tempParams[1], ';');
		   i++;
		   
		   if (tempParams[2])
		   {
		      tempParams[2][0] = 0;
		      tempParams[2]++;
		      i++;
		   }
		}

      numParams = i;
      while (--i >= 0) // The parameters order does not matter. 
		{
			tempParams[i] = strTrim(tempParams[i]);
			if (xstrstr(tempParams[i], "chars_type")) // Chars type param.
            isAscii = (xstrstr(tempParams[i], "ascii") != null);
			else if (xstrstr(tempParams[i], "path")) // Path param.
            path = TC_CharP2TCHARPBuf(&xstrchr(tempParams[i], '=')[1], sourcePath);
			else if (xstrstr(tempParams[i], "crypto")) // Cryptography param.
			   useCrypto = true;   
	      else if (numParams == 1) 
            path = TC_CharP2TCHARPBuf(tempParams[0], sourcePath); // Things do not change if there is only one parameter.
		   else // juliana@253_11: now a DriverException will be throw if an incorrect parameter is passed in LitebaseConnection.getInstance().
		   {
		      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PARAMETER), tempParams[i]);
		      return null;
		   }
		}
   }
 
   // Gets the slot and checks the path validity.
   if (!checkApppath(context, sourcePath, path)) // juliana@214_1
		return null;

	// juliana@221_3: solved a small bug that could make Litebase crash on Windows 32, Windows CE, Palm, iPhone, and Android when passing 2 arguments to
   // params when issuing a LitebaseConnection.getInstance().

   // fdie@555_2: driver not already created? Creates one.
   // If there is no connections with this key, creates a new one.
   if (!(driver = TC_htGetPtr(&htCreatedDrivers, (hash = TC_hashCodeFmt("ixiis", crid, context->thread, isAscii, useCrypto, sourcePath? TC_TCHARP2CharPBuf(sourcePath, params): "null"))))) 
   {
		Hashtable htTables,
                htPS;

		if (!(driver = TC_createObject(context, "litebase.LitebaseConnection")))
			return null;

      OBJ_LitebaseAppCrid(driver) = crid; // juliana@210a_10
	   OBJ_LitebaseIsAscii(driver) = isAscii;
	   OBJ_LitebaseUseCrypto(driver) = useCrypto;
	   OBJ_LitebaseKey(driver) = hash;
		
      // SourcePath.
      if (!(setLitebaseSourcePath(driver, xmalloc(sizeof(TCHAR) * (tcslen(sourcePath) + 1)))))
		{
error1:
		   freeLitebase(context, (size_t)driver);
		   TC_setObjectLock(driver, UNLOCKED);
         TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
         return null;
      }
      tcscpy(getLitebaseSourcePath(driver), sourcePath);

      // Current loaded tables.
      if (!(htTables = TC_htNew(10, null)).items)
         goto error1;
		if (!(setLitebaseHtTables(driver, xmalloc(sizeof(Hashtable)))))
		   goto error1;
	   xmemmove(getLitebaseHtTables(driver), &htTables, sizeof(Hashtable)); 
      
      // Current loaded prepared statements.
      // juliana@226_16: prepared statement is now a singleton.
      if (!(htPS = TC_htNew(30, null)).items)
         goto error1;
		if (!(setLitebaseHtPS(driver, xmalloc(sizeof(Hashtable)))))
		   goto error1;
	   xmemmove(getLitebaseHtPS(driver), &htPS, sizeof(Hashtable)); 

      // juliana@253_6: the maximum number of keys of a index was duplicated.
      if (!setLitebaseNodes(driver, xmalloc(MAX_IDX << 2)))
         goto error1;

      // Stores the driver into the drivers hash table.
      if (!TC_htPutPtr(&htCreatedDrivers, hash, driver))
         goto error1;
   }
   else
      TC_setObjectLock(driver, LOCKED);
   return driver;
}

/**
 * Frees all data concerning a certaim driver connection.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The driver as an int because this function may be called from a hash table.
 */
void freeLitebase(Context context, size_t driver)
{
	TRACE("freeLitebase")
   TCHARP sourcePath = getLitebaseSourcePath(driver);
   int32* nodes = getLitebaseNodes(driver); // juliana@253_6: the maximum number of keys of a index was duplicated.
	Hashtable* htTables = getLitebaseHtTables(driver);
   Hashtable* htPs = getLitebaseHtPS(driver);

	if (htTables) // Frees all the openned tables and the their hash table. 
	{
		TC_htFreeContext(context, htTables, (VisitElementContextFunc)freeTableHT);
		xfree(htTables);
	}

   if (htPs) // juliana@230_19: removed some possible memory problems with prepared statements and ResultSet.getStrings().
   {
      TC_htFree(htPs, (VisitElementFunc)freePreparedStatement);
		xfree(htPs);
   }

   xfree(sourcePath); // Frees the source path.
   xfree(nodes); // juliana@253_6: the maximum number of keys of a index was duplicated.
	TC_htRemove(&htCreatedDrivers, OBJ_LitebaseKey((TCObject)driver)); // fdie@555_2: removes this instance from the drivers hash table.
	OBJ_LitebaseDontFinalize((TCObject)driver) = true; // This object shouldn't be finalized again.
}

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
 * @param context The thread context where the function is being executed.
 * @param driver The current Litebase connection.
 * @param sqlStr The SQL creation command.
 * @param sqlLen The SQL string length.
 * @throws SQLParseException If the table name or a default string is too big, there is an invalid default value, or an unknown (on a create table)
 * or repeated column name, or an invalid number occurs.
 * @throws AlreadyCreatedException If the table or index is already created.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
void litebaseExecute(Context context, TCObject driver, JCharP sqlStr, uint32 sqlLen)
{
	TRACE("litebaseExecute")
   char tableName[DBNAME_SIZE];
   LitebaseParser* parser;
	bool locked = true;
   int32 i;
   int32* hashes;
   CharP* names;
   Heap heapParser = heapCreate(),
        heap = null;

   // Does de parsing.
	LOCKVAR(parser);
	IF_HEAP_ERROR(heapParser)
   {
		if (locked)
         UNLOCKVAR(parser);
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
error:
      heapDestroy(heapParser);
	   heapDestroy(heap);
      return;
   }
   heapParser->greedyAlloc = true;
	parser = initLitebaseParser(context, sqlStr, sqlLen, false, heapParser);
   UNLOCKVAR(parser);
	locked = false;
   if (!parser)
      goto error;

   if (parser->command == CMD_CREATE_TABLE)
   {
      int32 count = parser->fieldListSize + 1, // fieldListSize + rowid
            primaryKeyCol = NO_PRIMARY_KEY, // juliana@114_9
            composedPK = NO_PRIMARY_KEY,
            numberComposedPKCols,
            type;
      bool error = false;
      uint8* columnAttrs;
      uint8* composedPKCols = null;
      int8* types;
      int32* sizes;
      CharP buffer;
      JCharP defaultValue;
      SQLValue** defaultValues;
      SQLValue* defaultValueI;
      SQLFieldDefinition** fieldList = parser->fieldList; 
      SQLFieldDefinition* field;
      DoubleBuf doubleBuf;
                     
      // Verifies the length of the table name.
      if (xstrlen(parser->tableList[0]->tableName) > MAX_TABLE_NAME_LENGTH) // rnovais@112_3 rnovais@570_114: The table name can't be infinite.
      {
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_MAX_TABLE_NAME_LENGTH));
         goto error; // juliana@201_25: memory leak.
      }
      
      xstrcpy(tableName, parser->tableList[0]->tableName);
      if (tableExistsByName(context, driver, tableName)) // guich@105: verifies if it is already created.
         goto error; // juliana@201_25: memory leak.

      // Creates the table heap.
      heap = heapCreate();
      IF_HEAP_ERROR(heap)
      {
         TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
         goto error;
      }
      heap->greedyAlloc = true;

      // Now gets the columns.
      hashes = (int32*)TC_heapAlloc(heap, count << 2);
      types = (int8*)TC_heapAlloc(heap, count);
      sizes = (int32*)TC_heapAlloc(heap, count << 2);
      names = (CharP*)TC_heapAlloc(heap, count * TSIZE);
      defaultValues = (SQLValue**)TC_heapAlloc(heap, count * TSIZE);
      columnAttrs = (uint8*)TC_heapAlloc(heap, count);

      // Creates column 0 (rowid).
      hashes[0] = HCROWID;
      types[0] = INT_TYPE;
		names[0] = "rowid";

      i = count;
      while (--i) // Creates the other columns
      {
         TC_CharPToLower(buffer = (field = fieldList[i - 1])->fieldName);
         hashes[i] = TC_hashCode(names[i] = TC_hstrdup(buffer, heap));
         types[i] = field->fieldType;
         sizes[i] = field->fieldSize;

         if (field->isPrimaryKey) // Checks if there is a primary key definition.
            primaryKeyCol = i; // juliana@114_9
           
         if (field->defaultValue) // Default values: default null has no effect. This is handled by the parser.
         {
            defaultValueI = defaultValues[i] = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));
            sqlLen = TC_JCharPLen(defaultValue = field->defaultValue);
            columnAttrs[i] |= ATTR_COLUMN_HAS_DEFAULT;  // Sets the bit of default. 
            
            if (((type = types[i]) != CHARS_TYPE && type != CHARS_NOCASE_TYPE && sqlLen > 39) 
             || ((type == CHARS_TYPE || type == CHARS_NOCASE_TYPE) && (int32)sqlLen > sizes[i]))
            {
               TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_LENGTH_DEFAULT_VALUE_IS_BIGGER));
               goto error;
            }
            
            switch (types[i])
            {
               case CHARS_TYPE:
               case CHARS_NOCASE_TYPE:
                  defaultValueI->length = sqlLen;
                  defaultValueI->asChars = TC_heapAlloc(heap, (sqlLen << 1) + 2);
                  xmemmove(defaultValueI->asChars, defaultValue, sqlLen << 1);
                  break;

               case SHORT_TYPE:
                  defaultValueI->asShort = str2short(TC_JCharP2CharPBuf(defaultValue, sqlLen, doubleBuf), &error);
                  break;

               case INT_TYPE:
                  defaultValueI->asInt = TC_str2int(TC_JCharP2CharPBuf(defaultValue, sqlLen, doubleBuf), &error);
                  break;

               case LONG_TYPE:
                  defaultValueI->asLong = TC_str2long(TC_JCharP2CharPBuf(defaultValue, sqlLen, doubleBuf), &error);
                  break;

               case FLOAT_TYPE:
                  defaultValueI->asFloat = str2float(TC_JCharP2CharPBuf(defaultValue, sqlLen, doubleBuf), &error);
                  break;

               case DOUBLE_TYPE:
                  defaultValueI->asDouble = TC_str2double(TC_JCharP2CharPBuf(defaultValue, sqlLen, doubleBuf), &error);
                  break;
                  
               case DATE_TYPE:
               case DATETIME_TYPE:
                  TC_JCharP2CharPBuf(defaultValue, sqlLen, doubleBuf);                 
                  if (!testAndPrepareDateAndTime(context, defaultValues[i], doubleBuf, type))
                     goto error;
            }

            if (error)
            {
               TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INVALID_NUMBER), defaultValue, "number");
               goto error;
            }
         }

         if (field->isNotNull) // Sets the 'not null' bit.
            columnAttrs[i] |= ATTR_COLUMN_IS_NOT_NULL;
      }

      if ((numberComposedPKCols = parser->fieldNamesSize) > 0) // Gets the composed primary keys.
      {
         int32 pos = -1,
               j,
               hashCol;
         CharP* fields = parser->fieldNames;
         
         composedPKCols = (uint8*)TC_heapAlloc(heap, numberComposedPKCols);
         i = -1; 

         while (++i < numberComposedPKCols)
         {
            hashCol = TC_hashCode(fields[i]);
            j = count;

            while (--j >= 0) // Checks if the name of a table column exist.
               if (hashCol == hashes[j])
               {
                  pos = j;
                  break;
               }
            if (pos == -1) // Column not found.
            {
               TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_UNKNOWN_COLUMN), fields[i]);
               goto error;
            }
            if (types[pos] == BLOB_TYPE) // A blob can't be in a composed PK.
            {
               TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_BLOB_PRIMARY_KEY));
               goto error;
            }
            j = -1;
            while (++j < i) // Verifies if there's a duplicate definition.
               if (composedPKCols[j] == pos)
               {
                  TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_DUPLICATED_COLUMN_NAME), fields[i]);
                  goto error;
               }
            composedPKCols[i] = pos;
         }
         if (numberComposedPKCols == 1)
         {
            numberComposedPKCols = 0;
            primaryKeyCol = pos;
         }
         else 
            composedPK = 0;
      }

      driverCreateTable(context, driver, tableName, names, hashes, types, sizes, columnAttrs, defaultValues, primaryKeyCol, composedPK, composedPKCols, numberComposedPKCols, count, heap);
   }
   else if (parser->command == CMD_CREATE_INDEX)
   {
      Table* table;

      xstrcpy(tableName, parser->tableList[0]->tableName); // indexTableName ignored - formed internally.
      table = getTable(context, driver, tableName);

      if (table)
      {
         Hashtable htTable = TC_htNew((i = parser->fieldNamesSize) + 1, heapParser);
       
         IF_HEAP_ERROR(table->heap)
         {
            TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            goto error;
         }
         
			names = parser->fieldNames;
         hashes = (int32*)TC_heapAlloc(table->heap, i << 2);
         while (--i >= 0)
         {
            // juliana@225_8: it was possible to create a composed index with duplicated column names.
            if (TC_htGet32(&htTable, hashes[i] = TC_hashCode(names[i])))
            {
               TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_DUPLICATED_COLUMN_NAME), names[i]);
               goto error;
            }
            TC_htPut32(&htTable, hashes[i], hashes[i]);
         }
         driverCreateIndex(context, table, hashes, 0, parser->fieldNamesSize, null);
      }
   }
	else
		TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_ONLY_CREATE_TABLE_INDEX_IS_ALLOWED));
   heapDestroy(heapParser);
}

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
 *  values (32, 2000, 'guilherme campos hazan', 'guich@superwaba.com.br')&quot;);</code> will insert a record in the table.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The current Litebase connection.
 * @param sqlStr The SQL creation command.
 * @param sqlLen The SQL string length.
 * @return The number of rows affected or <code>0</code> if a drop or alter operation was successful.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
int32 litebaseExecuteUpdate(Context context, TCObject driver, JCharP sqlStr, int32 sqlLen)
{
   TRACE("litebaseExecuteUpdate")
   LitebaseParser* parser;
   int32 returnVal = -1;
	Heap heapParser = heapCreate();
	bool locked = true;

   // Does de parsing.
	LOCKVAR(parser);
	IF_HEAP_ERROR(heapParser)
   {
		if (locked)
         UNLOCKVAR(parser);
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
      goto finish;
   }
   heapParser->greedyAlloc = true;
	parser = initLitebaseParser(context, sqlStr, sqlLen, false, heapParser);
   UNLOCKVAR(parser);
	locked = false;
   if (!parser)
      goto finish;

   switch (parser->command)
   {
      case CMD_DROP_TABLE:
         litebaseExecuteDropTable(context, driver, parser);
         return 0;
      case CMD_DROP_INDEX:
         return litebaseExecuteDropIndex(context, driver, parser);
      case CMD_ALTER_DROP_PK:
      case CMD_ALTER_ADD_PK:
      case CMD_ALTER_RENAME_TABLE:
      case CMD_ALTER_RENAME_COLUMN:
      case CMD_ALTER_ADD_COLUMN:
         litebaseExecuteAlter(context, driver, parser);
         return 0;
      case CMD_INSERT:
      {
         SQLInsertStatement* insertStmt = initSQLInsertStatement(context, driver, parser);
			
         if (insertStmt && litebaseBindInsertStatement(context, insertStmt))
		      returnVal = litebaseDoInsert(context, insertStmt);
		   goto finish;   
		   
      }
      case CMD_UPDATE:
      {
         SQLUpdateStatement* updateStmt = initSQLUpdateStatement(context, driver, parser, false);
         
         if (updateStmt && litebaseBindUpdateStatement(context, updateStmt))
            returnVal = litebaseDoUpdate(context, updateStmt);
         goto finish;
      }
      case CMD_DELETE:
      {
        SQLDeleteStatement* deleteStmt = initSQLDeleteStatement(parser, false);
        if (litebaseBindDeleteStatement(context, driver, deleteStmt))
		     returnVal = litebaseDoDelete(context, deleteStmt);
        goto finish;
      }
   }
   
finish:      
   heapDestroy(heapParser);
   return returnVal;
}

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
 * @param context The thread context where the function is being executed.
 * @param driver The current Litebase connection.
 * @param sqlStr The SQL creation command.
 * @param sqlLen The SQL string length.
 * @return A result set with the values returned from the query or <code>null</code> if an error occurs.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
TCObject litebaseExecuteQuery(Context context, TCObject driver, JCharP strSql, int32 length)
{
   TRACE("litebaseExecuteQuery")
	Heap heapParser = heapCreate();
   LitebaseParser* parser;
	SQLSelectStatement* selectStmt;
   ResultSet* resultSetBag;
	TCObject resultSet;
   PlainDB* plainDB;
   bool locked = false;

   // Does the parsing.
	IF_HEAP_ERROR(heapParser)
   {
nomem:
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
error:
		if (locked)
         UNLOCKVAR(parser);
      heapDestroy(heapParser);
      return null;
   }
	locked = true;
	LOCKVAR(parser);
	heapParser->greedyAlloc = true;
	parser = initLitebaseParser(context, strSql, length, true, heapParser);
   UNLOCKVAR(parser);
	locked = false;
   if (!parser)
      goto error;

   // Creates the select statement, binds it and performs the select.
   if (!(selectStmt = initSQLSelectStatement(parser, false))
    || !litebaseBindSelectStatement(context, driver, selectStmt)
    || !(resultSet = litebaseDoSelect(context, driver, selectStmt)))
      goto error;

   // juliana@223_9: improved Litebase temporary table allocation on Windows 32, Windows CE, Palm, iPhone, and Android.
   locked = true;
	LOCKVAR(parser);

   // Gets the query result table size and stores it.
   resultSetBag = getResultSetBag(resultSet);
   plainDB = &resultSetBag->table->db;
   if (!muPut(&memoryUsage, selectStmt->selectClause->sqlHashCode, plainDB->db.size, plainDB->dbo.size))
      goto nomem;
	UNLOCKVAR(parser);
	locked = false;

   return resultSet;
}

/**
 * Drops a table.
 * 
 * @param context The thread context where the function is being executed.
 * @param driver The current Litebase connection.
 * @param parser The parser.
 * @throws DriverException If the table does not exist, if its name is greater than the maximum possible or it is not possible to remove it.
 */
void litebaseExecuteDropTable(Context context, TCObject driver, LitebaseParser* parser)
{
	TRACE("litebaseExecuteDropTable")
   Table* table;
   CharP tableName = parser->tableList[0]->tableName;
   int32 i,
         hashCode;
	Hashtable* htTables = getLitebaseHtTables(driver);
   Heap heap = parser->heap;
   TCHARP sourcePath = getLitebaseSourcePath(driver);
   
   if (xstrlen(tableName) > 23) // The table name has a maximum length because of palm os.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_MAX_TABLE_NAME_LENGTH));
      goto finish;
   }

   if ((table = (Table*)TC_htGetPtr(htTables, hashCode = TC_hashCode(tableName)))) // The table is open.
   {
      Index** columnIndexes = table->columnIndexes;
      ComposedIndex** composedIndexes = table->composedIndexes;
      Index* index;

      i = table->columnCount;
      TC_htRemove(htTables, hashCode);

      while (--i >= 0) // Drops its simple indices.
      {
         if (columnIndexes[i] && !indexRemove(context, columnIndexes[i]))
            goto finish;
         columnIndexes[i] = null;
      }

      // juliana@223_14: solved possible memory problems.
      if ((i = table->numberComposedIndexes - 1) >= 0) 
         while (--i >= 0) // Drops its composed indices.
         {
            if ((index = composedIndexes[i]->index) && !indexRemove(context, index))
               goto finish;
            composedIndexes[i]->index = null;
         }

      if (!freeTable(context, table, true, false)) // Drops the table.
         goto finish;
   }
   else // The table is closed.
   {
      int32 deleted = 0,
            count = 0;
      char fileName[DBNAME_SIZE],
		     fileSimpIdxName[DBNAME_SIZE],
		     fileCompIdxName[DBNAME_SIZE];
      TCHARPs* list = null;
      TCHAR buffer[MAX_PATHNAME];
      char value[DBNAME_SIZE];

      getDiskTableName(context, OBJ_LitebaseAppCrid(driver), tableName, fileName);
		
		// juliana@220_12: drop table was dropping a closed table and all tables starting with the same name of the dropped one.
		xstrcpy(fileSimpIdxName, fileName);
      xstrcpy(fileCompIdxName, fileName);
		xstrcat(fileName, ".");
		xstrcat(fileSimpIdxName, "$");
		xstrcat(fileCompIdxName, "&");

      if ((i = TC_listFiles(sourcePath, -1, &list, &count, heap, 0))) // Lists all the path files.
      {
         fileError(context, i, "");
         goto finish;
      }

      while (--count >= 0) // Erases the table files.
      {
         TC_TCHARP2CharPBuf(list->value, value);

         if (xstrstr(value, fileName) == value || xstrstr(value, fileSimpIdxName) == value || xstrstr(value, fileCompIdxName) == value)
         {
            getFullFileName(value, sourcePath, buffer);
            if ((i = lbfileDelete(null, buffer, false)))
            {
               fileError(context, i, value);
               goto finish;
            }
            deleted = true;
         }
         list = list->next;
      }

      if (!deleted) // If there is no file to be erased, an exception must be raised.
      {
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_NAME_NOT_FOUND), tableName);
         goto finish;
      }
   }
   
finish:     
   heapDestroy(heap);
}

/**
 * Drops an index.
 * 
 * @param context The thread context where the function is being executed.
 * @param driver The current Litebase connection.
 * @param parser The parser.
 * @return <code>-1</code> if an error occured; the number of indices removed, otherwise.
 * @throws DriverException If a column does not have an index, is invalid, or if the columns to have the index dropped are from a primary key.
 */
int32 litebaseExecuteDropIndex(Context context, TCObject driver, LitebaseParser* parser)
{
   TRACE("litebaseExecuteDropIndex")
	int32 count = -1, 
         i;
   Table* table = getTable(context, driver, parser->tableList[0]->tableName);
   CharP* fieldNames = parser->fieldNames;

   if (!table)
      goto finish;

   // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since its 
   // last opening. 
   if (!setModified(context, table))
      goto finish;

   if (fieldNames[0][0] == '*' && !fieldNames[0][1]) // Drops all the indices.
      count = deleteAllIndexes(context, table);
   else // Drops an especific index.
   { 
      int32 column = TC_htGet32Inv(&table->htName2index, TC_hashCode(fieldNames[0])),
            fieldNamesSize = parser->fieldNamesSize;
      
      if (fieldNamesSize == 1) // Simple index.
      {
         if (column < 0) // Unknown column. 
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_COLUMN_NAME), fieldNames[0]);
            goto finish;
         }
         if (column == table->primaryKeyCol) // Can't use drop index to drop a primary key.
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_DROP_PRIMARY_KEY));
            goto finish;
         }
      
         driverDropIndex(context, table, column);
         count = 1;
      }
      else
      {
         uint8 columns[MAXIMUMS + 1];
         
         i = fieldNamesSize;
         while (--i >= 0)
         {
            if ((column = TC_htGet32Inv(&table->htName2index, TC_hashCode(fieldNames[i]))) < 0) // Unknown column. 
            {
               TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_COLUMN_NAME), fieldNames[i]);
               goto finish;
            }
            columns[i] = column;
         }
         if ((i = table->numberComposedPKCols))
         {
            uint8* keyCols = table->composedPrimaryKeyCols;
            while (--i >= 0)
               if (columns[i] != keyCols[i])
                  break;
         }
         if (i < 0) // Can't use drop index to drop a primary key.
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_DROP_PRIMARY_KEY));
            goto finish;
         }

         driverDropComposedIndex(context, table, columns, fieldNamesSize, -1, true);
         count = 1;
      }
   }

finish:
   heapDestroy(parser->heap);
   return count;
}

/**
 * Executes an alter statement.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The current Litebase connection.
 * @param parser The parser.
 * @throws DriverException If there is no primary key to be dropped, or an invalid column name.
 * @throws AlreadyCreatedException If one tries to add another primary key, or a simple primary key is added to a column that already has
 * an index.
 * @throws SQLParseException If there is a blob in a primary key definition or there is a duplicated column name in the primary key definition.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
void litebaseExecuteAlter(Context context, TCObject driver, LitebaseParser* parser)
{
	TRACE("litebaseExecuteAlter")
   Table* table = getTable(context, driver, parser->tableList[0]->tableName);
   Heap heap,
        parserHeap = parser->heap;
   CharP* fieldNames = parser->fieldNames;
   int32 i;

	if (!table)
      goto finish;
   heap = table->heap;

   // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since its 
   // last opening. 
   if (!setModified(context, table))
      goto finish;

   switch (parser->command)
   {
      case CMD_ALTER_DROP_PK: // DROP PRIMARY KEY
         if (table->primaryKeyCol != NO_PRIMARY_KEY) // Simple primary key.
         {
            i = table->primaryKeyCol;
            table->primaryKeyCol = NO_PRIMARY_KEY;
            driverDropIndex(context, table, i);
         } 
         else if (table->numberComposedPKCols > 0) // Composed primary key.
         {
            // juliana@230_17: solved a possible crash or exception if the table is not closed properly after dropping a composed primary key.
            // The meta data is saved.
            int32 number = table->numberComposedPKCols;
            table->numberComposedPKCols = 0;
            table->composedPK = NO_PRIMARY_KEY;
            driverDropComposedIndex(context, table, table->composedPrimaryKeyCols, number, -1, true);
         }
         else // There's no primary key.
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_DOESNOT_HAVE_PRIMARY_KEY));
         break;

      case CMD_ALTER_ADD_PK: // ADD PRIMARY KEY
      {
         int32 size = parser->fieldNamesSize,
               j,
               colIndex = -1;
         int32* hashCols = (int32*)TC_heapAlloc(heap, size << 2);
         int8* types = table->columnTypes;
         uint8* composedPKCols = (uint8*)TC_heapAlloc(heap, size);
         Hashtable* htName2index = &table->htName2index;

         IF_HEAP_ERROR(heap) // juliana@223_14: solved possible memory problems. 
         {
            table->primaryKeyCol = table->composedPK = NO_PRIMARY_KEY;
            TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            break;
         }

         if (table->primaryKeyCol != NO_PRIMARY_KEY || table->composedPK != NO_PRIMARY_KEY) // There can't be two primary keys.
         {
            TC_throwExceptionNamed(context, "litebase.AlreadyCreatedException", getMessage(ERR_PRIMARY_KEY_ALREADY_DEFINED));
            break;
         }
         
         i = -1;
         while (++i < size)
         {
            if ((colIndex = TC_htGet32Inv(htName2index, hashCols[i] = TC_hashCode(fieldNames[i]))) < 0)
            {
               TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_COLUMN_NAME), fieldNames[i]);
               goto finish;
            }

            // Verifies if there's a duplicate definition.
            j = i;
            while (--j >= 0)
               if (composedPKCols[j] == colIndex)
               {
                  TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_DUPLICATED_COLUMN_NAME), fieldNames[i]);
                  goto finish;
               }

            composedPKCols[i] = colIndex;
            if (types[colIndex] == BLOB_TYPE)
            {
               TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_BLOB_INDEX));
               goto finish;
            }
         }

         if (size == 1) // Simple primary key.
         {
            // juliana@230_41: an AlreadyCreatedException is now thrown when trying to add a primary key for a column that already has a simple index.
            if (table->columnIndexes[colIndex])
            {
               IntBuf intBuf;
               TC_throwExceptionNamed(context, "litebase.AlreadyCreatedException", getMessage(ERR_INDEX_ALREADY_CREATED), TC_int2str(colIndex, intBuf));
               break;
            }
            else // If there is no index yet for the column, creates it.
            {
               table->primaryKeyCol = colIndex;
            	if (!driverCreateIndex(context, table, hashCols, 1, 1, null))
               {
                  table->primaryKeyCol = -1;
                  break;
               }
            }
            
         }
         else //composed primary key
         {
            // juliana@202_20: Corrected a bug that would not create the composed PK correctly with alter table.
            table->numberComposedPKCols = size;
            table->composedPrimaryKeyCols = composedPKCols;
				table->composedPK = table->numberComposedIndexes; 
            if (!driverCreateIndex(context, table, hashCols, 1,size, composedPKCols))
				{
					table->composedPK = -1;
					break;
				}
				
         }
         break;
      }

      case CMD_ALTER_RENAME_TABLE: // RENAME TABLE
         if (tableExistsByName(context, driver, fieldNames[0])) // The new table name is stored in the field list. 
            break;
         if (!renameTable(context, driver, table, fieldNames[0])) // juliana@223_14: solved possible memory problems.
            renameTable(context, driver, table, parser->tableList[0]->tableName);
         break;

      case CMD_ALTER_RENAME_COLUMN: // RENAME COLUMN
      {
         CharP oldColumn = fieldNames[1], 
               newColumn;
         uint32 length = xstrlen(fieldNames[0]);
         bool reuseSpace = (length <= xstrlen(oldColumn));
         
         IF_HEAP_ERROR(heap)
         {
            TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            break;
         }
         
         if (reuseSpace) // If the new column name length is smaller than the previous one, reuses its space.
            newColumn = fieldNames[0];
         else
         {
            newColumn = (CharP)TC_heapAlloc(heap, length + 1);
            xmemmove(newColumn, fieldNames[0], length);
         }
         renameTableColumn(context, table, oldColumn, newColumn, reuseSpace);
         break;
      }
      
      case CMD_ALTER_ADD_COLUMN:
      {
         SQLFieldDefinition* field = parser->fieldList[0];
         int32 oldCount = table->columnCount,
               newCount = oldCount + 1,
               bytes = (oldCount + 7) >> 3,
               hash = TC_hashCode(field->fieldName);
               
         if (TC_htGet32Inv(&table->htName2index, hash) >= 0)
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_DUPLICATED_COLUMN_NAME), field->fieldName);    
         else if (oldCount == MAXIMUMS)
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_MAX_NUM_FIELDS_REACHED));
         else
         {
            uint8* newAttrs = TC_heapAlloc(heap, newCount);
            uint8* newTypes = TC_heapAlloc(heap, newCount);
            uint8* newBuffer;
            uint8* columnNulls;
            uint16* newOffsets = TC_heapAlloc(heap, (newCount + 1) << 1);
            int32* newHashes = (int32*)TC_heapAlloc(heap, newCount << 2);
            int32* newSizes = (int32*)TC_heapAlloc(heap, newCount << 2);          
            CharP* newNames = (CharP*)TC_heapAlloc(heap, newCount * TSIZE);
            SQLValue** newDefaultValues = (SQLValue**)TC_heapAlloc(heap, newCount * TSIZE);
            SQLValue** record = (SQLValue**)TC_heapAlloc(parserHeap, newCount * TSIZE);
            SQLValue* newDefaultValue = null;
            Index** newIndices = (Index**)TC_heapAlloc(heap, newCount * TSIZE);
            JCharP defaultValue = field->defaultValue;
            int32 type = field->fieldType;
            PlainDB newDB,
                    oldDB;
            PlainDB* plainDB = &table->db;
            char tempName[DBNAME_SIZE + 1];
            bool isNull = (field->defaultValue == null),
                 useCrypto = plainDB->db.useCrypto;
            int32 crc32,
                  j,
                  k,
                  size,
                  length,
                  blobLength;
            
            if (((oldCount + 8) >> 3) > bytes) // Increases the nulls fields if the number of bytes must be increased.
            {
               columnNulls = table->columnNulls = TC_heapAlloc(heap, ++bytes);
               table->storeNulls = TC_heapAlloc(heap, bytes);
            }
            else
               columnNulls = table->columnNulls;
            
            // Increases all the columns.              
            
            // Column attrs.
            xmemmove(newAttrs, table->columnAttrs, oldCount);
            (table->columnAttrs = newAttrs)[oldCount] = (field->defaultValue? ATTR_COLUMN_HAS_DEFAULT : 0)
                                                      | (field->isNotNull? ATTR_COLUMN_IS_NOT_NULL : 0); 
            
            // Column hashes.
            xmemmove(newHashes, table->columnHashes, oldCount << 2);
            TC_htPut32(&table->htName2index, (table->columnHashes = newHashes)[oldCount] = hash, oldCount);
            
            // Column offsets.
            xmemmove(newOffsets, table->columnOffsets, newCount << 1);
            (table->columnOffsets = newOffsets)[newCount] = (newOffsets[oldCount] + typeSizes[field->fieldType]);
            length = table->columnOffsets[newCount] + NUMBEROFBYTES(newCount);
            
            // Column types.
            xmemmove(newTypes, table->columnTypes, oldCount);
            (table->columnTypes = (int8*)newTypes)[oldCount] = type;
            
            // Column sizes.
            xmemmove(newSizes, table->columnSizes, oldCount << 2);
            (table->columnSizes = newSizes)[oldCount] = field->fieldSize;
            
            // Column names.
            xmemmove(newNames, table->columnNames, oldCount * TSIZE);
            (table->columnNames = newNames)[oldCount] = TC_heapAlloc(heap, i = (xstrlen(field->fieldName) + 1));
            xmemmove(newNames[oldCount], field->fieldName, i);  
            
            // Default values.
            xmemmove(newDefaultValues, table->defaultValues, oldCount * TSIZE);
            table->defaultValues = newDefaultValues;            
            if (defaultValue) // Sets the new default value if it exists.
            {   
               bool error = false;
               DoubleBuf doubleBuf;
               
               newDefaultValue = newDefaultValues[oldCount] = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));
               i = TC_JCharPLen(defaultValue);
               if ((type != CHARS_TYPE && type != CHARS_NOCASE_TYPE && i > 39) 
                || ((type == CHARS_TYPE || type == CHARS_NOCASE_TYPE) && i > field->fieldSize))
               {
                  TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_LENGTH_DEFAULT_VALUE_IS_BIGGER));
                  goto finish;
               }
               
               // juliana@222_9: Some string conversions to numerical values could return spourious values if the string range were greater than 
               // the type range.
               switch (field->fieldType) 
               {
                  case CHARS_TYPE:
                  case CHARS_NOCASE_TYPE:
                     newDefaultValue->asChars = (JCharP)TC_heapAlloc(heap, (i + 1) << 1);
                     xmemmove(newDefaultValue->asChars, defaultValue, (newDefaultValue->length = i) << 1);                     
                     break;
                  case SHORT_TYPE:
                     newDefaultValue->asShort = str2short(TC_JCharP2CharPBuf(defaultValue, i, doubleBuf), &error);
                     break;
                  case INT_TYPE:
                     newDefaultValue->asInt = TC_str2int(TC_JCharP2CharPBuf(defaultValue, i, doubleBuf), &error);
                     break;
                  case LONG_TYPE:
                     newDefaultValue->asLong = TC_str2long(TC_JCharP2CharPBuf(defaultValue, i, doubleBuf), &error);
                     break;
                  case FLOAT_TYPE:
                     newDefaultValue->asFloat = str2float(TC_JCharP2CharPBuf(defaultValue, i, doubleBuf), &error);
                     break;
                  case DOUBLE_TYPE:
                     newDefaultValue->asDouble = TC_str2double(TC_JCharP2CharPBuf(defaultValue, i, doubleBuf), &error);
                     break;
                  case DATE_TYPE: 
                  case DATETIME_TYPE:    
                     TC_JCharP2CharPBuf(defaultValue, i, doubleBuf);                 
                     if (!testAndPrepareDateAndTime(context, newDefaultValue, doubleBuf, type))
                        goto finish;
               } 
            }
            
            // Column indices.
            xmemmove(newIndices, table->columnIndexes, oldCount * TSIZE);
            table->columnIndexes = newIndices;
            
            // Sets the new plain db.
            xstrcpy(tempName, table->name);
            xstrcat(tempName, "_");
            xmemzero(&newDB, sizeof(PlainDB));
            xmemmove(&oldDB, plainDB, sizeof(PlainDB));
            if (!(createPlainDB(context, &newDB, tempName, true, useCrypto, table->sourcePath)))
               goto finish;
            newDB.isAscii = oldDB.isAscii; // juliana@210_2: now Litebase supports tables with ascii strings.
            newDB.headerSize = oldDB.headerSize;
            newBuffer = newDB.basbuf = TC_heapAlloc(heap, i = newOffsets[newCount] + ((newCount + 7) >> 3) + 4);
            plainSetRowSize(&newDB, i, newBuffer);
            newDB.rowInc = oldDB.rowCount;

            record[oldCount] = newDefaultValue; // Sets the record for the new column.
                   
#if defined(POSIX) || defined(ANDROID)
            removeFileFromList(&newDB.db);
            removeFileFromList(&newDB.dbo);
#endif

            // Saves the new meta data.
            xmemmove(plainDB, &newDB, sizeof(PlainDB));
            table->columnCount++;
            tableSaveMetaData(context, table, TSMD_EVERYTHING); 

            xmemmove(&newDB, plainDB, sizeof(PlainDB));
            xmemmove(plainDB, &oldDB, sizeof(PlainDB));
            table->columnCount--;
            
            size = oldDB.rowCount;
            
            i = oldCount;
            while (--i >= 0)
            {
               record[i] = (SQLValue*)TC_heapAlloc(parserHeap, sizeof(SQLValue));
               if (newTypes[i] == CHARS_TYPE || newTypes[i] == CHARS_NOCASE_TYPE)
				      record[i]->asChars = (JCharP)TC_heapAlloc(parserHeap, (newSizes[i] << 1) + 2); 
               else if (newTypes[i] == BLOB_TYPE)
				      record[i]->asBlob = (uint8*)TC_heapAlloc(parserHeap, newSizes[i]);
            }
            
            i = -1;  
            while (++i < size)
            {
               if (!readRecord(context, table, record, i, columnNulls, null, 0, false, null, null)) // juliana@220_3 juliana@227_20
               {
free:               
                  plainRemove(context, &newDB, table->sourcePath);
                  goto finish;
               }   
               j = -1;
               
               if (isNull)
                  columnNulls[oldCount >> 3] |= (1 << (oldCount & 7)); // Sets the column as null.
               
               while (++j < newCount)
                  if (!writeValue(context, &newDB, record[j], &newBuffer[newOffsets[j]], newTypes[j], newSizes[j], true, true, (columnNulls[j >> 3] & (1 << (j & 7))), false)) 
                     goto free;
               xmemmove(&newBuffer[newOffsets[j]], columnNulls, bytes); 
               
               // juliana@230_12: improved recover table to take .dbo data into consideration.
               // juliana@223_8: corrected a bug on purge that would not copy the crc32 codes for the rows.
               // juliana@220_4: added a crc32 code for every record. Please update your tables.
               k = newBuffer[3];
               newBuffer[3] = 0; // juliana@222_5: The crc was not being calculated correctly for updates.
               newDB.useOldCrypto = false;

               // Computes the crc for the record and stores at the end of the record.
               crc32 = updateCRC32(newBuffer, length, 0);
               if (table->version == VERSION_TABLE)
               {  
                  j = newCount;
                  while (--j > 0)
                     if ((newTypes[j] == CHARS_TYPE || newTypes[j] == CHARS_NOCASE_TYPE) 
                      && (columnNulls[j >> 3] & (1 << (j & 7))) == 0)
                     {
                        crc32 = updateCRC32((uint8*)record[j]->asChars, record[j]->length << 1, crc32);
                     }
                     else if (newTypes[j] == BLOB_TYPE && (columnNulls[j >> 3] & (1 << (j & 7))) == 0)
                     {  
                        blobLength = record[j]->length;
                        crc32 = updateCRC32((uint8*)&blobLength, 4, crc32);
                     }
               }
               xmove4(&newBuffer[length], &crc32); // Computes the crc for the record and stores at the end of the record.
               newBuffer[3] = k;
               
               if (!plainAdd(context, &newDB) || !plainWrite(context, &newDB))
                  goto free;
            }
            
            // Puts the new plain db in the table and deletes the old one.            
            newDB.rowInc = DEFAULT_ROW_INC;
            
            if (!plainRemove(context, &oldDB, table->sourcePath) || !plainRename(context, &newDB, table->name, table->sourcePath))
               goto finish;
            xstrcpy(newDB.name, table->name);
            xmemmove(&table->db, &newDB, sizeof(PlainDB));
            table->columnCount++;
            
            i = newCount;
            while (--i >= 0) // Recreates the simple indices.
               if (newIndices[i] && (newTypes[i] == CHARS_TYPE || newTypes[i] == CHARS_NOCASE_TYPE) && !tableReIndex(context, table, i, false, null))
                  goto finish;

            if ((i = table->numberComposedIndexes) > 0) // Recreates the composed indices.  
               while (--i >= 0)
                  if (!tableReIndex(context, table, -1, false, table->composedIndexes[i]))
                     goto finish;
         }
      }
   }

finish:
   heapDestroy(parserHeap);
}

// 
// This is how the attributes flow:
//
//  Insert:
//           new
//  update:
//           if (synced or updated) then updated
//           if (new)               then new
//  delete:
//           deleted
//////////////////////////////////////////////////////////////////////////
// juliana@225_14: RowIterator must throw an exception if its driver is closed.
/**
 * Gets the value of a column stored in the row iterator.
 *
 * @param p->obj[0] The row iterator.
 * @param p->i32[0] The column index.
 * @param type The type of the column.
 * @param p->retI Receives an int or a short.
 * @param p->retL Receives a long.
 * @param p->retD Receives a float or a double.
 * @param p->retO Receives a string, blob, date, or datetime.
 * @throws DriverException If the column is not of type requested.
 * @throws IllegalArgumentException If the column index is invalid.
 */
void getByIndex(NMParams p, int32 type)
{
	TRACE("getByIndex")
	
   if (testRIClosed(p)) // The row iterator and its driver can't be closed.
   {
      TCObject rowIterator = p->obj[0];
      Context context = p->currentContext;
      Table* table = getRowIteratorTable(rowIterator);   
      uint8* data = (uint8*)ARRAYOBJ_START(OBJ_RowIteratorData(rowIterator));
      int32 column = p->i32[0],
            i;
               
	   if (column < 0 || column >= table->columnCount) // Checks if the column index is within range.
      {
         TC_throwExceptionNamed(context, "java.lang.IllegalArgumentException", getMessage(ERR_INVALID_COLUMN_NUMBER), column);
         return;
      }

      if (isBitSet(table->columnNulls, column)) // juliana@223_5: now possible null values are treated in RowIterator.
      {
         p->retD = 0;
         p->retO = null;
         return;
      }

	   // If the column type is char nocase, the column is compatible with the char type.
	   if ((i = table->columnTypes[column]) == CHARS_NOCASE_TYPE)
		   i = CHARS_TYPE;
      
	   if (i != type) // The column type and the desired type must be compatible.
      {
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES));
         return;
      }

      switch (type)
      {
         case SHORT_TYPE: // juliana@227_18: corrected a possible insertion of a negative short column being recovered in the select as positive.
            p->retI = 0;
            xmove2(&p->retI, &data[table->columnOffsets[column]]);
            break;
         case INT_TYPE:
			   xmove4(&p->retI, &data[table->columnOffsets[column]]);
            break;
         case LONG_TYPE:
            xmove8(&p->retL, &data[table->columnOffsets[column]]);
            break;
         case FLOAT_TYPE:
			   {	
				   float floatNum;
				   xmove4(&floatNum, &data[table->columnOffsets[column]]);
				   p->retD = floatNum;
				   break;
			   }
         case DOUBLE_TYPE:
            READ_DOUBLE((uint8*)&p->retD, &data[table->columnOffsets[column]]);
            break;
         case BLOB_TYPE:
         {
            int32 size;
			   XFile* file = &table->db.dbo;

			   xmove4(&i, &data[table->columnOffsets[column]]);
            nfSetPos(file, i); // Finds the blob position in the .dbo.
   			
			   // Finds the blob size and creates the returning blob object.
			   if (!nfReadBytes(context, file, (uint8*)&size, 4)
			    || (!(p->retO = TC_createArrayObject(context, BYTE_ARRAY, size)))) 
               return;

            TC_setObjectLock(p->retO, UNLOCKED);

            if (!nfReadBytes(context, file, ARRAYOBJ_START(p->retO), size)) // Reads the blob.
               return;

            break;
         }
         case CHARS_TYPE:
         case CHARS_NOCASE_TYPE:
         {
            int32 size = 0;
			   XFile* file = &table->db.dbo;

            xmove4(&i, &data[table->columnOffsets[column]]); // Finds the string position in the .dbo.
			   nfSetPos(file, i); // Finds the string position in the .dbo.

            // Finds the string size and creates the returning string object.
            if (!nfReadBytes(context, file, (uint8*)&size, 2)
             || (!(p->retO = TC_createStringObjectWithLen(context, size)))) 
               return;

            TC_setObjectLock(p->retO, UNLOCKED);
            loadString(context, &table->db, String_charsStart(p->retO), size);
            break; 
         } 
         case DATE_TYPE:
         {
            int32 date;
			   xmove4(&date, &data[table->columnOffsets[column]]); // Reads the date.
			   setDateObject(p, date);
            break;
         }
         case DATETIME_TYPE:
         {
            uint8* buffer = &data[table->columnOffsets[column]];
            int32 date,
                  time;
            xmove4(&date, buffer);
            xmove4(&time, buffer + 4);
            setTimeObject(p, date, time); 
         }
      }
   }
}

/**
 * Tests if the row iterator or the driver where it was created is closed.
 *
 * @param p->obj[0] The row iterator object.
 * @throws IllegalStateException If the row iterator or driver is closed.
 */
bool testRIClosed(NMParams params)
{
   TRACE("testRIClosed")
   TCObject rowIterator = params->obj[0];

   if (OBJ_LitebaseDontFinalize(OBJ_RowIteratorDriver(rowIterator))) // The connection with Litebase can't be closed.
   {
      TC_throwExceptionNamed(params->currentContext, "java.lang.IllegalStateException", getMessage(ERR_DRIVER_CLOSED));
      return false;
   }
   if (!(getRowIteratorTable(rowIterator))) // Row iterator closed.
   {
      TC_throwExceptionNamed(params->currentContext, "java.lang.IllegalStateException", getMessage(ERR_ROWITERATOR_CLOSED));
      return false;
   }
   return true;
}

// juliana@226_2: corrected possible path problems on Windows CE and Palm.
/** 
 * Checks if the path passed as a parameter is valid and uses an internal path if it is null.
 *
 * @param context The thread context where the function is being executed.
 * @param sourcePath Receives the path that Litebase will use to store and access tables.
 * @param pathParam the path passed as a parameter.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException if the path passed as a parameter is invalid. 
 */
bool checkApppath(Context context, TCHARP sourcePath, TCHARP pathParam) // juliana@214_1
{
	TRACE("checkApppath")
   TCHAR buffer[MAX_PATHNAME];
   int32 endCh,
         lenAppPath,
         ret = 0;

   if (pathParam)
	{
	   // juliana@252_3: corrected a possible crash if the path had more than 255 characteres.
      if (tcslen(pathParam) + 1 > MAX_PATHNAME)
		{
         char pathCharP[1024];
         TC_TCHARP2CharPBuf(pathParam, pathCharP);
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), pathCharP);
		   return false;
		}   
      //tcscpy(sourcePath, tstrTrim(pathParam)); - guich: was crashing on LaudoMovel
      {TCHARP src = tstrTrim(pathParam); xmemmove(sourcePath, src, sizeof(TCHAR) * (tcslen(src)+1));}

		if (!sourcePath[0])
		{
			TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), "");
			return false;
		}
	}
   else // Since no path was passed by the user, gets it from dataPath or appPath. 
      getCurrentPath(sourcePath);

   tcscpy(buffer, tstrTrim(sourcePath));
   
   tcscpy(sourcePath, buffer);

// juliana@214_1: relative paths can't be used with Litebase.
   if (!TC_validatePath(sourcePath))
	{
      char pathCharP[1024];
      TC_TCHARP2CharPBuf(sourcePath, pathCharP);
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), pathCharP);
		return false;
	}

   // Creates the path folder if it does not exist.
   if (!sourcePath[0] || (sourcePath[0] && !lbfileExists(sourcePath) && (ret = lbfileCreateDir(sourcePath))))
   {
      char pathCharP[1024];

      TC_TCHARP2CharPBuf(sourcePath, pathCharP);
      if (!ret)
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), pathCharP);
      else
         fileError(context, ret, pathCharP);
		return false;
	}

   // Puts a '/' at the end of the path.
   lenAppPath = tcslen(sourcePath);
   endCh = lenAppPath > 0? sourcePath[lenAppPath - 1] : -1;
   if ((endCh != '\\' && endCh != '/') || endCh == -1)
      tcscat(sourcePath, TEXT("/"));

	return true;
}

/**
 * Verifies if the function can be applied to a data type field.
 * 
 * @param parameterDataType The data type of the function parameter.
 * @param sqlFunction The function code.
 * @return <code>true</code> If the function can be applied to a data type field; <code>false</code>, otherwise.
 */
bool bindFunctionDataType(int32 parameterDataType, int32 sqlFunction) // rnovais@568_10
{
   TRACE("bindFunctionDataType")

   int32 length = 7; // Maximum number of functions for a data type.
   int8* functions;

	if (parameterDataType < 0 || parameterDataType > DATETIME_TYPE || sqlFunction <= FUNCTION_DT_NONE || sqlFunction > FUNCTION_DT_LOWER)
		return false;
	
	functions = function_x_datatype[parameterDataType];

   while (--length >= 0)
      if (*functions++ == sqlFunction)
         return true; 
   return false;
}

/**
 * Returns the name of the data type function.
 *
 * @param sqlFunction The function code.
 * @return A string with the function name. 
 */
CharP dataTypeFunctionsName(int32 sqlFunction) // rnovais@568_10
{
	TRACE("dataTypeFunctionsName")
   if (sqlFunction <= FUNCTION_DT_LOWER && sqlFunction >= FUNCTION_DT_YEAR)
      return names[sqlFunction];
   return "";
}

/**
 * Checks if the driver is opened and another parameter is not null when they are sent as parameters in some native methods. 
 *
 * @param p->obj[0] The connection with Litebase.
 * @param p->obj[1] The parameter to be checked.
 * @param parameter The name of the parameter that can't be null.
 * @throws IllegalStateException If the driver is closed.
 * @throws NullPointerException If the table name is null.
 */
bool checkParamAndDriver(NMParams params, CharP parameter)
{
   if (OBJ_LitebaseDontFinalize(params->obj[0])) // The driver can't be closed.
   {
      TC_throwExceptionNamed(params->currentContext, "java.lang.IllegalStateException", getMessage(ERR_DRIVER_CLOSED));
      return false;
   }
   if (!params->obj[1]) // The parameter can't be null.
   {
      TC_throwNullArgumentException(params->currentContext, parameter);
      return false;
   }
   return true;
} 

/**
 * Encrypts or decrypts all the tables of a connection given from the application id.
 *
 * @param p->obj[0] The application id of the database.
 * @param p->obj[1] The path where the files are stored.
 * @throws DriverException If a file error occurs, not all the tables use the desired cryptography format.
 * @throws NullPointerException If one of the string parameters is null.
 */
void encDecTables(NMParams params, bool toEncrypt)
{
   TRACE("encDecTables")
   TCObject cridObj = params->obj[0],
          pathObj = params->obj[1];
          
   if (cridObj) 
      if (pathObj)
         if (String_charsLen(pathObj) >= MAX_PATHNAME - 4 - DBNAME_SIZE) // The path length can't be greater than the buffer size.
            TC_throwExceptionNamed(params->currentContext, "litebase.DriverException", getMessage(ERR_INVALID_PATH), "");
         else
         {
            TCHARPs* list = null;
            TCHAR pathStr[MAX_PATHNAME]; // juliana@230_6
            char cridStr[5],
                 value[DBNAME_SIZE];
            int32 i,
                  j,
                  k,
                  error,
                  count = 0,
                  encByte = toEncrypt? 0 : 1;
            Heap heap = heapCreate();
            TCHAR buffer[MAX_PATHNAME];
            NATIVE_FILE file;
            uint8 bytes[CACHE_INITIAL_SIZE];

            IF_HEAP_ERROR(heap)
            {
               TC_throwExceptionNamed(params->currentContext, "java.lang.OutOfMemoryError", null);
               
error:               
               heapDestroy(heap);
               return;
            }

            TC_JCharP2CharPBuf(String_charsStart(cridObj), String_charsLen(cridObj), cridStr);
            TC_JCharP2TCHARPBuf(String_charsStart(pathObj), String_charsLen(pathObj), pathStr);

            if ((error = TC_listFiles(pathStr, -1, &list, &count, heap, 0))) // Lists all the files of the folder. 
            {
               fileError(params->currentContext, error, "");
               goto error;
            } 
            
            // Before doing anything, checks if all the .db files are marked as decrypted or encrypted.
            i = count;
            while (--i >= 0)
            {
               TC_TCHARP2CharPBuf(list->value, value);
               if (xstrstr(value, cridStr) == value && xstrstr(value, ".db") && !xstrstr(value, ".dbo"))
               {
                  getFullFileName(value, pathStr, buffer);
                  if ((error = lbfileCreate(&file, buffer, READ_ONLY)))
                  {
fileError:
                     fileError(params->currentContext, error, value);
                     goto error;
                  }
                  if ((error = lbfileReadBytes(file, (CharP)bytes, 0, 4, &j)) || (error = lbfileClose(&file)))
                     goto fileError; 
                  if ((toEncrypt? bytes[0] != 0 : (bytes[0] != 1 && bytes[0] != 3)) || bytes[1] != 0 || bytes[2] != 0 || bytes[3] != 0)
                  {
                     TC_throwExceptionNamed(params->currentContext, "litebase.DriverException", getMessage(ERR_WRONG_CRYPTO_FORMAT));
                     goto error;
                  }
               }

               list = list->next;             
            }
            
            i = count;
            encByte = toEncrypt? 1 : 0;
            while (--i >= 0)
            {
      
               TC_TCHARP2CharPBuf(list->value, value);
               if (xstrstr(value, cridStr) == value)         
               {
                  getFullFileName(value, pathStr, buffer);
                  if ((error = lbfileCreate(&file, buffer, READ_WRITE)))
                     goto fileError;
                  
                  count = 0;
                  if (xstrstr(value, ".db")) // Changes the .db file crypto information.
                  {
                     if ((error = lbfileReadBytes(file, (CharP)bytes, 0, 4, &j)))
                        goto fileError;
                     bytes[0] = encByte;
                     if ((error = lbfileSetPos(file, 0)) || (error = lbfileWriteBytes(file, (CharP)bytes, 0, 4, &j)))
                        goto fileError;
                     count = 4;
                  }
                  
                  // Encrypts or decrypts all the table files data.
                  while (!(error = lbfileReadBytes(file, (CharP)bytes, 0, CACHE_INITIAL_SIZE, &j)) && j)
                  {
                     k = j;
                     while (--k >= 0)
                        bytes[k] ^= 0xAA;
                        
                     if ((error = lbfileSetPos(file, count)) || (error = lbfileWriteBytes(file, (CharP)bytes, 0, j, &j)))
                        goto fileError;
                     count += j;      
                  }
                  
                  if (error || (error = lbfileClose(&file)))
                  {
                     lbfileClose(&file);
                     goto fileError;
                  }
                     
               }
               
               list = list->next;   
            }
            
            heapDestroy(heap);        
         
         }
      else // The string argument can't be null.
         TC_throwNullArgumentException(params->currentContext, "sourcePath");
   else // The string argument can't be null.
      TC_throwNullArgumentException(params->currentContext, "crid");
   
}

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if <code>LibClose()</code> finished some structures.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(LibClose)
{
   UNUSED(currentContext)
   LibClose();

   // All Created drivers are closed.
   ASSERT1_EQUALS(Null, htCreatedDrivers.items);
   ASSERT1_EQUALS(Null, htCreatedDrivers.heap);
   ASSERT2_EQUALS(I32, htCreatedDrivers.size, 0); 

finish : ;
}

/**
 * Tests if <code>LibOpen()</code> initializes all the needed variables.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(LibOpen)
{
   int32 i = '@';
   TOpenParams openParams;

   openParams.currentContext = currentContext;
   openParams.getProcAddress = TC_getProcAddress;
   ASSERT1_EQUALS(True, LibOpen(&openParams));

   // The TCVM functions needed by Litebase.
   ASSERT1_EQUALS(NotNull, TC_CharP2JCharP);
   ASSERT1_EQUALS(NotNull, TC_CharP2JCharPBuf);
   ASSERT1_EQUALS(NotNull, TC_CharP2TCHARPBuf);
   ASSERT1_EQUALS(NotNull, TC_CharPToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharP);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharPBuf);
   ASSERT1_EQUALS(NotNull, TC_JCharP2TCHARPBuf);
	ASSERT1_EQUALS(NotNull, TC_JCharPEqualsJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPEqualsIgnoreCaseJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPHashCode);
   ASSERT1_EQUALS(NotNull, TC_JCharPIndexOfJChar);
	ASSERT1_EQUALS(NotNull, TC_JCharPLen);
   ASSERT1_EQUALS(NotNull, TC_JCharToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharToUpper);
   ASSERT1_EQUALS(NotNull, TC_TCHARP2CharPBuf);
   ASSERT1_EQUALS(NotNull, TC_alert);
   ASSERT1_EQUALS(NotNull, TC_appendCharP); // juliana@230_30
   ASSERT1_EQUALS(NotNull, TC_appendJCharP); // juliana@230_30
   ASSERT1_EQUALS(NotNull, TC_areClassesCompatible);
   ASSERT1_EQUALS(NotNull, TC_createArrayObject);
   ASSERT1_EQUALS(NotNull, TC_createObject);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectFromCharP);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectFromTCHARP);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectWithLen);
   ASSERT1_EQUALS(NotNull, TC_debug);
   ASSERT1_EQUALS(NotNull, TC_double2str);
   ASSERT1_EQUALS(NotNull, TC_executeMethod);
	ASSERT1_EQUALS(NotNull, TC_getApplicationId);
   ASSERT1_EQUALS(NotNull, TC_getAppPath);
   ASSERT1_EQUALS(NotNull, TC_getDataPath);
   ASSERT1_EQUALS(NotNull, TC_getDateTime);
	ASSERT1_EQUALS(NotNull, TC_getErrorMessage);
   ASSERT1_EQUALS(NotNull, TC_getSettingsPtr);
   ASSERT1_EQUALS(NotNull, TC_getTimeStamp);
   ASSERT1_EQUALS(NotNull, TC_hashCode);
   ASSERT1_EQUALS(NotNull, TC_hashCodeFmt);
   ASSERT1_EQUALS(NotNull, TC_heapAlloc);
   ASSERT1_EQUALS(NotNull, TC_heapDestroyPrivate);
   ASSERT1_EQUALS(NotNull, TC_hstrdup);
   ASSERT1_EQUALS(NotNull, TC_htFree);
   ASSERT1_EQUALS(NotNull, TC_htFreeContext);
   ASSERT1_EQUALS(NotNull, TC_htGet32);
   ASSERT1_EQUALS(NotNull, TC_htGet32Inv);
   ASSERT1_EQUALS(NotNull, TC_htGetPtr);
   ASSERT1_EQUALS(NotNull, TC_htNew);
   ASSERT1_EQUALS(NotNull, TC_htPut32);
   ASSERT1_EQUALS(NotNull, TC_htPut32IfNew);
   ASSERT1_EQUALS(NotNull, TC_htPutPtr);
   ASSERT1_EQUALS(NotNull, TC_htRemove);
   ASSERT1_EQUALS(NotNull, TC_int2CRID);
   ASSERT1_EQUALS(NotNull, TC_int2str);
   ASSERT1_EQUALS(NotNull, TC_listFiles);
   ASSERT1_EQUALS(NotNull, TC_loadClass);
   ASSERT1_EQUALS(NotNull, TC_long2str);
   ASSERT1_EQUALS(NotNull, TC_privateHeapCreate);
   ASSERT1_EQUALS(NotNull, TC_privateHeapSetJump);
   ASSERT1_EQUALS(NotNull, TC_privateXfree);
   ASSERT1_EQUALS(NotNull, TC_privateXmalloc);
   ASSERT1_EQUALS(NotNull, TC_privateXrealloc);
   ASSERT1_EQUALS(NotNull, TC_setObjectLock);
   ASSERT1_EQUALS(NotNull, TC_str2double);
   ASSERT1_EQUALS(NotNull, TC_str2int);
   ASSERT1_EQUALS(NotNull, TC_str2long);
   ASSERT1_EQUALS(NotNull, TC_throwExceptionNamed);
   ASSERT1_EQUALS(NotNull, TC_throwNullArgumentException);
   ASSERT1_EQUALS(NotNull, TC_tiF_create_sii);
   ASSERT1_EQUALS(NotNull, TC_toLower);
   ASSERT1_EQUALS(NotNull, TC_trace);
   ASSERT1_EQUALS(NotNull, TC_validatePath); // juliana@214_1

#ifdef ENABLE_MEMORY_TEST
   ASSERT1_EQUALS(NotNull, TC_getCountToReturnNull);
	ASSERT1_EQUALS(NotNull, TC_setCountToReturnNull);
#endif 

   // A hash table for the loaded connections.
   ASSERT1_EQUALS(NotNull, htCreatedDrivers.items);
   ASSERT1_EQUALS(Null, htCreatedDrivers.heap);
   ASSERT2_EQUALS(I32, htCreatedDrivers.size, 0);
   ASSERT2_EQUALS(I32, htCreatedDrivers.hash, 9);
   ASSERT2_EQUALS(I32, htCreatedDrivers.threshold, 10);    

   // A hash table for select statistics. 
   ASSERT1_EQUALS(NotNull, memoryUsage.items);
   ASSERT2_EQUALS(I32, memoryUsage.size, 0);
   ASSERT2_EQUALS(I32, memoryUsage.hash, 99);
   ASSERT2_EQUALS(I32, memoryUsage.threshold, 100);   

   // Error messages.
   // English messages.
   // General errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[0], "Error: "));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[1], " Near position %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[2], "Syntax error."));
   
	// Limit errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[3], "Maximum number of different fields was reached."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[4], "Maximum number of parameters in the 'WHERE/HAVING' clause was reached."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[5], "Maximum number of composed indices 32 was reached."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[6], "Table name too big: must be <= 23."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[7], "The maximum number of fields in a SELECT clause was exceeded."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[8], "Maximum number of columns exceeded in the 'ORDER BY/GROUP BY' clause."));

	// Column errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[9], "Unknown column %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[10], "Invalid column name: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[11], "Invalid column number: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[12], "The following column(s) does (do) not have an associated index %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[13], "Column name in field list is ambiguous: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[14], "Column not found %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[15], "Duplicated column name: %s."));
	
	// Primary key errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[16], "A primary key was already defined for this table."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[17], "Table does not have a primary key."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[18], "Statement creates a duplicated primary key in %s."));

   // Type errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[19], "Incompatible types."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[20], "Field size must be a positive interger value."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[21], "Value %s is not a valid number for the desired type: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[22], "Incompatible data type for the function call: %s"));

	// Number of fields errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[23], "The number of fields does not match the number of values "));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[24], "The given number of values does not match the table definition %d."));

   // Default value errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[25], "Length of default value is bigger than column size."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[26], "An added column declared as NOT NULL must have a not null default value."));

	// Driver errors. 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[27], "This driver instance was closed and cannot be used anymore. Please get a new instance of it."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[28], "ResultSet already closed!"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[29], "ResultSetMetaData cannot be used after the ResultSet is closed."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[30], "The application id must be four characters long."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[31], "The increment must be greater than 0 or -1."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[32], "Iterator already closed."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[33], "Prepared statement closed. Please prepare it again."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[34], "Invalid connection parameter: %s."));

   // Table errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[35], "Table name not found: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[36], "Table already created: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[37], "It is not possible to open a table within a connection with a different string format."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[38], "It is not possible to open a table within a connection with a different cryptography format."));

   // ROWID errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[39], "ROWID can't be changed by the user!"));

   // Prepared Statement errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[40], "Query does not return result set."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[41], "Query does not perform updates in the database."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[42], "Not all parameters of the query had their values defined."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[43], "A value was not defined for the parameter %d."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[44], "Invalid parameter index."));

	// Rename errors. 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[45], "Can't rename table. This table already exists: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[46], "Column already exists: %s."));

	// Alias errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[47], "Not unique table/alias: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[48], "This alias is already being used in this expression: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[49], "An alias is required for the aggregate function column."));

	// Litebase.execute() error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[50], "Only CREATE TABLE and CREATE INDEX can be used in Litebase.execute()."));
   
	// Order by and group by errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[51], "ORDER BY and GROUP BY clauses must match."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[52], "No support for virtual columns in SQL queries with GROUP BY clause."));
   
   // Function errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[53], "All non-aggregation function columns in the SELECT clause must also be in the GROUP BY clause."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[54], 
	 "%s is not an aggregation function. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation functions."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[55], 
                                                "Can't mix aggregation functions with real columns in the SELECT clause without a GROUP BY clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[56], "Can't have aggregation functions with ORDER BY clause and no GROUP BY clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[57], 
"%s was not listed in the SELECT clause. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation funtions."
));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[58], "SUM and AVG aggregation functions are not used with DATE and DATETIME type fields."));

   // DATE and DATETIME errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[59], "Value is not a DATE: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[60], "Value is not a DATETIME: %s."));

   // Index error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[61], "Index already created for column %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[62], "Can't drop a primary key index withdrop index."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[63], "Index too large. It can't have more than 65534 nodes."));
      
   // NOT NULL errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[64], "Primary key can't have null."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[65], "Field can't be null: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[66], "A parameter in a where clause can't be null."));

   // Result set errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[67], "ResultSet in invalid record position: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[68], "Invalid value for decimal places: %d. It must range from -1 to 40."));

   // File errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[69], "Can't read from table %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[70], "Can't load leaf node!"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[71], "Table is corrupted: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[72], "Table not closed properly: %s.")); // juliana@220_2
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[73], "A properly closed table can't be used in recoverTable(): %s.")); // juliana@222_2
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[74], "Can't find index record position on delete."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[75], "The table format (%d) is incompatible with Litebase version. Please update your tables."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[76], "The table format is not the previous one: %s.")); // juliana@220_11
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[77], "Invalid path: %s.")); // juliana@214_1
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[78], "Invalid file position: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[79], "Database not found."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[80], "An opened table can't be recovered or converted: %s."));
   
   // BLOB errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[81], "The total size of a blob can't be greater then 10 Mb."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[82], "This is not a valid size multiplier."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[83], "A blob type can't be part of a primary key."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[84], "A BLOB column can't be indexed."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[85], "A BLOB can't be in the where clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[86], "A BLOB can't be converted to a string."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[87], "Blobs types can't be in ORDER BY or GROUP BY clauses."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[88], "It is not possible to compare BLOBs."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[89], "It is only possible to insert or update a BLOB through prepared statements using setBlob()."));

   // Portuguese messages.
	// General errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[0], "Erro: "));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[1], " Pr�ximo � posi��o %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[2], "Erro de sintaxe."));

	// Limit errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[3], "N�mero m�ximo de campos diferentes foi alcan�ado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[4], "N�mero m�ximo da lista de par�metros na cl�usula 'WHERE/HAVING' foi alcan�ado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[5], "Numero m�ximo de �ndices compostos 32 foi alcan�ado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[6], "Nome da tabela muito grande: deve ser <= 23"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[7], "O n�mero m�ximo de campos na cl�usula SELECT foi excedido."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[8],"O n�mero m�ximo de campos na cl�usula 'ORDER BY/GROUP BY' foi excedido."));

   // Column errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[9], "Coluna desconhecida %s.")); 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[10], "Nome de coluna inv�lido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[11], "N�mero de coluna inv�lido: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[12], "A(s) coluna(s) a seguir n�o tem (t�m) um ind�ce associado %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[13], "Nome de coluna amb�guo: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[14], "Coluna n�o encontrada: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[15], "Nome de coluna duplicado: %s."));

   // Primary key errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[16], "Uma chave prim�ria j� foi definida para esta tabela."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[17], "Tabela n�o tem chave prim�ria."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[18], "Comando cria uma chave prim�ria duplicada em %s."));

   // Type errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[19], "Tipos incompativeis."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[20], "Tamanho do campo deve ser um valor inteiro positivo."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[21], "O valor %s n�o � um n�mero v�lido para o tipo desejado: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[22], "Tipo de dados incompat�vel para a chamada de fun��o: %s"));

	// Number of fields errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[23], "O n�mero de campos � diferente do n�mero de valores "));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[24], "O n�mero de valores dado n�o coincide com a defini��o da tabela %d."));

   // Default value errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[25], "Tamanho do valor padr�o � maior que o tamanho definido para a coluna."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[26], "Uma coluna adicionada declarada como NOT NULL deve ter um valor padr�o n�o nulo."));

	// Driver errors. 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[27], 
                                 "Esta inst�ncia do driver est� fechada e n�o pode ser mais utilizada. Por favor, obtenha uma nova inst�ncia."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[28], "ResultSet j� est� fechado!"));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[29], "ResultSetMetaData n�o pode ser usado depois que o ResultSet estiver fechado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[30], "O id da aplica��o de ter 4 characteres."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[31], "O incremento deve ser maior do que 0 ou -1."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[32], "Iterador j� foi fechado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[33], "Prepared statement fechado. Por favor, prepare-o novamente."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[34], "Par�metro de conex�o inv�lido: %s."));
   
	// Table errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[35], "Nome da tabela n�o encontrado: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[36], "Tabela j� existe: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[37], "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de strings diferente."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[38], "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de criptografia diferente.")); 

	// ROWID errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[39], "ROWID n�o pode ser mudado pelo usu�rio!"));

   // Prepared Statement errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[40], "Comando SQL n�o retorna um ResultSet."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[41], "Comando SQL n�o executa uma atualiza��o no banco de dados."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[42], "Nem todos os par�metros da consulta tiveram seus valores definidos."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[43], "N�o foi definido um valor para o par�metro %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[44], "Invalid parameter index."));
   
   // Rename errors. 
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[45], "N�o � poss�vel renomear a tabela. Esta tabela j� existe: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[46], "Coluna j� existe: %s."));

	// Alias errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[47], "Nome de tabela/alias repetido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[48], "Este alias j� est� sendo utilizado no sql: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[49], "Um alias � necess�rio para colunas com fun��o de agrega��o."));
   
	// Litebase.execute() error.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[50], "Apenas CREATE TABLE e CREATE INDEX s�o permitidos no Litebase.execute()"));
   
   // Order by and group by errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[51], "Cl�usulas ORDER BY e GROUP BY devem coincidir."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[52], "SQL com cl�usula GROUP BY n�o tem suporte para colunas virtuais."));
   
   // Function errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[53], 
	            "Todas colunas que n�os�o fun��es de agrega��o na cl�usula SELECT devem estar na cl�usula GROUP BY."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[54], 
	           "%s n�o � uma fun��o de agrega��o. Todos as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[55], "N�o � possivel misturar colunas reais e de agrega��o no SELECT sem cl�usula GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[56], "N�o � poss�vel ter fun��es de agrega��o com cl�usula ORDER BY sem cl�usula GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[57], 
	            "%s n�o foi listado no SELECT. Todas as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[58], "Fun��es de agrega��o SUM e AVG n�o s�o usadas com colunas do tipo DATE e DATETIME."));

   // DATE and DATETIME errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[59], "Valor n�o � um tipo DATE v�lido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[60], "Valor n�o � um tipo DATETIME v�lido: %s."));

   // Index error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[61], "�ndice j� criado para a coluna %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[62], "N�o � poss�vel remover uma chave prim�ria usando drop index."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[63], "�ndice muito grande. Ele n�o pode ter mais do que 65534 n�s."));
      
   // NOT NULL errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[64], "Chave prim�ria n�o pode ter NULL."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[65], "Coluna n�o pode ser NULL: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[66], "Um par�metro em uma where clause n�o pode ser NULL."));

   // Result set errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[67], "ResultSet em uma posi��o de registro inv�lida %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[68], "Valor inv�lido para casas decimais: %d. Deve ficar entre - 1 e 40."));

   // File errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[69], "N�o � poss�vel ler da tabela %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[70], "N�o � poss�vel carregar n� folha!"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[71], "Tabela est� corrompida: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[72], "Tabela n�o foi fechada corretamente: %s.")); // juliana@220_2
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[73], "Uma tabela fechada corretamente n�o pode ser usada no recoverTable(): %s.")); // juliana@222_2
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[74], "N�o � poss�vel achar a posi��o de registro no �ndice na exclus�o."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[75], 
                                 "O formato de tabela (%d) n�o � compat�vel com a vers�o do Litebase. Por favor, atualize suas tabelas."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[76], "O formato de tabela n�o � o anterior: %s.")); // juliana@220_11
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[77], "Caminho inv�lido: %s.")); // juliana@214_1
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[78], "Posi��o inv�lida no arquivo: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[79], "Base de dados n�o encontrada."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[80], "Uma tabela aberta n�o pode ser recuperada ou convertida: %s."));

   // BLOB errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[81], "O tamanho total de um BLOB n�o pode ser maior do que 10 Mb."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[82], "O multiplicador de tamanho n�o � v�lido."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[83], "Um tipo BLOB n�o pode ser parte de uma chave prim�ria."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[84], "Uma coluna do tipo BLOB n�o pode ser indexada."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[85], "Um BLOB n�o pode estar na cl�usula WHERE."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[86], "Um BLOB n�o pode ser convertido em uma string."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[87], "Tipos BLOB n�o podem estar em cl�usulas ORDER BY ou GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[88], "N�o � poss�vel comparar BLOBs."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[89], "S� � poss�vel inserir ou atualizar um BLOB atrav�s prepared statements usando setBlob()."));
   
   // Lex structures.
   while (++i < '[') // The values for the letters.
   {
      ASSERT2_EQUALS(I32, is[i] & IS_ALPHA, IS_ALPHA);
      ASSERT2_EQUALS(I32, is[i + 32] & IS_ALPHA, IS_ALPHA);
   }
   
   // Letters denoting types of numbers can also be the end of the number.
   ASSERT2_EQUALS(I32, is['d'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['D'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['f'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['F'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['l'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['L'] & IS_END_NUM, IS_END_NUM);
   
   // The values for the digits.
   i = '/';
   while (++i < ':') 
      ASSERT2_EQUALS(U8, is[i], IS_DIGIT);

   ASSERT2_EQUALS(I32, is['_'] & (IS_ALPHA | IS_DIGIT), (IS_ALPHA | IS_DIGIT)); // '_' can be part of an identifier.

   // + and - can be operators or sign of numbers.
   ASSERT2_EQUALS(U8, is['+'], IS_SIGN);
   ASSERT2_EQUALS(U8, is['-'], IS_SIGN);

   // The other operators and brackets.
   ASSERT2_EQUALS(U8, is['*'], IS_OPERATOR);
   ASSERT2_EQUALS(U8, is['('], IS_OPERATOR);
   ASSERT2_EQUALS(U8, is[')'], IS_OPERATOR);

   // The symbols that can represent double tokens.
   ASSERT2_EQUALS(U8, is['<'], IS_RELATIONAL);
   ASSERT2_EQUALS(U8, is['>'], IS_RELATIONAL);
   ASSERT2_EQUALS(U8, is['!'], IS_RELATIONAL);

   // The symbols that are treated as punctuators and =.
   ASSERT2_EQUALS(U8, is['.'], IS_PUNCT);
   ASSERT2_EQUALS(U8, is[','], IS_PUNCT);
   ASSERT2_EQUALS(U8, is['?'], IS_PUNCT);
   ASSERT2_EQUALS(U8, is['='], IS_PUNCT);

   // Checks if the hash codes of the reserved words are in the hash table.
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("abs")), TK_ABS); 
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("add")), TK_ADD);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("alter")), TK_ALTER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("and")), TK_AND);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("as")), TK_AS);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("asc")), TK_ASC);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("avg")), TK_AVG);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("blob")), TK_BLOB);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("by")), TK_BY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("char")), TK_CHAR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("count")), TK_COUNT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("create")), TK_CREATE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("date")), TK_DATE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("datetime")), TK_DATETIME);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("day")), TK_DAY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("default")), TK_DEFAULT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("delete")), TK_DELETE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("desc")), TK_DESC);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("distinct")), TK_DISTINCT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("double")), TK_DOUBLE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("drop")), TK_DROP);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("float")), TK_FLOAT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("from")), TK_FROM);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("group")), TK_GROUP);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("having")), TK_HAVING);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("hour")), TK_HOUR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("index")), TK_INDEX);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("insert")), TK_INSERT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("int")), TK_INT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("into")), TK_INTO);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("is")), TK_IS);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("key")), TK_KEY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("like")), TK_LIKE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("long")), TK_LONG);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("lower")), TK_LOWER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("max")), TK_MAX);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("millis")), TK_MILLIS);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("min")), TK_MIN);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("minute")), TK_MINUTE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("month")), TK_MONTH);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("nocase")), TK_NOCASE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("not")), TK_NOT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("null")), TK_NULL) ;
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("on")), TK_ON);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("or")), TK_OR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("order")), TK_ORDER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("primary")), TK_PRIMARY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("rename")), TK_RENAME);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("second")), TK_SECOND);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("select")), TK_SELECT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("set")), TK_SET);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("short")), TK_SHORT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("sum")), TK_SUM);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("table")), TK_TABLE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("to")), TK_TO);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("update")), TK_UPDATE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("upper")), TK_UPPER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("values")), TK_VALUES);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("varchar")), TK_VARCHAR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("where")), TK_WHERE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("year")), TK_YEAR);

   // Classes.
   ASSERT1_EQUALS(NotNull, litebaseConnectionClass);
   ASSERT1_EQUALS(NotNull, loggerClass);

   ASSERT1_EQUALS(True, ranTests); // Enables the test cases.

finish: ;
}

/**
 * Tests the function <code>bindFunctionDataType()</code> works properly. It is tested with all possible data types and data function types, including 
 * invalid values.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(bindFunctionDataType)
{
   UNUSED(currentContext)

   // Tests UNDEFINED type, which cannot be bound.
   ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(UNDEFINED_TYPE, FUNCTION_DT_LOWER + 1));

	// Tests CHAR type, which is only used for UPPER and LOWER.
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(True, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(True, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_TYPE, FUNCTION_DT_LOWER + 1));

	// Tests SHORT type, which is only used for ABS.
	ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(True, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(SHORT_TYPE, FUNCTION_DT_LOWER + 1));

	// Tests INT type, which is only used for ABS.
	ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(True, bindFunctionDataType(INT_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(INT_TYPE, FUNCTION_DT_LOWER + 1));

	// Tests LONG type, which is only used for ABS.
	ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(True, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(LONG_TYPE, FUNCTION_DT_LOWER + 1));

	// Tests FLOAT type, which is only used for ABS.
   ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(True, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(FLOAT_TYPE, FUNCTION_DT_LOWER + 1));

	// Tests DOUBLE type, which is only used for ABS.
   ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(True, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(DOUBLE_TYPE, FUNCTION_DT_LOWER + 1));

	// Tests CHARS_NOCASE type, which is only used for UPPER and LOWER.
   ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(True, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(True, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(CHARS_NOCASE_TYPE, FUNCTION_DT_LOWER + 1));

   // Tests BOOLEAN type, which cannot be bound.
   ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(BOOLEAN_TYPE, FUNCTION_DT_LOWER + 1));

	// Tests DATE type, which is only used for YEAR, MONTH, and DAY.
   ASSERT1_EQUALS(False, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(True, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(True, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(True, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATE_TYPE, FUNCTION_DT_LOWER + 1));

	// Tests DATETIME type, which is only used for YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, and MILLIS. 
   ASSERT1_EQUALS(False, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(True, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(True, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(True, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(True, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(True, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(True, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(True, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(DATETIME_TYPE, FUNCTION_DT_LOWER + 1));
 
   // Tests BLOB type, which cannot be bound.
   ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_NONE));
	ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_UPPER));
	ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_LOWER));
	ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_ABS));
   ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_YEAR));
   ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_MONTH));
   ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_DAY));
	ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_HOUR));
	ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_MINUTE));
	ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_SECOND));
	ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_MILLIS));
	ASSERT1_EQUALS(False, bindFunctionDataType(BLOB_TYPE, FUNCTION_DT_LOWER + 1));

finish: ;  
}

/**
 * Tests if the function <code>checkApppath()</code> accepts paths if and only if they are valid.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(checkApppath)
{
	char sourcePath[MAX_PATHNAME];
	char defaultAppPath[MAX_PATHNAME];
	char path[300];
   int32 len;

   TC_getDataPath(defaultAppPath);
	if (!defaultAppPath[0])
		xstrcpy(defaultAppPath, TC_getAppPath());
	len = xstrlen(defaultAppPath);
	if (defaultAppPath[len - 1] != '\\' && defaultAppPath[len - 1] != '/')
		xstrcat(defaultAppPath, "/");

	// null.
	ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, null)); 
   ASSERT2_EQUALS(Sz, sourcePath, defaultAppPath);

	// Empty string.
	path[0] = 0;
   ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path)); 

	// Just spaces.
	xstrcpy(path, " ");  
	ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));
	xstrcpy(path, "  "); 
	ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));

	// Relative paths.
	xstrcpy(path, ".");
   ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));
   xstrcpy(path, "..");
   ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));
	xstrcpy(path, "./");
   ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));
   xstrcpy(path, "../");
   ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));
	xstrcpy(path, "/.");
   ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));
   xstrcpy(path, "/..");
   ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));
   xstrcpy(path, "/Litebase/../tables/");
   ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));

	// Correct paths.
   ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, defaultAppPath));
	ASSERT2_EQUALS(Sz, sourcePath, defaultAppPath);
   xstrcpy(path, "/"); 
   ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, path));
   ASSERT2_EQUALS(Sz, sourcePath, "/");
   xstrcpy(path, "\\"); 
   ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, path));
   ASSERT2_EQUALS(Sz, sourcePath, "/");
#if !defined(POSIX) && !defined(ANDROID)
	xstrcpy(path, "\\temp\\tables");
   ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, path)); 
   ASSERT2_EQUALS(Sz, sourcePath, "/temp/tables/");
   xstrcpy(path, "/temp/tables/"); 
   ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, path));
   ASSERT2_EQUALS(Sz, sourcePath, "/temp/tables/"); 
#endif
#ifdef WIN32
	xstrcpy(path, "p:\\temp\\tables/"); 
	ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, path));
   ASSERT2_EQUALS(Sz, sourcePath, "p:/temp/tables/");
   xstrcpy(path, "p:/temp/tables/"); 
	ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, path));
   ASSERT2_EQUALS(Sz, sourcePath, "p:/temp/tables/");
#endif

finish: ;
}

/**
 * Tests if the function <code>dataTypeFunctionsName()</code> returns the strings with the correct types.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(dataTypeFunctionsName)
{
   UNUSED(currentContext)
	ASSERT2_EQUALS(Sz, "", dataTypeFunctionsName(FUNCTION_DT_NONE));
	ASSERT2_EQUALS(Sz, "year", dataTypeFunctionsName(FUNCTION_DT_YEAR));
	ASSERT2_EQUALS(Sz, "month", dataTypeFunctionsName(FUNCTION_DT_MONTH));
	ASSERT2_EQUALS(Sz, "day", dataTypeFunctionsName(FUNCTION_DT_DAY));
	ASSERT2_EQUALS(Sz, "hour", dataTypeFunctionsName(FUNCTION_DT_HOUR));
	ASSERT2_EQUALS(Sz, "minute", dataTypeFunctionsName(FUNCTION_DT_MINUTE));
	ASSERT2_EQUALS(Sz, "second", dataTypeFunctionsName(FUNCTION_DT_SECOND));
	ASSERT2_EQUALS(Sz, "millis", dataTypeFunctionsName(FUNCTION_DT_MILLIS));
	ASSERT2_EQUALS(Sz, "abs", dataTypeFunctionsName(FUNCTION_DT_ABS));
   ASSERT2_EQUALS(Sz, "upper", dataTypeFunctionsName(FUNCTION_DT_UPPER));
	ASSERT2_EQUALS(Sz, "lower", dataTypeFunctionsName(FUNCTION_DT_LOWER));
	ASSERT2_EQUALS(Sz, "", dataTypeFunctionsName(FUNCTION_DT_LOWER + 1));

finish: ;
}

/**
 * Tests if <code>initVars()</code> initializes all the needed variables.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(initVars)
{
   int32 i = '@';
   UNUSED(currentContext)

   // The TCVM functions needed by Litebase.
   ASSERT1_EQUALS(NotNull, TC_CharP2JCharP);
   ASSERT1_EQUALS(NotNull, TC_CharP2JCharPBuf);
   ASSERT1_EQUALS(NotNull, TC_CharP2TCHARPBuf);
   ASSERT1_EQUALS(NotNull, TC_CharPToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharP);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharPBuf);
   ASSERT1_EQUALS(NotNull, TC_JCharP2TCHARPBuf);
	ASSERT1_EQUALS(NotNull, TC_JCharPEqualsJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPEqualsIgnoreCaseJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPHashCode);
   ASSERT1_EQUALS(NotNull, TC_JCharPIndexOfJChar);
	ASSERT1_EQUALS(NotNull, TC_JCharPLen);
   ASSERT1_EQUALS(NotNull, TC_JCharToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharToUpper);
   ASSERT1_EQUALS(NotNull, TC_TCHARP2CharPBuf);
   ASSERT1_EQUALS(NotNull, TC_alert);
   ASSERT1_EQUALS(NotNull, TC_appendCharP); // juliana@230_30
   ASSERT1_EQUALS(NotNull, TC_appendJCharP); // juliana@230_30
   ASSERT1_EQUALS(NotNull, TC_areClassesCompatible);
   ASSERT1_EQUALS(NotNull, TC_createArrayObject);
   ASSERT1_EQUALS(NotNull, TC_createObject);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectFromCharP);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectFromTCHARP);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectWithLen);
   ASSERT1_EQUALS(NotNull, TC_debug);
   ASSERT1_EQUALS(NotNull, TC_double2str);
   ASSERT1_EQUALS(NotNull, TC_executeMethod);
	ASSERT1_EQUALS(NotNull, TC_getApplicationId);
   ASSERT1_EQUALS(NotNull, TC_getAppPath);
   ASSERT1_EQUALS(NotNull, TC_getDataPath);
   ASSERT1_EQUALS(NotNull, TC_getDateTime);
	ASSERT1_EQUALS(NotNull, TC_getErrorMessage);
   ASSERT1_EQUALS(NotNull, TC_getSettingsPtr);
   ASSERT1_EQUALS(NotNull, TC_getTimeStamp);
   ASSERT1_EQUALS(NotNull, TC_hashCode);
   ASSERT1_EQUALS(NotNull, TC_hashCodeFmt);
   ASSERT1_EQUALS(NotNull, TC_heapAlloc);
   ASSERT1_EQUALS(NotNull, TC_heapDestroyPrivate);
   ASSERT1_EQUALS(NotNull, TC_hstrdup);
   ASSERT1_EQUALS(NotNull, TC_htFree);
   ASSERT1_EQUALS(NotNull, TC_htFreeContext);
   ASSERT1_EQUALS(NotNull, TC_htGet32);
   ASSERT1_EQUALS(NotNull, TC_htGet32Inv);
   ASSERT1_EQUALS(NotNull, TC_htGetPtr);
   ASSERT1_EQUALS(NotNull, TC_htNew);
   ASSERT1_EQUALS(NotNull, TC_htPut32);
   ASSERT1_EQUALS(NotNull, TC_htPut32IfNew);
   ASSERT1_EQUALS(NotNull, TC_htPutPtr);
   ASSERT1_EQUALS(NotNull, TC_htRemove);
   ASSERT1_EQUALS(NotNull, TC_int2CRID);
   ASSERT1_EQUALS(NotNull, TC_int2str);
   ASSERT1_EQUALS(NotNull, TC_listFiles);
   ASSERT1_EQUALS(NotNull, TC_loadClass);
   ASSERT1_EQUALS(NotNull, TC_long2str);
   ASSERT1_EQUALS(NotNull, TC_privateHeapCreate);
   ASSERT1_EQUALS(NotNull, TC_privateHeapSetJump);
   ASSERT1_EQUALS(NotNull, TC_privateXfree);
   ASSERT1_EQUALS(NotNull, TC_privateXmalloc);
   ASSERT1_EQUALS(NotNull, TC_privateXrealloc);
   ASSERT1_EQUALS(NotNull, TC_setObjectLock);
   ASSERT1_EQUALS(NotNull, TC_str2double);
   ASSERT1_EQUALS(NotNull, TC_str2int);
   ASSERT1_EQUALS(NotNull, TC_str2long);
   ASSERT1_EQUALS(NotNull, TC_throwExceptionNamed);
   ASSERT1_EQUALS(NotNull, TC_throwNullArgumentException);
   ASSERT1_EQUALS(NotNull, TC_tiF_create_sii);
   ASSERT1_EQUALS(NotNull, TC_toLower);
   ASSERT1_EQUALS(NotNull, TC_trace);
   ASSERT1_EQUALS(NotNull, TC_validatePath); // juliana@214_1

#ifdef ENABLE_MEMORY_TEST
   ASSERT1_EQUALS(NotNull, TC_getCountToReturnNull);
	ASSERT1_EQUALS(NotNull, TC_setCountToReturnNull);
#endif 

   // A hash table for the loaded connections.
   ASSERT1_EQUALS(NotNull, htCreatedDrivers.items);
   ASSERT1_EQUALS(Null, htCreatedDrivers.heap);
   ASSERT2_EQUALS(I32, htCreatedDrivers.size, 0);
   ASSERT2_EQUALS(I32, htCreatedDrivers.hash, 9);
   ASSERT2_EQUALS(I32, htCreatedDrivers.threshold, 10);    

   // A hash table for select statistics. 
   ASSERT1_EQUALS(NotNull, memoryUsage.items);
   ASSERT2_EQUALS(I32, memoryUsage.size, 0);
   ASSERT2_EQUALS(I32, memoryUsage.hash, 99);
   ASSERT2_EQUALS(I32, memoryUsage.threshold, 100);   

   // Error messages.
   // English messages.
   // General errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[0], "Error: "));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[1], " Near position %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[2], "Syntax error."));
   
	// Limit errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[3], "Maximum number of different fields was reached."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[4], "Maximum number of parameters in the 'WHERE/HAVING' clause was reached."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[5], "Maximum number of composed indices 32 was reached."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[6], "Table name too big: must be <= 23."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[7], "The maximum number of fields in a SELECT clause was exceeded."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[8], "Maximum number of columns exceeded in the 'ORDER BY/GROUP BY' clause."));

	// Column errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[9], "Unknown column %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[10], "Invalid column name: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[11], "Invalid column number: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[12], "The following column(s) does (do) not have an associated index %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[13], "Column name in field list is ambiguous: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[14], "Column not found %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[15], "Duplicated column name: %s."));
	
	// Primary key errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[16], "A primary key was already defined for this table."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[17], "Table does not have a primary key."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[18], "Statement creates a duplicated primary key in %s."));

   // Type errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[19], "Incompatible types."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[20], "Field size must be a positive interger value."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[21], "Value %s is not a valid number for the desired type: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[22], "Incompatible data type for the function call: %s"));

	// Number of fields errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[23], "The number of fields does not match the number of values "));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[24], "The given number of values does not match the table definition %d."));

   // Default value errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[25], "Length of default value is bigger than column size."));

	// Driver errors. 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[26], "This driver instance was closed and cannot be used anymore. Please get a new instance of it."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[27], "ResultSet already closed!"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[28], "ResultSetMetaData cannot be used after the ResultSet is closed."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[29], "The application id must be four characters long."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[30], "The increment must be greater than 0 or -1."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[31], "Iterator already closed."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[32], "Prepared statement closed. Please prepare it again."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[33], "Invalid connection parameter: %s."));

   // Table errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[34], "Table name not found: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[35], "Table already created: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[36], "It is not possible to open a table within a connection with a different string format."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[37], "It is not possible to open a table within a connection with a different cryptography format."));

   // ROWID errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[38], "ROWID can't be changed by the user!"));

   // Prepared Statement errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[39], "Query does not return result set."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[40], "Query does not perform updates in the database."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[41], "Not all parameters of the query had their values defined."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[42], "A value was not defined for the parameter %d."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[43], "Invalid parameter index."));

	// Rename errors. 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[44], "Can't rename table. This table already exists: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[45], "Column already exists: %s."));

	// Alias errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[46], "Not unique table/alias: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[47], "This alias is already being used in this expression: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[48], "An alias is required for the aggregate function column."));

	// Litebase.execute() error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[49], "Only CREATE TABLE and CREATE INDEX can be used in Litebase.execute()."));
   
	// Order by and group by errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[50], "ORDER BY and GROUP BY clauses must match."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[51], "No support for virtual columns in SQL queries with GROUP BY clause."));
   
   // Function errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[52], "All non-aggregation function columns in the SELECT clause must also be in the GROUP BY clause."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[53], 
	 "%s is not an aggregation function. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation functions."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[54], 
                                                "Can't mix aggregation functions with real columns in the SELECT clause without a GROUP BY clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[55], "Can't have aggregation functions with ORDER BY clause and no GROUP BY clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[56], 
"%s was not listed in the SELECT clause. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation funtions."
));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[57], "SUM and AVG aggregation functions are not used with DATE and DATETIME type fields."));

   // DATE and DATETIME errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[58], "Value is not a DATE: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[59], "Value is not a DATETIME: %s."));

   // Index error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[60], "Index already created for column %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[61], "Can't drop a primary key index withdrop index."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[62], "Index too large. It can't have more than 65534 nodes."));
      
   // NOT NULL errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[63], "Primary key can't have null."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[64], "Field can't be null: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[65], "A parameter in a where clause can't be null."));

   // Result set errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[66], "ResultSet in invalid record position: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[67], "Invalid value for decimal places: %d. It must range from -1 to 40."));

   // File errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[68], "Can't read from table %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[69], "Can't load leaf node!"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[70], "Table is corrupted: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[71], "Table not closed properly: %s.")); // juliana@220_2
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[72], "A properly closed table can't be used in recoverTable(): %s.")); // juliana@222_2
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[73], "Can't find index record position on delete."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[74], "The table format (%d) is incompatible with Litebase version. Please update your tables."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[75], "The table format is not the previous one: %s.")); // juliana@220_11
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[76], "Invalid path: %s.")); // juliana@214_1
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[77], "Invalid file position: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[78], "Database not found."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[79], "An opened table can't be recovered or converted: %s."));
   
   // BLOB errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[80], "The total size of a blob can't be greater then 10 Mb."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[81], "This is not a valid size multiplier."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[82], "A blob type can't be part of a primary key."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[83], "A BLOB column can't be indexed."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[84], "A BLOB can't be in the where clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[85], "A BLOB can't be converted to a string."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[86], "Blobs types can't be in ORDER BY or GROUP BY clauses."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[87], "It is not possible to compare BLOBs."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[88], "It is only possible to insert or update a BLOB through prepared statements using setBlob()."));

   // Portuguese messages.
	// General errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[0], "Erro: "));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[1], " Pr�ximo � posi��o %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[2], "Erro de sintaxe."));

	// Limit errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[3], "N�mero m�ximo de campos diferentes foi alcan�ado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[4], "N�mero m�ximo da lista de par�metros na cl�usula 'WHERE/HAVING' foi alcan�ado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[5], "Numero m�ximo de �ndices compostos 32 foi alcan�ado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[6], "Nome da tabela muito grande: deve ser <= 23"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[7], "O n�mero m�ximo de campos na cl�usula SELECT foi excedido."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[8],"O n�mero m�ximo de campos na cl�usula 'ORDER BY/GROUP BY' foi excedido."));

   // Column errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[9], "Coluna desconhecida %s.")); 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[10], "Nome de coluna inv�lido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[11], "N�mero de coluna inv�lido: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[12], "A(s) coluna(s) a seguir n�o tem (t�m) um ind�ce associado %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[13], "Nome de coluna amb�guo: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[14], "Coluna n�o encontrada: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[15], "Nome de coluna duplicado: %s."));

   // Primary key errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[16], "Uma chave prim�ria j� foi definida para esta tabela."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[17], "Tabela n�o tem chave prim�ria."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[18], "Comando cria uma chave prim�ria duplicada em %s."));

   // Type errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[19], "Tipos incompativeis."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[20], "Tamanho do campo deve ser um valor inteiro positivo."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[21], "O valor %s n�o � um n�mero v�lido para o tipo desejado: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[22], "Tipo de dados incompat�vel para a chamada de fun��o: %s"));

	// Number of fields errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[23], "O n�mero de campos � diferente do n�mero de valores "));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[24], "O n�mero de valores dado n�o coincide com a defini��o da tabela %d."));

   // Default value errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[25], "Tamanho do valor padr�o � maior que o tamanho definido para a coluna."));

	// Driver errors. 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[26], 
                                 "Esta inst�ncia do driver est� fechada e n�o pode ser mais utilizada. Por favor, obtenha uma nova inst�ncia."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[27], "ResultSet j� est� fechado!"));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[28], "ResultSetMetaData n�o pode ser usado depois que o ResultSet estiver fechado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[29], "O id da aplica��o de ter 4 characteres."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[30], "O incremento deve ser maior do que 0 ou -1."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[31], "Iterador j� foi fechado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[32], "Prepared statement fechado. Por favor, prepare-o novamente."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[33], "Par�metro de conex�o inv�lido: %s."));

	// Table errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[34], "Nome da tabela n�o encontrado: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[35], "Tabela j� existe: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[36], "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de strings diferente."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[37], "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de criptografia diferente."));

	// ROWID errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[38], "ROWID n�o pode ser mudado pelo usu�rio!"));

   // Prepared Statement errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[39], "Comando SQL n�o retorna um ResultSet."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[40], "Comando SQL n�o executa uma atualiza��o no banco de dados."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[41], "Nem todos os par�metros da consulta tiveram seus valores definidos."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[42], "N�o foi definido um valor para o par�metro %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[43], "Invalid parameter index."));
   
   // Rename errors. 
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[44], "N�o � poss�vel renomear a tabela. Esta tabela j� existe: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[45], "Coluna j� existe: %s."));

	// Alias errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[46], "Nome de tabela/alias repetido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[47], "Este alias j� est� sendo utilizado no sql: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[48], "Um alias � necess�rio para colunas com fun��o de agrega��o."));
   
	// Litebase.execute() error.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[49], "Apenas CREATE TABLE e CREATE INDEX s�o permitidos no Litebase.execute()"));
   
   // Order by and group by errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[50], "Cl�usulas ORDER BY e GROUP BY devem coincidir."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[51], "SQL com cl�usula GROUP BY n�o tem suporte para colunas virtuais."));
   
   // Function errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[52], 
	            "Todas colunas que n�os�o fun��es de agrega��o na cl�usula SELECT devem estar na cl�usula GROUP BY."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[53], 
	           "%s n�o � uma fun��o de agrega��o. Todos as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[54], "N�o � possivel misturar colunas reais e de agrega��o no SELECT sem cl�usula GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[55], "N�o � poss�vel ter fun��es de agrega��o com cl�usula ORDER BY sem cl�usula GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[56], 
	            "%s n�o foi listado no SELECT. Todas as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[57], "Fun��es de agrega��o SUM e AVG n�o s�o usadas com colunas do tipo DATE e DATETIME."));

   // DATE and DATETIME errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[58], "Valor n�o � um tipo DATE v�lido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[59], "Valor n�o � um tipo DATETIME v�lido: %s."));

   // Index error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[60], "�ndice j� criado para a coluna %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[61], "N�o � poss�vel remover uma chave prim�ria usando drop index."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[62], "�ndice muito grande. Ele n�o pode ter mais do que 65534 n�s."));
      
   // NOT NULL errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[63], "Chave prim�ria n�o pode ter NULL."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[64], "Coluna n�o pode ser NULL: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[65], "Um par�metro em uma where clause n�o pode ser NULL."));

   // Result set errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[66], "ResultSet em uma posi��o de registro inv�lida %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[67], "Valor inv�lido para casas decimais: %d. Deve ficar entre - 1 e 40."));

   // File errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[68], "N�o � poss�vel ler da tabela %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[69], "N�o � poss�vel carregar n� folha!"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[70], "Tabela est� corrompida: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[71], "Tabela n�o foi fechada corretamente: %s.")); // juliana@220_2
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[72], "Uma tabela fechada corretamente n�o pode ser usada no recoverTable(): %s.")); // juliana@222_2
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[73], "N�o � poss�vel achar a posi��o de registro no �ndice na exclus�o."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[74], 
                                 "O formato de tabela (%d) n�o � compat�vel com a vers�o do Litebase. Por favor, atualize suas tabelas."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[75], "O formato de tabela n�o � o anterior: %s.")); // juliana@220_11
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[76], "Caminho inv�lido: %s.")); // juliana@214_1
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[77], "Posi��o inv�lida no arquivo: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[78], "Base de dados n�o encontrada."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[79], "Uma tabela aberta n�o pode ser recuperada ou convertida: %s."));

   // BLOB errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[80], "O tamanho total de um BLOB n�o pode ser maior do que 10 Mb."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[81], "O multiplicador de tamanho n�o � v�lido."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[82], "Um tipo BLOB n�o pode ser parte de uma chave prim�ria."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[83], "Uma coluna do tipo BLOB n�o pode ser indexada."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[84], "Um BLOB n�o pode estar na cl�usula WHERE."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[85], "Um BLOB n�o pode ser convertido em uma string."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[86], "Tipos BLOB n�o podem estar em cl�usulas ORDER BY ou GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[87], "N�o � poss�vel comparar BLOBs."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[88], "S� � poss�vel inserir ou atualizar um BLOB atrav�s prepared statements usando setBlob()."));
   
   // Lex structures.
   while (++i < '[') // The values for the letters.
   {
      ASSERT2_EQUALS(I32, is[i] & IS_ALPHA, IS_ALPHA);
      ASSERT2_EQUALS(I32, is[i + 32] & IS_ALPHA, IS_ALPHA);
   }
   
   // Letters denoting types of numbers can also be the end of the number.
   ASSERT2_EQUALS(I32, is['d'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['D'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['f'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['F'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['l'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['L'] & IS_END_NUM, IS_END_NUM);
   
   // The values for the digits.
   i = '/';
   while (++i < ':') 
      ASSERT2_EQUALS(U8, is[i], IS_DIGIT);

   ASSERT2_EQUALS(I32, is['_'] & (IS_ALPHA | IS_DIGIT), (IS_ALPHA | IS_DIGIT)); // '_' can be part of an identifier.

   // + and - can be operators or sign of numbers.
   ASSERT2_EQUALS(U8, is['+'], IS_SIGN);
   ASSERT2_EQUALS(U8, is['-'], IS_SIGN);

   // The other operators and brackets.
   ASSERT2_EQUALS(U8, is['*'], IS_OPERATOR);
   ASSERT2_EQUALS(U8, is['('], IS_OPERATOR);
   ASSERT2_EQUALS(U8, is[')'], IS_OPERATOR);

   // The symbols that can represent double tokens.
   ASSERT2_EQUALS(U8, is['<'], IS_RELATIONAL);
   ASSERT2_EQUALS(U8, is['>'], IS_RELATIONAL);
   ASSERT2_EQUALS(U8, is['!'], IS_RELATIONAL);

   // The symbols that are treated as punctuators and =.
   ASSERT2_EQUALS(U8, is['.'], IS_PUNCT);
   ASSERT2_EQUALS(U8, is[','], IS_PUNCT);
   ASSERT2_EQUALS(U8, is['?'], IS_PUNCT);
   ASSERT2_EQUALS(U8, is['='], IS_PUNCT);

   // Checks if the hash codes of the reserved words are in the hash table.
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("abs")), TK_ABS); 
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("add")), TK_ADD);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("alter")), TK_ALTER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("and")), TK_AND);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("as")), TK_AS);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("asc")), TK_ASC);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("avg")), TK_AVG);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("blob")), TK_BLOB);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("by")), TK_BY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("char")), TK_CHAR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("count")), TK_COUNT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("create")), TK_CREATE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("date")), TK_DATE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("datetime")), TK_DATETIME);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("day")), TK_DAY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("default")), TK_DEFAULT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("delete")), TK_DELETE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("desc")), TK_DESC);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("distinct")), TK_DISTINCT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("double")), TK_DOUBLE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("drop")), TK_DROP);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("float")), TK_FLOAT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("from")), TK_FROM);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("group")), TK_GROUP);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("having")), TK_HAVING);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("hour")), TK_HOUR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("index")), TK_INDEX);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("insert")), TK_INSERT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("int")), TK_INT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("into")), TK_INTO);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("is")), TK_IS);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("key")), TK_KEY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("like")), TK_LIKE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("long")), TK_LONG);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("lower")), TK_LOWER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("max")), TK_MAX);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("millis")), TK_MILLIS);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("min")), TK_MIN);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("minute")), TK_MINUTE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("month")), TK_MONTH);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("nocase")), TK_NOCASE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("not")), TK_NOT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("null")), TK_NULL) ;
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("on")), TK_ON);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("or")), TK_OR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("order")), TK_ORDER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("primary")), TK_PRIMARY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("rename")), TK_RENAME);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("second")), TK_SECOND);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("select")), TK_SELECT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("set")), TK_SET);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("short")), TK_SHORT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("sum")), TK_SUM);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("table")), TK_TABLE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("to")), TK_TO);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("update")), TK_UPDATE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("upper")), TK_UPPER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("values")), TK_VALUES);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("varchar")), TK_VARCHAR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("where")), TK_WHERE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("year")), TK_YEAR);

   // Classes.
   ASSERT1_EQUALS(NotNull, litebaseConnectionClass);
	ASSERT1_EQUALS(NotNull, loggerClass);

finish: ;
}

#endif
