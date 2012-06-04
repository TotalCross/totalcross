package tc.tools.deployer.ipa;

import java.io.IOException;

public class MachLoadCommandSegment extends MachLoadCommand
{
   public String segname;
   public long vmaddr;
   public long vmsize;
   public long fileoff;
   public long filesize;
   public long maxprot;
   public long initprot;
   public long nsects;
   public long flags;

   private int offset2FileSize;

   void updateFileSize(ElephantMemoryWriter writer, long filesize) throws IOException
   {
      this.filesize = filesize;
      writer.memorize();
      writer.moveTo(offset2FileSize);
      writer.writeUnsignedIntLE(this.filesize);
      writer.moveBack();
   }

   protected void parseFromStream(ElephantMemoryReader reader) throws IOException
   {
      this.segname = reader.readString(16);
      this.vmaddr = reader.readUnsignedIntLE();
      this.vmsize = reader.readUnsignedIntLE();
      this.fileoff = reader.readUnsignedIntLE();
      this.offset2FileSize = reader.getPos();
      this.filesize = reader.readUnsignedIntLE();
      this.maxprot = reader.readUnsignedIntLE();
      this.initprot = reader.readUnsignedIntLE();
      this.nsects = reader.readUnsignedIntLE();
      this.flags = reader.readUnsignedIntLE();
      reader.moveTo(reader.getPos() + (nsects * 68));
   }
}
