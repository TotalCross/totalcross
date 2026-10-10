// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tc.tools.deployer.DeploySettings;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaMethod;

/**
 * Shared mapping rules between host-facing Java owners and TotalCross device
 * implementations. Keep converter, compatibility validation, generated indexes,
 * and native-bridge discovery on the same owner mapping.
 */
final class DeviceTypeMapping {
  private DeviceTypeMapping() {
  }

  static String removeReplacementSuffix(String name) {
    int i;
    if (name.endsWith("4D")) {
      return name.substring(0, name.length() - 2);
    } else if ((i = name.indexOf("4D$")) >= 0) {
      return name.substring(0, i).concat(name.substring(i + 2));
    }
    return name;
  }

  static String removeReplacementSuffix(String name, String sign) {
    int i, len = name.length();
    if (name.endsWith("4D")) {
      return name.substring(0, len - 2) + sign.substring(len);
    } else if ((i = name.indexOf("4D$")) >= 0) {
      return name.substring(0, i).concat(name.substring(i + 2)) + sign.substring(len);
    }
    return sign;
  }

  static String replaceCompatibilityOwner(String name) {
    if (name.startsWith("totalcross/lang/")) {
      return "java/lang/" + name.substring(16);
    }
    if (name.startsWith("totalcross/util/") && name.contains("4D") && !name.contains("/zip/")) {
      return name.replace("totalcross", "java");
    }
    if (name.startsWith("jdkcompat") && name.contains("4D")) {
      return name.replaceFirst("jdkcompat", "java");
    }
    return name;
  }

  static String deployedOwner(String deviceOwner) {
    return removeReplacementSuffix(replaceCompatibilityOwner(deviceOwner));
  }

  /** Selects and maps the class files that the device converter will deploy. */
  static List<JavaClass> selectDeployedClasses(List<JavaClass> classes) {
    Set<String> names = new HashSet<String>();
    for (JavaClass type : classes) {
      names.add(replacementComparableName(type.originalClassName));
    }

    List<JavaClass> deployed = new ArrayList<JavaClass>();
    for (JavaClass type : classes) {
      String normalized = replacementComparableName(type.originalClassName);
      boolean replacement = isReplacementClass(normalized);
      if (!replacement && isReplacedBy4D(normalized, names)) continue;
      if (!replacement && isInnerOfReplacedClass(normalized, names)) continue;

      type.className = removeReplacementSuffix(normalized);
      deployed.add(type);
    }
    return deployed;
  }

  static boolean isReplacementClass(String normalizedName) {
    return !normalizedName.equals(removeReplacementSuffix(normalizedName));
  }

  static boolean shouldSkipBaseClass(String normalizedName, boolean hasReplacement) {
    return hasReplacement && !"totalcross/util/Vector".equals(normalizedName)
        && !"totalcross/util/Hashtable".equals(normalizedName);
  }

  static boolean shouldSkipInnerClass(String normalizedName, boolean hasReplacementOuter) {
    return !isReplacementClass(normalizedName) && normalizedName.indexOf('$') >= 0
        && hasReplacementOuter;
  }

  static boolean isReplacedBy4DMethod(JavaClass type, JavaMethod method) {
    if (method.name.endsWith("4D") || type.methods == null) return false;
    String replacementSignature = method.name + "4D" + method.signature.substring(method.name.length());
    for (JavaMethod candidate : type.methods) {
      if (replacementSignature.equals(candidate.signature)) return true;
    }
    return false;
  }

  private static String replacementComparableName(String name) {
    return replaceCompatibilityOwner(name.replace('.', '/'));
  }

  private static boolean isReplacedBy4D(String normalizedName, Set<String> names) {
    return shouldSkipBaseClass(normalizedName, names.contains(normalizedName + "4D"));
  }

  private static boolean isInnerOfReplacedClass(String normalizedName, Set<String> names) {
    int nested = normalizedName.indexOf('$');
    if (nested < 0) return false;
    return shouldSkipInnerClass(normalizedName,
        names.contains(normalizedName.substring(0, nested) + "4D"));
  }

  static String converterMappedOwner(String owner) {
    String dotted = owner.replace('/', '.');
    return DeploySettings.mapClassName(dotted).replace('.', '/');
  }

  static Class<?>[] mappedHierarchyTypes(Class<?> type) {
    if (type == null) return new Class<?>[0];
    if (isDeviceOwned(type)) return new Class<?>[] { type };
    String name = type.getName();
    return name.startsWith("java.") || name.startsWith("javax.")
        ? findDeviceClasses(name.replace('.', '/')) : new Class<?>[0];
  }

  static Class<?>[] findDeviceClasses(String javaOwner) {
    String dotted = javaOwner.replace('/', '.');
    if (!dotted.startsWith("java.") && !dotted.startsWith("javax.")) return new Class<?>[0];
    ArrayList<Class<?>> foundClasses = new ArrayList<Class<?>>(2);
    String[] prefixes = { "totalcross", "jdkcompat" };
    for (String prefix : prefixes) {
      String mapped = prefix + dotted.substring(4);
      int nested = mapped.indexOf('$');
      String replacement = nested < 0 ? mapped + "4D"
          : mapped.substring(0, nested) + "4D" + mapped.substring(nested);
      Class<?> found = loadOwned(replacement);
      if (found != null) {
        foundClasses.add(found);
        continue;
      }
      found = loadOwned(mapped);
      if (found != null) foundClasses.add(found);
    }
    return foundClasses.toArray(new Class<?>[foundClasses.size()]);
  }

  static String javaFacingOwner(Class<?> declaration, String candidate) {
    String name = declaration.getName();
    if (!isDeviceOwned(declaration)) return candidate;
    if (name.endsWith("4D")) name = name.substring(0, name.length() - 2);
    if (name.startsWith("totalcross.")) name = "java." + name.substring("totalcross.".length());
    else if (name.startsWith("jdkcompat.")) name = "java." + name.substring("jdkcompat.".length());
    else if (name.startsWith("jdkcompatx.")) name = "javax." + name.substring("jdkcompatx.".length());
    return slash(name);
  }

  static boolean isDeviceOwned(Class<?> type) {
    String name = type.getName();
    return name.startsWith("totalcross.") || name.startsWith("jdkcompat.") || name.startsWith("jdkcompatx.");
  }

  private static Class<?> loadOwned(String className) {
    if (!className.startsWith("totalcross.") && !className.startsWith("jdkcompat.")
        && !className.startsWith("jdkcompatx.")) return null;
    try {
      return Class.forName(className, false, DeviceTypeMapping.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private static String slash(String value) {
    return value == null ? null : value.replace('.', '/');
  }
}
