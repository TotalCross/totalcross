// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

/** Encodes the legacy TotalCross Java-to-native symbol ABI. */
final class NativeBridgeSymbolEncoder {
  static final int MAX_SYMBOL_LENGTH = 32;

  private NativeBridgeSymbolEncoder() {
  }

  static String encode(String className, String methodName, String descriptor) {
    StringBuilder symbol = new StringBuilder(MAX_SYMBOL_LENGTH);
    appendOwner(symbol, className);
    symbol.append('_').append(methodName).append('_');

    boolean array = false;
    for (int i = 0; i < descriptor.length(); i++) {
      char c;
      switch (descriptor.charAt(i)) {
      case '(':
        continue;
      case ')':
        i = descriptor.length();
        continue;
      case '[':
        array = true;
        continue;
      case 'L':
        c = descriptor.charAt(++i);
        for (; descriptor.charAt(i) != ';'; i++) {
          if (descriptor.charAt(i) == '/' || descriptor.charAt(i) == '$') {
            c = descriptor.charAt(i + 1);
          }
        }
        break;
      default:
        c = descriptor.charAt(i);
        if (c == 'J') c = 'L';
        else if (c == 'Z') c = 'b';
        break;
      }
      symbol.append(array ? Character.toUpperCase(c) : Character.toLowerCase(c));
      array = false;
    }

    if (symbol.charAt(symbol.length() - 1) == '_') {
      symbol.setLength(symbol.length() - 1);
    }
    if (symbol.length() > MAX_SYMBOL_LENGTH) {
      symbol.setLength(MAX_SYMBOL_LENGTH);
    }
    return symbol.toString();
  }

  private static void appendOwner(StringBuilder symbol, String className) {
    String[] parts = className.split("/");
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      if (i < parts.length - 1) {
        symbol.append(Character.toLowerCase(part.charAt(0)));
      } else {
        for (int j = 0; j < part.length(); j++) {
          char c = part.charAt(j);
          if (('A' <= c && c <= 'Z') || ('0' <= c && c <= '9')) {
            symbol.append(c);
          }
        }
      }
    }
  }
}
