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

import totalcross.io.IOException;
import totalcross.util.IntVector;

/**
 * Generates the result set indexed rows map from the associated table indexes applied to the associated WHERE clause. This class should only be used
 * if the result set has a WHERE clause.
 */
class MarkBits
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

   // juliana@253_5: removed .idr files from all indices and changed its format. 
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

      // juliana@230_20: solved a possible crash when using aggregation functions with strings.
      PlainDB db = k.index.table.db;
      SQLValue key = k.keys[0];
      int type = leftKey.index.types[0];
      
      if (key.asString == null && (type == SQLElement.CHARS || type == SQLElement.CHARS_NOCASE)) // A string may not be loaded.
      {
         db.dbo.setPos(key.asInt); // Gets and sets the string position in the .dbo.
         key.asString = db.loadString();
      }
      
      if (rightKey != null)
      {
         int comp = Utils.arrayValueCompareTo(k.keys, rightKey.keys, k.index.types, null); // Compares the key with the right key.

         // If key <= right key, stops.
         if (r0 == SQLElement.OP_REL_LESS_EQUAL && comp > 0)
            return false;

         // if key < right key, stops.
         if (r0 == SQLElement.OP_REL_LESS && comp >= 0)
            return false;
      }

      // For inclusion operations, just uses the value.
      if (l0 == SQLElement.OP_REL_EQUAL || l0 == SQLElement.OP_REL_GREATER_EQUAL || (l0 == SQLElement.OP_REL_GREATER && isNoLongerEqual))
         onValue(k.record); // Climbs on the value.
      else if (l0 == SQLElement.OP_REL_GREATER) // The key can still be equal.
      {
         if (Utils.arrayValueCompareTo(leftKey.keys, k.keys, leftKey.index.types, null) != 0) // Compares the key with the left key.
         {
            isNoLongerEqual = true;
            onValue(k.record); // Climbs on the value.
         }
      }
      else // OP_PAT_MATCH_LIKE
      {
         String val = key.asString;
         if (type == SQLElement.CHARS_NOCASE)
            val = val.toLowerCase();
         
         // juliana@230_3: corrected a bug of LIKE using DATE and DATETIME not returning the correct result.
         else
            val = Utils.formatDateDateTime(db.driver.sBuffer, type, key);
         
         if (val.startsWith(leftKey.keys[0].asString)) // Only starts with are used with indices.
            onValue(k.record); // Climbs on the value.
         else
            return false; // Stops the search.
      
      }
      return true; // Does not visit this value, but continues the search.
   }

   /**
    * Climbs on a value.
    *
    * @param record The value record to be climbed on.
    */
   void onValue(int record)
   {
      if (record != Key.NO_VALUE)
         indexBitmap.setBit(record, bitValue); // (Un)sets the corresponding bit on the bit array.
   }
}
