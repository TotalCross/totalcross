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

package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.x509.X509Store;

public class BlobWrapper extends BlobCore {
  /**
   * https://bitbucket.org/khooyp/gdb/src/c3a263c415ad/include/mach-o/codesign.h
   * http://www.opensource.apple.com/source/libsecurity_utilities/libsecurity_utilities-55010/lib/blob.h
   */
  public static final long CSMAGIC_BLOB_WRAPPER = 0xfade0b01;

  private CodeDirectory codeDirectory;

  private CMSSignedDataGenerator signedDataGenerator;

  public BlobWrapper() {
    super(CSMAGIC_BLOB_WRAPPER);
  }

  public BlobWrapper(KeyStore keyStore, X509Store certStore, CodeDirectory codeDirectory)
      throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateEncodingException,
      OperatorCreationException, IOException, CMSException {
    super(CSMAGIC_BLOB_WRAPPER);
    this.codeDirectory = codeDirectory;

    signedDataGenerator = new CMSSignedDataGenerator();
    String firstAlias = (String) keyStore.aliases().nextElement();
    PrivateKey priv = (PrivateKey) (keyStore.getKey(firstAlias, "".toCharArray()));

    X509CertificateHolder cert = new X509CertificateHolder(keyStore.getCertificate(firstAlias).getEncoded());
    ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(priv);
    DigestCalculatorProvider digestCalculator = new JcaDigestCalculatorProviderBuilder().setProvider("BC").build();
    SignerInfoGenerator signerGenerator = new JcaSignerInfoGeneratorBuilder(digestCalculator).build(sha1Signer, cert);

    signedDataGenerator.addSignerInfoGenerator(signerGenerator);
    signedDataGenerator.addCertificates(certStore);

    sign();
  }

  public void sign() throws IOException, CMSException {
    byte[] rawData = codeDirectory.getBytes();
    CMSProcessableByteArray content = (rawData != null) ? new CMSProcessableByteArray(rawData) : null;
    CMSSignedData sign = signedDataGenerator.generate(content, false);
    super.data = sign.toASN1Structure().getEncoded("DER");
  }
}
