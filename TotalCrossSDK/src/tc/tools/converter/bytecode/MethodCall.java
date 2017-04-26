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

public class MethodCall extends ByteCode
{
   public String name, ret, parameters;
   public String signature;
   public String className;
   public String []jargs; // args in Java's format
   public TCValue[] args;
   private static StringBuffer retsb = new StringBuffer(30);

   public MethodCall(int idx)
   {
      JavaConstantInfo jci = (JavaConstantInfo)cp.constants[idx];
      int classIndex = jci.index1;
      int nameAndTypeIndex = jci.index2;
      className = cp.getString1(classIndex);
      name = cp.getString1(nameAndTypeIndex);
      parameters = cp.getString2(nameAndTypeIndex);
      pcInc = 3;
      parseSignature();
      targetType = convertJavaType(ret);
   }

   public void parseSignature()
   {
      jargs = JavaMethod.splitParams(parameters, retsb);
      ret = retsb.toString();
      signature = name+parameters.substring(0,parameters.length()-ret.length()); // (I)V -> (I)
      if (jargs != null)
      {
         args = new TCValue[jargs.length];
         for (int j =0; j < args.length; j++)
         {
            String p = jargs[j];
            int t = convertJavaType(p);
            args[j] = new TCValue();
            args[j].asInt = t;
            if (t == OBJECT)
               args[j].asObj = p;
         }
      }
   }
}
