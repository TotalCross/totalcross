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

package tc.tools.deployer.ipa;

import java.io.IOException;

import tc.tools.deployer.ipa.blob.BlobHandler;
import tc.tools.deployer.ipa.blob.EmbeddedSignature;

public class MachLoadCommandCodeSignature extends MachLoadCommand {
  protected EmbeddedSignature signature;
  protected long blobFileOffset;
  protected long blobFileSize;

  private int offset2FileSize;

  void updateFileSize(ElephantMemoryWriter writer, long filesize) throws IOException {
    this.blobFileSize = filesize;
    writer.memorize();
    writer.moveTo(offset2FileSize);
    writer.writeUnsignedIntLE(this.blobFileSize);
    writer.moveBack();
  }

  @Override
  protected void parseFromStream(ElephantMemoryReader reader)
      throws IOException, InstantiationException, IllegalAccessException {
    this.blobFileOffset = (int) reader.readUnsignedIntLE();
    this.offset2FileSize = reader.getPos();
    this.blobFileSize = (int) reader.readUnsignedIntLE();
    reader.memorize();
    reader.moveTo(this.blobFileOffset);
    this.signature = (EmbeddedSignature) BlobHandler.readBlob(reader);
    reader.moveBack();
  }
}
