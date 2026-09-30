// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import tc.tools.converter.runtimeconfig.RuntimeConfigurationParser;
import tc.tools.converter.runtimeconfig.ImageRuntimeConfigurationParser;
import totalcross.sys.runtime.ImageRuntimeConfigurationStartup;
import totalcross.sys.runtime.RuntimeConfigurationStartup;
import totalcross.sys.runtime.RuntimeSelector;
import totalcross.sys.runtime.RuntimeConfigurationFeatureBridge.FeatureRule;
import totalcross.ui.image.ImageStorageProfile;

/** Reads selector declarations from simulator class resources before app class initialization. */
final class RuntimeConfigurationSimulator {
  private RuntimeConfigurationSimulator() {
  }

  static void initialize(ClassLoader classLoader, String className) throws IOException {
    String osName = System.getProperty("os.name");
    String osArch = System.getProperty("os.arch");
    List<RuntimeSelector> selectors = null;
    List<FeatureRule<ImageStorageProfile>> imageRules = null;
    if (className != null) {
      String normalized = Launcher.normalizeMainWindowClassName(className);
      String resourceName = normalized.replace('.', '/') + ".class";
      InputStream resource = classLoader == null ? ClassLoader.getSystemResourceAsStream(resourceName)
          : classLoader.getResourceAsStream(resourceName);
      if (resource != null) {
        try {
          byte[] classBytes = readAll(resource);
          selectors = RuntimeConfigurationParser.parse(classBytes, normalized);
          imageRules = ImageRuntimeConfigurationParser.parse(classBytes, normalized);
        } finally {
          resource.close();
        }
      }
    }
    RuntimeConfigurationStartup.initializeForSimulator(selectors, osName, osArch);
    ImageRuntimeConfigurationStartup.initializeForSimulator(imageRules);
  }

  private static byte[] readAll(InputStream input) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int length;
    while ((length = input.read(buffer)) != -1) {
      output.write(buffer, 0, length);
    }
    return output.toByteArray();
  }
}
