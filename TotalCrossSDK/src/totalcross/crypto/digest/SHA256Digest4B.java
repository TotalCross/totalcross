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



package totalcross.crypto.digest;

public class SHA256Digest4B extends Digest
{
   public SHA256Digest4B()
   {
      digestRef = new net.rim.device.api.crypto.SHA256Digest();
   }
   
   public final String getAlgorithm()
   {
      return "SHA-256";
   }
   
   public final int getBlockLength()
   {
      return 64;
   }
   
   public final int getDigestLength()
   {
      return 32;
   }
   
   protected final byte[] process(byte[] data)
   {
      net.rim.device.api.crypto.SHA256Digest digest = (net.rim.device.api.crypto.SHA256Digest)digestRef;
      digest.reset();
      digest.update(data);
      
      return digest.getDigest();
   }
}
