// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import tc.tools.converter.bytecode.BC186_invokedynamic;
import tc.tools.converter.java.JavaBootstrapMethod;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaConstantInfo;
import tc.tools.converter.java.JavaConstantPool;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.java.JavaMethodHandle;
import tc.tools.converter.oper.Operand;
import tc.tools.converter.oper.OperandRegO;
import tc.tools.converter.oper.OperandSymO;
import totalcross.util.Vector;

public final class JavaStringConcatLowering implements TCConstants {
  private static final String BOOTSTRAP_OWNER = "java/lang/invoke/StringConcatFactory";
  private static final String MAKE_CONCAT = "makeConcat";
  private static final String MAKE_CONCAT_WITH_CONSTANTS = "makeConcatWithConstants";
  private static final char ARGUMENT_PLACEHOLDER = '\u0001';
  private static final char CONSTANT_PLACEHOLDER = '\u0002';

  private JavaStringConcatLowering() {
  }

  public static boolean isStringConcatFactory(JavaClass owner, BC186_invokedynamic site) {
    JavaBootstrapMethod bootstrap = bootstrapMethod(owner, site);
    if (bootstrap == null) {
      return false;
    }
    JavaMethodHandle handle = new JavaMethodHandle(owner.cp, bootstrap.bootstrapMethodRef);
    return BOOTSTRAP_OWNER.equals(handle.getOwner(owner.cp));
  }

  public static OperandRegO lower(JavaClass owner, BC186_invokedynamic site, Operand[] arguments, Vector vcode,
      int line) {
    ConcatSite concat = resolve(owner, site);
    OperandRegO builder = new OperandRegO();
    newObject(vcode, builder, "java/lang/StringBuffer", line);
    call(vcode, "java/lang/StringBuffer", "<init>", null, "<init>()", builder, new Operand[0], true, line);

    ArgumentCursor cursor = new ArgumentCursor(arguments, site.jargs == null ? new String[0] : site.jargs,
        concat.constants);
    StringBuffer literal = new StringBuffer();
    for (int i = 0; i < concat.recipe.length(); i++) {
      char ch = concat.recipe.charAt(i);
      if (ch == ARGUMENT_PLACEHOLDER || ch == CONSTANT_PLACEHOLDER) {
        builder = appendLiteral(vcode, builder, literal, line);
        literal.setLength(0);
        if (ch == ARGUMENT_PLACEHOLDER) {
          builder = append(vcode, builder, cursor.nextArgument(), cursor.nextArgumentDescriptor(), line);
        } else {
          builder = appendString(vcode, builder, cursor.nextConstant(), line);
        }
      } else {
        literal.append(ch);
      }
    }
    builder = appendLiteral(vcode, builder, literal, line);

    OperandRegO result = new OperandRegO();
    call(vcode, "java/lang/StringBuffer", "toString", null, "toString()", builder, new Operand[] { result }, false,
        line);
    cursor.validateFullyConsumed(owner, site);
    return result;
  }

  private static ConcatSite resolve(JavaClass owner, BC186_invokedynamic site) {
    if (!"Ljava/lang/String;".equals(site.ret)) {
      throw unsupported(owner, site, "StringConcatFactory site must return java.lang.String");
    }
    JavaBootstrapMethod bootstrap = bootstrapMethod(owner, site);
    if (bootstrap == null) {
      throw unsupported(owner, site, "missing BootstrapMethods entry " + site.bootstrapMethodAttrIndex);
    }
    JavaMethodHandle handle = new JavaMethodHandle(owner.cp, bootstrap.bootstrapMethodRef);
    if (!BOOTSTRAP_OWNER.equals(handle.getOwner(owner.cp))) {
      throw unsupported(owner, site,
          "unsupported invokedynamic bootstrap " + handle.getOwner(owner.cp) + "." + handle.getName(owner.cp));
    }

    String methodName = handle.getName(owner.cp);
    if (MAKE_CONCAT.equals(methodName)) {
      return new ConcatSite(argumentOnlyRecipe(site), new String[0]);
    }
    if (MAKE_CONCAT_WITH_CONSTANTS.equals(methodName)) {
      if (bootstrap.bootstrapArguments.length == 0) {
        throw unsupported(owner, site, "StringConcatFactory.makeConcatWithConstants requires a recipe argument");
      }
      String recipe = constantAsString(owner.cp, bootstrap.bootstrapArguments[0]);
      String[] constants = new String[bootstrap.bootstrapArguments.length - 1];
      for (int i = 1; i < bootstrap.bootstrapArguments.length; i++) {
        constants[i - 1] = constantAsString(owner.cp, bootstrap.bootstrapArguments[i]);
      }
      return new ConcatSite(recipe, constants);
    }
    throw unsupported(owner, site, "unsupported StringConcatFactory method " + methodName);
  }

  private static String argumentOnlyRecipe(BC186_invokedynamic site) {
    String[] args = site.jargs == null ? new String[0] : site.jargs;
    StringBuffer recipe = new StringBuffer(args.length);
    for (int i = 0; i < args.length; i++) {
      recipe.append(ARGUMENT_PLACEHOLDER);
    }
    return recipe.toString();
  }

  private static OperandRegO appendLiteral(Vector vcode, OperandRegO builder, StringBuffer literal, int line) {
    if (literal.length() == 0) {
      return builder;
    }
    return appendString(vcode, builder, literal.toString(), line);
  }

  private static OperandRegO appendString(Vector vcode, OperandRegO builder, String value, int line) {
    return append(vcode, builder, new OperandSymO(GlobalConstantPool.putStr(value)), "Ljava/lang/String;", line);
  }

  private static OperandRegO append(Vector vcode, OperandRegO builder, Operand value, String descriptor, int line) {
    String appendDescriptor = appendDescriptor(descriptor);
    OperandRegO result = new OperandRegO();
    call(vcode, "java/lang/StringBuffer", "append", new String[] { appendDescriptor },
        "append(" + appendDescriptor + ")", builder, new Operand[] { result, value }, false, line);
    return result;
  }

  private static String appendDescriptor(String descriptor) {
    if (descriptor == null || descriptor.length() == 0) {
      return "Ljava/lang/Object;";
    }
    switch (descriptor.charAt(0)) {
    case 'Z':
    case 'C':
    case 'I':
    case 'J':
    case 'D':
      return descriptor;
    case 'B':
    case 'S':
      return "I";
    case 'F':
      return "D";
    case 'L':
      return "Ljava/lang/String;".equals(descriptor) ? descriptor : "Ljava/lang/Object;";
    case '[':
      return "Ljava/lang/Object;";
    default:
      return "Ljava/lang/Object;";
    }
  }

  private static void newObject(Vector vcode, OperandRegO target, String className, int line) {
    int classIndex = GlobalConstantPool.putCls(className);
    GenerateInstruction.newInstruction(vcode, pref_NEWOBJ, target, new OperandSymO(classIndex), line);
  }

  private static void call(Vector vcode, String className, String methodName, String[] params, String signature,
      OperandRegO target, Operand[] retAndParams, boolean isVoid, int line) {
    int methodIndex = GlobalConstantPool.putMethod(className, methodName, params, signature);
    GenerateInstruction.newInstruction(vcode, CALL_normal, new OperandSymO(methodIndex), target, retAndParams, isVoid,
        line);
  }

  private static String constantAsString(JavaConstantPool cp, int constantPoolIndex) {
    Object constant = cp.constants[constantPoolIndex];
    if (constant instanceof String) {
      return (String) constant;
    }
    if (constant instanceof Integer || constant instanceof Long || constant instanceof Float || constant instanceof Double) {
      return String.valueOf(constant);
    }
    if (constant instanceof JavaConstantInfo) {
      JavaConstantInfo info = (JavaConstantInfo) constant;
      switch (info.type) {
      case 7:
      case 8:
        return cp.getString1(info.index1);
      default:
        break;
      }
    }
    throw new ConverterException("Unsupported StringConcatFactory constant argument at constant pool index "
        + constantPoolIndex);
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

  private static final class ConcatSite {
    final String recipe;
    final String[] constants;

    ConcatSite(String recipe, String[] constants) {
      this.recipe = recipe;
      this.constants = constants;
    }
  }

  private static final class ArgumentCursor {
    private final Operand[] arguments;
    private final String[] descriptors;
    private final String[] constants;
    private int argumentIndex;
    private int constantIndex;
    private String lastArgumentDescriptor;

    ArgumentCursor(Operand[] arguments, String[] descriptors, String[] constants) {
      this.arguments = arguments;
      this.descriptors = descriptors;
      this.constants = constants;
    }

    Operand nextArgument() {
      if (argumentIndex >= arguments.length) {
        throw new ConverterException("StringConcatFactory recipe consumes more arguments than the call site has");
      }
      Operand argument = arguments[argumentIndex++];
      lastArgumentDescriptor = descriptors[argumentIndex - 1];
      return argument;
    }

    String nextArgumentDescriptor() {
      return lastArgumentDescriptor;
    }

    String nextConstant() {
      if (constantIndex >= constants.length) {
        throw new ConverterException("StringConcatFactory recipe consumes more constants than the bootstrap has");
      }
      return constants[constantIndex++];
    }

    void validateFullyConsumed(JavaClass owner, BC186_invokedynamic site) {
      if (argumentIndex != arguments.length) {
        throw unsupported(owner, site, "StringConcatFactory recipe leaves call-site arguments unused");
      }
      if (constantIndex != constants.length) {
        throw unsupported(owner, site, "StringConcatFactory recipe leaves bootstrap constants unused");
      }
    }
  }

}
