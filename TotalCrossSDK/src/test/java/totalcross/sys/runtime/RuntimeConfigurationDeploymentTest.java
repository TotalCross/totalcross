// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.Inflater;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.Deploy;
import tc.tools.deployer.DeploySettings;
import totalcross.util.zip.TCZ;

class RuntimeConfigurationDeploymentTest {
  private static final String METADATA_RESOURCE = "tc.runtimeconfig";
  private static final String IMAGE_METADATA_RESOURCE = "tc.imageruntimeconfig";

  @TempDir
  Path workDir;

  @Test
  void storesDeclaredSelectorsAsAReservedTczResource() throws Exception {
    Path classFile = compileApplication();
    Path output = Files.createDirectories(workDir.resolve("output"));
    String previousBootClassPath = Deploy.bootClassPath;
    Deploy.bootClassPath = classFile.getParent().getParent() + java.io.File.pathSeparator
        + System.getProperty("java.class.path");
    DeploySettings.tczs = null;
    DeploySettings.filePrefix = null;
    DeploySettings.targetDir = null;
    DeploySettings.mainClassName = null;
    DeploySettings.runtimeConfigurationSelectors = null;
    DeploySettings.imageRuntimeConfigurationRules = null;

    try {
      new Deploy(new String[] { classFile.toString(), "/o", output.toString() });
    } finally {
      Deploy.bootClassPath = previousBootClassPath;
    }

    assertNotNull(DeploySettings.tczs);
    assertEquals(1, DeploySettings.tczs.length);
    byte[] metadata = readTczEntry(Path.of(DeploySettings.tczs[0]), METADATA_RESOURCE);
    assertNotNull(metadata, "Declared runtime configuration must be written to the application TCZ");
    List<RuntimeSelector> selectors = RuntimeConfigurationMetadata.decode(metadata);
    assertEquals(1, selectors.size());
    assertTrue(selectors.get(0).matches(
        RuntimeEnvironmentTestSupport.environment("WindowsCE", 4, false)));

    byte[] imageMetadata = readTczEntry(Path.of(DeploySettings.tczs[0]), IMAGE_METADATA_RESOURCE);
    assertNotNull(imageMetadata, "Declared Image rules must be written to the application TCZ");
    List<RuntimeConfigurationFeatureBridge.FeatureRule<totalcross.ui.image.ImageStorageProfile>> imageRules =
        ImageRuntimeConfigurationMetadata.decode(imageMetadata);
    assertEquals(1, imageRules.size());
    assertEquals(totalcross.ui.image.ImageStorageProfile.COMPACT, imageRules.get(0).requestedValue());
    assertTrue(imageRules.get(0).selector().matches(
        RuntimeEnvironmentTestSupport.environment("WindowsCE", 4, false)));
  }

  private Path compileApplication() throws Exception {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "A JDK with javac is required for deploy fixtures");
    Path source = workDir.resolve("fixture/RuntimeConfigurationFixture.java");
    Path classes = Files.createDirectories(workDir.resolve("classes"));
    Files.createDirectories(source.getParent());
    Files.writeString(source,
        "package fixture;\n"
            + "import totalcross.sys.Architecture;\n"
            + "import totalcross.sys.Platform;\n"
            + "import totalcross.sys.runtime.RuntimeCondition;\n"
            + "import totalcross.sys.runtime.RuntimeConfiguration;\n"
            + "import totalcross.sys.runtime.RuntimeRule;\n"
            + "import totalcross.sys.runtime.RuntimeWhen;\n"
            + "import totalcross.ui.image.ImageRuntimeRule;\n"
            + "import totalcross.ui.image.ImageStorageProfile;\n"
            + "@RuntimeConfiguration\n"
            + "@RuntimeRule(when = @RuntimeWhen(allOf = {\n"
            + "  @RuntimeCondition(platform = Platform.WINDOWS),\n"
            + "  @RuntimeCondition(architecture = Architecture.ARM64)\n"
            + "}))\n"
            + "@ImageRuntimeRule(when = @RuntimeWhen(allOf = {\n"
            + "  @RuntimeCondition(platform = Platform.WINDOWS),\n"
            + "  @RuntimeCondition(architecture = Architecture.ARM64)\n"
            + "}), storage = ImageStorageProfile.COMPACT)\n"
            + "public class RuntimeConfigurationFixture extends totalcross.ui.MainWindow { }\n",
        StandardCharsets.UTF_8);

    ByteArrayOutputStream compilerOutput = new ByteArrayOutputStream();
    int result = compiler.run(null, compilerOutput, compilerOutput, "-source", "8", "-target", "8", "-classpath",
        System.getProperty("java.class.path"), "-d", classes.toString(), source.toString());
    assertEquals(0, result, compilerOutput.toString(StandardCharsets.UTF_8.name()));
    return classes.resolve("fixture/RuntimeConfigurationFixture.class");
  }

  private static byte[] readTczEntry(Path tczPath, String name) throws Exception {
    totalcross.io.File file = new totalcross.io.File(tczPath.toString(), totalcross.io.File.READ_ONLY);
    try {
      TCZ tcz = new TCZ(file);
      byte[] contents = Files.readAllBytes(tczPath);
      for (int i = 0; i < tcz.numberOfChunks; i++) {
        if (name.equals(tcz.names[i])) {
          Inflater inflater = new Inflater();
          inflater.setInput(contents, tcz.offsets[i], tcz.offsets[i + 1] - tcz.offsets[i]);
          byte[] uncompressed = new byte[tcz.uncompressedSizes[i]];
          try {
            int length = inflater.inflate(uncompressed);
            assertEquals(uncompressed.length, length);
            return uncompressed;
          } finally {
            inflater.end();
          }
        }
      }
      return null;
    } finally {
      file.close();
    }
  }
}
