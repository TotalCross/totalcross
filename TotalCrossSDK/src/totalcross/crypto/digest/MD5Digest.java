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

import java.security.MessageDigest;
import totalcross.crypto.*;

/**
 * This class implements the MD5 message digest algorithm.
 */
public class MD5Digest extends Digest
{
   /**
    * Creates a new MD5Digest object.
    * 
    * @throws NoSuchAlgorithmException If no Provider supports a <code>MessageDigestSpi</code> implementation for the specified algorithm.
    */
   public MD5Digest() throws NoSuchAlgorithmException
   {
      try
      {
         digestRef = MessageDigest.getInstance("MD5");
      }
      catch (java.security.NoSuchAlgorithmException e) 
      {
         throw new NoSuchAlgorithmException(e.getMessage());
      }
   }
   
   /**
    * Returns the name of the algorithm.
    * 
    * @return "MD5".
    */
   public final String getAlgorithm()
   {
      return "MD5";
   }
   
   /**
    * Returns the block length.
    * 
    * @return 64.
    */
   public final int getBlockLength()
   {
      return 64;
   }
   
   /**
    * Returns the message digest length.
    * 
    * @return 16.
    */
   public final int getDigestLength()
   {
      return 16;
   }
   
   protected final byte[] process(byte[] data)
   {
      MessageDigest digest = (MessageDigest)digestRef;
      digest.reset();
      digest.update(data);
      
      return ((MessageDigest)digestRef).digest();
   }
}
