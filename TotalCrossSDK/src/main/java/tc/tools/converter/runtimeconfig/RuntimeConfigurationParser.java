// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.runtimeconfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import totalcross.sys.runtime.RuntimeConfiguration;
import totalcross.sys.runtime.RuntimeRule;
import totalcross.sys.runtime.RuntimeRules;
import totalcross.sys.runtime.RuntimeSelector;
import totalcross.sys.runtime.RuntimeWhen;

/** Parses CLASS-retained runtime selector declarations from application class bytes. */
public final class RuntimeConfigurationParser {
  private static final String CONFIGURATION = Type.getDescriptor(RuntimeConfiguration.class);
  private static final String RULE = Type.getDescriptor(RuntimeRule.class);
  private static final String RULES = Type.getDescriptor(RuntimeRules.class);
  private static final String WHEN = Type.getDescriptor(RuntimeWhen.class);

  private static final Set<String> NO_FIELDS = Collections.emptySet();
  private static final Set<String> RULE_FIELDS = Collections.singleton("when");
  private static final Set<String> RULES_FIELDS = Collections.singleton("value");

  private RuntimeConfigurationParser() {
  }

  /**
   * Parses selectors or returns {@code null} when the class has no runtime
   * configuration annotations. A marker with no rules returns an empty list.
   */
  public static List<RuntimeSelector> parse(byte[] classBytes, String className) {
    ClassNode classNode = RuntimeSelectorAnnotationParser.readClassNode(classBytes, className,
        CONFIGURATION, RULE, RULES, WHEN, Type.getDescriptor(totalcross.sys.runtime.RuntimeCondition.class));
    if (classNode == null) {
      return null;
    }

    String owner = className == null ? classNode.name.replace('/', '.') : className.replace('/', '.');
    List<AnnotationNode> annotations = RuntimeSelectorAnnotationParser.classAnnotations(classNode);
    boolean hasConfiguration = false;
    boolean hasRules = false;
    boolean hasDirectRule = false;
    boolean hasRuleContainer = false;
    List<AnnotationNode> ruleAnnotations = new ArrayList<AnnotationNode>();
    for (AnnotationNode annotation : annotations) {
      if (CONFIGURATION.equals(annotation.desc)) {
        RuntimeSelectorAnnotationParser.readFields(annotation, NO_FIELDS, owner + " @RuntimeConfiguration");
        hasConfiguration = true;
      } else if (RULE.equals(annotation.desc)) {
        hasRules = true;
        hasDirectRule = true;
        ruleAnnotations.add(annotation);
      } else if (RULES.equals(annotation.desc)) {
        hasRules = true;
        hasRuleContainer = true;
        Map<String, Object> values = RuntimeSelectorAnnotationParser.readFields(annotation, RULES_FIELDS,
            owner + " @RuntimeRules");
        ruleAnnotations.addAll(RuntimeSelectorAnnotationParser.nestedAnnotations(values.get("value"), RULE,
            owner + " @RuntimeRules.value"));
      }
    }

    if (!hasConfiguration && !hasRules) {
      return null;
    }
    if (!hasConfiguration) {
      throw invalid(owner, "@RuntimeRule requires @RuntimeConfiguration on the application entry class");
    }
    if (hasDirectRule && hasRuleContainer) {
      throw invalid(owner, "@RuntimeRule and its repeatable @RuntimeRules container cannot be mixed");
    }

    List<RuntimeSelector> selectors = new ArrayList<RuntimeSelector>(ruleAnnotations.size());
    for (int i = 0; i < ruleAnnotations.size(); i++) {
      String context = owner + " @RuntimeRule[" + i + "]";
      Map<String, Object> values = RuntimeSelectorAnnotationParser.readFields(ruleAnnotations.get(i), RULE_FIELDS,
          context);
      AnnotationNode when = RuntimeSelectorAnnotationParser.nestedAnnotation(values.get("when"), WHEN,
          context + ".when");
      selectors.add(RuntimeSelectorAnnotationParser.parseWhen(when, context + ".when"));
    }
    return Collections.unmodifiableList(selectors);
  }

  private static IllegalArgumentException invalid(String context, String message) {
    return RuntimeSelectorAnnotationParser.invalid(context, message);
  }
}
