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

public class Throwable4D
{
    /** Stores the message given when constructing this Throwable object */
    protected String msg;

    public String trace;
    
    /**
     * The cause of the throwable, including null for an unknown or non-chained
     * cause. This may only be set once; so the field is set to
     * <code>this</code> until initialized.
     *
     * @serial the cause, or null if unknown, or this if not yet set
     * @since 1.4
     */
    private Throwable4D cause = this;

    /** Constructs an empty Exception. */
    public Throwable4D()
    {
    }

    /** Constructs an exception with the given message. */
    public Throwable4D(String msg)
    {
	    this.msg = msg;
    }
    
    /**
     * Instantiate this Throwable with the given message and cause. Note that
     * the message is unrelated to the message of the cause.
     *
     * @param message the message to associate with the Throwable
     * @param cause the cause, may be null
     * @since 1.4
     */
    public Throwable4D(String message, Throwable4D cause)
    {
      this(message);
      this.cause = cause;
    }

    /**
     * Instantiate this Throwable with the given cause. The message is then
     * built as <code>cause == null ? null : cause.toString()</code>.
     *
     * @param cause the cause, may be null
     * @since 1.4
     */
    public Throwable4D(Throwable4D cause)
    {
      this(cause == null ? null : cause.toString(), cause);
    }

    /** Returns the message passed on the constructor. May be null. */
    public String getMessage()
    {
	    return msg;
    }

    public String toString()
    {
       if (msg == null)
          return super.toString();
       return super.toString() + " - " + msg;
    }

    /**
     * Prints the stack trace to the debug console.
     */
    public void printStackTrace() // guich@300_23
    {
       totalcross.sys.Vm.warning(toString());
       printStackTraceNative();
    }

    native private void printStackTraceNative();

    public void printStackTrace(Object o) {} // guich@582_6: just a place-holder to let it build-device
}
