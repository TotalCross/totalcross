// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang;

/** 
 * All arrays extend this class.
 * The purpose is to allow <code>hashCode()</code> and <code>toString()</code> methods on arrays. 
 *
 * <br><br>
 * IMPORTANT: the <code>totalcross.lang</code> package is the <code>java.lang</code> that will be used on the device.<br>
 * You CANNOT use nor import <code>totalcross.lang</code> package on the desktop. When <code>tc.Deploy</code> is called, all references to 
 * <code>java.lang</code> are replaced by <code>totalcross.lang</code> automatically. Given this, you must use only the classes and methods that 
 * exists BOTH in <code>java.lang</code> and <code>totalcross.lang</code>. For example, you can't use <code>java.lang.ClassLoader</code> because 
 * there is no <code>totalcross.lang.ClassLoader</code>. Another example, you can't use <code>java.lang.String.indexOfIgnoreCase()</code> because
 * there is no <code>totalcross.lang.String.indexOfIgnoreCase()</code> method. Trying to use a class or method from the <code>java.lang</code> 
 * package that has no correspondence with <code>totalcross.lang</code> will make the <code>tc.Deploy</code> program to abort, informing where the 
 * problem has occurred. A good idea is to always refer to this JavaDoc to know what is and what isn't available.
 */
public class Array4D implements Cloneable {
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null; // Never occurs.
    }
  }
}
