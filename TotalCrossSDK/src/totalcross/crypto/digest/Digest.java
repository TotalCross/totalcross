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

package totalcross.crypto.digest;

import totalcross.io.ByteArrayStream;

/**
 * This class provides the functionality of a message digest algorithm.
 */
public abstract class Digest
{
   Object digestRef;
   
   protected ByteArrayStream input = new ByteArrayStream(128);
   private byte[] oneByte = new byte[1];
   
   /**
    * Returns the name of the algorithm.
    * 
    * @return The name of the algorithm whose class heirs from Digest. 
    */
   public String toString()
   {
      return getAlgorithm();
   }
   
   /**
    * Returns the name of the algorithm.
    * 
    * @return The name of the algorithm whose class heirs from Digest. 
    */
   public abstract String getAlgorithm();
   
   /**
    * Returns the block length.
    * 
    * @return The block length (in bytes).
    */
   public abstract int getBlockLength();
   
   /**
    * Returns the message digest length.
    * 
    * @return The message digest length (in bytes).
    */
   public abstract int getDigestLength();
   
   /**
    * Initializes this message digest. Calling this method will also reset the input data buffer.
    */
   public final void reset()
   {
      input.reset();
   }
   
   /**
    * Updates the input data that will be processed by this message digest algorithm. The data will be accumulated in an input buffer to be processed 
    * when {@link #getDigest()} is finally called.
    * 
    * @param data The input data.
    */
   public final void update(int data)
   {
      oneByte[0] = (byte)(data & 0xFF);
      input.writeBytes(oneByte, 0, 1);
   }
   
   /**
    * Updates the input data that will be processed by this message digest algorithm. The data will be accumulated in an input buffer to be processed 
    * when {@link #getDigest()} is finally called.
    * 
    * @param data The input data.
    */
   public final void update(byte[] data)
   {
      input.writeBytes(data, 0, data.length);
   }
   
   /**
    * Updates the input data that will be processed by this message digest algorithm. The data will be accumulated in an input buffer to be processed 
    * when {@link #getDigest()} is finally called.
    * 
    * @param data The input data.
    * @param start The offset in <code>data</code> where the data starts.
    * @param count The input length.
    */
   public final void update(byte[] data, int start, int count)
   {
      input.writeBytes(data, start, count);
   }
   
   /**
    * Finalizes the message digest operation by processing all the accumulated input data and returning the result in a new buffer.
    * 
    * @return The operation result in a new buffer.
    */
   public final byte[] getDigest()
   {
      return process(input.toByteArray());
   }
   
   protected abstract byte[] process(byte[] data);
}
