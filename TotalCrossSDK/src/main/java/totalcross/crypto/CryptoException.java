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
package totalcross.crypto;

/** 
 * Thrown when something wrong occurs when using the cryptographic algorithms.
 * 
 * <p>If you get a <code>totalcross.crypto.CryptoException: Illegal key size</code>, you must download the strong cryptography files from Oracle 
 * site. In order to do that, go to the ReadMe file whole link is below the download link. In this file, search for "Unlimited Strength Java 
 * Cryptography Extension" and follow the instructions. 
 */

public class CryptoException extends Exception {
  /** 
   * Constructs an empty Exception. 
   */
  public CryptoException() {
  }

  /** 
   * Constructs an exception with the given message. 
   *
   * @param msg The exception message.
   */
  public CryptoException(String msg) {
    super(msg);
  }
}
