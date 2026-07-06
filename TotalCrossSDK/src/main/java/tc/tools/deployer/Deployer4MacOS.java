// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.deployer;

import java.util.List;

import org.apache.commons.io.FileUtils;

import totalcross.io.File;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

public class Deployer4MacOS {
  private static byte[] versionBytes = { 52, 0, 36, 0 };

  public Deployer4MacOS() throws Exception {
    String macosArguments = DeploySettings.commandLine; // the name of the tcz will be the same as the executable
    byte[] args = macosArguments.trim().getBytes();
    if (args.length > DeploySettings.defaultArgument.length) {
      throw new IllegalArgumentException("Error: command line for macOS too long. It has " + args.length
          + ", while the maximum allowed is " + DeploySettings.defaultArgument.length);
    }

    String targetDir = DeploySettings.targetDir + "macos/";
    File f = new File(targetDir);
    if (!f.exists()) {
      f.createDir();
    }

    Utils.copyTCZFile(targetDir);

    Hashtable ht = new Hashtable(13);
    Utils.processInstallFile("macos.pkg", ht);
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
      byte[] buf = Utils.findAndLoadFile(DeploySettings.etcDir + "launchers/macos/Launcher", false);
      if (buf == null) {
        throw new DeployerException("File macos/Launcher not found!");
      }

      int ofs = Utils.indexOf(buf, DeploySettings.defaultArgument, false);
      if (ofs < 0) {
        throw new DeployerException("Can't find offset for command line on macOS!");
      }
      Vm.arrayCopy(args, 0, buf, ofs, args.length);
      buf[ofs + args.length] = 0;

      ofs = Utils.indexOf(buf, versionBytes, false);
      if (ofs > 0) {
        buf[ofs] = (byte) DeploySettings.getAppVersionHi();
        buf[ofs + 2] = (byte) DeploySettings.getAppVersionLo();
      }

      String out = targetDir + DeploySettings.filePrefix;
      File fout = new File(out, File.CREATE_EMPTY);
      fout.writeBytes(buf, 0, buf.length);
      fout.close();

      java.io.File ff = new java.io.File(out);
      ff.setExecutable(true, false);

      List<java.io.File> defaultTczs = DeploySettings.getDefaultTczs();
      for (java.io.File file : defaultTczs) {
        FileUtils.copyFileToDirectory(file, new java.io.File(targetDir));
      }
      FileUtils.copyFileToDirectory(new java.io.File(DeploySettings.folderTotalCross3DistVM + "/macos/libtcvm.dylib"),
          new java.io.File(targetDir));
    }
    System.out.println("... Files written to folder " + targetDir);
  }
}
