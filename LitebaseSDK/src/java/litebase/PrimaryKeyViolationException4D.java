// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

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
