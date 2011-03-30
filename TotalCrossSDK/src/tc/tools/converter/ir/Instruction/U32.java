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

// $Id: U32.java,v 1.7 2011-01-04 13:19:06 guich Exp $

package tc.tools.converter.ir.Instruction;

import totalcross.util.*;
import tc.tools.converter.tclass.*;

public class U32 extends Parameter
{
   public int u32;
   public U32(int line, int v)
   {
      super(line);
      u32 = v;
   }

   public U32(int line)
   {
      super(line);
   }

   public void set(int v)
   {
      u32 = v;
   }

   public String toString()
   {
      String print = ""+u32;
      return print;
   }

   public void toTCCode(Vector vcode)
   {
      TCCode tc = new TCCode(line);
      tc.len = 0;
      tc.u32__u32(u32);
      vcode.addElement(tc);
   }
}
