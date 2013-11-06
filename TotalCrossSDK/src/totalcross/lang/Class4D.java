/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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

import java.lang.reflect.*;

/** 
 * This class contains utility methods that are used to load classes by name 
 * and get information about their fields and methods. 
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

public final class Class4D
{
   // place holders for the VM
   Object nativeClass; // TClass
   String targetName;  // java.lang.String

   /** The TotalCross deployer can find classes that are instantiated using Class.forName if, and only if, they are
    * String constants. If you build the className dynamically, then you must include the file passing it to the tc.Deploy
    * application (the deployer will warn you about that).
    * @see totalcross.sys.Vm#attachLibrary
    */
   native public static Class forName(String className) throws java.lang.ClassNotFoundException;

   /** Creates a new instance of this class.
    * @throws InstantiationException If you try to instantiate an interface, abstract class or array
    * @throws IllegalAccessException If you try to instantiate a private class
    */
   native public Object newInstance() throws java.lang.InstantiationException, java.lang.IllegalAccessException;

   /** Returns true if the given object is an instance of this class. */ 
   native public boolean isInstance(Object obj);

   /** Returns the fully qualified name of this class. */
   public String getName()
   {
      return targetName;
   }
   
   /** Returns the fully qualified name of this class. */
   public String toString()
   {
   	return targetName;
   }
   
   public boolean equals(Object o)
   {
      return o instanceof Class && ((Class)o).getName().equals(getName());
   }
   
   public native boolean isAssignableFrom(Class cls);
   public native boolean isInterface();
   public native boolean isArray();
   public native boolean isPrimitive();
   public native Class getSuperclass();
   public native Class[] getInterfaces();
   public native Class getComponentType();
   public native int getModifiers();
   public native Object[] getSigners();
   public native Field[] getFields() throws SecurityException;
   public native Method[] getMethods() throws SecurityException;
   public native Constructor[] getConstructors() throws SecurityException;
   public native Field getField(String name) throws NoSuchFieldException, SecurityException;
   public native Method getMethod(String name, Class parameterTypes[]) throws NoSuchMethodException, SecurityException;
   public native Constructor getConstructor(Class parameterTypes[]) throws NoSuchMethodException, SecurityException;
   public native Field[] getDeclaredFields() throws SecurityException;
   public native Method[] getDeclaredMethods() throws SecurityException;
   public native Constructor[] getDeclaredConstructors() throws SecurityException;
   public native Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException;
   public native Method getDeclaredMethod(String name, Class parameterTypes[]) throws NoSuchMethodException, SecurityException;
   public native Constructor getDeclaredConstructor(Class parameterTypes[]) throws NoSuchMethodException, SecurityException;
   
}