package tc.tools.deployer.ipa;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.jce.provider.X509CertificateObject;
import com.dd.plist.*;

public class MobileProvision
{
   public String ApplicationIdentifierPrefix;
   public NSDictionary Data;
   public List DeveloperCertificates = new ArrayList();
   public List ProvisionedDeviceIDs;
   public String ProvisionName;
   public Object Tag;

   public MobileProvision(String EmbeddedPListText) throws Exception
   {
      this.Data = (NSDictionary) PropertyListParser.parse(EmbeddedPListText.getBytes());
      NSArray array = (NSArray) Data.objectForKey("ApplicationIdentifierPrefix");
      if (array.count() > 0)
      {
         this.ApplicationIdentifierPrefix = array.objectAtIndex(0).toString();
      }

      CertificateFactory cf = CertificateFactory.getInstance("X509", "BC");
      NSObject[] certificates = ((NSArray) Data.objectForKey("DeveloperCertificates")).getArray();
      for (int i = 0 ; i < certificates.length ; i++)
      {
         NSData itemData = (NSData) certificates[i];
         byte[] rawData = itemData.bytes();
         X509CertificateObject x509certificate = (X509CertificateObject) cf.generateCertificate(new ByteArrayInputStream(rawData, 0, rawData.length));
         this.DeveloperCertificates.add(x509certificate);
      }
      NSObject item = Data.objectForKey("Name");
      this.ProvisionName = item != null ? item.toString() : "(unknown)";

      this.ProvisionedDeviceIDs = new ArrayList();
      array = (NSArray) Data.objectForKey("ProvisionedDevices");
      if (array != null && array.count() > 0)
      {
         NSObject[] devices = array.getArray();
         for (int i = 0 ; i < devices.length ; i++)
            this.ProvisionedDeviceIDs.add(devices[i].toString());
      }
   }

   public String GetEntitlementsString()
   {
      NSDictionary XCentPList = new NSDictionary();
      NSDictionary entitlements = (NSDictionary) this.Data.objectForKey("Entitlements");
      String[] keys = entitlements.allKeys();
      for (int i = 0 ; i < keys.length ; i++)
      {
         String key = keys[i];
         NSObject item = entitlements.objectForKey(key);
         XCentPList.put(key, item);
      }
      return XCentPList.toXMLPropertyList();
   }

   public static MobileProvision ParseFile(byte[] RawData) throws Exception
   {
      byte[] bytes = "<?xml".getBytes("UTF-8");
      for (int i = 2; i < (RawData.length - bytes.length); i++)
      {
         boolean flag = true;
         for (int j = 0; flag && (j < bytes.length); j++)
         {
            flag = flag && (RawData[i + j] == bytes[j]);
         }
         if (flag)
         {
            int count = (RawData[i - 2] << 8) | RawData[i - 1];
            String str = new String(RawData, i, count, "UTF-8");
            int num4 = str.lastIndexOf('>');
            return new MobileProvision(str.substring(0, num4 + 1));
         }
      }
      return null;
   }
}
