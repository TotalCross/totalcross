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
 * Thrown when you try to create a String using the constructor that receives a byte array, an offset and the length.
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

public class StringIndexOutOfBoundsException4D extends IndexOutOfBoundsException {
  /** Constructs an empty Exception. */
  public StringIndexOutOfBoundsException4D() {
  }

  /** Constructs an exception with the given message. */
  public StringIndexOutOfBoundsException4D(String msg) {
    super(msg);
  }

  /**
   * Create an exception noting the illegal index.
   *
   * @param index the invalid index
   */
  public StringIndexOutOfBoundsException4D(int index) {
    super("String index out of range: " + index);
  }
}
