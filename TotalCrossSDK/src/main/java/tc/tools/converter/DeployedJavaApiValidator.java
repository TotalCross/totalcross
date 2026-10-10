// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.bytecode.MethodCall;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaMethod;

/**
 * Validates host-compiled bytecode against the Java API model available to a
 * deployed TotalCross application.
 */
public final class DeployedJavaApiValidator {
  private static final int JVM_INVOKEDYNAMIC = 186;

  private DeployedJavaApiValidator() {
  }

  public static List<Problem> validateClassBytes(byte[]... classBytes) throws Exception {
    return validate(parse(classBytes));
  }

  public static List<Problem> validatePaths(List<Path> roots) throws Exception {
    List<byte[]> classBytes = new ArrayList<byte[]>();
    for (Path root : roots) {
      collectClassBytes(root, classBytes);
    }

    List<JavaClass> parsed =
        parse(classBytes.toArray(new byte[classBytes.size()][]));
    List<JavaClass> deviceClasses = DeviceTypeMapping.selectDeployedClasses(parsed);
    List<JavaClass> callers = new ArrayList<JavaClass>();
    for (JavaClass type : deviceClasses) {
      if (isSdkCallerClass(type.originalClassName)) {
        callers.add(type);
      }
    }
    return validate(callers, deviceClasses);
  }

  private static List<JavaClass> parse(byte[]... classBytes) throws Exception {
    ByteCode.initClasses();
    List<JavaClass> classes = new ArrayList<JavaClass>(classBytes.length);
    for (byte[] bytes : classBytes) {
      classes.add(new JavaClass(bytes, false, true));
    }
    return classes;
  }

  private static void collectClassBytes(Path root, List<byte[]> destination)
      throws IOException {
    if (Files.isRegularFile(root) && root.toString().endsWith(".class")) {
      destination.add(Files.readAllBytes(root));
      return;
    }
    if (Files.isRegularFile(root)
        && (root.toString().endsWith(".jar") || root.toString().endsWith(".zip"))) {
      collectArchiveClassBytes(root, destination);
      return;
    }
    if (!Files.isDirectory(root)) {
      return;
    }

    try (Stream<Path> paths = Files.walk(root)) {
      paths.filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".class"))
          .filter(path -> isDeviceModelCandidate(root.relativize(path).toString()))
          .forEach(path -> {
            try {
              destination.add(Files.readAllBytes(path));
            } catch (IOException e) {
              throw new ClassReadFailure(path, e);
            }
          });
    } catch (ClassReadFailure failure) {
      throw failure.cause;
    }
  }

  private static void collectArchiveClassBytes(
      Path archive, List<byte[]> destination) throws IOException {
    try (JarFile jar = new JarFile(archive.toFile())) {
      java.util.Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.isDirectory()
            || !entry.getName().endsWith(".class")
            || !isDeviceModelCandidate(entry.getName())) {
          continue;
        }
        try (InputStream input = jar.getInputStream(entry)) {
          destination.add(input.readAllBytes());
        }
      }
    }
  }

  static boolean isDeviceModelCandidate(String relativePath) {
    String name = relativePath.replace('\\', '/');
    return name.startsWith("totalcross/")
        || name.startsWith("jdkcompat/")
        || name.startsWith("jdkcompatx/");
  }

  static boolean isSdkCallerClass(String className) {
    String name = className.replace('.', '/');
    if (!name.startsWith("totalcross/")) {
      return false;
    }
    if (name.startsWith("totalcross/preview/")) {
      return false;
    }
    return !name.equals("totalcross/Launcher")
        && !name.startsWith("totalcross/Launcher$")
        && !name.equals("totalcross/LauncherApplet")
        && !name.equals("totalcross/TotalCrossApplication");
  }

  private static List<Problem> validate(List<JavaClass> classes) {
    return validate(classes, Collections.<JavaClass>emptyList());
  }

  private static List<Problem> validate(
      List<JavaClass> callers, List<JavaClass> deviceClasses) {
    MethodDeclarationResolver.beginConversionRun();
    for (JavaClass type : callers) {
      if (!isPlatformOwner(type.className)) {
        MethodDeclarationResolver.registerProgramClass(type);
      }
    }
    for (JavaClass type : deviceClasses) {
      MethodDeclarationResolver.registerDeviceClass(type);
    }

    List<Problem> problems = new ArrayList<Problem>();
    for (JavaClass type : callers) {
      if (type.methods == null) {
        continue;
      }

      for (JavaMethod method : type.methods) {
        if (method.replaceWithNative
            || DeviceTypeMapping.isReplacedBy4DMethod(type, method)
            || method.code == null
            || method.code.bcs == null) {
          continue;
        }

        for (ByteCode bytecode : method.code.bcs) {
          if (!(bytecode instanceof MethodCall)
              || bytecode.bc == JVM_INVOKEDYNAMIC) {
            continue;
          }

          MethodCall call = (MethodCall) bytecode;
          if (!isPlatformOwner(call.className)) {
            continue;
          }

          String mappedOwner =
              DeviceTypeMapping.converterMappedOwner(call.className);
          if (!isPlatformOwner(mappedOwner)) {
            continue;
          }

          MethodDeclarationResolver.Resolution resolution =
              MethodDeclarationResolver.resolve(
                  mappedOwner, call.name, call.parameters);
          if (!resolution.deviceClassFound || !resolution.deviceMemberFound) {
            problems.add(new Problem(
                type.originalClassName,
                method.name,
                call.className,
                call.name,
                call.parameters,
                resolution.deviceClassFound));
          }
        }
      }
    }
    return Collections.unmodifiableList(problems);
  }

  static boolean isPlatformOwner(String owner) {
    String normalized = owner.replace('.', '/');
    return normalized.startsWith("java/")
        || normalized.startsWith("javax/");
  }

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      throw new IllegalArgumentException(
          "Pass one or more compiled class files, class directories, or jars.");
    }

    List<Path> roots = new ArrayList<Path>(args.length);
    for (String arg : args) {
      roots.add(Paths.get(arg));
    }

    List<Problem> problems = validatePaths(roots);
    if (!problems.isEmpty()) {
      StringBuilder message =
          new StringBuilder("Unsupported deployed Java API usage:");
      for (Problem problem : problems) {
        message.append(System.lineSeparator())
            .append(" - ")
            .append(problem);
      }
      throw new IllegalStateException(message.toString());
    }
    System.out.println("Deployed Java API validation passed.");
  }

  public static final class Problem {
    public final String callerClass;
    public final String callerMethod;
    public final String owner;
    public final String name;
    public final String descriptor;
    public final boolean deviceClassFound;

    Problem(
        String callerClass,
        String callerMethod,
        String owner,
        String name,
        String descriptor,
        boolean deviceClassFound) {
      this.callerClass = callerClass;
      this.callerMethod = callerMethod;
      this.owner = owner;
      this.name = name;
      this.descriptor = descriptor;
      this.deviceClassFound = deviceClassFound;
    }

    @Override
    public String toString() {
      String target =
          owner.replace('/', '.') + "." + name + descriptor;
      String reason = deviceClassFound
          ? "member is not available on device"
          : "class is not available on device";
      return callerClass.replace('/', '.')
          + "."
          + callerMethod
          + " -> "
          + target
          + " ("
          + reason
          + ")";
    }
  }

  private static final class ClassReadFailure extends RuntimeException {
    final IOException cause;

    ClassReadFailure(Path path, IOException cause) {
      super("Could not read " + path, cause);
      this.cause = cause;
    }
  }
}
