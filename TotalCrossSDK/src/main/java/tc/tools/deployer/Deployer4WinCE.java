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

import java.util.List;

import totalcross.io.DataStream;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

/** An exe for Windows CE */
public class Deployer4WinCE {
  static final int versionOffsetARM = 0x114; // 3.11 = 0x03 0x00 0x0B 0x00

  // Offsets into the byte structure which defines the stub applcation
  static int launchOffset = -1; // dynamically set
  static int bitmap16x16x8_Offset = -1; //      "
  static int bitmap32x32x8_Offset = -1; //      "
  static int bitmap48x48x8_Offset = -1; //      "
  public static boolean keepExe;
  public static boolean keepExeAndDontCreateCabFiles; // guich@tc126_29

  private String cabName; // used in deleteAll
  private String targetDir;

  // guich@200 - to make modifications easier, we now read the prc bytes from disk and modify it.
  byte bytes[];

  String sEx = "The CEStub.exe file has been modified. some of the placeholder strings could not be found. Reinstall TotalCross. - "; // string Exception - sEx - good name, isnt? ;-)
  boolean allTargets;

  public Deployer4WinCE(boolean allTargets) throws Exception {
    this.allTargets = allTargets;
    // suportar tb arquivo .inf alem de .pkg
    String ceArguments = DeploySettings.commandLine.trim(); // the name of the tcz will be the same of the .exe
    if (ceArguments.length() > DeploySettings.defaultArgument.length / 2) {
      throw new IllegalArgumentException("ERROR - launch string too long: " + ceArguments);
    }
    targetDir = Convert.appendPath(DeploySettings.targetDir, "wince");
    File f = new File(targetDir);
    if (!f.exists()) {
      f.createDir();
    }
    if (DeploySettings.mainClassName != null) {
      try {
        String fileName = Convert.appendPath(DeploySettings.etcDir, "launchers/wince/Launcher.exe");
        bytes = Utils.findAndLoadFile(fileName, false);
        if (bytes == null) {
          throw new DeployerException("Could not find " + fileName + ". Please add tc.jar to the classpath!");
        }
        findOffsets(DeploySettings.bitmaps);
        storeInformation(ceArguments, versionOffsetARM);
        DeploySettings.bitmaps.saveWinCEIcons(bytes, bitmap16x16x8_Offset, bitmap32x32x8_Offset, bitmap48x48x8_Offset);
        saveStub(targetDir, DeploySettings.filePrefix, false);
      } catch (Exception e) {
        Utils.println("Exception: " + e.getMessage());
      }
    } else {
      // if no exe files, just create the directories
      try {
        new File(targetDir).createDir();
      } catch (totalcross.io.IOException e) {
      }
    }

    if (keepExe) {
      Utils.copyTCZFile(targetDir);
    }
    if (!keepExeAndDontCreateCabFiles) {
      createInstallFiles();
    }
    if (!keepExe) {
      try {
        new File(Convert.appendPath(targetDir, DeploySettings.filePrefix + ".exe"), File.DONT_OPEN).delete();
      } catch (FileNotFoundException e) {
        // ignore if file wasn't found for deletion
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  private void storeInformation(String args, int versionOffset) throws Exception {
    // launch string
    Utils.writeString(bytes, args, launchOffset, true);
    // version info
    bytes[versionOffset] = (byte) DeploySettings.getAppVersionHi();
    bytes[versionOffset + 2] = (byte) DeploySettings.getAppVersionLo();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  private void saveStub(String userPath, String exeName, boolean show) throws Exception {
    String fileName = DeploySettings.filePrefix + ".exe";
    // write the file
    String fullPath = Convert.appendPath(userPath, fileName);
    if (show) {
      Utils.println("...writing " + fullPath);
    }

    try {
      new File(userPath).createDir();
    } catch (totalcross.io.IOException e) {
    }

    File fos = new File(fullPath, File.CREATE_EMPTY);
    fos.writeBytes(bytes, 0, bytes.length);
    fos.close();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  byte[] icon48x48x8_header = { (byte) 0x28, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x30, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
      (byte) 0x08, (byte) 0x00 };
  byte[] icon32x32x8_header = { (byte) 0x28, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
      (byte) 0x08, (byte) 0x00 };
  byte[] icon16x16x8_header = { (byte) 0x28, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
      (byte) 0x08, (byte) 0x00 };

  // guich@200 - dynamically find all entries
  private void findOffsets(Bitmaps bitmaps) throws Exception {
    launchOffset = Utils.indexOf(bytes, DeploySettings.defaultArgument, false);
    if (launchOffset == -1) {
      throw new DeployerException(sEx + "2a");
    }

    bitmap16x16x8_Offset = Utils.indexOf(bytes, icon16x16x8_header, false);
    bitmap32x32x8_Offset = Utils.indexOf(bytes, icon32x32x8_header, false);
    bitmap48x48x8_Offset = Utils.indexOf(bytes, icon48x48x8_header, false);
    if (bitmap16x16x8_Offset == -1) {
      System.out.println("Could not find offset for 16x16x8");
    }
    if (bitmap32x32x8_Offset == -1) {
      System.out.println("Could not find offset for 32x32x8");
    }
    if (bitmap48x48x8_Offset == -1) {
      System.out.println("Could not find offset for 48x48x8");
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  private String getCabName() {
    return Convert.appendPath(targetDir, cabName + ".cab");
  }

  /////////////////////////////////////////////////////////////////////////////////////
  private void deleteCabs() throws totalcross.io.IOException {
    try {
      new File(getCabName()).delete(); // guich@341_14 replaced '/' by java.io.File.separatorChar
    } catch (FileNotFoundException e) {
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public void deleteTemp(boolean includingInf) {
    try {
      new File(Convert.appendPath(targetDir, cabName + ".dat")).delete();
    } catch (totalcross.io.IOException e) {
    }
    if (includingInf) {
      try {
        new File(Convert.appendPath(targetDir, cabName + ".inf")).delete();
      } catch (totalcross.io.IOException e) {
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  private String toString(Vector v, String post, boolean copyFile) throws Exception {
    String s = "\n";
    int n = v.size();
    for (int i = 0; i < n; i++) {
      String fullName = (String) v.items[i];
      String onlyName = Utils.getFileName(fullName);
      String targetName = Convert.appendPath(targetDir, onlyName);
      // copy the file to the current folder if it does not exists.
      // (cabwiz don't allow pathnames on the input file list)
      if (copyFile && !new File(targetName).exists()) {
        byte[] bytes = Utils.findAndLoadFile(fullName, false);
        if (bytes == null) {
          throw new totalcross.io.IOException("File not found: " + fullName);
        }
        Utils.writeBytes(bytes, targetName);
        v.items[i] = "*" + targetName; // mark the file to be deleted
      }
      // concat the string.
      s += onlyName + post;
    }
    return s;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  public void createInstallFiles() throws Exception {
    boolean infFileGiven = new File("wince.inf").exists();

    String infFileName;
    Vector vLocals = null, vGlobals = null;
    boolean hasExe = DeploySettings.mainClassName != null;

    if (infFileGiven) {
      infFileName = "wince.inf";
      cabName = infFileName.substring(0, Math.min(8, infFileName.length() - 4)); // strip the .inf - guich@421_75
      deleteCabs();
      File oFile = new File(Convert.appendPath(targetDir, infFileName), File.CREATE_EMPTY);
      File iFile = new File("wince.inf", File.READ_ONLY);
      iFile.copyTo(oFile);
      oFile.close();
      iFile.close();
    } else {
      // process the pkg file
      Hashtable ht = new Hashtable(13);
      Utils.processInstallFile("wince.pkg", ht);

      vLocals = (Vector) ht.get("[L]");
      if (vLocals == null) {
        vLocals = new Vector();
      }
      vGlobals = (Vector) ht.get("[G]");
      if (vGlobals == null) {
        vGlobals = new Vector();
      }
      vLocals.addElements(DeploySettings.tczs);
      Utils.preprocessPKG(vLocals, false);
      Utils.preprocessPKG(vGlobals, false);
      String tcFolder = null, lbFolder = null;
      if (DeploySettings.packageVM) // include the vm?
      {
        if (!hasExe) {
          System.out.println("Warning: /p ignored since package has no binary files");
        } else {
          // tc is always included
          // include non-binary files
          List<java.io.File> defaultTczs = DeploySettings.getDefaultTczs();
          for (java.io.File file : defaultTczs) {
            vLocals.addElement(file.getAbsolutePath());
          }
          lbFolder = Convert.appendPath(DeploySettings.folderTotalCross3DistVM, "wince");
          tcFolder = Convert.appendPath(DeploySettings.folderTotalCross3DistVM, "wince");
          // copy binary files
          try {
            new File(targetDir).createDir();
          } catch (Exception e) {
          }
          String name = "/TCVM.dll";
          File.copy(Convert.appendPath(tcFolder, name), Convert.appendPath(targetDir, name));
          if (lbFolder != null) {
            name = "/Litebase.dll";
            File.copy(Convert.appendPath(lbFolder, name), Convert.appendPath(targetDir, name));
          }
        }
      }

      cabName = DeploySettings.filePrefix.trim().replace(' ', '_');
      if (cabName.length() > 20) {
        cabName = cabName.substring(0, 20); // guich@421_75
      }
      deleteCabs();

      infFileName = cabName + ".inf";
      File infFile = new File(Convert.appendPath(targetDir, infFileName), File.CREATE_EMPTY);
      String installDir = "\\TotalCross\\" + DeploySettings.filePrefix; // guich@568_7: removed extra \"
      String inf = "[Version]\n" +

          "Signature       = \"$Windows NT$\"\n" + "Provider           = \""
          + (DeploySettings.companyInfo != null ? Utils.stripNonLetters(DeploySettings.companyInfo) : "TotalCross")
          + "\"\n" + "CESignature     = \"$Windows CE$\"\n" +

          //-----------------------------------------------

          "[CEStrings]\n" +

          "AppName  = \"" + DeploySettings.filePrefix + "\"\n" + "InstallDir   = " + installDir + "\n" +

          //-----------------------------------------------

          "[Strings]\n" +

          "TCDir    = \"\\TotalCross\"\n" +

          //-----------------------------------------------

          "[CEDevice]\n" +
          //            "ProcessorType   = 2577\n" +
          "VersionMin = 3.0\n" + "VersionMax = 7.99\n" + // support wm compact 7
          "BuildMax=0xE0000000\n" + "UnsupportedPlatforms = \"HPC\",\"Jupiter\"\n" +

          //-----------------------------------------------

          "[DefaultInstall]\n" + "CopyFiles   = " + (hasExe ? "Binaries," : "") + "GlobalFiles,LocalFiles\n"
          + "CEShortcuts = Startmenu\n" +

          //-----------------------------------------------
          // directories where the source files are located

          "[SourceDisksNames]\n" + (hasExe ? "1 =, \"Binaries\",,\n" : "") +
          // all local files, ie, local libraries, database pdbs, etc
          "2 =, \"LocalFiles\",,\n" +
          // any global files, ie, global libraries, fonts, etc
          // you must copy them to your app directory so they can be found
          "3 =, \"GlobalFiles\",,\n" +

          //-----------------------------------------------

          "[SourceDisksFiles]\n" +

          (hasExe ? (DeploySettings.filePrefix + ".exe    = 1\n") : "")
          + (tcFolder != null ? ("TCVM.dll      = 1\n") : "") + (lbFolder != null ? ("Litebase.dll  = 1\n") : "")
          + toString(vLocals, " = 2\n", false) + toString(vGlobals, " = 3\n", false) +

          //-----------------------------------------------
          // Ouput directories for files & shortcuts

          // global libraries go to the default TC directory
          // local libraries (and other files) goes to the default instalation directory
          "[DestinationDirs]\n" + (hasExe ? "Binaries = 0,%InstallDir%\n" : "") + "GlobalFiles = 0,%TCDir%\n"
          + "LocalFiles = 0,%InstallDir%\n" + "Startmenu = 0,%CE11%\n" +

          (hasExe ? ("[Binaries]\n" + DeploySettings.filePrefix + ".exe\n") : "")
          + (tcFolder != null ? ("TCVM.dll\n") : "") + (lbFolder != null ? ("Litebase.dll\n") : "") +

          "[LocalFiles]\n" + toString(vLocals, "\n", true) +

          "[GlobalFiles]\n" + toString(vGlobals, "\n", true) +

          (hasExe ? ("[Startmenu]\n" + "\"" + DeploySettings.appTitle + "\", 0, \"" + DeploySettings.filePrefix
              + ".exe\"\n") : "");

      new DataStream(infFile).writeBytes(inf.getBytes());
      infFile.close();
    }

    String path2Cabwiz, out;

    // create the cab
    path2Cabwiz = Utils.findPath(Convert.appendPath(DeploySettings.etcDir, "tools/makecab/Cabwiz.exe"), false);
    if (path2Cabwiz == null) {
      throw new DeployerException(
          "Could not find Cabwiz.exe in directories relative to the classpath. Be sure to add TotalCrossSDK/lib to the classpath");
    }
    // since exec don't allow us to change the current path, we create a batch file that will cd to the current folder
    try {
      new File(Convert.appendPath(targetDir, cabName + ".CAB")).delete();
    } catch (Exception e) {
    }
    String[] callCabWiz = allTargets ? new String[] { path2Cabwiz.replace('/', DeploySettings.SLASH), infFileName } // old wince devices
        : new String[] { path2Cabwiz.replace('/', DeploySettings.SLASH), infFileName, "/compress" };
    out = Utils.exec(callCabWiz, targetDir.replace('/', DeploySettings.SLASH));

    // now we need to wait the process finish. For some reason, the Process.waitFor does not work.
    for (int i = 0; i < 100; i++) {
      if (new File(Convert.appendPath(targetDir, cabName + ".CAB")).exists()) {
        break;
      }
      Vm.sleep(100);
    }
    if (!new File(Convert.appendPath(targetDir, cabName + ".CAB")).exists()) {
      System.err.println("\n\nFailed calling execCabWiz for WM5!\n\nExecution output:\n\n" + out);
    }

    // delete the temp files
    if (!keepExe) {
      deleteTemp(!infFileGiven);
    }
    if (vLocals != null) {
      deleteCopiedFiles(vLocals);
    }
    if (vGlobals != null) {
      deleteCopiedFiles(vGlobals);
    }

    File batFile, iniFile;
    String bat, ini;

    // the cab files were created, now we need to create the bat file to start the instalation
    batFile = new File(Convert.appendPath(targetDir, DeploySettings.filePrefix + "_Install.bat"), File.CREATE_EMPTY);
    bat = "@echo off\r\n" + "\r\n"
        + "echo This script installs this TotalCross Application on a WINDOWS CE or WINDOWS MOBILE device.\r\n"
        + "rem test on the ProgramFiles variable\r\n" + "if not \"%ProgramFiles%\"==\"\" goto ok\r\n"
        + "set ProgramFiles=c:\\Progra~1\r\n" + ":ok\r\n"
        + "\"%ProgramFiles%\\Microsoft ActiveSync\\CeAppMgr.exe\" \".\\" + DeploySettings.filePrefix
        + "_Install.ini\"\r\n" + // guich@tc110_36: surround with ""
        "if \"%errorlevel%\"==\"1\" goto end\r\n" + "\r\n" + "\"%windir%\\WindowsMobile\\CeAppMgr.exe\" \".\\"
        + DeploySettings.filePrefix + "_Install.ini\"\r\n" + // guich@tc110_36: surround with ""
        "if \"%errorlevel%\"==\"1\" goto end\r\n" + "\r\n" + "CeAppMgr.exe \".\\" + DeploySettings.filePrefix
        + "_Install.ini\"\r\n" + // guich@tc110_36: surround with ""
        "if \"%errorlevel%\"==\"0\" goto end\r\n" + "\r\n" + "echo:\r\n"
        + "echo ERROR: Cannot locate CeAppMgr.exe. Please put the\r\n"
        + "echo \"Microsoft ActiveSync\" directory on your path variable.\r\n"
        + "echo It is usually located under \"Program Files\".\r\n" + "echo:\r\n" + "pause\r\n" + ":end\r\n";
    new DataStream(batFile).writeBytes(bat.getBytes());
    batFile.close();
    // and now create the .ini file for installation
    iniFile = new File(Convert.appendPath(targetDir, DeploySettings.filePrefix + "_Install.ini"), File.CREATE_EMPTY);
    ini = "[CEAppManager]\r\n" + "Version        = 1.0\r\n" + "Component      = App\r\n" + "[App]\r\n"
        + "Description    = \"" + DeploySettings.filePrefix + "\"\r\n" + // guich@tc110_36: surround with ""
        "CabFiles       = " + cabName + ".CAB\r\n";
    new DataStream(iniFile).writeBytes(ini.getBytes());
    iniFile.close();

    // delete the inf file
    if (!keepExe) {
      for (int i = 0; i < DeploySettings.tczs.length; i++) {
        try {
          new File(Convert.appendPath(targetDir, Utils.getFileName(DeploySettings.tczs[i]))).delete();
        } catch (FileNotFoundException e) {
        }
      }
    }
    // everything done!
    System.out.println("... Files written to folder " + targetDir);
  }

  private void deleteCopiedFiles(Vector v) throws totalcross.io.IOException {
    for (int i = v.size() - 1; i >= 0; i--) {
      String s = (String) v.items[i];
      if (s.charAt(0) == '*') {
        new File(s.substring(1)).delete();
      }
    }
  }
}
