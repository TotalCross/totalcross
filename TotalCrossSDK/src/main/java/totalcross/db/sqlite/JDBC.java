/*
 * Copyright (c) 2007 David Crawshaw <david@zentus.com>
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package totalcross.db.sqlite;

import java.sql.SQLException;
import totalcross.sql.Connection;
import totalcross.sql.Driver;
import totalcross.sql.DriverManager;
import totalcross.sql.DriverPropertyInfo;
import totalcross.util.Hashtable;

public class JDBC implements Driver {
  public static final String PREFIX = "jdbc:sqlite:";

  static {
    try {
      DriverManager.registerDriver(new JDBC());
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * @see java.sql.Driver#getMajorVersion()
   */
  @Override
  public int getMajorVersion() {
    return /*SQLiteJDBCLoader.getMajorVersion()*/1;
  }

  /**
   * @see java.sql.Driver#getMinorVersion()
   */
  @Override
  public int getMinorVersion() {
    return /*SQLiteJDBCLoader.getMinorVersion()*/0;
  }

  /**
   * @see java.sql.Driver#jdbcCompliant()
   */
  public boolean jdbcCompliant() {
    return false;
  }

  /**
   * @see java.sql.Driver#acceptsURL(java.lang.String)
   */
  @Override
  public boolean acceptsURL(String url) {
    return isValidURL(url);
  }

  /**
   * Validates a URL
   * @param url
   * @return true if the URL is valid, false otherwise
   */
  public static boolean isValidURL(String url) {
    return url != null && url.toLowerCase().startsWith(PREFIX);
  }

  /**
   * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
   */
  public DriverPropertyInfo[] getPropertyInfo(String url, Hashtable info) throws SQLException {
    return SQLiteConfig.getDriverPropertyInfo();
  }

  /**
   * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
   */
  @Override
  public Connection connect(String url, Hashtable info) throws SQLException {
    return createConnection(url, info);
  }

  /**
   * Gets the location to the database from a given URL.
   * @param url The URL to extract the location from.
   * @return The location to the database.
   */
  static String extractAddress(String url) {
    // if no file name is given use a memory database
    return PREFIX.equalsIgnoreCase(url) ? ":memory:" : url.substring(PREFIX.length());
  }

  /**
   * Creates a new database connection to a given URL.
   * @param url the URL
   * @param prop the properties
   * @return a Connection object that represents a connection to the URL
   * @throws SQLException
   * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
   */
  public static totalcross.sql.Connection createConnection(String url, Hashtable prop) throws SQLException {
    if (!isValidURL(url)) {
      throw new SQLException("invalid database address: " + url);
    }

    url = url.trim();
    return new SQLiteConnection(url, extractAddress(url), prop);
  }
}
