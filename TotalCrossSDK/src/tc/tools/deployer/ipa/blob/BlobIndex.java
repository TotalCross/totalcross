package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import tc.tools.deployer.ipa.ElephantMemoryReader;

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

   BlobIndex(ElephantMemoryReader reader, long baseOffset) throws IOException
   {
      blobType = reader.readUnsignedInt();
      offset = reader.readUnsignedInt();

      reader.memorize();
      reader.moveTo(baseOffset + offset);
      blob = BlobCore.CreateFromStream(reader);
      reader.moveBack();
   }
}
