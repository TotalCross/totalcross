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

public class BC159to166
{
  public BC159to166()
  {
    int a = 1, b = 1, c = 10;

    if (a == b)
    {
      a = a + b;
    }
    else
    {
      b = a - b;
    }

    a = a * b;


    while (a < c)
    {
      a = a + 1;
    }
  }
}
