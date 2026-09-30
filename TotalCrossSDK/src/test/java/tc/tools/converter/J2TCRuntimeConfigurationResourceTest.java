// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import totalcross.util.Vector;
import totalcross.util.zip.TCZ;

class J2TCRuntimeConfigurationResourceTest {
  @Test
  void rejectsImageMetadataResourceCollisionCaseInsensitively() throws Exception {
    Vector entries = new Vector(1);
    entries.addElement(new TCZ.Entry(new byte[] { 1 }, "TC.IMAGERUNTIMECONFIG", 1));

    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> J2TC.validateReservedConfigurationResources(entries));

    assertTrue(error.getMessage().contains("tc.imageruntimeconfig"));
  }
}
