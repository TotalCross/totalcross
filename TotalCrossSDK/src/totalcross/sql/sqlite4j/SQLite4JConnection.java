package totalcross.sql.sqlite4j;

import totalcross.sql.*;
import java.sql.SQLException;
import java.sql.SQLWarning;

public class SQLite4JConnection implements Connection
{
   public org.sqlite.SQLiteConnection con;
   
   public SQLite4JConnection(org.sqlite.SQLiteConnection con)
   {
      this.con = con;
   }

   public Statement createStatement() throws SQLException
   {
      return new SQLite4JStatement(con.createStatement());
   }

   public PreparedStatement prepareStatement(String sql) throws SQLException
   {
      return new SQLite4JPreparedStatement(con.prepareStatement(sql));
   }

   public String nativeSQL(String sql)
   {
      return con.nativeSQL(sql);
   }

   public void setAutoCommit(boolean autoCommit) throws SQLException
   {
      con.setAutoCommit(autoCommit);
   }

   public boolean getAutoCommit() throws SQLException
   {
      return con.getAutoCommit();
   }

   public void commit() throws SQLException
   {
      con.commit();
   }

   public void rollback() throws SQLException
   {
      con.rollback();
   }

   public void close() throws SQLException
   {
      con.close();
   }

   public boolean isClosed() throws SQLException
   {
      return con.isClosed();
   }

   public void setReadOnly(boolean readOnly) throws SQLException
   {
      con.setReadOnly(readOnly);
   }

   public boolean isReadOnly() throws SQLException
   {
      return con.isReadOnly();
   }

   public void setCatalog(String catalog) throws SQLException
   {
      con.setCatalog(catalog);
   }

   public String getCatalog() throws SQLException
   {
      return con.getCatalog();
   }

   public void setTransactionIsolation(int level) throws SQLException
   {
      con.setTransactionIsolation(level);
   }

   public int getTransactionIsolation() throws SQLException
   {
      return con.getTransactionIsolation();
   }

   public SQLWarning getWarnings() throws SQLException
   {
      return con.getWarnings();
   }

   public void clearWarnings() throws SQLException
   {
      con.clearWarnings();
   }

   public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
   {
      return new SQLite4JStatement(con.createStatement(resultSetType, resultSetConcurrency));
   }

   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      return new SQLite4JPreparedStatement(con.prepareStatement(sql, resultSetType, resultSetConcurrency));
   }

}
