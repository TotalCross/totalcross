// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.provenance;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/** Reads the build identity embedded in the SDK artifact executing the converter. */
public final class BuildIdentity {
  public static final String TCZ_ENTRY_NAME = "META-INF/totalcross-build.properties";

  private static final String BUILD_ID = "TotalCross-Build-ID";
  private static final String IDENTITY_VERSION = "TotalCross-Build-Identity-Version";
  private static final String SOURCE_TREE = "TotalCross-Source-Tree";
  private static final String TCZ_FORMAT = "TotalCross-TCZ-Format";
  private static final String CONVERTER_ABI = "TotalCross-Converter-ABI";
  private static final String RUNTIME_ABI = "TotalCross-Runtime-ABI";

  private BuildIdentity() {
  }

  public static String buildId() {
    return attributeOrProperty(BUILD_ID, "totalcross.build.id");
  }

  public static String sourceTree() {
    return attributeOrProperty(SOURCE_TREE, "totalcross.source.tree");
  }

  public static String identityVersion() {
    return attributeOrProperty(IDENTITY_VERSION, "totalcross.build.identity.version");
  }

  public static String tczFormat() {
    return attributeOrProperty(TCZ_FORMAT, "totalcross.tcz.format");
  }

  public static String converterAbi() {
    return attributeOrProperty(CONVERTER_ABI, "totalcross.converter.abi");
  }

  public static String runtimeAbi() {
    return attributeOrProperty(RUNTIME_ABI, "totalcross.runtime.abi");
  }

  public static byte[] tczMetadata() {
    String value = "identityVersion=" + required(identityVersion(), IDENTITY_VERSION) + "\n"
        + "buildId=" + required(buildId(), BUILD_ID) + "\n"
        + "sourceTree=" + required(sourceTree(), SOURCE_TREE) + "\n"
        + "tczFormat=" + required(tczFormat(), TCZ_FORMAT) + "\n"
        + "converterAbi=" + required(converterAbi(), CONVERTER_ABI) + "\n"
        + "runtimeAbi=" + required(runtimeAbi(), RUNTIME_ABI) + "\n";
    return value.getBytes(StandardCharsets.UTF_8);
  }

  private static String required(String value, String name) {
    if (value == null || value.isEmpty()) {
      throw new IllegalStateException("SDK artifact is missing " + name);
    }
    return value;
  }

  private static String attributeOrProperty(String attributeName, String propertyName) {
    String value = attribute(attributeName);
    return value != null && !value.isEmpty() ? value : System.getProperty(propertyName);
  }

  private static String attribute(String name) {
    try {
      Manifest manifest = manifest();
      if (manifest == null) {
        return null;
      }
      Attributes attributes = manifest.getMainAttributes();
      return attributes.getValue(name);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to read SDK build identity", e);
    }
  }

  private static Manifest manifest() throws Exception {
    URI location = BuildIdentity.class.getProtectionDomain().getCodeSource().getLocation().toURI();
    File file = new File(location);
    if (file.isFile()) {
      try (JarFile jar = new JarFile(file)) {
        return jar.getManifest();
      }
    }

    ClassLoader loader = BuildIdentity.class.getClassLoader();
    try (InputStream in = loader.getResourceAsStream("META-INF/MANIFEST.MF")) {
      return in == null ? null : new Manifest(in);
    }
  }
}
