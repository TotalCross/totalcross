/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: ZLibException.java,v 1.9 2011-01-04 13:19:08 guich Exp $

package totalcross.util.zip;

/**
 * This exception may be dispatched by zlib routines.
 */

public class ZLibException extends Exception
{
   // Stores the position where the error was found.
   private String internalError = "";

   /** Default constructor.
    */
   public ZLibException()
   {
      super();
   }

   /** Constructor which accepts an error message.
    *
    * @param msg the error message
    */
   public ZLibException(String msg, String internalError)
   {
      super(msg);
      this.internalError = internalError;
   }

   /**
    * Returns the position where the error was found.
    * @return the position where the error was found.
    */
   public String getInternalError()
   {
      return internalError;
   }

   public String toString()
   {
      String s = super.toString();
      if (internalError != null && internalError.length() > 0)
         s += "; cause: " + internalError;

      return s;
   }
}
