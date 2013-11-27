package totalcross.sql.sqlite4d;

import totalcross.sql.*;
import java.sql.SQLException;

public class SQLite4DResultSetMetaData implements ResultSetMetaData
{
   public SQLite4DResultSetMetaData()
   {
      nativeCreate();
   }

   native void nativeCreate();
   native public int getColumnCount() throws SQLException;
   native public boolean isAutoIncrement(int column) throws SQLException;
   native public boolean isCaseSensitive(int column) throws SQLException;
   native public boolean isSearchable(int column) throws SQLException;
   native public boolean isCurrency(int column) throws SQLException;
   native public int isNullable(int column) throws SQLException;
   native public boolean isSigned(int column) throws SQLException;
   native public int getColumnDisplaySize(int column) throws SQLException;
   native public String getColumnLabel(int column) throws SQLException;
   native public String getColumnName(int column) throws SQLException;
   native public String getSchemaName(int column) throws SQLException;
   native public int getPrecision(int column) throws SQLException;
   native public int getScale(int column) throws SQLException;
   native public String getTableName(int column) throws SQLException;
   native public String getCatalogName(int column) throws SQLException;
   native public int getColumnType(int column) throws SQLException;
   native public String getColumnTypeName(int column) throws SQLException;
   native public boolean isReadOnly(int column) throws SQLException;
   native public boolean isWritable(int column) throws SQLException;
   native public boolean isDefinitelyWritable(int column) throws SQLException;
   native public String getColumnClassName(int column) throws SQLException;
}

