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



package tc.tools.converter.tclass;

/* Debug info - line number information */
public final class TCLineNumber
{
   // The pc where this line number goes
   public int /*uint16*/ startPC;
   // The line number itself;
   public int /*uint16*/ lineNumber;

   public TCLineNumber(int startPC, int lineNumber)
   {
      this.startPC = startPC;
      this.lineNumber = lineNumber;
   }
}
