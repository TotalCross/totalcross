// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Internal startup resolver. Domain setting keys and values are supplied by later typed integrations. */
final class RuntimeRuleResolver {
  private RuntimeRuleResolver() {
  }

  static Resolution resolve(RuntimeEnvironment environment, List<Rule> rules) {
    if (environment == null || rules == null) {
      throw new IllegalArgumentException("environment and rules are required");
    }
    List<MatchedRule> matched = new ArrayList<MatchedRule>();
    for (Rule rule : rules) {
      int specificity = rule.selector.specificityFor(environment);
      if (specificity >= 0) {
        matched.add(new MatchedRule(rule, specificity));
      }
    }
    Collections.sort(matched, new Comparator<MatchedRule>() {
      @Override
      public int compare(MatchedRule left, MatchedRule right) {
        int specificity = left.specificity - right.specificity;
        return specificity != 0 ? specificity : left.rule.name.compareTo(right.rule.name);
      }
    });

    Map<Object, Assignment> assignments = new HashMap<Object, Assignment>();
    for (MatchedRule matchedRule : matched) {
      Rule rule = matchedRule.rule;
      for (Change change : rule.changes) {
        Assignment previous = assignments.get(change.setting);
        if (previous == null || matchedRule.specificity > previous.specificity) {
          assignments.put(change.setting, new Assignment(change.value, matchedRule.specificity, rule.name));
        } else if (matchedRule.specificity == previous.specificity && !equals(previous.value, change.value)) {
          throw new ConfigurationConflictException("Equal-specificity runtime rules conflict for setting '"
              + change.setting + "': rule '" + previous.ruleName + "' requests '" + previous.value + "' while rule '"
              + rule.name + "' requests '" + change.value
              + "'. Add a more-specific combined rule to resolve the conflict.");
        }
      }
    }

    Map<Object, Object> requested = new LinkedHashMap<Object, Object>();
    for (Map.Entry<Object, Assignment> entry : assignments.entrySet()) {
      requested.put(entry.getKey(), entry.getValue().value);
    }
    return new Resolution(environment, requested, new ArrayList<MatchedRule>(matched));
  }

  private static boolean equals(Object left, Object right) {
    return left == right || (left != null && left.equals(right));
  }

  static final class Rule {
    private final String name;
    private final RuntimeSelector selector;
    private final List<Change> changes;

    Rule(String name, RuntimeSelector selector, List<Change> changes) {
      if (name == null || name.length() == 0 || selector == null || changes == null) {
        throw new IllegalArgumentException("a rule requires a name, selector, and changes");
      }
      this.name = name;
      this.selector = selector;
      this.changes = Collections.unmodifiableList(new ArrayList<Change>(changes));
    }
  }

  static final class Change {
    private final Object setting;
    private final Object value;

    Change(Object setting, Object value) {
      if (setting == null) {
        throw new IllegalArgumentException("a change requires a setting descriptor");
      }
      this.setting = setting;
      this.value = value;
    }
  }

  static final class Resolution {
    private final RuntimeEnvironment environment;
    private final Map<Object, Object> requestedValues;
    private final Map<Object, Object> effectiveValues;
    private final List<String> matchedRuleNames;

    private Resolution(RuntimeEnvironment environment, Map<Object, Object> requestedValues,
        List<MatchedRule> matchedRules) {
      this.environment = environment;
      this.requestedValues = Collections.unmodifiableMap(new LinkedHashMap<Object, Object>(requestedValues));
      this.effectiveValues = Collections.unmodifiableMap(new LinkedHashMap<Object, Object>(requestedValues));
      List<String> names = new ArrayList<String>(matchedRules.size());
      for (MatchedRule matchedRule : matchedRules) {
        names.add(matchedRule.rule.name);
      }
      this.matchedRuleNames = Collections.unmodifiableList(names);
    }

    RuntimeEnvironment environment() {
      return environment;
    }

    Map<Object, Object> requestedValues() {
      return requestedValues;
    }

    Map<Object, Object> effectiveValues() {
      return effectiveValues;
    }

    List<String> matchedRuleNames() {
      return matchedRuleNames;
    }
  }

  static final class ConfigurationConflictException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    ConfigurationConflictException(String message) {
      super(message);
    }
  }

  private static final class Assignment {
    private final Object value;
    private final int specificity;
    private final String ruleName;

    private Assignment(Object value, int specificity, String ruleName) {
      this.value = value;
      this.specificity = specificity;
      this.ruleName = ruleName;
    }
  }

  private static final class MatchedRule {
    private final Rule rule;
    private final int specificity;

    private MatchedRule(Rule rule, int specificity) {
      this.rule = rule;
      this.specificity = specificity;
    }
  }
}
