package totalcross.sql;

public interface Connection
{
   public static final int TRANSACTION_NONE = 0;
   public static final int TRANSACTION_READ_UNCOMMITTED = 1;
   public static final int TRANSACTION_READ_COMMITTED = 2;
   public static final int TRANSACTION_REPEATABLE_READ = 4;
   public static final int TRANSACTION_SERIALIZABLE = 8;

   public Statement createStatement() throws SQLWarning;
   
   public PreparedStatement prepareStatement(String sql) throws SQLWarning;
   
   public String nativeSQL(String sql) throws SQLWarning;
   
   public void setAutoCommit(boolean autoCommit) throws SQLWarning;
   
   public boolean getAutoCommit() throws SQLWarning;
   
   public void commit() throws SQLWarning;
   
   public void rollback() throws SQLWarning;
   
   public void close() throws SQLWarning;
   
   public boolean isClosed() throws SQLWarning;
   
   public void setReadOnly(boolean readOnly) throws SQLWarning;
   
   public boolean isReadOnly() throws SQLWarning;
   
   public void setCatalog(String catalog) throws SQLWarning;
   
   public String getCatalog() throws SQLWarning;
   
   public void setTransactionIsolation(int level) throws SQLWarning;
   
   public int getTransactionIsolation() throws SQLWarning;
   
   public SQLWarning getWarnings() throws SQLWarning;
   
   public void clearWarnings() throws SQLWarning;
   
   public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLWarning;
   
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLWarning;
}
