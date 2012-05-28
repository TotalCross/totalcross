package tc.tools.deployer.ipa;

import java.io.IOException;
import tc.tools.deployer.ipa.blob.BlobHandler;
import tc.tools.deployer.ipa.blob.EmbeddedSignature;

public class MachLoadCommandCodeSignature extends MachLoadCommand
{
   public EmbeddedSignature signature;
   public long blobFileOffset;
   public long blobFileSize;

   private int offset2FileSize;

   void updateFileSize(ElephantMemoryWriter writer, long filesize) throws IOException
   {
      this.blobFileSize = filesize;
      writer.memorize();
      writer.moveTo(offset2FileSize);
      writer.writeUnsignedInt(this.blobFileSize);
      writer.moveBack();
   }

   protected void parseFromStream(ElephantMemoryReader reader) throws IOException, InstantiationException,
         IllegalAccessException
   {
      this.blobFileOffset = (int) reader.readUnsignedInt();
      this.offset2FileSize = reader.getPos();
      this.blobFileSize = (int) reader.readUnsignedInt();
      reader.memorize();
      reader.moveTo(this.blobFileOffset);
      reader.bStreamLittleEndian = false;
      this.signature = (EmbeddedSignature) BlobHandler.readBlob(reader);
      reader.moveBack();
      reader.bStreamLittleEndian = true;
   }
}
