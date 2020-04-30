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

package tc.tools.deployer.ipa.blob;

import java.io.IOException;

import tc.tools.deployer.ipa.ElephantMemoryReader;
import tc.tools.deployer.ipa.ElephantMemoryWriter;

public class BlobCore {
  protected long magic;
  protected long length;
  protected long offset;

  protected byte[] data;

  protected BlobCore(long magic) {
    this.magic = magic;
  }

  public byte[] getBytes() throws IOException {
    ElephantMemoryWriter writer = new ElephantMemoryWriter();
    BlobHandler.writeBlob(this, writer);
    return writer.toByteArray();
  }

  protected void writeToStream(ElephantMemoryWriter writer) throws IOException {
    writer.write(this.data);
  }

  protected void readFromStream(ElephantMemoryReader reader)
      throws IOException, InstantiationException, IllegalAccessException {
    reader.skip(length - 8);
  }
}
