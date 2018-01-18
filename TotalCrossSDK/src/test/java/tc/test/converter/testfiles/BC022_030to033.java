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

public class BC022_030to033 {
  public BC022_030to033() {
    long l1 = 1, l2 = 2, l3 = 3, l4 = 4, l;
    l = l1;
    l = l2;
    l = l3;
    l = l4;

    if (false) {
      l += 0; // remove warnings
    }
  }
}
