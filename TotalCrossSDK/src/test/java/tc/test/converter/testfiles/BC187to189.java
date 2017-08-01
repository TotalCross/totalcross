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

public class BC187to189
{
  public BC187to189()
  {
    boolean arBool[] = new boolean[2];   // type code =  4;  NEWARRAY_len
    char arChar[]    = new char[4];      // type code =  5;  NEWARRAY_len
    float arFloat[]  = new float[8];     // type code =  6;  NEWARRAY_len
    double arDbl[]   = new double[16];   // type code =  7;  NEWARRAY_len
    byte arByte[]    = new byte[32];     // type code =  8;  NEWARRAY_regI
    short arShort[]  = new short[64];    // type code =  9;  NEWARRAY_regI
    int arInt[]      = new int[128];     // type code =  10; NEWARRAY_regI
    long arLong[]    = new long[256];    // type code =  11; NEWARRAY_regI

    int len = 4;
    arInt = new int[len]; // NEWARRAY_regI

    Object arObj1 [] = new Object[4];
    BC187to189 arObj2 [] = new BC187to189[4];

    if (false) {Object o; o = arBool; o = arChar; o = arFloat; o = arDbl; o = arByte; o = arShort; o = arInt; o = arLong; o = arObj1; o = arObj2; arObj1[0] = o;} // remove warnings
  }
}

class X
{
}