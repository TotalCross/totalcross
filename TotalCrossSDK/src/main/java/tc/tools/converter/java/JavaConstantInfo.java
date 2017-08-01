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



package tc.tools.converter.java;

public final class JavaConstantInfo
{
  public int index1,index2;
  public byte type;

  public JavaConstantInfo(byte type, int index1)
  {
    this.index1 = index1;
    this.type = type;
  }
  public JavaConstantInfo(byte type, int index1, int index2)
  {
    this.index1 = index1;
    this.index2 = index2;
    this.type = type;
  }
}

