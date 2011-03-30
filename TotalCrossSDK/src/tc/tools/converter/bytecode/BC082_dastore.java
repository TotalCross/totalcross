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

// $Id: BC082_dastore.java,v 1.8 2011-01-04 13:18:56 guich Exp $

package tc.tools.converter.bytecode;

public class BC082_dastore extends StoreArray
{
   public BC082_dastore()
   {
      super(-4,0,1,2,DOUBLE);
   }
}
