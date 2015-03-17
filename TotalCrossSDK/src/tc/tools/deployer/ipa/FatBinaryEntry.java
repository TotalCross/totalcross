package tc.tools.deployer.ipa;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.x509.X509Store;

public class FatBinaryEntry
{
   protected long cputype;
   protected long cpusubtype;
   protected long offset;
   protected long size;
   protected long alignment;
   protected AppleBinary file;

   public FatBinaryEntry(ElephantMemoryReader reader) throws IOException, InstantiationException, IllegalAccessException
   {
      cputype = reader.readUnsignedInt();
      cpusubtype = reader.readUnsignedInt();
      offset = reader.readUnsignedInt();
      size = reader.readUnsignedInt();
      alignment = reader.readUnsignedInt();

      file = AppleBinary.create(reader.copy((int) offset, (int) (offset + size)));
   }

   public void resign(ElephantMemoryWriter writer, KeyStore ks, X509Store certStore, String bundleIdentifier,
         byte[] entitlementsBytes, byte[] info, byte[] sourceData) throws UnrecoverableKeyException,
         CertificateEncodingException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException, IOException,
         CMSException
   {
      writer.align((int) offset);
      writer.write(file.resign(ks, certStore, bundleIdentifier, entitlementsBytes, info, sourceData));
   }
}
