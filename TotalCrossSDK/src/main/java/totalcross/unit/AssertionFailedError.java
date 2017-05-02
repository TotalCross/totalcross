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

package totalcross.unit;

/** 
 * Exception dispatched when an assertion fails. 
 */
public class AssertionFailedError extends RuntimeException
{
   /** 
    * Constructs an empty exception. 
    */
   public AssertionFailedError()
   {
      super();
   }

   /** 
    * Constructs an exception with the given message. 
    *
    * @param arg0 The error message.
    */
   public AssertionFailedError(String arg0)
   {
      super(arg0);
   }
}
