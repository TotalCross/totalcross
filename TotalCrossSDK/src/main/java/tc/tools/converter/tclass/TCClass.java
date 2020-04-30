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

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.deployer.Utils;
import totalcross.io.DataStreamLE;

public final class TCClass {
  // The offsets to the object where each instance field type starts in an Object
  public int /*uint32*/ objOfs, v64Ofs; // there's no i32Ofs because int32's offset is always 0
  // The default instance field values. Used in object creation. The length is the instance size, excluding TObjectProperties
  public byte[] /*Uint8Array*/ defaultInstanceValues;
  // A string representing this class.
  public String className;
  // A string representing the file name where this class was declared (.java, not .class) - NOT STORED IN THE CONSTANT POOL!
  public String sourceFile;
  // The access flags of this class (e.g.: if this class is public, static, etc)
  public TCClassFlags flags = new TCClassFlags();
  // The fields that will be copied into each instance (an Object) of this class.
  public TCField[] int32InstanceFields;
  public TCField[] value64InstanceFields;
  public TCField[] objectInstanceFields;
  // The fields that will be shared among all the instances of this class
  public TCField[] int32StaticFields;
  public TCField[] value64StaticFields;
  public TCField[] objectStaticFields;
  // The methods declared in this class
  public TCMethod[] methods;
  // The interfaces that this class implements  *** guich - for the compiler, it is important to load and compile the class; but for the converter, its not (unless method inline is implemented)
  public /*TCClass*/String[] interfaces;
  // The superclass of this class. The only class that has a null superClass is java.lang.Object
  public /*TCClass*/String superClass; // *** idem
  // Number of active instances of this class - will be used later to create a way to release memory in crucial situations
  public int /*uint32*/ activeInstances;
  // The original source code for each line of this class
  public String[] lines;

  public void write(DataStreamLE ds) throws totalcross.io.IOException {
    if (J2TC.dump) {
      System.out.println("\nClass: " + className);
    }
    if (className.equals("java/lang/Object")) {
      superClass = null; // java.lang.Object must have no superclass!
    }
    int i;
    int int32StaticFieldsCount = int32StaticFields != null ? int32StaticFields.length : 0;
    int objectStaticFieldsCount = objectStaticFields != null ? objectStaticFields.length : 0;
    int value64StaticFieldsCount = value64StaticFields != null ? value64StaticFields.length : 0;
    int int32InstanceFieldsCount = int32InstanceFields != null ? int32InstanceFields.length : 0;
    int objectInstanceFieldsCount = objectInstanceFields != null ? objectInstanceFields.length : 0;
    int value64InstanceFieldsCount = value64InstanceFields != null ? value64InstanceFields.length : 0;
    int methodsCount = methods != null ? Utils.countNotNull(methods) : 0;
    int interfacesCount = interfaces != null ? interfaces.length : 0;

    flags.write(ds);
    ds.writeShort(GlobalConstantPool.getClassIndex(className));
    ds.writeShort(superClass != null ? GlobalConstantPool.getClassIndex(superClass) : 0);
    ds.writeShort(interfacesCount);
    ds.writeShort(int32InstanceFieldsCount);
    ds.writeShort(objectInstanceFieldsCount);
    ds.writeShort(value64InstanceFieldsCount);
    ds.writeShort(int32StaticFieldsCount);
    ds.writeShort(objectStaticFieldsCount);
    ds.writeShort(value64StaticFieldsCount);
    ds.writeShort(methodsCount);
    // write the Interface indexes to the symbol table
    if (interfacesCount > 0) {
      if (J2TC.dump) {
        System.out.print("implements: ");
      }
      for (i = 0; i < interfacesCount; i++) {
        if (J2TC.dump) {
          System.out.print(interfaces[i] + " ");
        }
        ds.writeShort(GlobalConstantPool.getClassIndex(interfaces[i]));
      }
      if (J2TC.dump) {
        System.out.println();
      }
    }
    // static fields
    if (int32StaticFieldsCount > 0) {
      for (i = 0; i < int32StaticFieldsCount; i++) {
        int32StaticFields[i].write(ds);
      }
    }
    if (objectStaticFieldsCount > 0) {
      for (i = 0; i < objectStaticFieldsCount; i++) {
        objectStaticFields[i].write(ds);
      }
    }
    if (value64StaticFieldsCount > 0) {
      for (i = 0; i < value64StaticFieldsCount; i++) {
        value64StaticFields[i].write(ds);
      }
    }
    // instance fields
    if (int32InstanceFieldsCount > 0) {
      for (i = 0; i < int32InstanceFieldsCount; i++) {
        int32InstanceFields[i].write(ds);
      }
    }
    if (objectInstanceFieldsCount > 0) {
      for (i = 0; i < objectInstanceFieldsCount; i++) {
        objectInstanceFields[i].write(ds);
      }
    }
    if (value64InstanceFieldsCount > 0) {
      for (i = 0; i < value64InstanceFieldsCount; i++) {
        value64InstanceFields[i].write(ds);
      }
    }

    if (methodsCount > 0) {
      for (i = 0; i < methods.length; i++) {
        if (methods[i] != null) {
          methods[i].write(ds);
        }
      }
    }
  }
}
