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
 * Hash table for Litebase SQL reserved words collision list. 
 */
class Entry
{
   /**
    * The hash code of the reserved word string.
    */
   int hash;
   
   /**
    * The token code of the SQL reserved word.
    */
   int value;
   
   /**
    * The reserved word string.
    */
   String key;
   
   /**
    * The next entry of the collision list.
    */
   Entry next;
}