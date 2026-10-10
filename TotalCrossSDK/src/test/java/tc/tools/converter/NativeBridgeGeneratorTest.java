// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

class NativeBridgeGeneratorTest {
  @Test
  void excludesOptionalSyncEntriesFromCoreArtifacts(@TempDir Path tempDir) throws Exception {
    Path classes = tempDir.resolve("classes");
    writeNativeClass(classes, "totalcross/io/sync/SyncFixture", "syncCall");
    writeNativeClass(classes, "totalcross/io/CoreFixture", "coreCall");
    Path compatibilityFile = tempDir.resolve("native-bridge-compat.txt");
    Files.writeString(compatibilityFile, "module\tsync\ttotalcross/io/sync/\n");

    Path generated = tempDir.resolve("generated");
    NativeBridgeGenerator.main(new String[] {
        "--strict", "--output", generated.toString(), "--compat", compatibilityFile.toString(), classes.toString()
    });

    NativeBridgeModel.Result model = NativeBridgeModel.fromPaths(java.util.List.of(classes));
    String registrations = Files.readString(generated.resolve("nativeProcAddressesTC.generated.inc"));
    String header = Files.readString(generated.resolve("NativeMethods.generated.h"));
    String manifest = Files.readString(generated.resolve("native-bridges.json"));
    String syncSymbol = model.entries.stream().filter(entry -> entry.sourceOwner.startsWith("totalcross/io/sync/"))
        .map(entry -> entry.symbol).findFirst().orElseThrow();
    String coreSymbol = model.entries.stream().filter(entry -> entry.sourceOwner.startsWith("totalcross/io/Core"))
        .map(entry -> entry.symbol).findFirst().orElseThrow();

    assertTrue(!registrations.contains(syncSymbol));
    assertTrue(!header.contains(syncSymbol));
    assertTrue(registrations.contains(coreSymbol));
    assertTrue(manifest.contains("\"module\":\"sync\""));
  }

  @Test
  void strictGenerationRejectsThirtyTwoCharacterCollision(@TempDir Path tempDir) throws Exception {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "totalcross/test/NativeBridgeFixture", null,
        "java/lang/Object", null);
    writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
        "collisionAfterThirtyTwoCharactersAlpha", "()V", null, null).visitEnd();
    writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
        "collisionAfterThirtyTwoCharactersBeta", "()V", null, null).visitEnd();
    writer.visitEnd();

    Path classes = tempDir.resolve("classes");
    Path classFile = classes.resolve("totalcross/test/NativeBridgeFixture.class");
    Files.createDirectories(classFile.getParent());
    Files.write(classFile, writer.toByteArray());

    IllegalStateException failure = assertThrows(IllegalStateException.class,
        () -> NativeBridgeGenerator.main(new String[] {
            "--strict",
            "--output", tempDir.resolve("generated").toString(),
            classes.toString()
        }));
    assertTrue(failure.getMessage().contains("collision"));
  }

  private static void writeNativeClass(Path root, String owner, String methodName) throws Exception {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, owner, null, "java/lang/Object", null);
    writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
        methodName, "()V", null, null).visitEnd();
    writer.visitEnd();
    Path file = root.resolve(owner + ".class");
    Files.createDirectories(file.getParent());
    Files.write(file, writer.toByteArray());
  }
}
