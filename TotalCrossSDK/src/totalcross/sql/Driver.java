package totalcross.sql;

import totalcross.util.*;

import java.sql.SQLException;

public interface Driver
{
   public Connection connect(String url, Hashtable info) throws SQLException;
   public boolean acceptsURL(String url) throws SQLException;
   public int getMajorVersion();
   public int getMinorVersion();
}
