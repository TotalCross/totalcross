// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Inflater;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.Deploy;
import tc.tools.deployer.DeploySettings;
import tc.tools.deployer.Deployer4IPhoneIPA;
import totalcross.util.zip.TCZ;

class IOSCertDateArtifactRuntimeTest {
  @TempDir
  Path workDir;

  @Test
  void loadsCertificateDateFromGeneratedTczAndKeepsMissingDateNull() throws Exception {
    Path classFile = compileMainWindowFixture();
    Deployer4IPhoneIPA.resetIosDeploymentState();
    String previousBootClassPath = Deploy.bootClassPath;
    Deploy.bootClassPath = classFile.getParent().getParent() + java.io.File.pathSeparator
        + System.getProperty("java.class.path");

    try {
      new Deploy(new String[] { classFile.toString(), "-ios" });
    } catch (Exception ignored) {
      // The local SDK fixture has no IPA template. The TCZ is generated before packaging.
    } finally {
      Deploy.bootClassPath = previousBootClassPath;
    }

    assertNotNull(DeploySettings.tczs);
    assertTrue(DeploySettings.tczs.length > 0);
    String expectedDate = Settings.iosCertDate.toIso8601();
    String parameters = readTczEntry(Path.of(DeploySettings.tczs[0]), "tcparms.bin");
    assertNotNull(parameters);
    assertTrue(parameters.contains("iosCertDate=" + expectedDate));

    Settings.loadDeploymentParameters(parameters.getBytes(StandardCharsets.UTF_8));
    assertEquals(expectedDate, Settings.iosCertDate.toIso8601());

    Settings.loadDeploymentParameters("applicationId=abcd\n".getBytes(StandardCharsets.UTF_8));
    assertNull(Settings.iosCertDate);
  }

  private Path compileMainWindowFixture() throws Exception {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler);

    Path source = workDir.resolve("fixture/IOSDateRuntimeFixture.java");
    Path classes = workDir.resolve("classes");
    Files.createDirectories(source.getParent());
    Files.createDirectories(classes);
    Files.writeString(source,
        "package fixture; public class IOSDateRuntimeFixture extends totalcross.ui.MainWindow { }\n",
        StandardCharsets.UTF_8);

    int result = compiler.run(null, null, null, "-classpath", System.getProperty("java.class.path"), "-d",
        classes.toString(), source.toString());
    assertEquals(0, result);
    return classes.resolve("fixture/IOSDateRuntimeFixture.class");
  }

  private static String readTczEntry(Path tczPath, String name) throws Exception {
    totalcross.io.File file = new totalcross.io.File(tczPath.toString(), totalcross.io.File.READ_ONLY);
    try {
      TCZ tcz = new TCZ(file);
      for (int i = 0; i < tcz.numberOfChunks; i++) {
        if (name.equals(tcz.names[i])) {
          byte[] contents = Files.readAllBytes(tczPath);
          Inflater inflater = new Inflater();
          inflater.setInput(contents, tcz.offsets[i], tcz.offsets[i + 1] - tcz.offsets[i]);
          byte[] uncompressed = new byte[tcz.uncompressedSizes[i]];
          int length = inflater.inflate(uncompressed);
          inflater.end();
          return new String(uncompressed, 0, length, StandardCharsets.UTF_8);
        }
      }
      return null;
    } finally {
      file.close();
    }
  }
}
