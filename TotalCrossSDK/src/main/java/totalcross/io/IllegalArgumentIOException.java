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

/** Illegal argument passed to an IO method. */

public class IllegalArgumentIOException extends IOException
{
   /** Constructs an empty Exception. */
   public IllegalArgumentIOException()
   {
      super();
   }

   /** Constructs an exception with the given message. */
   public IllegalArgumentIOException(String msg)
   {
      super(msg);
   }

   /** Constructs an exception with the given message and argument value. */
   public IllegalArgumentIOException(String argumentName, String argumentValue)
   {
      super("Invalid value for argument '" + argumentName + "': " + argumentValue);
   }
}
