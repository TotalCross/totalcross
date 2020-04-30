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

public class MachObjectFile64 extends MachObjectFile {
  protected long reserved;

  protected MachObjectFile64(byte[] data) throws IOException, InstantiationException, IllegalAccessException {
    super(data);
  }

  @Override
  protected void readHeader(ElephantMemoryReader reader) throws IOException {
    super.readHeader(reader);
    this.reserved = reader.readUnsignedIntLE();
  }
}
