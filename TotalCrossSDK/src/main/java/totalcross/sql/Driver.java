// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sql;

import java.sql.SQLException;
import totalcross.util.Hashtable;

public interface Driver {
  public Connection connect(String url, Hashtable info) throws SQLException;

  public boolean acceptsURL(String url) throws SQLException;

  public int getMajorVersion();

  public int getMinorVersion();
}
