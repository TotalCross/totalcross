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

// $Id: FieldRef.java,v 1.6 2011-01-04 13:18:59 guich Exp $

package tc.tools.converter.bb.constant;

import tc.tools.converter.bb.JavaClass;

public class FieldRef extends DoubleReference
{
   public FieldRef(JavaClass jclass)
   {
      super(jclass);
   }

   public Class getValue1AsClass()
   {
      return (Class)value1.info;
   }

   public NameAndType getValue2AsNameAndType()
   {
      return (NameAndType)value2.info;
   }
}
