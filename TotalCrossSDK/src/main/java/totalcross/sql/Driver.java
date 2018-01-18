package totalcross.sql;

import java.sql.SQLException;
import totalcross.util.Hashtable;

public interface Driver {
  public Connection connect(String url, Hashtable info) throws SQLException;

  public boolean acceptsURL(String url) throws SQLException;

  public int getMajorVersion();

  public int getMinorVersion();
}
