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

public class BC193 implements BC193_C
{
  public BC193()
  {
    BC193 c1 = new BC193();
    BC193_A c2 = new BC193_A();
    BC193 c3 = new BC193();
    BC193_A c4 = new BC193_B();
    if (c1 instanceof BC193)
    {
    }
    if (c2 instanceof BC193_A)
    {
    }
    if (c3 instanceof BC193)
    {
    }
    if (c4 instanceof BC193_B)
    {
    }
    if (c4 instanceof BC193_A)
    {
    }
    if (c1 instanceof BC193_C)
    {
    }
  }
}

class BC193_A
{
}

class BC193_B extends BC193_A
{
}

interface BC193_C
{
}
