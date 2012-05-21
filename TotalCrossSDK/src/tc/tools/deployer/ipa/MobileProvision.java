package tc.tools.deployer.ipa;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.cert.CertificateFactory;
import org.apache.commons.io.FileUtils;
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

   public String getBundleIdentifier()
   {
      NSDictionary entitlements = (NSDictionary) this.Data.objectForKey("Entitlements");
      String bundleIdentifier = entitlements.objectForKey("application-identifier").toString();
      return bundleIdentifier.substring(bundleIdentifier.indexOf('.') + 1);
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

   public static MobileProvision readFromFile(File input) throws Exception
   {
      byte[] inputData = FileUtils.readFileToByteArray(input);
      String inputString = new String(inputData, "UTF-8");

      int startIdx = inputString.indexOf("<?xml");
      if (startIdx == -1)
         return null;

      int length = (inputData[startIdx - 2] << 8) | inputData[startIdx - 1];
      int endIdx = inputString.lastIndexOf('>', length + startIdx) + 1;
      inputString = inputString.substring(startIdx, endIdx);

      return new MobileProvision(inputString);
   }
}
