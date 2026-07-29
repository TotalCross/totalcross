// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.deployer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipOutputStream;
import totalcross.sys.Convert;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

/** Writes the TotalCross files archive embedded in the Android bundle. */
final class AndroidPackageFiles {
  private AndroidPackageFiles() {
  }

  static void write(OutputStream output, String targetTCZ, String tcFolder) throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream(8192);
    Hashtable packages = new Hashtable(13);
    Utils.processInstallFile("android.pkg", packages);
    Vector locals = (Vector) packages.get("[L]");
    if (locals == null) locals = new Vector();
    Vector globals = (Vector) packages.get("[G]");
    if (globals == null) globals = new Vector();
    locals.addElements(DeploySettings.tczs);
    if (globals.size() > 0) locals.addElements(globals.toObjectArray());
    for (File file : DeploySettings.getDefaultTczs()) locals.addElement(file.getAbsolutePath());
    Utils.preprocessPKG(locals, true);
    writeLocals(bytes, vector2set(locals, new HashSet<String>()), targetTCZ, tcFolder);
    bytes.writeTo(output);
  }

  static <E> Set<E> vector2set(Vector vector, Set<E> set) {
    for (int i = 0, n = vector.size(); i < n; i++) {
      @SuppressWarnings("unchecked") E item = (E) vector.items[i];
      set.add(item);
    }
    return set;
  }

  private static void writeLocals(ByteArrayOutputStream bytes, Set<String> locals,
                                  String targetTCZ, String tcFolder) throws IOException {
    ZipOutputStream zip = new ZipOutputStream(bytes);
    for (String item : locals) {
      String[] parts = Convert.tokenizeString(item, ',');
      String pathname = parts[0];
      String name = Utils.getFileName(pathname);
      if (parts.length > 1) {
        name = Convert.appendPath(parts[1], name);
        if (name.startsWith("/")) name = name.substring(1);
      }
      if (tcFolder != null && pathname.equals(DeploySettings.tczFileName)) name = targetTCZ + ".tcz";
      File file = readable(pathname, Convert.appendPath(DeploySettings.currentDir, pathname), Utils.findPath(pathname, true));
      if (file == null) {
        DeployLogger.warn("File not found: " + pathname);
        continue;
      }
      try (FileInputStream input = new FileInputStream(file)) {
        ByteArrayOutputStream secondary = new ByteArrayOutputStream(input.available());
        org.apache.commons.io.IOUtils.copy(input, secondary);
        byte[] content = secondary.toByteArray();
        ZipEntry entry = new ZipEntry(name);
        if (name.endsWith(".tcz")) stored(entry, content);
        zip.putNextEntry(entry);
        zip.write(content);
        zip.closeEntry();
      }
    }
    zip.close();
  }

  private static void stored(ZipEntry entry, byte[] content) {
    CRC32 crc = new CRC32();
    crc.update(content);
    entry.setCrc(crc.getValue());
    entry.setMethod(ZipEntry.STORED);
    entry.setCompressedSize(content.length);
    entry.setSize(content.length);
  }

  private static File readable(String... paths) {
    for (String path : paths) {
      if (path != null) {
        File file = new File(path);
        if (file.exists() && file.isFile() && file.canRead()) return file;
      }
    }
    return null;
  }
}
