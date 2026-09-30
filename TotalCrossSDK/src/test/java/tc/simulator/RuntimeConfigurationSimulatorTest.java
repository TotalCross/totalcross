// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import totalcross.sys.RuntimeFamily;
import totalcross.sys.runtime.RuntimeConfigurationStartup;
import totalcross.sys.runtime.RuntimeCondition;
import totalcross.sys.runtime.RuntimeConfiguration;
import totalcross.sys.runtime.RuntimeRule;
import totalcross.sys.runtime.RuntimeWhen;

class RuntimeConfigurationSimulatorTest {
  private static final String INITIALIZED_PROPERTY = "totalcross.runtimeConfigurationFixture.initialized";

  @AfterEach
  void cleanup() {
    System.clearProperty(INITIALIZED_PROPERTY);
    RuntimeConfigurationStartup.initializeForSimulator(null);
  }

  @Test
  void parsesClassResourceWithoutInitializingTheApplicationClass() throws Exception {
    System.clearProperty(INITIALIZED_PROPERTY);
    String fixtureName = AnnotatedFixture.class.getName();

    RuntimeConfigurationSimulator.initialize(getClass().getClassLoader(), fixtureName);

    assertNull(System.getProperty(INITIALIZED_PROPERTY));
    Object current = startupResult();
    assertNotNull(current);
    assertEquals(1, matchedRuleCount(current));

    Class.forName(fixtureName, true, getClass().getClassLoader());
    assertEquals("loaded", System.getProperty(INITIALIZED_PROPERTY));
  }

  @Test
  void leavesUnannotatedJava17ClassesOnTheNoMetadataPath() throws Exception {
    RuntimeConfigurationSimulator.initialize(getClass().getClassLoader(), PlainFixture.class.getName());

    assertNull(startupResult());
  }

  @RuntimeConfiguration
  @RuntimeRule(when = @RuntimeWhen(allOf = { @RuntimeCondition(family = RuntimeFamily.DESKTOP) }))
  static class AnnotatedFixture {
    static {
      System.setProperty(INITIALIZED_PROPERTY, "loaded");
    }
  }

  static class PlainFixture {
  }

  private static Object startupResult() throws Exception {
    java.lang.reflect.Method current = RuntimeConfigurationStartup.class.getDeclaredMethod("current");
    current.setAccessible(true);
    return current.invoke(null);
  }

  private static int matchedRuleCount(Object result) throws Exception {
    java.lang.reflect.Method count = result.getClass().getDeclaredMethod("matchedRuleCount");
    count.setAccessible(true);
    return ((Integer) count.invoke(result)).intValue();
  }
}
