/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools.deployer;

import totalcross.io.PDBFile;

public class PDBStream
{
   int pos, size;
   PDBFile pdb;
   private int PALMOS_RECORD_SIZE = 4096; /* 4 kb */
   private byte b_dest1[], b_dest2[];

   // all methods just call the File ones
   public PDBStream(String name, boolean create) throws totalcross.io.IOException
   {
      pdb = new PDBFile(name, create ? PDBFile.CREATE_EMPTY : PDBFile.READ_WRITE);
      int aux = PALMOS_RECORD_SIZE;
      b_dest1 = new byte[PALMOS_RECORD_SIZE + 8];
      b_dest2 = new byte[PALMOS_RECORD_SIZE + 8];

      // the first four bytes of the record is always the same
      b_dest1[0] = b_dest2[0] = (byte) 'D';
      b_dest1[1] = b_dest2[1] = (byte) 'B';
      b_dest1[2] = b_dest2[2] = (byte) 'L';
      b_dest1[3] = b_dest2[3] = (byte) 'K';

      b_dest1[7] = (byte) (aux & 0xFF);
      aux >>= 8;
      b_dest1[6] = (byte) (aux & 0xFF);
      aux >>= 8;
      b_dest1[5] = (byte) (aux & 0xFF);
      aux >>= 8;
      b_dest1[4] = (byte) (aux & 0xFF);

      pdb.setAttributes(PDBFile.DB_ATTR_BACKUP | PDBFile.DB_ATTR_STREAM);
//      pdb.closeOnFinalize = false;
      setSize();
   }

   public void shrinkTo(int size)
   {
      // not supported
   }

   public int readBytes(byte[] buf, int start, int count) throws totalcross.io.IOException
   {
      pos += count;
      return readWriteBytes(buf, start, count, true);
   }

   public int writeBytes(byte[] buf, int start, int count) throws totalcross.io.IOException
   {
      pos += count;
      return readWriteBytes(buf, start, count, false);
   }

   private int readWriteBytes(byte[] buf, int start, int count, boolean isRead)
         throws totalcross.io.IOException
   {
      int posRecord, posOffSet, numberOfBytesToReadOrWrite, freeSpaceOnRecord, numberOfReadOrWroteBytes = 0, i;
      posRecord = pdb.getRecordPos();
      posOffSet = pdb.getRecordOffset();
      freeSpaceOnRecord = PALMOS_RECORD_SIZE - posOffSet + 8;

      numberOfBytesToReadOrWrite = (freeSpaceOnRecord <= count) ? freeSpaceOnRecord : count;
      if (isRead)
         i = numberOfReadOrWroteBytes = pdb.readBytes(buf, start, numberOfBytesToReadOrWrite);
      else
         i = numberOfReadOrWroteBytes = pdb.writeBytes(buf, start, numberOfBytesToReadOrWrite);
      count -= numberOfReadOrWroteBytes;
      start += numberOfReadOrWroteBytes;
      freeSpaceOnRecord = PALMOS_RECORD_SIZE;

      while (i >= 0 && count > 0) // numberOfWroteBytes < count)
      {
         pdb.setRecordPos(++posRecord);
         pdb.setRecordOffset(8);
         numberOfBytesToReadOrWrite = (freeSpaceOnRecord <= count) ? freeSpaceOnRecord : count;
         if (isRead)
            i = readBytes(buf, start, numberOfBytesToReadOrWrite);
         else
            // TOWRITE
            i = writeBytes(buf, start, numberOfBytesToReadOrWrite);
         if (i != -1)
            numberOfReadOrWroteBytes += i;
         start += i;
         count -= i;
      }
      return numberOfReadOrWroteBytes;
   }

   public void close() throws totalcross.io.IOException
   {
      int n = pdb.getRecordCount();
      if (n > 0) // make sure that the last record really have size = 4096
      {
         pdb.setRecordPos(n - 1);
         pdb.resizeRecord(PALMOS_RECORD_SIZE);
      }
      pdb.close();
   }

   /**
    * This method MUST be called to grow the file - otherwise, getSize won't
    * work correctly.
    *
    * @throws totalcross.io.IOException
    */
   public boolean growTo(int newSize) throws totalcross.io.IOException
   {
      int numberOfRecord = 0, sizeLastRecord = 0;
      int sizeToGrow = newSize - size;

      int currentSizeOfLastRecord = getLastRecordSize(); // sets the cursor on last record
      if (sizeToGrow + currentSizeOfLastRecord <= PALMOS_RECORD_SIZE) // can be put into last Record
      {
         // currentSizeOfLastRecord += sizeToGrow;
         if (pdb.getRecordCount() == 0) // first time to do this
            createRecord(sizeToGrow);
         else
            // update the first 8 bytes
            updateRecordSize(currentSizeOfLastRecord + sizeToGrow);
      }
      else
      // must be created new record(s)
      {
         int freeSpaceOnLastRecord = 0;
         if (pdb.getRecordCount() != 0)
         {
            if (currentSizeOfLastRecord != PALMOS_RECORD_SIZE) // there's no space left on last record
               freeSpaceOnLastRecord = PALMOS_RECORD_SIZE - currentSizeOfLastRecord;
            updateRecordSize(PALMOS_RECORD_SIZE);
         }

         numberOfRecord = (sizeToGrow - freeSpaceOnLastRecord) / PALMOS_RECORD_SIZE;
         sizeLastRecord = (sizeToGrow - freeSpaceOnLastRecord) % PALMOS_RECORD_SIZE;
         for (int i = 0; i < numberOfRecord; i++)
            createRecord(PALMOS_RECORD_SIZE);
         if (sizeLastRecord > 0)
            createRecord(sizeLastRecord);
      }
      size = newSize;
      pos = newSize;
      return true;
   }

   public void setPos(int p) throws totalcross.io.IOException
   {
      this.pos = p;
      if (p < 0)
         return;
      /* first record goes from 0 to 4095 (DATA) */
      int rec, pos;
      // discovers what is the record pos is
      rec = (p / PALMOS_RECORD_SIZE);
      pos = (p % PALMOS_RECORD_SIZE);
      pdb.setRecordPos(rec);
      pdb.setRecordOffset(pos + 8);
   }

   public void remove() throws totalcross.io.IOException
   {
      pdb.delete();
   }

   public void rename(String newName) throws totalcross.io.IOException
   {
      pdb.rename(newName);
   }

   // auxiliar methods
   private void createRecord(int size) throws totalcross.io.IOException
   {
      pdb.addRecord(8 + size);
      // stores the first four bytes
      if (size == PALMOS_RECORD_SIZE)
         pdb.writeBytes(b_dest1, 0, 8);
      else
      {
         setFourBytes(size);
         pdb.writeBytes(b_dest2, 0, 8);
      }
   }

   private void setFourBytes(int size)
   {
      b_dest2[7] = (byte) (size & 0xFF);
      size >>= 8;
      b_dest2[6] = (byte) (size & 0xFF);
      size >>= 8;
      b_dest2[5] = (byte) (size & 0xFF);
      size >>= 8;
      b_dest2[4] = (byte) (size & 0xFF);
   }

   private int getLastRecordSize() throws totalcross.io.IOException
   {
      int t = 0;
      int i = pdb.getRecordCount();
      if (i != 0)
      {
         pdb.setRecordPos(i - 1);
         t = pdb.getRecordSize() - 8;
      }
      return t;
   }

   private void updateRecordSize(int newSize) throws totalcross.io.IOException // update the current record
   {
      setFourBytes(newSize);
      pdb.setRecordOffset(0);
      pdb.writeBytes(b_dest2, 0, 8);
      pdb.resizeRecord(newSize + 8);
   }

   private void setSize() throws totalcross.io.IOException
   {
      int s = 0, rc, i;
      if ((rc = pdb.getRecordCount()) > 0)
      {
         s = --rc * PALMOS_RECORD_SIZE;
         pdb.setRecordPos(rc);
         i = pdb.getRecordSize();
         if (i > 0)
            s += (i - 8);
      }
      size = s;
   }
}
