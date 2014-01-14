/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.tools.deployer;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.tools.tar.TarEntry;
import org.vafer.jdeb.*;
import org.vafer.jdeb.Console;
import org.vafer.jdeb.descriptors.PackageDescriptor;
import org.vafer.jdeb.mapping.Mapper;
import org.vafer.jdeb.producers.DataProducerDirectory;
import tc.tools.deployer.bzip2.CBZip2OutputStream;
import totalcross.crypto.NoSuchAlgorithmException;
import totalcross.crypto.digest.MD5Digest;
import totalcross.io.File;
import totalcross.io.IllegalArgumentIOException;
import totalcross.sys.*;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

/**
 * Generates IPhone application packages.
 */
public class Deployer4IPhone
{
   private String targetDir;
   
   public static void run() throws Exception
   {
      new Deployer4IPhone();
      cleanup();
   }

   public Deployer4IPhone() throws Exception
   {
      targetDir = DeploySettings.targetDir + "ios";
      // create the output folder
      File f = new File(targetDir);
      if (!f.exists())
         f.createDir();

      if (DeploySettings.mainClassName != null)
      {
         String version = DeploySettings.appVersion != null ? DeploySettings.appVersion : DeploySettings.filePrefix.equals("TotalCross") ? totalcross.sys.Settings.versionStr : "1.0";

         // copy the launcher for win32 to there
         byte[] buf = Utils.findAndLoadFile(DeploySettings.etcDir + "launchers/ios/Launcher", false);
         if (buf == null)
            throw new DeployerException("File launchers/ios/Launcher not found!");

         File fout = new File(DeploySettings.filePrefix, File.CREATE_EMPTY);
         fout.writeBytes(buf, 0, buf.length);
         fout.close();

         String[][] infos = new String[][]{
               { null, "A TotalCross application.", "Other TotalCross samples" },
               { "samples/apps", "A Litebase sample.", "Litebase applications" },
               { "samples/sys", "A Litebase tool.", "Litebase tools" },
               { "tc/samples/app", "A TotalCross APP sample.", "TotalCross APP samples" },
               { "tc/samples/sys", "A TotalCross SYS sample.", "TotalCross SYS samples" },
               { "tc/samples/net", "A TotalCross NET sample.", "TotalCross NET samples" },
               { "tc/samples/ui", "A TotalCross UI sample.", "TotalCross UI samples" },
               { "tc/samples/ui/image", "A TotalCross Image sample.", "TotalCross IMG samples" },
               { "tc/samples/game", "A TotalCross game.", "TotalCross GAMES" },
               { "tc/samples/xml", "A TotalCross XML sample.", "TotalCross XML" },
               { "tc/samples/io", "A TotalCross IO sample.", "TotalCross IO samples" }
         };

         int catnum = infos.length-1;
         for (; catnum > 0; catnum--)
            if (DeploySettings.mainClassName.startsWith(infos[catnum][0])) 
               break;

         // set user_defined values, ... or set default values
         String category    = totalcross.sys.Settings.appCategory;
         if (category == null)
            category = infos[catnum][2];

         String location    = totalcross.sys.Settings.appLocation;
         if (location == null)
            location = "http://www.totalcross.com/iphone";
         int trimProto      = (location.indexOf("://") >= 0) ? location.indexOf("://")+3 : 0;

         String url         = location.indexOf("/", trimProto) >= 0 ? location.substring(0, location.indexOf("/", trimProto)) : location;
         if (url == null)
            url = "http://www.totalcross.com.br";

         String uriBase     = null;
         if (catnum == 0)
         {
            uriBase = DeploySettings.mainClassName.replace("/", ".");
            uriBase = uriBase.lastIndexOf(".") >= 0 ? uriBase.substring(0, uriBase.lastIndexOf(".")) : "nopackage";
         }
         if (uriBase == null)
            uriBase = "com.totalcross";

         String description = totalcross.sys.Settings.appDescription;
         if (description == null)
            description = infos[catnum][1];
         description += " This package requires the TotalCross VM.";

         String iconfile = "icon60x60.png";

         String company = DeploySettings.companyInfo; // guich@tc100b5_10
         if (company == null)
            company = "SuperWaba Ltda";

         String author = DeploySettings.companyContact; // guich@tc126_64
         if (author == null)
            author = "author@company.com";

         String maintainer = DeploySettings.companyContact; // guich@tc126_64
         if (maintainer == null)
            maintainer = "author@company.com";
         
         createIPhone2xInstaller(author, maintainer, version, DeploySettings.mainClassName, DeploySettings.filePrefix,
                                 DeploySettings.appTitle, DeploySettings.commandLine, category, location, url, uriBase, description, iconfile, DeploySettings.isFullScreenPlatform(Settings.IPHONE));
      }
      System.out.println("... Files written to folder "+targetDir+"/");
   }

   /////////////////////////////////////////////////////////////////////////////////////

   private void createCommon(Vector vFiles, Vector vExtras, String dir, String version, String name, String cmdLine, String uriBase, String iconfile, boolean isFullScreen) throws Exception
   {
      vFiles.addElement(name + ".tcz");
      vFiles.addElement(name);

      String startFile = "start";
      if (cmdLine == null) cmdLine = "";
      Utils.println("...writing "+startFile);
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(dir + startFile));
      dos.writeBytes(
            "#!/bin/sh\n"+
            "exec " + appPath(name) + name + " -p " + appPath(name) + " " + cmdLine + "\n");
      dos.close();
      java.io.File ff = new java.io.File(dir + startFile);
      ff.setExecutable(true, false);
      vFiles.addElement("start");

      // write out Info.plist file
      String infoFile = "Info.plist";
      Utils.println("...writing "+infoFile);
      dos = new DataOutputStream(new FileOutputStream(dir + infoFile));
      dos.writeBytes(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"+
            "<plist version=\"1.0\">\n"+
            "<dict>\n"+
            "  <key>CFBundleDevelopmentRegion</key>\n"+
            "  <string>English</string>\n"+
            "  <key>CFBundleExecutable</key>\n"+
            "  <string>start</string>\n"+
            "  <key>CFBundleIdentifier</key>\n"+
            "  <string>" + uriBase + ".iphone." + name + "</string>\n"+
            "  <key>CFBundleInfoDictionaryVersion</key>\n"+
            "  <string>0.1</string>\n"+
            "  <key>CFBundleName</key>\n"+
            "  <string>" + name + "</string>\n"+
            "  <key>CFBundlePackageType</key>\n"+
            "  <string>APPL</string>\n"+
            "  <key>CFBundleSignature</key>\n"+
            "  <string>????</string>\n"+
            "  <key>CFBundleVersion</key>\n"+
            "  <string>" + version + "</string>\n"+
            "  <key>UIStatusBarHidden</key>\n" +   //flsobral@tc125: added key for full screen property on plist file.
            "  <" + isFullScreen + "/>\n" +
            "  <key>UIDeviceFamily</key>\n" + //flsobral@tc126_39: added support for full screen on iPad.
            "  <array>\n" +
            "   <integer>1</integer>\n" +
            "   <integer>2</integer>\n" +
            "  </array>\n" +
            "</dict>\n"+
            "</plist>\n");
      dos.close();
      vFiles.addElement("Info.plist");

      byte[] data = Bitmaps.IPHONE_DEB.getImage();
      if (data == null) data = Utils.findAndLoadFile(DeploySettings.etcDir+"images/" + iconfile, false);
      if (data != null)
      {
         // write out the icon file
         String icon_png = "icon.png";
         Utils.println("...writing "+icon_png);
         dos=new DataOutputStream(new FileOutputStream(dir + icon_png));
         dos.write(data);
         dos.close();
         vFiles.addElement(icon_png);

         icon_png = "Install.png";
         Utils.println("...writing "+icon_png);
         dos = new DataOutputStream(new FileOutputStream(dir + icon_png));
         dos.write(data);
         dos.close();
         vFiles.addElement(icon_png);
      }

      String default_png = "Default.png";
      data = Utils.findAndLoadFile(DeploySettings.etcDir+"images/" + "splash60x60.png", false);
      if (data != null)
      {
         // write out the splashscreen file
         Utils.println("...writing "+default_png);
         dos = new DataOutputStream(new FileOutputStream(dir + default_png));
         dos.write(data);
         dos.close();
      }
      vFiles.addElement(default_png);

      Hashtable ht = new Hashtable(13);
      Utils.processInstallFile("iphone.pkg", ht); // guich@tc111_22
      String[] extras = Utils.joinGlobalWithLocals(ht, null, true);
      for (int i=0; i < extras.length; i++)
      {
         File.copy(extras[i], Convert.appendPath(dir,Utils.getFileName(extras[i])));
         vExtras.addElement(extras[i]);
      }
   }

   private void createIPhone2xInstaller(String author, String maintainer, String version, String className, final String name, String iconName,
                                    String cmdLine, String category, String location, String url,
                                    String uriBase, String description, String iconfile, boolean isFullScreen) throws Exception
   {
      Utils.println("category    : " + category);
      Utils.println("location    : " + location);
      Utils.println("url         : " + url);
      Utils.println("uri base    : " + uriBase);
      Utils.println("description : " + description);
      
      if (iconName == null) iconName = name;
      
      String baseDir = "install/temp/cydia/" + name + appPath(name);
      
      Utils.println("baseDir = " + baseDir);
      // create the output folder
      File f = new File(baseDir);
      if (!f.exists())
         f.createDir();

      // These are the files to include in the ZIP file
      Vector vFiles = new Vector();
      Vector vExtras = new Vector();
      
      createCommon(vFiles, vExtras, baseDir, version, name, cmdLine, uriBase, iconfile, isFullScreen);

      String controlDir = "install/temp/cydia/" + name + "/DEBIAN/";
      
      Utils.println("controlDir = " + controlDir);
      // create the output folder
      f = new File(controlDir);
      if (!f.exists())
         f.createDir();

      java.io.File[] ctrlFiles = new java.io.File[2]; // control + preinst ctrl files

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
            "");
      dos.close();
      ctrlFiles[0] = new java.io.File(controlDir + outFile);

      /* control file sample
      +Package: com.saurik.myprogram
      +Name: MyProgram
      +Version: 1.0.4-1
      +Architecture: iphoneos-arm
      +Description: an example of using APT Every day people use Cydia, but with the instructions embodied in this package, people can publish for it as well.
      +Homepage: http://www.saurik.com/id/7
      -Depiction: http://www.saurik.com/id/7
      +Maintainer: Your Name <you@example.com>
      +Author: Jay Freeman (saurik) <saurik@saurik.com>
      -Sponsor: Microsoft <http://www.microsoft.com/>
      +Section: Games 
      */

      outFile = "postinst";
      Utils.println("...writing "+outFile);
      dos = new DataOutputStream(new FileOutputStream(controlDir + outFile));
      //flsobral@tc125: manual installation of applications should now automatically change the permission to allow the application to be executed.
      dos.writeBytes(
				"#!/bin/sh\n" +
            "echo \"Enjoy this TotalCross application.\"\n" +				
				"declare -a cydia\n" +
				"cydia=($CYDIA)\n" +
				"if [[ ${CYDIA+@} ]]; then\n" +
	        	"  if [[ $1 == install ]]; then\n" +
            "    eval \"echo 'finish:restart' >&${cydia[0]}\"\n" +
            "  fi\n" +
	        	"  if [[ $1 == upgrade ]]; then\n" +
				"    eval \"echo 'finish:restart' >&${cydia[0]}\"\n" +
				"  fi\n" +
				"fi\n" +
            "chmod -R 4777 /Applications/" + name + ".app\n" +
            "chmod +x /Applications/" + name + ".app/start\n" +
            "chmod +x /Applications/" + name + ".app/" + name + "\n" +
				"exit 0\n" +
            "");
      dos.close();
      java.io.File ff = new java.io.File(controlDir + outFile);
      ff.setExecutable(true, false);
      ctrlFiles[1] = ff;
      
      // copy the launcher
      byte[] buf = Utils.findAndLoadFile(name,false);
      if (buf == null)
         throw new Exception("File " + name + " not found!");
      File fout = new File(baseDir + name, File.CREATE_EMPTY);
      fout.writeBytes(buf, 0, buf.length);
      fout.close();
      ff = new java.io.File(baseDir + name);
      ff.setExecutable(true, false);
      
      // copy the classes
      Utils.copyTCZFile(baseDir);

      Processor debProc = new Processor(
            new Console() //flsobral@tc115: The anonymous class methods can't be obfuscated since they implement an interface
            {
               public void println(String s)
               {
                  Utils.println(s);
               }            
            },
            null); 
      
      Mapper mapper = new Mapper() //flsobral@tc115: The anonymous class methods can't be obfuscated since they implement an interface
      {
         public TarEntry map(TarEntry entry)
         {
            if (entry.getName().endsWith("/start"))
               entry.setMode(0100755);
            else if (entry.getName().endsWith("/" + name))
               entry.setMode(0100755);
            return entry;
         }         
      };
      
      DataProducer[] dataProd = new DataProducer[1];
      dataProd[0] = new DataProducerDirectory(new java.io.File("install/temp/cydia/" + name), 
                        new String[] { "Applications" + java.io.File.separatorChar + "**" }, // includes 
                        new String[0], // excludes
                        new Mapper[] { mapper });
      String debName = targetDir + '/' + name + ".deb";
      java.io.File debOut = new java.io.File(debName);

      PackageDescriptor pdsc = debProc.createDeb(ctrlFiles, dataProd, debOut, "bzip2");
      Utils.println("debian package descriptor:\n" + pdsc.toString());
      
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
         "Installed-Size: "+getInstalledSizeInKB("install/temp/cydia/"+name+"/Applications/"+name+".app")+"\n"+
         "Filename: ./"+name+".deb \n" +
         "Size: " + getFileSize(debName) + "\n" +
         "MD5sum: " + getMD5(debName) + "\n";
      String PackagesFile = "install/temp/Packages";
      htCleanup.put(PackagesFile,"");
      File fpack = new File(PackagesFile, File.CREATE_EMPTY);
      fpack.writeBytes(Package);
      fpack.close();
      
      createBZip2(PackagesFile, targetDir+"/Packages.bz2");
      
      // deferred temp files deletion
      for (int i = vFiles.size()-1; i >= 0; i--)
         htCleanup.put((String)vFiles.items[i],"");
   }
   
   static int getInstalledSizeInKB(String dir) throws totalcross.io.IOException
   {
      int sum = 0;
      String[] files = new File(dir).listFiles();
      for (int i =0; i < files.length; i++)
         sum += getFileSize(dir+"/"+files[i]);
      return (sum+1023) / 1024;
   }

   static int getFileSize(String file) throws totalcross.io.IOException
   {
      File f = new File(file, File.READ_WRITE);
      int size = f.getSize();
      f.close();
      return size;
   }
   
   // These are the files to delete
   static Hashtable htCleanup = new Hashtable(100);

   public static void cleanup()
   {
      Vector vCleanup = htCleanup.getKeys();
      int ttry = 5;
      for (int i = vCleanup.size()-1; i >= 0; i--)
      {
         try
         {
            new File((String)vCleanup.items[i]).delete();
            ttry = 5;
         }
         catch (totalcross.io.IOException e)
         {
            if (ttry-- > 0) // file in use, try again
            {
               i++;
               Vm.sleep(50);
            }
         }
      }
      
      try
      {
         deletePath(DeploySettings.targetDir + "temp/");
      }
      catch (totalcross.io.FileNotFoundException e)
      {
      }
      catch (IllegalArgumentIOException e)
      {
      }
      catch (totalcross.io.IOException e)
      {
         Utils.println("Failed to remove temp folder " + e.getMessage());
      }
   }
   
   // E.G.:
   // in: "P:/TotalCrossSDK/output/Packages"
   // out: "P:/TotalCrossSDK/output/Packages.bz2"
   static void createBZip2(String in, String out) throws Exception
   {
      totalcross.io.File f = new totalcross.io.File(in,totalcross.io.File.READ_WRITE);
      byte[] b = new byte[f.getSize()];
      f.readBytes(b,0,b.length);
      f.close();
      
      FileOutputStream fos = new FileOutputStream(out);
      fos.write(new byte[]{'B','Z'}); // write header
      CBZip2OutputStream z = new CBZip2OutputStream(fos);
      z.write(b);
      z.close();
      fos.close();
   }

   // String in = "P:/TotalCrossSDK/output/ChartTest.deb";
   static String getMD5(String in) throws totalcross.io.IOException, NoSuchAlgorithmException
   {
      totalcross.io.File f = new totalcross.io.File(in,totalcross.io.File.READ_WRITE);
      byte[] b = new byte[f.getSize()];
      f.readBytes(b,0,b.length);
      f.close();
      MD5Digest md5 = new MD5Digest();
      md5.update(b);
      byte[] md5bytes = md5.getDigest();
      return Convert.bytesToHexString(md5bytes).toLowerCase();
   }
   
   private static void deletePath(String path) throws IllegalArgumentIOException, totalcross.io.IOException
   {
      File file = new File(path);
      
      if (file.isDir())
      {
         String[] files = file.listFiles();
         for (int i = files.length - 1; i >= 0  ; i--)
            deletePath(path + files[i]);
      }
      file.delete();
   }

   static void zipFiles(ZipOutputStream out, String baseDir, Vector allFiles)
   {
      // Create a buffer for reading the files
      byte[] buf = new byte[1024];

      // Compress the files
      for (int n = 0; n < allFiles.size(); n++)
      {
         String fe = (String)allFiles.items[n];
         Utils.println("process: " + fe);
         String path = Utils.findPath(fe,false);
         if (path == null)
            path = Utils.findPath("fonts/" + fe, false);
         if (path == null)
         {
            System.err.println("Cannot find the file: " + fe);
            continue;
         }

         FileInputStream in;
         try
         {
            // Add ZIP entry to output stream.
            int sp = path.lastIndexOf('/');
            String fname = (sp >= 0) ? path.substring(sp+1) : path;

            String zipEN;
            if (fname.indexOf("Install") >= 0)
               zipEN = fname;
            else
               zipEN = baseDir + fname;
            Utils.println("......zip entry: " + zipEN + " <= " + path);

            in = new FileInputStream(path);

            ZipEntry zipEntry = new ZipEntry(zipEN);
            out.putNextEntry(zipEntry);

            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0)
               out.write(buf, 0, len);

            // Complete the entry
            out.closeEntry();
            in.close();
         }
         catch (FileNotFoundException e)
         {
            e.printStackTrace();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }

   static String appPath(String appName)
   {
      return "/Applications/" + appName + ".app/";
   }

   static String shellScript(String command)
   {
      return
         "      <array>\n"+
         "        <string>Exec</string>\n"+
         "        <string>" + command + "</string>\n"+
         "      </array>\n";
   }

   static String copyPathScript(String name)
   {
      return
         "      <array>\n"+
         "        <string>CopyPath</string>\n"+
         "        <string>" + name + ".app</string>\n"+
         "        <string>/Applications/" + name + ".app</string>\n"+
         "      </array>\n";
   }

   static String removePathScript(String name)
   {
      return
         "      <array>\n"+
         "        <string>RemovePath</string>\n"+
         "        <string>/Applications/" + name + ".app</string>\n"+
         "      </array>\n";
   }

   static String noticeScript(String message)
   {
      return
         "      <array>\n"+
         "        <string>Notice</string>\n"+
         "        <string>" + message + "</string>\n"+
         "      </array>\n";
   }
}
