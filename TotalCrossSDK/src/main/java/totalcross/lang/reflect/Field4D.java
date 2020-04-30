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

public class Field4D {
  int index;
  int mod;
  int primitiveType;
  Object nativeStruct; // TClass
  String name;
  Class<?> declaringClass; // class that owns this field
  Class<?> type;
  String cached;

  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  public String getName() {
    return name;
  }

  public int getModifiers() {
    return mod;
  }

  public Class<?> getType() {
    return type;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Field4D)) {
      return false;
    }
    Field4D f = (Field4D) obj;
    return f.mod == mod && f.name.equals(name) && f.type.getName().equals(type.getName())
        && f.declaringClass.getName().equals(declaringClass.getName());
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
    StringBuffer sb = new StringBuffer(64); // private static final int TCTestWin$TestField.x
    sb.append(Modifier4D.toString(mod));
    if (sb.length() > 0) {
      sb.append(' ');
    }
    String t;
    switch (primitiveType) {
    case /*public static final int BOOLEAN = */ 2:
      t = "boolean";
      break;
    case /*public static final int BYTE    = */ 3:
      t = "byte";
      break;
    case /*public static final int CHAR    = */ 4:
      t = "char";
      break;
    case /*public static final int SHORT   = */ 5:
      t = "short";
      break;
    case /*public static final int INT     = */ 6:
      t = "int";
      break;
    case /*public static final int LONG    = */ 7:
      t = "long";
      break;
    case /*public static final int FLOAT   = */ 8:
      t = "float";
      break;
    case /*public static final int DOUBLE  = */ 9:
      t = "double";
      break;
    default: {
      t = type.getName(); // Class may think that its a primitive, but its an Object, so we convert it back
      if (t.equals("int")) {
        t = "java.lang.Integer";
      } else if (t.equals("char")) {
        t = "java.lang.Character";
      } else if (t.equals("byte")) {
        t = "java.lang.Byte";
      } else if (t.equals("short")) {
        t = "java.lang.Short";
      } else if (t.equals("long")) {
        t = "java.lang.Long";
      } else if (t.equals("float")) {
        t = "java.lang.Float";
      } else if (t.equals("double")) {
        t = "java.lang.Double";
      } else if (t.equals("boolean")) {
        t = "java.lang.Boolean";
      }
    }
    }
    return cached = sb.append(t).append(' ').append(declaringClass.getName()).append('.').append(name).toString();
  }

  public native Object get(Object obj) throws IllegalArgumentException, IllegalAccessException;

  public native boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException;

  public native byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException;

  public native char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException;

  public native short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException;

  public native int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException;

  public native long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException;

  public native float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException;

  public native double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException;

  public native void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;

  public native void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException;

  public native void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException;

  public native void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException;

  public native void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException;

  public native void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException;

  public native void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException;

  public native void setFloat(Object obj, double f) throws IllegalArgumentException, IllegalAccessException;

  public native void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException;
}
