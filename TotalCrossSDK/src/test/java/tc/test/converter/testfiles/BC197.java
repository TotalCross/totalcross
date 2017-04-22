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

public class BC197
{
   public BC197()
   {
      boolean v1[][] = new boolean[1][2];
      int v2[][][]   = new int[1][2][3];
      int v3[][][][] = new int[1][2][3][4];
      long v4[][][][][] = new long[1][2][3][4][5];
      double v5[][][][][][][][][] = new double[1][2][3][4][5][6][7][8][9];
      Object v6[][]= new Object[1][2];
      BC197 v7[][]= new BC197[1][2];
      int v8[][][] = new int[193][191][192];
      long v9[][][][][] = new long[1][2][][][];
      v9[1][1] = new long[1][2][3];

      if (false) {Object o; o=v1; o=v2; o=v3; o=v4; o=v5; o=v6; o=v7; o=v8; o.toString();}
   }
}
