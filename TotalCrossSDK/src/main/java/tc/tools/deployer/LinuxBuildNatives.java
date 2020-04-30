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

import java.io.DataOutputStream;
import java.io.FileOutputStream;

import org.apache.tools.tar.TarEntry;
import org.vafer.jdeb.Console;
import org.vafer.jdeb.DataProducer;
import org.vafer.jdeb.PackagingException;
import org.vafer.jdeb.Processor;
import org.vafer.jdeb.descriptors.InvalidDescriptorException;
import org.vafer.jdeb.descriptors.PackageDescriptor;
import org.vafer.jdeb.mapping.Mapper;
import org.vafer.jdeb.producers.DataProducerDirectory;
import org.vafer.jdeb.utils.VariableResolver;

import totalcross.io.File;
import totalcross.util.Vector;

/**
 * Generates Linux packages for TotalCross VM and Litebase.
 */
public class LinuxBuildNatives {

  public static void main(String args[]) throws Exception {
    String targetDir = null;
    String binFile = null;
    String name = null;
    String category = null;
    String description = "no description";
    String location = "http://www.totalcross.com/linux";
    String url = "http://www.totalcross.com.br";
    String maintainer = "guich@superwaba.com.br";
    String version = null;
    Vector vFiles = new Vector();

    // parse the parameters
    for (int i = 0; i < args.length; i++) {
      String cmd = args[i].toLowerCase();
      if (cmd.charAt(0) == '-') {
        switch (cmd.charAt(1)) {
        case 'n':
          name = args[++i];
          break;
        case 'c':
          category = args[++i];
          break;
        case 'd':
          description = args[++i];
          break;
        case 'l':
          location = args[++i];
          break;
        case 'u':
          url = args[++i];
          break;
        case 'b':
          binFile = args[++i];
          break;
        case 't':
          targetDir = args[++i];
          break;
        case 'v':
          version = args[++i];
          break;
        case 'm':
          maintainer = args[++i];
          break;
        case 'q':
          DeploySettings.quiet = false;
          break;
        default:
          throw new IllegalArgumentException("Error: bad option: -" + cmd.charAt(1));
        }
      } else {
        vFiles.addElement(args[i]);
      }
    }

    if (name == null) {
      throw new DeployerException("Error: missing -n for the library name!");
    }
    if (category == null) {
      throw new DeployerException("Error: missing -c for the library category!");
    }
    if (version == null) {
      throw new DeployerException("Error: missing -v for the library version!");
    }
    if (binFile == null) {
      throw new DeployerException("Error: missing -b for the binary file!");
    }
    if (targetDir == null) {
      throw new DeployerException("Error: missing -t for the target directory!");
    }

    // create the output folder
    File f = new File(targetDir);
    if (!f.exists()) {
      f.createDir();
      //Utils.copyTCZFile(targetDir);
    }

    createLinuxPackage(name, category, description, location, url, version, maintainer, binFile, targetDir, vFiles);

    System.out.println("... Files written to folder " + targetDir);
  }

  /////////////////////////////////////////////////////////////////////////////////////

  public static void createLinuxPackage(String name, String category, String description, String location, String url,
      String version, String maintainer, String binFile, String targetDir, Vector files) throws Exception {
    Utils.println("...creating Linux Installation...");

    files.addElement(binFile);

    boolean vmPkg = name.equalsIgnoreCase("TotalCross");

    //String baseDir = "install/linux/" + name;
    String baseDir = "install/linux/usr/lib/totalcross";

    Utils.println("baseDir = " + baseDir);
    // create the output folder
    File dir = new File(baseDir);
    if (!dir.exists()) {
      dir.createDir();
    }

    // These are the files to include in the debian package file
    //Vector vExtras = new Vector();

    String controlDir = "install/linux/debian/" + name + "/DEBIAN/";

    Utils.println("controlDir = " + controlDir);
    // create the output folder
    dir = new File(controlDir);
    if (!dir.exists()) {
      dir.createDir();
    }

    //Set<java.io.File> ctrlFiles = new HashSet<java.io.File>();
    java.io.File[] ctrlFiles = new java.io.File[3]; // control files

    String outFile = "control";
    Utils.println("...writing " + outFile);
    DataOutputStream dos = new DataOutputStream(new FileOutputStream(controlDir + outFile));
    dos.writeBytes("Package: " + name + "\n" + "Name: " + name + "\n" + "Version: " + version + "\n"
        + "Architecture: i386\n" + "Priority: optional\n" + "Description: " + description + "\n" + "Homepage: " + url
        + "\n"
        + "Maintainer: " + maintainer + "\n" + "Section: " + category + "\n" + "Depends: " + (vmPkg
            ? "libdirectfb-1.2-0 (>= 1.2.7)" : "totalcross (>= " + getTCVersion(totalcross.sys.Settings.version) + ")")
        + "\n");
    dos.close();
    //ctrlFiles.add(new java.io.File(controlDir + outFile));
    ctrlFiles[0] = new java.io.File(controlDir + outFile);

    VariableResolver vars = null;

    Processor debProc = new Processor(new JDebConsole(), vars);

    Mapper mapper = new Mapper() {
      //@Override
      @Override
      public TarEntry map(TarEntry entry) {
        if (entry.getName().contains("/lib") && entry.getName().contains(".so")) {
          entry.setMode(0100755);
        }
        return entry;
      }
    };
    DataProducer[] dataProd = new DataProducer[1];
    for (int i = 0; i < files.size(); i++) {
      String source = (String) files.items[i];
      String fname = source.substring(source.lastIndexOf('/') + 1);
      String dest = baseDir + "/" + fname;
      Utils.println("file: " + source + " to " + dest);
      Utils.copyFile(source, dest, false);
    }

    outFile = "postinst";
    Utils.println("...writing " + outFile);
    dos = new DataOutputStream(new FileOutputStream(controlDir + outFile));
    dos.writeBytes("#!/bin/sh\n" + "set -e\n" + "if [ \"$1\" = \"configure\" ]; then\n"
        + "  ldconfig /usr/lib/totalcross\n" + "fi\n");
    dos.close();
    java.io.File ff = new java.io.File(controlDir + outFile);
    ff.setExecutable(true, false);
    ctrlFiles[1] = ff;

    outFile = "postrm";
    Utils.println("...writing " + outFile);
    dos = new DataOutputStream(new FileOutputStream(controlDir + outFile));
    dos.writeBytes("#!/bin/sh\n" + "set -e\n" + "if [ \"$1\" = \"remove\" ]; then\n"
        + "  ldconfig /usr/lib/totalcross\n" + "fi\n");
    dos.close();
    ff = new java.io.File(controlDir + outFile);
    ff.setExecutable(true, false);
    ctrlFiles[2] = ff;

    dataProd[0] = new DataProducerDirectory(new java.io.File("install/linux/"),
        new String[] { "usr" + java.io.File.separatorChar + "**" }, // includes
        new String[] {}, // excludes
        new Mapper[] { mapper });
    String debName = name + "_" + version + "_i386.deb";
    java.io.File debOut = new java.io.File("install/linux/" + debName);
    try {
      PackageDescriptor pdsc = debProc.createDeb(ctrlFiles, dataProd, debOut, "bzip2");
      System.out.println("debian package descriptor:\n" + pdsc.toString());
    } catch (PackagingException e) {
      System.err.println(e);
    } catch (InvalidDescriptorException e) {
      System.err.println(e);
    }
    Utils.copyFile("install/linux/" + debName, targetDir + "/" + debName, false);
    totalcross.io.File.deleteDir("install");

    //      int ret = Runtime.getRuntime().exec("dpkg-deb -b cydia/" + name).waitFor();
    //      if (ret != 0)
    //      {
    //         Utils.println("Failed to generate debian pkg: "+ret);
    //         return;
    //      }

    // deferred temp files deletion
    //      for (int i = vFiles.size()-1; i >= 0; i--)
    //         vCleanup.addElement((String)vFiles.items[i]);      

    //vFiles.addElement(name);

    //      long size = 0;
    //      long lastMod = 0;
    //
    //      // These are the files to include in the ZIP file
    //
    //      String icon_png = "Install.png";
    //      String iconfile = "icon60x60.png";
    //      byte[] data = Utils.findAndLoadFile("etc/"+"images/" + iconfile, false);
    //      if (data != null)
    //      {
    //         // write out the icon file
    //         Utils.println("...writing "+icon_png);
    //         dos=new DataOutputStream(new FileOutputStream(icon_png));
    //         dos.write(data);
    //         dos.close();
    //         files.addElement(icon_png);
    //      }
    //      else Utils.println("...didn't found "+DeploySettings.etcDir+"images/" + iconfile);

    // handle global libs
    //      for (int n = 0; n < vGlobals.size(); n++)
    //      {
    //         String path = (String)vGlobals.items[n];
    //         int sp = path.lastIndexOf('/');
    //         String fname = (sp >= 0) ? path.substring(sp+1) : path;
    //         // create symbolic links for each global component
    //         install_commands.append(shellScript("/bin/ln -s " + appPath(name) + fname + " " + appPath("TotalCross") + fname));
    //         // remove symbolic link at uninstall
    //         uninstall_commands.append(shellScript("/bin/rm -f " + appPath("TotalCross") + fname));
    //      }

    //      try
    //      {
    //         // Create the ZIP file
    //         String target = targetDir + "/" + name + ".zip";
    //         ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));
    //         Utils.println("...writing zipfile "+target);
    //
    //         Deployer4IPhone.zipFiles(out, name + ".app/", files);
    //
    //         // Complete the ZIP file
    //         out.close();
    //
    //         java.io.File f = new java.io.File(target);
    //         size = f.length();
    //         lastMod = f.lastModified() / 1000;
    //
    //      }
    //      catch (IOException e)
    //      {
    //         e.printStackTrace();
    //      }
  }

  private static String getTCVersion(int version) {
    String s = (version / 100) + "." + (version % 100);
    if (s.endsWith("0")) {
      s = s.substring(0, s.length() - 1); // 1.20 -> 1.2
    }
    return s;
  }

  static class JDebConsole implements Console {
    //@Override
    @Override
    public void println(String s) {
      Utils.println(s);
    }
  }
}

/*
      createCommon(vFiles, vExtras, baseDir, version, name, cmdLine, uriBase, iconfile, FIRMWARE_2x);


      // control file sample
//      +Package: com.saurik.myprogram
//      +Name: MyProgram
//      +Version: 1.0.4-1
//      +Architecture: iphoneos-arm
//      +Description: an example of using APT Every day people use Cydia, but with the instructions embodied in this package, people can publish for it as well.
//      +Homepage: http://www.saurik.com/id/7
//      -Depiction: http://www.saurik.com/id/7
//      +Maintainer: Your Name <you@example.com>
//      +Author: Jay Freeman (saurik) <saurik@saurik.com>
//      -Sponsor: Microsoft <http://www.microsoft.com/>
//      +Section: Games 

      outFile = "postinst";
      Utils.println("...writing "+outFile);
      dos = new DataOutputStream(new FileOutputStream(controlDir + outFile));
      dos.writeBytes(
            "#!/bin/sh\n" +
            "declare -a cydia\n" +
            "cydia=($CYDIA)\n" +
            "if [[ ${CYDIA+@} ]]; then\n" +
            "  echo \"Enjoy this TotalCross application.\"\n" +
            "  if [[ $1 == install ]]; then\n" +
            "    eval \"echo 'finish:restart' >&${cydia[0]}\"\n" +
            "  fi\n" +
            "  if [[ $1 == upgrade ]]; then\n" +
            "    eval \"echo 'finish:restart' >&${cydia[0]}\"\n" +
            "  fi\n" +
            "  chmod +x /Applications/" + name + ".app/start\n" +
            "  chmod +x /Applications/" + name + ".app/" + name + "\n" +
            "fi\n" +
            "exit 0\n" +
            "");
      dos.close();
      java.io.File ff = new java.io.File(controlDir + outFile);
      ff.setExecutable(true, false);
      //ctrlFiles.add(ff);
      ctrlFiles[1] = ff;

 */