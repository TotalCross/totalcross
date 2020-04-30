// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.java;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.io.DataStream;
import totalcross.util.ElementNotFoundException;
import totalcross.util.Vector;

public final class JavaMethod {
  public String name, ret, signature;
  public JavaCode code;
  public JavaClass classOfMethod;
  public String[] checkedExceptions;
  public boolean isPublic, isPrivate, isProtected, isStatic, isFinal, isVolatile, isTransient, isNative, isAbstract,
      isStrict, isSynchronized;
  // method signature
  public String[] params;
  public int paramCount;
  private static Vector vec = new Vector(20);
  private static StringBuffer retsb = new StringBuffer(30);

  public boolean replaceWithNative;

  public static String[] splitParams(String s, StringBuffer ret) // return the parameters array and the return type in the string buffer
  {
    Vector v = vec;
    v.removeAllElements();
    ret.setLength(0);
    int n = s.length();
    for (int i = 1; i < n; i++) {
      switch (s.charAt(i)) {
      case '[': // array?
      {
        int last = i;
        do {
          last++;
        } while (s.charAt(last) == '[');
        if (s.charAt(last) == 'L') {
          last = s.indexOf(';', last);
        }
        String param = s.substring(i, last + 1);
        v.addElement(param);
        i = last;
        break;
      }
      case 'L': // object
        int i2 = s.indexOf(';', i + 1);
        String param = s.substring(i, i2 + 1);
        i = i2;
        v.addElement(param);
        break;
      case '(':
      case ')':
        break;
      default:
        v.addElement(s.substring(i, i + 1));
      }
    }
    try
    // the return is the last added parameter
    {
      ret.append((String) v.pop());
    } catch (ElementNotFoundException e) {
    }
    return (String[]) v.toObjectArray();
  }

  private static int floatWarning;

  public JavaMethod(JavaClass jc, DataStream ds, JavaConstantPool cp) throws totalcross.io.IOException {
    this.classOfMethod = jc;
    int f = ds.readUnsignedShort();
    isPublic = (f & 0x1) != 0;
    isPrivate = (f & 0x2) != 0;
    isProtected = (f & 0x4) != 0;
    isStatic = (f & 0x8) != 0;
    isFinal = (f & 0x10) != 0;
    isSynchronized = (f & 0x20) != 0;
    isNative = (f & 0x100) != 0;
    isAbstract = (f & 0x400) != 0;
    isStrict = (f & 0x800) != 0;

    name = (String) cp.constants[ds.readUnsignedShort()];
    String parameters = (String) cp.constants[ds.readUnsignedShort()];
    params = splitParams(parameters, retsb);
    if (params != null) {
      paramCount = params.length;
    }
    this.ret = retsb.toString();
    signature = name + parameters.substring(0, parameters.length() - ret.length()); // (I)V -> (I)

    // check for float types in the method
    if (params != null) {
      for (int i = 0; i < params.length - 1; i++) {
        if (params[i].equals("F")) {
          if (floatWarning++ == 0) {
            System.out.println(
                "There's a bug in tc.Deploy that will lead to unpredictable results in device when a method has a float parameter that's not at the last position. To fix this, just change the parameter's type from float to double. Note that, in device, all float values ARE transformed into double (TCVM does not supports float in device).");
          }
          System.out.println("Caution! Method " + jc.className + "." + name + " has a float parameter.");
          break;
        }
      }
    }

    // read the attributes
    int n = ds.readUnsignedShort();
    for (int i = 0; i < n; i++) {
      String name = (String) cp.constants[ds.readUnsignedShort()];
      int len = ds.readInt();
      if (false) {
        System.out.println("Method attribute: " + name);
      }
      if (name.equals("Code") || name.equals("JavaCode")) {
        code = new JavaCode(this, ds, cp);
      } else if (name.equals("Exceptions")) {
        checkedExceptions = new String[ds.readUnsignedShort()];
        for (int j = 0; j < checkedExceptions.length; j++) {
          checkedExceptions[j] = cp.getString1(ds.readUnsignedShort());
        }
      } else {
        ds.skipBytes(len);
      }
    }
  }

  public JavaMethod(JavaClass jc, MethodNode methodNode) {
    this.classOfMethod = jc;

    isPublic = ((methodNode.access & Opcodes.ACC_PUBLIC) != 0);
    isPrivate = ((methodNode.access & Opcodes.ACC_PRIVATE) != 0);
    isProtected = ((methodNode.access & Opcodes.ACC_PROTECTED) != 0);
    isStatic = ((methodNode.access & Opcodes.ACC_STATIC) != 0);
    isFinal = ((methodNode.access & Opcodes.ACC_FINAL) != 0);
    isSynchronized = ((methodNode.access & Opcodes.ACC_SYNCHRONIZED) != 0);
    isNative = ((methodNode.access & Opcodes.ACC_NATIVE) != 0);
    isAbstract = ((methodNode.access & Opcodes.ACC_ABSTRACT) != 0);
    isStrict = ((methodNode.access & Opcodes.ACC_STRICT) != 0);

    name = methodNode.name;

    Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
    params = new String[argumentTypes.length];
    for (int i = 0; i < params.length; i++) {
      params[i] = argumentTypes[i].getDescriptor();
    }

    Type returnType = Type.getReturnType(methodNode.desc);
    ret = returnType.getDescriptor();

    signature = methodNode.name + Arrays.toString(Type.getArgumentTypes(methodNode.desc)).replace(", ", "")
        .replaceFirst("^\\[(.*)\\]$", "($1)");

    if (methodNode.invisibleAnnotations != null && methodNode.invisibleAnnotations.size() > 0) {
      for (AnnotationNode annotation : (List<AnnotationNode>) methodNode.invisibleAnnotations) {
        if (Type.getDescriptor(ReplacedByNativeOnDeploy.class).equals(annotation.desc)) {
          replaceWithNative = true;
        }
      }
    }
  }

  public JavaMethod parse(JavaClass jc, DataStream ds, JavaConstantPool cp) throws totalcross.io.IOException {

    this.classOfMethod = jc;
    int f = ds.readUnsignedShort();
    isPublic = (f & 0x1) != 0;
    isPrivate = (f & 0x2) != 0;
    isProtected = (f & 0x4) != 0;
    isStatic = (f & 0x8) != 0;
    isFinal = (f & 0x10) != 0;
    isSynchronized = (f & 0x20) != 0;
    isNative = (f & 0x100) != 0;
    isAbstract = (f & 0x400) != 0;
    isStrict = (f & 0x800) != 0;

    name = (String) cp.constants[ds.readUnsignedShort()];
    String parameters = (String) cp.constants[ds.readUnsignedShort()];
    params = splitParams(parameters, retsb);
    if (params != null) {
      paramCount = params.length;
    }
    this.ret = retsb.toString();
    signature = name + parameters.substring(0, parameters.length() - ret.length()); // (I)V -> (I)

    // check for float types in the method
    if (params != null) {
      for (int i = 0; i < params.length - 1; i++) { // float being last is ok
        if (params[i].equals("F")) {
          if (floatWarning++ == 0) {
            System.out.println(
                "There's a bug in tc.Deploy that will lead to unpredictable results in device when a method has a float parameter that's not at the last position. To fix this, just change the parameter's type from float to double. Note that, in device, all float values ARE transformed into double (TCVM does not supports float in device).");
          }
          System.out.println("Caution! Method " + jc.className + "." + name + " has a float parameter.");
          break;
        }
      }
    }

    // read the attributes
    int n = ds.readUnsignedShort();
    for (int i = 0; i < n; i++) {
      String name = (String) cp.constants[ds.readUnsignedShort()];
      int len = ds.readInt();
      if (name.equals("Code") || name.equals("JavaCode")) {
        code = new JavaCode(this, ds, cp);
      } else if (name.equals("Exceptions")) {
        checkedExceptions = new String[ds.readUnsignedShort()];
        for (int j = 0; j < checkedExceptions.length; j++) {
          checkedExceptions[j] = cp.getString1(ds.readUnsignedShort());
        }
      } else {
        ds.skipBytes(len);
      }
    }
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (isPublic) {
      sb.append("public ");
    }
    if (isPrivate) {
      sb.append("private ");
    }
    if (isProtected) {
      sb.append("protected ");
    }
    if (isStatic) {
      sb.append("static ");
    }
    if (isAbstract) {
      sb.append("abstract ");
    }
    if (isFinal) {
      sb.append("final ");
    }
    if (isNative) {
      sb.append("native ");
    }
    if (isVolatile) {
      sb.append("volatile ");
    }
    if (isTransient) {
      sb.append("transient ");
    }
    if (isStrict) {
      sb.append("strict ");
    }
    if (isSynchronized) {
      sb.append("synchronized ");
    }

    sb.append(ret).append(' ').append(name).append('(');

    if (params != null && params.length > 0) {
      for (String string : params) {
        sb.append(string).append(", ");
      }
      sb.setLength(sb.length() - 2);
    }
    sb.append(");");

    return sb.toString();
  }
}
