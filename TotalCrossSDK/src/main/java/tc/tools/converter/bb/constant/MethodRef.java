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



package tc.tools.converter.bb.constant;

import tc.tools.converter.bb.JavaClass;

public class MethodRef extends DoubleReference
{
  public MethodRef(JavaClass jclass)
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
