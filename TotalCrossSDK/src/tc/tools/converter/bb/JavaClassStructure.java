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

// $Id: JavaClassStructure.java,v 1.6 2011-01-04 13:19:21 guich Exp $

package tc.tools.converter.bb;

import totalcross.io.DataStream;
import totalcross.io.IOException;

public interface JavaClassStructure
{
   public int length();

   public void load(DataStream ds) throws IOException;

   public void save(DataStream ds) throws IOException;
}
