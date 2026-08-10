// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tc.Deploy;
import tc.tools.converter.J2TC;
import tc.tools.converter.metadata.CompilationMetadata.CallSiteMetadata;
import tc.tools.converter.metadata.CompilationMetadata.ClassMetadata;
import tc.tools.converter.metadata.CompilationMetadata.MethodMetadata;
import tc.tools.deployer.DeploySettings;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

/** Captures the fixed before/after workload used by the TCM boundary ExecPlan. */
public final class TcmBoundaryBaselineMain {
  private static final int BUFFER_SIZE = 16 * 1024;

  private TcmBoundaryBaselineMain() {
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 5) {
      throw new IllegalArgumentException("usage: <input.class> <none|aot> <output-dir> <warmups> <samples>");
    }
    String input = args[0];
    String mode = args[1].toLowerCase();
    Path outputDir = Paths.get(args[2]);
    int warmups = Integer.parseInt(args[3]);
    int sampleCount = Integer.parseInt(args[4]);
    if (!"none".equals(mode) && !"aot".equals(mode)) {
      throw new IllegalArgumentException("unsupported mode " + mode);
    }
    Files.createDirectories(outputDir);

    Summary expected = null;
    List<Long> samples = new ArrayList<Long>();
    for (int i = 0; i < warmups + sampleCount; i++) {
      resetConversionRun();
      DeploySettings.tcmMode = DeploySettings.TcmMode.NONE;
      long started = System.nanoTime();
      new Deploy(deployArgs(input, mode));
      long elapsed = System.nanoTime() - started;
      Summary current = Summary.from(J2TC.getCompilationMetadata());
      if (expected == null) {
        expected = current;
      } else if (!expected.equals(current)) {
        throw new IllegalStateException("metadata counts changed between identical conversions");
      }
      if (i >= warmups) {
        samples.add(Long.valueOf(elapsed));
      }
    }

    String fixtureHash = null;
    if ("aot".equals(mode)) {
      Path sidecar = Paths.get(DeploySettings.tcmFileName).toAbsolutePath().normalize();
      Path fixture = outputDir.resolve("baseline-v1.tcm");
      Files.copy(sidecar, fixture, StandardCopyOption.REPLACE_EXISTING);
      fixtureHash = hex(sha256(fixture));
    }
    Path output = outputDir.resolve("baseline-" + mode + ".json");
    Files.write(output, json(mode, warmups, samples, expected, fixtureHash).getBytes(StandardCharsets.UTF_8));
    System.out.println("TCM_BOUNDARY_BASELINE mode=" + mode + " samples=" + sampleCount + " output=" + output);
  }

  private static String[] deployArgs(String input, String mode) {
    return "aot".equals(mode) ? new String[] { input, "/tcm", "aot" } : new String[] { input };
  }

  private static void resetConversionRun() throws ReflectiveOperationException {
    J2TC.htAddedClasses = new Hashtable(0xff);
    J2TC.htExcludedClasses = new Hashtable(0xff);
    J2TC.callForName = new Vector(4);
    J2TC.notResolvedForNameFound = false;
    Field visited = J2TC.class.getDeclaredField("htVisited");
    visited.setAccessible(true);
    visited.set(null, new Hashtable(1000));
  }

  private static String json(String mode, int warmups, List<Long> samples, Summary summary, String fixtureHash) {
    List<Long> sorted = new ArrayList<Long>(samples);
    Collections.sort(sorted);
    long total = 0;
    for (Long sample : samples) {
      total += sample.longValue();
    }
    StringBuilder out = new StringBuilder(1024);
    out.append("{\n  \"mode\": \"").append(mode).append("\",\n");
    out.append("  \"warmups\": ").append(warmups).append(",\n");
    out.append("  \"sampleNanos\": ").append(samples).append(",\n");
    out.append("  \"minNanos\": ").append(sorted.get(0)).append(",\n");
    out.append("  \"medianNanos\": ").append(sorted.get(sorted.size() / 2)).append(",\n");
    out.append("  \"meanNanos\": ").append(total / samples.size()).append(",\n");
    out.append("  \"metadata\": ").append(summary.json()).append(",\n");
    out.append("  \"wire\": {\"major\": 1, \"minor\": 0, \"requiredBit\": 32768,")
        .append(" \"sectionIds\": [1,2,3,4,5,6,7,8,9,10],")
        .append(" \"nativeKinds\": [0,1,2], \"invokeKinds\": [0,1,2,3,4,5,6],")
        .append(" \"syntheticKinds\": [0,1,2]},\n");
    out.append("  \"fixtureSha256\": ")
        .append(fixtureHash == null ? "null" : "\"" + fixtureHash + "\"").append("\n}\n");
    return out.toString();
  }

  private static byte[] sha256(Path path) throws IOException, NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] buffer = new byte[BUFFER_SIZE];
    try (InputStream input = Files.newInputStream(path)) {
      int count;
      while ((count = input.read(buffer)) >= 0) {
        if (count > 0) {
          digest.update(buffer, 0, count);
        }
      }
    }
    return digest.digest();
  }

  private static String hex(byte[] bytes) {
    StringBuilder result = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) {
      result.append(String.format("%02x", value & 0xff));
    }
    return result.toString();
  }

  private static final class Summary {
    int classes;
    int fields;
    int methods;
    int bytecodeSites;
    int calls;
    int frames;
    int synthetic;
    int unresolvedCalls;
    int constructors;
    int interfaces;
    int inheritedOwners;

    static Summary from(CompilationMetadata metadata) {
      Summary result = new Summary();
      result.classes = metadata.classes.size();
      for (ClassMetadata type : metadata.classes) {
        result.fields += type.fields.size();
        result.methods += type.methods.size();
        result.synthetic += type.syntheticOrigins.size();
        for (MethodMetadata method : type.methods) {
          result.bytecodeSites += method.origins.size();
          result.calls += method.callSites.size();
          result.frames += method.verificationFrames.size();
          for (CallSiteMetadata call : method.callSites) {
            if (call.resolvedDeclarationOwner == null) result.unresolvedCalls++;
            if ("<init>".equals(call.name)) result.constructors++;
            if (call.invokeKind == CompilationMetadata.InvokeKind.INTERFACE) result.interfaces++;
            if (call.symbolicOwner != null && call.resolvedDeclarationOwner != null
                && !call.symbolicOwner.equals(call.resolvedDeclarationOwner)) result.inheritedOwners++;
          }
        }
      }
      return result;
    }

    String json() {
      return "{\"classes\":" + classes + ",\"fields\":" + fields + ",\"methods\":" + methods
          + ",\"bytecodeSites\":" + bytecodeSites + ",\"originRanges\":" + bytecodeSites
          + ",\"callSites\":" + calls + ",\"stackMapFrames\":" + frames + ",\"syntheticOrigins\":"
          + synthetic + ",\"unresolvedCalls\":" + unresolvedCalls + ",\"constructors\":" + constructors
          + ",\"interfaceCalls\":" + interfaces + ",\"inheritedOwners\":" + inheritedOwners + "}";
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof Summary && json().equals(((Summary) other).json());
    }

    @Override
    public int hashCode() {
      return json().hashCode();
    }
  }
}
