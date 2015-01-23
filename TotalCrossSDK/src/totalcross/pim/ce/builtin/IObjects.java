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
import totalcross.util.*;
/**
 * represents a collection of IObjects
 * @author Fabian Kroeher
 *
 */
abstract class IObjects
{
   protected Vector objects;
   /**
    * creates a new instance of IObjects and fills the objects-Vector with new instances
    * of IObject which are created from the data of the nativeString
    * @param nativeString a StringExt from a native method call of CeIoBuiltIn.dll
    */
   public IObjects(StringExt nativeString)
   {
      objects = new Vector();
      while (nativeString.length() > 0)
         objects.addElement(newObject(nativeString));
   }
   /**
    * @return the number of IObjects this instance contains
    */
   public int size()
   {
      return objects.size();
   }
   /**
    * returns a new IObject which is filled with the data from the nativeString
    * @param nativeString a StringExt from a native method call of CeIoBuiltIn.dll
    * @return a new IObject
    */
   abstract public IObject newObject(StringExt nativeString);
}
