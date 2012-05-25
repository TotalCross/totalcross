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

   public void PatchFileLength(ElephantMemoryWriter writer, long newLength) throws IOException
   {
      this.filesize = filesize;
      writer.memorize();
      writer.moveTo(offset2FileSize);
      writer.writeUnsignedInt(this.filesize);
      writer.moveBack();
   }

   protected void parseFromStream(ElephantMemoryReader reader) throws IOException
   {
      this.segname = reader.readString(16);
      this.vmaddr = reader.readUnsignedInt();
      this.vmsize = reader.readUnsignedInt();
      this.fileoff = reader.readUnsignedInt();
      this.offset2FileSize = reader.getPos();
      this.filesize = reader.readUnsignedInt();
      this.maxprot = reader.readUnsignedInt();
      this.initprot = reader.readUnsignedInt();
      this.nsects = reader.readUnsignedInt();
      this.flags = reader.readUnsignedInt();
      reader.moveTo(reader.getPos() + (nsects * 68));
   }
}
