// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JavaTypeMappingTest {
  @Test
  void lowersPrimitiveObjectAndArrayDescriptorsWithProductionRules() {
    assertEquals("&V", GlobalConstantPool.javaType2TCType("V"));
    assertEquals("&b", GlobalConstantPool.javaType2TCType("Z"));
    assertEquals("&B", GlobalConstantPool.javaType2TCType("B"));
    assertEquals("&C", GlobalConstantPool.javaType2TCType("C"));
    assertEquals("&S", GlobalConstantPool.javaType2TCType("S"));
    assertEquals("&I", GlobalConstantPool.javaType2TCType("I"));
    assertEquals("&L", GlobalConstantPool.javaType2TCType("J"));
    assertEquals("&D", GlobalConstantPool.javaType2TCType("F"));
    assertEquals("&D", GlobalConstantPool.javaType2TCType("D"));
    assertEquals("java.lang.String", GlobalConstantPool.javaType2TCType("Ljava/lang/String;"));
    assertEquals("[&I", GlobalConstantPool.javaType2TCType("[I"));
    assertEquals("[&F", GlobalConstantPool.javaType2TCType("[F"));
    assertEquals("[[java.lang.String", GlobalConstantPool.javaType2TCType("[[Ljava/lang/String;"));
    assertEquals("java.util.ArrayList",
        GlobalConstantPool.javaType2TCType("Ltotalcross/util/ArrayList4D;"));
  }

  @Test
  void matchesConstantPoolLoweringForEveryDescriptorFamily() {
    GlobalConstantPool.init();
    String[] primitives = { "V", "Z", "B", "C", "S", "I", "J", "F", "D" };
    for (String descriptor : primitives) {
      assertEquals(GlobalConstantPool.javaPrimitiveType2TCType(descriptor),
          GlobalConstantPool.javaType2TCType(descriptor));
    }
    String[] references = { "Ljava/lang/String;", "[I", "[F", "[[Ljava/lang/String;" };
    for (String descriptor : references) {
      int index = GlobalConstantPool.putParam(descriptor);
      assertEquals(GlobalConstantPool.getClassName(index), GlobalConstantPool.javaType2TCType(descriptor));
    }
  }
}
