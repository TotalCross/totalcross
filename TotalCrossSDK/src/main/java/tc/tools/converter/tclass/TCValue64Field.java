// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.tclass;

/* This structure represents a class field. */
public final class TCValue64Field extends TCField {
  public double dvalue; // may be a long too. see flags.isLong
  public long lvalue;
}
