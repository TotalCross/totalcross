// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.Optional;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jdkcompat.lang.runtime.ObjectMethods4D;
import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.bytecode.BC186_invokedynamic;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaMethod;

class Java17FeatureTest {
  @TempDir
  Path workDir;

  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void convertsRecordClassesWithObjectMethodsBootstrap() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava17RecordFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 17");

    JavaClass javaClass = new JavaClass(fixture.get().bytes, false);

    assertTrue(hasInvokeDynamic(javaClass), "javac record output should contain ObjectMethods invokedynamic sites");

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void recordObjectMethodsHelperMatchesRecordComponentSemantics() {
    Object[] values = new Object[] { "java", Integer.valueOf(17) };
    assertEquals("Release[name=java, version=17]", ObjectMethods4D.recordToString("Release", "name;version", values));
    assertEquals(100899375, ObjectMethods4D.recordHashCode(values));

    PlainRecord same = new PlainRecord("java", 17);
    PlainRecord different = new PlainRecord("java", 16);
    assertTrue(ObjectMethods4D.recordEquals(same, new PlainRecord("java", 17), "name;version", values));
    assertFalse(ObjectMethods4D.recordEquals(same, different, "name;version", values));
    assertFalse(ObjectMethods4D.recordEquals(same, null, "name;version", values));
  }

  private static boolean hasInvokeDynamic(JavaClass javaClass) {
    for (int i = 0; i < javaClass.methods.length; i++) {
      JavaMethod method = javaClass.methods[i];
      if (method.code == null || method.code.bcs == null) {
        continue;
      }
      for (int j = 0; j < method.code.bcs.length; j++) {
        if (method.code.bcs[j] instanceof BC186_invokedynamic) {
          return true;
        }
      }
    }
    return false;
  }

  public static final class PlainRecord {
    private final String name;
    private final int version;

    PlainRecord(String name, int version) {
      this.name = name;
      this.version = version;
    }

    public String name() {
      return name;
    }

    public int version() {
      return version;
    }
  }
}
