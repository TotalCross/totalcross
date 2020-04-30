// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util.zip;

import java.util.Enumeration;
import totalcross.Launcher;
import totalcross.io.IOException;
import totalcross.io.Stream;

/** @deprecated This class was never fully implemented. Use ZipStream instead. */

@Deprecated
public class ZipFile {
  private java.util.zip.ZipFile nativeFile;

  public ZipFile(String name) throws IOException {
    try {
      nativeFile = new java.util.zip.ZipFile(name);
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  public void close() throws IOException {
    try {
      if (nativeFile != null) {
        nativeFile.close();
        nativeFile = null;
      }
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  public ZipEntry[] entries() {
    ZipEntry[] entries = new ZipEntry[nativeFile.size()];
    Enumeration<?> e = nativeFile.entries();

    for (int i = 0; e.hasMoreElements(); i++) {
      entries[i] = new ZipEntry(((java.util.zip.ZipEntry) e.nextElement()).getName());
    }

    return entries;
  }

  public String getEntry(String name) {
    java.util.zip.ZipEntry entry = nativeFile.getEntry(name);
    if (entry == null) {
      return null;
    }
    return name;
  }

  public Stream getEntryStream(String name) throws IOException {
    java.util.zip.ZipEntry entry = nativeFile.getEntry(name);
    if (entry != null) {
      try {
        java.io.InputStream is = nativeFile.getInputStream(entry);
        return new Launcher.IS2S(is);
      } catch (java.io.IOException e) {
        throw new IOException(e.getMessage());
      }
    }
    return null;
  }

  public String getName() {
    return nativeFile.getName();
  }

  public int size() {
    return nativeFile.size();
  }

  @Override
  protected void finalize() {
    try {
      this.close();
    } catch (totalcross.io.IOException e) {
    }
  }
}
