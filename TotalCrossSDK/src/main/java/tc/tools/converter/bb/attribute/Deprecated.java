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

public class Deprecated implements AttributeInfo {
  @Override
  public int length() {
    return 0;
  }

  @Override
  public void load(DataStream ds) throws IOException {
  }

  @Override
  public void save(DataStream ds) throws IOException {
  }
}
