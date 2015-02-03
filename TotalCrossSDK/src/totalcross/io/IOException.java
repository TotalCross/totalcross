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



package totalcross.io;

/** Base class of all input/output exceptions.
 * <p>Some IO operations may return platform-specific codes.
 * <ul>
 * <li> Windows: <a href='http://www.superwaba.org/etc/winerror.h' target=_blank>http://www.superwaba.org/etc/winerror.h</a>
 * <li> Palm: <a href='http://prc-tools.sourceforge.net/errorcodes.html' target=_blank>http://prc-tools.sourceforge.net/errorcodes.html</a>
 * </ul> 
 */

public class IOException extends Exception
{
   /** Constructs an empty Exception. */
   public IOException()
   {
      super();
   }

   /** Constructs an exception with the given message. */
   public IOException(String msg)
   {
      super(msg);
   }
}
