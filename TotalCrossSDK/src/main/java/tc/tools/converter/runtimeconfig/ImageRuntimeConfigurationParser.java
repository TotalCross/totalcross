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
import totalcross.sys.runtime.RuntimeConfigurationFeatureBridge.FeatureRule;
import totalcross.sys.runtime.ImageRuntimeOptions;
import totalcross.sys.runtime.RuntimeFeatureState;
import totalcross.sys.runtime.RuntimeRule;
import totalcross.sys.runtime.RuntimeRules;
import totalcross.sys.runtime.RuntimeSelector;
import totalcross.sys.runtime.RuntimeWhen;
import totalcross.ui.image.ImageRuntimeRule;
import totalcross.ui.image.ImageRuntimeRules;
import totalcross.ui.image.ImagePrefetchWorkerMode;
import totalcross.ui.image.ImageStorageProfile;

/** Parses typed Image rules from application entry-class bytes without class initialization. */
public final class ImageRuntimeConfigurationParser {
  private static final String CONFIGURATION = Type.getDescriptor(RuntimeConfiguration.class);
  private static final String RUNTIME_RULE = Type.getDescriptor(RuntimeRule.class);
  private static final String RUNTIME_RULES = Type.getDescriptor(RuntimeRules.class);
  private static final String WHEN = Type.getDescriptor(RuntimeWhen.class);
  private static final String IMAGE_RULE = Type.getDescriptor(ImageRuntimeRule.class);
  private static final String IMAGE_RULES = Type.getDescriptor(ImageRuntimeRules.class);

  private static final Set<String> NO_FIELDS = Collections.emptySet();
  private static final Set<String> RULE_FIELDS = Collections.unmodifiableSet(
      new java.util.HashSet<String>(java.util.Arrays.asList("when", "storage", "targetColorConversion",
          "physicalVariantCache", "scrollRasterReuse", "prefetchWorker")));
  private static final Set<String> RULES_FIELDS = Collections.singleton("value");

  private ImageRuntimeConfigurationParser() {
  }

  /** Parses Image rules or returns {@code null} when the entry class declares none. */
  public static List<FeatureRule<ImageRuntimeOptions>> parse(byte[] classBytes, String className) {
    ClassNode classNode = RuntimeSelectorAnnotationParser.readClassNode(classBytes, className,
        CONFIGURATION, RUNTIME_RULE, RUNTIME_RULES, WHEN,
        Type.getDescriptor(totalcross.sys.runtime.RuntimeCondition.class), IMAGE_RULE, IMAGE_RULES);
    if (classNode == null) {
      return null;
    }

    String owner = className == null ? classNode.name.replace('/', '.') : className.replace('/', '.');
    List<AnnotationNode> annotations = RuntimeSelectorAnnotationParser.classAnnotations(classNode);
    boolean hasConfiguration = false;
    boolean hasImageRules = false;
    boolean hasDirectRule = false;
    boolean hasRuleContainer = false;
    List<AnnotationNode> ruleAnnotations = new ArrayList<AnnotationNode>();
    for (AnnotationNode annotation : annotations) {
      if (CONFIGURATION.equals(annotation.desc)) {
        RuntimeSelectorAnnotationParser.readFields(annotation, NO_FIELDS, owner + " @RuntimeConfiguration");
        hasConfiguration = true;
      } else if (IMAGE_RULE.equals(annotation.desc)) {
        hasImageRules = true;
        hasDirectRule = true;
        ruleAnnotations.add(annotation);
      } else if (IMAGE_RULES.equals(annotation.desc)) {
        hasImageRules = true;
        hasRuleContainer = true;
        Map<String, Object> values = RuntimeSelectorAnnotationParser.readFields(annotation, RULES_FIELDS,
            owner + " @ImageRuntimeRules");
        ruleAnnotations.addAll(RuntimeSelectorAnnotationParser.nestedAnnotations(values.get("value"), IMAGE_RULE,
            owner + " @ImageRuntimeRules.value"));
      }
    }

    if (!hasImageRules) {
      return null;
    }
    if (!hasConfiguration) {
      throw invalid(owner, "@ImageRuntimeRule requires @RuntimeConfiguration on the application entry class");
    }
    if (hasDirectRule && hasRuleContainer) {
      throw invalid(owner, "@ImageRuntimeRule and its repeatable @ImageRuntimeRules container cannot be mixed");
    }

    List<FeatureRule<ImageRuntimeOptions>> rules = new ArrayList<FeatureRule<ImageRuntimeOptions>>(
        ruleAnnotations.size());
    for (int i = 0; i < ruleAnnotations.size(); i++) {
      String context = owner + " @ImageRuntimeRule[" + i + "]";
      Map<String, Object> values = RuntimeSelectorAnnotationParser.readFields(ruleAnnotations.get(i), RULE_FIELDS,
          context);
      if (!values.containsKey("when")) {
        throw invalid(context, "is missing required field 'when'");
      }
      AnnotationNode when = RuntimeSelectorAnnotationParser.nestedAnnotation(values.get("when"), WHEN,
          context + ".when");
      RuntimeSelector selector = RuntimeSelectorAnnotationParser.parseWhen(when, context + ".when");
      ImageStorageProfile storage = values.containsKey("storage")
          ? RuntimeSelectorAnnotationParser.enumValue(values.get("storage"),
              Type.getDescriptor(ImageStorageProfile.class), ImageStorageProfile.class, context + ".storage")
          : ImageStorageProfile.DEFAULT;
      RuntimeFeatureState targetColorConversion = featureState(values, "targetColorConversion", context);
      RuntimeFeatureState physicalVariantCache = featureState(values, "physicalVariantCache", context);
      RuntimeFeatureState scrollRasterReuse = featureState(values, "scrollRasterReuse", context);
      ImagePrefetchWorkerMode prefetchWorker = values.containsKey("prefetchWorker")
          ? RuntimeSelectorAnnotationParser.enumValue(values.get("prefetchWorker"),
              Type.getDescriptor(ImagePrefetchWorkerMode.class), ImagePrefetchWorkerMode.class,
              context + ".prefetchWorker")
          : ImagePrefetchWorkerMode.DEFAULT;
      ImageRuntimeOptions options = new ImageRuntimeOptions(storage, targetColorConversion,
          physicalVariantCache, scrollRasterReuse, prefetchWorker);
      if (!options.hasExplicitAssignment()) {
        throw invalid(context, "must explicitly assign at least one Image runtime option");
      }
      rules.add(new FeatureRule<ImageRuntimeOptions>("image-rule-" + i, selector, options));
    }
    return Collections.unmodifiableList(rules);
  }

  private static RuntimeFeatureState featureState(Map<String, Object> values, String field, String context) {
    return values.containsKey(field)
        ? RuntimeSelectorAnnotationParser.enumValue(values.get(field),
            Type.getDescriptor(RuntimeFeatureState.class), RuntimeFeatureState.class, context + "." + field)
        : RuntimeFeatureState.DEFAULT;
  }

  private static IllegalArgumentException invalid(String context, String message) {
    return RuntimeSelectorAnnotationParser.invalid(context, message);
  }
}
