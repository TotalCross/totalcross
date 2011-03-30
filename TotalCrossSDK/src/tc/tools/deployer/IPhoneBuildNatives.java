/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools.deployer;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import org.apache.tools.tar.TarEntry;
import org.vafer.jdeb.Console;
import org.vafer.jdeb.DataProducer;
import org.vafer.jdeb.Processor;
import org.vafer.jdeb.descriptors.PackageDescriptor;
import org.vafer.jdeb.mapping.Mapper;
import org.vafer.jdeb.producers.DataProducerDirectory;

import totalcross.io.File;
import totalcross.util.Vector;

/**
 * Generates IPhone packages for TotalCross VM and Litebase.
 */
public class IPhoneBuildNatives
{
   public final static int FIRMWARE_1x = 1;
   public final static int FIRMWARE_2x = 2;

   public static void main(String args[]) throws Exception
   {
      String targetDir = null;
      String binFile = null;
      String name = null;
      String category = null;
      String description = "no description";
      String location = "http://www.totalcross.com/iphone";
      String url = "http://www.totalcross.com.br";
      String version = null;
      int firmware_version = FIRMWARE_1x;
      Vector vFiles = new Vector();

      // parse the parameters
      for (int i = 0; i < args.length; i++)
      {
         String cmd = args[i].toLowerCase();
         if (cmd.charAt(0) == '-')
         {
            switch (cmd.charAt(1))
            {
            case '1':
               firmware_version = FIRMWARE_1x;
               break;
            case '2':
               firmware_version = FIRMWARE_2x;
               break;
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
            case 'q':
               DeploySettings.quiet = false;
               break;
            default:
               throw new IllegalArgumentException("Error: bad option: -" + cmd.charAt(1));
            }
         }
         else
            vFiles.addElement(args[i]);
      }

      if (name == null)
         throw new DeployerException("Error: missing -n for the library name!");
      if (category == null)
         throw new DeployerException("Error: missing -c for the library category!");
      if (version == null)
         throw new DeployerException("Error: missing -v for the library version!");
      if (binFile == null)
         throw new DeployerException("Error: missing -b for the binary file!");
      if (targetDir == null)
         throw new DeployerException("Error: missing -t for the target directory!");

      // create the output folder
      File f = new File(targetDir);
      if (!f.exists())
         f.createDir();
      //Utils.copyTCZFile(targetDir);

      createIPhonePackage(name, category, description, location, url, version, binFile, targetDir, firmware_version, vFiles);

//      createIPhoneInstaller(version, DeploySettings.mainClassName, DeploySettings.filePrefix,
//            DeploySettings.appTitle, iPhoneArguments, category, location, url, uriBase, description, iconfile, firmware_version);

      String author = null;//DeploySettings.companyInfo; // guich@tc100b5_10
      if (author == null)
         author = "guich@superwaba.com.br";

      String maintainer = null;//DeploySettings.companyInfo; // guich@tc100b5_10
      if (maintainer == null)
         maintainer = "guich@superwaba.com.br";
      
      if (firmware_version ==  FIRMWARE_2x /*&& DeploySettings.isUnix()*/)
         createIPhoneCydia(author, maintainer, name, category, description, location, url, version, binFile, targetDir, vFiles);
//      author, maintainer, version, DeploySettings.mainClassName, DeploySettings.filePrefix,
//                           DeploySettings.appTitle, iPhoneArguments, category, location, url, uriBase, description, iconfile);
      
      System.out.println("... Files written to folder "+targetDir);
   }

   public static void createIPhoneCydia(String author, String maintainer, final String name, String category, String description,
                                       String location, String url, String version, String binFile, String targetDir, Vector fileset) throws Exception
   {
      Utils.println("...creating IPhone Installation...");
      
      Vector files = new Vector(fileset.toObjectArray());
      files.addElement(binFile);
      
      String uriBase = "com.totalcross";
      boolean vmPkg = name.equalsIgnoreCase("TotalCross");
      
      try {totalcross.io.File.deleteDir("install/temp/cydia");} catch (Exception e) {/* ignore path if it doesnt exist */} 
      String baseDir = "install/temp/cydia/Applications/" + name + ".app";
      
      Utils.println("baseDir = " + baseDir);
      // create the output folder
      File dir = new File(baseDir);
      if (!dir.exists())
      dir.createDir();
      
      // These are the files to include in the debian package file
      //Vector vExtras = new Vector();
      
      String controlDir = "install/temp/cydia/debian/" + name + "/DEBIAN/";
      
      Utils.println("controlDir = " + controlDir);
      // create the output folder
      dir = new File(controlDir);
      if (!dir.exists())
      dir.createDir();
      
      //Set<java.io.File> ctrlFiles = new HashSet<java.io.File>();
      java.io.File[] ctrlFiles = new java.io.File[2]; // control files
      
      String outFile = "control";
      Utils.println("...writing "+outFile);
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(controlDir + outFile));
      String packageStr = uriBase + ".iphone." + name;
      dos.writeBytes(
            "Package: " + packageStr + "\n" +
            "Name: " + name + "\n" +
            "Version: " + version + "\n" +
            "Architecture: iphoneos-arm\n" +
            "Priority: optional\n" +
            "Description: " + description + "\n" +
            "Homepage: " + url + "\n" +
            "Author: " + author + "\n" +
            "Maintainer: " + maintainer + "\n" +
            "Section: " + category + "\n" +
            //"Icon: http://www.superwaba.net/cydia/icon.png\n" +
            ""
//         "Depends: " + (vmPkg ? "libdirectfb-1.2-0 (>= 1.2.7)" : "totalcross (>= 1.15)") + "\n"
         );
      dos.close();
      //ctrlFiles.add(new java.io.File(controlDir + outFile));
      ctrlFiles[0] = new java.io.File(controlDir + outFile);
      
      outFile = "postinst";
      Utils.println("...writing "+outFile);
      dos = new DataOutputStream(new FileOutputStream(controlDir + outFile));
      dos.writeBytes(
            "#!/bin/sh\n" +
            "declare -a cydia\n" +
            "cydia=($CYDIA)\n" +
            "if [[ ${CYDIA+@} ]]; then\n" +
            "  echo \"Enjoy this TotalCross product. $1\" > /tmp/install.log\n" +
            (vmPkg ? "" :
            "  if [[ $1 == install ]]; then\n" +
            "     ln -s /Applications/" + name + ".app/lib" + name + ".dylib /Applications/TotalCross.app/\n" +
            "     ln -s /Applications/" + name + ".app/" + name + "Lib.tcz /Applications/TotalCross.app/\n" +
            "  fi\n" +
            "  if [[ $1 == remove ]]; then\n" +
            "     rm /Applications/TotalCross.app/lib" + name + ".dylib\n" +
            "     rm /Applications/TotalCross.app/" + name + "Lib.tcz\n" +
            "  fi\n")
            +
            "fi\n" +
            //flsobral@tc125: setup symbolic links on manual installation for Litebase.
            (vmPkg ? "" :
               " ln -s /Applications/" + name + ".app/lib" + name + ".dylib /Applications/TotalCross.app/\n" +
               " ln -s /Applications/" + name + ".app/" + name + "Lib.tcz /Applications/TotalCross.app/\n")
            +
            "exit 0\n" +
            "");
      dos.close();
      java.io.File ff = new java.io.File(controlDir + outFile);
      ff.setExecutable(true, false);
      ctrlFiles[1] = ff;
      
      Processor debProc = new Processor(
            new Console() //flsobral@tc115: The anonymous class methods can't be obfuscated since they implement an interface
            {
               public void println(String s)
               {
                  Utils.println(s);
               }            
            },
            null); 
      
      Mapper mapper = new Mapper()
      {
         //@Override
         public TarEntry map(TarEntry entry)
         {
            if ((entry.getName().contains("/lib") && entry.getName().contains(".dylib"))
                  || entry.getName().equals("tcpriv"))
               entry.setMode(0100755);
            return entry;
         }         
      };
      DataProducer[] dataProd = new DataProducer[1];
      for (int i = 0; i < files.size(); i++)
      {
         String source = (String)files.items[i];
         String fname = source.substring(source.lastIndexOf('/') + 1);
         String dest = baseDir + "/" + fname;
         Utils.println("file: " + source + " to " + dest);
         Utils.copyFile(source, dest, false);
      }
      
      dataProd[0] = new DataProducerDirectory(new java.io.File("install/temp/cydia/"), 
                        new String[] { "Applications" + java.io.File.separatorChar + "**" }, // includes 
                        new String[0], // excludes
                        new Mapper[] { mapper }); 
      // Create the DEB file
      String debName = targetDir + "/" + name + ".deb";
      java.io.File debOut = new java.io.File(debName);
      try
      {
         PackageDescriptor pdsc = debProc.createDeb(ctrlFiles, dataProd, debOut, "bzip2");
         Utils.println("debian package descriptor:\n" + pdsc.toString());
      } 
      catch (Exception e)
      {
         System.err.println(e);
         throw e;
      } 

      // guich@tc126_54: creates the bz2 file
      
      String Package = 
         "Package: " + packageStr + "\n" +
         "Version: " + version + "\n" +
         "Architecture: iphoneos-arm\n" +
         "Priority: optional\n" +
         "Description: " + description + "\n" +
         "Homepage: " + url + "\n" +
         "Author: " + author + "\n" +
         "Maintainer: " + maintainer + "\n" +
         "Section: " + category + "\n" +
         "Installed-Size: "+Deployer4IPhone.getInstalledSizeInKB("install/temp/cydia/Applications/"+name+".app")+"\n"+
         "Filename: ./"+name+".deb \n" +
         "Size: " + Deployer4IPhone.getFileSize(debName) + "\n" +
         "MD5sum: " + Deployer4IPhone.getMD5(debName) + "\n";
      String PackagesFile = "install/temp/Packages";
      File fpack = new File(PackagesFile, File.CREATE_EMPTY);
      fpack.writeBytes(Package);
      fpack.close();
      
      Deployer4IPhone.createBZip2(PackagesFile, targetDir+"/Packages.bz2");
      
      // deferred temp files deletion
//      for (int i = vFiles.size()-1; i >= 0; i--)
//         vCleanup.addElement((String)vFiles.items[i]);
      
   }
   
   /////////////////////////////////////////////////////////////////////////////////////

   public static void createIPhonePackage(String name, String category, String description, String location, String url,
         String version, String binFile, String targetDir, int firmware_version, Vector fileset) throws Exception
   {
      Utils.println("...creating iPhone Installation...");

      Vector files = new Vector(fileset.toObjectArray());
      files.addElement(binFile);

//      String location = "http://www.totalcross.com/iphone";
//      String url = "http://www.totalcross.com.br";
      String uriBase = "com.totalcross";
      boolean vmPkg = name.equals("TotalCross");

//         name = "TotalCross";
//         category = "TotalCross VM";
//         description = "The TotalCross Virtual Machine";

//         vFiles.addElement(sdkDir + "/dist/TCBase.tcz");
//         vFiles.addElement(sdkDir + "/etc/fonts/TCFont.tcz");
//         vFiles.addElement(binDir + "/builders/gcc-posix/tcvm/iphone/.libs/libtcvm.dylib");

//         name = "Litebase";
//         category = "TotalCross Litebase";
//         description = "TotalCross Litebase library";
//         libname = "lib" + name + ".dylib";
//         libtczname = name + "Lib.tcz";
//         vFiles.addElement(binDir + "/builders/gcc/iphone/.libs/" + libname);
//         vFiles.addElement(sdkDir + "/dist/" + libtczname);

      Utils.println("category    : " + category);
      Utils.println("location    : " + location);
      Utils.println("url         : " + url);
      Utils.println("category    : " + uriBase);
      Utils.println("description : " + description);

      //vFiles.addElement(name);
      DataOutputStream dos;

      long size = 0;
      long lastMod = 0;

      // These are the files to include in the ZIP file

      if (firmware_version == FIRMWARE_1x)
      {
         try
         {
            // Create the ZIP file
            String target = targetDir + "/" + name + ".zip";
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));
            Utils.println("...writing zipfile "+target);

            Deployer4IPhone.zipFiles(out, name + ".app/", files);

            // Complete the ZIP file
            out.close();

            java.io.File f = new java.io.File(target);
            size = f.length();
            lastMod = f.lastModified() / 1000;

         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      if (firmware_version == FIRMWARE_2x)
      {
         String icon_png = "Install.png";
         String iconfile = "icon60x60.png";
         byte[] data = Utils.findAndLoadFile("etc/"+"images/" + iconfile, false);
         if (data != null)
         {
            // write out the icon file
            Utils.println("...writing "+icon_png);
            dos=new DataOutputStream(new FileOutputStream(icon_png));
            dos.write(data);
            dos.close();
            files.addElement(icon_png);
         }
         else Utils.println("...didn't found "+DeploySettings.etcDir+"images/" + iconfile);
      }

      StringBuffer install_commands = new StringBuffer(Deployer4IPhone.copyPathScript(name));
      StringBuffer uninstall_commands = new StringBuffer("");

      // due to the lack of permissions in java.util.zip
      install_commands.append(Deployer4IPhone.shellScript("/bin/chmod -R 777 /Applications/" + name + ".app")); // fdie@ before a safer deployment...
      if (vmPkg)
      {
         install_commands.append(Deployer4IPhone.shellScript("/bin/chmod 777 /Applications/" + name + ".app")); // fdie@ before a safer deployment...
         install_commands.append(Deployer4IPhone.shellScript("/bin/chmod u+s /Applications/" + name + ".app/tcpriv"));
      }
      else
      {
         // symbolic link as a shortcut to call the VM
         //install_commands.append(shellScript("/bin/ln -s " + appPath("TotalCross") + "TCBase " + appPath(name) + name));
         String libname = "lib" + name + ".dylib";
         String libtczname = name + "Lib.tcz";
         // symbolic link to make the new app a subfolder of the VM root folder
         install_commands.append(Deployer4IPhone.shellScript("/bin/ln -s " + Deployer4IPhone.appPath(name) + libname + " " + Deployer4IPhone.appPath("TotalCross") + libname));
         install_commands.append(Deployer4IPhone.shellScript("/bin/ln -s " + Deployer4IPhone.appPath(name) + libtczname + " " + Deployer4IPhone.appPath("TotalCross") + libtczname));
         // remove symbolic link at uninstall
         uninstall_commands.append(Deployer4IPhone.shellScript("/bin/rm -f " + Deployer4IPhone.appPath("TotalCross") + libname));
         uninstall_commands.append(Deployer4IPhone.shellScript("/bin/rm -f " + Deployer4IPhone.appPath("TotalCross") + libtczname));
      }

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
      StringBuffer update_commands = new StringBuffer(uninstall_commands); // update = uninstall (w/o folder deletion for VM) + install
      if (!vmPkg)
         update_commands.append(Deployer4IPhone.removePathScript(name));
      update_commands.append(install_commands);

      uninstall_commands.append(Deployer4IPhone.removePathScript(name)); // now update is set, finalize the uninstall

      // dependency check: VM package must be installed for any TotalCross package except the VM itself
      String preinstall_commands = vmPkg ? "" :
              "      <array>\n"+
              "        <string>IfNot</string>\n"+
              "        <array>\n"+
              "          <array>\n"+
              "            <string>InstalledPackage</string>\n"+
              "            <string>com.totalcross.iphone.TotalCross</string>\n"+
              "          </array>\n"+
              "        </array>\n"+
              "        <array>\n"+
              "          <array>\n"+
              "            <string>AbortOperation</string>\n"+
              "            <string>Please install the TotalCross VM first.</string>\n"+
              "          </array>\n"+
              "        </array>\n"+
              "      </array>\n";

      // write out the install plist file
      String deployFile = firmware_version == FIRMWARE_2x ? "Install.plist" : (targetDir + "/" + name + ".plist");
      Utils.println("...writing "+deployFile);
      dos=new DataOutputStream(new FileOutputStream(deployFile));
      dos.writeBytes(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"+
            "<plist version=\"1.0\">\n"+
            "<dict>\n");
      if (firmware_version == FIRMWARE_2x)
         dos.writeBytes(
            "  <key>identifier</key>\n");
      else
         dos.writeBytes(
            "  <key>bundleIdentifier</key>\n");
      dos.writeBytes(
            "  <string>" + uriBase + ".iphone." + name + "</string>\n"+
            "  <key>name</key>\n"+
            "  <string>" + name + "</string>\n"+
            "  <key>version</key>\n"+
            "  <string>" + version + "</string>\n"+
            "  <key>location</key>\n"+
            "  <string>" + location + "/" + name + ".zip</string>\n"+
            "  <key>url</key>\n"+
            "  <string>" + url + "</string>\n"+
            "  <key>size</key>\n"+
            "  <string>" + size + "</string>\n"+
            "  <key>description</key>\n"+
            "  <string>" + description + "</string>\n"+
            "  <key>category</key>\n"+
            "  <string>" + category + "</string>\n"+
            "  <key>scripts</key>\n"+
            "  <dict>\n"+
            "    <key>preflight</key>\n"+ // PRE-INSTALL
            "    <array>\n"+
            preinstall_commands+
            "    </array>\n"+
            "    <key>install</key>\n"+ // INSTALL
            "    <array>\n"+
            install_commands.toString()+
            "    </array>\n"+
            "    <key>uninstall</key>\n"+ // UNINSTALL
            "    <array>\n"+
            (vmPkg ? Deployer4IPhone.noticeScript("You may have to reinstall all the TotalCross apps currently installed!") : "")+
            uninstall_commands.toString()+
            "    </array>\n"+
            "    <key>update</key>\n"+ // UPDATE
            "    <array>\n"+
            update_commands.toString()+
            "    </array>\n"+
            "  </dict>\n"+
            "  <key>date</key><string>" + lastMod + "</string>\n");
      if (firmware_version == FIRMWARE_2x)
         dos.writeBytes(
            "  <key>minOSRequired</key><string>2.0</string>\n");
      dos.writeBytes(
            "</dict>\n"+
            "</plist>\n");

      if (firmware_version == FIRMWARE_2x)
      {
         files.addElement(deployFile);

         try
         {
            // Create the ZIP file
            String target = targetDir + "/" + name + ".zip";
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));
            Utils.println("...writing zipfile "+target);

            Deployer4IPhone.zipFiles(out, name + ".app/", files);

            // Complete the ZIP file
            out.close();

            java.io.File f = new java.io.File(target);
            size = f.length();
            lastMod = f.lastModified() / 1000;

         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }
}
