package tc.tools.deployer.ipa;
import java.io.IOException;

public class MachLoadCommandCodeSignature extends MachLoadCommand
{
   public SuperBlob payload;
   public long blobFileOffset;
   public long blobFileSize;

   public void PatchPositionAndSize(ElephantMemoryWriter writer, long NewOffset, long NewLength) throws IOException
   {
      this.blobFileOffset = NewOffset;
      this.blobFileSize = NewLength;
      long newOffset = this.StartingLoadOffset + 8L;
      writer.memorize();
      writer.moveTo((int) newOffset);
      writer.writeUnsignedInt(this.blobFileOffset);
      writer.writeUnsignedInt(this.blobFileSize);
      writer.moveBack();
   }

   protected void UnpackageData(ElephantMemoryReader reader, int CommandSize) throws IOException
   {
      this.blobFileOffset = (int) reader.readUnsignedInt();
      this.blobFileSize = (int) reader.readUnsignedInt();
      reader.memorize();
      reader.moveTo(this.blobFileOffset);
      reader.bStreamLittleEndian = false;
      this.payload = (SuperBlob) AbstractBlob.CreateFromStream(reader);
      reader.moveBack();
      reader.bStreamLittleEndian = true;
   }
}
