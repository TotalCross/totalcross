package totalcross.sql.sqlite4d;

import totalcross.sql.*;
import java.sql.SQLException;

public class SQLite4DStatement implements Statement
{
   public SQLite4DStatement()
   {
      nativeCreate();
   }

   native void nativeCreate();
   native public ResultSet executeQuery(String sql) throws SQLException;
   native public int executeUpdate(String sql) throws SQLException;
   native public void close() throws SQLException;
   native public int getMaxRows() throws SQLException;
   native public void setMaxRows(int max) throws SQLException;
   native public int getQueryTimeout() throws SQLException;
   native public void setQueryTimeout(int seconds) throws SQLException;
   native public void cancel() throws SQLException;
   native public java.sql.SQLWarning getWarnings() throws SQLException;
   native public void clearWarnings() throws SQLException;
   native public void setCursorName(String name) throws SQLException;
   native public boolean execute(String sql) throws SQLException;
   native public ResultSet getResultSet() throws SQLException;
   native public int getUpdateCount() throws SQLException;
   native public boolean getMoreResults() throws SQLException;
   native public void setFetchDirection(int direction) throws SQLException;
   native public int getFetchDirection() throws SQLException;
   native public void setFetchSize(int rows) throws SQLException;
   native public int getFetchSize() throws SQLException;
   native public int getResultSetConcurrency() throws SQLException;
   native public int getResultSetType() throws SQLException;
   native public void addBatch(String sql) throws SQLException;
   native public void clearBatch() throws SQLException;
   native public int[] executeBatch() throws SQLException;
   native public Connection getConnection() throws SQLException;
}

