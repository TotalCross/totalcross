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

import totalcross.crypto.*;

public class SHA1Digest4D extends Digest
{
   static final byte[] ASN1_ID = { (byte)0x2B, (byte)0x0E, (byte)0x03, (byte)0x02, (byte)0x1A };
   
   public SHA1Digest4D() throws NoSuchAlgorithmException
   {
      nativeCreate();
   }
   
   public final String getAlgorithm()
   {
      return "SHA-1";
   }
   
   public final int getBlockLength()
   {
      return 64;
   }
   
   public final int getDigestLength()
   {
      return 20;
   }
   
   native void nativeCreate();
   
   native protected final byte[] process(byte[] data);
}
