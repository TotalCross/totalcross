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
 * Represents a SQL Statement that can be prepared (compiled) once and executed many times with different parameter
 * values.
 */
public class PreparedStatement
{
   /**
    * The SQL command expression.
    */
   private String sqlExpression;

   /**
    * The connection with Litebase.
    */
   private LitebaseConnection driver;

   /**
    * The type of the statement. It can be one of <code>SQLElement.STMT_INSERT</code>,
    * <code> SQLElement.STMT_UPDATE</code>, <code>SQLElement.STMT_DELETE</code>, <code>SQLElement.STMT_DROP</code>,
    * <code>SQLElement.STMT_ALTER</code>, and <code>SQLElement.STMT_CREATE</code>.
    */
   private int type;

   /**
    * The parameters for the prepared statement in string format.
    */
   private String[] paramsAsStrs; // guich@566_15

   /**
    * The positions of the '?' in the sql string.
    */
   private short[] paramsPos;

   /**
    * Indicates if there are parameters in the SQL command.
    */
   private boolean storeParams; // guich@566_15

   /**
    * The statement.
    */
   private SQLStatement statement;

   // juliana@230_11: Litebase public class constructors are now not public any more. 
   /**
    * The constructor.
    */
   PreparedStatement() {}
   
   /**
    * Prepares a SQL statement.
    *
    * @param newDriver The connection with Litebase.
    * @param sql the SQL expression.
    * 
    * @throws IOException If it an internal method throws it.
    * @throws InvalidDateException If it an internal method throws it.
    * @throws InvalidNumberException If it an internal method throws it.
    * @throws SQLParseException If the sql string is empty.
    */
   void prepare(LitebaseConnection newDriver, String sql) throws IOException, InvalidDateException, InvalidNumberException, SQLParseException
   {
      String tempSQL = sqlExpression = sql; // guich@503_9: Assign this, or it will become unusable. This is necessary for logging.
      driver = newDriver;
      type = SQLElement.CMD_NONE;
      
      LitebaseParser parser = null; // Parses and binds the statement.
      
      // juliana@202_5: removed possible NPE if there is a blank in the beginning of the sql command.
      if ((tempSQL = sql.toLowerCase().trim()).startsWith("select"))
      {
         // Parses the SQL statement.
         parser = new LitebaseParser();
         parser.tableList = new SQLResultSetTable[SQLElement.MAX_NUM_COLUMNS];
         parser.select = new SQLSelectClause();
         
         // juliana@253_9: improved Litebase parser.
         
         // juliana@224_2: improved memory usage on BlackBerry.
         LitebaseParser.parser(sql, parser, driver.lexer); // Does de parsing.
         
         type = parser.command;
      }
      else
      {
         if (tempSQL.startsWith("insert") || tempSQL.startsWith("update") || tempSQL.startsWith("delete"))
         {
            // Parses the SQL statement.
            parser = new LitebaseParser();
            parser.tableList = new SQLResultSetTable[1];
            if (tempSQL.startsWith("insert") || tempSQL.startsWith("update"))
            {   
               parser.fieldValues = new String[SQLElement.MAX_NUM_COLUMNS];
               parser.fieldNames = new String[SQLElement.MAX_NUM_COLUMNS];
            }
            
            // juliana@224_2: improved memory usage on BlackBerry.
            LitebaseParser.parser(sql, parser, driver.lexer); // Does de parsing.
            
            type = parser.command;
         }
            
         // juliana@212_1: corrected prepared statement parsing for create index.
         if (tempSQL.startsWith("create"))
            type = SQLElement.CMD_CREATE_TABLE;
         else
         if (tempSQL.length() == 0)
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_SYNTAX_ERROR));
      }      
      
      // juliana@226_15: corrected a bug that would make a prepared statement with where clause and indices not work correctly after the first 
      // execution.
      switch (type) // Gets the command in the SQL expression and calls the apropriate create statement.
      {
         case SQLElement.CMD_INSERT: // INSERT
            statement = new SQLInsertStatement(parser, driver).litebaseBindInsertStatement();
            break;
         case SQLElement.CMD_UPDATE: // UPDATE
            statement = new SQLUpdateStatement(parser).litebaseBindUpdateStatement(driver);
            SQLBooleanClause whereClause = ((SQLUpdateStatement)statement).whereClause;
            if (whereClause != null)
               whereClause.expressionTreeBak = whereClause.expressionTree.cloneTree(null);
            break;
         case SQLElement.CMD_DELETE: // DELETE
            statement = new SQLDeleteStatement(parser).litebaseBindDeleteStatement(driver);
            if ((whereClause = ((SQLDeleteStatement)statement).whereClause) != null)
               whereClause.expressionTreeBak = whereClause.expressionTree.cloneTree(null);
            break;
         case SQLElement.CMD_SELECT: // SELECT
            statement = new SQLSelectStatement(parser).litebaseBindSelectStatement(driver);
            SQLSelectStatement selectStmt = (SQLSelectStatement)statement;
            SQLColumnListClause orderByClause = selectStmt.orderByClause,
                                groupByClause = selectStmt.groupByClause;
            SQLResultSetField[] fieldList;
            short[] vi; // juliana@226_1
            int n;
            
            if (orderByClause != null)
            {
               n = orderByClause.fieldList.length;
               fieldList = orderByClause.fieldList;
            
               // Saves the order by clause if there's no backup yet.
               vi = orderByClause.fieldTableColIndexesBak = new short[n]; // juliana@226_1
               while (--n >= 0)
                  vi[n] = (short)fieldList[n].tableColIndex; // juliana@226_1
            }
            
            // juliana@226_14: corrected a bug that would make a prepared statement with group by not work correctly after the first execution.
            if (groupByClause != null)
            {
               n = groupByClause.fieldList.length;
               fieldList = groupByClause.fieldList;
            
               // Saves the order by clause if there's no backup yet.
               vi = groupByClause.fieldTableColIndexesBak = new short[n]; // juliana@226_1
               while (--n >= 0)
                  vi[n] = (short)fieldList[n].tableColIndex; // juliana@226_1
            }
            if ((whereClause = selectStmt.whereClause) != null)
               whereClause.expressionTreeBak = whereClause.expressionTree.cloneTree(null);
      }

      // If the statement is to be used as a prepared statement, it is possible to use log.
      if (statement != null && LitebaseConnection.logger != null)
      {
         int length = Convert.numberOf(sql, '?'); // Finds the number of '?'.
         
         paramsPos = new short[length + 1]; // The array of positions of the '?' in the sql. 

         if (length > 0) // If there is some '?' in the sql command string, there are parameters to be stored.
         {
            int i = sql.length();
            
            paramsAsStrs = new String[length]; // Creates the array of parameters.
            storeParams = true;
            
            // Marks the positions of the '?'.
            paramsPos[length--] = (short)i;
            while (--i >= 0)
               if (sql.charAt(i) == '?')
                  paramsPos[length--] = (short)i;
            
            // juliana@201_15: The prepared statement parameters for logging must be set as "unfilled" when creating it.
            Convert.fill(paramsAsStrs, 0, paramsAsStrs.length, "unfilled");  
         }
      }
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * This method executes a prepared SQL query and returns its <code>ResultSet</code>.
    *
    * @return The <code>ResultSet</code> of the SQL statement.
    * @throws DriverException If an error occurs. This can be the case if the statement to be execute is not a select or an <code>IOException</code> 
    * occurs.
    * @throws SQLParseException If an <code>InvalidDateFormat</code> or <code>InvalidNumberFormat</code> occurs.
    */
   public ResultSet executeQuery() throws DriverException, SQLParseException
   {
      testPSState();
      
      try
      {
         // juliana@225_9: removed a possible ClassCastException when passing an insert / update / delete to a PreparedStatement.executeQuery(). The 
         // correct exception is a DriverException.
         if (type != SQLElement.CMD_SELECT) // The statement must be a select.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_QUERY_DOESNOT_RETURN_RESULTSET));
         
         SQLSelectStatement selectStmt = (SQLSelectStatement)statement; // The select statement.
         
         selectStmt.allParamValuesDefined(); // All the parameters of the select statement must be defined.
          
         // juliana@253_18: now it is possible to log only changes during Litebase operation.
         if (LitebaseConnection.logger != null && !LitebaseConnection.logOnlyChanges) // If log is on, adds information to it.
            synchronized (LitebaseConnection.logger)
            {
               LitebaseConnection.logger.logInfo(toStringBuffer());
            }
   
         resetWhereClause(selectStmt.whereClause); // guich@550_43: fixed problem when reusing the statement.
   
         // guich@554_37: tableColIndex may change between runs of a prepared statement with a sort field so we have to cache the tableColIndex of the 
         // order by fields.
         resetColumnListClause(selectStmt.orderByClause);
         
         // juliana@226_14: corrected a bug that would make a prepared statement with group by not work correctly after the first execution.
         resetColumnListClause(selectStmt.groupByClause);

         return selectStmt.litebaseDoSelect(driver); // Executes the query.
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
      catch (InvalidDateException exception)
      {
         throw new SQLParseException(exception);
      }
      catch (InvalidNumberException exception)
      {
         throw new SQLParseException(exception);
      }
   }

   /**
    * Resets an order by or group by clause because the <code>tableColIndex</code> may change between runs of a prepared statement with a sort field. 
    * So, it is necessary to cache the <code>tableColIndex</code> of order by fields.
    *
    * @param columnListClause the order by clause to be reseted.
    */
   private void resetColumnListClause(SQLColumnListClause columnListClause)
   {
      if (columnListClause != null) // It may be null.
      {
         int n = columnListClause.fieldList.length;
         SQLResultSetField[] fieldList = columnListClause.fieldList;
         short[] vi = columnListClause.fieldTableColIndexesBak;
         
         while (--n >= 0)
            fieldList[n].tableColIndex = vi[n];
      }
   }

   // guich@554_13: this reset is needed by all statements.
   /**
    * Resets a where clause because the <code>expression</code> may change between runs of a prepared statement with a where clause.
    *
    * @param whereClause the were clause to be reseted.
    */
   private void resetWhereClause(SQLBooleanClause whereClause)
   {
      if (whereClause != null) // guich@552_37: It may be null.
      {
         whereClause.appliedIndexesBooleanOp = whereClause.appliedIndexesCount = 0;

         // After the first use of this, the tree is nulled. So, a copy of it is gotten.
         // guich@554_13: Use the expressionTreeBak as a condition instead of expressionTree (it should always be replaced after the first try).
         // juliana@226_15: corrected a bug that would make a prepared statement with where clause and indices not work correctly after the first 
         // execution.
         whereClause.expressionTree = whereClause.expressionTreeBak.cloneTree(whereClause.expressionTree);
         whereClause.resultSet = null; // Resets the result set.
      }
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * This method executes a SQL <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code> statement. SQL statements that return nothing such 
    * as SQL DDL statements can also be executed.
    *
    * @return The result is either the row count for <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code> statements; or 0 for SQL 
    * statements that return nothing.
    * @throws DriverException If an error occurs. This can happen if the query does not update the table or an <code>IOException</code> occurs.
    * @throws SQLParseException If an <code>InvalidDateException</code> or an <code>InvalidNumberExcepion</code> occurs.
    */
   public int executeUpdate() throws DriverException, SQLParseException
   {
      testPSState();
      
      if (type == SQLElement.CMD_SELECT) // The statement musn't be a select. executeQuery() must be used instead.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_QUERY_DOESNOT_PERFORM_UPDATE));

      // If there are undefined parameters (except for insert statements, where nulls are used instead, the statement must not be executed.
      if (statement != null) 
         statement.allParamValuesDefined();

      if (LitebaseConnection.logger != null) // If log is on, adds information to it.
         synchronized (LitebaseConnection.logger)
         {
            LitebaseConnection.logger.logInfo(toStringBuffer());
         }

      try
      {
         switch (type) // Returns the number of rows affected or if the command was successfully executed.
         {
            case SQLElement.CMD_INSERT:
               SQLInsertStatement insertStmt = (SQLInsertStatement)statement;
               rearrangeNullsInTable(insertStmt.table, insertStmt, true);
               insertStmt.table.convertStringsToValues(insertStmt.record);
               insertStmt.litebaseDoInsert(driver);
               return 1;
   
            case SQLElement.CMD_UPDATE:
               SQLUpdateStatement updateStmt = (SQLUpdateStatement)statement;
               rearrangeNullsInTable(updateStmt.rsTable.table, updateStmt, true);
               resetWhereClause(updateStmt.whereClause); // guich@554_13
               updateStmt.rsTable.table.convertStringsToValues(updateStmt.record);
               return updateStmt.litebaseDoUpdate(driver);
   
            case SQLElement.CMD_DELETE:
               SQLDeleteStatement deleteStmt = (SQLDeleteStatement)statement;
               resetWhereClause(deleteStmt.whereClause); // guich@_554_13
               return deleteStmt.litebaseDoDelete(driver);
   
            case SQLElement.CMD_CREATE_TABLE:
               driver.execute(sqlExpression);
               return 0;
               
            default:
               return driver.executeUpdate(sqlExpression);
         }
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
      catch (InvalidDateException exception)
      {
         throw new SQLParseException(exception);
      }
      catch (InvalidNumberException exception)
      {
         throw new SQLParseException(exception);
      }
   }

   /**
    * Stores the null values of prepared statement in the table.
    *
    * @param table The Table used in the prepared statement.
    * @param stmt The prepared statement.
    * @param isPreparedUpdateStmt Indicates if the prepared statement is an update prepared statement or not.
    */
   private void rearrangeNullsInTable(Table table, SQLStatement stmt, boolean isPreparedStmt)
   {
      byte[] storeNulls;
      boolean[] paramDefined;
      SQLValue[] record;
      short[] paramIndexes; // juliana@253_14: corrected a possible AIOBE if the number of parameters of a prepared statement were greater than 128.
      SQLValue value;
      
      if (stmt.type == SQLElement.CMD_INSERT) // Does the right cast.
      {
         SQLInsertStatement stat = (SQLInsertStatement)stmt;
         storeNulls = stat.storeNulls;
         record = stat.record;
         paramIndexes = stat.paramIndexes;
         paramDefined = stat.paramDefined;
      }
      else
      {
         SQLUpdateStatement stat = (SQLUpdateStatement)stmt;
         storeNulls = stat.storeNulls;
         record = stat.record;
         paramIndexes = stat.paramIndexes;
         paramDefined = stat.paramDefined;
      }

      int len = record.length < paramIndexes.length? record.length : paramIndexes.length; // Finds the smallest length.

      while (--len >= 0) // If a parameter is not defined, set it as null.
         if (!paramDefined[len] && (value = record[paramIndexes[len]]) != null) // juliana@201_17: NPE ocurred if record[paramIndexes[len]] == null.
            value.isNull = true;

      table.storeNulls = storeNulls;
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * This method sets the specified parameter from the given Java <code>short</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    */
   public void setShort(int index, short value)
   {
      testPSState();
      
      if (statement != null) // Only sets the parameter if the statement is not null.
      {
         statement.setParamValue(index, value);
         if (storeParams) // Only stores the parameter if there are parameters to be stored.
            paramsAsStrs[index] = Convert.toString(value);
      }
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * This method sets the specified parameter from the given Java <code>int</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    */
   public void setInt(int index, int value)
   {
      testPSState();
      
      if (statement != null) // Only sets the parameter if the statement is not null.
      { 
         statement.setParamValue(index, value);
         if (storeParams) // Only stores the parameter if there are parameters to be stored.
            paramsAsStrs[index] = Convert.toString(value);
      }
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * This method sets the specified parameter from the given Java <code>long</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    */
   public void setLong(int index, long value)
   {
      testPSState();
      
      if (statement != null) // Only sets the parameter if the statement is not null.
      {
         statement.setParamValue(index, value);
         if (storeParams) // Only stores the parameter if there are parameters to be stored.
            paramsAsStrs[index] = Convert.toString(value);
      }
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * This method sets the specified parameter from the given Java <code>float</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    */
   public void setFloat(int index, double value)
   {
      testPSState();
      
      if (statement != null)  // Only sets the parameter if the statement is not null.
      {
         statement.setParamValue(index, (float) value);
         if (storeParams) // Only stores the parameter if there are parameters to be stored.
            paramsAsStrs[index] = Convert.toString(value);
      }
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * This method sets the specified parameter from the given Java <code>double</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    */
   public void setDouble(int index, double value) 
   {
      testPSState();
      
      if (statement != null) // Only sets the parameter if the statement is not null.
      {
         statement.setParamValue(index, value);
         if (storeParams) // Only stores the parameter if there are parameters to be stored.
            paramsAsStrs[index] = Convert.toString(value);
      }
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * This method sets the specified parameter from the given Java <code>String</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter. DO NOT SURROUND IT WITH '!.
    * @throws SQLParseException If an <code>InvalidNumberException</code> or an <code>InvalidDateException</code> is thrown.
    */
   public void setString(int index, String value) throws SQLParseException
   {
      testPSState();
      
      if (statement != null) // Only sets the parameter if the statement is not null.
      {
         try
         {
            statement.setParamValue(index, value);
         }
         catch (InvalidNumberException exception)
         {
            throw new SQLParseException(exception);
         }
         catch (InvalidDateException exception)
         {
            throw new SQLParseException(exception);
         }
         
         if (storeParams) // Only stores the parameter if there are parameters to be stored.
         { 
            if (value == null)
               paramsAsStrs[index] = null;
            else
            {
               StringBuffer sbuf = driver.sBuffer;
               
               sbuf.setLength(0);
               paramsAsStrs[index] = sbuf.append('\'').append(value).append('\'').toString();
            }
         }
      }
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * This method sets the specified parameter from the given array of bytes as a blob.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    */
   public void setBlob(int index, byte[] value) 
   {
      testPSState();
      
      if (statement != null) // Only sets the parameter if the statement is not null.
      {
         statement.setParamValue(index, value); 
         if (storeParams) // A blob can't be stored as a string.
         {
            if (value != null)
               paramsAsStrs[index] = "[BLOB]";
            else
               paramsAsStrs[index] = null;
         }
      }
   }

   // rnovais@570_17: formats the Date d as a string "YYYY/MM/DD" and calls setString().
   /**
    * This method sets the specified parameter from the given Java <code>Date</code> value formated as "YYYY/MM/DD" <br>
    * <b>IMPORTANT</b>: The constructor <code>new Date(string_date)</code> must be used with care. Some devices can construct different dates, 
    * according to the device's date format. For example, the constructor <code>new Date("12/09/2006")</code>, depending on the device's date format,
    * can generate a date like "12 of September of 2006" or "09 of December of 2006". To avoid this, use the constructor
    * <code>new Date(string_date, totalcross.sys.Settings.DATE_XXX)</code> instead, where <code>totalcross.sys.Settings.DATE_XXX</code> is a date 
    * format parameter that must be one of the <code>totalcross.sys.Settings.DATE_XXX</code> constants.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param date The value of the parameter, which can be null.
    */
   public void setDate(int index, Date date) // juliana@220_6: setDate() and setDateTime() must accept null values.
   {
      setString(index, date == null? null : date.toString(Settings.DATE_YMD)); // Formats the date so that it can be accepted by SQL.
   }

   // rnovais@_570_17: formats the Date d and the Time t as a string "YYYY/MM/DD HH:MM:SS:ZZZ" and calls setString().
   /**
    * This method sets the specified parameter from the given Java <code>DateTime</code> value formated as "YYYY/MM/DD HH:MM:SS:ZZZ". <br>
    * <b>IMPORTANT</b>: The constructor <code>new Date(string_date)</code> must be used with care. Some devices can construct different dates, 
    * according to the device's date format. For example, the constructor <code>new Date("12/09/2006")</code>, depending on the device's date format,
    * can generate a date like "12 of September of 2006" or "09 of December of 2006". To avoid this, use the constructor 
    * <code>new Date(string_date, totalcross.sys.Settings.DATE_XXX)</code> instead, where <code>totalcross.sys.Settings.DATE_XXX</code> is a date 
    * format parameter that must be one of the <code>totalcross.sys.Settings.DATE_XXX</code> constants.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param date The value of the parameter, which can be null.
    */
   public void setDateTime(int index, Date date) // juliana@220_6: setDate() and setDateTime() must accept null values.
   {
      setString(index, date == null? null : date.toString(Settings.DATE_YMD)); // Formats the date so that it can be accepted by SQL.
   }

   // rnovais@570_56
   /**
    * Formats the <code>Time</code> t into a string "YYYY/MM/DD HH:MM:SS:ZZZ"
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param time The value of the parameter, which can be null.
    */
   public void setDateTime(int index, Time time) // juliana@220_6: setDate() and setDateTime() must accept null values.
   {
      // Formats the time so that it can be accepted by SQL.
      StringBuffer sbuf = driver.sBuffer;
         
      sbuf.setLength(0);
      if (time == null)
         setString(index, null);
      else
         setString(index, sbuf.append(time.year).append('/').append(time.month).append('/').append(time.day).append(' ').append(time.hour)
                              .append(':').append(time.minute).append(':').append(time.second).append(':').append(time.millis).toString());
   }
   
   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@223_3: PreparedStatement.setNull() now works for blobs.
   /**
    * Sets null in a given field. This can be used to set any column type as null. It must be just remembered that a parameter in a where clause 
    * can't be set to null. 
    *
    * @param index The index of the parameter value to be set as null, starting from 0.
    * @throws SQLParseException If an <code>InvalidNumberException</code> or an <code>InvalidDateException</code> is thrown.
    */
   public void setNull(int index) throws SQLParseException
   {
      testPSState();
      
      if (statement != null) // Only sets the parameter if the statement is not null.
      {
         statement.setNull(index);
         
         if (storeParams) // Only stores the parameter if there are parameters to be stored.
            paramsAsStrs[index] = null; // The string is null. 
      }
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * This method clears all of the input parameters that have been set on this statement.
    */
   public void clearParameters() 
   {
      testPSState();
      
      if (statement != null)  // Only sets the parameter if the statement is not null.
      {    
         if (storeParams) // Only stores the parameter if there are parameters to be stored.
            Convert.fill(paramsAsStrs, 0, paramsAsStrs.length, "unfilled");
         statement.clearParamValues();
      }
   }

   // juliana@253_20: added PreparedStatement.close().
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Returns the sql used in this statement. If logging is disabled, returns the sql without the arguments. If logging is enabled, returns the real
    * sql, filled with the arguments.
    *
    * @return the sql used in this statement.
    */
   public String toString() 
   {
      testPSState();
      
      if (storeParams)
      {
         StringBuffer sb = driver.sBuffer; 
         String sql = sqlExpression;
         short[] poss = paramsPos;
         String[] strs = paramsAsStrs; 
         int n = strs.length,
         i = -1;
         
         sb.setLength(0);
         sb.append("PREP: ");
         appendSubString(sb, sql, 0, poss[0]);
         
         while (++i < n)
         {
            sb.append(strs[i]);
            appendSubString(sb, sql, poss[i] + 1, poss[i + 1]);
         }
         return sb.toString();
      }
      return sqlExpression;
   }
   
   /**
    * Returns the sql used in this statement as a StringBuffer. If logging is disabled, returns the sql without the arguments. If logging is enabled,
    * returns the real sql, filled with the arguments. There is also logger information prepended, since this method is only used for logging.
    *
    * @return the sql used in this statement as a <code>StringBuffer</code>.
    */
   private StringBuffer toStringBuffer() 
   {  
      StringBuffer sb = driver.sBuffer;
      sb.setLength(0);
      
      if (storeParams)
      {   
          
         String sql = sqlExpression;
         short[] poss = paramsPos;
         String[] strs = paramsAsStrs; 
         int n = strs.length,
         i = -1;

         sb.append("PREP: ");
         appendSubString(sb, sql, 0, poss[0]);
         
         while (++i < n)
         {
            sb.append(strs[i]);
            appendSubString(sb, sql, poss[i] + 1, poss[i + 1]);
         }
         return sb;
      }
      sb.append(sqlExpression);
      return sb;
   }
   
   /**
    * Appends characters from a string in a string buffer.
    * 
    * @param strBuffer The string buffer.
    * @param string The string to have some of the characters appended at the end of the string buffer.
    * @param initialPos The position of the first character to be appended.
    * @param endPos The fist position of the character that won't be appended.
    */
   private void appendSubString(StringBuffer strBuffer, String string, int initialPos, int endPos)
   {
      while (initialPos < endPos)
         strBuffer.append(string.charAt(initialPos++));
   }
   
   // juliana@253_20: added PreparedStatement.close().
   /**
    * Tests if the driver is closed or the prepared statement is closed.
    *
    * @throws IllegalStateException If one of them is closed.
    */
   private void testPSState() throws IllegalStateException
   {
      if (driver.htTables == null) // The connection with Litebase can't be closed.
         throw new IllegalStateException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      if (!driver.htPS.exists(sqlExpression)) // The prepared statement can't be closed.
         throw new IllegalStateException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PREPARED_CLOSED));
   }
   
   // juliana@253_20: added PreparedStatement.close().
   /**
    * Closes a prepared statement.
    * 
    * @throws IllegalStateException If the driver or the prepared statement is closed.
    */
   public void close() throws IllegalStateException
   {
      if (driver.htTables == null) // The connection with Litebase can't be closed.
         throw new IllegalStateException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      if (driver.htPS.remove(sqlExpression.hashCode()) == null) // The prepared statement can't be closed.
         throw new IllegalStateException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PREPARED_CLOSED));
   }
   
   // juliana@253_21: added PreparedStatement.isValid().
   /**
    * Indicates if a prepared statement is valid or not: the driver is open and its SQL is in the hash table.
    *
    * @return <code>true</code> if the prepared statement is valid; <code>false</code>, otherwise.
    */
   public boolean isValid()
   {
      return (driver.htTables != null && driver.htPS.exists(sqlExpression));
   }
}
