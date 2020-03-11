/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
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
