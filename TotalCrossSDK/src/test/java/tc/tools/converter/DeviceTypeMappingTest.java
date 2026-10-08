// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DeviceTypeMappingTest {
  @Test
  void mapsCompatibilityReplacementOwnerExactlyLikeDeploy() {
    assertEquals("java/lang/System", DeviceTypeMapping.deployedOwner("jdkcompat/lang/System4D"));
    assertEquals("totalcross/net/ServerSocket",
        DeviceTypeMapping.deployedOwner("totalcross/net/ServerSocket4D"));
  }

  @Test
  void removesReplacementSuffixFromNestedOwners() {
    assertEquals("totalcross/Foo$Bar", DeviceTypeMapping.removeReplacementSuffix("totalcross/Foo4D$Bar"));
  }
}
