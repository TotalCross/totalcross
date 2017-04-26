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

public final class JavaException
{
   public int    startPC, endPC, handlerPC;
   public String catchType;

   public JavaException(DataStream ds, JavaConstantPool cp) throws totalcross.io.IOException
   {
      startPC = ds.readUnsignedShort();
      endPC = ds.readUnsignedShort();
      handlerPC = ds.readUnsignedShort();
      int c = ds.readUnsignedShort();
      if (c > 0) // If the value of the catch_type item is zero, this exception handler is called for all exceptions. This is used to implement finally
         catchType = cp.getString1(c);
   }

   public boolean isFinallyHandler()
   {
      return catchType == null;
   }
}
