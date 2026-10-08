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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
    writeOutputs(parsed.output, model);
    if (parsed.legacyRoot != null) {
      writeLegacyComparison(parsed.output.resolve("legacy-comparison.txt"), parsed.legacyRoot, model);
    }
    System.out.println("Generated native bridge model: " + model.entries.size()
        + " entries, " + model.collisions.size() + " collision(s) at " + parsed.output.toAbsolutePath());
  }

  static void writeOutputs(Path output, NativeBridgeModel.Result model) throws IOException {
    Files.createDirectories(output);
    writeManifest(output.resolve("native-bridges.json"), model);
    writeHeader(output.resolve("NativeMethods.generated.h"), model);
    writeRegistrations(output.resolve("nativeProcAddressesTC.generated.inc"), model);
  }

  private static void writeManifest(Path output, NativeBridgeModel.Result model) throws IOException {
    List<String> lines = new ArrayList<String>();
    lines.add("{");
    lines.add("  \"generated\": true,");
    lines.add("  \"entries\": [");
    for (int i = 0; i < model.entries.size(); i++) {
      NativeBridgeModel.Entry entry = model.entries.get(i);
      String comma = i + 1 == model.entries.size() ? "" : ",";
      lines.add("    {\"sourceOwner\":\"" + json(entry.sourceOwner)
          + "\",\"deployedOwner\":\"" + json(entry.deployedOwner)
          + "\",\"name\":\"" + json(entry.deployedName)
          + "\",\"descriptor\":\"" + json(entry.descriptor)
          + "\",\"symbol\":\"" + json(entry.symbol)
          + "\",\"kind\":\"" + json(entry.kind) + "\"}" + comma);
    }
    lines.add("  ],");
    lines.add("  \"collisions\": [");
    for (int i = 0; i < model.collisions.size(); i++) {
      NativeBridgeModel.Collision collision = model.collisions.get(i);
      String comma = i + 1 == model.collisions.size() ? "" : ",";
      lines.add("    {\"symbol\":\"" + json(collision.symbol)
          + "\",\"first\":\"" + json(collision.first.identity())
          + "\",\"second\":\"" + json(collision.second.identity()) + "\"}" + comma);
    }
    lines.add("  ]");
    lines.add("}");
    Files.write(output, lines, StandardCharsets.UTF_8);
  }

  private static void writeHeader(Path output, NativeBridgeModel.Result model) throws IOException {
    List<String> lines = new ArrayList<String>();
    lines.add("// Generated from compiled device Java classes. Do not edit.");
    lines.add("#ifndef TC_NATIVE_METHODS_GENERATED_H");
    lines.add("#define TC_NATIVE_METHODS_GENERATED_H");
    lines.add("");
    lines.add("#ifdef __cplusplus");
    lines.add("extern \"C\" {");
    lines.add("#endif");
    lines.add("");
    Set<String> emitted = new LinkedHashSet<String>();
    for (NativeBridgeModel.Entry entry : model.entries) {
      if (emitted.add(entry.symbol)) {
        lines.add("TC_API void " + entry.symbol + "(NMParams p);");
      }
    }
    lines.add("");
    lines.add("#ifdef __cplusplus");
    lines.add("}");
    lines.add("#endif");
    lines.add("");
    lines.add("#endif");
    Files.write(output, lines, StandardCharsets.UTF_8);
  }

  private static void writeRegistrations(Path output, NativeBridgeModel.Result model) throws IOException {
    List<String> lines = new ArrayList<String>();
    lines.add("/* Generated from compiled device Java classes. Do not edit. */");
    Set<String> emitted = new LinkedHashSet<String>();
    for (NativeBridgeModel.Entry entry : model.entries) {
      if (emitted.add(entry.symbol)) {
        lines.add("htPutPtr(&htNativeProcAddresses, hashCode(\"" + entry.symbol
            + "\"), &" + entry.symbol + ");");
      }
    }
    Files.write(output, lines, StandardCharsets.UTF_8);
  }

  private static void writeLegacyComparison(Path output, Path legacyRoot,
      NativeBridgeModel.Result model) throws IOException {
    Path header = legacyRoot.resolve("src/nm/NativeMethods.h");
    Path registrations = legacyRoot.resolve("src/init/nativeProcAddressesTC.c");
    Set<String> generated = new LinkedHashSet<String>();
    for (NativeBridgeModel.Entry entry : model.entries) generated.add(entry.symbol);
    Set<String> headerSymbols = readSymbols(header, HEADER_SYMBOL);
    Set<String> registrationSymbols = readSymbols(registrations, REGISTRATION_SYMBOL);
    registrationSymbols.remove("getMainContext");

    List<String> lines = new ArrayList<String>();
    lines.add("# Shadow comparison only; differences are expected until classified.");
    lines.add("generated=" + generated.size());
    lines.add("legacyHeader=" + headerSymbols.size());
    lines.add("legacyRegistrations=" + registrationSymbols.size());
    appendDifference(lines, "generated-not-in-header", generated, headerSymbols);
    appendDifference(lines, "header-not-generated", headerSymbols, generated);
    appendDifference(lines, "generated-not-registered", generated, registrationSymbols);
    appendDifference(lines, "registration-not-generated", registrationSymbols, generated);
    lines.add("");
    lines.add("[collisions]");
    if (model.collisions.isEmpty()) {
      lines.add("(none)");
    } else {
      for (NativeBridgeModel.Collision collision : model.collisions) {
        lines.add(collision.symbol + "\t" + collision.first.identity()
            + "\t" + collision.second.identity());
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

  private static final class Arguments {
    final Path output;
    final Path legacyRoot;
    final List<Path> classRoots;

    Arguments(Path output, Path legacyRoot, List<Path> classRoots) {
      this.output = output;
      this.legacyRoot = legacyRoot;
      this.classRoots = classRoots;
    }

    static Arguments parse(String[] args) {
      Path output = Paths.get("build/generated/native-bridges");
      Path legacyRoot = null;
      List<Path> roots = new ArrayList<Path>();
      for (int i = 0; i < args.length; i++) {
        if ("--output".equals(args[i])) {
          output = Paths.get(args[++i]);
        } else if ("--legacy-root".equals(args[i])) {
          legacyRoot = Paths.get(args[++i]);
        } else {
          roots.add(Paths.get(args[i]));
        }
      }
      if (roots.isEmpty()) {
        throw new IllegalArgumentException("Pass one or more compiled class directories.");
      }
      return new Arguments(output, legacyRoot, roots);
    }
  }
}
