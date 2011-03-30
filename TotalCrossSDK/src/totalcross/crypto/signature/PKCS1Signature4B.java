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

// $Id: PKCS1Signature4B.java,v 1.4 2011-01-04 13:19:29 guich Exp $

package totalcross.crypto.signature;

import net.rim.device.api.crypto.PKCS1SignatureSigner;
import net.rim.device.api.crypto.PKCS1SignatureVerifier;
import net.rim.device.api.crypto.RSACryptoSystem;
import totalcross.crypto.CryptoException;
import totalcross.crypto.cipher.Key;
import totalcross.crypto.cipher.RSAPrivateKey;
import totalcross.crypto.cipher.RSAPublicKey;
import totalcross.crypto.digest.Digest;
import totalcross.crypto.digest.MD5Digest;
import totalcross.crypto.digest.SHA1Digest;
import totalcross.crypto.digest.SHA256Digest;

public class PKCS1Signature4B extends Signature
{
   private net.rim.device.api.crypto.Digest digest;
   private String algorithm;
   
   public PKCS1Signature4B(Digest digest) throws CryptoException
   {
      if (digest instanceof MD5Digest)
      {
         this.digest = new net.rim.device.api.crypto.MD5Digest();
         algorithm = "MD5withRSA";
      }
      else if (digest instanceof SHA1Digest)
      {
         this.digest = new net.rim.device.api.crypto.SHA1Digest();
         algorithm = "SHA1withRSA";
      }
      else if (digest instanceof SHA256Digest)
      {
         this.digest = new net.rim.device.api.crypto.SHA256Digest();
         algorithm = "SHA256withRSA";
      }
      else
         throw new CryptoException("Invalid or unsupported signature digest: " + digest.getAlgorithm());
   }
   
   public String getAlgorithm()
   {
      return algorithm;
   }

   protected boolean isKeySupported(Key key, int operation)
   {
      return (operation == OPERATION_SIGN && key instanceof RSAPrivateKey) || (operation == OPERATION_VERIFY && key instanceof RSAPublicKey);
   }
   
   protected void doReset() throws CryptoException
   {
      try
      {
         if (operation == OPERATION_SIGN)
         {
            RSAPrivateKey privKey = (RSAPrivateKey)key;
            byte[] e = privKey.getPublicExponent();
            byte[] d = privKey.getPrivateExponent();
            byte[] n = privKey.getModulus();
            
            keyRef = new net.rim.device.api.crypto.RSAPrivateKey(new RSACryptoSystem(getModulusBitLength(n)), e, d, n);
         }
         else
         {
            RSAPublicKey pubKey = (RSAPublicKey)key;
            byte[] e = pubKey.getPublicExponent();
            byte[] n = pubKey.getModulus();
            
            keyRef = new net.rim.device.api.crypto.RSAPublicKey(new RSACryptoSystem(getModulusBitLength(n)), e, n);
         }
      }
      catch (net.rim.device.api.crypto.CryptoException e)
      {
         throw new CryptoException(e.getMessage());
      }
   }

   protected byte[] doSign(byte[] data) throws CryptoException
   {
      try
      {
         digest.reset();
         PKCS1SignatureSigner engine = new PKCS1SignatureSigner((net.rim.device.api.crypto.RSAPrivateKey)keyRef, digest, false);
         engine.update(data);
         
         byte[] signature = new byte[engine.getLength()];
         engine.sign(signature, 0);
         
         return signature;
      }
      catch (net.rim.device.api.crypto.CryptoException e)
      {
         throw new CryptoException(e.getMessage());
      }
   }
   
   protected boolean doVerify(byte[] data, byte[] signature) throws CryptoException
   {
      try
      {
         digest.reset();
         PKCS1SignatureVerifier engine = new PKCS1SignatureVerifier((net.rim.device.api.crypto.RSAPublicKey)keyRef, digest, signature, 0);
         engine.update(data);
         
         return engine.verify();
      }
      catch (net.rim.device.api.crypto.CryptoException e)
      {
         throw new CryptoException(e.getMessage());
      }
   }
   
   private int getModulusBitLength(byte[] n)
   {
      int i = 0, count = n.length;
      while (i < count && n[i] == 0)
         i++;
      
      return (count - i) * 8;
   }
}
