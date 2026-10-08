// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class DeployedJavaApiValidatorTest {
  @Test
  void acceptsMethodPresentInDeviceModel() throws Exception {
    List<DeployedJavaApiValidator.Problem> problems =
        DeployedJavaApiValidator.validateClassBytes(caller("java/lang/System", "nanoTime", "()J", Opcodes.POP2));
    assertTrue(problems.isEmpty());
  }

  @Test
  void rejectsHostOnlyMethodBeforeDeploy() throws Exception {
    List<DeployedJavaApiValidator.Problem> problems =
        DeployedJavaApiValidator.validateClassBytes(caller("java/lang/Thread", "onSpinWait", "()V", -1));
    assertEquals(1, problems.size());
    assertEquals("java/lang/Thread", problems.get(0).owner);
    assertEquals("onSpinWait", problems.get(0).name);
    assertTrue(problems.get(0).deviceClassFound);
  }

  @Test
  void rejectsWeakReferenceQueueConstructorFromAudit() throws Exception {
    List<DeployedJavaApiValidator.Problem> problems =
        DeployedJavaApiValidator.validateClassBytes(weakReferenceQueueConstructorCaller());
    assertEquals(1, problems.size());
    assertEquals("java/lang/ref/WeakReference", problems.get(0).owner);
    assertEquals("<init>", problems.get(0).name);
    assertTrue(problems.get(0).deviceClassFound);
  }

  @Test
  void rejectsReflectionSetAccessibleFromAudit() throws Exception {
    List<DeployedJavaApiValidator.Problem> problems =
        DeployedJavaApiValidator.validateClassBytes(reflectionSetAccessibleCaller());
    assertEquals(1, problems.size());
    assertEquals("java/lang/reflect/AccessibleObject", problems.get(0).owner);
    assertEquals("setAccessible", problems.get(0).name);
  }

  @Test
  void acceptsConverterMappedStringBuilder() throws Exception {
    List<DeployedJavaApiValidator.Problem> problems =
        DeployedJavaApiValidator.validateClassBytes(
            caller("java/lang/StringBuilder", "<init>", "()V", -1));
    assertTrue(problems.isEmpty());
  }

  @Test
  void ignoresBodyReplacedByNativeOnDeploy() throws Exception {
    List<DeployedJavaApiValidator.Problem> problems =
        DeployedJavaApiValidator.validateClassBytes(replacedNativeCaller());
    assertTrue(problems.isEmpty());
  }

  @Test
  void resolvesCompiledDeviceModelWithoutClassLoader(@TempDir Path tempDir) throws Exception {
    writeClass(tempDir, "totalcross/lang/SyntheticApi4D",
        methodOwnerClass("totalcross/lang/SyntheticApi4D", "available"));
    writeClass(tempDir, "totalcross/test/DeviceModelCaller",
        callerClass("totalcross/test/DeviceModelCaller",
            "java/lang/SyntheticApi", "available", "()V", -1));

    List<DeployedJavaApiValidator.Problem> problems =
        DeployedJavaApiValidator.validatePaths(List.of(tempDir));
    assertTrue(problems.isEmpty());
  }

  @Test
  void usesNested4DReplacementInsteadOfJavaSeNestedClass(@TempDir Path tempDir)
      throws Exception {
    writeClass(tempDir, "totalcross/ui/ImageFixture$Loader",
        callerClass("totalcross/ui/ImageFixture$Loader",
            "javax/imageio/ImageIO", "getImageReaders",
            "(Ljava/lang/Object;)Ljava/util/Iterator;", Opcodes.POP));
    writeClass(tempDir, "totalcross/ui/ImageFixture$Loader4D",
        callerClass("totalcross/ui/ImageFixture$Loader4D",
            "java/lang/System", "nanoTime", "()J", Opcodes.POP2));

    List<DeployedJavaApiValidator.Problem> problems =
        DeployedJavaApiValidator.validatePaths(List.of(tempDir));
    assertTrue(problems.isEmpty());
  }

  @Test
  void uses4DReplacementInsteadOfHostClass(@TempDir Path tempDir) throws Exception {
    writeClass(tempDir, "totalcross/io/TestPort",
        callerClass("totalcross/io/TestPort", "java/util/ResourceBundle", "getBundle",
            "(Ljava/lang/String;)Ljava/util/ResourceBundle;", Opcodes.POP));
    writeClass(tempDir, "totalcross/io/TestPort4D",
        callerClass("totalcross/io/TestPort4D", "java/lang/System", "nanoTime", "()J", Opcodes.POP2));

    List<DeployedJavaApiValidator.Problem> problems =
        DeployedJavaApiValidator.validatePaths(List.of(tempDir));
    assertTrue(problems.isEmpty());
  }

  @Test
  void ignoresJavaSeMethodWhenSibling4DMethodExists() throws Exception {
    List<DeployedJavaApiValidator.Problem> problems =
        DeployedJavaApiValidator.validateClassBytes(method4DReplacementClass());
    assertTrue(problems.isEmpty());
  }

  @Test
  void selectsHostAndReplacementCandidatesBeforeDeployFiltering() {
    assertTrue(DeployedJavaApiValidator.isDeviceModelCandidate("totalcross/ui/MainWindow.class"));
    assertTrue(DeployedJavaApiValidator.isDeviceModelCandidate("totalcross/io/File4D.class"));
    assertTrue(DeployedJavaApiValidator.isDeviceModelCandidate("jdkcompat/lang/System4D.class"));
    assertTrue(!DeployedJavaApiValidator.isDeviceModelCandidate("tc/tools/Foo.class"));
    assertTrue(DeployedJavaApiValidator.isSdkCallerClass("totalcross/ui/MainWindow"));
    assertTrue(!DeployedJavaApiValidator.isSdkCallerClass("jdkcompat/lang/System4D"));
    assertTrue(!DeployedJavaApiValidator.isSdkCallerClass("totalcross/Launcher"));
  }

  private static byte[] method4DReplacementClass() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "totalcross/test/MethodReplacement", null,
        "java/lang/Object", null);

    MethodVisitor host = writer.visitMethod(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "initialize", "()V", null, null);
    host.visitCode();
    host.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Calendar", "getInstance",
        "()Ljava/util/Calendar;", false);
    host.visitInsn(Opcodes.POP);
    finish(host, writer, false);

    MethodVisitor device = writer.visitMethod(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "initialize4D", "()V", null, null);
    device.visitCode();
    device.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
    device.visitInsn(Opcodes.POP2);
    finish(device, writer, true);
    return writer.toByteArray();
  }

  private static byte[] replacedNativeCaller() {
    ClassWriter writer = classWriter();
    MethodVisitor method = writer.visitMethod(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "call", "()V", null, null);
    AnnotationVisitor annotation = method.visitAnnotation(
        "Lcom/totalcross/annotations/ReplacedByNativeOnDeploy;", false);
    annotation.visitEnd();
    method.visitCode();
    method.visitInsn(Opcodes.ACONST_NULL);
    method.visitInsn(Opcodes.ICONST_1);
    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/AccessibleObject",
        "setAccessible", "(Z)V", false);
    finish(method, writer);
    return writer.toByteArray();
  }

  private static byte[] methodOwnerClass(String className, String methodName) {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
    MethodVisitor method = writer.visitMethod(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodName, "()V", null, null);
    method.visitCode();
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static byte[] callerClass(String className, String owner, String name,
      String descriptor, int resultOpcode) {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
    MethodVisitor method = writer.visitMethod(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "call", "()V", null, null);
    method.visitCode();
    if ("<init>".equals(name)) {
      method.visitTypeInsn(Opcodes.NEW, owner);
      method.visitInsn(Opcodes.DUP);
      method.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, descriptor, false);
    } else {
      if (descriptor.startsWith("(Ljava/lang/String;)")) {
        method.visitInsn(Opcodes.ACONST_NULL);
      }
      method.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, descriptor, false);
    }
    if (resultOpcode >= 0) {
      method.visitInsn(resultOpcode);
    }
    finish(method, writer);
    return writer.toByteArray();
  }

  private static void writeClass(Path root, String className, byte[] bytes) throws Exception {
    Path file = root.resolve(className + ".class");
    Files.createDirectories(file.getParent());
    Files.write(file, bytes);
  }

  private static byte[] weakReferenceQueueConstructorCaller() {
    ClassWriter writer = classWriter();
    MethodVisitor method = method(writer);
    method.visitTypeInsn(Opcodes.NEW, "java/lang/ref/WeakReference");
    method.visitInsn(Opcodes.DUP);
    method.visitInsn(Opcodes.ACONST_NULL);
    method.visitInsn(Opcodes.ACONST_NULL);
    method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ref/WeakReference",
        "<init>", "(Ljava/lang/Object;Ljava/lang/ref/ReferenceQueue;)V", false);
    method.visitInsn(Opcodes.POP);
    finish(method, writer);
    return writer.toByteArray();
  }

  private static byte[] reflectionSetAccessibleCaller() {
    ClassWriter writer = classWriter();
    MethodVisitor method = method(writer);
    method.visitInsn(Opcodes.ACONST_NULL);
    method.visitInsn(Opcodes.ICONST_1);
    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/AccessibleObject",
        "setAccessible", "(Z)V", false);
    finish(method, writer);
    return writer.toByteArray();
  }

  private static byte[] caller(String owner, String name, String descriptor, int resultOpcode) {
    ClassWriter writer = classWriter();
    MethodVisitor method = method(writer);
    method.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, descriptor, false);
    if (resultOpcode >= 0) {
      method.visitInsn(resultOpcode);
    }
    finish(method, writer);
    return writer.toByteArray();
  }

  private static ClassWriter classWriter() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "fixtures/CompatibilityCaller", null,
        "java/lang/Object", null);
    return writer;
  }

  private static MethodVisitor method(ClassWriter writer) {
    MethodVisitor method = writer.visitMethod(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "call", "()V", null, null);
    method.visitCode();
    return method;
  }

  private static void finish(MethodVisitor method, ClassWriter writer) {
    finish(method, writer, true);
  }

  private static void finish(
      MethodVisitor method, ClassWriter writer, boolean finishWriter) {
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
    if (finishWriter) {
      writer.visitEnd();
    }
  }
}
