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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class CodFile
{
   /**
    * Flag to indicate that this application denotes a system module.
    * Default value is "false".
    */
   public boolean isSystemModule;

   /**
    * Flag to indicate that this application or library must be executed during
    * the startup process. Default value is "false".
    */
   public boolean runOnStartup;

   /**
    * Defines the priority of this application or library in the startup process.
    * This parameter will have no effect if <code>runOnStartup</code> is not set.
    * Values can range from 0 to 7. Default value is "Low priority" (7).
    */
   public int startupTier = 7;

   /**
    * Defines the position of this application in the home screen. This parameter
    * will have no effect if <code>type</code> indicates a "Library" (2).
    * Default value is "No preference" (0).
    */
   public int ribbonPosition;

   /**
    * Defines the type of this cod file (TYPE_CLDC_APPLICATION, TYPE_MIDLET or TYPE_LIBRARY).
    * Default value is TYPE_CLDC_APPLICATION.
    */
   public int type;

   /**
    * The name of the .COD module to generate
    */
   public String moduleName;

   /**
    * The title of this application or library.
    */
   public String appTitle;

   /**
    * The creator Id of this application or library.
    */
   public String applicationId;

   /**
    * The version of this application or library.
    * Default value is "1".
    */
   public String appVersion;

   /**
    * The vendor of this application or library.
    */
   public String appVendor;
   
   /**
    * The description of this application or library.
    */
   public String appDescription;

   /**
    * The copyright info.
    */
   public String copyrightInfo;

   /**
    * The fully qualified name of the main application or library class. If not set,
    * will be equals to <code>codename</code>.
    */
   public String mainClassName;

   /**
    * A string that will be passed to the application as the command line.
    */
   public String commandLine;

   /**
    * The image to be used as the application's icon. This parameter will have no effect
    * if <code>type</code> indicates a "Library" (2).
    */
   public byte[] icon;

   /**
    * Flag indicating whether this module must be auto signed. If <code>false</code>, the
    * module signature will be skipped.
    */
   public boolean autoSign;

   /**
    * The password used to sign the resulting COD module.
    */
   public String autoSignPassword;

   private static File[] libraries;

   private static final String EXT_REGEX = "((\\.(alx|jad|cod|jar|csl|cso|rapc))|((\\-\\d+)?\\.debug))";
   private static Hashtable runExeImportant = new Hashtable();
   private static Hashtable runExeImportantIgnore = new Hashtable();

   public static final int TYPE_CLDC_APPLICATION = 0;
   public static final int TYPE_MIDLET = 1;
   public static final int TYPE_LIBRARY = 2;
   
   static
   {
      runExeImportant.put("rapc", new String[] {"error", "warning"});
      runExeImportantIgnore.put("rapc", new String[] {"parsing classfile", "not required in class", "initialized but not used", "not invoked", "should be declared static"});
   }

   /**
    * @param inputFile
    * @param targetDir
    * @throws IOException
    */
   public void create(File inputFile, File targetDir) throws IOException
   {
      if (System.getProperty("rim.root") == null)
         throw new IllegalArgumentException("net_rim_api.jar could not be found. Please run the tool with the '-Drim.root=<blackberry_jde_root>' option");

      // Use the curr dir as default if targetDir is invalid or cannot be created
      if (targetDir == null || (!targetDir.exists() && !targetDir.mkdirs()) || !targetDir.isDirectory())
         targetDir = new File(System.getProperty("user.dir"));

      if (applicationId == null)
         throw new DeployerException("Missing applicationId");
      if (appTitle == null)
         appTitle = moduleName;
      if (appVendor == null)
         appVendor = "N/A";
      if (appVersion == null)
         appVersion = "1.0";
      if (appDescription == null)
         appDescription = "N/A";
      if (commandLine == null)
         commandLine = "";
      if (copyrightInfo == null)
         copyrightInfo = "N/A";
      if (mainClassName != null)
         mainClassName = mainClassName.replace('/', '.');
      else if (type != TYPE_LIBRARY)
         throw new DeployerException("Missing mainClassName");
      if (moduleName == null)
         throw new DeployerException("Missing moduleName");

      inputFile = inputFile.getCanonicalFile();

      // Delete all files first
      deleteAll(targetDir);

      // Create the RAPC parameters file
      File rapc = createRapc(targetDir);
      rapc.deleteOnExit();

      // Compile everything
      build(inputFile, rapc, targetDir);

      try {new File(targetDir, "LogFile.txt").delete();} catch (Exception e) {};
   }

   private void deleteAll(File targetDir) throws IOException
   {
      String regex = "\\Q" + moduleName + "\\E" + EXT_REGEX;
      String[] files = targetDir.list();
      for (int i = files.length - 1; i >= 0; i--)
      {
         if (files[i].matches(regex))
         {
            File f = new File(targetDir, files[i]);
            if (!f.delete())
               throw new IOException("Cannot delete file: " + f.getPath());
         }
      }
   }

   private File createRapc(File targetDir) throws IOException
   {
      int flags = type;
      flags  += (7 - startupTier) * 32;
      if (runOnStartup) flags += 1;
      if (isSystemModule) flags += 2;

      File file = new File(targetDir, moduleName + ".rapc");
      PrintStream ps = new PrintStream(new FileOutputStream(file));

      ps.println("MIDlet-Name: " + appTitle);
      ps.println("MIDlet-Version: " + appVersion);
      ps.println("MIDlet-Vendor: " + appVendor);
      ps.println("MIDlet-Description: " + appDescription);
      ps.println("MIDlet-Jar-URL: " + moduleName + ".jar");
      ps.println("MIDlet-Jar-Size: 0");
      ps.println("MicroEdition-Profile: MIDP-2.0");
      ps.println("MicroEdition-Configuration: CLDC-1.1");
      ps.println((type == TYPE_LIBRARY ? "RIM-Library-Flags: " : "RIM-MIDlet-Flags-1: ") + flags);

      if (type != TYPE_LIBRARY)
      {
         ps.println("MIDlet-1: " + appTitle + ",icon.png," + mainClassName + "|" + applicationId + "|" + commandLine);
         if (ribbonPosition > 0)
            ps.println("RIM-MIDlet-Position-1: " + ribbonPosition);
      }

      ps.close();
      return file;
   }

   private File createAlx(File targetDir) throws IOException
   {
      File file = new File(targetDir, moduleName + ".alx");
      PrintStream ps = new PrintStream(new FileOutputStream(file));

      ps.println("<loader version=\"1.0\">");
      ps.println("   <application id=\"" + moduleName + "\">");
      ps.println("      <name>" + appTitle + "</name>");
      ps.println("      <description>" + appDescription + "</description>");
      ps.println("      <version>" + appVersion + "</version>");
      ps.println("      <vendor>" + appVendor + "</vendor>");
      ps.println("      <copyright>" + copyrightInfo + "</copyright>");
      ps.println("      <fileset Java=\"1.28\">");
      ps.println("         <directory></directory>");
      ps.println("         <files>");
      ps.println("            " + moduleName + ".cod");
      ps.println("         </files>");
      ps.println("      </fileset>");
      ps.println("   </application>");
      ps.println("</loader>");
      ps.close();

      return file;
   }

   private void build(File inputFile, File rapcFile, File targetDir) throws IOException
   {
      File rimRoot = new File(System.getProperty("rim.root")).getCanonicalFile();
      File preverifyExe = new File(rimRoot, "bin/preverify.exe");
      if (!preverifyExe.exists())
         throw new DeployerException("preverify.exe not found: " + preverifyExe.getPath());

      File rapcExe = new File(rimRoot, "bin/rapc.exe");
      if (!rapcExe.exists())
         throw new DeployerException("rapc.exe not found: " + rapcExe.getPath());
      
      if (Utils.searchIn(DeploySettings.path, DeploySettings.appendDotExe("jar")) == null) // guich@tc111_19
         throw new DeployerException("The "+DeploySettings.appendDotExe("jar")+" file was not found. Please add the java\\bin folder to the path. If its already in the path, then make sure that you have installed a JDK, and not a JRE, because JRE does not have "+DeploySettings.appendDotExe("jar")+", which is needed to deploy for BlackBerry.");
         
      // Get classpath (imported jars)
      String classpath = "";
      File[] libs = getLibraries();
      int n = libs.length;

      for (int i = 0; i < n; i ++)
         if (!Utils.strip(libs[i].getName()).equals(moduleName))
            classpath += libs[i].getPath() + File.pathSeparator;

      if (!classpath.contains("net_rim_api.jar" + File.pathSeparator)) // net_rim_api.jar not found
      {
         File rimLib = new File(rimRoot, "lib/net_rim_api.jar");
         if (!rimLib.exists())
            throw new DeployerException("net_rim_api.jar not found: " + rimLib.getPath());

         classpath += rimLib.getPath();
      }

      // First, preverify jar file
      runExeFile("preverify", preverifyExe, new String[]{"-classpath", classpath, "-d", inputFile.getParent(), inputFile.getPath()}, targetDir, true, true);

      // Then, compile it
      runExeFile("rapc", rapcExe, new String[]{"import=" + classpath, (type == TYPE_LIBRARY ? "library=" : type == TYPE_MIDLET ? "midlet=" : "codename=") + moduleName, "rapc=" + rapcFile.getAbsolutePath(), inputFile.getPath()}, targetDir, true, true); // guich@tc123_60: use getAbsolutePath

      createAlx(targetDir);

      // Request signature
      if (autoSign)
      {
         File sigToolJar = new File(rimRoot.getPath(), "bin/SignatureTool.jar");
         if (sigToolJar.exists())
         {
            // Run signature tool
            File javaExe = new File(System.getProperty("java.home"), DeploySettings.appendDotExe("/bin/java"));
            if (autoSignPassword == null) // password not provided - just launch signature tool
               runExeFile("signaturetool", javaExe, new String[]{"-jar", sigToolJar.getPath(), "-a", "-c", "-C", moduleName + ".cod"}, targetDir, true, true);
            else
               runExeFile("signaturetool", javaExe, new String[]{"-jar", sigToolJar.getPath(), "-a", "-c", "-C", "-p", autoSignPassword, moduleName + ".cod"}, targetDir, true, true);
         }
      }
   }

   private int runExeFile(String pid, File exeFile, String[] args, File targetDir, boolean wait, boolean failOnError) throws IOException
   {
      int n = args.length;
      String[] nArgs = new String[n + 1];
      nArgs[0] = exeFile.getPath();
      System.arraycopy(args, 0, nArgs, 1, n);

      Process p = Runtime.getRuntime().exec(nArgs, null, targetDir);

      int ret = 0;
      if (wait)
      {
         String line;
         
         BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
         while ((line = br.readLine()) != null)
            if (!line.equals("No errors."))
               Utils.println("[" + pid + "] " + line, isImportantMessage(pid, line)); // common messages are only printed if they are important

         br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
         while ((line = br.readLine()) != null)
            Utils.println("[" + pid + "] " + line, true); // error messages are always printed

         try
         {
            ret = p.waitFor();
            if (ret != 0 && failOnError)
               throw new DeployerException((pid + " failed (" + ret + ")."));
         }
         catch (InterruptedException e) {}
      }

      return ret;
   }
   
   private boolean isImportantMessage(String pid, String message)
   {
      String[] imp = (String[])runExeImportant.get(pid);
      String[] ign = (String[])runExeImportantIgnore.get(pid);
      message = message.toLowerCase();
      
      boolean important = false;
      if (imp != null)
      {
         for (int i = imp.length - 1; i >= 0; i--)
         {
            if (message.indexOf(imp[i]) >= 0) // this is an important message
            {
               important = true;
               break;
            }
         }
         
         if (important && ign != null)
         {
            for (int i = ign.length - 1; i >= 0; i--)
            {
               if (message.indexOf(ign[i]) >= 0) // this message should be ignored
               {
                  important = false;
                  break;
               }
            }
         }
      }
      
      return important;
   }

   public static File[] getLibraries()
   {
      if (libraries == null)
      {
         Vector libs = new Vector();
         String classPath = System.getProperty("rim.class.path");

         if (classPath != null)
         {
            Vector midlets = new Vector();
            String[] tokens = classPath.split(File.pathSeparator);
            int count = tokens.length;

            for (int i = 0; i < count; i++)
            {
               try
               {
                  File f = new File(tokens[i]).getCanonicalFile();
                  if (f.isDirectory())
                  {
                     String[] files = f.list();
                     for (int j = files.length - 1; j >= 0; j--)
                     {
                        if (files[j].toLowerCase().endsWith(".jar"))
                        {
                           try
                           {
                              addLibrary(new File(f, files[j]).getCanonicalFile(), libs, midlets);
                           }
                           catch (IOException ex) { }
                        }
                     }
                  }
                  else if (f.exists() && f.getName().toLowerCase().endsWith(".jar"))
                     addLibrary(f, libs, midlets);
               }
               catch (IOException ex)
               {
                  Utils.println("WARNING: Ignoring import '" + tokens[i] + "': " + ex.toString());
               }
            }

            if (libs.size() == 0)
               libraries = new File[0];
            else
            {
               count = libs.size();
               libraries = new File[count];
               for (int i = 0; i < count; i++)
                  libraries[i] = (File)libs.get(i);
            }
         }
      }

      return libraries;
   }

   private static void addLibrary(File f, Vector libs, Vector midlets) throws IOException
   {
      String midlet;
      
      if (f.getName().equalsIgnoreCase("net_rim_api.jar"))
         midlet = "net_rim_api";
      else
      {
         JarFile jf = new JarFile(f);
         Manifest man = jf.getManifest();
         jf.close();
         if (man == null || man.getMainAttributes().getValue("RIM-Library-Flags") == null)
            throw new IOException("File is not a RIM library.");

         midlet = man.getMainAttributes().getValue("MIDlet-Name");
      }
      
      if (!midlets.contains(midlet))
      {
         libs.addElement(f);
         midlets.addElement(midlet);
      }
   }
}
