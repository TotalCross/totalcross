package totalcross.sql.sqlite4d;

import totalcross.sql.*;
import java.sql.SQLException;

public class SQLite4DConnection implements Connection
{
   public SQLite4DConnection(String url, String name)
   {
      nativeCreate(url,name);
   }

   native void nativeCreate(String url, String name);
   native public Statement createStatement() throws SQLException;
   native public PreparedStatement prepareStatement(String sql) throws SQLException;
   native public String nativeSQL(String sql);
   native public void setAutoCommit(boolean autoCommit) throws SQLException;
   native public boolean getAutoCommit() throws SQLException;
   native public void commit() throws SQLException;
   native public void rollback() throws SQLException;
   native public void close() throws SQLException;
   native public boolean isClosed() throws SQLException;
   native public void setReadOnly(boolean readOnly) throws SQLException;
   native public boolean isReadOnly() throws SQLException;
   native public void setCatalog(String catalog) throws SQLException;
   native public String getCatalog() throws SQLException;
   native public void setTransactionIsolation(int level) throws SQLException;
   native public int getTransactionIsolation() throws SQLException;
   native public SQLException getWarnings() throws SQLException;
   native public void clearWarnings() throws SQLException;
   native public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException;
   native public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException;
}
