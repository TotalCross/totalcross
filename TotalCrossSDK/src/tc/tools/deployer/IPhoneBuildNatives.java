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

import totalcross.io.File;
import totalcross.util.*;

import java.io.*;

import org.apache.tools.tar.*;
import org.vafer.jdeb.*;
import org.vafer.jdeb.Console;
import org.vafer.jdeb.descriptors.*;
import org.vafer.jdeb.mapping.*;
import org.vafer.jdeb.producers.*;

/**
 * Generates IPhone packages for TotalCross VM and Litebase.
 */
public class IPhoneBuildNatives
{
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
      Vector vFiles = new Vector();

      // parse the parameters
      for (int i = 0; i < args.length; i++)
      {
         String cmd = args[i].toLowerCase();
         if (cmd.charAt(0) == '-')
         {
            switch (cmd.charAt(1))
            {
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

      String author = null;//DeploySettings.companyInfo; // guich@tc100b5_10
      if (author == null)
         author = "guich@superwaba.com.br";

      String maintainer = null;//DeploySettings.companyInfo; // guich@tc100b5_10
      if (maintainer == null)
         maintainer = "guich@superwaba.com.br";
      
      createIPhoneCydia(author, maintainer, name, category, description, location, url, version, binFile, targetDir, vFiles);
      
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

}
