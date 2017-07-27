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

public class LoadLocal extends ByteCode
{
  /** Index in the local array */
  public int localIdx;

  public LoadLocal(int idx, int type)
  {
    this.localIdx = idx;
    this.targetType = type;
  }
  @Override
  public void exec()
  {
    stack[stackPtr].copyFrom(local[localIdx]);
  }
}
