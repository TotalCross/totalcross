package tc.tools.deployer.ipa;

import java.io.IOException;
import tc.tools.deployer.ipa.blob.BlobCore;
import tc.tools.deployer.ipa.blob.SuperBlob;

public class MachLoadCommandCodeSignature extends MachLoadCommand
{
   public SuperBlob payload;
   public long blobFileOffset;
   public long blobFileSize;

   private int offset2FileSize;

   public void PatchPositionAndSize(ElephantMemoryWriter writer, long NewLength) throws IOException
   {
      this.blobFileSize = NewLength;
      writer.memorize();
      writer.moveTo(offset2FileSize);
      writer.writeUnsignedInt(this.blobFileSize);
      writer.moveBack();
   }

   protected void unpackageData(ElephantMemoryReader reader) throws IOException
   {
      this.blobFileOffset = (int) reader.readUnsignedInt();
      this.offset2FileSize = reader.getPos();
      this.blobFileSize = (int) reader.readUnsignedInt();
      reader.memorize();
      reader.moveTo(this.blobFileOffset);
      reader.bStreamLittleEndian = false;
      this.payload = (SuperBlob) BlobCore.CreateFromStream(reader);
      reader.moveBack();
      reader.bStreamLittleEndian = true;
   }
}
