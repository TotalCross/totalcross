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



package tc.tools.converter.bb.attribute;

import totalcross.io.DataStream;
import totalcross.io.IOException;

public class Generic implements AttributeInfo
{
  public byte[] bytes;

  public Generic(int length)
  {
    bytes = new byte[length];
  }

  @Override
  public int length()
  {
    return bytes.length;
  }

  @Override
  public void load(DataStream ds) throws IOException
  {
    ds.readBytes(bytes);
  }

  @Override
  public void save(DataStream ds) throws IOException
  {
    ds.writeBytes(bytes);
  }
}
