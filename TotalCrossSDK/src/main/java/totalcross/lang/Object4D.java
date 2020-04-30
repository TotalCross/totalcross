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

public class Object4D {
  // Note: The TotalCross Object class cannot have a constructor neither fields, because the VM relies in this to make some important optimizations.

  /** Returns the string representation of the object, that is full_class_name@internal_address_hex. Note that, differently from JDK, the package separator is / instead of .
   */
  @Override
  public String toString() {
    return toStringNative();
  }

  /** Returns the hashcode of this object. */
  @Override
  public int hashCode() {
    return nativeHashCode();
  }

  native private String toStringNative();

  native private int nativeHashCode();

  /** The equals method for class Object implements the most discriminating
   possible equivalence relation on objects; that is, for any reference values
   x and y, this method returns true if and only if x and y refer to the same
   object (x==y has the value true).
   @since SuperWaba 2.0 */
  @Override
  public boolean equals(Object other) {
    return this == other;
  }

  /** Returns the Class that this object represents.
   * @since SuperWaba 3.4
   */
  native public final Class<?> getClass4D();

  /**
   * Creates and returns a copy of this object.  The precise meaning
   * of "copy" may depend on the class of the object. The general
   * intent is that, for any object {@code x}, the expression:
   * <blockquote>
   * <pre>
   * x.clone() != x</pre></blockquote>
   * will be true, and that the expression:
   * <blockquote>
   * <pre>
   * x.clone().getClass() == x.getClass()</pre></blockquote>
   * will be {@code true}, but these are not absolute requirements.
   * While it is typically the case that:
   * <blockquote>
   * <pre>
   * x.clone().equals(x)</pre></blockquote>
   * will be {@code true}, this is not an absolute requirement.
   * <p>
   * By convention, the returned object should be obtained by calling
   * {@code super.clone}.  If a class and all of its superclasses (except
   * {@code Object}) obey this convention, it will be the case that
   * {@code x.clone().getClass() == x.getClass()}.
   * <p>
   * By convention, the object returned by this method should be independent
   * of this object (which is being cloned).  To achieve this independence,
   * it may be necessary to modify one or more fields of the object returned
   * by {@code super.clone} before returning it.  Typically, this means
   * copying any mutable objects that comprise the internal "deep structure"
   * of the object being cloned and replacing the references to these
   * objects with references to the copies.  If a class contains only
   * primitive fields or references to immutable objects, then it is usually
   * the case that no fields in the object returned by {@code super.clone}
   * need to be modified.
   * <p>
   * The method {@code clone} for class {@code Object} performs a
   * specific cloning operation. First, if the class of this object does
   * not implement the interface {@code Cloneable}, then a
   * {@code CloneNotSupportedException} is thrown. Note that all arrays
   * are considered to implement the interface {@code Cloneable} and that
   * the return type of the {@code clone} method of an array type {@code T[]}
   * is {@code T[]} where T is any reference or primitive type.
   * Otherwise, this method creates a new instance of the class of this
   * object and initializes all its fields with exactly the contents of
   * the corresponding fields of this object, as if by assignment; the
   * contents of the fields are not themselves cloned. Thus, this method
   * performs a "shallow copy" of this object, not a "deep copy" operation.
   * <p>
   * The class {@code Object} does not itself implement the interface
   * {@code Cloneable}, so calling the {@code clone} method on an object
   * whose class is {@code Object} will result in throwing an
   * exception at run time.
   *
   * @return     a clone of this instance.
   * @throws  CloneNotSupportedException  if the object's class does not
   *               support the {@code Cloneable} interface. Subclasses
   *               that override the {@code clone} method can also
   *               throw this exception to indicate that an instance cannot
   *               be cloned.
   * @see java.lang.Cloneable
   */
  @Override
  protected native Object clone() throws CloneNotSupportedException;
}