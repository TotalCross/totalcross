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

package tc.test.converter.testfiles;

public class BC025_042to045 {
  public BC025_042to045() {
    Object o1 = null, o2 = null, o3 = null, o4 = null, o;

    o = o1;
    o = o2;
    o = o3;
    o = o4;

    if (false) {
      o1 = o; // remove warnings
    }
  }
}
