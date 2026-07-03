// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import tc.tools.converter.bytecode.BC186_invokedynamic;
import tc.tools.converter.ir.Instruction.Call;
import tc.tools.converter.java.JavaBootstrapMethod;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.java.JavaMethodHandle;
import tc.tools.converter.oper.Operand;
import tc.tools.converter.oper.OperandRegI;
import tc.tools.converter.oper.OperandRegO;
import tc.tools.converter.oper.OperandSymO;
import totalcross.util.Vector;

public final class JavaObjectMethodsLowering implements TCConstants {
  private static final String BOOTSTRAP_OWNER = "java/lang/runtime/ObjectMethods";
  private static final String BOOTSTRAP_METHOD = "bootstrap";

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
    if ("toString".equals(site.name)) {
      requireArgumentCount(owner, site, arguments, 1);
      OperandRegO receiver = (OperandRegO) GenerateInstruction.promoteOperand(vcode, arguments[0], opr_regO, line);
      OperandRegO result = new OperandRegO();
      callObject(vcode, "toString", null, "toString()", receiver, new Operand[] { result }, line);
      return result;
    }
    if ("hashCode".equals(site.name)) {
      requireArgumentCount(owner, site, arguments, 1);
      OperandRegO receiver = (OperandRegO) GenerateInstruction.promoteOperand(vcode, arguments[0], opr_regO, line);
      OperandRegI result = new OperandRegI();
      callObject(vcode, "hashCode", null, "hashCode()", receiver, new Operand[] { result }, line);
      return result;
    }
    if ("equals".equals(site.name)) {
      requireArgumentCount(owner, site, arguments, 2);
      OperandRegO receiver = (OperandRegO) GenerateInstruction.promoteOperand(vcode, arguments[0], opr_regO, line);
      Operand other = GenerateInstruction.promoteOperand(vcode, arguments[1], opr_regO, line);
      OperandRegI result = new OperandRegI();
      callObject(vcode, "equals", new String[] { "Ljava/lang/Object;" }, "equals(Ljava/lang/Object;)", receiver,
          new Operand[] { result, other }, line);
      return result;
    }
    throw unsupported(owner, site, "unsupported ObjectMethods operation " + site.name);
  }

  private static void callObject(Vector vcode, String methodName, String[] params, String signature,
      OperandRegO receiver, Operand[] retAndParams, int line) {
    int methodIndex = GlobalConstantPool.putMethod("java/lang/Object", methodName, params, signature);
    Call call = (Call) GenerateInstruction.newInstruction(vcode, CALL_virtual, new OperandSymO(methodIndex),
        receiver, retAndParams, false, line);
    call.isStatic = false;
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
}
