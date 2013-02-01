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
 * This exception may be dispatched by routines that handle the parsing of an SQL command. It is an unchecked Exception (it can be thrown any time).
 */
public class SQLParseException4D extends RuntimeException
{
   // juliana@268_1: added field cause to SQLParseException for Windows 32, Windows CE, Palm, Android, and iOS.
   /** 
    * The exception that caused this exception to be dispatched, or null if the cause of this exception was not another exception.
    */
   public Exception cause;
}
