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

public class BC185 implements BC185_A
{
   public void test()
   {
      BC185 thisClass = new BC185();
      BC185_A superInterface = new BC185();
      int x = 1;

      mOverloading(1);  // this is not INVOKEINTERFACE
      thisClass.mOverloading(1); // this is not INVOKEINTERFACE
      superInterface.mOverloading(x); // INVOKEINTERFACE
      superInterface.mOverloading(x); // INVOKEINTERFACE (Repeated intentionally)
   }

   public void mOverloading(int i) {}
}

interface BC185_A
{
   public void mOverloading(int i);
}
