// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeployDownloadToolsTest {
  @Test
  void recognizesDownloadToolsAsAStandaloneCommand() {
    assertTrue(Deploy.isDownloadToolsCommand(new String[] { "-download-tools" }));
    assertTrue(Deploy.isDownloadToolsCommand(new String[] { "-DOWNLOAD-TOOLS" }));
    assertFalse(Deploy.isDownloadToolsCommand(new String[] { "Example" }));
    assertFalse(Deploy.isDownloadToolsCommand(new String[0]));
    assertFalse(Deploy.isDownloadToolsCommand(null));
  }
}
