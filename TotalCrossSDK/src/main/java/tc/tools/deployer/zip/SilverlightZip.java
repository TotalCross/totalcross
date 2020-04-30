// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

/**
 * Creates a zip file compatible with the format accepted by Silverlight.<br>
 * Silverlight can only handle zip files whose contents' headers include compressed size and crc, but most zip
 * implementations for Java only include this information on the extended local header. This class uses a
 * {@link BlackHoleOutputStream} to fill the ZipEntry fields before writing to the actual output stream.<br>
 * Also includes an implementation of {@link FilenameFilter} to copy the contents of another zip file directly using
 * TrueZip.
 * 
 * @author Fabio Sobral
 * 
 */
public class SilverlightZip {
  private ZipOutputStream zos;
  private BlackHoleOutputStream nos = new BlackHoleOutputStream();

  private Hashtable<String, ZipEntry> entries = new Hashtable<String, ZipEntry>();

  public SilverlightZip(File outputFile) throws FileNotFoundException {
    this.zos = new ZipOutputStream(new FileOutputStream(outputFile));
  }

  /**
   * Adds a new entry on the zip file.
   * 
   * @param name
   * @param content
   * @return true if successful, false if there's already a file with the given name on the zip file.
   * @throws IOException
   */
  public boolean putEntry(String name, byte[] content) throws IOException {
    if (entries.containsKey(name)) {
      return false;
    }

    ZipEntry entry = new ZipEntry(name);

    final ZipOutputStream zipOut = new ZipOutputStream(nos);
    zipOut.setMethod(ZipOutputStream.DEFLATED);
    zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);
    zipOut.putNextEntry(entry);
    zipOut.write(content);
    zipOut.closeEntry();
    zipOut.finish();
    zipOut.close();

    zos.putNextEntry(entry);
    zos.write(content);
    zos.closeEntry();

    entries.put(name, entry);
    return true;
  }

  public boolean putEntry(String name, File file) throws IOException {
    return putEntry(name, FileUtils.readFileToByteArray(file));
  }

  public void close() throws IOException {
    zos.close();
  }
}
