package totalcross.sql;

public interface Statement
{
   public ResultSet executeQuery(String sql) throws SQLWarning;
   
   public int executeUpdate(String sql) throws SQLWarning;
   
   public void close() throws SQLWarning;
   
   public int getMaxRows() throws SQLWarning;
   
   public void setMaxRows(int max) throws SQLWarning;
   
   public int getQueryTimeout() throws SQLWarning;
   
   public void setQueryTimeout(int seconds) throws SQLWarning;
   
   public void cancel() throws SQLWarning;
   
   public SQLWarning getWarnings() throws SQLWarning;
   
   public void clearWarnings() throws SQLWarning;
   
   public void setCursorName(String name) throws SQLWarning;
   
   public boolean execute(String sql) throws SQLWarning;
   
   public ResultSet getResultSet() throws SQLWarning;
   
   public int getUpdateCount() throws SQLWarning;
   
   public boolean getMoreResults() throws SQLWarning;
   
   public void setFetchDirection(int direction) throws SQLWarning;
   
   public int getFetchDirection() throws SQLWarning;
   
   public void setFetchSize(int rows) throws SQLWarning;
   
   public int getFetchSize() throws SQLWarning;
   
   public int getResultSetConcurrency() throws SQLWarning;
   
   public int getResultSetType() throws SQLWarning;
   
   public void addBatch(String sql) throws SQLWarning;
   
   public void clearBatch() throws SQLWarning;
   
   public int[] executeBatch() throws SQLWarning;
   
   public Connection getConnection() throws SQLWarning;
}

