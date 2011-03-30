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

// $Id: BC149_fcmpl.java,v 1.9 2011-01-04 13:18:56 guich Exp $

package tc.tools.converter.bytecode;

public class BC149_fcmpl extends Comparison
{
   public BC149_fcmpl()
   {
      super(-1,-2,-2,-1,FLOAT);
   }
   public void exec()
   {
      double r = stack[stackPtr-2].asDouble - stack[stackPtr-1].asDouble;
      stack[stackPtr-2].asInt = (r > 0)?1:(r < 0)?-1:0;
   }
}
