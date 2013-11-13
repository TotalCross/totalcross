package totalcross.sql;

import totalcross.util.*;

import java.sql.*;

public interface Driver
{
   public Connection connect(String url, Properties info) throws SQLWarning;
   public boolean acceptsURL(String url) throws SQLWarning;
   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLWarning;
   public int getMajorVersion();
   public int getMinorVersion();
   public boolean jdbcCompliant();
}
