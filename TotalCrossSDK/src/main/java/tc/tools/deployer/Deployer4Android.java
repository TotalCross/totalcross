// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.deployer;

import java.io.ByteArrayInputStream;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Adler32;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import totalcross.sys.Convert;
import totalcross.sys.Vm;

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
  private static final Set<String> additionalAndroidPermissions = new LinkedHashSet<>();
  private final Properties signingConfig = new Properties();
  private final String targetDir;
  private final String targetTCZ;
  private String tcFolder;

  public static void addAndroidPermission(String permission) throws DeployerException {
    if (permission == null || permission.isEmpty() || !permission.matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+")) {
      throw new DeployerException("Invalid Android permission name: " + String.valueOf(permission)
          + ". Use a fully qualified name such as android.permission.CAMERA or com.vendor.permission.ACCESS_DEVICE.");
    }
    additionalAndroidPermissions.add(permission);
  }

  /** Edits decoded aapt.pb.XmlNode text without disturbing unrelated manifest nodes. */
  static final class ManifestPermissionEditor {
    private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";
    private static final Set<String> OPTIONAL_PERMISSIONS = new HashSet<>();

    static {
      OPTIONAL_PERMISSIONS.add("android.permission.READ_MEDIA_IMAGES");
      OPTIONAL_PERMISSIONS.add("android.permission.READ_MEDIA_VIDEO");
      OPTIONAL_PERMISSIONS.add("android.permission.MANAGE_EXTERNAL_STORAGE");
      OPTIONAL_PERMISSIONS.add("android.permission.REQUEST_INSTALL_PACKAGES");
    }

    private static final class Block {
      final int start;
      final int open;
      final int end;

      Block(int start, int open, int end) {
        this.start = start;
        this.open = open;
        this.end = end;
      }

      String body(String text) {
        return text.substring(open + 1, end - 1);
      }
    }

    private static final class PermissionMask {
      String text;
      final List<String> markers = new ArrayList<>();
      final List<String> blocks = new ArrayList<>();
    }

    static PermissionMask maskPermissionBlocks(String decoded) {
      PermissionMask mask = new PermissionMask();
      List<Block> roots = findBlocks(decoded, 0, decoded.length(), "element");
      if (roots.isEmpty()) {
        mask.text = decoded;
        return mask;
      }
      Block root = roots.get(0);
      List<Block> children = findBlocks(decoded, root.open + 1, root.end - 1, "child");
      StringBuilder masked = new StringBuilder(decoded);
      for (int i = children.size() - 1; i >= 0; i--) {
        Block child = children.get(i);
        if (permissionName(decoded, child) != null) {
          String marker = "TC_PERMISSION_BLOCK_" + mask.blocks.size() + "_";
          mask.markers.add(0, marker);
          mask.blocks.add(0, decoded.substring(child.start, child.end));
          masked.replace(child.start, child.end, "child { text: \"" + marker + "\" }");
        }
      }
      mask.text = masked.toString();
      return mask;
    }

    static String restorePermissionBlocks(String transformed, PermissionMask mask) {
      String restored = transformed;
      for (int i = 0; i < mask.markers.size(); i++) {
        restored = restored.replace("child { text: \"" + mask.markers.get(i) + "\" }", mask.blocks.get(i));
      }
      return restored;
    }

    static String edit(String decoded, Set<String> requestedPermissions) {
      List<Block> roots = findBlocks(decoded, 0, decoded.length(), "element");
      if (roots.isEmpty()) {
        throw new IllegalArgumentException("Decoded Android manifest has no aapt.pb XmlNode element.");
      }

      Block root = roots.get(0);
      List<Block> children = findBlocks(decoded, root.open + 1, root.end - 1, "child");
      Set<String> retainedNames = new HashSet<>();
      List<Block> removals = new ArrayList<>();
      for (Block child : children) {
        String name = permissionName(decoded, child);
        if (name == null) {
          continue;
        }
        if ((OPTIONAL_PERMISSIONS.contains(name) && !requestedPermissions.contains(name)) || !retainedNames.add(name)) {
          removals.add(child);
        }
      }

      StringBuilder edited = new StringBuilder(decoded);
      for (int i = removals.size() - 1; i >= 0; i--) {
        Block child = removals.get(i);
        edited.delete(child.start, child.end);
      }
      String result = edited.toString();

      List<String> missing = new ArrayList<>();
      for (String permission : requestedPermissions) {
        if (!retainedNames.contains(permission)) {
          missing.add(permission);
          retainedNames.add(permission);
        }
      }
      if (missing.isEmpty()) {
        return result;
      }

      roots = findBlocks(result, 0, result.length(), "element");
      root = roots.get(0);
      children = findBlocks(result, root.open + 1, root.end - 1, "child");
      String rootIndent = indentationAt(result, root.start);
      String childIndent = children.isEmpty() ? rootIndent + "  " : indentationAt(result, children.get(0).start);
      String nestedIndent = childIndent + "  ";
      String attributeIndent = nestedIndent + "  ";
      String compiledIndent = attributeIndent + "  ";
      String stringIndent = compiledIndent + "  ";
      int close = root.end - 1;
      int contentEnd = close;
      while (contentEnd > root.open + 1 && Character.isWhitespace(result.charAt(contentEnd - 1))) {
        contentEnd--;
      }
      String newline = result.indexOf("\r\n") >= 0 ? "\r\n" : "\n";
      StringBuilder additions = new StringBuilder();
      for (String permission : missing) {
        additions.append(newline).append(childIndent).append("child {")
            .append(newline).append(nestedIndent).append("element {")
            .append(newline).append(attributeIndent).append("namespace_uri: \"\"")
            .append(newline).append(attributeIndent).append("name: \"uses-permission\"")
            .append(newline).append(attributeIndent).append("attribute {")
            .append(newline).append(compiledIndent).append("namespace_uri: \"").append(ANDROID_NAMESPACE).append("\"")
            .append(newline).append(compiledIndent).append("name: \"name\"")
            .append(newline).append(compiledIndent).append("value: \"").append(permission).append("\"")
            .append(newline).append(compiledIndent).append("resource_id: 16842755")
            .append(newline).append(compiledIndent).append("compiled_item {")
            .append(newline).append(stringIndent).append("str {")
            .append(newline).append(stringIndent).append("  value: \"").append(permission).append("\"")
            .append(newline).append(stringIndent).append("}")
            .append(newline).append(compiledIndent).append("}")
            .append(newline).append(attributeIndent).append("}")
            .append(newline).append(nestedIndent).append("}")
            .append(newline).append(childIndent).append("}");
      }
      return result.substring(0, contentEnd) + additions + result.substring(contentEnd);
    }

    private static String permissionName(String text, Block child) {
      List<Block> elements = findBlocks(text, child.open + 1, child.end - 1, "element");
      if (elements.isEmpty()) {
        return null;
      }
      Block element = elements.get(0);
      String elementName = scalar(element.body(text), "name");
      if (elementName == null || (!"uses-permission".equals(elementName) && !elementName.startsWith("uses-permission-"))) {
        return null;
      }
      for (Block attribute : findBlocks(text, element.open + 1, element.end - 1, "attribute")) {
        if ("name".equals(scalar(attribute.body(text), "name"))) {
          String value = scalar(attribute.body(text), "value");
          if (value == null) {
            List<Block> compiled = findBlocks(text, attribute.open + 1, attribute.end - 1, "compiled_item");
            if (!compiled.isEmpty()) {
              List<Block> strings = findBlocks(text, compiled.get(0).open + 1, compiled.get(0).end - 1, "str");
              if (!strings.isEmpty()) {
                value = scalar(strings.get(0).body(text), "value");
              }
            }
          }
          return value;
        }
      }
      return null;
    }

    private static String scalar(String body, String field) {
      Pattern pattern = Pattern.compile("(?m)^\\s*" + Pattern.quote(field) + "\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
      Matcher matcher = pattern.matcher(body);
      return matcher.find() ? unescapeProtoString(matcher.group(1)) : null;
    }

    private static String unescapeProtoString(String value) {
      return value.replace("\\\\", "\\").replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r");
    }

    private static String indentationAt(String text, int position) {
      int lineStart = position;
      while (lineStart > 0 && text.charAt(lineStart - 1) != '\n' && text.charAt(lineStart - 1) != '\r') {
        lineStart--;
      }
      int end = lineStart;
      while (end < position && (text.charAt(end) == ' ' || text.charAt(end) == '\t')) {
        end++;
      }
      return text.substring(lineStart, end);
    }

    private static List<Block> findBlocks(String text, int start, int end, String field) {
      List<Block> blocks = new ArrayList<>();
      int depth = 0;
      boolean quoted = false;
      boolean escaped = false;
      for (int i = start; i < end; i++) {
        char c = text.charAt(i);
        if (quoted) {
          if (escaped) {
            escaped = false;
          } else if (c == '\\') {
            escaped = true;
          } else if (c == '"') {
            quoted = false;
          }
          continue;
        }
        if (c == '"') {
          quoted = true;
          continue;
        }
        if (depth == 0 && tokenAt(text, i, field)) {
          int open = i + field.length();
          while (open < end && Character.isWhitespace(text.charAt(open))) {
            open++;
          }
          if (open < end && text.charAt(open) == '{') {
            int close = matchingBrace(text, open, end);
            if (close >= 0) {
              blocks.add(new Block(i, open, close + 1));
              i = close;
              continue;
            }
          }
        }
        if (c == '{') {
          depth++;
        } else if (c == '}') {
          depth--;
        }
      }
      return blocks;
    }

    private static boolean tokenAt(String text, int position, String field) {
      if (!text.regionMatches(position, field, 0, field.length())) {
        return false;
      }
      if (position > 0 && !Character.isWhitespace(text.charAt(position - 1))) {
        return false;
      }
      int after = position + field.length();
      return after >= text.length() || Character.isWhitespace(text.charAt(after)) || text.charAt(after) == '{';
    }

    private static int matchingBrace(String text, int open, int end) {
      int depth = 0;
      boolean quoted = false;
      boolean escaped = false;
      for (int i = open; i < end; i++) {
        char c = text.charAt(i);
        if (quoted) {
          if (escaped) {
            escaped = false;
          } else if (c == '\\') {
            escaped = true;
          } else if (c == '"') {
            quoted = false;
          }
          continue;
        }
        if (c == '"') {
          quoted = true;
        } else if (c == '{') {
          depth++;
        } else if (c == '}' && --depth == 0) {
          return i;
        }
      }
      return -1;
    }
  }

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
    DeployLogger.verbose("Android application folder: /data/data/" + (targetPackage.replace('/', '.')));

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
    final String protocExecutable = AndroidToolLocator.protoc();
    String[] decodeCmd = { protocExecutable, "--decode=aapt.pb.XmlNode", "--proto_path=tools", "Configuration.proto", "Resources.proto" };
    
    Process process = Runtime.getRuntime().exec(decodeCmd, null, new File(DeploySettings.etcDir, "tools/android"));
    final File decodedManifestFile = File.createTempFile("AndroidManifest2_" + DeploySettings.appTitle, ".xml");
    decodedManifestFile.deleteOnExit();
    IOUtils.copy(new FileInputStream(manifestFile), process.getOutputStream());
    process.getOutputStream().close();
    String originalManifest = IOUtils.toString(process.getInputStream(), "UTF-8");
    ManifestPermissionEditor.PermissionMask permissionMask = ManifestPermissionEditor.maskPermissionBlocks(originalManifest);
    originalManifest = permissionMask.text;
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
    originalManifest = ManifestPermissionEditor.restorePermissionBlocks(originalManifest, permissionMask);
    Set<String> requestedPermissions = new LinkedHashSet<>(additionalAndroidPermissions);
    if (permissionManageExternalStorage) {
      requestedPermissions.add("android.permission.MANAGE_EXTERNAL_STORAGE");
    }
    if (permissionRequestInstallPackages) {
      requestedPermissions.add("android.permission.REQUEST_INSTALL_PACKAGES");
    }
    originalManifest = ManifestPermissionEditor.edit(originalManifest, requestedPermissions);
    
    if (DeployLogger.isVerbose()) {
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
    byte[] encodedManifest;
    try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      IOUtils.copy(encodeProcess.getInputStream(), output);
      encodedManifest = output.toByteArray();
    }
    String encodeError = IOUtils.toString(encodeProcess.getErrorStream(), "UTF-8");
    if (encodeProcess.waitFor() != 0) {
      throw new DeployerException("Failed to encode AndroidManifest.xml with protoc: " + encodeError);
    }
    if (encodedManifest.length == 0) {
      throw new DeployerException("protoc encoded an empty AndroidManifest.xml");
    }
    b.input(new ByteArrayInputStream(encodedManifest));

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
      DeployLogger.warn("Could not find 'google-services.json', thus Firebase will be ignored further on (Android deploy)");
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
    final String bundletoolJar = AndroidToolLocator.bundletool();
    javaCmdList.add(javaExe);
    javaCmdList.add("-Djava.io.tmpdir=" + new File(targetDir).getAbsolutePath());
    javaCmdList.add("-jar");
    javaCmdList.add(bundletoolJar);
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
    if (DeployLogger.isVerbose() && execOutput != null && !execOutput.isEmpty()) {
      DeployLogger.debug(execOutput);
    }

    FileUtils.moveFile(new File(targetDir, fileName + ".apks/universal.apk"), new File(targetDir, fileName + ".apk"));
    FileUtils.deleteQuietly(new File(targetDir, fileName + ".apks"));

    String apk = targetDir + "/" + fileName + ".apk";

    String extraMsg = "";
    if (DeploySettings.installPlatforms.indexOf("android,") >= 0) {
      extraMsg = callADB(apk);
    }

    DeployLogger.normal("... Files written to folder " + targetDir + extraMsg);
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
    DeployLogger.warn(message);
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
      DeployLogger.verbose("Is service.");
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
    AndroidPackageFiles.write(z, targetTCZ, tcFolder);
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
