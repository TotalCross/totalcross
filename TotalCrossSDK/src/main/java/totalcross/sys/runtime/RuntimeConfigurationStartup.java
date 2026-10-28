// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import totalcross.sys.GraphicsBackend;
import totalcross.sys.Vm;
import totalcross.util.concurrent.Lock;

/** Internal startup bridge for loading and resolving optional TCZ configuration metadata. */
public final class RuntimeConfigurationStartup {
  private static final String RESOURCE_NAME = "tc.runtimeconfig";

  private static final Lock STARTUP_LOCK = new Lock();

  private static boolean startupAttempted;
  private static List<RuntimeSelector> pendingSelectors;
  private static ResolvedRuntimeConfiguration current;

  private RuntimeConfigurationStartup() {
  }

  /** Called by the native VM after runtime settings and graphics initialization. */
  public static void initializeAtStartup() {
    synchronized (STARTUP_LOCK) {
      if (startupAttempted) {
        resolveIfReady();
        return;
      }
      startupAttempted = true;
      byte[] bytes = Vm.getFile(RESOURCE_NAME);
      initializeSelectors(bytes == null ? null : RuntimeConfigurationMetadata.decode(bytes));
      resolveIfReady();
    }
  }

  /** Called by the simulator after parsing the application class resource. */
  public static void initializeForSimulator(List<RuntimeSelector> selectors) {
    synchronized (STARTUP_LOCK) {
      RuntimeEnvironment.resetSimulatorGraphicsBackend();
      startupAttempted = true;
      initializeSelectors(selectors);
      resolveIfReady();
    }
  }

  /** Called by the simulator before application loading to supply host-only environment facts. */
  public static void initializeForSimulator(List<RuntimeSelector> selectors, String osName,
      String osArch) {
    synchronized (STARTUP_LOCK) {
      RuntimeEnvironment.setSimulatorHostProperties(osName, osArch);
      initializeForSimulator(selectors);
    }
  }

  /** Called by the simulator after its AWT render surface has started. */
  public static void finalizeSimulatorGraphicsBackend(GraphicsBackend backend) {
    synchronized (STARTUP_LOCK) {
      RuntimeEnvironment.finalizeSimulatorGraphicsBackend(backend);
      resolveIfReady();
    }
  }

  /** Returns the startup result, or {@code null} when no configuration is declared. */
  static ResolvedRuntimeConfiguration current() {
    synchronized (STARTUP_LOCK) {
      resolveIfReady();
      return current;
    }
  }

  private static void initializeSelectors(List<RuntimeSelector> selectors) {
    current = null;
    pendingSelectors = selectors == null ? null
        : Collections.unmodifiableList(new ArrayList<RuntimeSelector>(selectors));
  }

  private static void resolveIfReady() {
    if (pendingSelectors == null) {
      return;
    }
    RuntimeEnvironment environment = RuntimeEnvironment.current();
    List<RuntimeRuleResolver.Rule> rules = new ArrayList<RuntimeRuleResolver.Rule>(pendingSelectors.size());
    for (int i = 0; i < pendingSelectors.size(); i++) {
      RuntimeSelector selector = pendingSelectors.get(i);
      if (selector == null) {
        throw new IllegalArgumentException("runtime configuration selectors cannot contain null");
      }
      rules.add(new RuntimeRuleResolver.Rule("selector-" + i, selector,
          Collections.<RuntimeRuleResolver.Change>emptyList()));
    }
    RuntimeRuleResolver.Resolution resolution = RuntimeRuleResolver.resolve(environment, rules);
    current = new ResolvedRuntimeConfiguration(environment, resolution.matchedRuleNames().size());
    if (environment.isGraphicsBackendFinalized()) {
      pendingSelectors = null;
    }
  }
}
