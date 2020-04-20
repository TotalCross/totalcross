/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.              *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
 *                                                                               *
 *********************************************************************************/

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
