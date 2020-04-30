// Copyright (C) 2012 SuperWaba Ltda.
// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509CollectionStoreParameters;
import org.bouncycastle.x509.X509Store;

import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import tc.tools.deployer.ipa.AppleBinary;
import tc.tools.deployer.ipa.MobileProvision;
import tc.tools.deployer.ipa.MyNSObjectSerializer;

public class InvalidateIPA {
  private Map<String, TFile> ipaContents = new HashMap<String, TFile>();

  private MobileProvision Provision;
  private File appleRootCA;
  private File appleWWDRCA;
  private String etcDir;
  private File appleCertStore;
  private File dummyProvision;

  private InvalidateIPA(String etcDir) {
    this.etcDir = etcDir;
  }

  public static void main(String[] args) throws Exception {
    InvalidateIPA invalidator = new InvalidateIPA(args[0]);
    for (int i = 1; i < args.length; i++) {
      invalidator.invalidate(args[i]);
    }
  }

  private void invalidate(String ipaPath) throws Exception {
    // initialize bouncy castle
    Security.addProvider(new BouncyCastleProvider());

    appleRootCA = new File(etcDir, "ipa/AppleRootCA.pem");
    appleWWDRCA = new File(etcDir, "ipa/AppleWWDRCA.pem");
    appleCertStore = new File(etcDir, "ipa/dummyStore.p12");
    dummyProvision = new File(etcDir, "ipa/dummy.mobileprovision");

    // locate template and target
    File templateFile = new File(ipaPath);
    File targetFile = File.createTempFile("DummyIPA", ".zip");
    targetFile.deleteOnExit();
    // create a copy of the original file
    FileUtils.copyFile(templateFile, targetFile);

    // open new file with truezip
    TFile targetZip = new TFile(targetFile);

    // get the payload folder from the zip
    targetZip.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        ipaContents.put(name, new TFile(dir, name));
        return false;
      }
    });
    TFile payload = (TFile) ipaContents.get("Payload");

    // get the template appFolder - TotalCross.app
    payload.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        ipaContents.put(name, new TFile(dir, name));
        return false;
      }
    });
    TFile appFolder = (TFile) ipaContents.get("TotalCross.app");

    // get references to the contents of the appFolder
    appFolder.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        ipaContents.put(name, new TFile(dir, name));
        return false;
      }
    });

    /** PROCESS CERTIFICATE **/
    // install certificates
    CertificateFactory cf = CertificateFactory.getInstance("X509", "BC");
    X509CertificateHolder[] certs = new X509CertificateHolder[3];
    certs[0] = new X509CertificateHolder(
        cf.generateCertificate(new ByteArrayInputStream(FileUtils.readFileToByteArray(appleRootCA))).getEncoded());
    certs[1] = new X509CertificateHolder(
        cf.generateCertificate(new ByteArrayInputStream(FileUtils.readFileToByteArray(appleWWDRCA))).getEncoded());

    KeyStore ks = java.security.KeyStore.getInstance("PKCS12", "BC");
    ks.load(new FileInputStream(appleCertStore), "".toCharArray());

    String keyAlias = (String) ks.aliases().nextElement();
    Certificate storecert = ks.getCertificate(keyAlias);
    if (storecert == null) {
      File[] certsInPath = appleCertStore.getParentFile().listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File arg0, String arg1) {
          return arg1.endsWith(".cer");
        }
      });
      if (certsInPath.length == 0) {
        throw new Exception("Distribution certificate was not found in " + appleCertStore.getParent());
      }

      storecert = cf.generateCertificate(new ByteArrayInputStream(FileUtils.readFileToByteArray(certsInPath[0])));
      PrivateKey pk = (PrivateKey) ks.getKey(keyAlias, "".toCharArray());
      ks.deleteEntry(keyAlias);
      ks.setEntry(keyAlias, new KeyStore.PrivateKeyEntry(pk, new Certificate[] { storecert }),
          new KeyStore.PasswordProtection("".toCharArray()));
    }
    certs[2] = new X509CertificateHolder(storecert.getEncoded());

    X509Store certStore = X509Store.getInstance("CERTIFICATE/Collection",
        new X509CollectionStoreParameters(Arrays.asList(certs)), "BC");

    /** PROCESS MOBILE PROVISION **/
    // update the mobile provision
    Provision = MobileProvision.readFromFile(dummyProvision);

    /** PROCESS INFO.PLIST **/
    // read the info.plist from the zip file
    TFile infoPlist = (TFile) ipaContents.get("Info.plist");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    infoPlist.output(baos);
    NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(baos.toByteArray());
    String executableName = rootDict.objectForKey("CFBundleExecutable").toString();

    String bundleIdentifier = "DUMMY";
    rootDict.put("CFBundleIdentifier", bundleIdentifier);

    // overwrite updated info.plist inside the zip file
    byte[] updatedInfoPlist = MyNSObjectSerializer.toXMLPropertyListBytesUTF8(rootDict);
    infoPlist.input(new ByteArrayInputStream(updatedInfoPlist));

    // provision
    TFile mobileProvision = (TFile) ipaContents.get("embedded.mobileprovision");
    mobileProvision.input(new ByteArrayInputStream(FileUtils.readFileToByteArray(dummyProvision)));

    /** CREATE THE MACHOBJECTFILE **/
    TFile executable = (TFile) ipaContents.get(executableName);
    String bundleResourceSpecification = rootDict.objectForKey("CFBundleResourceSpecification").toString();
    byte[] sourceData = CreateCodeResourcesDirectory(appFolder, bundleResourceSpecification, executableName);
    ByteArrayOutputStream appStream = new ByteArrayOutputStream();
    executable.output(appStream);

    AppleBinary file = AppleBinary.create(appStream.toByteArray());
    // executable
    executable.input(new ByteArrayInputStream(file.resign(ks, certStore, bundleIdentifier,
        Provision.GetEntitlementsString().getBytes("UTF-8"), updatedInfoPlist, sourceData)));

    TVFS.umount(targetZip);

    FileUtils.copyFile(targetFile, templateFile);
  }

  private byte[] CreateCodeResourcesDirectory(TFile appFolder, final String bundleResourceSpecification,
      final String executableName) throws UnsupportedEncodingException, IOException {
    NSDictionary root = new NSDictionary();

    NSDictionary rules = new NSDictionary();
    rules.put(".*", true);
    rules.put("Info.plist", createOmitAndWeight(true, 10));
    rules.put(bundleResourceSpecification, createOmitAndWeight(true, 100));
    root.put("rules", rules);

    TFile bundleResourceSpecificationFile = (TFile) ipaContents.get(bundleResourceSpecification);
    bundleResourceSpecificationFile
        .input(new ByteArrayInputStream(MyNSObjectSerializer.toXMLPropertyListBytesUTF8(root)));

    NSDictionary files = new NSDictionary();
    SHA1Digest digest = new SHA1Digest();
    ByteArrayOutputStream aux = new ByteArrayOutputStream();

    Set<String> ignoredFiles = new HashSet<String>();
    ignoredFiles.add("Info.plist");
    ignoredFiles.add("CodeResources");
    ignoredFiles.add("_CodeSignature/CodeResources");
    ignoredFiles.add(bundleResourceSpecification);
    ignoredFiles.add(executableName);

    fillCodeResourcesFiles(appFolder, ignoredFiles, files, appFolder.getPath(), aux, digest);

    root = new NSDictionary();
    root.put("files", files);
    root.put("rules", rules);

    byte[] rootBytes = MyNSObjectSerializer.toXMLPropertyListBytesUTF8(root);
    TFile codeResources = new TFile((TFile) ipaContents.get("_CodeSignature"), "CodeResources");
    codeResources.input(new ByteArrayInputStream(rootBytes));

    return rootBytes;
  }

  private NSDictionary createOmitAndWeight(boolean omit, double weight) {
    NSDictionary dictionary = new NSDictionary();
    dictionary.put("omit", true);
    dictionary.put("weight", weight);
    return dictionary;
  }

  private void fillCodeResourcesFiles(TFile rootFile, final Set<String> ignoredFiles, final NSDictionary files,
      final String removePrefix, final ByteArrayOutputStream aux, final GeneralDigest digest) {
    rootFile.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File arg0, String arg1) {
        TFile parent = (TFile) arg0;
        TFile file = new TFile(parent, arg1);
        String realFilePath = file.getPath().substring(removePrefix.length() + 1).replace('\\', '/');

        if (!ignoredFiles.contains(realFilePath)) {
          try {
            if (file.isDirectory()) {
              fillCodeResourcesFiles(file, ignoredFiles, files, removePrefix, aux, digest);
            } else {
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
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        return false;
      }
    });
  }
}
