// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.oper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OperandRegParameterMappingTest {
  private static final String[][] PARAMETER_MATRIX = {
      { "F" },
      { "F", "I" },
      { "I", "F" },
      { "I", "F", "I" },
      { "F", "Ljava/lang/Object;" },
      { "F", "J" },
      { "F", "D" },
      { "D", "F", "I" },
      { "J", "F", "Ljava/lang/Object;" },
      { "F", "F", "I" }
  };

  @Test
  void mapsCompleteFloatParameterMatrixForStaticAndInstanceMethods() throws Exception {
    for (boolean isStatic : new boolean[] { true, false }) {
      for (String[] parameters : PARAMETER_MATRIX) {
        assertMapping(parameters, isStatic);
      }
    }
  }

  private static void assertMapping(String[] parameters, boolean isStatic) throws Exception {
    OperandReg.init(parameters, isStatic);

    int local = isStatic ? 0 : 1;
    int nextI = 0;
    int next64 = 0;
    int nextO = isStatic ? 0 : 1;
    if (!isStatic) {
      assertEquals(0, OperandReg.hashO.get(0), "instance receiver");
    }

    for (String parameter : parameters) {
      char descriptor = parameter.charAt(0);
      String message = (isStatic ? "static " : "instance ") + join(parameters) + " local " + local;
      switch (descriptor) {
      case 'Z':
      case 'C':
      case 'B':
      case 'S':
      case 'I':
        assertEquals(nextI++, OperandReg.hashI.get(local), message);
        break;
      case 'J':
      case 'F':
      case 'D':
        assertEquals(next64++, OperandReg.hash64.get(local), message);
        break;
      default:
        assertEquals(nextO++, OperandReg.hashO.get(local), message);
      }
      local += descriptor == 'J' || descriptor == 'D' ? 2 : 1;
    }

    assertEquals(local, OperandReg.paramIdx, "final JVM local for " + join(parameters));
    assertEquals(nextI, OperandReg.paramRegI, "integer parameter registers");
    assertEquals(next64, OperandReg.paramReg64, "64-bit parameter registers");
    assertEquals(nextO, OperandReg.paramRegO, "object parameter registers");
  }

  private static String join(String[] parameters) {
    StringBuilder joined = new StringBuilder("(");
    for (String parameter : parameters) {
      joined.append(parameter);
    }
    return joined.append(')').toString();
  }
}
