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



package tc;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import tc.tools.converter.*;
import tc.tools.deployer.*;

import totalcross.sys.*;
import totalcross.util.*;

public class Deploy
{
   /** Set this to a classpath that will have precedence over the classpath passed by Java. 
    * This allows you to call tc.Deploy directly from an application. 
    */
   public static String bootClassPath;
   
   public static void main(String[] args)
   {
      new Deploy(args);
   }

   public static final int BUILD_PALM    = 1;
   public static final int BUILD_WINCE   = 2;
   public static final int BUILD_WIN32   = 4;
   public static final int BUILD_LINUX   = 8;
   public static final int BUILD_BB      = 32;
   public static final int BUILD_APPLET  = 64;
   public static final int BUILD_IPHONE  = 128;
   public static final int BUILD_ANDROID = 256;
   public static final int BUILD_WINMO   = 512; // guich@tc125_17
   public static final int BUILD_ALL     = 0xFFFF;
   
   private boolean waitIfError; // guich@tc111_24
   private static int platform;
   
   public static boolean isOnlyBB()
   {
      return platform == BUILD_BB;
   }

   public Deploy(String[] args)
   {
      try
      {
         if (args.length < 1 || args[0].indexOf('?') >= 0 || args[0].indexOf("help") >= 0)
         {
            usage();
            return;
         }
         DeploySettings.init();

         checkClasspath();

         // tc.tools.Deploy <arquivo zip/jar> palm wince win32 linux bb
         String fileName = args[0];
         int options = platform = parseOptions(args);

         // convert the jar file into a tcz file
         J2TC.process(fileName, options);

         if (DeploySettings.testClass)
            System.out.println("Test mode on. No file created.");
         else
         if (options == 0)
            System.out.println("TCZ file created, but no target platforms specified. Type \"java tc.Deploy\" for help.");
         else
         {
            if (DeploySettings.mainClassName != null) DeploySettings.bitmaps = new Bitmaps(DeploySettings.filePrefix);

            if ((options & BUILD_ANDROID) != 0) new Deployer4Android(); // must be first
            if ((options & BUILD_WINCE)   != 0) new Deployer4WinCE(true);
            else
            if ((options & BUILD_WINMO)   != 0) new Deployer4WinCE(false); // there's no need to build for winmo if built for wince
            if ((options & BUILD_WIN32)   != 0) new Deployer4Win32();
            if ((options & BUILD_LINUX)   != 0) new Deployer4Linux();
            if ((options & BUILD_APPLET)  != 0) new Deployer4Applet();
            if ((options & BUILD_IPHONE)  != 0)
            {
               //flsobral@tc115: dynamically load libraries required to build for iPhone.
               JarClassPathLoader.addFile(DeploySettings.etcDir + "tools/ipa/bcprov-jdk15on-147.jar");
               JarClassPathLoader.addFile(DeploySettings.etcDir + "tools/ipa/bcpkix-jdk15on-147.jar");
               JarClassPathLoader.addFile(DeploySettings.etcDir + "tools/ipa/commons-io-2.2.jar");
               JarClassPathLoader.addFile(DeploySettings.etcDir + "tools/ipa/commons-compress-1.4.jar");
               JarClassPathLoader.addFile(DeploySettings.etcDir + "tools/ipa/dd-plist.jar");
               JarClassPathLoader.addFile(DeploySettings.etcDir + "tools/ipa/truezip-driver-file-7.5.1.jar");
               JarClassPathLoader.addFile(DeploySettings.etcDir + "tools/ipa/truezip-driver-zip-7.5.1.jar");
               JarClassPathLoader.addFile(DeploySettings.etcDir + "tools/ipa/truezip-file-7.5.1.jar");
               JarClassPathLoader.addFile(DeploySettings.etcDir + "tools/ipa/truezip-kernel-7.5.1.jar");
               JarClassPathLoader.addFile(DeploySettings.etcDir + "tools/ipa/truezip-swing-7.5.1.jar");

               //if (DeploySettings.buildIPA)
               if (DeploySettings.certStorePath == null)
                  System.out.println("Warning: /m option not found, ignoring iOS deployment.");
               else
               {
                  if (DeploySettings.appleCertStore == null)
                     throw new DeployerException("Failed to build the ipa for iOS distribution: Couldn't find the certificate store at: " + DeploySettings.certStorePath);
                  else
                  if (DeploySettings.mobileProvision == null)
                     throw new DeployerException("Failed to build the ipa for iOS distribution: Couldn't find the mobile provision at: " + DeploySettings.certStorePath);
                  new Deployer4IPhoneIPA();
               }
            }
            if (!DeploySettings.inputFileWasTCZ) try {new totalcross.io.File(DeploySettings.tczFileName).delete();} catch (Exception e) {} // delete the file
            
            if (!DeploySettings.testClass && (options & BUILD_APPLET)  != 0 && DeploySettings.isJarOrZip)
               System.out.println("\nAttention: Deployer for Applet was not able to process the dependencies to create a single jar file because you passed a jar or zip as input file. In this situation, the applet will require the tc.jar file to run.");
            if (!DeploySettings.testClass && DeploySettings.showBBPKGName != null)
               System.out.println("\nThe BlackBerry's "+DeploySettings.showBBPKGName+".cod package was created. The files will be copied to the folder \""+DeploySettings.showBBPKGRoot+"\". Remember that the files are not uninstalled when your application is removed, and, if they are Litebase tables, be sure to DONT CHANGE the default database path, or the installed files will not be found.");
            if (DeploySettings.mainClassName == null && DeploySettings.isJarOrZip)
            {
               String fn = Utils.getFileName(fileName);
               int dot = fn.lastIndexOf('.');
               String name = fn.substring(0,dot);
               String ext = fn.substring(dot+1);
               System.out.println("\nThe file '"+fileName+"' does not contain a class named '"+name+"' that extends totalcross.ui.MainWindow, so this file is considered as LIBRARY-ONLY and no executable were generated. However, if this jar is indeed an application, make sure that the JAR has the same name of your MainWindow class.");
               if (!DeploySettings.filePrefix.equals("TCBase") && !DeploySettings.filePrefix.toLowerCase().endsWith("lib"))
                  System.out.println("If this file is really a library, you must name it "+DeploySettings.filePrefix+"Lib."+ext+", or it will NOT be loaded in the device.");
            }
         }
      }
      catch (OutOfMemoryError oome)
      {
         showException(oome,"\nConsider increasing the memory available for tc.Deploy passing the parameter \n\n-Xmx512m");
      }
      catch (Exception e)
      {
         showException(e, null);
      }
   }
   
   /**
    * Utility class that dynamically loads a jar file into the Deploy classpath.<br>
    * It uses reflection to grant access to the loaded jar, a little hackish but that's the easiest way of doing it.<br>
    * Another option would be to iterate through the jar entries and load classes one by one, which is not much better
    * than this IMHO. So I decided to go with the easiest-to-implement solution.
    * 
    * @author Fabio Sobral
    * @since TotalCross 1.15
    */
   //flsobral@tc115: just a mark for quick search, see class documentation above.
   static class JarClassPathLoader
   {
      private static final Class[] parameters = new Class[] { URL.class };

      public static void addFile(String s) throws java.io.IOException
      {
         java.io.File f = new java.io.File(s);
         addFile(f);
      }

      public static void addFile(java.io.File f) throws java.io.IOException
      {
         addURL(f.toURI().toURL());
      }

      public static void addURL(URL u) throws java.io.IOException
      {
         URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
         Class sysclass = URLClassLoader.class;

         try
         {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { u });
         }
         catch (Throwable t)
         {
            throw new java.io.IOException("Error, could not add URL to system classloader", t);
         }
      }
   }
   
   private void showException(Throwable e, String extraMsg)
   {
      System.out.println();
      System.out.println();
      System.out.println("################################# FATAL ERROR ##################################");
      if (J2TC.currentClass  != null) System.out.println("Class: "+J2TC.currentClass);
      if (J2TC.currentMethod != null) System.out.println("Method: "+J2TC.currentMethod);
      System.out.println("Exception type: "+e.getClass().getName());
      if (e.getMessage() != null) System.out.println("Exception message: "+e.getMessage());
      System.out.println("Exception stack trace:");
      
      java.io.StringWriter sw = new java.io.StringWriter(); // guich@tc100b4_6: with StringWriter it always works
      e.printStackTrace(new java.io.PrintWriter(sw));
      String stacktrace = sw.toString();
      stacktrace = stacktrace.substring(stacktrace.indexOf('\n')+1);
      System.out.println("   "+stacktrace.trim().replace("\t","   "));
      
      if (extraMsg != null) System.out.println(extraMsg);
      System.out.println("################################################################################");
      if (waitIfError)
         try {System.out.print("Press enter to quit "); System.in.read();} catch (Exception ee) {} // guich@tc111_24
   }

   private void checkClasspath() throws Exception
   {
      try // guich@500_8: verify if the tc.jar is in the classpath (IntHashtable is needed by Bitmap)
      {
         Class.forName("totalcross.util.IntHashtable");
      }
      catch (ClassNotFoundException cd)
      {
         throw new DeployerException("You must also add /TotalCrossSDK/lib/TotalCross.jar to the classpath!");
      }
   }

   private int parseOptions(String[] args) throws Exception
   {
      int options = 0;
      IntHashtable iht = new IntHashtable(17);
      iht.put("palm"   .hashCode(), 0);
      iht.put("palmos" .hashCode(), 0);
      iht.put("blackberry".hashCode(), 0);
      iht.put("bb"     .hashCode(), 0);
      iht.put("ce"     .hashCode(), BUILD_WINCE);
      iht.put("wince"  .hashCode(), BUILD_WINCE);
      iht.put("winmo"  .hashCode(), BUILD_WINMO);
      iht.put("win32"  .hashCode(), BUILD_WIN32);
      iht.put("linux"  .hashCode(), BUILD_LINUX);
      iht.put("applet" .hashCode(), BUILD_APPLET);
      iht.put("html"   .hashCode(), BUILD_APPLET);
      iht.put("ios"    .hashCode(), BUILD_IPHONE);
      iht.put("iphone" .hashCode(), BUILD_IPHONE);
      iht.put("android".hashCode(), BUILD_ANDROID);
      iht.put("all"    .hashCode(), BUILD_ALL);

      // parse the parameters
      for (int i = 1; i < args.length; i++)
      {
         String op = args[i].toLowerCase();
         char first = op.charAt(0);
         if (first == '-' || first == 8211)
         {
            op = op.substring(1);
            if (op.startsWith("no")) // guich@tc126_55
               try {options &= ~iht.get(op.substring(2).hashCode());}
               catch (ElementNotFoundException e) {throw new DeployerException("Unknown option: "+op);}
            else
               try {options |= iht.get(op.hashCode());}
               catch (ElementNotFoundException e) {throw new DeployerException("Unknown option: "+op);}
         }
         else
         if (first == '/')
            switch (op.charAt(1))
            {
               case 'a':
                  if (op.equals("/autostart"))
                  {
                     System.out.println("Autostart on Android's boot");
                     DeploySettings.autoStart = true;
                  }
                  else
                  {
                     DeploySettings.applicationId = args[++i]; 
                     DeploySettings.appIdSpecifiedAsArgument = true;
                  }
                  break;
               case 'c': try {DeploySettings.commandLine = args[++i];}
                         catch (Exception e) {throw new DeployerException("Invalid /a format. The arguments must be passed between \"\", like in /a \"all the arguments to be passed to the callee application\"");}
                         break;
               //$START:REMOVE-ON-SDK-GENERATION$                         
               case 'd': J2TC.dump = true;
                         if (op.length() == 3 && op.charAt(2) == '2')
                            J2TC.dumpBytecodes = true;
                         break;
               //$END:REMOVE-ON-SDK-GENERATION$
               case 's': DeploySettings.autoSign = true;
                         if (i < args.length - 1)
                         {
                            String password = args[++i];
                            if ((first = password.charAt(0)) != '-' || first != '/')
                               DeploySettings.autoSignPassword = password;
                            else // password not provided, back to the previous parameter
                               i--;
                         }
                         break;
               case 'm':
                        DeploySettings.buildIPA = true;
                        File folder = new File(args[++i]);
                        DeploySettings.certStorePath = folder.getPath();
                        folder.list(new FilenameFilter()
                        {
                           public boolean accept(File dir, String fileName)
                           {
                              String fileNameLower = fileName.toLowerCase();
                              if (fileNameLower.endsWith(".mobileprovision"))
                              {
                                 DeploySettings.mobileProvision = new File(dir, fileName);
                                 System.out.println("Mobile provision: "+DeploySettings.mobileProvision.getAbsolutePath());
                              }
                              else if (fileNameLower.endsWith(".p12"))
                                 DeploySettings.appleCertStore = new File(dir, fileName);
                              return false;
                           }
                        });
                        break;
               case 'n': 
                         DeploySettings.filePrefix = args[++i];
                         if (DeploySettings.filePrefix.toLowerCase().endsWith(".tcz"))
                            DeploySettings.filePrefix = DeploySettings.filePrefix.substring(0,DeploySettings.filePrefix.length()-4);
                         DeploySettings.isTotalCrossJarDeploy = DeploySettings.filePrefix.equals("TCBase");
                         break;
               case 'x': DeploySettings.excludeOptionSet = true;
                         String [] exc = totalcross.sys.Convert.tokenizeString(args[++i], ',');
                         for (int j =0; j < exc.length; j++)
                            DeploySettings.exclusionList.addElement(exc[j].replace('.','/'));
                         break;
               case 'k': Deployer4WinCE.keepExe = true;
                         Deployer4WinCE.keepExeAndDontCreateCabFiles = op.equals("/kn");
                         break;
               case 'o': DeploySettings.targetDir = args[++i];
                         break;
               case 'v': DeploySettings.quiet = false;
                         break;
               case 'r': String key = args[++i].toUpperCase();
                         if (!key.matches("([0-9A-F]{4}(\\-)?){6}"))
                            throw new DeployerException("The key must be specified in the following format: XXXX-XXXX-XXXX-XXXX-XXXX-XXXX (with or without '-')");
                         DeploySettings.rasKey = Convert.hexStringToBytes(key, true);
                         boolean lbok = DeploySettings.rasKey[2] == 'L' && DeploySettings.rasKey[3] == 'B';
                         System.out.println("The application was signed with the given registration key. "+(lbok ? "Litebase is allowed." : "Litebase is NOT allowed."));
                         break;
               case 't': DeploySettings.testClass = true; 
                         break; // guich@tc115_37: missing break
               case 'w': waitIfError = true;
                         break;
               case 'p': if (i >= args.length-1)
                            throw new DeployerException("You must provide the package type for /p");
                         String type = args[++i].toLowerCase();
                         if (type.startsWith("release"))
                         {
                            DeploySettings.packageType = DeploySettings.PACKAGE_RELEASE;
                            if (DeploySettings.folderTotalCrossVMSDistVM == null)
                               throw new DeployerException("Could not find the path for TotalCrossVMS, so its impossible to create a single installation package.");
                         }
                         else
                         if (type.startsWith("demo"))
                         {
                            DeploySettings.packageType = DeploySettings.PACKAGE_DEMO;
                            if (DeploySettings.folderTotalCrossSDKDistVM == null)
                               throw new DeployerException("Could not find the path for TotalCrossSDK, so its impossible to create a single installation package.");
                         }
                         else
                            throw new DeployerException("Invalid package option: "+type);
                         boolean isDemo = (DeploySettings.packageType & DeploySettings.PACKAGE_DEMO) != 0;
                         if (type.endsWith("litebase"))
                         {
                            DeploySettings.packageType |= DeploySettings.PACKAGE_LITEBASE;
                            String lbfolder = DeploySettings.folderLitebaseSDKDistLIB;
                            if (lbfolder == null)
                               throw new DeployerException("Could not find the path for LitebaseSDK, so its impossible to create a single installation package.");                               
                         }
                         System.out.println("Creating single installation package: "+(isDemo?"DEMO TCVM":"ACTIVATION TCVM")+((DeploySettings.packageType & DeploySettings.PACKAGE_LITEBASE) != 0 ? " + LITEBASE" : ""));
                         break;
               case 'i': DeploySettings.installPlatforms = args[++i].toLowerCase()+",";
                         break;
                         
               default:  throw new DeployerException("Invalid option: "+op);
            }
      }
      return options;
   }

   private void usage()
   {
      System.out.println(
            "\n"+
            "Format: tc.Deploy <what to deploy> <platforms to deploy>\n"+
            "\n"+
            "<what to deploy> is the path to search for class files, or a class that\n"+
            "extends MainWindow or implements MainClass, or a jar file containing all files to package (the name of the jar must match the MainWindow's name).\n"+
            "You can also specify a .tcz that will be converted to the target platforms.\n"+
            "Library files must be specified in a jar, and the file's name must end with 'Lib', or it will not be loaded by the VM.\n"+
            "A package file, if present in the current folder, will be used to specify additional files that will " +
            "be included in the installations (palm.pkg, wince.pkg, bb.pkg, iphone.pkg, android.pkg, win32.pkg, linux.pkg; or all.pkg for all platforms, " +
            "used if the platform's pkg was not found). Inside these files, [G] states that the " +
            "file will be placed in the same folder of the TCVM, and [L] states that the file will be placed in the program's folder " +
            "(this rule applies only for wince; in all other platforms, [G] and [L] are placed in the same folder). If the file ends with a slash '/', " +
            "indicating that its a path, all files inside that path will be added. " +
            "In android.pkg, you may add a target folder separating the paths with , (E.G.: c:/y/z/file.txt,/device/targetpath).\n"+
            "For WinCE, you can also create an wince.inf file with the whole inf file which will be used instead of the automatically created one.\n"+ 
            "\n"+
            "<platforms to deploy> : one of the following (none just creates the tcz file)\n" +
            "   -ce or -wince : create the cab files for Windows CE\n" +
            "   -winmo : create the cab files for Windows Mobile only\n" +
            "   -win32 : create the exe file to launch the application in Windows\n" +
            "   -linux : create the .sh file to launch the application in Linux\n" +
            "   -applet or -html : create the html file and a jar file with all dependencies\n" +
            "       to run the app from a java-enabled browser (the input cannot be a jar file)\n" +
            "   -iphone or -ios: create the iPhone 4.x (and up) installer packages\n" +
            "   -android: create the apk file for Android\n" +
            "\n"+
            "   -all : single parameter to deploy to all supported platforms\n"+
            "\n"+
            "Optionally, pass -noPlatformToNOTDeploy, to disable the deployment for that platform. For example \"-all -nowince\" builds for all platforms except wince. Just make sure that all -no options comes after the platform selections (E.G.: \"-nowince -all\" will not work)\n" +
            "\n"+
            "You can also use the options:\n" +
            "   /a ApId : Assigns the application id; can only be used for libraries or passing a tcz file\n"+
            "   /autostart: automatically starts the application after a boot is completed. Currently works for Android only.\n"+
            "   /c cmd  : Specify a command line to be passed to the application");
      //$START:REMOVE-ON-SDK-GENERATION$                         
      System.out.println(
            "   /d      : Dump generated opcodes for the program\n"+
            "   /d2     : Dump generated opcodes, including java bytecodes"
      );
      //$END:REMOVE-ON-SDK-GENERATION$
      System.out.println(
            "   /i platforms : install the file after generating it; platforms is a list of comma-separated platforms. Currently supports only \"/inst android\".\n" +
            "   /k      : Keep the exe and other temporary files during wince generation\n"+
            "   /kn     : As /k, but does not create the cab files for wince\n"+
            "   /m path : Specifies a path to the mobileprovision and certificate store to deploy an ipa file for iOS\n"+
            "   /n name : Override the name of the tcz file with the given name\n" +
            "   /o path : Override the output folder with the given path (defaults to the current folder)\n" +
            "   /p type : Package the vm (and optionally litebase) with the application, creating a single installation file. " +
                         "The type parameter can be one of the following: demo, demo+litebase, release, release+litebase " +
                         "(where demo/release are the virtual machine types you want to include, the time-limited demonstration, " +
                         "or the release that requires activation). The DEMO SDKs must be in the path or in the " +
                         "TOTALCROSS2_HOME/LITEBASE_HOME environment variables, and the RELEASE SDKs must be in the " +
                         "same parent folder of the DEMO ones. Example: if TOTALCROSS2_HOME points to t:\\sdks\\TotalCrossSDK, " +
                         "then the VMS must be at t:\\sdks\\TotalCrossVMS. If the TOTALCROSS2_HOME and LITEBASE_HOME are not set," +
                         "then all SDKs must be at the top-level folder of the TotalCrossSDK\\etc folder. " +
                         "The files are always installed at the same folder of the application, so each application will have its own vm/litebase." +
                         "You can optionally set four environment variables, pointing to the folder of each SDK (these will have priority over the " +
                         "other locations): TOTALCROSS2_DEMO (must point to TotalCrossSDK folder), TOTALCROSS_RELEASE (must point to TotalCrossVMS folder), " +
                         "LITEBASE_DEMO or LITEBASE_RELEASE (must point to LitebaseSDK folder).\n" +
            "   /r key  : Specify a registration key to be used to activate TotalCross when required\n" +
            "   /s pass : Launch the BlackBerry SignatureTool and automatically sign the COD module\n" +
            "             using the optional password. If no password is provided, the SignatureTool will\n" +
            "             be launched and you will have to specify a password manually.\n" +
            "   /t      : Just test the classes to see if there are any invalid references. Images are not converted, and nothing is written to disk.\n" +
            "   /v      : Verbose output for information messages\n" +
            "   /w      : Waits for a key press if an error occurs\n" +
            "   /x list : Comma-separated list of class names that must be excluded (in a starts-with manner). E.G.: \"/x com/framework/\" \n" +
            "\n" +
            "   The easiest way to create an icon is to provide an 'appicon.gif' file of any SQUARE size (80x80 preferable) " +
            "and any palette, which will be automatically converted to the target icon sizes. Put the file in the src folder." +
            "If you need better icons, you can create some bmp and png files with these sizes: icon15x9x8.bmp icon30x18x8.bmp icon22x22x8.bmp icon44x44x8.bmp (Palm OS), " +
            "icon16x16x8.bmp icon32x32x8.bmp icon48x48x8.bmp (Windows CE/Windows 32), icon60x60.png (iPhone), icon80x80.png (BlackBerry), icon72x72.png (Android - use alpha channel on PNG for better appearance). " +
            "Be careful with the palette of the bmp files, never use the MSPaint program; instead, get the bmp files that are in the etc/images folder and edit " +
            "them in a software that keeps the original palette, like Photoshop and PaintShopPro." +
            "");
   }
}
