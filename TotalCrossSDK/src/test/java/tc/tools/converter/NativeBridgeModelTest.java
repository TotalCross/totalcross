// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class NativeBridgeModelTest {
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
}
