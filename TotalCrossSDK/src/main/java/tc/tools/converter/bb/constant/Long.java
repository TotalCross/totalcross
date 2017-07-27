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

import totalcross.io.DataStream;
import totalcross.io.IOException;

public class Long implements ConstantInfo
{
  public long value;

  @Override
  public java.lang.String toString()
  {
    return "" + value;
  }

  @Override
  public int length()
  {
    return 8;
  }

  @Override
  public void load(DataStream ds) throws IOException
  {
    value = ds.readLong();
  }

  @Override
  public void save(DataStream ds) throws IOException
  {
    ds.writeLong(value);
  }
}
