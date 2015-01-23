package totalcross.sql.sqlite4j;

import totalcross.sql.*;
import java.sql.SQLException;

public class SQLite4JResultSetMetaData implements ResultSetMetaData
{
   java.sql.ResultSetMetaData meta;
   
   public SQLite4JResultSetMetaData(java.sql.ResultSetMetaData metaData)
   {
      this.meta = metaData;
   }

   public int getColumnCount() throws SQLException
   {
      return meta.getColumnCount();
   }

   public boolean isAutoIncrement(int column) throws SQLException
   {
      return meta.isAutoIncrement(column);
   }

   public boolean isCaseSensitive(int column) throws SQLException
   {
      return meta.isCaseSensitive(column);
   }

   public boolean isSearchable(int column) throws SQLException
   {
      return meta.isSearchable(column);
   }

   public boolean isCurrency(int column) throws SQLException
   {
      return meta.isCurrency(column);
   }

   public int isNullable(int column) throws SQLException
   {
      return meta.isNullable(column);
   }

   public boolean isSigned(int column) throws SQLException
   {
      return meta.isSigned(column);
   }

   public int getColumnDisplaySize(int column) throws SQLException
   {
      return meta.getColumnDisplaySize(column);
   }

   public String getColumnLabel(int column) throws SQLException
   {
      return meta.getColumnLabel(column);
   }

   public String getColumnName(int column) throws SQLException
   {
      return meta.getColumnName(column);
   }

   public String getSchemaName(int column) throws SQLException
   {
      return meta.getSchemaName(column);
   }

   public int getPrecision(int column) throws SQLException
   {
      return meta.getPrecision(column);
   }

   public int getScale(int column) throws SQLException
   {
      return meta.getScale(column);
   }

   public String getTableName(int column) throws SQLException
   {
      return meta.getTableName(column);
   }

   public String getCatalogName(int column) throws SQLException
   {
      return meta.getCatalogName(column);
   }

   public int getColumnType(int column) throws SQLException
   {
      return meta.getColumnType(column);
   }

   public String getColumnTypeName(int column) throws SQLException
   {
      return meta.getColumnTypeName(column);
   }

   public boolean isReadOnly(int column) throws SQLException
   {
      return meta.isReadOnly(column);
   }

   public boolean isWritable(int column) throws SQLException
   {
      return meta.isWritable(column);
   }

   public boolean isDefinitelyWritable(int column) throws SQLException
   {
      return meta.isDefinitelyWritable(column);
   }

   public String getColumnClassName(int column) throws SQLException
   {
      return meta.getColumnClassName(column);
   }
}

