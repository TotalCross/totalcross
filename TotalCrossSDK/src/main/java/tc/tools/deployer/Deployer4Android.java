// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.deployer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipOutputStream;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
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


    Also: all TotalCross programs must be signed with the same key, otherwise the vm 
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
  public static String signingPropertiesPath = null;
  public static boolean permissionManageExternalStorage = false;
  public static boolean permissionRequestInstallPackages = false;
  private final Properties signingConfig = new Properties();
  private final String targetDir;
  private final String targetTCZ;
  private String tcFolder;

  public Deployer4Android() throws Exception {
    try (FileInputStream fis = new FileInputStream(Utils.findPath(DeploySettings.etcDir + "security/android_keystore.properties", false))) {
      signingConfig.load(fis);
    }
    if (Deployer4Android.signingPropertiesPath != null) {
      try (FileInputStream fis = new FileInputStream(Deployer4Android.signingPropertiesPath)) {
        signingConfig.load(fis);
      }
    }
    targetDir = DeploySettings.targetDir + "android/";
    FileUtils.deleteQuietly(new File(targetDir));
    String fileName = DeploySettings.filePrefix;
    if (fileName.indexOf(' ') >= 0) {
      fileName = fileName.replace(" ", "");
    }
    // source and target packages must have the exact length
    final String sourcePackage = "totalcross/android";
    targetTCZ = "app" + DeploySettings.applicationId.toLowerCase(Locale.ROOT);
    final String targetPackage = "totalcross/" + targetTCZ;
    System.out.println("Android application folder: /data/data/" + (targetPackage.replace('/', '.')));

    String newPackage = targetPackage.replace('/', '.');
    String newVersion = DeploySettings.appVersion != null ? DeploySettings.appVersion : "1.0";
    String newTitle = DeploySettings.appTitle;
    String newSharedId = "totalcross.app.app" + DeploySettings.applicationId.toLowerCase(Locale.ROOT);

    // create the output folder
    File f = new File(targetDir);
    if (!f.exists()) {
      f.mkdirs();
    }

    tcFolder = DeploySettings.folderTotalCross3DistVM + "android/";

    // 1. locate template
    final File templateFile = new File(Convert.appendPath(DeploySettings.folderTotalCross3DistVM, "android/TotalCross.aab"));
    if (!templateFile.exists()) {
      throw new DeployerException("Missing file: " + templateFile.getPath() + ". Please reinstall the SDK.");
    }
    
    // 2. create a temp file and copy the template
    final File targetFile = File.createTempFile(DeploySettings.appTitle, ".zip");
    targetFile.deleteOnExit();
    FileUtils.copyFile(templateFile, targetFile);

    // 3. open the temp file with truezip
    final TFile targetZip = new TFile(targetFile);

    // 4. locate and copy the AndroidManifest
    final File manifestFile = File.createTempFile("AndroidManifest_" + DeploySettings.appTitle, ".xml");
    manifestFile.deleteOnExit();
    TFile b = new TFile(targetZip, "base/manifest/AndroidManifest.xml");
    if (!b.canRead()) {
      throw new DeployerException("Template aab is missing the file base/manifest/AndroidManifest.xml");
    }
    b.cp(manifestFile);
    
    // 5. decode the AndroidManifest
    // $PROTO_BIN --decode=aapt.pb.XmlNode --proto_path=tools Configuration.proto Resources.proto < $DEST_FOLDER/base/manifest/AndroidManifest.xml > AndroidManifest_temp.xml
    final String protocExecutable = new File(new File(DeploySettings.etcDir, "tools/android"), "protoc/protoc-3.20.1-" + (DeploySettings.isWindows() ? "win64" : (DeploySettings.isUnix() ? "linux-x86_64" : "osx-x86_64")) + DeploySettings.appendDotExe("/bin/protoc")).getAbsolutePath();
    String[] decodeCmd = { protocExecutable, "--decode=aapt.pb.XmlNode", "--proto_path=tools", "Configuration.proto", "Resources.proto" };
    
    Process process = Runtime.getRuntime().exec(decodeCmd, null, new File(DeploySettings.etcDir, "tools/android"));
    final File decodedManifestFile = File.createTempFile("AndroidManifest2_" + DeploySettings.appTitle, ".xml");
    decodedManifestFile.deleteOnExit();
    IOUtils.copy(new FileInputStream(manifestFile), process.getOutputStream());
    process.getOutputStream().close();
    String originalManifest = IOUtils.toString(process.getInputStream(), "UTF-8");
    originalManifest = originalManifest.replaceFirst("(.*\"[pP]ackage\"\\R.*value: )\"[a-z\\.]+\"", "$1\"" + newPackage + "\"");
    originalManifest = originalManifest.replaceFirst("(.*\"[vV]ersionName\"\\R.*value: )\"![0-9\\.]+!\"", "$1\"" + newVersion + "\"");
    originalManifest = originalManifest.replaceFirst("(.*\"[lL]abel\"\\R.*value: )\"Stub\"", "$1\"" + newTitle + "\"");
    originalManifest = originalManifest.replaceFirst("(.*\"[sS]haredUserId\"\\R.*value: )\"[a-z\\.]+\"", "$1\"" + newSharedId + "\"");
    boolean isFullScreen = DeploySettings.isFullScreenPlatform(totalcross.sys.Settings.ANDROID);
    originalManifest = originalManifest.replaceFirst("(.*\"[vV]alue\"\\R.*value: )\"fullscreen:0+\"", "$1\"fullscreen:" + (isFullScreen ? 1 : 0) + "\"");
    originalManifest = originalManifest.replaceFirst("(.*\"[aA]uthorities\"\\R.*value: )\"[a-z\\.]+\"", "$1\"com.totalcross." + targetTCZ + ".fileprovider\"");
    int versionCode = Utils.version2int(newVersion);
    originalManifest = originalManifest.replaceFirst("(.*\"[vV]ersionCode\"\\R.*\")[0-9]+(\"\\R)((?:.*\\R)*?)(.*int_decimal_value: )[0-9]+", "$1" + versionCode + "$2$3$4" + versionCode);
    originalManifest = originalManifest.replaceAll("totalcross\\.android", "totalcross." + targetTCZ);
    if (!Deployer4Android.permissionManageExternalStorage){
      originalManifest = originalManifest.replaceFirst("(child\\s*\\{\\s*)(element\\s*\\{\\s*name:\\s*\\\"uses-permission\\\"\\s*.*\\{(?:\\s*.*){3}\\\"android\\.permission\\.MANAGE_EXTERNAL_STORAGE\\\"(?:\\s*.*\\s*\\}){2})", "$1text: \"\\\\n.   \"");
    }
    if (!Deployer4Android.permissionRequestInstallPackages){
      originalManifest = originalManifest.replaceFirst("(child\\s*\\{\\s*)(element\\s*\\{\\s*name:\\s*\\\"uses-permission\\\"\\s*.*\\{(?:\\s*.*){3}\\\"android\\.permission\\.REQUEST_INSTALL_PACKAGES\\\"(?:\\s*.*\\s*\\}){2})", "$1text: \"\\\\n.   \"");
    }
    
    
    if (!DeploySettings.quiet) {
      IOUtils.write(originalManifest, new FileOutputStream(new File(targetDir, "AndroidManifest.xml")));
    }
    try (FileOutputStream fos = new FileOutputStream(decodedManifestFile)) {
      IOUtils.write(originalManifest, fos);
    }

    // 7. encode the updated AndroidManifest
    // $PROTO_BIN --encode=aapt.pb.XmlNode --proto_path=tools Configuration.proto Resources.proto < AndroidManifest_temp.xml > $DEST_FOLDER/base/manifest/AndroidManifest.xml
    String[] encodeCmd = { protocExecutable, "--encode=aapt.pb.XmlNode", "--proto_path=tools", "Configuration.proto", "Resources.proto" };
    Process encodeProcess = Runtime.getRuntime().exec(encodeCmd, null, new File(DeploySettings.etcDir, "tools/android"));
    IOUtils.copy(new FileInputStream(decodedManifestFile), encodeProcess.getOutputStream());
    encodeProcess.getOutputStream().close();
    b.input(encodeProcess.getInputStream());

    // 8. replace icons
    TFile res = new TFile(targetZip, "base/res/");
    TFile[] drawableFolders = res.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith("drawable");
      }
    });

    for (TFile tFile : drawableFolders) {
      TFile[] iconFiles = tFile.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith("icon.png");
        }
        
      });
      for (TFile iconFile : iconFiles) {
        try (PipedOutputStream pOut = outputToTFile(iconFile)) {
          insertIconPng(pOut, iconFile.getName());
        }
      }
    }

    // 9. replace tcfiles.zip
    TFile tcFilesZip = new TFile(targetZip, "base/assets/tcfiles.zip").toNonArchiveFile();
    try (PipedOutputStream pOut = outputToTFile(tcFilesZip)) {
      insertTCFilesZip(pOut);
    }

    // 10. update dex
    TFile dex = new TFile(targetZip, "base/dex/");
    TFile[] dexFiles = dex.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        return name.matches("classes[0-9]*\\.dex");
      }
    });
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for (TFile dexFile : dexFiles) {
      dexFile.output(baos);
      byte[] bytes = baos.toByteArray();

      try (PipedOutputStream pOut = outputToTFile(dexFile)) {
        processClassesDex(bytes, pOut, sourcePackage.getBytes(), targetPackage.getBytes());
      }
    }

    // 10. insert google-services.json
    try {
      String googleServicesJsonPath = Utils.findPath("google-services.json", true);

      if (googleServicesJsonPath == null) {
        throw new FileNotFoundException("can't find google-services.json in TotalCross deploy path");
      }
      File googleServicesJsonFile = new File(Utils.findPath("google-services.json", true));

      try (FileInputStream jsonStream = new FileInputStream(googleServicesJsonFile)){
        TFile googleServicesJson = new TFile(targetZip, "base/assets/google-services.json");
        googleServicesJson.input(jsonStream);
      }
    } catch (FileNotFoundException e) {
      System.out
          .println("Could not find 'google-services.json', thus Firebase will be ignored further on (Android deploy)");
    }

    // 11. unmount to commit changes
    TVFS.umount(targetZip);

    // 12. copy app file to target folder
    final File rawAabFile = File.createTempFile(fileName, ".aab");
    rawAabFile.deleteOnExit();
    FileUtils.copyFile(targetFile, rawAabFile);

    // 13. use jarsigner to sign the app
    // jarsigner -verbose -keystore $ANDROID_SIGNING_KEY -storepass $SIGN_KEY_PWD -keypass $KEY_PASS $TEMP_FILE $KEY_ALIAS
    if (Boolean.parseBoolean(signingConfig.getProperty("aab_signing_enabled"))) {
      Utils.jarSigner(rawAabFile.getName(), rawAabFile.getParent(), (Properties) signingConfig.clone());

      // 14. zipalign the app
      // $ZIP_ALIGN -f -v -p 4 $TEMP_FILE $OUT_FILE
      new ZipAlign().zipAlign(rawAabFile, new File(targetDir, fileName + ".aab"));
    } else {
      FileUtils.copyFile(rawAabFile, new File(targetDir, fileName + ".aab"));
    }

    // 15. generate apk
    // java -Djava.io.tmpdir=$PWD -jar $BUNDLE_TOOL build-apks --bundle $OUT_FILE --mode universal --output rollback.apks --output-format=DIRECTORY --ks $ANDROID_SIGNING_KEY --ks-pass "pass:$SIGN_KEY_PWD" --ks-key-alias "$KEY_ALIAS" --key-pass "pass:$KEY_PASS"
    List<String> javaCmdList = new ArrayList<>();
    
    final String javaExe = Utils.searchIn(DeploySettings.path, DeploySettings.appendDotExe("java"));
    javaCmdList.add(javaExe);
    javaCmdList.add("-Djava.io.tmpdir=" + new File(targetDir).getAbsolutePath());
    javaCmdList.add("-jar");
    javaCmdList.add(new File(DeploySettings.etcDir, "tools/android/bundletool-all-1.10.0.jar").getAbsolutePath());
    javaCmdList.add("build-apks");
    javaCmdList.add("--bundle=" + fileName + ".aab");
    javaCmdList.add("--mode=universal");
    javaCmdList.add("--output=" + fileName + ".apks");
    javaCmdList.add("--output-format=DIRECTORY");

    if (Boolean.parseBoolean(signingConfig.getProperty("apk_signing_enabled"))) {
      String keystore = Utils.findPath(signingConfig.getProperty("apk_keystore_path"), false);
      if (keystore == null) {
        keystore = Utils.findPath(DeploySettings.homeDir + signingConfig.getProperty("apk_keystore_path"), false);
        if (keystore == null) {
          throw new DeployerException("Keystore for APK signing not found!");
        }
      }
      javaCmdList.add("--ks=" + new File(keystore).getAbsolutePath());
      javaCmdList.add("--ks-pass=pass:" + signingConfig.getProperty("apk_keystore_storepass"));
      javaCmdList.add("--ks-key-alias=" + signingConfig.getProperty("apk_keystore_alias"));
      javaCmdList.add("--key-pass=pass:" + signingConfig.getProperty("apk_keystore_keypass"));
    }

    String execOutput = Utils.exec(javaCmdList.toArray(new String[0]), new File(targetDir).getAbsolutePath());
    if (!DeploySettings.quiet) {
      System.out.println(execOutput);
    }

    FileUtils.moveFile(new File(targetDir, fileName + ".apks/universal.apk"), new File(targetDir, fileName + ".apk"));
    FileUtils.deleteQuietly(new File(targetDir, fileName + ".apks"));

    String apk = targetDir + "/" + fileName + ".apk";

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
            new StringBuilder().append("unexpected digest write:").append(amt).append("bytes").toString());
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

  private static void processClassesDex(byte[] bytes, OutputStream outputStream, byte[] sourcePackageBytes, byte[] targetPackageBytes) throws Exception {
    replaceBytes(bytes, sourcePackageBytes, targetPackageBytes);
    if (DeploySettings.autoStart || DeploySettings.isService) {
      System.out.println("Is service.");
      replaceBytes(bytes, new byte[] { (byte) 0x71, (byte) 0xC3, (byte) 0x5B, (byte) 0x07 },
          DeploySettings.isService ? new byte[] { 1, 0, 0, 0 } : new byte[] { 0, 0, 0, 0 });
    }
    calcSignature(bytes);
    calcChecksum(bytes);
    outputStream.write(bytes, 0, bytes.length);
  }

  private static void replaceBytes(byte[] bytes, byte[] fromBytes, byte[] toBytes) {
    int ofs = 0;
    while ((ofs = Utils.indexOf(bytes, fromBytes, false, ofs)) != -1) {
      Vm.arrayCopy(toBytes, 0, bytes, ofs, toBytes.length);
      ofs += toBytes.length;
    }
  }

  private void insertIconPng(OutputStream zos, String name) throws Exception {
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

  private void insertTCFilesZip(OutputStream z) throws Exception {
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
    // tc is always included
    // include non-binary files
    List<File> defaultTczs = DeploySettings.getDefaultTczs();
    for (File file : defaultTczs) {
      vLocals.addElement(file.getAbsolutePath());
    }

    Utils.preprocessPKG(vLocals, true);
    writeVlocals(baos, vector2set(vLocals, new HashSet<String>()));

    // add the file UNCOMPRESSED
    baos.writeTo(z);
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
      String[] pathnames = Convert.tokenizeString(item, ',');
      String pathname = pathnames[0];
      String name = Utils.getFileName(pathname);
      if (pathnames.length > 1) {
        name = Convert.appendPath(pathnames[1], name);
        if (name.startsWith("/")) {
          name = name.substring(1);
        }
      }
      // tcz's name must match the lowercase sharedid
      if (tcFolder != null && pathname.equals(DeploySettings.tczFileName)) {
        name = targetTCZ + ".tcz";
      }

      File f = new File(pathname);
      if (!f.exists() || !f.isFile() || !f.canRead()) {
        f = new File(Convert.appendPath(DeploySettings.currentDir, pathname));
      }

      File file = getFirstReadableFile(pathname, Convert.appendPath(DeploySettings.currentDir, pathname), Utils.findPath(pathname, true));
      if (file == null) {
        System.out.println("File not found: " + pathname);
        continue;
      }

      try (FileInputStream fis = new FileInputStream(file)){
        int avaiable = fis.available();

        ByteArrayOutputStream secondary = new ByteArrayOutputStream(avaiable);
        IOUtils.copy(fis, secondary);
        byte[] bytes = secondary.toByteArray();
        fis.close();
        ZipEntry zze = new ZipEntry(name);
        // tcz files will be stored without compression so they can be read directly
        if (name.endsWith(".tcz")) {
          setEntryAsStored(zze, bytes);
        }
        zos.putNextEntry(zze);
        zos.write(bytes);
        zos.closeEntry();
      }
    }
    zos.close();
  }

  private static void setEntryAsStored(ZipEntry entry, byte[] content) {
    CRC32 crc = new CRC32();
    crc.update(content);
    entry.setCrc(crc.getValue());
    entry.setMethod(ZipEntry.STORED);
    entry.setCompressedSize(content.length);
    entry.setSize(content.length);
  }

  private static File getFirstReadableFile (String... paths) {
    for (String path : paths) {
      if (path != null) {
        File f = new File(path);
        if (f.exists() && f.isFile() && f.canRead()) {
          return f;
        }
      }
    }
    return null;
  }

  private static PipedOutputStream outputToTFile(TFile file) throws IOException {
    final PipedOutputStream pOut = new PipedOutputStream();
    final PipedInputStream pIn = new PipedInputStream(pOut);
    
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          file.input(pIn);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
    return pOut;
  }
}
