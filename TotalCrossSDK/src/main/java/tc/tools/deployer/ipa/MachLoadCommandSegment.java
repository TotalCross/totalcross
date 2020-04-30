// Copyright (C) 2012 SuperWaba Ltda.
// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.ipa;

import java.io.IOException;

public class MachLoadCommandSegment extends MachLoadCommand {
  protected String segname;
  protected long vmaddr;
  protected long vmsize;
  protected long fileoff;
  protected long filesize;
  protected long maxprot;
  protected long initprot;
  protected long nsects;
  protected long flags;

  protected int offset2FileSize;

  void updateFileSize(ElephantMemoryWriter writer, long filesize) throws IOException {
    this.filesize = filesize;
    writer.memorize();
    writer.moveTo(offset2FileSize);
    writer.writeUnsignedIntLE(this.filesize);
    writer.moveBack();
  }

  @Override
  protected void parseFromStream(ElephantMemoryReader reader) throws IOException {
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
