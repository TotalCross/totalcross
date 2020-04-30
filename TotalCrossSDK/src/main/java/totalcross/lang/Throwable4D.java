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

import java.util.ArrayList;

/**
 * Base class of all Exceptions. 
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

public class Throwable4D {
  /** Stores the message given when constructing this Throwable object */
  protected String msg;

  public String trace;

  /**
   * The throwable that caused this throwable to get thrown, or null if this 
   * throwable was not caused by another throwable, or if the causative 
   * throwable is unknown. If this field is equal to this throwable itself, 
   * it indicates that the cause of this throwable has not yet been 
   * initialized.
   *
   * @since 1.4
   */
  private Throwable4D cause = this;

  private ArrayList<Throwable4D> suppressed;
  private Throwable4D[] cachedSuppressedArray;
  
  private StackTraceElement[] stackTrace;

  /** Constructs an empty Exception. */
  public Throwable4D() {
    this(null, null);
  }

  /** Constructs an exception with the given message. */
  public Throwable4D(String msg) {
    this(msg, null);
  }

  /**
   * Instantiate this Throwable with the given message and cause. Note that
   * the message is unrelated to the message of the cause.
   *
   * @param message the message to associate with the Throwable
   * @param cause the cause, may be null
   * @since 1.4
   */
  public Throwable4D(String message, Throwable4D cause) {
    this(message, cause, true, true);
  }

  public Throwable4D(String message, Throwable4D cause, boolean enableSuppression, boolean writableStackTrace) {
    this.msg = message;
    this.cause = cause;
  }

  /**
   * Instantiate this Throwable with the given cause. The message is then
   * built as <code>cause == null ? null : cause.toString()</code>.
   *
   * @param cause the cause, may be null
   * @since 1.4
   */
  public Throwable4D(Throwable4D cause) {
    this(cause == null ? null : cause.toString(), cause);
  }

  /** Returns the message passed on the constructor. May be null. */
  public String getMessage() {
    return msg;
  }

  @Override
  public String toString() {
    String ret = getClass().getName();
    if (msg != null) {
      ret += ": " + msg;
    }
    return ret;
  }

  /**
   * Prints the stack trace to the debug console.
   */
  public void printStackTrace() // guich@300_23
  {
    totalcross.sys.Vm.warning(toString());
    printStackTraceNative();
    if (cause != null && cause != this) {
      totalcross.sys.Vm.warning("Caused by ");
      cause.printStackTrace(); // allows recursion
    }
  }

  native private void printStackTraceNative();

  public void printStackTrace(Object o) {
  } // guich@582_6: just a place-holder to let it build-device

  public Throwable4D getCause() {
    return (cause == this ? null : cause);
  }

  public Throwable4D initCause(Throwable4D cause) {
    if (this.cause != this) {
      throw new IllegalStateException("Can't overwrite cause");
    }
    if (cause == this) {
      throw new IllegalArgumentException("Self-causation not permitted");
    }
    this.cause = cause;
    return this;
  }

  public final void addSuppressed(Throwable4D exception) {
    if (this.suppressed == null) {
      this.suppressed = new ArrayList<>();
    }
    this.suppressed.add(exception);
    cachedSuppressedArray = null;
  }

  public final Throwable4D[] getSuppressed() {
    if (cachedSuppressedArray == null) {
      if (this.suppressed == null) {
        return new Throwable4D[0];
      }
      cachedSuppressedArray = this.suppressed.toArray(new Throwable4D[0]);
    }
    return cachedSuppressedArray;
  }

    public StackTraceElement[] getStackTrace() {
        if (stackTrace == null) {
            String[] lines = trace.split("\n");
            stackTrace = new StackTraceElement[lines.length];
            for (int i = lines.length - 1; i >= 0; i--) {
                int numberStart = lines[i].lastIndexOf(' ');
                int methodStart = lines[i].lastIndexOf('.');
                stackTrace[i] = new StackTraceElement(
                        lines[i].substring(0, methodStart),
                        numberStart != -1 ? lines[i].substring(methodStart + 1, numberStart)
                                : lines[i].substring(methodStart + 1),
                        null,
                        numberStart != -1 ? Integer.valueOf(lines[i].substring(numberStart + 1)) : -2);
            }
        }
        return stackTrace.clone();
    }
}
