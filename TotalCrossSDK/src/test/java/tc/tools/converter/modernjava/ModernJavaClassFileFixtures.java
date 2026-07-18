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

  static Optional<ModernJavaClassFileFixture> compileJava8StatelessLambdaFixture(Path workDir) throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8StatelessLambda";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public Runnable runnable() { return () -> touch(); }\n" + "  private static void touch() { }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 stateless lambda", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8MethodReferenceFixture(Path workDir) throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8MethodReference";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public interface TextFactory { String get(); }\n"
        + "  public interface TextMapper { String map(" + simpleName + " source); }\n"
        + "  public TextFactory staticReference() { return " + simpleName + "::text; }\n"
        + "  public TextMapper virtualReference() { return " + simpleName + "::value; }\n"
        + "  public TextFactory boundReference(" + simpleName + " source) { return source::value; }\n"
        + "  public static String text() { return \"text\"; }\n"
        + "  public String value() { return \"value\"; }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 method reference", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8ConstructorReferenceFixture(Path workDir) throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8ConstructorReference";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public interface BoxFactory { Box create(String value); }\n"
        + "  public BoxFactory factory() { return Box::new; }\n"
        + "  public static class Box {\n"
        + "    private final String value;\n"
        + "    public Box(String value) { this.value = value; }\n"
        + "    public String value() { return value; }\n"
        + "  }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 constructor reference", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8AltMetafactoryMarkerFixture(Path workDir)
      throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8AltMetafactoryMarker";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public interface TextFactory { String get(); }\n"
        + "  public interface Marker { }\n"
        + "  public TextFactory markerReference() { return (TextFactory & Marker) " + simpleName + "::text; }\n"
        + "  public static String text() { return \"text\"; }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 altMetafactory marker", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8AltMetafactoryBridgeFixture(Path workDir)
      throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8AltMetafactoryBridge";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public interface StringFactory { String get(); }\n"
        + "  public interface ObjectFactory { Object get(); }\n"
        + "  public StringFactory bridgeReference() { return (StringFactory & ObjectFactory) " + simpleName
        + "::text; }\n"
        + "  public static String text() { return \"text\"; }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 altMetafactory bridge", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8SerializableLambdaFixture(Path workDir)
      throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8SerializableLambda";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public interface TextFactory { String get(); }\n"
        + "  public TextFactory serializableReference() { return (TextFactory & java.io.Serializable) " + simpleName
        + "::text; }\n"
        + "  public static String text() { return \"text\"; }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 serializable lambda", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8ReferenceReturnAdaptationFixture(Path workDir)
      throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8ReferenceReturnAdaptation";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public interface ObjectFactory { Object get(); }\n"
        + "  public ObjectFactory factory() { return " + simpleName + "::text; }\n"
        + "  public static String text() { return \"text\"; }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 reference return adaptation", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8ReferenceArgumentAdaptationFixture(Path workDir)
      throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8ReferenceArgumentAdaptation";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public interface ValueMapper<T> { String map(T value); }\n"
        + "  public ValueMapper<String> staticReference() { return " + simpleName + "::trim; }\n"
        + "  public ValueMapper<String> virtualReference() { return String::trim; }\n"
        + "  public static String trim(String value) { return value.trim(); }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 reference argument adaptation", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8PrimitiveAdaptationFixture(Path workDir)
      throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8PrimitiveAdaptation";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public interface Function<T, R> { R apply(T value); }\n"
        + "  public Function<String, Integer> lengthReference() { return String::length; }\n"
        + "  public Function<Integer, Integer> twiceReference() { return " + simpleName + "::twice; }\n"
        + "  public static int twice(int value) { return value * 2; }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 primitive adaptation", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8InheritedVoidMethodReferenceFixture(Path workDir)
      throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8InheritedVoidMethodReference";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "class InheritedMiddle {\n"
        + "  public String inherited() { return \"inherited\"; }\n" + "}\n"
        + "class InheritedBase extends InheritedMiddle { }\n"
        + "public class " + simpleName + " extends InheritedBase {\n"
        + "  public interface VoidAction { void execute(); }\n"
        + "  public VoidAction action() { return this::inherited; }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 inherited void method reference", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8ReturnDiscardFixture(Path workDir) throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8ReturnDiscard";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public interface ReturnDiscardAction { void execute(); }\n"
        + "  public ReturnDiscardAction voidReference() { return this::returnVoid; }\n"
        + "  public ReturnDiscardAction booleanReference() { return this::returnBoolean; }\n"
        + "  public ReturnDiscardAction byteReference() { return this::returnByte; }\n"
        + "  public ReturnDiscardAction charReference() { return this::returnChar; }\n"
        + "  public ReturnDiscardAction shortReference() { return this::returnShort; }\n"
        + "  public ReturnDiscardAction intReference() { return this::returnInt; }\n"
        + "  public ReturnDiscardAction longReference() { return this::returnLong; }\n"
        + "  public ReturnDiscardAction floatReference() { return this::returnFloat; }\n"
        + "  public ReturnDiscardAction doubleReference() { return this::returnDouble; }\n"
        + "  public ReturnDiscardAction objectReference() { return this::returnObject; }\n"
        + "  public ReturnDiscardAction arrayReference() { return this::returnArray; }\n"
        + "  public void returnVoid() { }\n"
        + "  public boolean returnBoolean() { return true; }\n"
        + "  public byte returnByte() { return 7; }\n"
        + "  public char returnChar() { return 'c'; }\n"
        + "  public short returnShort() { return 8; }\n"
        + "  public int returnInt() { return 9; }\n"
        + "  public long returnLong() { return 10L; }\n"
        + "  public float returnFloat() { return 1.5F; }\n"
        + "  public double returnDouble() { return 2.5D; }\n"
        + "  public String returnObject() { return \"object\"; }\n"
        + "  public int[] returnArray() { return new int[] { 11 }; }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 return discard", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava8RetrolambdaRemovalFixture(Path workDir)
      throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava8RetrolambdaRemoval";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public interface TextFactory { String get(); }\n"
        + "  public interface Marker { }\n"
        + "  public interface Mapper<T, R> { R map(T value); }\n"
        + "  public interface BoxFactory { Box create(String value); }\n"
        + "  public Runnable captured(final String value) { return () -> value.length(); }\n"
        + "  public TextFactory staticReference() { return " + simpleName + "::text; }\n"
        + "  public TextFactory markerReference() { return (TextFactory & Marker) " + simpleName + "::text; }\n"
        + "  public Mapper<String, Integer> primitiveReference() { return String::length; }\n"
        + "  public BoxFactory constructorReference() { return Box::new; }\n"
        + "  public static String text() { return \"text\"; }\n"
        + "  public static class Box {\n"
        + "    public Box(String value) { }\n"
        + "  }\n" + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "java 8 retrolambda removal", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava11StringConcatFixture(Path workDir) throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava11StringConcat";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  public String concat(String value, int count) {\n"
        + "    return \"value=\" + value + \", count=\" + count;\n"
        + "  }\n" + "}\n";
    return compile(workDir, JAVA_11, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_11)).intValue(),
        "java 11 string concat", className, source);
  }

  static Optional<List<ModernJavaClassFileFixture>> compileJava11NestmateFixture(Path workDir) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      return Optional.empty();
    }

    int javaRelease = JAVA_11;
    int expectedMajorVersion = ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_11)).intValue();
    String packageName = "fixtures";
    String simpleName = "CompiledJava11Nestmates";
    String className = packageName + "." + simpleName;
    String innerClassName = className + "$Inner";
    String source = "package " + packageName + ";\n" + "public class " + simpleName + " {\n"
        + "  private String secret() { return \"secret\"; }\n"
        + "  public class Inner {\n"
        + "    public String read() { return secret(); }\n"
        + "  }\n"
        + "}\n";

    Path sourceDir = workDir.resolve("src");
    Path classesDir = workDir.resolve("classes-" + javaRelease + "-" + sanitize(className));
    Path sourceFile = sourceDir.resolve(className.replace('.', '/') + ".java");
    Files.createDirectories(sourceFile.getParent());
    Files.createDirectories(classesDir);
    try (Writer writer = new OutputStreamWriter(Files.newOutputStream(sourceFile), StandardCharsets.UTF_8)) {
      writer.write(source);
    }

    CompilationResult result = compileWithOptions(sourceFile, classesDir, "--release", String.valueOf(javaRelease));
    if (!result.succeeded && result.releaseIsUnsupported()) {
      return Optional.empty();
    }
    if (!result.succeeded) {
      throw new AssertionError("Compilation failed for Java 11 nestmate fixture:\n" + result.diagnostics);
    }

    List<ModernJavaClassFileFixture> fixtures = new ArrayList<ModernJavaClassFileFixture>();
    fixtures.add(new ModernJavaClassFileFixture(javaRelease, expectedMajorVersion, "java 11 nestmate outer",
        className, Files.readAllBytes(classesDir.resolve(className.replace('.', '/') + ".class")), true));
    fixtures.add(new ModernJavaClassFileFixture(javaRelease, expectedMajorVersion, "java 11 nestmate inner",
        innerClassName, Files.readAllBytes(classesDir.resolve(innerClassName.replace('.', '/') + ".class")), true));
    return Optional.of(fixtures);
  }

  static Optional<ModernJavaClassFileFixture> compileJava17RecordFixture(Path workDir) throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava17Record";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n"
        + "public record " + simpleName + "(String name, int count) {\n"
        + "  public int total() { return name.length() + count; }\n"
        + "}\n";
    return compile(workDir, JAVA_17, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_17)).intValue(),
        "java 17 record metadata", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileJava17SealedFixture(Path workDir) throws IOException {
    String packageName = "fixtures";
    String simpleName = "CompiledJava17Sealed";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n"
        + "public sealed class " + simpleName + " permits " + simpleName + ".Allowed {\n"
        + "  public int value() { return 17; }\n"
        + "  public static final class Allowed extends " + simpleName + " { }\n"
        + "}\n";
    return compile(workDir, JAVA_17, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_17)).intValue(),
        "java 17 sealed metadata", className, source);
  }

  static Optional<ModernJavaClassFileFixture> compileTCIRPocFixture(Path workDir) throws IOException {
    String packageName = "fixtures";
    String simpleName = "TCIRPoc";
    String className = packageName + "." + simpleName;
    String source = "package " + packageName + ";\n"
        + "public final class " + simpleName + " {\n"
        + "  public static int add(int left, int right) {\n"
        + "    return left + right;\n"
        + "  }\n"
        + "  public static int abs(int value) {\n"
        + "    return value < 0 ? -value : value;\n"
        + "  }\n"
        + "  public static int sumTo(int limit) {\n"
        + "    int sum = 0;\n"
        + "    for (int value = 0; value < limit; value++) {\n"
        + "      sum += value;\n"
        + "    }\n"
        + "    return sum;\n"
        + "  }\n"
        + "  public static int pureI32(int value, int distance) {\n"
        + "    int mixed = ((value << 5) & 0x5a5)\n"
        + "        ^ ((value >> 5) | 0x123)\n"
        + "        ^ ((value >>> 5) ^ -1);\n"
        + "    mixed = (mixed & value) | distance;\n"
        + "    mixed = (mixed << distance) ^ (mixed >> distance) ^ (mixed >>> distance);\n"
        + "    return (byte) mixed ^ (char) mixed ^ (short) mixed;\n"
        + "  }\n"
        + "  public static long pureI64(long value, int distance) {\n"
        + "    long shift = distance;\n"
        + "    long mixed = ((value << shift) & 0x5a5L)\n"
        + "        ^ ((value >> shift) | 0x123L)\n"
        + "        ^ (value >>> shift);\n"
        + "    mixed = (mixed + value) * 3L - shift;\n"
        + "    mixed ^= (long) (int) mixed;\n"
        + "    return mixed >= value ? mixed : value;\n"
        + "  }\n"
        + "  public static double pureF64(double left, double right) {\n"
        + "    double mixed = (left + right) * 3.0 - left;\n"
        + "    return mixed >= right ? mixed : right;\n"
        + "  }\n"
        + "  public static float normalizedF32(float value) {\n"
        + "    float score = 0.0f;\n"
        + "    if (value == 0.0f) score += 1.0f;\n"
        + "    if (value != 0.0f) score += 2.0f;\n"
        + "    if (value < 0.0f) score += 4.0f;\n"
        + "    if (value <= 0.0f) score += 8.0f;\n"
        + "    if (value > 0.0f) score += 16.0f;\n"
        + "    if (value >= 0.0f) score += 32.0f;\n"
        + "    return score;\n"
        + "  }\n"
        + "  public static double i32ToF64(int value) {\n"
        + "    return (double) value;\n"
        + "  }\n"
        + "  public static double i64ToF64(long value) {\n"
        + "    return (double) value;\n"
        + "  }\n"
        + "  public static Object selectRef(Object left, Object right) {\n"
        + "    Object selected = null;\n"
        + "    if (left != null) selected = left;\n"
        + "    if (selected == null) selected = right;\n"
        + "    return selected;\n"
        + "  }\n"
        + "  public static int referenceScore(Object left, Object right) {\n"
        + "    int score = 0;\n"
        + "    if (left == right) score += 1;\n"
        + "    if (left != right) score += 2;\n"
        + "    if (left == null) score += 4;\n"
        + "    if (left != null) score += 8;\n"
        + "    return score;\n"
        + "  }\n"
        + "  public static Object nullRef(Object ignored) {\n"
        + "    return null;\n"
        + "  }\n"
        + "  public static int switchScore(int value) {\n"
        + "    switch (value) {\n"
        + "      case -7: return 11;\n"
        + "      case 0: return 22;\n"
        + "      case 5: return 33;\n"
        + "      case 1024: return 44;\n"
        + "      default: return -1;\n"
        + "    }\n"
        + "  }\n"
        + "  public static int callStatic(int left, int right) {\n"
        + "    return callTarget(left, right);\n"
        + "  }\n"
        + "  private static int callTarget(int left, int right) {\n"
        + "    return left + right;\n"
        + "  }\n"
        + "  public static Object newObject() {\n"
        + "    return new Object();\n"
        + "  }\n"
        + "}\n";
    return compile(workDir, JAVA_8, ROADMAP_MAJOR_VERSIONS.get(Integer.valueOf(JAVA_8)).intValue(),
        "TCIR POC converter output", className, source);
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

  static byte[] minimalClassFile(String internalName, int majorVersion, int minorVersion) throws IOException {
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

  static byte[] classFileWithUnknownClassAttribute(String internalName, int majorVersion) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytes);
    out.writeInt(0xCAFEBABE);
    out.writeShort(0);
    out.writeShort(majorVersion);

    out.writeShort(11);
    writeRef(out, 10, 2, 3);
    writeClass(out, 4);
    writeNameAndType(out, 5, 6);
    writeUtf8(out, "java/lang/Object");
    writeUtf8(out, "<init>");
    writeUtf8(out, "()V");
    writeClass(out, 8);
    writeUtf8(out, internalName);
    writeUtf8(out, "Code");
    writeUtf8(out, "UnknownModernAttribute");

    writeMinimalClassBody(out, 7, 2, 9);
    out.writeShort(1);
    out.writeShort(10);
    out.writeInt(4);
    out.writeInt(0x12345678);
    out.flush();
    return bytes.toByteArray();
  }

  static byte[] classFileWithModernConstantPoolTags(String internalName, int majorVersion) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytes);
    out.writeInt(0xCAFEBABE);
    out.writeShort(0);
    out.writeShort(majorVersion);

    out.writeShort(18);
    writeRef(out, 10, 2, 3);
    writeClass(out, 4);
    writeNameAndType(out, 5, 6);
    writeUtf8(out, "java/lang/Object");
    writeUtf8(out, "<init>");
    writeUtf8(out, "()V");
    writeClass(out, 8);
    writeUtf8(out, internalName);
    writeUtf8(out, "Code");
    writeUtf8(out, "fixture.module");
    writeClassLike(out, 19, 10);
    writeUtf8(out, "fixture/package");
    writeClassLike(out, 20, 12);
    writeUtf8(out, "DYNAMIC_CONSTANT");
    writeUtf8(out, "I");
    writeNameAndType(out, 14, 15);
    writeRef(out, 17, 0, 16);

    writeMinimalClassBody(out, 7, 2, 9);
    out.writeShort(0);
    out.flush();
    return bytes.toByteArray();
  }

  static byte[] moduleInfoClassFile(String moduleName, int majorVersion) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytes);
    out.writeInt(0xCAFEBABE);
    out.writeShort(0);
    out.writeShort(majorVersion);

    out.writeShort(8);
    writeUtf8(out, "module-info");
    writeClass(out, 1);
    writeUtf8(out, "Module");
    writeUtf8(out, moduleName);
    writeClassLike(out, 19, 4);
    writeUtf8(out, "java.base");
    writeClassLike(out, 19, 6);

    out.writeShort(0x8000);
    out.writeShort(2);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(1);
    out.writeShort(3);
    out.writeInt(22);
    out.writeShort(5);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(1);
    out.writeShort(7);
    out.writeShort(0x8000);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(0);
    out.flush();
    return bytes.toByteArray();
  }

  static byte[] classFileWithLdcDynamicConstant(String internalName, int majorVersion) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytes);
    out.writeInt(0xCAFEBABE);
    out.writeShort(0);
    out.writeShort(majorVersion);

    out.writeShort(24);
    writeRef(out, 10, 2, 3);
    writeClass(out, 4);
    writeNameAndType(out, 5, 6);
    writeUtf8(out, "java/lang/Object");
    writeUtf8(out, "<init>");
    writeUtf8(out, "()V");
    writeClass(out, 8);
    writeUtf8(out, internalName);
    writeUtf8(out, "Code");
    writeUtf8(out, "BootstrapMethods");
    writeUtf8(out, "dynamicValue");
    writeUtf8(out, "Ljava/lang/String;");
    writeNameAndType(out, 11, 12);
    writeRef(out, 17, 0, 13);
    writeClass(out, 16);
    writeUtf8(out, "java/lang/invoke/ConstantBootstraps");
    writeUtf8(out, "nullConstant");
    writeUtf8(out, "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
    writeNameAndType(out, 17, 18);
    writeRef(out, 10, 15, 19);
    out.writeByte(15);
    out.writeByte(6);
    out.writeShort(20);
    writeUtf8(out, "value");
    writeUtf8(out, "()Ljava/lang/String;");

    out.writeShort(0x0021);
    out.writeShort(7);
    out.writeShort(2);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(2);
    writeDefaultConstructor(out, 9);
    out.writeShort(0x0001);
    out.writeShort(22);
    out.writeShort(23);
    out.writeShort(1);
    out.writeShort(9);
    out.writeInt(15);
    out.writeShort(1);
    out.writeShort(1);
    out.writeInt(3);
    out.writeByte(0x12);
    out.writeByte(14);
    out.writeByte(0xB0);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(1);
    out.writeShort(10);
    out.writeInt(6);
    out.writeShort(1);
    out.writeShort(21);
    out.writeShort(0);
    out.flush();
    return bytes.toByteArray();
  }

  private static void writeDefaultConstructor(DataOutputStream out, int codeAttributeNameIndex) throws IOException {
    out.writeShort(0x0001);
    out.writeShort(5);
    out.writeShort(6);
    out.writeShort(1);
    out.writeShort(codeAttributeNameIndex);
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

  private static void writeClassLike(DataOutputStream out, int tag, int nameIndex) throws IOException {
    out.writeByte(tag);
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

  private static void writeMinimalClassBody(DataOutputStream out, int thisClassIndex, int superClassIndex,
      int codeAttributeNameIndex) throws IOException {
    out.writeShort(0x0021);
    out.writeShort(thisClassIndex);
    out.writeShort(superClassIndex);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(1);
    out.writeShort(0x0001);
    out.writeShort(5);
    out.writeShort(6);
    out.writeShort(1);
    out.writeShort(codeAttributeNameIndex);
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
