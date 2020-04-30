// Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.nio.charset;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashSet;
import java.util.Set;

import totalcross.sys.Convert;

public abstract class Charset4D implements Comparable<Charset> {

  private final String name;

  private final Set<String> setAliases;

  protected Charset4D(String name, String[] aliases) {
    this.name = name;

    setAliases = new HashSet<>();
    for (String alias : aliases) {
      setAliases.add(alias);
    }
  }

  public final String name() {
    return name;
  }

  public Set<String> aliases() {
    return new HashSet<>(setAliases);
  }

    /**
     * Returns a charset object for the named charset.
     * 
     * @param charsetName The name of the requested charset; may be either a
     *                    canonical name or an alias
     * @return A charset object for the named charset
     * @throws IllegalArgumentException    If the given charsetName is null
     * @throws IllegalCharsetNameException if the specified charset name is illegal.
     * @throws UnsupportedCharsetException If no support for the named charset is
     *                                     available
     */
    public static Charset forName(String charsetName)
            throws IllegalArgumentException, UnsupportedCharsetException, IllegalCharsetNameException {
        checkCharsetName(charsetName);
        Charset charset = Convert.charsetForName(charsetName);
        if (charset == null) {
            throw new UnsupportedCharsetException(charsetName);
        }
        return charset;
    }
  
  /*
   * Checks whether a given string is a legal charset name. The argument name
   * should not be null.
   */
  private static void checkCharsetName(String name) {
      // An empty string is illegal charset name
      if (name.length() == 0) {
          throw new IllegalCharsetNameException(name);
      }
      // The first character must be a letter or a digit
       char first = name.charAt(0);
       if (!isLetter(first) && !isDigit(first)) {
           throw new IllegalCharsetNameException(name);
       }
      // Check the remaining characters
      int length = name.length();
      for (int i = 0; i < length; i++) {
          char c = name.charAt(i);
          if (!isLetter(c) && !isDigit(c) && !isSpecial(c)) {
              throw new IllegalCharsetNameException(name);
          }
      }
  }
  
  /*
   * Checks whether a character is a special character that can be used in
   * charset names, other than letters and digits.
   */
  private static boolean isSpecial(char c) {
      return ('+' == c || '-' == c || '.' == c || ':' == c || '_' == c);
  }

  /*
   * Checks whether a character is a letter (ascii) which are defined in the
   * spec.
   */
  private static boolean isLetter(char c) {
      return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
  }

  /*
   * Checks whether a character is a digit (ascii) which are defined in the
   * spec.
   */
  private static boolean isDigit(char c) {
      return ('0' <= c && c <= '9');
  }
}
