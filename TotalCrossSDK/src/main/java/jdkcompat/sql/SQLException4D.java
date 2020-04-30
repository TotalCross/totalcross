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

public class SQLException4D extends Exception {
  private static final long serialVersionUID = 2177808452277895382L;
  String state;
  int code;
  SQLException next;

  public SQLException4D(String reason, String sqlState, int errorCode) {
    super(reason);
    this.state = sqlState;
    this.code = errorCode;
  }

  public SQLException4D(String reason, String sqlState) {
    this(reason, sqlState, 0);
  }

  public SQLException4D(String reason, Throwable cause) {
    this(reason, null, 0);
    initCause(cause);
  }

  public SQLException4D(String reason) {
    this(reason, null, 0);
  }

  public SQLException4D() {
    this(null, null, 0);
  }

  public String getSQLState() {
    return state;
  }

  public int getErrorCode() {
    return code;
  }

  public SQLException getNextException() {
    return next;
  }

  public void setNextException(SQLException ex) {
    next = ex;
  }
}
