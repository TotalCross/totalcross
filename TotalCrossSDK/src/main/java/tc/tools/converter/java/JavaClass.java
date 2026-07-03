// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.java;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import tc.tools.converter.Bytecode2TCCode;
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
  public JavaBootstrapMethod[] bootstrapMethods;
  public String nestHost;
  public String[] nestMembers;
  public String moduleName;
  public String moduleMainClass;
  public String[] modulePackages;
  public byte[] bytes;

  //public ClassAttribute[] attrs;

  public JavaClass(byte[] bytes, boolean onlyHeader) throws totalcross.io.IOException {
    int i, n;
    this.bytes = bytes;
    DataStream ds = new DataStream(new ByteArrayStream(bytes));
    ds.skipBytes(4); // skip 4-byte  magic number
    minorVersion = ds.readUnsignedShort();
    majorVersion = ds.readUnsignedShort();
    JavaClassFileVersion.validate(null, majorVersion, minorVersion);

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
    JavaClassFileVersion.validate(className, majorVersion, minorVersion);

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
      }
      skipClassAttributes(ds);
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

    superClass = Bytecode2TCCode.replaceTotalCrossLangToJavaLang(classNode.superName);

    interfaces = (String[]) classNode.interfaces.toArray(new String[] {});
    for (int i = 0; i < interfaces.length; i++) {
      interfaces[i] = Bytecode2TCCode.replaceTotalCrossLangToJavaLang(interfaces[i]);
    }

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

    minorVersion = ds.readUnsignedShort();
    majorVersion = ds.readUnsignedShort();
    JavaClassFileVersion.validate(className, majorVersion, minorVersion);

    cp = new JavaConstantPool(ds);

    // skip access flags
    ds.readUnsignedShort();

    className = cp.getString1(ds.readUnsignedShort());
    JavaClassFileVersion.validate(className, majorVersion, minorVersion);

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
        if (oldMethods != null) {
          for (JavaMethod javaMethod : oldMethods) {
            if (javaMethod.replaceWithNative) {
              if (javaMethod.signature.equals(m.signature)) {
                m.replaceWithNative = javaMethod.replaceWithNative;
                m.isNative = true;
              }
            }
          }
        }
        methods[i] = m;
      }
      skipClassAttributes(ds);
    }
    return this;
  }

  private void skipClassAttributes(DataStream ds) throws totalcross.io.IOException {
    int n = ds.readUnsignedShort();
    for (int i = 0; i < n; i++) {
      String name = (String) cp.constants[ds.readUnsignedShort()];
      int len = ds.readInt();
      if ("BootstrapMethods".equals(name)) {
        readBootstrapMethods(ds);
      } else if ("NestHost".equals(name)) {
        readNestHost(ds);
      } else if ("NestMembers".equals(name)) {
        readNestMembers(ds);
      } else if ("Module".equals(name)) {
        readModule(ds);
      } else if ("ModuleMainClass".equals(name)) {
        readModuleMainClass(ds);
      } else if ("ModulePackages".equals(name)) {
        readModulePackages(ds);
      } else {
        ds.skipBytes(len);
      }
    }
  }

  private void readBootstrapMethods(DataStream ds) throws totalcross.io.IOException {
    int n = ds.readUnsignedShort();
    bootstrapMethods = new JavaBootstrapMethod[n];
    for (int i = 0; i < n; i++) {
      int bootstrapMethodRef = ds.readUnsignedShort();
      int argumentCount = ds.readUnsignedShort();
      int[] arguments = new int[argumentCount];
      for (int j = 0; j < argumentCount; j++) {
        arguments[j] = ds.readUnsignedShort();
      }
      bootstrapMethods[i] = new JavaBootstrapMethod(bootstrapMethodRef, arguments);
    }
  }

  private void readNestHost(DataStream ds) throws totalcross.io.IOException {
    nestHost = cp.getString1(ds.readUnsignedShort());
  }

  private void readNestMembers(DataStream ds) throws totalcross.io.IOException {
    int n = ds.readUnsignedShort();
    nestMembers = new String[n];
    for (int i = 0; i < n; i++) {
      nestMembers[i] = cp.getString1(ds.readUnsignedShort());
    }
  }

  private void readModule(DataStream ds) throws totalcross.io.IOException {
    moduleName = cp.getString1(ds.readUnsignedShort());
    ds.readUnsignedShort(); // module_flags
    ds.readUnsignedShort(); // module_version_index

    int requiresCount = ds.readUnsignedShort();
    for (int i = 0; i < requiresCount; i++) {
      ds.readUnsignedShort(); // requires_index
      ds.readUnsignedShort(); // requires_flags
      ds.readUnsignedShort(); // requires_version_index
    }

    skipModuleExportsOrOpens(ds);
    skipModuleExportsOrOpens(ds);

    int usesCount = ds.readUnsignedShort();
    for (int i = 0; i < usesCount; i++) {
      ds.readUnsignedShort();
    }

    int providesCount = ds.readUnsignedShort();
    for (int i = 0; i < providesCount; i++) {
      ds.readUnsignedShort(); // provides_index
      int providesWithCount = ds.readUnsignedShort();
      for (int j = 0; j < providesWithCount; j++) {
        ds.readUnsignedShort();
      }
    }
  }

  private void readModuleMainClass(DataStream ds) throws totalcross.io.IOException {
    moduleMainClass = cp.getString1(ds.readUnsignedShort());
  }

  private void readModulePackages(DataStream ds) throws totalcross.io.IOException {
    int n = ds.readUnsignedShort();
    modulePackages = new String[n];
    for (int i = 0; i < n; i++) {
      modulePackages[i] = cp.getString1(ds.readUnsignedShort());
    }
  }

  private void skipModuleExportsOrOpens(DataStream ds) throws totalcross.io.IOException {
    int n = ds.readUnsignedShort();
    for (int i = 0; i < n; i++) {
      ds.readUnsignedShort(); // exports_index or opens_index
      ds.readUnsignedShort(); // flags
      int targetCount = ds.readUnsignedShort();
      for (int j = 0; j < targetCount; j++) {
        ds.readUnsignedShort();
      }
    }
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
