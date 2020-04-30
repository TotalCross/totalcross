// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

public final class Class4D<T> {
  // place holders for the VM
  Object nativeStruct; // TClass
  String targetName; // java.lang.String
  String ncached, cached;

  /** The TotalCross deployer can find classes that are instantiated using Class.forName if, and only if, they are
   * String constants. If you build the className dynamically, then you must include the file passing it to the tc.Deploy
   * application (the deployer will warn you about that).
   * @see totalcross.sys.Vm#attachLibrary
   */
  native public static Class<?> forName(String className) throws java.lang.ClassNotFoundException;

  /** Creates a new instance of this class. The class must have a default and public constructor (E.G.: <code>public MyClass()</code>)
   * @throws InstantiationException If you try to instantiate an interface, abstract class or array
   * @throws IllegalAccessException If you try to instantiate a private class
   */
  native public Object newInstance() throws java.lang.InstantiationException, java.lang.IllegalAccessException;

  /** Returns true if the given object is an instance of this class. */
  native public boolean isInstance(Object obj);

  /** Returns the fully qualified name of this class. */
  public String getName() {
    if (ncached != null) {
      return ncached;
    }
    if (isPrimitive()) {
      if (targetName.endsWith(".Integer")) {
        return "int";
      }
      if (targetName.endsWith(".Character")) {
        return "char";
      }
      if (targetName.endsWith(".Byte")) {
        return "byte";
      }
      if (targetName.endsWith(".Short")) {
        return "short";
      }
      if (targetName.endsWith(".Long")) {
        return "long";
      }
      if (targetName.endsWith(".Float")) {
        return "float";
      }
      if (targetName.endsWith(".Double")) {
        return "double";
      }
      if (targetName.endsWith(".Boolean")) {
        return "boolean";
      }
    }
    return ncached = targetName;
  }

  public String getCanonicalName() {
    return getName();
  }

  /** Returns the fully qualified name of this class. */
  @Override
  public String toString() {
    if (cached != null) {
      return cached;
    }
    return cached = isPrimitive() ? getName() : "class " + getName();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Class4D && ((Class4D) o).getName().equals(getName());
  }

  /**
   * Returns the enumeration constants of this class, or
   * null if this class is not an <code>Enum</code>.
   *
   * @return an array of <code>Enum</code> constants
   *         associated with this class, or null if this
   *         class is not an <code>enum</code>.
   * @since 1.5
   */
  public T[] getEnumConstants() {
    try {
      Method m = getMethod("values", new Class[0]);
      return (T[]) m.invoke(null, new Object[0]);
    } catch (NoSuchMethodException exception) {
      throw new Error("Enum lacks values() method");
    } catch (IllegalAccessException exception) {
      throw new Error("Unable to access Enum class");
    } catch (InvocationTargetException exception) {
      throw new RuntimeException("The values method threw an exception", exception);
    }
  }

  public String getSimpleName() {
    String s = getName();
    return s.substring(s.lastIndexOf('.') + 1);
  }

  public T cast(Object obj) {
    if (obj != null && !isInstance(obj)) {
      throw new ClassCastException("Can't cast " + obj);
    }
    return (T) obj;
  }

  public native boolean isAssignableFrom(Class<?> cls);

  public native boolean isInterface();

  public native boolean isArray();

  public native boolean isPrimitive();

  public native Class<?> getSuperclass();

  public native Class<?>[] getInterfaces();

  public native Class<?> getComponentType();

  public native int getModifiers();

  public native Object[] getSigners();

  public native Field[] getFields() throws SecurityException;

  public native Method[] getMethods() throws SecurityException;

  public native Constructor<?>[] getConstructors() throws SecurityException;

  public native Field getField(String name) throws NoSuchFieldException, SecurityException;

  public native Method getMethod(String name, Class<?> parameterTypes[])
      throws NoSuchMethodException, SecurityException;

  public native Constructor<?> getConstructor(Class<?> parameterTypes[])
      throws NoSuchMethodException, SecurityException;

  public native Field[] getDeclaredFields() throws SecurityException;

  public native Method[] getDeclaredMethods() throws SecurityException;

  public native Constructor<?>[] getDeclaredConstructors() throws SecurityException;

  public native Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException;

  public native Method getDeclaredMethod(String name, Class<?> parameterTypes[])
      throws NoSuchMethodException, SecurityException;

  public native Constructor<?> getDeclaredConstructor(Class<?> parameterTypes[])
      throws NoSuchMethodException, SecurityException;

  public boolean desiredAssertionStatus() {
    return false;
  }

}