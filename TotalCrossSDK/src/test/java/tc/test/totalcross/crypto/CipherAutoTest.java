/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.test.totalcross.crypto;

import totalcross.crypto.CryptoException;
import totalcross.crypto.cipher.AESCipher;
import totalcross.crypto.cipher.AESKey;
import totalcross.crypto.cipher.Cipher;
import totalcross.crypto.cipher.Key;
import totalcross.crypto.cipher.RSACipher;
import totalcross.crypto.cipher.RSAPrivateKey;
import totalcross.crypto.cipher.RSAPublicKey;
import totalcross.ui.MainWindow;

public class CipherAutoTest extends MainWindow {
  private static final byte[] AES_KEY = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

  private static final byte[] RSA_N = new byte[] { (byte) 0, (byte) -60, (byte) -106, (byte) -118, (byte) -19,
      (byte) 57, (byte) -63, (byte) -18, (byte) 102, (byte) 111, (byte) -56, (byte) 1, (byte) 50, (byte) -101,
      (byte) -90, (byte) -85, (byte) -96, (byte) -66, (byte) -70, (byte) -49, (byte) -52, (byte) -3, (byte) 70,
      (byte) -120, (byte) 63, (byte) -76, (byte) -34, (byte) -114, (byte) 13, (byte) 8, (byte) 45, (byte) -124,
      (byte) -12, (byte) -6, (byte) 87, (byte) 90, (byte) 61, (byte) -124, (byte) -42, (byte) 34, (byte) 21, (byte) 14,
      (byte) -73, (byte) 21, (byte) -104, (byte) 70, (byte) 11, (byte) -59, (byte) 58, (byte) -72, (byte) -55,
      (byte) -98, (byte) 68, (byte) 123, (byte) -63, (byte) -11, (byte) -7, (byte) -115, (byte) 32, (byte) 57,
      (byte) -38, (byte) -41, (byte) -9, (byte) -108, (byte) 79 };

  private static final byte[] RSA_D = new byte[] { (byte) 122, (byte) -69, (byte) 13, (byte) -94, (byte) -54,
      (byte) -61, (byte) 67, (byte) 37, (byte) -38, (byte) -75, (byte) 127, (byte) -31, (byte) -21, (byte) -128,
      (byte) -29, (byte) 119, (byte) 104, (byte) 123, (byte) -46, (byte) -115, (byte) -60, (byte) -75, (byte) -53,
      (byte) 12, (byte) 18, (byte) -52, (byte) 58, (byte) -36, (byte) -15, (byte) -11, (byte) 17, (byte) 34,
      (byte) -109, (byte) -121, (byte) 5, (byte) 117, (byte) 109, (byte) -72, (byte) -27, (byte) -103, (byte) -85,
      (byte) -1, (byte) 37, (byte) -30, (byte) 38, (byte) -86, (byte) 88, (byte) -28, (byte) -26, (byte) -102,
      (byte) -10, (byte) 124, (byte) -97, (byte) -18, (byte) -118, (byte) 2, (byte) 36, (byte) 40, (byte) -47,
      (byte) -75, (byte) -44, (byte) 69, (byte) 10, (byte) 1 };

  private static final byte[] RSA_E = new byte[] { (byte) 1, (byte) 0, (byte) 1 };

  private Cipher[] ciphers = new Cipher[] { new AESCipher(), new RSACipher() };
  private Key[] encKeys = new Key[] { new AESKey(AES_KEY), new RSAPublicKey(RSA_E, RSA_N) };
  private Key[] decKeys = new Key[] { encKeys[0], new RSAPrivateKey(RSA_E, RSA_D, RSA_N) };

  private String input = "0123456789ABCDEF";
  private int[] chaining = new int[] { Cipher.CHAINING_NONE, Cipher.CHAINING_ECB, Cipher.CHAINING_CBC };
  private int[] padding = new int[] { Cipher.PADDING_NONE, Cipher.PADDING_PKCS1, Cipher.PADDING_PKCS5 };

  public CipherAutoTest() {
    super("Cipher Automatic Test", TAB_ONLY_BORDER);
  }

  @Override
  public void initUI() {
    try {
      testCipher(ciphers[0], encKeys[0], decKeys[0], chaining[0], padding[0]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      testCipher(ciphers[0], encKeys[0], decKeys[0], chaining[0], padding[1]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      testCipher(ciphers[0], encKeys[0], decKeys[0], chaining[0], padding[2]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      testCipher(ciphers[0], encKeys[0], decKeys[0], chaining[1], padding[0]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      testCipher(ciphers[0], encKeys[0], decKeys[0], chaining[1], padding[1]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      if (!testCipher(ciphers[0], encKeys[0], decKeys[0], chaining[1], padding[2]).equals(input)) {
        throw new RuntimeException();
      }
    } catch (CryptoException exception) {
      throw new RuntimeException();
    }
    try {
      testCipher(ciphers[0], encKeys[0], decKeys[0], chaining[2], padding[0]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      testCipher(ciphers[0], encKeys[0], decKeys[0], chaining[2], padding[1]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      if (!testCipher(ciphers[0], encKeys[0], decKeys[0], chaining[2], padding[2]).equals(input)) {
        throw new RuntimeException();
      }
    } catch (CryptoException exception) {
      throw new RuntimeException();
    }
    try {
      testCipher(ciphers[1], encKeys[1], decKeys[1], chaining[0], padding[0]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      testCipher(ciphers[1], encKeys[1], decKeys[1], chaining[0], padding[1]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      testCipher(ciphers[1], encKeys[1], decKeys[1], chaining[0], padding[2]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      testCipher(ciphers[1], encKeys[1], decKeys[1], chaining[1], padding[0]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      if (!testCipher(ciphers[1], encKeys[1], decKeys[1], chaining[1], padding[1]).equals(input)) {
        throw new RuntimeException();
      }
    } catch (CryptoException exception) {
      throw new RuntimeException();
    }
    try {
      testCipher(ciphers[1], encKeys[1], decKeys[1], chaining[2], padding[0]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      testCipher(ciphers[1], encKeys[1], decKeys[1], chaining[2], padding[1]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
    try {
      testCipher(ciphers[1], encKeys[1], decKeys[1], chaining[2], padding[2]);
      throw new RuntimeException();
    } catch (CryptoException exception) {
    }
  }

  private String testCipher(Cipher cipher, Key encKey, Key decKey, int chaining, int padding) throws CryptoException {
    byte[] iv = null; // no initialization vector => let the cipher generate a random one
    cipher.reset(Cipher.OPERATION_ENCRYPT, encKey, chaining, iv, padding);
    iv = cipher.getIV(); // store the generated iv

    cipher.update(input.getBytes());
    byte[] encrypted = cipher.getOutput();

    cipher.reset(Cipher.OPERATION_DECRYPT, decKey, chaining, iv, padding);
    cipher.update(encrypted);
    return new String(cipher.getOutput());
  }
}
