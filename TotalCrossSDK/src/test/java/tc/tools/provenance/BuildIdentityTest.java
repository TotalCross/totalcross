// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.provenance;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuildIdentityTest {
  @Test
  void writesReservedTczMetadataFromBuildProperties() {
    withProperties(() -> {
      System.setProperty("totalcross.build.identity.version", "1");
      System.setProperty("totalcross.build.id", "a".repeat(64));
      System.setProperty("totalcross.source.tree", "b".repeat(40));
      System.setProperty("totalcross.tcz.format", "200");
      System.setProperty("totalcross.converter.abi", "1");
      System.setProperty("totalcross.runtime.abi", "1");

      String metadata = new String(BuildIdentity.tczMetadata(), StandardCharsets.UTF_8);
      assertEquals(
          "identityVersion=1\n"
              + "buildId=" + "a".repeat(64) + "\n"
              + "sourceTree=" + "b".repeat(40) + "\n"
              + "tczFormat=200\n"
              + "converterAbi=1\n"
              + "runtimeAbi=1\n",
          metadata);
    });
  }

  @Test
  void rejectsMissingIdentityProperties() {
    withProperties(() -> {
      System.clearProperty("totalcross.build.identity.version");
      System.clearProperty("totalcross.build.id");
      System.clearProperty("totalcross.source.tree");
      System.clearProperty("totalcross.tcz.format");
      System.clearProperty("totalcross.converter.abi");
      System.clearProperty("totalcross.runtime.abi");

      assertThrows(IllegalStateException.class, BuildIdentity::tczMetadata);
    });
  }

  @Test
  void rejectsMissingCompatibilityProperties() {
    withProperties(() -> {
      System.setProperty("totalcross.build.identity.version", "1");
      System.setProperty("totalcross.build.id", "a".repeat(64));
      System.setProperty("totalcross.source.tree", "b".repeat(40));
      System.setProperty("totalcross.tcz.format", "200");
      System.setProperty("totalcross.converter.abi", "1");
      System.clearProperty("totalcross.runtime.abi");

      assertThrows(IllegalStateException.class, BuildIdentity::tczMetadata);
    });
  }

  private static void withProperties(Runnable assertion) {
    String version = System.getProperty("totalcross.build.identity.version");
    String buildId = System.getProperty("totalcross.build.id");
    String sourceTree = System.getProperty("totalcross.source.tree");
    String tczFormat = System.getProperty("totalcross.tcz.format");
    String converterAbi = System.getProperty("totalcross.converter.abi");
    String runtimeAbi = System.getProperty("totalcross.runtime.abi");
    try {
      assertion.run();
    } finally {
      restore("totalcross.build.identity.version", version);
      restore("totalcross.build.id", buildId);
      restore("totalcross.source.tree", sourceTree);
      restore("totalcross.tcz.format", tczFormat);
      restore("totalcross.converter.abi", converterAbi);
      restore("totalcross.runtime.abi", runtimeAbi);
    }
  }

  private static void restore(String name, String value) {
    if (value == null) {
      System.clearProperty(name);
    } else {
      System.setProperty(name, value);
    }
  }
}
