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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

import org.apache.commons.io.IOUtils;

import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipFile;
import de.schlichtherle.truezip.zip.ZipOutputStream;
import tc.tools.converter.bb.JavaAttribute;
import tc.tools.converter.bb.JavaClass;
import tc.tools.converter.bb.JavaConstant;
import tc.tools.converter.bb.JavaField;
import tc.tools.converter.bb.JavaMethod;
import tc.tools.converter.bb.attribute.Code;
import tc.tools.converter.bb.attribute.LocalVariableTable;
import tc.tools.converter.bb.attribute.SourceFile;
import tc.tools.converter.bb.constant.Class;
import tc.tools.converter.bb.constant.Integer;
import tc.tools.converter.bb.constant.NameAndType;
import tc.tools.converter.bb.constant.UTF8;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

/*
    A launcher for Android is placed in a Android PacKage (APK), which is a 
    zip file, with the following contents:

       + META-INF
       |    MANIFEST.MF
       |    CERT.RSA
       |    CERT.SF
       + res
       |    drawable-hdpi
       |       icon.png
       | assets 
       |    tcfiles.zip
       | resources.arsc
       | classes.dex
       | AndroidManifest.xml

    The res + resources.arsc + AndroidManifest.xml are stored inside a file named resources.ap_
    (created with APPT) 

    Then it adds the classes.dex and signs the package, creating the APK file.

    The AndroidManifest.xml is a compiled version of the original AndroidManifest.xml file. 
    Its format is (all numbers in little endian):
       pos 4: file size
       pos 12: pos to  - 8
       pos 36: list of positions to the strings, starting from 0 
          each string is prefixed with its size and ends with 0 (chars in unicode)


    The resources.arsc has the package name located at pos 156, prefixed with 
    the total length of the name (127 bytes) - which is NOT the length of the package.
    There is a free space to place the package there, so there's no need to shift the file.


    Since Android requires that data stored in res/raw must be all lower-case, we have to create
    a zip and store the TCZ (and also files from android.pkg) inside that zip. When the program
    first runs, it unpacks that zip in the /data/data/class_package folder. Note that there's no
    way to delete the zip inside tha APK after the installation.


    Also: all TotalCross programs must be signed with the same key, otherwise the vm and litebase 
    will not be able to read data from the program's folder.


    To create the launcher for the application, we follow these steps:

    1. Edit the files: R.class  Stub.class  R$attr.class  R$drawable.class  R$raw.class  and replace
    the package and Stub names with totalcross.app.MainClass (where MainClass is the class that extends
    MainWindow)

    2. Run the dx (dalvik converter) to create a classes.dex file

    3. Zip the contents into tcfiles.zip

    4. Update the resources as explained above.

    5. Create the APK (jar file) with everything

    6. Sign the APK.
 */

public class Deployer4Android {
  private static final boolean DEBUG = false;
  private static String targetDir, sourcePackage, targetPackage, targetTCZ, jarOut, fileName;
  private String tcFolder;
  private boolean singleApk;
  private boolean includeSms;

  byte[] buf = new byte[8192];

  public Deployer4Android() throws Exception {
    targetDir = DeploySettings.targetDir + "android/";
    fileName = DeploySettings.filePrefix;
    if (fileName.indexOf(' ') >= 0) {
      fileName = fileName.replace(" ", "");
    }
    // create the output folder
    File f = new File(targetDir);
    if (!f.exists()) {
      f.mkdirs();
    }
    includeSms = DeploySettings.includeSms;
    singleApk = DeploySettings.packageVM;
    if (includeSms) {
        singleApk = true;
    }
    if (!singleApk) {
      targetPackage = "totalcross/app/" + fileName.toLowerCase();
      if (!DeploySettings.quiet) {
        System.out.println("Android application folder: /data/data/" + (targetPackage.replace('/', '.')));
      }
    } else {
      tcFolder = DeploySettings.folderTotalCross3DistVM + "android/";
      // source and target packages must have the exact length
      sourcePackage = "totalcross/android";
      targetTCZ = "app" + DeploySettings.applicationId.toLowerCase();
      targetPackage = "totalcross/" + targetTCZ;
      System.out.println("Android application folder: /data/data/" + (targetPackage.replace('/', '.')));
    }

    if (!singleApk) {
      createLauncher(); // 1
      jar2dex(); // 2
    }
    updateResources(); // 3+4+5
    Utils.jarSigner(fileName + ".apk", targetDir); // 6
    new ZipAlign().zipAlign(new File(targetDir + "/" + fileName + ".apk"),
        new File(targetDir + "/" + fileName + "_.apk"));
    String apk = targetDir + "/" + fileName + ".apk";
    Utils.copyFile(targetDir + "/" + fileName + "_.apk", apk, true);

    String extraMsg = "";
    if (DeploySettings.installPlatforms.indexOf("android,") >= 0) {
      extraMsg = callADB(apk);
    }

    System.out.println("... Files written to folder " + targetDir + extraMsg);

  }

  private String callADB(String apk) throws Exception {
    String adb = Utils.findPath(DeploySettings.etcDir + "tools/android/adb.exe", false);
    if (adb == null) {
      throw new DeployerException("File android/adb.exe not found!");
    }
    String message = Utils.exec(new String[] { adb, "install", "-r", apk }, targetDir);
    if (message != null && message.indexOf("INPUT:Success") >= 0) {
      return " (installed)";
    }
    System.out.println(message);
    return " (error on installl)";
  }

  private void createLauncher() throws Exception {
    String jarIn = Utils.findPath(DeploySettings.etcDir + "launchers/android/Launcher.jar", false);
    if (jarIn == null) {
      throw new DeployerException("File android/Launcher.jar not found!");
    }
    jarOut = targetDir + fileName + ".jar";

    ZipFile zipf = new ZipFile(jarIn);
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(jarOut));

    for (ZipEntry ze : zipf) {
      String name = convertName(ze.getName());
      if (DEBUG) {
        System.out.println("=== Entry: " + name);
      }
      InputStream zis = zipf.getInputStream(ze);
      zos.putNextEntry(new ZipEntry(name));
      if (name.endsWith(".class")) {
        convertConstantPool(zis, zos);
      }
      zos.closeEntry();
      zis.close();
    }

    zipf.close();
    zos.close();
  }

  private void jar2dex() throws Exception {
    // java -classpath P:\TotalCross3\etc\tools\android\dx.jar com.android.dx.command.Main --dex --output=classes.dex UIGadgets.jar
    String dxjar = Utils.findPath(DeploySettings.etcDir + "tools/android/dx.jar", false);
    if (dxjar == null) {
      throw new DeployerException("File android/dx.jar not found!");
    }
    String javaExe = Utils.searchIn(DeploySettings.path, DeploySettings.appendDotExe("java"));
    String[] cmd = { javaExe, "-classpath", DeploySettings.pathAddQuotes(dxjar), "com.android.dx.command.Main", "--dex",
        "--output=classes.dex", new File(jarOut).getAbsolutePath() }; // guich@tc124_3: use the absolute path for the file
    String out = Utils.exec(cmd, targetDir);
    if (!new File(targetDir + "classes.dex").exists()) {
      throw new DeployerException(
          "An error occured when compiling the Java class with the Dalvik compiler. The command executed was: '"
              + Utils.toString(cmd) + "' at the folder '" + targetDir + "'\nThe output of the command is " + out);
    }
    new File(jarOut).delete(); // delete the jar
  }

  private void updateResources() throws Exception {
    String ap = Utils.findPath(DeploySettings.etcDir + "launchers/android/resources.ap_", false);
    if (ap == null) {
      throw new DeployerException("File android/resources.ap_ not found!");
    }
    String apk = targetDir + fileName + ".apk";
    ZipFile inf = new ZipFile(ap);
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(apk));

    // search input zip file, convert and write each entry to output zip file
    for (ZipEntry ze : inf) {
      ZipEntry ze2;
      String name = ze.getName();

      // keep all the metadata if possible
      if (ze.getMethod() != ZipEntry.STORED) {
        ze2 = ze;
        // little trick to make the entry reusable
        ze2.setCompressedSize(-1);
      } else {
        // the trick above doesn't work with stored entries
        // we'll ignore the metadata and use only the name
        ze2 = new ZipEntry(ze.getName());
      }
      InputStream zis = inf.getInputStream(ze2);
      if (name.indexOf("tcfiles.zip") >= 0) {
        continue; // skip tcfiles from resources
      } else if (name.indexOf("resources.arsc") >= 0) {
        zos.putNextEntry(ze2);
        insertResources_arsc(zis, zos);
      } else if (name.indexOf("icon.png") >= 0) {
        zos.putNextEntry(ze2);
        insertIcon_png(zos, name);
      } else if (name.indexOf("AndroidManifest.xml") >= 0) {
        zos.putNextEntry(ze2);
        insertAndroidManifest_xml(zis, zos);
      } else if (singleApk) {
        byte[] bytes = Utils.readJavaInputStream(zis);
        // for zxing beep.ogg file
        if (name.endsWith(".ogg")) {
          setEntryAsStored(ze2, bytes);
        }
        zos.putNextEntry(ze2);
        zos.write(bytes, 0, bytes.length);
      }
      zos.closeEntry();
      zis.close();
    }
    ZipEntry ze2 = new ZipEntry("assets/tcfiles.zip");
    insertTCFiles_zip(ze2, zos);
    zos.closeEntry();
    
    if (singleApk) {
      processClassesDexes(tcFolder + "TotalCross.apk", zos);
      copyZipEntries(tcFolder + "TotalCross.apk", "res", zos);
      copyZipEntries(tcFolder + "TotalCross.apk", "lib", zos);
    } else {
      // add classes.dex
      zos.putNextEntry(new ZipEntry("classes.dex"));
      totalcross.io.File f = new totalcross.io.File(targetDir + "classes.dex", totalcross.io.File.READ_WRITE);
      int n;
      while ((n = f.readBytes(buf, 0, buf.length)) > 0) {
        zos.write(buf, 0, n);
      }
      zos.closeEntry();
      f.delete(); // delete original file
    }
    try {
      String google_services_json_path = Utils.findPath("google-services.json", true);

      if (google_services_json_path == null) {
        throw new FileNotFoundException("can't find google-services.json in TotalCross deploy path");
      }
      File google_services_json_file = new File(Utils.findPath("google-services.json", true));

      FileInputStream jsonStream = new FileInputStream(google_services_json_file);
      zos.putNextEntry(new ZipEntry("assets/google-services.json"));
      IOUtils.copy(jsonStream, zos);
      zos.closeEntry();

      jsonStream.close();
    } catch (FileNotFoundException e) {
      System.out
          .println("Could not find 'google-services.json', thus Firebase will be ignored further on (Android deploy)");
    }

    zos.close();
    inf.close();
  }

  private void processClassesDexes(String baseApk, ZipOutputStream zos) throws Exception {
    ZipFile zipf = new ZipFile(baseApk);

    for (ZipEntry entry : zipf) {
      if (entry.getName().matches("classes[0-9]*\\.dex")) {
        processClassesDex(tcFolder + "TotalCross.apk", entry, zos);
      }
    }

    zipf.close();
  }

  private void copyZipEntries(String srcZip, String initPath, ZipOutputStream zos) throws IOException {
    ZipFile zipf = new ZipFile(srcZip);
    for (ZipEntry zEntry : zipf) {
      String zentryName = zEntry.getName();
      InputStream zIn = zipf.getInputStream(zEntry);
      if (zentryName.endsWith("/") || zentryName.endsWith("\\") || zentryName.indexOf("icon.png") >= 0) {
        // it's a directory or an old icon, just continue
        continue;
      }
      if (zentryName.startsWith(initPath)) {
        byte[] bytes = Utils.readJavaInputStream(zIn);
        copyEntryBytes(zEntry, bytes, zos);
      }
      zIn.close();
    }
    zipf.close();
  }

  // http://strazzere.com/blog/?p=3
  private static void calcSignature(byte bytes[]) {
    java.security.MessageDigest md;
    try {
      md = java.security.MessageDigest.getInstance("SHA-1");
    } catch (java.security.NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
    md.update(bytes, 32, bytes.length - 32);
    try {
      int amt = md.digest(bytes, 12, 20);
      if (amt != 20) {
        throw new RuntimeException(
            (new StringBuilder()).append("unexpected digest write:").append(amt).append("bytes").toString());
      }
    } catch (java.security.DigestException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static void calcChecksum(byte bytes[]) {
    Adler32 a32 = new Adler32();
    a32.update(bytes, 12, bytes.length - 12);
    int sum = (int) a32.getValue();
    bytes[8] = (byte) sum;
    bytes[9] = (byte) (sum >> 8);
    bytes[10] = (byte) (sum >> 16);
    bytes[11] = (byte) (sum >> 24);
  }

  private void processClassesDex(String srcZip, ZipEntry dexEntry, ZipOutputStream dstZip) throws Exception {
    String fileName = dexEntry.getName();
    dstZip.putNextEntry(new ZipEntry(fileName));
    byte[] bytes = Utils.loadZipEntry(srcZip, fileName);

    replaceBytes(bytes, sourcePackage.getBytes(), targetPackage.getBytes());
    if (DeploySettings.autoStart || DeploySettings.isService) {
      System.out.println("Is service.");
      replaceBytes(bytes, new byte[] { (byte) 0x71, (byte) 0xC3, (byte) 0x5B, (byte) 0x07 },
          DeploySettings.isService ? new byte[] { 1, 0, 0, 0 } : new byte[] { 0, 0, 0, 0 });
    }
    calcSignature(bytes);
    calcChecksum(bytes);
    dstZip.write(bytes, 0, bytes.length);
    dstZip.closeEntry();
  }

  private void replaceBytes(byte[] bytes, byte[] fromBytes, byte[] toBytes) {
    int ofs = 0;
    while ((ofs = Utils.indexOf(bytes, fromBytes, false, ofs)) != -1) {
      totalcross.sys.Vm.arrayCopy(toBytes, 0, bytes, ofs, toBytes.length);
      ofs += toBytes.length;
    }
  }

  private void copyZipEntry(String srcZip, String fileName, ZipOutputStream dstZip) throws Exception {
    byte[] bytes = Utils.loadZipEntry(srcZip, fileName);
    ZipEntry ze = new ZipEntry(fileName);

    copyEntryBytes(ze, bytes, dstZip);
    dstZip.closeEntry();
  }

  private void copyEntryBytes(ZipEntry ze, byte[] bytes, ZipOutputStream dstZip) throws IOException {
    String zentryName = ze.getName();
    if (zentryName.endsWith(".ogg")) // for zxing beep.ogg file 
    {
      setEntryAsStored(ze, bytes);
    }

    dstZip.putNextEntry(ze);
    dstZip.write(bytes, 0, bytes.length);
  }

  private void insertIcon_png(ZipOutputStream zos, String name) throws Exception {
    if (DeploySettings.bitmaps != null) {
      int res;
      if (name.startsWith("res/drawable-xhdpi") && name.endsWith("icon.png")) {
        res = 96;
      } else if (name.startsWith("res/drawable-xxhdpi") && name.endsWith("icon.png")) {
        res = 144;
      } else if (name.startsWith("res/drawable-xxxhdpi") && name.endsWith("icon.png")) {
        res = 192;
      } else {
        res = 72;
      }
      DeploySettings.bitmaps.saveAndroidIcon(zos, res); // libraries don't have icons
    }
  }

  private totalcross.io.ByteArrayStream readInputStream(java.io.InputStream is) {
    totalcross.io.ByteArrayStream bas = new totalcross.io.ByteArrayStream(2048);
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
    return bas;
  }

  private void insertAndroidManifest_xml(InputStream zis, OutputStream zos) throws Exception {
    totalcross.io.ByteArrayStream bas;
    if (includeSms) {
        byte[] bytes = Utils.loadFile(DeploySettings.etcDir + "tools/android/AndroidManifest_includeSms.xml", true);
        bas = new totalcross.io.ByteArrayStream(bytes);
        bas.skipBytes(bytes.length);
    } else if (singleApk) {
        byte[] bytes = Utils.loadFile(DeploySettings.etcDir + "tools/android/AndroidManifest_singleapk.xml", true);
        bas = new totalcross.io.ByteArrayStream(bytes);
        bas.skipBytes(bytes.length);
    } else {
        bas = readInputStream(zis);
    }
    bas.mark();
    totalcross.io.DataStreamLE ds = new totalcross.io.DataStreamLE(bas);
    String oldPackage, oldTitle, oldActivity;

    if (singleApk) {
      oldPackage = sourcePackage.replace('/', '.');
      oldTitle = "Stub";
      oldActivity = null;
    } else {
      oldPackage = "totalcross.app.stub";
      oldTitle = "Stub";
      oldActivity = ".Stub";
    }

    String oldVersion = "!1.0!";
    String oldSharedId = singleApk ? "totalcross.app.sharedid" : null;

    String newPackage = targetPackage.replace('/', '.');
    String newVersion = DeploySettings.appVersion != null ? DeploySettings.appVersion : "1.0";
    String newTitle = DeploySettings.appTitle;
    String newActivity = singleApk ? null : "." + fileName;
    String newSharedId = singleApk ? "totalcross.app.app" + DeploySettings.applicationId.toLowerCase() : null;

    int oldSize = bas.available();
    int difPackage = (newPackage.length() - oldPackage.length()) * 2;
    int difVersion = (newVersion.length() - oldVersion.length()) * 2;
    int difTitle = (newTitle.length() - oldTitle.length()) * 2;
    int difActivity = singleApk ? 0 : (newActivity.length() - oldActivity.length()) * 2;
    int difSharedId = !singleApk ? 0 : (newSharedId.length() - oldSharedId.length()) * 2;
    int dif = difPackage + difVersion + difTitle + difActivity + difSharedId;
    String newTcPackage = "totalcross.and" + DeploySettings.applicationId.toLowerCase(); // totalcross.android -> totalcross.app.tctestwin

    // get the xml size
    bas.setPos(12);
    int xmlsize = ds.readInt();
    xmlsize += dif;
    int gap = ((xmlsize + 3) & ~3) - xmlsize;

    int newSize = oldSize + dif + gap;

    // update new size and position of 
    bas.setPos(4);
    ds.writeInt(newSize);
    bas.setPos(12);
    ds.writeInt(xmlsize + gap);
    int len = ds.readInt();

    bas.setPos(40);
    int[] positions = new int[len + 1];
    for (int i = 0, last = len - 1; i <= last; i++) {
      if (i < last) {
        positions[i + 1] = ds.readInt();
      }
      if (DEBUG) {
        System.out.println(i + " " + positions[i] + " (" + (positions[i + 1] - positions[i]) + ")");
      }
    }

    String[] strings = new String[len];
    int pos0 = bas.getPos();
    for (int i = 0; i < len; i++) {
      int pos = bas.getPos();
      String s = new String(ds.readChars());
      if (DEBUG) {
        System.out.println(i + " #" + pos + " (" + (pos - pos0) + ") " + s + " (" + s.length() + " - "
            + (s.length() * 2 + 2 + 2) + ")");
      }
      strings[i] = s;
      bas.skipBytes(2); // skip 0-terminated string
    }

    // read the rest of the resource (other kinds of data)
    int resSize = bas.available();
    byte[] res = new byte[resSize];
    bas.readBytes(res, 0, resSize);
    int ofs;

    // now the "versionCode" is used to store some properties of the application
    // search and replace the value of versionCode="305419896" (0x12345678) with the application properties
    // note that currently there's no application properties!
    byte[] versionCodeMark = { (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12 };
    ofs = Utils.indexOf(res, versionCodeMark, false);
    if (ofs == -1) {
      throw new DeployerException("Error: could not find position for versionCode");
    }
    totalcross.io.ByteArrayStream dtbas = new totalcross.io.ByteArrayStream(res);
    dtbas.setPos(ofs);
    totalcross.io.DataStreamLE dsbas = new totalcross.io.DataStreamLE(dtbas);
    int props = Utils.version2int(newVersion);
    dsbas.writeInt(props);

    boolean isFullScreen = DeploySettings.isFullScreenPlatform(totalcross.sys.Settings.ANDROID); // guich@tc120_59
    // now, change the names accordingly
    for (int i = 0; i < len; i++) {
      String s = strings[i];
      if (isFullScreen && s.equals("fullscreen:0")) {
        strings[i] = "fullscreen:1";
      } else if (s.startsWith(oldPackage)) {
        if (singleApk) {
          strings[i] = newPackage + s.substring(oldPackage.length());
        } else if (s.endsWith("google_measurement_service")) {
          strings[i] = newTcPackage + s.substring(oldPackage.length());
        }
      } else if ("com.totalcross.android.fileprovider".equals(s)) {
        strings[i] = s.replace("android", targetTCZ);
      }
      if (DeploySettings.allowBackup == false) {
          /*
           * To deactivate allowBackup we swap the properties around.
           * 
           * allowBackup starts with true and changes to false.
           * backupInForeground starts with false and changes to true.
           */
          if (s.equals("allowBackup")) {
              strings[i] = "backupInForeground";
          } else if (s.equals("backupInForeground")) {
              strings[i] = "allowBackup";
          }
      }
      if (oldPackage != null && s.equals(oldPackage)) {
        strings[i] = newPackage;
      } else if (oldVersion != null && s.equals(oldVersion)) {
        strings[i] = newVersion;
      } else if (oldTitle != null && s.equals(oldTitle)) {
        strings[i] = newTitle;
      } else if (oldActivity != null && s.equals(oldActivity)) {
        strings[i] = newActivity;
      } else if (oldSharedId != null && s.equals(oldSharedId)) {
        strings[i] = newSharedId;
      }
    }
    // update the offsets table
    for (int i = 0; i < len; i++) {
      positions[i + 1] = positions[i] + (strings[i].length() * 2 + 2 + 2); // 2 for the positions, and 2 for the 0 termination
    }

    // now write everything again
    bas.setPos(36);
    for (int i = 0; i < len; i++) {
      ds.writeInt(positions[i]);
    }
    for (int i = 0; i < len; i++) {
      String s = strings[i];
      ds.writeChars(s, s.length());
      ds.writeShort(0);
    }
    if (gap > 0) {
      for (int i = 0; i < gap; i++) {
        ds.writeByte(0);
      }
    }

    ds.writeBytes(res);
    int nn = bas.getPos();
    if (nn != newSize) {
      throw new DeployerException(
          "Something went wrong when parsing AndroidManifest.xml. Expected size is " + newSize + ", but got " + nn);
    }

    zos.write(bas.getBuffer(), 0, newSize);
  }

  private void insertResources_arsc(InputStream zis, OutputStream zos) throws Exception {
    byte[] all;
    byte[] key;
    if (includeSms) {
        key = new byte[] { 't', (byte) 0, 'o', (byte) 0, 't', (byte) 0, 'a', (byte) 0, 'l', (byte) 0, 'c', (byte) 0, 'r',
                (byte) 0, 'o', (byte) 0, 's', (byte) 0, 's', (byte) 0, '.', (byte) 0, 'a', (byte) 0, 'n', (byte) 0, 'd',
                (byte) 0, 'r', (byte) 0, 'o', (byte) 0, 'i', (byte) 0, 'd', (byte) 0 };
            all = Utils.loadFile(DeploySettings.etcDir + "tools/android/resources_includeSms.arsc", true);
    } else if (singleApk) {
        key = new byte[] { 't', (byte) 0, 'o', (byte) 0, 't', (byte) 0, 'a', (byte) 0, 'l', (byte) 0, 'c', (byte) 0, 'r',
                (byte) 0, 'o', (byte) 0, 's', (byte) 0, 's', (byte) 0, '.', (byte) 0, 'a', (byte) 0, 'n', (byte) 0, 'd',
                (byte) 0, 'r', (byte) 0, 'o', (byte) 0, 'i', (byte) 0, 'd', (byte) 0 };
            all = Utils.loadFile(DeploySettings.etcDir + "tools/android/resources_singleapk.arsc", true);
    } else {
      key = new byte[] { 't', (byte) 0, 'o', (byte) 0, 't', (byte) 0, 'a', (byte) 0, 'l', (byte) 0, 'c', (byte) 0, 'r',
          (byte) 0, 'o', (byte) 0, 's', (byte) 0, 's', (byte) 0, '.', (byte) 0, 'a', (byte) 0, 'p', (byte) 0, 'p',
          (byte) 0, '.', (byte) 0, 's', (byte) 0, 't', (byte) 0, 'u', (byte) 0, 'b', (byte) 0 };
      all = readInputStream(zis).toByteArray();
    }
    int ofs = Utils.indexOf(all, key, false);
    if (ofs == -1) {
      throw new DeployerException("Could not find position for totalcross.android in arsc.");
    }
    // write the name
    char[] chars = targetPackage.replace('/', '.').toCharArray();
    if (chars.length > 0x7F) {
      throw new DeployerException("The package name length can't be bigger than " + 0x7F);
    }
    int i = 0, n = ofs + 0x7F * 2;
    for (; i < chars.length; i++, ofs += 2) {
      all[ofs] = (byte) chars[i];
    }
    while (ofs < n) {
      all[ofs++] = (byte) 0;
    }
    zos.write(all);
  }

  private void insertTCFiles_zip(ZipEntry ze, ZipOutputStream z) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);

    // parse the android.pkg
    Hashtable ht = new Hashtable(13);
    Utils.processInstallFile("android.pkg", ht);

    Vector vLocals = (Vector) ht.get("[L]");
    if (vLocals == null) {
      vLocals = new Vector();
    }
    Vector vGlobals = (Vector) ht.get("[G]");
    if (vGlobals == null) {
      vGlobals = new Vector();
    }
    vLocals.addElements(DeploySettings.tczs);
    if (vGlobals.size() > 0) {
      vLocals.addElements(vGlobals.toObjectArray());
    }
    if (singleApk) // include the vm?
    {
      // tc is always included
      // include non-binary files
      List<File> defaultTczs = DeploySettings.getDefaultTczs();
      for (File file : defaultTczs) {
        vLocals.addElement(file.getAbsolutePath());
      }
    }

    Utils.preprocessPKG(vLocals, true);
    writeVlocals(baos, vector2set(vLocals, new HashSet<String>()));

    // add the file UNCOMPRESSED
    byte[] bytes = baos.toByteArray();
    setEntryAsStored(ze, bytes);
    z.putNextEntry(ze);
    z.write(bytes);
  }

  public static <E> Set<E> vector2set(Vector vec, Set<E> set) {
    for (int i = 0, n = vec.size(); i < n; i++) {
      @SuppressWarnings("unchecked")
      E item = (E) vec.items[i];
      set.add(item);
    }
    return set;
  }

  private void writeVlocals(ByteArrayOutputStream baos, Set<String> vLocals) throws IOException {
    ZipOutputStream zos = new ZipOutputStream(baos);
    for (String item : vLocals) {
      String[] pathnames = totalcross.sys.Convert.tokenizeString(item, ',');
      String pathname = pathnames[0];
      String name = Utils.getFileName(pathname);
      if (pathnames.length > 1) {
        name = totalcross.sys.Convert.appendPath(pathnames[1], name);
        if (name.startsWith("/")) {
          name = name.substring(1);
        }
      }
      // tcz's name must match the lowercase sharedid
      if (tcFolder != null && pathname.equals(DeploySettings.tczFileName)) {
        name = targetTCZ + ".tcz";
      }
      FileInputStream fis;
      try {
        fis = new FileInputStream(pathname);
      } catch (FileNotFoundException fnfe) {
        try {
          fis = new FileInputStream(totalcross.sys.Convert.appendPath(DeploySettings.currentDir, pathname));
        } catch (FileNotFoundException fnfe2) {
          String pp = Utils.findPath(pathname, true);
          if (pp != null) {
            fis = new FileInputStream(pp);
          } else {
            System.out.println("File not found: " + pathname);
            continue;
          }
        }
      }
      int avaiable = fis.available();

      ByteArrayOutputStream secondary = new ByteArrayOutputStream(avaiable);
      IOUtils.copy(fis, secondary);
      byte[] bytes = secondary.toByteArray();
      fis.close();
      ZipEntry zze = new ZipEntry(name);
      // tcz files will be stored without
      // compression so they can be read
      // directly
      if (name.endsWith(".tcz")) {
        setEntryAsStored(zze, bytes);
      }
      zos.putNextEntry(zze);
      zos.write(bytes);
      zos.closeEntry();
    }
    zos.close();
  }

  private void setEntryAsStored(ZipEntry entry, byte[] content) {
    CRC32 crc = new CRC32();
    crc.update(content);
    entry.setCrc(crc.getValue());
    entry.setMethod(ZipEntry.STORED);
    entry.setCompressedSize(content.length);
    entry.setSize(content.length);
  }

  private void convertConstantPool(InputStream is, ZipOutputStream os) throws Exception {
    totalcross.io.ByteArrayStream bas = new totalcross.io.ByteArrayStream(1024);
    totalcross.io.DataStream ds = new totalcross.io.DataStream(bas);
    int n;

    while ((n = is.read(buf)) > 0) {
      bas.writeBytes(buf, 0, n);
    }
    bas.setPos(0);
    JavaClass jclass = new JavaClass();
    jclass.load(ds);

    checkConstantPool(jclass);

    bas.reuse();
    jclass.save(ds);
    os.write(bas.getBuffer(), 0, bas.getPos());
  }

  private static void checkConstantPool(JavaClass jclass) {
    UTF8 descriptor;
    // Check all class and name/type constants
    if (DEBUG) {
      System.out.println("Constant pool");
    }
    int count = jclass.constantPool.size();
    for (int i = 1; i < count; i++) {
      JavaConstant constant = (JavaConstant) jclass.constantPool.items[i];
      i += constant.slots() - 1; // skip empty slots

      switch (constant.tag) {
      case JavaConstant.CONSTANT_INTEGER:
        String cla = jclass.getClassName();
        if ((DeploySettings.autoStart || DeploySettings.isService) && cla.endsWith("/StartupIntentReceiver")
            && ((Integer) constant.info).value == 123454321) {
          Integer it = new Integer();
          it.value = DeploySettings.isService ? 1 : 0;
          constant.info = it;
        }
        break;
      case JavaConstant.CONSTANT_CLASS:
        descriptor = ((Class) constant.info).getValueAsName();
        descriptor.value = convertName(descriptor.value);
        break;
      case JavaConstant.CONSTANT_NAME_AND_TYPE:
        descriptor = ((NameAndType) constant.info).getValue2AsDescriptor();
        descriptor.value = convertName(descriptor.value);
        break;
      }
    }

    // Check class fields
    if (DEBUG) {
      System.out.println("Fields");
    }
    count = jclass.fields.size();
    for (int i = 0; i < count; i++) {
      JavaField field = (JavaField) jclass.fields.items[i];

      descriptor = (UTF8) field.descriptor.info;
      descriptor.value = convertName(descriptor.value);

      // Check field attributes
      int count2 = field.attributes.size();
      for (int j = 0; j < count2; j++) {
        checkAttribute((JavaAttribute) field.attributes.items[j]);
      }
    }

    // Check class methods
    if (DEBUG) {
      System.out.println("Methods");
    }
    count = jclass.methods.size();
    for (int i = 0; i < count; i++) {
      JavaMethod method = (JavaMethod) jclass.methods.items[i];

      descriptor = (UTF8) method.descriptor.info;
      descriptor.value = convertName(descriptor.value);

      // Check method attributes
      int count2 = method.attributes.size();
      for (int j = 0; j < count2; j++) {
        checkAttribute((JavaAttribute) method.attributes.items[j]);
      }
    }

    // Check class attributes
    if (DEBUG) {
      System.out.println("Atributes");
    }
    count = jclass.attributes.size();
    for (int i = 0; i < count; i++) {
      checkAttribute((JavaAttribute) jclass.attributes.items[i]);
    }

    if (DEBUG) {
      System.out.println("FINISHED");
    }
  }

  /**
   * @param attribute
   * @param classes
   */
  private static void checkAttribute(JavaAttribute attribute) {
    if (attribute.info instanceof SourceFile) {
      JavaConstant source = ((SourceFile) attribute.info).sourceFile;
      UTF8 descriptor = (UTF8) source.info;
      descriptor.value = convertName(descriptor.value);
    } else if (attribute.info instanceof LocalVariableTable) {
      Vector variables = ((LocalVariableTable) attribute.info).variables;
      int count = variables.size();
      for (int i = 0; i < count; i++) {
        UTF8 descriptor = (UTF8) ((LocalVariableTable.LocalVariable) variables.items[i]).descriptor.info;
        descriptor.value = convertName(descriptor.value);
      }
    } else if (attribute.info instanceof Code) {
      Code code = (Code) attribute.info;
      Vector attributes = code.attributes;
      int count = attributes.size();
      for (int i = 0; i < count; i++) {
        checkAttribute((JavaAttribute) attributes.items[i]);
      }
    }
  }

  private static String convertName(String name) {
    String value = name.replace("totalcross/app/stub", targetPackage); // totalcross/app/stub/R -> totalcross/app/uigadgets/R
    value = value.replace("Stub", fileName); // totalcross/app/stub/Stub -> totalcross/app/uigadgets/Stub
    if (DEBUG) {
      System.out.println(name + " -> " + value);
    }
    return value;
  }

}
