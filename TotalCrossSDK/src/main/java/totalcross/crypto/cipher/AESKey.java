// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.crypto.cipher;

/**
 * This class implements the AES cryptographic cipher key.
 */
public class AESKey extends Key {
  private byte[] data;

  /**
   * Creates a new AESKey object with the given data.
   * 
   * @param data A byte array containing the key data.
   */
  public AESKey(byte[] data) {
    this.data = data;
  }

  /**
   * Gets the key data.
   * 
   * @return A copy of the byte array containing the key data.
   */
  public byte[] getData() {
    return data;
  }
}
