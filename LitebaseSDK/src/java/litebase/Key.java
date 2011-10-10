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

import totalcross.io.*;
import totalcross.util.InvalidDateException;

/**
 * This class represents the key of a record. It may be any of the SQL types defined here.
 */
class Key
{
   /**
    * The size of valRec, an int.
    */
   static final int VALREC_SIZE = 4;

   /**
    * Represents a key that has no values attached to it.
    */
   static final int NO_VALUE = 0xFFFFFFF; // juliana@230_21

   /**
    * The values stored in the key.
    */
   SQLValue[] keys;

   /**
    * The record index or NO_VALUE.
    */
   int record;

   /**
    * The index that has this key.
    */
   Index index;

   /**
    * Creates a <code>Key</code> object.
    *
    * @param anIndex The index which has the new key.
    */
   Key(Index anIndex)
   {
      // Initializes the key object.
      index = anIndex;
      keys = SQLValue.newSQLValues(index.types.length);
      record = NO_VALUE;
   }

   /**
    * Sets a key of an index.
    *
    * @param key The values used to set the index key.
    */
   void set(SQLValue[] key)
   {
      int i = index.types.length;
      byte[] types = index.types;
      SQLValue[] keysAux = keys;
      
      while (--i >= 0)
         switch (types[i])
         {
            case SQLElement.DATETIME: // DATETIME.
               keysAux[i].asShort = key[i].asShort;
            case SQLElement.CHARS: // CHARS and VARCHAR.
            case SQLElement.CHARS_NOCASE: // CHARS NOCASE and VARCHAR NOCASE.
            case SQLElement.DATE: // DATE.
               keysAux[i].asString = key[i].asString; // juliana@230_3
            case SQLElement.INT: // INT.   
               keysAux[i].asInt = key[i].asInt;
               break;
            case SQLElement.SHORT: // SHORT.
               keysAux[i].asShort = key[i].asShort;
               break;
            case SQLElement.LONG: // LONG.
               keysAux[i].asLong = key[i].asLong;
               break;
            case SQLElement.FLOAT: // FLOAT.
            case SQLElement.DOUBLE: // DOUBLE.
               keysAux[i].asDouble = key[i].asDouble;                 
            // Blobs can't be used in indices.
         }
      record = NO_VALUE; // The record key is not stored yet.
   }

   /**
    * Loads a key.
    *
    * @param ds The data stream where the record to be read to find the key value stored.
    * @throws IOException If an internal method throws it.
    */
   void load(DataStreamLE ds) throws IOException, InvalidDateException
   {
      int n = index.types.length,
          i = -1;
      int[] colSizes = index.colSizes;
      byte[] types = index.types;
      PlainDB db = index.table.db;
      SQLValue key;
      
      while (++i < n)
      {
         key = keys[i];

         // String keys are not stored in the indices. Only their pointer is stored.
         if (colSizes[i] > 0)
         {
            // If the position is the same, the string is already loaded.
            int pos = ds.readInt();
            if (pos != key.asInt)
            {
               key.asString = null;
               key.asInt = pos;
            }
         }
         else
            // Must pass true to isTemporary so that the method does not think that the number is a rowid.
            // If the value read is null, some bytes must be skipped in the stream.
            // Note: since we're writing only primitive types, we can use any PlainDB available.
            // juliana@220_3 // juliana@230_14
            ds.skipBytes(colSizes[i] - db.readValue(key, 0, types[i], ds, true, false, false)); 
      }
      record = ds.readInt(); // Reads the number that represents the record.
   }

   /**
    * Saves a key.
    *
    * @param ds The data stream where to write the record.
    * @throws IOException If an internal method throws it.
    */
   void save(DataStreamLE ds) throws IOException
   {
      int n = index.types.length,
          i = -1;
      byte[] types = index.types;
      int[] colSizes = index.colSizes;
      
      while (++i < n)
      {
         if (colSizes[i] > 0)
            ds.writeInt(keys[i].asInt); // Saves only the string position in the .dbo.
         else 
            // If the key is not a string, stores its value in the index file.
            // Note: since primitive types are being written, it is possible to use any PlainDB available.
            // juliana@220_3
            index.table.db.writeValue(types[i], keys[i], ds, true, true, 0, 0, false); 
      }
      ds.writeInt(record); // Writes the number that represents the record.
   }

   /**
    * Adds a value in the repeated key structure.
    *
    * @param newRecord The value record to be inserted in the key.
    * @param isWriteDelayed Indicates that this key will be dirty after calling this method and must be saved.
    * @throws IOException If an internal method throws it.
    */
   void addValue(int newRecord, boolean isWriteDelayed) throws IOException
   {
      if (record == NO_VALUE) // First value being stored? Store it in the valRec as the negative.
         record = newRecord; 
   }

   /**
    * Climbs on the key.
    *
    * @param monkey Used to climb on the values of the key.
    * @throws IOException If an internal method throws it.
    */
   void climb(Monkey monkey) throws IOException
   {
      if (record == NO_VALUE) // If there are no values, there is nothing to be done.
         return;

      // juliana@224_2: improved memory usage on BlackBerry.
      monkey.onValue(record);
   }

   /**
    * Removes a value of the repeated key structure.
    *
    * @param oldRecord The value record to be removed.
    * @throws IOException If an internal method throws it.
    */
   void remove(int oldRecord) throws IOException
   {
      // juliana@224_2: improved memory usage on BlackBerry.
      if (record != NO_VALUE && record == oldRecord) // If this is the record, all that is done is to set the key as empty.
         record = NO_VALUE;        
      else
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_IDX_RECORD_DEL));
   }
   
}
