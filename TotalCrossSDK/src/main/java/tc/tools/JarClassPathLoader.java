// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

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

  public static void addFile(String s) throws java.io.IOException {
    java.io.File f = new java.io.File(s);
    addFile(f);
  }

  public static void addFile(java.io.File f) throws java.io.IOException {
    addURL(f.toURI().toURL());
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
}