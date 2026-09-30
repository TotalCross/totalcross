// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;

/**
 * Immutable predicate over independent runtime environment dimensions.
 *
 * <p>Factories accept one or more values as alternatives for that dimension.
 * Combine selectors with {@link #and(RuntimeSelector)} or
 * {@link #or(RuntimeSelector)}. {@link #any()} selects every environment.
 */
public final class RuntimeSelector {
  private static final RuntimeSelector ANY = new RuntimeSelector(Collections.singletonList(Clause.empty()));

  private final List<Clause> clauses;

  private RuntimeSelector(List<Clause> clauses) {
    this.clauses = simplify(clauses);
  }

  /** Returns a selector that matches every runtime environment. */
  public static RuntimeSelector any() {
    return ANY;
  }

  /** Selects any of the supplied platforms. */
  public static RuntimeSelector platform(Platform... values) {
    return forDimension(Platform.class, values);
  }

  /** Selects any of the supplied runtime families. */
  public static RuntimeSelector family(RuntimeFamily... values) {
    return forDimension(RuntimeFamily.class, values);
  }

  /** Selects any of the supplied graphics backends. */
  public static RuntimeSelector backend(GraphicsBackend... values) {
    return forDimension(GraphicsBackend.class, values);
  }

  /** Selects any of the supplied processor architectures. */
  public static RuntimeSelector architecture(Architecture... values) {
    return forDimension(Architecture.class, values);
  }

  /** Returns the conjunction of all supplied selectors. An empty list matches every environment. */
  public static RuntimeSelector allOf(RuntimeSelector... selectors) {
    if (selectors == null) {
      throw new IllegalArgumentException("selectors cannot be null");
    }
    RuntimeSelector result = any();
    for (RuntimeSelector selector : selectors) {
      result = result.and(requireSelector(selector));
    }
    return result;
  }

  /** Returns the disjunction of supplied selectors. At least one selector is required. */
  public static RuntimeSelector anyOf(RuntimeSelector... selectors) {
    if (selectors == null || selectors.length == 0) {
      throw new IllegalArgumentException("anyOf requires at least one selector");
    }
    RuntimeSelector result = requireSelector(selectors[0]);
    for (int i = 1; i < selectors.length; i++) {
      result = result.or(requireSelector(selectors[i]));
    }
    return result;
  }

  /** Returns a selector matching environments that satisfy both selectors. */
  public RuntimeSelector and(RuntimeSelector other) {
    requireSelector(other);
    if (clauses.isEmpty() || other.clauses.isEmpty()) {
      return new RuntimeSelector(Collections.<Clause>emptyList());
    }
    List<Clause> result = new ArrayList<Clause>(clauses.size() * other.clauses.size());
    for (Clause left : clauses) {
      for (Clause right : other.clauses) {
        Clause merged = left.and(right);
        if (merged != null) {
          result.add(merged);
        }
      }
    }
    return new RuntimeSelector(result);
  }

  /** Returns a selector matching environments that satisfy either selector. */
  public RuntimeSelector or(RuntimeSelector other) {
    requireSelector(other);
    List<Clause> result = new ArrayList<Clause>(clauses.size() + other.clauses.size());
    result.addAll(clauses);
    result.addAll(other.clauses);
    return new RuntimeSelector(result);
  }

  /** Tests this selector against an immutable environment snapshot. */
  public boolean matches(RuntimeEnvironment environment) {
    return specificityFor(environment) >= 0;
  }

  int specificityFor(RuntimeEnvironment environment) {
    if (environment == null) {
      throw new IllegalArgumentException("environment cannot be null");
    }
    int specificity = Integer.MAX_VALUE;
    for (Clause clause : clauses) {
      if (clause.matches(environment) && clause.conditions.size() < specificity) {
        specificity = clause.conditions.size();
      }
    }
    return specificity == Integer.MAX_VALUE ? -1 : specificity;
  }

  boolean isUnconditional() {
    for (Clause clause : clauses) {
      if (clause.conditions.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  static RuntimeSelector condition(Platform[] platforms, RuntimeFamily[] families, GraphicsBackend[] backends,
      Architecture[] architectures) {
    Map<Class<? extends Enum<?>>, Set<Enum<?>>> conditions = new HashMap<Class<? extends Enum<?>>, Set<Enum<?>>>();
    addDimension(conditions, Platform.class, platforms);
    addDimension(conditions, RuntimeFamily.class, families);
    addDimension(conditions, GraphicsBackend.class, backends);
    addDimension(conditions, Architecture.class, architectures);
    if (conditions.isEmpty()) {
      throw new IllegalArgumentException("a runtime condition must specify at least one dimension");
    }
    return new RuntimeSelector(Collections.singletonList(new Clause(conditions)));
  }

  private static <E extends Enum<E>> RuntimeSelector forDimension(Class<E> dimension, E[] values) {
    if (values == null || values.length == 0) {
      throw new IllegalArgumentException("at least one value is required");
    }
    Map<Class<? extends Enum<?>>, Set<Enum<?>>> conditions = new HashMap<Class<? extends Enum<?>>, Set<Enum<?>>>();
    addDimension(conditions, dimension, values);
    return new RuntimeSelector(Collections.singletonList(new Clause(conditions)));
  }

  private static <E extends Enum<E>> void addDimension(Map<Class<? extends Enum<?>>, Set<Enum<?>>> conditions,
      Class<E> dimension, E[] values) {
    if (values == null || values.length == 0) {
      return;
    }
    Set<Enum<?>> accepted = new HashSet<Enum<?>>();
    for (E value : values) {
      if (value == null) {
        throw new IllegalArgumentException("runtime condition values cannot contain null");
      }
      accepted.add(value);
    }
    conditions.put(dimension, accepted);
  }

  private static RuntimeSelector requireSelector(RuntimeSelector selector) {
    if (selector == null) {
      throw new IllegalArgumentException("selector cannot be null");
    }
    return selector;
  }

  private static List<Clause> simplify(List<Clause> candidates) {
    List<Clause> result = new ArrayList<Clause>();
    for (Clause candidate : candidates) {
      boolean covered = false;
      for (int i = 0; i < result.size();) {
        Clause existing = result.get(i);
        if (existing.subsumes(candidate)) {
          covered = true;
          break;
        }
        if (candidate.subsumes(existing)) {
          result.remove(i);
        } else {
          i++;
        }
      }
      if (!covered) {
        result.add(candidate);
      }
    }
    return Collections.unmodifiableList(result);
  }

  private static final class Clause {
    private final Map<Class<? extends Enum<?>>, Set<Enum<?>>> conditions;

    private Clause(Map<Class<? extends Enum<?>>, Set<Enum<?>>> conditions) {
      Map<Class<? extends Enum<?>>, Set<Enum<?>>> copy = new HashMap<Class<? extends Enum<?>>, Set<Enum<?>>>();
      for (Map.Entry<Class<? extends Enum<?>>, Set<Enum<?>>> entry : conditions.entrySet()) {
        copy.put(entry.getKey(), Collections.unmodifiableSet(new HashSet<Enum<?>>(entry.getValue())));
      }
      this.conditions = Collections.unmodifiableMap(copy);
    }

    private static Clause empty() {
      return new Clause(Collections.<Class<? extends Enum<?>>, Set<Enum<?>>>emptyMap());
    }

    private Clause and(Clause other) {
      Map<Class<? extends Enum<?>>, Set<Enum<?>>> merged = new HashMap<Class<? extends Enum<?>>, Set<Enum<?>>>();
      merged.putAll(conditions);
      for (Map.Entry<Class<? extends Enum<?>>, Set<Enum<?>>> entry : other.conditions.entrySet()) {
        Set<Enum<?>> existing = merged.get(entry.getKey());
        if (existing == null) {
          merged.put(entry.getKey(), entry.getValue());
        } else {
          Set<Enum<?>> intersection = new HashSet<Enum<?>>(existing);
          intersection.retainAll(entry.getValue());
          if (intersection.isEmpty()) {
            return null;
          }
          merged.put(entry.getKey(), intersection);
        }
      }
      return new Clause(merged);
    }

    private boolean matches(RuntimeEnvironment environment) {
      for (Map.Entry<Class<? extends Enum<?>>, Set<Enum<?>>> condition : conditions.entrySet()) {
        Enum<?> actual = environment.valueFor(condition.getKey());
        if (actual == null || !condition.getValue().contains(actual)) {
          return false;
        }
      }
      return true;
    }

    private boolean subsumes(Clause other) {
      if (conditions.size() > other.conditions.size()) {
        return false;
      }
      for (Map.Entry<Class<? extends Enum<?>>, Set<Enum<?>>> condition : conditions.entrySet()) {
        Set<Enum<?>> otherValues = other.conditions.get(condition.getKey());
        if (otherValues == null || !condition.getValue().containsAll(otherValues)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof Clause && conditions.equals(((Clause) other).conditions);
    }

    @Override
    public int hashCode() {
      return conditions.hashCode();
    }
  }
}
