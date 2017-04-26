/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools.converter.bb;

import tc.tools.converter.*;
import tc.tools.converter.bb.attribute.AttributeInfo;
import tc.tools.converter.bb.attribute.Code;
import tc.tools.converter.bb.attribute.ConstantValue;
import tc.tools.converter.bb.attribute.Deprecated;
import tc.tools.converter.bb.attribute.Exceptions;
import tc.tools.converter.bb.attribute.Generic;
import tc.tools.converter.bb.attribute.InnerClasses;
import tc.tools.converter.bb.attribute.LineNumberTable;
import tc.tools.converter.bb.attribute.LocalVariableTable;
import tc.tools.converter.bb.attribute.SourceFile;
import tc.tools.converter.bb.attribute.Synthetic;
import tc.tools.converter.bb.constant.UTF8;
import totalcross.io.DataStream;
import totalcross.io.IOException;

public class JavaAttribute implements JavaClassStructure
{
   public JavaClass jclass;
   public JavaConstant name;
   public AttributeInfo info;

   public JavaAttribute(JavaClass jclass)
   {
      this.jclass = jclass;
   }

   public String toString()
   {
      return name.toString();
   }

   public int length()
   {
      return 6 + info.length();
   }

   public void load(DataStream ds) throws IOException
   {
      name = jclass.getConstant(ds.readUnsignedShort(), this);
      int len = ds.readInt();

      String value = ((UTF8)name.info).value;
      if (value.equals("ConstantValue"))
         info = new ConstantValue(jclass);
      else if (value.equals("Code"))
         info = new Code(jclass);
      else if (value.equals("Exceptions"))
         info = new Exceptions(jclass);
      else if (value.equals("InnerClasses"))
         info = new InnerClasses(jclass);
      else if (value.equals("Synthetic"))
         info = new Synthetic();
      else if (value.equals("SourceFile"))
         info = new SourceFile(jclass);
      else if (value.equals("LineNumberTable"))
         info = new LineNumberTable();
      else if (value.equals("LocalVariableTable"))
         info = new LocalVariableTable(jclass);
      else if (value.equals("Deprecated"))
         info = new Deprecated();
      else
         info = new Generic(len);

      info.load(ds);
      if (len != info.length())
         throw new ConverterException("Invalid " + value + " length: " + len);
   }

   public void save(DataStream ds) throws IOException
   {
      ds.writeShort(jclass.getConstantIndex(name, this));
      ds.writeInt(info.length());
      info.save(ds);
   }
}
