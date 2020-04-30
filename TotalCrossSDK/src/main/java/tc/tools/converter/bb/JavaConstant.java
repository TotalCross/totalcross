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
package tc.tools.converter.bb;

import tc.tools.converter.bb.constant.Class;
import tc.tools.converter.bb.constant.ConstantInfo;
import tc.tools.converter.bb.constant.Double;
import tc.tools.converter.bb.constant.FieldRef;
import tc.tools.converter.bb.constant.Float;
import tc.tools.converter.bb.constant.Integer;
import tc.tools.converter.bb.constant.InterfaceMethodRef;
import tc.tools.converter.bb.constant.Long;
import tc.tools.converter.bb.constant.MethodRef;
import tc.tools.converter.bb.constant.NameAndType;
import tc.tools.converter.bb.constant.String;
import tc.tools.converter.bb.constant.UTF8;
import totalcross.io.DataStream;
import totalcross.io.IOException;

public class JavaConstant implements JavaClassStructure {
  private JavaClass jclass;
  public byte tag;
  public ConstantInfo info;

  public static final byte CONSTANT_UTF8 = 1;
  public static final byte CONSTANT_INTEGER = 3;
  public static final byte CONSTANT_FLOAT = 4;
  public static final byte CONSTANT_LONG = 5;
  public static final byte CONSTANT_DOUBLE = 6;
  public static final byte CONSTANT_CLASS = 7;
  public static final byte CONSTANT_STRING = 8;
  public static final byte CONSTANT_FIELD_REF = 9;
  public static final byte CONSTANT_METHOD_REF = 10;
  public static final byte CONSTANT_INTERFACE_METHOD_REF = 11;
  public static final byte CONSTANT_NAME_AND_TYPE = 12;

  /**
   * @param jclass
   */
  public JavaConstant(JavaClass jclass) {
    this.jclass = jclass;
  }

  @Override
  public java.lang.String toString() {
    return info.toString();
  }

  @Override
  public int length() {
    return 1 + info.length();
  }

  public int slots() {
    return (tag == CONSTANT_LONG || tag == CONSTANT_DOUBLE) ? 2 : 1;
  }

  @Override
  public void load(DataStream ds) throws IOException {
    tag = ds.readByte();

    switch (tag) {
    case CONSTANT_UTF8:
      info = new UTF8();
      break;
    case CONSTANT_INTEGER:
      info = new Integer();
      break;
    case CONSTANT_FLOAT:
      info = new Float();
      break;
    case CONSTANT_LONG:
      info = new Long();
      break;
    case CONSTANT_DOUBLE:
      info = new Double();
      break;
    case CONSTANT_CLASS:
      info = new Class(jclass);
      break;
    case CONSTANT_STRING:
      info = new String(jclass);
      break;
    case CONSTANT_FIELD_REF:
      info = new FieldRef(jclass);
      break;
    case CONSTANT_METHOD_REF:
      info = new MethodRef(jclass);
      break;
    case CONSTANT_INTERFACE_METHOD_REF:
      info = new InterfaceMethodRef(jclass);
      break;
    case CONSTANT_NAME_AND_TYPE:
      info = new NameAndType(jclass);
      break;
    }

    info.load(ds);
  }

  @Override
  public void save(DataStream ds) throws IOException {
    ds.writeByte(tag);
    info.save(ds);
  }

  public static JavaConstant createConstant(JavaClass jclass, byte tag, Object value1, Object value2) {
    JavaConstant constant = searchConstant(jclass, tag, value1, value2);
    if (constant == null) {
      ConstantInfo info = null;
      switch (tag) {
      case JavaConstant.CONSTANT_UTF8:
        info = new UTF8();
        ((UTF8) info).value = (java.lang.String) value1;
        break;
      case JavaConstant.CONSTANT_INTEGER:
        info = new Integer();
        ((Integer) info).value = ((java.lang.Integer) value1).intValue();
        break;
      case JavaConstant.CONSTANT_FLOAT:
        info = new Float();
        ((Float) info).value = ((java.lang.Float) value1).floatValue();
        break;
      case JavaConstant.CONSTANT_LONG:
        info = new Long();
        ((Long) info).value = ((java.lang.Long) value1).longValue();
        break;
      case JavaConstant.CONSTANT_DOUBLE:
        info = new Double();
        ((Double) info).value = ((java.lang.Double) value1).doubleValue();
        break;
      case JavaConstant.CONSTANT_CLASS:
        info = new Class(jclass);
        ((Class) info).value = (JavaConstant) value1;
        break;
      case JavaConstant.CONSTANT_STRING:
        info = new String(jclass);
        ((String) info).value = (JavaConstant) value1;
        break;
      case JavaConstant.CONSTANT_FIELD_REF:
        info = new FieldRef(jclass);
        ((FieldRef) info).value1 = (JavaConstant) value1;
        ((FieldRef) info).value2 = (JavaConstant) value2;
        break;
      case JavaConstant.CONSTANT_METHOD_REF:
        info = new MethodRef(jclass);
        ((MethodRef) info).value1 = (JavaConstant) value1;
        ((MethodRef) info).value2 = (JavaConstant) value2;
        break;
      case JavaConstant.CONSTANT_INTERFACE_METHOD_REF:
        info = new InterfaceMethodRef(jclass);
        ((InterfaceMethodRef) info).value1 = (JavaConstant) value1;
        ((InterfaceMethodRef) info).value2 = (JavaConstant) value2;
        break;
      case JavaConstant.CONSTANT_NAME_AND_TYPE:
        info = new NameAndType(jclass);
        ((NameAndType) info).value1 = (JavaConstant) value1;
        ((NameAndType) info).value2 = (JavaConstant) value2;
        break;
      default:
        return null;
      }

      constant = new JavaConstant(jclass);
      constant.tag = tag;
      constant.info = info;

      jclass.constantPool.addElement(constant);
    }

    return constant;
  }

  private static JavaConstant searchConstant(JavaClass jclass, byte tag, Object value1, Object value2) {
    JavaConstant constant;

    int count = jclass.constantPool.size();
    for (int i = 1; i < count; i++) {
      constant = (JavaConstant) jclass.constantPool.items[i];
      if (constant.tag == tag) {
        boolean result = false;
        switch (tag) {
        case CONSTANT_UTF8:
          result = ((UTF8) constant.info).value.equals(value1);
          break;
        case CONSTANT_INTEGER:
          result = ((Integer) constant.info).value == ((java.lang.Integer) value1).intValue();
          break;
        case CONSTANT_FLOAT:
          result = ((Float) constant.info).value == ((java.lang.Float) value1).floatValue();
          break;
        case CONSTANT_LONG:
          result = ((Long) constant.info).value == ((java.lang.Long) value1).longValue();
          break;
        case CONSTANT_DOUBLE:
          result = ((Double) constant.info).value == ((java.lang.Double) value1).doubleValue();
          break;
        case CONSTANT_CLASS:
          result = ((Class) constant.info).value.equals(value1);
          break;
        case CONSTANT_STRING:
          result = ((String) constant.info).value.equals(value1);
          break;
        case CONSTANT_FIELD_REF:
          result = ((FieldRef) constant.info).value1.equals(value1) && ((FieldRef) constant.info).value2.equals(value2);
          break;
        case CONSTANT_METHOD_REF:
          result = ((MethodRef) constant.info).value1.equals(value1)
              && ((MethodRef) constant.info).value2.equals(value2);
          break;
        case CONSTANT_INTERFACE_METHOD_REF:
          result = ((InterfaceMethodRef) constant.info).value1.equals(value1)
              && ((InterfaceMethodRef) constant.info).value2.equals(value2);
          break;
        case CONSTANT_NAME_AND_TYPE:
          result = ((NameAndType) constant.info).value1.equals(value1)
              && ((NameAndType) constant.info).value2.equals(value2);
          break;
        }

        if (result) {
          return constant;
        }
      }
    }

    return null;
  }
}
