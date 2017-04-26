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



package tc.tools.converter.bytecode;

import tc.tools.converter.*;
import tc.tools.converter.java.*;

public class BC018_ldc extends LoadLocal
{
   public TCValue val = new TCValue();

   public BC018_ldc()
   {
      this(readUInt8(pc+1),2);
   }
   public BC018_ldc(int ofs, int pcInc)
   {
      super(ofs,0);
      this.pcInc = pcInc;
      Object o = cp.constants[ofs];
      if (o instanceof JavaConstantInfo)
      {
         JavaConstantInfo jci = (JavaConstantInfo)o;
         o = cp.constants[jci.index1];
      }
      if (o instanceof String)
      {
         targetType = val.type = OBJECT;
         val.asObj = o;
      }
      else
      if (o instanceof Integer)
      {
         targetType = val.type = INT;
         val.asInt = ((Integer)o).intValue();
      }
      else
      if (o instanceof Float)
      {
         targetType = val.type = DOUBLE;
         val.asDouble = ((Float)o).floatValue();
      }
      else
      if (o instanceof Double)
      {
         targetType = val.type = DOUBLE;
         val.asDouble = ((Double)o).doubleValue();
      }
      else
      if (o instanceof Long)
      {
         targetType = val.type = LONG;
         val.asLong = ((Long)o).longValue();
      }
      else
         System.out.println("Invalid type in LDC: "+o);
   }

   public void exec()
   {
       stack[stackPtr].copyFrom(val);
   }

   public String toString()
   {
      return super.toString()+"->"+val;
   }
}
