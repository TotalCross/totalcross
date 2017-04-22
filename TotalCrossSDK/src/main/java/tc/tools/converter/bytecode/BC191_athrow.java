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

public class BC191_athrow extends ByteCode
{
   public int oldpc;
   public Object thrownException;

   public BC191_athrow()
   {
      oldpc = pc;
      stackInc = 1;
   }
   public void exec()
   {
      Object o = stack[stackPtr-1].asObj;
      if (o == null)
      {
         stackInc = -1;
         thrownException = "java.lang.NullPointerException";
      }
      else
      {
         thrownException = (String)o;
         // stack = local;
         // stack += method->maxLocals;
         // stack[stackPtr].asObj = thrownException
      }
   }
}
