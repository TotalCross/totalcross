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
package totalcross.crypto.signature;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.crypto.CryptoException;
import totalcross.crypto.NoSuchAlgorithmException;
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
public class PKCS1Signature extends Signature {
  protected Digest digest;
  private String algorithm;

  /**
   * Creates a new PKCS1Signature algorithm with the given message digest.
   * 
   * @param digest The message digest.
   * @throws CryptoException If the given message digest is not supported.
   * @throws NoSuchAlgorithmException If no Provider supports a Signature implementation for the specified algorithm.
   */
  public PKCS1Signature(Digest digest) throws NoSuchAlgorithmException, CryptoException {
    this.digest = digest;
    if (digest instanceof MD5Digest) {
      algorithm = "MD5withRSA";
    } else if (digest instanceof SHA1Digest) {
      algorithm = "SHA1withRSA";
    } else if (digest instanceof SHA256Digest) {
      algorithm = "SHA256withRSA";
    } else {
      throw new CryptoException("Invalid or unsupported signature digest: " + digest.getAlgorithm());
    }
    init();
  }

  /**
   * Returns the name of the algorithm.
   * 
   * @return The name of the algorithm used. 
   */
  @Override
  public String getAlgorithm() {
    return algorithm;
  }

  @Override
  protected boolean isKeySupported(Key key, int operation) {
    return (operation == OPERATION_SIGN && key instanceof RSAPrivateKey)
        || (operation == OPERATION_VERIFY && key instanceof RSAPublicKey);
  }

  @Override
  @ReplacedByNativeOnDeploy
  protected void doReset() throws NoSuchAlgorithmException, CryptoException {
    try {
      KeyFactory factory = KeyFactory.getInstance("RSA");
      if (operation == OPERATION_SIGN) {
        RSAPrivateKey privKey = (RSAPrivateKey) key;
        BigInteger d = new BigInteger(privKey.getPrivateExponent());
        BigInteger n = new BigInteger(privKey.getModulus());

        keyRef = factory.generatePrivate(new RSAPrivateKeySpec(n, d));
      } else {
        RSAPublicKey pubKey = (RSAPublicKey) key;
        BigInteger e = new BigInteger(pubKey.getPublicExponent());
        BigInteger n = new BigInteger(pubKey.getModulus());

        keyRef = factory.generatePublic(new RSAPublicKeySpec(n, e));
      }
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new CryptoException(e.getMessage());
    } catch (GeneralSecurityException e) {
      throw new CryptoException(e.getMessage());
    }
  }

  @Override
  @ReplacedByNativeOnDeploy
  protected byte[] doSign(byte[] data) throws CryptoException {
    try {
      java.security.Signature engine = (java.security.Signature) signatureRef;
      engine.initSign((java.security.PrivateKey) keyRef);
      engine.update(data);

      return engine.sign();
    } catch (GeneralSecurityException e) {
      throw new CryptoException(e.getMessage());
    }
  }

  @Override
  @ReplacedByNativeOnDeploy
  protected boolean doVerify(byte[] data, byte[] signature) throws CryptoException {
    try {
      java.security.Signature engine = (java.security.Signature) signatureRef;
      engine.initVerify((java.security.PublicKey) keyRef);
      engine.update(data);

      return engine.verify(signature);
    } catch (GeneralSecurityException e) {
      throw new CryptoException(e.getMessage());
    }
  }

  @ReplacedByNativeOnDeploy
  private void init() throws CryptoException {
    try {
      signatureRef = java.security.Signature.getInstance(algorithm);
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new CryptoException(e.getMessage());
    }
  }

  @Override
  @ReplacedByNativeOnDeploy
  protected final void finalize() {

  }
}
