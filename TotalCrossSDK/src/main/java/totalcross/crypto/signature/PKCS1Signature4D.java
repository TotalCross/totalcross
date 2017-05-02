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



package totalcross.crypto.signature;

import totalcross.crypto.*;
import totalcross.crypto.cipher.Key;
import totalcross.crypto.cipher.RSAPrivateKey;
import totalcross.crypto.cipher.RSAPublicKey;
import totalcross.crypto.digest.Digest;
import totalcross.crypto.digest.MD5Digest;
import totalcross.crypto.digest.SHA1Digest;
import totalcross.crypto.digest.SHA256Digest;

public class PKCS1Signature4D extends Signature
{ 
   Digest digest;
   private String algorithm;
   
   protected Object nativeHeap;
   
   public PKCS1Signature4D(Digest digest) throws NoSuchAlgorithmException, CryptoException
   {
      if (digest instanceof MD5Digest)
         algorithm = "MD5withRSA";
      else if (digest instanceof SHA1Digest)
         algorithm = "SHA1withRSA";
      else if (digest instanceof SHA256Digest)
         algorithm = "SHA256withRSA";
      else
         throw new CryptoException("Invalid or unsupported signature digest: " + digest.getAlgorithm());
      
      this.digest = digest;
      nativeCreate();
   }
   
   public String getAlgorithm()
   {
      return algorithm;
   }

   protected boolean isKeySupported(Key key, int operation)
   {
      return (operation == OPERATION_SIGN && key instanceof RSAPrivateKey) || (operation == OPERATION_VERIFY && key instanceof RSAPublicKey);
   }
   
   native void nativeCreate();
   
   native protected final void finalize();
   
   native protected final void doReset() throws NoSuchAlgorithmException, CryptoException;
   
   native protected byte[] doSign(byte[] data) throws CryptoException;
   
   native protected boolean doVerify(byte[] data, byte[] signature) throws CryptoException;
}
