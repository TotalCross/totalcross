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

import totalcross.sys.Convert;
import totalcross.util.IntHashtable;

/**
 * Internal use only. Represents the "select" clause portion of a SQL query.
 */
class SQLSelectClause
{
   // Possibles types for the select clause
   /**
    * Indicates if the query is of the form <code>select count(*) from table where...</code>.
    */
   static final int COUNT_WITH_WHERE = 1;

   // juliana@250_8: now the maximum number of columns, fields, tables, etc is 254 instead of 128 except on palm.
   /**
    * Maximum number of fields supported
    */
   static final int MAX_NUM_FIELDS = 254;

   /**
    * The resulting <code>ResultSet</code> field list.
    */
   SQLResultSetField[] fieldList = new SQLResultSetField[MAX_NUM_FIELDS];

   /**
    * Number of fields found.
    */
   int fieldsCount;
   
   /**
    * Indicates if the select clause has aggregated functions.
    */
   boolean hasAggFunctions;

   /**
    * Indicates if the select clause has real columns.
    */
   boolean hasRealColumns;

   /**
    * The resulting <code>ResultSet</code> table list.
    */
   SQLResultSetTable[] tableList = new SQLResultSetTable[MAX_NUM_FIELDS];

   /**
    * The index of the fields.
    */
   IntHashtable htName2index;

   // This will be better used when implement a PLANNER for litebase is implemented. 
   // Up to now this only indicates if the select clause has a count(*).
   /**
    * Indicates the type of the select clause.
    */
   int type = -1;
   
   /** 
    * Binds the column information of the underlying tables to the select clause. 
    *
    * @param driver The Litebase connection.
    * @throws SQLParseException In case of an unknown or ambiguous column name, the parameter and the function data types are incompatible, or the 
    * total number of fields of the select exceeds the maximum.
    */
   void bindColumnsSQLSelectClause(LitebaseConnection driver) throws SQLParseException
   {
      int i,
          j,
          n;
      Table table;
      SQLResultSetTable rsTable;
      SQLResultSetField field;
      StringBuffer sbufnf = driver.sBuffer;

      // If the select clause has a wild card (is null), then expands the list using the column information from the given tables.
      if (fieldList == null)
      {
         String tableName;
         String[] columnNames;
         int[] columnHashes;
         byte[] columnTypes;
         int[] columnSizes;
         int pos = 0,
             count = 0,
             m;
         
         j = n = tableList.length;
         while (--j >= 0)
            count += tableList[j].table.columnCount - 1; // Excludes the rowid.
         
         // juliana@250_7: now a select * will cause a SQLParseException if the total number of columns is more than 254.
         if (count > SQLElement.MAX_NUM_COLUMNS)
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELDS_OVERFLOW));
         
         fieldsCount = count;
         fieldList = new SQLResultSetField[fieldsCount];
 
         j = -1;
         count = 0;
         while (++j < n)
         {
            tableName = (rsTable = tableList[j]).tableName;
            columnNames = (table = rsTable.table).columnNames;
            columnHashes = table.columnHashes;
            columnTypes = table.columnTypes;
            columnSizes = table.columnSizes;
            m = table.columnCount;
            i = 0;
            while (++i < m) // guich@503_10: excludes the rowid.
            {
               (field = new SQLResultSetField()).alias = columnNames[i]; // guich@_512_2: added column name so the ResultSetMetaData can work.
               field.aliasHashCode = field.tableColHashCode = columnHashes[i];
               field.dataType = columnTypes[i];
               field.size = columnSizes[i];
               field.tableColIndex = i;
               sbufnf.setLength(0);
               htName2index.put(Convert.hashCode(sbufnf.append(tableName).append('.').append(field.alias)), pos);

               if (!htName2index.exists(field.aliasHashCode))
                  htName2index.put(field.aliasHashCode, pos);

               field.table = table;
               field.tableName = rsTable.tableName;
               fieldList[count++] = field;
               pos++;
            }
         }
      }
      else
      {
         int index,
             hash,
             auxIndex,
             aggFunctionType,
             dtFunctionType, // initialized with a UNDEFINED number
             sqlFunction;
         boolean foundFirst;
         String tableName;
         Table auxTable;
         SQLResultSetTable rsTableAux;
         SQLResultSetField param;
         
         n = fieldsCount;
         i = -1;
         
         while (++i < n)  // Binds the listed colums to the table.
         {
            field = fieldList[i];
            rsTable = null;
            index = -1;
            table = null;
           
            // Aggregation functions (count() doesn't have a column name yet).
            if (!field.isAggregatedFunction || field.sqlFunction != SQLElement.FUNCTION_AGG_COUNT) 
            {
               if ((tableName = field.tableName) != null) // Checks the names.
               {
                  j = tableList.length;
                  while (--j >= 0) // Verifies if it is a valid table name.
                     if (tableList[j].aliasTableName.equals(tableName))
                     {
                        table = (rsTable = tableList[j]).table;
                        break;
                     }
                  if (table == null)
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_UNKNOWN_COLUMN) + field.alias);
                  if ((index = table.htName2index.get(hash = field.tableColName.hashCode(), -1)) == -1)
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_UNKNOWN_COLUMN) + field.alias);
                  htName2index.put(field.aliasHashCode, i);
                  sbufnf.setLength(0);
                  
                  // Used an explicit alias.
                  if (field.aliasHashCode != Convert.hashCode(sbufnf.append(tableName).append('.').append(field.tableColName))) 
                  {
                     sbufnf.setLength(0);
                     htName2index.put(Convert.hashCode(sbufnf.append(tableName).append('.').append(field.alias)), i);
                  }
                  else
                  if (!htName2index.exists(hash))
                     htName2index.put(hash, i); // Stores the name of the field once; only the first.
               }
               else // Verifies if the column name in the field list is ambiguous.
               {
                  table = null;
                  rsTable = null;
                  foundFirst = false;
                  j = tableList.length;
                  while (--j >= 0)
                  {
                     auxIndex = (auxTable = (rsTableAux = tableList[j]).table).htName2index.get(field.tableColHashCode, -1);
                     if (auxIndex != -1)
                     {
                        if (foundFirst)
                           throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_AMBIGUOUS_COLUMN_NAME) + field.alias);
                        else
                        {
                           foundFirst = true;
                           index = auxIndex;
                           sbufnf.setLength(0);
                           htName2index.put(Convert.hashCode(sbufnf.append(tableList[j].tableName).append('.').append(field.alias)), i);
                           htName2index.put(field.aliasHashCode, i);
                           
                           // juliana@252_4: corrected the fact that a field used in a function can't be fetched using only the name of the field 
                           // unless it is also in the select field list.
                           if (field.sqlFunction == SQLElement.FUNCTION_DT_NONE)
                              htName2index.put(field.tableColHashCode, i);
                           
                           table = auxTable;
                           rsTable = rsTableAux;
                        }
                     }
                  }
                  if (!foundFirst)
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_UNKNOWN_COLUMN) + field.alias);
               }
            }
            
            if (!field.isVirtual) // If the field is not virtual, it needs to be mapped directly to the underlying table.
            {
               field.dataType = table.columnTypes[field.tableColIndex = index];
               field.size = table.columnSizes[index];
               field.table = table;
               field.tableName = rsTable.tableName;
               htName2index.put(field.aliasHashCode, i);
            }
            else
            if (field.isAggregatedFunction || field.isDataTypeFunction) // If it is an aggregated or data type function, maps its parameter.
            {
               param = field.parameter;
               aggFunctionType = dtFunctionType = SQLElement.UNDEFINED; // initialized with an UNDEFINED number.
               sqlFunction = field.sqlFunction;
               field.table = table; // rnovais@200_4
               htName2index.put(field.aliasHashCode, i);
               
               if (field.isAggregatedFunction) // Stores the correct function code.
                  aggFunctionType = SQLElement.aggregateFunctionsTypes[sqlFunction];
               else
                  dtFunctionType = SQLElement.dataTypeFunctionsTypes[sqlFunction];

               if (param != null)
               {
                  param.dataType = table.columnTypes[param.tableColIndex = index];
                  param.size = table.columnSizes[index]; 

                  if (field.isAggregatedFunction) // rnovais@568_10
                  {
                     // Checks if the parameter and aggregated function data types are compatible.
                     // juliana@226_5
                     if ((aggFunctionType == SQLElement.INT || aggFunctionType == SQLElement.DOUBLE) 
                      && (param.dataType == SQLElement.CHARS || param.dataType == SQLElement.CHARS_NOCASE))
                        throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES) + ' ' 
                                                                             + SQLElement.aggregateFunctionsNames[sqlFunction]);

                     // For aggregated functions, if the function does not have a defined data type, it inherits the parameter size and type.
                     if (aggFunctionType == SQLElement.UNDEFINED)
                     {
                        field.dataType = param.dataType;
                        field.size = param.size;
                     }
                  }
                  else
                  {
                     field.size = param.size; // rnovais@570_1
                     
                     // rnovais@570_5: if UNDEFINED the datatype will be the same of the field thus, it is possible to have functions that can be 
                     // applyed to diferents fieds type. e.g. ABS(int) returns int, ABS(double) returns double, etc.
                     if (dtFunctionType == SQLElement.UNDEFINED)
                        field.dataType = param.dataType;
                     
                     // Checks if the parameter and the data type function data types are compatible.
                     Utils.bindFunctionDataType(param.dataType, sqlFunction);
                  }
               }
            }
         }
      }
   }
}
