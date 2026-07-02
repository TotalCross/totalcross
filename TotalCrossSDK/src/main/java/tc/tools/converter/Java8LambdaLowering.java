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
  private static final String FACTORY_PREFIX = "$$tc_lambda_factory$";

  private Java8LambdaLowering() {
  }

  public static LambdaSite resolve(JavaClass owner, BC186_invokedynamic site) {
    JavaBootstrapMethod bootstrapMethod = bootstrapMethod(owner, site);
    JavaMethodHandle bootstrapHandle = new JavaMethodHandle(owner.cp, bootstrapMethod.bootstrapMethodRef);
    if (!BOOTSTRAP_OWNER.equals(bootstrapHandle.getOwner(owner.cp))
        || !BOOTSTRAP_METHOD.equals(bootstrapHandle.getName(owner.cp))) {
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
    int ordinal = lambdaOrdinal(owner, site);
    return new LambdaSite(owner.className + "$$TC$$Lambda$" + ordinal, FACTORY_PREFIX + ordinal,
        site.descriptor.substring(0, site.descriptor.length() - site.ret.length()), site.ret, site.jargs, site.name,
        samDescriptor, implementation.referenceKind, implementation.getOwner(owner.cp), implementation.getName(owner.cp),
        implementation.getDescriptor(owner.cp), instantiatedDescriptor, site.pcInMethod);
  }

  public static JavaClass[] generateAdapterClasses(JavaClass owner) throws totalcross.io.IOException {
    BC186_invokedynamic[] sites = lambdaSites(owner);
    JavaClass[] adapters = new JavaClass[sites.length];
    for (int i = 0; i < sites.length; i++) {
      LambdaSite site = resolve(owner, sites[i]);
      validateSupportedStaticLambda(owner, sites[i], site);
      adapters[i] = new JavaClass(generateAdapterBytes(site), false);
    }
    return adapters;
  }

  public static boolean hasLambdaSites(JavaClass owner) {
    return lambdaSites(owner).length > 0;
  }

  public static void validateSupportedStaticLambda(JavaClass owner, BC186_invokedynamic bytecode, LambdaSite site) {
    if (site.implementationKind != JavaMethodHandle.REF_INVOKE_STATIC) {
      throw unsupported(owner, bytecode,
          "only REF_invokeStatic lambda implementations are lowered yet; reference kind is "
              + site.implementationKind);
    }
    String expectedImplementationDescriptor = capturedAndSamDescriptor(site);
    if (!expectedImplementationDescriptor.equals(site.implementationDescriptor)
        || !site.samDescriptor.equals(site.instantiatedDescriptor)) {
      throw unsupported(owner, bytecode,
          "lambda method adaptation is not lowered yet; SAM " + site.samDescriptor + ", implementation "
              + site.implementationDescriptor + ", instantiated " + site.instantiatedDescriptor);
    }
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
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    cw.visit(V1_7, ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_SYNTHETIC, site.adapterClassName, null,
        "java/lang/Object", new String[] { functionalInterface });
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
    for (int i = 0; i < captureTypes.length; i++) {
      Type captureType = captureTypes[i];
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, site.adapterClassName, captureFieldName(i), captureType.getDescriptor());
    }
    Type[] argumentTypes = Type.getArgumentTypes(site.samDescriptor);
    int local = 1;
    for (int i = 0; i < argumentTypes.length; i++) {
      Type argumentType = argumentTypes[i];
      mv.visitVarInsn(argumentType.getOpcode(ILOAD), local);
      local += argumentType.getSize();
    }
    mv.visitMethodInsn(INVOKESTATIC, site.implementationOwner, site.implementationName, site.implementationDescriptor,
        false);
    mv.visitInsn(Type.getReturnType(site.samDescriptor).getOpcode(IRETURN));
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private static String capturedAndSamDescriptor(LambdaSite site) {
    return site.factoryDescriptorWithoutReturn.substring(0, site.factoryDescriptorWithoutReturn.length() - 1)
        + site.samDescriptor.substring(1);
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
        && BOOTSTRAP_METHOD.equals(bootstrapHandle.getName(owner.cp));
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
    public final int bytecodeIndex;

    private LambdaSite(String adapterClassName, String factoryMethodName, String factoryDescriptorWithoutReturn,
        String factoryReturnDescriptor, String[] factoryParams, String samMethodName, String samDescriptor,
        int implementationKind, String implementationOwner, String implementationName, String implementationDescriptor,
        String instantiatedDescriptor, int bytecodeIndex) {
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
      this.bytecodeIndex = bytecodeIndex;
    }
  }
}
