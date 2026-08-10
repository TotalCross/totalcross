// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.Optional;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.TCConstants;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.tclass.TCClass;
import tc.tools.converter.tclass.TCCode;
import tc.tools.converter.tclass.TCMethod;

class FloatParameterConversionTest {
  @TempDir
  Path workDir;

  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void convertedMethodsReturnParametersAfterFloatFromTheirIncomingRegisters() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileFloatParameterFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");

    GlobalConstantPool.init();
    TCClass converted = new J2TC(new JavaClass(fixture.get().bytes, false), true).converted;
    assertNotNull(converted);

    assertReturnRegister(converted, "staticInt", TCConstants.RETURN_regI, 0);
    assertReturnRegister(converted, "staticMiddle", TCConstants.RETURN_regI, 1);
    assertReturnRegister(converted, "staticObject", TCConstants.RETURN_regO, 0);
    assertReturnRegister(converted, "staticLong", TCConstants.RETURN_reg64, 1);
    assertReturnRegister(converted, "staticDouble", TCConstants.RETURN_reg64, 1);
    assertReturnRegister(converted, "instanceInt", TCConstants.RETURN_regI, 0);
    assertReturnRegister(converted, "instanceObject", TCConstants.RETURN_regO, 1);
    assertReturnRegister(converted, "instanceLong", TCConstants.RETURN_reg64, 1);
    assertReturnRegister(converted, "instanceDouble", TCConstants.RETURN_reg64, 1);
  }

  private static void assertReturnRegister(TCClass converted, String name, int opcode, int register) {
    TCMethod method = findMethod(converted, name);
    assertNotNull(method.code, name + " code");
    TCCode last = method.code[method.code.length - 1];
    assertEquals(opcode, last.op(), name + " return opcode");
    assertEquals(register, last.reg__reg(), name + " return register");
  }

  private static TCMethod findMethod(TCClass converted, String name) {
    for (TCMethod method : converted.methods) {
      if (name.equals(GlobalConstantPool.getMethodFieldName(method.cpName))) {
        return method;
      }
    }
    throw new AssertionError("Converted method not found: " + name);
  }
}
