// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang;

/**
 * Used to implement a thread in a class that already extends other class.
 * Example:
 * <pre>
 * public class ScrollingLabel extends Label implements Runnable
 * {
 *    public void run()
 *    {
 *       while (true)
 *       {
 *          // do something
 *       }
 *    }
 *    ... 
 * }
 * </pre>
 * To use it:
 * <pre>
 * ScrollingLabel l;
 * ...
 * new Thread(l).start();
 * </pre>
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

public interface Runnable4D {
  public void run();
}
