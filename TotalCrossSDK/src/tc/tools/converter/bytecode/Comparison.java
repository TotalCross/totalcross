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

// $Id: Comparison.java,v 1.9 2011-01-04 13:18:54 guich Exp $

package tc.tools.converter.bytecode;

public class Comparison extends ByteCode
{
   public int result, left, right, srcType;

   public Comparison(int stackInc, int result, int left, int right, int srcType)
   {
      this.stackInc = stackInc;
      this.result = result;
      this.left = left;
      this.right = right;
      this.srcType = srcType;
      targetType = BOOLEAN;
   }
}
