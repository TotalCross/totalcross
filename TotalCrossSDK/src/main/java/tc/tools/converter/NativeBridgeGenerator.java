// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Generates native-bridge metadata from compiled TotalCross device classes. */
public final class NativeBridgeGenerator {
  private static final Pattern HEADER_SYMBOL =
      Pattern.compile("TC_API\\s+void\\s+([A-Za-z0-9_]+)\\s*\\(NMParams\\s+p\\)");
  private static final Pattern REGISTRATION_SYMBOL =
      Pattern.compile("hashCode\\(\\\"([^\\\"]+)\\\"\\)");

  private NativeBridgeGenerator() {
  }

  public static void main(String[] args) throws Exception {
    Arguments parsed = Arguments.parse(args);
    NativeBridgeModel.Result model = NativeBridgeModel.fromPaths(parsed.classRoots);
    NativeBridgeCompatibility compatibility = NativeBridgeCompatibility.read(parsed.compatibility);
    compatibility.validateOverrides(model.entries);
    if (parsed.strict) {
      compatibility.validateMinimality(model.entries);
    }
    List<Collision> collisions = effectiveCollisions(model.entries, compatibility);
    if (parsed.strict && !collisions.isEmpty()) {
      throw new IllegalStateException(formatCollisions(collisions));
    }
    int sourceDefinitionsValidated = 0;
    if (parsed.strict && parsed.nativeSourceRoot != null) {
      sourceDefinitionsValidated = NativeBridgeNativeSources.validate(
          parsed.nativeSourceRoot, model, compatibility);
    }
    writeOutputs(parsed.output, model, compatibility, collisions);
    if (parsed.legacyRoot != null) {
      writeLegacyComparison(parsed.output.resolve("legacy-comparison.txt"),
          parsed.legacyRoot, model, compatibility, collisions);
    }
    System.out.println("Generated native bridge model: " + model.entries.size()
        + " Java entries, " + collisions.size() + " effective collision(s) at "
        + parsed.output.toAbsolutePath()
        + (parsed.strict && parsed.nativeSourceRoot != null
            ? "; validated " + sourceDefinitionsValidated + " native source definitions" : ""));
  }

  static void writeOutputs(Path output, NativeBridgeModel.Result model,
      NativeBridgeCompatibility compatibility, List<Collision> collisions) throws IOException {
    Files.createDirectories(output);
    writeManifest(output.resolve("native-bridges.json"), model, compatibility, collisions);
    writeHeader(output.resolve("NativeMethods.generated.h"), model, compatibility);
    writeRegistrations(output.resolve("nativeProcAddressesTC.generated.inc"), model, compatibility);
  }

  private static void writeManifest(Path output, NativeBridgeModel.Result model,
      NativeBridgeCompatibility compatibility, List<Collision> collisions) throws IOException {
    List<String> lines = new ArrayList<String>();
    lines.add("{");
    lines.add("  \"generated\": true,");
    lines.add("  \"entries\": [");
    for (int i = 0; i < model.entries.size(); i++) {
      NativeBridgeModel.Entry entry = model.entries.get(i);
      String effective = compatibility.effectiveSymbol(entry);
      String comma = i + 1 == model.entries.size() ? "" : ",";
      lines.add("    {\"sourceOwner\":\"" + json(entry.sourceOwner)
          + "\",\"deployedOwner\":\"" + json(entry.deployedOwner)
          + "\",\"module\":\"" + json(compatibility.moduleFor(entry))
          + "\",\"name\":\"" + json(entry.deployedName)
          + "\",\"descriptor\":\"" + json(entry.descriptor)
          + "\",\"derivedSymbol\":\"" + json(entry.symbol)
          + "\",\"symbol\":\"" + json(effective)
          + "\",\"kind\":\"" + json(entry.kind) + "\"}" + comma);
    }
    lines.add("  ],");
    lines.add("  \"compatibilityBridges\": [");
    List<String> compatibilityBridges = new ArrayList<String>(compatibility.bridgeSymbols());
    Collections.sort(compatibilityBridges);
    for (int i = 0; i < compatibilityBridges.size(); i++) {
      String symbol = compatibilityBridges.get(i);
      String guard = compatibility.guardFor(symbol);
      String comma = i + 1 == compatibilityBridges.size() ? "" : ",";
      lines.add("    {\"symbol\":\"" + json(symbol) + "\",\"guard\":"
          + (guard == null ? "null" : "\"" + json(guard) + "\"") + "}" + comma);
    }
    lines.add("  ],");
    lines.add("  \"collisions\": [");
    for (int i = 0; i < collisions.size(); i++) {
      Collision collision = collisions.get(i);
      String comma = i + 1 == collisions.size() ? "" : ",";
      lines.add("    {\"module\":\"" + json(collision.module)
          + "\",\"symbol\":\"" + json(collision.symbol)
          + "\",\"first\":\"" + json(collision.first)
          + "\",\"second\":\"" + json(collision.second) + "\"}" + comma);
    }
    lines.add("  ]");
    lines.add("}");
    Files.write(output, lines, StandardCharsets.UTF_8);
  }

  private static void writeHeader(Path output, NativeBridgeModel.Result model,
      NativeBridgeCompatibility compatibility) throws IOException {
    Set<String> symbols = effectiveCoreJavaSymbols(model, compatibility);
    symbols.addAll(compatibility.bridgeSymbols());
    symbols.addAll(compatibility.headerOnlySymbols());
    List<String> sorted = new ArrayList<String>(symbols);
    Collections.sort(sorted);

    List<String> lines = new ArrayList<String>();
    lines.add("// Generated from compiled device Java classes. Do not edit.");
    lines.add("#ifndef TC_NATIVE_METHODS_GENERATED_H");
    lines.add("#define TC_NATIVE_METHODS_GENERATED_H");
    lines.add("");
    lines.add("#ifdef __cplusplus");
    lines.add("extern \"C\" {");
    lines.add("#endif");
    lines.add("");
    for (String symbol : sorted) {
      lines.add("TC_API void " + symbol + "(NMParams p);");
    }
    lines.add("");
    lines.add("#ifdef __cplusplus");
    lines.add("}");
    lines.add("#endif");
    lines.add("");
    lines.add("#endif");
    Files.write(output, lines, StandardCharsets.UTF_8);
  }

  private static void writeRegistrations(Path output, NativeBridgeModel.Result model,
      NativeBridgeCompatibility compatibility) throws IOException {
    Set<String> symbols = effectiveCoreJavaSymbols(model, compatibility);
    symbols.addAll(compatibility.bridgeSymbols());

    List<String> unguarded = new ArrayList<String>();
    Map<String, List<String>> guarded = new TreeMap<String, List<String>>();
    for (String symbol : symbols) {
      String guard = compatibility.guardFor(symbol);
      if (guard == null) {
        unguarded.add(symbol);
      } else {
        guarded.computeIfAbsent(guard, unused -> new ArrayList<String>()).add(symbol);
      }
    }
    Collections.sort(unguarded);
    for (List<String> values : guarded.values()) Collections.sort(values);

    List<String> lines = new ArrayList<String>();
    lines.add("/* Generated from compiled device Java classes. Do not edit. */");
    for (String symbol : unguarded) lines.add(registration(symbol));
    for (Map.Entry<String, List<String>> group : guarded.entrySet()) {
      lines.add("#if defined(" + group.getKey() + ")");
      for (String symbol : group.getValue()) lines.add(registration(symbol));
      lines.add("#endif");
    }
    Files.write(output, lines, StandardCharsets.UTF_8);
  }

  private static String registration(String symbol) {
    return "htPutPtr(&htNativeProcAddresses, hashCode(\"" + symbol + "\"), &" + symbol + ");";
  }

  private static Set<String> effectiveCoreJavaSymbols(NativeBridgeModel.Result model,
      NativeBridgeCompatibility compatibility) {
    Set<String> values = new LinkedHashSet<String>();
    for (NativeBridgeModel.Entry entry : model.entries) {
      if ("core".equals(compatibility.moduleFor(entry))) {
        values.add(compatibility.effectiveSymbol(entry));
      }
    }
    return values;
  }

  private static List<Collision> effectiveCollisions(List<NativeBridgeModel.Entry> entries,
      NativeBridgeCompatibility compatibility) {
    Map<String, Map<String, String>> firstByModuleAndSymbol = new LinkedHashMap<String, Map<String, String>>();
    List<Collision> collisions = new ArrayList<Collision>();
    for (NativeBridgeModel.Entry entry : entries) {
      String module = compatibility.moduleFor(entry);
      String symbol = compatibility.effectiveSymbol(entry);
      String identity = entry.identity();
      Map<String, String> firstBySymbol = firstByModuleAndSymbol.computeIfAbsent(
          module, unused -> new LinkedHashMap<String, String>());
      String previous = firstBySymbol.putIfAbsent(symbol, identity);
      if (previous != null && !previous.equals(identity)) {
        collisions.add(new Collision(module, symbol, previous, identity));
      }
    }
    return collisions;
  }

  private static String formatCollisions(List<Collision> collisions) {
    StringBuilder message = new StringBuilder("Native bridge symbol collision(s):");
    for (Collision collision : collisions) {
        message.append(System.lineSeparator()).append(" - [").append(collision.module).append("] ")
            .append(collision.symbol)
          .append(": ").append(collision.first).append(" <> ").append(collision.second);
    }
    return message.toString();
  }

  private static void writeLegacyComparison(Path output, Path legacyRoot,
      NativeBridgeModel.Result model, NativeBridgeCompatibility compatibility,
      List<Collision> collisions) throws IOException {
    Path header = legacyRoot.resolve("src/nm/NativeMethods.h");
    Path registrations = legacyRoot.resolve("src/init/nativeProcAddressesTC.c");
    Set<String> generatedHeader = effectiveCoreJavaSymbols(model, compatibility);
    generatedHeader.addAll(compatibility.bridgeSymbols());
    generatedHeader.addAll(compatibility.headerOnlySymbols());
    Set<String> generatedRegistrations = effectiveCoreJavaSymbols(model, compatibility);
    generatedRegistrations.addAll(compatibility.bridgeSymbols());
    Set<String> headerSymbols = readSymbols(header, HEADER_SYMBOL);
    Set<String> registrationSymbols = readSymbols(registrations, REGISTRATION_SYMBOL);
    registrationSymbols.remove("getMainContext");

    List<String> lines = new ArrayList<String>();
    lines.add("# Shadow comparison only; differences are expected until classified.");
    lines.add("generatedHeader=" + generatedHeader.size());
    lines.add("generatedRegistrations=" + generatedRegistrations.size());
    lines.add("legacyHeader=" + headerSymbols.size());
    lines.add("legacyRegistrations=" + registrationSymbols.size());
    appendDifference(lines, "generated-not-in-header", generatedHeader, headerSymbols);
    appendDifference(lines, "header-not-generated", headerSymbols, generatedHeader);
    appendDifference(lines, "generated-not-registered", generatedRegistrations, registrationSymbols);
    appendDifference(lines, "registration-not-generated", registrationSymbols, generatedRegistrations);
    lines.add("");
    lines.add("[collisions]");
    if (collisions.isEmpty()) {
      lines.add("(none)");
    } else {
      for (Collision collision : collisions) {
        lines.add(collision.module + "\t" + collision.symbol + "\t" + collision.first + "\t" + collision.second);
      }
    }
    Files.write(output, lines, StandardCharsets.UTF_8);
  }

  private static Set<String> readSymbols(Path file, Pattern pattern) throws IOException {
    Set<String> values = new LinkedHashSet<String>();
    if (!Files.isRegularFile(file)) return values;
    for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
      Matcher matcher = pattern.matcher(line);
      while (matcher.find()) values.add(matcher.group(1));
    }
    return values;
  }

  private static void appendDifference(List<String> lines, String title,
      Set<String> left, Set<String> right) {
    List<String> difference = new ArrayList<String>();
    for (String value : left) if (!right.contains(value)) difference.add(value);
    Collections.sort(difference);
    lines.add("");
    lines.add("[" + title + "] count=" + difference.size());
    lines.addAll(difference);
  }

  private static String json(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  static final class Collision {
    final String module;
    final String symbol;
    final String first;
    final String second;

    Collision(String module, String symbol, String first, String second) {
      this.module = module;
      this.symbol = symbol;
      this.first = first;
      this.second = second;
    }
  }

  private static final class Arguments {
    final Path output;
    final Path legacyRoot;
    final Path compatibility;
    final Path nativeSourceRoot;
    final boolean strict;
    final List<Path> classRoots;

    Arguments(Path output, Path legacyRoot, Path compatibility, Path nativeSourceRoot,
        boolean strict, List<Path> classRoots) {
      this.output = output;
      this.legacyRoot = legacyRoot;
      this.compatibility = compatibility;
      this.nativeSourceRoot = nativeSourceRoot;
      this.strict = strict;
      this.classRoots = classRoots;
    }

    static Arguments parse(String[] args) {
      Path output = Paths.get("build/generated/native-bridges");
      Path legacyRoot = null;
      Path compatibility = null;
      Path nativeSourceRoot = null;
      boolean strict = false;
      List<Path> roots = new ArrayList<Path>();
      for (int i = 0; i < args.length; i++) {
        if ("--output".equals(args[i])) {
          output = Paths.get(args[++i]);
        } else if ("--legacy-root".equals(args[i])) {
          legacyRoot = Paths.get(args[++i]);
        } else if ("--compat".equals(args[i])) {
          compatibility = Paths.get(args[++i]);
        } else if ("--native-source-root".equals(args[i])) {
          nativeSourceRoot = Paths.get(args[++i]);
        } else if ("--strict".equals(args[i])) {
          strict = true;
        } else {
          roots.add(Paths.get(args[i]));
        }
      }
      if (roots.isEmpty()) {
        throw new IllegalArgumentException("Pass one or more compiled class directories.");
      }
      return new Arguments(output, legacyRoot, compatibility, nativeSourceRoot, strict, roots);
    }
  }
}
