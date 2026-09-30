// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import tc.tools.converter.runtimeconfig.ImageRuntimeConfigurationParser;
import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.runtime.RuntimeConfigurationFeatureBridge.FeatureRule;
import totalcross.ui.image.ImageRuntimeRule;
import totalcross.ui.image.ImageRuntimeRules;
import totalcross.ui.image.ImageStorageProfile;

class ImageRuntimeConfigurationParserTest {
  private static final String INITIALIZED_PROPERTY = "totalcross.imageRuntimeConfigurationFixture.initialized";

  @RuntimeConfiguration
  @ImageRuntimeRule(when = @RuntimeWhen(allOf = {
      @RuntimeCondition(platform = Platform.MACOS),
      @RuntimeCondition(family = RuntimeFamily.DESKTOP),
      @RuntimeCondition(backend = GraphicsBackend.RASTER),
      @RuntimeCondition(architecture = Architecture.ARM64)
  }), storage = ImageStorageProfile.COMPACT)
  static class SingleRuleApplication {
    static {
      System.setProperty(INITIALIZED_PROPERTY, "loaded");
    }
  }

  @RuntimeConfiguration
  @ImageRuntimeRule(when = @RuntimeWhen(allOf = { @RuntimeCondition(family = RuntimeFamily.DESKTOP) }),
      storage = ImageStorageProfile.COMPACT)
  @ImageRuntimeRule(when = @RuntimeWhen(allOf = { @RuntimeCondition(platform = Platform.MACOS) }),
      storage = ImageStorageProfile.STANDARD)
  static class RepeatedRuleApplication {
  }

  static class PlainApplication {
  }

  @ImageRuntimeRule(when = @RuntimeWhen(allOf = { @RuntimeCondition(platform = Platform.MACOS) }),
      storage = ImageStorageProfile.COMPACT)
  static class MissingConfigurationMarker {
  }

  @AfterEach
  void cleanup() {
    System.clearProperty(INITIALIZED_PROPERTY);
  }

  @Test
  void parsesTypedSelectorAndDirectRuleWithoutInitializingEntryClass() throws Exception {
    System.clearProperty(INITIALIZED_PROPERTY);
    List<FeatureRule<ImageStorageProfile>> rules = ImageRuntimeConfigurationParser.parse(
        classBytes(SingleRuleApplication.class), SingleRuleApplication.class.getName());

    assertNull(System.getProperty(INITIALIZED_PROPERTY));
    assertEquals(1, rules.size());
    assertEquals("image-rule-0", rules.get(0).name());
    assertEquals(ImageStorageProfile.COMPACT, rules.get(0).requestedValue());
    RuntimeEnvironment macArmRaster = RuntimeEnvironmentTestSupport.environment("MacOS", 4, false);
    assertTrue(rules.get(0).selector().matches(macArmRaster));
    assertFalse(rules.get(0).selector().matches(RuntimeEnvironmentTestSupport.environment("Linux", 4, false)));
  }

  @Test
  void parsesJavacRepeatableContainerWithStableDiagnosticNames() throws Exception {
    List<FeatureRule<ImageStorageProfile>> rules = ImageRuntimeConfigurationParser.parse(
        classBytes(RepeatedRuleApplication.class), RepeatedRuleApplication.class.getName());

    assertEquals(2, rules.size());
    assertEquals("image-rule-0", rules.get(0).name());
    assertEquals("image-rule-1", rules.get(1).name());
    assertEquals(ImageStorageProfile.COMPACT, rules.get(0).requestedValue());
    assertEquals(ImageStorageProfile.STANDARD, rules.get(1).requestedValue());
    assertNull(ImageRuntimeConfigurationParser.parse(classBytes(PlainApplication.class),
        PlainApplication.class.getName()));
  }

  @Test
  void rejectsMissingMarkerRequiredFieldsUnknownEnumsAndMixedEncodings() throws Exception {
    IllegalArgumentException marker = assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationParser.parse(classBytes(MissingConfigurationMarker.class),
            MissingConfigurationMarker.class.getName()));
    assertTrue(marker.getMessage().contains("requires @RuntimeConfiguration"));

    IllegalArgumentException missingWhen = assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationParser.parse(classWithImageRule(false, true, false, false), "sample.MissingWhen"));
    assertTrue(missingWhen.getMessage().contains("missing required field 'when'"));

    IllegalArgumentException missingStorage = assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationParser.parse(classWithImageRule(true, false, false, false), "sample.MissingStorage"));
    assertTrue(missingStorage.getMessage().contains("missing required field 'storage'"));

    IllegalArgumentException unknownStorage = assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationParser.parse(classWithImageRule(true, true, true, false), "sample.UnknownStorage"));
    assertTrue(unknownStorage.getMessage().contains("unknown ImageStorageProfile value 'TINY'"));

    IllegalArgumentException mixed = assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationParser.parse(classWithImageRule(true, true, false, true), "sample.MixedRules"));
    assertTrue(mixed.getMessage().contains("cannot be mixed"));
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

  private static byte[] classWithImageRule(boolean includeWhen, boolean includeStorage, boolean unknownStorage,
      boolean addContainerToo) {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "sample/Generated", null, "java/lang/Object", null);
    writer.visitAnnotation(Type.getDescriptor(RuntimeConfiguration.class), false).visitEnd();
    AnnotationVisitor rule = writer.visitAnnotation(Type.getDescriptor(ImageRuntimeRule.class), false);
    writeRuleFields(rule, includeWhen, includeStorage, unknownStorage);
    rule.visitEnd();
    if (addContainerToo) {
      AnnotationVisitor container = writer.visitAnnotation(Type.getDescriptor(ImageRuntimeRules.class), false);
      AnnotationVisitor values = container.visitArray("value");
      AnnotationVisitor nestedRule = values.visitAnnotation(null, Type.getDescriptor(ImageRuntimeRule.class));
      writeRuleFields(nestedRule, true, true, false);
      nestedRule.visitEnd();
      values.visitEnd();
      container.visitEnd();
    }
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static void writeRuleFields(AnnotationVisitor rule, boolean includeWhen, boolean includeStorage,
      boolean unknownStorage) {
    if (includeWhen) {
      rule.visitAnnotation("when", Type.getDescriptor(RuntimeWhen.class)).visitEnd();
    }
    if (includeStorage) {
      rule.visitEnum("storage", Type.getDescriptor(ImageStorageProfile.class),
          unknownStorage ? "TINY" : ImageStorageProfile.STANDARD.name());
    }
  }
}
