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
   private static final int NO_VALUE = 0xFFFFFFF;

   /**
    * The key must be saved before removed.
    */
   static final int REMOVE_SAVE_KEY = 1;

   /**
    * The key was already saved.
    */
   static final int REMOVE_VALUE_ALREADY_SAVED = 2;

   /**
    * The values stored in the key.
    */
   SQLValue[] keys;

   /**
    * - (record + 1) or NO_VALUE.
    */
   int valRec;

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
      valRec = NO_VALUE;
   }

   /**
    * Sets a key of an index.
    *
    * @param key The values used to set the index key.
    */
   void set(SQLValue[] key)
   {
      int i = index.types.length;
      int[] types = index.types;
      while (--i >= 0)
         switch (types[i])
         {
            case SQLElement.CHARS: // CHARS and VARCHAR.
            case SQLElement.CHARS_NOCASE: // CHARS NOCASE and VARCHAR NOCASE.
               keys[i].asString = key[i].asString;
               keys[i].asInt = key[i].asInt;
               break;
            case SQLElement.SHORT: // SHORT.
               keys[i].asShort = key[i].asShort;
               break;
            case SQLElement.DATE: // DATE.
            case SQLElement.INT: // INT.
               keys[i].asInt = key[i].asInt;
               keys[i].asString = key[i].asString; // juliana@230_3
               break;
            case SQLElement.LONG: // LONG.
               keys[i].asLong = key[i].asLong;
               break;
            case SQLElement.FLOAT: // FLOAT.
            case SQLElement.DOUBLE: // DOUBLE.
               keys[i].asDouble = key[i].asDouble;
               break;
            case SQLElement.DATETIME: // DATETIME.
               keys[i].asInt = key[i].asInt;
               keys[i].asShort = key[i].asShort;
               keys[i].asString = key[i].asString; // juliana@230_3
            
            // Blobs can't be used in indices.
         }
      valRec = NO_VALUE; // The record key is not stored yet.
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
      int[] types = index.types;
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
      valRec = ds.readInt(); // Reads the number that represents the record.
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
      int[] types = index.types;
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
      ds.writeInt(valRec); // Writes the number that represents the record.
   }

   /**
    * Adds a value in the repeated key structure.
    *
    * @param value The value to be inserted in the key.
    * @param isWriteDelayed Indicates that this key will be dirty after calling this method and must be saved.
    * @throws IOException If an internal method throws it.
    */
   void addValue(Value value, boolean isWriteDelayed) throws IOException
   {
      Index indexAux = index;
      
      if (valRec == NO_VALUE) // First value being stored? Store it in the valRec as the negative.
         valRec = -(value.record + 1); // 0 is a valid record number, and also a valid value; so it is necessary to make a difference.
      else // juliana@224_2: improved memory usage on BlackBerry.
      {
         if (index.fvalues == null)
         {
            String path = indexAux.fnodes.f.getPath();
            indexAux.fvalues = new NormalFile(path.substring(0, path.length() - 1) + "r", true, NormalFile.CACHE_INITIAL_SIZE);
            indexAux.fvalues.valAux = indexAux.table.tempVal2;
            indexAux.table.tableSaveMetaData(Utils.TSMD_EVERYTHING);
         }   
         
         byte[] valueBuf = indexAux.table.valueBuf;
         if (valRec < 0) // Is this the first repetition of the key? If so, it is necessary to move the value stored here to the values file.
            valRec = value.saveNew(indexAux.fvalues, -(valRec + 1), Value.NO_MORE, isWriteDelayed, valueBuf);
         value.next = valRec; // Links to the next value.
         valRec = value.saveNew(indexAux.fvalues, value.record, value.next, isWriteDelayed, valueBuf); // Stores the value record.
      }
   }

   /**
    * Climbs on the key.
    *
    * @param monkey Used to climb on the values of the key.
    * @throws IOException If an internal method throws it.
    */
   void climb(Monkey monkey) throws IOException
   {
      Index indexAux = index;
      Value tempVal = indexAux.table.tempVal1; // juliana@224_2: improved memory usage on BlackBerry.
      int idx = valRec;

      if (idx == NO_VALUE) // If there are no values, there is nothing to be done.
         return;

      // juliana@224_2: improved memory usage on BlackBerry.
      if (idx < 0) // Is it a value with no repetitions?
      {
         tempVal.record = -(idx + 1);
         tempVal.next = Value.NO_MORE;
         monkey.onValue(tempVal);
      }
      else // If there are repetitions, climbs on all the values.
      {
         NormalFile fvalues = indexAux.fvalues;
         while (idx != Value.NO_MORE) // juliana@224_2: improved memory usage on BlackBerry.
         {
            fvalues.setPos(Value.VALUERECSIZE * idx);
            tempVal.load(fvalues, indexAux.table.valueBuf);
            monkey.onValue(tempVal);
            idx = tempVal.next;
         }
      }
   }

   /**
    * Removes a value of the repeated key structure.
    *
    * @param value The value to be removed.
    * @return <code>REMOVE_SAVE_KEY</code> or <code>REMOVE_VALUE_ALREADY_SAVED</code>.
    * @throws IOException If an internal method throws it.
    */
   int remove(Value value) throws IOException
   {
      // juliana@224_2: improved memory usage on BlackBerry.
      Index indexAux = index;
      Value tempVal1 = indexAux.table.tempVal1,
            tempVal2 = indexAux.table.tempVal2; 
      
      int idx = valRec;
      
      if (idx != NO_VALUE)
      {
         if (idx < 0) // Is it a value with no repetitions?
         {           
            if (value.record == -(idx + 1)) // If this is the record, all that is done is to set the key as empty.
            {
               valRec = NO_VALUE;
               return REMOVE_SAVE_KEY;
            }
         }
         else  // Otherwise, it is necessary to find the record.
         {
            Value last = null;
            int lastPos = 0;
            NormalFile fvalues = indexAux.fvalues;
            byte[] valueBuf = indexAux.table.valueBuf;
            
            while (idx != Value.NO_MORE) // juliana@224_2: improved memory usage on BlackBerry.
            {
               int pos = Value.VALUERECSIZE * idx;
               fvalues.setPos(pos);
               tempVal1.load(fvalues, valueBuf);
               
               if (tempVal1.record == value.record)
               {
                  if (last == null) // The value removed is the last one.
                  {
                     valRec = tempVal1.next;
                     return REMOVE_SAVE_KEY;
                  }
                  else // The value removed is not the last one.
                  {
                     last.next = tempVal1.next;
                     fvalues.setPos(lastPos);
                     last.save(fvalues, valueBuf);
                     return REMOVE_VALUE_ALREADY_SAVED;
                  }
               }
               idx = tempVal1.next;
               if (last == null) // Sets a new last value if the current one is null.
                  last = tempVal2;
               last.record = tempVal2.record;
               last.next = tempVal2.next;
               lastPos = pos;
            }
            
         }
      }
      throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_IDX_RECORD_DEL));
   }
   
}
