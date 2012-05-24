package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.*;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.x509.NoSuchStoreException;
import org.bouncycastle.x509.X509Store;

public class CodeDirectorySignatureBlob extends BlobCore
{
   /**
    * https://bitbucket.org/khooyp/gdb/src/c3a263c415ad/include/mach-o/codesign.h
    * http://www.opensource.apple.com/source/libsecurity_utilities/libsecurity_utilities-55010/lib/blob.h
    */
   public static final long CSMAGIC_BLOB_WRAPPER = 0xfade0b01;

   public CodeDirectorySignatureBlob()
   {
      super(CSMAGIC_BLOB_WRAPPER);
   }

   List certs;
   Certificate[] certChain = null;

   public void SignCodeDirectory(KeyStore ks, X509Store certStore, CodeDirectory CodeDirectory) throws IOException,
         NoSuchStoreException, CMSException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException,
         OperatorCreationException, CertificateEncodingException
   {
      CMSSignedDataGenerator cmsGen = new CMSSignedDataGenerator();
      String firstAlias = (String) ks.aliases().nextElement();
      PrivateKey priv = (PrivateKey) (ks.getKey(firstAlias, "".toCharArray()));
      Certificate storecert = ks.getCertificate(firstAlias);

      ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(priv);
      DigestCalculatorProvider digestCalculatorProvider = new JcaDigestCalculatorProviderBuilder().setProvider("BC")
            .build();
      SignerInfoGenerator signerInfoGenerator = new JcaSignerInfoGeneratorBuilder(digestCalculatorProvider).build(
            sha1Signer, new X509CertificateHolder(storecert.getEncoded()));

      cmsGen.addSignerInfoGenerator(signerInfoGenerator);
      cmsGen.addCertificates(certStore);

      byte[] bData = CodeDirectory.GetBlobBytes();
      CMSProcessableByteArray content = (bData != null) ? new CMSProcessableByteArray(bData) : null;

      CMSSignedData sign = cmsGen.generate(content, false);

      super.data = sign.toASN1Structure().getEncoded("DER");
   }
}
