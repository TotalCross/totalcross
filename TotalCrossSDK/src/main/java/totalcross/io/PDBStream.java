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

package totalcross.io;

/**
 * Used to make a PDBFile act as a single Stream, reading all records in sequence.
 * Usually you use this to open a photo taken with the Palm's camera.  
 * Note that it does not support writings.
 */

public class PDBStream extends Stream {
  protected int recSize;
  protected PDBFile pdb;

  /** Constructs a new PDBStream opening a PDB with the given name in READ_WRITE mode. 
   * @param name The pdb name of the file to open in the format Name.CRTR.TYPE. 
   */
  public PDBStream(String name) throws IllegalArgumentIOException, FileNotFoundException, IOException {
    pdb = new PDBFile(name, PDBFile.READ_WRITE);
    moveTo(0);
  }

  private void moveTo(int recno) throws IOException {
    pdb.setRecordPos(recno);
    pdb.skipBytes(8);
    recSize = pdb.getRecordSize();
  }

  /** Always throws IOException; not implemented. */
  @Override
  public int writeBytes(byte[] buf, int start, int count) throws IOException {
    throw new IOException("PDBStream.writeBytes is not implemented.");
  }

  /** Closes the underlying PDBFile. */
  @Override
  public void close() throws IOException {
    pdb.close();
  }

  @Override
  public int readBytes(byte[] buf, int start, int count) throws IOException {
    int total = 0;
    // read some more data from the current record
    int maxRead = Math.min(count, recSize - pdb.getRecordOffset());
    int read = pdb.readBytes(buf, start, maxRead);
    if (read == count) {
      return count; // quick check: probably most common case
    }

    if (read > 0) {
      total = read;
      start += read;
    }
    // could not read enough? move to next record and keep reading until we fill the buffer
    int endRec = pdb.getRecordCount();
    int p = pdb.getRecordPos();
    while (total < count) {
      if (p < endRec) // still has records?
      {
        moveTo(++p);
        maxRead = Math.min(count - total, recSize - pdb.getRecordOffset());
        read = pdb.readBytes(buf, start, maxRead);
        if (read > 0) {
          total += read;
          start += read;
        }
      } else {
        if (total == 0) {
          total = -1;
        }
        break;
      }
    }
    return total;
  }
}
