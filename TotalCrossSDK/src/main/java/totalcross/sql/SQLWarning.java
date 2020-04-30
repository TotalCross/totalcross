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

public class SQLWarning extends SQLException {
  SQLWarning next;

  public SQLWarning(String reason, String sqlState, int errorCode) {
    super(reason, sqlState, errorCode);
  }

  public SQLWarning(String reason, String sqlState) {
    super(reason, sqlState);
  }

  public SQLWarning(String reason) {
    super(reason);
  }

  public SQLWarning() {
    super();
  }

  public SQLWarning getNextWarning() {
    return next;
  }

  public void setNextException(SQLWarning ex) {
    next = ex;
  }
}
