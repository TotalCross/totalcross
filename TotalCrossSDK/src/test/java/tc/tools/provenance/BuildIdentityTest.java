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

      String metadata = new String(BuildIdentity.tczMetadata(), StandardCharsets.UTF_8);
      assertEquals(
          "identityVersion=1\n"
              + "buildId=" + "a".repeat(64) + "\n"
              + "sourceTree=" + "b".repeat(40) + "\n",
          metadata);
    });
  }

  @Test
  void rejectsMissingIdentityProperties() {
    withProperties(() -> {
      System.clearProperty("totalcross.build.identity.version");
      System.clearProperty("totalcross.build.id");
      System.clearProperty("totalcross.source.tree");

      assertThrows(IllegalStateException.class, BuildIdentity::tczMetadata);
    });
  }

  private static void withProperties(Runnable assertion) {
    String version = System.getProperty("totalcross.build.identity.version");
    String buildId = System.getProperty("totalcross.build.id");
    String sourceTree = System.getProperty("totalcross.source.tree");
    try {
      assertion.run();
    } finally {
      restore("totalcross.build.identity.version", version);
      restore("totalcross.build.id", buildId);
      restore("totalcross.source.tree", sourceTree);
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
