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

package totalcross.lang.reflect;

public class InvocationTargetException4D extends ReflectiveOperationException {
  public InvocationTargetException4D() {
  }

  public InvocationTargetException4D(Throwable t) {
    super(t.getClass().getName() + ": " + t.getMessage());
  }
}
