/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package tc.samples.xml.soap.server; // REMOVE THIS PACKAGE AFTER RENAMING TO JWS!

public class TestHandler
{
   public String testParameters(String testString, int testInt, double testDouble, boolean testBoolean)
   {
      return "Test("+testString+","+testInt+","+testDouble+","+testBoolean+") arrived!";
   }

   public int somaInt(int x, int y)
   {
      return x+y;
   }

   public double somaDouble(double x, double y)
   {
      return x+y;
   }

   public boolean returnBoolean(boolean b)
   {
      return b;
   }

   public int sumIntArray(int[] array, String name)
   {
      int soma = 0;
      int len = array.length;
      for (int i=0; i<len; i++)
      {
         soma += array[i];
      }
      return soma;
   }

   public double sumDoubleArray(double[] array, String name)
   {
      double soma = 0;
      int len = array.length;
      for (int i=0; i<len; i++)
      {
         soma += array[i];
      }
      return soma;
   }

   public String[] returnStringArray(String[] s)
   {
      return s;
   }

   public int[] returnIntArray()
   {
      int[] array = {1,2,3,4,5};
      return array;
   }

   public double[] returnDoubleArray()
   {
      double[] array = {1.1, 2.2, 3.3, 4.4, 5.5};
      return array;
   }
}
