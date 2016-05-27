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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import tc.Deploy;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.sys.Time;
import totalcross.util.Hashtable;
import totalcross.util.IntVector;
import totalcross.util.InvalidDateException;
import totalcross.util.Vector;

public class DeploySettings
{
   public static final String UnknownVendor = "Unknown Vendor"; // fdie@570_96

   public static String[] tczs;
   // constants for including the vm and/or litebase in a package 
   public static boolean packageVM;
   public static String folderTotalCross3DistVM;
   
   public static String tczFileName;
   public static String targetDir;
   public static String filePrefix;
   public static Vector entriesList;
   public static Bitmaps bitmaps;
   public static String appTitle;
   public static String applicationId,appVersion,companyInfo,companyContact;
   public static String mainClassName;
   public static String commandLine = "";
   public static boolean isMainWindow;
   public static boolean isJarOrZip;
   public static boolean testClass; // guich@tc114_54
   public static boolean isFullScreen;
   public static String  fullScreenPlatforms;
   public static String fontTCZ = "TCFont.tcz";
   public static boolean resizableWindow;
   public static boolean isService, isMainClass;
   public static int windowFont, windowSize;
   public static double dJavaVersion;
   
   public static boolean autoStart;

   public static byte[] rasKey;
   public static boolean autoSign;
   public static String autoSignPassword;
   public static boolean quiet=true; // to set to false, pass /v(erbose) to tc.Deploy

   public static final char DIRSEP = java.io.File.pathSeparatorChar;
   public static final char SLASH = java.io.File.separatorChar;

   public static String [] classPath; // environment variable
   public static String [] path; // guich@tc111_19 environment variable
   public static byte[] defaultArgument = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890".getBytes();
   public static String homeDir, etcDir, binDir, distDir;
   public static String currentDir;
   public static String baseDir;
   public static String mainClassDir;
   public static Vector exclusionList = new Vector(10);
   public static IntVector appletFontSizes = new IntVector();
   public static String javaVersion = System.getProperty("java.version");
   public static String osName = System.getProperty("os.name").toLowerCase();
   public static boolean isBuggyJDKVersionForSynchronizedKeyword; // guich@tc120_0

   public static boolean appIdSpecifiedAsArgument;
   public static boolean inputFileWasTCZ;

   public static boolean excludeOptionSet;
   public static String showBBPKGName,showBBPKGRoot;

   public static String mainPackage;

   public static boolean isTotalCrossJarDeploy;

   public static String installPlatforms = "";
   
   public static HashMap<String,String> hmMappedClasses = new HashMap<String,String>(5);

   // iOS IPA required files
   public static boolean buildIPA = false;
   public static String certStorePath;
   public static java.io.File mobileProvision;
   public static java.io.File appleCertStore;
   public static KeyStore iosKeyStore;
   public static X509CertificateHolder iosDistributionCertificate;
   
   public static byte[] tcappProp;
   public static final String TCAPP_PROP = "tcapp.prop";
   public static int appBuildNumber=-1;

   public static boolean isFreeSDK;

   /////////////////////////////////////////////////////////////////////////////////////
   public static void init() throws Exception
   {
      String completeVersion = javaVersion;
      // guich@tc114_6: force java version 1.6 or above
      int firstDot = javaVersion.indexOf('.');
      int secondDot = javaVersion.indexOf('.',firstDot+1);
      if (secondDot != firstDot && secondDot != -1)
         javaVersion = javaVersion.substring(0,secondDot);
      dJavaVersion = Double.parseDouble(javaVersion);
      if (dJavaVersion < 1.6)
         throw new DeployerException("Error: the Deployer requires JDK 1.6 or above!");
      // guich@tc120_0: check the minor version and make sure no one uses 1.6.0_06
      int subver = 100;
      try {subver = Integer.parseInt(completeVersion.substring(completeVersion.indexOf('_')+1));} catch (Exception e) {}
      isBuggyJDKVersionForSynchronizedKeyword = dJavaVersion < 1.7 && dJavaVersion > 1.5 && subver <= 6; // 1.6 and <= 6 ?
      
      exclusionList.addElement("totalcross/");
      exclusionList.addElement("java/");
      exclusionList.addElement("[");
      exclusionList.addElement("tc/tools/");
      exclusionList.addElement("litebase/");
      exclusionList.addElement("ras/");
      exclusionList.addElement("net/rim/");
      
      hmMappedClasses.put("java.math.BigDecimal","totalcross.util.BigDecimal");
      hmMappedClasses.put("java.math.BigInteger","totalcross.util.BigInteger");
      hmMappedClasses.put("java.util.Random", "totalcross.util.Random");
      hmMappedClasses.put("java.lang.StringBuilder", "java.lang.StringBuffer");
      hmMappedClasses.put("java.lang.CharSequence", "java.lang.String"); // used in the replace method
      
      appletFontSizes.addElement(12);
      
      currentDir = System.getProperty("user.dir").replace('\\','/');
      // parse the classpath environment variable
      String cp0;
      String cp = cp0 = tc.Deploy.bootClassPath != null ? tc.Deploy.bootClassPath : System.getProperty("java.class.path");
      classPath = Convert.tokenizeString(cp,DIRSEP);
      if (classPath == null)
         classPath = new String[]{"."};
      Utils.removeQuotes(classPath);
      // parse the path environment variable
      cp = System.getenv("path"); // guich@tc111_19
      if (cp == null)
         cp = System.getenv("PATH"); // guich@tc122_29: linux is case-sensitive
      if (cp != null)
         path  = Convert.tokenizeString(cp,DIRSEP);
      if (path != null)
         Utils.removeQuotes(path);
      // setup the etc directory
      if (new File(currentDir+"/etc").exists())
         etcDir = currentDir+"/etc";
      else
      if (new File(System.getenv("GIT_HOME")+"/TotalCross/TotalCross3/etc").exists()) // check first at p:
         etcDir = System.getenv("GIT_HOME")+"/TotalCross/TotalCross3/etc";
      else
      if ((etcDir = Utils.findPath("etc",false)) == null)
      {
         String tchome = System.getenv("TOTALCROSS3_HOME");
         if (tchome == null)
            tchome = System.getenv("TOTALCROSS_HOME");
         
         if (tchome != null)
            etcDir = tchome.replace('\\','/')+"/etc";
         else
         if (isWindows()) // if in windows, search in all drives
         {
            for (char i = 'c'; i <= 'z'; i++)
               if (new File(i+":/TotalCross3/etc").exists())
               {
                  etcDir = i+":/TotalCross3/etc";
                  break;
               }
         }
         else
         if (new File("/TotalCross3/etc").exists()) // check on the root of the current drive
            etcDir = "/TotalCross3/etc";
      }
      if (etcDir != null)
      {
         if (!etcDir.endsWith("/"))
            etcDir += "/";
         etcDir = Convert.replace(etcDir, "//","/").replace('\\','/');
         homeDir = Convert.replace(etcDir, "/etc/","/");
         binDir = Convert.replace(etcDir, "/etc/","/bin/");
         distDir = Convert.replace(etcDir, "/etc/", "/dist/");
      }
      System.out.println("TotalCross SDK version "+Settings.versionStr+"."+Settings.buildNumber+" running on "+osName+" with JDK "+javaVersion);
      System.out.println("Current folder: "+currentDir);
      System.out.println("Etc directory: "+ (etcDir != null ? etcDir : "not found")); // keep this always visible, its a very important information
      System.out.println("Classpath: "+cp0);

      // find the demo and release folders for totalcross and litebase
      String f = System.getenv("TOTALCROSS3_HOME");
      if (f == null)
         f = System.getenv("TOTALCROSS3");
      if (f != null)
      {
         folderTotalCross3DistVM = Convert.appendPath(f, "dist/vm/");
         if (!new File(folderTotalCross3DistVM).isDir())
         {
            folderTotalCross3DistVM = Convert.appendPath(f, "vm/");
            if (!new File(folderTotalCross3DistVM).isDir())
               folderTotalCross3DistVM = f;
         }
      }

      if (folderTotalCross3DistVM == null)
         folderTotalCross3DistVM = distDir+"vm/";
      // check if folders exist
      if (folderTotalCross3DistVM == null || !new File(folderTotalCross3DistVM).exists()) 
         folderTotalCross3DistVM = null;
      
      Utils.fillExclusionList(); //flsobral@tc115: exclude files contained in jar files in the classpath.
      
      handleTCAppProp();
   }
   
   public static void iosKeystoreInit() throws CertificateException, NoSuchProviderException, KeyStoreException, NoSuchAlgorithmException, java.io.FileNotFoundException, IOException, UnrecoverableKeyException, InvalidDateException {
       //flsobral: dynamically load libraries required to build for iPhone.
       Deploy.JarClassPathLoader.addFile(DeploySettings.etcDir + "libs/bouncycastle/bcprov-jdk15on-147.jar");
       Deploy.JarClassPathLoader.addFile(DeploySettings.etcDir + "libs/bouncycastle/bcpkix-jdk15on-147.jar");
       // initialize bouncy castle
       Security.addProvider(new BouncyCastleProvider());
	   if (DeploySettings.appleCertStore != null) {
	      CertificateFactory cf = CertificateFactory.getInstance("X509", "BC");
	      KeyStore ks = java.security.KeyStore.getInstance("PKCS12", "BC");
	      ks.load(new FileInputStream(DeploySettings.appleCertStore), "".toCharArray());
	      
	      String keyAlias = (String) ks.aliases().nextElement();
	      Certificate storecert = ks.getCertificate(keyAlias);
	      if (storecert == null)
	      {
	         java.io.File[] certsInPath = DeploySettings.appleCertStore.getParentFile().listFiles(new FilenameFilter()
	         {
	            public boolean accept(java.io.File arg0, String arg1)
	            {
	               return arg1.endsWith(".cer");
	            }
	         });
	         if (certsInPath.length == 0)
	            throw new DeployerException("Distribution certificate was not found in " + DeploySettings.appleCertStore.getParent());
	
	         storecert = cf.generateCertificate(new ByteArrayInputStream(FileUtils.readFileToByteArray(certsInPath[0])));
	         PrivateKey pk = (PrivateKey) ks.getKey(keyAlias, "".toCharArray());
	         ks.deleteEntry(keyAlias);
	         ks.setEntry(
	               keyAlias,
	               new KeyStore.PrivateKeyEntry(pk, new Certificate[] { storecert }),
	               new KeyStore.PasswordProtection("".toCharArray())
	               );
	      }
	      DeploySettings.iosKeyStore = ks;
	      DeploySettings.iosDistributionCertificate = new X509CertificateHolder(storecert.getEncoded());
	      Settings.iosCertDate = new Time(DeploySettings.iosDistributionCertificate.getNotAfter().getTime(), false);
	      Utils.println("iOS Certificate expiration date: "+Settings.iosCertDate.getSQLString());
      }
   }
   
   private static void handleTCAppProp()
   {
      String dir = currentDir;
      while (true)
      {
         try
         {
            String path = Convert.appendPath(dir, TCAPP_PROP);
            File f = new File(path, File.READ_WRITE);
            Hashtable ht = new Hashtable(new String(f.read()));
            appBuildNumber = Convert.toInt((String)ht.get("build.number","0"), 0) + 1;
            ht.put("build.number", appBuildNumber);
            byte[] bytes = ht.getKeyValuePairs("=").toString("\n").getBytes();
            f.setSize(0);
            f.writeAndClose(bytes);
            tcappProp = bytes;
            System.out.println("Application's build number: "+appBuildNumber);
            break;
         }
         catch (FileNotFoundException fnfe)
         {
            dir = Utils.getParent(dir);
            if (dir == null)
               break;
         }
         catch (Exception e)
         {
            e.printStackTrace();
            break;
         }
         if (tcappProp == null)
            Utils.println("File "+TCAPP_PROP+" not found; build number could not be generated.");
      }
   }

   /** From 5.21, return 5 */
   public static int getAppVersionHi()
   {
      if (appVersion == null)
         return 1;
      try
      {
         if (appVersion.indexOf('.') > 0)
            return Convert.toInt(appVersion.substring(0, appVersion.indexOf('.')));
         return Convert.toInt(appVersion);
      } catch (InvalidNumberException ine) {return 1;}
   }

   /** From 5.21, returns 21 */
   public static int getAppVersionLo()
   {
      if (appVersion == null || appVersion.indexOf('.') < 0)
         return 0;
      try
      {
         return Convert.toInt(appVersion.substring(0, appVersion.indexOf('.')));
      } catch (InvalidNumberException ine) {return 0;}
   }
   
   public static boolean isWindows()
   {
      return osName.indexOf("windows") >= 0;
   }
   
   public static boolean isUnix()
   {
      return osName.indexOf("linux") >= 0 || osName.indexOf("unix") >= 0;
   }
   
   public static boolean isMac()
   {
      return osName.indexOf("mac") >= 0;
   }
   
   public static String appendDotExe(String s) // guich@tc115_85
   {
      return isUnix() || isMac() ? s : (s+".exe");
   }

   public static boolean isFullScreenPlatform(String plat) // guich@tc120_59
   {
      if (fullScreenPlatforms == null)
         return isFullScreen; // guich@tc126_32: return what user decided for all platforms
      return fullScreenPlatforms.indexOf(plat) >= 0;
   }
   
   public static String pathAddQuotes(String p)
   {
      if (p.indexOf(' ') == -1 || p.indexOf('"') != -1)
         return p;
      return isWindows() ? '"'+p+'"' : p;
   }
}
