/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package litebase;

// juliana@224_2: improved memory usage on BlackBerry.
/**
 * It is used when a object storing an integer is needed.
 */
class Int
{
   /**
    * The integer value stored in the object.
    */
   int value;
   
   /**
    * Creates the object.
    * @param integer
    */
   Int(int integer)
   {
      value = integer;
   }
}