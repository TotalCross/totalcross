// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;

class RuntimeRuleResolverTest {
  private enum SyntheticSetting {
    PROFILE
  }

  @Test
  void resolvesDefaultsOnly() {
    RuntimeRuleResolver.Resolution resolution = resolve(environment("Android", 4, true),
        rule("default", RuntimeSelector.any(), "baseline"));

    assertEquals("baseline", requested(resolution));
    assertEquals(Collections.singletonList("default"), resolution.matchedRuleNames());
  }

  @Test
  void matchesPlatformFamilyBackendAndArchitectureRules() {
    assertEquals("windows", requested(resolve(environment("Win32", 2, false),
        rule("windows", RuntimeSelector.platform(Platform.WINDOWS), "windows"))));
    assertEquals("embedded", requested(resolve(environment("WindowsCE", 3, false),
        rule("embedded", RuntimeSelector.family(RuntimeFamily.EMBEDDED), "embedded"))));
    assertEquals("gpu", requested(resolve(environment("Android", 4, true),
        rule("gpu", RuntimeSelector.backend(GraphicsBackend.GPU), "gpu"))));
    assertEquals("arm64", requested(resolve(environment("Android", 4, true),
        rule("arm64", RuntimeSelector.architecture(Architecture.ARM64), "arm64"))));
  }

  @Test
  void matchesDesktopAndRasterConjunction() {
    RuntimeSelector selector = RuntimeSelector.family(RuntimeFamily.DESKTOP)
        .and(RuntimeSelector.backend(GraphicsBackend.RASTER));

    assertEquals("desktop-raster", requested(resolve(environment("Win32", 2, false),
        rule("desktop-raster", selector, "desktop-raster"))));
    assertTrue(resolve(environment("Android", 4, true), rule("desktop-raster", selector, "desktop-raster"))
        .matchedRuleNames().isEmpty());
  }

  @Test
  void matchesWindowsRasterAndX8664Conjunction() {
    RuntimeSelector selector = RuntimeSelector.platform(Platform.WINDOWS)
        .and(RuntimeSelector.backend(GraphicsBackend.RASTER))
        .and(RuntimeSelector.architecture(Architecture.X86_64));

    assertEquals("win-raster-x64", requested(resolve(environment("Win32", 2, false),
        rule("win-raster-x64", selector, "win-raster-x64"))));
    assertTrue(resolve(environment("Win32", 1, false), rule("win-raster-x64", selector, "win-raster-x64"))
        .matchedRuleNames().isEmpty());
  }

  @Test
  void excludesNonMatchingRulesAndSupportsAnyOf() {
    RuntimeSelector windowsOrMobile = RuntimeSelector.anyOf(RuntimeSelector.platform(Platform.WINDOWS),
        RuntimeSelector.family(RuntimeFamily.MOBILE));
    RuntimeRuleResolver.Resolution resolution = resolve(environment("iPhone", 4, true),
        rule("conditional", windowsOrMobile, "matched"), rule("linux", RuntimeSelector.platform(Platform.LINUX),
            "excluded"));

    assertEquals("matched", requested(resolution));
    assertEquals(Collections.singletonList("conditional"), resolution.matchedRuleNames());
  }

  @Test
  void moreSpecificRuleOverridesLessSpecificAssignments() {
    RuntimeSelector windowsRaster = RuntimeSelector.platform(Platform.WINDOWS)
        .and(RuntimeSelector.backend(GraphicsBackend.RASTER));
    RuntimeRuleResolver.Resolution resolution = resolve(environment("Win32", 2, false),
        rule("default", RuntimeSelector.any(), "default"),
        rule("windows", RuntimeSelector.platform(Platform.WINDOWS), "windows"),
        rule("windows-raster", windowsRaster, "windows-raster"));

    assertEquals("windows-raster", requested(resolution));
    assertEquals(3, resolution.matchedRuleNames().size());
  }

  @Test
  void rejectsContradictoryEqualSpecificityAssignments() {
    RuntimeEnvironment environment = environment("Win32", 2, true);
    RuntimeRuleResolver.ConfigurationConflictException error = assertThrows(
        RuntimeRuleResolver.ConfigurationConflictException.class,
        () -> resolve(environment, rule("platform-rule", RuntimeSelector.platform(Platform.WINDOWS), "platform"),
            rule("backend-rule", RuntimeSelector.backend(GraphicsBackend.GPU), "backend")));

    assertTrue(error.getMessage().contains("PROFILE"));
    assertTrue(error.getMessage().contains("platform-rule"));
    assertTrue(error.getMessage().contains("backend-rule"));
    assertTrue(error.getMessage().contains("more-specific combined rule"));
  }

  @Test
  void returnsImmutableResolvedValuesAndMatchedRules() {
    RuntimeRuleResolver.Resolution resolution = resolve(environment("Win32", 2, false),
        rule("default", RuntimeSelector.any(), "baseline"));
    Map<Object, Object> requested = resolution.requestedValues();
    Map<Object, Object> effective = resolution.effectiveValues();

    assertEquals(requested, effective);
    assertThrows(UnsupportedOperationException.class, () -> requested.put(SyntheticSetting.PROFILE, "changed"));
    assertThrows(UnsupportedOperationException.class, () -> effective.put(SyntheticSetting.PROFILE, "changed"));
    assertThrows(UnsupportedOperationException.class, () -> resolution.matchedRuleNames().add("changed"));
    assertEquals("baseline", requested(resolution));
  }

  private static RuntimeEnvironment environment(String platform, int architecture, boolean isOpenGL) {
    return RuntimeEnvironmentTestSupport.environment(platform, architecture, isOpenGL);
  }

  private static RuntimeRuleResolver.Rule rule(String name, RuntimeSelector selector, Object value) {
    RuntimeRuleResolver.Change change = new RuntimeRuleResolver.Change(SyntheticSetting.PROFILE, value);
    return new RuntimeRuleResolver.Rule(name, selector, Arrays.asList(change));
  }

  private static RuntimeRuleResolver.Resolution resolve(RuntimeEnvironment environment,
      RuntimeRuleResolver.Rule... rules) {
    List<RuntimeRuleResolver.Rule> list = Arrays.asList(rules);
    return RuntimeRuleResolver.resolve(environment, list);
  }

  private static Object requested(RuntimeRuleResolver.Resolution resolution) {
    return resolution.requestedValues().get(SyntheticSetting.PROFILE);
  }
}
