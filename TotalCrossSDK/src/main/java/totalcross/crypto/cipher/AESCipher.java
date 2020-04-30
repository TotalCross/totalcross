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

import java.security.GeneralSecurityException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.crypto.CryptoException;
import totalcross.crypto.NoSuchAlgorithmException;

/**
 * This class implements the AES cryptographic cipher.
 *
 * <p>If you get a <code>totalcross.crypto.CryptoException: Illegal key size</code>, you must download the strong cryptography files from Oracle 
 * site. In order to do that, go to the ReadMe file whole link is below the download link. In this file, search for "Unlimited Strength Java 
 * Cryptography Extension" and follow the instructions. 
 */
public class AESCipher extends Cipher {
  public AESCipher() {
    init();
  }

  /**
   * Returns the name of the algorithm.
   * 
   * @return "AES".
   */
  @Override
  public final String getAlgorithm() {
    return "AES";
  }

  /**
   * Returns the block length.
   * 
   * @return Always returns 16.
   */
  @Override
  public final int getBlockLength() {
    // Applet may support 16 or 32, like axtls
    // javax.crypto.Cipher cipher = (javax.crypto.Cipher)cipherRef;
    // return (cipher != null) ? cipher.getBlockSize() : 16;
    return 16;
  }

  @Override
  @ReplacedByNativeOnDeploy
  protected final void doReset() throws NoSuchAlgorithmException, CryptoException {
    String transf = "AES";
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

      keyRef = new SecretKeySpec(((AESKey) key).getData(), "AES");

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
    return key instanceof AESKey;
  }

  @Override
  protected final boolean isChainingSupported(int chaining) {
    return chaining == CHAINING_ECB || chaining == CHAINING_CBC;
  }

  @Override
  protected final boolean isPaddingSupported(int padding) {
    return padding == PADDING_PKCS5;
  }

  @ReplacedByNativeOnDeploy
  private void init() {

  }
}
