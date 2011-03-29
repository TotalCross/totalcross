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

// $Id: Monkey.java,v 1.1.2.13 2011-01-03 20:05:13 juliana Exp $

package litebase;

import totalcross.io.IOException;

/**
 * Used to traverse a B-tree <i>in order</i>.
 */
abstract class Monkey
{
   /**
    * Climbs on a key.
    *
    * @param key The key to be climbed on.
    * @return <code>true</code>.
    * @throws IOException If an internal method throws it.
    */
   boolean onKey(Key key) throws IOException
   {
      key.climb(this);
      return true;
   }

   /**
    * Climbs on a value.
    *
    * @param value The value to be climbed on.
    */
   abstract void onValue(Value value);

}
