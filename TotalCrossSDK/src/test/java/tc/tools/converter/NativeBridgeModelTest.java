// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class NativeBridgeModelTest {
  @Test
  void rejectsMissingRequiredDeployArchive(@TempDir Path tempDir) {
    assertThrows(java.io.IOException.class,
        () -> NativeBridgeModel.fromPaths(List.of(tempDir.resolve("missing.jar"))));
  }

  @Test
  void detectsCollisionAfterThirtyTwoCharacterTruncation(@TempDir Path tempDir) throws Exception {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "totalcross/test/NativeBridgeFixture", null,
        "java/lang/Object", null);
    writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
        "collisionAfterThirtyTwoCharactersAlpha", "()V", null, null).visitEnd();
    writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
        "collisionAfterThirtyTwoCharactersBeta", "()V", null, null).visitEnd();
    writer.visitEnd();

    Path classFile = tempDir.resolve("totalcross/test/NativeBridgeFixture.class");
    Files.createDirectories(classFile.getParent());
    Files.write(classFile, writer.toByteArray());

    NativeBridgeModel.Result result = NativeBridgeModel.fromPaths(List.of(tempDir));
    assertEquals(2, result.entries.size());
    assertEquals(1, result.collisions.size());
    assertEquals(result.collisions.get(0).first.symbol, result.collisions.get(0).second.symbol);
  }

  @Test
  void readsPackagedDeviceClassesWithoutIncludingUnpackagedClasses(@TempDir Path tempDir)
      throws Exception {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "totalcross/test/Packaged", null,
        "java/lang/Object", null);
    writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
        "call", "()V", null, null).visitEnd();
    writer.visitEnd();

    Path archive = tempDir.resolve("tc.base.misc.jar");
    try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(archive))) {
      out.putNextEntry(new JarEntry("totalcross/test/Packaged.class"));
      out.write(writer.toByteArray());
      out.closeEntry();
    }
    NativeBridgeModel.Result result = NativeBridgeModel.fromPaths(List.of(archive));
    assertEquals(1, result.entries.size());
    assertEquals("totalcross/test/Packaged#call()V", result.entries.get(0).identity());
  }

  @Test
  void deprecatedGoogleMapsBooleanRouteRemainsJavaDelegation() throws Exception {
    try (java.io.InputStream stream = getClass().getClassLoader()
        .getResourceAsStream("totalcross/map/GoogleMaps.class")) {
      assertTrue(stream != null, "GoogleMaps must be compiled for bridge validation");
      List<NativeBridgeModel.Entry> entries = NativeBridgeModel.fromClassBytes(stream.readAllBytes());
      assertTrue(entries.stream().anyMatch(entry ->
          entry.identity().equals("totalcross/map/GoogleMaps#showRoute(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)Z")));
      assertTrue(entries.stream().noneMatch(entry ->
          entry.identity().equals("totalcross/map/GoogleMaps#showRoute(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)Z")));
    }
  }

  @Test
  void discoversNativeMethodFromCompiledReplacementClass() throws Exception {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "jdkcompat/lang/System4D", null,
        "java/lang/Object", null);
    MethodVisitor nativeMethod = writer.visitMethod(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
        "nanoTime", "()J", null, null);
    nativeMethod.visitEnd();
    writer.visitEnd();

    List<NativeBridgeModel.Entry> entries = NativeBridgeModel.fromClassBytes(writer.toByteArray());
    assertEquals(1, entries.size());
    assertEquals("java/lang/System#nanoTime()J", entries.get(0).identity());
    assertEquals("jlS_nanoTime", entries.get(0).symbol);
  }

  @Test
  void selectsReplacementClassesAndInnerClassesBeforeCollectingBridges(@TempDir Path tempDir)
      throws Exception {
    writeClass(tempDir, "totalcross/test/Replacement",
        fixtureClass("totalcross/test/Replacement", "legacyOnly"));
    writeClass(tempDir, "totalcross/test/Replacement4D",
        fixtureClass("totalcross/test/Replacement4D", "current4D"));
    writeClass(tempDir, "totalcross/test/Outer$Part",
        fixtureClass("totalcross/test/Outer$Part", "legacyInner"));
    writeClass(tempDir, "totalcross/test/Outer$Part4D",
        fixtureClass("totalcross/test/Outer$Part4D", "currentInner4D"));

    NativeBridgeModel.Result result = NativeBridgeModel.fromPaths(List.of(tempDir));
    assertEquals(List.of(
        "totalcross/test/Outer$Part#currentInner()V",
        "totalcross/test/Replacement#current()V"),
        result.entries.stream().map(NativeBridgeModel.Entry::identity).collect(java.util.stream.Collectors.toList()));
  }

  @Test
  void omitsBaseMethodReplacedByJava4DImplementation() throws Exception {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "totalcross/test/MethodReplacement", null,
        "java/lang/Object", null);
    writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
        "open", "()V", null, null).visitEnd();
    MethodVisitor replacement = writer.visitMethod(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "open4D", "()V", null, null);
    replacement.visitCode();
    replacement.visitInsn(Opcodes.RETURN);
    replacement.visitMaxs(0, 0);
    replacement.visitEnd();
    writer.visitEnd();

    assertTrue(NativeBridgeModel.fromClassBytes(writer.toByteArray()).isEmpty());
  }

  @Test
  void includesReplacedJavaMethodsInNativeBridgeModel() throws Exception {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "totalcross/test/JavaSeHelpers", null,
        "java/lang/Object", null);
    MethodVisitor helper = writer.visitMethod(
        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "helper", "()V", null, null);
    AnnotationVisitor replaced = helper.visitAnnotation(
        "Lcom/totalcross/annotations/ReplacedByNativeOnDeploy;", false);
    replaced.visitEnd();
    helper.visitCode();
    helper.visitInsn(Opcodes.RETURN);
    helper.visitMaxs(0, 0);
    helper.visitEnd();
    writer.visitEnd();

    List<NativeBridgeModel.Entry> entries = NativeBridgeModel.fromClassBytes(writer.toByteArray());

    assertEquals(1, entries.size());
    assertEquals("totalcross/test/JavaSeHelpers#helper()V", entries.get(0).identity());
  }

  private static byte[] fixtureClass(String name, String methodName) {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", null);
    writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
        methodName, "()V", null, null).visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static void writeClass(Path root, String className, byte[] bytes) throws Exception {
    Path file = root.resolve(className + ".class");
    Files.createDirectories(file.getParent());
    Files.write(file, bytes);
  }
}
