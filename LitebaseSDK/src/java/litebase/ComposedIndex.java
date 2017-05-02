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
