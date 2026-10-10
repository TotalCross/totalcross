// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.tclass.TCClass;
import tc.tools.converter.tclass.TCMethod;
import totalcross.lang.Class4D;

class Class4DConverterTest {
  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void getSignersExistsAndReturnsNull() {
    assertNull(new Class4D<Object>().getSigners());
  }

  @Test
  void getSignersConvertsToJavaAndDoesNotCreateNativeBridge() throws Exception {
    J2TC.htAddedClasses.clear();
    J2TC.htExcludedClasses.clear();
    GlobalConstantPool.init();

    try (InputStream stream = Class4D.class.getResourceAsStream("Class4D.class")) {
      assertNotNull(stream, "Class4D.class resource");
      byte[] bytes = stream.readAllBytes();
      TCClass converted = new J2TC(new JavaClass(bytes, false), true).converted;
      TCMethod getSigners = findMethod(converted, "getSigners");

      assertNotNull(getSigners, "converted getSigners method");
      assertFalse(getSigners.flags.isNative);
      assertNotNull(getSigners.code, "Java implementation must retain executable code");
      assertFalse(NativeBridgeModel.fromClassBytes(bytes).stream()
          .anyMatch(entry -> "getSigners".equals(entry.sourceName)));
    }
  }

  private static TCMethod findMethod(TCClass converted, String name) {
    for (TCMethod method : converted.methods) {
      if (name.equals(GlobalConstantPool.getMethodFieldName(method.cpName))) return method;
    }
    return null;
  }
}
