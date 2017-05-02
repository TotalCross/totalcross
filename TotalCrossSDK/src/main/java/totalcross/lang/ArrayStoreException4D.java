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
 * Thrown when the source and target arrays are not compatible when using <code>Vm.arrayCopy</code>.
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
 *
 * @see totalcross.sys.Vm#arrayCopy
 */
public class ArrayStoreException4D extends RuntimeException
{
   /** 
    * Constructs an empty Exception. 
    */
   public ArrayStoreException4D()
   {
   }

   /** 
    * Constructs an exception with the given message.
    * 
    * @param message The error message. 
    */
   public ArrayStoreException4D(String message)
   {
      super(message);
   }
}
