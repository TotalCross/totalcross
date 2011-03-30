/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

// $Id: RecordPipe.java,v 1.9 2011-01-04 13:19:30 guich Exp $

package tc.samples.ui.image.photos;

import totalcross.io.*;

/** Used to make a PDBFile act as a single Stream, reading all records in sequence.
  * Note that it does not support writings.
  */

public class RecordPipe extends Stream
{
   protected int endRec;
   protected PDBFile cat;
   protected int recordOffset;
   protected int recSize;

   public RecordPipe(PDBFile cat, int startRec, int endRec, int recordOffset) throws totalcross.io.IOException
   {
      this.cat = cat;
      this.endRec = endRec;
      this.recordOffset = recordOffset;
      // position in first record
      cat.setRecordPos(startRec);
      moveTo(startRec);
   }

   private boolean moveTo(int recno) throws totalcross.io.IOException
   {
      cat.setRecordPos(recno);
      try
      {
         cat.skipBytes(recordOffset);
         recSize = cat.getRecordSize();
         return true;
      }
      catch (IllegalArgumentIOException e)
      {
         recSize = 0;
         return false;
      }
   }

   public void close()
   {
   }

   public int writeBytes(byte[] buf, int start, int count)
   {
      return -1; // not implemented
   }

   public int readBytes(byte[] buf, int start, int count) throws totalcross.io.IOException
   {
      int total = 0;
      // read some more data from the current record
      int maxRead = Math.min(count,recSize-cat.getRecordOffset());
      int read = cat.readBytes(buf,start,maxRead);
      if (read == count) return count; // quick check: probably most common case

      if (read > 0)
      {
         total = read;
         start += read;
      }
      // could not read enough? move to next record and keep reading until we fill the buffer
      int p = cat.getRecordPos();
      while (total < count)
      {
         if (p < endRec && moveTo(++p)) // still has records?
         {
            maxRead = Math.min(count-total,recSize-cat.getRecordOffset());
            read = cat.readBytes(buf,start,maxRead);
            if (read > 0)
            {
               total += read;
               start += read;
            }
         }
         else
         {
            if (total == 0) total = -1;
            break;
         }
      }
      return total;
   }
}