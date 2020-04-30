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

import tc.tools.converter.JConstants;
import tc.tools.converter.bytecode.ByteCode;
import totalcross.io.DataStream;

public final class JavaCode implements JConstants {
  public int maxStack, maxLocals;
  public ByteCode[] bcs; // method's bytecodes
  public byte[] bcBytes; // method's bytecodes
  public JavaException[] eh;
  public JavaMethod method;
  public int[] lineNumberPC; // the pc of the given line number
  public int[] lineNumberLine; // the line number itself

  private static final boolean dump = false;
  private static boolean lineNumberWarned;

  public JavaCode(JavaMethod method, DataStream ds, JavaConstantPool cp) throws totalcross.io.IOException {
    this.method = method;
    maxStack = ds.readUnsignedShort();
    maxLocals = ds.readUnsignedShort();
    if (dump) {
      System.out.println(method.signature);
    }
    // code
    int n = ds.readInt();
    if (n > 0) {
      ByteCode bc;
      ds.readBytes(bcBytes = new byte[n]);
      ByteCode[] tempBcs = new ByteCode[n]; // we don't know how many bytecodes will exist, but surely not more than the number of bytes in this method
      // set the static members
      ByteCode.code = bcBytes;
      ByteCode.pc = 0;
      ByteCode.cp = cp;
      int i = 0;
      for (; ByteCode.pc < n; i++) {
        int bcode = bcBytes[ByteCode.pc] & 0xFF;
        if (bcode >= bcClassNames.length) {
          System.out.println(
              "Error in " + method.classOfMethod + "." + method.signature + "\nInvalid bytecode index: " + bcode);
          for (int j = 0; j < i; j++) {
            System.out.println(tempBcs[j]);
          }
          System.exit(1);
        }
        tempBcs[i] = bc = ByteCode.getInstance(bcode);
        bc.posInMethod = i;
        bc.bc = bcode;
        bc.jc = this;
        if (false) {
          System.out.println(tempBcs[i]);
        }
        ByteCode.pc += bc.pcInc;
      }
      bcs = new ByteCode[i];
      totalcross.sys.Vm.arrayCopy(tempBcs, 0, bcs, 0, i);
    }
    if (dump) {
      System.out.println();
    }
    // exceptions
    n = ds.readUnsignedShort();
    if (n > 0) {
      eh = new JavaException[n];
      for (int i = 0; i < n; i++) {
        eh[i] = new JavaException(ds, cp);
      }
    }
    // attributes - currently we'll skip all
    n = ds.readUnsignedShort();
    for (int i = 0; i < n; i++) {
      String name = (String) cp.constants[ds.readUnsignedShort()];
      if (name.equals("LineNumberTable")) {
        ds.skipBytes(4); // skip attribute length
        int l = ds.readUnsignedShort();
        lineNumberPC = new int[l];
        lineNumberLine = new int[l];
        for (int j = 0; j < l; j++) {
          lineNumberPC[j] = ds.readUnsignedShort();
          lineNumberLine[j] = ds.readUnsignedShort();
        }
      } else {
        if (dump) {
          System.out.println("Skipping attribute " + name + " in method " + method.signature);
        }
        int len = ds.readInt();
        ds.skipBytes(len); // skip the rest
      }
    }
    String className = method.classOfMethod.className.replace('/', '.');
    if (!lineNumberWarned && lineNumberLine == null && !className.startsWith("totalcross.")
        && !className.startsWith("litebase.")) // guich@tc110_68
    {
      lineNumberWarned = true;
      System.out.println(
          "Warning: line number information not found. Stack traces at device will be shown incomplete. To fix this, enable debug information when compiling the files. First detected on class "
              + className);
    }
  }

  public ByteCode getAtPC(int pc) {
    for (int i = 0; i < bcs.length; i++) {
      if (bcs[i].pcInMethod == pc) {
        return bcs[i];
      }
    }
    return null;
  }

  private static StringBuffer sb = new StringBuffer(4096);

  @Override
  public String toString() {
    sb.setLength(0);
    sb.append(method.classOfMethod).append('.').append(method.signature).append('\n');
    if (bcs != null) {
      for (int i = 0; i < bcs.length; i++) {
        sb.append(bcs[i].toString()).append('\n');
      }
      return sb.toString();
    } else {
      return sb.append("No code in method").toString();
    }
  }
}
