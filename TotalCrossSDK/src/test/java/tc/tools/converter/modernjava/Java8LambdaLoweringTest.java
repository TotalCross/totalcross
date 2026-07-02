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

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.Java8LambdaLowering;
import tc.tools.converter.bytecode.BC186_invokedynamic;
import tc.tools.converter.bytecode.BC192_checkcast;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.bytecode.MethodCall;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaField;
import tc.tools.converter.java.JavaMethod;

class Java8LambdaLoweringTest {
  @TempDir
  Path workDir;

  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void generatesAdapterClassForStatelessLambda() throws Exception {
    JavaClass javaClass = statelessLambdaClass();

    JavaClass[] adapters = Java8LambdaLowering.generateAdapterClasses(javaClass);

    assertEquals(1, adapters.length);
    assertEquals("fixtures/CompiledJava8StatelessLambda$$TC$$Lambda$0", adapters[0].className);
    assertTrue(hasMethod(adapters[0], "run", "run()"));
    assertTrue(hasMethod(adapters[0], "$$tc_lambda_factory$0", "$$tc_lambda_factory$0()"));
    assertFalse(hasInvokeDynamic(adapters[0]));

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[0], true));
  }

  @Test
  void convertsStatelessLambdaToNormalFactoryCall() throws Exception {
    JavaClass javaClass = statelessLambdaClass();
    GlobalConstantPool.init();

    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void generatesAdapterClassForCapturedLambda() throws Exception {
    JavaClass javaClass = capturedLambdaClass();

    JavaClass[] adapters = Java8LambdaLowering.generateAdapterClasses(javaClass);

    assertEquals(1, adapters.length);
    assertEquals("fixtures/CompiledJava8Lambda$$TC$$Lambda$0", adapters[0].className);
    assertTrue(hasField(adapters[0], "arg$0", "Ljava/lang/String;"));
    assertTrue(hasMethod(adapters[0], "<init>", "<init>(Ljava/lang/String;)"));
    assertTrue(hasMethod(adapters[0], "run", "run()"));
    assertTrue(hasMethod(adapters[0], "$$tc_lambda_factory$0", "$$tc_lambda_factory$0(Ljava/lang/String;)"));
    assertFalse(hasInvokeDynamic(adapters[0]));

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[0], true));
  }

  @Test
  void convertsCapturedLambdaToNormalFactoryCall() throws Exception {
    JavaClass javaClass = capturedLambdaClass();
    GlobalConstantPool.init();

    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void generatesAdapterClassesForMethodReferences() throws Exception {
    JavaClass javaClass = methodReferenceClass();

    JavaClass[] adapters = Java8LambdaLowering.generateAdapterClasses(javaClass);

    assertEquals(3, adapters.length);
    assertEquals("fixtures/CompiledJava8MethodReference$$TC$$Lambda$0", adapters[0].className);
    assertTrue(hasMethod(adapters[0], "get", "get()"));
    assertTrue(hasMethod(adapters[0], "$$tc_lambda_factory$0", "$$tc_lambda_factory$0()"));
    assertFalse(hasInvokeDynamic(adapters[0]));

    assertEquals("fixtures/CompiledJava8MethodReference$$TC$$Lambda$1", adapters[1].className);
    assertTrue(hasMethod(adapters[1], "map",
        "map(Lfixtures/CompiledJava8MethodReference;)"));
    assertTrue(hasMethod(adapters[1], "$$tc_lambda_factory$1", "$$tc_lambda_factory$1()"));
    assertFalse(hasInvokeDynamic(adapters[1]));

    assertEquals("fixtures/CompiledJava8MethodReference$$TC$$Lambda$2", adapters[2].className);
    assertTrue(hasField(adapters[2], "arg$0", "Lfixtures/CompiledJava8MethodReference;"));
    assertTrue(hasMethod(adapters[2], "get", "get()"));
    assertTrue(hasMethod(adapters[2], "$$tc_lambda_factory$2",
        "$$tc_lambda_factory$2(Lfixtures/CompiledJava8MethodReference;)"));
    assertFalse(hasInvokeDynamic(adapters[2]));

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[0], true));
    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[1], true));
    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[2], true));
  }

  @Test
  void convertsMethodReferencesToNormalFactoryCalls() throws Exception {
    JavaClass javaClass = methodReferenceClass();
    GlobalConstantPool.init();

    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void generatesAdapterClassForConstructorReference() throws Exception {
    JavaClass javaClass = constructorReferenceClass();

    JavaClass[] adapters = Java8LambdaLowering.generateAdapterClasses(javaClass);

    assertEquals(1, adapters.length);
    assertEquals("fixtures/CompiledJava8ConstructorReference$$TC$$Lambda$0", adapters[0].className);
    assertTrue(hasMethod(adapters[0], "create", "create(Ljava/lang/String;)"));
    assertTrue(hasMethod(adapters[0], "$$tc_lambda_factory$0", "$$tc_lambda_factory$0()"));
    assertFalse(hasInvokeDynamic(adapters[0]));

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[0], true));
  }

  @Test
  void convertsConstructorReferencesToNormalFactoryCalls() throws Exception {
    JavaClass javaClass = constructorReferenceClass();
    GlobalConstantPool.init();

    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void generatesAdapterClassForAltMetafactoryMarkers() throws Exception {
    JavaClass javaClass = altMetafactoryMarkerClass();

    JavaClass[] adapters = Java8LambdaLowering.generateAdapterClasses(javaClass);

    assertEquals(1, adapters.length);
    assertEquals("fixtures/CompiledJava8AltMetafactoryMarker$$TC$$Lambda$0", adapters[0].className);
    assertTrue(hasInterface(adapters[0], "fixtures/CompiledJava8AltMetafactoryMarker$TextFactory"));
    assertTrue(hasInterface(adapters[0], "fixtures/CompiledJava8AltMetafactoryMarker$Marker"));
    assertTrue(hasMethod(adapters[0], "get", "get()"));
    assertTrue(hasMethod(adapters[0], "$$tc_lambda_factory$0", "$$tc_lambda_factory$0()"));
    assertFalse(hasInvokeDynamic(adapters[0]));

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[0], true));
  }

  @Test
  void convertsAltMetafactoryMarkersToNormalFactoryCalls() throws Exception {
    JavaClass javaClass = altMetafactoryMarkerClass();
    GlobalConstantPool.init();

    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void generatesAdapterClassForAltMetafactoryBridges() throws Exception {
    JavaClass javaClass = altMetafactoryBridgeClass();

    JavaClass[] adapters = Java8LambdaLowering.generateAdapterClasses(javaClass);

    assertEquals(1, adapters.length);
    assertEquals("fixtures/CompiledJava8AltMetafactoryBridge$$TC$$Lambda$0", adapters[0].className);
    assertTrue(hasInterface(adapters[0], "fixtures/CompiledJava8AltMetafactoryBridge$StringFactory"));
    assertTrue(hasInterface(adapters[0], "fixtures/CompiledJava8AltMetafactoryBridge$ObjectFactory"));
    assertTrue(hasMethod(adapters[0], "get", "get()", "Ljava/lang/String;"));
    assertTrue(hasMethod(adapters[0], "get", "get()", "Ljava/lang/Object;"));
    assertFalse(hasInvokeDynamic(adapters[0]));

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[0], true));
  }

  @Test
  void convertsAltMetafactoryBridgesToNormalFactoryCalls() throws Exception {
    JavaClass javaClass = altMetafactoryBridgeClass();
    GlobalConstantPool.init();

    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void generatesAdapterClassForReferenceReturnAdaptation() throws Exception {
    JavaClass javaClass = referenceReturnAdaptationClass();

    JavaClass[] adapters = Java8LambdaLowering.generateAdapterClasses(javaClass);

    assertEquals(1, adapters.length);
    assertEquals("fixtures/CompiledJava8ReferenceReturnAdaptation$$TC$$Lambda$0", adapters[0].className);
    assertTrue(hasInterface(adapters[0], "fixtures/CompiledJava8ReferenceReturnAdaptation$ObjectFactory"));
    assertTrue(hasMethod(adapters[0], "get", "get()", "Ljava/lang/Object;"));
    assertTrue(hasMethod(adapters[0], "$$tc_lambda_factory$0", "$$tc_lambda_factory$0()"));
    assertFalse(hasInvokeDynamic(adapters[0]));

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[0], true));
  }

  @Test
  void convertsReferenceReturnAdaptationToNormalFactoryCall() throws Exception {
    JavaClass javaClass = referenceReturnAdaptationClass();
    GlobalConstantPool.init();

    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void generatesAdapterClassesForReferenceArgumentAdaptation() throws Exception {
    JavaClass javaClass = referenceArgumentAdaptationClass();

    JavaClass[] adapters = Java8LambdaLowering.generateAdapterClasses(javaClass);

    assertEquals(2, adapters.length);
    assertEquals("fixtures/CompiledJava8ReferenceArgumentAdaptation$$TC$$Lambda$0", adapters[0].className);
    assertTrue(hasInterface(adapters[0], "fixtures/CompiledJava8ReferenceArgumentAdaptation$ValueMapper"));
    assertTrue(hasMethod(adapters[0], "map", "map(Ljava/lang/Object;)", "Ljava/lang/String;"));
    assertTrue(hasCheckCast(adapters[0]));
    assertFalse(hasInvokeDynamic(adapters[0]));

    assertEquals("fixtures/CompiledJava8ReferenceArgumentAdaptation$$TC$$Lambda$1", adapters[1].className);
    assertTrue(hasInterface(adapters[1], "fixtures/CompiledJava8ReferenceArgumentAdaptation$ValueMapper"));
    assertTrue(hasMethod(adapters[1], "map", "map(Ljava/lang/Object;)", "Ljava/lang/String;"));
    assertTrue(hasCheckCast(adapters[1]));
    assertFalse(hasInvokeDynamic(adapters[1]));

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[0], true));
    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[1], true));
  }

  @Test
  void convertsReferenceArgumentAdaptationToNormalFactoryCalls() throws Exception {
    JavaClass javaClass = referenceArgumentAdaptationClass();
    GlobalConstantPool.init();

    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void generatesAdapterClassesForPrimitiveAdaptation() throws Exception {
    JavaClass javaClass = primitiveAdaptationClass();

    JavaClass[] adapters = Java8LambdaLowering.generateAdapterClasses(javaClass);

    assertEquals(2, adapters.length);
    assertEquals("fixtures/CompiledJava8PrimitiveAdaptation$$TC$$Lambda$0", adapters[0].className);
    assertTrue(hasMethod(adapters[0], "apply", "apply(Ljava/lang/Object;)", "Ljava/lang/Object;"));
    assertTrue(hasCheckCast(adapters[0]));
    assertTrue(hasMethodCall(adapters[0], "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
    assertFalse(hasInvokeDynamic(adapters[0]));

    assertEquals("fixtures/CompiledJava8PrimitiveAdaptation$$TC$$Lambda$1", adapters[1].className);
    assertTrue(hasMethod(adapters[1], "apply", "apply(Ljava/lang/Object;)", "Ljava/lang/Object;"));
    assertTrue(hasCheckCast(adapters[1]));
    assertTrue(hasMethodCall(adapters[1], "java/lang/Integer", "intValue", "()I"));
    assertTrue(hasMethodCall(adapters[1], "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
    assertFalse(hasInvokeDynamic(adapters[1]));

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[0], true));
    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(adapters[1], true));
  }

  @Test
  void convertsPrimitiveAdaptationToNormalFactoryCalls() throws Exception {
    JavaClass javaClass = primitiveAdaptationClass();
    GlobalConstantPool.init();

    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  private JavaClass statelessLambdaClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava8StatelessLambdaFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");
    return new JavaClass(fixture.get().bytes, false);
  }

  private JavaClass capturedLambdaClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava8LambdaFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");
    return new JavaClass(fixture.get().bytes, false);
  }

  private JavaClass methodReferenceClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava8MethodReferenceFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");
    return new JavaClass(fixture.get().bytes, false);
  }

  private JavaClass constructorReferenceClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture =
        ModernJavaClassFileFixtures.compileJava8ConstructorReferenceFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");
    return new JavaClass(fixture.get().bytes, false);
  }

  private JavaClass altMetafactoryMarkerClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture =
        ModernJavaClassFileFixtures.compileJava8AltMetafactoryMarkerFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");
    return new JavaClass(fixture.get().bytes, false);
  }

  private JavaClass altMetafactoryBridgeClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture =
        ModernJavaClassFileFixtures.compileJava8AltMetafactoryBridgeFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");
    return new JavaClass(fixture.get().bytes, false);
  }

  private JavaClass referenceReturnAdaptationClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture =
        ModernJavaClassFileFixtures.compileJava8ReferenceReturnAdaptationFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");
    return new JavaClass(fixture.get().bytes, false);
  }

  private JavaClass referenceArgumentAdaptationClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture =
        ModernJavaClassFileFixtures.compileJava8ReferenceArgumentAdaptationFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");
    return new JavaClass(fixture.get().bytes, false);
  }

  private JavaClass primitiveAdaptationClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture =
        ModernJavaClassFileFixtures.compileJava8PrimitiveAdaptationFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");
    return new JavaClass(fixture.get().bytes, false);
  }

  private static boolean hasMethod(JavaClass javaClass, String name, String signature) {
    for (int i = 0; i < javaClass.methods.length; i++) {
      JavaMethod method = javaClass.methods[i];
      if (name.equals(method.name) && signature.equals(method.signature)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasMethod(JavaClass javaClass, String name, String signature, String ret) {
    for (int i = 0; i < javaClass.methods.length; i++) {
      JavaMethod method = javaClass.methods[i];
      if (name.equals(method.name) && signature.equals(method.signature) && ret.equals(method.ret)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasField(JavaClass javaClass, String name, String type) {
    for (int i = 0; i < javaClass.fields.length; i++) {
      JavaField field = javaClass.fields[i];
      if (name.equals(field.name) && type.equals(field.type)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasInterface(JavaClass javaClass, String name) {
    for (int i = 0; i < javaClass.interfaces.length; i++) {
      if (name.equals(javaClass.interfaces[i])) {
        return true;
      }
    }
    return false;
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

  private static boolean hasCheckCast(JavaClass javaClass) {
    for (int i = 0; i < javaClass.methods.length; i++) {
      JavaMethod method = javaClass.methods[i];
      if (method.code == null || method.code.bcs == null) {
        continue;
      }
      for (int j = 0; j < method.code.bcs.length; j++) {
        if (method.code.bcs[j] instanceof BC192_checkcast) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean hasMethodCall(JavaClass javaClass, String className, String name, String parameters) {
    for (int i = 0; i < javaClass.methods.length; i++) {
      JavaMethod method = javaClass.methods[i];
      if (method.code == null || method.code.bcs == null) {
        continue;
      }
      for (int j = 0; j < method.code.bcs.length; j++) {
        if (method.code.bcs[j] instanceof MethodCall) {
          MethodCall call = (MethodCall) method.code.bcs[j];
          if (className.equals(call.className) && name.equals(call.name) && parameters.equals(call.parameters)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
