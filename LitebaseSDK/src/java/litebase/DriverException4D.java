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

package litebase;

// juliana@220_1: removed unused exception constructors and changed constructors visibility to package because it is not to be used by the user.
// juliana@223_10: corrected a bug of not finding DriverException
/**
 * This exception may be dispatched if any problem, other than a SQL parsing error, occurs. It is an unchecked Exception
 * (it can be thrown any time).
 */
public class DriverException4D extends RuntimeException
{
   /** 
    * The exception that caused this exception to be dispatched, or null if the cause of this exception was not another exception.
    */
   public Exception cause;
   
   /**
    * Constructs a new <code>DriverException</code> exception with the specified detail message.
    *
    * @param message the detail message.
    */
   DriverException4D(String message)
   {
      super(message);
   }
}
