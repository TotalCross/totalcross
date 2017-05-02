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

public class BC016to017
{
   public BC016to017()
   {
        byte b;
        b = -128;
        b = 127;
        b = 0;
        b = -32;
        b = 31;
        b = 32;

        short s;
        s = -32768;
        s = 32767;
        s = -2048;
        s = 2047;
        s = 2048;

        if (false) {s = b; b = (byte)s;} // remove warnings
   }
}
