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



package litebase;

import totalcross.io.IOException;
import totalcross.util.IntVector;

/**
 * Generates the result set indexed rows map from the associated table indexes applied to the associated WHERE clause. This class should only be used
 * if the result set has a WHERE clause.
 */
class MarkBits extends Monkey
{
   /**
    * The index bitmap of the where clause.
    */
   IntVector indexBitmap;

   /**
    * The value of a bit of the bitmap.
    */
   boolean bitValue;

   /**
    * Indicates if a value is equal or not.
    */
   private boolean isNoLongerEqual;

   /**
    * The left operator.
    */
   byte[] leftOp;

   /**
    * The right operator.
    */
   byte[] rightOp;

   /**
    * The left key.
    */
   Key leftKey;

   /**
    * The right key.
    */
   Key rightKey;

   /**
    * Resets the object and the bitmap.
    *
    * @param idx The index whose bitmap is being reseted.
    * @param bits The number of the bitmap.
    */
   void reset(Index idx, int bits)
   {
      int size = idx.types.length;

      leftOp = new byte[size];
      rightOp = new byte[size];
      leftKey = new Key(idx);
      isNoLongerEqual = false;
      bitValue = true;
      rightKey = null;
      indexBitmap = new IntVector(1);
      indexBitmap.ensureBit(bits);
   }

   /**
    * Climbs on a key.
    *
    * @param key The key to be climbed on.
    * @return <code>false</code> if the key could not be climbed; <code>true</code>, otherwise.
    * @throws IOException If an internal method throws it.
    */
   boolean onKey(Key k) throws IOException
   {
      int r0 = rightOp[0];
      int l0 = leftOp[0];

      if (rightKey != null)
      {
         int comp = Utils.arrayValueCompareTo(k.keys, rightKey.keys, k.index.types); // Compares the key with the right key.

         // If key <= right key, stops.
         if (r0 == SQLElement.OP_REL_LESS_EQUAL && comp > 0)
            return false;

         // if key < right key, stops.
         if (r0 == SQLElement.OP_REL_LESS && comp >= 0)
            return false;
      }

      // For inclusion operations, just uses the value.
      if (l0 == SQLElement.OP_REL_EQUAL || l0 == SQLElement.OP_REL_GREATER_EQUAL || (l0 == SQLElement.OP_REL_GREATER && isNoLongerEqual))
         return super.onKey(k); // Climbs on the values.

    
      if (l0 == SQLElement.OP_REL_GREATER) // The key can still be equal.
      {
         if (Utils.arrayValueCompareTo(leftKey.keys, k.keys, leftKey.index.types) != 0) // Compares the key with the left key.
         {
            isNoLongerEqual = true;
            return super.onKey(k); // climb on the values
         }
      }
      else // OP_PAT_MATCH_LIKE
      {
         PlainDB db = k.index.table.db;
         
         if (k.keys[0].asString == null) // A strinhg may not be loaded.
         {
            db.dbo.setPos(k.keys[0].asInt); // Gets and sets the string position in the .dbo.
            int length = db.dsdbo.readUnsignedShort();
            
            if (db.isAscii) // juliana@210_2: now Litebase supports tables with ascii strings.
            {
               byte[] buf = db.buffer;
               if (buf.length < length)
                  db.buffer = buf = new byte[length];
               db.dsdbo.readBytes(buf, 0, length);
               k.keys[0].asString = new String(buf, 0, length); // Reads the string.
            }
            else
            {
               char[] chars = db.valueAsChars;
               if (chars.length < length)
                  db.valueAsChars = chars = new char[length];
               db.dsdbo.readChars(chars, length);            
               k.keys[0].asString = new String(chars, 0, length); // Reads the string.
            }
         }
         
         String val = k.keys[0].asString;
         if (leftKey.index.types[0] == SQLElement.CHARS_NOCASE)
            val = val.toLowerCase();
         if (val.startsWith(leftKey.keys[0].asString)) // Only starts with are used with indices.
            return super.onKey(k); // Climbs on the values.
         return false; // Stops the search.
      
      }
      return true; // Does not visit this value, but continues the search.
   }

   /**
    * Climbs on a value.
    *
    * @param value The value to be climbed on.
    */
   void onValue(Value v)
   {
      indexBitmap.setBit(v.record, bitValue); // (Un)sets the corresponding bit on the bit array.
   }
}
