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

/**
 * A class used for repeated values in an index.
 */
class Value
{
   /**
    * The size of a repeated value.
    */
   static final int VALUERECSIZE = 6;

   /**
    * Indicates that there are no more values.
    */
   static final int NO_MORE = 0xFFFFFF;

   /**
    * Index to next value. Initially, there is no value.
    */
   int next = NO_MORE;

   /**
    * Index to the record of the table.
    */
   int record;

   // juliana@224_2: improved memory usage on BlackBerry.
   /**
    * Saves a new repeated value.
    *
    * @param fvalues The .idr file pointer.
    * @param record The new record to be saved.
    * @param next The next record to the one being saved.
    * @param isWriteDelayed Indicates if the index is to be saved now or later in the file.
    * @param valueBuf a buffer to write the value.
    * @return <code>valRec</code>.
    * @throws IOException If an internal method throws it.
    */
   static int saveNew(NormalFile fvalues, int record, int next, boolean isWriteDelayed, byte[] valueBuf) throws IOException
   {
      int idx = fvalues.finalPos / VALUERECSIZE; // Links that value.

      if (isWriteDelayed)
      {
         if ((idx & (Node.NODEGROWSIZE - 1)) == 0) // Grows more than 1 value per time.
            fvalues.growTo((idx + Node.NODEGROWSIZE) * VALUERECSIZE);
      }
      else
         fvalues.growTo((idx + 1) * VALUERECSIZE); // Opens space for the value.
      
      fvalues.finalPos = (idx + 1) * VALUERECSIZE;
      fvalues.setPos(idx * VALUERECSIZE); // Seeks write position.

      save(fvalues, valueBuf, record, next); // Adds the new value.

      return idx;
   }

   // juliana@224_2: improved memory usage on BlackBerry.
   /**
    * Loads a value from the .idr file.
    *
    * @param fvalues The .idr file.
    * @param valueBuf a buffer to read the value.
    * @throws IOException If an internal method throws it.
    */
   void load(NormalFile fvalues, byte[] valueBuf) throws IOException
   {      
      fvalues.readBytes(valueBuf, 0, VALUERECSIZE); // Reads the value.
   
      // Calculates the record and the next repeated value.
      record = (((valueBuf[0] & 0xFF) << 16) | ((valueBuf[1] & 0xFF) << 8) | (valueBuf[2] & 0xFF));
      next = (((valueBuf[3] & 0xFF) << 16) | ((valueBuf[4] & 0xFF) << 8) | (valueBuf[5] & 0xFF));
   }

   // juliana@224_2: improved memory usage on BlackBerry.
   /**
    * Saves a value to the .idr file.
    *
    * @param fvalues The .idr file.
    * @param valueBuf a buffer to write the value.
    * @param record The record of the value.
    * @param next The next same value of the list.
    * @throws IOException If an internal method throws it.
    */
   static void save(NormalFile fvalues, byte[] valueBuf, int record, int next) throws IOException
   {
      // Stores the record and the next repeated value in the buffer.
      valueBuf[0] = (byte)((record >> 16) & 0xFF);
      valueBuf[1] = (byte)((record >> 8) & 0xFF);
      valueBuf[2] = (byte)(record & 0xFF);
      valueBuf[3] = (byte)((next >> 16) & 0xFF);
      valueBuf[4] = (byte)((next >> 8) & 0xFF);
      valueBuf[5] = (byte)(next & 0xFF);

      fvalues.writeBytes(valueBuf, 0, VALUERECSIZE); // Writes the value.
   }
}
