// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CompilationMetadata {
  private static final CompilationMetadata EMPTY = new CompilationMetadata(Collections.<ClassMetadata>emptyList(),
      Collections.<String>emptyList(), false);

  public enum NativeKind {
    NONE(0), JAVA_NATIVE(1), REPLACED_ON_DEPLOY(2);

    public final int wireCode;

    NativeKind(int wireCode) {
      this.wireCode = wireCode;
    }

    static NativeKind fromWireCode(int wireCode) {
      for (NativeKind value : values()) if (value.wireCode == wireCode) return value;
      return null;
    }
  }

  public enum InvokeKind {
    STATIC(0), SPECIAL(1), INTERFACE(2), VIRTUAL(3), DYNAMIC_LAMBDA(4), DYNAMIC_STRING_CONCAT(5), DYNAMIC_RECORD(6);

    public final int wireCode;

    InvokeKind(int wireCode) {
      this.wireCode = wireCode;
    }

    static InvokeKind fromWireCode(int wireCode) {
      for (InvokeKind value : values()) if (value.wireCode == wireCode) return value;
      return null;
    }
  }

  public enum SyntheticKind {
    LAMBDA(0), STRING_CONCAT(1), RECORD_OBJECT_METHOD(2);

    public final int wireCode;

    SyntheticKind(int wireCode) {
      this.wireCode = wireCode;
    }

    static SyntheticKind fromWireCode(int wireCode) {
      for (SyntheticKind value : values()) if (value.wireCode == wireCode) return value;
      return null;
    }
  }

  public final List<ClassMetadata> classes;
  public final List<String> resolvedClassForNameRoots;
  public final boolean unresolvedDynamicClassLookup;

  CompilationMetadata(List<ClassMetadata> classes, List<String> roots, boolean unresolved) {
    this.classes = immutable(classes);
    this.resolvedClassForNameRoots = immutable(roots);
    this.unresolvedDynamicClassLookup = unresolved;
  }

  static CompilationMetadata empty() {
    return EMPTY;
  }

  static <T> List<T> immutable(List<T> values) {
    return Collections.unmodifiableList(new ArrayList<T>(values));
  }

  public static final class ClassMetadata {
    public final String originalName;
    public final String effectiveName;
    public final int rawAccessFlags;
    public final String originalSuperName;
    public final String effectiveSuperName;
    public final List<String> originalInterfaces;
    public final List<String> effectiveInterfaces;
    public final String sourceFile;
    public final String signature;
    public final String nestHost;
    public final List<String> nestMembers;
    public final List<String> permittedSubclasses;
    public final List<RecordComponentMetadata> recordComponents;
    public final List<FieldMetadata> fields;
    public final List<MethodMetadata> methods;
    public final List<SyntheticOrigin> syntheticOrigins;

    ClassMetadata(String originalName, String effectiveName, int rawAccessFlags, String originalSuperName,
        String effectiveSuperName, List<String> originalInterfaces, List<String> effectiveInterfaces, String sourceFile,
        String signature, String nestHost, List<String> nestMembers, List<String> permittedSubclasses,
        List<RecordComponentMetadata> recordComponents, List<FieldMetadata> fields, List<MethodMetadata> methods,
        List<SyntheticOrigin> syntheticOrigins) {
      this.originalName = originalName;
      this.effectiveName = effectiveName;
      this.rawAccessFlags = rawAccessFlags;
      this.originalSuperName = originalSuperName;
      this.effectiveSuperName = effectiveSuperName;
      this.originalInterfaces = immutable(originalInterfaces);
      this.effectiveInterfaces = immutable(effectiveInterfaces);
      this.sourceFile = sourceFile;
      this.signature = signature;
      this.nestHost = nestHost;
      this.nestMembers = immutable(nestMembers);
      this.permittedSubclasses = immutable(permittedSubclasses);
      this.recordComponents = immutable(recordComponents);
      this.fields = immutable(fields);
      this.methods = immutable(methods);
      this.syntheticOrigins = immutable(syntheticOrigins);
    }
  }

  public static final class RecordComponentMetadata {
    public final String name;
    public final String descriptor;

    RecordComponentMetadata(String name, String descriptor) {
      this.name = name;
      this.descriptor = descriptor;
    }
  }

  public static final class FieldMetadata {
    public final String originalOwner;
    public final String effectiveOwner;
    public final String name;
    public final String javaDescriptor;
    public final String loweredType;
    public final int rawAccessFlags;
    public final Object constantValue;
    public final int tcFieldSymbol;

    FieldMetadata(String originalOwner, String effectiveOwner, String name, String javaDescriptor, String loweredType,
        int rawAccessFlags, Object constantValue, int tcFieldSymbol) {
      this.originalOwner = originalOwner;
      this.effectiveOwner = effectiveOwner;
      this.name = name;
      this.javaDescriptor = javaDescriptor;
      this.loweredType = loweredType;
      this.rawAccessFlags = rawAccessFlags;
      this.constantValue = constantValue;
      this.tcFieldSymbol = tcFieldSymbol;
    }
  }

  public static final class MethodMetadata {
    public final String originalOwner;
    public final String effectiveOwner;
    public final String originalName;
    public final String effectiveName;
    public final String javaDescriptor;
    public final List<String> sourceParameterDescriptors;
    public final String sourceReturnDescriptor;
    public final List<String> loweredParameterTypes;
    public final String loweredReturnType;
    public final int rawAccessFlags;
    public final NativeKind nativeKind;
    public final int tcMethodNameSymbol;
    public final List<VerificationFrame> verificationFrames;
    public final List<CallSiteMetadata> callSites;
    public final List<OriginRange> origins;

    MethodMetadata(String originalOwner, String effectiveOwner, String originalName, String effectiveName,
        String javaDescriptor, List<String> sourceParameters, String sourceReturn, List<String> loweredParameters,
        String loweredReturn, int rawAccessFlags, NativeKind nativeKind, int tcMethodNameSymbol,
        List<VerificationFrame> verificationFrames, List<CallSiteMetadata> callSites, List<OriginRange> origins) {
      this.originalOwner = originalOwner;
      this.effectiveOwner = effectiveOwner;
      this.originalName = originalName;
      this.effectiveName = effectiveName;
      this.javaDescriptor = javaDescriptor;
      this.sourceParameterDescriptors = immutable(sourceParameters);
      this.sourceReturnDescriptor = sourceReturn;
      this.loweredParameterTypes = immutable(loweredParameters);
      this.loweredReturnType = loweredReturn;
      this.rawAccessFlags = rawAccessFlags;
      this.nativeKind = nativeKind;
      this.tcMethodNameSymbol = tcMethodNameSymbol;
      this.verificationFrames = immutable(verificationFrames);
      this.callSites = immutable(callSites);
      this.origins = immutable(origins);
    }
  }

  public static final class VerificationFrame {
    public final int javaPc;
    public final List<VerificationType> locals;
    public final List<VerificationType> stack;

    VerificationFrame(int javaPc, List<VerificationType> locals, List<VerificationType> stack) {
      this.javaPc = javaPc;
      this.locals = immutable(locals);
      this.stack = immutable(stack);
    }
  }

  public static final class VerificationType {
    public final String kind;
    public final String className;
    public final int newInstructionOffset;

    VerificationType(String kind, String className, int newInstructionOffset) {
      this.kind = kind;
      this.className = className;
      this.newInstructionOffset = newInstructionOffset;
    }
  }

  public static final class OriginRange {
    public final int javaPc;
    public final int javaOpcode;
    public final int tcStartSlot;
    public final int tcEndSlotExclusive;
    public final String allocationType;

    OriginRange(int javaPc, int javaOpcode, int tcStartSlot, int tcEndSlotExclusive, String allocationType) {
      this.javaPc = javaPc;
      this.javaOpcode = javaOpcode;
      this.tcStartSlot = tcStartSlot;
      this.tcEndSlotExclusive = tcEndSlotExclusive;
      this.allocationType = allocationType;
    }
  }

  public static final class CallSiteMetadata {
    public final int javaPc;
    public final int javaOpcode;
    public final InvokeKind invokeKind;
    public final String symbolicOwner;
    public final String name;
    public final String javaDescriptor;
    public final String resolvedDeclarationOwner;
    public final int loweredOpcode;
    public final int tcStartSlot;
    public final int tcEndSlotExclusive;

    CallSiteMetadata(int javaPc, int javaOpcode, InvokeKind invokeKind, String symbolicOwner, String name,
        String javaDescriptor, String resolvedDeclarationOwner, int loweredOpcode, int tcStartSlot,
        int tcEndSlotExclusive) {
      this.javaPc = javaPc;
      this.javaOpcode = javaOpcode;
      this.invokeKind = invokeKind;
      this.symbolicOwner = symbolicOwner;
      this.name = name;
      this.javaDescriptor = javaDescriptor;
      this.resolvedDeclarationOwner = resolvedDeclarationOwner;
      this.loweredOpcode = loweredOpcode;
      this.tcStartSlot = tcStartSlot;
      this.tcEndSlotExclusive = tcEndSlotExclusive;
    }
  }

  public static final class SyntheticOrigin {
    public final SyntheticKind kind;
    public final String owner;
    public final String methodDescriptor;
    public final int javaPc;
    public final String generatedClass;
    public final String factoryMethod;
    public final String samDescriptor;
    public final int implementationKind;
    public final String implementationOwner;
    public final String implementationName;
    public final String implementationDescriptor;
    public final List<String> captureDescriptors;

    SyntheticOrigin(SyntheticKind kind, String owner, String methodDescriptor, int javaPc, String generatedClass,
        String factoryMethod, String samDescriptor, int implementationKind, String implementationOwner,
        String implementationName, String implementationDescriptor, List<String> captureDescriptors) {
      this.kind = kind;
      this.owner = owner;
      this.methodDescriptor = methodDescriptor;
      this.javaPc = javaPc;
      this.generatedClass = generatedClass;
      this.factoryMethod = factoryMethod;
      this.samDescriptor = samDescriptor;
      this.implementationKind = implementationKind;
      this.implementationOwner = implementationOwner;
      this.implementationName = implementationName;
      this.implementationDescriptor = implementationDescriptor;
      this.captureDescriptors = immutable(captureDescriptors);
    }
  }
}
