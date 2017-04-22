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



package tc.tools.converter.ir.Instruction;

import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class Two16 extends Parameter
{
   public int v1;
   public int v2;

   public Two16(int line, int v1, int v2)
   {
      super(line);
      this.v1 = v1;
      this.v2 = v2;
   }

   public Two16(int line)
   {
      super(line);
   }

   public void set(int v1, int v2)
   {
      this.v1 = v1;
      this.v2 = v2;
   }

   public String toString()
   {
      String print;
      print = v1 + ", "+ v2;
      return print;
   }

   public void toTCCode(Vector vcode)
   {
      TCCode tc = new TCCode(line);
      tc.len = 0;
      tc.two16__v1(v1);
      tc.two16__v2(v2);
      vcode.addElement(tc);
   }
}
