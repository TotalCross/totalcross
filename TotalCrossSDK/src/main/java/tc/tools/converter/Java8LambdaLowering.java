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
    validateDescriptorAdaptation(owner, bytecode, site);
    validateBridgeDescriptors(owner, bytecode, site);
    validateImplementationDescriptor(owner, bytecode, site);
    if (!site.factoryReturnDescriptor.startsWith("L") || !site.factoryReturnDescriptor.endsWith(";")) {
      throw unsupported(owner, bytecode,
          "lambda call site must return an interface type, found " + site.factoryReturnDescriptor);
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
    generateBridgeMethods(cw, site);
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

  private static void generateBridgeMethods(ClassWriter cw, LambdaSite site) {
    for (int i = 0; i < site.bridgeDescriptors.length; i++) {
      String bridgeDescriptor = site.bridgeDescriptors[i];
      if (bridgeDescriptor.equals(site.samDescriptor)) {
        continue;
      }
      MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC, site.samMethodName,
          bridgeDescriptor, null, null);
      mv.visitCode();
      mv.visitVarInsn(ALOAD, 0);
      loadSamArguments(mv, Type.getArgumentTypes(bridgeDescriptor), 0);
      mv.visitMethodInsn(INVOKEVIRTUAL, site.adapterClassName, site.samMethodName, site.samDescriptor, false);
      adaptReference(mv, Type.getReturnType(site.samDescriptor), Type.getReturnType(bridgeDescriptor));
      mv.visitInsn(Type.getReturnType(bridgeDescriptor).getOpcode(IRETURN));
      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }
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
    Type[] samArgumentTypes = Type.getArgumentTypes(site.samDescriptor);
    Type[] instantiatedArgumentTypes = Type.getArgumentTypes(site.instantiatedDescriptor);
    Type[] implementationArgumentTypes = Type.getArgumentTypes(site.implementationDescriptor);
    if (site.implementationKind == JavaMethodHandle.REF_INVOKE_STATIC) {
      loadCapturedValues(mv, site, captureTypes, implementationArgumentTypes, 0, 0);
      loadSamArguments(mv, samArgumentTypes, instantiatedArgumentTypes,
          tail(implementationArgumentTypes, captureTypes.length), 0);
      mv.visitMethodInsn(INVOKESTATIC, site.implementationOwner, site.implementationName, site.implementationDescriptor,
          false);
    } else if (site.implementationKind == JavaMethodHandle.REF_NEW_INVOKE_SPECIAL) {
      mv.visitTypeInsn(NEW, site.implementationOwner);
      mv.visitInsn(DUP);
      loadCapturedValues(mv, site, captureTypes, implementationArgumentTypes, 0, 0);
      loadSamArguments(mv, samArgumentTypes, instantiatedArgumentTypes,
          tail(implementationArgumentTypes, captureTypes.length), 0);
      mv.visitMethodInsn(INVOKESPECIAL, site.implementationOwner, site.implementationName,
          site.implementationDescriptor, false);
    } else {
      ReceiverSource receiverSource = receiverSource(site, captureTypes, instantiatedArgumentTypes);
      if (receiverSource == ReceiverSource.CAPTURED) {
        loadCapturedValue(mv, site, captureTypes, 0, Type.getObjectType(site.implementationOwner));
        loadCapturedValues(mv, site, captureTypes, implementationArgumentTypes, 1, 0);
        loadSamArguments(mv, samArgumentTypes, instantiatedArgumentTypes,
            tail(implementationArgumentTypes, captureTypes.length - 1), 0);
      } else {
        loadSamArgument(mv, samArgumentTypes, 0, instantiatedArgumentTypes[0],
            Type.getObjectType(site.implementationOwner));
        loadCapturedValues(mv, site, captureTypes, implementationArgumentTypes, 0, 0);
        loadSamArguments(mv, samArgumentTypes, instantiatedArgumentTypes,
            tail(implementationArgumentTypes, captureTypes.length), 1);
      }
      mv.visitMethodInsn(invokeOpcode(site), site.implementationOwner, site.implementationName,
          site.implementationDescriptor, site.implementationKind == JavaMethodHandle.REF_INVOKE_INTERFACE);
    }
    adaptReturnValue(mv, site);
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
    loadCapturedValue(mv, site, captureTypes, index, captureTypes[index]);
  }

  private static void loadCapturedValue(MethodVisitor mv, LambdaSite site, Type[] captureTypes, int index,
      Type targetType) {
    Type captureType = captureTypes[index];
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, site.adapterClassName, captureFieldName(index), captureType.getDescriptor());
    adaptValue(mv, captureType, captureType, targetType);
  }

  private static void loadCapturedValues(MethodVisitor mv, LambdaSite site, Type[] sourceTypes, Type[] targetTypes,
      int start, int targetStart) {
    for (int i = start; i < sourceTypes.length; i++) {
      loadCapturedValue(mv, site, sourceTypes, i, targetTypes[targetStart + i - start]);
    }
  }

  private static void loadSamArguments(MethodVisitor mv, Type[] argumentTypes, int start) {
    for (int i = start; i < argumentTypes.length; i++) {
      loadSamArgument(mv, argumentTypes, i);
    }
  }

  private static void loadSamArguments(MethodVisitor mv, Type[] sourceTypes, Type[] targetTypes, int start) {
    for (int i = start; i < sourceTypes.length; i++) {
      loadSamArgument(mv, sourceTypes, i, targetTypes[i]);
    }
  }

  private static void loadSamArguments(MethodVisitor mv, Type[] sourceTypes, Type[] intermediateTypes,
      Type[] targetTypes, int start) {
    for (int i = start; i < sourceTypes.length; i++) {
      loadSamArgument(mv, sourceTypes, i, intermediateTypes[i], targetTypes[i - start]);
    }
  }

  private static void loadSamArgument(MethodVisitor mv, Type[] argumentTypes, int index) {
    loadSamArgument(mv, argumentTypes, index, argumentTypes[index]);
  }

  private static void loadSamArgument(MethodVisitor mv, Type[] argumentTypes, int index, Type targetType) {
    loadSamArgument(mv, argumentTypes, index, targetType, targetType);
  }

  private static void loadSamArgument(MethodVisitor mv, Type[] argumentTypes, int index, Type intermediateType,
      Type targetType) {
    int local = 1;
    for (int i = 0; i < index; i++) {
      local += argumentTypes[i].getSize();
    }
    mv.visitVarInsn(argumentTypes[index].getOpcode(ILOAD), local);
    adaptValue(mv, argumentTypes[index], intermediateType, targetType);
  }

  private static void adaptReturnValue(MethodVisitor mv, LambdaSite site) {
    Type sourceReturn = site.implementationKind == JavaMethodHandle.REF_NEW_INVOKE_SPECIAL
        ? Type.getObjectType(site.implementationOwner) : Type.getReturnType(site.implementationDescriptor);
    Type instantiatedReturn = Type.getReturnType(site.instantiatedDescriptor);
    Type samReturn = Type.getReturnType(site.samDescriptor);
    adaptValue(mv, sourceReturn, instantiatedReturn, samReturn);
  }

  private static void validateImplementationDescriptor(JavaClass owner, BC186_invokedynamic bytecode,
      LambdaSite site) {
    Type[] expectedArguments = expectedImplementationArguments(site);
    Type[] implementationArguments = Type.getArgumentTypes(site.implementationDescriptor);
    Type expectedReturn = Type.getReturnType(site.instantiatedDescriptor);
    Type implementationReturn = Type.getReturnType(site.implementationDescriptor);
    if (site.implementationKind == JavaMethodHandle.REF_NEW_INVOKE_SPECIAL) {
      String constructedType = "L" + site.implementationOwner + ";";
      if (!compatibleBridgeReturn(Type.getType(constructedType), expectedReturn)) {
        throw unsupported(owner, bytecode,
            "constructor reference return adaptation is not lowered yet; constructed " + constructedType
                + ", SAM return " + expectedReturn.getDescriptor());
      }
      expectedReturn = Type.VOID_TYPE;
    }
    if (!compatibleValueTypes(expectedArguments, implementationArguments)
        || !compatibleImplementationReturn(implementationReturn, expectedReturn)) {
      throw unsupported(owner, bytecode,
          "lambda method adaptation is not lowered yet; expected implementation "
              + descriptor(expectedArguments, expectedReturn) + ", found " + site.implementationDescriptor);
    }
  }

  private static Type[] expectedImplementationArguments(LambdaSite site) {
    if (site.implementationKind == JavaMethodHandle.REF_INVOKE_STATIC) {
      return concat(Type.getArgumentTypes(site.factoryDescriptorWithoutReturn + "V"),
          Type.getArgumentTypes(site.instantiatedDescriptor));
    }
    if (site.implementationKind == JavaMethodHandle.REF_NEW_INVOKE_SPECIAL) {
      return concat(Type.getArgumentTypes(site.factoryDescriptorWithoutReturn + "V"),
          Type.getArgumentTypes(site.instantiatedDescriptor));
    }
    Type[] captureTypes = Type.getArgumentTypes(site.factoryDescriptorWithoutReturn + "V");
    Type[] instantiatedTypes = Type.getArgumentTypes(site.instantiatedDescriptor);
    ReceiverSource receiverSource = receiverSource(site, captureTypes, instantiatedTypes);
    if (receiverSource == ReceiverSource.CAPTURED) {
      return concat(tail(captureTypes, 1), instantiatedTypes);
    }
    return concat(captureTypes, tail(instantiatedTypes, 1));
  }

  private static void validateBridgeDescriptors(JavaClass owner, BC186_invokedynamic bytecode, LambdaSite site) {
    Type[] samArguments = Type.getArgumentTypes(site.samDescriptor);
    Type samReturn = Type.getReturnType(site.samDescriptor);
    for (int i = 0; i < site.bridgeDescriptors.length; i++) {
      String bridgeDescriptor = site.bridgeDescriptors[i];
      Type[] bridgeArguments = Type.getArgumentTypes(bridgeDescriptor);
      Type bridgeReturn = Type.getReturnType(bridgeDescriptor);
      if (!sameTypes(samArguments, bridgeArguments) || !compatibleBridgeReturn(samReturn, bridgeReturn)) {
        throw unsupported(owner, bytecode,
            "altMetafactory bridge method adaptation is not lowered yet; SAM " + site.samDescriptor
                + ", bridge " + bridgeDescriptor);
      }
    }
  }

  private static void validateDescriptorAdaptation(JavaClass owner, BC186_invokedynamic bytecode, LambdaSite site) {
    Type[] samArguments = Type.getArgumentTypes(site.samDescriptor);
    Type[] instantiatedArguments = Type.getArgumentTypes(site.instantiatedDescriptor);
    Type samReturn = Type.getReturnType(site.samDescriptor);
    Type instantiatedReturn = Type.getReturnType(site.instantiatedDescriptor);
    if (!compatibleArgumentTypes(samArguments, instantiatedArguments)
        || !compatibleBridgeReturn(instantiatedReturn, samReturn)) {
      throw unsupported(owner, bytecode,
          "lambda method adaptation is not lowered yet; SAM " + site.samDescriptor + ", instantiated "
              + site.instantiatedDescriptor);
    }
  }

  private static boolean compatibleArgumentTypes(Type[] sourceTypes, Type[] targetTypes) {
    return compatibleValueTypes(sourceTypes, targetTypes);
  }

  private static boolean compatibleValueTypes(Type[] sourceTypes, Type[] targetTypes) {
    if (sourceTypes.length != targetTypes.length) {
      return false;
    }
    for (int i = 0; i < sourceTypes.length; i++) {
      if (!compatibleValueType(sourceTypes[i], targetTypes[i])) {
        return false;
      }
    }
    return true;
  }

  private static boolean sameTypes(Type[] left, Type[] right) {
    if (left.length != right.length) {
      return false;
    }
    for (int i = 0; i < left.length; i++) {
      if (!left[i].equals(right[i])) {
        return false;
      }
    }
    return true;
  }

  private static boolean compatibleBridgeReturn(Type samReturn, Type bridgeReturn) {
    if (samReturn.equals(bridgeReturn)) {
      return true;
    }
    return isReferenceType(samReturn) && isReferenceType(bridgeReturn);
  }

  private static boolean compatibleImplementationReturn(Type implementationReturn, Type expectedReturn) {
    return compatibleValueType(implementationReturn, expectedReturn);
  }

  private static boolean compatibleValueType(Type sourceType, Type targetType) {
    if (sourceType.equals(targetType)) {
      return true;
    }
    if (isReferenceType(sourceType) && isReferenceType(targetType)) {
      return true;
    }
    if (isReferenceType(sourceType) && isPrimitiveType(targetType)) {
      Type sourcePrimitive = primitiveForWrapper(sourceType);
      return sourcePrimitive != null && canWidenPrimitive(sourcePrimitive, targetType);
    }
    if (isPrimitiveType(sourceType) && isPrimitiveType(targetType)) {
      return canWidenPrimitive(sourceType, targetType);
    }
    if (isPrimitiveType(sourceType) && isReferenceType(targetType)) {
      Type targetPrimitive = primitiveForWrapper(targetType);
      return targetPrimitive == null ? "java/lang/Object".equals(targetType.getInternalName())
          : canWidenPrimitive(sourceType, targetPrimitive);
    }
    return false;
  }

  private static boolean isReferenceType(Type type) {
    return type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY;
  }

  private static boolean isPrimitiveType(Type type) {
    int sort = type.getSort();
    return sort >= Type.BOOLEAN && sort <= Type.DOUBLE;
  }

  private static void adaptReference(MethodVisitor mv, Type sourceType, Type targetType) {
    if (sourceType.equals(targetType) || !isReferenceType(sourceType) || !isReferenceType(targetType)) {
      return;
    }
    mv.visitTypeInsn(CHECKCAST, checkcastTypeName(targetType));
  }

  private static void adaptValue(MethodVisitor mv, Type sourceType, Type intermediateType, Type targetType) {
    Type currentType = adaptSingleValue(mv, sourceType, intermediateType);
    adaptSingleValue(mv, currentType, targetType);
  }

  private static Type adaptSingleValue(MethodVisitor mv, Type sourceType, Type targetType) {
    if (sourceType.equals(targetType)) {
      return targetType;
    }
    if (isReferenceType(sourceType) && isReferenceType(targetType)) {
      adaptReference(mv, sourceType, targetType);
      return targetType;
    }
    if (isReferenceType(sourceType) && isPrimitiveType(targetType)) {
      return unboxValue(mv, sourceType, targetType);
    }
    if (isPrimitiveType(sourceType) && isPrimitiveType(targetType)) {
      widenPrimitive(mv, sourceType, targetType);
      return targetType;
    }
    if (isPrimitiveType(sourceType) && isReferenceType(targetType)) {
      Type boxedType = boxValue(mv, sourceType, targetType);
      adaptReference(mv, boxedType, targetType);
      return targetType;
    }
    throw new ConverterException("Unsupported lambda descriptor adaptation from " + sourceType.getDescriptor()
        + " to " + targetType.getDescriptor());
  }

  private static Type unboxValue(MethodVisitor mv, Type sourceType, Type targetType) {
    Type sourcePrimitive = primitiveForWrapper(sourceType);
    Type wrapperType = sourcePrimitive == null ? wrapperForPrimitive(targetType) : sourceType;
    Type unboxedType = sourcePrimitive == null ? targetType : sourcePrimitive;
    mv.visitTypeInsn(CHECKCAST, wrapperType.getInternalName());
    mv.visitMethodInsn(INVOKEVIRTUAL, wrapperType.getInternalName(), unboxMethodName(unboxedType),
        "()" + unboxedType.getDescriptor(), false);
    widenPrimitive(mv, unboxedType, targetType);
    return targetType;
  }

  private static Type boxValue(MethodVisitor mv, Type sourceType, Type targetType) {
    Type targetPrimitive = primitiveForWrapper(targetType);
    Type boxedPrimitive = targetPrimitive == null ? sourceType : targetPrimitive;
    widenPrimitive(mv, sourceType, boxedPrimitive);
    Type boxedType = wrapperForPrimitive(boxedPrimitive);
    mv.visitMethodInsn(INVOKESTATIC, boxedType.getInternalName(), "valueOf",
        "(" + boxedPrimitive.getDescriptor() + ")" + boxedType.getDescriptor(), false);
    return boxedType;
  }

  private static boolean canWidenPrimitive(Type sourceType, Type targetType) {
    if (sourceType.equals(targetType)) {
      return true;
    }
    switch (sourceType.getSort()) {
    case Type.BYTE:
    case Type.SHORT:
    case Type.CHAR:
      return targetType.getSort() == Type.INT || targetType.getSort() == Type.LONG
          || targetType.getSort() == Type.FLOAT || targetType.getSort() == Type.DOUBLE;
    case Type.INT:
      return targetType.getSort() == Type.LONG || targetType.getSort() == Type.FLOAT
          || targetType.getSort() == Type.DOUBLE;
    case Type.LONG:
      return targetType.getSort() == Type.FLOAT || targetType.getSort() == Type.DOUBLE;
    case Type.FLOAT:
      return targetType.getSort() == Type.DOUBLE;
    default:
      return false;
    }
  }

  private static void widenPrimitive(MethodVisitor mv, Type sourceType, Type targetType) {
    if (sourceType.equals(targetType)) {
      return;
    }
    if (sourceType.getSort() == Type.BYTE || sourceType.getSort() == Type.SHORT
        || sourceType.getSort() == Type.CHAR) {
      sourceType = Type.INT_TYPE;
    }
    if (sourceType.getSort() == Type.INT) {
      if (targetType.getSort() == Type.LONG) {
        mv.visitInsn(I2L);
      } else if (targetType.getSort() == Type.FLOAT) {
        mv.visitInsn(I2F);
      } else if (targetType.getSort() == Type.DOUBLE) {
        mv.visitInsn(I2D);
      }
    } else if (sourceType.getSort() == Type.LONG) {
      if (targetType.getSort() == Type.FLOAT) {
        mv.visitInsn(L2F);
      } else if (targetType.getSort() == Type.DOUBLE) {
        mv.visitInsn(L2D);
      }
    } else if (sourceType.getSort() == Type.FLOAT && targetType.getSort() == Type.DOUBLE) {
      mv.visitInsn(F2D);
    }
  }

  private static Type wrapperForPrimitive(Type primitiveType) {
    switch (primitiveType.getSort()) {
    case Type.BOOLEAN:
      return Type.getObjectType("java/lang/Boolean");
    case Type.BYTE:
      return Type.getObjectType("java/lang/Byte");
    case Type.CHAR:
      return Type.getObjectType("java/lang/Character");
    case Type.SHORT:
      return Type.getObjectType("java/lang/Short");
    case Type.INT:
      return Type.getObjectType("java/lang/Integer");
    case Type.LONG:
      return Type.getObjectType("java/lang/Long");
    case Type.FLOAT:
      return Type.getObjectType("java/lang/Float");
    case Type.DOUBLE:
      return Type.getObjectType("java/lang/Double");
    default:
      throw new ConverterException("Unsupported primitive wrapper for " + primitiveType.getDescriptor());
    }
  }

  private static Type primitiveForWrapper(Type wrapperType) {
    if (!isReferenceType(wrapperType)) {
      return null;
    }
    String internalName = wrapperType.getInternalName();
    if ("java/lang/Boolean".equals(internalName)) {
      return Type.BOOLEAN_TYPE;
    }
    if ("java/lang/Byte".equals(internalName)) {
      return Type.BYTE_TYPE;
    }
    if ("java/lang/Character".equals(internalName)) {
      return Type.CHAR_TYPE;
    }
    if ("java/lang/Short".equals(internalName)) {
      return Type.SHORT_TYPE;
    }
    if ("java/lang/Integer".equals(internalName)) {
      return Type.INT_TYPE;
    }
    if ("java/lang/Long".equals(internalName)) {
      return Type.LONG_TYPE;
    }
    if ("java/lang/Float".equals(internalName)) {
      return Type.FLOAT_TYPE;
    }
    if ("java/lang/Double".equals(internalName)) {
      return Type.DOUBLE_TYPE;
    }
    return null;
  }

  private static String unboxMethodName(Type primitiveType) {
    switch (primitiveType.getSort()) {
    case Type.BOOLEAN:
      return "booleanValue";
    case Type.BYTE:
      return "byteValue";
    case Type.CHAR:
      return "charValue";
    case Type.SHORT:
      return "shortValue";
    case Type.INT:
      return "intValue";
    case Type.LONG:
      return "longValue";
    case Type.FLOAT:
      return "floatValue";
    case Type.DOUBLE:
      return "doubleValue";
    default:
      throw new ConverterException("Unsupported primitive unboxing for " + primitiveType.getDescriptor());
    }
  }

  private static String checkcastTypeName(Type type) {
    if (type.getSort() == Type.ARRAY) {
      return type.getDescriptor();
    }
    return type.getInternalName();
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

  private static String descriptor(Type[] argumentTypes, Type returnType) {
    return descriptor(argumentTypes, new Type[0], returnType);
  }

  private static Type[] concat(Type[] prefixTypes, Type[] suffixTypes) {
    Type[] result = new Type[prefixTypes.length + suffixTypes.length];
    System.arraycopy(prefixTypes, 0, result, 0, prefixTypes.length);
    System.arraycopy(suffixTypes, 0, result, prefixTypes.length, suffixTypes.length);
    return result;
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
