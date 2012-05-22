package tc.tools.deployer.ipa;

import java.io.IOException;

public class MachLoadCommandCodeSignature extends MachLoadCommand
{
   public SuperBlob payload;
   public long blobFileOffset;
   public long blobFileSize;

   private int offset2Start;

   public void PatchPositionAndSize(ElephantMemoryWriter writer, long NewOffset, long NewLength) throws IOException
   {
      this.blobFileOffset = NewOffset;
      this.blobFileSize = NewLength;
      writer.memorize();
      writer.moveTo(offset2Start);
      writer.writeUnsignedInt(this.blobFileOffset);
      writer.writeUnsignedInt(this.blobFileSize);
      writer.moveBack();
   }

   protected void unpackageData(ElephantMemoryReader reader) throws IOException
   {
      this.offset2Start = reader.getPos();
      this.blobFileOffset = (int) reader.readUnsignedInt();
      this.blobFileSize = (int) reader.readUnsignedInt();
      reader.memorize();
      reader.moveTo(this.blobFileOffset);
      reader.bStreamLittleEndian = false;
      this.payload = (SuperBlob) BlobCore.CreateFromStream(reader);
      reader.moveBack();
      reader.bStreamLittleEndian = true;
   }
}
