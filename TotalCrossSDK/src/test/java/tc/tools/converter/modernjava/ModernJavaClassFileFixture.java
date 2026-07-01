// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

final class ModernJavaClassFileFixture {
  final int javaRelease;
  final int expectedMajorVersion;
  final String featureName;
  final String className;
  final byte[] bytes;
  final boolean compiledWithJavac;

  ModernJavaClassFileFixture(int javaRelease, int expectedMajorVersion, String featureName, String className,
      byte[] bytes, boolean compiledWithJavac) {
    this.javaRelease = javaRelease;
    this.expectedMajorVersion = expectedMajorVersion;
    this.featureName = featureName;
    this.className = className;
    this.bytes = bytes;
    this.compiledWithJavac = compiledWithJavac;
  }

  ClassFileMetadata metadata() {
    return ClassFileMetadata.read(bytes);
  }
}
