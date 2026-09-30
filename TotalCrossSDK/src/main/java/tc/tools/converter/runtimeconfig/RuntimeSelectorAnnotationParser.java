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
import totalcross.sys.runtime.RuntimeSelector;
import totalcross.sys.runtime.RuntimeWhen;

/** Shared bytecode parser for selector annotations and their nested values. */
final class RuntimeSelectorAnnotationParser {
  private static final String WHEN = Type.getDescriptor(RuntimeWhen.class);
  private static final String CONDITION = Type.getDescriptor(RuntimeCondition.class);
  private static final Set<String> WHEN_FIELDS = immutableSet("allOf", "anyOf");
  private static final Set<String> CONDITION_FIELDS = immutableSet("platform", "family", "backend", "architecture");

  private RuntimeSelectorAnnotationParser() {
  }

  static ClassNode readClassNode(byte[] classBytes, String className, String... annotationDescriptors) {
    if (classBytes == null || classBytes.length == 0) {
      throw new IllegalArgumentException("class bytes are required to parse runtime configuration");
    }
    byte[] asmBytes = normalizeClassVersionForMetadata(classBytes, annotationDescriptors);
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
    return classNode;
  }

  static List<AnnotationNode> classAnnotations(ClassNode classNode) {
    List<AnnotationNode> annotations = new ArrayList<AnnotationNode>();
    if (classNode.visibleAnnotations != null) {
      annotations.addAll(classNode.visibleAnnotations);
    }
    if (classNode.invisibleAnnotations != null) {
      annotations.addAll(classNode.invisibleAnnotations);
    }
    return annotations;
  }

  static RuntimeSelector parseWhen(AnnotationNode annotation, String context) {
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

  static <E extends Enum<E>> List<E> enumValues(Object encoded, String descriptor, Class<E> enumType,
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
      values.add(enumValue(item, descriptor, enumType, context));
    }
    return values;
  }

  static <E extends Enum<E>> E enumValue(Object encoded, String descriptor, Class<E> enumType, String context) {
    if (!(encoded instanceof String[])) {
      throw invalid(context, "expected a " + enumType.getSimpleName() + " value");
    }
    String[] enumValue = (String[]) encoded;
    if (enumValue.length != 2 || !descriptor.equals(enumValue[0])) {
      throw invalid(context, "contains an enum value with an unexpected type");
    }
    try {
      return Enum.valueOf(enumType, enumValue[1]);
    } catch (IllegalArgumentException e) {
      throw invalid(context, "contains unknown " + enumType.getSimpleName() + " value '" + enumValue[1] + "'");
    }
  }

  static AnnotationNode nestedAnnotation(Object encoded, String descriptor, String context) {
    if (!(encoded instanceof AnnotationNode)) {
      throw invalid(context, "expected a nested " + Type.getType(descriptor).getClassName() + " annotation");
    }
    AnnotationNode annotation = (AnnotationNode) encoded;
    if (!descriptor.equals(annotation.desc)) {
      throw invalid(context, "has an unexpected annotation type " + annotation.desc);
    }
    return annotation;
  }

  static List<AnnotationNode> nestedAnnotations(Object encoded, String descriptor, String context) {
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

  static Map<String, Object> readFields(AnnotationNode annotation, Set<String> allowed, String context) {
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

  static IllegalArgumentException invalid(String context, String message) {
    return new IllegalArgumentException("Invalid runtime configuration in " + context + ": " + message);
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

  private static byte[] normalizeClassVersionForMetadata(byte[] classBytes, String... annotationDescriptors) {
    if (classBytes.length < 8) {
      return classBytes;
    }
    int majorVersion = ((classBytes[6] & 0xff) << 8) | (classBytes[7] & 0xff);
    if (majorVersion <= 52) {
      return classBytes;
    }
    for (String annotationDescriptor : annotationDescriptors) {
      if (contains(classBytes, annotationDescriptor)) {
        byte[] normalized = Arrays.copyOf(classBytes, classBytes.length);
        normalized[6] = 0;
        normalized[7] = 52;
        return normalized;
      }
    }
    return null;
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

  private static Set<String> immutableSet(String... fields) {
    return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(fields)));
  }
}
