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

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.util.*;

// juliana@253_5: removed .idr files from all indices and changed its format.
/**
 * The structure of a table.
 */
class Table
{
   /**
    * Current table format version.
    */
   static final int VERSION = 203; // juliana@230_12
   
   // ############ JOIN OPERATION CONSTANTS ###########
   /**
    * Indicates the end of the table in join operations.
    */
   private static final int NO_RECORD = 0;

   /**
    * Indicates that the row can be used in join operations.
    */
   private static final int VALIDATION_RECORD_OK = 1;

   /**
    * Indicates that the row can't be used in join operations.
    */
   private static final int VALIDATION_RECORD_NOT_OK = 2;

   /**
    * Indicates that the row validation is incomplete and must be continued in join operations.
    */
   private static final int VALIDATION_RECORD_INCOMPLETE = 3;

   /**
    * Used internally on <code>booleanTreeEvaluateJoin()</code>. The current branch was validated as <code>true</code>.
    */
   private static final int VALIDATION_RECORD_INCOMPLETE_OK = 4;
   
   /**
    * Indicates that a column has index. Used in <code>writeRecord()</code>.
    */
   private static final int HAS_IDX = 1;
   
   /**
    * Indicates that a column has null. Used in <code>writeRecord()</code>.
    */
   private static final int HAS_NULLVAL = 2;
   
   /**
    * Indicates that a column old value had null. Used in <code>writeRecord()</code>.
    */
   private static final int ISNULL_VOLDS = 4;
   
   /**
    * Indicates if a table was saved correctly the last time it was modified.
    */
   static final int IS_SAVED_CORRECTLY = 1;
   
   /**
    * Indicates if the table strings are stored in the ascii or unicode format. 
    */
   static final int IS_ASCII = 2;

   /**
    * Indicates if the table uses cryptography.
    */
   static final int USE_CRYPTO = 3; // juliana@253_8: now Litebase supports weak cryptography.
   
   /**
    * The counter of the current <code>rowid</code>. The <code>rowid</code> is continuously incremented so that two elements will never have the same
    * one, even if elements are deleted. <p>The record attributes are stored in the first two bits of the <code>rowid</code>.
    */
   int currentRowId = 1;

   /**
    * The attributes of the row.
    */
   int auxRowId = Utils.ATTR_DEFAULT_AUX_ROWID;   // rnovais@570_61

   /**
    * The number of columns of this table.
    */
   int columnCount;

   /**
    * The number of deleted rows.
    */
   int deletedRowsCount;

   /**
    * The primary key column.
    */
   int primaryKeyCol = Utils.NO_PRIMARY_KEY;

   /**
    * The index of the composed primary key.
    */
   int composedPK = Utils.NO_PRIMARY_KEY;

   /**
    * Number of composed indices.
    */
   int numberComposedIndices;

   /**
    * Number of composed primary key columns.
    */
   int numberComposedPKCols;

   /**
    * Used to order the tables.
    */
   int weight;
   
   /**
    * Used to return the number of rows that a select without a where clause returned.
    */
   int answerCount = -1; // juliana@230_14

   /**
    * The current table version.
    */
   int version; // juliana@230_12
   
   // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since its 
   // last opening. 
   /**
    * Indicates that a table has been modified and must be marked as not closed properly after opened and before closed.
    */
   boolean isModified;
   
   // juliana@270_27: now purge will also really purge the table if it only suffers updates.
   /**
    * Indicates if the table was updated after the last time it was opened.
    */
   boolean wasUpdated;

   /**
    * The full name of the table.
    */
   String name;

   /**
    * The corresponding files of the table.
    */
   PlainDB db;

   /**
    * Given a column name, returns its index for this table. <code>rowid</code>, a special column, is always column 0.
    */
   IntHashtable htName2index;

   /**
    * Just for the case when the column has a default value but the user explicited the insert or update of a null.
    */
   byte[] storeNulls;

   /**
    * The column attributes.
    */
   byte[] columnAttrs;
   
   /**
    * The composed primary key columns.
    */
   byte[] composedPrimaryKeyCols;

   /**
    * Contains the null values.
    */
   byte[][] columnNulls = new byte[3][];
   
   /**
    * The hashes of the column names.
    */
   int[] columnHashes;

   /**
    * Column offsets within the record.
    */
   short[] columnOffsets;

   /**
    * Column types (<code>SHORT</code>, <code>INT</code>, <code>LONG</code>, <code>FLOAT</code>, <code>DOUBLE</code>, <code>CHARS</code>, 
    * CHARS_NOCASE</code>)
    */
   byte[] columnTypes;

   /**
    * Column sizes (only used for CHAR and BLOB types).
    */
   int[] columnSizes;

   /**
    * The column names. If <code>null</code>, the column names are not available because it is a temporary table.
    */
   String[] columnNames; // nowosad@_200

   /**
    * Contains the default values for the columns.
    */
   SQLValue[] defaultValues;
   
   /**
    * Existing column indices for each column, or <code>null</code> if the column has no index.
    */
   Index[] columnIndices;

   /**
    * Existing composed column indices for each column, or <code>null</code> if the table has no composed index.
    */
   ComposedIndex[] composedIndices = new ComposedIndex[SQLBooleanClause.MAX_NUM_INDEXES_APPLIED]; 

   /**
    * A buffer to store the table meta data.
    */
   private ByteArrayStream tsmdBas;

   /**
    * A data stream for the table meta data.
    */
   private DataStreamLB tsmdDs; // juliana@253_8: now Litebase supports weak cryptography.
   
   /**
    * Stores old values read from the table. This is used by <code>writeRecord()</code> in order to reduce memory allocation.
    */
   SQLValue[] gvOlds;
   
   /**
    * Stores flags from the record. This is used by <code>writeRecord()</code> in order to reduce memory allocation.
    */
   byte[] ghas;
   
   /**
    * An array to store the primary key values. Used in <code>writeRecord()</code>. 
    */
   private SQLValue[] primaryKeyValues;
   
   /**
    * An array to store the primary key old values when doing an update. Used in <code>writeRecord()</code>. 
    */
   private SQLValue[] primaryKeyOldValues;
   
   /**
    * A map with rows that satisfy totally the query WHERE clause.
    */
   byte[] allRowsBitmap; // juliana@230_14
   
   /**
    * An array of only one integer to convert to a byte array.
    */
   private int[] oneInt = new int[1];
   
   /**
    * Verifies if the index already exists.
    *
    * @param columnNumbers The columns that are part of this index.
    * @return 0 for simple indices. For composed index, if there was this same index, it returns the negative number of the this old one; otherwise, 
    * it returns the new number.
    * @throws AlreadyCreatedException If an index already exists.
    */
   int verifyIfIndexAlreadyExists(byte[] columnNumbers) throws AlreadyCreatedException
   {
      int indexCount = columnNumbers.length, 
          idx = -1;

      if (indexCount == 1) // Simple index.
      {
         if (columnIndices[idx = columnNumbers[0]] != null)
            throw new AlreadyCreatedException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INDEX_ALREADY_CREATED) + columnNames[idx]);
         return 0;
      }
      else // Composed index.
      {
         ComposedIndex currCompIndex;
         ComposedIndex[] compIndices = composedIndices;
         byte[] columns;
         boolean alreadyExists;
         int i = numberComposedIndices, 
             j;

         if (i == 0)
            return 1; // First index number.
         while (--i >= 0)
         {
            currCompIndex = compIndices[i];
            alreadyExists = true;

            columns = currCompIndex.columns;
            j = columns.length;
                        
            if (j == indexCount) // juliana@253_2: corrected a bug if a composed index with less columns were created after one with more columns.  
            {
            while (--j >= 0)
               if (columnNumbers[j] != columns[j])
               {
                  alreadyExists = false;
                  break;
               }
            }
            else
               alreadyExists = false; 

            if (alreadyExists)
            {
               StringBuffer cols = db.driver.sBuffer;
               String[] colNames = columnNames;
               
               // Builds the exception message.
               cols.setLength(0);
               cols.append(colNames[currCompIndex.columns[0]]);
               j = columns.length;
               i = 0;
               while (++i < j)
                  cols.append(", ").append(colNames[columns[i]]);
               throw new AlreadyCreatedException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INDEX_ALREADY_CREATED) + cols);
            }
         }
         return numberComposedIndices + 1;
      }
   }
   
   /**
    * Drops an index.
    *
    * @param column The column of the index dropped.
    * @throws IOException If an internal method throws it.
    * @throws DriverException If the column does not have an index. 
    */
   void driverDropIndex(int column) throws IOException
   {
      Index index = columnIndices[column];
         
      if (index == null) // Column does not have an index.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_COLUMN_DOESNOT_HAVE_AN_INDEX) + columnNames[column]);

      // Deletes the index of this table.
      index.fnodes.f.delete();
      columnIndices[column] = null;

      // juliana@227_6
      columnAttrs[column] &= ~Utils.ATTR_COLUMN_HAS_INDEX; // Deletes the INDEX bit from the attributes.  
      
      // Saves the meta data.
      tableSaveMetaData(Utils.TSMD_EVERYTHING); // guich@560_24
   }
   
   /**
    * Drops a composed index.
    *
    * @param columns The columns of the composed index.
    * @param indexId The id of the composed index or -1 if its position is not known.
    * @param saveMD Indicates if the meta data is to be saved.
    * @throws IOException If an internal method throws it.
    * @throws DriverException If the table does not have the desired composed index to be dropped.
    */
   void driverDropComposedIndex(byte[] columns, int indexId, boolean saveMD) throws DriverException, IOException
   {
      ComposedIndex ci = null;
      int indexCount = columns.length;
      ComposedIndex[] compIndices = composedIndices;
      boolean found = true;
      int i = numberComposedIndices,
          j;
      if (indexId >= 0)
         ci = composedIndices[i = indexId];
      else
         while (--i >= 0)
         {
            found = true;
            ci = compIndices[i];

            if (ci.columns.length == indexCount) 
            {
               j = indexCount;
               while (--j >= 0)
                  if (columns[j] != ci.columns[j])
                  {
                     found = false;
                     break;
                  }
               if (found)
                  break;
            }
            else 
               found = false;
         }

      if (found && ci != null) // Removes the index.
      {
         ci.index.fnodes.f.delete();
         ci.index = null;
         
         // juliana@201_16: When a composed index is deleted, its information is now deleted from the metadata.
         (compIndices[i] = compIndices[--numberComposedIndices]).indexId = i + 1;
      }
      else // The given columns do not have a composed index.
      {
         StringBuffer cols = db.driver.sBuffer;
         String[] colNames = columnNames;
         
         cols.setLength(0);
         cols.append(colNames[columns[0]]);
         j = 0;
         while (++j < indexCount)
            cols.append(", ").append(colNames[columns[j]]);
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_COLUMN_DOESNOT_HAVE_AN_INDEX) + cols);
      }
      if (saveMD)
         tableSaveMetaData(Utils.TSMD_EVERYTHING);
   }

   // juliana@227_6: drop index * on table_name wold make the index reapear after closing the driver and reusing table_name.
   /**
    * Deletes all indices of a table.
    *
    * @return The number of indices deleted.
    * @throws IOException If an internal method throws it.
    */
   int deleteAllIndices() throws IOException
   {
      Index[] indices = columnIndices;
      ComposedIndex[] compIndices = composedIndices;
      byte[] attrs = columnAttrs;
      int i = indices.length,
          count = 0,
          pk = primaryKeyCol,
          cpk = composedPK;

      // Unique index.
      while (--i >= 0)
         if (i != pk && indices[i] != null)
         {
            indices[i].fnodes.f.delete();
            indices[i] = null;
            attrs[i] &= ~Utils.ATTR_COLUMN_HAS_INDEX;
            count++;
         }
      
      // juliana@201_33: When all indices are dropped by the user, the composed primary key can't be deleted.
      // Composed index.
      i = numberComposedIndices;
      while (--i >= 0)
         if (i != cpk)
         {
            driverDropComposedIndex(compIndices[i].columns, i, false);
            count++;
         }

      if (count > 0)
         tableSaveMetaData(Utils.TSMD_EVERYTHING); // guich@560_24
      return count;
   }
   
   /**
    * Computes the column offsets of the table columns.
    *
    * @throws IOException If the table has duplicated columns.
    */
   private void computeColumnOffsets() throws IOException
   {
      int sum = 0, 
          i = -1, 
          n = columnCount;
      boolean recomputing = columnOffsets != null;
      byte[] types = columnTypes;
      PlainDB plainDB = db;

      if (!recomputing) // Does not create the array 2 times.
         columnOffsets = new short[n + 1];

      short[] offsets = columnOffsets;

      while (++i < n)
      {
         offsets[i] = (short)sum; // Total offset till now.
         sum += Utils.typeSizes[types[i]]; // Gets the size of this column.
      }
      offsets[i] = (short)sum; // The offset for the last column.

      // the number of bytes necessary to store the columns. Each column in a table correspond to one bit.
      // Added a number of bytes corresponding to the null values and to the crc code.
      sum += ((n + 7) >> 3) + 4; // juliana@220_4

      plainDB.setRowSize(sum, plainDB.basbuf == null || sum > plainDB.basbuf.length? new byte[sum] : plainDB.basbuf); // Sets the new row size.

      if (!recomputing)
      {
         IntHashtable hashTable = htName2index = new IntHashtable(n);
         int[] hashes = columnHashes;

         
         if (name == null) // The hashes of the columns must be put in a hash table.
            while (--n >= 0)
               hashTable.put(hashes[n], n);
         else
            while (--n >= 0)
               try // There can't be duplicated hashes.
               {
                  hashTable.get(hashes[n]);
                  plainDB.remove();
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DUPLICATED_COLUMN_NAME));
               }
               catch (ElementNotFoundException e)
               {
                  hashTable.put(hashes[n], n);
               }
      }
   }

   // rnovais@570_75 juliana@220_2
   // juliana@253_8: now Litebase supports weak cryptography.
   /**
    * Loads the meta data of a table.
    *
    * @param appCrid The application id, used to identify the tables application.
    * @param sourcePath The path where the table is stored.
    * @param throwException Indicates that a <code>TableNotClosedException</code> should be thrown.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws DriverException If the table is in an incompatible format.
    * @throws TableNotClosedException If the table was not properly close when opened last time.
    */
   private void tableLoadMetaData(String appCrid, String sourcePath, boolean throwException) throws IOException, InvalidDateException, 
                                                                                                    DriverException, TableNotClosedException
   {
      PlainDB plainDB = db;
      NormalFile dbFile = (NormalFile)plainDB.db;
      byte[] bytes = plainDB.readMetaData(); // Reads the meta data.
      boolean exist;
      String nameAux;
      int flags;
      File idxFile;

      DataStreamLB ds = new DataStreamLB(new ByteArrayStream(bytes), plainDB.useCrypto);

      // The currentRowId is found from the last non-empty record, not from the metadata.
      if (!(((bytes[0] & USE_CRYPTO) == 0) ^ plainDB.useCrypto)  
       && bytes[1] == 0 && bytes[2] == 0 && bytes[3] == 0 && (bytes[0] == 0 || bytes[0] == 1 || bytes[0] == 3)) 
      {
         // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
         dbFile.close();
         plainDB.dbo.close();
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_WRONG_CRYPTO_FORMAT));
      }
      
      if (bytes[0] == 1)
         plainDB.useOldCrypto = true;
      
      ds.skipBytes(4); // It is not necessary to read the last position of the blobs and strings file.
      plainDB.headerSize = ds.readShort(); // Reads the header size.

      if (plainDB.headerSize == 0) // The header size can't be zero.
      {
         // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
         dbFile.close();
         plainDB.dbo.close();
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_TABLE_CORRUPTED) + name + '!');
      }
      
      // If the header needs to be bigger, re-creates the metadata buffer with the correct size and skips the bytes already read.
      if (plainDB.headerSize != bytes.length)
         (ds = new DataStreamLB(new ByteArrayStream(bytes = plainDB.readMetaData()), plainDB.useCrypto)).skipBytes(6);
      
      plainDB.dbo.finalPos = plainDB.dbo.size; // This does not let the user lose some database objects.

      // Checks if the table strings has the same format of the connection.
      if ((((flags = ds.readByte()) & IS_ASCII) != 0 && !plainDB.isAscii) || ((flags & IS_ASCII) == 0) && plainDB.isAscii) 
      {
         // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
         dbFile.close();
         plainDB.dbo.close();
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_WRONG_STRING_FORMAT));
      }
      
      // juliana@251_11: removed a possible exception when recovering an ascii table with indices on JavaSE and Blackberry.
      // juliana@220_2: added TableNotCreatedException which will be raised whenever a table is not closed properly.
      // If the table was not correctly closed, throws an specific exception to the user.
      if ((flags &= IS_SAVED_CORRECTLY) == 0 && throwException) 
      {
         // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
         dbFile.close();
         plainDB.dbo.close();
         throw new TableNotClosedException(name.substring(5));
      }  
        
      // juliana@230_12: improved recover table to take .dbo data into consideration.
      if ((version = ds.readShort()) < VERSION - 1) // The tables version must be the same as Litebase version.
      {
         // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
         dbFile.close();
         plainDB.dbo.close();
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_WRONG_VERSION) + (" (" + version) + ')');
      }
      
      deletedRowsCount = ds.readInt(); // Deleted rows count.
      auxRowId = ds.readInt(); // rnovais@570_61: reads the auxiliary rowid.

      // juliana@230_5: Corrected a AIOBE when using a table created on Windows 32, Windows CE, Linux, Palm, Android, iPhone, or iPad using 
      // primary key on BlackBerry and Eclipse.
      primaryKeyCol = ds.readByte(); // juliana@114_9: the simple primary key column.
      ds.skipBytes(1);
      composedPK = ds.readByte(); // The composed primary key index.    
      ds.skipBytes(1);
      columnCount = ds.readUnsignedShort(); // Reads the column count.

      int n = columnCount, 
              i = -1, 
              j;

      if (n <= 0) // The column count can't be negative.
      {
         // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
         dbFile.close();
         plainDB.dbo.close();
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_TABLE_CORRUPTED) + name + '!');
      }
      byte[] attrs = columnAttrs = new byte[n];
      int[] hashes = columnHashes = new int[n];
      byte[] types = columnTypes = new byte[n];
      int[] sizes = columnSizes = new int[n];
      SQLValue[] values = defaultValues = new SQLValue[n];

      columnIndices = new Index[n];
      storeNulls = new byte[(n + 7) >> 3];
      
      // The number of bytes necessary to store the columns. Each column in a table corresponds to one bit.
      // juliana@201_21: The null columns information must be created before openning the indices when reading the table meta data.
      j = (n + 7) >> 3; // Caution: n will be reused below.
      columnNulls[0] = new byte[j];
      columnNulls[1] = new byte[j];

      ds.readBytes(attrs, 0, n); // Reads the column attributes.
      ds.readBytes(types, 0, n); // Reads the column types.

      i = -1;
      while (++i < n) // Reads the column sizes.
         sizes[i] = ds.readInt();

      // juliana@224_1: corrected an inconsistency between java and native versions that would raise an <code>IndexOutOfBoundsException</code> when 
      // using a table created on Windows 32, Windows CE, Palm, Android, Linux, or iPhone on Java or BlackBerry.
      ds.skipBytes(2);
      String[] names = columnNames = ds.readStringArray(n); // Reads the column names.

      i = -1;
      while (++i < n) // Computes the hashes.
         hashes[i] = names[i].hashCode();

      computeColumnOffsets(); // Computes the column offsets.

      String fullName, 
             tableName = name;
      
      gvOlds = new SQLValue[n]; // Stores old values read from the table.
      ghas = new byte[n]; // Stores flags from the record.
      
      // Creates the primary key records. 
      primaryKeyValues = new SQLValue[n]; 
      primaryKeyOldValues = SQLValue.newSQLValues(n);
      
      i = n;
      nameAux = tableName + '$';
      while (--i >= 0) // Loads the indices.
         if ((attrs[i] & Utils.ATTR_COLUMN_HAS_INDEX) != 0)
         {
            // juliana@227_21: corrected a bug of recover table not working correctly if the table has indices.
            if ((exist = new File(fullName = Utils.getFullFileName(nameAux + i + ".idk", sourcePath)).exists()) && flags == 0)
            {
               idxFile = new File(fullName, File.READ_WRITE);
               idxFile.setSize(0);
               exist = false;
               idxFile.close();
            }
            
            indexCreateIndex(tableName, i, new int[]{sizes[i]}, new byte[]{types[i]}, appCrid, sourcePath, exist);
            if (!exist && flags != 0) // One of the files doesn't exist. juliana@227_21
            {
               // juliana@230_8: corrected a possible index corruption if its files are deleted and the application crashes after recreating it.
               setModified(); // Sets the table as not closed properly.
               tableReIndex(i, null, false);
            }
         }

      // Now the current rowid can be fetched.
      dbFile.setPos(plainDB.headerSize + (plainDB.rowCount > 0 ? plainDB.rowCount - 1 : 0) * plainDB.rowSize);
      currentRowId = (auxRowId != Utils.ATTR_DEFAULT_AUX_ROWID? auxRowId 
                                       : ((new DataStreamLB(dbFile, plainDB.useCrypto).readInt() & Utils.ROW_ID_MASK) + 1)) & Utils.ROW_ID_MASK;
      
      i = 0;
      while (++i < n) // Reads the default values.
         if ((attrs[i] & Utils.ATTR_COLUMN_HAS_DEFAULT) != 0) // Tests if it has default values.
         {
            values[i] = new SQLValue();

            switch (types[i])
            {
               case SQLElement.CHARS:
               case SQLElement.CHARS_NOCASE:
                  values[i].asString = new String(ds.readChars());
                  break;

               case SQLElement.SHORT:
                  values[i].asShort = ds.readShort();
                  break;

               case SQLElement.DATE: // stored as int.
               case SQLElement.INT:
                  values[i].asInt = ds.readInt();
                  break;

               case SQLElement.LONG:
                  values[i].asLong = ds.readLong();
                  break;

               case SQLElement.FLOAT:
                  values[i].asDouble = ds.readFloat();
                  break;

               case SQLElement.DOUBLE:
                  values[i].asDouble = ds.readDouble();
                  break;

               case SQLElement.DATETIME:
                  values[i].asInt = ds.readInt(); // date
                  values[i].asShort = ds.readInt(); // time
            }
         }

      int numCompIndices = numberComposedIndices = ds.readByte(); // Reads the composed indices.

      if (numCompIndices > 0)
      {
         int column,
             aComposedPK = composedPK,
             indexId,
             numColumns;
         byte[] columns;
         int[] columnSizes;
         byte[] columnTypes;
         ComposedIndex[] compIndices = composedIndices;

         i = -1;
         nameAux = tableName + '&';
         while (++i < numCompIndices)
         {
            indexId = ds.readByte(); // The composed index id.
            numColumns = ds.readByte(); // Number of columns on the composed index.
            ds.skipBytes(1);
            columns = new byte[numColumns];
            columnSizes = new int[numColumns];
            columnTypes = new byte[numColumns];

            j = -1;
            while (++j < numColumns)
            {
               column = columns[j] = ds.readByte(); // Columns of this composed index.
               columnSizes[j] = sizes[column];
               columnTypes[j] = types[column];
            }

            // juliana@227_21: corrected a bug of recover table not working correctly if the table has indices.
            if ((exist = new File(fullName = Utils.getFullFileName(nameAux + indexId + ".idk", sourcePath)).exists()) && flags == 0)
            {
               idxFile = new File(fullName, File.READ_WRITE);
               idxFile.setSize(0);
               exist = false;
               idxFile.close();
            }
            
            indexCreateComposedIndex(tableName, columns, columnSizes, columnTypes, indexId, aComposedPK == i, appCrid, false, sourcePath, exist);
            if (!exist && flags != 0) // One of the files doesn't exist.
            {
               // juliana@230_8: corrected a possible index corruption if its files are deleted and the application crashes after recreating it.
               setModified(); // Sets the table as not closed properly.
               tableReIndex(indexId - 1, compIndices[indexId - 1], false); // juliana@227_21
            }
            
         }
      }
      
      // Reads the composed primary key.
      if ((n = numberComposedPKCols = ds.readByte()) > 0)
      {
         byte[] compPrimaryKeyCols = composedPrimaryKeyCols = new byte[n];
         i = -1;
         while (++i < n)
            compPrimaryKeyCols[i] = ds.readByte(); // The composed primary key cols.
      }
   }
 
   // juliana@253_8: now Litebase supports weak cryptography.
   /**
    * Saves the table meta data
    *
    * @param saveType The kind of save. It can be one out of <code><B>TSMD_ONLY_DELETEDROWSCOUNT</B></code>, 
    * <code><B>TSMD_ONLY_PRIMARYKEYCOL</B></code>, <code><B>TSMD_EVERYTHING</B></code>, or <code><B>TSMD_ONLY_AUXROWID</B></code>.
    * @throws IOException If an internal method throws it.
    */
   void tableSaveMetaData(int saveType) throws IOException
   {
      // Stores the changeable information.
      int n = columnCount, 
          i = -1, 
          j = isModified? 0 : Table.IS_SAVED_CORRECTLY, 
          numberColumns;
      byte[] types = columnTypes;
      int[] sizes = columnSizes;
      byte[] attrs = columnAttrs;
      byte[] columns;
      SQLValue[] values = defaultValues;
      ComposedIndex[] compIndices = composedIndices;
      ComposedIndex ci;
      ByteArrayStream auxBas = tsmdBas;
      DataStreamLB auxDs = tsmdDs;
      PlainDB plainDB = db;
      
      if (auxBas != null) // If the buffer is not empty, only resets it.
         auxBas.reset();
      else // Otherwise, allocates it.
         auxDs = tsmdDs = new DataStreamLB(auxBas = tsmdBas = new ByteArrayStream(plainDB.headerSize), plainDB.useCrypto); 

      auxBas.getBuffer()[0] = (byte)(plainDB.useCrypto? (plainDB.useOldCrypto? 1 : USE_CRYPTO) : 0);
      auxDs.skipBytes(4); // The strings and blobs final position is deprecated.
      auxDs.writeShort(plainDB.headerSize); // Saves the header size.
      auxDs.writeByte(plainDB.isAscii? IS_ASCII | j : j); // juliana@226_4: table is not saved correctly if modified.
      auxDs.writeShort(version); // The table format version.
      auxDs.writeInt(deletedRowsCount); // Saves the deleted rows count.

      if (saveType != Utils.TSMD_ONLY_DELETEDROWSCOUNT) // More things other than the deleted rows count must be saved.
      {
         auxDs.writeInt(auxRowId); // rnovais@570_61: saves the auxiliary rowid.
    
         if (saveType != Utils.TSMD_ONLY_AUXROWID) // More things other than the auxiliary row id must be saved.
         {
        	   // juliana@230_5: Corrected a AIOBE when using a table created on Windows 32, Windows CE, Linux, Palm, Android, iPhone, or iPad using 
            // primary key on BlackBerry and Eclipse.
            auxDs.writeByte(primaryKeyCol); // Saves the primary key col.
            auxDs.writeByte(0);
            auxDs.writeByte(composedPK); // juliana@114_9: saves the composed primary key index.
            auxDs.writeByte(0);
            
            if (saveType != Utils.TSMD_ONLY_PRIMARYKEYCOL) // More things other than the primary key col must be saved.
            {
               auxDs.writeShort(n); // Saves the number of columns.
               auxDs.writeBytes(attrs, 0, n); // Saves the column attributes.
               
               if (saveType == Utils.TSMD_EVERYTHING) // Stores the rest.
               {
                  auxDs.writeBytes(types, 0, n); // Stores the column types.

                  i = -1;
                  while (++i < n) // Stores the column sizes.
                     auxDs.writeInt(sizes[i]);

                  auxDs.writeStringArray(columnNames); // Stores the column names.

                  i = 0;
                  while (++i < n) // Saves the default values.
                     if (values[i] != null)
                        switch (types[i])
                        {
                           case SQLElement.CHARS_NOCASE:
                           case SQLElement.CHARS:
                              String s = values[i].asString;
                              auxDs.writeChars(s, Math.min(s.length(), sizes[i]));
                              break;

                           case SQLElement.SHORT:
                              auxDs.writeShort(values[i].asShort);
                              break;

                           case SQLElement.DATE:
                           case SQLElement.INT:
                              auxDs.writeInt(values[i].asInt);
                              break;

                           case SQLElement.LONG:
                              auxDs.writeLong(values[i].asLong);
                              break;
                           case SQLElement.FLOAT:
                              auxDs.writeFloat(values[i].asDouble);
                              break;

                           case SQLElement.DOUBLE:
                              auxDs.writeDouble(values[i].asDouble);
                              break;

                           case SQLElement.DATETIME:
                              auxDs.writeInt(values[i].asInt);
                              auxDs.writeInt(values[i].asShort);
                        }
                  
                  auxDs.writeByte(n = numberComposedIndices); // Number of composed indices.

                  i = -1;
                  while (++i < n) // Stores the composed indices.
                  {
                     auxDs.writeByte((ci = compIndices[i]).indexId); // The composed index id.
                     auxDs.writeByte(numberColumns = ci.columns.length); // Number of columns on the composed index.
                     auxDs.writeByte(0); // Ignored.
                     columns = ci.columns;
                     j = -1;
                     while (++j < numberColumns)
                        auxDs.writeByte(columns[j]); // Columns of this composed index.
                  }

                  // Number of columns on composed primary key. If 0, there's no composed primary key.
                  auxDs.writeByte(n = numberComposedPKCols);
                  columns = composedPrimaryKeyCols;
                  i = -1;
                  while (++i < n) // Stores the composed primary key.
                     auxDs.writeByte(columns[i]); // The column of the composed primary key.
                  
               }
            }
         }
      }
      
      plainDB.writeMetaData(auxBas.getBuffer(), auxBas.getPos());
      ((NormalFile)(plainDB.db)).flushCache(); // juliana@223_11: table meta data is now always flushed imediately after being changed.
   }

   /**
    * Reorder the values of a statement to match the table definition.
    *
    * @param stmt An insert or an update statement.
    * @throws SQLParseException If a field in the field list does not exist.
    * @throws IOException If an internal method throws it.
    */
   void reorder(SQLStatement stmt) throws SQLParseException, IOException
   {
      byte[] nulls;
      byte[] tableNulls = storeNulls;
      String[] fields;
      SQLValue[] record;
      short[] paramIndexes; // juliana@253_14: corrected a possible AIOBE if the number of parameters of a prepared statement were greater than 128.
      SQLInsertStatement insertStmt = null;
      SQLUpdateStatement updateStmt = null;
      int idx,
          i = -1,
          length,
          numParams = 0;
      boolean isInsert = stmt.type == SQLElement.CMD_INSERT;

      if (isInsert) // Insert statement.
      {
         nulls = (insertStmt = (SQLInsertStatement)stmt).storeNulls; // Cleans the <code>storeNulls</code>.
         length = (fields = insertStmt.fields).length;
         paramIndexes = insertStmt.paramIndexes;
         record = insertStmt.record;
      }
      else // Update statement.
      {
         nulls = (updateStmt = (SQLUpdateStatement)stmt).storeNulls; // Cleans the <code>storeNulls</code>.
         length = (fields = updateStmt.fields).length;
         paramIndexes = updateStmt.paramIndexes;
         record = updateStmt.record;
      }

      if (fields[0] == null)
         fields[0] = "rowid"; // Inserts the rowid.

      SQLValue[] outRecord = new SQLValue[columnCount];
      IntHashtable hashTable = htName2index;

      Convert.fill(tableNulls, 0, tableNulls.length, 0); // Cleans the storeNulls.
      
      // juliana@230_9: solved a bug of prepared statement wrong parameter dealing.
      while (++i < length) // Makes sure the fields are in db creation order.
         try
         {
            outRecord[idx = hashTable.get(fields[i].hashCode())] = record[i]; // Finds the index of the field on the table and reorders the record.
            Utils.setBit(tableNulls, idx, (nulls[i >> 3] & (1 << (i & 7))) != 0);
            if (record[i] != null && record[i].asString != null && record[i].asString.equals("?"))
               paramIndexes[numParams++] = (short)idx;
         }
         catch (ElementNotFoundException enfe)
         {
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + fields[i]);
         }

      // Saves the ordered record.
      if (isInsert)
      {
         insertStmt.record = outRecord;
         insertStmt.storeNulls = tableNulls;
      }
      else
      {
         updateStmt.record = outRecord;
         updateStmt.storeNulls = tableNulls;
      }
   }

   // rnovais@570_75 juliana@220_2
   // juliana@253_8: now Litebase supports weak cryptography.
   /**
    * Creates the table files and loads its meta data if it was already created.
    *
    * @param sourcePath The path of the table on disk.
    * @param newName The name of the table.
    * @param create Indicates if the table is to be created or just opened.
    * @param appCrid The application id of the table.
    * @param driver The connection with Litebase.
    * @param ascii Indicates if the table strings are to be stored in the ascii format or in the unicode format.
    * @param crypto Indicates if the table is to be stored encrypted or not.
    * @param throwException Indicates that a TableNotClosedException should be thrown.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void tableCreate(String sourcePath, String newName, boolean create, String appCrid, LitebaseConnection driver, boolean ascii, boolean crypto, 
                                                                       boolean throwException) throws IOException, InvalidDateException 
   {
      PlainDB plainDB = db = new PlainDB(newName, sourcePath, create); // Creates or opens the table files.      
      plainDB.driver = driver;
      if (newName != null && (plainDB.db.size != 0 || create)) // The table is already created if the .db is not empty.
      {
         name = newName;
         plainDB.isAscii = ascii;
         plainDB.useCrypto = crypto;
         if (plainDB.db.size != 0) // If the table is already created, loads its meta data.
            tableLoadMetaData(appCrid, sourcePath, throwException);
      }
   }
   
   // rnovais@566_10
   /**
    * Renames a table. This never happens to be a temporary <code>ResultSet</code> memory table.
    *
    * @param driver The LitebaseConnection.
    * @param oldTableName The old table name.
    * @param newTableName The new table name.
    * @throws IOException If an internal method throws it.
    */
   void renameTable(LitebaseConnection driver, String oldTableName, String newTableName) throws IOException
   {
      String tableFullName = driver.appCrid + '-' + newTableName;
      Index index;
      Index[] indices = columnIndices;
      String nameIndex,
             newFullName;
      NormalFile fnodes;

      // Renames the table.
      db.rename(tableFullName, driver.sourcePath);
      name = tableFullName;
      driver.htTables.remove(oldTableName);
      driver.htTables.put(newTableName, this); // Adds the new table name to the hash table.

      // Renames the indices.
      int i = columnCount;
      
      while (--i >= 0)
         if (indices[i] != null)
         {
            index = indices[i];
            newFullName = Utils.getFullFileName(nameIndex = (tableFullName + '$') + i, driver.sourcePath);
            
            (fnodes = index.fnodes).f.rename(newFullName + ".idk"); // Keys.
            fnodes.f = new File(newFullName + ".idk", File.READ_WRITE);
            index.name = nameIndex;
         }
      
      // juliana@220_17: rename table now renames the composed indices.
      i = numberComposedIndices;
      ComposedIndex[] compIndices = composedIndices;
      while (--i >= 0)
      {
         newFullName = Utils.getFullFileName(nameIndex = (tableFullName + '&') + (i+1), driver.sourcePath);
         index = compIndices[i].index;
         (fnodes = index.fnodes).f.rename(newFullName + ".idk"); // Keys.
         fnodes.f = new File(newFullName + ".idk", File.READ_WRITE);
         index.name = nameIndex; 
      }
   }
   
   /**
    * Renames a column of a table.
    *
    * @param oldColumn The name of the old column.
    * @param newColumn The name of the new column.
    * @throws IOException If an internal method throws it.
    * @throws DriverException If the old column does not exist or the new column already exists.
    */
   void renameTableColumn(String oldColumn, String newColumn) throws IOException, DriverException
   {
      int oldHashCode = oldColumn.hashCode();
      IntHashtable ht = htName2index;
      
      // Gets the old column name. It must exist.
      int oldIdx = ht.get(oldHashCode, -1);
      if (oldIdx == -1)
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + oldColumn);

      int newHashCode = newColumn.hashCode();

      if (ht.exists(newHashCode)) // The new column name can't exist.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_COLUMN_ALREADY_EXIST) + newColumn);
      
      try // Changes the column information.
      {
         ht.remove(oldHashCode);
      }
      catch (ElementNotFoundException exception) {}
      ht.put(newHashCode, oldIdx);
      columnHashes[oldIdx] = newHashCode;
      columnNames[oldIdx] = newColumn;
      tableSaveMetaData(Utils.TSMD_EVERYTHING);
   }

   /**
    * Sets the meta data for a table.
    *
    * @param names The table column names.
    * @param hashes The table column names hash codes.
    * @param types The table column types.
    * @param sizes The table column sizes.
    * @param attrs The table column attributtes.
    * @param values The default values of the table columns.
    * @param pkCol The table primary key column.
    * @param composedPKIdx The composed primary key index in the composed indices.
    * @param composedPKColums The table composed primary columns.
    * @param ComposedPKColsSize The number of composed primary keys.
    * @throws AlreadyCreatedException if the table is already created.
    * @throws IOException If an internal method throws it. 
    */
   void tableSetMetaData(String[] names, int[] hashes, byte[] types, int[] sizes, byte[] attrs, SQLValue[] values, int pkCol,
                         int composedPKIdx, byte[] composedPKColums, int ComposedPKColsSize) throws AlreadyCreatedException, IOException
   {
      // Sets the number of columns.
      int count = columnCount = hashes.length;
      int bytes;

      columnHashes = hashes; // Sets the column hashes.
      columnTypes = types; // Sets the column types.
      columnSizes = sizes; // Sets the column sizes.
      storeNulls = new byte[bytes = (count + 7) >> 3]; // Initializes the arrays for the nulls.
      
      // The number of bytes necessary to store the nulls. Each column in a table correspond to one bit.
      columnNulls[0] = new byte[bytes]; 
      columnNulls[1] = new byte[bytes];

      computeColumnOffsets(); // Computes the column offests.

      // Saves the meta data after everything was set.
      if (name != null) // juliana@201_14: It is not necessary to save the meta data in the .db for memory tables.
      {
         if (db.db.size != 0) // The table can't be already created.
            throw new AlreadyCreatedException(LitebaseMessage.getMessage(LitebaseMessage.ERR_TABLE_ALREADY_CREATED));
         
         version = VERSION; // juliana@230_12
         
         columnNames = names; // Sets the column names.
         defaultValues = values; // Sets the defaut values.
         columnIndices = new Index[count]; // Initializes the indices.
         columnAttrs = attrs;  // Sets the column attributes.
         primaryKeyCol = pkCol; // Primary key column.
         composedPK = composedPKIdx; // Composed primary key index. 
         
         // Sets the composed primary key info.
         numberComposedPKCols = ComposedPKColsSize;
         composedPrimaryKeyCols = composedPKColums;
         
         gvOlds = new SQLValue[count]; // Stores old values read from the table.
         ghas = new byte[count]; // Stores flags from the record.
         
         // Creates the primary key records. 
         primaryKeyValues = new SQLValue[count]; 
         primaryKeyOldValues = SQLValue.newSQLValues(count);
         
         tableSaveMetaData(Utils.TSMD_EVERYTHING); // Saves the metadata after everything was set.  
      }
      else
         columnNulls[2] = new byte[bytes]; 
   }

   /**
    * Creates a result set.
    *
    * @param whereClause The condition of the query.
    * @return The created result set.
    */
   ResultSet createResultSet(SQLBooleanClause whereClause)
   {
      ResultSet rs = new ResultSet();
      rs.pos = -1; // guich@300: beforeFirst();
      rs.table = this;
      rs.lastRecordIndex = db.rowCount - 1;
      rs.whereClause = whereClause;
      return rs;
   }

   /**
    * Creates a simple result set.
    *
    * @param whereClause The condition of the query.
    * @return The created result set.  
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.  
    */
   ResultSet createSimpleResultSet(SQLBooleanClause whereClause) throws IOException, InvalidDateException
   {
      ResultSet rs = new ResultSet();
      rs.pos = -1; // guich@300: beforeFirst();
      rs.table = this;
      rs.lastRecordIndex = db.rowCount - 1;
      rs.whereClause = whereClause;
      if (whereClause != null) // tries to apply the table indexes to generate a bitmap of the rows to be returned.
         SQLSelectStatement.generateIndexedRowsMap(new ResultSet[] {rs}, rs.table.numberComposedIndices > 0);
      return rs;
   }

   /**
    * Creates a result set, not used for joins.
    *
    * @param whereClause The condition of the query.
    * @return The created result set.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it. 
    */
   ResultSet createResultSetForSelect(SQLBooleanClause whereClause) throws IOException, InvalidDateException
   {
      // Apply table indices, if any.
      ResultSet rs = createSimpleResultSet(whereClause);
      rs.columnCount = columnCount;
      return rs;
   }

   // juliana@201_3: if an index is created after populating or purging the table, its nodes will be full in order to improve its usage and search 
   // speed.
   /**
    * Re-builds an index of a table.
    *
    * @param column The table column number of the index or -1 for a composed index.
    * @param composedIndex The composed index to be rebuilt or null in case of a simple index.
    * @param isPrimaryKey Indicates that the index is of a primary key.
    * @throws DriverException If there is a null in the primary key or a duplicated key.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it. 
    */
   void tableReIndex(int column, ComposedIndex composedIndex, boolean isPrimaryKey) throws DriverException, IOException, InvalidDateException
   {
      // Gets the index.
      Index index = (composedIndex == null) ? columnIndices[column] : composedIndex.index;
      PlainDB plainDb = db;
      int numberColumns = index.types.length,  // Gets the number of columns.
          i = -1, 
          j, 
          n = plainDb.rowCount; 
      short[] offsets = columnOffsets;
      byte[] types = index.types;
      byte[] columns = (composedIndex != null) ? composedIndex.columns : null;        
      byte[] nulls = columnNulls[0];
      boolean isDelayed = index.isWriteDelayed;
      
      index.deleteAllRows(); // Cleans the index values.
      index.setWriteDelayed(true); // This makes the index creation faster.
      
      // rnovais@570_24 juliana@114_9: checks if the column being reindexed is the primary key columns.
      ((NormalFile)plainDb.dbo).loadIntoMemory(true);
      ((NormalFile)plainDb.db).loadIntoMemory(true);

      try
      {
         if (index.isOrdered && composedIndex == null) // Simple index using rowid.
         {
            SQLValue vals[] = SQLValue.newSQLValues(numberColumns);
            while (++i < n)
            {
               plainDb.read(i); // Reads the row.
               if (!plainDb.recordNotDeleted()) // Only gets non-deleted records.
                  continue;
               readValue(vals[0], offsets[0], SQLElement.INT, false, false); // juliana@220_3 juliana@230_14
               index.indexAddKey(vals, i);
            }
         }
         else 
         {
            int rows = plainDb.rowCount; // juliana@284_1: Solved a possible application crash when recreating indices.
            SQLValue[][] vals = new SQLValue[rows][];
            int k = 0;
            boolean isNull;
            
            while (++i < n)
            {
               isNull = false; // Resets the null info.
               plainDb.read(i); // Reads the row.
               if (!plainDb.recordNotDeleted()) // Only gets non-deleted records.
                  continue;
               readNullBytesOfRecord(0, false, 0); // juliana@201_22: the null columns information wasn't being read when re-creating an index.
               vals[k] = SQLValue.newSQLValues(numberColumns);
               
               if (composedIndex == null)
               {
                  // juliana@230_14
                  // juliana@220_3
                  // juliana@202_12: Corrected null values dealing when building an index.
                  readValue(vals[k][0], offsets[column], types[0], isNull = (nulls[column >> 3] & (1 << (column & 7))) != 0, false);

               
                  // The primary key can't be null.
                  // juliana@202_10: Corrected a bug that would cause a DriverException if there was a null in an index field when creating it after 
                  // the table is populated.
                  if (isPrimaryKey && isNull)
                     throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PK_CANT_BE_NULL));
               }
               else
               {
                  j = numberColumns;
                  while (--j >= 0)
                  {
                     // juliana@230_14
                     // juliana@220_3
                     // juliana@202_12: Corrected null values dealing when building an index.
                     readValue(vals[k][j], offsets[columns[j]], types[j],  
                                                                isNull |= (nulls[columns[j] >> 3] & (1 << (columns[j] & 7))) != 0, false);
                     
                     // The primary key can't have a null.
                     // juliana@202_10: Corrected a bug that would cause a DriverException if there was a null in an index field when creating it 
                     // after the table is populated.
                     if (isPrimaryKey && isNull)
                        throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PK_CANT_BE_NULL));
                  }
               }
               
               if (isNull) // Do not store null records.
                  rows--;
               else // juliana@212_2: DATETIME indices would be recreated incorrectly on desktop and BlackBerry.
                  if (types[0] == SQLElement.LONG) // The record value is stored in an empty field of the first record column value.
                     vals[k++][0].asInt = i;
                  else
                     vals[k++][0].asLong = i;
            }
            
            rows = k; // juliana@270_22: solved a possible crash when the table is corrupted on Android and possibly on other platforms.
            if (!index.isOrdered)
            {
               // A radix sort is done for integer types. It is much more efficient than quick sort.
               if (numberColumns == 1 
                && (types[0] == SQLElement.SHORT || types[0] == SQLElement.INT || types[0] == SQLElement.LONG || types[0] == SQLElement.DATE))
                  radixSort(vals, types[0], new SQLValue[rows][]);
                  
               else               
                  sortRecords(vals, types, 0, rows - 1);
               index.isOrdered = true; // The index elements will be inserted in the right order.
            }   
            int count = -1,
                compare;
            while (++count < rows)
            {
               // if it is the primary key, checks first if there is violation.
               if (isPrimaryKey && count > 0 && ((compare = compareRecords(vals[count], vals[count - 1], types)) == 0 
                                              || compare == Convert.MAX_INT_VALUE || compare == Convert.MIN_INT_VALUE))
                  throw new PrimaryKeyViolationException(LitebaseMessage.getMessage(LitebaseMessage.ERR_STATEMENT_CREATE_DUPLICATED_PK) + name);
               
               if (types[0] == SQLElement.LONG)
                  index.indexAddKey(vals[count], vals[count][0].asInt);
               else
                  index.indexAddKey(vals[count], (int)vals[count][0].asLong);
               if (count > 0) 
                  vals[count - 1] = null;
            }
            vals = null;
            
            // An index beggining with rowid is always ordered. 
            if (composedIndex == null || composedIndex.columns[0] != 0)
               index.isOrdered = false;
         }
      }
      finally
      {
         index.setWriteDelayed(isDelayed); // Uses the user desired delayed settings again.
         ((NormalFile)plainDb.dbo).loadIntoMemory(false);
         ((NormalFile)plainDb.db).loadIntoMemory(false);
      }         
   }

   // juliana@230_14
   // juliana@220_3: blobs are not loaded anymore in the temporary table when building result sets.
   /**
    * Reads a value from a table.
    *
    * @param value The value to be read.
    * @param offset The offset of the value in its row.
    * @param colType The type of the value.
    * @param isNull Indicates if the value is null.
    * @param isTempBlob Indicates if the blob is not to be loaded on memory.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void readValue(SQLValue value, int offset, int colType, boolean isNull, boolean isTempBlob) throws IOException, InvalidDateException
   {
      PlainDB plainDB = db;
      ByteArrayStream bas = plainDB.bas;
      bas.skipBytes(offset); // Skips the first columns.
      offset = plainDB.readValue(value, offset, colType, plainDB.basds, name == null, isNull, isTempBlob); // Reads the value
      bas.skipBytes(-offset); // Returns to the first column.
   }
   
   /**
    * Creates a simple index for the table for the given column.
    *
    * @param fullTableName The table disk name.
    * @param column The column of the index.
    * @param columnSizes The sizes of the columns.
    * @param columnTypes The types of the columns.
    * @param crid The application id of the table.
    * @param sourcePath The folder where the table files are stored.
    * @param exist Indicates that the index files already exist. 
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void indexCreateIndex(String fullTableName, int column, int[] columnSizes, byte[] columnTypes,
                                                           String crid, String sourcePath, boolean exist) throws IOException, InvalidDateException
   {
      String name = (fullTableName + '$') + column; // The index name.

      // Creates a new index structure.
      Index index = columnIndices[column] = new Index(this, columnTypes, columnSizes, name, sourcePath, exist);
      
      index.isOrdered = (column == 0); // rowid is always an ordered index.
      
      columnAttrs[column] |= Utils.ATTR_COLUMN_HAS_INDEX; 
   }

   /**
    * Creates a composed index for a given table.
    *
    * @param fullTableName The table disk name.
    * @param columnIndices he columns of the index.
    * @param columnSizes The sizes of the columns.
    * @param columnTypes The types of the columns.
    * @param newIndexNumber An id for the composed index.
    * @param isPK Indicates if the composed index is a composed primary key.
    * @param crid The application id of the table.
    * @param increaseArray Indicates if the composed indices array must be increased.
    * @param sourcePath The folder where the table files are stored.
    * @param exist Indicates that the index files already exist. 
    * @throws DriverException If the maximum number of composed indices was achieved.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void indexCreateComposedIndex(String fullTableName, byte[] columnIndices, int[] columnSizes, byte[] columnTypes, int newIndexNumber, boolean isPK, 
                   String crid, boolean increaseArray, String sourcePath, boolean exist) throws DriverException, IOException, InvalidDateException
   {
      ComposedIndex ci;
      int size = numberComposedIndices;

      String name = (fullTableName + '&') + newIndexNumber; // Passes the newIndex index id.

      if (isPK) // It it is a composed primary key, sets its attribute.
         composedPK = newIndexNumber - 1;
      
      ComposedIndex[] compIndices = composedIndices;
      ci = new ComposedIndex(newIndexNumber, columnIndices);
      
      // juliana@253_9: improved Litebase parser.
      
      if (increaseArray)
      {
         compIndices[size] = ci; // New composed index.
         numberComposedIndices++;
      }
      else
         compIndices[newIndexNumber - 1] = ci;   
      ci.index = new Index(this, columnTypes, columnSizes, name, sourcePath, exist); // Creates the index of the composed index.
      ci.index.isOrdered = (columnIndices[0] == 0); // The rowid is the column 0.
   }
   
   // Replaces writeRecord(), which received an array of strings.
   /**
    * Writes a record from an array of values in a result set.
    *
    * @param values The record to be written in the result set table.
    * @throws IOException If an internal method throws it.
    */
   void writeRSRecord(SQLValue[] values) throws IOException
   {
      // Important: this is an optimized version of writeRecord, specially designed to write the values of a resultset.
      int n = columnCount, 
           i;
      PlainDB plainDB = db;
      DataStreamLB ds = db.basds; // juliana@253_8: now Litebase supports weak cryptography.
      int[] sizes = columnSizes;
      byte[] types = columnTypes;
      byte[] nulls = columnNulls[0];

      plainDB.add(); // Adds a new row to the result set table.

      // Writes the columns into a temporary buffer.
      i = -1;
      while (++i < n) 
         plainDB.writeValue(types[i], values[i], ds, (nulls[i >> 3] & (1 << (i & 7))) == 0, true, sizes[i], 0, true); // juliana@220_3

      ds.writeBytes(nulls); // Writes the null values.
      plainDB.write(); // Finally, writes the row.
   }
   
   /**
    * Checks if a primary key constraint was violated
    *
    * @param val The values inserted in the table.
    * @param recPos The position of vals record.
    * @param newRecord Indicates if it is an inserted or an updated record.
    * @param values An array of values used if the record is not new.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws DriverException If a member of the primary key is null
    */
   private void checkPrimaryKey(SQLValue[] vals, int recPos, boolean newRecord, SQLValue[] values) throws IOException, InvalidDateException, DriverException
   {
      int pKCol = primaryKeyCol; // Simple primary key column.
         
      
      // If the column is a simple primary key, gets the index of its column. Otherwise, gets the index of its composed primary key.
      Index index = (pKCol != -1) ? columnIndices[pKCol] : composedIndices[composedPK].index;

      byte[] columns;
      boolean hasChanged = false;
      byte[] types = index.types;
      short[] offsets = columnOffsets;

      if (pKCol == -1) // Gets the columns of the index.
         columns = composedIndices[composedPK].columns;
      else
         columns = new byte[]{(byte)pKCol};
      int i = columns.length;

      if (!newRecord) // An update.
      {
         db.read(recPos); // Reads the table row.
         
         while (--i >= 0)
         {
            // If it is updating a record, reads the old value and checks if a primary key value has changed.
            readValue(values[i], offsets[columns[i]], types[i], false, false); // juliana@220_3 juliana@230_14
            
            // Tests if the primary key has not changed.
            hasChanged |= vals[i] != null && vals[i].valueCompareTo(values[i], types[i], false, false, null) != 0;
            
            if (vals[i] == null) // Uses the old value.
               vals[i] = values[i];
         }
      }

      // There can't be a null in a primary key.
      i = columns.length;
      while (--i >= 0)
         if (vals[i] == null)
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PK_CANT_BE_NULL));

      if (hasChanged || newRecord) // Sees if the record does not violate the primary key.
      {
         index.tempKey.set(vals);
         index.getValue(index.tempKey, null);
      }
   }
   
   
   /**
    * Verifies the null and default values of a statement.
    * 
    * @param record The record to be inserted or updated.
    * @param storeNullsStmt The <code>storeNulls</code> of the statement.
    * @param statementType The type of the statement, which can be <code>SQLElement.STMT_INSERT</code> or
    *           <code>SQLElement.STMT_UPDATE</code>.
    * @throws DriverException If a primary key is or a <code>NOT NULL</code> field is is <code>null</code>.
    */
   void verifyNullValues(SQLValue[] record, byte[] storeNullsStmt, int statementType) throws DriverException
   {
      int len = record.length;
      byte[] attrs = columnAttrs;
      byte[] nulls = storeNulls;

      if (statementType == SQLElement.CMD_INSERT) // Insert statement.
      {
         SQLValue[] defaults = defaultValues;
         
         if (primaryKeyCol != Utils.NO_PRIMARY_KEY && (nulls[primaryKeyCol >> 3] & (1 << (primaryKeyCol & 7))) != 0) // The primary key can't be null.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PK_CANT_BE_NULL));

         while (--len > 0) // A not null field can't have a null.
            if ((record[len] == null || record[len].isNull) && defaults[len] == null && (attrs[len] & Utils.ATTR_COLUMN_IS_NOT_NULL) != 0)
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_CANT_BE_NULL) + columnNames[len]);
      }
      else // Update statement.
      {
         if (primaryKeyCol != Utils.NO_PRIMARY_KEY && (nulls[primaryKeyCol >> 3] & (1 << (primaryKeyCol & 7))) != 0) // The primary key can't be null.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PK_CANT_BE_NULL));
         while (--len > 0) // If it is to store a null but a null can't be stored.
            if ((nulls[len >> 3] & (1 << (len & 7))) != 0 && (attrs[len] & Utils.ATTR_COLUMN_IS_NOT_NULL) != 0) 
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_CANT_BE_NULL) + columnNames[len]);
      }
   }
   
   /**
    * Writes the records of a result set to a table.
    *
    * @param list The result set list, one for each table in the from field.
    * @param rs2TableColIndexes The mapping between result set and table columns.
    * @param selectClause The select clause of the query.
    * @param columnIndexesTables Has the indices of the tables for each resulting column.
    * @param whereClauseType Indicates the where clause is an <code>AND</code> or an <code>OR</code>.
    * @return The total number of records added to the table.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    */
   int writeResultSetToTable(ResultSet[] list, short[] rs2TableColIndexes, SQLSelectClause selectClause, Table[] columnIndexesTables, 
                                               int whereClauseType) throws IOException, InvalidDateException, InvalidNumberException
   {
      int count = columnCount, 
          i = count,
          j, 
          colIndex;

      // rnovais@568_10: when it has an order by table.columnCount = selectClause.fieldsCount + 1.
      int totalRecords = 0;
      ResultSet rs;
      SQLValue[] values = SQLValue.newSQLValues(count);
      boolean hasJoin = list.length > 1;
      byte[] rsColumnNulls;
      byte[] nulls = columnNulls[0];

      if (!hasJoin)
      {
         rs = list[0];
         
         int size = 0;
         int[] sizes = columnSizes;
         j = columnCount;
         
         while (--j >= 0)
            if (sizes[j] > 0)
               size += 8;
         
         rsColumnNulls = rs.table.columnNulls[0];

         // If there's no where clause, allocate at once all the needed records.
         // If there are indices, grows the result set table to the number of index records which satisfy the query. 
         // This reduces a lot the number of "new"s to increase the temporary table.
         if (rs.whereClause == null)
         {
            PlainDB plainDB = db;
            
            plainDB.rowAvail = (rs.rowsBitmap == null? rs.table.db.rowCount - rs.table.deletedRowsCount : Utils.countBits(rs.rowsBitmap.items));
            plainDB.db.growTo(plainDB.rowAvail++ * plainDB.rowSize);
            plainDB.dbo.growTo(plainDB.rowAvail * size);
         }
         
         rs.pos = -1;
         
         // If rs2TableColIndexes == null, that indicates that the result set and the table have the same sequence of columns.
         if (rs2TableColIndexes == null)
            while (rs.getNextRecord()) // No preverify needed.
            {
               rs.table.readNullBytesOfRecord(0, false, 0); // Reads the bytes of the nulls.
               Vm.arrayCopy(rsColumnNulls, 0, nulls, 0, rsColumnNulls.length); // Since all the columns match, just copies the nulls.
 
                  i = count;
                  while (--i >= 0)  // Gets the values of the result set columns.
                  if ((rsColumnNulls[i >> 3] & (1 << (i & 7))) == 0) // If it is null, just skips.
                     rs.sqlwhereclausetreeGetTableColValue(i, values[i]);

               // Writes the record.
               writeRSRecord(values);
               totalRecords++;
            }
         else
         {
            boolean isNull;

            while (rs.getNextRecord()) // No preverify needed.
            {
               rs.table.readNullBytesOfRecord(0, false, 0); // Reads the bytes of the nulls.
               
               i = count;
               while (--i >= 0) // Gets the values of the result set columns.
               {
                  // For columns that do not map directly to the underlying table of the result set, just skips the reading.
                  if (!(isNull = ((colIndex = rs2TableColIndexes[i]) == -1 || (rsColumnNulls[colIndex >> 3] & (1 << (colIndex & 7))) != 0)))
                     rs.sqlwhereclausetreeGetTableColValue(colIndex, values[i]);

                  Utils.setBit(nulls, i, isNull); // Sets the null values for tempTable.
               }

               // Writes the record.
               writeRSRecord(values);
               totalRecords++;
            }
         }
                  
         if (rs.table.name == null)
            rs.table.db = null;
      }
      else
      {
         i = rs2TableColIndexes.length;
         while (--i >= 0) // join
            if (rs2TableColIndexes[i] != -1) // count(*)
            {
               j = list.length;
               while (--j >= 0)
                  if (columnIndexesTables[i].equals(list[j].table))
                  {
                     list[j].indices.addElement(i);
                     break;
                  }
            }
         return performJoin(list, rs2TableColIndexes, selectClause, values, whereClauseType);
      }

      return totalRecords;
   }
   
   /**
    * Executes a join operation.
    * 
    * @param list The list of the result sets.
    * @param rs2TableColIndexes The mapping between result set and table columns.
    * @param selectClause The select clause.
    * @param valuesJoin The record to be joined with.
    * @param whereClauseType The type of operation used: <code>AND</code> or <code>OR</code>.
    * @return The number of records written to the temporary table.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    */
   private int performJoin(ResultSet[] list, short[] rs2TableColIndexes, SQLSelectClause selectClause, SQLValue[] valuesJoin, int whereClauseType) throws IOException, InvalidDateException, InvalidNumberException
   {
      int numTables = list.length, 
          currentIndexTable = 0, 
          ret, 
          totalRecords = 0,
          colIndex,
          pos,
          len;
      boolean bitSet;
      ResultSet currentRs;
      Table table;
      boolean[] ignoreWhereCondition = new boolean[numTables];
      byte[] nulls;
      
      while (currentIndexTable >= 0)
      {
         currentRs = list[currentIndexTable];
         table = currentRs.table;
         nulls = columnNulls[0];
         ret = getNextRecordJoin(list, currentIndexTable, !ignoreWhereCondition[currentIndexTable], whereClauseType);
         switch (ret)
         {
            case VALIDATION_RECORD_OK:
            case VALIDATION_RECORD_INCOMPLETE:
               table.readNullBytesOfRecord(0, false, 0);
               
               // Fills the data of the current ResultSet.
               len = currentRs.indices.size();
               while (--len >=0)
               {
                  pos = currentRs.indices.items[len];
                  
                  // If rs2TableColIndexes == null, it indicates that the result set and the table have the same sequence of columns.
                  colIndex = (rs2TableColIndexes == null? len : rs2TableColIndexes[pos]);

                  // For columns that do no map directly to the underlying table of the result set, just skips the reading.
                  bitSet = (table.columnNulls[0][colIndex >> 3] & (1 << (colIndex & 7))) != 0;
                  if (colIndex != -1 && !bitSet) // If it is null, just skips.
                     currentRs.sqlwhereclausetreeGetTableColValue(colIndex, valuesJoin[pos]);

                  Utils.setBit(nulls, pos, colIndex != -1 && bitSet); // Sets the null values from the temporary table.

               }
               if (ret == VALIDATION_RECORD_OK)
               {
                  if (currentIndexTable < numTables - 1) // Goes to the next table.
                     ignoreWhereCondition[++currentIndexTable] = true;
                  else
                  {
                     // It is the last resultSet, so stores the data.
                     writeRSRecord(valuesJoin);
                     totalRecords++; 
                  }
               }
               else // VALIDATION_RECORD_INCOMPLETE
                  ignoreWhereCondition[++currentIndexTable] = false;
               break;
            case NO_RECORD:
               currentIndexTable--;
               currentRs.pos = -1; // Restarts the current resultset to the next iteration.
         }
      }
      return totalRecords;
   }
   
   /**
    * Gets the next record to perform the join operation.
    * 
    * @param list The list of the result sets.
    * @param rsIndex The index of the result set of the list used to get the next record.
    * @param verifyWhereCondition Indicates if the where clause needs to be verified.
    * @param whereClauseType The type of expression in the where clause (OR or AND).
    * @return <code>VALIDATION_RECORD_OK</code>, <code>NO_RECORD</code>, <code>VALIDATION_RECORD_NOT_OK</code>, or
    * <code>VALIDATION_RECORD_INCOMPLETE</code>.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    */
   private int getNextRecordJoin(ResultSet[] list, int rsIndex, boolean verifyWhereCondition, int whereClauseType) throws IOException, 
                                                                                                InvalidDateException, InvalidNumberException
   {
      ResultSet rs = list[rsIndex];
      IntVector auxRowsBitmap = rs.auxRowsBitmap;
      int len = list.length,
          ret,
          last = rs.lastRecordIndex;
      PlainDB db = rs.table.db;
      SQLBooleanClause whereClause = rs.whereClause;
      IntVector rowsBitmap = (auxRowsBitmap != null)? auxRowsBitmap : rs.rowsBitmap;
      
      if (rowsBitmap != null && verifyWhereCondition)
      {
         int p;
         
         int[] items = rowsBitmap.items;
         if (rs.pos < last)
            if (whereClause == null || auxRowsBitmap != null) // count_index++;
            {
               // juliana@230_45: join should not take deleted rows into consideration.
               // No WHERE clause. Just returns the rows marked in the bitmap.
               while ((p = Utils.findNextBitSet(items, rs.pos + 1)) != -1 && p <= last)
               {   
                  db.read(rs.pos = p);
                  if (db.recordNotDeleted())
                  {
                     if (verifyWhereCondition && auxRowsBitmap != null)
                        return booleanTreeEvaluateJoin(list, (list[rsIndex].whereClause.resultSet 
                                                                       = list[rsIndex]).whereClause.expressionTree);
                     if (whereClauseType == Utils.WC_TYPE_AND_DIFF_RS)
                        return (len == rs.indexRs + 1)? VALIDATION_RECORD_OK : VALIDATION_RECORD_INCOMPLETE;
                     return VALIDATION_RECORD_OK;
                  }

               }
            }
            else
               // With a remaining WHERE clause there are 2 situations.
               // 1) The relationship between the bitmap and the WHERE clause is an AND relationship.
               // 2) The relationship between the bitmap and the WHERE clause is an OR relationship.
               if (rs.rowsBitmapBoolOp == SQLElement.OP_BOOLEAN_AND)
               {
                  // AND case - Walks through the bits that are set in the bitmap and checks if the rows satisfy the where clause.
                  // juliana@230_45: join should not take deleted rows into consideration.
                  while ((p = Utils.findNextBitSet(items, rs.pos + 1)) != -1 && p <= last)
                  {   
                     db.read(rs.pos = p);
                     if (db.recordNotDeleted())
                        return booleanTreeEvaluateJoin(list, (list[rsIndex].whereClause.resultSet = list[rsIndex]).whereClause.expressionTree);
                  }
               }
               else
                  // OR case - Walks through all records. If the corresponding bit is set in the bitmap, does not need to evaluate WHERE clause.
                  // Otherwise, checks if row satisfies WHERE clause.
                  while (rs.pos < last)
                  {
                     db.read(++rs.pos);
                     if (db.recordNotDeleted())
                     {
                        if (rowsBitmap.isBitSet(rs.pos))
                           return VALIDATION_RECORD_OK;
                        if ((ret = booleanTreeEvaluateJoin(list, (list[rsIndex].whereClause.resultSet 
                                                                          = list[rsIndex]).whereClause.expressionTree)) != VALIDATION_RECORD_NOT_OK)
                           return ret;
                     }
                  }
         rs.auxRowsBitmap = null;
         return NO_RECORD;
      }
      else
         while (rs.pos < last) // count_noindex++;
         {
            db.read(++rs.pos);
            if (db.recordNotDeleted())
            {
               if (whereClause == null || !verifyWhereCondition)
                  return VALIDATION_RECORD_OK;
               
               // juliana@213_2: corrected a bug that could make joins not work with ORs using indices.
               ret = booleanTreeEvaluateJoin(list, (rs.whereClause.resultSet = list[rsIndex]).whereClause.expressionTree);
               if (ret == VALIDATION_RECORD_NOT_OK && rs.whereClause.appliedIndexesBooleanOp == SQLElement.OP_BOOLEAN_OR)
                  while (++rsIndex < len)
                     if (list[rsIndex].auxRowsBitmap != null || list[rsIndex].rowsBitmap != null)
                        return VALIDATION_RECORD_INCOMPLETE;
               return ret;
            }
         }
      
      return NO_RECORD;
   }
   
   /**
    * Evaluates an expression tree for a join.
    * 
    * @param list The list of the result sets.
    * @param tree The expression tree to be evaluated.
    * @return <code>VALIDATION_RECORD_OK</code>, <code>NO_RECORD</code>, <code>VALIDATION_RECORD_NOT_OK</code>, or
    * <code>VALIDATION_RECORD_INCOMPLETE</code>.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    */
   private int booleanTreeEvaluateJoin(ResultSet[] list, SQLBooleanClauseTree tree) throws IOException, InvalidDateException, 
                                                                                                                       InvalidNumberException
   {
      int indexRs = tree.booleanClause.resultSet.indexRs,
          indexTree = tree.indexRs;
      SQLBooleanClauseTree leftTree = tree.leftTree,
                           rightTree = tree.rightTree;

      if (indexTree >= 0) // AND, OR and BOOLEAN_NOT have index = -1.
      {
         if (indexTree < indexRs) // It was avaliated before and can return true.
            return VALIDATION_RECORD_INCOMPLETE_OK;
         if (indexTree > indexRs) // juliana@211_5: solved a bug with joins which would return more answers than desired.
         {
            if (tree.bothAreIdentifier && leftTree.indexRs == indexRs) // Fills leftTree.value.
            {
               ResultSet rsBag = list[rightTree.indexRs];
               int boolOp = rsBag.whereClause.appliedIndexesBooleanOp;
               
               leftTree.valueJoin = leftTree.getOperandValue(); 
               if (rightTree.hasIndex && boolOp <= 1)
               {
                  // juliana@225_13: join now behaves well with functions in columns with an index.
                  SQLBooleanClause booleanClause = tree.booleanClause;
                  SQLResultSetField[] fieldList = booleanClause.fieldList;
                  int i = booleanClause.fieldsCount;
                  
                  while (--i >= 0)
                     if (fieldList[i].tableColIndex == rightTree.colIndex && fieldList[i].isDataTypeFunction)
                        return VALIDATION_RECORD_INCOMPLETE;
                  
                  // Despite this is a join the parameter 'false' is sent because this is a simple index calculation.
                  SQLSelectStatement.computeIndex(list, false, rightTree.indexRs, leftTree.valueJoin, tree.operandType, rightTree.colIndex);
               
                  IntVector auxRowsBitmap = rsBag.auxRowsBitmap;
                  
                  // juliana@230_39: join now can be much faster if the query is smartly written.
                  if (rsBag.rowsBitmap != null && auxRowsBitmap != null && boolOp == 1)
                  {
                     SQLSelectStatement.mergeBitmaps(auxRowsBitmap.items, rsBag.rowsBitmap.items, 1);
                     if (Utils.countBits(auxRowsBitmap.items) == 0)
                        return VALIDATION_RECORD_NOT_OK;
                  }
               }
            }
            return VALIDATION_RECORD_INCOMPLETE;
         }
      }

      // The indexes match, so compare the records.

      switch (tree.operandType) // Checks what is the operand type of the tree.
      {
         // Relational operand.
         case SQLElement.OP_REL_LESS:
         case SQLElement.OP_REL_EQUAL:               
         case SQLElement.OP_REL_GREATER:               
         case SQLElement.OP_REL_GREATER_EQUAL:
         case SQLElement.OP_REL_LESS_EQUAL:
         case SQLElement.OP_REL_DIFF:
            switch (tree.valueType) // Calls the right operation accordingly to the values type.
            {
               case SQLElement.SHORT:
               case SQLElement.INT:
               case SQLElement.LONG:
               case SQLElement.FLOAT:
               case SQLElement.DOUBLE:
               case SQLElement.DATE:
               case SQLElement.DATETIME:
                  return tree.compareNumericOperands()? VALIDATION_RECORD_OK : VALIDATION_RECORD_NOT_OK;
               case SQLElement.CHARS:
                  return tree.compareStringOperands(false)? VALIDATION_RECORD_OK : VALIDATION_RECORD_NOT_OK;
               case SQLElement.CHARS_NOCASE:
                  return tree.compareStringOperands(true)? VALIDATION_RECORD_OK : VALIDATION_RECORD_NOT_OK;
            }

         // juliana@201_4: joins with like were returning the opposite result.
         // Relational operand.
         case SQLElement.OP_PAT_MATCH_LIKE:
         case SQLElement.OP_PAT_MATCH_NOT_LIKE:
            return tree.matchStringOperands(tree.valueType == SQLElement.CHARS_NOCASE) ? VALIDATION_RECORD_OK : VALIDATION_RECORD_NOT_OK;
         
         case SQLElement.OP_BOOLEAN_AND: // AND connector.
            if (leftTree != null && rightTree != null) // Expects both trees to be not null.
               switch (booleanTreeEvaluateJoin(list, leftTree))
               {
                  case VALIDATION_RECORD_NOT_OK:
                     return VALIDATION_RECORD_NOT_OK;
                  case VALIDATION_RECORD_INCOMPLETE:
                     if (booleanTreeEvaluateJoin(list, rightTree) == VALIDATION_RECORD_NOT_OK) // Verifies the right branch. 
                        return VALIDATION_RECORD_NOT_OK;
                     
                     // All other results return incomplete because the left side has returned incomplete.
                     return VALIDATION_RECORD_INCOMPLETE;
                     
                  case VALIDATION_RECORD_INCOMPLETE_OK:
                  case VALIDATION_RECORD_OK:
                     switch (booleanTreeEvaluateJoin(list, rightTree)) // The left side returned true, so verifies the right branch.
                     {
                        case VALIDATION_RECORD_NOT_OK:
                           return VALIDATION_RECORD_NOT_OK;
                        case VALIDATION_RECORD_INCOMPLETE_OK:
                        case VALIDATION_RECORD_OK:
                           return VALIDATION_RECORD_OK; // Both sides returns true.
                        case VALIDATION_RECORD_INCOMPLETE:
                           
                           // If the right side returns incomplete, incomplete must be returned, despite the left side returned OK.
                           return VALIDATION_RECORD_INCOMPLETE;
                     }
               }

         case SQLElement.OP_BOOLEAN_OR: // OR connector.
            if (leftTree != null && rightTree != null) // Expects both trees to be not null.
            {
               switch (booleanTreeEvaluateJoin(list, leftTree))
               {
                  case VALIDATION_RECORD_OK:
                     return VALIDATION_RECORD_OK; // Short circuit.
                  case VALIDATION_RECORD_INCOMPLETE_OK:
                     switch (booleanTreeEvaluateJoin(list, rightTree)) // Verifies the right branch.
                     {
                        case VALIDATION_RECORD_NOT_OK:
                           return VALIDATION_RECORD_INCOMPLETE_OK; // juliana@263_1: corrected a very old bug in a join with OR.
                        case VALIDATION_RECORD_INCOMPLETE:
                           return VALIDATION_RECORD_INCOMPLETE;
                        case VALIDATION_RECORD_OK:
                        case VALIDATION_RECORD_INCOMPLETE_OK:
                           return VALIDATION_RECORD_OK; // The right side returned true.
                     }
                     
                  case VALIDATION_RECORD_INCOMPLETE:
                     switch (booleanTreeEvaluateJoin(list, rightTree)) // Verifies the right branch.
                     {
                        case VALIDATION_RECORD_NOT_OK:
                        case VALIDATION_RECORD_INCOMPLETE:
                           return VALIDATION_RECORD_INCOMPLETE;
                        case VALIDATION_RECORD_OK:
                        case VALIDATION_RECORD_INCOMPLETE_OK:
                           return VALIDATION_RECORD_OK; // The right side returned true.
                     }
                     
                  case VALIDATION_RECORD_NOT_OK:
                  {
                     // The left side returned false, so continues verifing the right branch.
                     switch (booleanTreeEvaluateJoin(list, rightTree))
                     {
                        case VALIDATION_RECORD_NOT_OK:
                           return VALIDATION_RECORD_NOT_OK;
                        
                        // juliana@270_21: solved a very old join problem when using OR and false constants comparison which would make the join 
                        // return no results.
                        case VALIDATION_RECORD_INCOMPLETE_OK: 
                           return VALIDATION_RECORD_INCOMPLETE_OK;
                        case VALIDATION_RECORD_OK:
                           return VALIDATION_RECORD_OK; // Ther right side returned true.
                        case VALIDATION_RECORD_INCOMPLETE:
                           return VALIDATION_RECORD_INCOMPLETE;
                     }
                  }
               }
            }

         // juliana@214_4: nots were removed.

         // IS and IS NOT.
         case SQLElement.OP_PAT_IS:
         case SQLElement.OP_PAT_IS_NOT:
            return tree.compareNullOperands()? VALIDATION_RECORD_OK : VALIDATION_RECORD_NOT_OK;
      }

      return VALIDATION_RECORD_INCOMPLETE;
   }
   
   /**
    * Goes to the end of the record, reads the null bytes, and stores them in table.columnNulls. After this, returns the datastream cursor to the 
    * previous offset.
    * 
    * @param whichColumnNull The column null index. It ranges from 0 to 3.
    * @param dataStreamIsDislocated Indicates if the stream pointer is not pointing to its beginning,
    * @param col The column index where the stream pointer is pointing to.
    * @throws IOException If an internal method throws it.
    */
   void readNullBytesOfRecord(int whichColumnNull, boolean dataStreamIsDislocated, int col) throws IOException
   {
      DataStreamLB ds = db.basds; // juliana@253_8: now Litebase supports weak cryptography.
      int offset = columnOffsets[columnCount];
      if (dataStreamIsDislocated)
         offset -= columnOffsets[col];
      
      ds.skipBytes(offset);
      byte[] nulls = columnNulls[whichColumnNull];

      offset += ds.readBytes(nulls, 0, nulls.length);
      db.bas.skipBytes(-offset);
   }

   // juliana@220_3
   /**
    * Sorts a table, using an ORDER BY or GROUP BY clause.
    * 
    * @param groupByClause The group by clause.
    * @param orderByClause The order by clause.
    * @param driver The connection with Litebase.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void sortTable(SQLColumnListClause groupByClause, SQLColumnListClause orderByClause, LitebaseConnection driver) throws IOException, 
                                                                                                                           InvalidDateException
   {
      byte[] bufAux = new byte[db.rowSize]; // juliana@114_8
      
      // Binds the sort lists to the temp table columns.
      if (orderByClause != null)
         orderByClause.bindColumnsSQLColumnListClause(htName2index, columnTypes, null);
      if (groupByClause != null)
         groupByClause.bindColumnsSQLColumnListClause(htName2index, columnTypes, null);

      // Picks one of the Column List clauses as the sort list.
      SQLColumnListClause sortListClause = (orderByClause == null? groupByClause : orderByClause);

      int count = columnCount;

      // Quick sorts the table.
      quickSort(0, db.rowCount - 1, SQLValue.newSQLValues(count), SQLValue.newSQLValues(count), SQLValue.newSQLValues(count), bufAux, 
                                                                                                sortListClause.fieldList, driver);
   }

   // juliana@250_1: corrected a possible crash when doing ordering operations.
   // juliana@227_10: corrected order by or group by with strings being too slow.
   // juliana@220_3
   /**
    * Quick sort method used to sort the table.
    * 
    * @param first The first index of this partition.
    * @param last The last index of this partition.
    * @param pivot The pivot of this partition;
    * @param someRecord1 An auxiliar record to avoid re-creating it.
    * @param someRecord2 An auxiliar record to avoid re-creating it.
    * @param bufAux A buffer to store the records.
    * @param fieldList The order of comparison of the fields.
    * @param driver The connection with Litebase.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   private void quickSort(int first, int last, SQLValue[] pivot, SQLValue[] someRecord1, SQLValue[] someRecord2, byte[] bufAux, 
                                                 SQLResultSetField[] fieldList, LitebaseConnection driver) throws IOException, InvalidDateException
   {
      Random r = new Random();
      int[] intVector = db.driver.nodes;
      PlainDB plainDB = db;
      byte[] basbuf = db.basbuf;
      int rowSize = plainDB.rowSize,
          size = 2,
          low, 
          high,
          pivotIndex; // guich@212_3: now using random partition (improves worst case 2000x).
      byte[] nulls1 = columnNulls[0];
      byte[] nulls2 = columnNulls[1];
      byte[] nulls3 = columnNulls[2];
      String[][] strings = new String[last - first + 1][fieldList.length];
      String[] tempString;
      
      intVector[0] = first;
      intVector[1] = last;
      
      while (size > 0) // guich@212_3: removed recursion (storing in a IntVector).
      {
         high = last = intVector[--size];
         low = first = intVector[--size];
         
         // juliana@213_3: high can't be equal to low.
         pivotIndex = high == low? high : r.between(low, high); // guich@212_3: now using random partition (improves worst case 2000x).
         
         readRecord(pivot, pivotIndex, 2, driver, fieldList, true, strings);
         
         while (true) // Finds the partitions.
         {         
            while (high >= low)
            {
               readRecord(someRecord1, low, 0, driver, fieldList, true, strings);
               if (Utils.compareRecords(someRecord1, pivot, nulls1, nulls3, fieldList) >= 0)
                  break;
               low++;
            }
            
            Vm.arrayCopy(basbuf, 0, bufAux, 0, rowSize); // juliana@114_8
            
            while (high >= low) 
            {
               readRecord(someRecord2, high, 1, driver, fieldList, true, strings);
               if (Utils.compareRecords(someRecord2, pivot, nulls2, nulls3, fieldList) <= 0)
                  break;
               high--;
            }
            
            if (low <= high)
            {
               // juliana@114_8: optimized the swap of the records. Now the buffer is written at once.
               tempString = strings[low];
               strings[low] = strings[high];
               strings[high] = tempString;
               plainDB.rewrite(low++);
               Vm.arrayCopy(bufAux, 0, basbuf, 0, rowSize);
               plainDB.rewrite(high--);
            }
            else break;
         }
         
         // Sorts the partitions.
         if (first < high)
         {
            intVector[size++] = first;
            intVector[size++] = high;
         }
         if (low < last)
         {
            intVector[size++] = low;
            intVector[size++] = last;
         }
      } 
      strings = null;
   }

   // juliana@220_3
   /**
    * Reads the entire record from a table.
    * 
    * @param record An array where the record filed values will be stored.
    * @param recPos The record index.
    * @param whichColumnNull Indicates where the nulls will be stored.
    * @param driver The connection with Litebase.
    * @param fieldList A field list that indicates which fields to read from the table. 
    * @param isTempBlob Indicates if a blob must be loaded or not.
    * @param strings An array of strings if this method is used in a sort.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void readRecord(SQLValue[] record, int recPos, int whichColumnNull, LitebaseConnection driver, SQLResultSetField[] fieldList, boolean isTempBlob,
                                                                       String[][] strings) throws IOException, InvalidDateException
   {
      int i = fieldList != null? fieldList.length : columnCount;
      PlainDB plainDB = db;
      
      plainDB.read(recPos);
      readNullBytesOfRecord(whichColumnNull, false, 0); // Reads the null bytes of the end of the record.

      byte[] nulls = columnNulls[whichColumnNull];
      short[] offsets = columnOffsets;
      byte[] types = columnTypes;
      
      // juliana@226_12: corrected a bug that could make aggregation function not work properly.
      if ((plainDB.basds.readInt() & Utils.ROW_ATTR_MASK) == Utils.ROW_ATTR_DELETED && name != null)
      {
         plainDB.bas.skipBytes(-4);
         return;
      }
      plainDB.bas.skipBytes(-4);
      
      if (fieldList == null) // Reads all columns of the table.
         while (--i >= 0)
            readValue(record[i], offsets[i], types[i], (nulls[i >> 3] & (1 << (i & 7))) != 0, false); // juliana@230_14
      else // Reads only the columns used during sorting.
      {
         int j;
         while (--i >= 0)
         {
            // juliana@227_10: corrected order by or group by with strings being too slow.
            j = fieldList[i].tableColIndex;
            if ((types[j] != SQLElement.CHARS && types[j] != SQLElement.CHARS_NOCASE) || strings[recPos][i] == null)
            {
               readValue(record[j], offsets[j], types[j], (nulls[j >> 3] & (1 << (j & 7))) != 0, false); // juliana@230_14
               strings[recPos][i] = record[j].asString;
            }
            else
               record[j].asString = strings[recPos][i];
         }
      }
   }

   /**
    * Remaps a table column names, so it uses the alias names of the given field list, instead of the original names.
    * 
    * @param fieldList The field list of the select clause.
    */
   void remapColumnsNames2Aliases(SQLResultSetField[] fieldList)
   {
      IntHashtable tableName2Index = htName2index;
      int[] hashes = columnHashes;
      int i = fieldList.length;
      SQLResultSetField field;
      String[] names = columnNames;
      if (names == null)
      {
         names = columnNames = new String[i];

         while (--i >= 0)
         {
            names[i] = (field = fieldList[i]).alias;
            if (hashes[i] == field.aliasHashCode) // Replaces the original mapping, if necessary.
               continue;
            
            // rnovais@103_2: changed from columnHashCode to field.aliasHashCode
            tableName2Index.put(hashes[i], i); // Already replaces old values.
         }
      }
   }
   
   // juliana@222_9: Some string conversions to numerical values could return spourious values if the string range were greater than the type range.
   /**
    * Converts the strings of the record into the real values, accordingly to the given table column types.
    * 
    * @param record The record whose strings are to be transformed in their real types.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException  If an internal method throws it.
    */
   void convertStringsToValues(SQLValue[] record) throws InvalidNumberException, InvalidDateException
   {
      int i = record.length, 
          type;
      byte[] types = columnTypes;
      String strVal;

      while (--i > 0) // 0 = rowid.
      {
         // If the column is storing a null, the string is considered to be null.
         strVal = (record[i] != null ? record[i].asString : null);
         
         // A blob can't be set in a normal statement (if the string is not null, it won't be a "?".
         if ((type = types[i]) == SQLElement.BLOB && strVal != null && !strVal.equals("?"))
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_BLOBS_PREPARED));

         // Ignores null values, blobs or unset parameters.
         if (strVal == null || strVal.equals("?") || type == SQLElement.BLOB) // guich@tc100b4: also continue if its blob.
            continue;
         
         switch (type)
         {
            case SQLElement.SHORT: // SHORT
               record[i].asShort = Convert.toShort(strVal);
               break;

            case SQLElement.INT: // INT
               record[i].asInt = Convert.toInt(strVal);
               break;

            case SQLElement.LONG: // LONG
               record[i].asLong = Convert.toLong(strVal);
               break;

            case SQLElement.FLOAT: // FLOAT
               record[i].asDouble = Utils.toFloat(strVal);
               break;
             
            case SQLElement.DOUBLE: // DOUBLE
               record[i].asDouble = Convert.toDouble(strVal);
               break;
               
            case SQLElement.DATE: // DATE
               record[i].asInt = db.driver.tempDate.set(strVal.trim(), Settings.DATE_YMD);
               break;
               
            case SQLElement.DATETIME: // DATETIME
               record[i].parseDateTime(db.driver.tempDate, strVal);
         }   
      }
   }

   /**
    * Writes a record on a disk table.
    *
    * @param values The values to be written on the table.
    * @param recPos The record position.
    * @throws DriverException If there is a null in the primary key.
    * @throws IOException  If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void writeRecord(SQLValue[] values, int recPos) throws DriverException, IOException, InvalidDateException
   {
      int n = columnCount, // nowosad@200: the array of values may be larger than necessary; so use the table field count to truncate it.  
          j,
          i = n, 
          rowid = 0, 
          writePos = -1, 
          offset = 2;
      ByteArrayStream bas = db.bas;
      DataStreamLB ds = db.basds; // juliana@253_8: now Litebase supports weak cryptography.
      Index idx;
      boolean addingNewRecord = recPos == -1, 
               changePos = false;
      byte[] nulls = storeNulls;
      byte[] columnNulls0 = columnNulls[0];
      SQLValue[] defaults = defaultValues;
      SQLValue[] auxValues = primaryKeyValues;
      SQLValue[] auxOldValues = primaryKeyOldValues;
      byte[] attrs = columnAttrs;
      PlainDB plainDB = db;
      byte[] composedPKCols = composedPrimaryKeyCols;
      byte[] types = columnTypes;
      int[] sizes = columnSizes;

      bas.reset();
      Convert.fill(columnNulls0, 0, columnNulls0.length, 0); // First of all, clear the columnNulls used. 

      while (--i > 0) // 0 = rowid = never is null.
      {
         // juliana@225_7: a PrimaryKeyViolation was not being thrown when two strings with the same prefix were inserted and the field definition 
         // had the size of the prefix and a primary key.
         if ((types[i] == SQLElement.CHARS || types[i] == SQLElement.CHARS_NOCASE) 
          && values[i] != null && values[i].asString != null && values[i].asString.length() > sizes[i])
            values[i].asString = values[i].asString.substring(0, sizes[i]);   
         
         if (addingNewRecord)
         {
            if ((nulls[i >> 3] & (1 << (i & 7))) == 0) // If not explicit to store null.
            {
               if ((attrs[i] & Utils.ATTR_COLUMN_IS_NOT_NULL) == 0) // Can be null.
               {
                  if (values[i] == null || values[i].isNull)
                     if (defaults[i] == null) // It doesn't have a default value.
                        columnNulls0[i >> 3] |= (1 << (i & 7)); // Sets the column as null.
                     else
                        values[i] = defaults[i]; // If it doesn't have a value, stores the default value.
                  
                  // At this moment, if it can't be null, necessarily it has a default value.
               }
               else if (values[i] == null  || values[i].isNull)
                  values[i] = defaults[i];
            }
            else 
               columnNulls0[i >> 3] |= (1 << (i & 7));
         }
         else // Update Statement.
         if ((nulls[i >> 3] & (1 << (i & 7))) != 0)
            columnNulls0[i >> 3] |= (1 << (i & 7));
      }
            
      // If there is a primary key column, there can't be repeated values.
      if (primaryKeyCol != Utils.NO_PRIMARY_KEY) // nowosad@200: if table has a primary key, tests if writing the record does not violate it.
      {
         auxValues[0] = values[primaryKeyCol];
         checkPrimaryKey(auxValues, recPos, addingNewRecord, auxOldValues);
      }
         
      if (composedPKCols != null)
      {
         i = composedPKCols.length;
         while (--i >= 0) // A field of the composed primary key can't be null.
         {   
            j = composedPKCols[i];
            
            if ((nulls[j >> 3] & (1 << (j & 7))) != 0 || (values[j] != null && values[j].isNull))
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PK_CANT_BE_NULL));
            auxValues[i] = values[composedPKCols[i]];
         }
         checkPrimaryKey(auxValues, recPos, addingNewRecord, auxOldValues);
      }
      
      if (addingNewRecord) // Adding a record?
      {
         plainDB.add();
         writePos = plainDB.rowCount;
         values[0].asInt = rowid = currentRowId++; // Writes the rowId, marking the attribute as new.
         resetAuxRowId();
      }
      else // May have to read the value before deleting the index value.
      {
         plainDB.read(writePos = recPos);
         rowid = ds.readInt();
         bas.reset();
         wasUpdated = true; // juliana@270_27: now purge will also really purge the table if it only suffers updates.
      }

      int type;
      boolean isNull, 
               valueOk, 
               hasvolds;
      
      byte[] has = ghas; // Stores the 3 attributes for this method.
      SQLValue[] vOlds = gvOlds;
      Index[] indices = columnIndices;
      byte[] columnNulls1 = columnNulls[1];
      byte[] asBlob;
      SQLValue[] one = plainDB.driver.oneValue;
      
      Convert.fill(has, 0, has.length, 0);

      i = -1;
      while (++i < n) // 0 = rowid
      {
         changePos = false;
         type = types[i];
         if (isNull = ((columnNulls0[i >> 3] & (1 << (i & 7))) != 0))
            has[i] |= HAS_NULLVAL;
         valueOk = (values[i] != null || isNull);

         idx = indices[i]; // If a new value is being written, the table index (if any) needs to be updated.
        
         if (valueOk && idx != null)  // Only this row is being updated (valueOk).
            has[i] |= HAS_IDX;

         if (!addingNewRecord) // Reads the previous value if it is an update.
         {
            // Verifies if the value to be read is null. IMPORTANT: the array of bytes can't be stored in table.columnNulls[0]. This variable is in
            // use. Then, it will be read in Table.columnNulls[1].
            readNullBytesOfRecord(1, true, i);

            has[i] |= (columnNulls1[i >> 3] & (1 << (i & 7))) != 0 ? ISNULL_VOLDS : 0;
            if (vOlds[i] == null)
               vOlds[i] = new SQLValue();
            else
            {
               vOlds[i].asString = null;
               vOlds[i].asBlob = null;
               
            }
            
            vOlds[i].asInt = -1; // This is a flag that indicates that blobs are not to be loaded.
            
            // The offset is already positioned and is restored after read.
            readValue(vOlds[i], 0, type, (has[i] & ISNULL_VOLDS) != 0, false); // juliana@220_3 juliana@230_14 

            if (valueOk && type == SQLElement.BLOB)
            {
               // juliana@202_21: Always writes the string at the end of the .dbo. This removes possible bugs when doing updates.
               // A blob in the .dbo must have its position changed if the the new value is greater than the old one.
               j = ((asBlob = vOlds[i].asBlob) == null)? vOlds[i].asInt : asBlob.length;
               if (values[i] != null && values[i].asBlob != null 
                && (((asBlob != null || vOlds[i].asInt != -1) && j < values[i].asBlob.length) || asBlob == null))
                  changePos = true;
               else if (asBlob != null || vOlds[i].asInt != -1)
                  offset = j + 4;
            }
         }
         
         // Writes the value.
         plainDB.writeValue(type, values[i], ds, valueOk && !isNull, addingNewRecord || changePos, sizes[i], offset, false); // juliana@220_3 

         // If the new and the old values are null and the column is not the rowid, sets the value as null.
         if (i > 0 && values[i] == null && (has[i] & ISNULL_VOLDS) != 0)
            columnNulls0[i >> 3] |= (1 << (i & 7));
      }

      ds.writeBytes(columnNulls0, 0, columnNulls0.length); // After the columns, stores the bytes of the null values.
      
      // juliana@230_12: improved recover table to take .dbo data into consideration.
      // juliana@220_4: added a crc32 code for every record. Please update your tables.
      byte[] buffer = bas.getBuffer();
      byte[] byteArray;

      j = buffer[3]; // juliana@270_23: Corrected a RowIterator bug of an update changing an updated row to synced again.
      buffer[3] = (plainDB.useCrypto? (byte)0xAA : 0); // juliana@222_5: The crc was not being calculated correctly for updates.
      int crc32 = updateCRC32(buffer, bas.getPos(), 0, plainDB.useCrypto); 

      if (version == Table.VERSION)
      {
         int[] intArray = oneInt;
         
         i = n;
         while (--i > 0) 
            if (types[i] == SQLElement.CHARS || types[i] == SQLElement.CHARS_NOCASE)
            {
           	   if (values[i] != null && !values[i].isNull)
           	   {
       	         byteArray = Utils.toByteArray(values[i].asString);
       	         crc32 = Table.updateCRC32(byteArray, byteArray.length, crc32, false);
           	   }
       	      else if (!addingNewRecord && (values[i] == null || !values[i].isNull) && vOlds[i] != null && !vOlds[i].isNull && vOlds[i].asString != null)
       	      {
       	         byteArray = Utils.toByteArray(vOlds[i].asString); 
       	         crc32 = Table.updateCRC32(byteArray, byteArray.length, crc32, false);
       	      }
            }
            else if (types[i] == SQLElement.BLOB)
            {	
           	   if (values[i] != null && !values[i].isNull)
           	   {
           	      // juliana@239_4: corrected a non-desired possible row delete when recovering a table with blobs.
           	      intArray[0] = ((values[i].asBlob.length > sizes[i])? sizes[i] : values[i].asBlob.length);
           	      crc32 = Table.updateCRC32(Convert.ints2bytes(intArray, 4), 4, crc32, false);
           	   }
           	   // juliana@265_2: corrected a problem where a row could be wrongly deleted by recovering a table when an update was done and the 
               // column which was of type blob remained null on Java SE and BlackBerry.
        	      else if (!addingNewRecord && (values[i] == null || !values[i].isNull) && vOlds[i] != null && !vOlds[i].isNull && vOlds[i].asInt != -1)
        	      {
        	         intArray[0] = vOlds[i].asInt;
        	         crc32 = Table.updateCRC32(Convert.ints2bytes(intArray, 4), 4, crc32, false);
        	      }
            }
      }
      ds.writeInt(crc32); // Computes the crc for the record and stores at the end of the record.
      buffer[3] = (byte)j; // juliana@270_23: Corrected a RowIterator bug of an update changing an updated row to synced again.
      
      if (rowid > 0) // Now the record's attribute has to be updated.
      {
         bas.reset();
         ds.writeInt(addingNewRecord? (rowid & Utils.ROW_ID_MASK) | Utils.ROW_ATTR_NEW : rowUpdated(rowid)); // Sets the row as new or updated.
      }
           
      // Writes the row.
      if (addingNewRecord)
         plainDB.write();
      else
         plainDB.rewrite(writePos);   

      while (--n >= 0) // Finally, adds the values to the indices. // 0 = rowid
         
         if ((has[n] & HAS_IDX) != 0)
         {
            idx = indices[n];

            isNull = (columnNulls0[n >> 3] & (1 << (n & 7))) != 0;
            if (addingNewRecord)
            {
               if (!isNull) // Doesn't store null values on indices.
               {
                  one[0] = values[n];
                  idx.indexAddKey(one, writePos);
               }
            }
            else // Updating key? Removes the old one and adds the new one.
            if (values[n].valueCompareTo(vOlds[n], types[n], isNull, hasvolds = (has[n] & ISNULL_VOLDS) != 0, null) != 0)
            {
               if (!hasvolds)
               {
                  one[0] = vOlds[n]; // Encapsulates
                  idx.tempKey.set(one);
                  idx.removeValue(idx.tempKey, writePos);
               }
               
               if (!isNull) // If it is updating a 'non-null value' to 'null value', only removes it.
               {
                  one[0] = values[n];
                  idx.indexAddKey(one, writePos);
               }
            }
         }

      if ((i = numberComposedIndices) > 0) // Fills the composed indices.
      {
         // If a composed index has all column values equal to null, it is not possible to store this row in the index. Composed indices that have at 
         // least one field that is not null can be stored in the index, but the null values must be handled. This implies in changing the index 
         // format. Maybe using the same way like null values are handled on tables. This certainly will decrease the index performance. For 
         // simplicity, the key will be stored in a composed index only if all values are not null. This is a project choice.
         int size, 
             column;
         boolean store,
                 remove, // juliana@230_43
                 change; // juliana@252_2: corrected a bug of possible composed index corruption when updating or deleting data.
         ComposedIndex ci;
         SQLValue[] vals = null;
         SQLValue[] valsRev = null;
         byte[] columns;
         ComposedIndex[] compIndices = composedIndices; 
         Index index;
         
         while (--i >= 0)
         {
            size = (columns = (ci = compIndices[i]).columns).length;
            if (ci.indexId > 0)
            {
               if (vals == null || vals.length < size)
               {
                  vals = new SQLValue[size];
                  valsRev = new SQLValue[size];
               }
               store = remove = true;
               change = false;
               j = size;
               while (--j >= 0)
               {
                  if ((has[column = columns[j]] & HAS_NULLVAL) != 0) // Only stores non-null values.
                     store = false;
                  
                  // juliana@230_43: solved a possible exception if updating a table with composed indices and nulls.
                  if ((has[column] & ISNULL_VOLDS) != 0) 
                     remove = false;
                  
                  // Sets the old and new index values.
                  // juliana@282_2: Doesn't update the composed index or PK if the old values are the same of the new ones. 
                  if (values[column] == null) // juliana@201_18: can't reuse values. Otherwise, it will spoil the next update.
                     vals[j] = vOlds[column];
                  else
                  {
                     if (addingNewRecord || values[column].valueCompareTo(vOlds[column], types[column], !remove, !store, null) != 0)
                        change = true;
                     vals[j] = values[column];
                  }
                  
                  valsRev[j] = vOlds[column];
               }
               
               index = ci.index;
               
               // juliana@252_2: corrected a bug of possible composed index corruption when updating or deleting data.
               // juliana@230_43: solved a possible exception if updating a table with composed indices and nulls.
               if (!addingNewRecord && remove && change) // Removes the old composed index entry.
               {
                  index.tempKey.set(valsRev);
                  index.removeValue(ci.index.tempKey, writePos);
               }

               // juliana@252_2: corrected a bug of possible composed index corruption when updating or deleting data.
               if (store && change)
                  index.indexAddKey(vals, writePos);
            }
         }
      }
      
      // juliana@227_3: improved table files flush dealing.
      // juliana@270_25: corrected a possible lose of records in recover table when 10 is passed to LitebaseConnection.setRowInc().
      NormalFile dbFile = (NormalFile)plainDB.db,
                 dboFile = (NormalFile)plainDB.dbo;
      if (!dbFile.dontFlush) // juliana@202_23: Flushs the files to disk when row increment is the default.
      {           
         if (dbFile.cacheIsDirty)
            dbFile.flushCache(); // Flushs .db.
         if (dboFile.cacheIsDirty)
            dboFile.flushCache(); // Flushs .dbo.
      }
   }

   // juliana@230_12: improved recover table to take .dbo data into consideration.
   // juliana@220_4: added a crc32 code for every record. Please update your tables.
   /** 
    * Updates the CRC32 value with the values of the given buffer. 
    * 
    * @param buffer The buffer.
    * @param length The number of bytes to be used to update the CRC code.
    * @param oldCRC The previous CRC32 value.
    * @param useCrypto Indicates if cryptography is to be used or not.
    * @return The CRC32 code updated to include the buffer data.
    */
   static int updateCRC32(byte[] buffer, int length, int oldCRC, boolean useCrypto)
   {
     int[] crcTable = CRC32Stream.crcTable;
     int offset = 0;
     
     oldCRC = ~oldCRC;
     if (useCrypto)
        while (--length >= 0)
           oldCRC = crcTable[(oldCRC ^ (buffer[offset++] ^ 0xAA)) & 0xff] ^ (oldCRC >>> 8);
     else   
        while (--length >= 0)
           oldCRC = crcTable[(oldCRC ^ buffer[offset++]) & 0xff] ^ (oldCRC >>> 8);
     
     return ~oldCRC;
   }

   /**
    * Resets the auxiliary rowid.
    *
    * @throws IOException If an internal method throws it.
    */
   private void resetAuxRowId() throws IOException // rnovais@570_61
   {
      if (auxRowId != Utils.ATTR_DEFAULT_AUX_ROWID)
      {
         XFile dbFile = db.db;
         int pos = dbFile.pos;
         
         auxRowId = Utils.ATTR_DEFAULT_AUX_ROWID;
         tableSaveMetaData(Utils.TSMD_ONLY_AUXROWID);
         dbFile.setPos(pos);
      }
   }
   
   /**
    * Changes the state of a row to updated.
    *
    * @param id The rowid to have its atribute changed.
    * @return The rowid with its atribute changed to updated.
    */
   private int rowUpdated(int id)
   {
      if ((id & Utils.ROW_ATTR_MASK) == Utils.ROW_ATTR_SYNCED)
         return (id & Utils.ROW_ID_MASK) | Utils.ROW_ATTR_UPDATED; // Sets the row as update.
      return id;
   }
   
   /**
    * Compares two records. Used for sorting the table to build the indices from scratch.
    * 
    * @param vals1 The first record of the comparison.
    * @param vals2 The second record of the comparison.
    * @param types The types of the record values.
    * @return A positive number if vals1 > vals2; 0 if vals1 == vals2; -1, otherwise. It will return <code>MAX_INT_VALUE</code> if both records are 
    * equal but the record of the first is greater than the second, and <code>MIN_INT_VALUE</code> if both records are equal but the record of the 
    * first is less than the second. 
    * @throws IOException If an internal method throws it.
    */
   private static int compareRecords(SQLValue[] vals1, SQLValue[] vals2, byte[] types) throws IOException 
   {
      int n = vals1.length,
          i = -1,
          result;
  
      while (++i < n) // Does the comparison between the values till one of them is different from zero.
         if ((result = vals1[i].valueCompareTo(vals2[i], types[i], false, false, null)) != 0)
            return result;
      if (types[0] != SQLElement.LONG)
      {
         if (vals1[0].asLong > vals2[0].asLong)
            return Convert.MAX_INT_VALUE;
         if (vals1[0].asLong < vals2[0].asLong)
            return Convert.MIN_INT_VALUE;
         return 0;
      }
      if (vals1[0].asInt > vals2[0].asInt)
         return Convert.MAX_INT_VALUE;
      if (vals1[0].asInt < vals2[0].asInt)
         return Convert.MIN_INT_VALUE;
      return 0;
   }
   
   /**
    * Quick sort used for sorting the table to build the indices from scratch. This one is simpler than the sort used for order / gropu by.
    * Uses a stack instead of a recursion.
    * 
    * @param sortValues The records to be sorted.
    * @param types The types of the record values. 
    * @param first The first element of current partition.
    * @param last The last element of the current.
    * @throws IOException If an internal method throws it.
    */
   private void sortRecords(SQLValue[][] sortValues, byte[] types, int first, int last) throws IOException
   {
      // guich@212_3: checks if the values are already in order.
      SQLValue[] tempValues;
      int i = first;
      while (++i <= last)
         if (compareRecords(sortValues[i - 1], sortValues[i], types) > 0)
            break;
      if (i > last)
         return; 
      
      // juliana@250_1: corrected a possible crash when doing ordering operations.
      // Not fully sorted.
      int size = 2,
          low,
          high;
      int[] intVector = db.driver.nodes;
      Random r = new Random();
      SQLValue[] mid;
      
      intVector[0] = first;
      intVector[1] = last;
      while (size > 0) // guich@212_3: removed recursion (storing in a IntVector).
      {
         high = last = intVector[--size];
         low = first = intVector[--size];

         // juliana@213_3: high can't be equal to low.
         mid = sortValues[high == low? high : r.between(low, high)]; // guich@212_3: now using random partition (improves worst case 2000x).
         
         while (true) // Finds the partitions.
         {         
            while (high >= low && compareRecords(mid, sortValues[low], types)  > 0)
               low++;
            while (high >= low && compareRecords(mid, sortValues[high], types) < 0)
               high--;
            
            if (low <= high)
            {
               tempValues = sortValues[low];
               sortValues[low++] = sortValues[high];
               sortValues[high--] = tempValues;
            }
            else break;
         }
         
         // Sorts the partitions.
         if (first < high)
         {
            intVector[size++] = first;
            intVector[size++] = high;
         }
         if (low < last)
         {
            intVector[size++] = low;
            intVector[size++] = last;
         }
      }         
   }
   
   /** 
    * Does a radix sort on the given SQLValue array. Only integral types are allowed (SHORT, INT, LONG). This is faster than quicksort. Also used to 
    * build the indices from scratch.
    * 
    * @param source The values to be sorted. Only simple records for simple indices can be used.
    * @param type The type of the elements.
    * @param temp A temporary array for the sort.
    */
   private static void radixSort(SQLValue source[][], int type, SQLValue[][] temp)
   {
      int count[] = new int[256];
      int index[] = new int[256];
      SQLValue z[][];
      int byteCount = (type == SQLElement.INT)? 4 : (type == SQLElement.SHORT) ? 2 : 8,
          i = 0,
          length = temp.length; // juliana@227_15: corrected a possible NullPointerException when creating an index in a column with null values.
      long mask = 0xFF, 
           bits = radixPass(0, source, temp, count, index, type, length);
      
      while (++i < byteCount)
      {
         if ((bits & (mask <<= 8)) != 0) // Any bits in this range? 
         {
            // Swaps the from/to arrays.
            z = source; 
            source = temp; 
            temp = z; 
            radixPass(i, source, temp, count, index, type, length);  // Yes, sort.
         }
      }
      if (temp != source) // If the final sorted array is not at the source, copies to it.
         Vm.arrayCopy(temp, 0, source, 0, length);
   }

   /**
    * Executes a pass of the radix sort.
    * 
    * @param start Start bit.
    * @param source The source array,
    * @param dest The dest array where the operations with the source are copied to.
    * @param count A temporary array.
    * @param index A temporary array.
    * @param type The type of the values being sorted.
    * @param length The number of rows to be sorted.
    * @return A number of bits.
    */
   private static long radixPass(int start, SQLValue source[][], SQLValue dest[][], int[] count, int[] index, int type, int length)
   {
      int i = 0,
          n = length, // juliana@227_15: corrected a possible NullPointerException when creating an index in a column with null values.
          ibits = 0,
          b, 
          ishift = start << 3;
      long lbits = 0, 
           lshift = start << 3, 
           lb;
      
      if (start > 0) 
         Convert.fill(count, 0, 255, 0);

      switch (type)
      {
         case SQLElement.INT:
         case SQLElement.DATE: // juliana@214_9: index creation for date types could create corrupted indices.
            if (start == 0)
               while (--n >= 0) 
               {
                  count[(b = source[i++][0].asInt) & 0xFF]++; 
                  ibits |= b;
               }
            else
            if (start == 3)
               while (--n >= 0) 
                  count[(source[i++][0].asInt >> ishift) + 128]++;
            else
               while (--n >= 0) count[(source[i++][0].asInt >> ishift) & 0xFF]++;
            break;
         case SQLElement.SHORT:
            if (start == 0)
               while (--n >= 0) 
               {
                  count[(b=source[i++][0].asShort) & 0xFF]++; 
                  ibits |= b;
               }
            else
               while (--n >= 0) 
                  count[(source[i++][0].asShort >> ishift) + 128]++;
            break;
         case SQLElement.LONG:
            if (start == 0)
               while (--n >= 0) 
               {
                  count[(int)((lb=source[i++][0].asLong) & 0xFF)]++; 
                  lbits |= lb;
               }
            else
            if (start == 7)
               while (--n >= 0) 
                  count[(int)((source[i++][0].asLong >> lshift) + 128)]++;
            else
               while (--n >= 0) 
                  count[(int)((source[i++][0].asLong >> lshift) & 0xFF)]++; 
      }            

      index[0] = i = 0;
      n = 255; 
      while (--n >= 0)
      {
         index[i + 1] = index[i] + count[i];
         i++;
      }

      i = 0; 
      n = length;
      switch (type)
      {
         case SQLElement.INT:
         case SQLElement.DATE: // juliana@214_9: index creation for date types could create corrupted indices.
            if (start == 0)
               while (--n >= 0)
               {
                  dest[index[(source[i][0].asInt) & 0xFF]++] = source[i];
                  i++;
               }
            else if (start == 3)
               while (--n >= 0)
               {
                  dest[index[(source[i][0].asInt >> ishift) + 128]++] = source[i];
                  i++;
               }
            else
               while (--n >= 0)
               {
                  dest[index[(source[i][0].asInt >> ishift) & 0xFF]++] = source[i];
                  i++;
               }
            break;
         case SQLElement.SHORT:
            if (start == 0)
               while (--n >= 0)
               {
                  dest[index[(source[i][0].asShort) & 0xFF]++] = source[i];
                  i++;
               }
            else
               while (--n >= 0)
               {
                  dest[index[(source[i][0].asShort >> ishift) + 128]++] = source[i];
                  i++;
               }
            break;
         case SQLElement.LONG:
            if (start == 0)
               while (--n >= 0)
               {
                  dest[index[(int)((source[i][0].asLong) & 0xFF)]++] = source[i];
                  i++;
               }
            else if (start == 7)
               while (--n >= 0)
               {
                  dest[index[(int)((source[i][0].asLong >> lshift) + 128)]++] = source[i];
                  i++;
               }
            else
               while (--n >= 0)
               {
                  dest[index[(int)((source[i][0].asLong >> lshift) & 0xFF)]++] = source[i];
                  i++;
               }
      }            
      
      return type == SQLElement.LONG? lbits : ibits;
   }
   
   // guich@201_9: always shrink the .db and .dbo memory files.
   /**
    * Compresses the buffer at the current position.
    * 
    * @throws IOException If an internal method throws it.
    */
   void plainShrinkToSize() throws IOException 
   {
      PlainDB plainDB = db;
      MemoryFile dbFile = (MemoryFile)plainDB.db,
                 dboFile = (MemoryFile)plainDB.dbo;
      
      if (plainDB.rowCount > 0 && plainDB.rowAvail > 0) 
      {
         int ret = plainDB.rowCount * plainDB.rowSize;
         if (dbFile.size != ret)
         {
            dbFile.shrinkTo(ret);
            dbFile.size = ret;
            plainDB.rowAvail = 0;
         }
         if (dboFile.size != dboFile.finalPos)
         {
            dboFile.shrinkTo(dboFile.finalPos); // guich@201: also shrinks the .dbo,
            dboFile.size = dboFile.finalPos;
         }
      }
   }
   
   /**
    * Changes a table to the modified state whenever it is modified.
    * 
    * @throws IOException If an internal method throws it.
    */
   void setModified() throws IOException
   {
      PlainDB plainDB = db;
      NormalFile dbFile = (NormalFile)plainDB.db;
      byte[] oneByte = plainDB.driver.oneByte;
      
      dbFile.setPos(6);
      
      // juliana@230_13: removed some possible strange behaviours when using threads.
      oneByte[0] = (byte)(plainDB.isAscii? Table.IS_ASCII : 0);
      if (plainDB.useCrypto) // juliana@253_8: now Litebase supports weak cryptography.
         oneByte[0] = oneByte[0] ^= 0xAA;
      dbFile.writeBytes(oneByte, 0, 1);
      
      dbFile.flushCache();
      isModified = true;
   }
}
