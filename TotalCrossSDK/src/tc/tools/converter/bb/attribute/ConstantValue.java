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



package tc.tools.converter.bb.attribute;

import tc.tools.converter.bb.JavaClass;
import tc.tools.converter.bb.JavaConstant;
import totalcross.io.DataStream;
import totalcross.io.IOException;

public class ConstantValue implements AttributeInfo
{
   private JavaClass jclass;

   public JavaConstant value;

   public ConstantValue(JavaClass jclass)
   {
      this.jclass = jclass;
   }

   public int length()
   {
      return 2;
   }

   public void load(DataStream ds) throws IOException
   {
      value = jclass.getConstant(ds.readUnsignedShort(), this);
   }

   public void save(DataStream ds) throws IOException
   {
      ds.writeShort(jclass.getConstantIndex(value, this));
   }
}
