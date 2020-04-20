/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.              *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
 *                                                                               *
 *********************************************************************************/

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
