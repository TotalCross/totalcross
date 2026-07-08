// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.lang.runtime;

import java.lang.reflect.Method;

public final class ObjectMethods4D {
  private ObjectMethods4D() {
  }

  public static String recordToString(String recordName, String componentNames, Object[] values) {
    String[] names = splitComponentNames(componentNames);
    StringBuffer sb = new StringBuffer(recordName);
    sb.append('[');
    for (int i = 0; i < names.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(names[i]).append('=').append(values[i]);
    }
    return sb.append(']').toString();
  }

  public static int recordHashCode(Object[] values) {
    int hash = 0;
    for (int i = 0; i < values.length; i++) {
      hash = 31 * hash + hashCode(values[i]);
    }
    return hash;
  }

  public static boolean recordEquals(Object self, Object other, String componentNames, Object[] selfValues) {
    if (self == other) {
      return true;
    }
    if (other == null || !self.getClass().equals(other.getClass())) {
      return false;
    }
    String[] names = splitComponentNames(componentNames);
    for (int i = 0; i < names.length; i++) {
      if (!equals(selfValues[i], componentValue(other, names[i]))) {
        return false;
      }
    }
    return true;
  }

  private static Object componentValue(Object target, String name) {
    try {
      Method accessor = target.getClass().getMethod(name, new Class[0]);
      return accessor.invoke(target, new Object[0]);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String[] splitComponentNames(String componentNames) {
    if (componentNames.length() == 0) {
      return new String[0];
    }
    int count = 1;
    for (int i = 0; i < componentNames.length(); i++) {
      if (componentNames.charAt(i) == ';') {
        count++;
      }
    }

    String[] names = new String[count];
    int start = 0;
    int index = 0;
    for (int i = 0; i <= componentNames.length(); i++) {
      if (i == componentNames.length() || componentNames.charAt(i) == ';') {
        names[index++] = componentNames.substring(start, i);
        start = i + 1;
      }
    }
    return names;
  }

  private static boolean equals(Object left, Object right) {
    return left == null ? right == null : left.equals(right);
  }

  private static int hashCode(Object value) {
    return value == null ? 0 : value.hashCode();
  }
}
