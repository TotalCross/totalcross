// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.tools.converter.TCConstants;
import tc.tools.converter.metadata.CompilationMetadata.CallSiteMetadata;
import tc.tools.converter.metadata.CompilationMetadata.ClassMetadata;
import tc.tools.converter.metadata.CompilationMetadata.FieldMetadata;
import tc.tools.converter.metadata.CompilationMetadata.InvokeKind;
import tc.tools.converter.metadata.CompilationMetadata.MethodMetadata;
import tc.tools.converter.metadata.CompilationMetadata.NativeKind;
import tc.tools.converter.metadata.CompilationMetadata.OriginRange;
import tc.tools.converter.metadata.CompilationMetadata.RecordComponentMetadata;
import tc.tools.converter.metadata.CompilationMetadata.SyntheticKind;
import tc.tools.converter.metadata.CompilationMetadata.SyntheticOrigin;
import tc.tools.converter.metadata.CompilationMetadata.VerificationFrame;
import tc.tools.converter.metadata.CompilationMetadata.VerificationType;
import tc.tools.converter.metadata.TcmFile.Artifact;

class TcmReaderWriterTest {
  @TempDir
  Path workDir;

  @Test
  void writesDeterministicRoundTripWithAllSemanticSections() throws Exception {
    List<Path> tczs = tczFiles();
    List<Artifact> artifacts = TcmWriter.artifacts(tczs);
    CompilationMetadata metadata = metadata();
    byte[] first = new TcmWriter(metadata, artifacts, "7.2.2.test").write();
    byte[] second = new TcmWriter(metadata, artifacts, "7.2.2.test").write();
    assertArrayEquals(first, second);

    TcmFile decoded = new TcmReader().read(first);
    assertEquals("7.2.2.test", decoded.buildIdentity);
    assertEquals(2, decoded.artifacts.size());
    assertEquals("Sample.tcz", decoded.artifacts.get(0).relativeName);
    ClassMetadata type = decoded.metadata.classes.get(0);
    MethodMetadata method = type.methods.get(0);
    assertEquals("F", method.sourceParameterDescriptors.get(0));
    assertEquals("&D", method.loweredParameterTypes.get(0));
    assertEquals(NativeKind.REPLACED_ON_DEPLOY, method.nativeKind);
    assertEquals(InvokeKind.INTERFACE, method.callSites.get(0).invokeKind);
    assertEquals(TCConstants.CALL_normal, method.callSites.get(0).loweredOpcode);
    assertEquals("java/util/Properties", method.callSites.get(1).symbolicOwner);
    assertEquals("java/util/Hashtable", method.callSites.get(1).resolvedDeclarationOwner);
    assertEquals("FLOAT", method.verificationFrames.get(0).locals.get(0).kind);
    assertEquals("DOUBLE", method.verificationFrames.get(0).stack.get(0).kind);
    assertEquals(4, method.origins.get(0).tcStartSlot);
    assertEquals(SyntheticKind.LAMBDA, type.syntheticOrigins.get(0).kind);
    assertTrue(decoded.metadata.resolvedClassForNameRoots.contains("fixtures/Target"));
    assertTrue(decoded.metadata.unresolvedDynamicClassLookup);
  }

  @Test
  void usesPermanentV1WireCodesAndRejectsUnknownValues() throws Exception {
    assertEquals(0, NativeKind.NONE.wireCode);
    assertEquals(1, NativeKind.JAVA_NATIVE.wireCode);
    assertEquals(2, NativeKind.REPLACED_ON_DEPLOY.wireCode);
    assertEquals(0, InvokeKind.STATIC.wireCode);
    assertEquals(1, InvokeKind.SPECIAL.wireCode);
    assertEquals(2, InvokeKind.INTERFACE.wireCode);
    assertEquals(3, InvokeKind.VIRTUAL.wireCode);
    assertEquals(4, InvokeKind.DYNAMIC_LAMBDA.wireCode);
    assertEquals(5, InvokeKind.DYNAMIC_STRING_CONCAT.wireCode);
    assertEquals(6, InvokeKind.DYNAMIC_RECORD.wireCode);
    assertEquals(0, SyntheticKind.LAMBDA.wireCode);
    assertEquals(1, SyntheticKind.STRING_CONCAT.wireCode);
    assertEquals(2, SyntheticKind.RECORD_OBJECT_METHOD.wireCode);

    byte[] encoded = bytes();
    int nativeCode = sectionPayloadOffset(encoded, TcmFormat.METHODS) + 56;
    int invokeCode = sectionPayloadOffset(encoded, TcmFormat.CALL_SITES) + 16;
    int syntheticCode = sectionPayloadOffset(encoded, TcmFormat.ALLOCATION_AND_SYNTHETIC_ORIGINS) + 8;
    assertEquals(2, encoded[nativeCode] & 0xff);
    assertEquals(2, encoded[invokeCode] & 0xff);
    assertEquals(0, encoded[syntheticCode] & 0xff);

    assertInvalidWireCode(encoded, nativeCode, "invalid native kind 99");
    assertInvalidWireCode(encoded, invokeCode, "invalid invoke kind 99");
    assertInvalidWireCode(encoded, syntheticCode, "invalid synthetic kind 99");
  }

  @Test
  void readsFrozenMilestoneZeroV1FixtureWhenProvided() throws Exception {
    String fixtureDirectory = System.getenv("TCM_V1_FIXTURE_DIR");
    assumeTrue(fixtureDirectory != null, "Set TCM_V1_FIXTURE_DIR during compatibility validation");
    Path directory = Path.of(fixtureDirectory);
    TcmFile decoded = new TcmReader().read(directory.resolve("FeatureSmokeApp.tcm"),
        Collections.singletonList(directory.resolve("FeatureSmokeApp.tcz")));
    assertEquals(87, decoded.metadata.classes.size());
    assertEquals(10, sectionCount(Files.readAllBytes(directory.resolve("FeatureSmokeApp.tcm"))));
  }

  @Test
  void publishesAtomicallyAndValidatesArtifactHashes() throws Exception {
    List<Path> tczs = tczFiles();
    Path sidecar = TcmWriter.publishForTczs(tczs, metadata());
    assertEquals("Sample.tcm", sidecar.getFileName().toString());
    assertFalse(Files.exists(sidecar.resolveSibling("Sample.tcm.tmp")));
    assertEquals(2, new TcmReader().read(sidecar, tczs).artifacts.size());

    Files.write(tczs.get(1), new byte[] { 9, 9, 9 });
    IllegalArgumentException mismatch = assertThrows(IllegalArgumentException.class,
        () -> new TcmReader().read(sidecar, tczs));
    assertTrue(mismatch.getMessage().contains("SHA-256 mismatch"));
  }

  @Test
  void skipsUnknownOptionalSectionButRejectsUnknownRequiredSection() throws Exception {
    byte[] base = bytes();
    assertEquals(1, new TcmReader().read(withSection(base, 77, new byte[] { 1, 2, 3 })).metadata.classes.size());
    IllegalArgumentException required = assertThrows(IllegalArgumentException.class,
        () -> new TcmReader().read(withSection(base, TcmFormat.REQUIRED | 77, new byte[0])));
    assertTrue(required.getMessage().contains("unknown required section 77"));
  }

  @Test
  void reportsTruncationAndUnsupportedMajorVersionPrecisely() throws Exception {
    byte[] bytes = bytes();
    IllegalArgumentException truncated = assertThrows(IllegalArgumentException.class,
        () -> new TcmReader().read(Arrays.copyOf(bytes, bytes.length - 1)));
    assertTrue(truncated.getMessage().contains("truncated input"));

    bytes[4] = 2;
    IllegalArgumentException major = assertThrows(IllegalArgumentException.class, () -> new TcmReader().read(bytes));
    assertTrue(major.getMessage().contains("unsupported required major version 2"));
  }

  private byte[] bytes() throws Exception {
    return new TcmWriter(metadata(), TcmWriter.artifacts(tczFiles()), "7.2.2.test").write();
  }

  private List<Path> tczFiles() throws Exception {
    Path primary = workDir.resolve("Sample.tcz");
    Path split = workDir.resolve("Sample_1.tcz");
    Files.write(primary, new byte[] { 1, 2, 3 });
    Files.write(split, new byte[] { 4, 5, 6 });
    return Arrays.asList(primary, split);
  }

  private static byte[] withSection(byte[] source, int type, byte[] payload) {
    byte[] result = Arrays.copyOf(source, source.length + 8 + payload.length);
    putI32(result, 12, getI32(result, 12) + 1);
    int offset = source.length;
    putU16(result, offset, type);
    putU16(result, offset + 2, 1);
    putI32(result, offset + 4, payload.length);
    System.arraycopy(payload, 0, result, offset + 8, payload.length);
    return result;
  }

  private static void assertInvalidWireCode(byte[] source, int offset, String message) {
    byte[] changed = source.clone();
    changed[offset] = 99;
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> new TcmReader().read(changed));
    assertTrue(error.getMessage().contains(message));
  }

  private static int sectionPayloadOffset(byte[] bytes, int expectedType) {
    int offset = 16;
    int count = sectionCount(bytes);
    for (int i = 0; i < count; i++) {
      int type = getU16(bytes, offset) & ~TcmFormat.REQUIRED;
      int length = getI32(bytes, offset + 4);
      if (type == expectedType) return offset + 8;
      offset += 8 + length;
    }
    throw new AssertionError("Missing section " + expectedType);
  }

  private static int sectionCount(byte[] bytes) {
    return getI32(bytes, 12);
  }

  private static int getU16(byte[] bytes, int offset) {
    return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
  }

  private static int getI32(byte[] bytes, int offset) {
    return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8)
        | ((bytes[offset + 2] & 0xFF) << 16) | ((bytes[offset + 3] & 0xFF) << 24);
  }

  private static void putI32(byte[] bytes, int offset, int value) {
    bytes[offset] = (byte) value;
    bytes[offset + 1] = (byte) (value >>> 8);
    bytes[offset + 2] = (byte) (value >>> 16);
    bytes[offset + 3] = (byte) (value >>> 24);
  }

  private static void putU16(byte[] bytes, int offset, int value) {
    bytes[offset] = (byte) value;
    bytes[offset + 1] = (byte) (value >>> 8);
  }

  private static CompilationMetadata metadata() {
    List<VerificationType> locals = Arrays.asList(new VerificationType("FLOAT", null, -1));
    List<VerificationType> stack = Arrays.asList(new VerificationType("DOUBLE", null, -1));
    List<VerificationFrame> frames = Arrays.asList(new VerificationFrame(8, locals, stack));
    List<CallSiteMetadata> calls = Arrays.asList(
        new CallSiteMetadata(2, 185, InvokeKind.INTERFACE, "java/util/List", "size", "()I", "java/util/List",
            TCConstants.CALL_normal, 1, 3),
        new CallSiteMetadata(5, 182, InvokeKind.VIRTUAL, "java/util/Properties", "put",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "java/util/Hashtable",
            TCConstants.CALL_virtual, 3, 4));
    List<OriginRange> origins = Arrays.asList(new OriginRange(8, 187, 4, 5, "java/lang/StringBuilder"));
    MethodMetadata method = new MethodMetadata("fixtures/Sample4D", "fixtures/Sample", "run4D", "run",
        "(F)D", Arrays.asList("F"), "D", Arrays.asList("&D"), "&D", 0x109,
        NativeKind.REPLACED_ON_DEPLOY, 12, frames, calls, origins);
    FieldMetadata field = new FieldMetadata("fixtures/Sample4D", "fixtures/Sample", "VALUE", "Ljava/lang/String;",
        "Ljava/lang/String;", 0x19, "constant", 4);
    SyntheticOrigin lambda = new SyntheticOrigin(SyntheticKind.LAMBDA, "fixtures/Sample4D", "(F)D", 10,
        "fixtures/Sample$$TC$$Lambda$0", "$$tc_lambda_factory$0", "()V", 6, "fixtures/Sample4D", "lambda$run$0",
        "()V", Arrays.asList("F"));
    ClassMetadata type = new ClassMetadata("fixtures/Sample4D", "fixtures/Sample", 0x21, "java/lang/Object",
        "java/lang/Object", new ArrayList<String>(), new ArrayList<String>(), "Sample.java", null, null,
        new ArrayList<String>(), new ArrayList<String>(),
        Arrays.asList(new RecordComponentMetadata("value", "F")), Arrays.asList(field), Arrays.asList(method),
        Arrays.asList(lambda));
    return new CompilationMetadata(Arrays.asList(type), Arrays.asList("fixtures/Target"), true);
  }
}
