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

/**
 * Internal use only. Represents a table of various statements, except for inserts.
 */
class SQLResultSetTable
{
   /**
    * The object table, filled by <code>bindSelectStatement()</code>.
    */
   Table table;

   /**
    * The name of the table, filled during the parsing process.
    */
   String tableName;

   /**
    * The Table alias.
    */
   String aliasTableName;

   /**
    * The alias table name hash code.
    */
   int aliasTableNameHashCode;

   /**
    * Constructs a new <code>SQLResultSetTable</code> object using a table name and its optional alias name.
    *
    * @param aTableName the name of the new table.
    * @param anAliasTableName the optional alias name of the new table (it can be <code>null</code>).
    */
   SQLResultSetTable(String aTableName, String anAliasTableName)
   {
      tableName = aTableName; // Sets the table name.

      // If the alias is null, it receives the name of the table. If the alias is not null, the alias name is set with the alias passed as a 
      // parameter.
      aliasTableNameHashCode = (aliasTableName = (anAliasTableName == null) ? aTableName : anAliasTableName).hashCode();
   }

   /**
    * Constructs a new <code>SQLResultSetTable</code> object using just a table name.
    * @param aTableName
    */
   public SQLResultSetTable(String aTableName)
   {
      tableName = aTableName;
   }

}
