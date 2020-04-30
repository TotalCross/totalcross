// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

import totalcross.sys.Vm;

public class TestTypes extends Object
{
   public byte b=100;                   // int32[0]
   public short s=-23000;               // int32[1]
   public int i=0x12345678;             // int32[2]
   public char c='A';                   // int32[3]
   public double d=-1234.567;           // value64[0]
   public long l=-0x12345678ABCEL;      // value64[1]
   public String o="Vera Nardelli";     // object[0]

   public byte[] ab;                    // object[1]
   public short[] as;                   // object[2]
   public int[] ai;                     // object[3]
   public char[] ac;                    // object[4]
   public double[] ad;                  // object[5]
   public long[] al;                    // object[6]
   public String[] ao;                  // object[7]

   public static double getDouble() {return -1234.567;} // DOUBLE_TEST_VALUE

   public TestTypes()
   {
      print("Vera Nardelli");
      // these will be tested, so we must place the code here so that the extMtd tables can be correctly generated
      "Vera Nardelli".substring(0,4);
      TestTypes.getDouble();
      int[][][][][] v = new int[2][3][200][1][4];
      // used in testExtFields
      TestExt.si = i;
      TestExt.so = null;
      TestExt.sd = d;
      TestExt.sl = l;
      TestExt te = new TestExt();
      te.i = 1;
      te.o = null;
      te.d = d;
      te.l = l;
   }

   // tests exception being thrown and handled in the same method
   public static void testException() {methodA();}
   public static void methodA()       {methodB();}
   public static void methodB()       {methodC();}
   public static void methodC()
   {
      try
      {
         Object o = null;
         o.toString();
      }
      catch (NullPointerException npe)
      {
         printException(npe.getMessage());
      }
   }

   // tests exception being thrown in a method and handled in another one
   public static void testException2()
   {
      methodA2();
   }
   public static void methodA2()
   {
      try
      {
         methodB2();
      }
      catch (NullPointerException npe)
      {
         printException(npe.getMessage());
      }
   }
   public static void methodB2()
   {
      methodC2();
   }
   public static void methodC2()
   {
      Object o = null;
      o.toString();
   }

   // tests exception being explicitly thrown in a method and handled in another one
   public static void testException3()
   {
      methodA3();
   }
   public static void methodA3()
   {
      try
      {
         methodB3();
      }
      catch (Exception e)
      {
         printException(e.getMessage());
      }
   }
   public static void methodB3() throws Exception
   {
      methodC3();
   }
   public static void methodC3() throws Exception
   {
      throw new Exception("Testing exceptions (3)");
   }
   public static native boolean print(String s);
   public static native boolean printException(String s);
}
