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

package totalcross.net.mail;

/**
 * Thrown by the <code>Address</code> constructor if the given string address is invalid.
 * 
 * @since TotalCross 1.13
 */
public class AddressException extends Exception {
  /** 
   * Constructs an empty Exception. 
   */
  public AddressException() {
    super();
  }

  /** 
   * Constructs an exception with the given message. 
   *
   * @param msg The error message.
   */
  public AddressException(String msg) {
    super(msg);
  }
}
