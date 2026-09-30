// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.runtimeconfig;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.runtime.RuntimeCondition;
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
  private static final String CONDITION = Type.getDescriptor(RuntimeCondition.class);

  private static final Set<String> NO_FIELDS = Collections.emptySet();
  private static final Set<String> RULE_FIELDS = immutableSet("when");
  private static final Set<String> RULES_FIELDS = immutableSet("value");
  private static final Set<String> WHEN_FIELDS = immutableSet("allOf", "anyOf");
  private static final Set<String> CONDITION_FIELDS = immutableSet("platform", "family", "backend", "architecture");

  private RuntimeConfigurationParser() {
  }

  /**
   * Parses selectors or returns {@code null} when the class has no runtime
   * configuration annotations. A marker with no rules returns an empty list.
   */
  public static List<RuntimeSelector> parse(byte[] classBytes, String className) {
    if (classBytes == null || classBytes.length == 0) {
      throw new IllegalArgumentException("class bytes are required to parse runtime configuration");
    }
    byte[] asmBytes = normalizeClassVersionForMetadata(classBytes);
    if (asmBytes == null) {
      return null;
    }
    ClassNode classNode = new ClassNode();
    try {
      new ClassReader(asmBytes).accept(classNode,
          ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Cannot read runtime configuration class " + className, e);
    }

    String owner = className == null ? classNode.name.replace('/', '.') : className.replace('/', '.');
    List<AnnotationNode> annotations = classAnnotations(classNode);
    boolean hasConfiguration = false;
    boolean hasRules = false;
    boolean hasDirectRule = false;
    boolean hasRuleContainer = false;
    List<AnnotationNode> ruleAnnotations = new ArrayList<AnnotationNode>();
    for (AnnotationNode annotation : annotations) {
      if (CONFIGURATION.equals(annotation.desc)) {
        readFields(annotation, NO_FIELDS, owner + " @RuntimeConfiguration");
        hasConfiguration = true;
      } else if (RULE.equals(annotation.desc)) {
        hasRules = true;
        hasDirectRule = true;
        ruleAnnotations.add(annotation);
      } else if (RULES.equals(annotation.desc)) {
        hasRules = true;
        hasRuleContainer = true;
        Map<String, Object> values = readFields(annotation, RULES_FIELDS, owner + " @RuntimeRules");
        ruleAnnotations.addAll(nestedAnnotations(values.get("value"), RULE,
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
      Map<String, Object> values = readFields(ruleAnnotations.get(i), RULE_FIELDS, context);
      AnnotationNode when = nestedAnnotation(values.get("when"), WHEN, context + ".when");
      selectors.add(parseWhen(when, context + ".when"));
    }
    return Collections.unmodifiableList(selectors);
  }

  private static byte[] normalizeClassVersionForMetadata(byte[] classBytes) {
    if (classBytes.length < 8) {
      return classBytes;
    }
    int majorVersion = ((classBytes[6] & 0xff) << 8) | (classBytes[7] & 0xff);
    if (majorVersion <= 52) {
      return classBytes;
    }
    if (!contains(classBytes, CONFIGURATION) && !contains(classBytes, RULE) && !contains(classBytes, RULES)
        && !contains(classBytes, WHEN) && !contains(classBytes, CONDITION)) {
      return null;
    }
    byte[] normalized = Arrays.copyOf(classBytes, classBytes.length);
    normalized[6] = 0;
    normalized[7] = 52;
    return normalized;
  }

  private static boolean contains(byte[] bytes, String value) {
    byte[] pattern = value.getBytes(StandardCharsets.UTF_8);
    outer: for (int i = 0; i <= bytes.length - pattern.length; i++) {
      for (int j = 0; j < pattern.length; j++) {
        if (bytes[i + j] != pattern[j]) {
          continue outer;
        }
      }
      return true;
    }
    return false;
  }

  private static RuntimeSelector parseWhen(AnnotationNode annotation, String context) {
    Map<String, Object> values = readFields(annotation, WHEN_FIELDS, context);
    List<AnnotationNode> allOf = nestedAnnotations(values.get("allOf"), CONDITION, context + ".allOf");
    List<AnnotationNode> anyOf = nestedAnnotations(values.get("anyOf"), CONDITION, context + ".anyOf");

    RuntimeSelector selector = RuntimeSelector.allOf();
    for (int i = 0; i < allOf.size(); i++) {
      selector = selector.and(parseCondition(allOf.get(i), context + ".allOf[" + i + "]"));
    }
    if (!anyOf.isEmpty()) {
      RuntimeSelector[] alternatives = new RuntimeSelector[anyOf.size()];
      for (int i = 0; i < anyOf.size(); i++) {
        alternatives[i] = parseCondition(anyOf.get(i), context + ".anyOf[" + i + "]");
      }
      selector = selector.and(RuntimeSelector.anyOf(alternatives));
    }
    return selector;
  }

  private static RuntimeSelector parseCondition(AnnotationNode annotation, String context) {
    Map<String, Object> values = readFields(annotation, CONDITION_FIELDS, context);
    List<Platform> platforms = enumValues(values.get("platform"), Type.getDescriptor(Platform.class), Platform.class,
        context + ".platform");
    List<RuntimeFamily> families = enumValues(values.get("family"), Type.getDescriptor(RuntimeFamily.class),
        RuntimeFamily.class, context + ".family");
    List<GraphicsBackend> backends = enumValues(values.get("backend"), Type.getDescriptor(GraphicsBackend.class),
        GraphicsBackend.class, context + ".backend");
    List<Architecture> architectures = enumValues(values.get("architecture"), Type.getDescriptor(Architecture.class),
        Architecture.class, context + ".architecture");

    RuntimeSelector selector = RuntimeSelector.allOf();
    boolean specified = false;
    if (!platforms.isEmpty()) {
      selector = selector.and(RuntimeSelector.platform(platforms.toArray(new Platform[platforms.size()])));
      specified = true;
    }
    if (!families.isEmpty()) {
      selector = selector.and(RuntimeSelector.family(families.toArray(new RuntimeFamily[families.size()])));
      specified = true;
    }
    if (!backends.isEmpty()) {
      selector = selector.and(RuntimeSelector.backend(backends.toArray(new GraphicsBackend[backends.size()])));
      specified = true;
    }
    if (!architectures.isEmpty()) {
      selector = selector.and(RuntimeSelector.architecture(architectures.toArray(new Architecture[architectures.size()])));
      specified = true;
    }
    if (!specified) {
      throw invalid(context, "a RuntimeCondition must set at least one dimension");
    }
    return selector;
  }

  private static <E extends Enum<E>> List<E> enumValues(Object encoded, String descriptor, Class<E> enumType,
      String context) {
    if (encoded == null) {
      return Collections.emptyList();
    }
    if (!(encoded instanceof List<?>)) {
      throw invalid(context, "expected an array of " + enumType.getSimpleName() + " values");
    }
    List<E> values = new ArrayList<E>();
    for (Object item : (List<?>) encoded) {
      if (!(item instanceof String[])) {
        throw invalid(context, "contains an invalid encoded enum value");
      }
      String[] enumValue = (String[]) item;
      if (enumValue.length != 2 || !descriptor.equals(enumValue[0])) {
        throw invalid(context, "contains an enum value with an unexpected type");
      }
      try {
        values.add(Enum.valueOf(enumType, enumValue[1]));
      } catch (IllegalArgumentException e) {
        throw invalid(context, "contains unknown " + enumType.getSimpleName() + " value '" + enumValue[1] + "'");
      }
    }
    return values;
  }

  private static AnnotationNode nestedAnnotation(Object encoded, String descriptor, String context) {
    if (!(encoded instanceof AnnotationNode)) {
      throw invalid(context, "expected a nested " + Type.getType(descriptor).getClassName() + " annotation");
    }
    AnnotationNode annotation = (AnnotationNode) encoded;
    if (!descriptor.equals(annotation.desc)) {
      throw invalid(context, "has an unexpected annotation type " + annotation.desc);
    }
    return annotation;
  }

  private static List<AnnotationNode> nestedAnnotations(Object encoded, String descriptor, String context) {
    if (encoded == null) {
      return Collections.emptyList();
    }
    if (!(encoded instanceof List<?>)) {
      throw invalid(context, "expected an array of nested annotations");
    }
    List<AnnotationNode> annotations = new ArrayList<AnnotationNode>();
    for (Object item : (List<?>) encoded) {
      annotations.add(nestedAnnotation(item, descriptor, context));
    }
    return annotations;
  }

  private static Map<String, Object> readFields(AnnotationNode annotation, Set<String> allowed, String context) {
    Map<String, Object> fields = new HashMap<String, Object>();
    List<Object> pairs = annotation.values;
    if (pairs == null) {
      return fields;
    }
    if ((pairs.size() & 1) != 0) {
      throw invalid(context, "contains an incomplete annotation field");
    }
    for (int i = 0; i < pairs.size(); i += 2) {
      Object key = pairs.get(i);
      if (!(key instanceof String)) {
        throw invalid(context, "contains an invalid annotation field name");
      }
      String name = (String) key;
      if (!allowed.contains(name)) {
        throw invalid(context, "contains unknown field '" + name + "'");
      }
      if (fields.containsKey(name)) {
        throw invalid(context, "contains duplicate field '" + name + "'");
      }
      fields.put(name, pairs.get(i + 1));
    }
    return fields;
  }

  private static List<AnnotationNode> classAnnotations(ClassNode classNode) {
    List<AnnotationNode> annotations = new ArrayList<AnnotationNode>();
    if (classNode.visibleAnnotations != null) {
      annotations.addAll(classNode.visibleAnnotations);
    }
    if (classNode.invisibleAnnotations != null) {
      annotations.addAll(classNode.invisibleAnnotations);
    }
    return annotations;
  }

  private static IllegalArgumentException invalid(String context, String message) {
    return new IllegalArgumentException("Invalid runtime configuration in " + context + ": " + message);
  }

  private static Set<String> immutableSet(String... fields) {
    return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(fields)));
  }
}
