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
