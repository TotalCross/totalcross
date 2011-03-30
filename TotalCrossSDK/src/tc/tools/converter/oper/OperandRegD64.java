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

// $Id: OperandRegD64.java,v 1.7 2011-01-04 13:19:19 guich Exp $

package tc.tools.converter.oper;

public class OperandRegD64 extends OperandRegD
{
   public OperandRegD64()
   {
      super();
      this.nWords = 2;
   }

   public OperandRegD64(int framePosition)
   {
      super(framePosition);
      this.nWords = 2;
   }

   public OperandRegD64(String wordIndex, int index)
   {
      super(wordIndex, index);
      nWords = 2;
   }
}
