// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang.reflect;

public interface Member4D {
  public static final int DECLARED = 0;
  public static final int PUBLIC = 1;

  public abstract Class<?> getDeclaringClass();

  public abstract String getName();

  public abstract int getModifiers();
}
