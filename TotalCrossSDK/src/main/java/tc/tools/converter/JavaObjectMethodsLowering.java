// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import tc.tools.converter.bytecode.BC186_invokedynamic;
import tc.tools.converter.ir.Instruction.Call;
import tc.tools.converter.java.JavaBootstrapMethod;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaConstantInfo;
import tc.tools.converter.java.JavaConstantPool;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.java.JavaMethodHandle;
import tc.tools.converter.java.JavaRecordComponent;
import tc.tools.converter.oper.Operand;
import tc.tools.converter.oper.OperandArrayAccess;
import tc.tools.converter.oper.OperandConstant32;
import tc.tools.converter.oper.OperandExternal;
import tc.tools.converter.oper.OperandReg;
import tc.tools.converter.oper.OperandRegD32;
import tc.tools.converter.oper.OperandRegD64;
import tc.tools.converter.oper.OperandRegI;
import tc.tools.converter.oper.OperandRegL;
import tc.tools.converter.oper.OperandRegO;
import tc.tools.converter.oper.OperandSym;
import tc.tools.converter.oper.OperandSymO;
import totalcross.util.Vector;

public final class JavaObjectMethodsLowering implements TCConstants {
  private static final String BOOTSTRAP_OWNER = "java/lang/runtime/ObjectMethods";
  private static final String BOOTSTRAP_METHOD = "bootstrap";
  private static final String HELPER_CLASS = "java/lang/runtime/ObjectMethods";

  private JavaObjectMethodsLowering() {
  }

  public static boolean isObjectMethodsFactory(JavaClass owner, BC186_invokedynamic site) {
    JavaBootstrapMethod bootstrap = bootstrapMethod(owner, site);
    if (bootstrap == null) {
      return false;
    }
    JavaMethodHandle handle = new JavaMethodHandle(owner.cp, bootstrap.bootstrapMethodRef);
    return BOOTSTRAP_OWNER.equals(handle.getOwner(owner.cp)) && BOOTSTRAP_METHOD.equals(handle.getName(owner.cp));
  }

  public static Operand lower(JavaClass owner, BC186_invokedynamic site, Operand[] arguments, Vector vcode, int line) {
    RecordComponents components = resolveComponents(owner, site);
    if ("toString".equals(site.name)) {
      requireArgumentCount(owner, site, arguments, 1);
      OperandRegO receiver = (OperandRegO) GenerateInstruction.promoteOperand(vcode, arguments[0], opr_regO, line);
      OperandRegO result = new OperandRegO();
      OperandRegO values = componentValues(owner, components, receiver, vcode, line);
      callStatic(vcode, "recordToString",
          new String[] { "Ljava/lang/String;", "Ljava/lang/String;", "[Ljava/lang/Object;" },
          "recordToString(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)", new Operand[] { result,
              new OperandSymO(GlobalConstantPool.putStr(recordDisplayName(owner.className))),
              new OperandSymO(GlobalConstantPool.putStr(components.names)), values }, line);
      return result;
    }
    if ("hashCode".equals(site.name)) {
      requireArgumentCount(owner, site, arguments, 1);
      OperandRegO receiver = (OperandRegO) GenerateInstruction.promoteOperand(vcode, arguments[0], opr_regO, line);
      OperandRegI result = new OperandRegI();
      OperandRegO values = componentValues(owner, components, receiver, vcode, line);
      callStatic(vcode, "recordHashCode", new String[] { "[Ljava/lang/Object;" },
          "recordHashCode([Ljava/lang/Object;)", new Operand[] { result, values }, line);
      return result;
    }
    if ("equals".equals(site.name)) {
      requireArgumentCount(owner, site, arguments, 2);
      OperandRegO receiver = (OperandRegO) GenerateInstruction.promoteOperand(vcode, arguments[0], opr_regO, line);
      Operand other = GenerateInstruction.promoteOperand(vcode, arguments[1], opr_regO, line);
      OperandRegI result = new OperandRegI();
      OperandRegO values = componentValues(owner, components, receiver, vcode, line);
      callStatic(vcode, "recordEquals",
          new String[] { "Ljava/lang/Object;", "Ljava/lang/Object;", "Ljava/lang/String;", "[Ljava/lang/Object;" },
          "recordEquals(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)",
          new Operand[] { result, receiver, other, new OperandSymO(GlobalConstantPool.putStr(components.names)),
              values },
          line);
      return result;
    }
    throw unsupported(owner, site, "unsupported ObjectMethods operation " + site.name);
  }

  private static RecordComponents resolveComponents(JavaClass owner, BC186_invokedynamic site) {
    if (owner.recordComponents == null) {
      throw unsupported(owner, site, "ObjectMethods bootstrap requires record component metadata");
    }
    JavaBootstrapMethod bootstrap = bootstrapMethod(owner, site);
    if (bootstrap == null) {
      throw unsupported(owner, site, "missing BootstrapMethods entry " + site.bootstrapMethodAttrIndex);
    }
    String names = owner.recordComponents.length == 0 ? "" : joinComponentNames(owner.recordComponents);
    if (bootstrap.bootstrapArguments.length >= 2) {
      String bootstrapNames = constantAsString(owner.cp, bootstrap.bootstrapArguments[1]);
      if (!bootstrapNames.equals(names)) {
        throw unsupported(owner, site,
            "ObjectMethods component list does not match Record attribute: " + bootstrapNames);
      }
      names = bootstrapNames;
    }
    return new RecordComponents(owner.recordComponents, names);
  }

  private static OperandRegO componentValues(JavaClass owner, RecordComponents components, OperandRegO receiver,
      Vector vcode, int line) {
    OperandRegO values = new OperandRegO();
    GenerateInstruction.newInstruction(vcode, pref_NEWARRAY, values,
        new OperandSymO(GlobalConstantPool.putClsOrParam("[Ljava/lang/Object;")),
        new OperandConstant32(components.components.length, type_Int), line);

    for (int i = 0; i < components.components.length; i++) {
      Operand value = componentValue(owner, components.components[i], receiver, vcode, line);
      OperandReg index = (OperandReg) GenerateInstruction.promoteOperand(vcode, new OperandConstant32(i, type_Int),
          opr_regI, line);
      GenerateInstruction.newInstruction(vcode, pref_MOV, new OperandArrayAccess(opr_arcO, values, index), value,
          line);
    }
    return values;
  }

  private static Operand componentValue(JavaClass owner, JavaRecordComponent component, OperandRegO receiver,
      Vector vcode, int line) {
    Operand value = fieldValue(owner, component, receiver, vcode, line);
    if (isPrimitive(component.descriptor)) {
      return boxPrimitive(component.descriptor, value, vcode, line);
    }
    return GenerateInstruction.promoteOperand(vcode, value, opr_regO, line);
  }

  private static Operand fieldValue(JavaClass owner, JavaRecordComponent component, OperandRegO receiver, Vector vcode,
      int line) {
    int kind;
    OperandReg target;
    switch (component.descriptor.charAt(0)) {
    case 'Z':
    case 'C':
    case 'B':
    case 'S':
    case 'I':
      kind = opr_fieldI;
      target = new OperandRegI();
      break;
    case 'J':
      kind = opr_fieldL;
      target = new OperandRegL();
      break;
    case 'F':
      kind = opr_fieldD;
      target = new OperandRegD32();
      break;
    case 'D':
      kind = opr_fieldD;
      target = new OperandRegD64();
      break;
    default:
      kind = opr_fieldO;
      target = new OperandRegO();
      break;
    }
    int index = GlobalConstantPool.putInstanceField(owner.className, component.name);
    Bytecode2TCCode.htInstanceFieldIndexes.put(index, index);
    GenerateInstruction.newInstruction(vcode, pref_MOV, target,
        new OperandExternal(receiver, new OperandSym(kind, index)), line);
    return target;
  }

  private static OperandRegO boxPrimitive(String descriptor, Operand value, Vector vcode, int line) {
    Wrapper wrapper = wrapper(descriptor);
    OperandRegO boxed = new OperandRegO();
    GenerateInstruction.newInstruction(vcode, pref_NEWOBJ, boxed,
        new OperandSymO(GlobalConstantPool.putCls(wrapper.className)), line);
    call(vcode, wrapper.className, "<init>", new String[] { wrapper.parameter }, "<init>(" + wrapper.parameter + ")",
        boxed, new Operand[] { value }, true, false, line);
    return boxed;
  }

  private static boolean isPrimitive(String descriptor) {
    char type = descriptor.charAt(0);
    return type != 'L' && type != '[';
  }

  private static Wrapper wrapper(String descriptor) {
    switch (descriptor.charAt(0)) {
    case 'Z':
      return new Wrapper("java/lang/Boolean", "Z");
    case 'C':
      return new Wrapper("java/lang/Character", "C");
    case 'B':
      return new Wrapper("java/lang/Byte", "B");
    case 'S':
      return new Wrapper("java/lang/Short", "S");
    case 'I':
      return new Wrapper("java/lang/Integer", "I");
    case 'J':
      return new Wrapper("java/lang/Long", "J");
    case 'F':
      return new Wrapper("java/lang/Float", "F");
    case 'D':
      return new Wrapper("java/lang/Double", "D");
    default:
      throw new ConverterException("Unsupported record primitive component descriptor " + descriptor);
    }
  }

  private static void callStatic(Vector vcode, String methodName, String[] params, String signature,
      Operand[] retAndParams, int line) {
    OperandReg target = new OperandReg(opr_regO);
    target.index = 0;
    call(vcode, HELPER_CLASS, methodName, params, signature, target, retAndParams, false, true, line);
  }

  private static void call(Vector vcode, String className, String methodName, String[] params, String signature,
      OperandReg target, Operand[] retAndParams, boolean isVoid, boolean isStatic, int line) {
    int methodIndex = GlobalConstantPool.putMethod(className, methodName, params, signature);
    Call call = (Call) GenerateInstruction.newInstruction(vcode, CALL_normal, new OperandSymO(methodIndex),
        target, retAndParams, isVoid, line);
    call.isStatic = isStatic;
  }

  private static String recordDisplayName(String className) {
    int slash = className.lastIndexOf('/');
    String simpleName = slash < 0 ? className : className.substring(slash + 1);
    int nested = simpleName.lastIndexOf('$');
    simpleName = nested < 0 ? simpleName : simpleName.substring(nested + 1);
    int firstLetter = 0;
    while (firstLetter < simpleName.length() && Character.isDigit(simpleName.charAt(firstLetter))) {
      firstLetter++;
    }
    return simpleName.substring(firstLetter);
  }

  private static String joinComponentNames(JavaRecordComponent[] components) {
    StringBuffer names = new StringBuffer();
    for (int i = 0; i < components.length; i++) {
      if (i > 0) {
        names.append(';');
      }
      names.append(components[i].name);
    }
    return names.toString();
  }

  private static String constantAsString(JavaConstantPool cp, int constantPoolIndex) {
    Object constant = cp.constants[constantPoolIndex];
    if (constant instanceof String) {
      return (String) constant;
    }
    if (constant instanceof JavaConstantInfo) {
      JavaConstantInfo info = (JavaConstantInfo) constant;
      if (info.type == 8) {
        return cp.getString1(info.index1);
      }
    }
    throw new ConverterException("Unsupported ObjectMethods constant argument at constant pool index "
        + constantPoolIndex);
  }

  private static void requireArgumentCount(JavaClass owner, BC186_invokedynamic site, Operand[] arguments,
      int expected) {
    if (arguments.length != expected) {
      throw unsupported(owner, site,
          "ObjectMethods operation " + site.name + " expects " + expected + " arguments, found " + arguments.length);
    }
  }

  private static JavaBootstrapMethod bootstrapMethod(JavaClass owner, BC186_invokedynamic site) {
    if (owner.bootstrapMethods == null || site.bootstrapMethodAttrIndex < 0
        || site.bootstrapMethodAttrIndex >= owner.bootstrapMethods.length) {
      return null;
    }
    return owner.bootstrapMethods[site.bootstrapMethodAttrIndex];
  }

  private static ConverterException unsupported(JavaClass owner, BC186_invokedynamic site, String reason) {
    String method = findMethod(owner, site);
    return new ConverterException("Unsupported invokedynamic in " + owner.className + "." + method
        + " at bytecode pc " + site.pcInMethod + ": " + reason);
  }

  private static String findMethod(JavaClass owner, BC186_invokedynamic site) {
    for (int i = 0; i < owner.methods.length; i++) {
      JavaMethod method = owner.methods[i];
      if (method.code == null || method.code.bcs == null) {
        continue;
      }
      for (int j = 0; j < method.code.bcs.length; j++) {
        if (method.code.bcs[j] == site) {
          return method.signature;
        }
      }
    }
    return "<unknown>";
  }

  private static final class RecordComponents {
    final JavaRecordComponent[] components;
    final String names;

    RecordComponents(JavaRecordComponent[] components, String names) {
      this.components = components;
      this.names = names;
    }
  }

  private static final class Wrapper {
    final String className;
    final String parameter;

    Wrapper(String className, String parameter) {
      this.className = className;
      this.parameter = parameter;
    }
  }
}
