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

public class BC046to053
{
   public BC046to053()
   {
      boolean arBool[] = new boolean[4];
      boolean b1 = arBool[1];

      char arChar[] = new char[4];
      char c1 = arChar[1];

      byte arByte[] = new byte[4];
      byte B1 = arByte[1];

      short arShort[] = new short[4];
      short s1 = arShort[1];

      int arInt[] = new int[4];
      int i1 = arInt[0];
          i1 = arInt[3];
          i1 = arInt[i1];

      long arLong[] = new long[4];
      long l1 = arLong[1];

      float arFloat[] = new float[4];
      float f1 = arFloat[1];

      double arDbl[] = new double[4];
      double d1 = arDbl[1];

      Object arObj[] = new Object[4];
      Object o1 = arObj[1];

      if (false) {b1&=false; c1&=0; B1&=0; s1&=0; l1&=0; f1+=0; d1+=0; arObj[0] = o1;} // remove warnings
   }
}
