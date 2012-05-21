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

   private int offset2FileSize;

   public void PatchFileLength(ElephantMemoryWriter writer, long newLength) throws IOException
   {
      this.fileSize = newLength;
      writer.memorize();
      writer.moveTo(offset2FileSize);
      writer.writeUnsignedInt(this.fileSize);
      writer.moveBack();
   }

   protected void unpackageData(ElephantMemoryReader reader) throws IOException
   {
      this.segmentName = reader.readString(16);
      this.virtualAddress = reader.readUnsignedInt();
      this.virtualSize = reader.readUnsignedInt();
      this.fileOffset = reader.readUnsignedInt();
      this.offset2FileSize = reader.getPos();
      this.fileSize = reader.readUnsignedInt();
      this.maxProt = reader.readUnsignedInt();
      this.initProt = reader.readUnsignedInt();
      long num = reader.readUnsignedInt();
      this.flags = reader.readUnsignedInt();
      reader.moveTo(reader.getPos() + (num * 68));
   }
}
