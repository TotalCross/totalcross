// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class IOSCertDateRuntimeParametersTest {
  @Test
  void loadsAndIsolatesIosCertDateAcrossParameterSets() {
    Settings.loadDeploymentParameters(parameters("20300102T03:04:05"));
    assertEquals("20300102T03:04:05", Settings.iosCertDate.toIso8601());

    Settings.loadDeploymentParameters(parameters(null));
    assertNull(Settings.iosCertDate);

    Settings.loadDeploymentParameters(parameters("20400203T04:05:06"));
    assertEquals("20400203T04:05:06", Settings.iosCertDate.toIso8601());
  }

  @Test
  void ignoresInvalidOrMissingIosCertDate() {
    Settings.loadDeploymentParameters(parameters("not-a-date"));
    assertNull(Settings.iosCertDate);

    Settings.loadDeploymentParameters(null);
    assertNull(Settings.iosCertDate);
  }

  private static byte[] parameters(String iosCertDate) {
    String value = iosCertDate == null ? "applicationId=abcd\n" : "iosCertDate=" + iosCertDate + "\n";
    return value.getBytes(StandardCharsets.UTF_8);
  }
}
