// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang.reflect;

public class InvocationTargetException4D extends ReflectiveOperationException {
  public InvocationTargetException4D() {
  }

  public InvocationTargetException4D(Throwable t) {
    super(t.getClass().getName() + ": " + t.getMessage());
  }
}
