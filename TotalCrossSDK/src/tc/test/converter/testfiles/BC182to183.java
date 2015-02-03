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

public class BC182to183 extends A
{
   public void test()
   {
      BC182to183 thisClass;
      A superClass1;
      B superClass2;

      // testing constructor -::-  class <- new class(...)
      thisClass = new BC182to183();
      thisClass = new BC182to183(1, 2, 3);
      thisClass = new BC182to183(1, 2, 3, 4, 5, 6);

      // testing constructor -::- superclass <- new subclass(...)
      superClass1 = new BC182to183();
      superClass2 = new BC182to183();
      superClass2 = new A();

      // testing constructor -::- superclass <- new superclass(...)
      superClass1 = new A();
      superClass2 = new B();

      // testing internal method call
      m1(1, 2, 3, 4);
      int x = m2(1, 2);
      overloading();

      // testing external method call -::- declared in superclasses
      ma1();
      mb1();
      super.overloading();
      ma2(1);
      ma3(1, 2, 3, 4);

      // testing external method call -::- object of the same type of current class
      thisClass.m3();

      if (false) {Object o = superClass1; o = superClass2; o.toString(); x+=2;} // remove warnings
   }

   public BC182to183()   {  }
   public BC182to183(int x, int y, int z)  {   }
   public BC182to183(int a, int b, int c, int d, int e, int f)  {  }

   public void m1(int a, int b, int c, int d)   { }
   public int m2(int a, int b)
   {
      return (a+b);
   }
   public void m3() {}
   public void overloading() {}
}

class A extends B
{
   public void overloading() {}
   public void ma1()  { }
   public void ma2(int a)  { }
   public void ma3(int a, int b, int c, int d)  { }
}

class B
{
   public void overloading() {}
   public void mb1()  { }
}
