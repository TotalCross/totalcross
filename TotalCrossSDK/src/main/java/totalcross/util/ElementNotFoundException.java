// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util;

/** Thrown when an element of a Vector or a Hashtable is not found.
 */

public class ElementNotFoundException extends Exception {
  /** Constructs an exception with the given message. */
  public ElementNotFoundException(String s) {
    super(s);
  }
}
