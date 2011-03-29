/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: PrimaryKeyViolationException4D.java,v 1.1.2.2 2011-01-03 20:05:13 juliana Exp $

package litebase;

// juliana@220_1: removed unused exception constructors and changed constructors visibility to package because it is not to be used by the user.
/**
 * This exception may be thrown by <code>LitebaseConnection.executeUpdate</code>, when an update in a table can not be completed because it violates 
 * the primary key rule. It can also be thrown when adding a primary key to a table if there is a duplicated or null in a primary key. It is an 
 * unchecked Exception (can be thrown any time).
 */
public class PrimaryKeyViolationException4D extends RuntimeException
{
}
