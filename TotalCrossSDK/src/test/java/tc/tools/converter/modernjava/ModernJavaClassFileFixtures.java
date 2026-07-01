// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

final class ModernJavaClassFileFixtures {
  static final int JAVA_8 = 8;
  static final int JAVA_11 = 11;
  static final int JAVA_17 = 17;
  static final int JAVA_21 = 21;
  static final int JAVA_25 = 25;
  static final int JAVA_26 = 26;

  static final Map<Integer, Integer> ROADMAP_MAJOR_VERSIONS = roadmapMajorVersions();

  private ModernJavaClassFileFixtures() {
  }

  static List<ModernJavaClassFileFixture> generatedRoadmapFixtures() throws IOException {
    List<ModernJavaClassFileFixture> fixtures = new ArrayList<ModernJavaClassFileFixture>();
    for (Map.Entry<Integer, Integer> entry : ROADMAP_MAJOR_VERSIONS.entrySet()) {
      int release = entry.getKey().intValue();
      int major = entry.getValue().intValue();
      String className = "fixtures.GeneratedJava" + release;
      fixtures.add(new ModernJavaClassFileFixture(release, major, "minimal class file", className,
          minimalClassFile(className.replace('.', '/'), major, 0), false));
    }
    return fixtures;
  }

  static Optional<ModernJavaClassFileFixture> compileSimpleFixture(Path workDir, int javaRelease) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      return Optional.empty();
    }
    Integer major = ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(javaRelease));
    if (major == null) {
      throw new IllegalArgumentException("Unsupported Java release in fixture: " + javaRelease);
    }

    String packageName = "fixtures";
    String simpleName = "CompiledJava" + javaRelease;
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public String value() { return \"java-" + javaRelease + "\"; }\n" + "}\n";

    return compile(workDir, javaRelease, major.intValue(), "simple javac class", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8LambdaFixture(Path workDir) throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8Lambda";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public Runnable runnable(final String value) { return () -> value.length(); }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 lambda", className, source);
  }

  private static Optional<ModernJavaClassFileFixture> compile(Path workDir, int javaRelease, int expectedMajorVersion,
      String featureName, String className, String source) throws IOException {
    Path sourceDir = workDir.resolve("src");
    Path classesDir = workDir.resolve("classes-" + javaRelease + "-" + sanitize(className));
    Path sourceFile = sourceDir.resolve(className.replace('.', '/') + ".java");
    Files.createDirectories(sourceFile.getParent());
    Files.createDirectories(classesDir);
    try (Writer writer = new OutputStreamWriter(Files.newOutputStream(sourceFile), StandardCharsets.UTF_8)) {
      writer.write(source);
    }

    CompilationResult result;
    try {
      result = compileWithOptions(sourceFile, classesDir, "--release", String.valueOf(javaRelease));
    } catch (IllegalArgumentException e) {
      if (isUnsupportedRelease(e)) {
        return Optional.empty();
      }
      throw e;
    }
    if (!result.succeeded && javaRelease == JAVA_8) {
      result = compileWithOptions(sourceFile, classesDir, "-source", "1.8", "-target", "1.8");
    }
    if (!result.succeeded && result.releaseIsUnsupported()) {
      return Optional.empty();
    }
    if (!result.succeeded) {
      throw new AssertionError("Compilation failed for Java " + javaRelease + " fixture:\n" + result.diagnostics);
    }

    Path classFile = classesDir.resolve(className.replace('.', '/') + ".class");
    return Optional.of(new ModernJavaClassFileFixture(javaRelease, expectedMajorVersion, featureName, className,
        Files.readAllBytes(classFile), true));
  }

  private static CompilationResult compileWithOptions(Path sourceFile, Path classesDir, String... releaseOptions)
      throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ENGLISH,
        StandardCharsets.UTF_8)) {
      List<String> options = new ArrayList<String>();
      options.add("-d");
      options.add(classesDir.toString());
      Collections.addAll(options, releaseOptions);
      Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(
          Collections.singletonList(sourceFile.toFile()));
      Boolean succeeded = compiler.getTask(null, fileManager, diagnostics, options, null, units).call();
      return new CompilationResult(Boolean.TRUE.equals(succeeded), diagnostics.getDiagnostics());
    }
  }

  private static byte[] minimalClassFile(String internalName, int majorVersion, int minorVersion) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytes);
    out.writeInt(0xCAFEBABE);
    out.writeShort(minorVersion);
    out.writeShort(majorVersion);

    out.writeShort(10);
    writeRef(out, 10, 2, 3);
    writeClass(out, 4);
    writeNameAndType(out, 5, 6);
    writeUtf8(out, "java/lang/Object");
    writeUtf8(out, "<init>");
    writeUtf8(out, "()V");
    writeClass(out, 8);
    writeUtf8(out, internalName);
    writeUtf8(out, "Code");

    out.writeShort(0x0021);
    out.writeShort(7);
    out.writeShort(2);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(1);
    out.writeShort(0x0001);
    out.writeShort(5);
    out.writeShort(6);
    out.writeShort(1);
    out.writeShort(9);
    out.writeInt(17);
    out.writeShort(1);
    out.writeShort(1);
    out.writeInt(5);
    out.writeByte(0x2A);
    out.writeByte(0xB7);
    out.writeShort(1);
    out.writeByte(0xB1);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(0);
    out.flush();
    return bytes.toByteArray();
  }

  private static void writeRef(DataOutputStream out, int tag, int classIndex, int nameAndTypeIndex) throws IOException {
    out.writeByte(tag);
    out.writeShort(classIndex);
    out.writeShort(nameAndTypeIndex);
  }

  private static void writeClass(DataOutputStream out, int nameIndex) throws IOException {
    out.writeByte(7);
    out.writeShort(nameIndex);
  }

  private static void writeNameAndType(DataOutputStream out, int nameIndex, int descriptorIndex) throws IOException {
    out.writeByte(12);
    out.writeShort(nameIndex);
    out.writeShort(descriptorIndex);
  }

  private static void writeUtf8(DataOutputStream out, String value) throws IOException {
    out.writeByte(1);
    out.writeUTF(value);
  }

  private static String sanitize(String className) {
    return className.replace('.', '-').replace('$', '-');
  }

  private static boolean isUnsupportedRelease(IllegalArgumentException e) {
    String message = e.getMessage();
    return message != null && message.toLowerCase(Locale.ENGLISH).contains("release version")
        && message.toLowerCase(Locale.ENGLISH).contains("not supported");
  }

  private static Map<Integer, Integer> roadmapMajorVersions() {
    Map<Integer, Integer> versions = new LinkedHashMap<Integer, Integer>();
    versions.put(Integer.valueOf(JAVA_8), Integer.valueOf(52));
    versions.put(Integer.valueOf(JAVA_11), Integer.valueOf(55));
    versions.put(Integer.valueOf(JAVA_17), Integer.valueOf(61));
    versions.put(Integer.valueOf(JAVA_21), Integer.valueOf(65));
    versions.put(Integer.valueOf(JAVA_25), Integer.valueOf(69));
    versions.put(Integer.valueOf(JAVA_26), Integer.valueOf(70));
    return Collections.unmodifiableMap(versions);
  }

  private static final class CompilationResult {
    final boolean succeeded;
    final List<Diagnostic<? extends JavaFileObject>> diagnostics;

    CompilationResult(boolean succeeded, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
      this.succeeded = succeeded;
      this.diagnostics = diagnostics;
    }

    boolean releaseIsUnsupported() {
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
        String message = diagnostic.getMessage(Locale.ENGLISH).toLowerCase(Locale.ENGLISH);
        if (message.contains("release version") && message.contains("not supported")) {
          return true;
        }
        if (message.contains("invalid source release") || message.contains("invalid target release")) {
          return true;
        }
      }
      return false;
    }
  }
}
