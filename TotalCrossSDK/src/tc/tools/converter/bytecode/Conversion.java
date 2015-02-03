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

public class Conversion extends ByteCode
{
   public int result, operand, srcType;

   public Conversion(int result, int operand, int srcType, int dstType)
   {
      this.result = result;
      this.operand = operand;
      this.srcType = srcType;
      this.targetType = dstType;
      stackInc = 0;
   }
}
