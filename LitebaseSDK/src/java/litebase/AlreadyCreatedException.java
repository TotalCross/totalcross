/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package litebase;

// juliana@220_1: removed unused exception constructors and changed constructors visibility to package because it is not to be used by the user.
/**
 * This exception may be thrown by <code>LitebaseConnection.execute</code>, when a table or index has already been
 * created. It is an unchecked Exception (can be thrown any time).
 */
public class AlreadyCreatedException extends RuntimeException
{
   /**
    * Constructs a new <code>AlreadyCreatedException</code> exception with the specified detail message.
    *
    * @param message the detail message.
    */
   AlreadyCreatedException(String message)
   {
      super(message);
   }
}
