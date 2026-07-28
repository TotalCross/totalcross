// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.tools.deployer.DeploySettings;
import tc.tools.deployer.Deployer4IPhoneIPA;
import totalcross.sys.Settings;
import totalcross.util.zip.TCZ;

class IOSCertDateDeploymentTest {
  @TempDir
  Path workDir;

  @Test
  void writesCertificateDateBeforeTczGeneration() throws Exception {
    Path classFile = compileMainWindowFixture();
    resetIosDeploymentState();
    String previousBootClassPath = Deploy.bootClassPath;
    Deploy.bootClassPath = classFile.getParent().getParent() + java.io.File.pathSeparator
        + System.getProperty("java.class.path");

    try {
      new Deploy(new String[] { classFile.toString(), "-ios" });
    } catch (Exception ignored) {
      // The SDK test fixture intentionally has no IPA template. The TCZ is written before IPA packaging starts.
    } finally {
      Deploy.bootClassPath = previousBootClassPath;
    }

    assertNotNull(DeploySettings.tczs, "Deploy must write the TCZ before attempting to package the IPA");
    assertTrue(DeploySettings.tczs.length > 0, "Deploy must report the generated TCZ");
    assertNotNull(Settings.iosCertDate, "The configured iOS signing material must provide a date");

    String discoveredDate = Settings.iosCertDate.toIso8601();
    Deployer4IPhoneIPA.iosMetadataInit();
    assertEquals(discoveredDate, Settings.iosCertDate.toIso8601(),
        "Repeated iOS metadata discovery must preserve the provisioning profile expiration date");

    String parameters = readTczEntry(Path.of(DeploySettings.tczs[0]), "tcparms.bin");
    assertNotNull(parameters, "The generated application TCZ must contain tcparms.bin");
    assertTrue(parameters.contains("iosCertDate=" + Settings.iosCertDate.toIso8601()),
        "tcparms.bin must contain the iOS certificate date discovered for the deploy");
  }

  private Path compileMainWindowFixture() throws Exception {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "A JDK with javac is required for deploy fixtures");

    Path source = workDir.resolve("fixture/IOSDateFixture.java");
    Path classes = workDir.resolve("classes");
    Files.createDirectories(source.getParent());
    Files.createDirectories(classes);
    Files.writeString(source,
        "package fixture; public class IOSDateFixture extends totalcross.ui.MainWindow { }\n",
        StandardCharsets.UTF_8);

    int result = compiler.run(null, null, null, "-classpath", System.getProperty("java.class.path"), "-d",
        classes.toString(), source.toString());
    assertTrue(result == 0, "The deploy fixture must compile");
    return classes.resolve("fixture/IOSDateFixture.class");
  }

  private static void resetIosDeploymentState() {
    Settings.iosCertDate = null;
    Deployer4IPhoneIPA.buildIPA = false;
    Deployer4IPhoneIPA.certStorePath = null;
    Deployer4IPhoneIPA.mobileProvision = null;
    Deployer4IPhoneIPA.appleCertStore = null;
    Deployer4IPhoneIPA.iosKeyStore = null;
    Deployer4IPhoneIPA.iosDistributionCertificate = null;
  }

  private static String readTczEntry(Path tczPath, String name) throws Exception {
    totalcross.io.File file = new totalcross.io.File(tczPath.toString(), totalcross.io.File.READ_ONLY);
    try {
      TCZ tcz = new TCZ(file);
      for (int i = 0; i < tcz.numberOfChunks; i++) {
        if (name.equals(tcz.names[i])) {
          byte[] contents = Files.readAllBytes(tczPath);
          java.util.zip.Inflater inflater = new java.util.zip.Inflater();
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
