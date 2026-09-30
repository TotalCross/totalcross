// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import tc.tools.converter.runtimeconfig.RuntimeConfigurationParser;

class RuntimeConfigurationParserTest {
  @RuntimeConfiguration
  @RuntimeRule(when = @RuntimeWhen(allOf = {
      @RuntimeCondition(family = RuntimeFamily.DESKTOP),
      @RuntimeCondition(backend = GraphicsBackend.RASTER)
  }, anyOf = {
      @RuntimeCondition(platform = Platform.WINDOWS),
      @RuntimeCondition(architecture = Architecture.X86_64)
  }))
  @RuntimeRule(when = @RuntimeWhen(allOf = {@RuntimeCondition(platform = Platform.ANDROID)}))
  static class AnnotatedApplication {
  }

  @RuntimeConfiguration
  static class MarkerOnlyApplication {
  }

  static class PlainApplication {
  }

  @RuntimeRule(when = @RuntimeWhen(allOf = {@RuntimeCondition(platform = Platform.WINDOWS)}))
  static class MissingConfigurationMarker {
  }

  @RuntimeConfiguration
  @RuntimeRule(when = @RuntimeWhen(allOf = {@RuntimeCondition()}))
  static class EmptyConditionApplication {
  }

  @Test
  void javacStoresConfigurationAnnotationsInClassMetadata() throws Exception {
    ClassNode classNode = readClass(AnnotatedApplication.class);

    assertTrue(hasAnnotation(classNode.invisibleAnnotations, RuntimeConfiguration.class));
    assertTrue(hasAnnotation(classNode.invisibleAnnotations,
        totalcross.sys.runtime.RuntimeRules.class));
    assertFalse(hasAnnotation(classNode.visibleAnnotations, RuntimeConfiguration.class));
  }

  @Test
  void parsesRepeatableRulesAndNestedAllOfAnyOfConditions() throws Exception {
    List<RuntimeSelector> selectors = RuntimeConfigurationParser.parse(classBytes(AnnotatedApplication.class),
        AnnotatedApplication.class.getName());

    assertEquals(2, selectors.size());
    assertTrue(selectors.get(0).matches(environment("Win32", 2, false)));
    assertTrue(selectors.get(0).matches(
        RuntimeEnvironment.fromRuntimeSettings(null, true, "Linux", "amd64", 0, 0, 1)));
    assertFalse(selectors.get(0).matches(environment("Win32", 1, true)));
    assertTrue(selectors.get(1).matches(environment("Android", 4, true)));
    assertFalse(selectors.get(1).matches(environment("iPhone", 4, true)));
  }

  @Test
  void returnsNoModelForUnannotatedClassAndEmptyRulesForMarkerOnlyClass() throws Exception {
    assertNull(RuntimeConfigurationParser.parse(classBytes(PlainApplication.class), PlainApplication.class.getName()));
    assertTrue(RuntimeConfigurationParser.parse(classBytes(MarkerOnlyApplication.class),
        MarkerOnlyApplication.class.getName()).isEmpty());
  }

  @Test
  void rejectsRulesWithoutMarkerAndConditionsWithoutDimensions() throws Exception {
    IllegalArgumentException missingMarker = assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationParser.parse(classBytes(MissingConfigurationMarker.class),
            MissingConfigurationMarker.class.getName()));
    assertTrue(missingMarker.getMessage().contains("requires @RuntimeConfiguration"));

    IllegalArgumentException emptyCondition = assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationParser.parse(classBytes(EmptyConditionApplication.class),
            EmptyConditionApplication.class.getName()));
    assertTrue(emptyCondition.getMessage().contains("must set at least one dimension"));
  }

  @Test
  void rejectsUnknownEncodedEnumAndAnnotationFields() {
    IllegalArgumentException unknownEnum = assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationParser.parse(classWithPlatform("WINDOWS_FUTURE", null), "sample.UnknownPlatform"));
    assertTrue(unknownEnum.getMessage().contains("unknown Platform value 'WINDOWS_FUTURE'"));

    IllegalArgumentException unavailableValue = assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationParser.parse(classWithPlatform("UNKNOWN", null), "sample.UnknownPlatform"));
    assertTrue(unavailableValue.getMessage().contains("unknown Platform value 'UNKNOWN'"));

    IllegalArgumentException unknownField = assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationParser.parse(classWithPlatform("WINDOWS", "futureDimension"),
            "sample.UnknownDimension"));
    assertTrue(unknownField.getMessage().contains("unknown field 'futureDimension'"));
  }

  @Test
  void encodedConfigurationIsParsedFromBytesWithoutLoadingAnnotatedClass() throws Exception {
    byte[] bytes = classBytes(AnnotatedApplication.class);
    assertNotNull(bytes);
    assertEquals(2, RuntimeConfigurationParser.parse(bytes, "example.Application").size());
  }

  private static RuntimeEnvironment environment(String platform, int architecture, boolean isOpenGL) {
    return RuntimeEnvironmentTestSupport.environment(platform, architecture, isOpenGL);
  }

  private static boolean hasAnnotation(List<?> annotations, Class<?> annotationType) {
    if (annotations == null) {
      return false;
    }
    String descriptor = Type.getDescriptor(annotationType);
    for (Object value : annotations) {
      if (value instanceof AnnotationNode && descriptor.equals(((AnnotationNode) value).desc)) {
        return true;
      }
    }
    return false;
  }

  private static ClassNode readClass(Class<?> type) throws Exception {
    ClassNode node = new ClassNode();
    new ClassReader(asJava8ClassFile(classBytes(type))).accept(node,
        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    return node;
  }

  private static byte[] classBytes(Class<?> type) throws IOException {
    String resource = "/" + type.getName().replace('.', '/') + ".class";
    InputStream input = type.getResourceAsStream(resource);
    if (input == null) {
      throw new IOException("Missing class resource " + resource);
    }
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int count;
      while ((count = input.read(buffer)) != -1) {
        output.write(buffer, 0, count);
      }
      return output.toByteArray();
    } finally {
      input.close();
    }
  }

  private static byte[] asJava8ClassFile(byte[] classBytes) {
    byte[] bytes = classBytes.clone();
    // Direct ASM assertions use the converter's Java 8 class-file reader.
    bytes[6] = 0;
    bytes[7] = 52;
    return bytes;
  }

  private static byte[] classWithPlatform(String platform, String extraField) {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "sample/SyntheticApplication", null, "java/lang/Object", null);
    writer.visitAnnotation(Type.getDescriptor(RuntimeConfiguration.class), false).visitEnd();

    AnnotationVisitor rule = writer.visitAnnotation(Type.getDescriptor(RuntimeRule.class), false);
    AnnotationVisitor when = rule.visitAnnotation("when", Type.getDescriptor(RuntimeWhen.class));
    AnnotationVisitor allOf = when.visitArray("allOf");
    AnnotationVisitor condition = allOf.visitAnnotation(null, Type.getDescriptor(RuntimeCondition.class));
    AnnotationVisitor platforms = condition.visitArray("platform");
    platforms.visitEnum(null, Type.getDescriptor(Platform.class), platform);
    platforms.visitEnd();
    if (extraField != null) {
      condition.visit(extraField, true);
    }
    condition.visitEnd();
    allOf.visitEnd();
    when.visitEnd();
    rule.visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }
}
