package tc.tools.deployer.ipa;

import java.io.IOException;

public class MachLoadCommandSegment64 extends MachLoadCommandSegment {
  @Override
  void updateFileSize(ElephantMemoryWriter writer, long filesize) throws IOException {
    this.filesize = filesize;
    writer.memorize();
    writer.moveTo(offset2FileSize);
    writer.writeUnsignedLongLE(this.filesize);
    writer.moveBack();
  }

  @Override
  protected void parseFromStream(ElephantMemoryReader reader) throws IOException {
    this.segname = reader.readString(16);
    this.vmaddr = reader.readUnsignedLongLE();
    this.vmsize = reader.readUnsignedLongLE();
    this.fileoff = reader.readUnsignedLongLE();
    this.offset2FileSize = reader.getPos();
    this.filesize = reader.readUnsignedLongLE();
    this.maxprot = reader.readUnsignedIntLE();
    this.initprot = reader.readUnsignedIntLE();
    this.nsects = reader.readUnsignedIntLE();
    this.flags = reader.readUnsignedIntLE();
    reader.moveTo(reader.getPos() + (nsects * 80));
  }
}
