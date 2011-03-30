/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: I32.java,v 1.7 2011-01-04 13:19:06 guich Exp $

package tc.tools.converter.ir.Instruction;

import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class I32 extends Parameter
{
   public int i32;
   public I32(int line, int v)
   {
      super(line);
      i32 = v;
   }

   public I32(int line)
   {
      super(line);
   }

   public void set(int v)
   {
      i32 = v;
   }

   public String toString()
   {
      String print;
      print = "" + i32;
      return print;
   }

   public void toTCCode(Vector vcode)
   {
      TCCode tc = new TCCode(line);
      tc.len = 0;
      tc.i32__i32(i32);
      vcode.addElement(tc);
   }
}
