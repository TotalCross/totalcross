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

public class Modifier4D {
  public static final int PUBLIC = 1;
  public static final int PRIVATE = 2;
  public static final int PROTECTED = 4;
  public static final int STATIC = 8;
  public static final int FINAL = 16;
  public static final int SYNCHRONIZED = 32;
  public static final int VOLATILE = 64;
  public static final int TRANSIENT = 128;
  public static final int NATIVE = 256;
  public static final int INTERFACE = 512;
  public static final int ABSTRACT = 1024;

  public Modifier4D() {
  }

  public static boolean isPublic(int mod) {
    return (mod & PUBLIC) != 0;
  }

  public static boolean isPrivate(int mod) {
    return (mod & PRIVATE) != 0;
  }

  public static boolean isProtected(int mod) {
    return (mod & PROTECTED) != 0;
  }

  public static boolean isStatic(int mod) {
    return (mod & STATIC) != 0;
  }

  public static boolean isFinal(int mod) {
    return (mod & FINAL) != 0;
  }

  public static boolean isSynchronized(int mod) {
    return (mod & SYNCHRONIZED) != 0;
  }

  public static boolean isVolatile(int mod) {
    return (mod & VOLATILE) != 0;
  }

  public static boolean isTransient(int mod) {
    return (mod & TRANSIENT) != 0;
  }

  public static boolean isNative(int mod) {
    return (mod & NATIVE) != 0;
  }

  public static boolean isInterface(int mod) {
    return (mod & INTERFACE) != 0;
  }

  public static boolean isAbstract(int mod) {
    return (mod & ABSTRACT) != 0;
  }

  public static String toString(int mod) {
    StringBuffer sb = new StringBuffer(128);
    if (isPublic(mod)) {
      sb.append("public ");
    }
    if (isPrivate(mod)) {
      sb.append("private ");
    }
    if (isProtected(mod)) {
      sb.append("protected ");
    }
    if (isStatic(mod)) {
      sb.append("static ");
    }
    if (isFinal(mod)) {
      sb.append("final ");
    }
    if (isSynchronized(mod)) {
      sb.append("synchronized ");
    }
    if (isVolatile(mod)) {
      sb.append("volatile ");
    }
    if (isTransient(mod)) {
      sb.append("transient ");
    }
    if (isNative(mod)) {
      sb.append("native ");
    }
    if (isInterface(mod)) {
      sb.append("interface ");
    }
    if (isAbstract(mod)) {
      sb.append("abstract ");
    }
    return sb.toString().trim();
  }
}
