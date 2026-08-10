// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import tc.tools.converter.metadata.CompilationMetadata.CallSiteMetadata;
import tc.tools.converter.metadata.CompilationMetadata.ClassMetadata;
import tc.tools.converter.metadata.CompilationMetadata.FieldMetadata;
import tc.tools.converter.metadata.CompilationMetadata.MethodMetadata;
import tc.tools.converter.metadata.CompilationMetadata.OriginRange;
import tc.tools.converter.metadata.CompilationMetadata.SyntheticOrigin;
import tc.tools.converter.metadata.CompilationMetadata.VerificationFrame;
import tc.tools.converter.metadata.CompilationMetadata.VerificationType;
import tc.tools.converter.metadata.TcmFile.Artifact;
import totalcross.sys.Settings;

public final class TcmWriter {
  private final CompilationMetadata metadata;
  private final List<Artifact> artifacts;
  private final String buildIdentity;
  private final Map<String, Integer> strings;
  private final IdentityHashMap<ClassMetadata, Integer> classIds = new IdentityHashMap<ClassMetadata, Integer>();
  private final IdentityHashMap<MethodMetadata, Integer> methodIds = new IdentityHashMap<MethodMetadata, Integer>();

  public TcmWriter(CompilationMetadata metadata, List<Artifact> artifacts, String buildIdentity) {
    this.metadata = metadata;
    this.artifacts = new ArrayList<Artifact>(artifacts);
    this.buildIdentity = buildIdentity;
    indexMetadata();
    this.strings = collectStrings();
  }

  public byte[] write() {
    TcmBinary.Output output = new TcmBinary.Output();
    output.bytes(TcmFormat.MAGIC);
    output.u16(TcmFormat.MAJOR_VERSION);
    output.u16(TcmFormat.MINOR_VERSION);
    output.i32(0);
    output.i32(10);
    section(output, TcmFormat.STRING_TABLE | TcmFormat.REQUIRED, stringTable());
    section(output, TcmFormat.ARTIFACT_MANIFEST | TcmFormat.REQUIRED, artifactManifest());
    section(output, TcmFormat.CLASSES, classes());
    section(output, TcmFormat.FIELDS, fields());
    section(output, TcmFormat.METHODS, methods());
    section(output, TcmFormat.CALL_SITES, calls());
    section(output, TcmFormat.ORIGIN_MAP, origins());
    section(output, TcmFormat.ALLOCATION_AND_SYNTHETIC_ORIGINS, syntheticOrigins());
    section(output, TcmFormat.DYNAMIC_ACCESS, dynamicAccess());
    section(output, TcmFormat.TYPE_FRAMES, frames());
    return output.toByteArray();
  }

  public static Path publishForTczs(List<Path> tczPaths, CompilationMetadata metadata) throws IOException {
    if (tczPaths.isEmpty()) {
      throw new IllegalArgumentException("TCM requires at least one TCZ artifact");
    }
    List<Artifact> artifacts = artifacts(tczPaths);
    Path primary = tczPaths.get(0).toAbsolutePath().normalize();
    String fileName = primary.getFileName().toString();
    String base = fileName.toLowerCase().endsWith(".tcz") ? fileName.substring(0, fileName.length() - 4) : fileName;
    Path sidecar = primary.resolveSibling(base + ".tcm");
    String identity = Settings.versionStr + "." + Settings.buildNumber;
    publish(sidecar, new TcmWriter(metadata, artifacts, identity).write());
    return sidecar;
  }

  public static List<Artifact> artifacts(List<Path> paths) throws IOException {
    List<Artifact> artifacts = new ArrayList<Artifact>();
    MessageDigest digest = sha256();
    for (int i = 0; i < paths.size(); i++) {
      Path path = paths.get(i).toAbsolutePath().normalize();
      digest.reset();
      artifacts.add(new Artifact(path.getFileName().toString(), digest.digest(Files.readAllBytes(path))));
    }
    return artifacts;
  }

  private static void publish(Path sidecar, byte[] bytes) throws IOException {
    Path temporary = sidecar.resolveSibling(sidecar.getFileName().toString() + ".tmp");
    Files.deleteIfExists(temporary);
    Files.deleteIfExists(sidecar);
    try {
      Files.write(temporary, bytes);
      try {
        Files.move(temporary, sidecar, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(temporary, sidecar, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      Files.deleteIfExists(temporary);
      throw e;
    }
  }

  private void indexMetadata() {
    int methodId = 0;
    for (int i = 0; i < metadata.classes.size(); i++) {
      ClassMetadata type = metadata.classes.get(i);
      classIds.put(type, Integer.valueOf(i));
      for (int j = 0; j < type.methods.size(); j++) {
        methodIds.put(type.methods.get(j), Integer.valueOf(methodId++));
      }
    }
  }

  private Map<String, Integer> collectStrings() {
    Set<String> values = new TreeSet<String>();
    add(values, buildIdentity);
    for (Artifact artifact : artifacts) {
      add(values, artifact.relativeName);
    }
    for (ClassMetadata type : metadata.classes) {
      add(values, type.originalName, type.effectiveName, type.originalSuperName, type.effectiveSuperName,
          type.sourceFile, type.signature, type.nestHost);
      add(values, type.originalInterfaces);
      add(values, type.effectiveInterfaces);
      add(values, type.nestMembers);
      add(values, type.permittedSubclasses);
      type.recordComponents.forEach(component -> add(values, component.name, component.descriptor));
      for (FieldMetadata field : type.fields) {
        add(values, field.originalOwner, field.effectiveOwner, field.name, field.javaDescriptor, field.loweredType);
        if (field.constantValue instanceof String) {
          add(values, (String) field.constantValue);
        }
      }
      for (MethodMetadata method : type.methods) {
        add(values, method.originalOwner, method.effectiveOwner, method.originalName, method.effectiveName,
            method.javaDescriptor, method.sourceReturnDescriptor, method.loweredReturnType);
        add(values, method.sourceParameterDescriptors);
        add(values, method.loweredParameterTypes);
        for (CallSiteMetadata call : method.callSites) {
          add(values, call.symbolicOwner, call.name, call.javaDescriptor, call.resolvedDeclarationOwner);
        }
        for (OriginRange origin : method.origins) {
          add(values, origin.allocationType);
        }
        for (VerificationFrame frame : method.verificationFrames) {
          addTypes(values, frame.locals);
          addTypes(values, frame.stack);
        }
      }
      for (SyntheticOrigin origin : type.syntheticOrigins) {
        add(values, origin.owner, origin.methodDescriptor, origin.generatedClass, origin.factoryMethod,
            origin.samDescriptor, origin.implementationOwner, origin.implementationName,
            origin.implementationDescriptor);
        add(values, origin.captureDescriptors);
      }
    }
    add(values, metadata.resolvedClassForNameRoots);
    Map<String, Integer> result = new LinkedHashMap<String, Integer>();
    int index = 0;
    for (String value : values) {
      result.put(value, Integer.valueOf(index++));
    }
    return result;
  }

  private byte[] stringTable() {
    TcmBinary.Output out = new TcmBinary.Output();
    out.i32(strings.size());
    for (String value : strings.keySet()) {
      out.utf8(value);
    }
    return out.toByteArray();
  }

  private byte[] artifactManifest() {
    TcmBinary.Output out = new TcmBinary.Output();
    string(out, buildIdentity);
    out.i32(artifacts.size());
    for (Artifact artifact : artifacts) {
      string(out, artifact.relativeName);
      out.bytes(artifact.sha256);
    }
    return out.toByteArray();
  }

  private byte[] classes() {
    TcmBinary.Output out = new TcmBinary.Output();
    out.i32(metadata.classes.size());
    for (ClassMetadata type : metadata.classes) {
      string(out, type.originalName); string(out, type.effectiveName); out.i32(type.rawAccessFlags);
      string(out, type.originalSuperName); string(out, type.effectiveSuperName);
      strings(out, type.originalInterfaces); strings(out, type.effectiveInterfaces);
      string(out, type.sourceFile); string(out, type.signature); string(out, type.nestHost);
      strings(out, type.nestMembers); strings(out, type.permittedSubclasses);
      out.i32(type.recordComponents.size());
      type.recordComponents.forEach(component -> { string(out, component.name); string(out, component.descriptor); });
    }
    return out.toByteArray();
  }

  private byte[] fields() {
    TcmBinary.Output out = new TcmBinary.Output();
    int count = metadata.classes.stream().mapToInt(type -> type.fields.size()).sum();
    out.i32(count);
    for (ClassMetadata type : metadata.classes) {
      for (FieldMetadata field : type.fields) {
        out.i32(classIds.get(type).intValue());
        string(out, field.originalOwner); string(out, field.effectiveOwner); string(out, field.name);
        string(out, field.javaDescriptor); string(out, field.loweredType); out.i32(field.rawAccessFlags);
        constant(out, field.constantValue); out.i32(field.tcFieldSymbol);
      }
    }
    return out.toByteArray();
  }

  private byte[] methods() {
    TcmBinary.Output out = new TcmBinary.Output();
    out.i32(methodIds.size());
    for (ClassMetadata type : metadata.classes) {
      for (MethodMetadata method : type.methods) {
        out.i32(classIds.get(type).intValue());
        string(out, method.originalOwner); string(out, method.effectiveOwner);
        string(out, method.originalName); string(out, method.effectiveName); string(out, method.javaDescriptor);
        strings(out, method.sourceParameterDescriptors); string(out, method.sourceReturnDescriptor);
        strings(out, method.loweredParameterTypes); string(out, method.loweredReturnType);
        out.i32(method.rawAccessFlags); out.u8(method.nativeKind.ordinal()); out.i32(method.tcMethodNameSymbol);
      }
    }
    return out.toByteArray();
  }

  private byte[] calls() {
    TcmBinary.Output out = new TcmBinary.Output();
    int count = metadata.classes.stream().flatMap(type -> type.methods.stream()).mapToInt(m -> m.callSites.size()).sum();
    out.i32(count);
    for (ClassMetadata type : metadata.classes) for (MethodMetadata method : type.methods)
      for (CallSiteMetadata call : method.callSites) {
        out.i32(methodIds.get(method).intValue()); out.i32(call.javaPc); out.i32(call.javaOpcode);
        out.u8(call.invokeKind.ordinal()); string(out, call.symbolicOwner); string(out, call.name);
        string(out, call.javaDescriptor); string(out, call.resolvedDeclarationOwner); out.i32(call.loweredOpcode);
        out.i32(call.tcStartSlot); out.i32(call.tcEndSlotExclusive);
      }
    return out.toByteArray();
  }

  private byte[] origins() {
    TcmBinary.Output out = new TcmBinary.Output();
    int count = metadata.classes.stream().flatMap(type -> type.methods.stream()).mapToInt(m -> m.origins.size()).sum();
    out.i32(count);
    for (ClassMetadata type : metadata.classes) for (MethodMetadata method : type.methods)
      for (OriginRange origin : method.origins) {
        out.i32(methodIds.get(method).intValue()); out.i32(origin.javaPc); out.i32(origin.javaOpcode);
        out.i32(origin.tcStartSlot); out.i32(origin.tcEndSlotExclusive); string(out, origin.allocationType);
      }
    return out.toByteArray();
  }

  private byte[] syntheticOrigins() {
    TcmBinary.Output out = new TcmBinary.Output();
    int count = metadata.classes.stream().mapToInt(type -> type.syntheticOrigins.size()).sum();
    out.i32(count);
    for (ClassMetadata type : metadata.classes) for (SyntheticOrigin origin : type.syntheticOrigins) {
      out.i32(classIds.get(type).intValue()); out.u8(origin.kind.ordinal()); string(out, origin.owner);
      string(out, origin.methodDescriptor); out.i32(origin.javaPc); string(out, origin.generatedClass);
      string(out, origin.factoryMethod); string(out, origin.samDescriptor); out.i32(origin.implementationKind);
      string(out, origin.implementationOwner); string(out, origin.implementationName);
      string(out, origin.implementationDescriptor); strings(out, origin.captureDescriptors);
    }
    return out.toByteArray();
  }

  private byte[] dynamicAccess() {
    TcmBinary.Output out = new TcmBinary.Output();
    strings(out, metadata.resolvedClassForNameRoots);
    out.u8(metadata.unresolvedDynamicClassLookup ? 1 : 0);
    return out.toByteArray();
  }

  private byte[] frames() {
    TcmBinary.Output out = new TcmBinary.Output();
    int count = metadata.classes.stream().flatMap(type -> type.methods.stream())
        .mapToInt(method -> method.verificationFrames.size()).sum();
    out.i32(count);
    for (ClassMetadata type : metadata.classes) for (MethodMetadata method : type.methods)
      for (VerificationFrame frame : method.verificationFrames) {
        out.i32(methodIds.get(method).intValue()); out.i32(frame.javaPc);
        types(out, frame.locals); types(out, frame.stack);
      }
    return out.toByteArray();
  }

  private static void section(TcmBinary.Output output, int type, byte[] payload) {
    output.u16(type); output.u16(TcmFormat.SECTION_VERSION); output.i32(payload.length); output.bytes(payload);
  }

  private void string(TcmBinary.Output out, String value) {
    out.i32(value == null ? TcmFormat.NULL_STRING : strings.get(value).intValue());
  }

  private void strings(TcmBinary.Output out, List<String> values) {
    out.i32(values.size());
    for (String value : values) string(out, value);
  }

  private void types(TcmBinary.Output out, List<VerificationType> values) {
    out.i32(values.size());
    for (VerificationType value : values) {
      string(out, value.kind); string(out, value.className); out.i32(value.newInstructionOffset);
    }
  }

  private void constant(TcmBinary.Output out, Object value) {
    if (value == null) { out.u8(0); }
    else if (value instanceof String) { out.u8(1); string(out, (String) value); }
    else if (value instanceof Integer) { out.u8(2); out.i32(((Integer) value).intValue()); }
    else if (value instanceof Long) { out.u8(3); out.i64(((Long) value).longValue()); }
    else if (value instanceof Float) { out.u8(4); out.i32(Float.floatToRawIntBits(((Float) value).floatValue())); }
    else if (value instanceof Double) { out.u8(5); out.i64(Double.doubleToRawLongBits(((Double) value).doubleValue())); }
    else { throw new IllegalArgumentException("Unsupported TCM constant " + value.getClass().getName()); }
  }

  private static void add(Set<String> values, String... additions) {
    for (String value : additions) if (value != null) values.add(value);
  }

  private static void add(Set<String> values, List<String> additions) {
    for (String value : additions) add(values, value);
  }

  private static void addTypes(Set<String> values, List<VerificationType> types) {
    for (VerificationType type : types) add(values, type.kind, type.className);
  }

  private static MessageDigest sha256() {
    try { return MessageDigest.getInstance("SHA-256"); }
    catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 is unavailable", e); }
  }
}
