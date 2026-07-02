// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import tc.tools.converter.bytecode.BC186_invokedynamic;
import tc.tools.converter.java.JavaBootstrapMethod;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaConstantInfo;
import tc.tools.converter.java.JavaConstantPool;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.java.JavaMethodHandle;

public final class Java8LambdaLowering implements Opcodes {
  private static final String BOOTSTRAP_OWNER = "java/lang/invoke/LambdaMetafactory";
  private static final String BOOTSTRAP_METHOD = "metafactory";
  private static final String ALT_BOOTSTRAP_METHOD = "altMetafactory";
  private static final String FACTORY_PREFIX = "$$tc_lambda_factory$";
  private static final int FLAG_SERIALIZABLE = 1;
  private static final int FLAG_MARKERS = 2;
  private static final int FLAG_BRIDGES = 4;

  private Java8LambdaLowering() {
  }

  public static LambdaSite resolve(JavaClass owner, BC186_invokedynamic site) {
    JavaBootstrapMethod bootstrapMethod = bootstrapMethod(owner, site);
    JavaMethodHandle bootstrapHandle = new JavaMethodHandle(owner.cp, bootstrapMethod.bootstrapMethodRef);
    if (!BOOTSTRAP_OWNER.equals(bootstrapHandle.getOwner(owner.cp))
        || !isSupportedBootstrapMethod(bootstrapHandle.getName(owner.cp))) {
      throw unsupported(owner, site,
          "unsupported invokedynamic bootstrap " + bootstrapHandle.getOwner(owner.cp) + "."
              + bootstrapHandle.getName(owner.cp));
    }
    if (bootstrapMethod.bootstrapArguments.length < 3) {
      throw unsupported(owner, site, "LambdaMetafactory.metafactory requires three bootstrap arguments");
    }

    String samDescriptor = methodTypeDescriptor(owner.cp, bootstrapMethod.bootstrapArguments[0]);
    JavaMethodHandle implementation = new JavaMethodHandle(owner.cp, bootstrapMethod.bootstrapArguments[1]);
    String instantiatedDescriptor = methodTypeDescriptor(owner.cp, bootstrapMethod.bootstrapArguments[2]);
    AltMetafactoryOptions options = parseAltMetafactoryOptions(owner, site, bootstrapHandle, bootstrapMethod);
    int ordinal = lambdaOrdinal(owner, site);
    return new LambdaSite(owner.className + "$$TC$$Lambda$" + ordinal, FACTORY_PREFIX + ordinal,
        site.descriptor.substring(0, site.descriptor.length() - site.ret.length()), site.ret, site.jargs, site.name,
        samDescriptor, implementation.referenceKind, implementation.getOwner(owner.cp), implementation.getName(owner.cp),
        implementation.getDescriptor(owner.cp), instantiatedDescriptor, options.markerInterfaces,
        options.bridgeDescriptors, site.pcInMethod);
  }

  public static JavaClass[] generateAdapterClasses(JavaClass owner) throws totalcross.io.IOException {
    BC186_invokedynamic[] sites = lambdaSites(owner);
    JavaClass[] adapters = new JavaClass[sites.length];
    for (int i = 0; i < sites.length; i++) {
      LambdaSite site = resolve(owner, sites[i]);
      validateSupportedLambdaMetafactory(owner, sites[i], site);
      adapters[i] = new JavaClass(generateAdapterBytes(site), false);
    }
    return adapters;
  }

  public static boolean hasLambdaSites(JavaClass owner) {
    return lambdaSites(owner).length > 0;
  }

  public static void validateSupportedLambdaMetafactory(JavaClass owner, BC186_invokedynamic bytecode,
      LambdaSite site) {
    if (!site.samDescriptor.equals(site.instantiatedDescriptor)) {
      throw unsupported(owner, bytecode,
          "lambda method adaptation is not lowered yet; SAM " + site.samDescriptor + ", instantiated "
              + site.instantiatedDescriptor);
    }
    if (!site.factoryReturnDescriptor.startsWith("L") || !site.factoryReturnDescriptor.endsWith(";")) {
      throw unsupported(owner, bytecode,
          "lambda call site must return an interface type, found " + site.factoryReturnDescriptor);
    }
    if (site.bridgeDescriptors.length > 0) {
      throw unsupported(owner, bytecode,
          "altMetafactory bridge methods are not lowered yet; bridge count is " + site.bridgeDescriptors.length);
    }
    if (!expectedImplementationDescriptor(site).equals(site.implementationDescriptor)) {
      throw unsupported(owner, bytecode,
          "lambda method adaptation is not lowered yet; expected implementation "
              + expectedImplementationDescriptor(site) + ", found " + site.implementationDescriptor);
    }
  }

  public static String factorySignature(LambdaSite site) {
    return site.factoryMethodName + site.factoryDescriptorWithoutReturn;
  }

  private static byte[] generateAdapterBytes(LambdaSite site) {
    String functionalInterface = site.factoryReturnDescriptor.substring(1, site.factoryReturnDescriptor.length() - 1);
    String[] interfaces = new String[site.markerInterfaces.length + 1];
    interfaces[0] = functionalInterface;
    System.arraycopy(site.markerInterfaces, 0, interfaces, 1, site.markerInterfaces.length);
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    cw.visit(V1_7, ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_SYNTHETIC, site.adapterClassName, null,
        "java/lang/Object", interfaces);
    generateCaptureFields(cw, site);
    generateConstructor(cw, site);
    generateFactory(cw, site);
    generateSamMethod(cw, site);
    cw.visitEnd();
    return cw.toByteArray();
  }

  private static void generateCaptureFields(ClassWriter cw, LambdaSite site) {
    Type[] captureTypes = Type.getArgumentTypes(site.factoryDescriptorWithoutReturn + "V");
    for (int i = 0; i < captureTypes.length; i++) {
      cw.visitField(ACC_PRIVATE | ACC_FINAL, captureFieldName(i), captureTypes[i].getDescriptor(), null, null)
          .visitEnd();
    }
  }

  private static void generateConstructor(ClassWriter cw, LambdaSite site) {
    String constructorDescriptor = site.factoryDescriptorWithoutReturn + "V";
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", constructorDescriptor, null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    Type[] captureTypes = Type.getArgumentTypes(constructorDescriptor);
    int local = 1;
    for (int i = 0; i < captureTypes.length; i++) {
      Type captureType = captureTypes[i];
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(captureType.getOpcode(ILOAD), local);
      mv.visitFieldInsn(PUTFIELD, site.adapterClassName, captureFieldName(i), captureType.getDescriptor());
      local += captureType.getSize();
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private static void generateFactory(ClassWriter cw, LambdaSite site) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC, site.factoryMethodName,
        site.factoryDescriptorWithoutReturn + site.factoryReturnDescriptor, null, null);
    mv.visitCode();
    mv.visitTypeInsn(NEW, site.adapterClassName);
    mv.visitInsn(DUP);
    Type[] captureTypes = Type.getArgumentTypes(site.factoryDescriptorWithoutReturn + "V");
    int local = 0;
    for (int i = 0; i < captureTypes.length; i++) {
      Type captureType = captureTypes[i];
      mv.visitVarInsn(captureType.getOpcode(ILOAD), local);
      local += captureType.getSize();
    }
    mv.visitMethodInsn(INVOKESPECIAL, site.adapterClassName, "<init>", site.factoryDescriptorWithoutReturn + "V",
        false);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private static void generateSamMethod(ClassWriter cw, LambdaSite site) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, site.samMethodName, site.samDescriptor, null, null);
    mv.visitCode();
    Type[] captureTypes = Type.getArgumentTypes(site.factoryDescriptorWithoutReturn + "V");
    Type[] argumentTypes = Type.getArgumentTypes(site.samDescriptor);
    if (site.implementationKind == JavaMethodHandle.REF_INVOKE_STATIC) {
      loadCapturedValues(mv, site, captureTypes, 0);
      loadSamArguments(mv, argumentTypes, 0);
      mv.visitMethodInsn(INVOKESTATIC, site.implementationOwner, site.implementationName, site.implementationDescriptor,
          false);
    } else if (site.implementationKind == JavaMethodHandle.REF_NEW_INVOKE_SPECIAL) {
      mv.visitTypeInsn(NEW, site.implementationOwner);
      mv.visitInsn(DUP);
      loadCapturedValues(mv, site, captureTypes, 0);
      loadSamArguments(mv, argumentTypes, 0);
      mv.visitMethodInsn(INVOKESPECIAL, site.implementationOwner, site.implementationName,
          site.implementationDescriptor, false);
    } else {
      ReceiverSource receiverSource = receiverSource(site, captureTypes, argumentTypes);
      if (receiverSource == ReceiverSource.CAPTURED) {
        loadCapturedValue(mv, site, captureTypes, 0);
        loadCapturedValues(mv, site, captureTypes, 1);
        loadSamArguments(mv, argumentTypes, 0);
      } else {
        loadSamArgument(mv, argumentTypes, 0);
        loadCapturedValues(mv, site, captureTypes, 0);
        loadSamArguments(mv, argumentTypes, 1);
      }
      mv.visitMethodInsn(invokeOpcode(site), site.implementationOwner, site.implementationName,
          site.implementationDescriptor, site.implementationKind == JavaMethodHandle.REF_INVOKE_INTERFACE);
    }
    mv.visitInsn(Type.getReturnType(site.samDescriptor).getOpcode(IRETURN));
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private static void loadCapturedValues(MethodVisitor mv, LambdaSite site, Type[] captureTypes, int start) {
    for (int i = start; i < captureTypes.length; i++) {
      loadCapturedValue(mv, site, captureTypes, i);
    }
  }

  private static void loadCapturedValue(MethodVisitor mv, LambdaSite site, Type[] captureTypes, int index) {
    Type captureType = captureTypes[index];
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, site.adapterClassName, captureFieldName(index), captureType.getDescriptor());
  }

  private static void loadSamArguments(MethodVisitor mv, Type[] argumentTypes, int start) {
    for (int i = start; i < argumentTypes.length; i++) {
      loadSamArgument(mv, argumentTypes, i);
    }
  }

  private static void loadSamArgument(MethodVisitor mv, Type[] argumentTypes, int index) {
    int local = 1;
    for (int i = 0; i < index; i++) {
      local += argumentTypes[i].getSize();
    }
    mv.visitVarInsn(argumentTypes[index].getOpcode(ILOAD), local);
  }

  private static String expectedImplementationDescriptor(LambdaSite site) {
    Type returnType = Type.getReturnType(site.samDescriptor);
    if (site.implementationKind == JavaMethodHandle.REF_INVOKE_STATIC) {
      return descriptor(Type.getArgumentTypes(site.factoryDescriptorWithoutReturn + "V"),
          Type.getArgumentTypes(site.samDescriptor), returnType);
    }
    if (site.implementationKind == JavaMethodHandle.REF_NEW_INVOKE_SPECIAL) {
      String constructedType = "L" + site.implementationOwner + ";";
      if (!constructedType.equals(returnType.getDescriptor())) {
        throw new ConverterException("Unsupported constructor reference adaptation from " + constructedType
            + " to " + returnType.getDescriptor());
      }
      return descriptor(Type.getArgumentTypes(site.factoryDescriptorWithoutReturn + "V"),
          Type.getArgumentTypes(site.samDescriptor), Type.VOID_TYPE);
    }
    Type[] captureTypes = Type.getArgumentTypes(site.factoryDescriptorWithoutReturn + "V");
    Type[] samTypes = Type.getArgumentTypes(site.samDescriptor);
    ReceiverSource receiverSource = receiverSource(site, captureTypes, samTypes);
    if (receiverSource == ReceiverSource.CAPTURED) {
      return descriptor(tail(captureTypes, 1), samTypes, returnType);
    }
    return descriptor(captureTypes, tail(samTypes, 1), returnType);
  }

  private static String descriptor(Type[] prefixTypes, Type[] suffixTypes, Type returnType) {
    StringBuffer descriptor = new StringBuffer();
    descriptor.append('(');
    for (int i = 0; i < prefixTypes.length; i++) {
      descriptor.append(prefixTypes[i].getDescriptor());
    }
    for (int i = 0; i < suffixTypes.length; i++) {
      descriptor.append(suffixTypes[i].getDescriptor());
    }
    descriptor.append(')').append(returnType.getDescriptor());
    return descriptor.toString();
  }

  private static Type[] tail(Type[] types, int start) {
    Type[] tail = new Type[types.length - start];
    System.arraycopy(types, start, tail, 0, tail.length);
    return tail;
  }

  private static ReceiverSource receiverSource(LambdaSite site, Type[] captureTypes, Type[] samTypes) {
    String receiverDescriptor = "L" + site.implementationOwner + ";";
    if (captureTypes.length > 0 && receiverDescriptor.equals(captureTypes[0].getDescriptor())) {
      return ReceiverSource.CAPTURED;
    }
    if (samTypes.length > 0 && receiverDescriptor.equals(samTypes[0].getDescriptor())) {
      return ReceiverSource.SAM_ARGUMENT;
    }
    throw new ConverterException("Unsupported invokedynamic in " + site.implementationOwner + "."
        + site.implementationName + ": instance method reference does not expose receiver " + receiverDescriptor);
  }

  private static int invokeOpcode(LambdaSite site) {
    switch (site.implementationKind) {
    case JavaMethodHandle.REF_INVOKE_VIRTUAL:
      return INVOKEVIRTUAL;
    case JavaMethodHandle.REF_INVOKE_INTERFACE:
      return INVOKEINTERFACE;
    case JavaMethodHandle.REF_INVOKE_SPECIAL:
      return INVOKESPECIAL;
    default:
      throw new ConverterException("Unsupported invokedynamic implementation reference kind "
          + site.implementationKind);
    }
  }

  private static String captureFieldName(int index) {
    return "arg$" + index;
  }

  private static JavaBootstrapMethod bootstrapMethod(JavaClass owner, BC186_invokedynamic site) {
    if (owner.bootstrapMethods == null || site.bootstrapMethodAttrIndex >= owner.bootstrapMethods.length) {
      throw unsupported(owner, site, "missing BootstrapMethods entry " + site.bootstrapMethodAttrIndex);
    }
    return owner.bootstrapMethods[site.bootstrapMethodAttrIndex];
  }

  private static String methodTypeDescriptor(JavaConstantPool cp, int constantPoolIndex) {
    JavaConstantInfo info = (JavaConstantInfo) cp.constants[constantPoolIndex];
    if (info.type != 16) {
      throw new IllegalArgumentException("Constant pool entry " + constantPoolIndex + " is not a MethodType");
    }
    return cp.getString1(info.index1);
  }

  private static BC186_invokedynamic[] lambdaSites(JavaClass owner) {
    totalcross.util.Vector sites = new totalcross.util.Vector(4);
    if (owner.methods != null) {
      for (int i = 0; i < owner.methods.length; i++) {
        JavaMethod method = owner.methods[i];
        if (method.code == null || method.code.bcs == null) {
          continue;
        }
        for (int j = 0; j < method.code.bcs.length; j++) {
          if (method.code.bcs[j] instanceof BC186_invokedynamic) {
            BC186_invokedynamic site = (BC186_invokedynamic) method.code.bcs[j];
            if (isLambdaMetafactory(owner, site)) {
              sites.addElement(site);
            }
          }
        }
      }
    }
    BC186_invokedynamic[] result = new BC186_invokedynamic[sites.size()];
    sites.copyInto(result);
    return result;
  }

  private static boolean isLambdaMetafactory(JavaClass owner, BC186_invokedynamic site) {
    JavaBootstrapMethod bootstrapMethod = bootstrapMethod(owner, site);
    JavaMethodHandle bootstrapHandle = new JavaMethodHandle(owner.cp, bootstrapMethod.bootstrapMethodRef);
    return BOOTSTRAP_OWNER.equals(bootstrapHandle.getOwner(owner.cp))
        && isSupportedBootstrapMethod(bootstrapHandle.getName(owner.cp));
  }

  private static boolean isSupportedBootstrapMethod(String name) {
    return BOOTSTRAP_METHOD.equals(name) || ALT_BOOTSTRAP_METHOD.equals(name);
  }

  private static AltMetafactoryOptions parseAltMetafactoryOptions(JavaClass owner, BC186_invokedynamic site,
      JavaMethodHandle bootstrapHandle, JavaBootstrapMethod bootstrapMethod) {
    if (BOOTSTRAP_METHOD.equals(bootstrapHandle.getName(owner.cp))) {
      return new AltMetafactoryOptions(new String[0], new String[0]);
    }
    int[] bootstrapArguments = bootstrapMethod.bootstrapArguments;
    if (bootstrapArguments.length < 4) {
      throw unsupported(owner, site, "LambdaMetafactory.altMetafactory requires flags after the three fixed arguments");
    }
    int cursor = 3;
    int flags = intConstant(owner.cp, bootstrapArguments[cursor++]);
    totalcross.util.Vector markerInterfaces = new totalcross.util.Vector(2);
    totalcross.util.Vector bridgeDescriptors = new totalcross.util.Vector(2);
    if ((flags & FLAG_SERIALIZABLE) != 0) {
      markerInterfaces.addElement("java/io/Serializable");
    }
    if ((flags & FLAG_MARKERS) != 0) {
      int markerCount = intConstant(owner.cp, bootstrapArguments[cursor++]);
      for (int i = 0; i < markerCount; i++) {
        markerInterfaces.addElement(owner.cp.getString1(bootstrapArguments[cursor++]));
      }
    }
    if ((flags & FLAG_BRIDGES) != 0) {
      int bridgeCount = intConstant(owner.cp, bootstrapArguments[cursor++]);
      for (int i = 0; i < bridgeCount; i++) {
        bridgeDescriptors.addElement(methodTypeDescriptor(owner.cp, bootstrapArguments[cursor++]));
      }
    }
    return new AltMetafactoryOptions(toStringArray(markerInterfaces), toStringArray(bridgeDescriptors));
  }

  private static int intConstant(JavaConstantPool cp, int constantPoolIndex) {
    Object constant = cp.constants[constantPoolIndex];
    if (!(constant instanceof Integer)) {
      throw new IllegalArgumentException("Constant pool entry " + constantPoolIndex + " is not an Integer");
    }
    return ((Integer) constant).intValue();
  }

  private static String[] toStringArray(totalcross.util.Vector vector) {
    if (vector.size() == 0) {
      return new String[0];
    }
    return (String[]) vector.toObjectArray();
  }

  private static int lambdaOrdinal(JavaClass owner, BC186_invokedynamic target) {
    int ordinal = 0;
    if (owner.methods != null) {
      for (int i = 0; i < owner.methods.length; i++) {
        JavaMethod method = owner.methods[i];
        if (method.code == null || method.code.bcs == null) {
          continue;
        }
        for (int j = 0; j < method.code.bcs.length; j++) {
          if (method.code.bcs[j] instanceof BC186_invokedynamic) {
            BC186_invokedynamic site = (BC186_invokedynamic) method.code.bcs[j];
            if (isLambdaMetafactory(owner, site)) {
              if (site == target) {
                return ordinal;
              }
              ordinal++;
            }
          }
        }
      }
    }
    throw unsupported(owner, target, "lambda call site was not found in owner class");
  }

  private static ConverterException unsupported(JavaClass owner, BC186_invokedynamic site, String reason) {
    String method = site.jc != null && site.jc.method != null ? site.jc.method.signature : "<unknown>";
    return new ConverterException("Unsupported invokedynamic in " + owner.className + "." + method
        + " at bytecode index " + site.pcInMethod + ": " + reason);
  }

  public static final class LambdaSite {
    public final String adapterClassName;
    public final String factoryMethodName;
    public final String factoryDescriptorWithoutReturn;
    public final String factoryReturnDescriptor;
    public final String[] factoryParams;
    public final String samMethodName;
    public final String samDescriptor;
    public final int implementationKind;
    public final String implementationOwner;
    public final String implementationName;
    public final String implementationDescriptor;
    public final String instantiatedDescriptor;
    public final String[] markerInterfaces;
    public final String[] bridgeDescriptors;
    public final int bytecodeIndex;

    private LambdaSite(String adapterClassName, String factoryMethodName, String factoryDescriptorWithoutReturn,
        String factoryReturnDescriptor, String[] factoryParams, String samMethodName, String samDescriptor,
        int implementationKind, String implementationOwner, String implementationName, String implementationDescriptor,
        String instantiatedDescriptor, String[] markerInterfaces, String[] bridgeDescriptors, int bytecodeIndex) {
      this.adapterClassName = adapterClassName;
      this.factoryMethodName = factoryMethodName;
      this.factoryDescriptorWithoutReturn = factoryDescriptorWithoutReturn;
      this.factoryReturnDescriptor = factoryReturnDescriptor;
      this.factoryParams = factoryParams;
      this.samMethodName = samMethodName;
      this.samDescriptor = samDescriptor;
      this.implementationKind = implementationKind;
      this.implementationOwner = implementationOwner;
      this.implementationName = implementationName;
      this.implementationDescriptor = implementationDescriptor;
      this.instantiatedDescriptor = instantiatedDescriptor;
      this.markerInterfaces = markerInterfaces;
      this.bridgeDescriptors = bridgeDescriptors;
      this.bytecodeIndex = bytecodeIndex;
    }
  }

  private static final class AltMetafactoryOptions {
    final String[] markerInterfaces;
    final String[] bridgeDescriptors;

    AltMetafactoryOptions(String[] markerInterfaces, String[] bridgeDescriptors) {
      this.markerInterfaces = markerInterfaces;
      this.bridgeDescriptors = bridgeDescriptors;
    }
  }

  private enum ReceiverSource {
    CAPTURED,
    SAM_ARGUMENT
  }
}
