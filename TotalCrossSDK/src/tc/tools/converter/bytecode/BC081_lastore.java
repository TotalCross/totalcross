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

// $Id: BC081_lastore.java,v 1.8 2011-01-04 13:18:55 guich Exp $

package tc.tools.converter.bytecode;

public class BC081_lastore extends StoreArray
{
   public BC081_lastore()
   {
      super(-4,0,1,2,LONG);
   }
}
