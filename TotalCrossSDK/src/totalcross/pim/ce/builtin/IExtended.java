/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Fabian Kroeher                                            *
 *  Copyright (C) 2003-2012 SuperWaba Ltda.                                      *
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



package totalcross.pim.ce.builtin;
/**
 * superclass of all the converting classes
 * @author Fabian Kroeher
 *
 */
public class IExtended
{
   private String value;
   /**
    * creates a new instance of IExtended
    * @param value the value to convert by this class
    */
   public IExtended(String value)
   {
      this.value = value;
   }
   /**
    * @return a String representation of this Object
    */
   public String toString()
   {
      return value;
   }
}
