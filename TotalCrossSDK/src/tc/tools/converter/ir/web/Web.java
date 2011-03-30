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

// $Id: Web.java,v 1.6 2011-01-04 13:19:22 guich Exp $

package tc.tools.converter.ir.web;

import totalcross.util.*;
import tc.tools.converter.ir.Instruction.*;

public class Web
{
   public int type; // opr_regI / opr_regO / opr_regD / opr_regL
   public int number;
   public Vector dChain = new Vector(32);
   public Vector uChain = new Vector(32);

   public Web(int t, int n)
   {
      type = t;
      number = n;
   }

   public void addDefinition(Instruction i)
   {
      dChain.addElement(i);
   }

   public void addUse(Instruction i)
   {
      uChain.addElement(i);
   }
}
