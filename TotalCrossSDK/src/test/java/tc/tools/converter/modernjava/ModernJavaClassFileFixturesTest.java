// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModernJavaClassFileFixturesTest {
  @TempDir
  Path workDir;

  @Test
  void generatedFixturesCoverRoadmapMajorVersions() throws Exception {
    List<ModernJavaClassFileFixture> fixtures = ModernJavaClassFileFixtures.generatedRoadmapFixtures();

    assertEquals(ModernJavaClassFileFixtures.ROADMAP_MAJOR_VERSIONS.size(), fixtures.size());
    for (ModernJavaClassFileFixture fixture : fixtures) {
      ClassFileMetadata metadata = fixture.metadata();

      assertEquals(0, metadata.minorVersion, fixture.className);
      assertEquals(fixture.expectedMajorVersion, metadata.majorVersion, fixture.className);
      assertEquals(ModernJavaClassFileFixtures.ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(fixture.javaRelease)).intValue(),
          metadata.majorVersion, fixture.className);
      assertFalse(fixture.compiledWithJavac, fixture.className);
    }
  }

  @Test
  void javacFixturesUseMatchingClassFileMajorWhenReleaseIsAvailable() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");

    int compiledCount = 0;
    for (Map.Entry<Integer, Integer> entry : ModernJavaClassFileFixtures.ROADMAP_MAJOR_VERSIONS.entrySet()) {
      Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileSimpleFixture(workDir,
          entry.getKey().intValue());
      if (fixture.isPresent()) {
        compiledCount++;
        assertTrue(fixture.get().compiledWithJavac, fixture.get().className);
        assertEquals(entry.getValue().intValue(), fixture.get().metadata().majorVersion, fixture.get().className);
      }
    }

    assertTrue(compiledCount > 0, "At least one roadmap fixture should compile with the current javac");
  }

  @Test
  void java8LambdaFixtureCompilesWhenCurrentJavacCanTargetJava8() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");

    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava8LambdaFixture(workDir);

    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");
    assertEquals(ModernJavaClassFileFixtures.ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(8)).intValue(),
        fixture.get().metadata().majorVersion);
    assertEquals("java 8 lambda", fixture.get().featureName);
  }
}
