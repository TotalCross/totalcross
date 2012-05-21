package tc.tools.deployer.ipa;
import java.io.IOException;

public class MachLoadCommandSegment extends MachLoadCommand
{
   public long fileOffset;
   public long fileSize;
   public long flags;
   public long initProt;
   public long maxProt;
   public String segmentName;
   public long virtualAddress;
   public long virtualSize;

   public void PatchFileLength(ElephantMemoryWriter writer, long newLength) throws IOException
   {
      this.fileSize = newLength;
      long newOffset = super.StartingLoadOffset + 8L;
      newOffset += 0x10L;
      newOffset += 12L;
      writer.memorize();
      writer.moveTo((int) newOffset);
      writer.writeUnsignedInt(this.fileSize);
      writer.moveBack();
   }

   protected void UnpackageData(ElephantMemoryReader reader, int CommandSize) throws IOException
   {
      this.segmentName = reader.readString(16);
      this.virtualAddress = reader.readUnsignedInt();
      this.virtualSize = reader.readUnsignedInt();
      this.fileOffset = reader.readUnsignedInt();
      this.fileSize = reader.readUnsignedInt();
      this.maxProt = reader.readUnsignedInt();
      this.initProt = reader.readUnsignedInt();
      long num = reader.readUnsignedInt();
      this.flags = reader.readUnsignedInt();
      reader.moveTo(reader.getPos() + (num * 68));
   }
}
