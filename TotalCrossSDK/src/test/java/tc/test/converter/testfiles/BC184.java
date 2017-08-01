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

public class BC184 extends BC184_A
{
  public void test()
  {
    /*      BC184 thisClass = new BC184();
      BC184_A superClass = new BC184_A();

      //------- testing internal method calls -------
      sm1();
      this.sm2();
      // these four are the same thing.
      smOverloading();
      this.smOverloading();
      BC184.smOverloading();
      thisClass.smOverloading();


      //------- testing external method calls -------
      super.smA1();
      smA2();
      super.smOverloading();
      this.smA2();
      superClass.smA1();
      BC184_A.smA2();
      BC184_A.smOverloading();
     */   }

  public static void sm1() {}
  public static void sm2() {}
  public static void smOverloading() {}
}

class BC184_A
{
  public static void smA1() {}
  public static void smA2() {}
  public static void smOverloading() {}
}