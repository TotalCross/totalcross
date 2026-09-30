// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Internal adapter for typed feature settings using the shared runtime configuration machinery. */
public final class RuntimeConfigurationFeatureBridge {
  private static final Object SINGLE_SETTING = new Object();
  private static final Map<String, DescriptionContributor> DESCRIPTION_CONTRIBUTORS =
      new TreeMap<String, DescriptionContributor>();

  private RuntimeConfigurationFeatureBridge() {
  }

  /** Encodes one selector after applying B's deployment pruning, or returns {@code null} if pruned. */
  public static byte[] encodeSingleSelectorForDeployment(RuntimeSelector selector,
      List<RuntimeConfigurationMetadata.DeploymentTarget> targets) {
    return RuntimeConfigurationMetadata.encodeSingleSelectorForDeployment(selector, targets);
  }

  /** Decodes one opaque selector payload previously returned by {@link #encodeSingleSelectorForDeployment}. */
  public static RuntimeSelector decodeSingleSelector(byte[] payload) {
    List<RuntimeSelector> selectors = RuntimeConfigurationMetadata.decode(payload);
    if (selectors.size() != 1) {
      throw new IllegalArgumentException("single selector metadata must contain exactly one selector");
    }
    return selectors.get(0);
  }

  /** Resolves one typed setting through B's shared specificity and conflict rules. */
  public static <T> FeatureResolution<T> resolveSingleSetting(RuntimeEnvironment environment,
      List<FeatureRule<T>> featureRules, T defaultValue) {
    if (environment == null || featureRules == null || defaultValue == null) {
      throw new IllegalArgumentException("environment, feature rules, and default value are required");
    }
    List<RuntimeRuleResolver.Rule> rules = new ArrayList<RuntimeRuleResolver.Rule>(featureRules.size());
    for (FeatureRule<T> featureRule : featureRules) {
      if (featureRule == null) {
        throw new IllegalArgumentException("feature rules cannot contain null");
      }
      List<RuntimeRuleResolver.Change> changes = Collections.singletonList(
          new RuntimeRuleResolver.Change(SINGLE_SETTING, featureRule.requestedValue));
      rules.add(new RuntimeRuleResolver.Rule(featureRule.name, featureRule.selector, changes));
    }
    RuntimeRuleResolver.Resolution resolution = RuntimeRuleResolver.resolve(environment, rules);
    Object value = resolution.requestedValues().get(SINGLE_SETTING);
    @SuppressWarnings("unchecked")
    T requestedValue = value == null ? defaultValue : (T) value;
    return new FeatureResolution<T>(requestedValue, resolution.matchedRuleNames());
  }

  /** Registers or replaces a diagnostic section contributed by a runtime feature. */
  public static synchronized void registerDescriptionContributor(String sectionName,
      DescriptionContributor contributor) {
    if (sectionName == null || sectionName.length() == 0 || contributor == null) {
      throw new IllegalArgumentException("a description section requires a name and contributor");
    }
    DESCRIPTION_CONTRIBUTORS.put(sectionName, contributor);
  }

  /** Appends the registered feature sections in deterministic name order. */
  public static synchronized void appendDescriptionSections(StringBuilder output) {
    if (output == null) {
      throw new IllegalArgumentException("description output is required");
    }
    for (Map.Entry<String, DescriptionContributor> entry : DESCRIPTION_CONTRIBUTORS.entrySet()) {
      output.append('\n').append(entry.getKey()).append('\n');
      output.append(entry.getValue().describe());
    }
  }

  /** Internal diagnostic section provider. */
  public interface DescriptionContributor {
    String describe();
  }

  /** Typed adapter from one feature rule to B's common selector resolver. */
  public static final class FeatureRule<T> {
    private final String name;
    private final RuntimeSelector selector;
    private final T requestedValue;

    public FeatureRule(String name, RuntimeSelector selector, T requestedValue) {
      if (name == null || name.length() == 0 || selector == null || requestedValue == null) {
        throw new IllegalArgumentException("a feature rule requires a name, selector, and value");
      }
      this.name = name;
      this.selector = selector;
      this.requestedValue = requestedValue;
    }

    public String name() {
      return name;
    }

    public RuntimeSelector selector() {
      return selector;
    }

    public T requestedValue() {
      return requestedValue;
    }
  }

  /** Typed result of resolving one feature setting through B. */
  public static final class FeatureResolution<T> {
    private final T requestedValue;
    private final List<String> matchedRuleNames;

    private FeatureResolution(T requestedValue, List<String> matchedRuleNames) {
      this.requestedValue = requestedValue;
      this.matchedRuleNames = Collections.unmodifiableList(new ArrayList<String>(matchedRuleNames));
    }

    public T requestedValue() {
      return requestedValue;
    }

    public List<String> matchedRuleNames() {
      return matchedRuleNames;
    }
  }
}
