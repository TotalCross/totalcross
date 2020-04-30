// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.sql;

import java.sql.SQLException;

public class SQLWarning4D extends SQLException {
  private static final long serialVersionUID = 3734030809727595104L;
  SQLWarning4D next;

  public SQLWarning4D(String reason, String sqlState, int errorCode) {
    super(reason, sqlState, errorCode);
  }

  public SQLWarning4D(String reason, String sqlState) {
    super(reason, sqlState);
  }

  public SQLWarning4D(String reason) {
    super(reason);
  }

  public SQLWarning4D() {
    super();
  }

  public SQLWarning4D getNextWarning() {
    return next;
  }

  public void setNextException(SQLWarning4D ex) {
    next = ex;
  }
}
