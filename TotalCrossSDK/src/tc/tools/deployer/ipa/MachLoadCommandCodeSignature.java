package tc.tools.deployer.ipa;
import java.io.IOException;

public class MachLoadCommandCodeSignature extends MachLoadCommand
{
   public SuperBlob Payload;
   public long BlobFileOffset;
   public long BlobFileSize;

   public void PatchPositionAndSize(ElephantMemoryWriter writer, long NewOffset, long NewLength) throws IOException
   {
      this.BlobFileOffset = NewOffset;
      this.BlobFileSize = NewLength;
      long newOffset = this.StartingLoadOffset + 8L;
      writer.memorize();
      writer.moveTo((int) newOffset);
      writer.writeUnsignedInt(this.BlobFileOffset);
      writer.writeUnsignedInt(this.BlobFileSize);
      writer.moveBack();
   }

   protected void UnpackageData(ElephantMemoryReader reader, int CommandSize) throws IOException
   {
      this.BlobFileOffset = (int) reader.readUnsignedInt();
      this.BlobFileSize = (int) reader.readUnsignedInt();
      reader.memorize();
      reader.moveTo(this.BlobFileOffset);
      reader.bStreamLittleEndian = false;
      this.Payload = (SuperBlob) AbstractBlob.CreateFromStream(reader);
      reader.moveBack();
      reader.bStreamLittleEndian = true;
   }
}
