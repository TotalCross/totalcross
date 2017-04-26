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



package tc.tools.converter.java;

import totalcross.io.DataStream;

public final class JavaField
{
   public String name, type;
   public Object constantValue;
   public boolean isPublic, isPrivate, isProtected, isStatic, isFinal, isVolatile, isTransient, is64bit;

   public JavaField(DataStream ds, JavaConstantPool cp) throws totalcross.io.IOException
   {
      // access flags
      int f = ds.readUnsignedShort();
      isPublic    = (f & 0x1) != 0;
      isPrivate   = (f & 0x2) != 0;
      isProtected = (f & 0x4) != 0;
      isStatic    = (f & 0x8) != 0;
      isFinal     = (f & 0x10) != 0;
      isVolatile  = (f & 0x40) != 0;
      isTransient = (f & 0x80) != 0;

      name = (String) cp.constants[ds.readUnsignedShort()];
      type = (String) cp.constants[ds.readUnsignedShort()];
      is64bit = type.charAt(0) == 'D' || type.charAt(0) == 'J'; // double or long
      // read the attributes
      int n = ds.readUnsignedShort();
      for (int i = 0; i < n; i++)
      {
         String attr = (String) cp.constants[ds.readUnsignedShort()];
         int len = ds.readInt();
         if (attr.equals("ConstantValue") && len == 2) // JLS 4.7.2: If a field_info structure representing a non-static field has a ConstantValue attribute, then that attribute must silently be ignored
            constantValue = cp.constants[ds.readUnsignedShort()];
         else
            ds.skipBytes(len);
      }
   }
}
