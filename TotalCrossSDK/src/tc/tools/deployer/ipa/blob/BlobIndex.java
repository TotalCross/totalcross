package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import tc.tools.deployer.ipa.ElephantMemoryReader;
import tc.tools.deployer.ipa.ElephantMemoryWriter;

public class BlobIndex
{
   public static final int CSSLOT_CODEDIRECTORY = 0;
   public static final int CSSLOT_REQUIREMENTS = 2;
   public static final int CSSLOT_ENTITLEMENTS = 5;
   public static final int CSSLOT_SIGNATURE = 0x10000;

   public long blobType;
   public long offset;
   public BlobCore blob;

   BlobIndex(long blobType, BlobCore blob)
   {
      this.blobType = blobType;
      this.blob = blob;
   }

   static BlobIndex readObject(ElephantMemoryReader reader, long baseOffset) throws IOException,
         InstantiationException, IllegalAccessException
   {
      long blobType = reader.readUnsignedInt();
      long offset = reader.readUnsignedInt();

      reader.memorize();
      reader.moveTo(baseOffset + offset);
      BlobCore blob = BlobHandler.readFromStream(reader);
      reader.moveBack();

      return new BlobIndex(blobType, blob);
   }

   static void writeObject(ElephantMemoryWriter writer)
   {

   }
}
