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

import totalcross.io.DataStream;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

/** An exe for Windows CE */
public class Deployer4WinCE
{
   String []stubPaths = // guich@321_1 and guich@330_3: changed \\ to /
   {
      "POCKETPC/ARM",
//      "HPC211/ARM",
//      "POCKETPC/MIPS",
//      "POCKETPC/SH3",
//      "HPC2000/ARM",
   };
   static int []versionOffsets =   // 3.11 = 0x03 0x00 0x0B 0x00
   {
      0x114, // POCKETPC-ARM
//      0x11c, // HPC211-ARM      - guich@400_45
//      0x114, // POCKETPC-MIPS
//      0x114, // POCKETPC-SH3
//      0x10c, // HPC2000-ARM
   };

   // Offsets into the byte structure which defines the stub applcation
   static int launchOffset=-1;            // dynamically set
   static int bitmap16x16x8_Offset=-1;      //      "
   static int bitmap32x32x8_Offset=-1;      //      "
   static int bitmap48x48x8_Offset=-1;      //      "
   public static boolean keepExe;
   public static boolean keepExeAndDontCreateCabFiles; // guich@tc126_29

   private String cabName; // used in deleteAll
   private String targetDir;
   
   private int pathsCount; // guich@tc125_17

   // guich@200 - to make modifications easier, we now read the prc bytes from disk and modify it.
   byte bytes[];

   String sEx = "The CEStub.exe file has been modified. some of the placeholder strings could not be found. Reinstall TotalCross. - "; // string Exception - sEx - good name, isnt? ;-)

   public Deployer4WinCE(boolean allTargets) throws Exception
   {
      pathsCount = allTargets ? stubPaths.length : 1;
      // suportar tb arquivo .inf alem de .pkg
      String ceArguments = DeploySettings.commandLine.trim(); // the name of the tcz will be the same of the .exe
      if (ceArguments.length() > DeploySettings.defaultArgument.length/2)
         throw new IllegalArgumentException("ERROR - launch string too long: "+ceArguments);
      targetDir = DeploySettings.targetDir+"wince/";
      File f = new File(targetDir);
      if (!f.exists())
         f.createDir();
      if (DeploySettings.mainClassName != null)
         for (int i =0; i < pathsCount; i++)
            try
            {
               String fileName = DeploySettings.etcDir+"launchers/wince/"+stubPaths[i]+"/Launcher.exe";
               bytes = Utils.findAndLoadFile(fileName,false);
               if (bytes == null) throw new DeployerException("Could not find "+fileName+". Please add tc.jar to the classpath!");
               findOffsets(DeploySettings.bitmaps);
               storeInformation(ceArguments, versionOffsets[i]);
               DeploySettings.bitmaps.saveWinCEIcons(bytes, bitmap16x16x8_Offset, bitmap32x32x8_Offset, bitmap48x48x8_Offset);
               saveStub(targetDir, stubPaths[i], DeploySettings.filePrefix, false);
            } catch (Exception e) {Utils.println("Exception: "+i+" - "+e.getMessage());}
      else // if no exe files, just create the directories
         for (int i =0; i < pathsCount; i++)
            try {new File(targetDir+stubPaths[i]).createDir();} catch (totalcross.io.IOException e) {}

      if (keepExe) // guich@tc126_29: if we're keeping the exe, copy the tcz to the target folder
         Utils.copyTCZFile(targetDir);
      if (!keepExeAndDontCreateCabFiles) // guich@tc126_29: don't create the cab file
         createInstallFiles();
      if (!keepExe) deleteAll();
   }

   /////////////////////////////////////////////////////////////////////////////////////
   private void storeInformation(String args, int versionOffset) throws Exception
   {
      // launch string
      Utils.writeString(bytes, args, launchOffset, true);
      // version info
      bytes[versionOffset] = (byte)DeploySettings.getAppVersionHi();
      bytes[versionOffset+2] = (byte)DeploySettings.getAppVersionLo();
   }
   /////////////////////////////////////////////////////////////////////////////////////
   private void saveStub(String userPath, String exePath, String exeName, boolean show) throws Exception
   {
      String path = userPath+exePath;
      String fileName = DeploySettings.filePrefix+".exe";
      // write the file
      String fullPath = path+"/"+fileName;
      if (show) Utils.println("...writing "+fullPath);

      try {new File(path).createDir();} catch (totalcross.io.IOException e) {}

      File fos = new File(fullPath, File.CREATE_EMPTY);
      fos.writeBytes(bytes,0,bytes.length);
      fos.close();
   }
   /////////////////////////////////////////////////////////////////////////////////////
   byte[] icon48x48x8_header = {(byte)0x28, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x30, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x60, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x08, (byte)0x00};
   byte[] icon32x32x8_header = {(byte)0x28, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x40, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x08, (byte)0x00};
   byte[] icon16x16x8_header = {(byte)0x28, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x08, (byte)0x00};
   // guich@200 - dynamically find all entries
   private void findOffsets(Bitmaps bitmaps) throws Exception
   {
      launchOffset = Utils.indexOf(bytes,DeploySettings.defaultArgument,false);
      if (launchOffset == -1) throw new DeployerException(sEx+"2a");

      bitmap16x16x8_Offset = Utils.indexOf(bytes, icon16x16x8_header, false);
      bitmap32x32x8_Offset = Utils.indexOf(bytes, icon32x32x8_header, false);
      bitmap48x48x8_Offset = Utils.indexOf(bytes, icon48x48x8_header, false);
      if (bitmap16x16x8_Offset == -1) System.out.println("Could not find offset for 16x16x8");
      if (bitmap32x32x8_Offset == -1) System.out.println("Could not find offset for 32x32x8");
      if (bitmap48x48x8_Offset == -1) System.out.println("Could not find offset for 48x48x8");
   }
   /////////////////////////////////////////////////////////////////////////////////////
   private String getCabName(int i)
   {
      return targetDir+cabName+"."+stubPaths[i].replace('/','_')+".cab";
   }
   // guich@340_43: from here to bottom.
   private boolean wasAllCabsCreated() throws totalcross.io.IOException
   {
      for (int i=0; i < pathsCount; i++)
         if (!new File(getCabName(i)).exists())
            return false;
      return true;
   }
   /////////////////////////////////////////////////////////////////////////////////////
   private void deleteCabs() throws totalcross.io.IOException
   {
      for (int i=0; i < pathsCount; i++)
         try
         {
            new File(getCabName(i)).delete(); // guich@341_14 replaced '/' by java.io.File.separatorChar
         }
         catch (FileNotFoundException e)
         {
         }
   }
   /////////////////////////////////////////////////////////////////////////////////////
   public void deleteAll() throws totalcross.io.IOException
   {
      File f;
      for (int i=0; i < pathsCount; i++)
      {
         File.deleteDir(targetDir+stubPaths[i]);
         f = new File(targetDir+stubPaths[i]);
         if (f.exists())
            f.delete();
      }
//      f = new File(targetDir+"HPC211");
//      if (f.exists())
//         f.delete();
      f = new File(targetDir+"POCKETPC");
      if (f.exists())
         f.delete();
//      f = new File(targetDir+"HPC2000");
//      if (f.exists())
//         f.delete();
   }
   /////////////////////////////////////////////////////////////////////////////////////
   public void deleteTemp(boolean includingInf)
   {
      for (int i=0; i < pathsCount; i++)
         try {new File(targetDir+cabName+"."+stubPaths[i].replace('/','_')+".dat").delete();} catch (totalcross.io.IOException e) {}
      if (includingInf) try {new File(targetDir+cabName+".inf").delete();} catch (totalcross.io.IOException e) {}
   }
   /////////////////////////////////////////////////////////////////////////////////////
   private String toString(Vector v, String post, boolean copyFile) throws Exception
   {
      String s = "\n";
      int n = v.size();
      for (int i =0; i < n; i++)
      {
         String fullName = (String)v.items[i];
         String onlyName = Utils.getFileName(fullName);
         String targetName = targetDir+onlyName;
         // copy the file to the current folder if it does not exists.
         // (cabwiz don't allow pathnames on the input file list)
         if (copyFile && !new File(targetName).exists())
         {
            byte[] bytes = Utils.findAndLoadFile(fullName,false);
            if (bytes == null)
               throw new totalcross.io.IOException("File not found: "+fullName);
            Utils.writeBytes(bytes,targetName);
            v.items[i] = "*"+targetName; // mark the file to be deleted
         }
         // concat the string.
         s += onlyName+post;
      }
      return s;
   }
   /////////////////////////////////////////////////////////////////////////////////////
   public void createInstallFiles() throws Exception
   {
      boolean infFileGiven = new File("wince.inf").exists();

      String infFileName;
      Vector vLocals=null,vGlobals=null;
      boolean hasExe = DeploySettings.mainClassName != null;

      if (infFileGiven)
      {
         infFileName = "wince.inf";
         cabName = infFileName.substring(0,Math.min(8,infFileName.length()-4)); // strip the .inf - guich@421_75
         deleteCabs();
         File oFile = new File(targetDir+infFileName,File.CREATE_EMPTY);
         File iFile = new File("wince.inf",File.READ_ONLY);
         iFile.copyTo(oFile);
         oFile.close();
         iFile.close();
      }
      else
      {
         // process the pkg file
         Hashtable ht = new Hashtable(13);
         Utils.processInstallFile("wince.pkg", ht);

         vLocals  = (Vector)ht.get("[L]"); if (vLocals == null) vLocals  = new Vector();
         vGlobals = (Vector)ht.get("[G]"); if (vGlobals== null) vGlobals = new Vector();
         vLocals.addElement(DeploySettings.tczFileName);
         Utils.preprocessPKG(vLocals,false);
         Utils.preprocessPKG(vGlobals,false);
         String tcFolder = null, lbFolder = null;
         if (DeploySettings.packageVM) // include the vm?
         {
            if (!hasExe)
               System.out.println("Warning: /p ignored since package has no binary files");
            else
            {
               // tc is always included
               // include non-binary files
               vLocals.addElement(DeploySettings.folderTotalCross3DistVM+"TCBase.tcz");
               vLocals.addElement(DeploySettings.folderTotalCross3DistVM+"TCUI.tcz");
               vLocals.addElement(DeploySettings.folderTotalCross3DistVM+DeploySettings.fontTCZ);
               vLocals.addElement(DeploySettings.folderTotalCross3DistVM+"LitebaseLib.tcz");
               lbFolder = DeploySettings.folderTotalCross3DistVM+"wince/";
               // copy binary files
               for (int i =0; i < pathsCount; i++)
               {
                  String name = stubPaths[i]+"/tcvm.dll";
                  try {new File(targetDir+stubPaths[i]).createDir();} catch (Exception e) {}
                  File.copy(tcFolder+name,targetDir+name);
                  if (lbFolder != null)
                  {
                     name = stubPaths[i]+"/Litebase.dll";
                     File.copy(lbFolder+name,targetDir+name);
                  }
               }
            }
         }         

         cabName = DeploySettings.filePrefix.trim().replace(' ','_');
         if (cabName.length() > 8) cabName = cabName.substring(0,8); // guich@421_75
         deleteCabs();

         infFileName = cabName+".inf";
         File infFile = new File(targetDir+infFileName,File.CREATE_EMPTY);
         String installDir = "\\TotalCross\\"+DeploySettings.filePrefix; // guich@568_7: removed extra \"
         String inf =
            "[Version]\n" +

            "Signature       = \"$Windows NT$\"\n" +
            "Provider           = \""+(DeploySettings.companyInfo != null ? Utils.stripNonLetters(DeploySettings.companyInfo) : "TotalCross")+"\"\n" +
            "CESignature     = \"$Windows CE$\"\n" +

            //-----------------------------------------------

            "[CEStrings]\n" +

            "AppName  = \""+DeploySettings.filePrefix+"\"\n" +
            "InstallDir   = "+installDir+"\n" +

            //-----------------------------------------------

            "[Strings]\n" +

            "TCDir    = \"\\TotalCross\"\n" +

            //-----------------------------------------------

//            (pathsCount == 1 ? "" : "[CEDevice.HPC2000_ARM]\n" +
            "ProcessorType   = 2577\n" +
            "VersionMin = 3.0\n" +
            "VersionMax = 4.0\n" +
            "UnsupportedPlatforms = \"Palm PC\"\n" +

//            "[CEDevice.HPC211_ARM]\n" +
//            "ProcessorType   = 2577\n" +
//            "VersionMin = 2.11\n" +
//            "VersionMax = 2.11\n" +
//            "UnsupportedPlatforms = \"Palm PC\"\n" +

            "[CEDevice.PocketPC_ARM]\n" +
            ";ProcessorType   = 2577\n" +
            "VersionMin = 3.0\n" +
            "VersionMax = 6.99\n" +
            "UnsupportedPlatforms = \"HPC\",\"Jupiter\"\n" +
            "BuildMax=0xE0000000\n"+

//            "[CEDevice.PocketPC_MIPS]\n" +
//            "ProcessorType   = 4000\n" +
//            "VersionMin = 3.0\n" +
//            "VersionMax = 6.99\n" +
//            "UnsupportedPlatforms = \"HPC\",\"Jupiter\"\n" +
//            "BuildMax=0xE0000000\n"+
//
//            "[CEDevice.PocketPC_SH3]\n" +
//            "ProcessorType   = 10003\n" +
//            "VersionMin = 3.0\n" +
//            "VersionMax = 5.99\n" +
//            "UnsupportedPlatforms = \"HPC\",\"Jupiter\"\n" +
//            "BuildMax=0xE0000000\n")+

            "[CEDevice.WMOBILE_ARM]\n" +
            ";ProcessorType   = 2577\n" +
            ";VersionMin = 3.0\n" +
            ";VersionMax = 6.99\n" +
            "UnsupportedPlatforms = \"HPC\",\"Jupiter\"\n" +
            "BuildMax=0xE0000000\n"+

            //-----------------------------------------------

            "[DefaultInstall]\n" +
            "CopyFiles   = "+(hasExe?"Binaries,":"")+"GlobalFiles,LocalFiles\n" +
            "CEShortcuts = Startmenu\n" +

            //-----------------------------------------------
            // directories where the source files are located

            "[SourceDisksNames]\n" +
            // all local files, ie, local libraries, database pdbs, etc
            "2 =, \"LocalFiles\",,\n" +
            // any global files, ie, global libraries, fonts, etc
            // you must copy them to your app directory so they can be found
            "3 =, \"GlobalFiles\",,\n" +
//            (hasExe ? ((pathsCount == 1 ? "" : ( 
//            "[SourceDisksNames.HPC2000_ARM]\n" +
//            "1 =, \"Binaries\",,HPC2000\\ARM\n" +
//            "[SourceDisksNames.HPC211_ARM]\n" +
//            "1 =, \"Binaries\",,HPC211\\ARM\n" +
//            "[SourceDisksNames.PocketPC_ARM]\n" +
//            "1 =, \"Binaries\",,POCKETPC\\ARM\n" +
//            "[SourceDisksNames.PocketPC_MIPS]\n" +
//            "1 =, \"Binaries\",,POCKETPC\\MIPS\n" +
//            "[SourceDisksNames.PocketPC_SH3]\n" +
//            "1 =, \"Binaries\",,POCKETPC\\SH3\n")) +
//            "[SourceDisksNames.WMOBILE_ARM]\n" +
//            "1 =, \"Binaries\",,POCKETPC\\ARM\n") : "") +
            (hasExe ? ((pathsCount == 1 ? "" : ( 
               "[SourceDisksNames.PocketPC_ARM]\n" +
               "1 =, \"Binaries\",,POCKETPC\\ARM\n")) +
               "[SourceDisksNames.WMOBILE_ARM]\n" +
               "1 =, \"Binaries\",,POCKETPC\\ARM\n") : "") +

            //-----------------------------------------------

            "[SourceDisksFiles]\n" +

            (hasExe ? (DeploySettings.filePrefix+".exe    = 1\n") : "") +
            (tcFolder != null ? ("tcvm.dll      = 1\n") : "") +
            (lbFolder != null ? ("Litebase.dll  = 1\n") : "") +
            toString(vLocals, " = 2\n",false) +
            toString(vGlobals, " = 3\n",false) +

            //-----------------------------------------------
            // Ouput directories for files & shortcuts

            // global libraries go to the default TC directory
            // local libraries (and other files) goes to the default instalation directory
            "[DestinationDirs]\n" +
            (hasExe ? "Binaries = 0,%InstallDir%\n" : "") +
            "GlobalFiles = 0,%TCDir%\n" +
            "LocalFiles = 0,%InstallDir%\n" +
            "Startmenu = 0,%CE11%\n"+

            (hasExe ? ("[Binaries]\n" + DeploySettings.filePrefix+".exe\n") : "") +
            (tcFolder != null ? ("tcvm.dll\n") : "") +
            (lbFolder != null ? ("Litebase.dll\n") : "") +

            "[LocalFiles]\n" +
            toString(vLocals, "\n",true) +

            "[GlobalFiles]\n" +
            toString(vGlobals, "\n",true) +

            (hasExe ? ("[Startmenu]\n" +
            "\""+DeploySettings.appTitle+"\", 0, \""+DeploySettings.filePrefix+".exe\"\n") : "")
            ;
         
         new DataStream(infFile).writeBytes(inf.getBytes());
         infFile.close();
      }

      String path2Cabwiz, out;
      if (pathsCount != 1)
      {
	      // ok, the .inf file is created, now we call the cabwiz program
	      path2Cabwiz = Utils.findPath(DeploySettings.etcDir+"tools/makecab/Cabwiz.exe",false);
	      if (path2Cabwiz == null)
	         throw new DeployerException("Could not find Cabwiz.exe in directories relative to the classpath. Be sure to add TotalCrossSDK/dist/tc.jar to the classpath");
	      // since exec don't allow us to change the current path, we create a batch file that will cd to the current folder
	      String[] callCabWiz = {path2Cabwiz.replace('/',DeploySettings.SLASH),infFileName,"/cpu","HPC2000_ARM","HPC211_ARM","PocketPC_ARM","PocketPC_MIPS","PocketPC_SH3"};
	      out = Utils.exec(callCabWiz, targetDir.replace('/',DeploySettings.SLASH));
	      // now we need to wait the process finish. For some reason, the Process.waitFor does not work.
	      for (int i =0; i < 100; i++)
	      {
	         if (wasAllCabsCreated())
	            break;
	         Vm.sleep(100);
	      }
	      if (!wasAllCabsCreated())
	         System.err.println("\n\nFailed calling execCabWiz. Files for Windows CE prior to WM5 not generated!\n\nExecution output:\n\n"+out);
      }

      // create the windows mobile 5/6 cab
      path2Cabwiz = Utils.findPath(DeploySettings.etcDir+"tools/makecab/Cabwizsp.exe",false);
      if (path2Cabwiz == null)
         throw new DeployerException("Could not find Cabwizsp.exe in directories relative to the classpath. Be sure to add TotalCrossSDK/lib to the classpath");
      // since exec don't allow us to change the current path, we create a batch file that will cd to the current folder
      try {new File(targetDir+cabName+".WMobile_ARM.CAB").delete();} catch (Exception e) {}
      String[] callCabWiz = {path2Cabwiz.replace('/',DeploySettings.SLASH),infFileName,"/cpu","WMobile_ARM"};
      out = Utils.exec(callCabWiz, targetDir.replace('/',DeploySettings.SLASH));

      // now we need to wait the process finish. For some reason, the Process.waitFor does not work.
      for (int i =0; i < 100; i++)
      {
         if (new File(targetDir+cabName+".WMobile_ARM.CAB").exists())
            break;
         Vm.sleep(100);
      }
      if (!new File(targetDir+cabName+".WMobile_ARM.CAB").exists())
      {
         System.err.println("\n\nFailed calling execCabWiz for WM5!\n\nExecution output:\n\n"+out);
      }

      // delete the temp files
      deleteTemp(!infFileGiven);
      if (vLocals != null) deleteCopiedFiles(vLocals);
      if (vGlobals != null) deleteCopiedFiles(vGlobals);
      
     File batFile, iniFile;
     String bat, ini;
      
      if (pathsCount != 1)
      {
	     // the cab files were created, now we need to create the bat file to start the instalation
	     batFile = new File(targetDir+DeploySettings.filePrefix+"_Install.bat",File.CREATE_EMPTY);
	     bat =
	        "@echo off\r\n" +
	        "\r\n" +
	        "echo This script installs this TotalCross Application on CE device.\r\n" +
	        "rem test on the ProgramFiles variable\r\n" +
	        "if not \"%ProgramFiles%\"==\"\" goto ok\r\n" +
	        "set ProgramFiles=c:\\Progra~1\r\n" +
	        ":ok\r\n" +
	        "\"%ProgramFiles%\\Microsoft ActiveSync\\CeAppMgr.exe\" \".\\"+DeploySettings.filePrefix+"_Install.ini\"\r\n" + // guich@tc110_36: surround with ""
	        "if \"%errorlevel%\"==\"1\" goto end\r\n" +
	        "\r\n" +
	        "\"%windir%\\WindowsMobile\\CeAppMgr.exe\" \".\\"+DeploySettings.filePrefix+"_Install.ini\"\r\n" + // guich@tc110_36: surround with ""
	        "if \"%errorlevel%\"==\"1\" goto end\r\n" +
	        "\r\n" +
	        "CeAppMgr.exe \".\\"+DeploySettings.filePrefix+"_Install.ini\"\r\n" + // guich@tc110_36: surround with ""
	        "if \"%errorlevel%\"==\"0\" goto end\r\n" +
	        "\r\n" +
	        "echo:\r\n" +
	        "echo ERROR: Cannot locate CeAppMgr.exe. Please put the\r\n" +
	        "echo \"Microsoft ActiveSync\" directory on your path variable.\r\n" +
	        "echo It is usually located under \"Program Files\".\r\n" +
	        "echo:\r\n" +
	        "pause\r\n" +
	        ":end\r\n";
	     new DataStream(batFile).writeBytes(bat.getBytes());
	     batFile.close();
	     
	     // and now create the .ini file for installation
	     iniFile = new File(targetDir+DeploySettings.filePrefix+"_Install.ini", File.CREATE_EMPTY);
	     ini =
	        "[CEAppManager]\r\n" +
	        "Version        = 1.0\r\n" +
	        "Component      = App\r\n" +
	        "[App]\r\n" +
	        "Description    = \""+DeploySettings.filePrefix+"\"\r\n" + // guich@tc110_36: surround with ""
	        "CabFiles       = "+cabName+".HPC2000_ARM.CAB,"+cabName+".HPC211_ARM.CAB,"+cabName+".PocketPC_ARM.CAB,"+cabName+".PocketPC_MIPS.CAB,"+cabName+".PocketPC_SH3.CAB\r\n";
	     new DataStream(iniFile).writeBytes(ini.getBytes());
	     iniFile.close();
      }
      // guich@tc114_85: dedicated files for WINDOWS MOBILE devices
      // the cab files were created, now we need to create the bat file to start the instalation
      batFile = new File(targetDir+DeploySettings.filePrefix+"_Install_WMOBILE.bat",File.CREATE_EMPTY);
      bat =
         "@echo off\r\n" +
         "\r\n" +
         "echo This script installs this TotalCross Application on a WINDOWS MOBILE device.\r\n" +
         "rem test on the ProgramFiles variable\r\n" +
         "if not \"%ProgramFiles%\"==\"\" goto ok\r\n" +
         "set ProgramFiles=c:\\Progra~1\r\n" +
         ":ok\r\n" +
         "\"%ProgramFiles%\\Microsoft ActiveSync\\CeAppMgr.exe\" \".\\"+DeploySettings.filePrefix+"_Install_WMOBILE.ini\"\r\n" + // guich@tc110_36: surround with ""
         "if \"%errorlevel%\"==\"1\" goto end\r\n" +
         "\r\n" +
         "\"%windir%\\WindowsMobile\\CeAppMgr.exe\" \".\\"+DeploySettings.filePrefix+"_Install_WMOBILE.ini\"\r\n" + // guich@tc110_36: surround with ""
         "if \"%errorlevel%\"==\"1\" goto end\r\n" +
         "\r\n" +
         "CeAppMgr.exe \".\\"+DeploySettings.filePrefix+"_Install_WMOBILE.ini\"\r\n" + // guich@tc110_36: surround with ""
         "if \"%errorlevel%\"==\"0\" goto end\r\n" +
         "\r\n" +
         "echo:\r\n" +
         "echo ERROR: Cannot locate CeAppMgr.exe. Please put the\r\n" +
         "echo \"Microsoft ActiveSync\" directory on your path variable.\r\n" +
         "echo It is usually located under \"Program Files\".\r\n" +
         "echo:\r\n" +
         "pause\r\n" +
         ":end\r\n";
      new DataStream(batFile).writeBytes(bat.getBytes());
      batFile.close();
      // and now create the .ini file for installation
      iniFile = new File(targetDir+DeploySettings.filePrefix+"_Install_WMOBILE.ini", File.CREATE_EMPTY);
      ini =
         "[CEAppManager]\r\n" +
         "Version        = 1.0\r\n" +
         "Component      = App\r\n" +
         "[App]\r\n" +
         "Description    = \""+DeploySettings.filePrefix+"\"\r\n" + // guich@tc110_36: surround with ""
         "CabFiles       = "+cabName+".WMobile_ARM.CAB\r\n";
      new DataStream(iniFile).writeBytes(ini.getBytes());
      iniFile.close();
      
      // delete the inf file
      if (!keepExe) try {new File(targetDir+Utils.getFileName(DeploySettings.tczFileName)).delete();} catch (FileNotFoundException e) {}
      // everything done!
      System.out.println("... Files written to folder "+targetDir);
   }

   private void deleteCopiedFiles(Vector v) throws totalcross.io.IOException
   {
      for (int i = v.size()-1; i >= 0; i--)
      {
         String s = (String)v.items[i];
         if (s.charAt(0) == '*')
            new File(s.substring(1)).delete();
      }
   }
}
