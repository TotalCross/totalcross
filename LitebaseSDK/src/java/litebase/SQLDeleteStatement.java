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
import totalcross.util.InvalidDateException;

/**
 * Represents a SQL <code>DELETE</code> statement.
 */
class SQLDeleteStatement extends SQLStatement
{
   /** 
    * The base table used by the SQL expression. 
    */
   SQLResultSetTable rsTable;
   
   /**
    * The where clause.
    */
   SQLBooleanClause whereClause;

   /**
    * Constructs a delete statement given the result of the parsing process.
    *
    * @param parser The result of the parsing process.
    */
   SQLDeleteStatement(LitebaseParser parser)
   {
      type = SQLElement.CMD_DELETE;

      rsTable = parser.tableList[0]; // Creates the result set table.

      SQLBooleanClause  booleanClause = whereClause = parser.whereClause; // Gets the where clause.
      if (booleanClause != null && booleanClause.fieldList.length != booleanClause.fieldsCount)
      {
         // Compacts the resulting field list.
         SQLResultSetField[] compactFieldList = new SQLResultSetField[booleanClause.fieldsCount];
         Vm.arrayCopy(booleanClause.fieldList, 0, compactFieldList, 0, booleanClause.fieldsCount);
         booleanClause.fieldList = compactFieldList;

         // Compacts the parameter list.
         SQLBooleanClauseTree[] compactParamList = new SQLBooleanClauseTree[booleanClause.paramCount];
         Vm.arrayCopy(booleanClause.paramList, 0, compactParamList, 0, booleanClause.paramCount);
         booleanClause.paramList = compactParamList;
      }
   }

   /**
    * Sets the value of a short parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the short parameter.
    */
   void setParamValue(int index, short val)
   {
      checkIndex(index); // Checks if the index is within the range.
      whereClause.paramList[index].setParamValue(val); // Sets the value.
   }

   /**
    * Sets the value of an integer parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the integer parameter.
    */
   void setParamValue(int index, int val)
   {
      checkIndex(index); // Checks if the index is within the range.
      whereClause.paramList[index].setParamValue(val); // Sets the value.
   }

   /**
    * Sets the value of a long parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the long parameter.
    * @throws DriverException If the parameter index.
    */
   void setParamValue(int index, long val)
   {
      checkIndex(index); // Checks if the index is within the range.
      whereClause.paramList[index].setParamValue(val); // Sets the value.
   }

   /**
    * Sets the value of a float parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the float parameter.
    * @throws DriverException If the parameter index is invalid or the column parameter is not float.
    */
   void setParamValue(int index, float val)
   {
      checkIndex(index); // Checks if the index is within the range.
      whereClause.paramList[index].setParamValue(val); // Sets the value.
   }

   /**
    * Sets the value of a double parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the double parameter.
    */
   void setParamValue(int index, double val)
   {
      checkIndex(index); // Checks if the index is within the range.
      whereClause.paramList[index].setParamValue(val); // Sets the value.
   }

   /**
    * Sets the value of a string parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the string parameter.
    * @throws NullPointerException If a <code>null</code> is used as a parameter of a where clause.
    * @throws InvalidNumberException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void setParamValue(int index, String val) throws NullPointerException, InvalidNumberException, InvalidDateException
   {
      checkIndex(index); // Checks if the index is within the range.
      if (val == null) // A null can't be in a parameter of a where clause.
         throw new NullPointerException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PARAM_NULL)); 
      whereClause.paramList[index].setParamValue(val); // Sets the value.
   }

   /**
    * This was expected to set the value of a byte array parameter at the given index. Since blobs can't be in a where clause and a delete clause
    * only has parameters in a where clause, this method will only raise an exception.
    *
    * @param index The index of the parameter.
    * @param val The value of the blob parameter.
    * @throws SQLParseException Blobs can't be in a where clause.
    */
   void setParamValue(int index, byte[] val) throws SQLParseException
   {
      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_BLOB_WHERE));
   }
   
   // juliana@223_3: PreparedStatement.setNull() now works for blobs.
   /**
    * This was expected to set null in a given field. Since a null can't be in a parameter of a where clause, this method will only raise an 
    * exception.
    *
    * @param index The index of the parameter. Not used since a null can't be in a parameter of a where clause.
    * @throws SQLParserException A null can't be in a parameter of a where clause.
    */
   void setNull(int index) throws SQLParseException
   {
      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PARAM_NULL));
   }

   /**
    * Clears all parameter values of a prepared statement delete.
    */
   void clearParamValues()
   {
      SQLBooleanClause booleanClause = whereClause;
      if (booleanClause != null)
      {
         SQLBooleanClauseTree[] paramList = booleanClause.paramList;
         int i = booleanClause.paramCount;
         
         while (--i >= 0)
            paramList[i].isParamValueDefined = false;
      }
   }

   /**
    * Checks if all parameters values are defined.
    *
    * @throws DriverException If not all parameter values are defined.
    */
   void allParamValuesDefined() throws DriverException
   {
      SQLBooleanClause booleanClause = whereClause;
      if (booleanClause != null)
      {
         SQLBooleanClauseTree[] paramList = booleanClause.paramList;
         int i = booleanClause.paramCount;
         
         while (--i >= 0)
            if (!paramList[i].isParamValueDefined)
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_NOT_ALL_PARAMETERS_DEFINED));
      }
   }
   
   /**
    * Executes a delete statement.
    *
    * @param driver The connection with Litebase.
    * @return The number of rows deleted.
    * @throws DriverException If the record can't be removed from the indices.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    */
   int litebaseDoDelete(LitebaseConnection driver) throws IOException, InvalidDateException, InvalidNumberException
   {
      if (rsTable.table.db.db == null) // juliana@201_28: If a table is re-created after the prepared statement is parsed, there won't be a NPE.
         rsTable.table = driver.getTable(rsTable.tableName);
      
      Table table = rsTable.table;
      PlainDB plainDB = table.db;
      NormalFile dbFile = (NormalFile)plainDB.db;
      ByteArrayStream bas = plainDB.bas;
      DataStreamLB ds = plainDB.basds; // juliana@253_8: now Litebase supports weak cryptography.
      int nn = 0,
          columnCount = table.columnCount,
          i = columnCount,
          j;
      Index index;
      ComposedIndex ci;
      Index[] columnIndices = table.columnIndices;
      ComposedIndex[] composedIndices = table.composedIndices;

      // If there are indices, this is needed to remove the values from them.
      boolean wholeTable = whereClause == null,
              hasIndices = table.numberComposedIndices > 0; // juliana@201_6
      while (--i >= 0)
         if (columnIndices[i] != null)
         {
            hasIndices = true;
            break;
         }  

      // juliana@250_10: removed some cases when a table was marked as not closed properly without being changed.
      // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since 
      // its last opening. 
      table.setModified(); // Sets the table as not closed properly.
      
      // juliana@270_32: corrected a bug of a delete not updating the number of total deleted rows in the metadata when there is an index corruption.
      try 
      {
         if (wholeTable) // Deletes the whole table.
         {
            if (hasIndices) // If the whole table is being deleted, just empties all indexes.
            {
               i = columnCount;
               while (--i >= 0)
                  if ((index = columnIndices[i]) != null)
                     index.deleteAllRows();
               if ((i = table.numberComposedIndices) > 0) // juliana@201_6: it now deletes the erases the composed index when deleting the whole table.
                  while (--i >= 0)
                     composedIndices[i].index.deleteAllRows();
            }
            
            // juliana@227_10: Corrected a bug of a delete with no where clause not taking the already deleted rows into consideration when returning 
            // the number of deleted rows.
            nn = plainDB.rowCount - table.deletedRowsCount;
            i = table.deletedRowsCount = plainDB.rowCount;
            
            while (--i >= 0)
            {
               // Logically deletes the record: changes the attribute to 'deleted'.  
               plainDB.read(i);
               j = (ds.readInt() & Utils.ROW_ID_MASK) | Utils.ROW_ATTR_DELETED; 
               bas.reset();
               ds.writeInt(j);
               plainDB.rewrite(i);
            }
         }
         else
         {
            // guich@300: now all records are just marked as deleted instead of physical removal.
            int column;
            SQLValue[] keys1 = new SQLValue[1];
            SQLValue[] keys2;
            byte[] types = table.columnTypes;
            short[] offsets = table.columnOffsets;
            byte[] nulls = table.columnNulls[0];
            ResultSet rs = table.createSimpleResultSet(whereClause);
            rs.pos = - 1;
         
            if (hasIndices)
               while (rs.getNextRecord())
               {
                  i = columnCount; // juliana@201_35: Would not remove key from the index of the last column.
                  
                  // juliana@227_11: corrected a bug of an exception being thrown when trying to delete a row with a null in column which has an 
                  // index.
                  while (--i >= 0) // Simple index.
                     if (columnIndices[i] != null && (nulls[i >> 3] & (1 << (i & 7))) == 0)
                     {
                        index = columnIndices[i];
                        bas.reset(); // juliana@116_1: if reset is not done, the value read is wrong.
                        table.readValue(driver.sqlv, offsets[i], types[i], false, false); // juliana@220_3 juliana@230_14
                        keys1[0] = driver.sqlv;
                        index.tempKey.set(keys1);
                        index.removeValue(index.tempKey, rs.pos);
                     }
      
                  if ((i = table.numberComposedIndices) > 0) // Composed index.
                     while (--i >= 0)
                     {
                        ci = composedIndices[i];
                        index = ci.index;
                        keys2 = SQLValue.newSQLValues(j = ci.columns.length); // juliana@201_6
                        while (--j >= 0)
                        {
                           // juliana@116_1: if reset is not done, the value read is wrong.
                           bas.reset();
                           
                           // juliana@220_3
                           table.readValue(keys2[j], offsets[column = ci.columns[j]], types[column], false, false); // juliana@230_14
                           
                        }
                        index.tempKey.set(keys2);
                        index.removeValue(index.tempKey, rs.pos);
                     }
               
                  // Logically deletes the record: changes the attribute to 'deleted'.
                  i = (ds.readInt() & Utils.ROW_ID_MASK) | Utils.ROW_ATTR_DELETED;
                  bas.reset();
                  ds.writeInt(i);
                  plainDB.rewrite(rs.pos);
                  nn++; // Increments the number of deleted rows.
               }
            else
               while (rs.getNextRecord())
               {
                  // Logically deletes the record: changes the attribute to 'deleted'.
                  i = (ds.readInt() & Utils.ROW_ID_MASK) | Utils.ROW_ATTR_DELETED;
                  bas.reset();
                  ds.writeInt(i);
                  plainDB.rewrite(rs.pos);
                  nn++; // Increments the number of deleted rows.
               }
            table.deletedRowsCount += nn;
         }
         return nn;
      }
      finally
      {
         if (nn > 0)
            table.tableSaveMetaData(Utils.TSMD_ONLY_DELETEDROWSCOUNT);
    
         // juliana@227_3: improved table files flush dealing.
         // juliana@270_25: corrected a possible lose of records in recover table when 10 is passed to LitebaseConnection.setRowInc().
         if (!dbFile.dontFlush) // juliana@202_23: flushs the files to disk when row increment is the default.
         {  
            if (dbFile.cacheIsDirty)
               dbFile.flushCache(); // Flushs .db.
            if (((NormalFile)plainDB.dbo).cacheIsDirty)
               ((NormalFile)plainDB.dbo).flushCache(); // Flushs .dbo.
         }
      }
   }

   // nowosad@200
   /**
    * Binds a <code>SQL DELETE</code> expression.
    *
    * @param driver The connection with Litebase.
    * @return The delete statement binded.
    * @throws InvalidDateException If it is thrown by an internal method.
    * @throws InvalidNumberException If it is thrown by an internal method.
    * @throws IOException If it is thrown by an internal method. 
    */
   SQLDeleteStatement litebaseBindDeleteStatement(LitebaseConnection driver) throws InvalidDateException, InvalidNumberException, IOException
   {
      Table table = driver.getTable(rsTable.tableName);
      rsTable.table = table;

      if (whereClause != null) // Binds the delete statement to its table.
         whereClause.bindColumnsSQLBooleanClause(table.htName2index, table.columnTypes, rsTable);
      return this;
   }
   
   // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.   
   /**
    * Checks the prepared statement parameter index.
    * 
    * @param index The index of the prepared statement parameter.
    * @throws IllegalArgumentException If the index is out of range.
    */
   void checkIndex(int index) throws IllegalArgumentException
   {
      if (index < 0 || whereClause == null || index >= whereClause.paramCount) // Checks if the index is within the range.
         throw new IllegalArgumentException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
   }
}
