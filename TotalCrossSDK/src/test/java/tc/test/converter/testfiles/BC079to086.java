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

public class BC079to086
{
  public BC079to086()
  {
    boolean b1 = true; //regI: 1
    char c1 = 'a';     //regI: 2
    byte B1 = 0;       //regI: 3
    short s1 = 0;      //regI: 4
    int i1 = 0;        //regI: 5
    long l1 = 0;       //reg64: 1
    float f1 = 0;      //reg64: 2
    double d1 = 0;     //reg64: 3
    Object o1 = null;  //regO: 2

    boolean arBool[] = new boolean[4]; // regO: 4
    char arChar[] = new char[4];       // regO: 6
    byte arByte[] = new byte[4];       // regO: 8
    short arShort[] = new short[4];    // regO: 10
    int arInt[] = new int[4];          // regO: 12
    long arLong[] = new long[4];       // regO: 14
    float arFloat[] = new float[4];    // regO: 16
    double arDbl[] = new double[4];    // regO: 18
    Object arObj[] = new Object[4];    // regO: 20

    arBool[1] = b1;  // MOV_arc_regI --> regO(4)[regI(6)]<- regI(1)
    arChar[1] = c1;  // MOV_arc_regI --> regO(6)[regI(7)]<- regI(2)
    arByte[1] = B1;  // MOV_arc_regI --> regO(8)[regI(8)]<- regI(3)
    arShort[1] = s1; // MOV_arc_regI --> regO(10)[regI(9)]<- regI(4)
    arInt[1] = i1;   // MOV_arc_regI --> regO(12)[regI(10)]<- regI(5)
    arLong[1] = l1;  // MOV_arc_reg64 --> regO(14)[regI(11)]<- reg64(1)
    arFloat[1] = f1; // MOV_arc_reg64 --> regO(16)[regI(12)]<- reg64(2)
    arDbl[1] = d1;   // MOV_arc_reg64 --> regO(18)[regI(13)]<- reg64(3)
    arObj[1] = o1;   // MOV_arc_regO --> regO(20)[regI(14)]<- regO(2)
  }
}
