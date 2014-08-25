/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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



package totalcross.lang;

/**
 * Object is the base class for all objects.
 * <br><br>
 * Important: the finalize method cannot create any kind of objects; you must only destruct and close objects. Also, be careful: when the vm exits, it calls the finalize objects in any order, so an object you use in a class may have been already collected.
 * <br><br>
 * The number of methods in this class is smaller than the one used in Java SE.
 * <br><br>
 * IMPORTANT: the totalcross.lang package is the java.lang that will be used in the device.
 * You CANNOT use nor import totalcross.lang package in desktop. When tc.Deploy is called,
 * all references to java.lang are replaced by totalcross.lang automatically. Given this,
 * you must use only the classes and methods that exists BOTH in java.lang and totalcross.lang.
 * For example, you can't use java.lang.ClassLoader because there are no totalcross.lang.ClassLoader.
 * Another example, you can't use java.lang.String.indexOfIgnoreCase because there are no
 * totalcross.lang.String.indexOfIgnoreCase method. Trying to use a class or method from the java.lang package
 * that has no correspondence with totalcross.lang will make the tc.Deploy program to abort, informing
 * where the problem occured. A good idea is to always refer to this javadoc to know what is and what isn't
 * available.
 */

public class Object4D
{
	// Note: The TotalCross Object class cannot have a constructor neither fields, because the VM relies in this to make some important optimizations.

   /** Returns the string representation of the object, that is full_class_name@internal_address_hex. Note that, differently from JDK, the package separator is / instead of .
     */
   public String toString()
   {
	   return toStringNative();
   }

   /** Returns the hashcode of this object. */
   public int hashCode()
   {
      return nativeHashCode();
   }

   native private String toStringNative();
   native private int nativeHashCode();

   /** The equals method for class Object implements the most discriminating
   possible equivalence relation on objects; that is, for any reference values
   x and y, this method returns true if and only if x and y refer to the same
   object (x==y has the value true).
   @since SuperWaba 2.0 */
   public boolean equals(Object other)
   {
      return this == other;
   }

   /** Returns the Class that this object represents.
     * @since SuperWaba 3.4
     */
   native public final Class<?> getClass4D();
}