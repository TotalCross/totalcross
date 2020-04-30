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

public class Constructor4D implements Member4D {
  int mod;
  Object nativeStruct; // TClass
  String name;
  Class<?> declaringClass; // class that owns this method
  Class<?> parameterTypes[];
  Class<?> exceptionTypes[];
  String cached;

  @Override
  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getModifiers() {
    return mod;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Constructor4D)) {
      return false;
    }
    Constructor4D m = (Constructor4D) obj;
    if (m.mod != mod || !m.name.equals(name) || !m.declaringClass.getName().equals(declaringClass.getName())
        || parameterTypes.length != m.parameterTypes.length/* || exceptionTypes.length != m.exceptionTypes.length*/) {
      return false;
    }
    for (int i = 0; i < parameterTypes.length; i++) {
      if (!parameterTypes[i].equals(m.parameterTypes[i])) {
        return false;
      }
    }
    //for (int i =0; i < exceptionTypes.length; i++) if (!exceptionTypes[i].equals(m.exceptionTypes[i])) return false; - not needed
    return true;
  }

  @Override
  public int hashCode() {
    return declaringClass.getName().hashCode() ^ name.hashCode();
  }

  @Override
  public String toString() {
    if (cached != null) {
      return cached;
    }
    StringBuffer sb = new StringBuffer(128); // public static final void TCTestWin$TestMethod.printTest(int,short,java.lang.String,boolean,java.lang.Object,long,byte,char,double)
    sb.append(Modifier4D.toString(mod));
    if (sb.length() > 0) {
      sb.append(' ');
    }
    sb.append(name).append('(');
    for (int i = 0, last = parameterTypes.length - 1; i <= last; i++) {
      sb.append(Method4D.toString(parameterTypes[i]));
      if (i < last) {
        sb.append(',');
      }
    }
    return cached = sb.append(')').toString();
  }

  public Class<?>[] getParameterTypes() {
    return parameterTypes;
  }

  public Class<?>[] getExceptionTypes() {
    return exceptionTypes;
  }

  public native Object newInstance(Object initargs[])
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException4D;
}
