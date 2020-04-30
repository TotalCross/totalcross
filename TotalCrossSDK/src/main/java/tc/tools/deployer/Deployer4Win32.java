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

import org.apache.commons.io.FileUtils;

import de.schlichtherle.truezip.file.TFile;
import totalcross.io.File;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

public class Deployer4Win32 {
  private static byte[] versionBytes = { 52, 0, 36, 0 };

  public Deployer4Win32() throws Exception {
    String winArguments = DeploySettings.commandLine; // the name of the tcz will be the same of the .exe
    byte[] args = winArguments.trim().getBytes();
    if (args.length > DeploySettings.defaultArgument.length) {
      throw new IllegalArgumentException("Error: command line for Win32 too long. It has " + args.length
          + ", while the maximum allowed is " + DeploySettings.defaultArgument.length);
    }

    String targetDir = DeploySettings.targetDir + "win32/";
    // create the output folder
    File f = new File(targetDir);
    if (!f.exists()) {
      f.createDir();
    }
    // copy the tcz file to there
    Utils.copyTCZFile(targetDir);
    // guich@tc123_22: process the pkg file
    Hashtable ht = new Hashtable(13);
    Utils.processInstallFile("win32.pkg", ht);
    Vector vLocals = (Vector) ht.get("[L]");
    if (vLocals != null) {
      Utils.preprocessPKG(vLocals, true);
      for (int i = vLocals.size(); --i >= 0;) {
        Utils.copyEntry((String) vLocals.items[i], targetDir);
      }
    }
    Vector vGlobals = (Vector) ht.get("[G]");
    if (vGlobals != null) {
      Utils.preprocessPKG(vGlobals, true);
      for (int i = vGlobals.size(); --i >= 0;) {
        Utils.copyEntry((String) vGlobals.items[i], targetDir);
      }
    }

    if (DeploySettings.mainClassName != null) {
      // copy the launcher for win32 to there
      byte[] buf = Utils.findAndLoadFile(DeploySettings.etcDir + "launchers/win32/Launcher.exe", false);
      if (buf == null) {
        throw new DeployerException("File win32/Launcher.exe not found!");
      }

      // now find the offset of the argument
      int ofs = Utils.indexOf(buf, DeploySettings.defaultArgument, false);
      if (ofs < 0) {
        throw new DeployerException("Can't find offset for command line on win32!");
      }
      Vm.arrayCopy(args, 0, buf, ofs, args.length); // no "write taking care" needed
      buf[ofs + args.length] = 0;

      // find the offset of the version
      ofs = Utils.indexOf(buf, versionBytes, false);
      if (ofs > 0) // found version offset?
      {
        buf[ofs] = (byte) DeploySettings.getAppVersionHi();
        buf[ofs + 2] = (byte) DeploySettings.getAppVersionLo();
      }

      // find the offset for the icon
      byte[] empty_icon = Utils.findAndLoadFile(DeploySettings.etcDir + "images/empty_icon.ico", false);
      if (empty_icon == null) {
        throw new DeployerException("Can't find empty_icon.ico");
      }
      byte[] skip22 = new byte[empty_icon.length - 22];
      Vm.arrayCopy(empty_icon, 22, skip22, 0, skip22.length);
      int iconOffset = Utils.indexOf(buf, skip22, false);
      if (iconOffset == -1) {
        throw new DeployerException("Can't find offset of empty_icon.ico in Launcher.exe");
      }
      DeploySettings.bitmaps.saveWin32Icon(buf, iconOffset);
      String out = targetDir + DeploySettings.filePrefix + ".exe";
      File fout = new File(out, File.CREATE_EMPTY);
      fout.writeBytes(buf, 0, buf.length);
      fout.close();

      // TCBase & other default tczs
      List<java.io.File> defaultTczs = DeploySettings.getDefaultTczs();
      for (java.io.File file : defaultTczs) {
        FileUtils.copyFileToDirectory(file, new java.io.File(targetDir));
      }
      FileUtils.copyFileToDirectory(new java.io.File(DeploySettings.folderTotalCross3DistVM + "/win32/TCVM.dll"),
          new java.io.File(targetDir));
    }
    System.out.println("... Files written to folder " + targetDir);
  }
}
