// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package litebase;

// juliana@220_2: added TableNotClosedException which will be raised whenever a table is not closed properly.
/**
 * This exception may be thrown if a table was not closed properly.
 *
 * @see LitebaseConnection#recoverTable(String)
 */
public class TableNotClosedException4D extends RuntimeException
{
}
