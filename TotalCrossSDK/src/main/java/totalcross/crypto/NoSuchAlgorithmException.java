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
 * This exception is thrown when a particular cryptographic algorithm is requested but is not available in the environment.
 */
public class NoSuchAlgorithmException extends CryptoException {
  /**
   * Constructs a NoSuchAlgorithmException with no detail message. A detail message is a string that describes this particular exception.
   */
  public NoSuchAlgorithmException() {
  }

  /**
   * Constructs a NoSuchAlgorithmException with the specified detail message. A detail message is a string that describes this particular exception, 
   * which may, for example, specifies which algorithm is not available.
   *
   * @param msg The exception message.
   */
  public NoSuchAlgorithmException(String msg) {
    super(msg);
  }
}
