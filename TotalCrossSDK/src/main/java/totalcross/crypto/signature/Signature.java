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

import totalcross.crypto.CryptoException;
import totalcross.crypto.NoSuchAlgorithmException;
import totalcross.crypto.cipher.Key;
import totalcross.io.ByteArrayStream;

/**
 * This class provides the functionality of a signature algorithm for signing and verifying.
 */
public abstract class Signature {
  Object signatureRef;
  Object keyRef;

  protected int operation = -1;
  protected Key key;

  private ByteArrayStream input = new ByteArrayStream(128);
  private byte[] oneByte = new byte[1];

  /** 
   * Constant used to initialize this signature algorithm to sign. 
   */
  public static final int OPERATION_SIGN = 0;

  /** 
   * Constant used to initialize this signature algorithm to verify. 
   */
  public static final int OPERATION_VERIFY = 1;

  /**
   * Returns the name of the algorithm.
   * 
   * @return The name of the algorithm used. 
   */
  @Override
  public final String toString() {
    return getAlgorithm();
  }

  /**
   * Initializes this message signature algorithm to sign or verify. Calling this method will also reset the input data buffer.
   * 
   * @param operation The operation mode of this signature algorithm (<code>OPERATION_SIGN</code> or <code>OPERATION_VERIFY</code>).
   * @param key The key.
   * 
   * @throws CryptoException If one or more initialization parameters are invalid or the signature algorithm fails to initialize with the given 
   * parameters. 
   */
  public final void reset(int operation, Key key) throws CryptoException {
    if (operation < OPERATION_SIGN || operation > OPERATION_VERIFY) {
      throw new CryptoException("Invalid or unsupported signature operation: " + operation);
    }
    if (key == null || !isKeySupported(key, operation)) {
      throw new CryptoException("Invalid or unsupported signature key: " + key);
    }

    this.operation = operation;
    this.key = key;

    input.reset();
    doReset();
  }

  /**
   * Updates the input data that will be processed by this signature algorithm. The data will be accumulated in an input buffer to be processed when 
   * {@link #sign()} or {@link #verify(byte[])} is finally called.
   * 
   * @param data The input data.
   */
  public final void update(int data) {
    oneByte[0] = (byte) (data & 0xFF);
    input.writeBytes(oneByte, 0, 1);
  }

  /**
   * Updates the input data that will be processed by this signature algorithm. The data will be accumulated in an input buffer to be processed when 
   * {@link #sign()} or {@link #verify(byte[])} is finally called.
   * 
   * @param data The input data.
   */
  public final void update(byte[] data) {
    input.writeBytes(data, 0, data.length);
  }

  /**
   * Updates the input data that will be processed by this signature algorithm. The data will be accumulated in an input buffer to be processed when 
   * {@link #sign()} or {@link #verify(byte[])} is finally called.
   * 
   * @param data The input data.
   * @param start The offset in <code>data</code> where the data starts.
   * @param count The input length.
   */
  public final void update(byte[] data, int start, int count) {
    input.writeBytes(data, start, count);
  }

  /**
   * Finalizes the sign operation by processing all the accumulated input data and returning
   * the result in a new buffer.
   * 
   * @return The signature in a new buffer.
   */
  public byte[] sign() throws CryptoException {
    if (operation != OPERATION_SIGN) {
      throw new CryptoException("Signature is not in sign mode");
    }

    return doSign(input.toByteArray());
  }

  /**
   * Finalizes the verify operation by processing all the accumulated input data and returning
   * the signature comparison result.
   * 
   * @return the signature comparison result.
   */
  public boolean verify(byte[] signature) throws CryptoException {
    if (operation != OPERATION_VERIFY) {
      throw new CryptoException("Signature is not in verify mode");
    }

    return doVerify(input.toByteArray(), signature);
  }

  /**
   * Returns the name of the signature algorithm.
   * 
   * @return The name of the signature algorithm used. 
   */
  public abstract String getAlgorithm();

  protected abstract boolean isKeySupported(Key key, int operation);

  protected abstract void doReset() throws NoSuchAlgorithmException, CryptoException;

  protected abstract byte[] doSign(byte[] data) throws CryptoException;

  protected abstract boolean doVerify(byte[] data, byte[] expected) throws CryptoException;
}
