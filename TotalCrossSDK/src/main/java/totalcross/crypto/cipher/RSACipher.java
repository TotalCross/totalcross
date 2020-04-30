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
package totalcross.crypto.cipher;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.crypto.CryptoException;
import totalcross.crypto.NoSuchAlgorithmException;

/**
 * This class implements the RSA cryptographic cipher.
 */
public class RSACipher extends Cipher {
  public RSACipher() {
    init();
  }

  /**
   * Returns the name of the algorithm.
   * 
   * @return "RSA".
   */
  @Override
  public final String getAlgorithm() {
    return "RSA";
  }

  /**
   * Returns the block length.
   * 
   * @return Always returns 0.
   */
  @Override
  public final int getBlockLength() {
    return 0;
  }

  @Override
  @ReplacedByNativeOnDeploy
  protected final void doReset() throws NoSuchAlgorithmException, CryptoException {
    String transf = "RSA";
    switch (chaining) {
    case CHAINING_NONE:
      transf += "/NONE";
      break;
    case CHAINING_ECB:
      transf += "/ECB";
      break;
    case CHAINING_CBC:
      transf += "/CBC";
      break;
    }
    switch (padding) {
    case PADDING_NONE:
      transf += "/NoPadding";
      break;
    case PADDING_PKCS1:
      transf += "/PKCS1Padding";
      break;
    case PADDING_PKCS5:
      transf += "/PKCS5Padding";
    }

    try {
      javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(transf);
      cipherRef = cipher;

      KeyFactory factory = KeyFactory.getInstance("RSA");
      if (operation == OPERATION_ENCRYPT) {
        RSAPublicKey pubKey = (RSAPublicKey) key;
        keyRef = factory.generatePublic(
            new RSAPublicKeySpec(new BigInteger(pubKey.getModulus()), new BigInteger(pubKey.getPublicExponent())));
      } else // DECRYPT
      {
        RSAPrivateKey privKey = (RSAPrivateKey) key;
        keyRef = factory.generatePrivate(
            new RSAPrivateKeySpec(new BigInteger(privKey.getModulus()), new BigInteger(privKey.getPrivateExponent())));
      }

      int mode = operation == OPERATION_ENCRYPT ? javax.crypto.Cipher.ENCRYPT_MODE : javax.crypto.Cipher.DECRYPT_MODE;
      cipher.init(mode, (java.security.Key) keyRef, iv == null ? null : new IvParameterSpec(iv));
      if (iv == null) {
        iv = cipher.getIV();
      }
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new NoSuchAlgorithmException(e.getMessage());
    } catch (GeneralSecurityException e) {
      throw new CryptoException(e.getMessage());
    }
  }

  @Override
  @ReplacedByNativeOnDeploy
  protected final byte[] process(byte[] data) throws CryptoException {
    try {
      javax.crypto.Cipher cipher = (javax.crypto.Cipher) cipherRef;
      return cipher.doFinal(data);
    } catch (GeneralSecurityException e) {
      throw new CryptoException(e.getMessage());
    }
  }

  @Override
  protected final boolean isKeySupported(Key key, int operation) {
    return (operation == OPERATION_ENCRYPT && key instanceof RSAPublicKey)
        || (operation == OPERATION_DECRYPT && key instanceof RSAPrivateKey);
  }

  @Override
  protected final boolean isChainingSupported(int chaining) {
    return chaining == CHAINING_ECB;
  }

  @Override
  protected final boolean isPaddingSupported(int padding) {
    return padding == PADDING_PKCS1;
  }

  @ReplacedByNativeOnDeploy
  private void init() {

  }

  @Override
  @ReplacedByNativeOnDeploy
  protected final void finalize() {

  }
}
