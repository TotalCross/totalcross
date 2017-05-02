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

// juliana@220_2: added TableNotClosedException which will be raised whenever a table is not closed properly.
/**
 * This exception may be thrown if a table was not closed properly.
 *
 * @see LitebaseConnection#recoverTable(String)
 */
public class TableNotClosedException extends RuntimeException
{
   /**
    * Constructs a new <code>TableNotClosedException</code> exception with the specified detail message.
    *
    * @param tableName The name of the table not closed properly.
    */
   TableNotClosedException(String tableName)
   {
      super(LitebaseMessage.getMessage(LitebaseMessage.ERR_TABLE_NOT_CLOSED) + tableName + '.');
   }
   
}
