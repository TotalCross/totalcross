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

// $Id: PKCS1Signature.java,v 1.5 2011-01-04 13:19:29 guich Exp $

package totalcross.crypto.signature;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import totalcross.crypto.CryptoException;
import totalcross.crypto.cipher.Key;
import totalcross.crypto.cipher.RSAPrivateKey;
import totalcross.crypto.cipher.RSAPublicKey;
import totalcross.crypto.digest.Digest;
import totalcross.crypto.digest.MD5Digest;
import totalcross.crypto.digest.SHA1Digest;
import totalcross.crypto.digest.SHA256Digest;

/**
 * This class implements the PKCS #1 signature algorithm.
 */
public class PKCS1Signature extends Signature
{
   private String algorithm;
   
   /**
    * Creates a new PKCS1Signature algorithm with the given message digest.
    * 
    * @param digest the message digest.
    * 
    * @throws CryptoException if the given message digest is not supported.
    */
   public PKCS1Signature(Digest digest) throws CryptoException
   {
      if (digest instanceof MD5Digest)
         algorithm = "MD5withRSA";
      else if (digest instanceof SHA1Digest)
         algorithm = "SHA1withRSA";
      else if (digest instanceof SHA256Digest)
         algorithm = "SHA256withRSA";
      else
         throw new CryptoException("Invalid or unsupported signature digest: " + digest.getAlgorithm());
      
      try
      {
         signatureRef = java.security.Signature.getInstance(algorithm);
      }
      catch (GeneralSecurityException e)
      {
         throw new CryptoException(e.getMessage());
      }
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
         KeyFactory factory = KeyFactory.getInstance("RSA");
         if (operation == OPERATION_SIGN)
         {
            RSAPrivateKey privKey = (RSAPrivateKey)key;
            BigInteger d = new BigInteger(privKey.getPrivateExponent());
            BigInteger n = new BigInteger(privKey.getModulus());
            
            keyRef = factory.generatePrivate(new RSAPrivateKeySpec(n, d));
         }
         else
         {
            RSAPublicKey pubKey = (RSAPublicKey)key;
            BigInteger e = new BigInteger(pubKey.getPublicExponent());
            BigInteger n = new BigInteger(pubKey.getModulus());
            
            keyRef = factory.generatePublic(new RSAPublicKeySpec(n, e));
         }
      }
      catch (GeneralSecurityException e)
      {
         throw new CryptoException(e.getMessage());
      }
   }

   protected byte[] doSign(byte[] data) throws CryptoException
   {
      try
      {
         java.security.Signature engine = (java.security.Signature)signatureRef;
         engine.initSign((java.security.PrivateKey)keyRef);
         engine.update(data);
         
         return engine.sign();
      }
      catch (GeneralSecurityException e)
      {
         throw new CryptoException(e.getMessage());
      }
   }
   
   protected boolean doVerify(byte[] data, byte[] signature) throws CryptoException
   {
      try
      {
         java.security.Signature engine = (java.security.Signature)signatureRef;
         engine.initVerify((java.security.PublicKey)keyRef);
         engine.update(data);
         
         return engine.verify(signature);
      }
      catch (GeneralSecurityException e)
      {
         throw new CryptoException(e.getMessage());
      }
   }
}
