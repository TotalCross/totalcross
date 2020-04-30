// Copyright (C) 2003-2004 Pierre G. Richard
// Copyright (C) 2004-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.xml;

/**
 * Exception thrown by the XmlTokenizer when a syntax error is found.
 */
public class SyntaxException extends Exception {
  /**
   * Get a human readable explaination from the code
   *
   * @param code exception code
   * @return an explanatory message
   * Impl Note: this is set apart for future localization
   */
  private static String getMessageFromCode(int code) {
    switch (code) {
    case 1:
    case 2:
      return "invalid tag name";
    case 3:
      return "invalid attribute name";
    case 5:
      return "attribute value indicator is missing";
    case 6:
      return "delimiter literal is missing";
    case 7:
      return "missing end quote";
    case 8:
    case 15:
      return "unterminated start-tag";
    case 9:
      return "unterminated start-empty-tag";
    case 11:
      return "invalid entity reference";
    case 12:
    case 13:
    case 14:
      return "unterminated end tag";
    case 16:
    case 17:
      return "unterminated declaration";
    case 18:
    case 19:
      return "unterminated comment";
    case 20:
      return "unterminated processing instruction";
    case 21:
      return "missing tag close delimiter";
    case 22:
    case 23:
    case 24:
      return "missing CDATA end tag";
    default:
      return "???";
    }
  }

  /**
   * Constructor with an explanatory message.
   *
   * @param code exception code
   * @param offset absolute offset (seek pos) at which the error occurred
   */
  SyntaxException(int code, int offset) {
    super("Error " + code + " at offset " + offset + ": " + getMessageFromCode(code));
  }
}
