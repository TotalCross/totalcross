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

import totalcross.sys.*;
import totalcross.util.Hashtable;
import totalcross.util.Vector;
import com.dd.plist.*;
import de.schlichtherle.truezip.file.*;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;
import org.apache.commons.io.*;
import org.bouncycastle.cert.*;
import org.bouncycastle.crypto.digests.*;
import org.bouncycastle.jce.provider.*;
import org.bouncycastle.x509.*;
import tc.tools.deployer.ipa.*;

/**
 * Generates IPhone application packages.
 */
public class Deployer4IPhoneIPA
{
   public static final String appleRootCA = Convert.appendPath(DeploySettings.etcDir, "tools/ipa/AppleRootCA.pem");
   public static final String appleWWDRCA = Convert.appendPath(DeploySettings.etcDir, "tools/ipa/AppleWWDRCA.pem");
   
   private Map<String, TFile> ipaContents = new HashMap<String, TFile>();
   
   MobileProvision Provision;
   
   public Deployer4IPhoneIPA() throws Exception
   {
      if (DeploySettings.mobileProvision == null || DeploySettings.appleCertStore == null)
         throw new NullPointerException();

      // initialize bouncy castle
      Security.addProvider(new BouncyCastleProvider());

      // locate template and target
      File templateFile = new File(Convert.appendPath(DeploySettings.folderTotalCross3DistVM, "ios/TotalCross.ipa"));
      File targetFile = File.createTempFile(DeploySettings.appTitle, ".zip");
      targetFile.deleteOnExit();
      // create a copy of the original file
      FileUtils.copyFile(templateFile, targetFile);

      // open new file with truezip
      TFile targetZip = new TFile(targetFile);

      // get the payload folder from the zip
      targetZip.listFiles(new FilenameFilter()
      {
         public boolean accept(File dir, String name)
         {
            ipaContents.put(name, new TFile(dir, name));
            return false;
         }
      });
      TFile payload = (TFile) ipaContents.get("Payload");

      // get the template appFolder - TotalCross.app
      payload.listFiles(new FilenameFilter()
      {
         public boolean accept(File dir, String name)
         {
            ipaContents.put(name, new TFile(dir, name));
            return false;
         }
      });
      TFile appFolder = (TFile) ipaContents.get("TotalCross.app");

      // create new appFolder using the deployed tcz name
      TFile newAppFolder = new TFile(payload, DeploySettings.filePrefix + ".app");
      // rename the appFolder
      appFolder.mv(newAppFolder);
      appFolder = newAppFolder;
      
      // Add tcz
      for (int i = 0; i < DeploySettings.tczs.length; i++)
         new TFile(DeploySettings.tczs[i]).cp(new TFile(appFolder, new File(DeploySettings.tczs[i]).getName()));
      // TCBase & TCUI
      new TFile(DeploySettings.folderTotalCross3DistVM, "TCBase.tcz").cp(new TFile(appFolder, "TCBase.tcz"));
      new TFile(DeploySettings.folderTotalCross3DistVM, "TCUI.tcz").cp(new TFile(appFolder, "TCUI.tcz"));
      // TCFont
      new TFile(DeploySettings.folderTotalCross3DistVM, DeploySettings.fontTCZ).cp(new TFile(appFolder, DeploySettings.fontTCZ));
      // Litebase
      new TFile(DeploySettings.folderTotalCross3DistVM, "LitebaseLib.tcz").cp(new TFile(appFolder, "LitebaseLib.tcz"));
      
      Hashtable ht = new Hashtable(13);
      Utils.processInstallFile("iphone.pkg", ht); // guich@tc111_22
      String[] extras = Utils.joinGlobalWithLocals(ht, null, true);
      Vector v = new Vector(extras);
      Utils.preprocessPKG(v,true);
      if (extras.length > 0)
      {
         TFile pkgFolder = new TFile(appFolder, "pkg");
         pkgFolder.mkdir();
         for (int i = 0; i < extras.length; i++)
         {
            String fname = extras[i];
            File ff = new File(fname);
            if (!ff.exists())
               ff = new File(Utils.findPath(fname,true));            
            new TFile(ff.getPath()).cp(new TFile(fname.endsWith(".tcz") ? appFolder : pkgFolder, Utils.getFileName(fname))); // guich@tc310: keep all tczs at the same parent folder
         }
      }
      
      // get references to the contents of the appFolder
      appFolder.list(new FilenameFilter()
      {
         public boolean accept(File dir, String name)
         {
            ipaContents.put(name, new TFile(dir, name));
            return false;
         }
      });

      /** PROCESS MOBILE PROVISION **/
      // update the mobile provision
      this.Provision = MobileProvision.readFromFile(DeploySettings.mobileProvision);

      /** PROCESS INFO.PLIST **/
      // read the info.plist from the zip file
      TFile infoPlist = (TFile) ipaContents.get("Info.plist");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      infoPlist.output(baos);
      NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(baos.toByteArray());
      String executableName = rootDict.objectForKey("CFBundleExecutable").toString();

      // add all the iphone icons
      addIcons(appFolder, rootDict);
      
      // update the application name
      rootDict.put("CFBundleName", DeploySettings.filePrefix);
      rootDict.put("CFBundleDisplayName", DeploySettings.appTitle);
      if (DeploySettings.appVersion != null)
      {
         rootDict.put("CFBundleVersion", DeploySettings.appVersion);
         rootDict.put("CFBundleShortVersionString", DeploySettings.appVersion);
      }
      rootDict.put("UIStatusBarHidden", DeploySettings.isFullScreen);

      String bundleIdentifier = this.Provision.bundleIdentifier;
      if (Settings.iosCFBundleIdentifier != null)
         bundleIdentifier = Settings.iosCFBundleIdentifier;
      else
      if (bundleIdentifier.equals("*"))
         bundleIdentifier = "com." + DeploySettings.applicationId + "." + DeploySettings.appTitle.replace(" ","").trim().toLowerCase();
      Utils.println("Package suffix id (CFBundleIdentifier): "+bundleIdentifier);
 
      rootDict.put("CFBundleIdentifier", bundleIdentifier);

      // overwrite updated info.plist inside the zip file
      byte[] updatedInfoPlist = MyNSObjectSerializer.toXMLPropertyListBytesUTF8(rootDict);
      infoPlist.input(new ByteArrayInputStream(updatedInfoPlist));

      /** PROCESS CERTIFICATE **/
      // install certificates
      CertificateFactory cf = CertificateFactory.getInstance("X509", "BC");
      X509CertificateHolder[] certs = new X509CertificateHolder[3];
      certs[0] = new X509CertificateHolder(cf.generateCertificate(
            new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(appleRootCA)))).getEncoded());
      certs[1] = new X509CertificateHolder(cf.generateCertificate(
            new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(appleWWDRCA)))).getEncoded());
      
      KeyStore ks = java.security.KeyStore.getInstance("PKCS12", "BC");
      ks.load(new FileInputStream(DeploySettings.appleCertStore), "".toCharArray());
      
      String keyAlias = (String) ks.aliases().nextElement();
      Certificate storecert = ks.getCertificate(keyAlias);
      if (storecert == null)
      {
         File[] certsInPath = DeploySettings.appleCertStore.getParentFile().listFiles(new FilenameFilter()
         {
            public boolean accept(File arg0, String arg1)
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
      certs[2] = new X509CertificateHolder(storecert.getEncoded());

      X509Store certStore = X509Store.getInstance(
            "CERTIFICATE/Collection", new X509CollectionStoreParameters(Arrays.asList(certs)), "BC");

      // provision
      TFile mobileProvision = (TFile) ipaContents.get("embedded.mobileprovision");
      mobileProvision.input(new ByteArrayInputStream(FileUtils.readFileToByteArray(DeploySettings.mobileProvision)));

      /** CREATE THE MACHOBJECTFILE **/
      TFile executable = (TFile) ipaContents.get(executableName);
//      String bundleResourceSpecification = rootDict.objectForKey("CFBundleResourceSpecification").toString();

      TFile codeResources = new TFile((TFile) ipaContents.get("_CodeSignature"), "CodeResources");
      baos.reset();
      codeResources.output(baos);
      NSDictionary codeResourcesDict = (NSDictionary) PropertyListParser.parse(baos.toByteArray());
      NSDictionary rules = (NSDictionary) codeResourcesDict.objectForKey("rules");
      
      byte[] sourceData = this.CreateCodeResourcesDirectory(appFolder, rules, executableName);
      ByteArrayOutputStream appStream = new ByteArrayOutputStream();
      executable.output(appStream);

      AppleBinary file = AppleBinary.create(appStream.toByteArray());
      // executable
      executable.input(new ByteArrayInputStream(file.resign(ks, certStore, bundleIdentifier, this.Provision.GetEntitlementsString().getBytes("UTF-8"), updatedInfoPlist, sourceData)));
      
      TVFS.umount(targetZip);      

      FileUtils.copyFile(targetFile, new File(Convert.appendPath(DeploySettings.targetDir, "/ios/" + DeploySettings.filePrefix + ".ipa")));
      
      System.out.println("... Files written to folder "+ Convert.appendPath(DeploySettings.targetDir, "/ios/"));
   }
   
   private void addIcons(TFile appFolder, NSDictionary rootDict) throws IOException
   {
      NSString[] icons = new NSString[Bitmaps.IOS_ICONS.length];

      for (int i = icons.length - 1; i >= 0; i--)
      {
         TFile icon = new TFile(appFolder, Bitmaps.IOS_ICONS[i].name);
         icon.input(new ByteArrayInputStream(Bitmaps.IOS_ICONS[i].getImage()));
         icons[i] = new NSString(Bitmaps.IOS_ICONS[i].name);
      }

      NSArray iconBundle = new NSArray(icons);
      rootDict.put("CFBundleIconFiles", iconBundle);
   }

   protected byte[] CreateCodeResourcesDirectory(TFile appFolder, final NSDictionary rules,
         final String executableName) throws UnsupportedEncodingException, IOException
   {
//       NSDictionary root = new NSDictionary();
       
//       NSDictionary rules = new NSDictionary();
//       rules.put(".*", true);
//       rules.put("Info.plist", this.createOmitAndWeight(true, 10));
//       rules.put(bundleResourceSpecification, this.createOmitAndWeight(true, 100));
//       root.put("rules", rules);
       
//       TFile bundleResourceSpecificationFile = (TFile) ipaContents.get(bundleResourceSpecification);
//       bundleResourceSpecificationFile.input(new ByteArrayInputStream(MyNSObjectSerializer.toXMLPropertyListBytesUTF8(root)));
       
       NSDictionary files = new NSDictionary();
       SHA1Digest digest = new SHA1Digest();
       ByteArrayOutputStream aux = new ByteArrayOutputStream();
       
       Set<String> ignoredFiles = new HashSet<String>();
       ignoredFiles.add("Info.plist");
       ignoredFiles.add("CodeResources");
       ignoredFiles.add("_CodeSignature/CodeResources");
//       ignoredFiles.add(bundleResourceSpecification);
       ignoredFiles.add(executableName);
       
       fillCodeResourcesFiles(appFolder, ignoredFiles, files, appFolder.getPath(), aux, digest);
       
       NSDictionary root = new NSDictionary();
       root.put("files", files);
       root.put("rules", rules);
       
       byte[] rootBytes = MyNSObjectSerializer.toXMLPropertyListBytesUTF8(root);
       TFile codeResources = new TFile((TFile) ipaContents.get("_CodeSignature"), "CodeResources");
       codeResources.input(new ByteArrayInputStream(rootBytes));
       
       return rootBytes;
   }
   
   private NSDictionary createOmitAndWeight(boolean omit, double weight)
   {
      NSDictionary dictionary = new NSDictionary();
      dictionary.put("omit", true);
      dictionary.put("weight", weight);
      return dictionary;
   }
   
   private void fillCodeResourcesFiles(TFile rootFile, final Set<String> ignoredFiles, final NSDictionary files, final String removePrefix, final ByteArrayOutputStream aux, final GeneralDigest digest)
   {
      rootFile.listFiles(new FilenameFilter()
      {
         public boolean accept(File arg0, String arg1)
         {
            TFile parent = (TFile) arg0;
            TFile file = new TFile(parent, arg1);
            String realFilePath = file.getPath().substring(removePrefix.length() + 1).replace('\\', '/');

            if (!ignoredFiles.contains(realFilePath))
            {
               try
               {
                  if (file.isDirectory())
                     fillCodeResourcesFiles(file, ignoredFiles, files, removePrefix, aux, digest);
                  else
                  {
                     aux.reset();
                     digest.reset();
                     file.output(aux);
                     byte[] b = aux.toByteArray();
                     digest.update(b, 0, b.length);
                     b = new byte[digest.getDigestSize()];
                     digest.doFinal(b, 0);
                     files.put(realFilePath, new NSData(new String(org.bouncycastle.util.encoders.Base64.encode(b))));
                  }
                  return true;
               }
               catch (IOException e)
               {
                  e.printStackTrace();
               }
            }
            return false;
         }
      });
   }   
}
