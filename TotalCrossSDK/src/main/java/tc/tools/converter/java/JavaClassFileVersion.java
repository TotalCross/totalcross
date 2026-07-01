// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.java;

public final class JavaClassFileVersion {
  public static final int JAVA_7 = 51;
  public static final int JAVA_8 = 52;
  public static final int JAVA_11 = 55;
  public static final int JAVA_17 = 61;
  public static final int JAVA_21 = 65;
  public static final int JAVA_25 = 69;
  public static final int JAVA_26 = 70;

  public static final int PREVIEW_MINOR_VERSION = 65535;

  private JavaClassFileVersion() {
  }

  public static int maxSupportedMajor() {
    return JAVA_26;
  }

  public static void validate(String className, int major, int minor) {
    String name = className == null || className.length() == 0 ? "<unknown>" : className;
    if (minor == PREVIEW_MINOR_VERSION) {
      throw new IllegalArgumentException("Class " + name + " uses preview class file version " + major + "." + minor
          + ", which is not supported by tc.Deploy");
    }
    if (major > maxSupportedMajor()) {
      throw new IllegalArgumentException("Class " + name + " uses class file major version " + major
          + ", but tc.Deploy supports up to " + maxSupportedMajor());
    }
  }
}
