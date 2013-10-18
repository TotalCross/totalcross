/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

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
TC_ImplementList(Object); 

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
      test_errorWithoutPosition(&testSuite, currentContext);
      test_initLitebaseParser(&testSuite, currentContext);
      test_lbError(&testSuite, currentContext);
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
      test_read24(&testSuite, currentContext);
      test_valueLoad(&testSuite, currentContext);
      test_valueSave(&testSuite, currentContext);
      test_valueSaveNew(&testSuite, currentContext);
      test_write24(&testSuite, currentContext);
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

#ifdef PALMOS // It is necessary to get the application id for mutex on Palm.   
   getApplicationIdFunc getApplicationId = params->getProcAddress(null, "getApplicationId");
	int32 applicationId = getApplicationId();
#endif

   // Initializes the mutexes.
	SETUP_MUTEX;
   INIT_MUTEX(parser);
   INIT_MUTEX(log);

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

/**
 * Creates a LitebaseConnection for the given creator id and with the given connection param list. This method avoids the creation of more than
 * one instance with the same creator id and parameters, which would lead to performance and memory problems.
 *
 * @param context The thread context where the function is being executed.
 * @param crid The creator id, which may be the same one of the current application and MUST be 4 characters long.
 * @param objParams Only the folder where it is desired to store the tables, <code>null</code>, if it is desired to use the current data 
 * path, or <code>chars_type = chars_format; path = source_path</code>, where <code>chars_format</code> can be <code>ascii</code> or 
 * <code>unicode</code>, and <code>source_path</code> is the folder where the tables will be stored. The params can be entered in any order. If
 * only the path is passed as a parameter, unicode is used. Notice that path must be absolute, not relative.
 * <p>If it is desired to store the database in the memory card (on Palm OS devices only), use the desired volume in the path given to the method.
 * <p>Most PDAs will only have one card, but others, like Tungsten T5, can have more then one. So it is necessary to specify the desired card 
 * slot.
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
Object create(Context context, int32 crid, Object objParams) 
{
	TRACE("create")
	Object driver,
          logger = litebaseConnectionClass->objStaticValues[1];
   int32 hash,
         slot;
   bool isAscii = false;
   char sourcePath[MAX_PATHNAME];
	CharP path = null;

   if (logger) // juliana@230_30: reduced log files size.
   {
		Object logSBuffer = litebaseConnectionClass->objStaticValues[2];
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
		CharP tempParams[2];
      char params[300];
		int32 i = 2;
		
      params[0] = 0;
      tempParams[0] = tempParams[1] = 0;

      // juliana@250_4: now getInstance() can receive only the parameter chars_type = ...
      // juliana@210_2: now Litebase supports tables with ascii strings.
      TC_JCharP2CharPBuf(String_charsStart(objParams), String_charsLen(objParams), params);
		tempParams[0] = params;
      tempParams[1] = xstrchr(params, ';'); // Separates the parameters.
		if (!tempParams[1]) 
			i = 1;
		else
		{ 
		   i = 2;
		   tempParams[1][0] = 0;
		   tempParams[1]++;
		}
      while (--i >= 0) // The parameters order does not matter. 
		{
			tempParams[i] = strTrim(tempParams[i]);
			if (xstrstr(tempParams[i], "chars_type")) // Chars type param.
            isAscii = (xstrstr(tempParams[i], "ascii") != null);
			else if (xstrstr(tempParams[i], "path")) // Path param.
				path = &xstrchr(tempParams[i], '=')[1];
		   else 
		      path = tempParams[0]; // Things do not change if there is only one parameter.
		}
	} 
 
   // Gets the slot and checks the path validity.
   if (!(slot = checkApppath(context, sourcePath, path))) // juliana@214_1
		return null;

	// juliana@221_3: solved a small bug that could make Litebase crash on Windows 32, Windows CE, Palm, iPhone, and Android when passing 2 arguments to
   // params when issuing a LitebaseConnection.getInstance().

   // fdie@555_2: driver not already created? Creates one.
   // If there is no connections with this key, creates a new one.
   if (!(driver = TC_htGetPtr(&htCreatedDrivers, (hash = TC_hashCodeFmt("ixis", crid, context->thread, isAscii, path? path: "null"))))) 
   {
		Hashtable htTables,
                htPS;

		if (!(driver = TC_createObject(context, "litebase.LitebaseConnection")))
			return null;

      OBJ_LitebaseAppCrid(driver) = crid; // juliana@210a_10
      OBJ_LitebaseSlot(driver) = slot; // juliana@223_1
	   OBJ_LitebaseIsAscii(driver) = isAscii;
	   OBJ_LitebaseKey(driver) = hash;
		
      // SourcePath.
		if (!(setLitebaseSourcePath(driver, xmalloc(xstrlen(sourcePath) + 1))))
		{
error1:
		   freeLitebase(context, (int32)driver);
		   TC_setObjectLock(driver, UNLOCKED);
         TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
         return null;
      }
      xstrcpy(getLitebaseSourcePath(driver), sourcePath);

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
void freeLitebase(Context context, int32 driver)
{
	TRACE("freeLitebase")
   CharP sourcePath = getLitebaseSourcePath(driver);
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
	TC_htRemove(&htCreatedDrivers, OBJ_LitebaseKey((Object)driver)); // fdie@555_2: removes this instance from the drivers hash table.
	OBJ_LitebaseDontFinalize((Object)driver) = true; // This object shouldn't be finalized again.
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
void litebaseExecute(Context context, Object driver, JCharP sqlStr, uint32 sqlLen)
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
      names = (CharP*)TC_heapAlloc(heap, count << 2);
      defaultValues = (SQLValue**)TC_heapAlloc(heap, count * PTRSIZE);
      columnAttrs = (uint8*)TC_heapAlloc(heap, count);

      // Creates column 0 (rowid).
      hashes[0] = HCROWID;
      types[0] = INT_TYPE;
		names[0] = "rowid";

      i = count;
      while (--i > 0) // Creates the other columns
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
                  TC_JCharP2CharPBuf(defaultValue, -1, doubleBuf);                 
                  if (!testAndPrepareDateAndTime(context, defaultValues[i], doubleBuf, types[i]))
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
int32 litebaseExecuteUpdate(Context context, Object driver, JCharP sqlStr, int32 sqlLen)
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
Object litebaseExecuteQuery(Context context, Object driver, JCharP strSql, int32 length)
{
   TRACE("litebaseExecuteQuery")
	Heap heapParser = heapCreate();
   LitebaseParser* parser;
	SQLSelectStatement* selectStmt;
   ResultSet* resultSetBag;
	Object resultSet;
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
void litebaseExecuteDropTable(Context context, Object driver, LitebaseParser* parser)
{
	TRACE("litebaseExecuteDropTable")
   Table* table;
   CharP tableName = parser->tableList[0]->tableName;
   int32 i,
         hashCode;
	Hashtable* htTables = getLitebaseHtTables(driver);
   Heap heap = parser->heap;
   CharP sourcePathCharP = getLitebaseSourcePath(driver);

// The source path type depends on the platform.
#ifndef WINCE
   CharP sourcePath = sourcePathCharP;
#else
   TCHAR sourcePath[MAX_PATHNAME];
   TC_CharP2JCharPBuf(sourcePathCharP, -1, sourcePath, true);
#endif
   
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
            count = 0,
            slot = OBJ_LitebaseSlot(driver);
      char fileName[DBNAME_SIZE],
		     fileSimpIdxName[DBNAME_SIZE],
		     fileCompIdxName[DBNAME_SIZE];
      TCHARPs* list = null;
      TCHAR buffer[MAX_PATHNAME];

#ifdef WINCE // A file name in char for Windows CE, which uses TCHAR.
      char value[DBNAME_SIZE];
#else
      CharP value;
#endif

      getDiskTableName(context, OBJ_LitebaseAppCrid(driver), tableName, fileName);
		
		// juliana@220_12: drop table was dropping a closed table and all tables starting with the same name of the dropped one.
		xstrcpy(fileSimpIdxName, fileName);
      xstrcpy(fileCompIdxName, fileName);
		xstrcat(fileName, ".");
		xstrcat(fileSimpIdxName, "$");
		xstrcat(fileCompIdxName, "&");

      if ((i = TC_listFiles(sourcePath, slot, &list, &count, heap, 0))) // Lists all the path files.
      {
         fileError(context, i, "");
         goto finish;
      }

      while (--count >= 0) // Erases the table files.
      {
#ifndef WINCE         
         value = list->value;
#else
         TC_JCharP2CharPBuf(list->value, -1, value);
#endif
         if (xstrstr(value, fileName) == value || xstrstr(value, fileSimpIdxName) == value || xstrstr(value, fileCompIdxName) == value)
         {
            getFullFileName(value, sourcePathCharP, buffer);
            if ((i = lbfileDelete(null, buffer, slot, false)))
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
int32 litebaseExecuteDropIndex(Context context, Object driver, LitebaseParser* parser)
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
void litebaseExecuteAlter(Context context, Object driver, LitebaseParser* parser)
{
	TRACE("litebaseExecuteAlter")
   Table* table = getTable(context, driver, parser->tableList[0]->tableName);
   Heap heap;
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
      }
   }

finish:
   heapDestroy(parser->heap);
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
      Object rowIterator = p->obj[0];
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
   Object rowIterator = params->obj[0];

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
 * @return The slot number for palm, -1 for the other devices or 0 in case of error.
 * @throws DriverException if the path passed as a parameter is invalid. 
 */
int32 checkApppath(Context context, CharP sourcePath, CharP params) // juliana@214_1
{
	TRACE("checkApppath")
#ifdef WINCE
   TCHAR appPathTCHARP[MAX_PATHNAME];
#endif
   char buffer[MAX_PATHNAME];
   int32 endCh,
         lenAppPath,
         slot = -1,
         ret = 0;

   if (params)
	{
	   // juliana@252_3: corrected a possible crash if the path had more than 255 characteres.
	   if (xstrlen(params) + 1 > MAX_PATHNAME)
		{
		   TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), params);
		   return 0;
		}   
		xstrcpy(sourcePath, strTrim(params));

#ifndef PALMOS // The path passed for palm os can't be empty.
		if (!sourcePath[0])
		{
			TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), "");
			return 0;
		}
#else
      if (!sourcePath[0] && params[0])
      {
			TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), "");
			return 0;
		}
#endif

	}
   else // Since no path was passed by the user, gets it from dataPath or appPath. 
      getCurrentPath(sourcePath);
#ifndef PALMOS
   xstrcpy(buffer, strTrim(sourcePath));
   xstrcpy(sourcePath, buffer);
#endif 

// juliana@214_1: relative paths can't be used with Litebase.
#ifndef WINCE
   if (!TC_validatePath(sourcePath))
	{
		TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), sourcePath);
		return 0;
	}
#else
   TC_CharP2JCharPBuf(sourcePath, -1, appPathTCHARP, true);
   if (!TC_validatePath(appPathTCHARP))
	{
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), sourcePath);
		return 0;
	}
   TC_JCharP2CharPBuf(appPathTCHARP, -1, sourcePath);

   // Creates the path folder if it does not exist.
   if (!appPathTCHARP[0] || (appPathTCHARP[0] && !lbfileExists(appPathTCHARP, 0) && (ret = lbfileCreateDir(appPathTCHARP, 0))))
   {
      if (!ret)
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), sourcePath);
      else
         fileError(context, ret, sourcePath);
		return 0;
	}
#endif

#if defined(PALMOS)
   // Finds the correct folder and its slot.
   slot = 1;
   if (!sourcePath[0]) // 1:/Litebase_DBs/
      xstrcpy(sourcePath, "/Litebase_DBs/");
   else if (sourcePath[0] == '-' && sourcePath[1] == '1' && sourcePath[2] == ':') // -1:/sourcePath
   {
      slot = TC_getLastVolume();
      xstrcpy(buffer, &sourcePath[3]);
      xstrcpy(sourcePath, buffer);
   }
   else if (sourcePath[1] == ':') // v:/sourcePath
   {
      slot = sourcePath[0] - '0';
      xstrcpy(buffer, &sourcePath[2]);
      xstrcpy(sourcePath, buffer);
   }

   // Creates the path folder if it does not exist.
   if (!lbfileExists(sourcePath, slot) && (ret = lbfileCreateDir(sourcePath, slot))) // Creates the path folder if it does not exist.
   {
		fileError(context, ret, sourcePath);
		return 0;
	}
#elif !defined(WINCE) // WIN32 and POSIX
   // Creates the path folder if it does not exist; it can't be empty.
   if (!sourcePath[0] || (sourcePath[0] && !lbfileExists(sourcePath, 0) && (ret = lbfileCreateDir(sourcePath, 0))))
   {
      if (!ret)
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), sourcePath);
      else
	      fileError(context, ret, sourcePath);
		return 0;
	}
#endif

   // Puts a '/' at the end of the path.
   lenAppPath = xstrlen(sourcePath);
   endCh = lenAppPath > 0? sourcePath[lenAppPath - 1] : -1;
   if ((endCh != '\\' && endCh != '/') || endCh == -1)
      xstrcat(sourcePath, "/");

	return slot;
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
   ASSERT1_EQUALS(NotNull, TC_CharPToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharP);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharPBuf);
	ASSERT1_EQUALS(NotNull, TC_JCharPEqualsJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPEqualsIgnoreCaseJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPHashCode);
   ASSERT1_EQUALS(NotNull, TC_JCharPIndexOfJChar);
	ASSERT1_EQUALS(NotNull, TC_JCharPLen);
   ASSERT1_EQUALS(NotNull, TC_JCharToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharToUpper);
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
#ifdef PALMOS
   ASSERT1_EQUALS(NotNull, TC_getLastVolume);
#endif
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

   // Table errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[33], "Table name not found: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[34], "Table already created: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[35], "It is not possible to open a table within a connection with a different string format."));

   // ROWID errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[36], "ROWID can't be changed by the user!"));

   // Prepared Statement errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[37], "Query does not return result set."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[38], "Query does not perform updates in the database."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[39], "Not all parameters of the query had their values defined."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[40], "A value was not defined for the parameter %d."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[41], "Invalid parameter index."));

	// Rename errors. 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[42], "Can't rename table. This table already exists: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[43], "Column already exists: %s."));

	// Alias errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[44], "Not unique table/alias: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[45], "This alias is already being used in this expression: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[46], "An alias is required for the aggregate function column."));

	// Litebase.execute() error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[47], "Only CREATE TABLE and CREATE INDEX can be used in Litebase.execute()."));
   
	// Order by and group by errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[48], "ORDER BY and GROUP BY clauses must match."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[49], "No support for virtual columns in SQL queries with GROUP BY clause."));
   
   // Function errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[50], "All non-aggregation function columns in the SELECT clause must also be in the GROUP BY clause."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[51], 
	 "%s is not an aggregation function. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation functions."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[52], 
                                                "Can't mix aggregation functions with real columns in the SELECT clause without a GROUP BY clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[53], "Can't have aggregation functions with ORDER BY clause and no GROUP BY clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[54], 
"%s was not listed in the SELECT clause. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation funtions."
));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[55], "SUM and AVG aggregation functions are not used with DATE and DATETIME type fields."));

   // DATE and DATETIME errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[56], "Value is not a DATE: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[57], "Value is not a DATETIME: %s."));

   // Index error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[58], "Index already created for column %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[59], "Can't drop a primary key index withdrop index."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[60], "Index too large. It can't have more than 32767 nodes."));
      
   // NOT NULL errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[61], "Primary key can't have null."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[62], "Field can't be null: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[63], "A parameter in a where clause can't be null."));

   // Result set errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[64], "ResultSet in invalid record position: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[65], "Invalid value for decimal places: %d. It must range from -1 to 40."));

   // File errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[66], "Can't read from table %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[67], "Can't load leaf node!"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[68], "Table is corrupted: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[69], "Table not closed properly: %s.")); // juliana@220_2
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[70], "A properly closed table can't be used in recoverTable(): %s.")); // juliana@222_2
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[71], "Can't find index record position on delete."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[72], "The table format (%d) is incompatible with Litebase version. Please update your tables."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[73], "The table format is not the previous one: %s.")); // juliana@220_11
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[74], "Invalid path: %s.")); // juliana@214_1
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[75], "Invalid file position: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[76], "Database not found."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[77], "An opened table can't be recovered or converted: %s."));
   
   // BLOB errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[78], "The total size of a blob can't be greater then 10 Mb."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[79], "This is not a valid size multiplier."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[80], "A blob type can't be part of a primary key."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[81], "A BLOB column can't be indexed."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[82], "A BLOB can't be in the where clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[83], "A BLOB can't be converted to a string."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[84], "Blobs types can't be in ORDER BY or GROUP BY clauses."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[85], "It is not possible to compare BLOBs."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[86], "It is only possible to insert or update a BLOB through prepared statements using setBlob()."));

   // Portuguese messages.
	// General errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[0], "Erro: "));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[1], " Próximo à posição %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[2], "Erro de sintaxe."));

	// Limit errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[3], "Número máximo de campos diferentes foi alcançado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[4], "Número máximo da lista de parâmetros na cláusula 'WHERE/HAVING' foi alcançado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[5], "Numero máximo de índices compostos 32 foi alcançado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[6], "Nome da tabela muito grande: deve ser <= 23"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[7], "O número máximo de campos na cláusula SELECT foi excedido."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[8],"O número máximo de campos na cláusula 'ORDER BY/GROUP BY' foi excedido."));

   // Column errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[9], "Coluna desconhecida %s.")); 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[10], "Nome de coluna inválido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[11], "Número de coluna inválido: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[12], "A(s) coluna(s) a seguir não tem (têm) um indíce associado %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[13], "Nome de coluna ambíguo: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[14], "Coluna não encontrada: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[15], "Nome de coluna duplicado: %s."));

   // Primary key errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[16], "Uma chave primária já foi definida para esta tabela."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[17], "Tabela não tem chave primária."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[18], "Comando cria uma chave primária duplicada em %s."));

   // Type errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[19], "Tipos incompativeis."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[20], "Tamanho do campo deve ser um valor inteiro positivo."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[21], "O valor %s não é um número válido para o tipo desejado: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[22], "Tipo de dados incompatível para a chamada de função: %s"));

	// Number of fields errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[23], "O número de campos é diferente do número de valores "));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[24], "O número de valores dado não coincide com a definição da tabela %d."));

   // Default value errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[25], "Tamanho do valor padrão é maior que o tamanho definido para a coluna."));

	// Driver errors. 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[26], 
                                 "Esta instância do driver está fechada e não pode ser mais utilizada. Por favor, obtenha uma nova instância."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[27], "ResultSet já está fechado!"));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[28], "ResultSetMetaData não pode ser usado depois que o ResultSet estiver fechado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[29], "O id da aplicação de ter 4 characteres."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[30], "O incremento deve ser maior do que 0 ou -1."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[31], "Iterador já foi fechado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[32], "Prepared statement fechado. Por favor, prepare-o novamente."));

	// Table errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[33], "Nome da tabela não encontrado: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[34], "Tabela já existe: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[35],  "Não é possível abrir uma tabela com uma conexão com um tipo de strings diferente."));

	// ROWID errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[36], "ROWID não pode ser mudado pelo usuário!"));

   // Prepared Statement errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[37], "Comando SQL não retorna um ResultSet."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[38], "Comando SQL não executa uma atualização no banco de dados."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[39], "Nem todos os parâmetros da consulta tiveram seus valores definidos."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[40], "Não foi definido um valor para o parâmetro %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[41], "Invalid parameter index."));
   
   // Rename errors. 
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[42], "Não é possível renomear a tabela. Esta tabela já existe: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[43], "Coluna já existe: %s."));

	// Alias errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[44], "Nome de tabela/alias repetido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[45], "Este alias já está sendo utilizado no sql: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[46], "Um alias é necessário para colunas com função de agregação."));
   
	// Litebase.execute() error.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[47], "Apenas CREATE TABLE e CREATE INDEX são permitidos no Litebase.execute()"));
   
   // Order by and group by errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[48], "Cláusulas ORDER BY e GROUP BY devem coincidir."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[49], "SQL com cláusula GROUP BY não tem suporte para colunas virtuais."));
   
   // Function errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[50], 
	            "Todas colunas que nãosão funções de agregação na cláusula SELECT devem estar na cláusula GROUP BY."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[51], 
	           "%s não é uma função de agregação. Todos as colunas da cláusula HAVING devem ser listadas no SELECT utilizando alias."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[52], "Não é possivel misturar colunas reais e de agregação no SELECT sem cláusula GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[53], "Não é possível ter funções de agregação com cláusula ORDER BY sem cláusula GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[54], 
	            "%s não foi listado no SELECT. Todas as colunas da cláusula HAVING devem ser listadas no SELECT utilizando alias."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[55], "Funções de agregação SUM e AVG não são usadas com colunas do tipo DATE e DATETIME."));

   // DATE and DATETIME errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[56], "Valor não é um tipo DATE válido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[57], "Valor não é um tipo DATETIME válido: %s."));

   // Index error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[58], "Índice já criado para a coluna %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[59], "Não é possível remover uma chave primária usando drop index."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[60], "Índice muito grande. Ele não pode ter mais do que 32767 nós."));
      
   // NOT NULL errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[61], "Chave primária não pode ter NULL."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[62], "Coluna não pode ser NULL: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[63], "Um parâmetro em uma where clause não pode ser NULL."));

   // Result set errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[64], "ResultSet em uma posição de registro inválida %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[65], "Valor inválido para casas decimais: %d. Deve ficar entre - 1 e 40."));

   // File errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[66], "Não é possível ler da tabela %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[67], "Não é possível carregar nó folha!"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[68], "Tabela está corrompida: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[69], "Tabela não foi fechada corretamente: %s.")); // juliana@220_2
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[70], "Uma tabela fechada corretamente não pode ser usada no recoverTable(): %s.")); // juliana@222_2
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[71], "Não é possível achar a posição de registro no índice na exclusão."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[72], 
                                 "O formato de tabela (%d) não é compatível com a versão do Litebase. Por favor, atualize suas tabelas."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[73], "O formato de tabela não é o anterior: %s.")); // juliana@220_11
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[74], "Caminho inválido: %s.")); // juliana@214_1
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[75], "Posição inválida no arquivo: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[76], "Base de dados não encontrada."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[77], "Uma tabela aberta não pode ser recuperada ou convertida: %s."));

   // BLOB errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[78], "O tamanho total de um BLOB não pode ser maior do que 10 Mb."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[79], "O multiplicador de tamanho não é válido."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[80], "Um tipo BLOB não pode ser parte de uma chave primária."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[81], "Uma coluna do tipo BLOB não pode ser indexada."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[82], "Um BLOB não pode estar na cláusula WHERE."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[83], "Um BLOB não pode ser convertido em uma string."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[84], "Tipos BLOB não podem estar em cláusulas ORDER BY ou GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[85], "Não é possível comparar BLOBs."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[86], "Só é possível inserir ou atualizar um BLOB através prepared statements usando setBlob()."));
   
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
#ifdef PALMOS
   ASSERT2_EQUALS(Sz, sourcePath, "/Litebase_DBs/");
#else
   ASSERT2_EQUALS(Sz, sourcePath, defaultAppPath);
#endif

	// Empty string.
	path[0] = 0;
#ifdef PALMOS
   ASSERT2_EQUALS(Sz, sourcePath, "/Litebase_DBs/");
#else
   ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path)); 
#endif

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
   #ifndef WINCE
	xstrcpy(path, "p:\\temp\\tables/"); 
	ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, path));
   ASSERT2_EQUALS(Sz, sourcePath, "p:/temp/tables/");
   xstrcpy(path, "p:/temp/tables/"); 
	ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, path));
   ASSERT2_EQUALS(Sz, sourcePath, "p:/temp/tables/");
   #else
   xstrcpy(path, "p:\\temp\\tables/"); 
	ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));
   ASSERT2_EQUALS(Sz, sourcePath, "p:/temp/tables/");
   xstrcpy(path, "p:/temp/tables/"); 
	ASSERT1_EQUALS(False, checkApppath(currentContext, sourcePath, path));
   ASSERT2_EQUALS(Sz, sourcePath, "p:/temp/tables/");
#endif
#endif
#ifdef PALMOS
	xstrcpy(path, "1:\\temp\\tables");
	ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, path));
	ASSERT2_EQUALS(Sz, sourcePath, "/temp/tables/");
	xstrcpy(path, "-1:/temp/tables");
	ASSERT1_EQUALS(True, checkApppath(currentContext, sourcePath, path));
	ASSERT2_EQUALS(Sz, sourcePath, "/temp/tables/");
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
   ASSERT1_EQUALS(NotNull, TC_CharPToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharP);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharPBuf);
	ASSERT1_EQUALS(NotNull, TC_JCharPEqualsJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPEqualsIgnoreCaseJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPHashCode);
   ASSERT1_EQUALS(NotNull, TC_JCharPIndexOfJChar);
	ASSERT1_EQUALS(NotNull, TC_JCharPLen);
   ASSERT1_EQUALS(NotNull, TC_JCharToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharToUpper);
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
#ifdef PALMOS
   ASSERT1_EQUALS(NotNull, TC_getLastVolume);
#endif
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

   // Table errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[33], "Table name not found: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[34], "Table already created: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[35], "It is not possible to open a table within a connection with a different string format."));

   // ROWID errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[36], "ROWID can't be changed by the user!"));

   // Prepared Statement errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[37], "Query does not return result set."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[38], "Query does not perform updates in the database."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[39], "Not all parameters of the query had their values defined."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[40], "A value was not defined for the parameter %d."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[41], "Invalid parameter index."));

	// Rename errors. 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[42], "Can't rename table. This table already exists: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[43], "Column already exists: %s."));

	// Alias errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[44], "Not unique table/alias: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[45], "This alias is already being used in this expression: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[46], "An alias is required for the aggregate function column."));

	// Litebase.execute() error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[47], "Only CREATE TABLE and CREATE INDEX can be used in Litebase.execute()."));
   
	// Order by and group by errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[48], "ORDER BY and GROUP BY clauses must match."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[49], "No support for virtual columns in SQL queries with GROUP BY clause."));
   
   // Function errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[50], "All non-aggregation function columns in the SELECT clause must also be in the GROUP BY clause."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[51], 
	 "%s is not an aggregation function. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation functions."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[52], 
                                                "Can't mix aggregation functions with real columns in the SELECT clause without a GROUP BY clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[53], "Can't have aggregation functions with ORDER BY clause and no GROUP BY clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[54], 
"%s was not listed in the SELECT clause. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation funtions."
));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[55], "SUM and AVG aggregation functions are not used with DATE and DATETIME type fields."));

   // DATE and DATETIME errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[56], "Value is not a DATE: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[57], "Value is not a DATETIME: %s."));

   // Index error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[58], "Index already created for column %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[59], "Can't drop a primary key index withdrop index."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[60], "Index too large. It can't have more than 32767 nodes."));
      
   // NOT NULL errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[61], "Primary key can't have null."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[62], "Field can't be null: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[63], "A parameter in a where clause can't be null."));

   // Result set errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[64], "ResultSet in invalid record position: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[65], "Invalid value for decimal places: %d. It must range from -1 to 40."));

   // File errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[66], "Can't read from table %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[67], "Can't load leaf node!"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[68], "Table is corrupted: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[69], "Table not closed properly: %s.")); // juliana@220_2
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[70], "A properly closed table can't be used in recoverTable(): %s.")); // juliana@222_2
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[71], "Can't find index record position on delete."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[72], "The table format (%d) is incompatible with Litebase version. Please update your tables."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[73], "The table format is not the previous one: %s.")); // juliana@220_11
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[74], "Invalid path: %s.")); // juliana@214_1
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[75], "Invalid file position: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[76], "Database not found."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[77], "An opened table can't be recovered or converted: %s."));
   
   // BLOB errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[78], "The total size of a blob can't be greater then 10 Mb."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[79], "This is not a valid size multiplier."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[80], "A blob type can't be part of a primary key."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[81], "A BLOB column can't be indexed."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[82], "A BLOB can't be in the where clause."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[83], "A BLOB can't be converted to a string."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[84], "Blobs types can't be in ORDER BY or GROUP BY clauses."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[85], "It is not possible to compare BLOBs."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_en[86], "It is only possible to insert or update a BLOB through prepared statements using setBlob()."));

   // Portuguese messages.
	// General errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[0], "Erro: "));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[1], " Próximo à posição %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[2], "Erro de sintaxe."));

	// Limit errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[3], "Número máximo de campos diferentes foi alcançado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[4], "Número máximo da lista de parâmetros na cláusula 'WHERE/HAVING' foi alcançado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[5], "Numero máximo de índices compostos 32 foi alcançado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[6], "Nome da tabela muito grande: deve ser <= 23"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[7], "O número máximo de campos na cláusula SELECT foi excedido."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[8],"O número máximo de campos na cláusula 'ORDER BY/GROUP BY' foi excedido."));

   // Column errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[9], "Coluna desconhecida %s.")); 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[10], "Nome de coluna inválido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[11], "Número de coluna inválido: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[12], "A(s) coluna(s) a seguir não tem (têm) um indíce associado %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[13], "Nome de coluna ambíguo: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[14], "Coluna não encontrada: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[15], "Nome de coluna duplicado: %s."));

   // Primary key errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[16], "Uma chave primária já foi definida para esta tabela."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[17], "Tabela não tem chave primária."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[18], "Comando cria uma chave primária duplicada em %s."));

   // Type errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[19], "Tipos incompativeis."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[20], "Tamanho do campo deve ser um valor inteiro positivo."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[21], "O valor %s não é um número válido para o tipo desejado: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[22], "Tipo de dados incompatível para a chamada de função: %s"));

	// Number of fields errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[23], "O número de campos é diferente do número de valores "));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[24], "O número de valores dado não coincide com a definição da tabela %d."));

   // Default value errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[25], "Tamanho do valor padrão é maior que o tamanho definido para a coluna."));

	// Driver errors. 
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[26], 
                                 "Esta instância do driver está fechada e não pode ser mais utilizada. Por favor, obtenha uma nova instância."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[27], "ResultSet já está fechado!"));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[28], "ResultSetMetaData não pode ser usado depois que o ResultSet estiver fechado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[29], "O id da aplicação de ter 4 characteres."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[30], "O incremento deve ser maior do que 0 ou -1."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[31], "Iterador já foi fechado."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[32], "Prepared statement fechado. Por favor, prepare-o novamente."));

	// Table errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[33], "Nome da tabela não encontrado: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[34], "Tabela já existe: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[35],  "Não é possível abrir uma tabela com uma conexão com um tipo de strings diferente."));

	// ROWID errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[36], "ROWID não pode ser mudado pelo usuário!"));

   // Prepared Statement errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[37], "Comando SQL não retorna um ResultSet."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[38], "Comando SQL não executa uma atualização no banco de dados."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[39], "Nem todos os parâmetros da consulta tiveram seus valores definidos."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[40], "Não foi definido um valor para o parâmetro %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[41], "Invalid parameter index."));
   
   // Rename errors. 
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[42], "Não é possível renomear a tabela. Esta tabela já existe: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[43], "Coluna já existe: %s."));

	// Alias errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[44], "Nome de tabela/alias repetido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[45], "Este alias já está sendo utilizado no sql: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[46], "Um alias é necessário para colunas com função de agregação."));
   
	// Litebase.execute() error.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[47], "Apenas CREATE TABLE e CREATE INDEX são permitidos no Litebase.execute()"));
   
   // Order by and group by errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[48], "Cláusulas ORDER BY e GROUP BY devem coincidir."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[49], "SQL com cláusula GROUP BY não tem suporte para colunas virtuais."));
   
   // Function errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[50], 
	            "Todas colunas que nãosão funções de agregação na cláusula SELECT devem estar na cláusula GROUP BY."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[51], 
	           "%s não é uma função de agregação. Todos as colunas da cláusula HAVING devem ser listadas no SELECT utilizando alias."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[52], "Não é possivel misturar colunas reais e de agregação no SELECT sem cláusula GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[53], "Não é possível ter funções de agregação com cláusula ORDER BY sem cláusula GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[54], 
	            "%s não foi listado no SELECT. Todas as colunas da cláusula HAVING devem ser listadas no SELECT utilizando alias."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[55], "Funções de agregação SUM e AVG não são usadas com colunas do tipo DATE e DATETIME."));

   // DATE and DATETIME errors.
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[56], "Valor não é um tipo DATE válido: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[57], "Valor não é um tipo DATETIME válido: %s."));

   // Index error.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[58], "Índice já criado para a coluna %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[59], "Não é possível remover uma chave primária usando drop index."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[60], "Índice muito grande. Ele não pode ter mais do que 32767 nós."));
      
   // NOT NULL errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[61], "Chave primária não pode ter NULL."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[62], "Coluna não pode ser NULL: %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[63], "Um parâmetro em uma where clause não pode ser NULL."));

   // Result set errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[64], "ResultSet em uma posição de registro inválida %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[65], "Valor inválido para casas decimais: %d. Deve ficar entre - 1 e 40."));

   // File errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[66], "Não é possível ler da tabela %s."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[67], "Não é possível carregar nó folha!"));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[68], "Tabela está corrompida: %s."));
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[69], "Tabela não foi fechada corretamente: %s.")); // juliana@220_2
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[70], "Uma tabela fechada corretamente não pode ser usada no recoverTable(): %s.")); // juliana@222_2
	ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[71], "Não é possível achar a posição de registro no índice na exclusão."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[72], 
                                 "O formato de tabela (%d) não é compatível com a versão do Litebase. Por favor, atualize suas tabelas."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[73], "O formato de tabela não é o anterior: %s.")); // juliana@220_11
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[74], "Caminho inválido: %s.")); // juliana@214_1
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[75], "Posição inválida no arquivo: %d."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[76], "Base de dados não encontrada."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[77], "Uma tabela aberta não pode ser recuperada ou convertida: %s."));

   // BLOB errors.
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[78], "O tamanho total de um BLOB não pode ser maior do que 10 Mb."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[79], "O multiplicador de tamanho não é válido."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[80], "Um tipo BLOB não pode ser parte de uma chave primária."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[81], "Uma coluna do tipo BLOB não pode ser indexada."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[82], "Um BLOB não pode estar na cláusula WHERE."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[83], "Um BLOB não pode ser convertido em uma string."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[84], "Tipos BLOB não podem estar em cláusulas ORDER BY ou GROUP BY."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[85], "Não é possível comparar BLOBs."));
   ASSERT1_EQUALS(False, xstrcmp(errorMsgs_pt[86], "Só é possível inserir ou atualizar um BLOB através prepared statements usando setBlob()."));
   
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
