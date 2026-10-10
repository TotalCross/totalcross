// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaMethod;

/**
 * Derived view of methods that cross the deployed Java-to-native boundary.
 *
 * <p>The compiled device classes are authoritative. No NativeMethods.txt-style
 * source inventory is consulted here.</p>
 */
final class NativeBridgeModel {
  private NativeBridgeModel() {
  }

  static Result fromPaths(List<Path> roots) throws Exception {
    ByteCode.initClasses();
    Map<String, JavaClass> classesByName = new LinkedHashMap<String, JavaClass>();
    for (Path root : roots) {
      if (!Files.exists(root)) {
        throw new IOException("Native bridge class input does not exist: " + root);
      }
      if (Files.isRegularFile(root) && (root.toString().endsWith(".jar") || root.toString().endsWith(".zip"))) {
        try (JarFile jar = new JarFile(root.toFile())) {
          java.util.Enumeration<JarEntry> members = jar.entries();
          while (members.hasMoreElements()) {
            JarEntry member = members.nextElement();
            if (member.isDirectory() || !member.getName().endsWith(".class")
                || !isDeviceImplementationClass(member.getName())) continue;
            try (InputStream input = jar.getInputStream(member)) {
              addClass(classesByName, input.readAllBytes(), root + "!" + member.getName());
            } catch (Exception e) {
              throw new IOException("Could not inspect compiled device class " + root + "!" + member.getName(), e);
            }
          }
        }
        continue;
      }
      try (Stream<Path> paths = Files.walk(root)) {
        paths.filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".class"))
            .filter(path -> isDeviceImplementationClass(root.relativize(path).toString()))
            .sorted()
            .forEach(path -> {
              try {
                addClass(classesByName, Files.readAllBytes(path), path.toString());
              } catch (Exception e) {
                throw new ClassReadFailure(path, e);
              }
            });
      } catch (ClassReadFailure failure) {
        throw failure.cause;
      }
    }
    return result(entries(new ArrayList<JavaClass>(classesByName.values())));
  }

  static List<Entry> fromClassBytes(byte[] classBytes) throws Exception {
    ByteCode.initClasses();
    JavaClass type = new JavaClass(classBytes, false, true);
    return entries(Collections.singletonList(type));
  }

  private static void addClass(Map<String, JavaClass> classesByName, byte[] classBytes, String source)
      throws Exception {
    JavaClass type = new JavaClass(classBytes, false, true);
    JavaClass previous = classesByName.putIfAbsent(type.originalClassName, type);
    if (previous != null && !java.util.Arrays.equals(previous.bytes, type.bytes)) {
      throw new IOException("Conflicting compiled device class " + type.originalClassName + " from " + source);
    }
  }

  private static List<Entry> entries(List<JavaClass> classes) {
    List<Entry> entries = new ArrayList<Entry>();
    for (JavaClass type : DeviceTypeMapping.selectDeployedClasses(classes)) {
      if (type.methods == null) continue;

      String sourceOwner = slash(type.originalClassName);
      String deployedOwner = type.className;
      for (JavaMethod method : type.methods) {
        if (DeviceTypeMapping.isReplacedBy4DMethod(type, method)) continue;
        if (!method.isNative && !method.replaceWithNative) continue;
        String deployedName = DeviceTypeMapping.removeReplacementSuffix(method.name);
        String symbol = NativeBridgeSymbolEncoder.encode(deployedOwner, deployedName, method.descriptor);
        entries.add(new Entry(sourceOwner, deployedOwner, method.name, deployedName,
            method.descriptor, symbol, method.replaceWithNative ? "replaced" : "native"));
      }
    }
    return deduplicate(entries);
  }

  private static List<Entry> deduplicate(List<Entry> entries) {
    Map<String, Entry> byIdentity = new LinkedHashMap<String, Entry>();
    for (Entry entry : entries) byIdentity.putIfAbsent(entry.identity(), entry);
    return new ArrayList<Entry>(byIdentity.values());
  }

  static boolean isDeviceImplementationClass(String relativePath) {
    String name = relativePath.replace('\\', '/');
    return name.startsWith("totalcross/") || name.startsWith("jdkcompat/")
        || name.startsWith("jdkcompatx/");
  }

  private static Result result(List<Entry> entries) {
    Collections.sort(entries, Comparator.comparing(Entry::identity));
    Map<String, Entry> firstBySymbol = new HashMap<String, Entry>();
    List<Collision> collisions = new ArrayList<Collision>();
    for (Entry entry : entries) {
      Entry previous = firstBySymbol.putIfAbsent(entry.symbol, entry);
      if (previous != null && !previous.identity().equals(entry.identity())) {
        collisions.add(new Collision(entry.symbol, previous, entry));
      }
    }
    return new Result(Collections.unmodifiableList(entries),
        Collections.unmodifiableList(collisions));
  }

  private static String slash(String value) {
    return value == null ? null : value.replace('.', '/');
  }

  static final class Result {
    final List<Entry> entries;
    final List<Collision> collisions;

    Result(List<Entry> entries, List<Collision> collisions) {
      this.entries = entries;
      this.collisions = collisions;
    }
  }

  static final class Entry {
    final String sourceOwner;
    final String deployedOwner;
    final String sourceName;
    final String deployedName;
    final String descriptor;
    final String symbol;
    final String kind;

    Entry(String sourceOwner, String deployedOwner, String sourceName, String deployedName,
        String descriptor, String symbol, String kind) {
      this.sourceOwner = sourceOwner;
      this.deployedOwner = deployedOwner;
      this.sourceName = sourceName;
      this.deployedName = deployedName;
      this.descriptor = descriptor;
      this.symbol = symbol;
      this.kind = kind;
    }

    String identity() {
      return deployedOwner + "#" + deployedName + descriptor;
    }
  }

  static final class Collision {
    final String symbol;
    final Entry first;
    final Entry second;

    Collision(String symbol, Entry first, Entry second) {
      this.symbol = symbol;
      this.first = first;
      this.second = second;
    }
  }

  private static final class ClassReadFailure extends RuntimeException {
    final Exception cause;

    ClassReadFailure(Path path, Exception cause) {
      super("Could not inspect compiled device class " + path, cause);
      this.cause = cause;
    }
  }
}
