// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Finds concrete native bridge definitions so strict generation can reject dangling registrations. */
final class NativeBridgeNativeSources {
  private static final Pattern DEFINITION = Pattern.compile(
      "\\b(?:TC_API|SYNC_API)\\s+void\\s+([A-Za-z0-9_]+)\\s*\\(\\s*NMParams\\s+p\\s*\\)\\s*\\{");

  private NativeBridgeNativeSources() {
  }

  static int validate(Path sourceRoot, NativeBridgeModel.Result model,
      NativeBridgeCompatibility compatibility) throws IOException {
    if (!Files.isDirectory(sourceRoot)) {
      throw new IOException("Native bridge source root does not exist: " + sourceRoot);
    }
    Map<String, Path> definitions = findDefinitions(sourceRoot);
    List<String> missing = new ArrayList<String>();
    for (NativeBridgeModel.Entry entry : model.entries) {
      String symbol = compatibility.effectiveSymbol(entry);
      if (!definitions.containsKey(symbol)) {
        missing.add(compatibility.moduleFor(entry) + "\t" + entry.identity() + "\t" + symbol);
      }
    }
    for (String symbol : compatibility.bridgeSymbols()) {
      if (!definitions.containsKey(symbol)) {
        missing.add("core\t<compatibility bridge>\t" + symbol);
      }
    }
    if (!missing.isEmpty()) {
      Collections.sort(missing);
      throw new IllegalStateException("Native bridge registrations lack source definitions:\n - "
          + String.join("\n - ", missing));
    }
    return model.entries.size() + compatibility.bridgeSymbols().size();
  }

  private static Map<String, Path> findDefinitions(Path sourceRoot) throws IOException {
    Map<String, Path> definitions = new LinkedHashMap<String, Path>();
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)
          .filter(NativeBridgeNativeSources::isNativeSource)
          .sorted()::iterator) {
        String source = stripComments(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
        Matcher matcher = DEFINITION.matcher(source);
        while (matcher.find()) definitions.putIfAbsent(matcher.group(1), path);
      }
    }
    return definitions;
  }

  private static boolean isNativeSource(Path path) {
    String name = path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
    return name.endsWith(".c") || name.endsWith(".cc") || name.endsWith(".cpp")
        || name.endsWith(".cxx") || name.endsWith(".h") || name.endsWith(".hpp");
  }

  private static String stripComments(String source) {
    StringBuilder result = new StringBuilder(source.length());
    boolean lineComment = false;
    boolean blockComment = false;
    char quote = 0;
    boolean escaped = false;
    for (int i = 0; i < source.length(); i++) {
      char current = source.charAt(i);
      char next = i + 1 < source.length() ? source.charAt(i + 1) : 0;
      if (lineComment) {
        if (current == '\n' || current == '\r') {
          lineComment = false;
          result.append(current);
        } else {
          result.append(' ');
        }
      } else if (blockComment) {
        if (current == '*' && next == '/') {
          result.append("  ");
          i++;
          blockComment = false;
        } else {
          result.append(current == '\n' || current == '\r' ? current : ' ');
        }
      } else if (quote != 0) {
        result.append(current == '\n' || current == '\r' ? current : ' ');
        if (escaped) escaped = false;
        else if (current == '\\') escaped = true;
        else if (current == quote) quote = 0;
      } else if (current == '/' && next == '/') {
        result.append("  ");
        i++;
        lineComment = true;
      } else if (current == '/' && next == '*') {
        result.append("  ");
        i++;
        blockComment = true;
      } else if (current == '"' || current == '\'') {
        result.append(' ');
        quote = current;
      } else {
        result.append(current);
      }
    }
    return result.toString();
  }
}
