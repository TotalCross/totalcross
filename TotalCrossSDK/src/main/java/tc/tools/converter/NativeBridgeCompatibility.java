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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Small explicit compatibility layer for bridge metadata that cannot be
 * derived from compiled Java declarations alone.
 *
 * <p>The file format is tab-separated:</p>
 * <ul>
 *   <li>override TAB deployed-owner#method(descriptor)return TAB symbol</li>
 *   <li>bridge TAB symbol</li>
 *   <li>header TAB symbol</li>
 *   <li>guard TAB preprocessor-macro TAB symbol</li>
 *   <li>module TAB module-name TAB deployed-owner-prefix</li>
 * </ul>
 */
final class NativeBridgeCompatibility {
  private final Map<String, String> overrides;
  private final Set<String> explicitBridgeSymbols;
  private final Set<String> bridgeSymbols;
  private final Set<String> headerOnlySymbols;
  private final Map<String, String> guardsBySymbol;
  private final Map<String, String> modulesByOwnerPrefix;

  private NativeBridgeCompatibility(Map<String, String> overrides,
      Set<String> explicitBridgeSymbols, Set<String> bridgeSymbols,
      Set<String> headerOnlySymbols, Map<String, String> guardsBySymbol,
      Map<String, String> modulesByOwnerPrefix) {
    this.overrides = overrides;
    this.explicitBridgeSymbols = explicitBridgeSymbols;
    this.bridgeSymbols = bridgeSymbols;
    this.headerOnlySymbols = headerOnlySymbols;
    this.guardsBySymbol = guardsBySymbol;
    this.modulesByOwnerPrefix = modulesByOwnerPrefix;
  }

  static NativeBridgeCompatibility empty() {
    return new NativeBridgeCompatibility(Collections.emptyMap(), Collections.emptySet(),
        Collections.emptySet(), Collections.emptySet(), Collections.emptyMap(), Collections.emptyMap());
  }

  static NativeBridgeCompatibility read(Path path) throws IOException {
    if (path == null) return empty();

    Map<String, String> overrides = new LinkedHashMap<String, String>();
    Set<String> explicitBridgeSymbols = new LinkedHashSet<String>();
    Set<String> bridgeSymbols = new LinkedHashSet<String>();
    Set<String> headerOnlySymbols = new LinkedHashSet<String>();
    Map<String, String> guardsBySymbol = new LinkedHashMap<String, String>();
    Map<String, String> modulesByOwnerPrefix = new LinkedHashMap<String, String>();

    int lineNumber = 0;
    for (String raw : Files.readAllLines(path, StandardCharsets.UTF_8)) {
      lineNumber++;
      String line = raw.trim();
      if (line.isEmpty() || line.startsWith("#")) continue;
      String[] parts = line.split("\\t");
      switch (parts[0]) {
      case "override":
        requireParts(path, lineNumber, parts, 3);
        putUnique(path, lineNumber, overrides, parts[1], parts[2]);
        break;
      case "bridge":
        requireParts(path, lineNumber, parts, 2);
        explicitBridgeSymbols.add(parts[1]);
        bridgeSymbols.add(parts[1]);
        break;
      case "header":
        requireParts(path, lineNumber, parts, 2);
        headerOnlySymbols.add(parts[1]);
        break;
      case "guard":
        requireParts(path, lineNumber, parts, 3);
        bridgeSymbols.add(parts[2]);
        putUnique(path, lineNumber, guardsBySymbol, parts[2], parts[1]);
        break;
      case "module":
        requireParts(path, lineNumber, parts, 3);
        if (!parts[1].matches("[a-z][a-z0-9_-]*") || "core".equals(parts[1])) {
          throw new IllegalArgumentException(path + ":" + lineNumber + ": invalid optional module name " + parts[1]);
        }
        if (!parts[2].matches("[A-Za-z0-9_$/]+/$")) {
          throw new IllegalArgumentException(path + ":" + lineNumber
              + ": deployed owner prefix must be a slash-terminated package path");
        }
        putUnique(path, lineNumber, modulesByOwnerPrefix, parts[2], parts[1]);
        break;
      default:
        throw new IllegalArgumentException(path + ":" + lineNumber
            + ": unknown native bridge compatibility directive: " + parts[0]);
      }
    }

    return new NativeBridgeCompatibility(
        Collections.unmodifiableMap(overrides),
        Collections.unmodifiableSet(explicitBridgeSymbols),
        Collections.unmodifiableSet(bridgeSymbols),
        Collections.unmodifiableSet(headerOnlySymbols),
        Collections.unmodifiableMap(guardsBySymbol),
        Collections.unmodifiableMap(modulesByOwnerPrefix));
  }

  String effectiveSymbol(NativeBridgeModel.Entry entry) {
    String override = overrides.get(entry.identity());
    return override == null ? entry.symbol : override;
  }

  String guardFor(String symbol) {
    return guardsBySymbol.get(symbol);
  }

  Set<String> bridgeSymbols() {
    return bridgeSymbols;
  }

  Set<String> headerOnlySymbols() {
    return headerOnlySymbols;
  }

  String moduleFor(NativeBridgeModel.Entry entry) {
    String selectedModule = "core";
    int selectedLength = -1;
    for (Map.Entry<String, String> module : modulesByOwnerPrefix.entrySet()) {
      if (entry.deployedOwner.startsWith(module.getKey()) && module.getKey().length() > selectedLength) {
        selectedModule = module.getValue();
        selectedLength = module.getKey().length();
      }
    }
    return selectedModule;
  }

  void validateOverrides(List<NativeBridgeModel.Entry> entries) {
    if (overrides.isEmpty()) return;
    Set<String> identities = new LinkedHashSet<String>();
    for (NativeBridgeModel.Entry entry : entries) identities.add(entry.identity());
    List<String> missing = new ArrayList<String>();
    for (String identity : overrides.keySet()) {
      if (!identities.contains(identity)) missing.add(identity);
    }
    if (!missing.isEmpty()) {
      throw new IllegalStateException("Native bridge ABI override(s) no longer match compiled Java: " + missing);
    }
  }

  void validateMinimality(List<NativeBridgeModel.Entry> entries) {
    Set<String> derived = new LinkedHashSet<String>();
    for (NativeBridgeModel.Entry entry : entries) {
      derived.add(effectiveSymbol(entry));
    }

    List<String> redundant = new ArrayList<String>();
    for (String symbol : explicitBridgeSymbols) {
      if (derived.contains(symbol)) redundant.add("bridge " + symbol);
    }
    for (String symbol : headerOnlySymbols) {
      if (derived.contains(symbol)) redundant.add("header " + symbol);
    }
    if (!redundant.isEmpty()) {
      throw new IllegalStateException(
          "Native bridge compatibility contains Java-derivable entries: " + redundant);
    }
  }

  private static void requireParts(Path path, int lineNumber, String[] parts, int expected) {
    if (parts.length != expected) {
      throw new IllegalArgumentException(path + ":" + lineNumber + ": expected " + expected
          + " tab-separated fields, got " + parts.length);
    }
  }

  private static void putUnique(Path path, int lineNumber, Map<String, String> values,
      String key, String value) {
    String previous = values.putIfAbsent(key, value);
    if (previous != null && !previous.equals(value)) {
      throw new IllegalArgumentException(path + ":" + lineNumber + ": conflicting value for " + key
          + ": " + previous + " vs " + value);
    }
  }
}
