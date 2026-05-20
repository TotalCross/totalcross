// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class that dynamically loads a jar file into the Deploy classpath.<br>
 * It uses reflection to grant access to the loaded jar, a little hackish but that's the easiest way of doing it.<br>
 * Another option would be to iterate through the jar entries and load classes one by one, which is not much better
 * than this IMHO. So I decided to go with the easiest-to-implement solution.
 * 
 * @author Fabio Sobral
 * @since TotalCross 1.15
 */
//flsobral@tc115: just a mark for quick search, see class documentation above.
public class JarClassPathLoader {
  private static final Class<?>[] parameters = new Class[] { URL.class };

  public static File findJar(File path, String jarName) {
    if (path == null || jarName == null) {
      return null;
    }

    File dir = path;
    if (!dir.isDirectory()) {
      return null;
    }

    String jarPrefix = jarName + "-";
    File[] jars = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(jarPrefix) && name.endsWith(".jar");
      }
    });

    if (jars == null || jars.length == 0) {
      return null;
    }

    List<File> candidates = new ArrayList<File>();
    Collections.addAll(candidates, jars);
    Collections.sort(candidates, new Comparator<File>() {
      @Override
      public int compare(File left, File right) {
        return compareJarVersions(jarName, right.getName(), left.getName());
      }
    });

    return candidates.get(0);
  }

  public static void addFile(java.io.File f) throws java.io.IOException {
    addURL(f.toURI().toURL());
  }

  public static void addJar(File path, String jarName) throws java.io.IOException {
    File jar = findJar(path, jarName);
    if (jar != null) {
      addFile(jar);
    }
  }

  public static void addURL(URL u) throws java.io.IOException {
    ClassLoader cl = ClassLoader.getSystemClassLoader();

    if (cl instanceof URLClassLoader) {
      URLClassLoader sysloader = (URLClassLoader) cl;
      Class<?> sysclass = URLClassLoader.class;

      try {
        Method method = sysclass.getDeclaredMethod("addURL", parameters);
        method.setAccessible(true);
        method.invoke(sysloader, new Object[] { u });
      } catch (Throwable t) {
        throw new java.io.IOException("Error, could not add URL to system classloader", t);
      }
    }
  }

  private static int compareJarVersions(String jarName, String leftName, String rightName) {
    String leftVersion = extractVersion(jarName, leftName);
    String rightVersion = extractVersion(jarName, rightName);
    return compareVersions(leftVersion, rightVersion);
  }

  private static String extractVersion(String jarName, String fileName) {
    return fileName.substring(jarName.length() + 1, fileName.length() - ".jar".length());
  }

  private static int compareVersions(String leftVersion, String rightVersion) {
    String[] leftParts = leftVersion.split("[^A-Za-z0-9]+");
    String[] rightParts = rightVersion.split("[^A-Za-z0-9]+");
    int length = Math.max(leftParts.length, rightParts.length);

    for (int i = 0; i < length; i++) {
      String leftPart = i < leftParts.length ? leftParts[i] : "0";
      String rightPart = i < rightParts.length ? rightParts[i] : "0";
      int comparison = compareVersionPart(leftPart, rightPart);
      if (comparison != 0) {
        return comparison;
      }
    }

    return leftVersion.compareTo(rightVersion);
  }

  private static int compareVersionPart(String leftPart, String rightPart) {
    boolean leftNumber = isNumber(leftPart);
    boolean rightNumber = isNumber(rightPart);

    if (leftNumber && rightNumber) {
      return compareNumberStrings(leftPart, rightPart);
    }

    if (leftNumber) {
      return 1;
    }

    if (rightNumber) {
      return -1;
    }

    return leftPart.compareToIgnoreCase(rightPart);
  }

  private static boolean isNumber(String value) {
    if (value.length() == 0) {
      return false;
    }

    for (int i = 0; i < value.length(); i++) {
      if (!Character.isDigit(value.charAt(i))) {
        return false;
      }
    }

    return true;
  }

  private static int compareNumberStrings(String leftNumber, String rightNumber) {
    String left = stripLeadingZeroes(leftNumber);
    String right = stripLeadingZeroes(rightNumber);

    if (left.length() != right.length()) {
      return left.length() - right.length();
    }

    return left.compareTo(right);
  }

  private static String stripLeadingZeroes(String value) {
    int index = 0;
    while (index < value.length() - 1 && value.charAt(index) == '0') {
      index++;
    }
    return value.substring(index);
  }
}
