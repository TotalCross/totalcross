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

public class BC190 {
  public BC190() {
    int len = 0;

    int arInt[] = new int[4];
    len = arInt.length;

    arInt = new int[32];
    len = arInt.length;

    arInt = new int[len];
    len = arInt.length;
  }
}
