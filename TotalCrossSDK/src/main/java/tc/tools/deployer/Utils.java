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
package tc.tools.deployer;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.schlichtherle.truezip.file.TFile;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.io.IOException;
import totalcross.io.IllegalArgumentIOException;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

/**
 * Some general utility methods used by the deployer programs.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 */

public class Utils {
  public static final String INST_FILE_GLOBALS = "[G]";
  public static final String INST_FILE_LOCALS = "[L]";
  public static final String INST_FILE_PKEY = "[PKEY]";
  public static final String INST_FILE_PASS = "[PASS]";
  public static final String INST_FILE_CERT = "[CERT]";
  public static final String INST_FILE_CATEGORY = "[CATEGORY]";
  public static final String INST_FILE_LOCATION = "[LOCATION]";
  public static final String INST_FILE_URL = "[URL]";
  public static final String INST_FILE_URI_BASE = "[URI_BASE]";
  public static final String INST_FILE_DESCRIPTION = "[DESCRIPTION]";
  public static final String INST_FILE_ICON_FILE = "[ICON_FILE]";

  private static byte bytebuf[] = new byte[4096];

  /////////////////////////////////////////////////////////////////////////////////////
  public static void copyFile(String source, String dest, boolean deleteSource) throws Exception {
    File in = new File(source, File.READ_WRITE);
    byte[] bytebuf = Utils.bytebuf;
    int r;
    File out = new File(dest, File.CREATE_EMPTY);
    while ((r = in.readBytes(bytebuf, 0, bytebuf.length)) > 0) {
      out.writeBytes(bytebuf, 0, r);
    }
    if (deleteSource) {
      in.delete();
    } else {
      in.close();
    }
    out.close();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static void processInstallFile(String file, Hashtable ht) {
    byte[] bytes = findAndLoadFile(file, true);
    if (bytes == null) {
      bytes = findAndLoadFile("all.pkg", true); // guich@tc111_22
    }
    if (bytes == null) {
      return;
    }
    String[] lines = Convert.tokenizeString(new String(bytes), '\n');
    if (lines != null) {
      for (int i = 0; i < lines.length; i++) {
        if (lines[i].length() > 0) {
          String capStart = lines[i].substring(0, lines[i].indexOf(']') + 1).toUpperCase(); // switch on the first 8 capitalized letters
          if (capStart.startsWith(INST_FILE_GLOBALS) || capStart.startsWith(INST_FILE_LOCALS)) {
            String id = capStart.substring(0, 3); // id has 3 letters '[X]'
            totalcross.util.Vector v = null;
            v = (totalcross.util.Vector) ht.get(id);
            if (v == null) {
              ht.put(id, v = new totalcross.util.Vector());
            }
            v.addElement(lines[i].substring(3).trim());
            capStart = null;
          } else if (capStart.startsWith(INST_FILE_PKEY) || capStart.startsWith(INST_FILE_CERT)
              || capStart.startsWith(INST_FILE_PASS)) {
            String id = capStart.substring(0, 6); // id has 6 letters '[XXXX]'
            ht.put(id, lines[i].substring(6).trim());
            capStart = null;
          } else {
            String[] iPhoneAttr = new String[] { INST_FILE_CATEGORY, INST_FILE_LOCATION, INST_FILE_URL,
                INST_FILE_URI_BASE, INST_FILE_DESCRIPTION, INST_FILE_ICON_FILE };
            for (int a = 0; a < iPhoneAttr.length; a++) {
              if (capStart.startsWith(iPhoneAttr[a])) {
                ht.put(iPhoneAttr[a], lines[i].substring(iPhoneAttr[a].length()).trim());
                capStart = null;
                break;
              }
            }
          }
          if (capStart != null && !lines[i].startsWith(";")) {
            println("invalid line in custom inf file: " + lines[i]);
          }
        }
      }
    }
  }

  private static final String pdbtczError = "You can't add TCZ files to the installer using the palm.pkg file; they must be converted to .pdb using \"java tc.Deploy mytczfile.tcz -palm\"; then reference the pdb in the palm.pkg file.";

  public static String[] joinGlobalWithLocals(Hashtable ht, Vector more, boolean allowTCZ)
      throws IllegalArgumentIOException, IOException // guich@tc114_87: changed dontAllowTCZ into allowTCZ
  {
    Vector vLocals, vGlobals;
    vLocals = (Vector) ht.get("[L]");
    if (vLocals == null) {
      vLocals = new Vector();
    }
    vGlobals = (Vector) ht.get("[G]");
    if (vGlobals == null) {
      vGlobals = new Vector();
    }
    if (more != null) {
      for (int i = more.size(); --i >= 0;) {
        vGlobals.addElement(more.items[i]);
      }
    }

    if (vLocals != null) {
      preprocessPKG(vLocals, false);
    }
    if (vGlobals != null) {
      preprocessPKG(vGlobals, false);
    }
    int nl = vLocals.size();
    int ng = vGlobals.size();
    String[] extras = new String[nl + ng];
    int c = 0;
    for (int i = 0; i < nl; i++, c++) {
      extras[c] = (String) vLocals.items[i];
      if (extras[c].indexOf(".tcz") >= 0 && !allowTCZ) {
        throw new IllegalArgumentException(pdbtczError);
      }
    }
    for (int i = 0; i < ng; i++, c++) {
      extras[c] = (String) vGlobals.items[i];
      if (extras[c].indexOf(".tcz") >= 0 && !allowTCZ) {
        throw new IllegalArgumentException(pdbtczError);
      }
    }
    return extras;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static void preprocessPKG(Vector v, boolean acceptsPath) throws IllegalArgumentIOException, IOException {
    Vector vextra = new Vector(100);
    for (int i = v.size(); --i >= 0;) {
      String[] pathnames = totalcross.sys.Convert.tokenizeString((String) v.items[i], ',');
      final String pathname = pathnames[0];
      if (pathname.endsWith("/")) { // a folder?
        v.removeElementAt(i);
        String foundPath = findPath(pathname, true);
        if (foundPath == null) {
        	throw new IOException("Invalid path in pkg file: " + pathname);
        }
        String[] ff = new File(foundPath).listFiles();
        if (ff != null) {
          for (int j = 0; j < ff.length; j++) {
            vextra.addElement(pathname + ff[j] + (pathnames.length > 1 && acceptsPath ? "," + pathnames[1] : ""));
          }
        }
      } else if (pathname.endsWith(".tcz")) {
        File ff = new File(pathname);
        if (!ff.exists()) {
          final String p = Utils.findPath(pathname, true);
          if (p != null) {
            ff = new File(p);
          }
        }
        if (ff.exists()) {
          for (int idx = 1 ; true ; idx++) {
            final String pathname2 = ff.getPath().substring(0, pathname.length() - 4) + "_" + idx + "lib.tcz";
            File ff2 = new File(pathname2);
            if (!ff2.exists()) {
              break;
            }
            vextra.addElement(pathname2);
          }
        }
      }
    }
    if (vextra.size() > 0) {
      v.addElements(vextra.toObjectArray());
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static void copyEntry(String s, String targetDir) throws Exception {
    String tpath = "";
    if (s.indexOf(',') >= 0) {
      String[] ss = s.split(",");
      s = ss[0];
      tpath = ss[1];
    }
    if (!new File(s).exists()) {
      String s0 = s;
      s = Utils.findPath(s, true);
      if (s == null) {
        throw new DeployerException("File not found: " + s0);
      }
    }
    String to = Convert.appendPath(targetDir, Convert.appendPath(tpath, Utils.getFileName(s)));
    try {
      new File(getParent(to)).createDir();
    } catch (Exception e) {
    } // try to create target dir      
    Utils.copyFile(s, to, false);
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static File waitForFile(String file) throws Exception {
    File in = null;
    int count = 300;
    while (count-- > 0) {
      in = new File(file);
      if (in.exists()) {
        break;
      }
      Vm.sleep(300);
    }
    if (count <= 0) {
      throw new DeployerException("Error in script: expected file " + file + " was never written!");
    }
    return in;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String findPath(String fileName, boolean ignoreFileWithoutSlash) {
    String p = null;

    try {
      if ((!ignoreFileWithoutSlash || fileName.replace('/', '\\').indexOf('\\') >= 0)
          && new File(p = fileName).exists()) {
        return p;
      }
      if (DeploySettings.classPath != null && (p = searchIn(DeploySettings.classPath, fileName)) != null) {
        return p;
      }
      if (new File(p = (DeploySettings.currentDir + "/" + fileName)).exists()) {
        return p.replace('\\', '/');
      }
      if (new File(p = (DeploySettings.baseDir + "/" + fileName)).exists()) {
        return p.replace('\\', '/');
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String searchIn(String[] searchPath, String fileName) // guich@tc111_19
  {
    if (searchPath != null) {
      try {
        String p;
        for (int i = 0; i < searchPath.length; i++) {
          String path = searchPath[i].replace('\\', '/');
          path = path.replace("\"", ""); // remove any " surrounding the path
          if (path.contains("system32")) {
            continue;
          }
          p = path + "/" + fileName;
          if (new File(p).exists()) {
            return p;
          }
          // check if its a jar file. If it is, then use its path to try to find the file
          if (path.toLowerCase().endsWith(".jar")) // is a jar file!
          {
            path = getParentFolder(path);
            p = path + "/" + fileName;
            if (new File(p).exists()) {
              return p;
            }
          }
        }
      } catch (IOException e) {
        /* just ignore */}
    }
    return null;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String getParent(String path) {
    int idx = path.lastIndexOf('/');
    if (idx == -1) {
      idx = path.lastIndexOf('\\');
    }
    return idx == -1 ? null : path.substring(0, idx);
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String getParentFolder(String path) {
    if (path != null) {
      for (int i = 0; i < 2; i++) // p:/totalcross/dist/tc.jar -> p:/totalcross
      {
        int idx = path != null ? path.lastIndexOf('/') : -1;
        if (idx == -1 && path != null) {
          idx = path.lastIndexOf('\\');
        }
        path = idx == -1 ? null : path.substring(0, idx);
      }
    }
    return path;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static byte[] loadZipEntry(String zip, String file) throws java.io.IOException {
    java.io.File f = new java.io.File(zip);
    ZipInputStream zIn = new ZipInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(f)));
    byte[] bytes = null;
    for (ZipEntry zEntry = zIn.getNextEntry(); zEntry != null && bytes == null; zEntry = zIn.getNextEntry()) {
      String name = zEntry.getName();
      if (name.endsWith("/")) {
        continue;
      }
      if (name.replace('\\', '/').equals(file)) {
        bytes = readJavaInputStream(zIn);
      }
      zIn.closeEntry();
    }
    zIn.close();
    if (bytes == null) {
      throw new DeployerException("Error: \"" + file + "\" not found inside " + zip);
    }
    return bytes;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static byte[] findAndLoadFile(String fileName, boolean showFile) {
    byte[] bytes = null;
    String path = null;
    try {
      fileName = fileName.replace('/', DeploySettings.SLASH);
      bytes = loadFile(path = DeploySettings.currentDir + "/" + fileName, false); // first, in currentDir
      if (bytes == null) {
        bytes = loadFile(path = fileName, false); // second, search in the current path
      }
      if (bytes == null) {
        bytes = loadFile(path = DeploySettings.baseDir + "/" + fileName, false); // third, search in the base path
      }
      if (bytes == null && DeploySettings.mainClassDir != null) {
        bytes = loadFile(path = DeploySettings.mainClassDir + "/" + fileName, false); // fourth, search in the directory where the MainWindow is placed
      }
      if (bytes == null && DeploySettings.classPath != null) {
        for (int i = 0; i < DeploySettings.classPath.length && bytes == null; i++) {
          path = DeploySettings.classPath[i]; // guich@340_19: show the path
          if (path.toLowerCase().indexOf(".jar") < 0) {
            bytes = loadFile(path = path + "/" + fileName, false);
          } else {
            try {
              String lowerfile = fileName.toLowerCase();
              if (path.toLowerCase().indexOf("jdeb") >= 0) {
                continue;
              }
              java.io.File f = new java.io.File(path);
              ZipInputStream zIn = new ZipInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(f)));
              for (ZipEntry zEntry = zIn.getNextEntry(); zEntry != null && bytes == null; zEntry = zIn.getNextEntry()) {
                String name = zEntry.getName();
                if (name.endsWith("/")) {
                  continue;
                }
                name = name.replace('/', DeploySettings.SLASH);
                if (name.toLowerCase().endsWith(lowerfile)) {
                  bytes = readJavaInputStream(zIn);
                }
                zIn.closeEntry();
              }
              zIn.close();
            } catch (java.io.FileNotFoundException fnfe) {
            } // guich@tc115_45: ignore if the file is in the classpath but does not exist
          }
        }
      }
      if (bytes == null && fileName.toLowerCase().endsWith(".png") && DeploySettings.mainPackage != null
          && DeploySettings.mainPackage.indexOf("samples/") >= 0) {
        bytes = loadFile(path = DeploySettings.etcDir + "images/" + fileName, false);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (bytes != null && showFile) {
      println("Found " + path);
    }
    return bytes;
  }

  ///////////////////////////////////////////////////////////////////////////////////
  public static void fillExclusionList() {
    String path = null;
    String etcDir = null;

    if (DeploySettings.etcDir != null) {
      etcDir = DeploySettings.etcDir.toLowerCase();
      if (java.io.File.separatorChar != '/') {
        etcDir = etcDir.replace('/', '\\'); // use the platform default separator
      }
    }
    try {
      if (DeploySettings.classPath != null) {
        for (int i = 0; i < DeploySettings.classPath.length; i++) {
          path = DeploySettings.classPath[i].toLowerCase();
          if (path.endsWith(".jar") && (etcDir == null || !path.startsWith(etcDir))) {
            try {
              java.io.File f = new java.io.File(path);
              ZipInputStream zIn = new ZipInputStream(new java.io.FileInputStream(f));
              for (ZipEntry zEntry = zIn.getNextEntry(); zEntry != null; zEntry = zIn.getNextEntry()) {
                String name = zEntry.getName();
                if (name.endsWith("/")) {
                  continue;
                }
                DeploySettings.exclusionList.addElement(name);
                zIn.closeEntry();
              }
              zIn.close();
            } catch (java.io.FileNotFoundException fnfe) {
            } // ignore if the file is in the classpath but does not exist.
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static byte[] readJavaInputStream(java.io.InputStream is) {
    if (is == null) {
      return null;
    }
    ByteArrayStream bas = new ByteArrayStream(1024);
    byte[] buf = new byte[128];
    int len;
    while (true) {
      try {
        len = is.read(buf);
      } catch (java.io.IOException e) {
        break;
      }
      if (len > 0) {
        bas.writeBytes(buf, 0, len);
      } else {
        break;
      }
    }
    return bas.toByteArray();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static byte[] loadFile(String path, boolean throwEx) throws IOException {
    try {
      File fis;
      try {
        fis = new File(path, File.READ_ONLY); // try first without quotes
      } catch (FileNotFoundException enfe) {
        fis = new File(DeploySettings.pathAddQuotes(path), File.READ_ONLY); // now try with quotes
      }
      int len = fis.getSize();
      byte[] bytes = new byte[len];
      fis.readBytes(bytes, 0, len);
      fis.close();
      return bytes;
    } catch (IOException e) {
      if (throwEx) {
        throw e;
      } else {
        return null;
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  /**
   * Writes a string to the bytes array.
   * @param s the string to write
   * @param pos the position in the array to start
   * @param zeroTerminate true to add a zero byte after the end of the string, false to add nothing
   */
  public static void writeString(byte[] bytes, String s, int pos, boolean zeroTerminate) {
    writeString(bytes, s, pos, zeroTerminate, 0, (byte) 0);
  }

  /////////////////////////////////////////////////////////////////////////////////////
  /**
   * Writes a string to the bytes array with some padding at the end
   * @param s the string to write
   * @param pos the position in the array to start
   * @param zeroTerminate true to add a zero byte after the end of the string, false to add nothing
   * @param len the total length of the space allocated
   * @param pad the character to pad at the end if the string doesn't use all the allocated space
   */
  public static void writeString(byte[] bytes, String s, int pos, boolean zeroTerminate, int len, byte pad) {
    byte[] b = s.getBytes();
    System.arraycopy(b, 0, bytes, pos, b.length);
    int padStart = b.length;
    if (zeroTerminate) {
      bytes[pos + (padStart++)] = (byte) 0;
    }
    Convert.fill(bytes, pos + padStart, len, pad);
  }

  /////////////////////////////////////////////////////////////////////////////////////
  /** writes a string in unicode format, filling with spaces */
  public static void writeStringUnicode(byte[] bytes, String title, int ofs, int size) {
    char[] ac = title.toCharArray();
    int len = ac.length;
    for (int i = 0; i < len; i++, ofs += 2) {
      bytes[ofs] = (byte) ac[i];
    }
    // pad
    for (; len < size; len++, ofs += 2) {
      bytes[ofs] = (byte) '~';
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  //guich@200
  /** searches for a string in an array of bytes */
  public static int indexOf(byte[] src, byte[] what, boolean skipStrange) // guich@340_60: added skipStrange
  {
    return indexOf(src, what, skipStrange, 0);
  }

  public static int indexOf(byte[] src, byte[] what, boolean skipStrange, int start) // guich@340_60: added skipStrange
  {
    if (src == null || src.length == 0 || what == null || what.length == 0) {
      return -1;
    }
    int len = src.length - what.length;
    byte b = what[0];
    int i, j;
    for (i = start; i < len; i++) {
      if (src[i] == b) // first letter matches?
      {
        boolean found = true;
        int plus = 0;
        for (j = 1; j < what.length && found; j++) {
          if (skipStrange && src[i + j + plus] < 0) {
            plus++;
          } else if (src[i + j + plus] != what[j]) {
            found = false; // ps: cannot use continue here since were insice 2 loops
          }
        }
        if (found) {
          return i; // all matches!
        }
      }
    }
    return -1;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  /** searches for a unicode string in an array of bytes */
  public static int uniIndexOf(byte[] src, byte[] what, int i) // guich@340_60: added skipStrange
  {
    if (src == null || src.length == 0 || what == null || what.length == 0) {
      return -1;
    }
    int len = src.length - what.length;
    byte b = what[0];
    int j;
    for (; i < len; i++) {
      if (src[i] == b) // first letter matches?
      {
        boolean found = true;
        for (j = 1; j < what.length && found; j++) {
          if (src[i + (j << 1)] != what[j]) {
            found = false; // ps: cannot use continue here since were insice 2 loops
          }
        }
        if (found) {
          return i; // all matches!
        }
      }
    }
    return -1;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static int readInt(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset + 1] & 0xFF) << 16) | ((bytes[offset + 2] & 0xFF) << 8)
        | (bytes[offset + 3] & 0xFF);
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static short readShort(byte[] bytes, int offset) {
    return (short) (((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF));
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static void writeInt(byte[] bytes, int offset, int i) // write a reverse int
  {
    bytes[offset++] = (byte) (i & 0xFF);
    i >>= 8;
    bytes[offset++] = (byte) (i & 0xFF);
    i >>= 8;
    bytes[offset++] = (byte) (i & 0xFF);
    i >>= 8;
    bytes[offset++] = (byte) (i & 0xFF);
    i >>= 8;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  /**
   * Strips any path components or extensions from a filename to get the
   * base name.  eg <code>strip("one\two\Three.ext")</code> would return
   * <code>"Three"</code>
   * @param s the string to strip
   */
  public static String strip(String s) {
    int st = s.lastIndexOf('\\');
    if (st == -1) {
      st = s.lastIndexOf('/');
    }
    if (st == -1) {
      st = 0;
    }
    if (s.charAt(st) == '\"') {
      st++;
    }
    int en = s.lastIndexOf('.');
    if (en == -1 || en <= st) {
      en = s.length();
    }
    if (s.charAt(en - 1) == '\"') {
      en--;
    }
    return s.substring(st, en);
  }

  /////////////////////////////////////////////////////////////////////////////////////
  /**
   * Removes all white spaces from a string and change the first char after every white
   * space to upper case.
   * @param s The string to remove the spaces from.
   * @param upperCaseAfterSpace Flag indicating if the next char after a space must be
   * changed to its corresponding upper case char.
   * @return The new string, without white spaces.
   */
  public static String removeWhiteSpaces(String s, boolean upperCaseAfterSpace) {
    s.trim();
    String noSpaces = "";

    boolean nextUpper = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == ' ') {
        nextUpper = true;
      } else {
        if (nextUpper) {
          if (upperCaseAfterSpace) {
            c = Character.toUpperCase(c);
          }
          nextUpper = false;
        }
        noSpaces += c;
      }
    }

    return noSpaces;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static void println(String s) {
    if (!DeploySettings.quiet) {
      System.out.println(s);
    }
  }

  public static void println(String s, boolean force) {
    if (!DeploySettings.quiet || force) {
      System.out.println(s);
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  /** Convert an ABCD into its hexadecimal equivalent (0x)41424344 */
  public static String palm2epocCrid(String s) {
    char[] c = s.toCharArray();
    return "0x" + totalcross.sys.Convert.unsigned2hex(c[0], 2) + totalcross.sys.Convert.unsigned2hex(c[1], 2)
        + totalcross.sys.Convert.unsigned2hex(c[2], 2) + totalcross.sys.Convert.unsigned2hex(c[3], 2);
  }

  /////////////////////////////////////////////////////////////////////////////////////
  /** Replaces a string by another one */
  public static String replace(String src, String from, String to) {
    int idx;
    int l = from.length();
    while ((idx = src.indexOf(from)) >= 0) {
      src = src.substring(0, idx) + to + src.substring(idx + l);
    }
    return src;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  /** Writes the given contents to a file */
  public static void writeFile(String fileName, String[] contents, String[] extraContents) throws Exception {
    File f = new File(fileName, File.CREATE_EMPTY);
    DataStream ds = new DataStream(f);
    byte[] crlf = "\r\n".getBytes();
    for (int i = 0; i < contents.length; i++) {
      ds.writeBytes(contents[i].getBytes());
      ds.writeBytes(crlf);
    }
    if (extraContents != null) {
      for (int i = 0; i < extraContents.length; i++) {
        ds.writeBytes(extraContents[i].getBytes());
        ds.writeBytes(crlf);
      }
    }
    f.close();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String appendPaths(String[] extras) {
    StringBuffer sb = new StringBuffer(1000);
    for (int i = 0; i < extras.length; i++) {
      sb.append(" ").append(extras[i]); // guich@tc100b5_57: surround with ""
    }
    return sb.toString();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static void writeBytes(byte[] bs, String toDir) throws Exception {
    File out = new File(toDir, File.CREATE_EMPTY);
    out.writeBytes(bs, 0, bs.length);
    out.close();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String getCreator(String name) {
    //if (library=='G') return "SWAB";
    int i;
    int n = name.length();
    int hash = 0;
    byte[] creat = new byte[4];
    for (i = 0; i < n; i++) {
      hash += (byte) name.charAt(i);
    }
    for (i = 0; i < 4; i++) {
      creat[i] = (byte) ((hash % 26) + 'a');
      if ((hash & 64) > 0) {
        creat[i] += ('A' - 'a');
      }
      hash = hash / 2;
    }
    return new String(creat);
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String getFileName(String path) {
    int slash = path.lastIndexOf('/');
    if (slash == -1) {
      slash = path.lastIndexOf('\\'); // guich@tc112_8: consider \ too
    }
    return slash == -1 ? path : path.substring(slash + 1);
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String getFileNameWithoutExt(String path) {
    int slash = path.lastIndexOf('/');
    if (slash == -1) {
      slash = path.lastIndexOf('\\'); // guich@tc112_8: consider \ too
    }
    path = slash == -1 ? path : path.substring(slash + 1);
    int i = path.lastIndexOf('.');
    return i == -1 ? path : path.substring(0, i);
  }

  /////////////////////////////////////////////////////////////////////////////////////
  private static final char[] NON_LETTERS = { ' ', ':', '-', '\\', '/' };

  /** "Agenda" -> "Agenda"    "Agenda 1.0" -> "Agenda"     "--== Agenda ==--" -> "Agenda"  */
  public static String stripNonLetters(String s) {
    String[] parts = totalcross.sys.Convert.tokenizeString(s, NON_LETTERS); // guich@tc115_84: exclude other chars
    if (parts.length > 1) {
      s = "";
      for (int j = 0; j < parts.length; j++) {
        if (parts[j].length() > 0 && Character.isLetter(parts[j].charAt(0))) {
          s += parts[j] + " ";
        }
      }
      s = s.trim();
    }
    return s;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static void copyTCZFile(String targetDir) throws Exception {
    for (int i = 0; i < DeploySettings.tczs.length; i++) {
      copyFile(DeploySettings.tczs[i], targetDir + "/" + Utils.getFileName(DeploySettings.tczs[i]), false);
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  // currentDir: "w:/TotalCross3/classes/tc"
  // passed: "samples/ui/gadgets/UIGadgets.class"
  // className   "tc/samples/ui/gadgets/UIGadgets"
  public static String getBaseFolder(String currentDir, String passed, String className) {
    // currentDir: /home/raphael/Documentos/projetos/totalcross/marte/trunk/n 
    // passed    : /home/raphael/Documentos/projetos/totalcross/marte/trunk/n/build/classes/main/Carregar.class
    // className : main/Carregar
    String className2 = className.endsWith(".class") ? className : className + ".class";
    if (passed.startsWith(currentDir) && passed.endsWith(className2)) {
      return passed.substring(0, passed.length() - className2.length());
    }
    // normalize the slashes
    currentDir = currentDir.replace('/', DeploySettings.SLASH).replace('\\', DeploySettings.SLASH);
    if (passed.indexOf(':') >= 0) {
      currentDir = "";
    } else if (!currentDir.endsWith("" + DeploySettings.SLASH)) {
      currentDir += DeploySettings.SLASH;
    }
    passed = passed.replace('/', DeploySettings.SLASH).replace('\\', DeploySettings.SLASH);
    className = className.replace('/', DeploySettings.SLASH).replace('\\', DeploySettings.SLASH);
    if (!className.toLowerCase().endsWith(".class")) {
      className += ".class";
    }
    currentDir += passed;

    int idx = currentDir.indexOf(className);
    currentDir = idx > 0 ? currentDir.substring(0, idx - 1) : null; // do not change to >= 0 !   - remove the last slash
    return currentDir;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String[] removePath(String[] extras) {
    String[] ret = new String[extras.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = getFileName(extras[i].replace('\\', '/'));
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String toString(byte[] bs, int i, int j) {
    StringBuffer sb = new StringBuffer(50);
    while (j-- > 0) {
      sb.append(totalcross.sys.Convert.unsigned2hex(bs[i++], 2)).append(' ');
    }
    return sb.toString();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  // Calls runtime.exec, returning the output string which can be used to debug a problem.
  public static String exec(String[] command, String path) throws Exception {
    Process process = Runtime.getRuntime().exec(command, null, new java.io.File(path));
    java.io.InputStream inputStream = process.getInputStream();
    java.io.InputStream errorStream = process.getErrorStream();
    StringBuffer message = new StringBuffer(1024);
    String lineIn;

    for (int i = 0; i < 30000 / 200; i++) // 30 seconds must be enough...
    {
      if (inputStream.available() > 0) {
        while (inputStream.available() > 0 && (lineIn = readStream(inputStream)) != null) {
          message.append("INPUT:").append(lineIn).append("\n");
        }
      }
      if (errorStream.available() > 0) {
        while (errorStream.available() > 0 && (lineIn = readStream(errorStream)) != null) {
          message.append("ERROR: ").append(lineIn).append("\n");
        }
      }
      try {
        process.exitValue();
        break;
      } catch (Throwable throwable) {
        Thread.sleep(200);
      }
    }
    return (message.length() > 0) ? message.toString() : null;
  }

  private static String readStream(java.io.InputStream is) throws Exception {
    int avail = is.available();
    byte[] buf = bytebuf.length >= avail ? bytebuf : new byte[avail];
    is.read(buf, 0, avail);
    return new String(buf, 0, avail).trim();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static int countNotNull(Object[] o) {
    int n = 0;
    for (int i = o.length; --i >= 0;) {
      if (o[i] != null) {
        n++;
      }
    }
    return n;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static void removeQuotes(String[] path) {
    for (int i = 0; i < path.length; i++) {
      path[i] = path[i].replace("\"", "");
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static void jarSigner(String jar, String targetDir) throws Exception {
    // Certificate fingerprint (MD5): 0D:79:8E:42:A9:CD:50:AC:29:72:85:F8:12:3C:22:0E
    // jarsigner -keystore P:\TotalCross3\etc\security\tcandroidkey.keystore -storepass @ndroid$w -keypass @ndroidsw UIGadgets.apk tcandroidkey
    String jarsignerExe = Utils.searchIn(DeploySettings.path, DeploySettings.appendDotExe("jarsigner"));
    if (jarsignerExe == null) {
      throw new DeployerException("Could not find the file " + DeploySettings.appendDotExe("jarsigner")
          + ". Make sure you have installed a JDK that has this file in the bin folder. If so, make sure that the %JAVA_HOME%/bin is in the PATH.");
    }
    if (jarsignerExe.contains(" ")) {
      jarsignerExe = DeploySettings.appendDotExe("jarsigner");
    }
    String keystore = Utils.findPath(DeploySettings.etcDir + "security/tcandroidkey.keystore", false);
    if (keystore == null) {
      throw new DeployerException("File security/tcandroidkey.keystore not found!");
    }
    keystore = new java.io.File(keystore).getAbsolutePath();
    Vector v = new Vector(10);
    v.addElement(jarsignerExe);
    if (DeploySettings.dJavaVersion >= 1.7) {
      v.addElements(new String[] { "-digestalg", "SHA1", "-sigalg", "MD5withRSA" });
    }
    v.addElement("-keystore");
    v.addElement(keystore);
    v.addElement("-storepass");
    v.addElement("@ndroid$w");
    v.addElement("-keypass");
    v.addElement("@ndroidsw");
    v.addElement(jar);
    v.addElement("tcandroidkey");
    String out = Utils.exec((String[]) v.toObjectArray(), targetDir);
    if (out != null && !out.startsWith("INPUT:jar signed")) {
      throw new DeployerException("An error occured when signing the APK. The output is " + out);
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static int version2int(String v) {
    int i;
    try {
      if (DeploySettings.appBuildNumber != -1) // 2.9 -> 209, 2.10 -> 210, 2.123 -> 2123
      {
        int afterDot = v.length() - (v.indexOf('.') + 1);
        i = Convert.toInt(afterDot == 1 ? v.replace('.', '0') // 2.9 -> 209 
            : v.replace(".", "")); // 2.10 -> 210
      } else {
        // 2.9 -> 290
        v = Convert.replace(v, ".", "");
        i = Convert.toInt(v);
        if (i < 10) {
          i *= 10;
        }
        if (i < 100) {
          i *= 10;
        }
        return i;
      }
    } catch (InvalidNumberException ine) {
      i = 100;
    }
    return i;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String toString(String[] cmd) {
    StringBuilder sb = new StringBuilder(200);
    for (int i = 0; i < cmd.length; i++) {
      sb.append(cmd[i]).append(" ");
    }
    return sb.toString();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static int getToday() {
    java.util.Calendar c = java.util.Calendar.getInstance();
    int y = c.get(java.util.Calendar.YEAR);
    int m = c.get(java.util.Calendar.MONTH) + 1;
    int d = c.get(java.util.Calendar.DAY_OF_MONTH);
    return y * 10000 + m * 100 + d;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static String pipeConcat(String... kv) {
    StringBuilder sb = new StringBuilder(256);
    for (int i = 0; i < kv.length;) {
      if (i > 0) {
        sb.append('|');
      }
      sb.append(kv[i++]).append('=').append(kv[i++]);
    }
    return sb.toString();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public static java.util.HashMap<String, String> pipeSplit(String sp) {
    java.util.HashMap<String, String> ret = new java.util.HashMap<String, String>(5);
    String[] kv = sp.split("\\|");
    for (int i = 0; i < kv.length; i++) {
      String[] s = kv[i].split("=");
      if (s.length == 1) {
        s = new String[] { s[0], "" };
      }
      ret.put(s[0], s[1]);
    }
    return ret;
  }

}
