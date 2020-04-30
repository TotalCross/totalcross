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
package tc.tools.converter.tclass;

import java.lang.reflect.Method;

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.Storage;
import tc.tools.converter.TCConstants;
import tc.tools.converter.TCValue;
import tc.tools.converter.bb.InvalidClassException;
import tc.tools.converter.ir.CFG;
import tc.tools.converter.ir.Instruction.Call;
import tc.tools.converter.ir.Instruction.Instruction;
import tc.tools.converter.regalloc.AdjListNode;
import totalcross.io.DataStreamLE;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

public final class TCMethod implements TCConstants {
  // How many registers of each type are used in this method, including
  // the method parameters, but excluding the instance reference (if any)
  public int /*uint8*/ iCount, oCount, v64Count, paramSkip;
  // an array of instructions.
  public TCCode[] code;
  // an array of intermediate instructions.
  public Vector insts;
  // The class to whom this method belongs to
  public TCClass tcclass;
  // Signature of this method, containing indexes to the CP: name, number of parameters, and the parameters
  public int /*uint16*/ cpName;
  public int /*uint16*/ paramCount;
  public int[] /*Uint16Array*/ cpParams; // indexes to the constant pool of the parameters
  public byte[] /*Uint8Array*/ paramRegs; // the RegType to where the parameters must go on - two bits would be enough, but we use more to improve speed
  // The return type, as an index to the constant pool. Zero for constructors and methods that return void.
  public int /*uint16*/ cpReturn;
  // The method's access flags (public, static, private, etc)
  public TCMethodFlags flags = new TCMethodFlags();
  // The exception handlers defining which parts of the code have a try/catch
  public TCException[] exceptionHandlers;
  // Used in debug info - the line numbers for each set of instructions
  public TCLineNumber[] lineNumbers;
  // The method index on VM (Virtual Machine). Used by the compiler in the intermediate code generation.
  public int indexOnVM;

  public CFG cfg;
  // How many registers are used by parameters of this method.
  public int /*uint8*/ iParamCount, oParamCount, v64ParamCount;

  public static boolean checkJavaCalls = true;
  private static final int ALL_1 = 255;

  public void write(DataStreamLE ds) throws totalcross.io.IOException {
    int i;
    String name = GlobalConstantPool.getMethodFieldName(cpName);
    if (tc.tools.converter.J2TC.dump) {
      System.out.print("\n" + tcclass.className + "." + name + "(");
      for (i = 0; i < paramCount; i++) {
        System.out.print(GlobalConstantPool.getClassName(cpParams[i]) + (i < paramCount - 1 ? "," : ""));
      }
      System.out.println(") i=" + iCount + ",o=" + oCount + ",v64=" + v64Count + " - "
          + (lineNumbers == null ? "lines=0" : ("lines=" + lineNumbers.length)));
    } else if (iCount >= 64 || oCount >= 64 || v64Count >= 64) {
      System.err.println("Warning: " + tcclass.className + "." + GlobalConstantPool.getMethodFieldName(cpName)
          + " has reached 64 registers. i=" + iCount + ",o=" + oCount + ",v64=" + v64Count);
    }

    int[] opcode = getCode();
    int opcodeCount = code != null ? code.length : 0;
    int exceptionHandlersCount = exceptionHandlers != null ? exceptionHandlers.length : 0;
    int lineNumberDebugInfoCount = lineNumbers != null ? lineNumbers.length : 0;

    // guich@tc110_69: first do some checks in the LineNumberDebugInfo array
    boolean allPC1 = true;
    boolean allLN1 = true;
    if (lineNumberDebugInfoCount > 0 && !name.equals("<S>")) {
      int p, l;
      // first do some checks
      if (lineNumberDebugInfoCount > 2 && lineNumbers[0].lineNumber == 0) {
        lineNumbers[0].lineNumber = lineNumbers[1].lineNumber;
      }
      for (i = 1; i < lineNumberDebugInfoCount; i++) {
        p = lineNumbers[i].startPC - lineNumbers[i - 1].startPC;
        l = lineNumbers[i].lineNumber - lineNumbers[i - 1].lineNumber;
        if (p >= ALL_1 || l >= ALL_1) // 255 is reserved for mark telling that diff is always 1
        {
          System.out.print("Cannot write line numbers for " + tcclass.className + "." + name + " with " + paramCount
              + " parameters (" + p + "/" + l + "). ");
          if (l >= ALL_1) {
            System.out.println(
                "Move statement at line " + lineNumbers[i].lineNumber + " to above line " + lineNumbers[0].lineNumber);
          } else {
            System.out.println();
          }
          //for (i=0; i < lineNumberDebugInfoCount; i++) System.out.println(i+" "+lineNumbers[i].startPC+" "+lineNumbers[i].lineNumber);
          lineNumberDebugInfoCount = 0;
          break;
        }
        allPC1 &= p == 1;
        allLN1 &= l == 1;
      }
    } else {
      lineNumberDebugInfoCount = 0;
    }

    flags.write(ds);
    ds.writeShort(opcodeCount);
    ds.writeShort(exceptionHandlersCount);
    ds.writeShort(lineNumberDebugInfoCount);
    ds.writeByte(iCount);
    ds.writeByte(oCount);
    ds.writeByte(v64Count);
    ds.writeByte(paramCount);
    ds.writeShort(cpName);
    ds.writeShort(cpReturn);

    // write the parameters
    if (paramCount > 0) {
      Storage.writeUnsignedShortArray(ds, cpParams);
    }
    // write the opcodes array - native methods have none
    if (opcodeCount > 0) {
      Storage.writeIntArray(ds, opcode); // note that the bit fields are inverted when comparing to Java, so we store them in inverse order
      checkMethodInvocations();
    }
    // write the ExceptionInfoArray
    if (exceptionHandlersCount > 0) {
      if (tc.tools.converter.J2TC.dump) {
        System.out.println("Exception handlers:");
      }
      for (i = 0; i < exceptionHandlersCount; i++) {
        exceptionHandlers[i].write(ds);
      }
    }
    // write the LineNumberDebugInfoArray - guich@tc110_69: completely rewritten
    /*System.out.println(tcclass.className+"."+name);
      System.out.println("pc:");
      for (i=0; i < lineNumberDebugInfoCount; i++) System.out.println(lineNumbers[i].startPC);
      System.out.println("line:");
      for (i=0; i < lineNumberDebugInfoCount; i++) System.out.println(lineNumbers[i].lineNumber);*/

    if (lineNumberDebugInfoCount == 1) {
      ds.writeShort(lineNumbers[0].lineNumber); // pc is already 0
    } else if (lineNumberDebugInfoCount > 1) {
      // pcs
      if (allPC1) {
        ds.writeByte(ALL_1); // the diff is always 1
      } else {
        for (i = 1; i < lineNumberDebugInfoCount; i++) {
          ds.writeByte(lineNumbers[i].startPC - lineNumbers[i - 1].startPC);
        }
      }
      // line numbers - write the first, then all differences
      ds.writeShort(lineNumbers[0].lineNumber);
      if (allLN1) {
        ds.writeByte(ALL_1);
      } else {
        for (i = 1; i < lineNumberDebugInfoCount; i++) {
          if (lineNumbers[i].lineNumber < lineNumbers[i - 1].lineNumber) {
            lineNumbers[i].lineNumber = lineNumbers[i - 1].lineNumber;
          }
          ds.writeByte(lineNumbers[i].lineNumber - lineNumbers[i - 1].lineNumber);
        }
      }
    }
  }

  private int[] getCode() {
    if (code == null) {
      return null;
    }
    if (tc.tools.converter.J2TC.dump) {
      TCCode.printAsX8x8x8x8Count = 0;
      for (int j = 0, i = 0; i < code.length; j++, i++) {
        System.out.println(Convert.zeroPad(Convert.toString(j), 3) + " " + code[i].toString());
      }
    }
    int[] v = new int[code.length];
    for (int i = 0; i < code.length; i++) {
      v[i] = code[i].val;
    }
    return v;
  }

  public void printCode() {
    System.out.print("\n" + tcclass.className + "." + GlobalConstantPool.getMethodFieldName(cpName) + "(");
    for (int i = 0; i < paramCount; i++) {
      System.out.print(GlobalConstantPool.getClassName(cpParams[i]) + (i < paramCount - 1 ? "," : ""));
    }
    System.out.println(") i=" + iCount + ",o=" + oCount + ",v64=" + v64Count + " - "
        + (lineNumbers == null ? "lines=0" : ("lines=" + lineNumbers.length)));

    if (insts != null) {
      for (int i = 0, n = insts.size(); i < n; i++) {
        Instruction x = (Instruction) insts.items[i];
        System.out.println(x.toString());
      }
    }
  }

  private void checkMethodInvocations() {
    J2TC.currentClass = tcclass.className;
    J2TC.currentMethod = GlobalConstantPool.getMethodFieldName(cpName);
    for (int i = 0, n = insts.size(); i < n; i++) {
      Instruction x = (Instruction) insts.items[i];
      if (x.opcode == CALL_virtual || x.opcode == CALL_normal) {
        Call call = (Call) x;
        int sym = call.sym;
        TCValue v = (TCValue) GlobalConstantPool.getMtdRef(sym);
        if (checkJavaCalls) {
          checkIfAllowed((int[]) v.asObj);
        }
      }
    }
    J2TC.currentClass = J2TC.currentMethod = null;
  }

  public void modifyCode(AdjListNode[] adjListI, AdjListNode[] adjListD, AdjListNode[] adjListO) {
    if (insts != null) {
      for (int i = 0; i < insts.size(); i++) {
        Instruction x = (Instruction) insts.items[i];
        x.virtualRegs2PhysicalRegs(adjListI, adjListD, adjListO);
      }
    }
  }

  private static String beAware = " Be aware that the classes and methods you use from the java.lang package at desktop are automatically transformed by tc.Deploy into the classes and methods available in totalcross.lang. Thus, you must use only the classes and methods described in the javadocs.";
  private static totalcross.util.Hashtable htAlreadyChecked = new totalcross.util.Hashtable(40);

  // checks if this is a java class that exists at device
  private void checkIfAllowed(int[] params) // guich@tc100b5_26
  {
    String className = GlobalConstantPool.getClassName(params[0]);
    String method = GlobalConstantPool.getMethodFieldName(params[1]);
    if (method.length() == 1) {
      return; // single letter? probably an obfuscated method
    }
    //System.out.println(J2TC.currentClass+" "+J2TC.currentMethod+" checking "+className+" "+method);
    if (J2TC.inProhibitedList(className, false) && !htAlreadyChecked.exists(params)) {
      htAlreadyChecked.put(params, "");
      Class<?> c4D;
      // checks if class java.xxx exists as a totalcross.xxx4D class
      String tcClassName = "totalcross" + className.substring(4);
      try {
        int index = tcClassName.indexOf('$');
        if (index < 0) {
          c4D = Class.forName(tcClassName + "4D"); // first try the 4D class
        } else {
          c4D = Class.forName(tcClassName.substring(0, index) + "4D" + tcClassName.substring(index));
        }
      } catch (ClassNotFoundException e) {
        try {
          c4D = Class.forName(tcClassName);
        } catch (ClassNotFoundException ee) {
          tcClassName = "jdkcompat" + className.substring(4);
          try {
            int index = tcClassName.indexOf('$');
            if (index < 0) {
              c4D = Class.forName(tcClassName + "4D"); // first try the 4D class
            } else {
              c4D = Class.forName(tcClassName.substring(0, index) + "4D" + tcClassName.substring(index));
            }
          } catch (ClassNotFoundException e1) {
            throw new InvalidClassException("Class '" + className
                + "' is not available at the device! To see the available classes, see the Javadocs for totalcross.lang package."
                + beAware);
          }
        }
      }
      // now checks if the method exists in the totalcross4D class
      boolean found = false;
      if (method.equals(TClassConstants.CONSTRUCTOR_NAME)) // is this a constructor?
      {
        java.lang.reflect.Constructor<?>[] consts = c4D.getDeclaredConstructors();
        for (int i = 0; i < consts.length; i++) {
          if (areParametersCompatible(params, consts[i].getParameterTypes())) {
            found = true;
            break;
          }
        }
      } else {
        Vector vector = new Vector(c4D.getMethods());
        vector.addElements(c4D.getDeclaredMethods());
        Object[] objects = vector.toObjectArray();
        Method[] methods = new Method[objects.length];

        Vm.arrayCopy(objects, 0, methods, 0, objects.length);
        // lookahead to see if there are 4D methods with this same name
        Hashtable ht = new Hashtable(methods.length);
        for (int i = 0; i < methods.length; i++) {
          ht.put(methods[i].getName(), "");
        }
        for (int i = 0; i < methods.length; i++) {
          String name = methods[i].getName();
          if (name.endsWith("4D")) {
            name = name.substring(0, name.length() - 2);
          } else if (ht.exists(name + "4D")) {
            continue;
          }
          if (name.equals(method) && areParametersCompatible(params, methods[i].getParameterTypes())) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        throw new InvalidClassException(toHumanReadable(className, method, params)
            + " is not available at the device! To find the available ones, see the Javadocs for "
            + java2totalcross(className) + " class." + (className.startsWith("java.lang") ? beAware : ""));
      }
    }
  }

  private static String java2totalcross(String name) {
    return name.startsWith("java.") ? name.replace("java.", "totalcross.") : name;
  }

  private String toHumanReadable(String className, String name, int[] args) {
    StringBuffer sb = new StringBuffer(100);
    className = className.replace('.', '/');
    if (name.equals(CONSTRUCTOR_NAME)) {
      sb.append("Constructor ").append(className);
    } else {
      sb.append("Method ").append(className).append('.').append(name);
    }
    sb.append("(");
    if (args != null) {
      for (int i = 0, n = args.length - 3; i <= n; i++) {
        String a = GlobalConstantPool.getClassName(args[i + 2]);
        if (a.charAt(0) == '[') {
          String ret = GlobalConstantPool.getJavaTypeName(a);
          sb.append(ret);
          a = a.substring(ret.length() / 2);
        }
        sb.append(GlobalConstantPool.getJavaTypeName(a));
        if (i < n) {
          sb.append(", ");
        }
      }
    }
    return sb.append(")").toString();
  }

  // jiargs is what the user is calling; args is from the method retrieved with introspection in the totalcross/xxx4D class
  private static boolean areParametersCompatible(int[] jiargs, Class<?>[] args) {
    int jiargsLen = jiargs.length - 2;
    int argsLen = args == null ? 0 : args.length;
    if (jiargsLen != argsLen) {
      return false;
    }
    boolean found = true;
    for (int i = 0; i < jiargsLen && found; i++) {
      String name = args[i].getName();
      String js = GlobalConstantPool.getClassName(jiargs[i + 2]);
      if (name == "float") {
        name = "double";
      }

      if (js.charAt(0) == '&') {
        found &= name.equals(GlobalConstantPool.getPrimitiveJavaName(js));
      } else if (js.charAt(0) == '[') {
        int j = 0;
        while (name.charAt(++j) == '[') {
          ;
        }
        String suffix = "";
        switch (name.charAt(j)) {
        case 'Z':
          suffix = "&b";
          break;
        case 'C':
          suffix = "&C";
          break;
        case 'B':
          suffix = "&B";
          break;
        case 'S':
          suffix = "&S";
          break;
        case 'I':
          suffix = "&I";
          break;
        case 'J':
          suffix = "&L";
          break;
        case 'F':
          suffix = "&D";
          break;
        case 'D':
          suffix = "&D";
          break;
        case 'L':
          suffix = name.substring(j + 1, name.length() - 1);
        }
        name = name.substring(0, j) + suffix;
        found &= name.equals(js); // for arrays they return in the same format
      } else // object
      {
        name = java2totalcross(name);
        js = java2totalcross(js);
        found &= name.equals(js) || name.equals(js + "4D") || Convert.replace(name, "4D", "").equals(js);
      }
    }
    return found;
  }
}
