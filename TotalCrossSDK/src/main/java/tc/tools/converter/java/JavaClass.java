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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;

public final class JavaClass {
  public boolean isPublic, isFinal, isSuper, isInterface, isAbstract;
  public int minorVersion, majorVersion;
  public JavaConstantPool cp;
  public String className, superClass;
  public String[] interfaces;
  public JavaField[] fields;
  public JavaMethod[] methods;
  public byte[] bytes;

  //public ClassAttribute[] attrs;

  public JavaClass(byte[] bytes, boolean onlyHeader) throws totalcross.io.IOException {
    int i, n;
    this.bytes = bytes;
    DataStream ds = new DataStream(new ByteArrayStream(bytes));
    ds.skipBytes(4); // skip 4-byte  magic number
    minorVersion = ds.readUnsignedShort();
    majorVersion = ds.readUnsignedShort();

    cp = new JavaConstantPool(ds);
    // access flags

    int f = ds.readUnsignedShort();
    isPublic = (f & 0x1) != 0;
    isFinal = (f & 0x10) != 0;
    isSuper = (f & 0x20) != 0;
    isInterface = (f & 0x200) != 0;
    isAbstract = (f & 0x400) != 0;
    // names
    className = cp.getString1(ds.readUnsignedShort());

    if (!onlyHeader && majorVersion > 52) // check if we're using the correct class file format
    {
      String e = "-target 1.8 -source 1.8";
      throw new IllegalArgumentException("\n\nThe class " + className
          + " has an invalid .class file format.\n\nTo correct this, you must make sure that the file is compiled using the 1.8 Java Class specification. So, if you're compiling using JAVAC in the command shell (or an ANT file), add this to your javac command line:\n\nJAVAC "
          + e
          + " ...\n\nOtherwise, if the files were compiled in Eclipse, go to the project properties, select 'Java Compiler', set 'Enable project specific settings', and set 'Compiler compliance level' to 1.8.");
    }

    int idx = ds.readUnsignedShort();
    if (idx == 0) {
      superClass = "";
    } else {
      superClass = cp.getString1(idx);
    }
    // interfaces
    n = ds.readUnsignedShort();
    interfaces = new String[n];
    for (i = 0; i < n; i++) {
      interfaces[i] = cp.getString1(ds.readUnsignedShort());
    }
    if (!onlyHeader) {
      // fields
      n = ds.readUnsignedShort();
      fields = new JavaField[n];
      for (i = 0; i < n; i++) {
        fields[i] = new JavaField(ds, cp);
      }
      // methods
      n = ds.readUnsignedShort();
      methods = new JavaMethod[n];
      for (i = 0; i < n; i++) {
        methods[i] = new JavaMethod(this, ds, cp);
        // skip - attributes
        /*
         * n = ds.readUnsignedShort(); attrs = new ClassAttribute[n]; for (i
         * =0; i < n; i++) fields[i] = new JavaField(ds);
         */
      }
    }
  }

  public JavaClass(ClassNode classNode) {
    majorVersion = classNode.version;
    isPublic = ((classNode.access & Opcodes.ACC_PUBLIC) != 0);
    isFinal = ((classNode.access & Opcodes.ACC_FINAL) != 0);
    isSuper = ((classNode.access & Opcodes.ACC_SUPER) != 0);
    isInterface = ((classNode.access & Opcodes.ACC_INTERFACE) != 0);
    isAbstract = ((classNode.access & Opcodes.ACC_ABSTRACT) != 0);

    className = classNode.name;

    superClass = classNode.superName;
    interfaces = (String[]) classNode.interfaces.toArray(new String[] {});

    fields = new JavaField[classNode.fields.size()];
    for (int i = 0; i < fields.length; i++) {
      fields[i] = new JavaField((FieldNode) classNode.fields.get(i));
    }
    methods = new JavaMethod[classNode.methods.size()];
    for (int i = 0; i < methods.length; i++) {
      methods[i] = new JavaMethod(this, (MethodNode) classNode.methods.get(i));
    }
  }

  public JavaClass parse(byte[] bytes, boolean onlyHeader) throws totalcross.io.IOException {
    int i, n;
    this.bytes = bytes;
    DataStream ds = new DataStream(new ByteArrayStream(bytes));
    ds.skipBytes(4); // skip 4-byte  magic number

    // skip major version
    ds.readUnsignedShort();
    // skip minor version
    ds.readUnsignedShort();

    cp = new JavaConstantPool(ds);

    // skip access flags
    ds.readUnsignedShort();

    // skip className
    cp.getString1(ds.readUnsignedShort());

    if (!onlyHeader && majorVersion > 52) // check if we're using the correct class file format
    {
      String e = "-target 1.8 -source 1.8";
      throw new IllegalArgumentException("\n\nThe class " + className
          + " has an invalid .class file format.\n\nTo correct this, you must make sure that the file is compiled using the 1.8 Java Class specification. So, if you're compiling using JAVAC in the command shell (or an ANT file), add this to your javac command line:\n\nJAVAC "
          + e
          + " ...\n\nOtherwise, if the files were compiled in Eclipse, go to the project properties, select 'Java Compiler', set 'Enable project specific settings', and set 'Compiler compliance level' to 1.8.");
    }

    // skip superClass
    int idx = ds.readUnsignedShort();
    if (idx > 0) {
      cp.getString1(idx);
    }

    // skip interfaces
    n = ds.readUnsignedShort();
    for (i = 0; i < n; i++) {
      cp.getString1(ds.readUnsignedShort());
    }
    if (!onlyHeader) {
      // fields
      n = ds.readUnsignedShort();
      fields = new JavaField[n];
      for (i = 0; i < n; i++) {
        fields[i] = new JavaField(ds, cp);
      }

      // methods
      n = ds.readUnsignedShort();
      JavaMethod[] oldMethods = methods;
      methods = new JavaMethod[n];
      for (i = 0; i < n; i++) {
        JavaMethod m = new JavaMethod(this, ds, cp);
        for (JavaMethod javaMethod : oldMethods) {
          if (javaMethod.replaceWithNative) {
            if (javaMethod.signature.equals(m.signature)) {
              m.replaceWithNative = javaMethod.replaceWithNative;
              m.isNative = true;
            }
          }
        }
        methods[i] = m;
      }

      // skip - attributes
      /*
       * n = ds.readUnsignedShort(); attrs = new ClassAttribute[n]; for (i
       * =0; i < n; i++) fields[i] = new JavaField(ds);
       */
    }
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("/* ").append(majorVersion).append('.').append(minorVersion).append(" */\n");

    if (isSuper) {
      sb.append("super ");
    }
    if (isPublic) {
      sb.append("public ");
    }
    if (isFinal) {
      sb.append("final ");
    }
    if (isAbstract) {
      sb.append("abstract ");
    }
    if (isInterface) {
      sb.append("interface ");
    } else {
      sb.append("class ");
    }

    sb.append(className).append(" extends ").append(superClass);
    if (interfaces != null && interfaces.length > 0) {
      sb.append(" implements ");
      for (String string : interfaces) {
        sb.append(string).append(", ");
      }
      sb.setLength(sb.length() - 2);
    }
    sb.append("{");
    if (fields != null && fields.length > 0) {
      for (JavaField javaField : fields) {
        sb.append("\n").append(javaField);
      }
    }
    if (methods != null && methods.length > 0) {
      for (JavaMethod javaMethod : methods) {
        sb.append("\n").append(javaMethod);
      }
    }
    sb.append("\n}\n");

    return sb.toString();
  }

  public byte[] JavaClassToBytes() {
    int access_flags = 0;
    int classNameIdx = 0;
    int superclassIdx = 0;
    ByteArrayStream array = new ByteArrayStream(51200);
    DataStream stream = new DataStream(array);
    try {
      stream.writeInt(0xCAFEBABE); // write magic number that identifies it as a java class
      stream.writeShort(this.minorVersion);
      stream.writeShort(this.majorVersion);
      stream.writeShort(this.cp.numConstants);

      for (int i = 0; i < this.cp.numConstants; i++) {
        Object obj = this.cp.constants[i];

        if (obj instanceof String) {
          stream.writeByte(1); // write type
          System.out.println(((String) obj));
          if (className.equals((String) obj)) {
            classNameIdx = i;
          }
          if (!superClass.equals("") && superClass.equals((String) obj)) {
            superclassIdx = i;
          }
        } else if (obj instanceof Integer) {
          stream.writeByte(3);
          stream.writeInt(((Integer) obj).intValue());
        } else if (obj instanceof Float) {
          stream.writeByte(4);
          stream.writeFloat(((Float) obj).floatValue());
        } else if (obj instanceof Long) {
          stream.writeByte(5);
          stream.writeLong(((Long) obj).longValue());
        } else if (obj instanceof Double) {
          stream.writeByte(6);
          stream.writeDouble(((Double) obj).doubleValue());
        } else if (obj instanceof JavaConstantInfo) {
          JavaConstantInfo jci = (JavaConstantInfo) obj;
          stream.writeByte(jci.type);
          switch (jci.type) {
          case 7:
          case 8:
            stream.writeShort(jci.index1);
            break;
          case 9:
          case 10:
          case 11:
          case 12:
            stream.writeShort(jci.index1);
            stream.writeShort(jci.index2);
            break;
          }
        } // end switch
      } // end for
      if (isPublic) {
        access_flags |= 0x1;
      }
      if (isFinal) {
        access_flags |= 0x10;
      }
      if (isSuper) {
        access_flags |= 0x20;
      }
      if (isInterface) {
        access_flags |= 0x200;
      }
      if (isAbstract) {
        access_flags |= 0x400;
      }
      stream.writeShort(access_flags);

      stream.writeShort(classNameIdx); //class name
      stream.writeShort(superclassIdx); // super class

      stream.writeShort(interfaces.length);
      for (int i = 0; i < interfaces.length; i++) {
        stream.writeByte(11);
      }
      return array.toByteArray();
    } catch (totalcross.io.IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
