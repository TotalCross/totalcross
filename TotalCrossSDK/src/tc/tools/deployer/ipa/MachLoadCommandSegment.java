package tc.tools.deployer.ipa;
import java.io.IOException;

public class MachLoadCommandSegment extends MachLoadCommand
{
   public long FileOffset;
   public long FileSize;
   public long Flags;
   public long InitProt;
   public long MaxProt;
   public String SegmentName;
   public long VirtualAddress;
   public long VirtualSize;

   public void PatchFileLength(ElephantMemoryWriter writer, long NewLength) throws IOException
   {
      this.FileSize = NewLength;
      long newOffset = super.StartingLoadOffset + 8L;
      newOffset += 0x10L;
      newOffset += 12L;
      writer.memorize();
      writer.moveTo((int) newOffset);
      writer.writeUnsignedInt(this.FileSize);
      writer.moveBack();
   }

   protected void UnpackageData(ElephantMemoryReader reader, int CommandSize) throws IOException
   {
      this.SegmentName = reader.readString(16);
      this.VirtualAddress = reader.readUnsignedInt();
      this.VirtualSize = reader.readUnsignedInt();
      this.FileOffset = reader.readUnsignedInt();
      this.FileSize = reader.readUnsignedInt();
      this.MaxProt = reader.readUnsignedInt();
      this.InitProt = reader.readUnsignedInt();
      long num = reader.readUnsignedInt();
      this.Flags = reader.readUnsignedInt();
      reader.moveTo(reader.getPos() + (num * 68));
   }
}
