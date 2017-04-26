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
 * This class implements the SHA-1 message digest algorithm.
 */
public class SHA1Digest extends Digest
{
   /**
    * Creates a new SHA1Digest object.
    * 
    * @throws NoSuchAlgorithmException If no Provider supports a <code>MessageDigestSpi</code> implementation for the specified algorithm.
    */
   public SHA1Digest() throws NoSuchAlgorithmException
   {
      try
      {
         digestRef = MessageDigest.getInstance("SHA-1");
      }
      catch (java.security.NoSuchAlgorithmException e) 
      {
         throw new NoSuchAlgorithmException(e.getMessage());
      }
   }
   
   /**
    * Returns the name of the algorithm.
    * 
    * @return "SHA-1".
    */
   public final String getAlgorithm()
   {
      return "SHA-1";
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
    * @return 20.
    */
   public final int getDigestLength()
   {
      return 20;
   }
   
   protected final byte[] process(byte[] data)
   {
      MessageDigest digest = (MessageDigest)digestRef;
      digest.reset();
      digest.update(data);
      
      return ((MessageDigest)digestRef).digest();
   }
}
