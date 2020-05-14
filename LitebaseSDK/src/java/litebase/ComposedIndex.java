// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package litebase;

/**
 * Represents a composed index.
 */
class ComposedIndex
{
   /**
    * Identifies the composed index.
    */
   int indexId;

   /**
    * The columns index of the composed index.
    */
   byte[] columns;

   /**
    * The index itself.
    */
   Index index;

   /**
    * Creates a composed index.
    *
    * @param id The index id.
    * @param newColumns The columns of this index.
    */
   ComposedIndex(int id, byte[] NewColumns)
   {
      indexId = id;
      columns = NewColumns;
   }
}
