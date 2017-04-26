/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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



package totalcross.io;

/** This class is used to handle record sizes of a PDBFile. 
 * It is used in most cases with a DataStream. For example:
   <pre>
      PDBFile pdb = new PDBFile("Name.Crtr.Type",PDBFile.CREATE);
      ResizeRecord rs = new ResizeRecord(pdb,512);
      DataStream ds = new DataStream(rs);
      rs.startRecord();
      ds.writeStringArray(aStringArray);
      rs.endRecord();
   </pre>
   PS: if you dont call startRecord, writeBytes will simply call PDBFile.writeBytes and will not resize the record!
   @since SuperWaba 5.8
*/
public class ResizeRecord extends Stream // guich@580_19
{
   /** the associated PDBFile */
   private PDBFile cat;
   /** the intial size of the record */
   private int initialSize;
   /** how many bytes were written */
   private int written;
   /** current record size */
   private int size;

   boolean dontFinalize;
   
   private boolean isRecordClosed = true; // flsobral@tc113_30: flag to avoid the execution of endRecord twice for the same record.
   private boolean isClosing; // flsobral@tc113_30

   /** Constructs the resize stream.
   @param cat The PDBFile associated
   @param initialSize The initial size of the record. CANNOT BE 0!
   */
   public ResizeRecord(PDBFile cat, int initialSize)
   {
      this.cat = cat;
      this.initialSize = initialSize;
   }

   /**
    * Inserts the record at the specified index in the PDBFile.
    * 
    * @param pos
    *           The position where to insert the record. If greater or equal to the record count, the record is
    *           appended.
    * @throws IOException
    * @since SuperWaba 1.21. *
    * @see #startRecord()
    * @see #restartRecord(int)
    * @see #endRecord()
    */
   public void startRecord(int pos) throws IOException // flsobral@tc100b5_49: returning boolean is no longer necessary.
   {
      if (pos >= cat.getRecordCount()) // append?
         cat.addRecord(initialSize);
      else
         cat.addRecord(initialSize, pos);
      written = 0;
      size = initialSize;
      isRecordClosed = false;
   }

   /**
    * Appends a new record to the PDBFile.
    * 
    * @throws IOException
    * @see #restartRecord(int)
    * @see #startRecord(int)
    * @see #endRecord()
    */
   public void startRecord() throws IOException
   {
      cat.addRecord(initialSize); // guich@200b4_96
      written = 0;
      size = initialSize;
      isRecordClosed = false;
   }

   /** Restart writing to the given record pos, overwritting the current record. 
    * @param pos The position to overwrite. If pos is lower than 0 or greater than the
     * number of records of the PDBFile, the record is appended.
     * @see #startRecord()
     * @see #startRecord(int)
     * @see #endRecord()
     */
   public boolean restartRecord(int pos) throws IOException
   {
      if (pos < 0 || pos >= cat.getRecordCount()) // append?  guich@310_4: check if < 0
         startRecord();
      else
      {
         written = 0;
         size = initialSize;
         cat.setRecordPos(pos); // guich@241_14
         size = cat.getRecordSize(); // guich@300_35: initialize to the current record size
      }
      return true;
   }

   /** Must be called after the record is finished so it can be properly resized.
     * @see #startRecord()
     * @see #restartRecord(int)
     * @see #startRecord(int)
     */
   public void endRecord() throws IOException
   {
      if (!isRecordClosed)
      {
         isRecordClosed = true;
         if (written > 0) // guich@400_22
         {
            if (written != size) // guich@550_12: only call resizeRecord if there were a change
               cat.resizeRecord(written);
         }
         else
            cat.deleteRecord();
         size = 0;
      }
   }

   public int readBytes(byte buf[], int start, int count) throws totalcross.io.IOException
   {
      return cat.readBytes(buf, start, count);
   }

   public int writeBytes(byte buf[], int start, int count) throws totalcross.io.IOException
   {
      if (size == 0)
         return cat.writeBytes(buf, start, count);
      if (count - start <= 0)
         return 0;

      int total = written + (count - start);

      while (total > size) // no more space?
      {
         if (size == 65520)
            return -1; // guich@550_14: avoid infinite recursion
         size += initialSize;

         if (size > 65520)
            size = 65520; // guich@500_7: avoid blowing up the buffer
         try
         {
            cat.resizeRecord(size); // expand
         }
         catch (IOException e)
         {
            return -1;
         }
      }
      int n = cat.writeBytes(buf, start, count);
      if (n >= 0)
         written += n;
      return n;
   }

   /**
    * closes the PDBFile
    *
    * @throws totalcross.io.IOException
    */
   public void close() throws totalcross.io.IOException
   {
      if (isClosing) // flsobral@tc113_30
         return;
      isClosing = true;
      //cat.close();    guich@tc122_45
      dontFinalize = true;
   }

   /** Returns the stream attached to this stream (which is always a PDBFile). */
   public Stream getStream()
   {
      return cat;
   }

   protected void finalize()
   {
      try
      {
         if (cat.mode != -1)
            endRecord();
         close();
      }
      catch (IOException ex)
      {
      }
   }
}