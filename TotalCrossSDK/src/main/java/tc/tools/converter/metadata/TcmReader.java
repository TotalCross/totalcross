// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public final class TcmReader {
  private List<String> strings;

  public TcmFile read(byte[] bytes) {
    TcmBinary.Input input = new TcmBinary.Input(bytes);
    if (!Arrays.equals(TcmFormat.MAGIC, input.bytes(4))) {
      throw input.error("bad magic; expected TCM1");
    }
    int major = input.u16();
    int minor = input.u16();
    if (major != TcmFormat.MAJOR_VERSION) {
      throw input.error("unsupported required major version " + major);
    }
    input.i32(); // flags reserved for v1
    int sectionCount = input.count("section count");
    if (sectionCount > 65535) {
      throw input.error("section count exceeds 65535");
    }
    Map<Integer, byte[]> sections = new HashMap<Integer, byte[]>();
    for (int i = 0; i < sectionCount; i++) {
      int encodedType = input.u16();
      int version = input.u16();
      int length = input.count("section length");
      boolean required = (encodedType & TcmFormat.REQUIRED) != 0;
      int type = encodedType & ~TcmFormat.REQUIRED;
      byte[] payload = input.bytes(length);
      if (!known(type)) {
        if (required) {
          throw input.error("unknown required section " + type);
        }
        continue;
      }
      if (version != TcmFormat.SECTION_VERSION) {
        if (required) {
          throw input.error("unsupported required section version " + type + "." + version);
        }
        continue;
      }
      if (sections.put(Integer.valueOf(type), payload) != null) {
        throw input.error("duplicate section " + type);
      }
    }
    input.requireFullyRead("TCM file");
    byte[] stringSection = required(sections, TcmFormat.STRING_TABLE, input);
    byte[] manifestSection = required(sections, TcmFormat.ARTIFACT_MANIFEST, input);
    strings = readStringTable(stringSection);
    Manifest manifest = readManifest(manifestSection);
    List<ClassData> classes = readClasses(sections.get(Integer.valueOf(TcmFormat.CLASSES)));
    readFields(sections.get(Integer.valueOf(TcmFormat.FIELDS)), classes);
    List<MethodData> methods = readMethods(sections.get(Integer.valueOf(TcmFormat.METHODS)), classes);
    readCalls(sections.get(Integer.valueOf(TcmFormat.CALL_SITES)), methods);
    readOrigins(sections.get(Integer.valueOf(TcmFormat.ORIGIN_MAP)), methods);
    readSynthetic(sections.get(Integer.valueOf(TcmFormat.ALLOCATION_AND_SYNTHETIC_ORIGINS)), classes);
    DynamicData dynamic = readDynamic(sections.get(Integer.valueOf(TcmFormat.DYNAMIC_ACCESS)));
    readFrames(sections.get(Integer.valueOf(TcmFormat.TYPE_FRAMES)), methods);
    return new TcmFile(manifest.buildIdentity, manifest.artifacts,
        new CompilationMetadata(buildClasses(classes), dynamic.roots, dynamic.unresolved));
  }

  public TcmFile read(Path sidecar, List<Path> tczPaths) throws IOException {
    TcmFile file = read(Files.readAllBytes(sidecar));
    List<Artifact> actual = TcmWriter.artifacts(tczPaths);
    if (file.artifacts.size() != actual.size()) {
      throw new IllegalArgumentException("TCM artifact count mismatch: expected " + file.artifacts.size()
          + ", received " + actual.size());
    }
    for (int i = 0; i < actual.size(); i++) {
      Artifact expected = file.artifacts.get(i);
      Artifact supplied = actual.get(i);
      if (!expected.relativeName.equals(supplied.relativeName)) {
        throw new IllegalArgumentException("TCM artifact name mismatch at index " + i + ": expected "
            + expected.relativeName + ", received " + supplied.relativeName);
      }
      if (!MessageDigest.isEqual(expected.sha256, supplied.sha256)) {
        throw new IllegalArgumentException("TCM SHA-256 mismatch for " + expected.relativeName);
      }
    }
    return file;
  }

  private List<String> readStringTable(byte[] payload) {
    TcmBinary.Input in = new TcmBinary.Input(payload);
    int count = in.count("string count");
    List<String> result = new ArrayList<String>(count);
    String previous = null;
    for (int i = 0; i < count; i++) {
      String value = in.utf8();
      if (previous != null && previous.compareTo(value) >= 0) {
        throw in.error("string table is not strictly sorted");
      }
      result.add(value);
      previous = value;
    }
    in.requireFullyRead("string table");
    return result;
  }

  private Manifest readManifest(byte[] payload) {
    TcmBinary.Input in = new TcmBinary.Input(payload);
    String buildIdentity = string(in);
    int count = in.count("artifact count");
    List<Artifact> artifacts = new ArrayList<Artifact>(count);
    for (int i = 0; i < count; i++) {
      artifacts.add(new Artifact(string(in), in.bytes(32)));
    }
    in.requireFullyRead("artifact manifest");
    return new Manifest(buildIdentity, artifacts);
  }

  private List<ClassData> readClasses(byte[] payload) {
    List<ClassData> classes = new ArrayList<ClassData>();
    if (payload == null) return classes;
    TcmBinary.Input in = new TcmBinary.Input(payload);
    int count = in.count("class count");
    for (int i = 0; i < count; i++) {
      ClassData type = new ClassData();
      type.originalName = string(in); type.effectiveName = string(in); type.rawAccessFlags = in.i32();
      type.originalSuperName = string(in); type.effectiveSuperName = string(in);
      type.originalInterfaces = strings(in); type.effectiveInterfaces = strings(in);
      type.sourceFile = string(in); type.signature = string(in); type.nestHost = string(in);
      type.nestMembers = strings(in); type.permittedSubclasses = strings(in);
      int components = in.count("record component count");
      for (int j = 0; j < components; j++) {
        type.recordComponents.add(new RecordComponentMetadata(string(in), string(in)));
      }
      classes.add(type);
    }
    in.requireFullyRead("classes section");
    return classes;
  }

  private void readFields(byte[] payload, List<ClassData> classes) {
    if (payload == null) return;
    TcmBinary.Input in = new TcmBinary.Input(payload);
    int count = in.count("field count");
    for (int i = 0; i < count; i++) {
      ClassData owner = index(classes, in.i32(), "field class");
      owner.fields.add(new FieldMetadata(string(in), string(in), string(in), string(in), string(in), in.i32(),
          constant(in), in.i32()));
    }
    in.requireFullyRead("fields section");
  }

  private List<MethodData> readMethods(byte[] payload, List<ClassData> classes) {
    List<MethodData> methods = new ArrayList<MethodData>();
    if (payload == null) return methods;
    TcmBinary.Input in = new TcmBinary.Input(payload);
    int count = in.count("method count");
    for (int i = 0; i < count; i++) {
      ClassData owner = index(classes, in.i32(), "method class");
      MethodData method = new MethodData();
      method.originalOwner = string(in); method.effectiveOwner = string(in);
      method.originalName = string(in); method.effectiveName = string(in); method.descriptor = string(in);
      method.sourceParameters = strings(in); method.sourceReturn = string(in);
      method.loweredParameters = strings(in); method.loweredReturn = string(in);
      method.rawAccessFlags = in.i32(); method.nativeKind = enumValue(NativeKind.values(), in.u8(), in, "native kind");
      method.tcMethodNameSymbol = in.i32();
      owner.methods.add(method); methods.add(method);
    }
    in.requireFullyRead("methods section");
    return methods;
  }

  private void readCalls(byte[] payload, List<MethodData> methods) {
    if (payload == null) return;
    TcmBinary.Input in = new TcmBinary.Input(payload);
    int count = in.count("call count");
    for (int i = 0; i < count; i++) {
      MethodData method = index(methods, in.i32(), "call method");
      int javaPc = in.i32(); int opcode = in.i32();
      InvokeKind kind = enumValue(InvokeKind.values(), in.u8(), in, "invoke kind");
      method.calls.add(new CallSiteMetadata(javaPc, opcode, kind, string(in), string(in), string(in), string(in),
          in.i32(), in.i32(), in.i32()));
    }
    in.requireFullyRead("call-sites section");
  }

  private void readOrigins(byte[] payload, List<MethodData> methods) {
    if (payload == null) return;
    TcmBinary.Input in = new TcmBinary.Input(payload);
    int count = in.count("origin count");
    for (int i = 0; i < count; i++) {
      MethodData method = index(methods, in.i32(), "origin method");
      method.origins.add(new OriginRange(in.i32(), in.i32(), in.i32(), in.i32(), string(in)));
    }
    in.requireFullyRead("origin-map section");
  }

  private void readSynthetic(byte[] payload, List<ClassData> classes) {
    if (payload == null) return;
    TcmBinary.Input in = new TcmBinary.Input(payload);
    int count = in.count("synthetic-origin count");
    for (int i = 0; i < count; i++) {
      ClassData owner = index(classes, in.i32(), "synthetic class");
      SyntheticKind kind = enumValue(SyntheticKind.values(), in.u8(), in, "synthetic kind");
      owner.synthetic.add(new SyntheticOrigin(kind, string(in), string(in), in.i32(), string(in), string(in),
          string(in), in.i32(), string(in), string(in), string(in), strings(in)));
    }
    in.requireFullyRead("synthetic-origins section");
  }

  private DynamicData readDynamic(byte[] payload) {
    if (payload == null) return new DynamicData(new ArrayList<String>(), false);
    TcmBinary.Input in = new TcmBinary.Input(payload);
    List<String> roots = strings(in);
    boolean unresolved = in.u8() != 0;
    in.requireFullyRead("dynamic-access section");
    return new DynamicData(roots, unresolved);
  }

  private void readFrames(byte[] payload, List<MethodData> methods) {
    if (payload == null) return;
    TcmBinary.Input in = new TcmBinary.Input(payload);
    int count = in.count("frame count");
    for (int i = 0; i < count; i++) {
      MethodData method = index(methods, in.i32(), "frame method");
      method.frames.add(new VerificationFrame(in.i32(), types(in), types(in)));
    }
    in.requireFullyRead("type-frames section");
  }

  private List<ClassMetadata> buildClasses(List<ClassData> data) {
    List<ClassMetadata> classes = new ArrayList<ClassMetadata>();
    for (ClassData type : data) {
      List<MethodMetadata> methods = new ArrayList<MethodMetadata>();
      for (MethodData method : type.methods) methods.add(method.build());
      classes.add(new ClassMetadata(type.originalName, type.effectiveName, type.rawAccessFlags,
          type.originalSuperName, type.effectiveSuperName, type.originalInterfaces, type.effectiveInterfaces,
          type.sourceFile, type.signature, type.nestHost, type.nestMembers, type.permittedSubclasses,
          type.recordComponents, type.fields, methods, type.synthetic));
    }
    return classes;
  }

  private List<VerificationType> types(TcmBinary.Input in) {
    int count = in.count("verification type count");
    List<VerificationType> types = new ArrayList<VerificationType>(count);
    for (int i = 0; i < count; i++) types.add(new VerificationType(string(in), string(in), in.i32()));
    return types;
  }

  private List<String> strings(TcmBinary.Input in) {
    int count = in.count("string-list count");
    List<String> result = new ArrayList<String>(count);
    for (int i = 0; i < count; i++) result.add(string(in));
    return result;
  }

  private String string(TcmBinary.Input in) {
    int index = in.i32();
    if (index == TcmFormat.NULL_STRING) return null;
    return index(strings, index, "string");
  }

  private Object constant(TcmBinary.Input in) {
    switch (in.u8()) {
    case 0: return null;
    case 1: return string(in);
    case 2: return Integer.valueOf(in.i32());
    case 3: return Long.valueOf(in.i64());
    case 4: return Float.valueOf(Float.intBitsToFloat(in.i32()));
    case 5: return Double.valueOf(Double.longBitsToDouble(in.i64()));
    default: throw in.error("unknown constant kind");
    }
  }

  private static boolean known(int type) {
    return type >= TcmFormat.STRING_TABLE && type <= TcmFormat.TYPE_FRAMES;
  }

  private static byte[] required(Map<Integer, byte[]> sections, int type, TcmBinary.Input in) {
    byte[] payload = sections.get(Integer.valueOf(type));
    if (payload == null) throw in.error("missing required section " + type);
    return payload;
  }

  private static <T> T index(List<T> values, int index, String label) {
    if (index < 0 || index >= values.size()) throw new IllegalArgumentException("Invalid TCM " + label + " index " + index);
    return values.get(index);
  }

  private static <T> T enumValue(T[] values, int ordinal, TcmBinary.Input in, String label) {
    if (ordinal >= values.length) throw in.error("invalid " + label + " " + ordinal);
    return values[ordinal];
  }

  private static final class Manifest {
    final String buildIdentity; final List<Artifact> artifacts;
    Manifest(String buildIdentity, List<Artifact> artifacts) { this.buildIdentity = buildIdentity; this.artifacts = artifacts; }
  }

  private static final class DynamicData {
    final List<String> roots; final boolean unresolved;
    DynamicData(List<String> roots, boolean unresolved) { this.roots = roots; this.unresolved = unresolved; }
  }

  private static final class ClassData {
    String originalName, effectiveName, originalSuperName, effectiveSuperName, sourceFile, signature, nestHost;
    int rawAccessFlags;
    List<String> originalInterfaces, effectiveInterfaces, nestMembers, permittedSubclasses;
    final List<RecordComponentMetadata> recordComponents = new ArrayList<RecordComponentMetadata>();
    final List<FieldMetadata> fields = new ArrayList<FieldMetadata>();
    final List<MethodData> methods = new ArrayList<MethodData>();
    final List<SyntheticOrigin> synthetic = new ArrayList<SyntheticOrigin>();
  }

  private static final class MethodData {
    String originalOwner, effectiveOwner, originalName, effectiveName, descriptor, sourceReturn, loweredReturn;
    List<String> sourceParameters, loweredParameters;
    int rawAccessFlags, tcMethodNameSymbol;
    NativeKind nativeKind;
    final List<VerificationFrame> frames = new ArrayList<VerificationFrame>();
    final List<CallSiteMetadata> calls = new ArrayList<CallSiteMetadata>();
    final List<OriginRange> origins = new ArrayList<OriginRange>();
    MethodMetadata build() {
      return new MethodMetadata(originalOwner, effectiveOwner, originalName, effectiveName, descriptor,
          sourceParameters, sourceReturn, loweredParameters, loweredReturn, rawAccessFlags, nativeKind,
          tcMethodNameSymbol, frames, calls, origins);
    }
  }
}
