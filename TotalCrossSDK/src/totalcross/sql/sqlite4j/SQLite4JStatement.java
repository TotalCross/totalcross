package totalcross.sql.sqlite4j;

import totalcross.sql.*;
import java.sql.SQLException;

public class SQLite4JStatement implements Statement
{
   java.sql.Statement stat;
   
   public SQLite4JStatement(java.sql.Statement stat)
   {
      this.stat = stat;
   }

   public ResultSet executeQuery(String sql) throws SQLException
   {
      return new SQLite4JResultSet(stat.executeQuery(sql));
   }

   public int executeUpdate(String sql) throws SQLException
   {
      return stat.executeUpdate(sql);
   }

   public void close() throws SQLException
   {
      stat.close();
   }

   public int getMaxRows() throws SQLException
   {
      return stat.getMaxRows();
   }

   public void setMaxRows(int max) throws SQLException
   {
      stat.setMaxRows(max);
   }

   public int getQueryTimeout() throws SQLException
   {
      return stat.getQueryTimeout();
   }

   public void setQueryTimeout(int seconds) throws SQLException
   {
      stat.setQueryTimeout(seconds);
   }

   public void cancel() throws SQLException
   {
      stat.cancel();
   }

   public java.sql.SQLWarning getWarnings() throws SQLException
   {
      return stat.getWarnings();
   }

   public void clearWarnings() throws SQLException
   {
      stat.clearWarnings();
   }

   public void setCursorName(String name) throws SQLException
   {
      stat.setCursorName(name);
   }

   public boolean execute(String sql) throws SQLException
   {
      return stat.execute(sql);
   }

   public ResultSet getResultSet() throws SQLException
   {
      java.sql.ResultSet rs = stat.getResultSet();
      return rs == null ? null : new SQLite4JResultSet(rs);
   }

   public int getUpdateCount() throws SQLException
   {
      return stat.getUpdateCount();
   }

   public boolean getMoreResults() throws SQLException
   {
      return stat.getMoreResults();
   }

   public void setFetchDirection(int direction) throws SQLException
   {
      stat.setFetchDirection(direction);
   }

   public int getFetchDirection() throws SQLException
   {
      return stat.getFetchDirection();
   }

   public void setFetchSize(int rows) throws SQLException
   {
      stat.setFetchSize(rows);
   }

   public int getFetchSize() throws SQLException
   {
      return stat.getFetchSize();
   }

   public int getResultSetConcurrency() throws SQLException
   {
      return stat.getResultSetConcurrency();
   }

   public int getResultSetType() throws SQLException
   {
      return stat.getResultSetType();
   }

   public void addBatch(String sql) throws SQLException
   {
      stat.addBatch(sql);
   }

   public void clearBatch() throws SQLException
   {
      stat.clearBatch();
   }

   public int[] executeBatch() throws SQLException
   {
      return stat.executeBatch();
   }

   public Connection getConnection() throws SQLException
   {
      return new SQLite4JConnection((org.sqlite.SQLiteConnection)stat.getConnection());
   }
}

