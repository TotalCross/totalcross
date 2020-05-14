// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang;

/** 
 * General exception class. 
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

public class Exception4D extends Throwable4D {
  /** Constructs an empty Exception. */
  public Exception4D() {
  }

  /** Constructs an exception with the given message. */
  public Exception4D(String msg) {
    super(msg);
  }

  /**
   * Create an exception with a message and a cause.
   *
   * @param s the message string
   * @param cause the cause of this error
   * @since 1.4
   */
  public Exception4D(String s, Throwable4D cause) {
    super(s, cause);
  }

  /**
   * Create an exception with a given cause, and a message of
   * <code>cause == null ? null : cause.toString()</code>.
   *
   * @param cause the cause of this exception
   * @since 1.4
   */
  public Exception4D(Throwable4D cause) {
    super(cause);
  }

  public Exception4D(String message, Throwable4D cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
