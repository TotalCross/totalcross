// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.Java8LambdaLowering;
import tc.tools.converter.JavaObjectMethodsLowering;
import tc.tools.converter.JavaStringConcatLowering;
import tc.tools.converter.MethodDeclarationResolver;
import tc.tools.converter.TCConstants;
import tc.tools.converter.bytecode.BC186_invokedynamic;
import tc.tools.converter.bytecode.BC187_new;
import tc.tools.converter.bytecode.BC188_newarray;
import tc.tools.converter.bytecode.BC189_anewarray;
import tc.tools.converter.bytecode.BC197_multinewarray;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.bytecode.MethodCall;
import tc.tools.converter.ir.Instruction.Call;
import tc.tools.converter.ir.Instruction.Instruction;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaConstantInfo;
import tc.tools.converter.java.JavaField;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.java.JavaRecordComponent;
import tc.tools.converter.java.JavaStackMapFrame;
import tc.tools.converter.java.JavaVerificationType;
import tc.tools.converter.tclass.TCMethod;
import totalcross.util.Vector;

public final class CompilationMetadataCollector implements CompilationMetadataCapture, TCConstants {
  private static final int JVM_INVOKESPECIAL = 183;
  private static final int JVM_INVOKESTATIC = 184;
  private static final int JVM_INVOKEINTERFACE = 185;
  private static final int JVM_INVOKEDYNAMIC = 186;
  private final List<ClassBuilder> classes = new ArrayList<ClassBuilder>();
  private final IdentityHashMap<JavaClass, ClassBuilder> classesByIdentity = new IdentityHashMap<JavaClass, ClassBuilder>();
  private final IdentityHashMap<JavaMethod, MethodBuilder> methods = new IdentityHashMap<JavaMethod, MethodBuilder>();
  private final Set<String> classForNameRoots = new LinkedHashSet<String>();
  private boolean unresolvedDynamicClassLookup;

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void captureClass(JavaClass source, String effectiveName) {
    ClassBuilder builder = new ClassBuilder(source, effectiveName);
    classes.add(builder);
    classesByIdentity.put(source, builder);
    if (source.methods != null) {
      for (int i = 0; i < source.methods.length; i++) {
        MethodBuilder method = new MethodBuilder(builder, source.methods[i]);
        builder.methods.add(method);
        methods.put(source.methods[i], method);
      }
    }
  }

  @Override
  public void captureField(JavaClass owner, JavaField field, int tcFieldSymbol) {
    ClassBuilder builder = classesByIdentity.get(owner);
    if (builder != null) {
      builder.fields.add(new CompilationMetadata.FieldMetadata(builder.originalName, builder.effectiveName, field.name,
          field.type, GlobalConstantPool.javaType2TCType(field.type), field.rawAccessFlags,
          safeConstant(owner, field.constantValue),
          tcFieldSymbol));
    }
  }

  @Override
  public void captureMethodHeader(JavaMethod source, TCMethod target) {
    MethodBuilder builder = methods.get(source);
    if (builder != null) {
      builder.effectiveName = source.name;
      builder.tcMethodNameSymbol = target.cpName;
    }
  }

  @Override
  public SiteCapture beginBytecode(JavaClass owner, JavaMethod method, ByteCode bytecode) {
    MethodBuilder builder = methods.get(method);
    if (builder == null) {
      return null;
    }
    SiteCapture site = new SiteCapture(builder, bytecode.pcInMethod, bytecode.bc, allocationType(bytecode));
    if (bytecode instanceof MethodCall && bytecode.bc != JVM_INVOKEDYNAMIC) {
      MethodCall call = (MethodCall) bytecode;
      site.call = new CallBuilder(site, invokeKind(bytecode.bc), call.className, call.name, call.parameters,
          MethodDeclarationResolver.resolve(call.className, call.name, call.parameters).declarationOwner);
    } else if (bytecode instanceof BC186_invokedynamic) {
      captureDynamic(owner, method, (BC186_invokedynamic) bytecode, site);
    }
    builder.sites.add(site);
    builder.sitesByJavaPc.put(Integer.valueOf(site.javaPc), site);
    return site;
  }

  @Override
  public void endBytecode(CompilationMetadataCapture.SiteCapture capturedSite, Vector instructions,
      int firstInstruction) {
    SiteCapture site = (SiteCapture) capturedSite;
    if (site == null) {
      return;
    }
    for (int i = firstInstruction; i < instructions.size(); i++) {
      Instruction instruction = (Instruction) instructions.items[i];
      instruction.javaPc = site.javaPc;
      instruction.javaOpcode = site.javaOpcode;
    }
  }

  @Override
  public void finishMethod(JavaMethod source, TCMethod target) {
    MethodBuilder builder = methods.get(source);
    if (builder == null || target.insts == null) {
      return;
    }
    int slot = 0;
    for (int i = 0; i < target.insts.size(); i++) {
      Instruction instruction = (Instruction) target.insts.items[i];
      SiteCapture site = builder.sitesByJavaPc.get(Integer.valueOf(instruction.javaPc));
      if (site != null && instruction.javaOpcode == site.javaOpcode) {
        if (site.tcStartSlot < 0) {
          site.tcStartSlot = slot;
        }
        site.tcEndSlot = slot + instruction.len;
        if (site.call != null && instruction instanceof Call) {
          site.call.tcStartSlot = slot;
          site.call.tcEndSlot = slot + instruction.len;
          site.call.loweredOpcode = instruction.opcode;
        }
      }
      slot += instruction.len;
    }
    for (int i = 0; i < builder.sites.size(); i++) {
      SiteCapture site = builder.sites.get(i);
      builder.origins.add(new CompilationMetadata.OriginRange(site.javaPc, site.javaOpcode, site.tcStartSlot,
          site.tcEndSlot, site.allocationType));
      if (site.call != null) {
        if (site.call.kind.name().startsWith("DYNAMIC_")) {
          site.call.tcStartSlot = site.tcStartSlot;
          site.call.tcEndSlot = site.tcEndSlot;
        }
        builder.calls.add(site.call);
      }
    }
  }

  @Override
  public void recordResolvedClassForName(String className) {
    if (className != null) {
      classForNameRoots.add(className.replace('.', '/'));
    }
  }

  @Override
  public void recordUnresolvedClassForName() {
    unresolvedDynamicClassLookup = true;
  }

  @Override
  public CompilationMetadata snapshot() {
    List<CompilationMetadata.ClassMetadata> result = new ArrayList<CompilationMetadata.ClassMetadata>();
    for (int i = 0; i < classes.size(); i++) {
      result.add(classes.get(i).build());
    }
    return new CompilationMetadata(result, new ArrayList<String>(classForNameRoots), unresolvedDynamicClassLookup);
  }

  private void captureDynamic(JavaClass owner, JavaMethod method, BC186_invokedynamic dynamic, SiteCapture site) {
    CompilationMetadata.SyntheticKind kind;
    CompilationMetadata.InvokeKind invokeKind;
    Java8LambdaLowering.LambdaSite lambda = null;
    if (JavaStringConcatLowering.isStringConcatFactory(owner, dynamic)) {
      kind = CompilationMetadata.SyntheticKind.STRING_CONCAT;
      invokeKind = CompilationMetadata.InvokeKind.DYNAMIC_STRING_CONCAT;
    } else if (JavaObjectMethodsLowering.isObjectMethodsFactory(owner, dynamic)) {
      kind = CompilationMetadata.SyntheticKind.RECORD_OBJECT_METHOD;
      invokeKind = CompilationMetadata.InvokeKind.DYNAMIC_RECORD;
    } else {
      kind = CompilationMetadata.SyntheticKind.LAMBDA;
      invokeKind = CompilationMetadata.InvokeKind.DYNAMIC_LAMBDA;
      lambda = Java8LambdaLowering.resolve(owner, dynamic);
    }
    site.call = new CallBuilder(site, invokeKind, null, dynamic.name, dynamic.descriptor, null);
    if (lambda != null) {
      site.method.owner.synthetic.add(new CompilationMetadata.SyntheticOrigin(kind, owner.originalClassName,
          method.descriptor, dynamic.pcInMethod, lambda.adapterClassName, lambda.factoryMethodName,
          lambda.samDescriptor, lambda.implementationKind, lambda.implementationOwner, lambda.implementationName,
          lambda.implementationDescriptor, strings(lambda.factoryParams)));
    } else {
      site.method.owner.synthetic.add(new CompilationMetadata.SyntheticOrigin(kind, owner.originalClassName,
          method.descriptor, dynamic.pcInMethod, null, null, null, -1, null, null, null,
          new ArrayList<String>()));
    }
  }

  private static CompilationMetadata.InvokeKind invokeKind(int opcode) {
    switch (opcode) {
    case JVM_INVOKESTATIC:
      return CompilationMetadata.InvokeKind.STATIC;
    case JVM_INVOKESPECIAL:
      return CompilationMetadata.InvokeKind.SPECIAL;
    case JVM_INVOKEINTERFACE:
      return CompilationMetadata.InvokeKind.INTERFACE;
    default:
      return CompilationMetadata.InvokeKind.VIRTUAL;
    }
  }

  private static String allocationType(ByteCode bytecode) {
    if (bytecode instanceof BC187_new) {
      return ((BC187_new) bytecode).className;
    }
    if (bytecode instanceof BC189_anewarray) {
      return "[L" + ((BC189_anewarray) bytecode).classType + ";";
    }
    if (bytecode instanceof BC197_multinewarray) {
      return ((BC197_multinewarray) bytecode).className;
    }
    if (bytecode instanceof BC188_newarray) {
      String types = "?ZCFDBSIJ";
      int type = ((BC188_newarray) bytecode).arrayType;
      return type >= 4 && type <= 11 ? "[" + types.charAt(type - 3) : null;
    }
    return null;
  }

  private static Object safeConstant(JavaClass owner, Object value) {
    if (value instanceof JavaConstantInfo) {
      return owner.cp.getString1(((JavaConstantInfo) value).index1);
    }
    return value instanceof String || value instanceof Number ? value : null;
  }

  private static List<String> strings(String[] values) {
    List<String> result = new ArrayList<String>();
    if (values != null) {
      for (int i = 0; i < values.length; i++) {
        result.add(values[i]);
      }
    }
    return result;
  }

  public static final class SiteCapture implements CompilationMetadataCapture.SiteCapture {
    final MethodBuilder method;
    final int javaPc;
    final int javaOpcode;
    final String allocationType;
    CallBuilder call;
    int tcStartSlot = -1;
    int tcEndSlot = -1;

    SiteCapture(MethodBuilder method, int javaPc, int javaOpcode, String allocationType) {
      this.method = method;
      this.javaPc = javaPc;
      this.javaOpcode = javaOpcode;
      this.allocationType = allocationType;
    }
  }

  private static final class CallBuilder {
    final SiteCapture site;
    final CompilationMetadata.InvokeKind kind;
    final String owner;
    final String name;
    final String descriptor;
    final String declarationOwner;
    int loweredOpcode = -1;
    int tcStartSlot = -1;
    int tcEndSlot = -1;

    CallBuilder(SiteCapture site, CompilationMetadata.InvokeKind kind, String owner, String name, String descriptor,
        String declarationOwner) {
      this.site = site;
      this.kind = kind;
      this.owner = owner;
      this.name = name;
      this.descriptor = descriptor;
      this.declarationOwner = declarationOwner;
    }

    CompilationMetadata.CallSiteMetadata build() {
      return new CompilationMetadata.CallSiteMetadata(site.javaPc, site.javaOpcode, kind, owner, name, descriptor,
          declarationOwner, loweredOpcode, tcStartSlot, tcEndSlot);
    }
  }

  private static final class MethodBuilder {
    final ClassBuilder owner;
    final JavaMethod source;
    final String originalName;
    final List<SiteCapture> sites = new ArrayList<SiteCapture>();
    final HashMap<Integer, SiteCapture> sitesByJavaPc = new HashMap<Integer, SiteCapture>();
    final List<CallBuilder> calls = new ArrayList<CallBuilder>();
    final List<CompilationMetadata.OriginRange> origins = new ArrayList<CompilationMetadata.OriginRange>();
    String effectiveName;
    int tcMethodNameSymbol = -1;

    MethodBuilder(ClassBuilder owner, JavaMethod source) {
      this.owner = owner;
      this.source = source;
      this.originalName = source.name;
      this.effectiveName = source.name;
    }

    CompilationMetadata.MethodMetadata build() {
      List<String> loweredParams = new ArrayList<String>();
      if (source.params != null) {
        for (int i = 0; i < source.params.length; i++) {
          loweredParams.add(GlobalConstantPool.javaType2TCType(source.params[i]));
        }
      }
      CompilationMetadata.NativeKind nativeKind = source.replaceWithNative
          ? CompilationMetadata.NativeKind.REPLACED_ON_DEPLOY
          : source.isNative ? CompilationMetadata.NativeKind.JAVA_NATIVE : CompilationMetadata.NativeKind.NONE;
      List<CompilationMetadata.CallSiteMetadata> builtCalls = new ArrayList<CompilationMetadata.CallSiteMetadata>();
      for (int i = 0; i < calls.size(); i++) {
        builtCalls.add(calls.get(i).build());
      }
      return new CompilationMetadata.MethodMetadata(owner.originalName, owner.effectiveName, originalName,
          effectiveName, source.descriptor, strings(source.params), source.ret, loweredParams,
          GlobalConstantPool.javaType2TCType(source.ret),
          source.rawAccessFlags, nativeKind, tcMethodNameSymbol, frames(source), builtCalls, origins);
    }
  }

  private static final class ClassBuilder {
    final JavaClass source;
    final String originalName;
    final String effectiveName;
    final List<CompilationMetadata.FieldMetadata> fields = new ArrayList<CompilationMetadata.FieldMetadata>();
    final List<MethodBuilder> methods = new ArrayList<MethodBuilder>();
    final List<CompilationMetadata.SyntheticOrigin> synthetic = new ArrayList<CompilationMetadata.SyntheticOrigin>();

    ClassBuilder(JavaClass source, String effectiveName) {
      this.source = source;
      this.originalName = source.originalClassName == null ? source.className : source.originalClassName;
      this.effectiveName = effectiveName;
    }

    CompilationMetadata.ClassMetadata build() {
      List<CompilationMetadata.RecordComponentMetadata> components = new ArrayList<CompilationMetadata.RecordComponentMetadata>();
      if (source.recordComponents != null) {
        for (int i = 0; i < source.recordComponents.length; i++) {
          JavaRecordComponent component = source.recordComponents[i];
          components.add(new CompilationMetadata.RecordComponentMetadata(component.name, component.descriptor));
        }
      }
      List<CompilationMetadata.MethodMetadata> builtMethods = new ArrayList<CompilationMetadata.MethodMetadata>();
      for (int i = 0; i < methods.size(); i++) {
        builtMethods.add(methods.get(i).build());
      }
      return new CompilationMetadata.ClassMetadata(originalName, effectiveName, source.rawAccessFlags,
          source.originalSuperClass, source.superClass, strings(source.originalInterfaces), strings(source.interfaces),
          source.sourceFile, source.signature, source.nestHost, strings(source.nestMembers),
          strings(source.permittedSubclasses), components, fields, builtMethods, synthetic);
    }
  }

  private static List<CompilationMetadata.VerificationFrame> frames(JavaMethod method) {
    List<CompilationMetadata.VerificationFrame> result = new ArrayList<CompilationMetadata.VerificationFrame>();
    if (method.code == null || method.code.stackMapFrames == null) {
      return result;
    }
    for (int i = 0; i < method.code.stackMapFrames.length; i++) {
      JavaStackMapFrame frame = method.code.stackMapFrames[i];
      result.add(new CompilationMetadata.VerificationFrame(frame.bytecodeOffset, types(frame.locals),
          types(frame.stack)));
    }
    return result;
  }

  private static List<CompilationMetadata.VerificationType> types(JavaVerificationType[] values) {
    List<CompilationMetadata.VerificationType> result = new ArrayList<CompilationMetadata.VerificationType>();
    for (int i = 0; i < values.length; i++) {
      JavaVerificationType value = values[i];
      result.add(new CompilationMetadata.VerificationType(value.kind.name(), value.className,
          value.newInstructionOffset));
    }
    return result;
  }
}
