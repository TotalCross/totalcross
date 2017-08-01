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

public class BC002to015
{
  public BC002to015()
  {
    int i;
    i = -1;
    i =  0;
    i =  1;
    i =  2;
    i =  3;
    i =  4;
    i =  5;

    float f;
    f = 0.0f;
    f = 1.0f;
    f = 2.0f;

    double d;
    d = 0.0;
    d = 1.0;

    if (false) {i+=0; f+=0; d+=0;} // remove warnings
  }
}
