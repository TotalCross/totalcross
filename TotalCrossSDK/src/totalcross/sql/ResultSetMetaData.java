package totalcross.sql;

public interface ResultSetMetaData
{
   public static final int columnNoNulls = 0;
   public static final int columnNullable = 1;
   public static final int columnNullableUnknown = 2;
   
   public int getColumnCount() throws SQLWarning;
   
   public boolean isAutoIncrement(int column) throws SQLWarning;
   
   public boolean isCaseSensitive(int column) throws SQLWarning;
   
   public boolean isSearchable(int column) throws SQLWarning;
   
   public boolean isCurrency(int column) throws SQLWarning;
   
   public int isNullable(int column) throws SQLWarning;
   
   public boolean isSigned(int column) throws SQLWarning;
   
   public int getColumnDisplaySize(int column) throws SQLWarning;
   
   public String getColumnLabel(int column) throws SQLWarning;
   
   public String getColumnName(int column) throws SQLWarning;
   
   public String getSchemaName(int column) throws SQLWarning;
   
   public int getPrecision(int column) throws SQLWarning;
   
   public int getScale(int column) throws SQLWarning;
   
   public String getTableName(int column) throws SQLWarning;
   
   public String getCatalogName(int column) throws SQLWarning;
   
   public int getColumnType(int column) throws SQLWarning;
   
   public String getColumnTypeName(int column) throws SQLWarning;
   
   public boolean isReadOnly(int column) throws SQLWarning;
   
   public boolean isWritable(int column) throws SQLWarning;
   
   public boolean isDefinitelyWritable(int column) throws SQLWarning;
   
   public String getColumnClassName(int column) throws SQLWarning;
}

