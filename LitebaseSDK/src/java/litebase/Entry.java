// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

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