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

/**
 * Internal use only. Represents a SQL <code>SELECT</code> statement.
 */
class SQLSelectStatement extends SQLStatement
{
   /**
    * The select clause of the statement.
    */
   private SQLSelectClause selectClause;

   /**
    * The where clause of the statement.
    */
   SQLBooleanClause whereClause;

   /**
    * The group by clause of the statement.
    */
   SQLColumnListClause groupByClause; // juliana@226_14

   /**
    * The having clause of the statement.
    */
   private SQLBooleanClause havingClause;

   /**
    * The order by clause of the statement.
    */
   SQLColumnListClause orderByClause;

   /**
    * Creates a new select statement for a SQL <code>SELECT</code> query.
    *
    * @param parser The result of the parsing process.
    */
   SQLSelectStatement(LitebaseParser parser)
   {
      type = SQLElement.CMD_SELECT; // Sets the type of statement.
      
      // Sets the select clause and its hash table.
      (selectClause = parser.select).htName2index = new IntHashtable(parser.select.fieldsCount == 0? 3 : parser.select.fieldsCount);
      
      whereClause = parser.whereClause; // Sets the where clause.
      groupByClause = parser.groupBy; // Sets the group by clause.
      havingClause = parser.havingClause; // Sets the having clause.
      orderByClause = parser.orderBy; // Sets the order by clause.
   }

   /**
    * Sets the value of a short parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the short parameter.
    * @throws DriverException If the parameter index is invalid.
    */
   void setParamValue(int index, short val) throws DriverException
   {
      SQLBooleanClause whereClauseAux = whereClause,
                       havingClauseAux = havingClause;
      int whereParamCount = (whereClauseAux  == null? 0 : whereClauseAux.paramCount); // Gets the where clause parameter count.
      
      if (index < 0 || index >= (whereParamCount + (havingClauseAux == null? 0 : havingClauseAux.paramCount))) // Checks if the index is within the range.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
     
      if (index < whereParamCount)  // Sets the parameter value in its proper place.
         whereClauseAux.paramList[index].setParamValue(val);
      else
         havingClauseAux.paramList[index - whereParamCount].setParamValue(val);
   }

   /**
    * Sets the value of a integer parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the integer parameter.
    * @throws DriverException If the parameter index is invalid.
    */
   void setParamValue(int index, int val) throws DriverException
   {
      SQLBooleanClause whereClauseAux = whereClause,
                       havingClauseAux = havingClause;
      int whereParamCount = (whereClauseAux == null? 0 : whereClauseAux.paramCount); // Gets the where clause parameter count.
      
      if (index < 0 || index >= (whereParamCount + (havingClauseAux == null? 0 : havingClauseAux.paramCount))) // Checks if the index is within the range.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));

      if (index < whereParamCount) // Sets the parameter value in its proper place.
         whereClauseAux.paramList[index].setParamValue(val);
      else
         havingClauseAux.paramList[index - whereParamCount].setParamValue(val);
   }

   /**
    * Sets the value of a long parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the long parameter.
    * @throws DriverException If the parameter index is invalid.
    */
   void setParamValue(int index, long val) throws DriverException
   {
      SQLBooleanClause whereClauseAux = whereClause,
                       havingClauseAux = havingClause;
      int whereParamCount = (whereClauseAux == null? 0 : whereClauseAux.paramCount); // Gets the where clause parameter count.
      
      if (index < 0 || index >= (whereParamCount + (havingClauseAux == null? 0 : havingClauseAux.paramCount))) // Checks if the index is within the range.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
      
      if (index < whereParamCount) // Sets the parameter value in its proper place.
         whereClauseAux.paramList[index].setParamValue(val);
      else
         havingClauseAux.paramList[index - whereParamCount].setParamValue(val);
   }

   /**
    * Sets the value of a float parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the float parameter.
    * @throws DriverException If the parameter index is invalid.
    */
   void setParamValue(int index, float val) throws DriverException
   {
      SQLBooleanClause whereClauseAux = whereClause,
                       havingClauseAux = havingClause;
      int whereParamCount = (whereClauseAux == null ? 0 : whereClauseAux.paramCount); // Gets the where clause parameter count.

      if (index < 0 || index >= (whereParamCount + (havingClauseAux == null? 0 : havingClauseAux.paramCount))) // Checks if the index is within the range.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
      
      if (index < whereParamCount) // Sets the parameter value in its proper place.
         whereClauseAux.paramList[index].setParamValue(val);
      else
         havingClauseAux.paramList[index - whereParamCount].setParamValue(val);
   }

   /**
    * Sets the value of a double parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the double parameter.
    * @throws DriverException If the parameter index is invalid.
    */
   void setParamValue(int index, double val) throws DriverException
   {
      SQLBooleanClause whereClauseAux = whereClause,
                       havingClauseAux = havingClause;
      int whereParamCount = (whereClauseAux == null ? 0 : whereClauseAux.paramCount); // Gets the where clause parameter count. 

      if (index < 0 || index >= (whereParamCount + (havingClauseAux == null ? 0 : havingClauseAux.paramCount))) // Checks if the index is within the range.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
      
      if (index < whereParamCount) // Sets the parameter value in its proper place.
         whereClauseAux.paramList[index].setParamValue(val);
      else
         havingClauseAux.paramList[index - whereParamCount].setParamValue(val);
   }

   /**
    * Sets the value of a string parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the string parameter.
    * @throws DriverException If the parameter index is invalid.
    * @throws SQLParserException If a <code>null</code> is used as a parameter of a where clause.
    * @throws InvalidNumberException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void setParamValue(int index, String val) throws DriverException, SQLParseException, InvalidNumberException, InvalidDateException
   {
      SQLBooleanClause whereClauseAux = whereClause,
                       havingClauseAux = havingClause;
      int whereParamCount = (whereClauseAux == null ? 0 : whereClauseAux.paramCount); // Gets the where clause parameter count.
      
      if (index < 0 || index >= (whereParamCount + (havingClauseAux == null? 0 : havingClauseAux.paramCount))) // Checks if the index is within the range.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
      
      if (val == null) // A null can't be in a parameter of a where clause.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PARAM_NULL)); 
      
      if (index < whereParamCount) // Sets the parameter value in its proper place.
         whereClauseAux.paramList[index].setParamValue(val);
      else
         havingClauseAux.paramList[index - whereParamCount].setParamValue(val);
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
    * This was expected to set the value of a byte array parameter at the given index. Since blobs can't be in a where clause and a select clause 
    * only has parameters in a where clause, this method will only raise an exception.
    *
    * @param index The index of the parameter.
    * @param val The value of the blob parameter.
    * @throws SQLParseException Blobs can't be in a where clause.
    */
   void setParamValue(int index, byte[] val)
   {
      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_BLOB_WHERE));
   }

   /**
    * Clears all parameter values of a prepared statement select.
    */
   void clearParamValues()
   {
      SQLBooleanClauseTree[] paramList;
      SQLBooleanClause clause;
      int i;
      
      if ((clause = whereClause) != null) // Clears all the parameters of the where clause.
      {
         paramList = clause.paramList;
         i = clause.paramCount;
         while (--i >= 0)
            paramList[i].isParamValueDefined = false;
      }
      
      if ((clause = havingClause) != null) // Clears all the parameters of the having clause.
      {
         paramList = clause.paramList;
         i = clause.paramCount - 1;
         while (--i >= 0)
            paramList[i].isParamValueDefined = false;
      }
   }

   /**
    * Checks if all the parameters values are defined.
    *
    * @throws DriverException If not all parameter values are defined.
    */
   void allParamValuesDefined() throws DriverException
   {
      int i;
      SQLBooleanClauseTree[] paramList;
      SQLBooleanClause clause;

      if ((clause = whereClause) != null) // Checks if all the where clause parameters are defined.
      {
         paramList = clause.paramList;
         i = clause.paramCount;
         while (--i >= 0)
            if (!paramList[i].isParamValueDefined)
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_NOT_ALL_PARAMETERS_DEFINED));
      }

      if ((clause = havingClause) != null) // Checks if all the having clause parameters are defined.
      {
         paramList = clause.paramList;
         i = clause.paramCount;
         while (--i >= 0)
            if (!paramList[i].isParamValueDefined)
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_NOT_ALL_PARAMETERS_DEFINED));
      }
      // All parameters are defined.
   }

   // Modified to support the new SQL clauses (ORDER BY, GROUP BY, HAVING).
   // Modified to use temporary tables except when the query is a simple select (select * from tablename).
   /**
    * Executes the select statement.
    *
    * @param driver The connection with Litebase.
    * @return The query result set.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it. 
    */
   ResultSet litebaseDoSelect(LitebaseConnection driver) throws IOException, InvalidDateException, InvalidNumberException
   {
      Table rsBaseTable;
      boolean isSimpleSelect = false;
      SQLResultSetTable[] tableList = selectClause.tableList;
      int i = tableList.length;
      
      while (--i >= 0) // juliana@201_28: If a table is re-created after the prepared statement is parsed, there won't be a NPE.
         if (tableList[i].table.db.db == null) 
            tableList[i].table = driver.getTable(tableList[i].tableName);
      
      // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
      // juliana@114_10: simple selects do not use temporary tables.
      // juliana@212_4: if the select fields are in the table order beginning with rowid, do not build a temporary table.
      if (groupByClause == null && havingClause == null && orderByClause == null && whereClause == null && !selectClause.hasAggFunctions 
       && selectClause.tableList.length == 1)
      {
         isSimpleSelect = true;
         rsBaseTable = selectClause.tableList[0].table;
         rsBaseTable.answerCount = -1;
      }
      else
      {
         rsBaseTable = generateResultSetTable(driver); // Temporary table.

         if (rsBaseTable.name == null)
         {   
            // Remaps the table column names to use the aliases of the select statement instead of the original column names.
            rsBaseTable.remapColumnsNames2Aliases(selectClause.fieldList);
         
            // This must be used only for temporary tables.
            rsBaseTable.plainShrinkToSize(); // guich@201_9: always shrink the .db and .dbo memory files.
         } 
      }

      // Creates the result set without passing any fields or WHERE clause, since all the records and all the fields in the temporary table are part 
      // of the result set.
      ResultSet rs = rsBaseTable.createResultSetForSelect(null);

      rs.htName2index = selectClause.htName2index; // Stores the hash table.
      rs.fields = selectClause.fieldList; // Stores the field list for the meta data.
      rs.isSimpleSelect = isSimpleSelect; // juliana@114_10: indicates if it is a simple select or not.
      rs.driver = driver; // juliana@220_3
      
      if (rsBaseTable.answerCount >= 0)
      {
         rs.answerCount = rsBaseTable.answerCount;
         
         // juliana@263_3: corrected a bug where a new result set data could overlap an older result set data if both were related to the same table.
         Vm.arrayCopy(rsBaseTable.allRowsBitmap, 0, rs.allRowsBitmap = new byte[rsBaseTable.allRowsBitmap.length], 0, 
                      rsBaseTable.allRowsBitmap.length);
      }
      return rs;
   }
   
   /**
    * Binds a select statement.
    *
    * @param driver The Litebase connection.
    * @return A select statement binded.
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    */
   SQLSelectStatement litebaseBindSelectStatement(LitebaseConnection driver) throws InvalidDateException, IOException, InvalidNumberException
   {
      // Gets all tables from the select statement.
      SQLResultSetTable[] list = selectClause.tableList; 
      int i = list.length;
      while (--i >= 0)
         list[i].table = driver.getTable(list[i].tableName);

      bindSelectStatement(driver); // Validates and binds the select statement to the table list.
      orderTablesToJoin(); // Finds the best table order for the join operation.
      validateSelectStatement(); // Validates the select statement.
      return this;
   }
   
   /**
    * Tries to put as inner table a table that has an index used more often in the where clause, when the where clause has a comparison between 
    * fields from different tables. e.g.: <code>select * from table1, table2 where table1.field1 = table2.field2 </code> If only 
    * <code>table1.field1</code> has index, changes the select to: <code>select * from table2, table1 where table1.field1 = table2.field2</code>. 
    * If both tables has the same level of index using, sorts them by the row count.
    */
   private void orderTablesToJoin()
   {
      // Gets the number of tables.
      SQLResultSetTable[] list = selectClause.tableList;
      int size = list.length; 

      if (size == 1) // size == 1 is not a join.
         return;

      SQLResultSetField[] fields = whereClause != null? whereClause.fieldList : null;
      int i = size,
          j,
          startedIndexAux,
          highest;
      SQLResultSetTable rsTableAux1,
                        rsTableAux2;
      Table table1,
            table2;
      int[] startedIndex = new int[size];
      int[] changedTo = new int[size];
      
      // Starts the weight of the where clause expression tree.
      while (--i >= 0)
      {
         list[i].table.weight = 0;
         startedIndex[i] = changedTo[i] = i;
      }
      if (whereClause != null)
         whereClause.expressionTree.weightTheTree();

      i = size;
      while (--i >= 0) // Reorders the tables according to the weight.
      {
         highest = -1;
         table1 = (rsTableAux1 = list[j = i]).table;
         
         while (--j >= 0)
            // juliana@238_2: improved join table reordering.
            // Takes the table size into consideration.
            if (table1.weight > (table2 = (rsTableAux2 = list[j]).table).weight 
             || (table1.weight == table2.weight && table1.db.rowCount > table2.db.rowCount))
            {
               rsTableAux1 = rsTableAux2;
               highest = j;
            }
            
         if (highest != -1) // Changes table order.
         {
            list[highest] = list[i];
            list[i] = rsTableAux1;
            changedTo[startedIndex[highest]] = i;
            changedTo[startedIndexAux = startedIndex[i]] = highest;
            startedIndex[i] = startedIndex[highest];
            startedIndex[highest] = startedIndexAux;
         }
      }

      if (whereClause != null) // Rearranges the indexRs of the where clause fieldlist.
      {   
         i = whereClause.fieldsCount;
         while (-- i >= 0)
            fields[i].indexRs = changedTo[fields[i].indexRs];  
      }
   }

   // juliana@212_4

   /**
    * Binds the SQLSelectStatement to the select clause tables.
    *
    ** @param driver The Litebase connection.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it. 
    */
   private void bindSelectStatement(LitebaseConnection driver) throws InvalidDateException, InvalidNumberException
   {
      // First thing to do is to bind the columns in all clauses.
      SQLSelectClause select = selectClause;
      IntHashtable names2Index = select.htName2index;
      SQLResultSetTable[] listRsTables = select.tableList;
      int i = listRsTables.length, 
          j = 0, 
          totalLen = 0;
      Table table;

      while (--i >= 0)
         totalLen += listRsTables[i].table.columnCount;

      byte[] types = new byte[totalLen];

      i = listRsTables.length;
      while (--i >= 0) // Adds the column types properties of all tables to a big array.
      {
         table = listRsTables[i].table;
         Vm.arrayCopy(table.columnTypes, 0, types, j, table.columnCount);
         j += table.columnCount;
      }

      // Binds the SQL Clauses. Note: The HAVING clause will have a late binding.
      select.bindColumnsSQLSelectClause(driver); // Binds the select clause.
      if (whereClause != null) // Binds the where clause if it exists.
         whereClause.bindColumnsSQLBooleanClause(names2Index, types, select.tableList);
      if (groupByClause != null) // Binds the group by clause if it exists.
         groupByClause.bindColumnsSQLColumnListClause(names2Index, types, select.tableList);
      if (orderByClause != null) // Binds the order by clause if it exists.
         orderByClause.bindColumnsSQLColumnListClause(names2Index, types, select.tableList);
   }

   /**
    * Validates the SQLSelectStatement.
    *
    * @throws SQLParseException If a query with group by is not well-formed, if there is a having clause without an aggregation, a field in the 
    * having clause is not in the select clause, there is no order by and there are aggregated functions mixed with real columns, or there is 
    * an aggregation with an order by clause and no group by clause.
    */
   private void validateSelectStatement() throws SQLParseException
   {
      SQLColumnListClause groupBy = groupByClause,
                          orderBy = orderByClause;
      SQLSelectClause select = selectClause;
      
      // Checks if the order by and group by clauses matchs.
      if (groupBy != null && orderBy != null)
         groupBy.checkEquality(orderBy);

      int selectCount = select.fieldsCount,
          i = selectCount, 
          j;
      SQLResultSetField field1, 
                        field2;
      SQLResultSetField[] selectFields = select.fieldList;

      if (groupBy != null) // Validates the group by clause if it exists.
      {
         SQLBooleanClause having = havingClause;
         
         while (--i >= 0)
         {
            field1 = selectFields[i];

            // For now, there is no support for queries with GROUP BY and virtual columns in the SELECT clause that are not aggregated functions.
            if (!field1.isAggregatedFunction && field1.isVirtual)
               throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_VIRTUAL_COLUMN_ON_GROUPBY));

            // Checks if every non-aggregated function field that is listed in the SELECT clause is listed in the GROUP BY clause.
            if (!field1.isAggregatedFunction)
               groupBy.sqlcolumnlistclauseContains(field1.tableColIndex);
         }

         // Checks if all fields referenced in the HAVING clause are listed as aliased aggregated functions in the SELECT clause.
         if (having != null)
         {
            i = having.fieldsCount;
            SQLResultSetField[] havingFields = having.fieldList;
            boolean found;

            while (--i >= 0)
            {
               field1 = havingFields[i];
               found = false;
               j = selectCount;

               while (--j >= 0)
               {
                  if (field1.aliasHashCode == (field2 = selectFields[j]).aliasHashCode)
                  {
                     if (field2.isAggregatedFunction)
                     {
                        found = true;
                        break;
                     }
                     
                     // It is not an aggregated function. Throws an exception.
                     throw new SQLParseException(field1.alias + LitebaseMessage.getMessage(LitebaseMessage.ERR_IS_NOT_AGGREG_FUNCTION));
                  }
               }
               
               if (!found) // The having clause fields must be in the select clause fields.
                  throw new SQLParseException(field1.alias + LitebaseMessage.getMessage(LitebaseMessage.ERR_WAS_NOT_LISTED_ON_AGGREG_FUNCTION));
            }
         }
      }
      else
      {
         // If there is no 'GROUP BY' clause, there can not be aggregated functions mixed with real columns in the SELECT clause.
         if (select.hasRealColumns && select.hasAggFunctions)
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANNOT_MIX_AGGREG_FUNCTION));

         // If there are aggregate functions with an ORDER BY clause with no GROUP BY clause, also raises an exception.
         if (select.hasAggFunctions && orderBy != null)
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANNOT_HAVE_AGGREG_AND_NO_GROUPBY));
      }
   }
   
   /**
    * Generates a table to store the result set.
    *
    * @param driver The connection with Litebase.
    * @return The temporary result set table.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it. 
    */
   private Table generateResultSetTable(LitebaseConnection driver) throws IOException, InvalidDateException, InvalidNumberException
   {
      ResultSet[] listRsTemp;
      SQLSelectClause select = selectClause;
      SQLColumnListClause orderBy = orderByClause,
                          groupBy = groupByClause;
      SQLBooleanClause where = whereClause,
                       having = havingClause;
      SQLResultSetTable[] tableList = select.tableList;
      SQLResultSetField[] fieldList = select.fieldList;
      SQLColumnListClause sortListClause = orderBy == null? groupBy : orderBy;
      int numTables = tableList.length,
          i = numTables, 
          count = sortListClause != null? sortListClause.fieldsCount : 0, 
          totalRecords, 
          selectFieldsCount = select.fieldsCount;
      Table tableOrig = null;
      
      if (numTables == 1) // The query is not a join.
         (tableOrig = tableList[0].table).answerCount = -1;

      // Optimization for queries that just wants to count the number of records of a table ("SELECT COUNT(*) FROM TABLE").
      boolean countQueryWithWhere = false;
      String countAlias = null;

      // juliana@226_13: corrected a bug where a query of the form "select year(field) as years from table" would be confunded with 
      // "select count(*) as years from table".
      if (selectFieldsCount == 1 && groupBy == null && fieldList[0].sqlFunction == SQLElement.FUNCTION_AGG_COUNT && fieldList[0].isAggregatedFunction)
      {
         countAlias = fieldList[0].alias;
         if (where == null)
         {
            totalRecords = 1;
            while (--i >= 0)
            {
               tableOrig = tableList[i].table;
               totalRecords *= tableOrig.db.rowCount - tableOrig.deletedRowsCount;
            }
            return createIntValueTable(driver, totalRecords, countAlias);
         }
         countQueryWithWhere = true;
      }

      // Gathers meta data for the temporary table that will store the result set.
      byte[] columnTypes = new byte[count + selectFieldsCount];
      short[] columnIndexes = new short[count + selectFieldsCount];
      int[] columnHashes = new int[count + selectFieldsCount];
      int[] columnSizes = new int[count + selectFieldsCount];
      Table[] columnIndexesTables = new Table[count + selectFieldsCount];
      int size = 0;

      SQLResultSetField field, 
                        param;
      ResultSet rsTemp = null;
      
      // juliana@270_24: corrected a possible application crash or exception when using order/group by with join.
      IntHashtable colHashesTable = new IntHashtable(selectFieldsCount); // The hash table of the columns.

      // Maps the aggregated function parameter column indexes to the aggregate function code.
      IntHashtable aggFunctionsTable = new IntHashtable(selectFieldsCount);

      i = -1;
      while (++i < selectFieldsCount)
      {
         columnSizes[size] = (field = fieldList[i]).size;

         // Decides which hash code to use as the column name in the temporary table and which data type to assign to the temporary table.
         if (field.isVirtual)
         {
            if (field.isAggregatedFunction)
            {
               // Finds the fields that are parameter for a MAX() and MIN() function that can use an index.
               // juliana@230_21: MAX() and MIN() now use indices on simple queries.
               if (field.sqlFunction == SQLElement.FUNCTION_AGG_MAX || field.sqlFunction == SQLElement.FUNCTION_AGG_MIN)
                  field.findMaxMinIndex();
               
               aggFunctionsTable.put(i, field.sqlFunction);
            }
            if ((param = field.parameter) == null)
            {
               columnHashes[size] = field.aliasHashCode; // Uses the alias hash code instead.

               // This is just a place holder, since this column does not map to any column in the database.
               columnIndexes[size] = (short)-1;
               columnIndexesTables[size] = null;

               columnTypes[size++] = (byte)field.dataType; // Uses the field data type.
            }
            else
            {
               // Uses the parameter hash and data type.
               columnTypes[size] = (byte)param.dataType;
               columnHashes[size] = field.aliasHashCode;
               columnIndexes[size] = (short)param.tableColIndex;
               columnIndexesTables[size++] = field.table;
               
               // juliana@270_24: corrected a possible application crash or exception when using order/group by with join.
               colHashesTable.put(param.aliasHashCode, 1); // juliana@253_1: corrected a bug when sorting if the sort field is in a function.
            }
         }
         else
         {
            // A real column was selected.
            columnTypes[size] = (byte)field.dataType;
            columnHashes[size] = field.tableColHashCode;
            columnIndexes[size] = (short)field.tableColIndex;
            columnIndexesTables[size++] = field.table;
            
            // juliana@270_24: corrected a possible application crash or exception when using order/group by with join.
            colHashesTable.put(field.aliasHashCode, 0); // juliana@253_1: corrected a bug when sorting if the sort field is in a function.
            colHashesTable.put(field.tableColHashCode, 0);
         }
      }

      if (sortListClause != null)
      {
         sortListClause.findSortIndex(); // juliana@230_29: order by and group by now use indices on simple queries.
         
         // Checks if all columns listed in the order by/group by clause were selected. If not, includes the ones that are missing.
         // It must be remembered that, if both present, group by and order by must match. So, it does not matter which one is picked.
         
         fieldList = sortListClause.fieldList;

         i = -1;
         while (++i < count)
         {
            // juliana@253_1: corrected a sort causing AOOIBE if the sort field is in a function.
            // juliana@270_24: corrected a possible application crash or exception when using order/group by with join.
            if (colHashesTable.get((field = fieldList[i]).aliasHashCode, -1) == 0)
               continue;

            // The sorting column is missing. Adds it to the temporary table.
            columnTypes[size] = (byte)field.dataType;
            columnSizes[size] = field.size;
            columnHashes[size] = field.tableColHashCode;
            columnIndexesTables[size] = field.table;
            columnIndexes[size++] = (short)field.tableColIndex;
         }
      }

      // Creates the temporary table to store records that satisfy the WHERE clause.
      Table tempTable = null;
      totalRecords = 0;
      boolean useIndex = true;

      // juliana@230_21: MAX() and MIN() now use indices on simple queries.
      // For optimization, the first temporary table will NOT be created, in case there is no WHERE clause and sort clause (either ORDER BY or GROUP 
      // BY) and the SELECT clause contains aggregated functions. In this case calculation of the aggregated functions will be made on the existing 
      // table.
      if (where == null && sortListClause == null && select.hasAggFunctions && numTables == 1)
      {   
         // In this case, there is no need to create the temporary table. Just points to the necessary structures of the original table.
         totalRecords = (tempTable = tableOrig).db.rowCount;
         
         // The index should not be used for MAX() and MIN() if not all the fields are MAX() and MIN() or one of the parametes cannot use an index.
         i = -1;
         while (++i < selectFieldsCount)
            if (!(field = fieldList[i]).isAggregatedFunction || field.index < 0 
             || (field.sqlFunction != SQLElement.FUNCTION_AGG_MAX && field.sqlFunction != SQLElement.FUNCTION_AGG_MIN)) 
            {
               useIndex = false;
               break;
            }
      }
      else 
      {
         // Creates a result set from the table, using the current WHERE clause applying the table indexes.
         rsTemp = (listRsTemp = createListResultSetForSelect(tableList, where))[0];
         
         // The index should not be used for MAX() and MIN() if there is a join, a sort or the indices do not resolve all the query.
         if (sortListClause == null && (where == null || where.expressionTree == null) && numTables == 1)
         {
            // The index should not be used for MAX() and MIN() if not all the fields are MAX() and MIN() or one of the parametes cannot use an 
            // index.
            i = -1; 
            while (++i < selectFieldsCount)
               if (!(field = fieldList[i]).isAggregatedFunction || field.index < 0 
                || (field.sqlFunction != SQLElement.FUNCTION_AGG_MAX && field.sqlFunction != SQLElement.FUNCTION_AGG_MIN)) 
               {
                  useIndex = false;
                  break;
               }
         }
         else
            useIndex = false;
         
         // juliana@230_29: order by and group by now use indices on simple queries.
         // Only uses index when sorting if all the indices are applied.
         if (sortListClause != null && ((where != null && where.expressionTree != null) || select.hasAggFunctions || numTables != 1))
            sortListClause.index = -1;
                        
         // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
         if ((sortListClause != null && sortListClause.index == -1) || countQueryWithWhere || numTables != 1)
         {
            // Optimization for queries of type "SELECT COUNT(*) FROM TABLE WHERE..." Just counts the records of the result set and writes it to a 
            // table.
            if (countQueryWithWhere && numTables == 1)
            {
               where.sqlBooleanClausePreVerify();
               rsTemp.pos = -1;
               while (rsTemp.getNextRecord())
                  totalRecords++;
               if (rsTemp.table.name == null)
                  rsTemp.table.db = null;
               return createIntValueTable(driver, totalRecords, countAlias);
            }
   
            // Creates the temporary table to store the result set records.
            // Not creating a new array for hashes means BUM!
            tempTable = driver.driverCreateTable(null, null, duplicateIntArray(columnHashes, size), duplicateByteArray(columnTypes, size), 
                                                 duplicateIntArray(columnSizes, size), null, null, Utils.NO_PRIMARY_KEY, Utils.NO_PRIMARY_KEY, null);
  
            // Writes the result set records to the temporary table.
            totalRecords = tempTable.writeResultSetToTable(listRsTemp, duplicateShortArray(columnIndexes, size), selectClause, columnIndexesTables, 
                                                                                                                 where != null ? where.type : -1);
   
            if (select.type == SQLSelectClause.COUNT_WITH_WHERE)
               return createIntValueTable(driver, totalRecords, countAlias);
   
            if (totalRecords == 0) // No records retrieved. Exit.
               return tempTable;
         }  
         
         // A query that use index for MAX() and MIN() should not check now which rows are answered.
         else if (useIndex)
            tempTable = tableOrig;
         else if (sortListClause == null)
         {
            byte[] allRowsBitmap = tableOrig.allRowsBitmap;
            int newLength = (tableOrig.db.rowCount + 7) >> 3,
                oldLength = allRowsBitmap == null? -1 : allRowsBitmap.length;
            
            if (newLength > oldLength)
               tableOrig.allRowsBitmap = allRowsBitmap = new byte[newLength];
            else
               Convert.fill(allRowsBitmap, 0, oldLength, 0);
            computeAnswer(rsTemp);

            if (!select.hasAggFunctions)
               return tableOrig;
            else
               totalRecords = (tempTable = tableOrig).answerCount;
         }
      }
      
      // juliana@230_29: order by and group by now use indices on simple queries.
      if (sortListClause != null) // Sorts the temporary table, if required.
      {
         if (sortListClause.index == -1) 
            tempTable.sortTable(groupBy, orderBy, driver); // juliana@220_3
         else 
         {
            tempTable = driver.driverCreateTable(null, null, duplicateIntArray(columnHashes, size), duplicateByteArray(columnTypes, size), 
                                                 duplicateIntArray(columnSizes, size), null, null, Utils.NO_PRIMARY_KEY, Utils.NO_PRIMARY_KEY, null);
            
            PlainDB plainDB = tempTable.db;
            SQLValue[] record = SQLValue.newSQLValues(tempTable.columnCount);
            Index index;
                  
            plainDB.rowAvail = (rsTemp.rowsBitmap == null? tableOrig.db.rowCount - tableOrig.deletedRowsCount : 
                                                           Utils.countBits(rsTemp.rowsBitmap.items));
            plainDB.db.growTo(plainDB.rowAvail++ * plainDB.rowSize);

            if (sortListClause.isComposed)
               index = tableOrig.composedIndices[sortListClause.index].index;
            else
               index = tableOrig.columnIndices[sortListClause.index];
            if (sortListClause.fieldList[0].isAscending)
               index.sortRecordsAsc(rsTemp.rowsBitmap, tempTable, record, duplicateShortArray(columnIndexes, size), selectClause);
            else
               index.sortRecordsDesc(rsTemp.rowsBitmap, tempTable, record, duplicateShortArray(columnIndexes, size), selectClause);
            if ((totalRecords = plainDB.rowCount) == 0)
               return tempTable;
            if (groupBy != null)
               groupBy.bindColumnsSQLColumnListClause(tempTable.htName2index, tempTable.columnTypes, null);
         }
      }
      // There is still one new temporary table to be created, if:
      // 1) The select clause has aggregate functions, or
      // 2) There is a group by clause.
      if (!select.hasAggFunctions && groupBy == null)
         return tempTable;

      // When creating the new temporary table, removes the extra fields that were created to perform the sort.
      // juliana@270_24: corrected a possible application crash or exception when using order/group by with join.
      if (sortListClause != null && (count = tempTable.columnCount) != selectFieldsCount)
         size = selectFieldsCount;

      // Also updates the types and hashcodes to reflect the types and aliases of the final temporary table, since they may still reflect the 
      // aggregated functions parameter list types and hashcodes.
      byte[] columnTypesItems = duplicateByteArray(columnTypes, size); // Not creating a new array means BUM!
      int[] columnSizesItems = duplicateIntArray(columnSizes, size);
      int[] columnHashesItems = duplicateIntArray(columnHashes, size); 

      // First preserves the original types, since they will be needed in the aggregated functions running totals calculation.
      byte[] origColumnTypesItems = tempTable.columnTypes;

      fieldList = select.fieldList;
      i = selectFieldsCount;
      
      while (--i >= 0)
      {
         columnTypesItems[i] = (byte)(field = fieldList[i]).dataType;
         columnSizesItems[i] = field.size;
         columnHashesItems[i] = field.aliasHashCode;
      }

      // Creates the second temporary table.
      Table tempTable2 = driver.driverCreateTable(null, null, columnHashesItems, columnTypesItems, columnSizesItems, null, null, 
                                                                                  Utils.NO_PRIMARY_KEY, Utils.NO_PRIMARY_KEY, null);
      
      count = totalRecords; // Starts writing the records from the first temporary table into the second temporary table.

      // Aggregates functions local variables.
      SQLValue[] aggFunctionsRunTotals = null;
      int[] aggFunctionsParamCols = null;
      int[] aggFunctionsRealParamCols = null;
      int[] aggFunctionsCodes = null;
      int[] paramCols = null;

      int aggFunctionsColsCount = 0,
          groupCount = 0; // Variable to count how many records are currently listed in the group.
      boolean aggFunctionExist = select.hasAggFunctions; // Initializes the aggregated functions running totals.

      if (aggFunctionExist && !useIndex)
      {
         aggFunctionsRunTotals = SQLValue.newSQLValues(selectFieldsCount);
         aggFunctionsParamCols = aggFunctionsTable.getKeys().items;
         aggFunctionsColsCount = aggFunctionsParamCols.length;
         aggFunctionsCodes = aggFunctionsTable.getValues().items;

         // If the temporary table points to a real table, stores also the column indexes of aggregate functions parameter list from the real table.
         if (tempTable.name != null)
         {
            aggFunctionsRealParamCols = new int[aggFunctionsColsCount];
            i = aggFunctionsColsCount;
            while (--i >= 0)
            {
               param = fieldList[aggFunctionsParamCols[i]].parameter;
               aggFunctionsRealParamCols[i] = (param == null? -1 : param.tableColIndex);
            }
         }
      }

      SQLValue[] record1 = SQLValue.newSQLValues((count = tempTable.columnCount) > selectFieldsCount? count : selectFieldsCount);
      SQLValue[] curRecord = record1;
      
      // juliana@230_21: MAX() and MIN() now use indices on simple queries.
      if (useIndex)
      {
         // No rows in the answer.
         if (tableOrig.db.rowCount - tableOrig.deletedRowsCount == 0 || (where != null && Utils.countBits(rsTemp.rowsBitmap.items) == 0))
            return tempTable2;
         
         Index index;
         byte[] nulls = tempTable2.columnNulls[0];
         IntVector rowsBitmap = (rsTemp == null? null : rsTemp.rowsBitmap);
         
         // Computes the MAX() and MIN() for all the fields.
         i = -1;
         curRecord[0].isNull = true; // No rows yet.
         while (++i < selectFieldsCount)
         {
            if ((field = fieldList[i]).isComposed)
               index = tableOrig.composedIndices[field.index].index;
            else
               index = tableOrig.columnIndices[field.index];
            if (field.sqlFunction == SQLElement.FUNCTION_AGG_MAX)
               index.findMaxValue(curRecord[i], rowsBitmap);
            else
               index.findMinValue(curRecord[i], rowsBitmap);
         }
         if (curRecord[0].isNull) // No rows found: returns an empty table.
            return tempTable2;
         Convert.fill(nulls, 0, nulls.length, 0);
         tempTable2.writeRSRecord(curRecord);
         return tempTable2;
      }
      
      SQLValue[] record2 = SQLValue.newSQLValues(record1.length);
      SQLValue[] prevRecord = null;
      byte[] nullsPrevRecord = tempTable.columnNulls[0];
      byte[] nullsCurRecord = tempTable.columnNulls[1];
      boolean writeDelayed = aggFunctionExist || groupByClause != null;
      
      // Loops through the records of the temporary table, to calculate agregated values and/or write the group records.
      boolean isTableTemporary = tempTable.name == null;
      paramCols = (isTableTemporary? aggFunctionsParamCols : aggFunctionsRealParamCols);

      int[] groupCountCols = new int[aggFunctionsColsCount]; // Each column has a groupCount because of the null values.
      byte[] allRowsBitMap = tempTable.allRowsBitmap;
      int j,
          colIndex,
          sqlAggFunction,
          colType,
          row = -1,
          numberRows = tempTable.db.rowCount,
          answerCount = tempTable.answerCount;
      SQLValue aggValue,
               value;
                     
      for (i = -1; ++i < totalRecords; groupCount++)
      {
         if (answerCount >= 0)
         {
            while (row++ < numberRows && (allRowsBitMap[row >> 3] & (1 << (row & 7))) == 0);
            tempTable.readRecord(curRecord, row, 1, driver, null, true, null);
         }
         else
            tempTable.readRecord(curRecord, i, 1, driver, null, true, null); // juliana@220_3 juliana@227_20
         if (!isTableTemporary && !tempTable.db.recordNotDeleted()) // Because it is possible to be pointing to a real table, skips deleted records.
         {
            groupCount--;
            continue;
         }

         // In case there is a group by, checks if there was a change in the group record composition.
         if (groupBy != null && groupCount != 0 
          && Utils.compareRecords(prevRecord, curRecord, nullsPrevRecord, nullsCurRecord, groupBy.fieldList) != 0)
         {
            if (aggFunctionExist) // Checks if there are aggregate functions.

               // Concludes any aggregated function calculation.
               endAggFunctionsCalc(prevRecord, groupCount, aggFunctionsRunTotals, aggFunctionsCodes, aggFunctionsParamCols, paramCols, 
                                                                                  aggFunctionsColsCount, origColumnTypesItems, groupCountCols);

            // Flushs the previous record and starts a new group counting.
            // Takes the null values for the non-aggregate fields into consideration.
            tempTable2.columnNulls[0] = nullsPrevRecord;
            j = aggFunctionsColsCount;
            while (--j >= 0)
            {
               Utils.setBit(tempTable2.columnNulls[0], aggFunctionsParamCols[j], groupCountCols[j] == 0);
               groupCountCols[j] = 0;
            }
            tempTable2.writeRSRecord(prevRecord);
            groupCount = 0;
         }

         if (aggFunctionExist) // Checks if there are aggregate functions.
         {
            // Performs the calculation of the aggregate functions.
            j = aggFunctionsColsCount;
            while (--j >= 0)
            {
               // Increments the count(*). NOTE: in the future, implementation of count(field_name) needs verify the null values.
               if (aggFunctionsCodes[j] == 0) 
               {
                  groupCountCols[j]++;
                  continue;
               }

               if ((colIndex = paramCols[j]) < 0 || (nullsCurRecord[colIndex >> 3] & (1 << (colIndex & 7))) != 0) // The agg functions skip nulls.
                  continue;

               groupCountCols[j]++;
               sqlAggFunction = aggFunctionsCodes[j];
               colType = origColumnTypesItems[colIndex];

               aggValue = aggFunctionsRunTotals[j];
               value = curRecord[colIndex];

               switch (sqlAggFunction)
               {
                  case SQLElement.FUNCTION_AGG_AVG:
                  case SQLElement.FUNCTION_AGG_SUM:
                  {
                     switch (colType) // Checks the type of the column.
                     {
                        case SQLElement.SHORT:
                           aggValue.asDouble += value.asShort;
                           break;

                        case SQLElement.INT:
                           aggValue.asDouble += value.asInt;
                           break;

                        case SQLElement.LONG:
                           aggValue.asDouble += value.asLong;
                           break;

                        case SQLElement.FLOAT:
                        case SQLElement.DOUBLE:
                           aggValue.asDouble += value.asDouble;
                           break;

                        // AVG and SUM can't be used with date and datetime.
                        case SQLElement.DATE: // rnovais@567_2
                        case SQLElement.DATETIME:
                           throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_SUM_AVG_WITH_DATE_DATETIME));
                     }

                     break;
                  }

                  // juliana@226_5: the aggregation functions MAX() and MIN() now work for CHAR, VARCHAR, CHAR NOCASE, and VARCHAR NOCASE column 
                  // types. 
                  case SQLElement.FUNCTION_AGG_MAX:
                  {
                     switch (colType) // Checks the type of the column
                     {
                        case SQLElement.CHARS: // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets. 
                           if (groupCountCols[j] == 1 || aggValue.asString.compareTo(value.asString) < 0)
                           {
                              aggValue.asString = value.asString;
                              aggValue.asInt = value.asInt;
                              aggValue.asLong = value.asLong;
                           }
                           break;
                        
                        case SQLElement.SHORT:
                           if (groupCountCols[j] == 1 || aggValue.asShort < value.asShort)
                              aggValue.asShort = value.asShort;
                           break;

                        case SQLElement.DATE: // rnovais@567_2
                        case SQLElement.INT:
                           if (groupCountCols[j] == 1 || aggValue.asInt < value.asInt)
                              aggValue.asInt = value.asInt;
                           break;

                        case SQLElement.LONG:
                           if (groupCountCols[j] == 1 || aggValue.asLong < value.asLong)
                              aggValue.asLong = value.asLong;
                           break;

                        case SQLElement.FLOAT:
                        case SQLElement.DOUBLE:
                           if (groupCountCols[j] == 1 || aggValue.asDouble < value.asDouble)
                              aggValue.asDouble = value.asDouble;
                           break;
                           
                        // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.   
                        case SQLElement.CHARS_NOCASE:  
                           String aggValueString = aggValue.asString.toLowerCase(),
                                  valueString = value.asString.toLowerCase();
                           if (groupCountCols[j] == 1 || aggValueString.compareTo(valueString) < 0)
                           {
                              aggValue.asString = value.asString;
                              aggValue.asInt = value.asInt;
                              aggValue.asLong = value.asLong;
                           }
                           break;
                           
                        // The date is smaller, or the time is smaller.   
                        case SQLElement.DATETIME: // rnovais@567_2
                           if (groupCountCols[j] == 1)
                           {
                              aggValue.asInt = value.asInt;
                              aggValue.asShort = value.asShort;
                           }
                           else
                           if (aggValue.asInt <= value.asInt && (aggValue.asInt < value.asInt || aggValue.asShort < value.asShort)) 
                           {
                              aggValue.asInt = value.asInt;
                              aggValue.asShort = value.asShort;
                           }
                     }
                     break;
                  }

                  // juliana@226_5: the aggregation functions MAX() and MIN() now work for CHAR, VARCHAR, CHAR NOCASE, and VARCHAR NOCASE column 
                  // types. 
                  case SQLElement.FUNCTION_AGG_MIN:
                  {
                     switch (colType) // Checks the type of the column
                     {
                        case SQLElement.CHARS: // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
                           if (groupCountCols[j] == 1 || aggValue.asString.compareTo(value.asString) > 0)
                           {
                              aggValue.asString = value.asString;
                              aggValue.asInt = value.asInt;
                              aggValue.asLong = value.asLong;
                           }
                           break;
                           
                        case SQLElement.SHORT:
                           if (groupCountCols[j] == 1 || aggValue.asShort > value.asShort)
                              aggValue.asShort = value.asShort;
                           break;

                        case SQLElement.DATE: // rnovais@_567_2
                        case SQLElement.INT:
                           if (groupCountCols[j] == 1 || aggValue.asInt > value.asInt)
                              aggValue.asInt = value.asInt;
                           break;

                        case SQLElement.LONG:
                           if (groupCountCols[j] == 1 || aggValue.asLong > value.asLong)
                              aggValue.asLong = value.asLong;
                           break;

                        case SQLElement.FLOAT:
                        case SQLElement.DOUBLE:
                           if (groupCountCols[j] == 1 || aggValue.asDouble > value.asDouble)
                              aggValue.asDouble = value.asDouble;
                           break;
                           
                        // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
                        case SQLElement.CHARS_NOCASE:
                           String aggValueString = aggValue.asString.toLowerCase(),
                           valueString = value.asString.toLowerCase();
                           if (groupCountCols[j] == 1 || aggValueString.compareTo(valueString) > 0)
                           {
                              aggValue.asString = value.asString;
                              aggValue.asInt = value.asInt;
                              aggValue.asLong = value.asLong;
                           }
                           break;
                           
                        case SQLElement.DATETIME: // rnovais@567_2
                           if (groupCountCols[j] == 1)
                           {
                              aggValue.asInt = value.asInt;
                              aggValue.asShort = value.asShort;
                           }
                           else
                           if (aggValue.asInt >= value.asInt && (aggValue.asInt > value.asInt || aggValue.asShort > value.asShort)) // the date is greater, or the time is greater
                           {
                              aggValue.asInt = value.asInt;
                              aggValue.asShort = value.asShort;
                           }
                     }
                  }
               }
            }
         }
         if (!writeDelayed)
         {
            tempTable2.columnNulls[0] = nullsCurRecord;
            tempTable2.writeRSRecord(curRecord);
         }

         Vm.arrayCopy(nullsCurRecord, 0, nullsPrevRecord, 0, nullsCurRecord.length);
         curRecord = ((prevRecord = curRecord) == record1)? record2 : record1;
      }
      
      if (writeDelayed && groupCount > 0) // If there was delayed writing, flushs the last record.
      {
         if (aggFunctionExist)
            // Concludes any aggregated function calculation.
            endAggFunctionsCalc(prevRecord, groupCount, aggFunctionsRunTotals, aggFunctionsCodes, aggFunctionsParamCols, paramCols, 
                                                                               aggFunctionsColsCount, origColumnTypesItems, groupCountCols);

         tempTable2.columnNulls[0] = nullsCurRecord; // Takes the null values for the non-aggregate fields into consideration.
         j = aggFunctionsColsCount; 
         while (--j >= 0) // Writes the last record.
         {
            Utils.setBit(tempTable2.columnNulls[0], aggFunctionsParamCols[j], groupCountCols[j] == 0);
            groupCountCols[j] = 0;
         }
         tempTable2.writeRSRecord(prevRecord);
      }

      if (having == null) // If there is no having clause, returns the second temorary table.
         return tempTable2;

      // Creates the last temporary table to hold the records that comply to the "HAVING" clause.
      // Creates the second temporary table.
      Table tempTable3 = driver.driverCreateTable(null, null, tempTable2.columnHashes, tempTable2.columnTypes, tempTable2.columnSizes, null, null,
                                                                                        Utils.NO_PRIMARY_KEY, Utils.NO_PRIMARY_KEY, null);

      // The HAVING clause only works with aliases. So, remaps the table column names to use the aliases of the select statement, instead of the 
      // original column names.
      tempTable3.remapColumnsNames2Aliases(select.fieldList);

      // Binds the HAVING clause to the table columns.
      having.bindColumnsSQLBooleanClause(tempTable3.htName2index, tempTable3.columnTypes, (SQLResultSetTable[])null);
      
      rsTemp = tempTable2.createResultSetForSelect(having); // Creates a result set from the table, using the HAVING clause
      rsTemp.driver = driver;
      
      // Writes the result set to the third temporary table.
      tempTable3.writeResultSetToTable(new ResultSet[] {rsTemp}, null, select, null, -1);
      return tempTable3;
   }
   
   // Included the whereClause parameter.
   // Renamed from createResultSet to createResultSetForSelect
   /**
    * Creates a list of result sets for each table of the select clause.
    *
    * @param tableList The tableList of the select clause.
    * @param whereClause The condition of the query.
    * @return A list of result sets.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   private ResultSet[] createListResultSetForSelect(SQLResultSetTable[] tableList, SQLBooleanClause whereClause) throws IOException, InvalidDateException
   {
      int size = tableList.length, // The table list size.
          i = size;
      ResultSet[] rsList = new ResultSet[size]; // The result set list.
      boolean hasComposedIndex = false; // Indicates if there is a composed index.

      while (--i >= 0)
      {
         Table t = tableList[i].table;
         
         // Apply table indexes, if any.
         ResultSet rs = rsList[i] = t.createResultSet(whereClause);
         rs.columnCount = t.columnCount; // Sets the column count.
         rs.indexRs = i; // Sets the table index.

         if (!hasComposedIndex) // It is only necessary to have one table with composed indices.
            hasComposedIndex = t.numberComposedIndices > 0;
      }

      if (whereClause != null) // Tries to apply the table indices to generate a bitmap of the rows to be returned.
      {
         if (size > 1) // Join.
            whereClause.expressionTree.setIndexRsOnTree();
         generateIndexedRowsMap(rsList, hasComposedIndex);
      }

      return rsList;
   }
   
   /**
    * Generates an index bit map for a list of result sets.
    *
    * @param rsList The list of result sets.
    * @param hasComposedIndex Indicates if the table has a composed index.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   static void generateIndexedRowsMap(ResultSet[] rsList, boolean hasComposedIndex) throws IOException, InvalidDateException
   {
      SQLBooleanClause whereClause = rsList[0].whereClause;

      if (rsList.length > 1) // Applies the table indexes to the where clause. If it was not possible, return.
      {
         if (!whereClause.sqlbooleanclauseApplyTableIndexesJoin())
            return;
      }
      else if (!whereClause.sqlbooleanclauseApplyTableIndices(rsList[0].table.columnIndices, hasComposedIndex))
            return;
         
      computeIndex(rsList, rsList.length > 1, -1, null, -1, -1);

      if (whereClause.expressionTree == null) // There is no where clause left, since all rows can be returned using the indexes.
      {
         int i = rsList.length;
         while (--i >= 0)
            rsList[i].whereClause = null;
      }
            
   }
   
   /**
    * Finds the rows that satisfy the query clause using the indices.
    *
    * @param rsList The result set list, one for each table.
    * @param isJoin Indicates that the query has a join.
    * @param indexRsOnTheFly The index of the result set or -1 if the query is being indexed on the fly.
    * @param value The value to be indexed on the fly.
    * @param operator The operand type. Used only to index on the fly.
    * @param colIndex The index column. Used only to index on the fly.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   static void computeIndex(ResultSet[] rsList, boolean isJoin, int indexRsOnTheFly, SQLValue value, int operator, int colIndex) 
                                                                                                        throws IOException, InvalidDateException
   {
      MarkBits markBits = new MarkBits();
      int[] items;
      boolean onTheFly = indexRsOnTheFly != -1;
      int count = 1, 
          booleanOp, 
          i = -1, 
          j,
          size = rsList.length;
      SQLBooleanClause whereClause = rsList[0].whereClause;
      byte[] indexedCols = whereClause.appliedIndexesCols;
      byte[] relationalOps = whereClause.appliedIndexesRelOps;
      ResultSet[] rsListPointer; // The resulting indexed row bitmap.
      Table[] appliedIndexesTables = whereClause.appliedIndexesTables;
      SQLBooleanClauseTree[] indexedValues = whereClause.appliedIndexesValueTree;
      ComposedIndex[] appliedComposedIndexes = whereClause.appliedComposedIndexes;

      // Gets the list of indexes that were applied to the where clause, together
      // with the indexes values to search for and the boolean operation to apply.
      if (onTheFly)
         booleanOp = rsList[indexRsOnTheFly].rowsBitmapBoolOp;
      else
      {
         count = whereClause.appliedIndexesCount;
         booleanOp = whereClause.appliedIndexesBooleanOp;
      }

      rsListPointer = new ResultSet[count];
      if (isJoin)  // Puts the result set bag in order with the tables.
      {
         i = count;
         while (--i >= 0)
         {
            j = size;
            while (--j >= 0)
               if (rsList[j].table.equals(appliedIndexesTables[i]))
               {
                  rsListPointer[i] = rsList[j];
                  break;
               }
         }
      }
      
      // Loops through all applied indexes and records the indexed rows in the bitmap.
      ResultSet rsBag = rsList[onTheFly? indexRsOnTheFly : 0];

      Table table = rsBag.table;
      int col,
          op, 
          realOp,
          idx;
      boolean isComposed;
      int[] ops = null;
      SQLValue[] leftVal = new SQLValue[1];
      Index index;

      rsBag.indexCount = 0;
      while (++i < count)
      {
         if (isJoin)
         {
            table = appliedIndexesTables[i];
            rsBag = rsListPointer[i];
         }
         rsBag.indexCount++;
         isComposed = appliedComposedIndexes[i] != null;
         col = op = -1;
         size = 1;
         if (onTheFly)
         {
            col = colIndex;
            op = operator;
            leftVal[0] = value;
         }
         else
         if (isComposed)
         {
            if (leftVal.length < (size = appliedComposedIndexes[i].columns.length)) // Reuses the buffer.
               leftVal = new SQLValue[size];
            if (ops == null || ops.length < size) // Reuses the buffer.
               ops = new int[size];
            for (int m = 0, k = i; m < size; m++, k++)
            {
               leftVal[m] = indexedValues[k].getOperandValue();
               ops[m] = relationalOps[k];
            }
         }
         else
         {
            leftVal[0] = indexedValues[i].getOperandValue();
            col = indexedCols[i];
            op = relationalOps[i];
         }

         index = isComposed? appliedComposedIndexes[i].index : table.columnIndices[col];

         markBits.reset(index, table.db.rowCount); // Prepares the index row bitmap.
         items = markBits.indexBitmap.items;  
         
         j = size;
         while (--j >= 0)
         {
            if (isComposed)
               op = ops[j];
            idx = i + j;

            // If operation is 'like x%', then replaces the operand by the value without the % mask.
            // juliana@230_10: solved a bug that could crash the application when more than one index is applied.
            if (op == SQLElement.OP_PAT_MATCH_NOT_LIKE || op == SQLElement.OP_PAT_MATCH_LIKE)
               leftVal[j].asString = (index.types[j] == SQLElement.CHARS_NOCASE)? indexedValues[i].strToMatch.toLowerCase()
                                                                                   : indexedValues[i].strToMatch; 
            
            else // Checks if this is a "between" operation.
            if (booleanOp == SQLElement.OP_BOOLEAN_AND && i < count - 1 && indexedCols[i + 1] == col 
             && (op == SQLElement.OP_REL_GREATER || op == SQLElement.OP_REL_GREATER_EQUAL)
             && (relationalOps[idx + 1] == SQLElement.OP_REL_LESS || relationalOps[idx + 1] == SQLElement.OP_REL_LESS_EQUAL))
            {
               markBits.rightKey = new Key(index);
               
               // Encapsulates.
               markBits.rightKey.set(new SQLValue[] {indexedValues[idx + 1].getOperandValue()});
               markBits.rightOp[j] = relationalOps[idx + 1];
               
               i++; // Next operation already processed.
            }
            else
            {
               realOp = op;
               
               switch (op) // When searching for !=, <, <=, opposite operation will be used instead.
               {
                  case SQLElement.OP_PAT_MATCH_NOT_LIKE:
                     realOp = SQLElement.OP_PAT_MATCH_LIKE;
                     break;
                  case SQLElement.OP_REL_LESS:
                     realOp = SQLElement.OP_REL_GREATER_EQUAL;
                     break;
                  case SQLElement.OP_REL_LESS_EQUAL:
                     realOp = SQLElement.OP_REL_GREATER;
                     break;
                  case SQLElement.OP_REL_DIFF:
                     realOp = SQLElement.OP_REL_EQUAL;
               }

               if (op != realOp) // All rows will be marked and only the rows that satisfy the opposite operation will be reseted.
               {
                  Convert.fill(items, 0, items.length, 0xFFFFFFFF);
                  markBits.bitValue = false;
                  op = realOp;
               }
            }
            markBits.leftOp[j] = (byte)op;
         }
         markBits.leftKey.set(leftVal);
         
         switch (op) // Finally, marks all rows that match this value / range of values.
         {
            case SQLElement.OP_REL_EQUAL:
               index.getValue(markBits.leftKey, markBits);
               break;
            case SQLElement.OP_PAT_MATCH_LIKE:
            case SQLElement.OP_REL_GREATER:
            case SQLElement.OP_REL_GREATER_EQUAL:
               index.getGreaterOrEqual(markBits);
         }

         // If it is the first index, assigns the index bitmap to the resulting bitmap. Otherwise, merges the index bitmap with the existing bitmap, 
         // using the boolean operator.
         if (rsBag.indexCount == 1)
         {
            if (onTheFly)
               rsBag.auxRowsBitmap = markBits.indexBitmap;
            else
               rsBag.rowsBitmap = markBits.indexBitmap;
         }
         else 
            mergeBitmaps(rsBag.rowsBitmap.items, markBits.indexBitmap.items, booleanOp);
         rsBag.rowsBitmapBoolOp = booleanOp;
         if (isComposed)
            i += size - 1;
      }
   }
   
   /**
    * Merges two bitmaps into the first bitmap using the given boolean operator.
    *
    * @param bitmap1 The first bitmap.
    * @param bitmap2 The second bitmap.
    * @param booleanOp The boolean operator to be applied.
    */
   static void mergeBitmaps(int[] bitmap1, int[] bitmap2, int booleanOp)
   {
      int i = bitmap1.length;

      if (booleanOp == SQLElement.OP_BOOLEAN_AND)
         while (--i >= 0)
            bitmap1[i] &= bitmap2[i];
      else
         while (--i >= 0)
            bitmap1[i] |= bitmap2[i];
   }

   /**
    * Concludes the calculation of the given aggregated function running totals based on the given record and the group count.
    * 
    * @param record The record that is the parameter for the aggregated function.
    * @param groupCount The result of a COUNT(*).
    * @param aggFunctionsRunTotals The results of the aggregated functions. 
    * @param aggFunctionsCodes The aggregated function codes.
    * @param aggFunctionsParamCols The columns that are parameters to the aggregated functions.
    * @param aggFunctionsRealParamCols The real columns that are parameters to the aggregated functions.
    * @param aggFunctionsColsCount The number of columns that are parameters to the aggregated functions.
    * @param columnTypes The types of the columns.
    * @param groupCountCols The count for the groups.
    */
   private void endAggFunctionsCalc(SQLValue[] record, int groupCount, SQLValue[] aggFunctionsRunTotals, int[] aggFunctionsCodes, 
                  int[] aggFunctionsParamCols, int[] aggFunctionsRealParamCols, int aggFunctionsColsCount, byte[] columnTypes, int[] groupCountCols)
   {
      int j = aggFunctionsColsCount;
      SQLValue aggValue,
               value;
      
      // Concludes the calculation of the aggregate functions running totals.
      while  (--j >= 0)
      {
         aggValue = aggFunctionsRunTotals[j];
         value = record[aggFunctionsParamCols[j]];

         switch (aggFunctionsCodes[j])
         {
            case SQLElement.FUNCTION_AGG_AVG:
            {
               if (groupCountCols[j] != 0)
                  value.asDouble = aggValue.asDouble / groupCountCols[j];
               aggValue.asDouble = 0;
               break;
            }

            // juliana@226_5: the aggregation functions MAX() and MIN() now work for CHAR, VARCHAR, CHAR NOCASE, and VARCHAR NOCASE column types. 
            case SQLElement.FUNCTION_AGG_MAX:
            case SQLElement.FUNCTION_AGG_MIN:
            {  
               switch (columnTypes[aggFunctionsRealParamCols[j]]) // Checks the type of the column.
               {
                  case SQLElement.SHORT:
                     value.asShort = aggValue.asShort;
                     break;

                  // juliana@230_31: corrected a bug of MAX() and MIN() not working properly with DATE and DATETIME.
                  case SQLElement.DATETIME:
                     value.asShort = aggValue.asShort;
                  case SQLElement.INT:
                  case SQLElement.DATE:
                     value.asInt = aggValue.asInt;
                     break;

                  case SQLElement.LONG:
                     value.asLong = aggValue.asLong;
                     break;

                  case SQLElement.FLOAT:
                  case SQLElement.DOUBLE:
                     value.asDouble = aggValue.asDouble;
                     break;
                    
                  // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets. 
                  case SQLElement.CHARS:
                  case SQLElement.CHARS_NOCASE:
                     value.asString = aggValue.asString;
                     value.asInt = aggValue.asInt;
                     value.asLong = aggValue.asLong;
               }
               break;
            }

            case SQLElement.FUNCTION_AGG_COUNT:
            {
               value.asInt = groupCount;
               break;
            }

            case SQLElement.FUNCTION_AGG_SUM:
            {
               value.asDouble = aggValue.asDouble;
               aggValue.asDouble = 0;
            }
         }
      }
   }
   
   /**
    * Creates a temporary table that stores only an integer value.
    * 
    * @param driver The connection with Litebase.
    * @param intValue The value to be put in the table.
    * @param colName The column name of the single table column.
    * @return The table.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   Table createIntValueTable(LitebaseConnection driver, int intValue, String colName) throws IOException, InvalidDateException
   {
      SQLValue[] record = SQLValue.newSQLValues(1);
      record[0].asInt = intValue;

      Table table = driver.driverCreateTable(null, null, new int[] {colName.hashCode()}, new byte[] {SQLElement.INT}, new int[1], null, null, 
                                                                                           Utils.NO_PRIMARY_KEY, Utils.NO_PRIMARY_KEY, null);
      table.writeRSRecord(record);
      return table;
   }
   
   // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
   /**
    * Calculates the answer of a select without aggregation, join, order by, or group by without using a temporary table.
    * 
    * @param resultSet The result set of the table.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    */
   private void computeAnswer(ResultSet resultSet) throws IOException, InvalidDateException, InvalidNumberException
   {
      int i;
      Table table = resultSet.table;
      
      if (resultSet.whereClause == null && resultSet.rowsBitmap == null && table.deletedRowsCount == 0)
      {
         i = table.answerCount = table.db.rowCount;
         while (--i >= 0)
            Utils.setBit(table.allRowsBitmap, i, true);
      }
      else
      {
         i = 0;
         while (resultSet.getNextRecord()) // No preverify needed.
         {
            Utils.setBit(table.allRowsBitmap, resultSet.pos, true);   
            i++;
         }
         table.answerCount = i;
      }
   }
   
   /** 
    * Returns a duplicated byte array.
    * 
    * @param array The array to be duplicated.
    * @param size The size of the array to be duplicated.
    * @return An array of byte with all the elements.
    */
   private byte[] duplicateByteArray(byte[] array, int size) // guich@554_34
   {
      byte[] newArray = new byte[size];
      Vm.arrayCopy(array, 0, newArray, 0, size);
      return newArray;
   }
   
   /** 
    * Returns a duplicated short array.
    * 
    * @param array The array to be duplicated.
    * @param size The size of the array to be duplicated.
    * @return An array of short with all the elements.
    */
   private short[] duplicateShortArray(short[] array, int size) // guich@554_34
   {
      short[] newArray = new short[size];
      Vm.arrayCopy(array, 0, newArray, 0, size);
      return newArray;
   }
   
   /** 
    * Returns a duplicated int array.
    * 
    * @param array The array to be duplicated.
    * @param size The size of the array to be duplicated.
    * @return An array of int with all the elements.
    */
   private int[] duplicateIntArray(int[] array, int size) // guich@554_34
   {
      int[] newArray = new int[size];
      Vm.arrayCopy(array, 0, newArray, 0, size);
      return newArray;
   }
}
